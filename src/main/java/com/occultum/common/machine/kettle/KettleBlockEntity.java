package com.occultum.common.machine.kettle;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class KettleBlockEntity extends BlockEntity {
    public static final int SLOT_OUT = 6;
    public static final int SLOT_BOTTLES = 7;
    public static final int MAX_WATER_MB = 1000;

    // 8 posições internas (0..5: ingredientes sequenciais, 6: lote de saída, 7: garrafas de vidro)
    private NonNullList<ItemStack> items = NonNullList.withSize(8, ItemStack.EMPTY);
    private int ingredientCount = 0;
    private int waterAmount = 0;
    private boolean isRuined = false;
    private boolean isReady = false;
    private boolean awaitingAltarPower = false;
    private int liquidColor = 0x3F76E4;
    private int requiredPower = 0;
    private boolean requiresBottle = true;

    public record KettleRecipe(
            List<Supplier<Item>> ingredients,
            Supplier<ItemStack> resultBatch,
            int altarPower,
            int hexColor,
            boolean requiresBottle
    ) {
        public boolean matchesPrefix(List<ItemStack> currentInputs) {
            if (currentInputs.size() > ingredients.size()) return false;
            for (int i = 0; i < currentInputs.size(); i++) {
                if (!currentInputs.get(i).is(ingredients.get(i).get())) {
                    return false;
                }
            }
            return true;
        }

        public boolean matchesFull(List<ItemStack> currentInputs) {
            return currentInputs.size() == ingredients.size() && matchesPrefix(currentInputs);
        }
    }

    private static List<KettleRecipe> RECIPES;

    public static List<KettleRecipe> getRecipes() {
        if (RECIPES == null) {
            RECIPES = List.of(
                    // 1. Caldo de Vinhas (Brew of Vines) - 3 Frascos, 0 poder, #009947
                    new KettleRecipe(
                            List.of(() -> Items.VINE, () -> Items.BROWN_MUSHROOM, () -> Items.RED_MUSHROOM,
                                    ModItems.DOG_TONGUE::get, () -> Items.SPIDER_EYE, ModItems.REEK_OF_MISFORTUNE::get),
                            () -> new ItemStack(ModItems.BREW_OF_VINES.get(), 3), 0, 0x009947, true
                    ),
                    // 2. Caldo de Teias (Brew of Webs) - 3 Frascos, 0 poder, #FFFFFF
                    new KettleRecipe(
                            List.of(() -> Items.COBWEB, () -> Items.BROWN_MUSHROOM, ModItems.WOOL_OF_BAT::get,
                                    () -> Items.DANDELION, ModItems.WHIFF_OF_MAGIC::get, ModItems.BELLADONNA_FLOWER::get),
                            () -> new ItemStack(ModItems.BREW_OF_WEBS.get(), 3), 0, 0xFFFFFF, true
                    ),
                    // 3. Caldo de Espinhos (Brew of Thorns) - 3 Frascos, 0 poder, #66FE00
                    new KettleRecipe(
                            List.of(() -> Items.GREEN_DYE, () -> Items.RED_MUSHROOM, ModItems.OIL_OF_VITRIOL::get,
                                    ModItems.ODOUR_OF_PURITY::get, () -> Items.POPPY, ModItems.MANDRAKE_ROOT::get),
                            () -> new ItemStack(ModItems.BREW_OF_THORNS.get(), 3), 0, 0x66FE00, true
                    ),
                    // 4. Caldo de Tinta (Brew of Ink) - 3 Frascos, 0 poder, #333333
                    new KettleRecipe(
                            List.of(() -> Items.INK_SAC, ModItems.QUICKLIME::get, ModItems.OIL_OF_VITRIOL::get,
                                    () -> Items.IRON_NUGGET, ModItems.BELLADONNA_FLOWER::get, ModItems.ROWAN_BERRIES::get),
                            () -> new ItemStack(ModItems.BREW_OF_INK.get(), 3), 0, 0x333333, true
                    ),
                    // 5. Caldo de Germinação (Brew of Sprouting) - 3 Frascos, 0 poder, #543947
                    new KettleRecipe(
                            List.of(ModItems.ROWAN_SAPLING::get, ModItems.ALDER_SAPLING::get, ModItems.HAWTHORN_SAPLING::get,
                                    ModItems.DOG_TONGUE::get, ModItems.MANDRAKE_ROOT::get, () -> Items.POPPY),
                            () -> new ItemStack(ModItems.BREW_OF_SPROUTING.get(), 3), 0, 0x543947, true
                    ),
                    // 6. Caldo de Erosão (Brew of Erosion) - 3 Frascos, 0 poder, #BC0530
                    new KettleRecipe(
                            List.of(ModItems.OIL_OF_VITRIOL::get, ModItems.OIL_OF_VITRIOL::get, ModItems.QUICKLIME::get,
                                    ModItems.BELLADONNA_FLOWER::get, () -> Items.DANDELION, () -> Items.STONE_BRICKS),
                            () -> new ItemStack(ModItems.BREW_OF_EROSION.get(), 3), 0, 0xBC0530, true
                    ),
                    // 7. Caldo de Reviver (Brew of Raising) - 3 Frascos, 500 poder, #4705A7
                    new KettleRecipe(
                            List.of(ModItems.WOOL_OF_BAT::get, ModItems.MUTANDIS::get, () -> Items.GOLD_INGOT,
                                    ModItems.OIL_OF_VITRIOL::get, () -> Items.GLISTERING_MELON_SLICE, () -> Items.REDSTONE),
                            () -> new ItemStack(ModItems.BREW_OF_RAISING.get(), 3), 500, 0x4705A7, true
                    ),
                    // 8. Caldo Grotesco (Brew Grotesque) - 3 Frascos, 500 poder, #322156
                    new KettleRecipe(
                            List.of(ModItems.MUTANDIS_EXTREMIS::get, ModItems.MANDRAKE_ROOT::get, ModItems.WATER_ARTICHOKE::get,
                                    ModItems.DOG_TONGUE::get, () -> Items.GOLDEN_CARROT, () -> Items.FERMENTED_SPIDER_EYE),
                            () -> new ItemStack(ModItems.BREW_GROTESQUE.get(), 3), 500, 0x322156, true
                    ),
                    // 9. Caldo do Amor (Brew of Love) - 3 Frascos, 0 poder, #FFA604
                    new KettleRecipe(
                            List.of(() -> Items.POPPY, ModItems.WHIFF_OF_MAGIC::get, ModItems.WATER_ARTICHOKE::get,
                                    () -> Items.GOLDEN_APPLE, () -> Items.SUNFLOWER, () -> Items.COCOA_BEANS),
                            () -> new ItemStack(ModItems.BREW_OF_LOVE.get(), 3), 0, 0xFFA604, true
                    ),
                    // 10. Caldo de Gelo (Brew of Ice) - 3 Frascos, 1000 poder, #3104DF
                    new KettleRecipe(
                            List.of(ModItems.ICY_NEEDLE::get, () -> Items.SNOWBALL, ModItems.WATER_ARTICHOKE::get,
                                    () -> Items.QUARTZ, () -> Items.BROWN_MUSHROOM, ModItems.ODOUR_OF_PURITY::get),
                            () -> new ItemStack(ModItems.BREW_OF_ICE.get(), 3), 1000, 0x3104DF, true
                    ),
                    // 11. Caldo das Profundezas (Brew of the Depths) - 3 Frascos, 0 poder, #1723A3
                    new KettleRecipe(
                            List.of(ModItems.MANDRAKE_ROOT::get, ModItems.WATER_ARTICHOKE::get, ModItems.ODOUR_OF_PURITY::get,
                                    ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.SUNFLOWER, () -> Items.INK_SAC),
                            () -> new ItemStack(ModItems.BREW_OF_THE_DEPTHS.get(), 3), 0, 0x1723A3, true
                    ),
                    // 12. Caldo de Infecção (Brew of Infection) - 3 Frascos, 0 poder, #566C6E
                    new KettleRecipe(
                            List.of(ModItems.TOE_OF_FROG::get, ModItems.CREEPER_HEART::get, ModItems.ROTTEN_APPLE::get,
                                    ModItems.BELLADONNA_FLOWER::get, () -> Items.REDSTONE, ModItems.MUTANDIS::get),
                            () -> new ItemStack(ModItems.BREW_OF_INFECTION.get(), 3), 0, 0x566C6E, true
                    ),
                    // 13. Caldo do Sono (Brew of Sleeping) - 3 Frascos, 0 poder, #8A5098
                    new KettleRecipe(
                            List.of(ModItems.PURIFIED_MILK::get, () -> Items.WHITE_BED, ModItems.BREW_OF_LOVE::get,
                                    ModItems.WHIFF_OF_MAGIC::get, ModItems.ICY_NEEDLE::get, ModItems.WATER_ARTICHOKE::get),
                            () -> new ItemStack(ModItems.BREW_OF_SLEEPING.get(), 3), 0, 0x8A5098, true
                    ),
                    // 14. Caldo do Espírito Fluido (Flowing Spirit) - 3 Frascos, 0 poder, #00FFC6
                    new KettleRecipe(
                            List.of(ModItems.FANCIFUL_THREAD::get, ModItems.WATER_ARTICHOKE::get, ModItems.MANDRAKE_ROOT::get,
                                    ModItems.SPANISH_MOSS::get, ModItems.GLINT_WEED::get, ModItems.WOOL_OF_BAT::get),
                            () -> new ItemStack(ModItems.BREW_OF_FLOWING_SPIRIT.get(), 3), 0, 0x00FFC6, true
                    ),
                    // 15. Caldo do Desperdício (Brew of Wasting) - 3 Frascos, 0 poder, #42261E
                    new KettleRecipe(
                            List.of(ModItems.MELLIFLUOUS_HUNGER::get, () -> Items.REDSTONE, ModItems.BELLADONNA_FLOWER::get,
                                    ModItems.EMBER_MOSS::get, () -> Items.FERMENTED_SPIDER_EYE, () -> Items.GLOWSTONE_DUST),
                            () -> new ItemStack(ModItems.BREW_OF_WASTING.get(), 3), 0, 0x42261E, true
                    ),
                    // 16. Caldo de Morcegos (Brew of Bats) - 3 Frascos, 0 poder, #6A4A5E
                    new KettleRecipe(
                            List.of(ModItems.BAT_BALL::get, ModItems.WOOL_OF_BAT::get, () -> Items.LEATHER,
                                    () -> Items.FEATHER, () -> Items.SUGAR, () -> Items.CLAY_BALL),
                            () -> new ItemStack(ModItems.BREW_OF_BATS.get(), 3), 0, 0x6A4A5E, true
                    ),
                    // 17. Caldo de Substituição (Brew of Substitution) - 3 Frascos, 0 poder, #94FD80
                    new KettleRecipe(
                            List.of(ModItems.ENDER_DEW::get, ModItems.ENDER_DEW::get, ModItems.MUTANDIS_EXTREMIS::get,
                                    () -> Items.EGG, () -> Items.STONE_BRICKS, ModItems.ENT_BRANCH::get),
                            () -> new ItemStack(ModItems.BREW_OF_SUBSTITUTION.get(), 3), 0, 0x94FD80, true
                    ),
                    // 18. Caldo Revelador (Brew of Revealing) - 3 Frascos, 0 poder, #C1B9C1
                    new KettleRecipe(
                            List.of(() -> Items.GOLDEN_CARROT, () -> Items.GLOWSTONE_DUST, () -> Items.GLOWSTONE_DUST,
                                    () -> Items.POTION, () -> Items.RED_MUSHROOM, ModItems.ODOUR_OF_PURITY::get),
                            () -> new ItemStack(ModItems.BREW_OF_REVEALING.get(), 3), 0, 0xC1B9C1, true
                    ),
                    // 19a. Caldo de Solidificação (Terra) - 3 Frascos, 2000 poder
                    new KettleRecipe(
                            List.of(() -> Items.DIRT, ModItems.FOUL_FUME::get, ModItems.ODOUR_OF_PURITY::get,
                                    ModItems.MUTANDIS::get, ModItems.WOOD_ASH::get, ModItems.SPANISH_MOSS::get),
                            () -> new ItemStack(ModItems.BREW_OF_SOLID_DIRT.get(), 3), 2000, 0x7A5230, true
                    ),
                    // 19b. Caldo de Solidificação (Rocha) - 3 Frascos, 2000 poder
                    new KettleRecipe(
                            List.of(() -> Items.STONE, ModItems.FOUL_FUME::get, ModItems.ODOUR_OF_PURITY::get,
                                    ModItems.MUTANDIS::get, ModItems.WOOD_ASH::get, ModItems.SPANISH_MOSS::get),
                            () -> new ItemStack(ModItems.BREW_OF_SOLID_STONE.get(), 3), 2000, 0x808080, true
                    ),
                    // 19c. Caldo de Solidificação (Areia) - 3 Frascos, 2000 poder
                    new KettleRecipe(
                            List.of(() -> Items.SAND, ModItems.FOUL_FUME::get, ModItems.ODOUR_OF_PURITY::get,
                                    ModItems.MUTANDIS::get, ModItems.WOOD_ASH::get, ModItems.SPANISH_MOSS::get),
                            () -> new ItemStack(ModItems.BREW_OF_SOLID_SAND.get(), 3), 2000, 0xDBD3A0, true
                    ),
                    // 19d. Caldo de Solidificação (Arenito) - 3 Frascos, 2000 poder
                    new KettleRecipe(
                            List.of(() -> Items.SANDSTONE, ModItems.FOUL_FUME::get, ModItems.ODOUR_OF_PURITY::get,
                                    ModItems.MUTANDIS::get, ModItems.WOOD_ASH::get, ModItems.SPANISH_MOSS::get),
                            () -> new ItemStack(ModItems.BREW_OF_SOLID_SANDSTONE.get(), 3), 2000, 0xD8CB9B, true
                    ),
                    // 19e. Caldo de Solidificação (Erosão) - 3 Frascos, 2000 poder
                    new KettleRecipe(
                            List.of(ModItems.BREW_OF_EROSION::get, ModItems.FOUL_FUME::get, ModItems.ODOUR_OF_PURITY::get,
                                    ModItems.MUTANDIS::get, ModItems.WOOD_ASH::get, ModItems.SPANISH_MOSS::get),
                            () -> new ItemStack(ModItems.BREW_OF_HOLLOW_TEARS_EROSION.get(), 3), 2000, 0x5A2A2A, true
                    ),
                    // 20. Caldo do Salto Amaldiçoado (Cursed Leaping) - 3 Frascos, 0 poder, #00381F
                    new KettleRecipe(
                            List.of(() -> Items.GLISTERING_MELON_SLICE, () -> Items.LEATHER, ModItems.BREW_OF_SPROUTING::get,
                                    ModItems.BELLADONNA_FLOWER::get, () -> Items.FEATHER, () -> Items.FLINT),
                            () -> new ItemStack(ModItems.BREW_OF_CURSED_LEAPING.get(), 3), 0, 0x00381F, true
                    ),
                    // 21. Caldo da Língua de Sapo (Frog's Tongue) - 3 Frascos, 0 poder, #3A752E
                    new KettleRecipe(
                            List.of(() -> Items.BROWN_MUSHROOM, () -> Items.SPIDER_EYE, ModItems.BREW_OF_WEBS::get,
                                    ModItems.WATER_ARTICHOKE::get, () -> Items.IRON_NUGGET, ModItems.TOE_OF_FROG::get),
                            () -> new ItemStack(ModItems.BREW_OF_FROGS_TONGUE.get(), 3), 0, 0x3A752E, true
                    ),
                    // 22. Caldo de Hitchcock (Brew of Hitchcock) - 3 Frascos, 0 poder, #C45A3A
                    new KettleRecipe(
                            List.of(() -> Items.RED_MUSHROOM, () -> Items.CHICKEN, ModItems.BREW_OF_THORNS::get,
                                    ModItems.WOOL_OF_BAT::get, () -> Items.FEATHER, ModItems.OWLETS_WING::get),
                            () -> new ItemStack(ModItems.BREW_OF_HITCHCOCK.get(), 3), 0, 0xC45A3A, true
                    ),
                    // 23. Espírito Congelado (Congealed Spirit) - 1 Item, 2000 poder, #D0C54A
                    new KettleRecipe(
                            List.of(ModItems.HOLLOW_TEARS_BOTTLE::get, ModItems.SUBDUED_SPIRIT::get, ModItems.SUBDUED_SPIRIT::get,
                                    ModItems.SUBDUED_SPIRIT::get, ModItems.SUBDUED_SPIRIT::get, ModItems.SUBDUED_SPIRIT::get),
                            () -> new ItemStack(ModItems.CONGEALED_SPIRIT.get(), 1), 2000, 0xD0C54A, false
                    ),
                    // 24. Sopa de Redstone (Redstone Soup) - 1 Item, 1000 poder, #FF15FE
                    new KettleRecipe(
                            List.of(() -> Items.GOLD_INGOT, ModItems.DROP_OF_LUCK::get, ModItems.WOOL_OF_BAT::get,
                                    ModItems.DOG_TONGUE::get, ModItems.BELLADONNA_FLOWER::get, ModItems.MANDRAKE_ROOT::get),
                            () -> new ItemStack(ModItems.REDSTONE_SOUP.get(), 1), 1000, 0xFF15FE, true
                    ),
                    // 25. Unguento de Voo (Flying Ointment) - 1 Item, 3000 poder, #FFBB34
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, () -> Items.POTION, () -> Items.DIAMOND,
                                    () -> Items.FEATHER, ModItems.WOOL_OF_BAT::get, ModItems.BELLADONNA_FLOWER::get),
                            () -> new ItemStack(ModItems.FLYING_OINTMENT.get(), 1), 3000, 0xFFBB34, true
                    ),
                    // 26. Unguento Místico (Mystic Unguent) - 1 Item, 3000 poder, #2542B3
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, () -> Items.POTION, () -> Items.DIAMOND,
                                    ModItems.ROWAN_SAPLING::get, ModItems.CREEPER_HEART::get, ModItems.DEMONIC_BLOOD::get),
                            () -> new ItemStack(ModItems.MYSTIC_UNGUENT.get(), 1), 3000, 0x2542B3, true
                    ),
                    // 27. Óleo do Acaso (Happenstance Oil) - 1 Item, 2000 poder, #823A5A
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, () -> Items.POTION, () -> Items.GHAST_TEAR,
                                    () -> Items.GOLDEN_APPLE, () -> Items.GLOWSTONE_DUST, ModItems.MANDRAKE_ROOT::get),
                            () -> new ItemStack(ModItems.HAPPENSTANCE_OIL.get(), 1), 2000, 0x823A5A, true
                    ),
                    // 28. Fantasma da Luz (Ghost of the Light) - 2 Itens, 4000 poder, #AAAD6E
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, () -> Items.POTION, () -> Items.POTION,
                                    ModItems.UNBOUND_POPPET::get, () -> Items.TORCH, ModItems.DOG_TONGUE::get),
                            () -> new ItemStack(ModItems.GHOST_OF_THE_LIGHT.get(), 2), 4000, 0xAAAD6E, false
                    ),
                    // 29. Alma do Mundo (Soul of the World) - 2 Itens, 4000 poder, #0BE440
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, () -> Items.POTION, () -> Items.ENCHANTED_GOLDEN_APPLE,
                                    ModItems.ATTUNED_STONE::get, ModItems.MANDRAKE_ROOT::get, ModItems.ROWAN_SAPLING::get),
                            () -> new ItemStack(ModItems.SOUL_OF_THE_WORLD.get(), 2), 4000, 0x0BE440, false
                    ),
                    // 30. Espírito do Outro Lugar (Spirit of Otherwhere) - 2 Itens, 4000 poder, #9333DF
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, () -> Items.POTION, () -> Items.GHAST_TEAR,
                                    () -> Items.GHAST_TEAR, ModItems.DROP_OF_LUCK::get, ModItems.WOOL_OF_BAT::get),
                            () -> new ItemStack(ModItems.SPIRIT_OF_OTHERWHERE.get(), 2), 4000, 0x9333DF, false
                    ),
                    // 31. Ânimo Infernal (Infernal Animus) - 2 Itens, 4000 poder, #8BFA40
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, () -> Items.POTION, ModItems.UNBOUND_POPPET::get,
                                    ModItems.DEMON_HEART::get, ModItems.REFINED_EVIL::get, () -> Items.LAVA_BUCKET),
                            () -> new ItemStack(ModItems.INFERNAL_ANIMUS.get(), 2), 4000, 0x8BFA40, false
                    ),
                    // 32. Base de Infusão (Infusion Base) - 1 Item, 3000 poder, #5F73AF
                    new KettleRecipe(
                            List.of(ModItems.REDSTONE_SOUP::get, ModItems.BREW_OF_FLOWING_SPIRIT::get, ModItems.CREEPER_HEART::get,
                                    ModItems.TOE_OF_FROG::get, ModItems.OWLETS_WING::get, ModItems.DOG_TONGUE::get),
                            () -> new ItemStack(ModItems.INFUSION_BASE.get(), 1), 3000, 0x5F73AF, true
                    )
            );
        }
        return RECIPES;
    }

    public KettleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KETTLE_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KettleBlockEntity kettle) {
        if (!kettle.hasWater()) return;

        // Requisito Térmico Estrito: caso o fogo se apague durante uma infusão, ela é corrompida instantaneamente
        if (!kettle.isHeated() && kettle.ingredientCount > 0 && !kettle.isRuined) {
            kettle.ruinInfusion();
            return;
        }

        // Se está aguardando energia do Altar após o 6º ingrediente, verifica a cada 20 ticks
        if (kettle.awaitingAltarPower && level.getGameTime() % 20 == 0) {
            kettle.tryCompleteWithAltarPower();
        }
    }

    public boolean isHeated() {
        if (level == null) return false;
        BlockState below = level.getBlockState(worldPosition.below());
        if (below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        return (below.is(Blocks.CAMPFIRE) || below.is(Blocks.SOUL_CAMPFIRE))
                && below.hasProperty(CampfireBlock.LIT) && below.getValue(CampfireBlock.LIT);
    }

    public void fillWithWater() {
        for (int i = 0; i < 8; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ingredientCount = 0;
        waterAmount = MAX_WATER_MB;
        isRuined = false;
        isReady = false;
        awaitingAltarPower = false;
        liquidColor = 0x3F76E4;
        requiredPower = 0;
        requiresBottle = true;
        sync();
    }

    public boolean tryInsertIngredient(ItemStack thrownStack) {
        if (!hasWater() || isReady || isRuined || ingredientCount >= 6 || !isHeated()) {
            return false;
        }

        ItemStack single = thrownStack.split(1);
        items.set(ingredientCount, single);
        ingredientCount++;

        List<ItemStack> currentSeq = getCurrentIngredients();

        // Validação prefixo a prefixo
        boolean anyPrefixMatch = false;
        KettleRecipe completedRecipe = null;
        for (KettleRecipe recipe : getRecipes()) {
            if (recipe.matchesPrefix(currentSeq)) {
                anyPrefixMatch = true;
                if (recipe.matchesFull(currentSeq)) {
                    completedRecipe = recipe;
                }
                break;
            }
        }

        if (!anyPrefixMatch) {
            ruinInfusion();
            return true;
        }

        if (completedRecipe != null) {
            this.requiredPower = completedRecipe.altarPower();
            this.liquidColor = completedRecipe.hexColor();
            this.requiresBottle = completedRecipe.requiresBottle();
            this.items.set(SLOT_OUT, completedRecipe.resultBatch().get().copy());
            this.awaitingAltarPower = true;
            tryCompleteWithAltarPower();
        }

        sync();
        return true;
    }

    private void tryCompleteWithAltarPower() {
        if (level == null || !awaitingAltarPower) return;
        if (requiredPower <= 0 || AltarPowerNetwork.consumePower(level, worldPosition, requiredPower)) {
            awaitingAltarPower = false;
            isReady = true;
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
            sync();
        }
    }

    private void ruinInfusion() {
        this.isRuined = true;
        this.isReady = false;
        this.awaitingAltarPower = false;
        this.liquidColor = 0x222222;
        this.items.set(SLOT_OUT, ItemStack.EMPTY);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.8F);
        }
        sync();
    }

    public void extractWithBottle(Player player, ItemStack bottleStack) {
        if (!isReady || items.get(SLOT_OUT).isEmpty() || level == null) return;

        ItemStack batch = items.get(SLOT_OUT);
        ItemStack extracted = batch.copyWithCount(1);

        // Cálculo de doses extras por vestimentas e familiares (Seção 4.4)
        int bonusCount = calculateBonusBottles(player, extracted.getItem());
        extracted.setCount(1 + bonusCount);

        bottleStack.shrink(1);
        if (!player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }

        batch.shrink(1);
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (batch.isEmpty()) {
            resetToEmpty();
        } else {
            sync();
        }
    }

    public void extractDirectItem(Player player) {
        if (!isReady || items.get(SLOT_OUT).isEmpty() || level == null) return;
        ItemStack batch = items.get(SLOT_OUT).copy();
        if (!player.getInventory().add(batch)) {
            player.drop(batch, false);
        }
        level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
        resetToEmpty();
    }

    /**
     * Seção 4.4 - Bônus de Vestimentas e Familiares:
     * - Chapéu de Bruxa: +35%
     * - Chapéu da Baba Yaga: +25% no 1º teste E +25% em um 2º teste independente
     * - Túnica de Bruxa: +35% (exceto Caldo de Reviver)
     * - Túnica de Necromante: +35% EXCLUSIVO para o Caldo de Reviver
     * - Familiar Sapo/Coruja: +5% cumulativo (e +5% no 2º teste do Chapéu da Baba Yaga)
     */
    private int calculateBonusBottles(Player player, Item brewItem) {
        if (level == null) return 0;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        String familiar = player.getData(ModAttachmentTypes.FAMILIAR_TYPE);
        boolean hasBrewFamiliar = "frog".equalsIgnoreCase(familiar) || "owl".equalsIgnoreCase(familiar) || "toad".equalsIgnoreCase(familiar);

        double firstChance = 0.0D;
        double secondChance = 0.0D;

        if (head.is(ModItems.WITCHES_HAT.get())) {
            firstChance += 0.35D;
        } else if (head.is(ModItems.BABA_YAGAS_HAT.get())) {
            firstChance += 0.25D;
            secondChance += 0.25D;
            if (hasBrewFamiliar) {
                secondChance += 0.05D;
            }
        }

        boolean isRaising = brewItem == ModItems.BREW_OF_RAISING.get();
        if (!isRaising && chest.is(ModItems.WITCHES_ROBES.get())) {
            firstChance += 0.35D;
        } else if (isRaising && chest.is(ModItems.NECROMANCER_ROBES.get())) {
            firstChance += 0.35D;
        }

        if (hasBrewFamiliar) {
            firstChance += 0.05D;
        }

        int bonus = 0;
        if (firstChance > 0.0D && level.random.nextDouble() < firstChance) {
            bonus++;
        }
        if (secondChance > 0.0D && level.random.nextDouble() < secondChance) {
            bonus++;
        }
        return bonus;
    }

    private void resetToEmpty() {
        for (int i = 0; i < 8; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ingredientCount = 0;
        waterAmount = 0;
        isRuined = false;
        isReady = false;
        awaitingAltarPower = false;
        liquidColor = 0x3F76E4;
        requiredPower = 0;
        sync();
    }

    private List<ItemStack> getCurrentIngredients() {
        List<ItemStack> list = new ArrayList<>(ingredientCount);
        for (int i = 0; i < ingredientCount; i++) {
            list.add(items.get(i));
        }
        return list;
    }

    public boolean hasWater() {
        return waterAmount >= MAX_WATER_MB;
    }

    public boolean isRuined() {
        return isRuined;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean requiresGlassBottle() {
        return requiresBottle;
    }

    public int getLiquidColor() {
        return liquidColor;
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("IngredientCount", ingredientCount);
        tag.putInt("WaterAmount", waterAmount);
        tag.putBoolean("IsRuined", isRuined);
        tag.putBoolean("IsReady", isReady);
        tag.putBoolean("AwaitingAltarPower", awaitingAltarPower);
        tag.putInt("LiquidColor", liquidColor);
        tag.putInt("RequiredPower", requiredPower);
        tag.putBoolean("RequiresBottle", requiresBottle);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(8, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        ingredientCount = tag.getInt("IngredientCount");
        waterAmount = tag.getInt("WaterAmount");
        isRuined = tag.getBoolean("IsRuined");
        isReady = tag.getBoolean("IsReady");
        awaitingAltarPower = tag.getBoolean("AwaitingAltarPower");
        liquidColor = tag.getInt("LiquidColor");
        requiredPower = tag.getInt("RequiredPower");
        requiresBottle = tag.getBoolean("RequiresBottle");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
