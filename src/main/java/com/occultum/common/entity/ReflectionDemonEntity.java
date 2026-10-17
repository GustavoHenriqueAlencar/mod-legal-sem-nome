package com.occultum.common.entity;

import com.occultum.common.infusion.MysticBranchItem;
import com.occultum.common.mirror.HandBowItem;
import com.occultum.common.mirror.MysticMirrorBlockEntity;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModItems;
import com.occultum.common.vampire.VampirismSystem;
import com.occultum.common.werewolf.LycanthropySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Chefe `ReflectionDemonEntity` — O Demônio do Reflexo (`14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md` Seção 4):
 * - 100.0 HP (50 corações), velocidade base 0.35, dano físico base 2.0 (ou 6.0 em forma de Lobisomem),
 *   alcance de perseguição 50.0 blocos, resistência a repulsão 1.0, XP = 50.
 * - Teto de dano por golpe: máximo de 6.0 HP (ou 2.0 HP se em trânsito místico).
 * - Mimetismo Perfeito a cada 30 ticks (1.5s) em raio 10x8x10:
 *   * Adota skin, UUID e nome do jogador mais próximo (ou nome disfarçado).
 *   * Clona forma de Homem-Lobo (dano base 6.0 HP, oculta arma) e condição vampírica.
 *   * Clona as 4 peças de armadura e a melhor arma da hotbar (9 slots).
 * - Cópia de Efeitos de Status a cada 60 ticks (3.0s): limpa efeitos próprios e clona todos os efeitos ativos no alvo.
 * - IA Adaptativa:
 *   * Ramo Místico: combate à distância, 50% de chance/ciclo de conjurar feitiço com pesos exatos:
 *     Ignianima (14.0), Expelliarmus (2.0), Flipendo (2.0), Impedimenta (2.0), Confundus (1.0).
 *   * Pistola-Besta / Arco: seleciona Virote de Prata contra Lobisomem, Virote Solar (100 ticks de fogo solar)
 *     contra Vampiro/Morto-Vivo, ou 25% Virote Sagrado / 75% virote perfurante contra humanos.
 *   * Arma branca: velocidade 1.2x e golpes a cada 10 ticks.
 *   * Feitiço de Atração (*Attraho*): puxa o jogador magneticamente se perder caminho ou tentar se esconder.
 *   * Anti-Voo: 5% de chance por tick de aplicar Lentidão V por 10s (200 ticks) se o oponente voar.
 * - Ao morrer (ou se for um clone amigável da Granada de Duplicação):
 *   * Se chefe hostil: dropa 1x Coração de Demônio e purifica o Espelho Inquebrável + Espelho Material.
 */
public class ReflectionDemonEntity extends Monster {

