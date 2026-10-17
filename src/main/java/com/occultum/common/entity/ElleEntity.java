package com.occultum.common.entity;

import com.occultum.common.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Elle, a Mensageira de Lilith (Seção 2.3 Transição 0 -> 1 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Invocada pelo raio no altar 7x7 de crânio com sangue de galinha.
 * - Segue o invocador e caminha até uma piscina profunda de lava no Nether (pelo menos 4 blocos de profundidade
 *   ou imersa em lava no Nether), onde explode violentamente e invoca Lilith.
 */
public class ElleEntity extends PathfinderMob {

    @Nullable
    private UUID summonerUUID;

    public ElleEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    public void setSummonerUUID(@Nullable UUID uuid) {
        this.summonerUUID = uuid;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel sl)) return;

        // Segue o jogador que a invocou se estiver a mais de 4 blocos
        if (summonerUUID != null && this.tickCount % 20 == 0) {
            Player summoner = sl.getPlayerByUUID(summonerUUID);
            if (summoner != null && summoner.distanceToSqr(this) > 16.0D && summoner.distanceToSqr(this) < 1024.0D) {
                this.getNavigation().moveTo(summoner, 1.15D);
            }
        }

        // No Nether, procura piscina de lava próxima e caminha até ela
        if (sl.dimension() == Level.NETHER) {
            if (this.isInLava() || isNearDeepLavaPool(sl, this.blockPosition())) {
                // Explode violentamente e invoca Lilith!
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
                sl.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 1.0, 1.0, 1.0, 0.1);
                sl.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.7F);

                LilithEntity lilith = ModEntityTypes.LILITH.get().create(sl);
                if (lilith != null) {
                    lilith.moveTo(this.getX(), this.getY() + 1.0D, this.getZ(), this.getYRot(), 0.0F);
                    sl.addFreshEntity(lilith);
                }
                this.discard();
            } else if (this.tickCount % 40 == 0) {
                BlockPos lavaTarget = findNearbyLava(sl, this.blockPosition(), 12);
                if (lavaTarget != null) {
                    this.getNavigation().moveTo(lavaTarget.getX() + 0.5D, lavaTarget.getY(), lavaTarget.getZ() + 0.5D, 1.1D);
                }
            }
        }
    }

    private static boolean isNearDeepLavaPool(ServerLevel level, BlockPos pos) {
        int lavaColumns = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos check = pos.offset(dx, -1, dz);
                if (level.getFluidState(check).is(FluidTags.LAVA)) {
                    int depth = 0;
                    for (int dy = 0; dy >= -4; dy--) {
                        if (level.getFluidState(check.offset(0, dy, 0)).is(FluidTags.LAVA)) {
                            depth++;
                        }
                    }
                    if (depth >= 2) {
                        lavaColumns++;
                    }
                }
            }
        }
        return lavaColumns >= 6;
    }

    @Nullable
    private static BlockPos findNearbyLava(ServerLevel level, BlockPos origin, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-radius, -3, -radius), origin.offset(radius, 2, radius))) {
            if (level.getFluidState(p).is(FluidTags.LAVA)) {
                return p.immutable();
            }
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (summonerUUID != null) {
            tag.putUUID("SummonerUUID", summonerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("SummonerUUID")) {
            summonerUUID = tag.getUUID("SummonerUUID");
        }
    }
}
