package com.occultum.datagen;

import com.occultum.OccultumMod;
import com.occultum.common.botany.crop.BaseWitchCropBlock;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class OccultumBlockStateProvider extends BlockStateProvider {

    public OccultumBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OccultumMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Altar
        simpleBlock(ModBlocks.ALTAR_BLOCK.get(), models().cubeAll("altar", modLoc("block/altar")));

        // Planks
        simpleBlock(ModBlocks.ROWAN_PLANKS.get(), models().cubeAll("rowan_planks", modLoc("block/rowan_planks")));
        simpleBlock(ModBlocks.ALDER_PLANKS.get(), models().cubeAll("alder_planks", modLoc("block/alder_planks")));
        simpleBlock(ModBlocks.HAWTHORN_PLANKS.get(), models().cubeAll("hawthorn_planks", modLoc("block/hawthorn_planks")));

        // Logs and Wood
        logBlock(ModBlocks.ROWAN_LOG.get());
        axisBlock(ModBlocks.ROWAN_WOOD.get(), modLoc("block/rowan_log"), modLoc("block/rowan_log"));
        logBlock(ModBlocks.STRIPPED_ROWAN_LOG.get());
        axisBlock(ModBlocks.STRIPPED_ROWAN_WOOD.get(), modLoc("block/stripped_rowan_log"), modLoc("block/stripped_rowan_log"));

        logBlock(ModBlocks.ALDER_LOG.get());
        axisBlock(ModBlocks.ALDER_WOOD.get(), modLoc("block/alder_log"), modLoc("block/alder_log"));
        logBlock(ModBlocks.STRIPPED_ALDER_LOG.get());
        axisBlock(ModBlocks.STRIPPED_ALDER_WOOD.get(), modLoc("block/stripped_alder_log"), modLoc("block/stripped_alder_log"));

        logBlock(ModBlocks.HAWTHORN_LOG.get());
        axisBlock(ModBlocks.HAWTHORN_WOOD.get(), modLoc("block/hawthorn_log"), modLoc("block/hawthorn_log"));
        logBlock(ModBlocks.STRIPPED_HAWTHORN_LOG.get());
        axisBlock(ModBlocks.STRIPPED_HAWTHORN_WOOD.get(), modLoc("block/stripped_hawthorn_log"), modLoc("block/stripped_hawthorn_log"));

        // Leaves
        simpleBlock(ModBlocks.ROWAN_LEAVES.get(), models().cubeAll("rowan_leaves", modLoc("block/rowan_leaves")));
        simpleBlock(ModBlocks.ALDER_LEAVES.get(), models().cubeAll("alder_leaves", modLoc("block/alder_leaves")));
        simpleBlock(ModBlocks.HAWTHORN_LEAVES.get(), models().cubeAll("hawthorn_leaves", modLoc("block/hawthorn_leaves")));

        // Saplings & Plants
        crossBlock(ModBlocks.ROWAN_SAPLING.get());
        crossBlock(ModBlocks.ALDER_SAPLING.get());
        crossBlock(ModBlocks.HAWTHORN_SAPLING.get());
        crossBlock(ModBlocks.SPANISH_MOSS.get());
        crossBlock(ModBlocks.GLINT_WEED.get());
        crossBlock(ModBlocks.BLOOD_ROSE.get());
        crossBlock(ModBlocks.WISPY_COTTON.get());

        // Crops (stages 0 to 4)
        makeCrop(ModBlocks.BELLADONNA_CROP.get(), "belladonna");
        makeCrop(ModBlocks.MANDRAKE_CROP.get(), "mandrake");
        makeCrop(ModBlocks.WATER_ARTICHOKE_CROP.get(), "water_artichoke");
        makeCrop(ModBlocks.SNOWBELL_CROP.get(), "snowbell");
        makeCrop(ModBlocks.WORMWOOD_CROP.get(), "wormwood");
        makeCrop(ModBlocks.WOLFSBANE_CROP.get(), "wolfsbane");
        makeCrop(ModBlocks.MINDRAKE_CROP.get(), "mindrake");

        // Artifacts
        simpleBlock(ModBlocks.CANDELABRA.get(), models().cubeAll("candelabra", modLoc("block/candelabra")));
        simpleBlock(ModBlocks.CHALICE.get(), models().cubeAll("chalice", modLoc("block/chalice")));
        simpleBlock(ModBlocks.CHALICE_FILLED.get(), models().cubeAll("chalice_filled", modLoc("block/chalice_filled")));
        simpleBlock(ModBlocks.KOBOLDITE_PENTACLE_BLOCK.get(), models().cubeAll("koboldite_pentacle", modLoc("block/koboldite_pentacle")));
        simpleBlock(ModBlocks.INFINITE_EGG_BLOCK.get(), models().cubeAll("infinite_egg", modLoc("block/infinite_egg")));
        simpleBlock(ModBlocks.GRASSPER.get(), models().cubeAll("grassper", modLoc("block/grassper")));
        simpleBlock(ModBlocks.CRITTER_SNARE.get(), models().cubeAll("critter_snare", modLoc("block/critter_snare")));
        simpleBlock(ModBlocks.VOID_BRAMBLE.get(), models().cubeAll("void_bramble", modLoc("block/void_bramble")));
        simpleBlock(ModBlocks.EMBER_BRAMBLE.get(), models().cubeAll("ember_bramble", modLoc("block/ember_bramble")));
        simpleBlock(ModBlocks.LEECH_CHEST.get(), models().cubeAll("leech_chest", modLoc("block/leech_chest")));
        simpleBlock(ModBlocks.EMBER_MOSS.get(), models().cubeAll("ember_moss", modLoc("block/ember_moss")));

        // Sprint 2 Machines, Chalks, Pit Blocks & Fluids
        simpleBlock(ModBlocks.WITCHES_OVEN.get(), models().cubeAll("witches_oven", modLoc("block/witches_oven")));
        simpleBlock(ModBlocks.FUME_FUNNEL.get(), models().cubeAll("fume_funnel", modLoc("block/fume_funnel")));
        simpleBlock(ModBlocks.FILTERED_FUME_FUNNEL.get(), models().cubeAll("filtered_fume_funnel", modLoc("block/filtered_fume_funnel")));
        simpleBlock(ModBlocks.DISTILLERY.get(), models().cubeAll("distillery", modLoc("block/distillery")));
        simpleBlock(ModBlocks.SPINNING_WHEEL.get(), models().cubeAll("spinning_wheel", modLoc("block/spinning_wheel")));
        simpleBlock(ModBlocks.KETTLE.get(), models().cubeAll("kettle", modLoc("block/kettle")));
        simpleBlock(ModBlocks.WITCHES_CAULDRON.get(), models().cubeAll("witches_cauldron", modLoc("block/witches_cauldron")));
        simpleBlock(ModBlocks.BRAZIER.get(), models().cubeAll("brazier", modLoc("block/brazier")));
        simpleBlock(ModBlocks.PIT_GRASS.get(), models().cubeAll("pit_grass", modLoc("block/pit_grass")));
        simpleBlock(ModBlocks.PIT_DIRT.get(), models().cubeAll("pit_dirt", modLoc("block/pit_dirt")));

        simpleBlock(ModBlocks.RITUAL_CHALK_BLOCK.get(), models().carpet("ritual_chalk_block", modLoc("block/ritual_chalk_block")));
        simpleBlock(ModBlocks.GOLDEN_CHALK_BLOCK.get(), models().carpet("golden_chalk_block", modLoc("block/golden_chalk_block")));
        simpleBlock(ModBlocks.INFERNAL_CHALK_BLOCK.get(), models().carpet("infernal_chalk_block", modLoc("block/infernal_chalk_block")));
        simpleBlock(ModBlocks.OTHERWHERE_CHALK_BLOCK.get(), models().carpet("otherwhere_chalk_block", modLoc("block/otherwhere_chalk_block")));

        simpleBlock(ModBlocks.FLOWING_SPIRIT_BLOCK.get(), models().cubeAll("flowing_spirit_block", modLoc("block/flowing_spirit_block")));
        simpleBlock(ModBlocks.HOLLOW_TEARS_BLOCK.get(), models().cubeAll("hollow_tears_block", modLoc("block/hollow_tears_block")));
        simpleBlock(ModBlocks.DISEASE_LIQUID_BLOCK.get(), models().cubeAll("disease_liquid_block", modLoc("block/disease_liquid_block")));
    }

    private void crossBlock(Block block) {
        String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
        simpleBlock(block, models().cross(name, modLoc("block/" + name)));
    }

    private void makeCrop(Block cropBlock, String modelName) {
        getVariantBuilder(cropBlock).forAllStates(state -> {
            int age = state.getValue(BaseWitchCropBlock.AGE);
            ModelFile model = models().cross(modelName + "_stage" + age, modLoc("block/" + modelName + "_stage" + age));
            return ConfiguredModel.builder().modelFile(model).build();
        });
    }
}
