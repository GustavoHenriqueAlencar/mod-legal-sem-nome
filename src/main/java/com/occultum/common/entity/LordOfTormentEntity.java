package com.occultum.common.entity;

import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModItems;
import com.occultum.common.torment.TormentDimensionManager;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * O Soberano do Tormento (LordOfTormentEntity - Seção 2 de `14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md`):
 * - 500.0 HP, velocidade 0.20, dano melee 8.0 base + bônus aleatório de 7 a 21 (15 a 29 total) + impulso +0.4 em Y.
 * - Alcance de perseguição: 50.0 blocos; 100% imune a knockback; 80 XP; hitbox 0.6 x 1.9 blocos; levitação.
 * - Teto de Dano Recebido:
 *   * Comum: limitado a 5.0 HP por impacto.
 *   * Demoníaco: aumentado para 8.0 HP por impacto.
 *   * 100% imune a danos do vazio (FELL_OUT_OF_WORLD).
 * - Mecânica Anti-Voo: a cada tick operacional, 5% de chance de detectar se o alvo está voando e aplicar
 *   Lentidão V (amplificador 4) por 10 segundos (200 ticks).
 * - Fuga com Meia-Vida (<= 250 HP fora da Dimensão do Tormento):
 *   * Seleciona um andar aleatório do Tormento (0 a 5).
 *   * Varre todos os jogadores em raio cúbico 16x32x16 + todos os atacantes registrados.
 *   * Envia todos para o andar selecionado e despawna do mundo exterior, surgindo na arena do andar
 *     em `X = 9.0, Y = (10 + nivel * 15) + 1, Z = 36.0` conservando exatamente os 250 HP.
 * - Ataque de Projéteis: dispara feitiço *Tormentum* + saraivada de Fogo da Alma (90% 3 projéteis, 10% 9 projéteis).
 * - Drops Exatos: 2 Livros Encantados de Alto Nível, 1x Coração de Demônio e 1x Poção de Alma do Tormento.
 */
public class LordOfTormentEntity extends Monster {

    private final Set<UUID> attackerUUIDs = new HashSet<>();
    private boolean hasFledToTorment = false;

