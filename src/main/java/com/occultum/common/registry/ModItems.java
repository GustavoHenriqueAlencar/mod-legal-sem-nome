package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.botany.crop.TreefydSeedItem;
import com.occultum.common.botany.powder.MutandisExtremisItem;
import com.occultum.common.botany.powder.MutandisItem;
import com.occultum.common.botany.powder.MutatingSprigItem;
import com.occultum.common.item.EarmuffsItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OccultumMod.MOD_ID);

    // Altar & Artefatos (Itens de Bloco)
    public static final DeferredItem<BlockItem> ALTAR =
            ITEMS.registerSimpleBlockItem("altar", ModBlocks.ALTAR_BLOCK);

    public static final DeferredItem<BlockItem> CANDELABRA =
            ITEMS.registerSimpleBlockItem("candelabra", ModBlocks.CANDELABRA);

    public static final DeferredItem<BlockItem> CHALICE =
            ITEMS.registerSimpleBlockItem("chalice", ModBlocks.CHALICE);

    public static final DeferredItem<BlockItem> CHALICE_FILLED =
            ITEMS.registerSimpleBlockItem("chalice_filled", ModBlocks.CHALICE_FILLED);

    public static final DeferredItem<BlockItem> KOBOLDITE_PENTACLE =
            ITEMS.registerSimpleBlockItem("koboldite_pentacle", ModBlocks.KOBOLDITE_PENTACLE_BLOCK);

    public static final DeferredItem<BlockItem> INFINITE_EGG =
            ITEMS.registerSimpleBlockItem("infinite_egg", ModBlocks.INFINITE_EGG_BLOCK);

    public static final DeferredItem<BlockItem> GRASSPER =
            ITEMS.registerSimpleBlockItem("grassper", ModBlocks.GRASSPER);

    // Árvore 1: Sorveira (Rowan)
    public static final DeferredItem<BlockItem> ROWAN_LOG =
            ITEMS.registerSimpleBlockItem("rowan_log", ModBlocks.ROWAN_LOG);

    public static final DeferredItem<BlockItem> ROWAN_WOOD =
            ITEMS.registerSimpleBlockItem("rowan_wood", ModBlocks.ROWAN_WOOD);

    public static final DeferredItem<BlockItem> STRIPPED_ROWAN_LOG =
            ITEMS.registerSimpleBlockItem("stripped_rowan_log", ModBlocks.STRIPPED_ROWAN_LOG);

    public static final DeferredItem<BlockItem> STRIPPED_ROWAN_WOOD =
            ITEMS.registerSimpleBlockItem("stripped_rowan_wood", ModBlocks.STRIPPED_ROWAN_WOOD);

    public static final DeferredItem<BlockItem> ROWAN_PLANKS =
            ITEMS.registerSimpleBlockItem("rowan_planks", ModBlocks.ROWAN_PLANKS);

    public static final DeferredItem<BlockItem> ROWAN_LEAVES =
            ITEMS.registerSimpleBlockItem("rowan_leaves", ModBlocks.ROWAN_LEAVES);

    public static final DeferredItem<BlockItem> ROWAN_SAPLING =
            ITEMS.registerSimpleBlockItem("rowan_sapling", ModBlocks.ROWAN_SAPLING);

    // Bagas de Sorveira (Alimento místico: 1 ponto de fome, saturação alta 6.0)
    public static final DeferredItem<Item> ROWAN_BERRIES =
            ITEMS.register("rowan_berries", () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationModifier(6.0F).build()
            )));

    // Árvore 2: Amieiro (Alder)
    public static final DeferredItem<BlockItem> ALDER_LOG =
            ITEMS.registerSimpleBlockItem("alder_log", ModBlocks.ALDER_LOG);

    public static final DeferredItem<BlockItem> ALDER_WOOD =
            ITEMS.registerSimpleBlockItem("alder_wood", ModBlocks.ALDER_WOOD);

    public static final DeferredItem<BlockItem> STRIPPED_ALDER_LOG =
            ITEMS.registerSimpleBlockItem("stripped_alder_log", ModBlocks.STRIPPED_ALDER_LOG);

    public static final DeferredItem<BlockItem> STRIPPED_ALDER_WOOD =
            ITEMS.registerSimpleBlockItem("stripped_alder_wood", ModBlocks.STRIPPED_ALDER_WOOD);

    public static final DeferredItem<BlockItem> ALDER_PLANKS =
            ITEMS.registerSimpleBlockItem("alder_planks", ModBlocks.ALDER_PLANKS);

    public static final DeferredItem<BlockItem> ALDER_LEAVES =
            ITEMS.registerSimpleBlockItem("alder_leaves", ModBlocks.ALDER_LEAVES);

    public static final DeferredItem<BlockItem> ALDER_SAPLING =
            ITEMS.registerSimpleBlockItem("alder_sapling", ModBlocks.ALDER_SAPLING);

    // Árvore 3: Pilriteiro / Espinheiro (Hawthorn)
    public static final DeferredItem<BlockItem> HAWTHORN_LOG =
            ITEMS.registerSimpleBlockItem("hawthorn_log", ModBlocks.HAWTHORN_LOG);

    public static final DeferredItem<BlockItem> HAWTHORN_WOOD =
            ITEMS.registerSimpleBlockItem("hawthorn_wood", ModBlocks.HAWTHORN_WOOD);

    public static final DeferredItem<BlockItem> STRIPPED_HAWTHORN_LOG =
            ITEMS.registerSimpleBlockItem("stripped_hawthorn_log", ModBlocks.STRIPPED_HAWTHORN_LOG);

    public static final DeferredItem<BlockItem> STRIPPED_HAWTHORN_WOOD =
            ITEMS.registerSimpleBlockItem("stripped_hawthorn_wood", ModBlocks.STRIPPED_HAWTHORN_WOOD);

    public static final DeferredItem<BlockItem> HAWTHORN_PLANKS =
            ITEMS.registerSimpleBlockItem("hawthorn_planks", ModBlocks.HAWTHORN_PLANKS);

    public static final DeferredItem<BlockItem> HAWTHORN_LEAVES =
            ITEMS.registerSimpleBlockItem("hawthorn_leaves", ModBlocks.HAWTHORN_LEAVES);

    public static final DeferredItem<BlockItem> HAWTHORN_SAPLING =
            ITEMS.registerSimpleBlockItem("hawthorn_sapling", ModBlocks.HAWTHORN_SAPLING);

    // Sementes dos Cultivos Místicos
    public static final DeferredItem<ItemNameBlockItem> BELLADONNA_SEEDS =
            ITEMS.register("belladonna_seeds", () -> new ItemNameBlockItem(ModBlocks.BELLADONNA_CROP.get(), new Item.Properties()));

    public static final DeferredItem<ItemNameBlockItem> MANDRAKE_SEEDS =
            ITEMS.register("mandrake_seeds", () -> new ItemNameBlockItem(ModBlocks.MANDRAKE_CROP.get(), new Item.Properties()));

    public static final DeferredItem<ItemNameBlockItem> WATER_ARTICHOKE_SEEDS =
            ITEMS.register("water_artichoke_seeds", () -> new ItemNameBlockItem(ModBlocks.WATER_ARTICHOKE_CROP.get(), new Item.Properties()));

    public static final DeferredItem<ItemNameBlockItem> SNOWBELL_SEEDS =
            ITEMS.register("snowbell_seeds", () -> new ItemNameBlockItem(ModBlocks.SNOWBELL_CROP.get(), new Item.Properties()));

    public static final DeferredItem<ItemNameBlockItem> WORMWOOD_SEEDS =
            ITEMS.register("wormwood_seeds", () -> new ItemNameBlockItem(ModBlocks.WORMWOOD_CROP.get(), new Item.Properties()));

    public static final DeferredItem<ItemNameBlockItem> WOLFSBANE_SEEDS =
            ITEMS.register("wolfsbane_seeds", () -> new ItemNameBlockItem(ModBlocks.WOLFSBANE_CROP.get(), new Item.Properties()));

    public static final DeferredItem<ItemNameBlockItem> MINDRAKE_SEEDS =
            ITEMS.register("mindrake_seeds", () -> new ItemNameBlockItem(ModBlocks.MINDRAKE_CROP.get(), new Item.Properties()));

    public static final DeferredItem<TreefydSeedItem> TREEFYD_SEED =
            ITEMS.register("treefyd_seed", () -> new TreefydSeedItem(new Item.Properties()));

    // Produtos de Colheita
    public static final DeferredItem<Item> BELLADONNA_FLOWER =
            ITEMS.registerSimpleItem("belladonna_flower", new Item.Properties());

    public static final DeferredItem<Item> MANDRAKE_ROOT =
            ITEMS.registerSimpleItem("mandrake_root", new Item.Properties());

    public static final DeferredItem<Item> WATER_ARTICHOKE =
            ITEMS.registerSimpleItem("water_artichoke", new Item.Properties());

    public static final DeferredItem<Item> SNOWBELL =
            ITEMS.registerSimpleItem("snowbell", new Item.Properties());

    public static final DeferredItem<Item> ICY_NEEDLE =
            ITEMS.registerSimpleItem("icy_needle", new Item.Properties());

    public static final DeferredItem<Item> WORMWOOD_LEAF =
            ITEMS.registerSimpleItem("wormwood_leaf", new Item.Properties());

    public static final DeferredItem<Item> WOLFSBANE_FLOWER =
            ITEMS.registerSimpleItem("wolfsbane_flower", new Item.Properties());

    public static final DeferredItem<Item> MINDRAKE_ROOT =
            ITEMS.registerSimpleItem("mindrake_root", new Item.Properties());

    public static final DeferredItem<Item> ENT_BRANCH =
            ITEMS.registerSimpleItem("ent_branch", new Item.Properties());

    // Vegetação Selvagem (Itens de Bloco)
    public static final DeferredItem<BlockItem> SPANISH_MOSS =
            ITEMS.registerSimpleBlockItem("spanish_moss", ModBlocks.SPANISH_MOSS);

    public static final DeferredItem<BlockItem> EMBER_MOSS =
            ITEMS.registerSimpleBlockItem("ember_moss", ModBlocks.EMBER_MOSS);

    public static final DeferredItem<BlockItem> GLINT_WEED =
            ITEMS.registerSimpleBlockItem("glint_weed", ModBlocks.GLINT_WEED);

    public static final DeferredItem<BlockItem> BLOOD_ROSE =
            ITEMS.registerSimpleBlockItem("blood_rose", ModBlocks.BLOOD_ROSE);

    public static final DeferredItem<BlockItem> CRITTER_SNARE =
            ITEMS.registerSimpleBlockItem("critter_snare", ModBlocks.CRITTER_SNARE);

    public static final DeferredItem<BlockItem> VOID_BRAMBLE =
            ITEMS.registerSimpleBlockItem("void_bramble", ModBlocks.VOID_BRAMBLE);

    public static final DeferredItem<BlockItem> EMBER_BRAMBLE =
            ITEMS.registerSimpleBlockItem("ember_bramble", ModBlocks.EMBER_BRAMBLE);

    public static final DeferredItem<BlockItem> LEECH_CHEST =
            ITEMS.registerSimpleBlockItem("leech_chest", ModBlocks.LEECH_CHEST);

    public static final DeferredItem<BlockItem> WISPY_COTTON =
            ITEMS.registerSimpleBlockItem("wispy_cotton", ModBlocks.WISPY_COTTON);

    // Pós, Ferramentas e Relíquias Místicas
    public static final DeferredItem<MutandisItem> MUTANDIS =
            ITEMS.register("mutandis", () -> new MutandisItem(new Item.Properties()));

    public static final DeferredItem<MutandisExtremisItem> MUTANDIS_EXTREMIS =
            ITEMS.register("mutandis_extremis", () -> new MutandisExtremisItem(new Item.Properties()));

    public static final DeferredItem<MutatingSprigItem> MUTATING_SPRIG =
            ITEMS.register("mutating_sprig", () -> new MutatingSprigItem(new Item.Properties()));

    public static final DeferredItem<SwordItem> ATHAME =
            ITEMS.register("athame", () -> new SwordItem(Tiers.GOLD, new Item.Properties().attributes(
                    SwordItem.createAttributes(Tiers.GOLD, 3, -2.4F)
            )));

    public static final DeferredItem<Item> MYSTIC_BRANCH =
            ITEMS.registerSimpleItem("mystic_branch", new Item.Properties().stacksTo(1));

    public static final DeferredItem<EarmuffsItem> EARMUFFS =
            ITEMS.register("earmuffs", () -> new EarmuffsItem(new Item.Properties().durability(120)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
