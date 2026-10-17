package com.occultum.common.entity;

import com.occultum.common.spirit.SpiritWorldManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Corpo Físico Adormecido (CorpseEntity - Seção 3.2 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - Materializado no Overworld quando o jogador entra no Mundo Espiritual via Poção do Sono ou Maçã Adormecida.
 * - Herda o nome, a skin/textura e os pontos de vida exatos do jogador.
 * - Fica deitado e imóvel no leito/chão.
 * - Se sofrer dano no Overworld, reflete o dano na consciência do jogador e força seu despertar imediato.
 */
public class CorpseEntity extends Mob {

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);

    public CorpseEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setPose(Pose.SLEEPING);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_OWNER_NAME, "");
    }

    public void setOwner(Player player) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(player.getUUID()));
        this.entityData.set(DATA_OWNER_NAME, player.getName().getString());
        this.setCustomName(player.getName());
        this.setCustomNameVisible(true);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (!this.level().isClientSide && this.getServer() != null) {
            UUID ownerId = getOwnerUUID();
            if (ownerId != null) {
                ServerPlayer owner = this.getServer().getPlayerList().getPlayer(ownerId);
                if (owner != null) {
                    // Força o despertar súbito e violento refletindo o dano sofrido pelo corpo físico
                    SpiritWorldManager.returnFromSpiritWorld(owner, false);
                    owner.hurt(source, amount);
                    owner.displayClientMessage(
                            Component.translatable("message.occultum.spirit.corpse_attacked").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                            false
                    );
                }
            }
            this.discard();
        }
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID id = getOwnerUUID();
        if (id != null) {
            tag.putUUID("OwnerUUID", id);
        }
        tag.putString("OwnerName", getOwnerName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID("OwnerUUID")));
        }
        if (tag.contains("OwnerName")) {
            this.entityData.set(DATA_OWNER_NAME, tag.getString("OwnerName"));
        }
    }
}
