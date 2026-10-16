package com.occultum.common.registry;

import com.occultum.OccultumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, OccultumMod.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, OccultumMod.MOD_ID);

    // 4.1 Espírito Fluido (Flowing Spirit): Densidade 500, Viscosidade 2000, Luz 7
    public static final DeferredHolder<FluidType, FluidType> FLOWING_SPIRIT_TYPE =
            FLUID_TYPES.register("flowing_spirit", () -> new FluidType(FluidType.Properties.create()
                    .density(500)
                    .viscosity(2000)
                    .lightLevel(7)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SPIRIT =
            FLUIDS.register("flowing_spirit", () -> new BaseFlowingFluid.Source(flowingSpiritProperties()));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_FLOWING_SPIRIT =
            FLUIDS.register("flowing_flowing_spirit", () -> new BaseFlowingFluid.Flowing(flowingSpiritProperties()));

    private static BaseFlowingFluid.Properties flowingSpiritProperties() {
        return new BaseFlowingFluid.Properties(FLOWING_SPIRIT_TYPE, FLOWING_SPIRIT, FLOWING_FLOWING_SPIRIT)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .block(ModBlocks.FLOWING_SPIRIT_BLOCK)
                .bucket(ModItems.FLOWING_SPIRIT_BUCKET);
    }

    // 4.2 Lágrimas Ocas (Hollow Tears): Densidade 100, Viscosidade 1500
    public static final DeferredHolder<FluidType, FluidType> HOLLOW_TEARS_TYPE =
            FLUID_TYPES.register("hollow_tears", () -> new FluidType(FluidType.Properties.create()
                    .density(100)
                    .viscosity(1500)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, FlowingFluid> HOLLOW_TEARS =
            FLUIDS.register("hollow_tears", () -> new BaseFlowingFluid.Source(hollowTearsProperties()));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_HOLLOW_TEARS =
            FLUIDS.register("flowing_hollow_tears", () -> new BaseFlowingFluid.Flowing(hollowTearsProperties()));

    private static BaseFlowingFluid.Properties hollowTearsProperties() {
        return new BaseFlowingFluid.Properties(HOLLOW_TEARS_TYPE, HOLLOW_TEARS, FLOWING_HOLLOW_TEARS)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .block(ModBlocks.HOLLOW_TEARS_BLOCK)
                .bucket(ModItems.HOLLOW_TEARS_BUCKET);
    }

    // 4.3 Doença Líquida (Disease Liquid): Densidade 50, Viscosidade 3000
    public static final DeferredHolder<FluidType, FluidType> DISEASE_LIQUID_TYPE =
            FLUID_TYPES.register("disease_liquid", () -> new FluidType(FluidType.Properties.create()
                    .density(50)
                    .viscosity(3000)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, FlowingFluid> DISEASE_LIQUID =
            FLUIDS.register("disease_liquid", () -> new BaseFlowingFluid.Source(diseaseLiquidProperties()));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_DISEASE_LIQUID =
            FLUIDS.register("flowing_disease_liquid", () -> new BaseFlowingFluid.Flowing(diseaseLiquidProperties()));

    private static BaseFlowingFluid.Properties diseaseLiquidProperties() {
        return new BaseFlowingFluid.Properties(DISEASE_LIQUID_TYPE, DISEASE_LIQUID, FLOWING_DISEASE_LIQUID)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .block(ModBlocks.DISEASE_LIQUID_BLOCK)
                .bucket(ModItems.DISEASE_LIQUID_BUCKET);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
