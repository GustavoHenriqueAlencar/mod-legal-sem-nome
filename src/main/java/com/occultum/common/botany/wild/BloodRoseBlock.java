package com.occultum.common.botany.wild;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BloodRoseBlock extends BushBlock implements EntityBlock {
    public static final MapCodec<BloodRoseBlock> CODEC = simpleCodec(BloodRoseBlock::new);
    private static final VoxelShape SHAPE = box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    public BloodRoseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodRoseBlockEntity(pos, state);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            // Seção 4.4: Dano contínuo de espinhos e armazena o sangue da vítima
            if (living.hurt(level.damageSources().cactus(), 1.0F)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BloodRoseBlockEntity rose) {
                    rose.addBlood(25);
                }
            }
        }
        super.entityInside(state, level, pos, entity);
    }
}
