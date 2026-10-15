package com.occultum.datagen;

import com.occultum.OccultumMod;
import com.occultum.common.botany.crop.BaseWitchCropBlock;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
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
