package com.occultum.datagen;

import com.occultum.OccultumMod;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class OccultumItemTagsProvider extends ItemTagsProvider {

    public OccultumItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, OccultumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.LOGS)
                .add(ModItems.ROWAN_LOG.get())
                .add(ModItems.ROWAN_WOOD.get())
                .add(ModItems.STRIPPED_ROWAN_LOG.get())
                .add(ModItems.STRIPPED_ROWAN_WOOD.get())
                .add(ModItems.ALDER_LOG.get())
                .add(ModItems.ALDER_WOOD.get())
                .add(ModItems.STRIPPED_ALDER_LOG.get())
                .add(ModItems.STRIPPED_ALDER_WOOD.get())
                .add(ModItems.HAWTHORN_LOG.get())
                .add(ModItems.HAWTHORN_WOOD.get())
                .add(ModItems.STRIPPED_HAWTHORN_LOG.get())
                .add(ModItems.STRIPPED_HAWTHORN_WOOD.get());

        tag(ItemTags.LEAVES)
                .add(ModItems.ROWAN_LEAVES.get())
                .add(ModItems.ALDER_LEAVES.get())
                .add(ModItems.HAWTHORN_LEAVES.get());

        tag(ItemTags.SAPLINGS)
                .add(ModItems.ROWAN_SAPLING.get())
                .add(ModItems.ALDER_SAPLING.get())
                .add(ModItems.HAWTHORN_SAPLING.get());

        tag(ItemTags.PLANKS)
                .add(ModItems.ROWAN_PLANKS.get())
                .add(ModItems.ALDER_PLANKS.get())
                .add(ModItems.HAWTHORN_PLANKS.get());
    }
}