    private static final EntityDataAccessor<Optional<UUID>> MIMICKED_UUID =
            SynchedEntityData.defineId(ReflectionDemonEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> MIMICKED_NAME =
            SynchedEntityData.defineId(ReflectionDemonEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> MIMICKED_WOLFMAN =
            SynchedEntityData.defineId(ReflectionDemonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MIMICKED_VAMPIRE =
            SynchedEntityData.defineId(ReflectionDemonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FRIENDLY_CLONE =
            SynchedEntityData.defineId(ReflectionDemonEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos boundMirrorPos;
    @Nullable
    private BlockPos materialMirrorPos;
    private boolean inMysticTransit = false;
    private int rangedAttackCooldown = 0;
    private int meleeTickCooldown = 0;
    private int friendlyLifespanTicks = 0;

    public ReflectionDemonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 50.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MIMICKED_UUID, Optional.empty());
        builder.define(MIMICKED_NAME, "Reflection");
        builder.define(MIMICKED_WOLFMAN, false);
        builder.define(MIMICKED_VAMPIRE, false);
        builder.define(FRIENDLY_CLONE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                living -> !this.isFriendlyClone() || !living.getUUID().equals(this.getMimickedUuid().orElse(null))));
    }

    public Optional<UUID> getMimickedUuid() {
        return this.entityData.get(MIMICKED_UUID);
    }

    public String getMimickedName() {
        return this.entityData.get(MIMICKED_NAME);
    }

    public boolean isMimickedWolfman() {
        return this.entityData.get(MIMICKED_WOLFMAN);
    }

    public boolean isMimickedVampire() {
        return this.entityData.get(MIMICKED_VAMPIRE);
    }

    public boolean isFriendlyClone() {
        return this.entityData.get(FRIENDLY_CLONE);
    }

    public void setFriendlyClone(boolean friendly, int lifespanTicks) {
        this.entityData.set(FRIENDLY_CLONE, friendly);
        this.friendlyLifespanTicks = lifespanTicks;
    }

    public void setBoundMirrorPos(@Nullable BlockPos pos) {
        this.boundMirrorPos = pos;
    }

    public void setMaterialMirrorPos(@Nullable BlockPos pos) {
        this.materialMirrorPos = pos;
    }

    public void setInMysticTransit(boolean transit) {
        this.inMysticTransit = transit;
    }

    /**
     * Seção 4.3: Clona identidade, skin, espécie (Lobisomem/Vampiro), 4 peças de armadura e melhor arma da hotbar.
     */
    public void mimicPlayer(Player targetPlayer) {
        this.entityData.set(MIMICKED_UUID, Optional.of(targetPlayer.getUUID()));
        String displayName = targetPlayer.getCustomName() != null
                ? targetPlayer.getCustomName().getString()
                : targetPlayer.getScoreboardName();
        this.entityData.set(MIMICKED_NAME, displayName);
        this.setCustomName(Component.literal(displayName));

        String wolfForm = targetPlayer.getData(ModAttachmentTypes.WEREWOLF_FORM.get());
        boolean isWolfman = ("WOLFMAN".equals(wolfForm) || "WOLF".equals(wolfForm));
        this.entityData.set(MIMICKED_WOLFMAN, isWolfman);
        this.entityData.set(MIMICKED_VAMPIRE, VampirismSystem.isVampire(targetPlayer));

        AttributeInstance atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.setBaseValue(isWolfman ? 6.0D : 2.0D);
        }

        // Clona as 4 peças de armadura
        this.setItemSlot(EquipmentSlot.HEAD, targetPlayer.getItemBySlot(EquipmentSlot.HEAD).copy());
        this.setItemSlot(EquipmentSlot.CHEST, targetPlayer.getItemBySlot(EquipmentSlot.CHEST).copy());
        this.setItemSlot(EquipmentSlot.LEGS, targetPlayer.getItemBySlot(EquipmentSlot.LEGS).copy());
        this.setItemSlot(EquipmentSlot.FEET, targetPlayer.getItemBySlot(EquipmentSlot.FEET).copy());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0F);
        }

