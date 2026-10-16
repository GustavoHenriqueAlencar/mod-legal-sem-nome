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
    }
}
