package com.occultum.common.botany.wild;

import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LeechChestBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private String ownerUuid = "";
    private String lastVictimSignature = "";
    private String lastVictimName = "";

    public LeechChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEECH_CHEST_BE.get(), pos, state);
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    public void onOpenedByPlayer(net.minecraft.world.entity.player.Player player) {
        if (ownerUuid.isEmpty()) {
            ownerUuid = player.getUUID().toString();
            setChanged();
            return;
        }
        if (!ownerUuid.equals(player.getUUID().toString())) {
            player.hurt(player.damageSources().magic(), 1.0F);
            this.lastVictimSignature = player.getUUID().toString();
            this.lastVictimName = player.getName().getString();
            setChanged();
        }
    }

    public String getLastVictimSignature() {
        return lastVictimSignature;
    }

    public String getLastVictimName() {
        return lastVictimName;
    }

    public void clearVictimSample() {
        this.lastVictimSignature = "";
        this.lastVictimName = "";
        setChanged();
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
        tag.putString("OwnerUUID", ownerUuid);
        tag.putString("LastVictimSignature", lastVictimSignature);
        tag.putString("LastVictimName", lastVictimName);
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
        this.ownerUuid = tag.getString("OwnerUUID");
        this.lastVictimSignature = tag.getString("LastVictimSignature");
        this.lastVictimName = tag.getString("LastVictimName");
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.occultum.leech_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }
}
