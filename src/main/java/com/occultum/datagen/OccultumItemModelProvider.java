package com.occultum.datagen;

import com.occultum.OccultumMod;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class OccultumItemModelProvider extends ItemModelProvider {

    private static final Set<String> FLAT_BLOCK_ITEMS = Set.of(
            "rowan_sapling", "alder_sapling", "hawthorn_sapling",
            "spanish_moss", "glint_weed", "blood_rose", "wispy_cotton"
    );

    private static final Set<String> CHALK_ITEMS = Set.of(
            "ritual_chalk", "golden_chalk", "infernal_chalk", "otherwhere_chalk"
    );

    public OccultumItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OccultumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            String name = BuiltInRegistries.ITEM.getKey(item).getPath();

            if (FLAT_BLOCK_ITEMS.contains(name)) {
                itemFromBlockTexture(item);
            } else if (CHALK_ITEMS.contains(name)) {
                basicItem(item);
            } else if (item instanceof net.neoforged.neoforge.common.DeferredSpawnEggItem) {
                withExistingParent(name, mcLoc("item/template_spawn_egg"));
            } else if (item instanceof BlockItem && !(item instanceof ItemNameBlockItem)) {
                withExistingParent(name, modLoc("block/" + name));
            } else if (item instanceof SwordItem || item == ModItems.MYSTIC_BRANCH.get()) {
                handheldItem(item);
            } else {
                basicItem(item);
            }
        }
    }

    private void itemFromBlockTexture(Item item) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + name));
    }
}
