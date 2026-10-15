package com.occultum.common.altar;

import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class AltarNatureScanner {

    public static class NatureBlockEntry {
        public final int powerPerBlock;
        public final int maxCount;
        private int currentCount = 0;

        public NatureBlockEntry(int powerPerBlock, int maxCount) {
            this.powerPerBlock = powerPerBlock;
            this.maxCount = maxCount;
        }

        public void add() {
            if (currentCount < maxCount) {
                currentCount++;
            }
        }

        public int calculatePower() {
            return currentCount * powerPerBlock;
        }

        public void reset() {
            currentCount = 0;
        }
    }

    public static class NatureMatrix {
        public final NatureBlockEntry saplings = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry commonLogs = new NatureBlockEntry(2, 50);
        public final NatureBlockEntry commonLeaves = new NatureBlockEntry(3, 100);
        public final NatureBlockEntry grassBlock = new NatureBlockEntry(2, 80);
        public final NatureBlockEntry dirt = new NatureBlockEntry(1, 80);
        public final NatureBlockEntry farmland = new NatureBlockEntry(1, 100);
        public final NatureBlockEntry shortGrassFern = new NatureBlockEntry(3, 50);
        public final NatureBlockEntry dandelion = new NatureBlockEntry(4, 30);
        public final NatureBlockEntry smallFlowers = new NatureBlockEntry(4, 30);
        public final NatureBlockEntry doublePlants = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry stillWater = new NatureBlockEntry(1, 50);
        public final NatureBlockEntry brownMushroom = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry redMushroom = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry hugeBrownMushroom = new NatureBlockEntry(3, 50);
        public final NatureBlockEntry hugeRedMushroom = new NatureBlockEntry(3, 50);
        public final NatureBlockEntry pumpkin = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry pumpkinStem = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry melon = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry melonStem = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry cactus = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry sugarCane = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry vine = new NatureBlockEntry(2, 50);
        public final NatureBlockEntry lilyPad = new NatureBlockEntry(1, 80);
        public final NatureBlockEntry cocoa = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry wheatCrop = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry carrotCrop = new NatureBlockEntry(4, 20);

        // Mod Mystic Plants
        public final NatureBlockEntry belladonnaCrop = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry mandrakeCrop = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry waterArtichokeCrop = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry snowbellCrop = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry emberMoss = new NatureBlockEntry(4, 20);
        public final NatureBlockEntry mysticLeaves = new NatureBlockEntry(4, 50);
        public final NatureBlockEntry mysticLogs = new NatureBlockEntry(3, 100);
        public final NatureBlockEntry spanishMoss = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry glintWeed = new NatureBlockEntry(2, 20);
        public final NatureBlockEntry critterSnare = new NatureBlockEntry(2, 10);
        public final NatureBlockEntry bloodRose = new NatureBlockEntry(2, 10);
        public final NatureBlockEntry grassper = new NatureBlockEntry(2, 10);
        public final NatureBlockEntry wispyCotton = new NatureBlockEntry(3, 20);
        public final NatureBlockEntry demonHeart = new NatureBlockEntry(40, 2);
        public final NatureBlockEntry dragonEgg = new NatureBlockEntry(250, 1);
        public final NatureBlockEntry infiniteEgg = new NatureBlockEntry(1000, 1);
        public final NatureBlockEntry genericModFlora = new NatureBlockEntry(2, 4);

        public int calculateTotalPower() {
            return saplings.calculatePower()
                    + commonLogs.calculatePower()
                    + commonLeaves.calculatePower()
                    + grassBlock.calculatePower()
                    + dirt.calculatePower()
                    + farmland.calculatePower()
                    + shortGrassFern.calculatePower()
                    + dandelion.calculatePower()
                    + smallFlowers.calculatePower()
                    + doublePlants.calculatePower()
                    + stillWater.calculatePower()
                    + brownMushroom.calculatePower()
                    + redMushroom.calculatePower()
                    + hugeBrownMushroom.calculatePower()
                    + hugeRedMushroom.calculatePower()
                    + pumpkin.calculatePower()
                    + pumpkinStem.calculatePower()
                    + melon.calculatePower()
                    + melonStem.calculatePower()
                    + cactus.calculatePower()
                    + sugarCane.calculatePower()
                    + vine.calculatePower()
                    + lilyPad.calculatePower()
                    + cocoa.calculatePower()
                    + wheatCrop.calculatePower()
                    + carrotCrop.calculatePower()
                    + belladonnaCrop.calculatePower()
                    + mandrakeCrop.calculatePower()
                    + waterArtichokeCrop.calculatePower()
                    + snowbellCrop.calculatePower()
                    + emberMoss.calculatePower()
                    + mysticLeaves.calculatePower()
                    + mysticLogs.calculatePower()
                    + spanishMoss.calculatePower()
                    + glintWeed.calculatePower()
                    + critterSnare.calculatePower()
                    + bloodRose.calculatePower()
                    + grassper.calculatePower()
                    + wispyCotton.calculatePower()
                    + demonHeart.calculatePower()
                    + dragonEgg.calculatePower()
                    + infiniteEgg.calculatePower()
                    + genericModFlora.calculatePower();
        }

        public void processBlock(BlockState state, FluidState fluid) {
            // Still water
            if (fluid.is(Fluids.WATER) && fluid.isSource()) {
                stillWater.add();
                return;
            }

            if (state.isAir()) return;

            // Mystic Mod Blocks Check
            if (state.is(ModBlocks.ROWAN_LOG.get()) || state.is(ModBlocks.ROWAN_WOOD.get())
                    || state.is(ModBlocks.ALDER_LOG.get()) || state.is(ModBlocks.ALDER_WOOD.get())
                    || state.is(ModBlocks.HAWTHORN_LOG.get()) || state.is(ModBlocks.HAWTHORN_WOOD.get())) {
                mysticLogs.add();
                return;
            }

            if (state.is(ModBlocks.ROWAN_LEAVES.get()) || state.is(ModBlocks.ALDER_LEAVES.get()) || state.is(ModBlocks.HAWTHORN_LEAVES.get())) {
                mysticLeaves.add();
                return;
            }

            if (state.is(ModBlocks.BELLADONNA_CROP.get())) { belladonnaCrop.add(); return; }
            if (state.is(ModBlocks.MANDRAKE_CROP.get())) { mandrakeCrop.add(); return; }
            if (state.is(ModBlocks.WATER_ARTICHOKE_CROP.get())) { waterArtichokeCrop.add(); return; }
            if (state.is(ModBlocks.SNOWBELL_CROP.get())) { snowbellCrop.add(); return; }
            if (state.is(ModBlocks.EMBER_MOSS.get())) { emberMoss.add(); return; }
            if (state.is(ModBlocks.SPANISH_MOSS.get())) { spanishMoss.add(); return; }
            if (state.is(ModBlocks.GLINT_WEED.get())) { glintWeed.add(); return; }
            if (state.is(ModBlocks.CRITTER_SNARE.get())) { critterSnare.add(); return; }
            if (state.is(ModBlocks.BLOOD_ROSE.get())) { bloodRose.add(); return; }
            if (state.is(ModBlocks.GRASSPER.get())) { grassper.add(); return; }
            if (state.is(ModBlocks.WISPY_COTTON.get())) { wispyCotton.add(); return; }
            if (state.is(ModBlocks.INFINITE_EGG_BLOCK.get())) { infiniteEgg.add(); return; }

            // Vanilla Blocks
            if (state.is(Blocks.DRAGON_EGG)) { dragonEgg.add(); return; }
            if (state.is(Blocks.GRASS_BLOCK)) { grassBlock.add(); return; }
            if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MUD)) { dirt.add(); return; }
            if (state.is(Blocks.FARMLAND)) { farmland.add(); return; }
            if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN)) { shortGrassFern.add(); return; }
            if (state.is(Blocks.DANDELION)) { dandelion.add(); return; }
            if (state.is(Blocks.POPPY) || state.is(Blocks.BLUE_ORCHID) || state.is(Blocks.ALLIUM)
                    || state.is(Blocks.AZURE_BLUET) || state.is(Blocks.RED_TULIP) || state.is(Blocks.ORANGE_TULIP)
                    || state.is(Blocks.WHITE_TULIP) || state.is(Blocks.PINK_TULIP) || state.is(Blocks.OXEYE_DAISY)
                    || state.is(Blocks.CORNFLOWER) || state.is(Blocks.LILY_OF_THE_VALLEY) || state.is(Blocks.TORCHFLOWER)
                    || state.is(Blocks.WITHER_ROSE)) {
                smallFlowers.add();
                return;
            }
            if (state.getBlock() instanceof DoublePlantBlock) {
                // Avoid double counting tall plants
                if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                    doublePlants.add();
                }
                return;
            }
            if (state.is(Blocks.BROWN_MUSHROOM)) { brownMushroom.add(); return; }
            if (state.is(Blocks.RED_MUSHROOM)) { redMushroom.add(); return; }
            if (state.is(Blocks.BROWN_MUSHROOM_BLOCK)) { hugeBrownMushroom.add(); return; }
            if (state.is(Blocks.RED_MUSHROOM_BLOCK)) { hugeRedMushroom.add(); return; }
            if (state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN)) { pumpkin.add(); return; }
            if (state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.ATTACHED_PUMPKIN_STEM)) { pumpkinStem.add(); return; }
            if (state.is(Blocks.MELON)) { melon.add(); return; }
            if (state.is(Blocks.MELON_STEM) || state.is(Blocks.ATTACHED_MELON_STEM)) { melonStem.add(); return; }
            if (state.is(Blocks.CACTUS)) { cactus.add(); return; }
            if (state.is(Blocks.SUGAR_CANE)) { sugarCane.add(); return; }
            if (state.is(Blocks.VINE)) { vine.add(); return; }
            if (state.is(Blocks.LILY_PAD)) { lilyPad.add(); return; }
            if (state.is(Blocks.COCOA)) { cocoa.add(); return; }
            if (state.is(Blocks.WHEAT)) { wheatCrop.add(); return; }
            if (state.is(Blocks.CARROTS)) { carrotCrop.add(); return; }

            // Tags
            if (state.is(BlockTags.SAPLINGS)) { saplings.add(); return; }
            if (state.is(BlockTags.LEAVES)) { commonLeaves.add(); return; }
            if (state.is(BlockTags.LOGS)) { commonLogs.add(); return; }

            // Generic mod flora
            if (state.is(BlockTags.CROPS) || state.is(BlockTags.FLOWERS)) {
                genericModFlora.add();
            }
        }
    }

    public static int scanNatureVolume(Level level, BlockPos corePos) {
        NatureMatrix matrix = new NatureMatrix();
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        int minX = corePos.getX() - 14;
        int maxX = corePos.getX() + 14;
        int minY = Math.max(level.getMinBuildHeight(), corePos.getY() - 14);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, corePos.getY() + 14);
        int minZ = corePos.getZ() - 14;
        int maxZ = corePos.getZ() + 14;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mPos.set(x, y, z);
                    BlockState state = level.getBlockState(mPos);
                    FluidState fluid = level.getFluidState(mPos);
                    matrix.processBlock(state, fluid);
                }
            }
        }

        return matrix.calculateTotalPower();
    }
}
