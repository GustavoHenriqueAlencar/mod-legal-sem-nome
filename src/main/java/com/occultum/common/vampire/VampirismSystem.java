package com.occultum.common.vampire;

import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.entity.AttackBatEntity;
import com.occultum.common.network.OccultumNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModItems;
import com.occultum.common.registry.ModMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import com.occultum.OccultumMod;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Sistema Completo de Vampirismo (Níveis 0 a 10) conforme `08_VAMPIRISMO_E_LICANTROPIA.md`:
 * - Capacidade máxima de sangue: 500 + Lv * 250 (ou 500 + floor(0.5 * Lv) * 250 para híbridos lobisomem >= 2).
 * - Alimentação DRINK: alcance 1.3 normal (d^2 <= 1.69) e 2.1 hipnotizado (d^2 <= 4.41),
 *   volume 10 (15 com vestes vampíricas, 2 em morcego), reação de guardas em 16x8x16,
 *   teto de 25% em animais, refluxo tóxico de 4.0 de dano e chamas em lobisomens.
 * - Livro dos Vampiros e teto de nível (3 inicial -> até 10 com 9 páginas).
 * - Missões exatas do Nível 0 ao 10.
 * - Habilidades ativas: DRINK, MESMERIZE (+ Visão Noturna agachado), golpe cinético agachado (Lv >= 3),
 *   SPEED (Lv >= 4), quebra de pedra agachado (Lv >= 6), Morcego BAT (Lv >= 7) e Ultimates STORM, SWARM, FARM (Lv 10).
 * - Fraquezas exatas: trava em 1.0 HP a danos comuns, combustão solar (-60 sangue/ciclo, morte após 400 ticks sem sangue),
 *   morte instantânea por Estaca de Madeira dormindo, vulnerabilidade a fogo e repulsão da Guirlanda de Alho.
 */
public final class VampirismSystem {

