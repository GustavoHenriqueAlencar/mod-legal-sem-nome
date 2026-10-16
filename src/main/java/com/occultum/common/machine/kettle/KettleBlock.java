package com.occultum.common.machine.kettle;

import com.mojang.serialization.MapCodec;
import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class KettleBlock extends BaseEntityBlock {
    public static final MapCodec<KettleBlock> CODEC = simpleCodec(KettleBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public KettleBlock(Properties properties) {
        super(properties);
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof KettleBlockEntity kettle)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Abastecer ou reiniciar com Balde de Água (1.000 mB)
        if (stack.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide) {
                kettle.fillWithWater();
                if (!player.isCreative()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Extrair caldo ou item pronto com Garrafa de Vidro Vazia
        if (stack.is(Items.GLASS_BOTTLE) && kettle.isReady()) {
            if (!level.isClientSide) {
                kettle.extractWithBottle(player, stack);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KettleBlockEntity kettle && kettle.isReady() && !kettle.requiresGlassBottle()) {
            if (!level.isClientSide) {
                kettle.extractDirectItem(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ItemEntity itemEntity && !itemEntity.getItem().isEmpty()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof KettleBlockEntity kettle) {
                if (kettle.tryInsertIngredient(itemEntity.getItem())) {
                    level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.6F, 1.2F);
                    if (itemEntity.getItem().isEmpty()) {
                        itemEntity.discard();
                    }
                }
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof KettleBlockEntity kettle && kettle.hasWater()) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.7D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            if (kettle.isRuined()) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.04D, 0.0D);
            } else if (kettle.isReady()) {
                level.addParticle(ParticleTypes.WITCH, x, y, z, 0.0D, 0.03D, 0.0D);
            } else if (kettle.isHeated()) {
                level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KettleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        return createTickerHelper(blockEntityType, ModBlockEntities.KETTLE_BE.get(), KettleBlockEntity::serverTick);
    }
}
