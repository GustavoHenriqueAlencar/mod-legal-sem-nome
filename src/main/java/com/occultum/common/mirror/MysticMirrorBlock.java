package com.occultum.common.mirror;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.entity.FairestFollowerEntity;
import com.occultum.common.poppet.TaglockKitItem;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

/**
 * Bloco de Espelho Místico (`14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md` Seções 3 e 5):
 * - Suporta variante Móvel (quebrável) e variante Inquebrável (gerada nas células da Dimensão do Espelho).
 * - Ocupa 2 blocos de altura (LOWER e UPPER).
 * - Colisão frontal com jogador: aciona `MirrorDimensionManager.handlePlayerStepIntoMirror`.
 * - Interações no Mundo Material:
 *   1. Mão vazia -> Oráculo *"Espelho, Espelho Meu"* (partículas 0x7777FF, anúncio em 64 blocos,
 *      mecânica do "Mais Belo" em 100x32x100 e recitação da lista de espionagem de 4 blocos).
 *   2. Kit Taglock preenchido -> Disfarce de Identidade (4.000 de poder do Altar) ou remoção gratuita com o próprio sangue/vazio.
 *   3. Esfera de Quartzo (`QUARTZ_SPHERE`) -> Granada de Duplicação (`DUPLICATION_GRENADE`) por 2.000 de poder do Altar.
 */
public class MysticMirrorBlock extends BaseEntityBlock {

