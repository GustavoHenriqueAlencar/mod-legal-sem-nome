package com.occultum.common.botany.powder;

import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class MutandisExtremisItem extends MutandisItem {

    public MutandisExtremisItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState targetState = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // 1. Mutações Geológicas Especiais (Seção 5.2.B)
        // A. Bloco de Grama <-> Vitória-Régia (50% de chance)
        if (targetState.is(Blocks.GRASS_BLOCK)) {
            if (!level.isClientSide) {
                if (level.random.nextFloat() < 0.50F) {
                    level.setBlock(pos, Blocks.LILY_PAD.defaultBlockState(), 3);
                    playSuccessEffects(level, pos);
                }
                if (player != null && !player.isCreative()) stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        } else if (targetState.is(Blocks.LILY_PAD)) {
            if (!level.isClientSide) {
                if (level.random.nextFloat() < 0.50F) {
                    level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    playSuccessEffects(level, pos);
                }
                if (player != null && !player.isCreative()) stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        // B. Terra Sob a Água -> Argila (50% de chance, formato em cruz de 5 blocos)
        if (targetState.is(Blocks.DIRT) && level.getFluidState(pos.above()).is(Fluids.WATER)) {
            if (!level.isClientSide) {
                if (level.random.nextFloat() < 0.50F) {
                    level.setBlock(pos, Blocks.CLAY.defaultBlockState(), 3);
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos nPos = pos.relative(dir);
                        if (level.getBlockState(nPos).is(Blocks.DIRT)) {
                            level.setBlock(nPos, Blocks.CLAY.defaultBlockState(), 3);
                        }
                    }
                    playSuccessEffects(level, pos);
                }
                if (player != null && !player.isCreative()) stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    private void playSuccessEffects(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, 0.4, 0.4, 0.4, 0.05);
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    @Override
    protected List<BlockState> getCommonMutandisDestinations(Level level, BlockPos pos) {
        List<BlockState> list = super.getCommonMutandisDestinations(level, pos);
        // Adições do Extremis (Seção 5.2.A)
        list.add(Blocks.WHEAT.defaultBlockState());
        list.add(Blocks.CARROTS.defaultBlockState());
        list.add(Blocks.POTATOES.defaultBlockState());
        list.add(Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
        list.add(Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
        list.add(ModBlocks.BELLADONNA_CROP.get().defaultBlockState());
        list.add(ModBlocks.MANDRAKE_CROP.get().defaultBlockState());
        list.add(ModBlocks.WATER_ARTICHOKE_CROP.get().defaultBlockState());
        list.add(Blocks.PUMPKIN_STEM.defaultBlockState());
        list.add(Blocks.MELON_STEM.defaultBlockState());
        list.add(Blocks.SUGAR_CANE.defaultBlockState());
        list.add(Blocks.NETHER_WART.defaultBlockState());
        return list;
    }
}
