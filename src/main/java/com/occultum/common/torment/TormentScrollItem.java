package com.occultum.common.torment;

import com.occultum.common.entity.LordOfTormentEntity;
import com.occultum.common.infusion.InfusionPowerHandler;
import com.occultum.common.registry.ModEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
 * Pacto de Tormento (Item Contrato - Seção 1.2 de 14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md):
 * - Pergaminho negro canalizado pelo jogador por 14 segundos (280 ticks),
 *   consumindo 80 unidades de energia de infusão (10 unidades a cada 2 segundos = 40 ticks).
 * - Se concluído com sucesso: provoca uma detonação de poder 7.0 no ponto de conjuração e invoca o
 *   Lorde do Tormento (LordOfTormentEntity) diretamente no mundo físico.
 * - Se a energia de infusão falhar durante o ritual: teleporta o usuário diretamente para a Dimensão do Tormento.
 */
public class TormentScrollItem extends Item {

    public TormentScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 280; // 14 segundos (280 ticks)
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof ServerPlayer player) || !(level instanceof ServerLevel sl)) {
            return;
        }
        int elapsed = 280 - remainingUseDuration;
        if (elapsed > 0 && elapsed % 40 == 0) {
            // Consome 10 unidades de energia de infusão a cada 2 segundos (40 ticks) -> total 70..80 unidades
            if (!InfusionPowerHandler.tryConsumeCharges(player, 10)) {
                player.stopUsingItem();
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                int floor = sl.random.nextInt(6);
                TormentDimensionManager.sendPlayerToTorment(player, floor);
                player.displayClientMessage(
                        Component.translatable("message.occultum.torment.scroll_backfire").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                        false
                );
                return;
            }
            sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 15, 0.5, 0.5, 0.5, 0.04);
            sl.playSound(null, player.blockPosition(), SoundEvents.BLAZE_BURN, SoundSource.PLAYERS, 0.8F, 0.8F);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && level instanceof ServerLevel sl && entity instanceof ServerPlayer player) {
            if (!InfusionPowerHandler.tryConsumeCharges(player, 10)) {
                int floor = sl.random.nextInt(6);
                TormentDimensionManager.sendPlayerToTorment(player, floor);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return stack;
            }

            // Detonação de poder 7.0 no ponto de conjuração e invocação do Lorde do Tormento
            sl.explode(player, player.getX() + 3.0D, player.getY(), player.getZ() + 3.0D, 7.0F, Level.ExplosionInteraction.NONE);
            LordOfTormentEntity boss = ModEntityTypes.LORD_OF_TORMENT.get().create(sl);
            if (boss != null) {
                boss.moveTo(player.getX() + 3.0D, player.getY() + 1.5D, player.getZ() + 3.0D, player.getYRot(), 0.0F);
                boss.setTarget(player);
                sl.addFreshEntity(boss);
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        return stack;
    }
}
