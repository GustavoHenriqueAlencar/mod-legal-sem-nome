package com.occultum.client.event;

import com.occultum.OccultumMod;
import com.occultum.client.gui.AltarScreen;
import com.occultum.client.gui.DistilleryScreen;
import com.occultum.client.gui.InfusionHudOverlay;
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
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
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
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "infusion_power_hud"),
                new InfusionHudOverlay());
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(OccultumMod.MOD_ID, "vampire_blood_hud"),
                new com.occultum.client.gui.VampireBloodHudOverlay());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Entidades
        event.registerEntityRenderer(ModEntityTypes.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENT.get(), EntRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TREEFYD.get(), TreefydRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TOAD_FAMILIAR.get(), ToadFamiliarRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.OWL_FAMILIAR.get(), OwlFamiliarRenderer::new);

        // Entidades Sprint 4
        event.registerEntityRenderer(ModEntityTypes.ELLE.get(), Sprint4Renderers.ElleRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LILITH.get(), Sprint4Renderers.LilithRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ATTACK_BAT.get(), Sprint4Renderers.AttackBatRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VAMPIRE_SERVANT.get(), Sprint4Renderers.VampireServantRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.WOLFMAN.get(), Sprint4Renderers.WolfmanRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HORNED_HUNTSMAN.get(), Sprint4Renderers.HornedHuntsmanRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPIRIT.get(), Sprint4Renderers.SpiritRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CORPSE.get(), Sprint4Renderers.CorpseRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NIGHTMARE.get(), Sprint4Renderers.NightmareRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LORD_OF_TORMENT.get(), Sprint4Renderers.LordOfTormentRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.REFLECTION_DEMON.get(), Sprint4Renderers.ReflectionDemonRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FAIREST_FOLLOWER.get(), Sprint4Renderers.FairestFollowerRenderer::new);

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

        // Blocos Sprint 3
        event.registerBlockEntityRenderer(ModBlockEntities.POPPET_SHELF_BE.get(), PoppetShelfRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FETISH_BE.get(), FetishRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_BALL_BE.get(), CrystalBallRenderer::new);

        // Blocos Sprint 4
        event.registerBlockEntityRenderer(ModBlockEntities.BLOOD_CRUCIBLE.get(), Sprint4Renderers.BloodCrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MYSTIC_MIRROR.get(), Sprint4Renderers.MysticMirrorRenderer::new);
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

        // Sprint 3 Models
        event.registerLayerDefinition(PoppetShelfModel.LAYER_LOCATION, PoppetShelfModel::createBodyLayer);
        event.registerLayerDefinition(FetishModel.LAYER_LOCATION, FetishModel::createBodyLayer);
        event.registerLayerDefinition(CrystalBallModel.LAYER_LOCATION, CrystalBallModel::createBodyLayer);
        event.registerLayerDefinition(ToadFamiliarModel.LAYER_LOCATION, ToadFamiliarModel::createBodyLayer);
        event.registerLayerDefinition(OwlFamiliarModel.LAYER_LOCATION, OwlFamiliarModel::createBodyLayer);

        // Sprint 4 Models
        event.registerLayerDefinition(OccultumHumanoidModel.ELLE_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.LILITH_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.VAMPIRE_SERVANT_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.WOLFMAN_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.HORNED_HUNTSMAN_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.CORPSE_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.NIGHTMARE_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.LORD_OF_TORMENT_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.REFLECTION_DEMON_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(OccultumHumanoidModel.FAIREST_FOLLOWER_LAYER, OccultumHumanoidModel::createBodyLayer);
        event.registerLayerDefinition(SpiritModel.LAYER_LOCATION, SpiritModel::createBodyLayer);
        event.registerLayerDefinition(Sprint4BlockModels.BloodCrucibleModel.LAYER_LOCATION, Sprint4BlockModels.BloodCrucibleModel::createBodyLayer);
        event.registerLayerDefinition(Sprint4BlockModels.MysticMirrorModel.LAYER_LOCATION, Sprint4BlockModels.MysticMirrorModel::createBodyLayer);
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
