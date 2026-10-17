package com.occultum.common.ritual;

import com.occultum.common.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Pedra de Vidência (Seer Stone):
 * - Quando usada no Glifo Dourado, convoca as bruxas do Coven para participar do ritual (Seção 3.3).
 * - Quando usada no ar, inspeciona o nível de Coven, Infusão Mística e poder espiritual do jogador.
 */
public class SeerStoneItem extends Item {

    public SeerStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int coven = player.getData(ModAttachmentTypes.COVEN_LEVEL.get());
            String infusion = player.getData(ModAttachmentTypes.INFUSION_TYPE.get());
            int power = player.getData(ModAttachmentTypes.INFUSION_POWER.get());
            int maxPower = player.getData(ModAttachmentTypes.INFUSION_MAX_POWER.get());
            String familiar = player.getData(ModAttachmentTypes.FAMILIAR_TYPE.get());

            player.displayClientMessage(Component.translatable("message.occultum.seer_stone.status",
                    coven,
                    infusion.isEmpty() ? "none" : infusion,
                    power, maxPower,
                    familiar.isEmpty() ? "none" : familiar
            ).withStyle(ChatFormatting.AQUA), true);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
