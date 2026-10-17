package com.occultum.common.vampire;

import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Entidade de Bloco do Crisol de Sangue (capacidade: 0 a 20 níveis de sangue concentrado).
 */
public class BloodCrucibleBlockEntity extends BlockEntity {

    private int bloodLevels = 0;

    public BloodCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLOOD_CRUCIBLE_BE.get(), pos, state);
    }

    public int getBloodLevels() {
        return bloodLevels;
    }

    public void setBloodLevels(int levels) {
        this.bloodLevels = Math.max(0, Math.min(20, levels));
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("BloodLevels", bloodLevels);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        bloodLevels = tag.getInt("BloodLevels");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
