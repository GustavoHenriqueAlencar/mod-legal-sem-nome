package com.occultum.common.registry;

import com.mojang.serialization.Codec;
import com.occultum.OccultumMod;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OccultumMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DNA_SIGNATURE =
            DATA_COMPONENT_TYPES.register("dna_signature", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> BOUND_COORDINATES =
            DATA_COMPONENT_TYPES.register("bound_coordinates", () -> DataComponentType.<GlobalPos>builder()
                    .persistent(GlobalPos.CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_BLOOD =
            DATA_COMPONENT_TYPES.register("stored_blood", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CAPTURED_CRITTER =
            DATA_COMPONENT_TYPES.register("captured_critter", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
