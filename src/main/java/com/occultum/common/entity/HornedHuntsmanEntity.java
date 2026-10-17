package com.occultum.common.entity;

import com.occultum.common.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * O Caçador Chifrudo (Horned Huntsman - Seção 3.2 Transição 4 -> 5 de 08_VAMPIRISMO_E_LICANTROPIA.md
 * e Seção 6.2 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - Invocado pela Trompa da Caçada Selvagem (Horn of the Hunt).
 * - Derrotá-lo conclui a missão 4 -> 5 do Lobisomem e dropa a Lança do Caçador (Huntsman's Spear).
 */
public class HornedHuntsmanEntity extends Monster {

    public HornedHuntsmanEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel sl)) return;
        LivingEntity target = this.getTarget();
        if (target != null && this.tickCount % 160 == 0) {
            Wolf hound = EntityType.WOLF.create(sl);
            if (hound != null) {
                hound.moveTo(this.getX() + 1.5D, this.getY(), this.getZ() + 1.5D, this.getYRot(), 0.0F);
                hound.setTarget(target);
                sl.addFreshEntity(hound);
                sl.playSound(null, this.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 1.2F, 0.8F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.HUNTSMANS_SPEAR.get()));
    }
}
