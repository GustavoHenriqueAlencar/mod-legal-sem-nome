package com.occultum.common.entity;

import com.occultum.common.registry.ModItems;
import com.occultum.common.vampire.VampirismSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Chefe Lilith (Mãe dos Vampiros - Seção 2.3 e 4.1 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - 200 HP, bolas de fogo, feitiços de varinha, teto de 12.0 de dano por golpe.
 * - Ao ter seus pontos de vida reduzidos a 0, ela NÃO morre: recupera-se para 100% de HP e entra em estado Amigável.
 * - No estado Amigável:
 *   * Cálice de Vidro vazio -> Preenche com Sangue de Lilith (Nível 0 -> 1).
 *   * Papoula Vermelha (Poppy) -> Oferenda de Nível 6 -> 7 (Desbloqueia forma de Morcego BAT).
 *   * Alho (Garlic) -> Purifica e cura o Vampirismo instantaneamente (Nível -> 0).
 */
public class LilithEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_FRIENDLY =
            SynchedEntityData.defineId(LilithEntity.class, EntityDataSerializers.BOOLEAN);

    public LilithEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FRIENDLY, false);
    }

    public boolean isFriendly() {
        return this.entityData.get(DATA_FRIENDLY);
    }

    public void setFriendly(boolean friendly) {
        this.entityData.set(DATA_FRIENDLY, friendly);
        if (friendly) {
            this.setTarget(null);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, false) {
            @Override
            public boolean canUse() {
                return !LilithEntity.this.isFriendly() && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return !LilithEntity.this.isFriendly() && super.canUse();
            }
        });
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || isFriendly()) return;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.tickCount % 40 == 0 && this.distanceToSqr(target) > 16.0D) {
            Vec3 dir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
            SmallFireball fb = new SmallFireball(this.level(), this, dir);
            fb.setPos(this.getX() + dir.x, this.getEyeY() - 0.2D, this.getZ() + dir.z);
            this.level().addFreshEntity(fb);
            this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isFriendly()) return false;
        // Teto de 12.0 de dano por golpe
        float capped = Math.min(12.0F, amount);
        if (this.getHealth() - capped <= 0.0F) {
            // Não morre: recupera-se para 100% de HP e entra em estado Amigável
            this.setHealth(this.getMaxHealth());
            this.setFriendly(true);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.5D, this.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
                sl.playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.0F, 1.2F);
            }
            if (source.getEntity() instanceof Player p && !this.level().isClientSide) {
                p.displayClientMessage(Component.translatable("message.occultum.lilith.defeated_friendly").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
            }
            return false;
        }
        return super.hurt(source, capped);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isFriendly()) {
            return super.mobInteract(player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // 1. Cálice de Vidro vazio -> Sangue de Lilith
        if (stack.is(ModItems.GLASS_GOBLET.get())) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            ItemStack filled = new ItemStack(ModItems.GLASS_GOBLET_LILITH_BLOOD.get());
            if (stack.isEmpty()) {
                player.setItemInHand(hand, filled);
            } else if (!player.getInventory().add(filled)) {
                player.drop(filled, false);
            }
            this.level().playSound(null, this.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 0.8F);
            return InteractionResult.SUCCESS;
        }

        // 2. Papoula Vermelha (Poppy) -> Transição Nível 6 -> 7 (Desbloqueia forma de Morcego BAT)
        if (stack.is(Items.POPPY) && VampirismSystem.getVampireLevel(player) == 6) {
            if (VampirismSystem.tryAdvanceVampireLevel(player, 7)) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Alho (Garlic) -> Cura do Vampirismo (Seção 4.1)
        if (stack.is(ModItems.GARLIC.get()) && VampirismSystem.isVampire(player)) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            VampirismSystem.setVampireLevel(player, 0);
            this.level().playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("message.occultum.vampire.cured").withStyle(ChatFormatting.GREEN), false);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Friendly", isFriendly());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFriendly(tag.getBoolean("Friendly"));
    }
}
