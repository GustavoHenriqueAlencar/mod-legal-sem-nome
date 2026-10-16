package com.occultum;

import com.occultum.common.config.OccultumConfig;
import com.occultum.common.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OccultumMod.MOD_ID)
public class OccultumMod {
    public static final String MOD_ID = "occultum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public OccultumMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Inicializando Occultum - Sistema de Magia, Ocultismo e Alquimia.");

        // Registrar Configuracoes
        modContainer.registerConfig(ModConfig.Type.COMMON, OccultumConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, OccultumConfig.CLIENT_SPEC);

        // Registrar Registros Diferidos
        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModDataComponentTypes.register(modEventBus);
        ModAttachmentTypes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
    }
}
