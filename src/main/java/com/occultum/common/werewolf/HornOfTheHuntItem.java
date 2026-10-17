package com.occultum.common.werewolf;

import com.occultum.common.entity.HornedHuntsmanEntity;
import com.occultum.common.registry.ModEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * A Trompa da Caçada Selvagem (Horn of the Hunt - Seção 3.2 Transição 4 -> 5 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Quando soprada pelo jogador, invoca o chefe lendário Horned Huntsman (Caçador Chifrudo).
 * - Derrotá-lo em combate mortal desbloqueia a forma bípede de Homem-Lobo (Wolfman) no Altar do Lobisomem.
 */
public class HornOfTheHuntItem extends Item {

    public HornOfTheHuntItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 2.0F, 0.85F);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && level instanceof ServerLevel sl && entity instanceof Player player) {
            HornedHuntsmanEntity boss = ModEntityTypes.HORNED_HUNTSMAN.get().create(sl);
            if (boss != null) {
                double sx = player.getX() + (sl.random.nextBoolean() ? 6.0D : -6.0D);
                double sz = player.getZ() + (sl.random.nextBoolean() ? 6.0D : -6.0D);
                boss.moveTo(sx, player.getY(), sz, player.getYRot(), 0.0F);
                boss.setTarget(player);
                sl.addFreshEntity(boss);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, sx, player.getY() + 1.0D, sz, 35, 0.6, 1.0, 0.6, 0.05);
            }
            player.displayClientMessage(Component.translatable("message.occultum.huntsman.summoned").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), false);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        return stack;
    }
}
