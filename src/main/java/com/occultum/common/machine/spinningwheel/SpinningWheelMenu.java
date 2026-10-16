package com.occultum.common.machine.spinningwheel;

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

public class SpinningWheelMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public SpinningWheelMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(5), new SimpleContainerData(3));
    }

    public SpinningWheelMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.SPINNING_WHEEL_MENU.get(), containerId);
        checkContainerSize(container, 5);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;

        // 5 Slots da Roda de Fiar (Seção 3.2):
        // Slot 0: Fibra Base Primária
        this.addSlot(new Slot(container, SpinningWheelBlockEntity.SLOT_PRIMARY_FIBER, 42, 20));
        // Slots 1, 3 e 4: Modificadores, essências e insumos secundários
        this.addSlot(new Slot(container, SpinningWheelBlockEntity.SLOT_MOD_1, 42, 50));
        // Slot 2: Saída do fio ou tecido manufaturado
        this.addSlot(new Slot(container, SpinningWheelBlockEntity.SLOT_OUTPUT, 130, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new Slot(container, SpinningWheelBlockEntity.SLOT_MOD_2, 60, 50));
        this.addSlot(new Slot(container, SpinningWheelBlockEntity.SLOT_MOD_3, 78, 50));

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

    public float getSpinProgress() {
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
            if (index == SpinningWheelBlockEntity.SLOT_OUTPUT) {
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else if (index >= 5) {
                if (!this.moveItemStackTo(slotStack, 0, 2, false)
                        && !this.moveItemStackTo(slotStack, 3, 5, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 5, 41, false)) {
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
