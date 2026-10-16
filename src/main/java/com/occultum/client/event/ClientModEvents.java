package com.occultum.client.event;

import com.occultum.OccultumMod;
import com.occultum.client.gui.AltarScreen;
import com.occultum.client.gui.DistilleryScreen;
import com.occultum.client.gui.SpinningWheelScreen;
import com.occultum.client.gui.WitchesOvenScreen;
import com.occultum.client.model.*;
import com.occultum.client.renderer.*;
import com.occultum.common.machine.cauldron.CauldronCustomPotionItem;
import com.occultum.common.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = OccultumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ALTAR_MENU.get(), AltarScreen::new);
        event.register(ModMenus.WITCHES_OVEN.get(), WitchesOvenScreen::new);
        event.register(ModMenus.DISTILLERY.get(), DistilleryScreen::new);
        event.register(ModMenus.SPINNING_WHEEL.get(), SpinningWheelScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Entidades
        event.registerEntityRenderer(ModEntityTypes.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENT.get(), EntRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TREEFYD.get(), TreefydRenderer::new);

        // Blocos Sprint 1
        event.registerBlockEntityRenderer(ModBlockEntities.ALTAR_BE.get(), AltarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRASSPER_BE.get(), GrassperRenderer::new);

        // Blocos Sprint 2
        event.registerBlockEntityRenderer(ModBlockEntities.WITCHES_OVEN.get(), WitchesOvenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DISTILLERY.get(), DistilleryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SPINNING_WHEEL.get(), SpinningWheelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.KETTLE.get(), KettleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WITCHES_CAULDRON.get(), WitchesCauldronRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BRAZIER.get(), BrazierRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AltarModel.LAYER_LOCATION, AltarModel::createBodyLayer);
        event.registerLayerDefinition(MandrakeModel.LAYER_LOCATION, MandrakeModel::createBodyLayer);
        event.registerLayerDefinition(EntModel.LAYER_LOCATION, EntModel::createBodyLayer);
        event.registerLayerDefinition(TreefydModel.LAYER_LOCATION, TreefydModel::createBodyLayer);

        // Sprint 2 Machine Models
        event.registerLayerDefinition(WitchesOvenModel.LAYER_LOCATION, WitchesOvenModel::createBodyLayer);
        event.registerLayerDefinition(DistilleryModel.LAYER_LOCATION, DistilleryModel::createBodyLayer);
        event.registerLayerDefinition(SpinningWheelModel.LAYER_LOCATION, SpinningWheelModel::createBodyLayer);
        event.registerLayerDefinition(KettleModel.LAYER_LOCATION, KettleModel::createBodyLayer);
        event.registerLayerDefinition(WitchesCauldronModel.LAYER_LOCATION, WitchesCauldronModel::createBodyLayer);
        event.registerLayerDefinition(BrazierModel.LAYER_LOCATION, BrazierModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> 0xFF000000 | CauldronCustomPotionItem.getData(stack).colorRgb(),
                ModItems.CUSTOM_POTION.get());
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xCC70E8FF;
            }
        }, ModFluids.FLOWING_SPIRIT_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xDD3870B8;
            }
        }, ModFluids.HOLLOW_TEARS_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xEE586828;
            }
        }, ModFluids.DISEASE_LIQUID_TYPE.get());
    }
}
