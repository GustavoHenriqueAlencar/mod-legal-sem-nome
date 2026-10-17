package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.botany.crop.TreefydSeedItem;
import com.occultum.common.botany.powder.MutandisExtremisItem;
import com.occultum.common.botany.powder.MutandisItem;
import com.occultum.common.botany.powder.MutatingSprigItem;
import com.occultum.common.divination.NecroStoneItem;
import com.occultum.common.infusion.MysticBranchItem;
import com.occultum.common.item.EarmuffsItem;
import com.occultum.common.item.EnchantedBroomItem;
import com.occultum.common.machine.cauldron.AnointingPasteItem;
import com.occultum.common.machine.cauldron.CauldronCustomPotionItem;
import com.occultum.common.machine.kettle.KettleBrewItem;
import com.occultum.common.poppet.PoppetItem;
import com.occultum.common.poppet.ShelfCompassItem;
import com.occultum.common.poppet.TaglockKitItem;
import com.occultum.common.ritual.CircleTalismanItem;
import com.occultum.common.ritual.SeerStoneItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
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
    public static final DeferredItem<Item> WATER_ARTICHOKE_GLOBE = WATER_ARTICHOKE;

    public static final DeferredItem<Item> SNOWBELL =
            ITEMS.registerSimpleItem("snowbell", new Item.Properties());

    public static final DeferredItem<com.occultum.common.spirit.IcyNeedleItem> ICY_NEEDLE =
            ITEMS.register("icy_needle", () -> new com.occultum.common.spirit.IcyNeedleItem(new Item.Properties()));

    public static final DeferredItem<Item> WORMWOOD_LEAF =
            ITEMS.registerSimpleItem("wormwood_leaf", new Item.Properties());
    public static final DeferredItem<Item> WORMWOOD = WORMWOOD_LEAF;

    public static final DeferredItem<Item> WOLFSBANE_FLOWER =
            ITEMS.registerSimpleItem("wolfsbane_flower", new Item.Properties());
    public static final DeferredItem<Item> WOLFSBANE = WOLFSBANE_FLOWER;

    public static final DeferredItem<Item> MINDRAKE_ROOT =
            ITEMS.registerSimpleItem("mindrake_root", new Item.Properties());
    public static final DeferredItem<Item> MINDRAKE_BULB = MINDRAKE_ROOT;

    public static final DeferredItem<Item> ENT_BRANCH =
            ITEMS.registerSimpleItem("ent_branch", new Item.Properties());
    public static final DeferredItem<Item> ENT_TWIG = ENT_BRANCH;

    // Vegetação Selvagem (Itens de Bloco)
    public static final DeferredItem<BlockItem> SPANISH_MOSS =
            ITEMS.registerSimpleBlockItem("spanish_moss", ModBlocks.SPANISH_MOSS);
    public static final DeferredItem<BlockItem> SPANISH_MOSS_ITEM = SPANISH_MOSS;

    public static final DeferredItem<BlockItem> EMBER_MOSS =
            ITEMS.registerSimpleBlockItem("ember_moss", ModBlocks.EMBER_MOSS);
    public static final DeferredItem<BlockItem> EMBER_MOSS_ITEM = EMBER_MOSS;

    public static final DeferredItem<BlockItem> GLINT_WEED =
            ITEMS.registerSimpleBlockItem("glint_weed", ModBlocks.GLINT_WEED);
    public static final DeferredItem<BlockItem> GLINT_WEED_ITEM = GLINT_WEED;

    public static final DeferredItem<BlockItem> BLOOD_ROSE =
            ITEMS.registerSimpleBlockItem("blood_rose", ModBlocks.BLOOD_ROSE);

    public static final DeferredItem<BlockItem> CRITTER_SNARE =
            ITEMS.registerSimpleBlockItem("critter_snare", ModBlocks.CRITTER_SNARE);

    public static final DeferredItem<BlockItem> VOID_BRAMBLE =
            ITEMS.registerSimpleBlockItem("void_bramble", ModBlocks.VOID_BRAMBLE);
    public static final DeferredItem<BlockItem> WILD_BRAMBLE_ITEM = VOID_BRAMBLE;

    public static final DeferredItem<BlockItem> EMBER_BRAMBLE =
            ITEMS.registerSimpleBlockItem("ember_bramble", ModBlocks.EMBER_BRAMBLE);
    public static final DeferredItem<BlockItem> BLOOD_BRAMBLE_ITEM = EMBER_BRAMBLE;

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

    public static final DeferredItem<MysticBranchItem> MYSTIC_BRANCH =
            ITEMS.register("mystic_branch", () -> new MysticBranchItem(new Item.Properties()));

    public static final DeferredItem<EarmuffsItem> EARMUFFS =
            ITEMS.register("earmuffs", () -> new EarmuffsItem(new Item.Properties().durability(120)));

    // =========================================================================
    // SPRINT 2: MÁQUINAS, JARROS, VAPORES, FIOS, CHALEIRA, CALDEIRÃO E FLUIDOS
    // =========================================================================

    // Itens de Bloco das Máquinas
    public static final DeferredItem<BlockItem> WITCHES_OVEN =
            ITEMS.registerSimpleBlockItem("witches_oven", ModBlocks.WITCHES_OVEN);
    public static final DeferredItem<BlockItem> FUME_FUNNEL =
            ITEMS.registerSimpleBlockItem("fume_funnel", ModBlocks.FUME_FUNNEL);
    public static final DeferredItem<BlockItem> FILTERED_FUME_FUNNEL =
            ITEMS.registerSimpleBlockItem("filtered_fume_funnel", ModBlocks.FILTERED_FUME_FUNNEL);
    public static final DeferredItem<BlockItem> DISTILLERY =
            ITEMS.registerSimpleBlockItem("distillery", ModBlocks.DISTILLERY);
    public static final DeferredItem<BlockItem> SPINNING_WHEEL =
            ITEMS.registerSimpleBlockItem("spinning_wheel", ModBlocks.SPINNING_WHEEL);
    public static final DeferredItem<BlockItem> KETTLE =
            ITEMS.registerSimpleBlockItem("kettle", ModBlocks.KETTLE);
    public static final DeferredItem<BlockItem> WITCHES_CAULDRON =
            ITEMS.registerSimpleBlockItem("witches_cauldron", ModBlocks.WITCHES_CAULDRON);
    public static final DeferredItem<BlockItem> BRAZIER =
            ITEMS.registerSimpleBlockItem("brazier", ModBlocks.BRAZIER);
    public static final DeferredItem<BlockItem> PIT_GRASS =
            ITEMS.registerSimpleBlockItem("pit_grass", ModBlocks.PIT_GRASS);
    public static final DeferredItem<BlockItem> PIT_DIRT =
            ITEMS.registerSimpleBlockItem("pit_dirt", ModBlocks.PIT_DIRT);

    // Gizes Rituais
    public static final DeferredItem<BlockItem> RITUAL_CHALK =
            ITEMS.registerSimpleBlockItem("ritual_chalk", ModBlocks.RITUAL_CHALK_BLOCK);
    public static final DeferredItem<BlockItem> GOLDEN_CHALK =
            ITEMS.registerSimpleBlockItem("golden_chalk", ModBlocks.GOLDEN_CHALK_BLOCK);
    public static final DeferredItem<BlockItem> INFERNAL_CHALK =
            ITEMS.registerSimpleBlockItem("infernal_chalk", ModBlocks.INFERNAL_CHALK_BLOCK);
    public static final DeferredItem<BlockItem> OTHERWHERE_CHALK =
            ITEMS.registerSimpleBlockItem("otherwhere_chalk", ModBlocks.OTHERWHERE_CHALK_BLOCK);

    // Baldes e Frascos de Fluidos
    public static final DeferredItem<BucketItem> FLOWING_SPIRIT_BUCKET =
            ITEMS.register("flowing_spirit_bucket", () -> new BucketItem(ModFluids.FLOWING_SPIRIT.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> HOLLOW_TEARS_BUCKET =
            ITEMS.register("hollow_tears_bucket", () -> new BucketItem(ModFluids.HOLLOW_TEARS.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> DISEASE_LIQUID_BUCKET =
            ITEMS.register("disease_liquid_bucket", () -> new BucketItem(ModFluids.DISEASE_LIQUID.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<Item> HOLLOW_TEARS_BOTTLE =
            ITEMS.registerSimpleItem("hollow_tears_bottle", new Item.Properties());

    // Jarros e Vapores do Forno da Bruxa e Destilaria
    public static final DeferredItem<Item> SOFT_CLAY_JAR =
            ITEMS.registerSimpleItem("soft_clay_jar", new Item.Properties());
    public static final DeferredItem<Item> CLAY_JAR =
            ITEMS.registerSimpleItem("clay_jar", new Item.Properties());
    public static final DeferredItem<Item> FOUL_FUME =
            ITEMS.registerSimpleItem("foul_fume", new Item.Properties());
    public static final DeferredItem<Item> EXHALE_OF_THE_HORNED_ONE =
            ITEMS.registerSimpleItem("exhale_of_the_horned_one", new Item.Properties());
    public static final DeferredItem<Item> HINT_OF_REBIRTH =
            ITEMS.registerSimpleItem("hint_of_rebirth", new Item.Properties());
    public static final DeferredItem<Item> BREATH_OF_THE_GODDESS =
            ITEMS.registerSimpleItem("breath_of_the_goddess", new Item.Properties());
    public static final DeferredItem<Item> WHIFF_OF_MAGIC =
            ITEMS.registerSimpleItem("whiff_of_magic", new Item.Properties());
    public static final DeferredItem<Item> REEK_OF_MISFORTUNE =
            ITEMS.registerSimpleItem("reek_of_misfortune", new Item.Properties());
    public static final DeferredItem<Item> ODOUR_OF_PURITY =
            ITEMS.registerSimpleItem("odour_of_purity", new Item.Properties());
    public static final DeferredItem<Item> TEAR_OF_THE_GODDESS =
            ITEMS.registerSimpleItem("tear_of_the_goddess", new Item.Properties());
    public static final DeferredItem<Item> OIL_OF_VITRIOL =
            ITEMS.registerSimpleItem("oil_of_vitriol", new Item.Properties());
    public static final DeferredItem<Item> DIAMOND_VAPOR =
            ITEMS.registerSimpleItem("diamond_vapor", new Item.Properties());
    public static final DeferredItem<Item> ENDER_DEW =
            ITEMS.registerSimpleItem("ender_dew", new Item.Properties());
    public static final DeferredItem<Item> DEMONIC_BLOOD =
            ITEMS.registerSimpleItem("demonic_blood", new Item.Properties());
    public static final DeferredItem<Item> REFINED_EVIL =
            ITEMS.registerSimpleItem("refined_evil", new Item.Properties());
    public static final DeferredItem<Item> FOCUSED_WILL =
            ITEMS.registerSimpleItem("focused_will", new Item.Properties());
    public static final DeferredItem<Item> CONDENSED_FEAR =
            ITEMS.registerSimpleItem("condensed_fear", new Item.Properties());

    // Fios da Roda de Fiar
    public static final DeferredItem<Item> DISTURBED_COTTON =
            ITEMS.registerSimpleItem("disturbed_cotton", new Item.Properties());
    public static final DeferredItem<Item> FANCIFUL_THREAD =
            ITEMS.registerSimpleItem("fanciful_thread", new Item.Properties());
    public static final DeferredItem<Item> TORMENTED_TWINE =
            ITEMS.registerSimpleItem("tormented_twine", new Item.Properties());
    public static final DeferredItem<Item> GOLDEN_THREAD =
            ITEMS.registerSimpleItem("golden_thread", new Item.Properties());

    // Insumos Alquímicos, Pós, Ingredientes e Artefatos
    public static final DeferredItem<Item> WOOD_ASH =
            ITEMS.registerSimpleItem("wood_ash", new Item.Properties());
    public static final DeferredItem<Item> QUICKLIME =
            ITEMS.registerSimpleItem("quicklime", new Item.Properties());
    public static final DeferredItem<Item> GYPSUM =
            ITEMS.registerSimpleItem("gypsum", new Item.Properties());
    public static final DeferredItem<AnointingPasteItem> ANOINTING_PASTE =
            ITEMS.register("anointing_paste", () -> new AnointingPasteItem(new Item.Properties()));
    public static final DeferredItem<Item> GRAVEYARD_DUST =
            ITEMS.registerSimpleItem("graveyard_dust", new Item.Properties());
    public static final DeferredItem<Item> DEMON_HEART =
            ITEMS.registerSimpleItem("demon_heart", new Item.Properties());
    public static final DeferredItem<Item> DOG_TONGUE =
            ITEMS.registerSimpleItem("dog_tongue", new Item.Properties());
    public static final DeferredItem<Item> WOOL_OF_BAT =
            ITEMS.registerSimpleItem("wool_of_bat", new Item.Properties());
    public static final DeferredItem<Item> TOE_OF_FROG =
            ITEMS.registerSimpleItem("toe_of_frog", new Item.Properties());
    public static final DeferredItem<Item> OWLETS_WING =
            ITEMS.registerSimpleItem("owlets_wing", new Item.Properties());
    public static final DeferredItem<Item> CREEPER_HEART =
            ITEMS.registerSimpleItem("creeper_heart", new Item.Properties());
    public static final DeferredItem<Item> ROTTEN_APPLE =
            ITEMS.registerSimpleItem("rotten_apple", new Item.Properties());
    public static final DeferredItem<Item> PURIFIED_MILK =
            ITEMS.registerSimpleItem("purified_milk", new Item.Properties());
    public static final DeferredItem<Item> MELLIFLUOUS_HUNGER =
            ITEMS.registerSimpleItem("mellifluous_hunger", new Item.Properties());
    public static final DeferredItem<Item> BAT_BALL =
            ITEMS.registerSimpleItem("bat_ball", new Item.Properties());
    public static final DeferredItem<Item> SUBDUED_SPIRIT =
            ITEMS.registerSimpleItem("subdued_spirit", new Item.Properties());
    public static final DeferredItem<Item> CONGEALED_SPIRIT =
            ITEMS.registerSimpleItem("congealed_spirit", new Item.Properties());
    public static final DeferredItem<Item> DROP_OF_LUCK =
            ITEMS.registerSimpleItem("drop_of_luck", new Item.Properties());
    public static final DeferredItem<Item> REDSTONE_SOUP =
            ITEMS.registerSimpleItem("redstone_soup", new Item.Properties());
    public static final DeferredItem<Item> FLYING_OINTMENT =
            ITEMS.registerSimpleItem("flying_ointment", new Item.Properties());
    public static final DeferredItem<Item> MYSTIC_UNGUENT =
            ITEMS.registerSimpleItem("mystic_unguent", new Item.Properties());
    public static final DeferredItem<Item> HAPPENSTANCE_OIL =
            ITEMS.registerSimpleItem("happenstance_oil", new Item.Properties());
    public static final DeferredItem<Item> GHOST_OF_THE_LIGHT =
            ITEMS.registerSimpleItem("ghost_of_the_light", new Item.Properties());
    public static final DeferredItem<Item> SOUL_OF_THE_WORLD =
            ITEMS.registerSimpleItem("soul_of_the_world", new Item.Properties());
    public static final DeferredItem<Item> SPIRIT_OF_OTHERWHERE =
            ITEMS.registerSimpleItem("spirit_of_otherwhere", new Item.Properties());
    public static final DeferredItem<Item> INFERNAL_ANIMUS =
            ITEMS.registerSimpleItem("infernal_animus", new Item.Properties());
    public static final DeferredItem<Item> INFUSION_BASE =
            ITEMS.registerSimpleItem("infusion_base", new Item.Properties());
    public static final DeferredItem<Item> ODD_PORKCHOP_RAW =
            ITEMS.register("odd_porkchop_raw", () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build()
            )));
    public static final DeferredItem<Item> ODD_PORKCHOP_COOKED =
            ITEMS.register("odd_porkchop_cooked", () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()
            )));
    public static final DeferredItem<Item> WITCH_HAND =
            ITEMS.registerSimpleItem("witch_hand", new Item.Properties());
    public static final DeferredItem<Item> FROZEN_HEART =
            ITEMS.registerSimpleItem("frozen_heart", new Item.Properties());
    public static final DeferredItem<Item> ICY_SLIPPERS =
            ITEMS.registerSimpleItem("icy_slippers", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SPECTRAL_DUST =
            ITEMS.registerSimpleItem("spectral_dust", new Item.Properties());
    public static final DeferredItem<Item> ATTUNED_STONE =
            ITEMS.registerSimpleItem("attuned_stone", new Item.Properties());
    public static final DeferredItem<Item> CHARGED_ATTUNED_STONE =
            ITEMS.registerSimpleItem("charged_attuned_stone", new Item.Properties());
    public static final DeferredItem<PoppetItem> UNBOUND_POPPET =
            ITEMS.register("unbound_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.UNBOUND));
    public static final DeferredItem<Item> GOLDEN_HEART =
            ITEMS.registerSimpleItem("golden_heart", new Item.Properties());
    public static final DeferredItem<Item> SILVER_DUST =
            ITEMS.registerSimpleItem("silver_dust", new Item.Properties());
    public static final DeferredItem<Item> GARLIC =
            ITEMS.registerSimpleItem("garlic", new Item.Properties());
    public static final DeferredItem<Item> WAYSTONE =
            ITEMS.registerSimpleItem("waystone", new Item.Properties());
    public static final DeferredItem<TaglockKitItem> TAGLOCK_KIT =
            ITEMS.register("taglock_kit", () -> new TaglockKitItem(new Item.Properties()));
    public static final DeferredItem<Item> PLAYER_COMPASS =
            ITEMS.registerSimpleItem("player_compass", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> ENDLESS_WATER_BREW =
            ITEMS.registerSimpleItem("endless_water_brew", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> CONTINUOUS_FUEL_BREW =
            ITEMS.registerSimpleItem("continuous_fuel_brew", new Item.Properties());

    // Vestimentas de Bruxa e Necromante (Bônus de Rendimento de Chaleira e Caldeirão)
    public static final DeferredItem<Item> WITCHES_HAT =
            ITEMS.registerSimpleItem("witches_hat", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BABA_YAGAS_HAT =
            ITEMS.registerSimpleItem("baba_yagas_hat", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> WITCHES_ROBES =
            ITEMS.registerSimpleItem("witches_robes", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> NECROMANCER_ROBES =
            ITEMS.registerSimpleItem("necromancer_robes", new Item.Properties().stacksTo(1));

    // Poção Alquímica Customizada do Caldeirão
    public static final DeferredItem<CauldronCustomPotionItem> CUSTOM_POTION =
            ITEMS.register("custom_potion", () -> new CauldronCustomPotionItem(new Item.Properties().stacksTo(16)));

    // 26 Infusões da Chaleira (Kettle Brews - Tabela 4.5 do Arquivo 02)
    public static final DeferredItem<KettleBrewItem> BREW_OF_VINES =
            ITEMS.register("brew_of_vines", () -> new KettleBrewItem(KettleBrewItem.BrewType.VINES, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_WEBS =
            ITEMS.register("brew_of_webs", () -> new KettleBrewItem(KettleBrewItem.BrewType.WEBS, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_THORNS =
            ITEMS.register("brew_of_thorns", () -> new KettleBrewItem(KettleBrewItem.BrewType.THORNS, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_INK =
            ITEMS.register("brew_of_ink", () -> new KettleBrewItem(KettleBrewItem.BrewType.INK, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_SPROUTING =
            ITEMS.register("brew_of_sprouting", () -> new KettleBrewItem(KettleBrewItem.BrewType.SPROUTING, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_EROSION =
            ITEMS.register("brew_of_erosion", () -> new KettleBrewItem(KettleBrewItem.BrewType.EROSION, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_RAISING =
            ITEMS.register("brew_of_raising", () -> new KettleBrewItem(KettleBrewItem.BrewType.RAISING, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_GROTESQUE =
            ITEMS.register("brew_grotesque", () -> new KettleBrewItem(KettleBrewItem.BrewType.GROTESQUE, false, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_LOVE =
            ITEMS.register("brew_of_love", () -> new KettleBrewItem(KettleBrewItem.BrewType.LOVE, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_ICE =
            ITEMS.register("brew_of_ice", () -> new KettleBrewItem(KettleBrewItem.BrewType.ICE, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_THE_DEPTHS =
            ITEMS.register("brew_of_the_depths", () -> new KettleBrewItem(KettleBrewItem.BrewType.THE_DEPTHS, false, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_INFECTION =
            ITEMS.register("brew_of_infection", () -> new KettleBrewItem(KettleBrewItem.BrewType.INFECTION, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_SLEEPING =
            ITEMS.register("brew_of_sleeping", () -> new KettleBrewItem(KettleBrewItem.BrewType.SLEEPING, false, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_FLOWING_SPIRIT =
            ITEMS.register("brew_of_flowing_spirit", () -> new KettleBrewItem(KettleBrewItem.BrewType.FLOWING_SPIRIT, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_WASTING =
            ITEMS.register("brew_of_wasting", () -> new KettleBrewItem(KettleBrewItem.BrewType.WASTING, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_BATS =
            ITEMS.register("brew_of_bats", () -> new KettleBrewItem(KettleBrewItem.BrewType.BATS, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_SUBSTITUTION =
            ITEMS.register("brew_of_substitution", () -> new KettleBrewItem(KettleBrewItem.BrewType.SUBSTITUTION, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_REVEALING =
            ITEMS.register("brew_of_revealing", () -> new KettleBrewItem(KettleBrewItem.BrewType.REVEALING, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_SOLID_DIRT =
            ITEMS.register("brew_of_solid_dirt", () -> new KettleBrewItem(KettleBrewItem.BrewType.SOLID_DIRT, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_SOLID_STONE =
            ITEMS.register("brew_of_solid_stone", () -> new KettleBrewItem(KettleBrewItem.BrewType.SOLID_STONE, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_SOLID_SAND =
            ITEMS.register("brew_of_solid_sand", () -> new KettleBrewItem(KettleBrewItem.BrewType.SOLID_SAND, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_SOLID_SANDSTONE =
            ITEMS.register("brew_of_solid_sandstone", () -> new KettleBrewItem(KettleBrewItem.BrewType.SOLID_SANDSTONE, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_HOLLOW_TEARS_EROSION =
            ITEMS.register("brew_of_hollow_tears_erosion", () -> new KettleBrewItem(KettleBrewItem.BrewType.HOLLOW_TEARS_EROSION, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_CURSED_LEAPING =
            ITEMS.register("brew_of_cursed_leaping", () -> new KettleBrewItem(KettleBrewItem.BrewType.CURSED_LEAPING, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_FROGS_TONGUE =
            ITEMS.register("brew_of_frogs_tongue", () -> new KettleBrewItem(KettleBrewItem.BrewType.FROGS_TONGUE, true, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<KettleBrewItem> BREW_OF_HITCHCOCK =
            ITEMS.register("brew_of_hitchcock", () -> new KettleBrewItem(KettleBrewItem.BrewType.HITCHCOCK, true, new Item.Properties().stacksTo(16)));

    // =========================================================================
    // SPRINT 3: MAGIA DE CÍRCULO, 11 POPPETS, FETICHES, ADIVINHAÇÃO E FAMILIARES
    // =========================================================================

    // Catálogo dos 11 Poppets (1.000 pontos de durabilidade - Seção 5 de 06_POPPETS_E_FETISHES.md)
    public static final DeferredItem<PoppetItem> EARTH_PROTECTION_POPPET =
            ITEMS.register("earth_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.EARTH_PROTECTION));
    public static final DeferredItem<PoppetItem> WATER_PROTECTION_POPPET =
            ITEMS.register("water_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.WATER_PROTECTION));
    public static final DeferredItem<PoppetItem> FIRE_PROTECTION_POPPET =
            ITEMS.register("fire_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.FIRE_PROTECTION));
    public static final DeferredItem<PoppetItem> HUNGER_PROTECTION_POPPET =
            ITEMS.register("hunger_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.HUNGER_PROTECTION));
    public static final DeferredItem<PoppetItem> TOOL_PROTECTION_POPPET =
            ITEMS.register("tool_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.TOOL_PROTECTION));
    public static final DeferredItem<PoppetItem> DEATH_PROTECTION_POPPET =
            ITEMS.register("death_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.DEATH_PROTECTION));
    public static final DeferredItem<PoppetItem> VOODOO_PROTECTION_POPPET =
            ITEMS.register("voodoo_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.VOODOO_PROTECTION));
    public static final DeferredItem<PoppetItem> VOODOO_POPPET =
            ITEMS.register("voodoo_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.VOODOO));
    public static final DeferredItem<PoppetItem> VAMPIRIC_POPPET =
            ITEMS.register("vampiric_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.VAMPIRIC));
    public static final DeferredItem<PoppetItem> POPPET_PROTECTION_POPPET =
            ITEMS.register("poppet_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.POPPET_PROTECTION));
    public static final DeferredItem<PoppetItem> ARMOR_PROTECTION_POPPET =
            ITEMS.register("armor_protection_poppet", () -> new PoppetItem(new Item.Properties(), PoppetItem.PoppetType.ARMOR_PROTECTION));

    // Prateleira de Poppets, Bússola, Fetiches e Bola de Cristal
    public static final DeferredItem<BlockItem> POPPET_SHELF =
            ITEMS.registerSimpleBlockItem("poppet_shelf", ModBlocks.POPPET_SHELF);
    public static final DeferredItem<ShelfCompassItem> SHELF_COMPASS =
            ITEMS.register("shelf_compass", () -> new ShelfCompassItem(new Item.Properties()));
    public static final DeferredItem<BlockItem> SCARECROW_FETISH =
            ITEMS.registerSimpleBlockItem("scarecrow_fetish", ModBlocks.SCARECROW_FETISH);
    public static final DeferredItem<BlockItem> TRENT_EFFIGY =
            ITEMS.registerSimpleBlockItem("trent_effigy", ModBlocks.TRENT_EFFIGY);
    public static final DeferredItem<BlockItem> WITCHS_LADDER =
            ITEMS.registerSimpleBlockItem("witchs_ladder", ModBlocks.WITCHS_LADDER);
    public static final DeferredItem<BlockItem> CRYSTAL_BALL =
            ITEMS.registerSimpleBlockItem("crystal_ball", ModBlocks.CRYSTAL_BALL);

    // Relíquias de Círculo, Adivinhação, Necromancia e Vassouras
    public static final DeferredItem<CircleTalismanItem> CIRCLE_TALISMAN =
            ITEMS.register("circle_talisman", () -> new CircleTalismanItem(new Item.Properties()));
    public static final DeferredItem<Item> BOUND_WAYSTONE =
            ITEMS.registerSimpleItem("bound_waystone", new Item.Properties().stacksTo(1));
    public static final DeferredItem<SeerStoneItem> SEER_STONE =
            ITEMS.register("seer_stone", () -> new SeerStoneItem(new Item.Properties()));
    public static final DeferredItem<NecroStoneItem> NECRO_STONE =
            ITEMS.register("necro_stone", () -> new NecroStoneItem(new Item.Properties()));
    public static final DeferredItem<Item> SPECTRAL_STONE =
            ITEMS.registerSimpleItem("spectral_stone", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BROOM =
            ITEMS.registerSimpleItem("broom", new Item.Properties().stacksTo(1));
    public static final DeferredItem<EnchantedBroomItem> ENCHANTED_BROOM =
            ITEMS.register("enchanted_broom", () -> new EnchantedBroomItem(new Item.Properties()));
    public static final DeferredItem<Item> BREW_OF_SOARING =
            ITEMS.registerSimpleItem("brew_of_soaring", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> BREW_OF_THE_GRAVE =
            ITEMS.registerSimpleItem("brew_of_the_grave", new Item.Properties().stacksTo(16));
    public static final DeferredItem<BlockItem> MYSTIC_MIRROR =
            ITEMS.registerSimpleBlockItem("mystic_mirror", ModBlocks.MYSTIC_MIRROR_BLOCK);
    public static final DeferredItem<Item> POLYNESIA_CHARM =
            ITEMS.registerSimpleItem("polynesia_charm", new Item.Properties().stacksTo(1));

    // Ovos de Invocação dos Familiares
    public static final DeferredItem<DeferredSpawnEggItem> TOAD_FAMILIAR_SPAWN_EGG =
            ITEMS.register("toad_familiar_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.TOAD_FAMILIAR, 0x3D6E2E, 0x8CA842, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> OWL_FAMILIAR_SPAWN_EGG =
            ITEMS.register("owl_familiar_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.OWL_FAMILIAR, 0x6B4C35, 0xD9B382, new Item.Properties()));

    // =========================================================================
    // SPRINT 4: VAMPIRISMO, LICANTROPIA, MUNDO ESPIRITUAL, TORMENTO E ESPELHO
    // =========================================================================

    // Vampirismo
    public static final DeferredItem<com.occultum.common.vampire.GlassGobletItem> GLASS_GOBLET =
            ITEMS.register("glass_goblet", () -> new com.occultum.common.vampire.GlassGobletItem(com.occultum.common.vampire.GlassGobletItem.GobletFluid.EMPTY, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.occultum.common.vampire.GlassGobletItem> GLASS_GOBLET_CHICKEN_BLOOD =
            ITEMS.register("glass_goblet_chicken_blood", () -> new com.occultum.common.vampire.GlassGobletItem(com.occultum.common.vampire.GlassGobletItem.GobletFluid.CHICKEN_BLOOD, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.occultum.common.vampire.GlassGobletItem> GLASS_GOBLET_LILITH_BLOOD =
            ITEMS.register("glass_goblet_lilith_blood", () -> new com.occultum.common.vampire.GlassGobletItem(com.occultum.common.vampire.GlassGobletItem.GobletFluid.LILITH_BLOOD, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.occultum.common.vampire.GlassGobletItem> GLASS_GOBLET_VAMPIRE_BLOOD =
            ITEMS.register("glass_goblet_vampire_blood", () -> new com.occultum.common.vampire.GlassGobletItem(com.occultum.common.vampire.GlassGobletItem.GobletFluid.VAMPIRE_BLOOD, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.occultum.common.vampire.VampireBookItem> VAMPIRE_BOOK =
            ITEMS.register("vampire_book", () -> new com.occultum.common.vampire.VampireBookItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> VAMPIRE_BOOK_PAGE =
            ITEMS.registerSimpleItem("vampire_book_page", new Item.Properties());
    public static final DeferredItem<Item> WOODEN_STAKE =
            ITEMS.registerSimpleItem("wooden_stake", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BLOOD_STAINED_WOOL =
            ITEMS.registerSimpleItem("blood_stained_wool", new Item.Properties());
    public static final DeferredItem<Item> SUN_COLLECTOR =
            ITEMS.registerSimpleItem("sun_collector", new Item.Properties().stacksTo(16));
    public static final DeferredItem<com.occultum.common.vampire.SunlightGrenadeItem> SUNLIGHT_GRENADE =
            ITEMS.register("sunlight_grenade", () -> new com.occultum.common.vampire.SunlightGrenadeItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> CANE_SWORD =
            ITEMS.register("cane_sword", () -> new SwordItem(Tiers.IRON, new Item.Properties().attributes(
                    SwordItem.createAttributes(Tiers.IRON, 4, -2.2F)
            )));
    public static final DeferredItem<BlockItem> BLOOD_CRUCIBLE =
            ITEMS.registerSimpleBlockItem("blood_crucible", ModBlocks.BLOOD_CRUCIBLE);
    public static final DeferredItem<BlockItem> COFFIN =
            ITEMS.registerSimpleBlockItem("coffin", ModBlocks.COFFIN);
    public static final DeferredItem<BlockItem> GARLIC_GARLAND =
            ITEMS.registerSimpleBlockItem("garlic_garland", ModBlocks.GARLIC_GARLAND);

    // Licantropia & Prata
    public static final DeferredItem<com.occultum.common.werewolf.MoonCharmItem> MOON_CHARM =
            ITEMS.register("moon_charm", () -> new com.occultum.common.werewolf.MoonCharmItem(new Item.Properties().stacksTo(1).durability(64)));
    public static final DeferredItem<BlockItem> WOLF_ALTAR =
            ITEMS.registerSimpleBlockItem("wolf_altar", ModBlocks.WOLF_ALTAR);
    public static final DeferredItem<Item> WOLF_TOKEN =
            ITEMS.registerSimpleItem("wolf_token", new Item.Properties().stacksTo(1));
    public static final DeferredItem<com.occultum.common.werewolf.HornOfTheHuntItem> HORN_OF_THE_HUNT =
            ITEMS.register("horn_of_the_hunt", () -> new com.occultum.common.werewolf.HornOfTheHuntItem(new Item.Properties().stacksTo(1).durability(16)));
    public static final DeferredItem<SwordItem> SILVER_SWORD =
            ITEMS.register("silver_sword", () -> new SwordItem(Tiers.IRON, new Item.Properties().attributes(
                    SwordItem.createAttributes(Tiers.IRON, 3, -2.4F)
            )));
    public static final DeferredItem<SwordItem> HUNTSMAN_SPEAR =
            ITEMS.register("huntsman_spear", () -> new SwordItem(Tiers.DIAMOND, new Item.Properties().attributes(
                    SwordItem.createAttributes(Tiers.DIAMOND, 5, -2.4F)
            )));
    public static final DeferredItem<SwordItem> HUNTSMANS_SPEAR = HUNTSMAN_SPEAR;
    public static final DeferredItem<Item> WOLFSBANE_BREW =
            ITEMS.registerSimpleItem("wolfsbane_brew", new Item.Properties().stacksTo(16));

    // Mundo Espiritual & Sonhos
    public static final DeferredItem<com.occultum.common.spirit.SleepingAppleItem> SLEEPING_APPLE =
            ITEMS.register("sleeping_apple", () -> new com.occultum.common.spirit.SleepingAppleItem(
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible().build())
            ));
    public static final DeferredItem<Item> CHARM_OF_DISRUPTED_DREAMS =
            ITEMS.registerSimpleItem("charm_of_disrupted_dreams", new Item.Properties().stacksTo(1));
    public static final DeferredItem<BlockItem> DREAM_WEAVER_FLEET_FOOT =
            ITEMS.registerSimpleBlockItem("dream_weaver_fleet_foot", ModBlocks.DREAM_WEAVER_FLEET_FOOT);
    public static final DeferredItem<BlockItem> DREAM_WEAVER_IRON_ARM =
            ITEMS.registerSimpleBlockItem("dream_weaver_iron_arm", ModBlocks.DREAM_WEAVER_IRON_ARM);
    public static final DeferredItem<BlockItem> DREAM_WEAVER_FASTING =
            ITEMS.registerSimpleBlockItem("dream_weaver_fasting", ModBlocks.DREAM_WEAVER_FASTING);
    public static final DeferredItem<BlockItem> DREAM_WEAVER_NIGHTMARES =
            ITEMS.registerSimpleBlockItem("dream_weaver_nightmares", ModBlocks.DREAM_WEAVER_NIGHTMARES);
    public static final DeferredItem<BlockItem> DREAM_WEAVER_INTENSITY =
            ITEMS.registerSimpleBlockItem("dream_weaver_intensity", ModBlocks.DREAM_WEAVER_INTENSITY);

    // Dimensão do Tormento & Dimensão do Espelho
    public static final DeferredItem<com.occultum.common.torment.TormentScrollItem> TORMENT_SCROLL =
            ITEMS.register("torment_scroll", () -> new com.occultum.common.torment.TormentScrollItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SOUL_OF_TORMENT_BREW =
            ITEMS.registerSimpleItem("soul_of_torment_brew", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> BREW_OF_SOUL_OF_TORMENT = SOUL_OF_TORMENT_BREW;
    public static final DeferredItem<BlockItem> TORMENT_STONE =
            ITEMS.registerSimpleBlockItem("torment_stone", ModBlocks.TORMENT_STONE);
    public static final DeferredItem<BlockItem> REFILLING_CHEST =
            ITEMS.registerSimpleBlockItem("refilling_chest", ModBlocks.REFILLING_CHEST);
    public static final DeferredItem<BlockItem> MIRROR_WALL =
            ITEMS.registerSimpleBlockItem("mirror_wall", ModBlocks.MIRROR_WALL);
    public static final DeferredItem<Item> QUARTZ_SPHERE =
            ITEMS.registerSimpleItem("quartz_sphere", new Item.Properties());
    public static final DeferredItem<com.occultum.common.mirror.DuplicationGrenadeItem> DUPLICATION_GRENADE =
            ITEMS.register("duplication_grenade", () -> new com.occultum.common.mirror.DuplicationGrenadeItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<com.occultum.common.mirror.HandBowItem> HAND_BOW =
            ITEMS.register("hand_bow", () -> new com.occultum.common.mirror.HandBowItem(new Item.Properties().stacksTo(1).durability(384)));
    public static final DeferredItem<Item> SILVER_BOLT =
            ITEMS.registerSimpleItem("silver_bolt", new Item.Properties());
    public static final DeferredItem<Item> SOLAR_BOLT =
            ITEMS.registerSimpleItem("solar_bolt", new Item.Properties());
    public static final DeferredItem<Item> HOLY_BOLT =
            ITEMS.registerSimpleItem("holy_bolt", new Item.Properties());

    // Ovos de Invocação da Sprint 4
    public static final DeferredItem<DeferredSpawnEggItem> ELLE_SPAWN_EGG =
            ITEMS.register("elle_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.ELLE, 0x4A1212, 0xD4AF37, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LILITH_SPAWN_EGG =
            ITEMS.register("lilith_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.LILITH, 0x220509, 0xFF1E38, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> WOLFMAN_SPAWN_EGG =
            ITEMS.register("wolfman_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.WOLFMAN, 0x3D312A, 0x8B6F4E, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> HORNED_HUNTSMAN_SPAWN_EGG =
            ITEMS.register("horned_huntsman_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.HORNED_HUNTSMAN, 0x1E3318, 0x997A3E, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SPIRIT_SPAWN_EGG =
            ITEMS.register("spirit_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.SPIRIT, 0xA8E6FF, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> NIGHTMARE_SPAWN_EGG =
            ITEMS.register("nightmare_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.NIGHTMARE, 0x0F0F1A, 0x7A1C99, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LORD_OF_TORMENT_SPAWN_EGG =
            ITEMS.register("lord_of_torment_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.LORD_OF_TORMENT, 0x5A0A0A, 0xFF6600, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> REFLECTION_DEMON_SPAWN_EGG =
            ITEMS.register("reflection_demon_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.REFLECTION_DEMON, 0x4E6880, 0xA0D8EF, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
