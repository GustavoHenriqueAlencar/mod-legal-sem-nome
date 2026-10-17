package com.occultum.common.mirror;

import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * BlockEntity do Espelho Místico (`MysticMirrorBlockEntity`):
 * - Armazena coordenadas vinculadas (`linkedPos`, `linkedDimension`), flag de purificação (`demonSlain`),
 *   cooldown de travessia (60 ticks), favorito do Oráculo (`favoritePlayerName`), lista de espionagem
 *   de jogadores que passaram a até 4 blocos na frente do espelho (`seenPlayers`) e timestamp da última
 *   geração de `FairestFollowerEntity`.
 */
public class MysticMirrorBlockEntity extends BlockEntity {

    @Nullable
    private BlockPos linkedPos;
    private String linkedDimension = "";
    private boolean demonSlain = false;
    private int cooldownTicks = 0;
    private int oracleFaceTicks = 0;
    private String favoritePlayerName = "";
    private long lastFairestSpawnTick = -2400L;
    private final Set<String> seenPlayers = new LinkedHashSet<>();

    public MysticMirrorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MYSTIC_MIRROR.get(), pos, blockState);
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (oracleFaceTicks > 0) {
            oracleFaceTicks--;
        }

        // Vigilância silenciosa a cada 20 ticks: detecta jogadores em um raio de 4 blocos na frente do espelho
        if (level.getGameTime() % 20L == 0L && state.getBlock() instanceof MysticMirrorBlock) {
            Direction facing = state.getValue(MysticMirrorBlock.FACING);
            BlockPos frontCenter = pos.relative(facing, 2);
            AABB spyBox = new AABB(frontCenter).inflate(4.0D);
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, spyBox)) {
                String name = player.getScoreboardName();
                if (!name.equals(favoritePlayerName)) {
                    if (seenPlayers.add(name)) {
                        setChanged();
                    }
                }
            }
        }
    }

    @Nullable
    public BlockPos getLinkedPos() {
        return linkedPos;
    }

    public void setLinkedPos(@Nullable BlockPos linkedPos) {
        this.linkedPos = linkedPos;
        setChanged();
    }

    public String getLinkedDimension() {
        return linkedDimension;
    }

    public void setLinkedDimension(String linkedDimension) {
        this.linkedDimension = linkedDimension == null ? "" : linkedDimension;
        setChanged();
    }

    public boolean isDemonSlain() {
        return demonSlain;
    }

    public void setDemonSlain(boolean demonSlain) {
        this.demonSlain = demonSlain;
        setChanged();
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
        setChanged();
    }

    public void activateOracleFace(int ticks) {
        this.oracleFaceTicks = ticks;
        setChanged();
    }

    public int getOracleFaceTicks() {
        return oracleFaceTicks;
    }

    public String getFavoritePlayerName() {
        return favoritePlayerName;
    }

    public void setFavoritePlayerName(String favoritePlayerName) {
        this.favoritePlayerName = favoritePlayerName == null ? "" : favoritePlayerName;
        setChanged();
    }

    public long getLastFairestSpawnTick() {
        return lastFairestSpawnTick;
    }

    public void setLastFairestSpawnTick(long lastFairestSpawnTick) {
        this.lastFairestSpawnTick = lastFairestSpawnTick;
        setChanged();
    }

    public List<String> consumeSeenPlayers(String excludeFavorite) {
        List<String> result = new ArrayList<>();
        for (String name : seenPlayers) {
            if (!name.equals(excludeFavorite)) {
                result.add(name);
            }
        }
        seenPlayers.clear();
        setChanged();
        return result;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (linkedPos != null) {
            tag.putInt("LinkedX", linkedPos.getX());
            tag.putInt("LinkedY", linkedPos.getY());
            tag.putInt("LinkedZ", linkedPos.getZ());
        }
        tag.putString("LinkedDim", linkedDimension);
        tag.putBoolean("DemonSlain", demonSlain);
        tag.putInt("Cooldown", cooldownTicks);
        tag.putString("FavoritePlayer", favoritePlayerName);
        tag.putLong("LastFairestSpawn", lastFairestSpawnTick);
        ListTag list = new ListTag();
        for (String s : seenPlayers) {
            list.add(StringTag.valueOf(s));
        }
        tag.put("SeenPlayers", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("LinkedX")) {
            linkedPos = new BlockPos(tag.getInt("LinkedX"), tag.getInt("LinkedY"), tag.getInt("LinkedZ"));
        } else {
            linkedPos = null;
        }
        linkedDimension = tag.getString("LinkedDim");
        demonSlain = tag.getBoolean("DemonSlain");
        cooldownTicks = tag.getInt("Cooldown");
        favoritePlayerName = tag.getString("FavoritePlayer");
        lastFairestSpawnTick = tag.getLong("LastFairestSpawn");
        seenPlayers.clear();
        ListTag list = tag.getList("SeenPlayers", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            seenPlayers.add(list.getString(i));
        }
    }
}
