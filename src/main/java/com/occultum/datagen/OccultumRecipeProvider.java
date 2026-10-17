package com.occultum.datagen;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class OccultumRecipeProvider extends RecipeProvider {

    public OccultumRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 1.1 Bloco de Altar
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ALTAR_BLOCK.get(), 3)
                .pattern("SBS")
                .pattern("STS")
                .pattern("TTT")
                .define('S', Blocks.SMOOTH_STONE)
                .define('B', ModItems.BREATH_OF_THE_GODDESS.get())
                .define('T', Blocks.STONE_BRICKS)
                .unlockedBy("has_breath", has(ModItems.BREATH_OF_THE_GODDESS.get()))
                .save(recipeOutput, "occultum:altar_crafting");

        // 1.2 Forno da Bruxa e Chaminés
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.WITCHES_OVEN.get(), 1)
                .pattern(" B ")
                .pattern("III")
                .pattern("IBI")
                .define('B', Items.IRON_BARS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FUME_FUNNEL.get(), 1)
                .pattern("BLB")
                .pattern("GFG")
                .pattern("III")
                .define('B', Items.BUCKET)
                .define('L', Items.LAVA_BUCKET)
                .define('G', Blocks.GLOWSTONE)
                .define('F', Blocks.IRON_BLOCK)
                .define('I', Items.IRON_BARS)
                .unlockedBy("has_witches_oven", has(ModBlocks.WITCHES_OVEN.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FILTERED_FUME_FUNNEL.get(), 1)
                .pattern("III")
                .pattern("IFI")
                .pattern("BBB")
                .define('I', Items.IRON_INGOT)
                .define('F', ModBlocks.FUME_FUNNEL.get())
                .define('B', Items.IRON_BARS)
                .unlockedBy("has_fume_funnel", has(ModBlocks.FUME_FUNNEL.get()))
                .save(recipeOutput);

        // 1.3 Destilaria
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DISTILLERY.get(), 1)
                .pattern("IJI")
                .pattern("IAI")
                .pattern("GGG")
                .define('I', Items.IRON_INGOT)
                .define('J', ModItems.CLAY_JAR.get())
                .define('A', ModItems.ATTUNED_STONE.get())
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_attuned_stone", has(ModItems.ATTUNED_STONE.get()))
                .save(recipeOutput);

        // 1.4 Chaleira
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.KETTLE.get(), 1)
                .pattern(" S ")
                .pattern("ISI")
                .pattern("IBI")
                .define('S', Items.STICK)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.WATER_BUCKET)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);

        // 1.5 Braseiro Ritualístico
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.BRAZIER.get(), 1)
                .pattern("IAI")
                .pattern(" I ")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('A', ModItems.ATTUNED_STONE.get())
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);

        // 1.6 Roda de Fiar
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SPINNING_WHEEL.get(), 1)
                .pattern(" W ")
                .pattern("SAI")
                .pattern("PPP")
                .define('W', Blocks.WHITE_WOOL)
                .define('S', Items.STICK)
                .define('A', ModItems.ATTUNED_STONE.get())
                .define('I', Items.IRON_INGOT)
                .define('P', ModBlocks.ALDER_PLANKS.get())
                .unlockedBy("has_alder_planks", has(ModBlocks.ALDER_PLANKS.get()))
                .save(recipeOutput);

        // Madeiras e Tábuas
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ROWAN_PLANKS.get(), 4)
                .requires(ModBlocks.ROWAN_LOG.get())
                .unlockedBy("has_rowan_log", has(ModBlocks.ROWAN_LOG.get()))
                .save(recipeOutput, "occultum:rowan_planks_from_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ROWAN_WOOD.get(), 3)
                .pattern("LL")
                .pattern("LL")
                .define('L', ModBlocks.ROWAN_LOG.get())
                .unlockedBy("has_rowan_log", has(ModBlocks.ROWAN_LOG.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ALDER_PLANKS.get(), 4)
                .requires(ModBlocks.ALDER_LOG.get())
                .unlockedBy("has_alder_log", has(ModBlocks.ALDER_LOG.get()))
                .save(recipeOutput, "occultum:alder_planks_from_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ALDER_WOOD.get(), 3)
                .pattern("LL")
                .pattern("LL")
                .define('L', ModBlocks.ALDER_LOG.get())
                .unlockedBy("has_alder_log", has(ModBlocks.ALDER_LOG.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HAWTHORN_PLANKS.get(), 4)
                .requires(ModBlocks.HAWTHORN_LOG.get())
                .unlockedBy("has_hawthorn_log", has(ModBlocks.HAWTHORN_LOG.get()))
                .save(recipeOutput, "occultum:hawthorn_planks_from_log");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.HAWTHORN_WOOD.get(), 3)
                .pattern("LL")
                .pattern("LL")
                .define('L', ModBlocks.HAWTHORN_LOG.get())
                .unlockedBy("has_hawthorn_log", has(ModBlocks.HAWTHORN_LOG.get()))
                .save(recipeOutput);

        // Ingredientes e Materiais Intermediários (Seção 6 e 8)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOFT_CLAY_JAR.get(), 4)
                .pattern(" C ")
                .pattern("CCC")
                .define('C', Items.CLAY_BALL)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.QUICKLIME.get(), 1)
                .requires(ModItems.WOOD_ASH.get())
                .requires(Items.CLAY_BALL)
                .unlockedBy("has_wood_ash", has(ModItems.WOOD_ASH.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ANOINTING_PASTE.get(), 1)
                .requires(ModItems.BELLADONNA_SEEDS.get())
                .requires(ModItems.MANDRAKE_SEEDS.get())
                .requires(ModItems.WATER_ARTICHOKE_SEEDS.get())
                .requires(ModItems.SNOWBELL_SEEDS.get())
                .unlockedBy("has_mandrake_seeds", has(ModItems.MANDRAKE_SEEDS.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GRAVEYARD_DUST.get(), 1)
                .requires(Items.BONE_MEAL)
                .requires(ModItems.FOUL_FUME.get())
                .unlockedBy("has_foul_fume", has(ModItems.FOUL_FUME.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ATTUNED_STONE.get(), 1)
                .pattern("W")
                .pattern("D")
                .pattern("L")
                .define('W', ModItems.WHIFF_OF_MAGIC.get())
                .define('D', Items.DIAMOND)
                .define('L', Items.LAVA_BUCKET)
                .unlockedBy("has_whiff", has(ModItems.WHIFF_OF_MAGIC.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.RITUAL_CHALK.get(), 2)
                .requires(ModItems.WOOD_ASH.get())
                .requires(ModItems.GYPSUM.get())
                .requires(ModItems.TEAR_OF_THE_GODDESS.get())
                .unlockedBy("has_tear", has(ModItems.TEAR_OF_THE_GODDESS.get()))
                .save(recipeOutput);

        // Vestimentas de Bruxa e Necromante (Seção 3.1)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WITCHES_HAT.get(), 1)
                .pattern("STS")
                .pattern("SLS")
                .pattern("LLL")
                .define('S', Items.STRING)
                .define('T', ModItems.FANCIFUL_THREAD.get())
                .define('L', Items.LEATHER)
                .unlockedBy("has_fanciful_thread", has(ModItems.FANCIFUL_THREAD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WITCHES_ROBES.get(), 1)
                .pattern("L L")
                .pattern("LGL")
                .pattern("LLL")
                .define('L', Items.LEATHER)
                .define('G', ModItems.GOLDEN_THREAD.get())
                .unlockedBy("has_golden_thread", has(ModItems.GOLDEN_THREAD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EARMUFFS.get(), 1)
                .pattern("III")
                .pattern("I I")
                .pattern("W W")
                .define('I', Items.IRON_INGOT)
                .define('W', Blocks.WHITE_WOOL)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);

        // Receitas de Fornalha Convencional (Seção 10)
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.SOFT_CLAY_JAR.get()), RecipeCategory.MISC,
                        ModItems.CLAY_JAR.get(), 0.1F, 200)
                .unlockedBy("has_soft_clay_jar", has(ModItems.SOFT_CLAY_JAR.get()))
                .save(recipeOutput, "occultum:clay_jar_from_smelting");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.ODD_PORKCHOP_RAW.get()), RecipeCategory.FOOD,
                        ModItems.ODD_PORKCHOP_COOKED.get(), 0.35F, 200)
                .unlockedBy("has_odd_porkchop", has(ModItems.ODD_PORKCHOP_RAW.get()))
                .save(recipeOutput, "occultum:odd_porkchop_cooked_from_smelting");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.GOLDEN_THREAD.get()), RecipeCategory.MISC,
                        Items.GOLD_NUGGET, 0.1F, 200)
                .unlockedBy("has_golden_thread", has(ModItems.GOLDEN_THREAD.get()))
                .save(recipeOutput, "occultum:gold_nugget_from_golden_thread");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(
                        ModItems.ROWAN_SAPLING.get(), ModItems.ALDER_SAPLING.get(), ModItems.HAWTHORN_SAPLING.get()
                ), RecipeCategory.MISC, ModItems.WOOD_ASH.get(), 0.1F, 200)
                .unlockedBy("has_rowan_sapling", has(ModItems.ROWAN_SAPLING.get()))
                .save(recipeOutput, "occultum:wood_ash_from_saplings");

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(
                        ModBlocks.ROWAN_LOG.get(), ModBlocks.ALDER_LOG.get(), ModBlocks.HAWTHORN_LOG.get()
                ), RecipeCategory.MISC, Items.CHARCOAL, 0.15F, 200)
                .unlockedBy("has_rowan_log", has(ModBlocks.ROWAN_LOG.get()))
                .save(recipeOutput, "occultum:charcoal_from_mystic_logs");

        // =========================================================================
        // SPRINT 3: POPPETS, PRATELEIRA, FETICHES, TALISMÃ E ITENS DE CÍRCULO
        // =========================================================================

        // Kit Taglock
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.TAGLOCK_KIT.get(), 2)
                .requires(Items.GLASS_BOTTLE)
                .requires(Items.IRON_NUGGET)
                .unlockedBy("has_glass_bottle", has(Items.GLASS_BOTTLE))
                .save(recipeOutput);

        // Poppet Base (Unbound Poppet) - Seção 5.1 de 06_POPPETS_E_FETISHES.md
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.UNBOUND_POPPET.get(), 1)
                .pattern("WMN")
                .pattern("TSW")
                .pattern(" W ")
                .define('W', Blocks.WHITE_WOOL)
                .define('M', ModItems.SPANISH_MOSS.get())
                .define('N', Items.GOLD_NUGGET)
                .define('T', ModItems.TORMENTED_TWINE.get())
                .define('S', Items.WHEAT_SEEDS)
                .unlockedBy("has_spanish_moss", has(ModItems.SPANISH_MOSS.get()))
                .save(recipeOutput);

        // 11 Poppets (Seção 5.2 de 06_POPPETS_E_FETISHES.md) + Vinculação com Taglock Kit
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.EARTH_PROTECTION_POPPET.get(), 1)
                .pattern(" M ")
                .pattern("SPS")
                .pattern(" D ")
                .define('M', ModItems.SPANISH_MOSS.get())
                .define('S', Blocks.STONE)
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WATER_PROTECTION_POPPET.get(), 1)
                .pattern(" W ")
                .pattern("FPF")
                .pattern(" D ")
                .define('W', ModItems.WATER_ARTICHOKE.get())
                .define('F', Items.COD)
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FIRE_PROTECTION_POPPET.get(), 1)
                .pattern(" E ")
                .pattern("CPC")
                .pattern(" D ")
                .define('E', ModItems.EMBER_MOSS.get())
                .define('C', Items.MAGMA_CREAM)
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HUNGER_PROTECTION_POPPET.get(), 1)
                .pattern(" R ")
                .pattern("MPM")
                .pattern(" D ")
                .define('R', ModItems.ROWAN_BERRIES.get())
                .define('M', Items.GLISTERING_MELON_SLICE)
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TOOL_PROTECTION_POPPET.get(), 1)
                .pattern(" B ")
                .pattern("SPS")
                .pattern(" D ")
                .define('B', ModItems.BELLADONNA_FLOWER.get())
                .define('S', Items.IRON_SWORD)
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ARMOR_PROTECTION_POPPET.get(), 1)
                .pattern(" T ")
                .pattern("APA")
                .pattern(" D ")
                .define('T', ModItems.GOLDEN_THREAD.get())
                .define('A', Items.IRON_LEGGINGS)
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEATH_PROTECTION_POPPET.get(), 1)
                .pattern(" G ")
                .pattern("HPF")
                .pattern(" D ")
                .define('G', Items.GOLD_NUGGET)
                .define('H', ModItems.HINT_OF_REBIRTH.get())
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('F', ModItems.FOCUSED_WILL.get())
                .define('D', ModItems.DROP_OF_LUCK.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOODOO_PROTECTION_POPPET.get(), 1)
                .pattern(" O ")
                .pattern("BPB")
                .pattern(" D ")
                .define('O', ModItems.ODOUR_OF_PURITY.get())
                .define('B', ModItems.BELLADONNA_FLOWER.get())
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOODOO_POPPET.get(), 1)
                .pattern(" R ")
                .pattern("NPB")
                .pattern(" D ")
                .define('R', ModItems.REEK_OF_MISFORTUNE.get())
                .define('N', ModItems.ICY_NEEDLE.get())
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('B', ModItems.BELLADONNA_FLOWER.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VAMPIRIC_POPPET.get(), 1)
                .pattern(" R ")
                .pattern("WPB")
                .pattern(" D ")
                .define('R', ModItems.REEK_OF_MISFORTUNE.get())
                .define('W', ModItems.TORMENTED_TWINE.get())
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('B', ModItems.DEMONIC_BLOOD.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.POPPET_PROTECTION_POPPET.get(), 1)
                .pattern(" W ")
                .pattern("EPB")
                .pattern(" D ")
                .define('W', ModItems.WHIFF_OF_MAGIC.get())
                .define('E', ModItems.ENDER_DEW.get())
                .define('P', ModItems.UNBOUND_POPPET.get())
                .define('B', ModItems.BELLADONNA_FLOWER.get())
                .define('D', ModItems.DIAMOND_VAPOR.get())
                .unlockedBy("has_unbound_poppet", has(ModItems.UNBOUND_POPPET.get()))
                .save(recipeOutput);

        // Prateleira de Poppets e Bússola (Seção 3 de 06_POPPETS_E_FETISHES.md)
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.POPPET_SHELF.get(), 1)
                .pattern("TAT")
                .pattern("GWG")
                .pattern("WWW")
                .define('T', ModItems.TORMENTED_TWINE.get())
                .define('A', ModItems.ATTUNED_STONE.get())
                .define('G', Blocks.GREEN_WOOL)
                .define('W', ModBlocks.ROWAN_PLANKS.get())
                .unlockedBy("has_attuned_stone", has(ModItems.ATTUNED_STONE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SHELF_COMPASS.get(), 1)
                .pattern(" A ")
                .pattern("ACA")
                .pattern(" A ")
                .define('A', ModItems.ATTUNED_STONE.get())
                .define('C', Items.COMPASS)
                .unlockedBy("has_attuned_stone", has(ModItems.ATTUNED_STONE.get()))
                .save(recipeOutput);

        // Fetiches Base (Scarecrow, Trent Effigy, Witch's Ladder)
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SCARECROW_FETISH.get(), 1)
                .pattern(" W ")
                .pattern("SPS")
                .pattern(" L ")
                .define('W', Blocks.WHITE_WOOL)
                .define('S', Items.STICK)
                .define('P', ModBlocks.ROWAN_PLANKS.get())
                .define('L', ModBlocks.ROWAN_LOG.get())
                .unlockedBy("has_rowan_log", has(ModBlocks.ROWAN_LOG.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TRENT_EFFIGY.get(), 1)
                .pattern(" H ")
                .pattern("AHA")
                .pattern(" R ")
                .define('H', ModBlocks.HAWTHORN_LOG.get())
                .define('A', ModBlocks.ALDER_LOG.get())
                .define('R', ModBlocks.ROWAN_LOG.get())
                .unlockedBy("has_rowan_log", has(ModBlocks.ROWAN_LOG.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.WITCHS_LADDER.get(), 1)
                .pattern("FTF")
                .pattern("STS")
                .pattern("FTF")
                .define('F', Items.FEATHER)
                .define('T', ModItems.TORMENTED_TWINE.get())
                .define('S', Items.STICK)
                .unlockedBy("has_tormented_twine", has(ModItems.TORMENTED_TWINE.get()))
                .save(recipeOutput);

        // Talismã de Círculo, Pedra de Rumo e Vassoura
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CIRCLE_TALISMAN.get(), 1)
                .pattern(" G ")
                .pattern("GDG")
                .pattern(" G ")
                .define('G', Items.GOLD_INGOT)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WAYSTONE.get(), 1)
                .pattern(" S ")
                .pattern("SFS")
                .pattern(" S ")
                .define('S', Blocks.SMOOTH_STONE)
                .define('F', Items.FLINT)
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.BROOM.get(), 1)
                .pattern("  H")
                .pattern(" S ")
                .pattern("W  ")
                .define('H', ModBlocks.HAWTHORN_SAPLING.get())
                .define('S', Items.STICK)
                .define('W', Items.WHEAT)
                .unlockedBy("has_hawthorn_sapling", has(ModBlocks.HAWTHORN_SAPLING.get()))
                .save(recipeOutput);

        // =========================================================================
        // SPRINT 4: VAMPIRISMO, LICANTROPIA, APANHADORES DE SONHOS E ESPELHO
        // =========================================================================

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.GLASS_GOBLET.get(), 1)
                .pattern("G G")
                .pattern(" G ")
                .pattern(" G ")
                .define('G', Blocks.GLASS)
                .unlockedBy("has_glass", has(Blocks.GLASS))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WOODEN_STAKE.get(), 1)
                .pattern("  S")
                .pattern(" P ")
                .pattern("S  ")
                .define('S', Items.STICK)
                .define('P', ModBlocks.HAWTHORN_PLANKS.get())
                .unlockedBy("has_hawthorn_planks", has(ModBlocks.HAWTHORN_PLANKS.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.BLOOD_CRUCIBLE.get(), 1)
                .pattern("N N")
                .pattern("NDN")
                .pattern("NNN")
                .define('N', Blocks.NETHER_BRICKS)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_nether_bricks", has(Blocks.NETHER_BRICKS))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.COFFIN.get(), 1)
                .pattern("PPP")
                .pattern("WWW")
                .pattern("PPP")
                .define('P', ModBlocks.ALDER_PLANKS.get())
                .define('W', Blocks.RED_WOOL)
                .unlockedBy("has_alder_planks", has(ModBlocks.ALDER_PLANKS.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.GARLIC_GARLAND.get(), 1)
                .pattern("GGG")
                .pattern("STS")
                .define('G', ModItems.GARLIC.get())
                .define('S', Items.STRING)
                .define('T', ModItems.TORMENTED_TWINE.get())
                .unlockedBy("has_garlic", has(ModItems.GARLIC.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SUN_COLLECTOR.get(), 1)
                .pattern(" G ")
                .pattern("GQG")
                .pattern(" I ")
                .define('G', Blocks.GLASS)
                .define('Q', Items.QUARTZ)
                .define('I', Items.GOLD_INGOT)
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.WOLF_ALTAR.get(), 1)
                .pattern(" S ")
                .pattern("SBS")
                .pattern("SSS")
                .define('S', Blocks.STONE_BRICKS)
                .define('B', Items.BONE_BLOCK)
                .unlockedBy("has_bone_block", has(Items.BONE_BLOCK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SILVER_SWORD.get(), 1)
                .pattern(" D ")
                .pattern("IDI")
                .pattern(" S ")
                .define('D', ModItems.SILVER_DUST.get())
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_silver_dust", has(ModItems.SILVER_DUST.get()))
                .save(recipeOutput);

        // Os 5 Apanhadores de Sonhos (Dream Weavers - Seção 5.2 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md)
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DREAM_WEAVER_FLEET_FOOT.get(), 1)
                .pattern("TTT")
                .pattern("FSF")
                .pattern(" W ")
                .define('T', ModItems.FANCIFUL_THREAD.get())
                .define('F', Items.FEATHER)
                .define('S', Items.STICK)
                .define('W', ModItems.WISPY_COTTON.get())
                .unlockedBy("has_wispy_cotton", has(ModItems.WISPY_COTTON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DREAM_WEAVER_IRON_ARM.get(), 1)
                .pattern("TTT")
                .pattern("ISI")
                .pattern(" W ")
                .define('T', ModItems.FANCIFUL_THREAD.get())
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .define('W', ModItems.WISPY_COTTON.get())
                .unlockedBy("has_wispy_cotton", has(ModItems.WISPY_COTTON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DREAM_WEAVER_FASTING.get(), 1)
                .pattern("TTT")
                .pattern("MSM")
                .pattern(" W ")
                .define('T', ModItems.FANCIFUL_THREAD.get())
                .define('M', ModItems.MELLIFLUOUS_HUNGER.get())
                .define('S', Items.STICK)
                .define('W', ModItems.WISPY_COTTON.get())
                .unlockedBy("has_wispy_cotton", has(ModItems.WISPY_COTTON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DREAM_WEAVER_NIGHTMARES.get(), 1)
                .pattern("TTT")
                .pattern("DSD")
                .pattern(" C ")
                .define('T', ModItems.TORMENTED_TWINE.get())
                .define('D', ModItems.DISTURBED_COTTON.get())
                .define('S', Items.STICK)
                .define('C', ModItems.REEK_OF_MISFORTUNE.get())
                .unlockedBy("has_disturbed_cotton", has(ModItems.DISTURBED_COTTON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DREAM_WEAVER_INTENSITY.get(), 1)
                .pattern("TTT")
                .pattern("GSG")
                .pattern(" W ")
                .define('T', ModItems.GOLDEN_THREAD.get())
                .define('G', Items.GLOWSTONE_DUST)
                .define('S', Items.STICK)
                .define('W', ModItems.WISPY_COTTON.get())
                .unlockedBy("has_wispy_cotton", has(ModItems.WISPY_COTTON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CHARM_OF_DISRUPTED_DREAMS.get(), 1)
                .pattern(" G ")
                .pattern("WMW")
                .pattern(" D ")
                .define('G', ModItems.GOLDEN_THREAD.get())
                .define('W', ModItems.WISPY_COTTON.get())
                .define('M', ModItems.MELLIFLUOUS_HUNGER.get())
                .define('D', ModItems.DISTURBED_COTTON.get())
                .unlockedBy("has_wispy_cotton", has(ModItems.WISPY_COTTON.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.QUARTZ_SPHERE.get(), 1)
                .pattern(" Q ")
                .pattern("QGQ")
                .pattern(" Q ")
                .define('Q', Items.QUARTZ)
                .define('G', Blocks.GLASS)
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.HAND_BOW.get(), 1)
                .pattern("ISI")
                .pattern(" T ")
                .pattern(" H ")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STRING)
                .define('T', Items.TRIPWIRE_HOOK)
                .define('H', ModBlocks.HAWTHORN_PLANKS.get())
                .unlockedBy("has_hawthorn_planks", has(ModBlocks.HAWTHORN_PLANKS.get()))
                .save(recipeOutput);
    }
}
