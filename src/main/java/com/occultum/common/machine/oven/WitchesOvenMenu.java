package com.occultum.common.machine.oven;

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
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class WitchesOvenMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public WitchesOvenMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(5), new SimpleContainerData(4));
    }

    public WitchesOvenMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.WITCHES_OVEN_MENU.get(), containerId);
        checkContainerSize(container, 5);
        checkContainerDataCount(data, 4);
        this.container = container;
        this.data = data;

        // Disposição exata dos 5 slots (Seção 1.2):
        // Slot 0 (Entrada Superior - X: 56, Y: 17)
        this.addSlot(new Slot(container, WitchesOvenBlockEntity.SLOT_INPUT, 56, 17));
        // Slot 1 (Combustível Inferior - X: 56, Y: 53)
        this.addSlot(new Slot(container, WitchesOvenBlockEntity.SLOT_FUEL, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AbstractFurnaceBlockEntity.isFuel(stack);
            }
        });
        // Slot 2 (Saída Principal - X: 118, Y: 21)
        this.addSlot(new Slot(container, WitchesOvenBlockEntity.SLOT_PRIMARY_OUT, 118, 21) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        // Slot 3 (Saída de Subproduto - X: 118, Y: 53)
        this.addSlot(new Slot(container, WitchesOvenBlockEntity.SLOT_FUME_OUT, 118, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        // Slot 4 (Entrada de Jarros - X: 83, Y: 53)
        this.addSlot(new Slot(container, WitchesOvenBlockEntity.SLOT_JARS, 83, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.CLAY_JAR.get());
            }
        });

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

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    public float getLitProgress() {
        int duration = this.data.get(1);
        if (duration == 0) duration = 200;
        return Math.clamp((float) this.data.get(0) / (float) duration, 0.0F, 1.0F);
    }

    public float getBurnProgress() {
        int current = this.data.get(2);
        int total = this.data.get(3);
        return total != 0 && current != 0 ? Math.clamp((float) current / (float) total, 0.0F, 1.0F) : 0.0F;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index == WitchesOvenBlockEntity.SLOT_PRIMARY_OUT || index == WitchesOvenBlockEntity.SLOT_FUME_OUT) {
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else if (index >= 5) {
                if (slotStack.is(ModItems.CLAY_JAR.get())) {
                    if (!this.moveItemStackTo(slotStack, WitchesOvenBlockEntity.SLOT_JARS, WitchesOvenBlockEntity.SLOT_JARS + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (AbstractFurnaceBlockEntity.isFuel(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, WitchesOvenBlockEntity.SLOT_FUEL, WitchesOvenBlockEntity.SLOT_FUEL + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, WitchesOvenBlockEntity.SLOT_INPUT, WitchesOvenBlockEntity.SLOT_INPUT + 1, false)) {
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
