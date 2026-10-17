package com.occultum.common.mirror;

import com.occultum.common.vampire.VampirismSystem;
import com.occultum.common.werewolf.LycanthropySystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Pistola-Besta (`HandBowItem`) — compatível com Virotes de Prata, Solares e Sagrados (`14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md` Seção 4.4).
 */
public class HandBowItem extends Item {

    public HandBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(32.0D));
            AABB box = player.getBoundingBox().expandTowards(look.scale(32.0D)).inflate(1.0D);
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(serverLevel, player, eye, end, box,
                    e -> e instanceof LivingEntity && e.isPickable() && e != player);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.1F);
            if (hit != null && hit.getEntity() instanceof LivingEntity target) {
                if (LycanthropySystem.isWerewolf(target)) {
                    target.setHealth(Math.max(0.0F, target.getHealth() - 13.0F));
                    target.hurt(serverLevel.damageSources().playerAttack(player), 0.1F);
                } else if (target instanceof Player tp && VampirismSystem.isVampire(tp)) {
                    tp.igniteForSeconds(5.0F);
                    VampirismSystem.drainBlood(tp, 120);
                    tp.hurt(serverLevel.damageSources().onFire(), 6.0F);
                } else {
                    target.hurt(serverLevel.damageSources().playerAttack(player), 7.0F);
                }
            }
            player.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
