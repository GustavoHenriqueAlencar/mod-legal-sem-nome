package com.occultum.datagen;

import com.occultum.OccultumMod;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class OccultumBlockTagsProvider extends BlockTagsProvider {

    public OccultumBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OccultumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ALTAR_BLOCK.get())
                .add(ModBlocks.CANDELABRA.get())
                .add(ModBlocks.CHALICE.get())
                .add(ModBlocks.CHALICE_FILLED.get())
                .add(ModBlocks.KOBOLDITE_PENTACLE_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.ROWAN_LOG.get())
                .add(ModBlocks.ROWAN_WOOD.get())
                .add(ModBlocks.STRIPPED_ROWAN_LOG.get())
                .add(ModBlocks.STRIPPED_ROWAN_WOOD.get())
                .add(ModBlocks.ROWAN_PLANKS.get())
                .add(ModBlocks.ALDER_LOG.get())
                .add(ModBlocks.ALDER_WOOD.get())
                .add(ModBlocks.STRIPPED_ALDER_LOG.get())
                .add(ModBlocks.STRIPPED_ALDER_WOOD.get())
                .add(ModBlocks.ALDER_PLANKS.get())
                .add(ModBlocks.HAWTHORN_LOG.get())
                .add(ModBlocks.HAWTHORN_WOOD.get())
                .add(ModBlocks.STRIPPED_HAWTHORN_LOG.get())
                .add(ModBlocks.STRIPPED_HAWTHORN_WOOD.get())
                .add(ModBlocks.HAWTHORN_PLANKS.get())
                .add(ModBlocks.LEECH_CHEST.get());

        tag(BlockTags.LOGS)
                .add(ModBlocks.ROWAN_LOG.get())
                .add(ModBlocks.ROWAN_WOOD.get())
                .add(ModBlocks.STRIPPED_ROWAN_LOG.get())
                .add(ModBlocks.STRIPPED_ROWAN_WOOD.get())
                .add(ModBlocks.ALDER_LOG.get())
                .add(ModBlocks.ALDER_WOOD.get())
                .add(ModBlocks.STRIPPED_ALDER_LOG.get())
                .add(ModBlocks.STRIPPED_ALDER_WOOD.get())
                .add(ModBlocks.HAWTHORN_LOG.get())
                .add(ModBlocks.HAWTHORN_WOOD.get())
                .add(ModBlocks.STRIPPED_HAWTHORN_LOG.get())
                .add(ModBlocks.STRIPPED_HAWTHORN_WOOD.get());

        tag(BlockTags.LEAVES)
                .add(ModBlocks.ROWAN_LEAVES.get())
                .add(ModBlocks.ALDER_LEAVES.get())
                .add(ModBlocks.HAWTHORN_LEAVES.get());

        tag(BlockTags.SAPLINGS)
                .add(ModBlocks.ROWAN_SAPLING.get())
                .add(ModBlocks.ALDER_SAPLING.get())
                .add(ModBlocks.HAWTHORN_SAPLING.get());

        tag(BlockTags.PLANKS)
                .add(ModBlocks.ROWAN_PLANKS.get())
                .add(ModBlocks.ALDER_PLANKS.get())
                .add(ModBlocks.HAWTHORN_PLANKS.get());

        tag(BlockTags.CROPS)
                .add(ModBlocks.BELLADONNA_CROP.get())
                .add(ModBlocks.MANDRAKE_CROP.get())
                .add(ModBlocks.WATER_ARTICHOKE_CROP.get())
                .add(ModBlocks.SNOWBELL_CROP.get())
                .add(ModBlocks.WORMWOOD_CROP.get())
                .add(ModBlocks.WOLFSBANE_CROP.get())
                .add(ModBlocks.MINDRAKE_CROP.get());
    }
}
