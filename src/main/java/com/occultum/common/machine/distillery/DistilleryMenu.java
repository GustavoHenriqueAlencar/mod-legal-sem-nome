package com.occultum.common.machine.distillery;

import com.occultum.common.registry.ModItems;
import com.occultum.common.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DistilleryMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public DistilleryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(7), new SimpleContainerData(3));
    }

    public DistilleryMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.DISTILLERY_MENU.get(), containerId);
        checkContainerSize(container, 7);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;

        // 7 Slots da Destilaria (Seção 2.2):
        // Slot 0: Insumo Reagente A
        this.addSlot(new Slot(container, DistilleryBlockEntity.SLOT_INPUT_A, 48, 16));
        // Slot 1: Insumo Reagente B
        this.addSlot(new Slot(container, DistilleryBlockEntity.SLOT_INPUT_B, 48, 34));
        // Slot 2: Jarros de Argila Vazios
        this.addSlot(new Slot(container, DistilleryBlockEntity.SLOT_JARS, 48, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.CLAY_JAR.get());
            }
        });

        // Slots 3, 4, 5 e 6: Quatro receptáculos de saída
        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.SLOT_OUT_1, 110, 16));
        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.SLOT_OUT_2, 128, 16));
        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.SLOT_OUT_3, 110, 34));
        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.SLOT_OUT_4, 128, 34));

        // Inventário do jogador
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    private static class OutputSlot extends Slot {
        public OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    public float getDistillationProgress() {
        int progress = this.data.get(0);
        int total = this.data.get(1);
        return total > 0 ? Math.clamp((float) progress / (float) total, 0.0F, 1.0F) : 0.0F;
    }

    public boolean hasAltarPower() {
        return this.data.get(2) > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index >= DistilleryBlockEntity.SLOT_OUT_1 && index <= DistilleryBlockEntity.SLOT_OUT_4) {
                if (!this.moveItemStackTo(slotStack, 7, 43, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else if (index >= 7) {
                if (slotStack.is(ModItems.CLAY_JAR.get())) {
                    if (!this.moveItemStackTo(slotStack, DistilleryBlockEntity.SLOT_JARS, DistilleryBlockEntity.SLOT_JARS + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, DistilleryBlockEntity.SLOT_INPUT_A, DistilleryBlockEntity.SLOT_INPUT_B + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 7, 43, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
}
