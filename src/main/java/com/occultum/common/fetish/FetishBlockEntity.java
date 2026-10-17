package com.occultum.common.fetish;

import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BlockEntity dos Fetiches (Seção 6.2 e 6.3 de 06_POPPETS_E_FETISHES.md):
 * - 5 Modos de Operação: PLAYER_WHITELIST, PLAYER_BLACKLIST, CREATURE_WHITELIST, ABSENCE_ALARM, DISABLED.
 * - 6 Efeitos de Espíritos Infundidos:
 *   1. POPPET_ENHANCEMENT (1 Espectro + 1 Banshee + 1 Poltergeist, raio 16)
 *   2. SENTINEL (3 Espectros, raio 16, feixe espectral 6 de dano + repulsão)
 *   3. SCREAMER (2 Banshees, raio 20, grito de alarme + Náusea II e Lentidão II por 15s)
 *   4. TWISTER (2 Poltergeists, raio 16, recolhe itens e arremessa invasores pelos ares em rotação)
 *   5. GHOST_WALKER (1 Espectro + 1 Banshee, raio 16, invisibilidade contínua para autorizados)
 *   6. DEATH_FETISH (5 Espectros + 5 Banshees + 5 Poltergeists, raio 24, 4 pts de dano por segundo ignorando armadura)
 */
public class FetishBlockEntity extends BlockEntity {

    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_FETISHES = new ConcurrentHashMap<>();

    public enum OperationMode {
        PLAYER_WHITELIST,
        PLAYER_BLACKLIST,
        CREATURE_WHITELIST,
        ABSENCE_ALARM,
        DISABLED;

