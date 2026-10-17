package com.occultum.common.divination;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BlockEntity da Bola de Cristal (Seção 2.1 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md):
 * - Custo: 1.000 de poder do Altar em raio de 16 blocos.
 * - Cooldown: 100 segundos (2.000 ticks) por jogador.
 * - Verifica perícia do vidente (infusão ativa ou vestimenta de bruxa).
 */
public class CrystalBallBlockEntity extends BlockEntity {

    public static final int ALTAR_COST = 1000;
    public static final long COOLDOWN_TICKS = 2000L; // 100 segundos

    private final Map<UUID, Long> lastDivinationGameTime = new ConcurrentHashMap<>();

    public CrystalBallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_BALL_BE.get(), pos, state);
    }

    public void performDivination(Player player) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long now = serverLevel.getGameTime();
        Long last = lastDivinationGameTime.get(player.getUUID());
        if (last != null && now - last < COOLDOWN_TICKS) {
            long secsLeft = (COOLDOWN_TICKS - (now - last)) / 20L;
            player.displayClientMessage(Component.translatable("message.occultum.crystal_ball.cooldown", secsLeft).withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        if (!AltarPowerNetwork.consumePower(serverLevel, worldPosition, ALTAR_COST)) {
            player.displayClientMessage(Component.translatable("message.occultum.crystal_ball.no_power", ALTAR_COST).withStyle(ChatFormatting.RED), true);
            serverLevel.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.BLOCKS, 1.0F, 0.6F);
            return;
        }

        lastDivinationGameTime.put(player.getUUID(), now);

        // Seção 2.1: Perícia do Vidente (Infusão Mística ou Chapéu/Túnica de Bruxa)
        boolean skilled = !player.getData(ModAttachmentTypes.INFUSION_TYPE.get()).isEmpty()
                || player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.WITCHES_HAT.get())
                || player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.BABA_YAGAS_HAT.get())
                || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.WITCHES_ROBES.get())
                || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.NECROMANCER_ROBES.get());

        if (!skilled && serverLevel.random.nextFloat() < 0.25F) {
            player.displayClientMessage(Component.translatable("message.occultum.crystal_ball.murky").withStyle(ChatFormatting.GRAY), false);
            serverLevel.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5, 15, 0.2, 0.2, 0.2, 0.02);
            return;
        }

        ProphecyEngine.assignRandomProphecy(serverLevel, player);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.1);
        serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.0F);
    }
}
