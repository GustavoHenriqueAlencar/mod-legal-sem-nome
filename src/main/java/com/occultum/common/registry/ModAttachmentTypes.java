package com.occultum.common.registry;

import com.mojang.serialization.Codec;
import com.occultum.OccultumMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, OccultumMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> VAMPIRE_LEVEL =
            ATTACHMENT_TYPES.register("vampire_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> WEREWOLF_LEVEL =
            ATTACHMENT_TYPES.register("werewolf_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> BLOOD_LEVEL =
            ATTACHMENT_TYPES.register("blood_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> INFUSION_POWER =
            ATTACHMENT_TYPES.register("infusion_power", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> COVEN_LEVEL =
            ATTACHMENT_TYPES.register("coven_level", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> TRANSMUTATION_DISGUISE =
            ATTACHMENT_TYPES.register("transmutation_disguise", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> FAMILIAR_TYPE =
            ATTACHMENT_TYPES.register("familiar_type", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> BOTTLING_XP =
            ATTACHMENT_TYPES.register("bottling_xp", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build());

    /** Tipo de infusão ativa: "", "light", "overworld", "otherwhere", "nether" ou "grave". */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> INFUSION_TYPE =
            ATTACHMENT_TYPES.register("infusion_type", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Teto da barra de poder espiritual de infusão (200 a 400). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> INFUSION_MAX_POWER =
            ATTACHMENT_TYPES.register("infusion_max_power", () -> AttachmentType.builder(() -> 200)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    /** ID da profecia ativa da Bola de Cristal (Seção 2.3 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> ACTIVE_PROPHECY =
            ATTACHMENT_TYPES.register("active_prophecy", () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .copyOnDeath()
                    .build());

    /** Timer regressivo de concretização forçada da profecia (9.600 a 36.000 ticks). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> PROPHECY_TIMER =
            ATTACHMENT_TYPES.register("prophecy_timer", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
