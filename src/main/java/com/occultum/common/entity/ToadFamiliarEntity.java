package com.occultum.common.entity;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Sapo Familiar (Toad Familiar - Seção 4.2.2 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md):
 * - Imortalidade espiritual: quando sofre dano fatal, dissipa-se em névoa espectral com 1 ponto de vida residual.
 * - Maestria Alquímica no Caldeirão: dobro de poções por lote e mitigação de falhas pela metade.
 * - Concede salto elevado (+1 bloco de altura), imunidade a Veneno e -50% dano ácido ao mestre.
 */
public class ToadFamiliarEntity extends TamableAnimal {

    public ToadFamiliarEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.15D, 5.0F, 2.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SPIDER_EYE) || stack.is(Items.SLIME_BALL) || stack.is(ModItems.MANDRAKE_ROOT.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame() && isFood(stack)) {
            if (!this.level().isClientSide) {
                if (!player.isCreative()) stack.shrink(1);
                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isOwnedBy(player) && !this.level().isClientSide) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 40 == 0 && this.isTame() && this.getOwner() instanceof Player owner) {
            if (owner.distanceToSqr(this) <= 256.0D) {
                owner.addEffect(new MobEffectInstance(MobEffects.JUMP, 80, 0, true, false));
                if (owner.hasEffect(MobEffects.POISON)) {
                    owner.removeEffect(MobEffects.POISON);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Imortalidade Espiritual (Seção 4.1.3): nunca morre definitivamente
        if (!this.level().isClientSide && this.isTame() && this.getHealth() - amount <= 0.0F) {
            this.setHealth(1.0F);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.4, this.getZ(), 25, 0.3, 0.3, 0.3, 0.03);
            }
            this.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.0F, 1.2F);
            this.discard();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
