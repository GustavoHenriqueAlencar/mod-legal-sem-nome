package com.occultum.common.altar;

import com.occultum.common.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class AltarMenu extends AbstractContainerMenu {
    private final ContainerData data;

    // Construtor do Servidor
    public AltarMenu(int containerId, Inventory playerInventory, AltarBlockEntity altar) {
        this(containerId, playerInventory, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> altar.getCurrentPower();
                    case 1 -> altar.getMaxPower();
                    case 2 -> altar.getRechargeMultiplier();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only no client
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
    }

    // Construtor do Cliente
    public AltarMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainerData(3));
    }

    public AltarMenu(int containerId, Inventory playerInventory, ContainerData data) {
        super(ModMenus.ALTAR_MENU.get(), containerId);
        checkContainerDataCount(data, 3);
        this.data = data;
        this.addDataSlots(data);
    }

    public int getCurrentPower() {
        return this.data.get(0);
    }

    public int getMaxPower() {
        return this.data.get(1);
    }

    public int getRechargeMultiplier() {
        return this.data.get(2);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
