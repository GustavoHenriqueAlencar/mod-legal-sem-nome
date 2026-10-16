package com.occultum.common.machine.distillery;

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

import java.util.List;
import java.util.function.Supplier;

public class DistilleryBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int SLOT_INPUT_A = 0;
    public static final int SLOT_INPUT_B = 1;
    public static final int SLOT_JARS = 2;
    public static final int SLOT_OUT_1 = 3;
    public static final int SLOT_OUT_2 = 4;
    public static final int SLOT_OUT_3 = 5;
    public static final int SLOT_OUT_4 = 6;

    public static final int TOTAL_CYCLE_TICKS = 800;
    public static final double ALTAR_POWER_PER_TICK = 0.6D;

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT_A, SLOT_INPUT_B};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_JARS, SLOT_INPUT_A, SLOT_INPUT_B};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_OUT_1, SLOT_OUT_2, SLOT_OUT_3, SLOT_OUT_4};

    private NonNullList<ItemStack> items = NonNullList.withSize(7, ItemStack.EMPTY);
    private int distillationProgress = 0;
    private int hasPowerFlag = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> distillationProgress;
                case 1 -> TOTAL_CYCLE_TICKS;
                case 2 -> hasPowerFlag;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> distillationProgress = value;
                case 2 -> hasPowerFlag = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public record DistilleryRecipe(
            Supplier<Item> inputA,
            @Nullable Supplier<Item> inputB,
            int jarsRequired,
            Supplier<ItemStack> out1,
            Supplier<ItemStack> out2,
            Supplier<ItemStack> out3,
            Supplier<ItemStack> out4
    ) {
        public boolean matches(ItemStack stackA, ItemStack stackB) {
            Item reqA = inputA.get();
            Item reqB = inputB != null ? inputB.get() : null;
            if (reqB == null) {
                return (stackA.is(reqA) && stackB.isEmpty()) || (stackB.is(reqA) && stackA.isEmpty());
            }
            return (stackA.is(reqA) && stackB.is(reqB)) || (stackA.is(reqB) && stackB.is(reqA));
        }
    }

    private static final List<DistilleryRecipe> RECIPES = List.of(
            // 1. Fumaça Fétida + Cal Virgem + 1 Jarro -> Gesso (1), Óleo de Vitríolo (1), Pepita de Ferro (1), Vazio
            new DistilleryRecipe(
                    ModItems.FOUL_FUME::get, ModItems.QUICKLIME::get, 1,
                    () -> new ItemStack(ModItems.GYPSUM.get(), 1),
                    () -> new ItemStack(ModItems.OIL_OF_VITRIOL.get(), 1),
                    () -> new ItemStack(Items.IRON_NUGGET, 1),
                    () -> ItemStack.EMPTY
            ),
            // 2. Respiração da Deusa + Lápis-Lazúli + 3 Jarros -> Lágrima da Deusa (1), Baforada de Magia (1), Pepita de Ferro (1), Fumaça Fétida (1)
            new DistilleryRecipe(
                    ModItems.BREATH_OF_THE_GODDESS::get, () -> Items.LAPIS_LAZULI, 3,
                    () -> new ItemStack(ModItems.TEAR_OF_THE_GODDESS.get(), 1),
                    () -> new ItemStack(ModItems.WHIFF_OF_MAGIC.get(), 1),
                    () -> new ItemStack(Items.IRON_NUGGET, 1),
                    () -> new ItemStack(ModItems.FOUL_FUME.get(), 1)
            ),
            // 3. Diamante + Óleo de Vitríolo + 3 Jarros -> Vapor de Diamante (1), Vapor de Diamante (1), Odor de Pureza (1), Vazio
            new DistilleryRecipe(
                    () -> Items.DIAMOND, ModItems.OIL_OF_VITRIOL::get, 3,
                    () -> new ItemStack(ModItems.DIAMOND_VAPOR.get(), 1),
                    () -> new ItemStack(ModItems.DIAMOND_VAPOR.get(), 1),
                    () -> new ItemStack(ModItems.ODOUR_OF_PURITY.get(), 1),
                    () -> ItemStack.EMPTY
            ),
            // 4. Vapor de Diamante + Lágrima de Ghast + 3 Jarros -> Odor de Pureza (1), Cheiro de Desgraça (1), Fumaça Fétida (1), Mal Refinado (1)
            new DistilleryRecipe(
                    ModItems.DIAMOND_VAPOR::get, () -> Items.GHAST_TEAR, 3,
                    () -> new ItemStack(ModItems.ODOUR_OF_PURITY.get(), 1),
                    () -> new ItemStack(ModItems.REEK_OF_MISFORTUNE.get(), 1),
                    () -> new ItemStack(ModItems.FOUL_FUME.get(), 1),
                    () -> new ItemStack(ModItems.REFINED_EVIL.get(), 1)
            ),
            // 5. Pérola do End + Nenhum + 6 Jarros -> Orvalho do End (2), Orvalho do End (2), Orvalho do End (1), Baforada de Magia (1)
            new DistilleryRecipe(
                    () -> Items.ENDER_PEARL, null, 6,
                    () -> new ItemStack(ModItems.ENDER_DEW.get(), 2),
                    () -> new ItemStack(ModItems.ENDER_DEW.get(), 2),
                    () -> new ItemStack(ModItems.ENDER_DEW.get(), 1),
                    () -> new ItemStack(ModItems.WHIFF_OF_MAGIC.get(), 1)
            ),
            // 6. Pó de Blaze + Bola de Argila + 1 Jarro -> Quartzo do Nether (1), Quartzo do Nether (1), Cheiro de Desgraça (1), Vazio
            new DistilleryRecipe(
                    () -> Items.BLAZE_POWDER, () -> Items.CLAY_BALL, 1,
                    () -> new ItemStack(Items.QUARTZ, 1),
                    () -> new ItemStack(Items.QUARTZ, 1),
                    () -> new ItemStack(ModItems.REEK_OF_MISFORTUNE.get(), 1),
                    () -> ItemStack.EMPTY
            ),
            // 7. Coração Demoníaco + Vapor de Diamante + 4 Jarros -> Sangue Infernal (2), Sangue Infernal (2), Mal Refinado (1), Vazio
            new DistilleryRecipe(
                    ModItems.DEMON_HEART::get, ModItems.DIAMOND_VAPOR::get, 4,
                    () -> new ItemStack(ModItems.DEMONIC_BLOOD.get(), 2),
                    () -> new ItemStack(ModItems.DEMONIC_BLOOD.get(), 2),
                    () -> new ItemStack(ModItems.REFINED_EVIL.get(), 1),
                    () -> ItemStack.EMPTY
            ),
            // 8. Coração Demoníaco + Rocha Matriz + 2 Jarros -> Rocha Matriz (1), Sangue Infernal (1), Sangue Infernal (1), Vazio
            new DistilleryRecipe(
                    ModItems.DEMON_HEART::get, () -> Items.BEDROCK, 2,
                    () -> new ItemStack(Items.BEDROCK, 1),
                    () -> new ItemStack(ModItems.DEMONIC_BLOOD.get(), 1),
                    () -> new ItemStack(ModItems.DEMONIC_BLOOD.get(), 1),
                    () -> ItemStack.EMPTY
            ),
            // 9. Poção de Espírito Fluido + Óleo de Vitríolo + 2 Jarros -> Vontade Focada (1), Medo Condensado (1), Lágrimas Ocas (4), Lágrimas Ocas (4)
            new DistilleryRecipe(
                    ModItems.BREW_OF_FLOWING_SPIRIT::get, ModItems.OIL_OF_VITRIOL::get, 2,
                    () -> new ItemStack(ModItems.FOCUSED_WILL.get(), 1),
                    () -> new ItemStack(ModItems.CONDENSED_FEAR.get(), 1),
                    () -> new ItemStack(ModItems.HOLLOW_TEARS_BOTTLE.get(), 4),
                    () -> new ItemStack(ModItems.HOLLOW_TEARS_BOTTLE.get(), 4)
            )
    );

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTILLERY_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DistilleryBlockEntity distillery) {
        DistilleryRecipe recipe = distillery.findMatchingRecipe();
        boolean active = false;
        boolean changed = false;

        if (recipe != null && distillery.canProcess(recipe)) {
            // Consome 0,6 unidades de poder do Altar por tick; congela o progresso se faltar energia
            if (AltarPowerNetwork.consumePower(level, pos, ALTAR_POWER_PER_TICK)) {
                distillery.hasPowerFlag = 1;
                active = true;
                distillery.distillationProgress++;
                changed = true;

                if (distillery.distillationProgress >= TOTAL_CYCLE_TICKS) {
                    distillery.distillationProgress = 0;
                    distillery.finishDistillation(recipe);
                }
            } else {
                // Congela o temporizador no estado corrente sem resetar distillationProgress
                distillery.hasPowerFlag = 0;
            }
        } else {
            if (distillery.distillationProgress != 0) {
                distillery.distillationProgress = 0;
                changed = true;
            }
            distillery.hasPowerFlag = 0;
        }

        if (state.getValue(DistilleryBlock.LIT) != active) {
            state = state.setValue(DistilleryBlock.LIT, active);
            level.setBlock(pos, state, 3);
            changed = true;
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    @Nullable
    public DistilleryRecipe findMatchingRecipe() {
        ItemStack stackA = items.get(SLOT_INPUT_A);
        ItemStack stackB = items.get(SLOT_INPUT_B);
        if (stackA.isEmpty() && stackB.isEmpty()) return null;

        for (DistilleryRecipe recipe : RECIPES) {
            if (recipe.matches(stackA, stackB)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canProcess(DistilleryRecipe recipe) {
        ItemStack jars = items.get(SLOT_JARS);
        if (recipe.jarsRequired() > 0) {
            if (jars.isEmpty() || !jars.is(ModItems.CLAY_JAR.get()) || jars.getCount() < recipe.jarsRequired()) {
                return false;
            }
        }
        return canFitOutput(SLOT_OUT_1, recipe.out1().get())
                && canFitOutput(SLOT_OUT_2, recipe.out2().get())
                && canFitOutput(SLOT_OUT_3, recipe.out3().get())
                && canFitOutput(SLOT_OUT_4, recipe.out4().get());
    }

    private boolean canFitOutput(int slot, ItemStack result) {
        if (result.isEmpty()) return true;
        ItemStack current = items.get(slot);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(current, result)) return false;
        return current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private void finishDistillation(DistilleryRecipe recipe) {
        if (!canProcess(recipe)) return;

        ItemStack stackA = items.get(SLOT_INPUT_A);
        ItemStack stackB = items.get(SLOT_INPUT_B);
        ItemStack jars = items.get(SLOT_JARS);

        if (!stackA.isEmpty()) stackA.shrink(1);
        if (!stackB.isEmpty()) stackB.shrink(1);
        if (recipe.jarsRequired() > 0 && !jars.isEmpty()) {
            jars.shrink(recipe.jarsRequired());
        }

        insertOutput(SLOT_OUT_1, recipe.out1().get().copy());
        insertOutput(SLOT_OUT_2, recipe.out2().get().copy());
        insertOutput(SLOT_OUT_3, recipe.out3().get().copy());
        insertOutput(SLOT_OUT_4, recipe.out4().get().copy());
    }

    private void insertOutput(int slot, ItemStack result) {
        if (result.isEmpty()) return;
        ItemStack current = items.get(slot);
        if (current.isEmpty()) {
            items.set(slot, result);
        } else if (ItemStack.isSameItemSameComponents(current, result)) {
            current.grow(result.getCount());
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.occultum.distillery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DistilleryMenu(containerId, playerInventory, this, this.dataAccess);
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
        return index >= SLOT_OUT_1 && index <= SLOT_OUT_4;
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
        if (index >= SLOT_OUT_1) return false;
        if (index == SLOT_JARS) return stack.is(ModItems.CLAY_JAR.get());
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("DistillationProgress", this.distillationProgress);
        tag.putInt("HasPower", this.hasPowerFlag);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.distillationProgress = tag.getInt("DistillationProgress");
        this.hasPowerFlag = tag.getInt("HasPower");
    }
}
