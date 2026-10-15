package com.occultum.common.altar;

import com.mojang.serialization.MapCodec;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AltarBlock extends BaseEntityBlock {
    public static final MapCodec<AltarBlock> CODEC = simpleCodec(AltarBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public AltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, ModBlockEntities.ALTAR_BE.get(), AltarBlockEntity::clientTick);
        } else {
            return createTickerHelper(blockEntityType, ModBlockEntities.ALTAR_BE.get(), AltarBlockEntity::serverTick);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && !state.getValue(FORMED)) {
            tryFormMultiblock(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(FORMED)) {
                destroyMultiblock(level, pos);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!state.getValue(FORMED)) {
            // Tenta formar caso ainda não esteja formado
            if (!level.isClientSide) {
                if (tryFormMultiblock(level, pos)) {
                    player.displayClientMessage(Component.literal("§aO Altar das Bruxas despertou!"), true);
                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(Component.literal("§cEstrutura 2x3 incompleta ou invalida."), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide) {
            AltarBlockEntity core = findCore(level, pos);
            if (core != null) {
                // Seção 7.3: Força a atualização imediata dos cálculos ambientais e dos artefatos
                core.scanEnvironment(true);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.openMenu(new SimpleMenuProvider(
                            (id, inv, p) -> new AltarMenu(id, inv, core),
                            Component.literal("Altar")
                    ), core.getBlockPos());
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static @Nullable AltarBlockEntity findCore(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AltarBlockEntity altar && altar.isFormed()) {
            if (altar.isCore()) return altar;
            // Busca o bloco central
            for (BlockPos memberPos : altar.getAltarBlocks()) {
                BlockEntity mBe = level.getBlockEntity(memberPos);
                if (mBe instanceof AltarBlockEntity mAltar && mAltar.isCore()) {
                    return mAltar;
                }
            }
        }
        return null;
    }

    public static boolean tryFormMultiblock(Level level, BlockPos startPos) {
        // Encontra todos os blocos de altar contíguos no mesmo nível Y
        Set<BlockPos> connected = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        connected.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(dir);
                if (!connected.contains(next) && level.getBlockState(next).is(ModBlocks.ALTAR_BLOCK.get())) {
                    connected.add(next);
                    queue.add(next);
                    if (connected.size() > 6) {
                        return false; // Mais de 6 blocos contíguos
                    }
                }
            }
        }

        if (connected.size() != 6) {
            return false;
        }

        // Validação topológica de 2x3 ou 3x2:
        // Exatamente 4 blocos das pontas possuem 2 vizinhos horizontais.
        // Exatamente 2 blocos centrais possuem 3 vizinhos horizontais.
        int cornerCount = 0;
        int centerCount = 0;

        for (BlockPos bPos : connected) {
            int neighbors = 0;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (connected.contains(bPos.relative(dir))) {
                    neighbors++;
                }
            }
            if (neighbors == 2) {
                cornerCount++;
            } else if (neighbors == 3) {
                centerCount++;
            } else {
                return false; // Geometria inválida (1 vizinho ou 4 vizinhos)
            }
        }

        if (cornerCount != 4 || centerCount != 2) {
            return false;
        }

        // Escolhe o bloco com menor X e menor Z como Core / Núcleo
        List<BlockPos> memberList = new ArrayList<>(connected);
        memberList.sort((a, b) -> {
            int cmpX = Integer.compare(a.getX(), b.getX());
            return cmpX != 0 ? cmpX : Integer.compare(a.getZ(), b.getZ());
        });
        BlockPos corePos = memberList.get(0);

        // Ativa os 6 blocos
        for (BlockPos memberPos : memberList) {
            BlockState currentState = level.getBlockState(memberPos);
            level.setBlock(memberPos, currentState.setValue(FORMED, true), 3);
            BlockEntity be = level.getBlockEntity(memberPos);
            if (be instanceof AltarBlockEntity altar) {
                altar.formAltar(corePos, memberList);
            }
        }

        return true;
    }

    public static void destroyMultiblock(Level level, BlockPos brokenPos) {
        BlockEntity be = level.getBlockEntity(brokenPos);
        List<BlockPos> membersToInvalidate = new ArrayList<>();

        if (be instanceof AltarBlockEntity altar && altar.isFormed()) {
            membersToInvalidate.addAll(altar.getAltarBlocks());
        } else {
            // Varre vizinhança horizontal próxima
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = brokenPos.offset(dx, 0, dz);
                    if (level.getBlockState(p).is(ModBlocks.ALTAR_BLOCK.get())) {
                        membersToInvalidate.add(p);
                    }
                }
            }
        }

        for (BlockPos memberPos : membersToInvalidate) {
            BlockState state = level.getBlockState(memberPos);
            if (state.is(ModBlocks.ALTAR_BLOCK.get()) && state.getValue(FORMED)) {
                level.setBlock(memberPos, state.setValue(FORMED, false), 3);
            }
            BlockEntity mBe = level.getBlockEntity(memberPos);
            if (mBe instanceof AltarBlockEntity mAltar) {
                mAltar.invalidateAltar();
            }
        }
    }
}
