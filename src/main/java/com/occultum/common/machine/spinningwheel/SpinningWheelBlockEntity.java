package com.occultum.common.machine.spinningwheel;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SpinningWheelBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int SLOT_PRIMARY_FIBER = 0;
    public static final int SLOT_MOD_1 = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_MOD_2 = 3;
    public static final int SLOT_MOD_3 = 4;

    public static final int TOTAL_CYCLE_TICKS = 300;
    public static final double ALTAR_POWER_PER_TICK = 0.6D;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_PRIMARY_FIBER};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_MOD_1, SLOT_MOD_2, SLOT_MOD_3};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_OUTPUT};

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int spinProgress = 0;
    private int hasPowerFlag = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> spinProgress;
                case 1 -> TOTAL_CYCLE_TICKS;
                case 2 -> hasPowerFlag;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> spinProgress = value;
                case 2 -> hasPowerFlag = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public record SpinningRecipe(
            Supplier<Item> baseFiber,
            int baseFiberCount,
            List<Supplier<Item>> modifiers,
            Supplier<ItemStack> output
    ) {
        public boolean matches(ItemStack primary, List<ItemStack> modStacks) {
            if (primary.isEmpty() || !primary.is(baseFiber.get()) || primary.getCount() < baseFiberCount) {
                return false;
            }
            List<Item> required = new ArrayList<>();
            for (Supplier<Item> mod : modifiers) {
                required.add(mod.get());
            }
            int nonEmptyCount = 0;
            for (ItemStack modStack : modStacks) {
                if (!modStack.isEmpty()) {
                    nonEmptyCount++;
                    if (!required.remove(modStack.getItem())) {
                        return false;
                    }
                }
            }
            return required.isEmpty() && nonEmptyCount == modifiers.size();
        }
    }

    private static final List<SpinningRecipe> RECIPES = List.of(
            // 1. Algodão Fantasmagórico (4) + Linha Comum (1) + Odor de Pureza (1) -> Fio Fantasioso (1)
            new SpinningRecipe(
                    ModItems.WISPY_COTTON::get, 4,
                    List.of(() -> Items.STRING, ModItems.ODOUR_OF_PURITY::get),
                    () -> new ItemStack(ModItems.FANCIFUL_THREAD.get(), 1)
            ),
            // 2. Algodão Perturbado (4) + Linha Comum (1) + Cheiro de Desgraça (1) -> Barbante Atormentado (1)
            new SpinningRecipe(
                    ModItems.DISTURBED_COTTON::get, 4,
                    List.of(() -> Items.STRING, ModItems.REEK_OF_MISFORTUNE::get),
                    () -> new ItemStack(ModItems.TORMENTED_TWINE.get(), 1)
            ),
            // 3. Linha Comum (8) + Nenhum -> Bloco de Teia de Aranha (1)
            new SpinningRecipe(
                    () -> Items.STRING, 8,
                    List.of(),
                    () -> new ItemStack(Items.COBWEB, 1)
            ),
            // 4. Fardo de Feno (1) + Baforada de Magia (1) -> Fio Dourado (3)
            new SpinningRecipe(
                    () -> Items.HAY_BLOCK, 1,
                    List.of(ModItems.WHIFF_OF_MAGIC::get),
                    () -> new ItemStack(ModItems.GOLDEN_THREAD.get(), 3)
            )
    );

    public SpinningWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPINNING_WHEEL_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpinningWheelBlockEntity wheel) {
        SpinningRecipe recipe = wheel.findMatchingRecipe();
        boolean changed = false;

        if (recipe != null && wheel.canProcess(recipe)) {
            if (AltarPowerNetwork.consumePower(level, pos, ALTAR_POWER_PER_TICK)) {
                wheel.hasPowerFlag = 1;
                wheel.spinProgress++;
                changed = true;

                if (wheel.spinProgress >= TOTAL_CYCLE_TICKS) {
                    wheel.spinProgress = 0;
                    wheel.finishSpinning(recipe);
                }
            } else {
                // Congela o progresso se faltar energia
                wheel.hasPowerFlag = 0;
            }
        } else {
            if (wheel.spinProgress != 0) {
                wheel.spinProgress = 0;
                changed = true;
            }
            wheel.hasPowerFlag = 0;
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    @Nullable
    public SpinningRecipe findMatchingRecipe() {
        ItemStack primary = items.get(SLOT_PRIMARY_FIBER);
        List<ItemStack> mods = List.of(items.get(SLOT_MOD_1), items.get(SLOT_MOD_2), items.get(SLOT_MOD_3));
        for (SpinningRecipe recipe : RECIPES) {
            if (recipe.matches(primary, mods)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canProcess(SpinningRecipe recipe) {
        ItemStack result = recipe.output().get();
        ItemStack currentOut = items.get(SLOT_OUTPUT);
        if (currentOut.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(currentOut, result)) return false;
        return currentOut.getCount() + result.getCount() <= currentOut.getMaxStackSize();
    }

    private void finishSpinning(SpinningRecipe recipe) {
        if (!canProcess(recipe)) return;

        items.get(SLOT_PRIMARY_FIBER).shrink(recipe.baseFiberCount());
        int[] modSlots = new int[]{SLOT_MOD_1, SLOT_MOD_2, SLOT_MOD_3};
        for (Supplier<Item> modSupplier : recipe.modifiers()) {
            Item modItem = modSupplier.get();
            for (int slot : modSlots) {
                ItemStack stack = items.get(slot);
                if (!stack.isEmpty() && stack.is(modItem)) {
                    stack.shrink(1);
                    break;
                }
            }
        }

        ItemStack result = recipe.output().get().copy();
        ItemStack currentOut = items.get(SLOT_OUTPUT);
        if (currentOut.isEmpty()) {
            items.set(SLOT_OUTPUT, result);
        } else {
            currentOut.grow(result.getCount());
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.occultum.spinning_wheel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SpinningWheelMenu(containerId, playerInventory, this, this.dataAccess);
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
        return index == SLOT_OUTPUT;
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
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index != SLOT_OUTPUT;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("SpinProgress", this.spinProgress);
        tag.putInt("HasPower", this.hasPowerFlag);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.spinProgress = tag.getInt("SpinProgress");
        this.hasPowerFlag = tag.getInt("HasPower");
    }
}
