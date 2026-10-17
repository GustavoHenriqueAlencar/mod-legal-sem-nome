package com.occultum.common.ritual;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.altar.GrassperBlockEntity;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BlockEntity do Glifo Dourado central.
 * Gerencia a ativação de todos os 96 Rituais (instantâneos, temporizados e de manutenção contínua/upkeep),
 * coleta de itens no solo e Preensores de Grama (Grasspers) num raio de 5 blocos, sacrifícios vivos e Coven.
 */
public class GoldenChalkBlockEntity extends BlockEntity {

    private int activeRitualId = 0;
    private int upkeepTickCounter = 0;
    private int remainingDurationTicks = -1; // -1 = contínuo indefinido enquanto houver energia; >0 = temporizado
    @Nullable
    private UUID activatorUuid = null;
    private int covenSize = 0;

    public GoldenChalkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOLDEN_CHALK_BE.get(), pos, state);
    }

    public boolean isActive() {
        return activeRitualId > 0;
    }

    public int getActiveRitualId() {
        return activeRitualId;
    }

    public void onRightClick(Player player, boolean usedSeerStone) {
        if (level == null || level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        // Seção 3.4: Desativação manual clicando com botão direito no Glifo Dourado ativo
        if (isActive()) {
            stopRitual(false);
            player.displayClientMessage(Component.translatable("message.occultum.ritual.aborted").withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        // 1. Detectar anéis concêntricos na matriz 17x17
        CircleGeometryValidator.DetectedRings rings = CircleGeometryValidator.detectAllRings(serverLevel, worldPosition);

        // 2. Determinar tamanho do Coven (via Pedra de Vidência ou bruxas próximas)
        int detectedCoven = Math.max(0, player.getData(ModAttachmentTypes.COVEN_LEVEL.get()));
        if (usedSeerStone && detectedCoven > 0) {
            summonCovenWitchesAroundCircle(serverLevel, player, detectedCoven);
        } else {
            // Conta também bruxas aliadas já presentes em raio de 12 blocos
            List<Witch> nearbyWitches = serverLevel.getEntitiesOfClass(Witch.class, new AABB(worldPosition).inflate(12.0));
            detectedCoven = Math.max(detectedCoven, Math.min(6, nearbyWitches.size()));
        }

        // 3. Coletar itens no chão (dentro do perímetro) e em Preensores de Grama (raio 5 blocos)
        int scanRadius = Math.max(5, rings.maxRadius());
        List<RitualItemSource> availableItems = collectAvailableItems(serverLevel, worldPosition, scanRadius);

        // 4. Coletar entidades vivas dentro do perímetro para sacrifício vivo
        List<LivingEntity> livingInCircle = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(worldPosition).inflate(scanRadius, 4.0, scanRadius),
                e -> e.isAlive() && !(e instanceof Player)
        );

        // 5. Procurar ritual correspondente no catálogo dos 96 Rituais
        CircleRitualRegistry.RitualDefinition matched = CircleRitualRegistry.findMatchingRitual(
                serverLevel, worldPosition, player, rings, availableItems, livingInCircle, detectedCoven
        );

        if (matched == null) {
            // Seção 5.1: Ritual Desconhecido (Unknown Ritual) -> estalido seco percussivo + fumaça cinza
            serverLevel.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1.0F, 0.8F);
            serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.2, worldPosition.getZ() + 0.5, 25, 0.35, 0.2, 0.35, 0.02);
            player.displayClientMessage(Component.translatable("message.occultum.ritual.unknown").withStyle(ChatFormatting.RED), true);
            return;
        }

        // 6. Verificar e drenar o custo inicial de energia do Altar
        if (matched.initialAltarCost() > 0 && !AltarPowerNetwork.consumePower(serverLevel, worldPosition, matched.initialAltarCost())) {
            // Seção 5.2: Déficit de Energia de Altar -> sons estridentes + faíscas vermelhas
            serverLevel.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.BLOCKS, 1.2F, 0.5F);
            serverLevel.sendParticles(ParticleTypes.FLAME, worldPosition.getX() + 0.5, worldPosition.getY() + 0.2, worldPosition.getZ() + 0.5, 20, 0.3, 0.2, 0.3, 0.03);
            player.displayClientMessage(Component.translatable("message.occultum.ritual.no_power", matched.initialAltarCost()).withStyle(ChatFormatting.RED), true);
            return;
        }

        // 7. Consumir itens de foco e sacrifício vivo, preservando metadados necessários (Taglock, Waystone, Talisman)
        CircleRitualRegistry.ConsumedRitualContext context = matched.consumeInputs(
                serverLevel, worldPosition, player, rings, availableItems, livingInCircle, detectedCoven
        );

        // 8. Executar efeito inicial do ritual
        matched.executeInitial(context);
        serverLevel.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 30, 0.5, 0.4, 0.5, 0.05);

        // 9. Se for ritual de manutenção contínua (upkeep) ou temporizado, manter o Glifo Dourado ativo
        if (matched.upkeepCostPerSecond() > 0 || matched.fixedDurationTicks() > 0) {
            this.activeRitualId = matched.id();
            this.upkeepTickCounter = 0;
            this.remainingDurationTicks = matched.fixedDurationTicks() > 0 ? matched.fixedDurationTicks() : -1;
            this.activatorUuid = player.getUUID();
            this.covenSize = detectedCoven;
            setBlockActiveState(true);
            sync();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GoldenChalkBlockEntity be) {
        if (!be.isActive() || !(level instanceof ServerLevel serverLevel)) return;

        CircleRitualRegistry.RitualDefinition def = CircleRitualRegistry.getById(be.activeRitualId);
        if (def == null) {
            be.stopRitual(true);
            return;
        }

        // Verificar integridade geométrica dos anéis requeridos a cada 20 ticks
        if (be.upkeepTickCounter % 20 == 0) {
            CircleGeometryValidator.DetectedRings rings = CircleGeometryValidator.detectAllRings(serverLevel, pos);
            if (!rings.matches(def.smallRing(), def.mediumRing(), def.largeRing())) {
                be.stopRitual(true);
                return;
            }

            // Drenar taxa de manutenção (upkeep) a cada 20 ticks (1 segundo)
            if (def.upkeepCostPerSecond() > 0) {
                if (!AltarPowerNetwork.consumePower(serverLevel, pos, def.upkeepCostPerSecond())) {
                    be.stopRitual(true);
                    return;
                }
            }
        }

        be.upkeepTickCounter++;
        Player activator = be.activatorUuid != null ? serverLevel.getPlayerByUUID(be.activatorUuid) : null;
        def.executeUpkeepTick(serverLevel, pos, activator, be.upkeepTickCounter, be.covenSize);

        if (be.remainingDurationTicks > 0) {
            be.remainingDurationTicks--;
            if (be.remainingDurationTicks == 0) {
                be.stopRitual(false);
            }
        }
    }

    public void onGlyphBroken(BlockPos brokenPos) {
        if (!isActive() || level == null || level.isClientSide) return;
        CircleRitualRegistry.RitualDefinition def = CircleRitualRegistry.getById(activeRitualId);
        if (def != null) {
            CircleGeometryValidator.DetectedRings rings = CircleGeometryValidator.detectAllRings(level, worldPosition);
            if (!rings.matches(def.smallRing(), def.mediumRing(), def.largeRing())) {
                stopRitual(true);
            }
        }
    }

    public void stopRitual(boolean failed) {
        this.activeRitualId = 0;
        this.upkeepTickCounter = 0;
        this.remainingDurationTicks = -1;
        setBlockActiveState(false);
        if (level instanceof ServerLevel sl) {
            if (failed) {
                sl.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.7F);
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.3, worldPosition.getZ() + 0.5, 20, 0.4, 0.2, 0.4, 0.03);
            } else {
                sl.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        }
        sync();
    }

    private void setBlockActiveState(boolean active) {
        if (level != null && getBlockState().hasProperty(GoldenChalkBlock.ACTIVE)
                && getBlockState().getValue(GoldenChalkBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, getBlockState().setValue(GoldenChalkBlock.ACTIVE, active), 3);
        }
    }

    private void summonCovenWitchesAroundCircle(ServerLevel level, Player player, int count) {
        int toSpawn = Math.min(6, count);
        List<Witch> existing = level.getEntitiesOfClass(Witch.class, new AABB(worldPosition).inflate(14.0));
        for (int i = existing.size(); i < toSpawn; i++) {
            double angle = (2.0 * Math.PI * i) / toSpawn;
            double wx = worldPosition.getX() + 0.5 + Math.cos(angle) * 4.5;
            double wz = worldPosition.getZ() + 0.5 + Math.sin(angle) * 4.5;
            Witch witch = EntityType.WITCH.create(level);
            if (witch != null) {
                witch.moveTo(wx, worldPosition.getY(), wz, (float) Math.toDegrees(angle), 0.0F);
                witch.setCustomName(Component.translatable("entity.occultum.coven_witch", player.getName()));
                witch.setPersistenceRequired();
                level.addFreshEntity(witch);
            }
        }
        level.sendParticles(ParticleTypes.PORTAL, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, 30, 3.0, 0.5, 3.0, 0.1);
    }

    public record RitualItemSource(ItemStack stack, BlockPos pos, @Nullable ItemEntity entity, @Nullable GrassperBlockEntity grassper) {
        public ItemStack consumeOne() {
            ItemStack copy = stack.copyWithCount(1);
            stack.shrink(1);
            if (entity != null && stack.isEmpty()) {
                entity.discard();
            }
            if (grassper != null) {
                grassper.setHeldItem(stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
            return copy;
        }
    }

    public static List<RitualItemSource> collectAvailableItems(ServerLevel level, BlockPos center, int radius) {
        List<RitualItemSource> list = new ArrayList<>();
        // 1. Entidades de item dropadas no chão dentro do perímetro
        AABB box = new AABB(center).inflate(radius, 2.0, radius);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive)) {
            if (!itemEntity.getItem().isEmpty()) {
                list.add(new RitualItemSource(itemEntity.getItem(), itemEntity.blockPosition(), itemEntity, null));
            }
        }
        // 2. Preensores de Grama (Grasspers) em um raio cúbico de 5 blocos
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-5, -2, -5), center.offset(5, 2, 5))) {
            if (level.getBlockEntity(p) instanceof GrassperBlockEntity grassper && !grassper.getHeldItem().isEmpty()) {
                list.add(new RitualItemSource(grassper.getHeldItem(), p.immutable(), null, grassper));
            }
        }
        return list;
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ActiveRitualId", activeRitualId);
        tag.putInt("UpkeepTickCounter", upkeepTickCounter);
        tag.putInt("RemainingDurationTicks", remainingDurationTicks);
        tag.putInt("CovenSize", covenSize);
        if (activatorUuid != null) {
            tag.putUUID("ActivatorUUID", activatorUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.activeRitualId = tag.getInt("ActiveRitualId");
        this.upkeepTickCounter = tag.getInt("UpkeepTickCounter");
        this.remainingDurationTicks = tag.getInt("RemainingDurationTicks");
        this.covenSize = tag.getInt("CovenSize");
        this.activatorUuid = tag.hasUUID("ActivatorUUID") ? tag.getUUID("ActivatorUUID") : null;
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