    public static final MapCodec<MysticMirrorBlock> CODEC = simpleCodec(p -> new MysticMirrorBlock(p, false));
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape NORTH_SHAPE = Block.box(1.0D, 0.0D, 14.0D, 15.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 16.0D, 2.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(14.0D, 0.0D, 1.0D, 16.0D, 16.0D, 15.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 1.0D, 2.0D, 16.0D, 15.0D);

    private static final Vector3f ORACLE_PARTICLE_COLOR = new Vector3f(0x77 / 255.0F, 0x77 / 255.0F, 1.0F);

    private final boolean unbreakable;

    public MysticMirrorBlock(Properties properties, boolean unbreakable) {
        super(properties);
        this.unbreakable = unbreakable;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public boolean isUnbreakableVariant() {
        return unbreakable;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (facing.getAxis() == Direction.Axis.Y && ((half == DoubleBlockHalf.LOWER) == (facing == Direction.UP))) {
            return facingState.is(this) && facingState.getValue(HALF) != half
                    ? state.setValue(FACING, facingState.getValue(FACING))
                    : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && state.getValue(HALF) == DoubleBlockHalf.LOWER && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MysticMirrorBlockEntity be && be.getLinkedPos() != null && level instanceof ServerLevel sl) {
                ServerLevel mirrorLevel = sl.getServer().getLevel(OccultumDimensions.MIRROR_LEVEL);
                if (mirrorLevel != null && mirrorLevel.getBlockEntity(be.getLinkedPos()) instanceof MysticMirrorBlockEntity cellBe) {
                    cellBe.setLinkedPos(null);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            BlockPos basePos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockState baseState = level.getBlockState(basePos);
            if (baseState.getBlock() instanceof MysticMirrorBlock) {
                MirrorDimensionManager.handlePlayerStepIntoMirror(serverLevel, basePos, baseState, player);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new MysticMirrorBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(HALF) != DoubleBlockHalf.LOWER) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof MysticMirrorBlockEntity mirrorBe) {
                mirrorBe.serverTick((ServerLevel) lvl, pos, st);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos basePos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(serverLevel.getBlockEntity(basePos) instanceof MysticMirrorBlockEntity mirrorBe)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Seção 5.2: Disfarce de Identidade com Taglock Kit (4.000 de energia de Altar) ou remoção gratuita
        if (stack.is(ModItems.TAGLOCK_KIT.get())) {
            UUID targetUuid = TaglockKitItem.getBoundEntityUUID(stack);
            String targetName = TaglockKitItem.getBoundEntityName(stack);
            if (targetUuid == null || targetUuid.equals(serverPlayer.getUUID())) {
                // Restauração gratuita da aparência original
                serverPlayer.setCustomName(null);
                serverPlayer.setCustomNameVisible(false);
                serverPlayer.displayClientMessage(Component.translatable("message.occultum.mirror.disguise_cleared").withStyle(ChatFormatting.GREEN), true);
                serverLevel.playSound(null, basePos, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.BLOCKS, 1.0F, 1.2F);
                return ItemInteractionResult.SUCCESS;
            }
            if (!AltarPowerNetwork.tryConsumeNearestAltarPower(serverLevel, basePos, 32, 4000)) {
                serverPlayer.displayClientMessage(Component.translatable("message.occultum.mirror.insufficient_altar_power", 4000).withStyle(ChatFormatting.RED), true);
                return ItemInteractionResult.FAIL;
            }
            serverPlayer.setCustomName(Component.literal(targetName));
            serverPlayer.setCustomNameVisible(true);
            serverPlayer.displayClientMessage(Component.translatable("message.occultum.mirror.disguise_applied", targetName).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            serverLevel.playSound(null, basePos, SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        // Seção 5.3: Fabricação de Granada de Duplicação (Esfera de Quartzo + 2.000 de energia de Altar)
        if (stack.is(ModItems.QUARTZ_SPHERE.get())) {
            if (!AltarPowerNetwork.tryConsumeNearestAltarPower(serverLevel, basePos, 32, 2000)) {
                serverPlayer.displayClientMessage(Component.translatable("message.occultum.mirror.insufficient_altar_power", 2000).withStyle(ChatFormatting.RED), true);
                return ItemInteractionResult.FAIL;
            }
            stack.shrink(1);
            ItemStack grenade = new ItemStack(ModItems.DUPLICATION_GRENADE.get());
            CompoundTag tag = new CompoundTag();
            tag.putUUID("OwnerUUID", serverPlayer.getUUID());
            tag.putString("OwnerName", serverPlayer.getScoreboardName());
            grenade.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            if (!serverPlayer.getInventory().add(grenade)) {
                serverPlayer.drop(grenade, false);
            }
            serverLevel.playSound(null, basePos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            triggerMirrorOracle(serverLevel, basePos, state, mirrorBe, serverPlayer);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos basePos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            if (serverLevel.getBlockEntity(basePos) instanceof MysticMirrorBlockEntity mirrorBe) {
                triggerMirrorOracle(serverLevel, basePos, state, mirrorBe, serverPlayer);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Seção 5.1: O Oráculo "Espelho, Espelho Meu" (Vigilância, Julgamento e "Mais Belo").
     */
    private void triggerMirrorOracle(ServerLevel level, BlockPos basePos, BlockState state,
                                     MysticMirrorBlockEntity mirrorBe, ServerPlayer player) {
        mirrorBe.activateOracleFace(80);
        level.playSound(null, basePos, SoundEvents.AMBIENT_CAVE.value(), SoundSource.BLOCKS, 1.0F, 1.4F);
        level.sendParticles(new DustParticleOptions(ORACLE_PARTICLE_COLOR, 1.2F),
                basePos.getX() + 0.5D, basePos.getY() + 1.0D, basePos.getZ() + 0.5D,
                25, 0.3D, 0.5D, 0.3D, 0.02D);

        // Verifica se há um FairestFollowerEntity vivo em 100 blocos horizontais por 32 blocos verticais
        AABB fairestBox = new AABB(basePos).inflate(100.0D, 16.0D, 100.0D);
        List<FairestFollowerEntity> followers = level.getEntitiesOfClass(FairestFollowerEntity.class, fairestBox, LivingEntity::isAlive);

        long now = level.getGameTime();
        if (followers.isEmpty() && now - mirrorBe.getLastFairestSpawnTick() >= 2400L) {
            // Gera um novo seguidor em um raio de 50 a 100 blocos de distância do espelho
            mirrorBe.setLastFairestSpawnTick(now);
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double dist = 50.0D + level.random.nextDouble() * 50.0D;
            int fx = basePos.getX() + (int) Math.round(Math.cos(angle) * dist);
            int fz = basePos.getZ() + (int) Math.round(Math.sin(angle) * dist);
            int fy = basePos.getY();
            FairestFollowerEntity spawned = ModEntityTypes.FAIREST_FOLLOWER.get().create(level);
            if (spawned != null) {
                spawned.moveTo(fx + 0.5D, fy, fz + 0.5D, 0.0F, 0.0F);
                level.addFreshEntity(spawned);
                followers = List.of(spawned);
            }
        }

        Component oracleBroadcast;
        if (!followers.isEmpty()) {
            FairestFollowerEntity fairest = followers.get(0);
            String dir = getCardinal8Direction(basePos, fairest.blockPosition());
            oracleBroadcast = Component.translatable("message.occultum.mirror.oracle_fairest_npc",
                    fairest.getName(), dir).withStyle(ChatFormatting.AQUA);
        } else {
            String favName = mirrorBe.getFavoritePlayerName();
            if (favName == null || favName.isEmpty() || favName.equals(player.getScoreboardName())) {
                mirrorBe.setFavoritePlayerName(player.getScoreboardName());
                oracleBroadcast = Component.translatable("message.occultum.mirror.oracle_you_fairest",
                        player.getScoreboardName()).withStyle(ChatFormatting.GOLD);
            } else {
                ServerPlayer rival = level.getServer().getPlayerList().getPlayerByName(favName);
                String dir = rival != null ? getCardinal8Direction(basePos, rival.blockPosition()) : "Norte";
                oracleBroadcast = Component.translatable("message.occultum.mirror.oracle_rival_fairest",
                        favName, dir).withStyle(ChatFormatting.LIGHT_PURPLE);
            }
        }

        // Anuncia o julgamento no chat de todos os jogadores em um raio esférico de 64 blocos
        for (ServerPlayer nearby : level.players()) {
            if (nearby.blockPosition().distSqr(basePos) <= 64.0D * 64.0D) {
                nearby.sendSystemMessage(oracleBroadcast);
            }
        }

        // Se o jogador é o favorito do espelho, recita os nomes na lista de espionagem de 4 blocos e limpa a lista
        if (player.getScoreboardName().equals(mirrorBe.getFavoritePlayerName())) {
            List<String> seen = mirrorBe.consumeSeenPlayers(player.getScoreboardName());
            if (!seen.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.occultum.mirror.oracle_seen_list",
                        String.join(", ", seen)).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static String getCardinal8Direction(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx)); // 0 = Leste, 90 = Sul, -90 = Norte, 180 = Oeste
        if (angle < 0) angle += 360.0D;
        int sector = (int) Math.round(angle / 45.0D) % 8;
        return switch (sector) {
            case 0 -> "Leste";
            case 1 -> "Sudeste";
            case 2 -> "Sul";
            case 3 -> "Sudoeste";
            case 4 -> "Oeste";
            case 5 -> "Noroeste";
            case 6 -> "Norte";
            default -> "Nordeste";
        };
    }
}
