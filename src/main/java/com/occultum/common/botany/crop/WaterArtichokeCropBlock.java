package com.occultum.common.botany.crop;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import com.mojang.serialization.MapCodec;

public class WaterArtichokeCropBlock extends BaseWitchCropBlock {
    public static final MapCodec<WaterArtichokeCropBlock> CODEC = simpleCodec(WaterArtichokeCropBlock::new);

    @Override
    protected MapCodec<? extends BaseWitchCropBlock> codec() {
        return CODEC;
    }

    public WaterArtichokeCropBlock(Properties properties) {
        super(properties, ModItems.WATER_ARTICHOKE_SEEDS);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Exclusivamente na superfície de água parada
        FluidState fluid = level.getFluidState(pos);
        return fluid.is(Fluids.WATER) && fluid.isSource();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isMaxAge(state)) {
            popResource(level, pos, new ItemStack(ModItems.WATER_ARTICHOKE.get()));
            int seeds = 1 + level.random.nextInt(4);
            popResource(level, pos, new ItemStack(ModItems.WATER_ARTICHOKE_SEEDS.get(), seeds));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
