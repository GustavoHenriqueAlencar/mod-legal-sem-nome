package com.occultum.common.botany.wild;

import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BloodRoseBlockEntity extends BlockEntity {
    private int storedBlood = 0;
    private String lastVictimSignature = "";
    private String lastVictimName = "";
    private static final int MAX_BLOOD = 1000;

    public BloodRoseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLOOD_ROSE_BE.get(), pos, state);
    }

    public void addBlood(int amount) {
        this.storedBlood = Math.min(MAX_BLOOD, this.storedBlood + amount);
        setChanged();
    }

    public void recordBloodSample(net.minecraft.world.entity.LivingEntity victim, int amount) {
        this.storedBlood = Math.min(MAX_BLOOD, this.storedBlood + amount);
        this.lastVictimSignature = victim.getUUID().toString();
        this.lastVictimName = victim.getName().getString();
        setChanged();
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
        this.storedBlood = 0;
        setChanged();
    }

    public int getStoredBlood() {
        return storedBlood;
    }

    public int drainBlood(int amount) {
        int drained = Math.min(amount, this.storedBlood);
        this.storedBlood -= drained;
        setChanged();
        return drained;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("StoredBlood", storedBlood);
        tag.putString("LastVictimSignature", lastVictimSignature);
        tag.putString("LastVictimName", lastVictimName);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.storedBlood = tag.getInt("StoredBlood");
        this.lastVictimSignature = tag.getString("LastVictimSignature");
        this.lastVictimName = tag.getString("LastVictimName");
    }
}
