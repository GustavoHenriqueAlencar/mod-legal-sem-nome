package com.occultum.common.machine.oven;

import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WitchesOvenBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_PRIMARY_OUT = 2;
    public static final int SLOT_FUME_OUT = 3;
    public static final int SLOT_JARS = 4;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_PRIMARY_OUT, SLOT_FUME_OUT};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FUEL, SLOT_JARS};

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int litTime = 0;
    private int litDuration = 0;
    private int cookingProgress = 0;
    private int cookingTotalTime = 180;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> cookingProgress;
                case 3 -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
                case 2 -> cookingProgress = value;
                case 3 -> cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public record OvenRecipeResult(ItemStack primaryOutput, ItemStack fumeOutput) {}

    public WitchesOvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WITCHES_OVEN_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitchesOvenBlockEntity oven) {
        boolean wasLit = oven.isLit();
        boolean changed = false;

        if (oven.isLit()) {
            oven.litTime--;
        }

        oven.cookingTotalTime = oven.calculateCookTime(level, pos, state);

        ItemStack input = oven.items.get(SLOT_INPUT);
        ItemStack fuel = oven.items.get(SLOT_FUEL);
        OvenRecipeResult recipe = oven.getRecipeResult(level, input);

        if (oven.isLit() || (!fuel.isEmpty() && !input.isEmpty() && recipe != null)) {
            if (!oven.isLit() && oven.canBurn(recipe)) {
                oven.litTime = oven.getBurnDuration(fuel);
                oven.litDuration = oven.litTime;
                if (oven.isLit()) {
                    changed = true;
                    if (fuel.hasCraftingRemainingItem()) {
                        oven.items.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                    } else if (!fuel.isEmpty()) {
                        fuel.shrink(1);
                        if (fuel.isEmpty()) {
                            oven.items.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                        }
                    }
                }
            }

            if (oven.isLit() && oven.canBurn(recipe)) {
                oven.cookingProgress++;
                if (oven.cookingProgress >= oven.cookingTotalTime) {
                    oven.cookingProgress = 0;
                    oven.cookingTotalTime = oven.calculateCookTime(level, pos, state);
                    oven.burnItem(level, pos, state, recipe);
                    changed = true;
                }
            } else {
                oven.cookingProgress = 0;
            }
        } else if (!oven.isLit() && oven.cookingProgress > 0) {
            oven.cookingProgress = Math.max(0, oven.cookingProgress - 2);
        }

        if (wasLit != oven.isLit()) {
            changed = true;
            state = state.setValue(WitchesOvenBlock.LIT, oven.isLit());
            level.setBlock(pos, state, 3);
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    /**
     * Dinâmica Temporal e Conexão de Chaminés:
     * Tempo base: 180 ticks.
     * Até 3 chaminés acopladas (1 no topo Y+1, 2 nas laterais alinhadas à rotação frontal):
     * T_cozimento = 180 - (20 * N_chamine)
     */
    public int calculateCookTime(Level level, BlockPos pos, BlockState state) {
        int funnels = 0;
        if (level.getBlockState(pos.above()).getBlock() instanceof FumeFunnelBlock) {
            funnels++;
        }
        Direction facing = state.hasProperty(WitchesOvenBlock.FACING) ? state.getValue(WitchesOvenBlock.FACING) : Direction.NORTH;
        Direction left = facing.getClockWise();
        Direction right = facing.getCounterClockWise();
        if (level.getBlockState(pos.relative(left)).getBlock() instanceof FumeFunnelBlock) {
            funnels++;
        }
        if (level.getBlockState(pos.relative(right)).getBlock() instanceof FumeFunnelBlock) {
            funnels++;
        }
        return Math.max(120, 180 - (20 * Math.min(3, funnels)));
    }

    /**
     * Matemática da Captura de Fumaça:
     * Chance Base: 0.30 (30%).
     * Chaminé Comum lateral: +0.25 (25%).
     * Chaminé Filtrada lateral: +0.30 (30%).
     * Probabilidade = min(0.30 + Bonus_1 + Bonus_2, 1.0)
     */
    public double calculateFumeCaptureChance(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(WitchesOvenBlock.FACING) ? state.getValue(WitchesOvenBlock.FACING) : Direction.NORTH;
        Direction left = facing.getClockWise();
        Direction right = facing.getCounterClockWise();

        double bonus1 = 0.0D;
        if (level.getBlockState(pos.relative(left)).getBlock() instanceof FumeFunnelBlock leftFunnel) {
            bonus1 = leftFunnel.getLateralCaptureBonus();
        }

        double bonus2 = 0.0D;
        if (level.getBlockState(pos.relative(right)).getBlock() instanceof FumeFunnelBlock rightFunnel) {
            bonus2 = rightFunnel.getLateralCaptureBonus();
        }

        return Math.min(0.30D + bonus1 + bonus2, 1.0D);
    }

    @Nullable
    public OvenRecipeResult getRecipeResult(Level level, ItemStack input) {
        if (input.isEmpty()) return null;

        // 1. Mudas específicas da Tabela 1.6
        if (input.is(Items.OAK_SAPLING)) {
            return new OvenRecipeResult(new ItemStack(Items.CHARCOAL), new ItemStack(ModItems.EXHALE_OF_THE_HORNED_ONE.get()));
        }
        if (input.is(Items.SPRUCE_SAPLING)) {
            return new OvenRecipeResult(new ItemStack(Items.CHARCOAL), new ItemStack(ModItems.HINT_OF_REBIRTH.get()));
        }
        if (input.is(Items.BIRCH_SAPLING)) {
            return new OvenRecipeResult(new ItemStack(Items.CHARCOAL), new ItemStack(ModItems.BREATH_OF_THE_GODDESS.get()));
        }
        if (input.is(ModItems.ROWAN_SAPLING.get())) {
            return new OvenRecipeResult(new ItemStack(ModItems.WOOD_ASH.get()), new ItemStack(ModItems.WHIFF_OF_MAGIC.get()));
        }
        if (input.is(ModItems.ALDER_SAPLING.get())) {
            return new OvenRecipeResult(new ItemStack(ModItems.WOOD_ASH.get()), new ItemStack(ModItems.REEK_OF_MISFORTUNE.get()));
        }
        if (input.is(ModItems.HAWTHORN_SAPLING.get())) {
            return new OvenRecipeResult(new ItemStack(ModItems.WOOD_ASH.get()), new ItemStack(ModItems.ODOUR_OF_PURITY.get()));
        }
        if (input.is(Items.DARK_OAK_SAPLING) || input.is(Items.JUNGLE_SAPLING) || input.is(Items.ACACIA_SAPLING)
                || input.is(Items.CHERRY_SAPLING) || input.is(Items.MANGROVE_PROPAGULE) || input.is(ItemTags.SAPLINGS)) {
            return new OvenRecipeResult(new ItemStack(Items.CHARCOAL), new ItemStack(ModItems.FOUL_FUME.get()));
        }

        // 2. Troncos de madeira de qualquer espécie -> Carvão Vegetal + Fumaça Fétida
        if (input.is(ItemTags.LOGS) || input.is(ItemTags.LOGS_THAT_BURN)) {
            return new OvenRecipeResult(new ItemStack(Items.CHARCOAL), new ItemStack(ModItems.FOUL_FUME.get()));
        }

        // 3. Carnes cruas e alimentos culinários (recusando minérios metálicos ou pedras)
        if (input.is(ModItems.ODD_PORKCHOP_RAW.get())) {
            return new OvenRecipeResult(new ItemStack(ModItems.ODD_PORKCHOP_COOKED.get()), new ItemStack(ModItems.FOUL_FUME.get()));
        }
        if (level != null) {
            Optional<RecipeHolder<SmeltingRecipe>> smelting = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
            if (smelting.isPresent()) {
                ItemStack result = smelting.get().value().getResultItem(level.registryAccess()).copy();
                // Aceita apenas se a entrada ou saída for um alimento culinário (recusa minérios/pedras)
                if (input.has(DataComponents.FOOD) || result.has(DataComponents.FOOD)) {
                    return new OvenRecipeResult(result, new ItemStack(ModItems.FOUL_FUME.get()));
                }
            }
        }

        return null;
    }

    private boolean canBurn(@Nullable OvenRecipeResult recipe) {
        if (recipe == null || items.get(SLOT_INPUT).isEmpty()) return false;
        ItemStack primaryResult = recipe.primaryOutput();
        if (primaryResult.isEmpty()) return false;

        ItemStack currentOut = items.get(SLOT_PRIMARY_OUT);
        if (currentOut.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(currentOut, primaryResult)) return false;
        return currentOut.getCount() + primaryResult.getCount() <= getMaxStackSize()
                && currentOut.getCount() + primaryResult.getCount() <= currentOut.getMaxStackSize();
    }

    private void burnItem(Level level, BlockPos pos, BlockState state, @Nullable OvenRecipeResult recipe) {
        if (recipe == null || !canBurn(recipe)) return;

        ItemStack input = items.get(SLOT_INPUT);
        ItemStack primaryResult = recipe.primaryOutput().copy();
        ItemStack currentPrimary = items.get(SLOT_PRIMARY_OUT);

        if (currentPrimary.isEmpty()) {
            items.set(SLOT_PRIMARY_OUT, primaryResult);
        } else if (ItemStack.isSameItemSameComponents(currentPrimary, primaryResult)) {
            currentPrimary.grow(primaryResult.getCount());
        }

        // Captura de Fumaça (exige Jarros de Argila Vazios no Slot 4 e espaço no Slot 3)
        ItemStack jars = items.get(SLOT_JARS);
        ItemStack fumeResult = recipe.fumeOutput().copy();
        ItemStack currentFume = items.get(SLOT_FUME_OUT);

        if (!jars.isEmpty() && jars.is(ModItems.CLAY_JAR.get()) && !fumeResult.isEmpty()) {
            boolean canFitFume = currentFume.isEmpty()
                    || (ItemStack.isSameItemSameComponents(currentFume, fumeResult)
                    && currentFume.getCount() + fumeResult.getCount() <= currentFume.getMaxStackSize());
            if (canFitFume) {
                double chance = calculateFumeCaptureChance(level, pos, state);
                if (level.random.nextDouble() < chance) {
                    jars.shrink(1);
                    if (currentFume.isEmpty()) {
                        items.set(SLOT_FUME_OUT, fumeResult);
                    } else {
                        currentFume.grow(fumeResult.getCount());
                    }
                }
            }
        }

        input.shrink(1);
    }

    public boolean isLit() {
        return this.litTime > 0;
    }

    protected int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;
        return fuel.getBurnTime(RecipeType.SMELTING);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.occultum.witches_oven");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new WitchesOvenMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return SLOTS_FOR_DOWN;
        return side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == SLOT_PRIMARY_OUT || index == SLOT_FUME_OUT;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack current = this.items.get(slot);
        boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(current, stack);
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        if (slot == SLOT_INPUT && !same && this.level != null) {
            this.cookingTotalTime = calculateCookTime(this.level, this.worldPosition, this.getBlockState());
            this.cookingProgress = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == SLOT_PRIMARY_OUT || index == SLOT_FUME_OUT) return false;
        if (index == SLOT_JARS) return stack.is(ModItems.CLAY_JAR.get());
        if (index == SLOT_FUEL) return AbstractFurnaceBlockEntity.isFuel(stack);
        return getRecipeResult(this.level, stack) != null;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("LitTime", this.litTime);
        tag.putInt("LitDuration", this.litDuration);
        tag.putInt("CookTime", this.cookingProgress);
        tag.putInt("CookTimeTotal", this.cookingTotalTime);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.litTime = tag.getInt("LitTime");
        this.litDuration = tag.getInt("LitDuration");
        this.cookingProgress = tag.getInt("CookTime");
        this.cookingTotalTime = tag.getInt("CookTimeTotal");
    }
}
