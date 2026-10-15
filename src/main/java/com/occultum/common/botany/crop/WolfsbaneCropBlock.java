package com.occultum.common.botany.crop;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

public class WolfsbaneCropBlock extends BaseWitchCropBlock {
    public static final MapCodec<WolfsbaneCropBlock> CODEC = simpleCodec(WolfsbaneCropBlock::new);

    @Override
    protected MapCodec<? extends BaseWitchCropBlock> codec() {
        return CODEC;
    }

    public WolfsbaneCropBlock(Properties properties) {
        super(properties, ModItems.WOLFSBANE_SEEDS);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isMaxAge(state)) {
            popResource(level, pos, new ItemStack(ModItems.WOLFSBANE_FLOWER.get()));
            int seeds = 1 + level.random.nextInt(4);
            popResource(level, pos, new ItemStack(ModItems.WOLFSBANE_SEEDS.get(), seeds));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