    public LordOfTormentEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 80;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 50.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public void setHasFledToTorment(boolean fled) {
        this.hasFledToTorment = fled;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel sl)) return;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            // Levitação suave em direção à altura do alvo
            double dy = (target.getY() + 0.8D) - this.getY();
            Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, Math.max(-0.2D, Math.min(0.2D, dy * 0.1D)), m.z);

            // Seção 2.3: Mecânica Anti-Voo (5% de chance a cada tick de aplicar Lentidão V por 10s se o alvo estiver voando)
            if (target instanceof Player tp && (tp.getAbilities().flying || !tp.onGround() && tp.getDeltaMovement().y > 0.05D)) {
                if (sl.random.nextFloat() < 0.05F) {
                    tp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                    if (!tp.isCreative() && !tp.isSpectator()) {
                        tp.getAbilities().flying = false;
                        tp.onUpdateAbilities();
                    }
                }
            }

            // Seção 2.5: Ataque de Projéteis à Distância (Tormentum + 3 ou 9 projéteis de Soulfire)
            if (this.tickCount % 50 == 0 && this.distanceToSqr(target) > 9.0D) {
                performSoulfireBarrage(sl, target);
            }
        }
    }

    private void performSoulfireBarrage(ServerLevel sl, LivingEntity target) {
        Vec3 baseDir = target.getEyePosition().subtract(this.getEyePosition()).normalize();
        // 90% de chance: 3 projéteis; 10% de chance: 9 projéteis em dispersão parabólica
        int count = sl.random.nextFloat() < 0.10F ? 9 : 3;
        int half = count / 2;

        for (int i = -half; i <= half; i++) {
            Vec3 dir = baseDir.yRot(i * 0.12F);
            SmallFireball soulball = new SmallFireball(sl, this, dir);
            soulball.setPos(this.getX() + dir.x, this.getEyeY() - 0.1D, this.getZ() + dir.z);
            if (i == 0) {
                soulball.getPersistentData().putBoolean("OccultumTormentumSpell", true);
            }
            sl.addFreshEntity(soulball);
        }
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getEyeY(), this.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
        sl.playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.5F, 0.7F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity living) {
            // Seção 2.1 e 2.5: 8.0 base + bônus aleatório de 7 a 21 (entre 15 e 29 de dano combinado) + impulso +0.4 em Y
            float damage = 8.0F + 7.0F + this.random.nextInt(15); // 15..29
            boolean hit = living.hurt(this.damageSources().mobAttack(this), damage);
            if (hit) {
                Vec3 cur = living.getDeltaMovement();
                living.setDeltaMovement(cur.x, cur.y + 0.4D, cur.z);
                living.hurtMarked = true;
            }
            return hit;
        }
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Seção 2.2: 100% imune a danos causados pelo vazio
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        if (source.getEntity() instanceof Player p) {
            attackerUUIDs.add(p.getUUID());
        }

        // Teto de Dano: 8.0 HP para fontes demoníacas, 5.0 HP para ataques convencionais
        boolean isDemonicSource = false;
        if (source.getEntity() instanceof LivingEntity attacker) {
            ItemStack main = attacker.getMainHandItem();
            isDemonicSource = main.is(ModItems.HUNTSMANS_SPEAR.get()) || main.is(ModItems.ATHAME.get());
        }
        float maxCap = isDemonicSource ? 8.0F : 5.0F;
        float capped = Math.min(maxCap, amount);

        boolean damaged = super.hurt(source, capped);

        // Seção 2.4: Fuga com Meia-Vida (<= 250 HP fora da Dimensão do Tormento)
        if (damaged && this.isAlive() && !hasFledToTorment
                && this.getHealth() <= 250.0F
                && this.level() instanceof ServerLevel sl
                && sl.dimension() != OccultumDimensions.TORMENT_LEVEL) {
            executeHalfHealthRetreatToTorment(sl);
        }
        return damaged;
    }

    private void executeHalfHealthRetreatToTorment(ServerLevel currentLevel) {
        this.hasFledToTorment = true;
        int floor = currentLevel.random.nextInt(6); // Andar aleatório 0 a 5
        ServerLevel tormentLevel = currentLevel.getServer().getLevel(OccultumDimensions.TORMENT_LEVEL);
        if (tormentLevel == null) return;

        // Varre todos os jogadores em raio cúbico 16x32x16 + todos os atacantes registrados
        Set<ServerPlayer> victims = new HashSet<>();
        AABB box = new AABB(this.blockPosition()).inflate(16.0D, 32.0D, 16.0D);
        victims.addAll(currentLevel.getEntitiesOfClass(ServerPlayer.class, box));
        for (UUID id : attackerUUIDs) {
            ServerPlayer sp = currentLevel.getServer().getPlayerList().getPlayer(id);
            if (sp != null) {
                victims.add(sp);
            }
        }

        currentLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 0.8, 1.2, 0.8, 0.1);
        currentLevel.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 2.0F, 0.6F);

        for (ServerPlayer victim : victims) {
            TormentDimensionManager.sendPlayerToTorment(victim, floor);
        }

        // Posiciona o chefe na arena do andar em X = 9.0, Y = (10 + nivel * 15) + 1, Z = 36.0 com 250 HP
        TormentDimensionManager.generateTormentFloor(tormentLevel, floor);
        LordOfTormentEntity arenaBoss = ModEntityTypes.LORD_OF_TORMENT.get().create(tormentLevel);
        if (arenaBoss != null) {
            double arenaY = (10 + floor * 15) + 1.0D;
            arenaBoss.moveTo(9.0D, arenaY, 36.0D, 180.0F, 0.0F);
            arenaBoss.setHealth(250.0F);
            arenaBoss.setHasFledToTorment(true);
            tormentLevel.addFreshEntity(arenaBoss);
        }
        this.discard();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // 1. Dois Livros Encantados de Alto Nível
        this.spawnAtLocation(createHighLevelEnchantedBook(level, 3));
        this.spawnAtLocation(createHighLevelEnchantedBook(level, 1));
        // 2. 1x Coração de Demônio
        this.spawnAtLocation(new ItemStack(ModItems.DEMON_HEART.get(), 1));
        // 3. 1x Poção de Alma do Tormento
        this.spawnAtLocation(new ItemStack(ModItems.BREW_OF_SOUL_OF_TORMENT.get(), 1));
    }

    private ItemStack createHighLevelEnchantedBook(ServerLevel level, int minOffset) {
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<Holder.Reference<Enchantment>> all = registry.holders().toList();
        if (!all.isEmpty()) {
            Holder<Enchantment> chosen = all.get(level.random.nextInt(all.size()));
            int minLvl = chosen.value().getMinLevel();
            int maxLvl = chosen.value().getMaxLevel();
            int lowerBound = Math.min(maxLvl, minLvl + minOffset);
            int power = lowerBound + (maxLvl > lowerBound ? level.random.nextInt(maxLvl - lowerBound + 1) : 0);
            return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(chosen, power));
        }
        return new ItemStack(ModItems.DEMONIC_BLOOD.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasFledToTorment", hasFledToTorment);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        hasFledToTorment = tag.getBoolean("HasFledToTorment");
    }
}