    private static final ResourceLocation UNARMED_DAMAGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "vampire_unarmed_damage");

    public static final List<String> POWER_CYCLE = List.of("DRINK", "MESMERIZE", "SPEED", "BAT", "ULTIMATE");

    private VampirismSystem() {}

    public static int getVampireLevel(Player player) {
        return player.getData(ModAttachmentTypes.VAMPIRE_LEVEL.get());
    }

    public static boolean isVampire(Player player) {
        return getVampireLevel(player) >= 1;
    }

    /**
     * Seção 2.1.2:
     * Para Vampiro Puro: B_max = 500 + (L_v * 250)
     * Para Híbrido (Lobisomem Nível >= 2): B_max = 500 + (floor(0.5 * L_v) * 250)
     */
    public static int getMaxBlood(Player player) {
        int lv = getVampireLevel(player);
        if (lv <= 0) return 0;
        int lw = player.getData(ModAttachmentTypes.WEREWOLF_LEVEL.get());
        if (lw >= 2) {
            return 500 + ((lv / 2) * 250);
        }
        return 500 + (lv * 250);
    }

    public static int getBlood(Player player) {
        return player.getData(ModAttachmentTypes.BLOOD_LEVEL.get());
    }

    public static void setBlood(Player player, int amount) {
        int max = getMaxBlood(player);
        int clamped = Math.max(0, Math.min(max, amount));
        player.setData(ModAttachmentTypes.BLOOD_LEVEL.get(), clamped);
        // Missão 1 -> 2 (A Saciedade): preenchimento absoluto 750/750
        if (getVampireLevel(player) == 1 && clamped >= 750) {
            tryAdvanceVampireLevel(player, 2);
        }
        OccultumNetwork.syncToClient(player);
    }

    public static void addBlood(Player player, int delta) {
        setBlood(player, getBlood(player) + delta);
    }

    /**
     * Despertar Vampírico (0 -> 1) ou mudança de nível.
     */
    public static void setVampireLevel(Player player, int newLevel) {
        int clamped = Math.max(0, Math.min(10, newLevel));
        player.setData(ModAttachmentTypes.VAMPIRE_LEVEL.get(), clamped);
        player.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), 0);
        player.setData(ModAttachmentTypes.VAMPIRE_QUEST_DATA.get(), "");

        if (clamped == 0) {
            player.setData(ModAttachmentTypes.BLOOD_LEVEL.get(), 0);
            player.setData(ModAttachmentTypes.HUMAN_BLOOD.get(), 500);
            player.setData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get(), false);
            player.setData(ModAttachmentTypes.VAMPIRE_VISION_ACTIVE.get(), false);
            player.setData(ModAttachmentTypes.VAMPIRE_SUN_TICKS.get(), 0);
        } else {
            // Seção 2.1.1: Extinção do sangue humano imediata para 0 unidades
            player.setData(ModAttachmentTypes.HUMAN_BLOOD.get(), 0);
            if (clamped == 1 && getBlood(player) == 0) {
                // Começa com 125 unidades no momento do despertar
                player.setData(ModAttachmentTypes.BLOOD_LEVEL.get(), 125);
            }
        }
        updateVampireAttributes(player);
        player.refreshDimensions();
        OccultumNetwork.syncToClient(player);
    }

    public static boolean tryAdvanceVampireLevel(Player player, int targetLevel) {
        int current = getVampireLevel(player);
        if (targetLevel != current + 1) return false;
        int cap = player.getData(ModAttachmentTypes.VAMPIRE_LEVEL_CAP.get());
        if (targetLevel > 3 && cap < targetLevel) {
            player.displayClientMessage(
                    Component.translatable("message.occultum.vampire.need_book_cap", targetLevel).withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }
        setVampireLevel(player, targetLevel);
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(
                Component.translatable("message.occultum.vampire.level_up", targetLevel).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                false
        );
        return true;
    }

    public static void updateVampireAttributes(Player player) {
        AttributeInstance attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr == null) return;
        attackAttr.removeModifier(UNARMED_DAMAGE_MODIFIER_ID);
        int lv = getVampireLevel(player);
        if (lv >= 1) {
            double bonus = lv >= 8 ? 3.0D : (lv >= 4 ? 2.0D : 1.0D);
            attackAttr.addTransientModifier(new AttributeModifier(
                    UNARMED_DAMAGE_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    public static boolean hasFullVampireAttire(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(ModItems.WITCHES_ROBES.get()) || chest.is(ModItems.NECROMANCER_ROBES.get());
    }

    public static boolean hasExtendedFireProtection(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return (head.is(ModItems.WITCHES_HAT.get()) || head.is(ModItems.BABA_YAGAS_HAT.get()))
                && (chest.is(ModItems.WITCHES_ROBES.get()) || chest.is(ModItems.NECROMANCER_ROBES.get()));
    }

    public static void drainBlood(Player player, int amount) {
        addBlood(player, -Math.abs(amount));
    }

    public static void cureVampirism(Player player) {
        setVampireLevel(player, 0);
        player.displayClientMessage(
                Component.translatable("message.occultum.vampire.cured").withStyle(ChatFormatting.GOLD),
                false
        );
    }

    /**
     * Ciclo de seleção de poderes vampíricos (DRINK -> MESMERIZE -> SPEED -> BAT -> ULTIMATE).
     */
    public static void cycleSelectedPower(Player player) {
        int lv = getVampireLevel(player);
        if (lv <= 0) return;
        String current = player.getData(ModAttachmentTypes.VAMPIRE_SELECTED_POWER.get());
        List<String> unlocked = new ArrayList<>();
        unlocked.add("DRINK");
        if (lv >= 2) unlocked.add("MESMERIZE");
        if (lv >= 4) unlocked.add("SPEED");
        if (lv >= 7) unlocked.add("BAT");
        if (lv >= 10 && !"NONE".equals(player.getData(ModAttachmentTypes.VAMPIRE_ULTIMATE_TYPE.get()))) {
            unlocked.add("ULTIMATE");
        }
        int idx = unlocked.indexOf(current);
        String next = unlocked.get((idx + 1) % unlocked.size());
        player.setData(ModAttachmentTypes.VAMPIRE_SELECTED_POWER.get(), next);
        OccultumNetwork.syncToClient(player);
        player.displayClientMessage(
                Component.translatable("message.occultum.vampire.power_selected", next).withStyle(ChatFormatting.DARK_RED),
                true
        );
    }

    /**
     * Ativação da habilidade vampírica selecionada.
     */
    public static boolean activateSelectedPower(ServerLevel level, Player player, @Nullable LivingEntity explicitTarget) {
        int lv = getVampireLevel(player);
        if (lv <= 0) return false;
        String power = player.getData(ModAttachmentTypes.VAMPIRE_SELECTED_POWER.get());

        return switch (power) {
            case "DRINK" -> {
                LivingEntity target = explicitTarget != null ? explicitTarget : raycastTarget(level, player, 2.2D);
                yield target != null && performDrink(level, player, target);
            }
            case "MESMERIZE" -> performMesmerizeOrVision(level, player, explicitTarget);
            case "SPEED" -> performSpeedBurst(level, player);
            case "BAT" -> toggleBatForm(level, player);
            case "ULTIMATE" -> performUltimate(level, player);
            default -> false;
        };
    }

    /**
     * Seção 2.1.3: Alimentação e Drenagem (DRINK).
     */
    public static boolean performDrink(ServerLevel level, Player vampire, LivingEntity target) {
        int lv = getVampireLevel(vampire);
        if (lv <= 0 || target == vampire || !target.isAlive()) return false;

        boolean paralyzed = target.hasEffect(ModMobEffects.PARALYSIS) || target.isSleeping();
        double maxDistSq = paralyzed ? 4.41D : 1.69D; // 2.1^2 = 4.41, 1.3^2 = 1.69
        if (vampire.distanceToSqr(target) > maxDistSq) {
            vampire.displayClientMessage(Component.translatable("message.occultum.vampire.too_far").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        // Lobisomens: refluxo tóxico instantâneo (4.0 dano puro, chamas e 0 sangue)
        boolean targetIsWerewolf = (target instanceof Player tp && tp.getData(ModAttachmentTypes.WEREWOLF_LEVEL.get()) >= 1)
                || target.getType() == ModEntityTypes.WOLFMAN.get();
        if (targetIsWerewolf) {
            vampire.hurt(level.damageSources().magic(), 4.0F);
            vampire.igniteForSeconds(4.0F);
            level.playSound(null, vampire.blockPosition(), SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 1.0F, 1.0F);
            vampire.displayClientMessage(Component.translatable("message.occultum.vampire.toxic_werewolf_blood").withStyle(ChatFormatting.RED), true);
            return true;
        }

        boolean inBatForm = vampire.getData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get());
        int drainUnits = inBatForm ? 2 : (hasFullVampireAttire(vampire) ? 15 : 10);

        // Animais Comuns: 1.0 dano, +2 sangue, teto dietético de 25% de B_max
        if (target instanceof Animal) {
            int maxAnimalCap = (int) (0.25D * getMaxBlood(vampire));
            target.hurt(level.damageSources().mobAttack(vampire), 1.0F);
            if (getBlood(vampire) < maxAnimalCap) {
                setBlood(vampire, Math.min(maxAnimalCap, getBlood(vampire) + 2));
            } else {
                vampire.displayClientMessage(Component.translatable("message.occultum.vampire.animal_cap_reached").withStyle(ChatFormatting.YELLOW), true);
            }
            level.playSound(null, vampire.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.8F, 1.2F);
            return true;
        }

        // Outros Jogadores: drena sangue humano (máx 500)
        if (target instanceof Player victimPlayer) {
            int victimBlood = victimPlayer.getData(ModAttachmentTypes.HUMAN_BLOOD.get());
            if (victimBlood <= 0) return false;
            int drainedFromVictim = Math.min(victimBlood, drainUnits * 2);
            int remaining = victimBlood - drainedFromVictim;
            victimPlayer.setData(ModAttachmentTypes.HUMAN_BLOOD.get(), remaining);

            int gained = victimPlayer.isSleeping() ? drainUnits : (int) Math.ceil(0.66D * drainUnits);
            addBlood(vampire, gained);

            if (remaining < 250) {
                victimPlayer.hurt(level.damageSources().mobAttack(vampire), 1.0F);
            } else if (!victimPlayer.isSleeping()) {
                victimPlayer.hurt(level.damageSources().mobAttack(vampire), 0.1F);
            }
            level.playSound(null, vampire.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.9F, 0.9F);
            return true;
        }

        // Aldeões e Guardas (Iron Golem / Villager)
        if (target instanceof Villager || target instanceof IronGolem) {
            int targetBlood = target.getPersistentData().contains("OccultumVillagerBlood")
                    ? target.getPersistentData().getInt("OccultumVillagerBlood")
                    : 500;
            int stepDrain = Math.min(targetBlood, 50);
            int newTargetBlood = Math.max(0, targetBlood - stepDrain);
            target.getPersistentData().putInt("OccultumVillagerBlood", newTargetBlood);

            addBlood(vampire, drainUnits);
            level.playSound(null, vampire.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.9F, 0.9F);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.2, 0.2, 0.2, 0.05);

            // Se não estiver paralisado/hipnotizado, alerta guardas num raio de 16x8x16 com linha de visão
            if (!paralyzed) {
                AABB guardBox = new AABB(vampire.blockPosition()).inflate(16.0D, 8.0D, 16.0D);
                for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class, guardBox)) {
                    if (golem.hasLineOfSight(vampire)) {
                        golem.setTarget(vampire);
                    }
                }
            }

            if (newTargetBlood < 240 && newTargetBlood > 0 && !paralyzed) {
                target.hurt(level.damageSources().mobAttack(vampire), 2.0F);
            } else if (newTargetBlood == 0) {
                // Morte por exsanguinação se não estiver sendo transformado no nível 9->10
                if (lv < 9) {
                    target.hurt(level.damageSources().mobAttack(vampire), 100.0F);
                }
            }

            // Verificar Missão 2 -> 3 (O Autocontrole: 5 aldeões entre 250 e 280 sem matar)
            handleQuest2To3VillagerFeed(vampire, target, newTargetBlood);
            // Verificar Missão 8 -> 9 (O Mestre do Calabouço: cela de 15 barras de ferro e teto 9 blocos)
            handleQuest8To9DungeonFeed(level, vampire, target, newTargetBlood);
            return true;
        }

        // Demais humanoides vivos
        target.hurt(level.damageSources().mobAttack(vampire), 1.0F);
        addBlood(vampire, drainUnits / 2);
        level.playSound(null, vampire.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.8F, 1.0F);
        return true;
    }

    private static void handleQuest2To3VillagerFeed(Player vampire, LivingEntity villager, int newBlood) {
        if (getVampireLevel(vampire) != 2) return;
        if (newBlood < 240) {
            // Se cair abaixo de 240 unidades, o contador zera imediatamente de volta a 0
            vampire.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), 0);
            vampire.setData(ModAttachmentTypes.VAMPIRE_QUEST_DATA.get(), "");
            vampire.displayClientMessage(Component.translatable("message.occultum.vampire.quest2_failed").withStyle(ChatFormatting.RED), true);
            return;
        }
        if (newBlood >= 250 && newBlood <= 280) {
            String data = vampire.getData(ModAttachmentTypes.VAMPIRE_QUEST_DATA.get());
            String id = villager.getUUID().toString();
            Set<String> fedSet = new HashSet<>(Arrays.asList(data.split(";")));
            fedSet.remove("");
            if (fedSet.add(id)) {
                vampire.setData(ModAttachmentTypes.VAMPIRE_QUEST_DATA.get(), String.join(";", fedSet));
                vampire.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), fedSet.size());
                if (fedSet.size() >= 5) {
                    tryAdvanceVampireLevel(vampire, 3);
                }
            }
        }
    }

    private static void handleQuest8To9DungeonFeed(ServerLevel level, Player vampire, LivingEntity villager, int newBlood) {
        if (getVampireLevel(vampire) != 8) return;
        if (newBlood < 240) {
            vampire.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), 0);
            return;
        }
        if (newBlood >= 250 && newBlood <= 280 && isInsideValidIronBarCell(level, villager.blockPosition())) {
            int count = vampire.getData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get()) + 1;
            vampire.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), count);
            if (count >= 5) {
                tryAdvanceVampireLevel(vampire, 9);
            }
        }
    }

    /**
     * Seção 2.3 (8 -> 9): Masmorra/cela contendo pelo menos 15 Barras de Ferro num perímetro 3x3 e 9 blocos sólidos no teto.
     */
    public static boolean isInsideValidIronBarCell(ServerLevel level, BlockPos pos) {
        int ironBars = 0;
        int roofBlocks = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    if (level.getBlockState(pos.offset(dx, dy, dz)).is(Blocks.IRON_BARS)) {
                        ironBars++;
                    }
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 2; dy <= 4; dy++) {
                    if (level.getBlockState(pos.offset(dx, dy, dz)).isSolid()) {
                        roofBlocks++;
                        break;
                    }
                }
            }
        }
        return ironBars >= 15 && roofBlocks >= 9;
    }

    /**
     * Seção 2.3 (1 -> 2 & 7 -> 8): Habilidade MESMERIZE + Visão Vampírica.
     */
    public static boolean performMesmerizeOrVision(ServerLevel level, Player vampire, @Nullable LivingEntity explicitTarget) {
        int lv = getVampireLevel(vampire);
        if (lv < 2) return false;

        if (vampire.isShiftKeyDown()) {
            boolean next = !vampire.getData(ModAttachmentTypes.VAMPIRE_VISION_ACTIVE.get());
            vampire.setData(ModAttachmentTypes.VAMPIRE_VISION_ACTIVE.get(), next);
            if (!next) {
                vampire.removeEffect(MobEffects.NIGHT_VISION);
            }
            level.playSound(null, vampire.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.7F, next ? 1.4F : 0.8F);
            return true;
        }

        LivingEntity target = explicitTarget != null ? explicitTarget : raycastTarget(level, vampire, 6.0D);
        if (target == null) return false;
        if (getBlood(vampire) < 50) {
            vampire.displayClientMessage(Component.translatable("message.occultum.vampire.no_blood").withStyle(ChatFormatting.RED), true);
            return false;
        }

        addBlood(vampire, -50);
        int durationTicks = (5 + (lv / 2)) * 20;
        int amplifier = lv >= 8 ? 4 : 3; // Nível 4 (amp 3) ou Nível 5 (amp 4) no nível 8+
        target.addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, durationTicks, amplifier));
        level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.2, target.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
        level.playSound(null, target.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }

    /**
     * Seção 2.3 (3 -> 4): Velocidade Sobrenatural (SPEED - Custo: 10 de sangue).
     */
    public static boolean performSpeedBurst(ServerLevel level, Player vampire) {
        int lv = getVampireLevel(vampire);
        if (lv < 4 || getBlood(vampire) < 10) return false;
        addBlood(vampire, -10);
        int amp = Math.min(3, (lv - 2) / 2);
        vampire.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, amp, false, false));
        vampire.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, Math.max(0, amp - 1), false, false));
        level.playSound(null, vampire.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.8F, 1.3F);
        return true;
    }

    /**
     * Seção 2.3 (6 -> 7): Transfiguração em Morcego (BAT - Custo inicial: 50; manutenção: 1/tick).
     */
    public static boolean toggleBatForm(ServerLevel level, Player vampire) {
        int lv = getVampireLevel(vampire);
        if (lv < 7) return false;
        boolean current = vampire.getData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get());
        if (!current) {
            if (getBlood(vampire) < 50) {
                vampire.displayClientMessage(Component.translatable("message.occultum.vampire.no_blood").withStyle(ChatFormatting.RED), true);
                return false;
            }
            addBlood(vampire, -50);
            vampire.setData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get(), true);
            vampire.getAbilities().mayfly = true;
            vampire.getAbilities().flying = true;
            vampire.onUpdateAbilities();
        } else {
            vampire.setData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get(), false);
            if (!vampire.isCreative() && !vampire.isSpectator()) {
                vampire.getAbilities().mayfly = false;
                vampire.getAbilities().flying = false;
                vampire.onUpdateAbilities();
            }
        }
        vampire.refreshDimensions();
        OccultumNetwork.syncToClient(vampire);
        level.playSound(null, vampire.blockPosition(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.SMOKE, vampire.getX(), vampire.getY() + 0.5, vampire.getZ(), 20, 0.3, 0.3, 0.3, 0.03);
        return true;
    }

    /**
     * Seção 2.4: Poderes Supremos (Ultimates - Nível 10): STORM, SWARM, FARM.
     */
    public static boolean performUltimate(ServerLevel level, Player vampire) {
        if (getVampireLevel(vampire) < 10) return false;
        String ult = vampire.getData(ModAttachmentTypes.VAMPIRE_ULTIMATE_TYPE.get());
        int charges = vampire.getData(ModAttachmentTypes.VAMPIRE_ULTIMATE_CHARGES.get());
        if ("NONE".equals(ult) || charges <= 0 || getBlood(vampire) < 50) {
            vampire.displayClientMessage(Component.translatable("message.occultum.vampire.no_ultimate_charges").withStyle(ChatFormatting.RED), true);
            return false;
        }

        addBlood(vampire, -50);
        vampire.setData(ModAttachmentTypes.VAMPIRE_ULTIMATE_CHARGES.get(), charges - 1);
        OccultumNetwork.syncToClient(vampire);

        switch (ult) {
            case "STORM" -> {
                // Duração = (300 + rand(0, 599)) * 20 ticks = 6.000 a 18.000 ticks
                int durationTicks = (300 + level.random.nextInt(600)) * 20;
                level.setWeatherParameters(0, durationTicks, true, true);
                level.playSound(null, vampire.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0F, 0.8F);
                return true;
            }
            case "SWARM" -> {
                // Invoca 15 Morcegos Assassinos (AttackBatEntity) em uma coluna de 3 blocos acima do vampiro
                for (int i = 0; i < 15; i++) {
                    AttackBatEntity bat = ModEntityTypes.ATTACK_BAT.get().create(level);
                    if (bat != null) {
                        double dy = 1.0D + (i % 3);
                        bat.moveTo(vampire.getX() + (level.random.nextDouble() - 0.5D),
                                vampire.getY() + dy,
                                vampire.getZ() + (level.random.nextDouble() - 0.5D),
                                vampire.getYRot(), 0.0F);
                        bat.setOwnerUUID(vampire.getUUID());
                        level.addFreshEntity(bat);
                    }
                }
                level.playSound(null, vampire.blockPosition(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 1.5F, 0.8F);
                return true;
            }
            case "FARM" -> {
                if (vampire instanceof ServerPlayer sp) {
                    BlockPos respawnPos = sp.getRespawnPosition();
                    ServerLevel overworld = level.getServer().overworld();
                    if (respawnPos == null) {
                        respawnPos = overworld.getSharedSpawnPos();
                    }
                    // Se já estiver próximo de sua cama/caixão (distância^2 <= 36.0): teleporta para a Vila mais próxima em raio de 512 blocos
                    if (sp.level().dimension() == sp.getRespawnDimension() && sp.blockPosition().distSqr(respawnPos) <= 36.0D) {
                        BlockPos villagePos = level.findNearestMapStructure(StructureTags.VILLAGE, sp.blockPosition(), 32, false);
                        if (villagePos != null && villagePos.distManhattan(sp.blockPosition()) <= 512) {
                            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, villagePos.getX(), villagePos.getZ());
                            sp.teleportTo(level, villagePos.getX() + 0.5D, y + 1.0D, villagePos.getZ() + 0.5D, sp.getYRot(), sp.getXRot());
                            level.playSound(null, sp.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                            return true;
                        }
                    }
                    ServerLevel targetDim = level.getServer().getLevel(sp.getRespawnDimension());
                    if (targetDim == null) targetDim = overworld;
                    sp.teleportTo(targetDim, respawnPos.getX() + 0.5D, respawnPos.getY() + 0.6D, respawnPos.getZ() + 0.5D, sp.getYRot(), sp.getXRot());
                    targetDim.playSound(null, respawnPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Rotina por Tick do Vampiro:
     * - Regeneração de sangue humano para não-vampiros.
     * - Manutenção da Visão Vampírica e Forma de Morcego (1 sangue/tick).
     * - Vigília das Trevas (Missão 3 -> 4: 300 ticks contínuos sob céu aberto noturno).
     * - Reconhecimento Territorial (Missão 7 -> 8: sobrevoar 3 vilas em forma de morcego).
     * - Exposição e Combustão Solar (Seção 2.5.2).
     * - Repulsão contínua por Guirlanda de Alho (Seção 2.5.5).
     */
    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) return;

        int lv = getVampireLevel(player);
        if (lv <= 0) {
            // Humanos recuperam sangue humano gradualmente até 500
            if (player.tickCount % 100 == 0) {
                int hb = player.getData(ModAttachmentTypes.HUMAN_BLOOD.get());
                if (hb < 500) {
                    player.setData(ModAttachmentTypes.HUMAN_BLOOD.get(), Math.min(500, hb + 5));
                }
            }
            return;
        }

        // Sem necessidade de comida tradicional
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);

        // Visão Vampírica contínua (20s renovada a cada tick)
        if (lv >= 2 && player.getData(ModAttachmentTypes.VAMPIRE_VISION_ACTIVE.get())) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
        }

        // Manutenção da forma de Morcego (1 de sangue por tick contínuo)
        if (player.getData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get())) {
            player.resetFallDistance();
            if (getBlood(player) > 0) {
                player.setData(ModAttachmentTypes.BLOOD_LEVEL.get(), getBlood(player) - 1);
                if (player.tickCount % 20 == 0) {
                    OccultumNetwork.syncToClient(player);
                }
            } else {
                toggleBatForm(level, player);
            }

            // Missão 7 -> 8 (A Revoada Noturna: visitar 3 Vilas geradas distintas em forma de morcego)
            if (lv == 7 && player.tickCount % 40 == 0) {
                BlockPos village = level.findNearestMapStructure(StructureTags.VILLAGE, player.blockPosition(), 4, false);
                if (village != null && village.distSqr(player.blockPosition()) <= 4096.0D) {
                    String key = (village.getX() >> 6) + "," + (village.getZ() >> 6);
                    String data = player.getData(ModAttachmentTypes.VAMPIRE_QUEST_DATA.get());
                    Set<String> visited = new HashSet<>(Arrays.asList(data.split(";")));
                    visited.remove("");
                    if (visited.add(key)) {
                        player.setData(ModAttachmentTypes.VAMPIRE_QUEST_DATA.get(), String.join(";", visited));
                        player.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), visited.size());
                        if (visited.size() >= 3) {
                            tryAdvanceVampireLevel(player, 8);
                        }
                    }
                }
            }
        }

        // Missão 3 -> 4 (Vigília das Trevas: 300 ticks contínuos sob céu aberto noturno)
        if (lv == 3) {
            if (level.dimension() == Level.OVERWORLD && level.isNight() && level.canSeeSky(player.blockPosition()) && !player.isSleeping()) {
                int ticks = player.getData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get()) + 1;
                player.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), ticks);
                if (ticks >= 300) {
                    tryAdvanceVampireLevel(player, 4);
                }
            } else {
                player.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), 0);
            }
        }

        // Repulsão por Guirlanda de Alho num raio de 4 blocos (Seção 2.5.5)
        BlockPos pPos = player.blockPosition();
        for (BlockPos gp : BlockPos.betweenClosed(pPos.offset(-4, -2, -4), pPos.offset(4, 2, 4))) {
            if (level.getBlockState(gp).is(ModBlocks.GARLIC_GARLAND.get())) {
                Vec3 away = player.position().subtract(Vec3.atCenterOf(gp));
                if (away.lengthSqr() < 16.0D && away.lengthSqr() > 1.0E-4D) {
                    Vec3 push = away.normalize().scale(0.45D);
                    player.setDeltaMovement(push.x, 0.15D, push.z);
                    player.hurtMarked = true;
                }
            }
        }

        // Dano e Combustão pela Luz Solar (Seção 2.5.2)
        if (isExposedToDirectSunlight(level, player)) {
            if (lv <= 4) {
                // Níveis 1 a 4: sangue total zerado na hora
                if (getBlood(player) > 0) {
                    setBlood(player, 0);
                }
            } else if (player.tickCount % 20 == 0) {
                // Níveis 5 a 10: drena -60 sangue a cada ciclo + Fraqueza IV, Lentidão I e Cegueira I por 10s
                addBlood(player, -60);
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            }

            if (getBlood(player) <= 0) {
                player.igniteForSeconds(5.0F);
                int sunTicks = player.getData(ModAttachmentTypes.VAMPIRE_SUN_TICKS.get()) + 1;
                player.setData(ModAttachmentTypes.VAMPIRE_SUN_TICKS.get(), sunTicks);
                if (sunTicks > 400) {
                    // Morte Instantânea Irreversível após 400 ticks (20 segundos) sem sangue sob o sol
                    player.setData(ModAttachmentTypes.VAMPIRE_SUN_TICKS.get(), 0);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
                    player.hurt(level.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                    player.kill();
                }
            } else {
                player.setData(ModAttachmentTypes.VAMPIRE_SUN_TICKS.get(), 0);
            }
        } else {
            player.setData(ModAttachmentTypes.VAMPIRE_SUN_TICKS.get(), 0);
        }
    }

    /**
     * Seção 2.5.2: Condições de Isenção de Sol Direto.
     */
    public static boolean isExposedToDirectSunlight(ServerLevel level, Player player) {
        if (level.dimension() == Level.NETHER
                || level.dimension() == Level.END
                || level.dimension() == OccultumDimensions.SPIRIT_WORLD_LEVEL
                || level.dimension() == OccultumDimensions.TORMENT_LEVEL
                || level.dimension() == OccultumDimensions.MIRROR_LEVEL) {
            return false;
        }
        if (level.isNight() || level.isRaining() || level.isThundering()) {
            return false;
        }
        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!level.canSeeSky(eyePos)) {
            return false;
        }
        // Verifica se há qualquer bloco sólido ou translúcido acima da cabeça
        int maxY = level.getMaxBuildHeight();
        for (int y = eyePos.getY() + 1; y < maxY; y++) {
            BlockState above = level.getBlockState(new BlockPos(eyePos.getX(), y, eyePos.getZ()));
            if (!above.isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Seção 2.5: Tratamento de Dano Recebido pelo Vampiro:
     * - Imunidade 100% a afogamento.
     * - Estaca de Madeira no coração enquanto dorme -> Morte Instantânea Irreversível.
     * - Vulnerabilidade a Fogo/Chamas ao zerar HP (100% sem vestes, 75% com vestes, 25% com proteção estendida).
     * - Imortalidade a danos convencionais (trava em 1.0 HP, +5.0 exaustão, 25% chance de pegar fogo por 2s se impacto físico).
     */
    public static boolean onVampireIncomingDamage(Player player, DamageSource source, float amount) {
        int lv = getVampireLevel(player);
        if (lv <= 0) return false;

        // 6. Imunidade total a afogamento
        if (source.is(DamageTypes.DROWN)) {
            return true;
        }

        // 3. Estaca de Madeira no Coração quando dormindo em cama ou caixão
        if (player.isSleeping() && source.getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.is(ModItems.WOODEN_STAKE.get())) {
                if (!attacker.hasInfiniteMaterials()) {
                    weapon.shrink(1);
                }
                player.stopSleeping();
                player.setHealth(0.0F);
                player.die(source);
                return false;
            }
        }

        // Se o golpe reduziria a vida para <= 0
        if (player.getHealth() - amount <= 0.0F) {
            if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
                return false;
            }
            boolean isFireDamage = source.is(DamageTypes.IN_FIRE)
                    || source.is(DamageTypes.ON_FIRE)
                    || source.is(DamageTypes.LAVA)
                    || player.isOnFire();

            if (isFireDamage) {
                float deathChance = 1.0F;
                if (hasExtendedFireProtection(player)) {
                    deathChance = 0.25F;
                } else if (hasFullVampireAttire(player)) {
                    deathChance = 0.75F;
                }
                if (player.level().random.nextFloat() < deathChance) {
                    return false; // Permite a morte por fogo
                }
            }

            // Imortalidade Relativa a Danos Convencionais: trava em 1.0 HP
            player.setHealth(1.0F);
            player.causeFoodExhaustion(5.0F);
            if (source.getDirectEntity() != null && player.level().random.nextFloat() < 0.25F) {
                player.igniteForSeconds(2.0F);
            }
            return true; // Cancela o dano fatal
        }
        return false;
    }

    /**
     * Seção 2.3 (2 -> 3): Golpe Cinético Vampírico agachado na forma humana (1.8x olhar + 0.9 Y).
     */
    public static void onVampireAttackEntity(Player vampire, LivingEntity target) {
        int lv = getVampireLevel(vampire);
        if (lv >= 3 && vampire.isShiftKeyDown() && !vampire.getData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get())) {
            Vec3 look = vampire.getLookAngle().normalize();
            target.setDeltaMovement(look.x * 1.8D, 0.9D, look.z * 1.8D);
            target.hurtMarked = true;
        }
    }

    /**
     * Seção 2.3 (5 -> 6): Quebra de pedras/pedregulhos com as mãos agachado consumindo 10 de exaustão.
     * Seção 2.5.5: Tentativa de minerar Guirlanda de Alho incendeia o vampiro por 1s e cancela.
     */
    public static boolean onVampireLeftClickBlock(ServerLevel level, Player vampire, BlockPos pos, BlockState state) {
        int lv = getVampireLevel(vampire);
        if (lv <= 0) return false;

        if (state.is(ModBlocks.GARLIC_GARLAND.get())) {
            vampire.igniteForSeconds(1.0F);
            return true; // Cancela quebra da guirlanda
        }

        if (lv >= 6 && vampire.isShiftKeyDown() && vampire.getMainHandItem().isEmpty()) {
            if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.STONE_BRICKS)) {
                level.destroyBlock(pos, true, vampire);
                vampire.causeFoodExhaustion(10.0F);
                return true;
            }
        }
        return false;
    }

    /**
     * Seção 2.2: Queda de Páginas do Livro dos Vampiros e Missão 5 -> 6 (20 Blazes no Nether).
     */
    public static void onVampireKillEntity(ServerLevel level, Player killer, LivingEntity victim) {
        int lv = getVampireLevel(killer);

        // Missão 5 -> 6 (Caçada no Inferno: 20 Blazes no Nether)
        if (lv == 5 && level.dimension() == Level.NETHER && victim instanceof Blaze) {
            int kills = killer.getData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get()) + 1;
            killer.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), kills);
            if (kills >= 20) {
                tryAdvanceVampireLevel(killer, 6);
            }
        }

        // Queda de Páginas Perdidas enquanto carregar o Livro dos Vampiros no inventário
        if (hasVampireBookInInventory(killer)) {
            double dropChance = 0.0D;
            if (!victim.canChangeDimensions(level, level) || victim.getMaxHealth() >= 100.0F) {
                dropChance = 1.0D; // Chefes / Bosses: 100%
            } else if (victim instanceof ZombifiedPiglin || victim instanceof EnderMan) {
                dropChance = 0.09D; // 9%
            } else if (victim instanceof Villager) {
                dropChance = 0.10D; // 10%
            } else if (victim instanceof Zombie || victim instanceof net.minecraft.world.entity.monster.AbstractSkeleton) {
                dropChance = 0.02D; // 2%
            }
            if (dropChance > 0.0D && level.random.nextDouble() < dropChance) {
                victim.spawnAtLocation(new ItemStack(ModItems.VAMPIRE_BOOK_PAGE.get()));
            }
        }
    }

    private static boolean hasVampireBookInInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.VAMPIRE_BOOK.get())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Seção 2.3 (0 -> 1): Validação da estrutura 7x7 do Ritual de Invocação de Lilith:
     * - Centro: Crânio de Esqueleto
     * - Anel 3x3 ao redor imediato: 8 traços de Poeira de Redstone
     * - Cantos de raio 3: 4 Tochas acesas
     * - Borda externa: Fios de linha (Tripwire) ou Teias (Cobweb).
     */
    public static boolean validateSkullAltar7x7(ServerLevel level, BlockPos skullPos) {
        BlockState center = level.getBlockState(skullPos);
        if (!center.is(Blocks.SKELETON_SKULL) && !center.is(Blocks.SKELETON_WALL_SKULL)) {
            return false;
        }
        // 8 traços de Redstone no anel 3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (!level.getBlockState(skullPos.offset(dx, 0, dz)).is(Blocks.REDSTONE_WIRE)) {
                    return false;
                }
            }
        }
        // 4 Tochas nos cantos de raio 3
        int[][] corners = {{-3, -3}, {-3, 3}, {3, -3}, {3, 3}};
        for (int[] c : corners) {
            BlockState st = level.getBlockState(skullPos.offset(c[0], 0, c[1]));
            if (!st.is(Blocks.TORCH) && !st.is(Blocks.WALL_TORCH) && !st.is(Blocks.SOUL_TORCH)) {
                return false;
            }
        }
        // Borda externa com Tripwire ou Cobweb
        int stringCount = 0;
        for (int d = -2; d <= 2; d++) {
            if (isThreadOrWeb(level.getBlockState(skullPos.offset(d, 0, -3)))) stringCount++;
            if (isThreadOrWeb(level.getBlockState(skullPos.offset(d, 0, 3)))) stringCount++;
            if (isThreadOrWeb(level.getBlockState(skullPos.offset(-3, 0, d)))) stringCount++;
            if (isThreadOrWeb(level.getBlockState(skullPos.offset(3, 0, d)))) stringCount++;
        }
        return stringCount >= 12;
    }

    private static boolean isThreadOrWeb(BlockState state) {
        return state.is(Blocks.TRIPWIRE) || state.is(Blocks.COBWEB);
    }

    @Nullable
    public static LivingEntity raycastTarget(ServerLevel level, Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        LivingEntity best = null;
        double bestDist = range;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, ent -> ent != player && ent.isAlive())) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(0.35D).clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceTo(hit.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
        }
        return best;
    }
}
