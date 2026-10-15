package com.occultum.common.botany.crop;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

public class BelladonnaCropBlock extends BaseWitchCropBlock {
    public static final MapCodec<BelladonnaCropBlock> CODEC = simpleCodec(BelladonnaCropBlock::new);

    @Override
    protected MapCodec<? extends BaseWitchCropBlock> codec() {
        return CODEC;
    }

    public BelladonnaCropBlock(Properties properties) {
        super(properties, ModItems.BELLADONNA_SEEDS);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isMaxAge(state)) {
            popResource(level, pos, new ItemStack(ModItems.BELLADONNA_FLOWER.get()));
            // 1 a 4 Sementes de Beladona
            int seeds = 1 + level.random.nextInt(4);
            popResource(level, pos, new ItemStack(ModItems.BELLADONNA_SEEDS.get(), seeds));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
