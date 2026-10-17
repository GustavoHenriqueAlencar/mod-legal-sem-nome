package com.occultum.common.entity;

import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * O Chefe Nightmare (Pesadelo - Seção 6 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - 100.0 HP, velocidade 0.35, dano físico 4.0 no Mundo Espiritual (0.5 fora dele),
 *   resistência a repulsão 1.0 (100% imune a knockback), teto de dano recebido de 15.0 por golpe.
 * - Escudo de Invulnerabilidade Absoluta:
 *   * Contra armas convencionais: 80 ticks (4s) de invulnerabilidade.
 *   * Contra a Lança do Caçador (Huntsman's Spear): reduzido para 40 ticks (2s).
 *   * Fraqueza Secreta: pisar ou ficar imerso em Espírito Fluido (Flowing Spirit) dissolve o escudo
 *     instantaneamente e impede que ele seja erguido!
 * - Desarmamento e Roubo de Armadura: a cada golpe, arranca 1 peça aleatória de armadura do jogador e a joga no chão,
 *   a menos que o jogador possua o Amuleto dos Sonhos Rompidos (Charm of Disrupted Dreams) no inventário.
 * - Despawn se a vítima despertar, morrer ou se afastar > 256 blocos.
 * - Drop: 1 a 2 unidades de Fome Melíflua (Mellifluous Hunger) e registro de triunfo no jogador.
 */
public class NightmareEntity extends Monster {

    @Nullable
    private UUID victimUUID;
    private int shieldTicks = 0;

    public NightmareEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 40;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public void setVictimUUID(@Nullable UUID victimUUID) {
        this.victimUUID = victimUUID;
    }

    @Nullable
    public UUID getVictimUUID() {
        return victimUUID;
    }

    public int getShieldTicks() {
        return shieldTicks;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isInFlowingSpirit() {
        return this.level().getBlockState(this.blockPosition()).is(ModBlocks.FLOWING_SPIRIT_BLOCK.get())
                || this.level().getBlockState(this.blockPosition().above()).is(ModBlocks.FLOWING_SPIRIT_BLOCK.get());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel sl)) return;

        // Se estiver pisando em Espírito Fluido, dissolve o escudo instantaneamente
        if (isInFlowingSpirit()) {
            if (shieldTicks > 0) {
                shieldTicks = 0;
                sl.playSound(null, this.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.8F, 1.4F);
            }
        } else if (shieldTicks > 0) {
            shieldTicks--;
            if (this.tickCount % 4 == 0) {
                sl.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 1.0D, this.getZ(), 5, 0.3, 0.5, 0.3, 0.02);
            }
        }

        // Verificar vínculo com a vítima (despawn se acordou, morreu ou distância > 256 blocos)
        if (victimUUID != null && this.tickCount % 20 == 0) {
            ServerPlayer victim = sl.getServer().getPlayerList().getPlayer(victimUUID);
            if (victim == null || !victim.isAlive()
                    || victim.level().dimension() != this.level().dimension()
                    || victim.distanceToSqr(this) > (256.0D * 256.0D)) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
                this.discard();
            } else {
                this.setTarget(victim);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) return super.hurt(source, amount);

        boolean inSpiritFluid = isInFlowingSpirit();
        if (shieldTicks > 0 && !inSpiritFluid) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 1.2F);
            return false;
        }

        // Teto de dano recebido limitado a 15.0 pontos por golpe
        float capped = Math.min(15.0F, amount);
        boolean damaged = super.hurt(source, capped);

        if (damaged && this.isAlive() && !inSpiritFluid) {
            boolean usedHuntsmansSpear = false;
            if (source.getEntity() instanceof LivingEntity attacker) {
                usedHuntsmansSpear = attacker.getMainHandItem().is(ModItems.HUNTSMANS_SPEAR.get());
            }
            // 40 ticks contra Lança do Caçador, 80 ticks contra armas convencionais
            this.shieldTicks = usedHuntsmansSpear ? 40 : 80;
        }
        return damaged;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Fora do Mundo Espiritual, o dano é forçado para 0.5 ponto
        if (this.level().dimension() != OccultumDimensions.SPIRIT_WORLD_LEVEL && target instanceof LivingEntity living) {
            return living.hurt(this.damageSources().mobAttack(this), 0.5F);
        }

        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof Player victim) {
            // Roubo de 1 peça aleatória de armadura (bloqueado pelo Charm of Disrupted Dreams)
            if (!hasDisruptedDreamsCharm(victim)) {
                EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                List<EquipmentSlot> equipped = new ArrayList<>();
                for (EquipmentSlot slot : armorSlots) {
                    if (!victim.getItemBySlot(slot).isEmpty()) {
                        equipped.add(slot);
                    }
                }
                if (!equipped.isEmpty()) {
                    EquipmentSlot chosen = equipped.get(this.random.nextInt(equipped.size()));
                    ItemStack stripped = victim.getItemBySlot(chosen).copy();
                    victim.setItemSlot(chosen, ItemStack.EMPTY);
                    victim.drop(stripped, true);
                    this.level().playSound(null, victim.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.HOSTILE, 1.0F, 0.7F);
                }
            }
        }
        return hit;
    }

    private static boolean hasDisruptedDreamsCharm(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.CHARM_OF_DISRUPTED_DREAMS.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        int count = 1 + level.random.nextInt(2); // 1 a 2 unidades de Fome Melíflua
        this.spawnAtLocation(new ItemStack(ModItems.MELLIFLUOUS_HUNGER.get(), count));

        if (damageSource.getEntity() instanceof Player killer) {
            killer.setData(ModAttachmentTypes.NIGHTMARE_TRIUMPH_TIME.get(), level.getGameTime());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (victimUUID != null) {
            tag.putUUID("VictimUUID", victimUUID);
        }
        tag.putInt("ShieldTicks", shieldTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("VictimUUID")) {
            victimUUID = tag.getUUID("VictimUUID");
        }
        shieldTicks = tag.getInt("ShieldTicks");
    }
}
