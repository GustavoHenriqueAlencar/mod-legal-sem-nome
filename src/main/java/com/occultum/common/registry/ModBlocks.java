package com.occultum.common.registry;

import com.occultum.OccultumMod;
import com.occultum.common.altar.AltarBlock;
import com.occultum.common.altar.GrassperBlock;
import com.occultum.common.altar.artifact.*;
import com.occultum.common.botany.crop.*;
import com.occultum.common.botany.tree.MysticLeavesBlock;
import com.occultum.common.botany.tree.MysticLogBlock;
import com.occultum.common.botany.tree.MysticSaplingBlock;
import com.occultum.common.botany.wild.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OccultumMod.MOD_ID);

    // Altar Multibloco
    public static final DeferredBlock<AltarBlock> ALTAR_BLOCK =
            BLOCKS.register("altar", () -> new AltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()));

    // Artefatos de Topo do Altar
    public static final DeferredBlock<CandelabraBlock> CANDELABRA =
            BLOCKS.register("candelabra", () -> new CandelabraBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.5F)
                    .noOcclusion()));

    public static final DeferredBlock<ChaliceBlock> CHALICE =
            BLOCKS.register("chalice", () -> new ChaliceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.5F)
                    .noOcclusion(), false));

    public static final DeferredBlock<ChaliceBlock> CHALICE_FILLED =
            BLOCKS.register("chalice_filled", () -> new ChaliceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.5F)
                    .noOcclusion(), true));

    public static final DeferredBlock<KobolditePentacleBlock> KOBOLDITE_PENTACLE_BLOCK =
            BLOCKS.register("koboldite_pentacle", () -> new KobolditePentacleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0F)
                    .noOcclusion()));

    public static final DeferredBlock<InfiniteEggBlock> INFINITE_EGG_BLOCK =
            BLOCKS.register("infinite_egg", () -> new InfiniteEggBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5.0F, 1200.0F)
                    .noOcclusion()));

    public static final DeferredBlock<GrassperBlock> GRASSPER =
            BLOCKS.register("grassper", () -> new GrassperBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .noOcclusion()));

    // Árvore 1: Sorveira (Rowan)
    public static final DeferredBlock<MysticLogBlock> ROWAN_LOG =
            BLOCKS.register("rowan_log", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> ROWAN_WOOD =
            BLOCKS.register("rowan_wood", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> STRIPPED_ROWAN_LOG =
            BLOCKS.register("stripped_rowan_log", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> STRIPPED_ROWAN_WOOD =
            BLOCKS.register("stripped_rowan_wood", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<Block> ROWAN_PLANKS =
            BLOCKS.register("rowan_planks", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)));

    public static final DeferredBlock<MysticLeavesBlock> ROWAN_LEAVES =
            BLOCKS.register("rowan_leaves", () -> new MysticLeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .noOcclusion(), ModItems.ROWAN_SAPLING, true));

    public static final DeferredBlock<MysticSaplingBlock> ROWAN_SAPLING =
            BLOCKS.register("rowan_sapling", () -> new MysticSaplingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    // Árvore 2: Amieiro (Alder)
    public static final DeferredBlock<MysticLogBlock> ALDER_LOG =
            BLOCKS.register("alder_log", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> ALDER_WOOD =
            BLOCKS.register("alder_wood", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> STRIPPED_ALDER_LOG =
            BLOCKS.register("stripped_alder_log", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> STRIPPED_ALDER_WOOD =
            BLOCKS.register("stripped_alder_wood", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F)));

    public static final DeferredBlock<Block> ALDER_PLANKS =
            BLOCKS.register("alder_planks", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0F, 3.0F)));

    public static final DeferredBlock<MysticLeavesBlock> ALDER_LEAVES =
            BLOCKS.register("alder_leaves", () -> new MysticLeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .noOcclusion(), ModItems.ALDER_SAPLING, false));

    public static final DeferredBlock<MysticSaplingBlock> ALDER_SAPLING =
            BLOCKS.register("alder_sapling", () -> new MysticSaplingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    // Árvore 3: Pilriteiro / Espinheiro (Hawthorn)
    public static final DeferredBlock<MysticLogBlock> HAWTHORN_LOG =
            BLOCKS.register("hawthorn_log", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> HAWTHORN_WOOD =
            BLOCKS.register("hawthorn_wood", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> STRIPPED_HAWTHORN_LOG =
            BLOCKS.register("stripped_hawthorn_log", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<MysticLogBlock> STRIPPED_HAWTHORN_WOOD =
            BLOCKS.register("stripped_hawthorn_wood", () -> new MysticLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)));

    public static final DeferredBlock<Block> HAWTHORN_PLANKS =
            BLOCKS.register("hawthorn_planks", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)));

    public static final DeferredBlock<MysticLeavesBlock> HAWTHORN_LEAVES =
            BLOCKS.register("hawthorn_leaves", () -> new MysticLeavesBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .noOcclusion(), ModItems.HAWTHORN_SAPLING, false));

    public static final DeferredBlock<MysticSaplingBlock> HAWTHORN_SAPLING =
            BLOCKS.register("hawthorn_sapling", () -> new MysticSaplingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    // Cultivos Místicos (Witch Crops)
    public static final DeferredBlock<BelladonnaCropBlock> BELLADONNA_CROP =
            BLOCKS.register("belladonna_crop", () -> new BelladonnaCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<MandrakeCropBlock> MANDRAKE_CROP =
            BLOCKS.register("mandrake_crop", () -> new MandrakeCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<WaterArtichokeCropBlock> WATER_ARTICHOKE_CROP =
            BLOCKS.register("water_artichoke_crop", () -> new WaterArtichokeCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WATER)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<SnowbellCropBlock> SNOWBELL_CROP =
            BLOCKS.register("snowbell_crop", () -> new SnowbellCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<WormwoodCropBlock> WORMWOOD_CROP =
            BLOCKS.register("wormwood_crop", () -> new WormwoodCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<WolfsbaneCropBlock> WOLFSBANE_CROP =
            BLOCKS.register("wolfsbane_crop", () -> new WolfsbaneCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<MindrakeCropBlock> MINDRAKE_CROP =
            BLOCKS.register("mindrake_crop", () -> new MindrakeCropBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .noCollission()
                    .instabreak()));

    // Vegetação Selvagem, Parasitária e Armadilhas
    public static final DeferredBlock<SpanishMossBlock> SPANISH_MOSS =
            BLOCKS.register("spanish_moss", () -> new SpanishMossBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<EmberMossBlock> EMBER_MOSS =
            BLOCKS.register("ember_moss", () -> new EmberMossBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<GlintWeedBlock> GLINT_WEED =
            BLOCKS.register("glint_weed", () -> new GlintWeedBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<BloodRoseBlock> BLOOD_ROSE =
            BLOCKS.register("blood_rose", () -> new BloodRoseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<CritterSnareBlock> CRITTER_SNARE =
            BLOCKS.register("critter_snare", () -> new CritterSnareBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<VoidBrambleBlock> VOID_BRAMBLE =
            BLOCKS.register("void_bramble", () -> new VoidBrambleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F)
                    .noOcclusion()));

    public static final DeferredBlock<EmberBrambleBlock> EMBER_BRAMBLE =
            BLOCKS.register("ember_bramble", () -> new EmberBrambleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(1.2F)
                    .noOcclusion()));

    public static final DeferredBlock<LeechChestBlock> LEECH_CHEST =
            BLOCKS.register("leech_chest", () -> new LeechChestBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)));

    public static final DeferredBlock<WispyCottonBlock> WISPY_COTTON =
            BLOCKS.register("wispy_cotton", () -> new WispyCottonBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .noCollission()
                    .instabreak()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
