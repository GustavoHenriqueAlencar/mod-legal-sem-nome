package com.occultum.datagen;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class OccultumRecipeProvider extends RecipeProvider {

    public OccultumRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 1.1 Bloco de Altar das Bruxas (12_RECEITAS_DE_BANCADA_E_FORNALHA.md)
        // Linha 1: [Pedra Lisa] [Pó de Redstone] [Pedra Lisa] (substituindo provisoriamente Respiração da Deusa)
        // Linha 2: [Pedra Lisa] [Tijolo de Pedra] [Pedra Lisa]
        // Linha 3: [Tijolo de Pedra] [Tijolo de Pedra] [Tijolo de Pedra]
        // Rendimento: 3 Blocos de Altar
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ALTAR_BLOCK.get(), 3)
                .pattern("SRS")
                .pattern("SBS")
                .pattern("BBB")
                .define('S', Blocks.STONE)
                .define('R', Items.REDSTONE)
                .define('B', Blocks.STONE_BRICKS)
                .unlockedBy("has_stone_bricks", has(Blocks.STONE_BRICKS))
                .save(recipeOutput, "occultum:altar_crafting");

        // Madeira e Tábuas de Sorveira
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

        // Madeira e Tábuas de Amieiro
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

        // Madeira e Tábuas de Pilriteiro
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
    }
}
