package com.occultum.common.machine.cauldron;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModDataComponentTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class WitchesCauldronBlockEntity extends BlockEntity {

    public static final int MAX_WATER_MB = 3000;
    public static final int BOIL_TICKS_REQUIRED = 100;
    public static final int POWER_DEFICIT_TIMEOUT_TICKS = 400;

    public record TransmutationRecipe(
            List<Supplier<Item>> ingredients,
            int altarPower,
            Supplier<ItemStack> result
    ) {}

    private static List<TransmutationRecipe> TRANSMUTATIONS;

    public static List<TransmutationRecipe> getTransmutations() {
        if (TRANSMUTATIONS == null) {
            List<TransmutationRecipe> list = new ArrayList<>();
            // Instant Boiling Cooking (0 Altar cost)
            list.add(new TransmutationRecipe(List.of(() -> Items.PORKCHOP), 0, () -> new ItemStack(Items.COOKED_PORKCHOP)));
            list.add(new TransmutationRecipe(List.of(() -> Items.BEEF), 0, () -> new ItemStack(Items.COOKED_BEEF)));
            list.add(new TransmutationRecipe(List.of(() -> Items.CHICKEN), 0, () -> new ItemStack(Items.COOKED_CHICKEN)));
            list.add(new TransmutationRecipe(List.of(ModItems.ODD_PORKCHOP_RAW::get), 0, () -> new ItemStack(ModItems.ODD_PORKCHOP_COOKED.get())));
            list.add(new TransmutationRecipe(List.of(() -> Items.MUTTON), 0, () -> new ItemStack(Items.COOKED_MUTTON)));
            list.add(new TransmutationRecipe(List.of(ModItems.WITCH_HAND::get), 0, () -> new ItemStack(Items.ROTTEN_FLESH, 6)));

            // Chalks
            list.add(new TransmutationRecipe(List.of(
                    ModItems.RITUAL_CHALK::get, () -> Items.NETHER_WART, ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.ENDER_PEARL
            ), 2000, () -> new ItemStack(ModItems.OTHERWHERE_CHALK.get())));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.RITUAL_CHALK::get, ModItems.MANDRAKE_ROOT::get, () -> Items.GLISTERING_MELON_SLICE
            ), 0, () -> new ItemStack(ModItems.GOLDEN_CHALK.get())));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.RITUAL_CHALK::get, ModItems.MANDRAKE_ROOT::get, () -> Items.GOLD_NUGGET
            ), 2000, () -> new ItemStack(ModItems.GOLDEN_CHALK.get())));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.RITUAL_CHALK::get, () -> Items.NETHER_WART, () -> Items.BLAZE_POWDER
            ), 0, () -> new ItemStack(ModItems.INFERNAL_CHALK.get())));

            // Mutandis & Mutandis Extremis
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.BONE, ModItems.MANDRAKE_ROOT::get, ModItems.EXHALE_OF_THE_HORNED_ONE::get
            ), 0, () -> new ItemStack(ModItems.MUTANDIS.get(), 6)));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.EGG, ModItems.MANDRAKE_ROOT::get, ModItems.EXHALE_OF_THE_HORNED_ONE::get
            ), 0, () -> new ItemStack(ModItems.MUTANDIS.get(), 6)));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.MUTANDIS::get, () -> Items.NETHER_WART
            ), 0, () -> new ItemStack(ModItems.MUTANDIS_EXTREMIS.get())));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.MUTANDIS::get, ModItems.MANDRAKE_ROOT::get, ModItems.TEAR_OF_THE_GODDESS::get,
                    ModItems.DIAMOND_VAPOR::get, () -> Items.ENDER_PEARL, () -> Items.WHEAT
            ), 0, () -> new ItemStack(ModItems.MUTANDIS_EXTREMIS.get())));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.MUTANDIS_EXTREMIS::get, ModItems.MANDRAKE_ROOT::get, () -> Items.STONE, () -> Items.END_STONE
            ), 1800, () -> new ItemStack(Items.END_STONE, 2)));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.MUTANDIS_EXTREMIS::get, ModItems.MANDRAKE_ROOT::get, () -> Items.SMOOTH_STONE, () -> Items.END_STONE
            ), 1800, () -> new ItemStack(Items.END_STONE, 2)));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.MUTANDIS_EXTREMIS::get, () -> Items.NETHER_WART, ModItems.ENT_TWIG::get
            ), 1800, () -> new ItemStack(ModItems.MUTATING_SPRIG.get())));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.MUTANDIS_EXTREMIS::get, ModItems.MANDRAKE_ROOT::get, () -> Items.NETHER_WART,
                    ModItems.TEAR_OF_THE_GODDESS::get, ModItems.REFINED_EVIL::get
            ), 1800, () -> new ItemStack(ModItems.DROP_OF_LUCK.get())));

            // Endless Water Brews (4 tiers)
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.LILY_PAD, () -> Items.NETHER_WART, () -> Items.QUARTZ, () -> Items.LAVA_BUCKET, ModItems.CHARGED_ATTUNED_STONE::get
            ), 3000, () -> new ItemStack(ModItems.ENDLESS_WATER_BREW.get())));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.LILY_PAD, () -> Items.NETHER_WART, () -> Items.QUARTZ, () -> Items.LAVA_BUCKET
            ), 3000, () -> new ItemStack(ModItems.ENDLESS_WATER_BREW.get())));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.LILY_PAD, () -> Items.NETHER_WART, () -> Items.QUARTZ
            ), 3000, () -> new ItemStack(ModItems.ENDLESS_WATER_BREW.get())));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.LILY_PAD, () -> Items.NETHER_WART
            ), 3000, () -> new ItemStack(ModItems.ENDLESS_WATER_BREW.get())));

            // Continuous Fuel Brews (4 tiers)
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.CHARCOAL, ModItems.MANDRAKE_ROOT::get, () -> Items.GLOWSTONE_DUST, () -> Items.BLAZE_ROD, ModItems.CHARGED_ATTUNED_STONE::get
            ), 0, () -> new ItemStack(ModItems.CONTINUOUS_FUEL_BREW.get(), 4)));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.CHARCOAL, ModItems.MANDRAKE_ROOT::get, () -> Items.GLOWSTONE_DUST, () -> Items.BLAZE_ROD
            ), 0, () -> new ItemStack(ModItems.CONTINUOUS_FUEL_BREW.get(), 3)));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.CHARCOAL, ModItems.MANDRAKE_ROOT::get, () -> Items.GLOWSTONE_DUST
            ), 0, () -> new ItemStack(ModItems.CONTINUOUS_FUEL_BREW.get(), 2)));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.CHARCOAL, ModItems.MANDRAKE_ROOT::get
            ), 0, () -> new ItemStack(ModItems.CONTINUOUS_FUEL_BREW.get(), 1)));

            // Pit Grass, Pit Dirt & Player Compass
            list.add(new TransmutationRecipe(List.of(
                    ModItems.TORMENTED_TWINE::get, () -> Items.NETHER_WART, () -> Items.DIRT, () -> Items.DANDELION
            ), 4000, () -> new ItemStack(ModBlocks.PIT_GRASS.get(), 4)));
            list.add(new TransmutationRecipe(List.of(
                    ModItems.TORMENTED_TWINE::get, ModItems.MANDRAKE_ROOT::get, () -> Items.DIRT
            ), 4000, () -> new ItemStack(ModBlocks.PIT_DIRT.get(), 4)));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.COMPASS, () -> Items.NETHER_WART, ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.VINE, () -> Items.SPIDER_EYE
            ), 5000, () -> new ItemStack(ModItems.PLAYER_COMPASS.get())));
            list.add(new TransmutationRecipe(List.of(
                    () -> Items.COMPASS, () -> Items.NETHER_WART, ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.VINE, () -> Items.GLOWSTONE_DUST
            ), 5000, () -> new ItemStack(ModItems.PLAYER_COMPASS.get())));

            TRANSMUTATIONS = list;
        }
        return TRANSMUTATIONS;
    }

    private int waterMb = 0;
    private int boilTicks = 0;
    private int deficitTicks = 0;
    private final List<Item> thrownHistory = new ArrayList<>();

    // Custom Alchemy State
    private int maxCapacity = 0;
    private int usedCapacity = 0;
    private int pendingPowerMod = 0;
    private int pendingDurationMod = 0;
    private boolean powerCeilingUnlocked = false;
    private boolean pendingInvert = false;
    private int yieldPenalty = 0;
    private int accumulatedAltarCost = 0;
    private CauldronPotionData.DispersalType dispersal = CauldronPotionData.DispersalType.DRINKABLE;
    private int extentBonus = 0;
    private int lingerBonus = 0;
    private boolean disableBlockEffects = false;
    private boolean disableEntityEffects = false;
    private boolean noParticles = false;
    private int drinkSpeedReduction = 0;
    private int colorRgb = 0x3A6EE8;
    private final List<CauldronPotionData.EffectEntry> effects = new ArrayList<>();

    public WitchesCauldronBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WITCHES_CAULDRON.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitchesCauldronBlockEntity be) {
        boolean hasFire = be.hasHeatSourceBelow(level, pos);
        if (be.waterMb > 0 && hasFire) {
            if (be.boilTicks < BOIL_TICKS_REQUIRED) {
                be.boilTicks++;
                if (be.boilTicks == BOIL_TICKS_REQUIRED) {
                    be.sync();
                }
            } else if (level.getGameTime() % 10 == 0 && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, be.colorRgb),
                        pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, 4, 0.25, 0.05, 0.25, 0.02);
            }
        } else if (be.boilTicks > 0) {
            be.boilTicks = 0;
            be.sync();
        }

        // Check pending transmutation or deficit timeout (Section 7.2)
        if (be.isBoiling() && !be.thrownHistory.isEmpty()) {
            Optional<TransmutationRecipe> exact = be.findExactTransmutation();
            if (exact.isPresent()) {
                TransmutationRecipe recipe = exact.get();
                int scaledCost = be.calculateScaledPowerCost(recipe.altarPower());
                if (scaledCost == 0 || AltarPowerNetwork.consumePower(level, pos, scaledCost)) {
                    be.completeTransmutation((ServerLevel) level, recipe.result().get());
                } else {
                    be.deficitTicks++;
                    if (be.deficitTicks >= POWER_DEFICIT_TIMEOUT_TICKS) {
                        Item last = be.thrownHistory.get(be.thrownHistory.size() - 1);
                        ItemEntity dropped = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, new ItemStack(last));
                        level.addFreshEntity(dropped);
                        be.boilTicks = 0;
                        be.resetAlchemyState();
                        be.sync();
                    }
                }
            }
        }
    }

    private boolean hasHeatSourceBelow(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(BlockTags.FIRE) || below.is(Blocks.MAGMA_BLOCK) || below.is(Blocks.LAVA)) {
            return true;
        }
        if (below.getBlock() instanceof CampfireBlock) {
            return below.getValue(CampfireBlock.LIT);
        }
        return false;
    }

    public boolean isBoiling() {
        return waterMb >= MAX_WATER_MB && boilTicks >= BOIL_TICKS_REQUIRED;
    }

    public boolean tryAcceptThrownItem(ItemStack stack, @Nullable Player thrower) {
        if (level == null || level.isClientSide || !isBoiling()) {
            return false;
        }
        Item item = stack.getItem();

        // 1. Gypsum: Instant Clean & Drain (Section 5.2)
        if (item == ModItems.GYPSUM.get()) {
            stack.shrink(1);
            waterMb = 0;
            boilTicks = 0;
            resetAlchemyState();
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 20, 0.25, 0.2, 0.25, 0.05);
            }
            sync();
            return true;
        }

        // 2. Quicklime: Violent Alchemical Detonation (Section 5.2)
        if (item == ModItems.QUICKLIME.get()) {
            stack.shrink(1);
            triggerAlchemicalExplosion(thrower, true);
            return true;
        }

        // 3. Coven Witch Summon Ritual (Section 6.3: Witches Hat + 10,000 Altar Power)
        if (item == ModItems.WITCHES_HAT.get() && thrownHistory.isEmpty() && effects.isEmpty()) {
            int cost = calculateScaledPowerCost(10000);
            if (AltarPowerNetwork.consumePower(level, worldPosition, cost)) {
                stack.shrink(1);
                Witch witch = EntityType.WITCH.create(level);
                if (witch != null && level instanceof ServerLevel sl) {
                    witch.moveTo(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, 0, 0);
                    witch.finalizeSpawn(sl, sl.getCurrentDifficultyAt(worldPosition), MobSpawnType.EVENT, null);
                    sl.addFreshEntity(witch);
                }
                performMisfortuneRoll((ServerLevel) level);
                waterMb = 0;
                boilTicks = 0;
                resetAlchemyState();
                sync();
                return true;
            }
        }

        // Record in thrown history and check if it matches a transmutation prefix
        thrownHistory.add(item);
        stack.shrink(1);
        level.playSound(null, worldPosition, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.7F, 1.2F);

        Optional<TransmutationRecipe> exact = findExactTransmutation();
        if (exact.isPresent()) {
            TransmutationRecipe recipe = exact.get();
            int scaledCost = calculateScaledPowerCost(recipe.altarPower());
            if (scaledCost == 0 || AltarPowerNetwork.consumePower(level, worldPosition, scaledCost)) {
                completeTransmutation((ServerLevel) level, recipe.result().get());
            }
            return true;
        }

        // Process Custom Alchemy Modifiers & Effects (Section 3 & 5)
        processAlchemyIngredient(item, thrower);
        sync();
        return true;
    }

    private void processAlchemyIngredient(Item item, @Nullable Player thrower) {
        // 3.1 Capacity Catalysts
        if (item == ModItems.MANDRAKE_ROOT.get() && maxCapacity < 1) {
            maxCapacity = Math.min(1, maxCapacity + 1);
            colorRgb = 0x7A4CE0;
            return;
        }
        if (item == Items.NETHER_WART && maxCapacity < 2) {
            maxCapacity = Math.min(2, maxCapacity + 2);
            accumulatedAltarCost += 50;
            colorRgb = 0xA82040;
            return;
        }
        if (item == ModItems.TEAR_OF_THE_GODDESS.get() && maxCapacity < 4) {
            maxCapacity = Math.min(4, maxCapacity + 2);
            accumulatedAltarCost += 100;
            colorRgb = 0x40B8F0;
            return;
        }
        if (item == ModItems.DIAMOND_VAPOR.get() && maxCapacity < 6) {
            maxCapacity = Math.min(6, maxCapacity + 2);
            accumulatedAltarCost += 150;
            colorRgb = 0x60F0E8;
            return;
        }
        if (item == Items.DIAMOND && maxCapacity < 8) {
            maxCapacity = Math.min(8, maxCapacity + 2);
            accumulatedAltarCost += 150;
            yieldPenalty += 2;
            colorRgb = 0x40FFFF;
            return;
        }
        if (item == Items.NETHER_STAR && maxCapacity < 10) {
            maxCapacity = Math.min(10, maxCapacity + 4);
            accumulatedAltarCost += 150;
            powerCeilingUnlocked = true;
            colorRgb = 0xFFF8B0;
            return;
        }
        if (item == ModItems.KOBOLDITE_PENTACLE.get() && maxCapacity < 16) {
            maxCapacity = Math.min(16, maxCapacity + 6);
            accumulatedAltarCost += 1000;
            colorRgb = 0xFFD030;
            return;
        }

        // 3.2 Power Modifiers
        int maxAllowedPower = powerCeilingUnlocked ? 7 : 3;
        if (item == Items.GLOWSTONE_DUST && pendingPowerMod < 1) {
            pendingPowerMod = Math.min(maxAllowedPower, pendingPowerMod + 1);
            accumulatedAltarCost += 50;
            return;
        }
        if (item == Items.BLAZE_ROD && pendingPowerMod < 2) {
            pendingPowerMod = Math.min(maxAllowedPower, pendingPowerMod + 1);
            accumulatedAltarCost += 100;
            return;
        }
        if (item == ModItems.CHARGED_ATTUNED_STONE.get() && pendingPowerMod < maxAllowedPower) {
            pendingPowerMod = Math.min(maxAllowedPower, pendingPowerMod + 1);
            accumulatedAltarCost += 150;
            return;
        }
        if (item == Items.TROPICAL_FISH) {
            powerCeilingUnlocked = true;
            accumulatedAltarCost += 200;
            return;
        }

        // 3.3 Duration Modifiers
        if (item == Items.REDSTONE && pendingDurationMod < 1) {
            pendingDurationMod = 1;
            accumulatedAltarCost += 50;
            return;
        }
        if (item == Items.OBSIDIAN && pendingDurationMod < 2) {
            pendingDurationMod = 2;
            accumulatedAltarCost += 100;
            return;
        }
        if (item == ModItems.MINDRAKE_SEEDS.get() && pendingDurationMod < 3) {
            pendingDurationMod = 3;
            accumulatedAltarCost += 150;
            return;
        }

        // 3.4 Dispersal Modifiers
        if (item == Items.GUNPOWDER) {
            dispersal = CauldronPotionData.DispersalType.SPLASH;
            return;
        }
        if (item == ModItems.WOOL_OF_BAT.get()) {
            dispersal = CauldronPotionData.DispersalType.GAS;
            return;
        }
        if (item == ModItems.WORMWOOD.get()) {
            dispersal = CauldronPotionData.DispersalType.LIQUID;
            return;
        }
        if (item == Items.ZOMBIE_HEAD) {
            dispersal = CauldronPotionData.DispersalType.TRIGGER;
            return;
        }
        if (item == ModItems.WATER_ARTICHOKE_GLOBE.get()) {
            dispersal = CauldronPotionData.DispersalType.DRINKABLE;
            return;
        }

        // 3.5 Extent & Linger Modifiers
        if (item == ModItems.WOOD_ASH.get() && extentBonus < 1) {
            extentBonus = 1;
            accumulatedAltarCost += 50;
            return;
        }
        if (item == Items.COCOA_BEANS && extentBonus < 2) {
            extentBonus = 2;
            accumulatedAltarCost += 100;
            return;
        }
        if (item == ModItems.WISPY_COTTON.get() && extentBonus < 3) {
            extentBonus = 3;
            accumulatedAltarCost += 150;
            return;
        }
        if (item == ModItems.BELLADONNA_FLOWER.get() && lingerBonus < 1) {
            lingerBonus = 1;
            accumulatedAltarCost += 50;
            return;
        }
        if (item == Items.LAPIS_LAZULI && lingerBonus < 2) {
            lingerBonus = 2;
            accumulatedAltarCost += 100;
            return;
        }
        if (item == Items.END_STONE && lingerBonus < 3) {
            lingerBonus = 3;
            accumulatedAltarCost += 150;
            return;
        }

        // 3.6 Target Filters & Adjustments
        if (item == Items.NETHER_BRICK) {
            disableBlockEffects = true;
            accumulatedAltarCost += 50;
            return;
        }
        if (item == Items.BRICK) {
            disableEntityEffects = true;
            accumulatedAltarCost += 50;
            return;
        }
        if (item == Items.FERMENTED_SPIDER_EYE) {
            pendingInvert = !pendingInvert;
            accumulatedAltarCost += 25;
            return;
        }
        if (item == Items.GOLD_NUGGET) {
            noParticles = true;
            accumulatedAltarCost += 50;
            return;
        }
        int woolRgb = getWoolColorRgb(item);
        if (woolRgb != -1) {
            colorRgb = woolRgb;
            return;
        }

        // 3.7 Accelerated Drinking Speed
        if (item == ModItems.ROWAN_BERRIES.get()) {
            drinkSpeedReduction += 4;
            accumulatedAltarCost += 50;
            return;
        }
        if (item == ModItems.EXHALE_OF_THE_HORNED_ONE.get()) {
            drinkSpeedReduction += 6;
            return;
        }
        if (item == ModItems.SPANISH_MOSS_ITEM.get()) {
            drinkSpeedReduction += 4;
            accumulatedAltarCost += 50;
            return;
        }

        // Section 5: Catalog Effect Lookup
        Optional<CauldronCustomPotionItem.CatalogEffect> catOpt = CauldronCustomPotionItem.findByIngredient(item);
        if (catOpt.isPresent()) {
            CauldronCustomPotionItem.CatalogEffect cat = catOpt.get();
            if (usedCapacity + cat.levelCost() > maxCapacity) {
                // Overfill Alchemical Explosion (Section 7.1)
                triggerAlchemicalExplosion(thrower, false);
                return;
            }
            usedCapacity += cat.levelCost();
            accumulatedAltarCost += cat.altarPower();
            effects.add(new CauldronPotionData.EffectEntry(cat.id(), pendingPowerMod, pendingDurationMod, pendingInvert));
            colorRgb = cat.defaultColor();
            pendingPowerMod = 0;
            pendingDurationMod = 0;
            pendingInvert = false;
        }
    }

    public void tryBottlePotion(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide || !isBoiling() || effects.isEmpty() || waterMb <= 0) {
            return;
        }
        int requiredPower = calculateScaledPowerCost(accumulatedAltarCost);
        if (requiredPower > 0 && !AltarPowerNetwork.consumePower(level, worldPosition, requiredPower)) {
            // Section 7.2: Power deficit pop sound & red/black particles
            level.playSound(null, worldPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 1.0F, 0.6F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.85, worldPosition.getZ() + 0.5, 12, 0.2, 0.1, 0.2, 0.02);
            }
            return;
        }
        // Once initial altar cost is paid on first bottle, subsequent bottles from same batch cost 0
        accumulatedAltarCost = 0;

        int yieldTier = calculatePlayerYieldTier(player);
        int drainPerBottle = switch (yieldTier) {
            case 0 -> 3000;
            case 1 -> 1500;
            case 2 -> 1000;
            case 3 -> 750;
            case 4 -> 600;
            case 5 -> 500;
            case 6 -> 375;
            case 7 -> 300;
            case 8 -> 200;
            default -> 100;
        };

        waterMb = Math.max(0, waterMb - drainPerBottle);
        ItemStack potionStack = new ItemStack(ModItems.CUSTOM_POTION.get());
        CauldronPotionData data = new CauldronPotionData(
                List.copyOf(effects), dispersal, extentBonus, lingerBonus,
                disableBlockEffects, disableEntityEffects, noParticles, drinkSpeedReduction, colorRgb
        );
        potionStack.set(ModDataComponentTypes.CAULDRON_POTION.get(), data);

        // Grant +1 Bottling XP (Section 4.2)
        int currentXp = player.getData(ModAttachmentTypes.BOTTLING_XP.get());
        player.setData(ModAttachmentTypes.BOTTLING_XP.get(), currentXp + 1);

        ItemStack held = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (held.isEmpty()) {
            player.setItemInHand(hand, potionStack);
        } else if (!player.getInventory().add(potionStack)) {
            player.drop(potionStack, false);
        }

        level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (waterMb == 0) {
            boilTicks = 0;
            resetAlchemyState();
        }
        sync();
    }

    /**
     * Exact decision tree for bottling yield tier calculation.
     */
    private int calculatePlayerYieldTier(Player player) {
        int xp = player.getData(ModAttachmentTypes.BOTTLING_XP.get());
        int skillLevel = xp / 30;

        int apparelLevel = 0;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.is(ModItems.BABA_YAGAS_HAT.get())) apparelLevel += 2;
        else if (head.is(ModItems.WITCHES_HAT.get())) apparelLevel += 1;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.is(ModItems.WITCHES_ROBES.get()) || chest.is(ModItems.NECROMANCER_ROBES.get())) {
            apparelLevel += 1;
        }

        MobEffectInstance luckEffect = player.getEffect(MobEffects.LUCK);
        int potionLevel = luckEffect != null ? luckEffect.getAmplifier() + 1 : 0;

        String familiar = player.getData(ModAttachmentTypes.FAMILIAR_TYPE.get());
        boolean hasToad = "toad".equalsIgnoreCase(familiar) || "frog".equalsIgnoreCase(familiar);

        int yieldBase;
        if (skillLevel == 0) {
            yieldBase = 0;
        } else if (skillLevel == 1) {
            yieldBase = (apparelLevel >= 1 || potionLevel >= 1) ? 2 : 1;
        } else {
            if (apparelLevel >= 3 && potionLevel >= 3 && hasToad) yieldBase = 9;
            else if (apparelLevel >= 2 && potionLevel >= 3 && hasToad) yieldBase = 8;
            else if (apparelLevel >= 2 && potionLevel >= 3) yieldBase = 7;
            else if (apparelLevel >= 2 && potionLevel >= 2) yieldBase = 6;
            else if (apparelLevel >= 2 && potionLevel >= 1) yieldBase = 5;
            else if (apparelLevel >= 2 || potionLevel >= 1) yieldBase = 4;
            else if (apparelLevel >= 1 || potionLevel >= 1) yieldBase = 3;
            else yieldBase = 2;
        }

        return Math.max(0, yieldBase - yieldPenalty);
    }

    /**
     * Section 6.1: Protective Circle cost multiplier around the cauldron.
     */
    public int calculateScaledPowerCost(int baseCost) {
        if (baseCost <= 0 || level == null) return 0;
        CircleAnalysis circles = analyzeCircles();
        double scale = 1.4
                - (circles.smallRitual ? 0.20 : 0.0)
                - (circles.mediumRitual ? 0.20 : 0.0)
                - (circles.smallInfernal ? 0.37 : 0.0)
                - (circles.mediumInfernal ? 0.37 : 0.0);
        return Math.max(0, (int) Math.floor(baseCost * Math.max(0.1, scale)));
    }

    private record CircleAnalysis(boolean smallRitual, boolean mediumRitual, boolean smallInfernal, boolean mediumInfernal, int riskLevel) {}

    private CircleAnalysis analyzeCircles() {
        if (level == null) return new CircleAnalysis(false, false, false, false, 1);
        Block smallBlock = level.getBlockState(worldPosition.offset(3, 0, 0)).getBlock();
        Block medBlock = level.getBlockState(worldPosition.offset(5, 0, 0)).getBlock();

        boolean smallRitual = smallBlock == ModBlocks.RITUAL_CHALK_BLOCK.get();
        boolean mediumRitual = medBlock == ModBlocks.RITUAL_CHALK_BLOCK.get();
        boolean smallInfernal = smallBlock == ModBlocks.INFERNAL_CHALK_BLOCK.get();
        boolean mediumInfernal = medBlock == ModBlocks.INFERNAL_CHALK_BLOCK.get();

        int risk = 0;
        if (smallInfernal) risk++;
        if (mediumInfernal) risk++;
        if (!smallRitual && !mediumRitual) risk++;
        return new CircleAnalysis(smallRitual, mediumRitual, smallInfernal, mediumInfernal, risk);
    }

    /**
     * Section 6.2: The Misfortune Roll.
     */
    public void performMisfortuneRoll(ServerLevel serverLevel) {
        CircleAnalysis circles = analyzeCircles();
        if (circles.riskLevel <= 0) return;

        double roll = serverLevel.random.nextDouble() * (1.0 + (circles.riskLevel - 1) * 0.2);
        if (roll < 0.50) return;

        AABB area = new AABB(worldPosition).inflate(16.0);
        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, area);

        if (roll < 0.75) {
            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 0));
            }
        } else if (roll < 0.90) {
            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 5));
            }
        } else if (roll < 0.98) {
            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 3600, 1));
                e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 3600, 1));
            }
        } else {
            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
                List<MobEffectInstance> buffs = e.getActiveEffects().stream()
                        .filter(inst -> inst.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL)
                        .toList();
                buffs.forEach(inst -> e.removeEffect(inst.getEffect()));
            }
            int bolts = 2 + serverLevel.random.nextInt(3);
            for (int i = 0; i < bolts; i++) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    double dx = (serverLevel.random.nextDouble() - 0.5) * 8.0;
                    double dz = (serverLevel.random.nextDouble() - 0.5) * 8.0;
                    bolt.moveTo(worldPosition.getX() + 0.5 + dx, worldPosition.getY(), worldPosition.getZ() + 0.5 + dz);
                    serverLevel.addFreshEntity(bolt);
                }
            }
        }
    }

    private void triggerAlchemicalExplosion(@Nullable Player nearest, boolean quicklime) {
        if (level == null || level.isClientSide) return;
        ServerLevel sl = (ServerLevel) level;
        sl.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 0.9F);
        sl.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 40, 0.5, 0.5, 0.5, 0.15);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.1);

        Player victim = nearest != null ? nearest : sl.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 5.0, false);
        if (victim != null) {
            float dmg = 6.0F + sl.random.nextFloat() * 4.0F;
            victim.hurt(sl.damageSources().magic(), dmg);
            Vec3 knock = victim.position().subtract(Vec3.atCenterOf(worldPosition)).normalize().scale(1.2);
            victim.setDeltaMovement(knock.x, 0.45, knock.z);
            victim.hurtMarked = true;
            if (!quicklime) {
                for (CauldronPotionData.EffectEntry entry : effects) {
                    CauldronCustomPotionItem.applyEffectToEntity(sl, null, victim,
                            new CauldronPotionData.EffectEntry(entry.effectId(), entry.power(), 0, true), false);
                }
            }
        }

        waterMb = 0;
        boilTicks = 0;
        resetAlchemyState();
        sync();
    }

    private Optional<TransmutationRecipe> findExactTransmutation() {
        for (TransmutationRecipe r : getTransmutations()) {
            if (r.ingredients().size() == thrownHistory.size()) {
                boolean match = true;
                for (int i = 0; i < r.ingredients().size(); i++) {
                    if (r.ingredients().get(i).get() != thrownHistory.get(i)) {
                        match = false;
                        break;
                    }
                }
                if (match) return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    private void completeTransmutation(ServerLevel serverLevel, ItemStack output) {
        ItemEntity spawned = new ItemEntity(serverLevel, worldPosition.getX() + 0.5, worldPosition.getY() + 1.15, worldPosition.getZ() + 0.5, output.copy());
        spawned.setDeltaMovement(0.0, 0.25, 0.0);
        serverLevel.addFreshEntity(spawned);
        serverLevel.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 1.2F);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, worldPosition.getX() + 0.5, worldPosition.getY() + 0.9, worldPosition.getZ() + 0.5, 20, 0.3, 0.2, 0.3, 0.05);
        performMisfortuneRoll(serverLevel);

        waterMb = 0;
        boilTicks = 0;
        resetAlchemyState();
        sync();
    }

    public void resetAlchemyState() {
        thrownHistory.clear();
        deficitTicks = 0;
        maxCapacity = 0;
        usedCapacity = 0;
        pendingPowerMod = 0;
        pendingDurationMod = 0;
        powerCeilingUnlocked = false;
        pendingInvert = false;
        yieldPenalty = 0;
        accumulatedAltarCost = 0;
        dispersal = CauldronPotionData.DispersalType.DRINKABLE;
        extentBonus = 0;
        lingerBonus = 0;
        disableBlockEffects = false;
        disableEntityEffects = false;
        noParticles = false;
        drinkSpeedReduction = 0;
        colorRgb = 0x3A6EE8;
        effects.clear();
    }

    /**
     * Section 2.3: Redstone Comparator Analog Output (0, 3, 6, 9, 12, 15).
     */
    public int getComparatorSignal() {
        if (waterMb <= 0) return 0;
        if (!isBoiling()) return 3;
        if (thrownHistory.isEmpty() && effects.isEmpty()) return 6;
        if (deficitTicks > 0) return 9;
        if (maxCapacity > 0 && usedCapacity >= maxCapacity) return 15;
        return 12;
    }

    public int getWaterMb() {
        return waterMb;
    }

    public void addWater(int mb) {
        this.waterMb = Math.min(MAX_WATER_MB, this.waterMb + mb);
        sync();
    }

    public int getColorRgb() {
        return colorRgb;
    }

    private static int getWoolColorRgb(Item item) {
        if (item == Items.WHITE_WOOL) return DyeColor.WHITE.getTextColor();
        if (item == Items.ORANGE_WOOL) return DyeColor.ORANGE.getTextColor();
        if (item == Items.MAGENTA_WOOL) return DyeColor.MAGENTA.getTextColor();
        if (item == Items.LIGHT_BLUE_WOOL) return DyeColor.LIGHT_BLUE.getTextColor();
        if (item == Items.YELLOW_WOOL) return DyeColor.YELLOW.getTextColor();
        if (item == Items.LIME_WOOL) return DyeColor.LIME.getTextColor();
        if (item == Items.PINK_WOOL) return DyeColor.PINK.getTextColor();
        if (item == Items.GRAY_WOOL) return DyeColor.GRAY.getTextColor();
        if (item == Items.LIGHT_GRAY_WOOL) return DyeColor.LIGHT_GRAY.getTextColor();
        if (item == Items.CYAN_WOOL) return DyeColor.CYAN.getTextColor();
        if (item == Items.PURPLE_WOOL) return DyeColor.PURPLE.getTextColor();
        if (item == Items.BLUE_WOOL) return DyeColor.BLUE.getTextColor();
        if (item == Items.BROWN_WOOL) return DyeColor.BROWN.getTextColor();
        if (item == Items.GREEN_WOOL) return DyeColor.GREEN.getTextColor();
        if (item == Items.RED_WOOL) return DyeColor.RED.getTextColor();
        if (item == Items.BLACK_WOOL) return DyeColor.BLACK.getTextColor();
        return -1;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("WaterMb", waterMb);
        tag.putInt("BoilTicks", boilTicks);
        tag.putInt("MaxCapacity", maxCapacity);
        tag.putInt("UsedCapacity", usedCapacity);
        tag.putInt("AccumulatedCost", accumulatedAltarCost);
        tag.putInt("ColorRgb", colorRgb);
        ListTag hist = new ListTag();
        for (Item item : thrownHistory) {
            hist.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        tag.put("History", hist);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        waterMb = tag.getInt("WaterMb");
        boilTicks = tag.getInt("BoilTicks");
        maxCapacity = tag.getInt("MaxCapacity");
        usedCapacity = tag.getInt("UsedCapacity");
        accumulatedAltarCost = tag.getInt("AccumulatedCost");
        colorRgb = tag.contains("ColorRgb") ? tag.getInt("ColorRgb") : 0x3A6EE8;
        thrownHistory.clear();
        if (tag.contains("History", Tag.TAG_LIST)) {
            ListTag hist = tag.getList("History", Tag.TAG_STRING);
            for (int i = 0; i < hist.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(hist.getString(i));
                if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                    thrownHistory.add(BuiltInRegistries.ITEM.get(id));
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
