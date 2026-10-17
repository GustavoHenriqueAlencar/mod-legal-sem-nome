package com.occultum.common.werewolf;

import com.occultum.OccultumMod;
import com.occultum.common.entity.HornedHuntsmanEntity;
import com.occultum.common.entity.WolfmanEntity;
import com.occultum.common.network.OccultumNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModItems;
import com.occultum.common.registry.ModMobEffects;
import com.occultum.common.vampire.VampirismSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * Sistema Completo de Licantropia (Níveis 0 a 10) conforme a Seção 3 de `08_VAMPIRISMO_E_LICANTROPIA.md`:
 * - Contração por mordida (25% de chance se HP < 25% à noite) ou Ritual #92/#93.
 * - Matriz exata de atributos por Nível e Forma (WOLF e WOLFMAN) da Seção 3.4:
 *   velocidade, pulo vertical, bote horizontal (Leap), vida extra (+4 a +40 HP), dano físico (+1 a +7),
 *   resistência (+0 a +5), queda segura (+2 a +7 blocos) e Teto Rígido de Dano (Damage Cap de 4.0, 3.0 e 2.0 HP).
 * - Habilidades lupinas: escavação rápida agachado (Lv >= 3), devorar carne ao abater (+8 fome e 0.8 saturação no Lv >= 4),
 *   uivo de convocação (Lv >= 3), uivo paralisante em 16 blocos e 16 chunks distintos (Lv 6 -> 7),
 *   estraçalhar 25% da durabilidade da armadura e arrancá-la por 5s (Lv >= 9) e vetor de infecção (Lv 10).
 * - Vulnerabilidade total a Dano por Prata (ignora Damage Cap e Resistência e soma +5.0 de dano puro).
 */
