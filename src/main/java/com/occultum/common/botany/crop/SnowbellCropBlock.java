package com.occultum.common.botany.crop;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

public class SnowbellCropBlock extends BaseWitchCropBlock {
    public static final MapCodec<SnowbellCropBlock> CODEC = simpleCodec(SnowbellCropBlock::new);

    @Override
    protected MapCodec<? extends BaseWitchCropBlock> codec() {
        return CODEC;
    }

    public SnowbellCropBlock(Properties properties) {
        super(properties, ModItems.SNOWBELL_SEEDS);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isMaxAge(state)) {
            popResource(level, pos, new ItemStack(ModItems.SNOWBELL.get()));
            int seeds = 1 + level.random.nextInt(4);
            popResource(level, pos, new ItemStack(ModItems.SNOWBELL_SEEDS.get(), seeds));

            // Subproduto raro: 20% de chance de Agulha Glacial (Icy Needle)
            if (level.random.nextFloat() < 0.20F) {
                popResource(level, pos, new ItemStack(ModItems.ICY_NEEDLE.get()));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
