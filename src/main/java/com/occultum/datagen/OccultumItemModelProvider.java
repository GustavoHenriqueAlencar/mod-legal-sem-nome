package com.occultum.datagen;

import com.occultum.OccultumMod;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class OccultumItemModelProvider extends ItemModelProvider {

    public OccultumItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OccultumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Bloco Itens Simples
        withExistingParent("altar", modLoc("block/altar"));
        withExistingParent("candelabra", modLoc("block/candelabra"));
        withExistingParent("chalice", modLoc("block/chalice"));
        withExistingParent("chalice_filled", modLoc("block/chalice_filled"));
        withExistingParent("koboldite_pentacle", modLoc("block/koboldite_pentacle"));
        withExistingParent("infinite_egg", modLoc("block/infinite_egg"));
        withExistingParent("grassper", modLoc("block/grassper"));
        withExistingParent("critter_snare", modLoc("block/critter_snare"));
        withExistingParent("void_bramble", modLoc("block/void_bramble"));
        withExistingParent("ember_bramble", modLoc("block/ember_bramble"));
        withExistingParent("leech_chest", modLoc("block/leech_chest"));
        withExistingParent("ember_moss", modLoc("block/ember_moss"));

        withExistingParent("rowan_log", modLoc("block/rowan_log"));
        withExistingParent("rowan_wood", modLoc("block/rowan_wood"));
        withExistingParent("stripped_rowan_log", modLoc("block/stripped_rowan_log"));
        withExistingParent("stripped_rowan_wood", modLoc("block/stripped_rowan_wood"));
        withExistingParent("rowan_planks", modLoc("block/rowan_planks"));
        withExistingParent("rowan_leaves", modLoc("block/rowan_leaves"));

        withExistingParent("alder_log", modLoc("block/alder_log"));
        withExistingParent("alder_wood", modLoc("block/alder_wood"));
        withExistingParent("stripped_alder_log", modLoc("block/stripped_alder_log"));
        withExistingParent("stripped_alder_wood", modLoc("block/stripped_alder_wood"));
        withExistingParent("alder_planks", modLoc("block/alder_planks"));
        withExistingParent("alder_leaves", modLoc("block/alder_leaves"));

        withExistingParent("hawthorn_log", modLoc("block/hawthorn_log"));
        withExistingParent("hawthorn_wood", modLoc("block/hawthorn_wood"));
        withExistingParent("stripped_hawthorn_log", modLoc("block/stripped_hawthorn_log"));
        withExistingParent("stripped_hawthorn_wood", modLoc("block/stripped_hawthorn_wood"));
        withExistingParent("hawthorn_planks", modLoc("block/hawthorn_planks"));
        withExistingParent("hawthorn_leaves", modLoc("block/hawthorn_leaves"));

        // Mudas e Plantas 2D
        itemFromBlockTexture(ModItems.ROWAN_SAPLING.get());
        itemFromBlockTexture(ModItems.ALDER_SAPLING.get());
        itemFromBlockTexture(ModItems.HAWTHORN_SAPLING.get());
        itemFromBlockTexture(ModItems.SPANISH_MOSS.get());
        itemFromBlockTexture(ModItems.GLINT_WEED.get());
        itemFromBlockTexture(ModItems.BLOOD_ROSE.get());
        itemFromBlockTexture(ModItems.WISPY_COTTON.get());

        // Sementes
        basicItem(ModItems.BELLADONNA_SEEDS.get());
        basicItem(ModItems.MANDRAKE_SEEDS.get());
        basicItem(ModItems.WATER_ARTICHOKE_SEEDS.get());
        basicItem(ModItems.SNOWBELL_SEEDS.get());
        basicItem(ModItems.WORMWOOD_SEEDS.get());
        basicItem(ModItems.WOLFSBANE_SEEDS.get());
        basicItem(ModItems.MINDRAKE_SEEDS.get());
        basicItem(ModItems.TREEFYD_SEED.get());

        // Produtos
        basicItem(ModItems.BELLADONNA_FLOWER.get());
        basicItem(ModItems.MANDRAKE_ROOT.get());
        basicItem(ModItems.WATER_ARTICHOKE.get());
        basicItem(ModItems.SNOWBELL.get());
        basicItem(ModItems.ICY_NEEDLE.get());
        basicItem(ModItems.WORMWOOD_LEAF.get());
        basicItem(ModItems.WOLFSBANE_FLOWER.get());
        basicItem(ModItems.MINDRAKE_ROOT.get());
        basicItem(ModItems.ROWAN_BERRIES.get());
        basicItem(ModItems.ENT_BRANCH.get());

        // Pós, Ferramentas e Relíquias
        basicItem(ModItems.MUTANDIS.get());
        basicItem(ModItems.MUTANDIS_EXTREMIS.get());
        basicItem(ModItems.MUTATING_SPRIG.get());
        handheldItem(ModItems.ATHAME.get());
        handheldItem(ModItems.MYSTIC_BRANCH.get());
        basicItem(ModItems.EARMUFFS.get());
    }

    private void itemFromBlockTexture(Item item) {
        String name = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + name));
    }
}
