package com.occultum.common.poppet;

import com.mojang.serialization.MapCodec;
import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Prateleira de Poppets (Poppet Shelf - Seção 4 de 06_POPPETS_E_FETISHES.md):
 * - Capacidade: 9 slots dedicados para Poppets.
 * - Chunkloader Integrado no servidor.
 * - Alcance Dimensional Global.
 */
public class PoppetShelfBlock extends BaseEntityBlock {
    public static final MapCodec<PoppetShelfBlock> CODEC = simpleCodec(PoppetShelfBlock::new);
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public PoppetShelfBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PoppetShelfBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.POPPET_SHELF_BE.get(), PoppetShelfBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
            if (placer instanceof Player p) {
                shelf.setOwnerUuid(p.getUUID().toString());
            }
            shelf.enableChunkLoading();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
            if (stack.getItem() instanceof PoppetItem) {
                if (shelf.tryInsertPoppet(stack)) {
                    return ItemInteractionResult.SUCCESS;
                }
            } else if (stack.isEmpty() && player.isShiftKeyDown()) {
                shelf.tryExtractLastPoppet(player);
                return ItemInteractionResult.SUCCESS;
            }
            player.openMenu(shelf);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
            if (player.isShiftKeyDown()) {
                shelf.tryExtractLastPoppet(player);
            } else {
                player.openMenu(shelf);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
                shelf.disableChunkLoading();
                Containers.dropContents(level, pos, shelf);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
