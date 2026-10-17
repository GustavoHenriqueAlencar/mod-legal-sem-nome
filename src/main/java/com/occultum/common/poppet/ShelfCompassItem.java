package com.occultum.common.poppet;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

/**
 * Bússola de Prateleira de Poppets (Shelf Compass - Seção 4.2 de 06_POPPETS_E_FETISHES.md):
 * Aponta na direção exata da coordenada (X, Y, Z) da Prateleira de Poppets mais próxima da bruxa.
 */
public class ShelfCompassItem extends Item {

    public ShelfCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            BlockPos nearest = findNearestShelf(serverLevel, player);
            if (nearest != null) {
                double dist = Math.sqrt(nearest.distSqr(player.blockPosition()));
                player.displayClientMessage(Component.translatable("message.occultum.shelf_compass.found",
                        nearest.getX(), nearest.getY(), nearest.getZ(), (int) dist).withStyle(ChatFormatting.GOLD), true);
                level.playSound(null, player.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                player.displayClientMessage(Component.translatable("message.occultum.shelf_compass.none").withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static BlockPos findNearestShelf(ServerLevel level, Player player) {
        Set<BlockPos> shelves = PoppetShelfBlockEntity.getShelvesInDimension(level.dimension());
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : shelves) {
            if (level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
                if (shelf.getOwnerUuid().isEmpty() || shelf.getOwnerUuid().equals(player.getUUID().toString())) {
                    double d = pos.distSqr(player.blockPosition());
                    if (d < bestDist) {
                        bestDist = d;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.occultum.shelf_compass").withStyle(ChatFormatting.GRAY));
    }
}