        public OperationMode next() {
            OperationMode[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public enum SpiritEffect {
        NONE(0),
        POPPET_ENHANCEMENT(16),
        SENTINEL(16),
        SCREAMER(20),
        TWISTER(16),
        GHOST_WALKER(16),
        DEATH_FETISH(24);

        private final int radius;

        SpiritEffect(int radius) {
            this.radius = radius;
        }

        public int getRadius() {
            return radius;
        }

        public SpiritEffect next() {
            SpiritEffect[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }

        public static SpiritEffect fromCapturedString(String raw, SpiritEffect fallback) {
            String lower = raw.toLowerCase();
            if (lower.contains("spectre:5") && lower.contains("banshee:5") && lower.contains("poltergeist:5")) {
                return DEATH_FETISH;
            }
            if (lower.contains("spectre") && lower.contains("banshee") && lower.contains("poltergeist")) {
                return POPPET_ENHANCEMENT;
            }
            if (lower.contains("spectre") && lower.contains("banshee")) {
                return GHOST_WALKER;
            }
            if (lower.contains("poltergeist")) {
                return TWISTER;
            }
            if (lower.contains("banshee")) {
                return SCREAMER;
            }
            if (lower.contains("spectre")) {
                return SENTINEL;
            }
            return fallback;
        }
    }

    private String ownerUuid = "";
    private final Set<String> listedPlayerUuids = new HashSet<>();
    private OperationMode operationMode = OperationMode.PLAYER_WHITELIST;
    private SpiritEffect spiritEffect = SpiritEffect.SENTINEL;

    public FetishBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FETISH_BE.get(), pos, state);
    }

    public static boolean hasPoppetEnhancementNearby(Level level, BlockPos pos) {
        Set<BlockPos> set = ACTIVE_FETISHES.get(level.dimension());
        if (set == null || set.isEmpty()) return false;
        for (BlockPos fPos : set) {
            if (fPos.distSqr(pos) <= 16.0 * 16.0 && level.getBlockEntity(fPos) instanceof FetishBlockEntity fetish) {
                if (fetish.operationMode != OperationMode.DISABLED && fetish.spiritEffect == SpiritEffect.POPPET_ENHANCEMENT) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            ACTIVE_FETISHES.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(worldPosition.immutable());
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            Set<BlockPos> set = ACTIVE_FETISHES.get(level.dimension());
            if (set != null) set.remove(worldPosition);
        }
        super.setRemoved();
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
        this.listedPlayerUuids.add(ownerUuid);
        setChanged();
    }

    public SpiritEffect getSpiritEffect() {
        return spiritEffect;
    }

    public void setSpiritEffect(SpiritEffect spiritEffect) {
        this.spiritEffect = spiritEffect;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public OperationMode getOperationMode() {
        return operationMode;
    }

    public void cycleOperationMode(Player player) {
        this.operationMode = this.operationMode.next();
        setChanged();
        player.displayClientMessage(Component.translatable("message.occultum.fetish.mode", operationMode.name()).withStyle(ChatFormatting.GOLD), true);
    }

    public void cycleSpiritEffect(Player player) {
        this.spiritEffect = this.spiritEffect.next();
        setChanged();
        player.displayClientMessage(Component.translatable("message.occultum.fetish.spirit", spiritEffect.name()).withStyle(ChatFormatting.AQUA), true);
    }

    public void toggleListedPlayer(Player actor, String targetUuid, String targetName) {
        if (listedPlayerUuids.contains(targetUuid)) {
            listedPlayerUuids.remove(targetUuid);
            actor.displayClientMessage(Component.translatable("message.occultum.fetish.removed", targetName).withStyle(ChatFormatting.YELLOW), true);
        } else {
            listedPlayerUuids.add(targetUuid);
            actor.displayClientMessage(Component.translatable("message.occultum.fetish.added", targetName).withStyle(ChatFormatting.GREEN), true);
        }
        setChanged();
    }

    private boolean isAuthorized(LivingEntity entity, boolean ownerPresentInArea) {
        if (entity instanceof Player p) {
            String id = p.getUUID().toString();
            if (id.equals(ownerUuid)) return true;
            return switch (operationMode) {
                case PLAYER_WHITELIST -> listedPlayerUuids.contains(id);
                case PLAYER_BLACKLIST -> !listedPlayerUuids.contains(id);
                case CREATURE_WHITELIST -> true;
                case ABSENCE_ALARM -> ownerPresentInArea || listedPlayerUuids.contains(id);
                case DISABLED -> true;
            };
        }
        if (entity instanceof Animal) {
            return operationMode == OperationMode.CREATURE_WHITELIST || operationMode == OperationMode.PLAYER_BLACKLIST;
        }
        if (entity instanceof Monster) {
            return operationMode == OperationMode.PLAYER_BLACKLIST;
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FetishBlockEntity fetish) {
        if (fetish.operationMode == OperationMode.DISABLED || fetish.spiritEffect == SpiritEffect.NONE) {
            return;
        }
        if (level.getGameTime() % 20 != 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int radius = fetish.spiritEffect.getRadius();
        AABB area = new AABB(pos).inflate(radius);
        boolean ownerPresent = false;
        if (!fetish.ownerUuid.isEmpty()) {
            for (Player p : serverLevel.getEntitiesOfClass(Player.class, area)) {
                if (p.getUUID().toString().equals(fetish.ownerUuid) || fetish.listedPlayerUuids.contains(p.getUUID().toString())) {
                    ownerPresent = true;
                    break;
                }
            }
        }

        switch (fetish.spiritEffect) {
            case POPPET_ENHANCEMENT -> {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
            case SENTINEL -> {
                for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (!fetish.isAuthorized(e, ownerPresent)) {
                        e.hurt(serverLevel.damageSources().magic(), 6.0F);
                        Vec3 push = e.position().subtract(Vec3.atCenterOf(pos)).normalize().scale(0.9D);
                        e.setDeltaMovement(push.x, 0.35D, push.z);
                        e.hurtMarked = true;
                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, e.getX(), e.getY() + 1.0, e.getZ(), 10, 0.2, 0.3, 0.2, 0.03);
                    }
                }
            }
            case SCREAMER -> {
                boolean intruderFound = false;
                for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (!fetish.isAuthorized(e, ownerPresent)) {
                        intruderFound = true;
                        e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 1));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
                    }
                }
                if (intruderFound) {
                    serverLevel.playSound(null, pos, SoundEvents.GHAST_SCREAM, SoundSource.BLOCKS, 1.5F, 1.4F);
                    if (!fetish.ownerUuid.isEmpty()) {
                        try {
                            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(UUID.fromString(fetish.ownerUuid));
                            if (owner != null) {
                                owner.displayClientMessage(Component.translatable("message.occultum.fetish.screamer_alert",
                                        pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.RED), true);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
            case TWISTER -> {
                for (ItemEntity ie : serverLevel.getEntitiesOfClass(ItemEntity.class, area)) {
                    Vec3 pull = Vec3.atCenterOf(pos.above()).subtract(ie.position()).scale(0.25D);
                    ie.setDeltaMovement(pull);
                }
                for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (!fetish.isAuthorized(e, ownerPresent)) {
                        e.setDeltaMovement((serverLevel.random.nextDouble() - 0.5) * 0.8D, 1.05D, (serverLevel.random.nextDouble() - 0.5) * 0.8D);
                        e.setYRot(e.getYRot() + 90.0F);
                        e.hurtMarked = true;
                    }
                }
            }
            case GHOST_WALKER -> {
                for (Player p : serverLevel.getEntitiesOfClass(Player.class, area)) {
                    if (fetish.isAuthorized(p, true)) {
                        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, true, false));
                    }
                }
            }
            case DEATH_FETISH -> {
                for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (!fetish.isAuthorized(e, ownerPresent)) {
                        // 2 corações (4.0 pts) por segundo ignorando armadura
                        e.hurt(serverLevel.damageSources().fellOutOfWorld(), 4.0F);
                        serverLevel.sendParticles(ParticleTypes.SMOKE, e.getX(), e.getY() + 1.0, e.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
                    }
                }
            }
            default -> {}
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("OwnerUUID", ownerUuid);
        tag.putString("OperationMode", operationMode.name());
        tag.putString("SpiritEffect", spiritEffect.name());
        ListTag list = new ListTag();
        for (String id : listedPlayerUuids) {
            list.add(StringTag.valueOf(id));
        }
        tag.put("ListedPlayers", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownerUuid = tag.getString("OwnerUUID");
        try {
            this.operationMode = OperationMode.valueOf(tag.getString("OperationMode"));
        } catch (IllegalArgumentException ignored) {
            this.operationMode = OperationMode.PLAYER_WHITELIST;
        }
        try {
            this.spiritEffect = SpiritEffect.valueOf(tag.getString("SpiritEffect"));
        } catch (IllegalArgumentException ignored) {
            this.spiritEffect = SpiritEffect.SENTINEL;
        }
        this.listedPlayerUuids.clear();
        ListTag list = tag.getList("ListedPlayers", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            this.listedPlayerUuids.add(list.getString(i));
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
