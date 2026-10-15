package com.occultum.datagen;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.Set;

public class OccultumBlockLootSubProvider extends BlockLootSubProvider {

    public OccultumBlockLootSubProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.ALTAR_BLOCK.get());
        dropSelf(ModBlocks.CANDELABRA.get());
        dropSelf(ModBlocks.CHALICE.get());
        dropSelf(ModBlocks.CHALICE_FILLED.get());
        dropSelf(ModBlocks.KOBOLDITE_PENTACLE_BLOCK.get());
        dropSelf(ModBlocks.INFINITE_EGG_BLOCK.get());
        dropSelf(ModBlocks.GRASSPER.get());

        dropSelf(ModBlocks.ROWAN_LOG.get());
        dropSelf(ModBlocks.ROWAN_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_ROWAN_LOG.get());
        dropSelf(ModBlocks.STRIPPED_ROWAN_WOOD.get());
        dropSelf(ModBlocks.ROWAN_PLANKS.get());
        dropSelf(ModBlocks.ROWAN_SAPLING.get());

        dropSelf(ModBlocks.ALDER_LOG.get());
        dropSelf(ModBlocks.ALDER_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_ALDER_LOG.get());
        dropSelf(ModBlocks.STRIPPED_ALDER_WOOD.get());
        dropSelf(ModBlocks.ALDER_PLANKS.get());
        dropSelf(ModBlocks.ALDER_SAPLING.get());

        dropSelf(ModBlocks.HAWTHORN_LOG.get());
        dropSelf(ModBlocks.HAWTHORN_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_HAWTHORN_LOG.get());
        dropSelf(ModBlocks.STRIPPED_HAWTHORN_WOOD.get());
        dropSelf(ModBlocks.HAWTHORN_PLANKS.get());
        dropSelf(ModBlocks.HAWTHORN_SAPLING.get());

        add(ModBlocks.ROWAN_LEAVES.get(), block -> createLeavesDrops(block, ModBlocks.ROWAN_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        add(ModBlocks.ALDER_LEAVES.get(), block -> createLeavesDrops(block, ModBlocks.ALDER_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        add(ModBlocks.HAWTHORN_LEAVES.get(), block -> createLeavesDrops(block, ModBlocks.HAWTHORN_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));

        dropSelf(ModBlocks.CRITTER_SNARE.get());
        dropSelf(ModBlocks.VOID_BRAMBLE.get());
        dropSelf(ModBlocks.EMBER_BRAMBLE.get());
        dropSelf(ModBlocks.LEECH_CHEST.get());
        dropSelf(ModBlocks.WISPY_COTTON.get());
        dropSelf(ModBlocks.GLINT_WEED.get());
        dropSelf(ModBlocks.BLOOD_ROSE.get());

        // Drops controlados por lógica específica no bloco (playerWillDestroy / tesoura / maturação)
        add(ModBlocks.SPANISH_MOSS.get(), noDrop());
        add(ModBlocks.EMBER_MOSS.get(), noDrop());
        add(ModBlocks.BELLADONNA_CROP.get(), noDrop());
        add(ModBlocks.MANDRAKE_CROP.get(), noDrop());
        add(ModBlocks.WATER_ARTICHOKE_CROP.get(), noDrop());
        add(ModBlocks.SNOWBELL_CROP.get(), noDrop());
        add(ModBlocks.WORMWOOD_CROP.get(), noDrop());
        add(ModBlocks.WOLFSBANE_CROP.get(), noDrop());
        add(ModBlocks.MINDRAKE_CROP.get(), noDrop());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(h -> (Block) h.get())::iterator;
    }
}
