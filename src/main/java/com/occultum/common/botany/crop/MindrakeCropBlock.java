package com.occultum.common.botany.crop;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

public class MindrakeCropBlock extends BaseWitchCropBlock {
    public static final MapCodec<MindrakeCropBlock> CODEC = simpleCodec(MindrakeCropBlock::new);

    @Override
    protected MapCodec<? extends BaseWitchCropBlock> codec() {
        return CODEC;
    }

    public MindrakeCropBlock(Properties properties) {
        super(properties, ModItems.MINDRAKE_SEEDS);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos.above(), 0) >= 9) {
            int currentAge = this.getAge(state);
            if (currentAge < this.getMaxAge()) {
                // Penalidade de crescimento nativa constante: F_taxa / 1.5
                float growthRate = getGrowthSpeed(this, level, pos) / 1.5F;
                int denominator = Mth.floor(25.0F / growthRate) + 1;
                if (random.nextInt(Math.max(1, denominator)) == 0) {
                    level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
                }
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isMaxAge(state)) {
            // Drops com despawn timer de 60 ticks (3 segundos)
            spawnShortLivedItem(level, pos, new ItemStack(ModItems.MINDRAKE_SEEDS.get()));
            if (level.random.nextFloat() < 0.25F) {
                spawnShortLivedItem(level, pos, new ItemStack(ModItems.MINDRAKE_ROOT.get()));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void spawnShortLivedItem(Level level, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setDefaultPickUpDelay();
        entity.lifespan = 60; // Desaparece em 60 ticks (3 segundos)
        level.addFreshEntity(entity);
    }
}
