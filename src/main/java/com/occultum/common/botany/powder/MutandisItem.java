package com.occultum.common.botany.powder;

import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class MutandisItem extends Item {

    public MutandisItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState targetState = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (isValidMutationTarget(targetState)) {
            if (!level.isClientSide) {
                BlockState newBlock = getCommonMutandisDestinations(level, pos).get(level.random.nextInt(getCommonMutandisDestinations(level, pos).size()));
                level.setBlock(pos, newBlock, 3);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.05);
                }
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);

                if (player != null && !player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    protected boolean isValidMutationTarget(BlockState state) {
        return state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS)
                || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.RED_MUSHROOM) || state.is(ModBlocks.SPANISH_MOSS.get()) || state.is(ModBlocks.EMBER_MOSS.get());
    }

    protected List<BlockState> getCommonMutandisDestinations(Level level, BlockPos pos) {
        List<BlockState> list = new ArrayList<>();
        list.add(Blocks.OAK_SAPLING.defaultBlockState());
        list.add(Blocks.SPRUCE_SAPLING.defaultBlockState());
        list.add(Blocks.BIRCH_SAPLING.defaultBlockState());
        list.add(Blocks.JUNGLE_SAPLING.defaultBlockState());
        list.add(Blocks.ACACIA_SAPLING.defaultBlockState());
        list.add(Blocks.DARK_OAK_SAPLING.defaultBlockState());
        list.add(ModBlocks.ROWAN_SAPLING.get().defaultBlockState());
        list.add(ModBlocks.ALDER_SAPLING.get().defaultBlockState());
        list.add(ModBlocks.HAWTHORN_SAPLING.get().defaultBlockState());
        list.add(ModBlocks.EMBER_MOSS.get().defaultBlockState());
        list.add(Blocks.SHORT_GRASS.defaultBlockState());
        list.add(Blocks.FERN.defaultBlockState());
        list.add(Blocks.RED_MUSHROOM.defaultBlockState());
        list.add(Blocks.BROWN_MUSHROOM.defaultBlockState());
        list.add(Blocks.POPPY.defaultBlockState());
        list.add(Blocks.DANDELION.defaultBlockState());
        list.add(ModBlocks.SPANISH_MOSS.get().defaultBlockState());
        return list;
    }
}
