package com.occultum.client.event;

import com.occultum.OccultumMod;
import com.occultum.client.gui.AltarScreen;
import com.occultum.client.model.AltarModel;
import com.occultum.client.model.EntModel;
import com.occultum.client.model.MandrakeModel;
import com.occultum.client.model.TreefydModel;
import com.occultum.client.renderer.*;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = OccultumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ALTAR_MENU.get(), AltarScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Entidades
        event.registerEntityRenderer(ModEntityTypes.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENT.get(), EntRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TREEFYD.get(), TreefydRenderer::new);

        // Blocos
        event.registerBlockEntityRenderer(ModBlockEntities.ALTAR_BE.get(), AltarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRASSPER_BE.get(), GrassperRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AltarModel.LAYER_LOCATION, AltarModel::createBodyLayer);
        event.registerLayerDefinition(MandrakeModel.LAYER_LOCATION, MandrakeModel::createBodyLayer);
        event.registerLayerDefinition(EntModel.LAYER_LOCATION, EntModel::createBodyLayer);
        event.registerLayerDefinition(TreefydModel.LAYER_LOCATION, TreefydModel::createBodyLayer);
    }
}
