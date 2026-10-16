package com.occultum.common.machine.cauldron;

import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AnointingPasteItem extends Item {

    public AnointingPasteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)) {
            if (!level.isClientSide) {
                boolean hadWater = state.is(Blocks.WATER_CAULDRON);
                level.setBlockAndUpdate(pos, ModBlocks.WITCHES_CAULDRON.get().defaultBlockState());
                if (hadWater && level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity be) {
                    be.addWater(3000);
                }
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                ((ServerLevel) level).sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 25, 0.3, 0.3, 0.3, 0.05);
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
    }
}