public final class LycanthropySystem {

    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "werewolf_bonus_health");
    private static final ResourceLocation DAMAGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "werewolf_bonus_damage");
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "werewolf_bonus_speed");
    private static final ResourceLocation SAFE_FALL_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "werewolf_safe_fall");
    private static final ResourceLocation JUMP_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "werewolf_jump_strength");

    public record WerewolfStats(
            double speedBonus,
            double jumpVertical,
            double leapHorizontal,
            double extraHealth,
            double attackDamage,
            double resistance,
            double safeFallBlocks,
            float damageCap
    ) {}

    // Tabela Exata Seção 3.4 - Forma Quadrúpede de Lobo (WOLF), Níveis 0 a 10
    private static final WerewolfStats[] WOLF_STATS = {
            new WerewolfStats(0.00D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 4.0F), // Lv 0
            new WerewolfStats(0.50D, 0.2D, 0.2D, 0.0D, 1.0D, 0.0D, 2.0D, 4.0F), // Lv 1
            new WerewolfStats(0.50D, 0.2D, 0.2D, 0.0D, 1.0D, 0.0D, 2.0D, 3.0F), // Lv 2
            new WerewolfStats(0.75D, 0.2D, 0.3D, 0.0D, 2.0D, 0.0D, 2.0D, 3.0F), // Lv 3
            new WerewolfStats(0.75D, 0.2D, 0.4D, 0.0D, 2.0D, 0.0D, 3.0D, 3.0F), // Lv 4
            new WerewolfStats(0.75D, 0.2D, 0.5D, 0.0D, 2.0D, 0.0D, 3.0D, 2.0F), // Lv 5
            new WerewolfStats(1.00D, 0.2D, 0.6D, 0.0D, 2.0D, 1.0D, 3.0D, 2.0F), // Lv 6
            new WerewolfStats(1.25D, 0.3D, 0.7D, 4.0D, 2.0D, 1.0D, 4.0D, 2.0F), // Lv 7
            new WerewolfStats(1.50D, 0.3D, 0.8D, 8.0D, 3.0D, 2.0D, 4.0D, 2.0F), // Lv 8
            new WerewolfStats(1.75D, 0.3D, 0.9D, 12.0D, 3.0D, 3.0D, 5.0D, 2.0F), // Lv 9
            new WerewolfStats(1.75D, 0.3D, 1.0D, 12.0D, 3.0D, 3.0D, 5.0D, 2.0F)  // Lv 10
    };

    // Tabela Exata Seção 3.4 - Forma Bípede de Homem-Lobo (WOLFMAN), Níveis 5 a 10
    private static final WerewolfStats[] WOLFMAN_STATS = {
            new WerewolfStats(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 4.0F), // 0
            new WerewolfStats(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 4.0F), // 1
            new WerewolfStats(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 3.0F), // 2
            new WerewolfStats(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 3.0F), // 3
            new WerewolfStats(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 3.0F), // 4
            new WerewolfStats(0.2D, 0.2D, 0.2D, 20.0D, 4.0D, 3.0D, 3.0D, 2.0F), // Lv 5
            new WerewolfStats(0.2D, 0.3D, 0.2D, 20.0D, 4.0D, 3.0D, 4.0D, 2.0F), // Lv 6
            new WerewolfStats(0.4D, 0.4D, 0.4D, 20.0D, 5.0D, 4.0D, 5.0D, 2.0F), // Lv 7
            new WerewolfStats(0.4D, 0.5D, 0.4D, 30.0D, 6.0D, 4.0D, 6.0D, 2.0F), // Lv 8
            new WerewolfStats(0.5D, 0.6D, 0.6D, 40.0D, 7.0D, 5.0D, 7.0D, 2.0F), // Lv 9
            new WerewolfStats(0.5D, 0.6D, 0.6D, 40.0D, 7.0D, 5.0D, 7.0D, 2.0F)  // Lv 10
    };

    private LycanthropySystem() {}

    public static int getWerewolfLevel(Player player) {
        return player.getData(ModAttachmentTypes.WEREWOLF_LEVEL.get());
    }

    public static boolean isWerewolf(LivingEntity entity) {
        if (entity instanceof Player p) {
            return getWerewolfLevel(p) >= 1;
        }
        return entity instanceof WolfmanEntity;
    }

    public static void cureLycanthropy(Player player) {
        setWerewolfLevel(player, 0);
        player.displayClientMessage(
                Component.translatable("message.occultum.werewolf.cured").withStyle(ChatFormatting.GREEN),
                false
        );
    }

    public static String getWerewolfForm(Player player) {
        return player.getData(ModAttachmentTypes.WEREWOLF_FORM.get());
    }

    public static boolean isTransformed(Player player) {
        String form = getWerewolfForm(player);
        return "WOLF".equals(form) || "WOLFMAN".equals(form);
    }

    public static WerewolfStats getActiveStats(Player player) {
        int lw = Math.max(0, Math.min(10, getWerewolfLevel(player)));
        String form = getWerewolfForm(player);
        if ("WOLFMAN".equals(form) && lw >= 5) {
            return WOLFMAN_STATS[lw];
        }
        if ("WOLF".equals(form)) {
            return WOLF_STATS[lw];
        }
        return WOLF_STATS[0];
    }

    public static void setWerewolfLevel(Player player, int level) {
        int clamped = Math.max(0, Math.min(10, level));
        player.setData(ModAttachmentTypes.WEREWOLF_LEVEL.get(), clamped);
        player.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), 0);
        player.setData(ModAttachmentTypes.WEREWOLF_QUEST_DATA.get(), "");
        if (clamped == 0) {
            setWerewolfForm(player, "NONE");
        }
        applyFormAttributes(player);
        OccultumNetwork.syncToClient(player);
    }

    public static void setWerewolfForm(Player player, String form) {
        int lw = getWerewolfLevel(player);
        if (lw <= 0) form = "NONE";
        if ("WOLFMAN".equals(form) && lw < 5) form = "WOLF";

        player.setData(ModAttachmentTypes.WEREWOLF_FORM.get(), form);
        applyFormAttributes(player);
        player.refreshDimensions();
        OccultumNetwork.syncToClient(player);
    }

    /**
     * Seção 3.3: Verificação de Bloqueio por Acônito (Wolfsbane):
     * Força = 1 + max(0, Amplificador * 3 - 1).
     * Se Força >= L_w, a transfiguração falha.
     */
    public static boolean isBlockedByWolfsbane(Player player) {
        MobEffectInstance inst = player.getEffect(ModMobEffects.WOLFSBANE);
        if (inst == null) return false;
        int strength = 1 + Math.max(0, inst.getAmplifier() * 3 - 1);
        return strength >= getWerewolfLevel(player);
    }

    public static void applyFormAttributes(Player player) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance safeFallAttr = player.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        AttributeInstance jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);

        if (healthAttr != null) healthAttr.removeModifier(HEALTH_MODIFIER_ID);
        if (damageAttr != null) damageAttr.removeModifier(DAMAGE_MODIFIER_ID);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_MODIFIER_ID);
        if (safeFallAttr != null) safeFallAttr.removeModifier(SAFE_FALL_MODIFIER_ID);
        if (jumpAttr != null) jumpAttr.removeModifier(JUMP_MODIFIER_ID);

        if (!isTransformed(player)) {
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
            return;
        }

        WerewolfStats stats = getActiveStats(player);
        if (healthAttr != null && stats.extraHealth() > 0) {
            healthAttr.addTransientModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, stats.extraHealth(), AttributeModifier.Operation.ADD_VALUE
            ));
        }
        if (damageAttr != null && stats.attackDamage() > 0) {
            damageAttr.addTransientModifier(new AttributeModifier(
                    DAMAGE_MODIFIER_ID, stats.attackDamage(), AttributeModifier.Operation.ADD_VALUE
            ));
        }
        if (speedAttr != null && stats.speedBonus() > 0) {
            speedAttr.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID, stats.speedBonus() * 0.1D, AttributeModifier.Operation.ADD_VALUE
            ));
        }
        if (safeFallAttr != null && stats.safeFallBlocks() > 0) {
            safeFallAttr.addTransientModifier(new AttributeModifier(
                    SAFE_FALL_MODIFIER_ID, stats.safeFallBlocks(), AttributeModifier.Operation.ADD_VALUE
            ));
        }
        if (jumpAttr != null && stats.jumpVertical() > 0) {
            jumpAttr.addTransientModifier(new AttributeModifier(
                    JUMP_MODIFIER_ID, stats.jumpVertical(), AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    /**
     * Seção 3.5: O Bote do Predador (Leap) ao pular correndo (sprinting):
     * dX = -sin(theta) * Leap
     * dZ = cos(theta) * Leap
     * dY = Pulo Base + JumpBoost
     */
    public static void onPlayerJump(Player player) {
        if (!isTransformed(player) || !player.isSprinting()) return;
        WerewolfStats stats = getActiveStats(player);
        if (stats.leapHorizontal() <= 0.0D) return;

        double theta = player.getYRot() * (Math.PI / 180.0D);
        double dx = -Math.sin(theta) * stats.leapHorizontal() * 0.55D;
        double dz = Math.cos(theta) * stats.leapHorizontal() * 0.55D;
        Vec3 cur = player.getDeltaMovement();
        player.setDeltaMovement(cur.x + dx, cur.y + (stats.jumpVertical() * 0.25D), cur.z + dz);
        player.hurtMarked = true;
    }

    /**
     * Uivo Lupino (Seção 3.2 Transições 2->3 e 6->7):
     * - Nível >= 3: Uivo de Convocação (invoca lobos aliados).
     * - Nível 6: Registra o chunk atual na missão "O Uivo das Matilhas" (16 chunks diferentes -> avança progresso).
     * - Nível >= 7 na forma WOLFMAN: Uivo Paralisante em raio de 16 blocos (cooldown de 60s = 1200 ticks),
     *   aplicando Paralisia por 4 + rand(0, L_w - 6) segundos com nível de imobilização 3 (amp 2).
     */
    public static boolean performHowl(ServerLevel level, Player player) {
        int lw = getWerewolfLevel(player);
        if (lw < 3 || !isTransformed(player)) return false;

        int cooldown = player.getData(ModAttachmentTypes.WEREWOLF_HOWL_COOLDOWN.get());
        if (cooldown > 0) {
            player.displayClientMessage(Component.translatable("message.occultum.werewolf.howl_cooldown", cooldown / 20).withStyle(ChatFormatting.YELLOW), true);
            return false;
        }

        level.playSound(null, player.blockPosition(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 2.0F, 0.85F);

        // Missão 6 -> 7: Uivar para o céu noturno em 16 Chunks diferentes
        if (lw == 6 && level.isNight() && level.canSeeSky(player.blockPosition())) {
            ChunkPos cp = player.chunkPosition();
            String key = cp.x + "," + cp.z;
            String data = player.getData(ModAttachmentTypes.WEREWOLF_QUEST_DATA.get());
            Set<String> chunks = new HashSet<>(Arrays.asList(data.split(";")));
            chunks.remove("");
            if (chunks.add(key)) {
                player.setData(ModAttachmentTypes.WEREWOLF_QUEST_DATA.get(), String.join(";", chunks));
                player.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), chunks.size());
                player.displayClientMessage(Component.translatable("message.occultum.werewolf.howl_chunk_progress", chunks.size(), 16).withStyle(ChatFormatting.GOLD), true);
            }
        }

        // Nível >= 7 em forma de Homem-Lobo (WOLFMAN): Uivo Paralisante Aterrorizante (raio 16 blocos, cooldown 60s)
        if (lw >= 7 && "WOLFMAN".equals(getWerewolfForm(player))) {
            player.setData(ModAttachmentTypes.WEREWOLF_HOWL_COOLDOWN.get(), 1200);
            AABB box = player.getBoundingBox().inflate(16.0D);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                if (target instanceof Player tp && (getWerewolfLevel(tp) >= 1 || VampirismSystem.isVampire(tp))) {
                    continue;
                }
                if (target instanceof Wolf || target instanceof WolfmanEntity) {
                    continue;
                }
                int extraRand = lw > 6 ? level.random.nextInt(lw - 6 + 1) : 0;
                int durationTicks = (4 + extraRand) * 20;
                target.addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, durationTicks, 2));
            }
            level.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 1.5D, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            return true;
        }

        // Uivo de Convocação (Nível >= 3): invoca 2 lobos aliados temporários
        player.setData(ModAttachmentTypes.WEREWOLF_HOWL_COOLDOWN.get(), 400);
        for (int i = 0; i < 2; i++) {
            Wolf wolf = EntityType.WOLF.create(level);
            if (wolf != null) {
                wolf.moveTo(player.getX() + (i == 0 ? 1.5D : -1.5D), player.getY(), player.getZ() + 1.5D, player.getYRot(), 0.0F);
                wolf.tame(player);
                level.addFreshEntity(wolf);
            }
        }
        return true;
    }

    /**
     * Rotina por Tick do Lobisomem:
     * - Decrementa cooldown do uivo.
     * - Transformação forçada na Lua Cheia à noite se nível >= 1 e não bloqueado por Acônito.
     */
    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) return;
        int lw = getWerewolfLevel(player);
        if (lw <= 0) return;

        int cd = player.getData(ModAttachmentTypes.WEREWOLF_HOWL_COOLDOWN.get());
        if (cd > 0) {
            player.setData(ModAttachmentTypes.WEREWOLF_HOWL_COOLDOWN.get(), cd - 1);
        }

        // Sob Lua Cheia à noite a céu aberto, força transformação se ainda estiver humano
        if (player.tickCount % 100 == 0 && level.isNight() && level.getMoonPhase() == 0
                && level.canSeeSky(player.blockPosition()) && !isTransformed(player) && !isBlockedByWolfsbane(player)) {
            setWerewolfForm(player, lw >= 5 ? "WOLFMAN" : "WOLF");
            level.playSound(null, player.blockPosition(), SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.2F, 0.9F);
        }
    }

    /**
     * Seção 3.6: Regras Matemáticas de Dano Recebido pelo Lobisomem:
     * 1. Dano por Prata (Espada de Prata, Virote de Prata):
     *    - Ignora Teto de Dano (Damage Cap) e Resistência passiva.
     *    - Dano Final = Dano da Arma + 5.0.
     * 2. Dano Convencional (quando transformado em WOLF ou WOLFMAN e não for Prata, Fogo ou Absoluto):
     *    - Dano_1 = max(0, Dano Recebido - Resistência)
     *    - Dano Final = max(min(Dano_1, DamageCap), 0.5)
     */
    public static float computeModifiedWerewolfDamage(Player player, DamageSource source, float rawAmount) {
        int lw = getWerewolfLevel(player);
        if (lw <= 0) return rawAmount;

        // Verificar Dano por Prata
        if (isSilverDamageSource(source)) {
            return rawAmount + 5.0F;
        }

        if (!isTransformed(player)) {
            return rawAmount;
        }

        if (source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.GENERIC_KILL)) {
            return rawAmount;
        }

        WerewolfStats stats = getActiveStats(player);
        float d1 = Math.max(0.0F, rawAmount - (float) stats.resistance());
        if (d1 <= 0.0F) {
            return 0.5F;
        }
        return Math.max(Math.min(d1, stats.damageCap()), 0.5F);
    }

    public static boolean isSilverDamageSource(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            ItemStack main = attacker.getMainHandItem();
            if (main.is(ModItems.SILVER_SWORD.get()) || main.is(ModItems.SILVER_BOLT.get())) {
                return true;
            }
        }
        if (source.getDirectEntity() != null && source.getDirectEntity().getPersistentData().getBoolean("OccultumSilverBolt")) {
            return true;
        }
        return false;
    }

    /**
     * Seção 3.1, 3.2 (Nível 9 e 10):
     * - Garras Dilaceradoras de Armadura (Nível >= 9 em forma WOLFMAN): causa 25% da durabilidade total
     *   a uma peça aleatória de armadura da vítima; se quebrar ou for arrancada, joga no chão com pickUpDelay de 100 ticks (5s).
     * - Vetor de Infecção (Nível 10 ou WolfmanEntity): à noite, se HP da vítima cair abaixo de 25%, 25% de chance de infectar.
     */
    public static void onWerewolfAttackEntity(ServerLevel level, Player attacker, LivingEntity target) {
        int lw = getWerewolfLevel(attacker);
        String form = getWerewolfForm(attacker);

        // Nível >= 9 em forma de Homem-Lobo: Estraçalhar 25% da armadura
        if (lw >= 9 && "WOLFMAN".equals(form)) {
            EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            List<EquipmentSlot> equipped = new ArrayList<>();
            for (EquipmentSlot slot : armorSlots) {
                if (!target.getItemBySlot(slot).isEmpty()) {
                    equipped.add(slot);
                }
            }
            if (!equipped.isEmpty()) {
                EquipmentSlot chosen = equipped.get(level.random.nextInt(equipped.size()));
                ItemStack piece = target.getItemBySlot(chosen);
                if (piece.isDamageableItem()) {
                    int shredDamage = Math.max(1, (int) Math.ceil(piece.getMaxDamage() * 0.25D));
                    int newDmg = piece.getDamageValue() + shredDamage;
                    if (newDmg >= piece.getMaxDamage()) {
                        // Arranca e arremessa no chão com bloqueio de coleta de 5 segundos (100 ticks)
                        ItemStack droppedCopy = piece.copy();
                        droppedCopy.setDamageValue(Math.max(0, piece.getMaxDamage() - 1));
                        target.setItemSlot(chosen, ItemStack.EMPTY);
                        ItemEntity ie = new ItemEntity(level, target.getX(), target.getY() + 0.5D, target.getZ(), droppedCopy);
                        ie.setPickUpDelay(100);
                        level.addFreshEntity(ie);
                        level.playSound(null, target.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 0.9F);
                    } else {
                        piece.setDamageValue(newDmg);
                    }
                }
            }
        }

        // Vetor de Infecção de Licantropia (Nível 10 à noite, vítima < 25% HP -> 25% de chance)
        if (lw >= 10 && isTransformed(attacker) && level.isNight()) {
            tryInfectVictimWithLycanthropy(level, target);
        }
    }

    public static void tryInfectVictimWithLycanthropy(ServerLevel level, LivingEntity target) {
        if (target.getHealth() < target.getMaxHealth() * 0.25F && level.random.nextFloat() < 0.25F) {
            if (target instanceof Player victimPlayer && getWerewolfLevel(victimPlayer) == 0 && !VampirismSystem.isVampire(victimPlayer)) {
                setWerewolfLevel(victimPlayer, 1);
                victimPlayer.displayClientMessage(
                        Component.translatable("message.occultum.werewolf.infected").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                        false
                );
            } else if (target instanceof Villager villager && villager.isAlive()) {
                WolfmanEntity wolfman = com.occultum.common.registry.ModEntityTypes.WOLFMAN.get().create(level);
                if (wolfman != null) {
                    wolfman.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
                    level.addFreshEntity(wolfman);
                    villager.discard();
                }
            }
        }
    }

    /**
     * Seção 3.2 (Nível 2 -> 3): Escavação rápida de terra, areia ou cascalho agachado na forma de lobo.
     */
    public static boolean onWerewolfLeftClickBlock(ServerLevel level, Player player, BlockPos pos, BlockState state) {
        int lw = getWerewolfLevel(player);
        if (lw >= 3 && isTransformed(player) && player.isShiftKeyDown()) {
            if (state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is( net.minecraft.world.level.block.Blocks.GRAVEL)) {
                level.destroyBlock(pos, true, player);
                level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 0.9F, 1.0F);
                return true;
            }
        }
        return false;
    }

    /**
     * Seção 3.2: Eventos de Abate pelo Lobisomem:
     * - Nível >= 4 (Frenesi Predador / Devorar): recupera 8 pontos de fome (4 pernis) e 0.8 de saturação.
     * - Nível 4 -> 5: Abater Horned Huntsman.
     * - Nível 5 -> 6: Abater 10 Monstros Hostis à noite no Overworld em forma de Homem-Lobo.
     * - Nível 8 -> 9: Exterminar 30 Homens-Porco Zumbis (ZombifiedPiglin) no Nether em forma de Homem-Lobo.
     * - Nível 9 -> 10: Abater 1 Aldeão Indefeso ou 1 Jogador Humano em forma de lobo.
     * - Queda de Língua de Cão (Dog Tongue) ao abater lobos.
     */
    public static void onEntityKilled(ServerLevel level, Player killer, LivingEntity victim) {
        if (victim instanceof Wolf) {
            victim.spawnAtLocation(new ItemStack(ModItems.DOG_TONGUE.get()));
        }

        int lw = getWerewolfLevel(killer);
        if (lw <= 0 || !isTransformed(killer)) return;

        // Nível >= 4: Frenesi Predador (Devorar) -> +8 fome e 0.8 saturação
        if (lw >= 4) {
            killer.getFoodData().eat(8, 0.8F);
            level.playSound(null, killer.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 0.9F);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, victim.getX(), victim.getY() + 0.5D, victim.getZ(), 6, 0.2, 0.2, 0.2, 0.05);
        }

        String form = getWerewolfForm(killer);

        // Missão 4 -> 5: Derrotar o Horned Huntsman
        if (lw == 4 && victim instanceof HornedHuntsmanEntity) {
            killer.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), 1);
            killer.displayClientMessage(Component.translatable("message.occultum.werewolf.huntsman_slain").withStyle(ChatFormatting.GOLD), false);
        }

        // Missão 5 -> 6: Expurgo Noturno (10 Monstros Hostis à noite no Overworld em WOLFMAN)
        if (lw == 5 && "WOLFMAN".equals(form) && level.dimension() == net.minecraft.world.level.Level.OVERWORLD
                && level.isNight() && victim instanceof Monster) {
            int c = killer.getData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get()) + 1;
            killer.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), c);
        }

        // Missão 8 -> 9: Carnificina Infernal (30 ZombifiedPiglin no Nether em WOLFMAN)
        if (lw == 8 && "WOLFMAN".equals(form) && level.dimension() == net.minecraft.world.level.Level.NETHER
                && victim instanceof ZombifiedPiglin) {
            int c = killer.getData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get()) + 1;
            killer.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), c);
        }

        // Missão 9 -> 10: O Sacrifício do Alfa (1 Aldeão ou 1 Jogador Humano em forma lupina)
        if (lw == 9 && (victim instanceof Villager || victim instanceof Player)) {
            killer.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), 1);
        }
    }

    /**
     * Missão 7 -> 8 (Comunhão Selvagem): Domesticar 6 Lobos estando na forma de Lobo quadrúpede (WOLF).
     */
    public static void onWolfTamed(Player player) {
        if (getWerewolfLevel(player) == 7 && "WOLF".equals(getWerewolfForm(player))) {
            int c = player.getData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get()) + 1;
            player.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), c);
        }
    }
}
