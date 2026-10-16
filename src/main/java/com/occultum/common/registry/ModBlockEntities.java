package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.altar.AltarBlockEntity;
import com.occultum.common.altar.GrassperBlockEntity;
import com.occultum.common.botany.wild.BloodRoseBlockEntity;
import com.occultum.common.botany.wild.CritterSnareBlockEntity;
import com.occultum.common.botany.wild.LeechChestBlockEntity;
import com.occultum.common.machine.brazier.BrazierBlockEntity;
import com.occultum.common.machine.cauldron.WitchesCauldronBlockEntity;
import com.occultum.common.machine.distillery.DistilleryBlockEntity;
import com.occultum.common.machine.kettle.KettleBlockEntity;
import com.occultum.common.machine.oven.WitchesOvenBlockEntity;
import com.occultum.common.machine.spinningwheel.SpinningWheelBlockEntity;
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

    // Sprint 2 Block Entities
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WitchesOvenBlockEntity>> WITCHES_OVEN =
            BLOCK_ENTITIES.register("witches_oven", () -> BlockEntityType.Builder.of(
                    WitchesOvenBlockEntity::new,
                    ModBlocks.WITCHES_OVEN.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WitchesOvenBlockEntity>> WITCHES_OVEN_BE = WITCHES_OVEN;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistilleryBlockEntity>> DISTILLERY =
            BLOCK_ENTITIES.register("distillery", () -> BlockEntityType.Builder.of(
                    DistilleryBlockEntity::new,
                    ModBlocks.DISTILLERY.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistilleryBlockEntity>> DISTILLERY_BE = DISTILLERY;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpinningWheelBlockEntity>> SPINNING_WHEEL =
            BLOCK_ENTITIES.register("spinning_wheel", () -> BlockEntityType.Builder.of(
                    SpinningWheelBlockEntity::new,
                    ModBlocks.SPINNING_WHEEL.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpinningWheelBlockEntity>> SPINNING_WHEEL_BE = SPINNING_WHEEL;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KettleBlockEntity>> KETTLE =
            BLOCK_ENTITIES.register("kettle", () -> BlockEntityType.Builder.of(
                    KettleBlockEntity::new,
                    ModBlocks.KETTLE.get()
            ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KettleBlockEntity>> KETTLE_BE = KETTLE;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WitchesCauldronBlockEntity>> WITCHES_CAULDRON =
            BLOCK_ENTITIES.register("witches_cauldron", () -> BlockEntityType.Builder.of(
                    WitchesCauldronBlockEntity::new,
                    ModBlocks.WITCHES_CAULDRON.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrazierBlockEntity>> BRAZIER =
            BLOCK_ENTITIES.register("brazier", () -> BlockEntityType.Builder.of(
                    BrazierBlockEntity::new,
                    ModBlocks.BRAZIER.get()
            ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
