package com.occultum.common.werewolf;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Amuleto da Lua (Moon Charm - Seção 3.2 e 3.3 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - 50 usos de durabilidade, reparável com Barras de Ouro na bigorna.
 * - Tempo de Canalização: max((11 - L_w) * 4, 1) (quanto mais experiente o lobisomem, mais instantânea a metamorfose).
 * - Máquina de Estados:
 *   * NONE (Humano): normal -> WOLF; agachado -> WOLFMAN (se L_w >= 5).
 *   * WOLF (Lobo): normal -> NONE; agachado -> WOLFMAN (se L_w >= 5).
 *   * WOLFMAN (Homem-Lobo): normal -> WOLF; agachado -> NONE.
 * - Bloqueio por Acônito (Wolfsbane): se Força = 1 + max(0, Amplificador * 3 - 1) >= L_w, falha com fumaça e nota de erro.
 */
public class MoonCharmItem extends Item {

    public MoonCharmItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.GOLD_INGOT) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            int lw = Math.max(1, LycanthropySystem.getWerewolfLevel(player));
            // Quanto maior o nível, menor o tempo de canalização (mínimo 1 tick)
            return Math.max((11 - lw) * 4, 1);
        }
        return 36;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (LycanthropySystem.getWerewolfLevel(player) < 2) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.occultum.moon_charm.unworthy").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player && level instanceof ServerLevel sl) {
            int lw = LycanthropySystem.getWerewolfLevel(player);
            if (lw < 2) return stack;

            // Bloqueio por Acônito (Wolfsbane)
            if (LycanthropySystem.isBlockedByWolfsbane(player)) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                sl.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
                player.displayClientMessage(Component.translatable("message.occultum.moon_charm.wolfsbane_blocked").withStyle(ChatFormatting.DARK_PURPLE), true);
                return stack;
            }

            String currentForm = LycanthropySystem.getWerewolfForm(player);
            boolean sneaking = player.isShiftKeyDown();
            String nextForm;

            switch (currentForm) {
                case "WOLF" -> nextForm = (sneaking && lw >= 5) ? "WOLFMAN" : "NONE";
                case "WOLFMAN" -> nextForm = sneaking ? "NONE" : "WOLF";
                default -> nextForm = (sneaking && lw >= 5) ? "WOLFMAN" : "WOLF";
            }

            LycanthropySystem.setWerewolfForm(player, nextForm);
            sl.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.8D, player.getZ(), 25, 0.4, 0.5, 0.4, 0.05);
            sl.playSound(null, player.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1.0F, 0.9F);

            EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);
        }
        return stack;
    }
}
