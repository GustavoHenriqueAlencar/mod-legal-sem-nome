package com.occultum.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Morcego Assassino (AttackBatEntity - Seção 2.4 Ultimate SWARM de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Invocado em hordas de 15 unidades acima do vampiro.
 * - Possui lealdade absoluta ao invocador, persegue alvos hostis em alta velocidade e ataca em grupo.
 * - Não dropa itens nem experiência ao morrer.
 */
public class AttackBatEntity extends PathfinderMob {

    @Nullable
    private UUID ownerUUID;
    private int lifeTicks = 600; // 30s de duração do enxame

    public AttackBatEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (--lifeTicks <= 0) {
            this.discard();
            return;
        }

        if (ownerUUID != null && this.level() instanceof ServerLevel sl) {
            Player owner = sl.getPlayerByUUID(ownerUUID);
            if (owner != null) {
                LivingEntity ownerTarget = owner.getLastHurtMob();
                if (ownerTarget == null || !ownerTarget.isAlive()) {
                    ownerTarget = owner.getLastHurtByMob();
                }
                if (ownerTarget != null && ownerTarget != owner && ownerTarget != this && ownerTarget.isAlive()) {
                    this.setTarget(ownerTarget);
                }
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            Vec3 dir = target.getEyePosition().subtract(this.position()).normalize().scale(0.38D);
            this.setDeltaMovement(dir);
            if (this.distanceToSqr(target) < 2.25D && this.tickCount % 15 == 0) {
                this.doHurtTarget(target);
            }
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putInt("LifeTicks", lifeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("LifeTicks")) {
            lifeTicks = tag.getInt("LifeTicks");
        }
    }
}
