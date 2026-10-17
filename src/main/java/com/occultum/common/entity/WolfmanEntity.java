package com.occultum.common.entity;

import com.occultum.common.werewolf.LycanthropySystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Homem-Lobo Selvagem (WolfmanEntity - Seção 3.1 e 3.6 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Ao atacar à noite e deixar a vítima com menos de 25% de vida, possui 25% de chance de infectá-la com Licantropia.
 * - Sofre vulnerabilidade a Dano por Prata (ignora teto de dano de 2.0 HP e adiciona +5.0 de dano puro).
 */
public class WolfmanEntity extends Monster {

    public WolfmanEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living && this.level() instanceof ServerLevel sl && sl.isNight()) {
            LycanthropySystem.tryInfectVictimWithLycanthropy(sl, living);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (LycanthropySystem.isSilverDamageSource(source)) {
            return super.hurt(source, amount + 5.0F);
        }
        float capped = Math.max(Math.min(Math.max(0.0F, amount - 3.0F), 2.0F), 0.5F);
        return super.hurt(source, capped);
    }
}
