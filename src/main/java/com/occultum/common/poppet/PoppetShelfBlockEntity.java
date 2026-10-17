package com.occultum.common.poppet;

import com.occultum.common.fetish.FetishBlockEntity;
import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BlockEntity da Prateleira de Poppets (9 slots, Chunkloader integrado no servidor
 * e registro global interdimensional).
 */
public class PoppetShelfBlockEntity extends RandomizableContainerBlockEntity {

    private static final Map<ResourceKey<Level>, Set<BlockPos>> LOADED_SHELVES = new ConcurrentHashMap<>();

    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private String ownerUuid = "";
    private boolean chunkLoaded = false;

    public PoppetShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POPPET_SHELF_BE.get(), pos, state);
    }

    public static Set<BlockPos> getShelvesInDimension(ResourceKey<Level> dimension) {
        return LOADED_SHELVES.getOrDefault(dimension, Set.of());
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public void enableChunkLoading() {
        if (level instanceof ServerLevel serverLevel) {
            LOADED_SHELVES.computeIfAbsent(serverLevel.dimension(), k -> ConcurrentHashMap.newKeySet()).add(worldPosition.immutable());
            if (!chunkLoaded) {
                ChunkPos cp = new ChunkPos(worldPosition);
                serverLevel.setChunkForced(cp.x, cp.z, true);
                chunkLoaded = true;
            }
        }
    }

    public void disableChunkLoading() {
        if (level instanceof ServerLevel serverLevel) {
            Set<BlockPos> set = LOADED_SHELVES.get(serverLevel.dimension());
            if (set != null) {
                set.remove(worldPosition);
            }
            if (chunkLoaded) {
                ChunkPos cp = new ChunkPos(worldPosition);
                serverLevel.setChunkForced(cp.x, cp.z, false);
                chunkLoaded = false;
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            enableChunkLoading();
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            disableChunkLoading();
        }
        super.setRemoved();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PoppetShelfBlockEntity shelf) {
        if (!shelf.chunkLoaded && level instanceof ServerLevel) {
            shelf.enableChunkLoading();
        }
        // Se houver um Fetiche com Aprimoramento de Poppet (Poppet Enhancement) num raio de 16 blocos,
        // recupera lentamente pequenos desgastes de durabilidade a cada 100 ticks (Seção 6.3)
        if (level.getGameTime() % 100 == 0 && FetishBlockEntity.hasPoppetEnhancementNearby(level, pos)) {
            boolean changed = false;
            for (int i = 0; i < shelf.items.size(); i++) {
                ItemStack s = shelf.items.get(i);
                if (!s.isEmpty() && s.isDamaged()) {
                    s.setDamageValue(Math.max(0, s.getDamageValue() - 5));
                    changed = true;
                }
            }
            if (changed) {
                shelf.sync();
            }
        }
    }

    public boolean tryInsertPoppet(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.split(1));
                sync();
                return true;
            }
        }
        return false;
    }

    public void tryExtractLastPoppet(Player player) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack extracted = items.get(i);
                items.set(i, ItemStack.EMPTY);
                if (!player.getInventory().add(extracted)) {
                    player.drop(extracted, false);
                }
                sync();
                return;
            }
        }
    }

    public void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int getContainerSize() {
        return 9;
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
        return Component.translatable("container.occultum.poppet_shelf");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new DispenserMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putString("OwnerUUID", ownerUuid);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(9, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        ownerUuid = tag.getString("OwnerUUID");
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
