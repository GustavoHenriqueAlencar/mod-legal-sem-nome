package com.occultum.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class OccultumConfig {

    public static class Common {
        // 7.1 Integrations
        public final ModConfigSpec.BooleanValue allowGeneralModIntegration;
        public final ModConfigSpec.BooleanValue allowThaumcraftIntegration;
        public final ModConfigSpec.BooleanValue allowMfrIntegration;
        public final ModConfigSpec.BooleanValue allowForestryIntegration;
        public final ModConfigSpec.BooleanValue allowTreecapitatorIntegration;
        public final ModConfigSpec.BooleanValue allowNeiIntegration;
        public final ModConfigSpec.BooleanValue allowMystcraftIntegration;
        public final ModConfigSpec.BooleanValue allowArsMagicaIntegration;
        public final ModConfigSpec.BooleanValue respectOtherModProtectionEvents;
        public final ModConfigSpec.BooleanValue respectOtherModGraveChests;

        // 7.2 World Generation
        public final ModConfigSpec.IntValue worldGenFrequency;
        public final ModConfigSpec.BooleanValue generateInTwilightForest;
        public final ModConfigSpec.BooleanValue generateApothecaryInVillages;
        public final ModConfigSpec.ConfigValue<String> apothecaryProfessionId;
        public final ModConfigSpec.BooleanValue generateWitchHutsInSwamps;
        public final ModConfigSpec.BooleanValue generateMysticLibrariesInVillages;
        public final ModConfigSpec.BooleanValue generateCovenCircles;
        public final ModConfigSpec.BooleanValue generateWickerMen;
        public final ModConfigSpec.BooleanValue generateHermitHuts;
        public final ModConfigSpec.BooleanValue generateHobgoblinSettlements;
        public final ModConfigSpec.IntValue villageWallMode;
        public final ModConfigSpec.IntValue villageWallWeight;
        public final ModConfigSpec.IntValue villageCentralTowerMode;
        public final ModConfigSpec.IntValue villageCentralTowerWeight;
        public final ModConfigSpec.BooleanValue allowDesertVillages;
        public final ModConfigSpec.BooleanValue allowPlainsVillages;
        public final ModConfigSpec.BooleanValue allowMountainVillages;
        public final ModConfigSpec.BooleanValue allowHillsVillages;
        public final ModConfigSpec.BooleanValue allowForestVillages;
        public final ModConfigSpec.BooleanValue allowSnowVillages;
        public final ModConfigSpec.BooleanValue allowWastelandVillages;
        public final ModConfigSpec.BooleanValue allowMesaVillages;
        public final ModConfigSpec.BooleanValue allowJungleVillages;
        public final ModConfigSpec.IntValue villageZombieMitigationMode;

        // 7.3 Dimensions
        public final ModConfigSpec.ConfigValue<String> spiritWorldDimensionId;
        public final ModConfigSpec.ConfigValue<String> tormentDimensionId;
        public final ModConfigSpec.ConfigValue<String> mirrorDimensionId;
        public final ModConfigSpec.BooleanValue mirrorWorldSpatialCompression;

        // 7.4 Entities & Population
        public final ModConfigSpec.IntValue covenWitchSpawnWeight;
        public final ModConfigSpec.IntValue hobgoblinBaseSpawnChance;
        public final ModConfigSpec.IntValue hobgoblinDensityWeight;
        public final ModConfigSpec.BooleanValue preventNamedHobgoblinDespawn;
        public final ModConfigSpec.IntValue mogrokManifestationChance;
        public final ModConfigSpec.IntValue hellhoundSpawnWeight;
        public final ModConfigSpec.IntValue spiritSpawnFrequency;
        public final ModConfigSpec.DoubleValue fairestMirrorRecognitionChance;
        public final ModConfigSpec.DoubleValue vampireHunterSpawnChance;

        // 7.5 Lycanthropy & Vampirism
        public final ModConfigSpec.BooleanValue allowHybridVampireWerewolf;
        public final ModConfigSpec.BooleanValue allowStakeVampireKill;
        public final ModConfigSpec.BooleanValue unlockVampireAscensionQuests;
        public final ModConfigSpec.BooleanValue enableBloodCrucibleRitual;
        public final ModConfigSpec.BooleanValue allowPlayerWerewolfTransmission;
        public final ModConfigSpec.IntValue deadVampireItemRetentionMinutes;
        public final ModConfigSpec.BooleanValue showVictimBloodNumberInGui;

        // 7.6 Magic & Balancing
        public final ModConfigSpec.IntValue eclipseRitualCooldownSeconds;
        public final ModConfigSpec.BooleanValue allowHellOnEarthFire;
        public final ModConfigSpec.BooleanValue allowBiomeClimateChangeRituals;
        public final ModConfigSpec.BooleanValue enableGraveItemRecoveryRitual;
        public final ModConfigSpec.BooleanValue allowGoddessStatueCrafting;
        public final ModConfigSpec.BooleanValue allowVoidBrambleCrafting;
        public final ModConfigSpec.BooleanValue allowAnySaplingWoodAsh;
        public final ModConfigSpec.BooleanValue doubleWitchOvenFilterEfficiency;
        public final ModConfigSpec.BooleanValue allowVolatilityBlockDestruction;
        public final ModConfigSpec.IntValue diseasePuddleDissipationChance;
        public final ModConfigSpec.BooleanValue explodeOnEatingRawCreeperHeart;
        public final ModConfigSpec.IntValue minSleepingPercentageForDreamCatch;
        public final ModConfigSpec.BooleanValue restrictPoppetShelvesToMainDimensions;

        // 7.7 Protection & Anti-Griefing
        public final ModConfigSpec.BooleanValue enableAltarCurseProtection;
        public final ModConfigSpec.IntValue altarCurseProtectionRadius;
        public final ModConfigSpec.BooleanValue enableTeleportPullProtection;
        public final ModConfigSpec.IntValue teleportPullProtectionRadius;
        public final ModConfigSpec.BooleanValue blockTaglockInNonPvp;
        public final ModConfigSpec.BooleanValue blockTaglockOnAdmins;

        public Common(ModConfigSpec.Builder builder) {
            builder.comment("Configuracoes Gerais e Mecanicas do Mod Occultum").push("integrations");
            allowGeneralModIntegration = builder.define("allowGeneralModIntegration", true);
            allowThaumcraftIntegration = builder.define("allowThaumcraftIntegration", true);
            allowMfrIntegration = builder.define("allowMfrIntegration", true);
            allowForestryIntegration = builder.define("allowForestryIntegration", true);
            allowTreecapitatorIntegration = builder.define("allowTreecapitatorIntegration", true);
            allowNeiIntegration = builder.define("allowNeiIntegration", true);
            allowMystcraftIntegration = builder.define("allowMystcraftIntegration", true);
            allowArsMagicaIntegration = builder.define("allowArsMagicaIntegration", true);
            respectOtherModProtectionEvents = builder.define("respectOtherModProtectionEvents", true);
            respectOtherModGraveChests = builder.define("respectOtherModGraveChests", true);
            builder.pop();

            builder.push("world_gen");
            worldGenFrequency = builder.comment("Frequencia de geracao estrutural (menor = mais denso)").defineInRange("worldGenFrequency", 12, 1, 100);
            generateInTwilightForest = builder.define("generateInTwilightForest", true);
            generateApothecaryInVillages = builder.define("generateApothecaryInVillages", true);
            apothecaryProfessionId = builder.define("apothecaryProfessionId", "occultum:apothecary");
            generateWitchHutsInSwamps = builder.define("generateWitchHutsInSwamps", true);
            generateMysticLibrariesInVillages = builder.define("generateMysticLibrariesInVillages", true);
            generateCovenCircles = builder.define("generateCovenCircles", true);
            generateWickerMen = builder.define("generateWickerMen", true);
            generateHermitHuts = builder.define("generateHermitHuts", true);
            generateHobgoblinSettlements = builder.define("generateHobgoblinSettlements", true);
            villageWallMode = builder.comment("0 = Desativado, 1 = Ocasional, 2 = Sempre").defineInRange("villageWallMode", 1, 0, 2);
            villageWallWeight = builder.defineInRange("villageWallWeight", 100, 0, 1000);
            villageCentralTowerMode = builder.comment("0 = Desativado, 1 = Ocasional, 2 = Sempre").defineInRange("villageCentralTowerMode", 1, 0, 2);
            villageCentralTowerWeight = builder.defineInRange("villageCentralTowerWeight", 100, 0, 1000);
            allowDesertVillages = builder.define("allowDesertVillages", true);
            allowPlainsVillages = builder.define("allowPlainsVillages", true);
            allowMountainVillages = builder.define("allowMountainVillages", true);
            allowHillsVillages = builder.define("allowHillsVillages", true);
            allowForestVillages = builder.define("allowForestVillages", true);
            allowSnowVillages = builder.define("allowSnowVillages", true);
            allowWastelandVillages = builder.define("allowWastelandVillages", true);
            allowMesaVillages = builder.define("allowMesaVillages", true);
            allowJungleVillages = builder.define("allowJungleVillages", false);
            villageZombieMitigationMode = builder.comment("0 = Normal, 1 = Dano Reduzido, 2 = Zumbis Ignoram Aldeoes").defineInRange("villageZombieMitigationMode", 1, 0, 2);
            builder.pop();

            builder.push("dimensions");
            spiritWorldDimensionId = builder.define("spiritWorldDimensionId", "occultum:spirit_world");
            tormentDimensionId = builder.define("tormentDimensionId", "occultum:torment");
            mirrorDimensionId = builder.define("mirrorDimensionId", "occultum:mirror");
            mirrorWorldSpatialCompression = builder.define("mirrorWorldSpatialCompression", false);
            builder.pop();

            builder.push("entities");
            covenWitchSpawnWeight = builder.defineInRange("covenWitchSpawnWeight", 2, 0, 100);
            hobgoblinBaseSpawnChance = builder.defineInRange("hobgoblinBaseSpawnChance", 2, 0, 100);
            hobgoblinDensityWeight = builder.defineInRange("hobgoblinDensityWeight", 4, 0, 100);
            preventNamedHobgoblinDespawn = builder.define("preventNamedHobgoblinDespawn", true);
            mogrokManifestationChance = builder.defineInRange("mogrokManifestationChance", 10, 0, 100);
            hellhoundSpawnWeight = builder.defineInRange("hellhoundSpawnWeight", 25, 0, 100);
            spiritSpawnFrequency = builder.defineInRange("spiritSpawnFrequency", 1, 0, 100);
            fairestMirrorRecognitionChance = builder.defineInRange("fairestMirrorRecognitionChance", 0.01, 0.0, 1.0);
            vampireHunterSpawnChance = builder.defineInRange("vampireHunterSpawnChance", 0.01, 0.0, 1.0);
            builder.pop();

            builder.push("lycanthropy_and_vampirism");
            allowHybridVampireWerewolf = builder.define("allowHybridVampireWerewolf", true);
            allowStakeVampireKill = builder.define("allowStakeVampireKill", true);
            unlockVampireAscensionQuests = builder.define("unlockVampireAscensionQuests", true);
            enableBloodCrucibleRitual = builder.define("enableBloodCrucibleRitual", true);
            allowPlayerWerewolfTransmission = builder.define("allowPlayerWerewolfTransmission", true);
            deadVampireItemRetentionMinutes = builder.defineInRange("deadVampireItemRetentionMinutes", 12, 1, 120);
            showVictimBloodNumberInGui = builder.define("showVictimBloodNumberInGui", false);
            builder.pop();

            builder.push("magic_and_balance");
            eclipseRitualCooldownSeconds = builder.defineInRange("eclipseRitualCooldownSeconds", 0, 0, 86400);
            allowHellOnEarthFire = builder.define("allowHellOnEarthFire", true);
            allowBiomeClimateChangeRituals = builder.define("allowBiomeClimateChangeRituals", true);
            enableGraveItemRecoveryRitual = builder.define("enableGraveItemRecoveryRitual", true);
            allowGoddessStatueCrafting = builder.define("allowGoddessStatueCrafting", false);
            allowVoidBrambleCrafting = builder.define("allowVoidBrambleCrafting", false);
            allowAnySaplingWoodAsh = builder.define("allowAnySaplingWoodAsh", true);
            doubleWitchOvenFilterEfficiency = builder.define("doubleWitchOvenFilterEfficiency", false);
            allowVolatilityBlockDestruction = builder.define("allowVolatilityBlockDestruction", true);
            diseasePuddleDissipationChance = builder.defineInRange("diseasePuddleDissipationChance", 10, 0, 100);
            explodeOnEatingRawCreeperHeart = builder.define("explodeOnEatingRawCreeperHeart", true);
            minSleepingPercentageForDreamCatch = builder.defineInRange("minSleepingPercentageForDreamCatch", 100, 1, 100);
            restrictPoppetShelvesToMainDimensions = builder.define("restrictPoppetShelvesToMainDimensions", true);
            builder.pop();

            builder.push("protection_and_antigrief");
            enableAltarCurseProtection = builder.define("enableAltarCurseProtection", false);
            altarCurseProtectionRadius = builder.defineInRange("altarCurseProtectionRadius", 32, 1, 256);
            enableTeleportPullProtection = builder.define("enableTeleportPullProtection", false);
            teleportPullProtectionRadius = builder.defineInRange("teleportPullProtectionRadius", 32, 1, 256);
            blockTaglockInNonPvp = builder.define("blockTaglockInNonPvp", false);
            blockTaglockOnAdmins = builder.define("blockTaglockOnAdmins", false);
            builder.pop();
        }
    }

    public static class Client {
        public final ModConfigSpec.BooleanValue renderMysticGuiOnLeft;
        public final ModConfigSpec.IntValue mysticBranchGlyphSet;
        public final ModConfigSpec.BooleanValue render3dMysticWeaponGlow;
        public final ModConfigSpec.BooleanValue renderHornedHunterOutline;
        public final ModConfigSpec.DoubleValue trickMandrakeAlpha;
        public final ModConfigSpec.BooleanValue allowChatMasking;
        public final ModConfigSpec.BooleanValue allowNameplateMasking;

        public Client(ModConfigSpec.Builder builder) {
            builder.comment("Configuracoes de Cliente e Interface").push("client");
            renderMysticGuiOnLeft = builder.define("renderMysticGuiOnLeft", true);
            mysticBranchGlyphSet = builder.defineInRange("mysticBranchGlyphSet", 0, 0, 2);
            render3dMysticWeaponGlow = builder.define("render3dMysticWeaponGlow", true);
            renderHornedHunterOutline = builder.define("renderHornedHunterOutline", true);
            trickMandrakeAlpha = builder.defineInRange("trickMandrakeAlpha", 0.3, 0.0, 1.0);
            allowChatMasking = builder.define("allowChatMasking", true);
            allowNameplateMasking = builder.define("allowNameplateMasking", true);
            builder.pop();
        }
    }

    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        Pair<Common, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Client, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }
}
