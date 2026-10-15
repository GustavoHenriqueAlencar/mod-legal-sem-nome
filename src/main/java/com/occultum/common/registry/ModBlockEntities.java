package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.altar.AltarBlockEntity;
import com.occultum.common.altar.GrassperBlockEntity;
import com.occultum.common.botany.wild.BloodRoseBlockEntity;
import com.occultum.common.botany.wild.CritterSnareBlockEntity;
import com.occultum.common.botany.wild.LeechChestBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OccultumMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR_BE =
            BLOCK_ENTITIES.register("altar", () -> BlockEntityType.Builder.of(
                    AltarBlockEntity::new,
                    ModBlocks.ALTAR_BLOCK.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrassperBlockEntity>> GRASSPER_BE =
            BLOCK_ENTITIES.register("grassper", () -> BlockEntityType.Builder.of(
                    GrassperBlockEntity::new,
                    ModBlocks.GRASSPER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodRoseBlockEntity>> BLOOD_ROSE_BE =
            BLOCK_ENTITIES.register("blood_rose", () -> BlockEntityType.Builder.of(
                    BloodRoseBlockEntity::new,
                    ModBlocks.BLOOD_ROSE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CritterSnareBlockEntity>> CRITTER_SNARE_BE =
            BLOCK_ENTITIES.register("critter_snare", () -> BlockEntityType.Builder.of(
                    CritterSnareBlockEntity::new,
                    ModBlocks.CRITTER_SNARE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LeechChestBlockEntity>> LEECH_CHEST_BE =
            BLOCK_ENTITIES.register("leech_chest", () -> BlockEntityType.Builder.of(
                    LeechChestBlockEntity::new,
                    ModBlocks.LEECH_CHEST.get()
            ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
