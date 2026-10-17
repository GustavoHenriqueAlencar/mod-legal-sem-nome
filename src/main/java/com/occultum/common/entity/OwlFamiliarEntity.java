package com.occultum.common.entity;

import com.occultum.common.machine.cauldron.CauldronCustomPotionItem;
import com.occultum.common.machine.kettle.KettleBrewItem;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Coruja Familiar (Owl Familiar - Seção 4.2.3 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md):
 * - Imortalidade espiritual.
 * - Maestria em Voo de Vassoura (velocidade máxima sobe de 0.90 para 1.20 blocos/tick, aceleração +0.20).
 * - Suporte Aéreo em Combate: ataques rasantes de bico causando 2.0 pontos de dano.
 * - Bombardeio Aéreo com Poções: segura poção nas garras e solta com precisão cirúrgica sobre o alvo/coordenada!
 * - Visão Espectral Compartilhada.
 */
public class OwlFamiliarEntity extends TamableAnimal {

    private ItemStack heldPotion = ItemStack.EMPTY;
    @Nullable
    private BlockPos bombingTarget = null;

    public OwlFamiliarEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.FLYING_SPEED, 0.65D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.4D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.3D, 5.0F, 2.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.RABBIT) || stack.is(Items.CHICKEN) || stack.is(ModItems.MANDRAKE_ROOT.get());
    }

    public ItemStack getHeldPotion() {
        return heldPotion;
    }

    public void setHeldPotion(ItemStack stack) {
        this.heldPotion = stack;
    }

    public void orderAerialBombing(BlockPos targetPos) {
        this.bombingTarget = targetPos.immutable();
        this.setOrderedToSit(false);
        this.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY() + 3.5, targetPos.getZ() + 0.5, 1.5D);
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
            // Entregar poção arremessável para a coruja segurar nas garras (Bombardeio Aéreo com Poções)
            if (stack.getItem() instanceof ThrowablePotionItem
                    || stack.getItem() instanceof CauldronCustomPotionItem
                    || stack.getItem() instanceof KettleBrewItem) {
                if (!this.heldPotion.isEmpty()) {
                    player.getInventory().add(this.heldPotion);
                }
                this.heldPotion = stack.split(1);
                this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.2F);
                return InteractionResult.SUCCESS;
            }

            // Se clicar agachado com mão vazia: ativa Visão Espectral Compartilhada (Visão Noturna + Brilho de alvos próximos)
            if (player.isShiftKeyDown() && stack.isEmpty()) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(24.0D), le -> le != player && le != this)) {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
                }
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.3F);
                return InteractionResult.SUCCESS;
            }

            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) return;

        // Se a coruja possui uma poção nas garras e um alvo em combate ou coordenada de bombardeio designada
        LivingEntity combatTarget = this.getTarget();
        BlockPos dropTarget = this.bombingTarget != null ? this.bombingTarget : (combatTarget != null ? combatTarget.blockPosition() : null);

        if (!this.heldPotion.isEmpty() && dropTarget != null) {
            Vec3 above = new Vec3(dropTarget.getX() + 0.5, dropTarget.getY() + 3.0, dropTarget.getZ() + 0.5);
            this.getNavigation().moveTo(above.x, above.y, above.z, 1.5D);
            double horizDistSq = (this.getX() - above.x) * (this.getX() - above.x) + (this.getZ() - above.z) * (this.getZ() - above.z);
            if (horizDistSq <= 4.0D) {
                executePotionDrop(serverLevel, dropTarget);
                this.bombingTarget = null;
            }
        }
    }

    private void executePotionDrop(ServerLevel level, BlockPos targetPos) {
        if (this.heldPotion.isEmpty()) return;
        ItemStack potion = this.heldPotion.copy();
        this.heldPotion = ItemStack.EMPTY;

        if (potion.getItem() instanceof ThrowablePotionItem) {
            ThrownPotion thrown = new ThrownPotion(level, this);
            thrown.setItem(potion);
            thrown.setDeltaMovement(0.0D, -0.5D, 0.0D);
            level.addFreshEntity(thrown);
        } else if (potion.getItem() instanceof CauldronCustomPotionItem) {
            CauldronCustomPotionItem.deployDispersal(level, this.getOwner() instanceof Player p ? p : this, targetPos, CauldronCustomPotionItem.getData(potion));
        } else if (potion.getItem() instanceof KettleBrewItem) {
            level.playSound(null, targetPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        level.sendParticles(ParticleTypes.WITCH, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 25, 1.0, 0.5, 1.0, 0.05);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Imortalidade Espiritual (Seção 4.1.3)
        if (!this.level().isClientSide && this.isTame() && this.getHealth() - amount <= 0.0F) {
            this.setHealth(1.0F);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 25, 0.3, 0.3, 0.3, 0.03);
            }
            this.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.0F, 1.2F);
            this.discard();
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (!heldPotion.isEmpty()) {
            compound.put("HeldPotion", heldPotion.save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("HeldPotion")) {
            heldPotion = ItemStack.parse(this.registryAccess(), compound.getCompound("HeldPotion")).orElse(ItemStack.EMPTY);
        } else {
            heldPotion = ItemStack.EMPTY;
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
