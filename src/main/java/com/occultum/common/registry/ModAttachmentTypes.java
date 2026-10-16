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

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
