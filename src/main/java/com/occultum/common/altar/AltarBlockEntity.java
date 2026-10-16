package com.occultum.common.altar;

import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AltarBlockEntity extends BlockEntity {
    private boolean isFormed = false;
    private BlockPos corePos = BlockPos.ZERO;
    private final List<BlockPos> altarBlocks = new ArrayList<>();

    private double currentPower = 0.0D;
    private int basePower = 0;
    private int maxPower = 0;

    private int capMultiplier = 1;
    private int rechargeMultiplier = 1;
    private int rangeMultiplier = 1;
    private int upgradeLevel = 0;

    private int ticksSinceLastScan = 100; // Throttling (min 100 ticks)
    private int rechargeTimer = 0; // Rotina de 20 ticks (1s)

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        if (!altar.isFormed || !altar.isCore()) {
            return;
        }

        altar.ticksSinceLastScan++;
        altar.rechargeTimer++;

        // Rotina periódica de 20 ticks (1 segundo real): P_atual = min(P_atual + 10 * M_rec, P_max), onde P_max = P_base * M_cap
        if (altar.rechargeTimer >= 20) {
            altar.rechargeTimer = 0;

            altar.maxPower = altar.basePower * altar.capMultiplier;
            double rechargeAmount = 10.0D * altar.rechargeMultiplier;
            altar.currentPower = Math.min(altar.currentPower + rechargeAmount, (double) altar.maxPower);

            if (altar.currentPower > altar.maxPower) {
                altar.currentPower = altar.maxPower;
            }

            altar.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        if (!altar.isFormed || !altar.isCore()) return;

        // Efeito visual particular no cliente:
        // Seleciona aleatoriamente coordenadas no volume X +- 16, Y +- 4, Z +- 16
        if (level.random.nextInt(3) == 0) {
            int rx = pos.getX() + level.random.nextInt(33) - 16;
            int ry = pos.getY() + level.random.nextInt(9) - 4;
            int rz = pos.getZ() + level.random.nextInt(33) - 16;
            BlockPos targetPos = new BlockPos(rx, ry, rz);
            BlockState targetState = level.getBlockState(targetPos);

            if (targetState.is(BlockTags.LEAVES) || targetState.is(BlockTags.CROPS) || targetState.is(BlockTags.FLOWERS)
                    || targetState.is(Blocks.SHORT_GRASS) || targetState.is(Blocks.FERN)) {
                // Trajetória em direção ao altar com velocidade de 0.25 blocos por tick
                double dx = pos.getX() + 0.5 - (rx + 0.5);
                double dy = pos.getY() + 0.9 - (ry + 0.5);
                double dz = pos.getZ() + 0.5 - (rz + 0.5);
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > 0.5) {
                    double speed = 0.25;
                    double vx = (dx / dist) * speed;
                    double vy = (dy / dist) * speed;
                    double vz = (dz / dist) * speed;
                    level.addParticle(ParticleTypes.HAPPY_VILLAGER, rx + 0.5, ry + 0.5, rz + 0.5, vx, vy, vz);
                }
            }
        }
    }

    public void formAltar(BlockPos corePos, List<BlockPos> members) {
        this.isFormed = true;
        this.corePos = corePos.immutable();
        this.altarBlocks.clear();
        for (BlockPos p : members) {
            this.altarBlocks.add(p.immutable());
        }

        if (level != null && !level.isClientSide) {
            AltarPowerNetwork.registerAltar(level, this.corePos);
            // Escaneamento inicial forçado
            scanEnvironment(true);
        }
        setChanged();
    }

    public void invalidateAltar() {
        if (level != null && !level.isClientSide && isCore()) {
            AltarPowerNetwork.unregisterAltar(level, this.corePos);
        }

        this.isFormed = false;
        this.currentPower = 0.0D;
        this.basePower = 0;
        this.maxPower = 0;
        this.capMultiplier = 1;
        this.rechargeMultiplier = 1;
        this.rangeMultiplier = 1;
        this.upgradeLevel = 0;
        this.altarBlocks.clear();
        setChanged();
    }

    public void scanEnvironment(boolean force) {
        if (level == null || level.isClientSide || !isCore()) return;

        // Throttling de 100 ticks (5 segundos)
        if (!force && ticksSinceLastScan < 100) {
            return;
        }
        ticksSinceLastScan = 0;

        // 1. Avalia artefatos superiores
        AltarArtifactEvaluator.Modifiers mods = AltarArtifactEvaluator.evaluate(level, this.altarBlocks);
        this.capMultiplier = mods.capMultiplier();
        this.rechargeMultiplier = mods.rechargeMultiplier();
        this.rangeMultiplier = mods.rangeMultiplier();
        this.upgradeLevel = mods.upgradeLevel();

        // 2. Varredura dos 24.389 blocos cúbicos (29x29x29)
        this.basePower = AltarNatureScanner.scanNatureVolume(level, this.corePos);

        // 3. Cálculo da capacidade máxima efetiva: P_max = P_base * M_cap
        this.maxPower = this.basePower * this.capMultiplier;
        if (this.currentPower > this.maxPower) {
            this.currentPower = this.maxPower;
        }

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean tryConsumePower(int cost) {
        return tryConsumePower((double) cost);
    }

    public boolean tryConsumePower(double cost) {
        if (cost <= 0.0D) return true;
        if (this.currentPower + 1e-6D >= cost) {
            this.currentPower = Math.max(0.0D, this.currentPower - cost);
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    public boolean isCore() {
        return this.worldPosition.equals(this.corePos);
    }

    public boolean isFormed() {
        return isFormed;
    }

    public int getCurrentPower() {
        return (int) Math.floor(currentPower + 1e-6D);
    }

    public double getCurrentPowerExact() {
        return currentPower;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getBasePower() {
        return basePower;
    }

    public int getRechargeMultiplier() {
        return rechargeMultiplier;
    }

    public int getRangeMultiplier() {
        return rangeMultiplier;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public List<BlockPos> getAltarBlocks() {
        return altarBlocks;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("IsFormed", isFormed);
        tag.put("CorePos", NbtUtils.writeBlockPos(corePos));
        tag.putDouble("CurrentPower", currentPower);
        tag.putInt("BasePower", basePower);
        tag.putInt("MaxPower", maxPower);
        tag.putInt("CapMultiplier", capMultiplier);
        tag.putInt("RechargeMultiplier", rechargeMultiplier);
        tag.putInt("RangeMultiplier", rangeMultiplier);
        tag.putInt("UpgradeLevel", upgradeLevel);

        ListTag list = new ListTag();
        for (BlockPos p : altarBlocks) {
            list.add(NbtUtils.writeBlockPos(p));
        }
        tag.put("AltarBlocks", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        isFormed = tag.getBoolean("IsFormed");
        if (tag.contains("CorePos")) {
            corePos = NbtUtils.readBlockPos(tag, "CorePos").orElse(BlockPos.ZERO);
        }
        currentPower = tag.getDouble("CurrentPower");
        basePower = tag.getInt("BasePower");
        maxPower = tag.getInt("MaxPower");
        capMultiplier = tag.getInt("CapMultiplier");
        rechargeMultiplier = tag.getInt("RechargeMultiplier");
        rangeMultiplier = tag.getInt("RangeMultiplier");
        upgradeLevel = tag.getInt("UpgradeLevel");

        altarBlocks.clear();
        if (tag.contains("AltarBlocks", Tag.TAG_LIST)) {
            ListTag list = tag.getList("AltarBlocks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                NbtUtils.readBlockPos(list.getCompound(i), "").ifPresent(altarBlocks::add);
            }
        }
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
