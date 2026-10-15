package com.occultum.common.botany.wild;

import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CritterSnareBlockEntity extends BlockEntity {
    private String capturedCritter = ""; // "", "bat", "frog", "silverfish"

    public CritterSnareBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRITTER_SNARE_BE.get(), pos, state);
    }

    public boolean hasCritter() {
        return !capturedCritter.isEmpty();
    }

    public String getCapturedCritter() {
        return capturedCritter;
    }

    public void setCapturedCritter(String critter) {
        this.capturedCritter = critter;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("CapturedCritter", capturedCritter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.capturedCritter = tag.getString("CapturedCritter");
    }
}
