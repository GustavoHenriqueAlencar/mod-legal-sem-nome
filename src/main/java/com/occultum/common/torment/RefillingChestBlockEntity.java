package com.occultum.common.torment;

import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Entidade de Bloco do Baú Reabastecedor (Seção 1.5 de 14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md):
 * - Verifica seu conteúdo a cada tick.
 * - Se estiver completamente vazio e tiverem se passado >= 72.000 ticks (3600 segundos / 1 hora),
 *   reabastece seu estoque preenchendo entre 2 e 5 itens aleatórios de masmorra.
 */
public class RefillingChestBlockEntity extends RandomizableContainerBlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private long lastRefillTick = -72000L;

    public RefillingChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFILLING_CHEST_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RefillingChestBlockEntity chest) {
        if (!(level instanceof ServerLevel sl)) return;
        if (chest.isEmpty() && (sl.getGameTime() - chest.lastRefillTick) >= 72000L) {
            chest.refillLoot(sl);
        }
    }

    public void refillLoot(ServerLevel level) {
        this.items.clear();
        this.lastRefillTick = level.getGameTime();
        int count = 2 + level.random.nextInt(4); // Entre 2 e 5 itens

        List<ItemStack> pool = List.of(
                new ItemStack(Items.BREAD, 3),
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(Items.IRON_INGOT, 4),
                new ItemStack(Items.GOLD_INGOT, 2),
                new ItemStack(Items.TORCH, 8),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.ICY_NEEDLE.get(), 1),
                new ItemStack(ModItems.ROWAN_BERRIES.get(), 4),
                new ItemStack(Items.IRON_SWORD, 1)
        );

        for (int i = 0; i < count; i++) {
            int slot = level.random.nextInt(27);
            ItemStack chosen = pool.get(level.random.nextInt(pool.size())).copy();
            this.items.set(slot, chosen);
        }
        setChanged();
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
        return Component.translatable("container.occultum.refilling_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return ChestMenu.threeRows(id, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
        tag.putLong("LastRefillTick", lastRefillTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
        lastRefillTick = tag.getLong("LastRefillTick");
    }
}