        if (isWolfman) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else {
            // Varre os 9 slots da hotbar e escolhe o item com maior potencial ofensivo
            ItemStack bestWeapon = ItemStack.EMPTY;
            double bestScore = -1.0D;
            for (int i = 0; i < 9; i++) {
                ItemStack candidate = targetPlayer.getInventory().getItem(i);
                if (candidate.isEmpty()) continue;
                double score = evaluateWeaponScore(candidate);
                if (score > bestScore) {
                    bestScore = score;
                    bestWeapon = candidate.copy();
                }
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, bestWeapon);
        }
    }

    private double evaluateWeaponScore(ItemStack stack) {
        if (stack.getItem() instanceof MysticBranchItem) return 15.0D;
        if (stack.getItem() instanceof HandBowItem || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) return 12.0D;
        double base = 1.0D;
        var modifiers = stack.getAttributeModifiers();
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                base += entry.modifier().amount();
            }
        }
        return base;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (isFriendlyClone() && friendlyLifespanTicks > 0) {
            friendlyLifespanTicks--;
            if (friendlyLifespanTicks <= 0) {
                this.discard();
                return;
            }
        }

        if (rangedAttackCooldown > 0) rangedAttackCooldown--;
        if (meleeTickCooldown > 0) meleeTickCooldown--;

        // Seção 4.3: A cada 30 ticks (1.5s), varre 10x8x10 e atualiza o mimetismo no jogador mais próximo
        if (!isFriendlyClone() && this.tickCount % 30 == 0) {
            AABB scanBox = this.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
            Player nearest = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 12.0D, true);
            if (nearest != null && scanBox.intersects(nearest.getBoundingBox())) {
                mimicPlayer(nearest);
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            // Seção 4.3 (item 5): A cada 60 ticks (3.0s), remove os próprios efeitos e clona todos os efeitos ativos no alvo
            if (this.tickCount % 60 == 0) {
                this.removeAllEffects();
                for (MobEffectInstance effect : target.getActiveEffects()) {
                    this.addEffect(new MobEffectInstance(effect));
                }
            }

            // Seção 4.4: Mecânica Anti-Voo (5% de chance por tick de aplicar Lentidão V por 10s se o alvo estiver voando)
            if (target instanceof Player player && (player.getAbilities().flying || !player.onGround() && player.getDeltaMovement().y > 0.05D)) {
                if (this.random.nextFloat() < 0.05F) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                }
            }

            // Seção 4.4: Habilidade de Atração (*Attraho*) se perder linha de visão/caminho ou alvo fugir além de 8 blocos
            if (this.tickCount % 40 == 0 && (this.distanceToSqr(target) > 64.0D || !this.getSensing().hasLineOfSight(target))) {
                Vec3 pull = this.position().subtract(target.position()).normalize().scale(1.15D);
                target.setDeltaMovement(pull.x, 0.35D, pull.z);
                target.hurtMarked = true;
                this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.7F);
            }

            ItemStack held = this.getMainHandItem();
            if (held.getItem() instanceof MysticBranchItem) {
                if (rangedAttackCooldown <= 0 && this.distanceToSqr(target) <= 400.0D) {
                    rangedAttackCooldown = 20;
                    // 50% de chance de conjurar feitiço ponderado na tabela da Seção 4.4
                    if (this.random.nextFloat() < 0.50F) {
                        castWeightedMysticBranchSpell(target);
                    }
                }
            } else if (held.getItem() instanceof HandBowItem || held.getItem() instanceof BowItem || held.getItem() instanceof CrossbowItem) {
                if (rangedAttackCooldown <= 0 && this.distanceToSqr(target) <= 400.0D) {
                    rangedAttackCooldown = 25;
                    fireAdaptiveBolt(target);
                }
            } else {
                // Combate corpo a corpo acelerado: golpes a cada 10 ticks quando no alcance
                if (meleeTickCooldown <= 0 && this.distanceToSqr(target) <= 6.25D) {
                    meleeTickCooldown = 10;
                    this.doHurtTarget(target);
                }
            }
        }
    }

    /**
     * Seção 4.4: Conjura um feitiço da tabela exata de pesos:
     * - Ignianima: 14.0 (66.7%)
     * - Expelliarmus: 2.0 (9.5%)
     * - Flipendo: 2.0 (9.5%)
     * - Impedimenta: 2.0 (9.5%)
     * - Confundus: 1.0 (4.8%)
     * Total = 21.0
     */
    private void castWeightedMysticBranchSpell(LivingEntity target) {
        double roll = this.random.nextDouble() * 21.0D;
        if (roll < 14.0D) {
            // Ignianima: dano de fogo puro + incendeia
            target.igniteForSeconds(6.0F);
            target.hurt(this.damageSources().onFire(), 6.0F);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
            }
        } else if (roll < 16.0D) {
            // Expelliarmus: desarma o jogador
            if (target instanceof Player player && !player.getMainHandItem().isEmpty()) {
                ItemStack dropped = player.getMainHandItem().copy();
                player.setItemInHand( net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                ItemEntity itemEntity = player.drop(dropped, true);
                if (itemEntity != null) {
                    itemEntity.setPickUpDelay(40);
                }
            }
        } else if (roll < 18.0D) {
            // Flipendo: arremessa para trás com impacto cinético
            Vec3 push = target.position().subtract(this.position()).normalize().scale(1.6D);
            target.setDeltaMovement(push.x, 0.45D, push.z);
            target.hurtMarked = true;
            target.hurt(this.damageSources().magic(), 4.0F);
        } else if (roll < 20.0D) {
            // Impedimenta: Lentidão profunda
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 3));
        } else {
            // Confundus: Náusea severa
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.2F);
    }

    /**
     * Seção 4.4: Seleção inteligente de virotes da Pistola-Besta:
     * - Contra Lobisomem -> Virote de Prata (ignora defesas licantrópicas + bônus de prata).
     * - Contra Vampiro -> Virote Solar (queima solar instantânea de 100 ticks = 5s).
     * - Contra humanos -> 25% Virote Sagrado ou 75% virote perfurante.
     */
    private void fireAdaptiveBolt(LivingEntity target) {
        this.level().playSound(null, this.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
        if (target instanceof Player player) {
            if (LycanthropySystem.isWerewolf(player)) {
                // Virote de Prata: causa dano direto + 5.0 de prata pura
                float raw = 8.0F;
                player.setHealth(Math.max(0.0F, player.getHealth() - (raw + 5.0F)));
                player.hurt(this.damageSources().mobAttack(this), 0.1F);
                return;
            }
            if (VampirismSystem.isVampire(player)) {
                // Virote Solar: queima solar instantânea de 100 ticks
                player.igniteForSeconds(5.0F);
                VampirismSystem.drainBlood(player, 120);
                player.hurt(this.damageSources().onFire(), 6.0F);
                return;
            }
        }
        if (this.random.nextFloat() < 0.25F) {
            target.hurt(this.damageSources().magic(), 7.0F);
        } else {
            target.hurt(this.damageSources().mobProjectile(this, this), 6.0F);
        }
    }

    /**
     * Seção 4.2: Teto rígido de dano por golpe: máximo de 6.0 HP (3 corações) por impacto,
     * ou 2.0 HP se estiver em trânsito místico.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        float cap = inMysticTransit ? 2.0F : 6.0F;
        float capped = Math.min(amount, cap);
        return super.hurt(source, capped);
    }

    /**
     * Seção 4.5: Ao ser abatido:
     * 1. Dropa obrigatoriamente 1x Coração de Demônio (`DEMON_HEART`).
     * 2. Notifica o Espelho Inquebrável da câmara e o Espelho Material correspondente no Overworld,
     *    definindo `demonSlain = true` permanentemente.
     */
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide && !isFriendlyClone() && this.level() instanceof ServerLevel serverLevel) {
            this.spawnAtLocation(new ItemStack(ModItems.DEMON_HEART.get(), 1));

            if (boundMirrorPos != null && serverLevel.getBlockEntity(boundMirrorPos) instanceof MysticMirrorBlockEntity cellBe) {
                cellBe.setDemonSlain(true);
            }
            ServerLevel overworld = serverLevel.getServer().overworld();
            if (materialMirrorPos != null && overworld.getBlockEntity(materialMirrorPos) instanceof MysticMirrorBlockEntity matBe) {
                matBe.setDemonSlain(true);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        getMimickedUuid().ifPresent(uuid -> compound.putUUID("MimickedUUID", uuid));
        compound.putString("MimickedName", getMimickedName());
        compound.putBoolean("MimickedWolfman", isMimickedWolfman());
        compound.putBoolean("MimickedVampire", isMimickedVampire());
        compound.putBoolean("FriendlyClone", isFriendlyClone());
        compound.putInt("FriendlyLifespan", friendlyLifespanTicks);
        if (boundMirrorPos != null) {
            compound.putInt("BoundMirrorX", boundMirrorPos.getX());
            compound.putInt("BoundMirrorY", boundMirrorPos.getY());
            compound.putInt("BoundMirrorZ", boundMirrorPos.getZ());
        }
        if (materialMirrorPos != null) {
            compound.putInt("MatMirrorX", materialMirrorPos.getX());
            compound.putInt("MatMirrorY", materialMirrorPos.getY());
            compound.putInt("MatMirrorZ", materialMirrorPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("MimickedUUID")) {
            this.entityData.set(MIMICKED_UUID, Optional.of(compound.getUUID("MimickedUUID")));
        }
        if (compound.contains("MimickedName")) {
            this.entityData.set(MIMICKED_NAME, compound.getString("MimickedName"));
        }
        this.entityData.set(MIMICKED_WOLFMAN, compound.getBoolean("MimickedWolfman"));
        this.entityData.set(MIMICKED_VAMPIRE, compound.getBoolean("MimickedVampire"));
        this.entityData.set(FRIENDLY_CLONE, compound.getBoolean("FriendlyClone"));
        this.friendlyLifespanTicks = compound.getInt("FriendlyLifespan");
        if (compound.contains("BoundMirrorX")) {
            boundMirrorPos = new BlockPos(compound.getInt("BoundMirrorX"), compound.getInt("BoundMirrorY"), compound.getInt("BoundMirrorZ"));
        }
        if (compound.contains("MatMirrorX")) {
            materialMirrorPos = new BlockPos(compound.getInt("MatMirrorX"), compound.getInt("MatMirrorY"), compound.getInt("MatMirrorZ"));
        }
    }
}
