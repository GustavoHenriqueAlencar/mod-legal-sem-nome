package com.occultum.common.entity;

import com.occultum.common.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Espírito Voador (SpiritEntity - Seção 2.2.3 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - Pequena criatura alada espectral do Mundo Espiritual.
 * - Tamanho de hitbox: 0.25 x 0.25 blocos.
 * - Vida: 4.0 HP, Velocidade: 0.40.
 * - Flutua emitindo trilhas de partículas coloridas brilhantes, é atraída por Vontade Focada (Focused Will)
 *   e pode ser subjugada em Frascos de Espírito Subjugado (Subdued Spirit).
 */
public class SpiritEntity extends PathfinderMob {

    public SpiritEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.40D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.15D, Ingredient.of(ModItems.FOCUSED_WILL.get()), false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    this.getY() + 0.15D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    0.0D, 0.01D, 0.0D);
        } else if (this.tickCount % 30 == 0) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, (this.random.nextDouble() - 0.48D) * 0.08D, motion.z);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GLASS_BOTTLE) || stack.is(ModItems.CLAY_JAR.get()) || stack.is(ModItems.FOCUSED_WILL.get())) {
            if (!this.level().isClientSide) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                ItemStack subdued = new ItemStack(ModItems.SUBDUED_SPIRIT.get());
                if (!player.getInventory().add(subdued)) {
                    player.drop(subdued, false);
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.3F);
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
