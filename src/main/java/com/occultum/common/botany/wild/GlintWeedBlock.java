package com.occultum.common.botany.wild;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;

public class GlintWeedBlock extends BushBlock {
    public static final MapCodec<GlintWeedBlock> CODEC = simpleCodec(GlintWeedBlock::new);
    private static final VoxelShape SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    public GlintWeedBlock(Properties properties) {
        super(properties.lightLevel(state -> 14));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
