package com.occultum.common.item;

import com.occultum.common.registry.ModAttachmentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Vassoura Encantada (Enchanted Broom - Seção 4.2.3 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md
 * e Ritual #08 de Encantamento de Vassoura):
 * - Permite voo livre tridimensional enquanto segurada/usada.
 * - Com a Coruja Familiar vinculada ("owl"):
 *   * Velocidade máxima horizontal aumenta de 0.90 para 1.20 blocos/tick.
 *   * Aceleração recebe +0.20 blocos/tick².
 *   * Frenagem instantânea no ar ao soltar os controles de movimento.
 */
public class EnchantedBroomItem extends Item {

    public EnchantedBroomItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        player.startUsingItem(usedHand);
        level.playSound(null, player.blockPosition(), SoundEvents.PARROT_FLY, SoundSource.PLAYERS, 0.8F, 0.7F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        boolean hasOwl = "owl".equals(player.getData(ModAttachmentTypes.FAMILIAR_TYPE.get()));
        double maxSpeed = hasOwl ? 1.20D : 0.90D;
        double accel = 0.15D + (hasOwl ? 0.20D : 0.0D);

        Vec3 look = player.getLookAngle();
        Vec3 currentVel = player.getDeltaMovement();

        if (player.isShiftKeyDown() && hasOwl) {
            // Frenagem instantânea no ar concedida pela Coruja Familiar
            player.setDeltaMovement(0.0D, 0.02D, 0.0D);
        } else {
            Vec3 targetVel = look.scale(maxSpeed);
            Vec3 blended = currentVel.lerp(targetVel, Math.min(1.0D, accel));
            player.setDeltaMovement(blended);
        }
        player.fallDistance = 0.0F;

        if (!level.isClientSide && level instanceof ServerLevel sl && remainingUseDuration % 4 == 0) {
            sl.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.15, 0.05, 0.15, 0.01);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (isSelected && entity instanceof Player player && player.isUsingItem() && player.getUseItem() == stack) {
            player.fallDistance = 0.0F;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.occultum.enchanted_broom").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
