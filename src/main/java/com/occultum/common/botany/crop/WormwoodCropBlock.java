package com.occultum.common.botany.crop;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import com.mojang.serialization.MapCodec;

public class WormwoodCropBlock extends BaseWitchCropBlock {
    public static final MapCodec<WormwoodCropBlock> CODEC = simpleCodec(WormwoodCropBlock::new);

    @Override
    protected MapCodec<? extends BaseWitchCropBlock> codec() {
        return CODEC;
    }
    public static final BooleanProperty IS_UPPER = BooleanProperty.create("is_upper");

    public WormwoodCropBlock(Properties properties) {
        super(properties, ModItems.WORMWOOD_SEEDS);
        this.registerDefaultState(this.defaultBlockState().setValue(IS_UPPER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IS_UPPER);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(IS_UPPER)) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && !below.getValue(IS_UPPER);
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        // Se atingiu o estágio 4 na base e o bloco superior for ar, brota a parte superior
        if (!state.getValue(IS_UPPER) && isMaxAge(state)) {
            BlockPos above = pos.above();
            if (level.isEmptyBlock(above)) {
                level.setBlock(above, this.getStateForAge(0).setValue(IS_UPPER, true), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (state.getValue(IS_UPPER)) {
                BlockPos below = pos.below();
                if (level.getBlockState(below).is(this)) {
                    level.destroyBlock(below, true, player);
                }
            } else {
                BlockPos above = pos.above();
                if (level.getBlockState(above).is(this)) {
                    level.destroyBlock(above, false, player);
                }
            }

            if (isMaxAge(state)) {
                popResource(level, pos, new ItemStack(ModItems.WORMWOOD_LEAF.get(), 1 + level.random.nextInt(2)));
                popResource(level, pos, new ItemStack(ModItems.WORMWOOD_SEEDS.get(), 1 + level.random.nextInt(3)));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
