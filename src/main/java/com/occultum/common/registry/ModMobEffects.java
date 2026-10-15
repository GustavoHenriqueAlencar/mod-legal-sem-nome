package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.effect.OccultumMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, OccultumMod.MOD_ID);

    // 01 - 10
    public static final DeferredHolder<MobEffect, MobEffect> PARALYSIS =
            MOB_EFFECTS.register("paralysis", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x555555, false));
    public static final DeferredHolder<MobEffect, MobEffect> WRAPPED_IN_VINE =
            MOB_EFFECTS.register("wrapped_in_vine", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x228B22, true));
    public static final DeferredHolder<MobEffect, MobEffect> SPIKED =
            MOB_EFFECTS.register("spiked", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x8B0000, true));
    public static final DeferredHolder<MobEffect, MobEffect> SPROUTING =
            MOB_EFFECTS.register("sprouting", () -> new OccultumMobEffect(MobEffectCategory.NEUTRAL, 0x00FF7F, true));
    public static final DeferredHolder<MobEffect, MobEffect> GROTESQUE =
            MOB_EFFECTS.register("grotesque", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x322156, false));
    public static final DeferredHolder<MobEffect, MobEffect> LOVE =
            MOB_EFFECTS.register("love", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xFF69B4, true));
    public static final DeferredHolder<MobEffect, MobEffect> SUN_ALLERGY =
            MOB_EFFECTS.register("sun_allergy", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0xFFD700, false));
    public static final DeferredHolder<MobEffect, MobEffect> CHILLED =
            MOB_EFFECTS.register("chilled", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0xADD8E6, true));
    public static final DeferredHolder<MobEffect, MobEffect> SNOW_TRAIL =
            MOB_EFFECTS.register("snow_trail", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF, true));
    public static final DeferredHolder<MobEffect, MobEffect> FLOATING =
            MOB_EFFECTS.register("floating", () -> new OccultumMobEffect(MobEffectCategory.NEUTRAL, 0xE0FFFF, true));

    // 11 - 20
    public static final DeferredHolder<MobEffect, MobEffect> HELLISH_AURA =
            MOB_EFFECTS.register("hellish_aura", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xFF4500, true));
    public static final DeferredHolder<MobEffect, MobEffect> BREWING_EXPERTISE =
            MOB_EFFECTS.register("brewing_expertise", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x9370DB, true));
    public static final DeferredHolder<MobEffect, MobEffect> DOUBLE_JUMP =
            MOB_EFFECTS.register("double_jump", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x87CEEB, true));
    public static final DeferredHolder<MobEffect, MobEffect> FEATHER_FALL =
            MOB_EFFECTS.register("feather_fall", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xF5F5F5, true));
    public static final DeferredHolder<MobEffect, MobEffect> DARKNESS_ALLERGY =
            MOB_EFFECTS.register("darkness_allergy", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x1C1C1C, false));
    public static final DeferredHolder<MobEffect, MobEffect> REINCARNATE =
            MOB_EFFECTS.register("reincarnate", () -> new OccultumMobEffect(MobEffectCategory.NEUTRAL, 0xDA70D6, true));
    public static final DeferredHolder<MobEffect, MobEffect> INSANITY =
            MOB_EFFECTS.register("insanity", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x4B0082, false));
    public static final DeferredHolder<MobEffect, MobEffect> KEEP_INVENTORY =
            MOB_EFFECTS.register("keep_inventory", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xFFD700, true));
    public static final DeferredHolder<MobEffect, MobEffect> SINKING =
            MOB_EFFECTS.register("sinking", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x00008B, false));
    public static final DeferredHolder<MobEffect, MobEffect> OVERHEATING =
            MOB_EFFECTS.register("overheating", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0xB22222, false));

    // 21 - 30
    public static final DeferredHolder<MobEffect, MobEffect> WAKING_NIGHTMARE =
            MOB_EFFECTS.register("waking_nightmare", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x2F4F4F, false));
    public static final DeferredHolder<MobEffect, MobEffect> QUEASY =
            MOB_EFFECTS.register("queasy", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x556B2F, true));
    public static final DeferredHolder<MobEffect, MobEffect> SWIMMING =
            MOB_EFFECTS.register("swimming", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x20B2AA, true));
    public static final DeferredHolder<MobEffect, MobEffect> RESIZING =
            MOB_EFFECTS.register("resizing", () -> new OccultumMobEffect(MobEffectCategory.NEUTRAL, 0x9932CC, true));
    public static final DeferredHolder<MobEffect, MobEffect> COLORFUL =
            MOB_EFFECTS.register("colorful", () -> new OccultumMobEffect(MobEffectCategory.NEUTRAL, 0xFF1493, true));
    public static final DeferredHolder<MobEffect, MobEffect> ENDER_INHIBITION =
            MOB_EFFECTS.register("ender_inhibition", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x483D8B, false));
    public static final DeferredHolder<MobEffect, MobEffect> ILL_FITTING =
            MOB_EFFECTS.register("ill_fitting", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x8B4513, false));
    public static final DeferredHolder<MobEffect, MobEffect> VOLATILITY =
            MOB_EFFECTS.register("volatility", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0xFF8C00, false));
    public static final DeferredHolder<MobEffect, MobEffect> ENSLAVED =
            MOB_EFFECTS.register("enslaved", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x800080, false));
    public static final DeferredHolder<MobEffect, MobEffect> MORTAL_COIL =
            MOB_EFFECTS.register("mortal_coil", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x000000, false));

    // 31 - 40
    public static final DeferredHolder<MobEffect, MobEffect> ABSORB_MAGIC =
            MOB_EFFECTS.register("absorb_magic", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x00CED1, true));
    public static final DeferredHolder<MobEffect, MobEffect> POISON_WEAPONS =
            MOB_EFFECTS.register("poison_weapons", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x32CD32, true));
    public static final DeferredHolder<MobEffect, MobEffect> REFLECT_PROJECTILES =
            MOB_EFFECTS.register("reflect_projectiles", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x00BFFF, true));
    public static final DeferredHolder<MobEffect, MobEffect> REFLECT_DAMAGE =
            MOB_EFFECTS.register("reflect_damage", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xDC143C, true));
    public static final DeferredHolder<MobEffect, MobEffect> ATTRACT_PROJECTILES =
            MOB_EFFECTS.register("attract_projectiles", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x8A2BE2, true));
    public static final DeferredHolder<MobEffect, MobEffect> REPELL_ATTACKER =
            MOB_EFFECTS.register("repell_attacker", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xF0E68C, true));
    public static final DeferredHolder<MobEffect, MobEffect> STOUT_BELLY =
            MOB_EFFECTS.register("stout_belly", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xD2B48C, true));
    public static final DeferredHolder<MobEffect, MobEffect> FEEL_NO_PAIN =
            MOB_EFFECTS.register("feel_no_pain", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xB0C4DE, false));
    public static final DeferredHolder<MobEffect, MobEffect> GAS_MASK =
            MOB_EFFECTS.register("gas_mask", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x708090, false));
    public static final DeferredHolder<MobEffect, MobEffect> DISEASED =
            MOB_EFFECTS.register("diseased", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x556B2F, false));

    // 41 - 44
    public static final DeferredHolder<MobEffect, MobEffect> MYSTIC_FORTUNE =
            MOB_EFFECTS.register("mystic_fortune", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xFFD700, true));
    public static final DeferredHolder<MobEffect, MobEffect> WORSHIP =
            MOB_EFFECTS.register("worship", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0xBA55D3, true));
    public static final DeferredHolder<MobEffect, MobEffect> KEEP_EFFECTS =
            MOB_EFFECTS.register("keep_effects", () -> new OccultumMobEffect(MobEffectCategory.BENEFICIAL, 0x98FB98, true));
    public static final DeferredHolder<MobEffect, MobEffect> WOLFSBANE =
            MOB_EFFECTS.register("wolfsbane", () -> new OccultumMobEffect(MobEffectCategory.HARMFUL, 0x4B0082, false));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
