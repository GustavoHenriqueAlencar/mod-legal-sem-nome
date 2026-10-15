package com.occultum.datagen;

import com.occultum.OccultumMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = OccultumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OccultumDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Providers do Lado do Cliente
        OccultumBlockStateProvider blockStateProvider = new OccultumBlockStateProvider(packOutput, existingFileHelper);
        generator.addProvider(event.includeClient(), blockStateProvider);
        generator.addProvider(event.includeClient(), new OccultumItemModelProvider(packOutput, existingFileHelper));

        // Providers do Lado do Servidor
        OccultumBlockTagsProvider blockTags = new OccultumBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new OccultumItemTagsProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), OccultumLootTableProvider.create(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new OccultumRecipeProvider(packOutput, lookupProvider));
    }
}
