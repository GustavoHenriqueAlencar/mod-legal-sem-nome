package com.occultum.common.botany.crop;

import com.occultum.common.entity.TreefydEntity;
import com.occultum.common.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TreefydSeedItem extends Item {

    public TreefydSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide) {
            TreefydEntity treefyd = ModEntityTypes.TREEFYD.get().create(level);
            if (treefyd != null) {
                treefyd.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                if (player != null) {
                    treefyd.tame(player);
                }
                level.addFreshEntity(treefyd);
                level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (player != null && !player.isCreative()) {
                    stack.shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
