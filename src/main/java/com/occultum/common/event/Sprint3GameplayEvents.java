package com.occultum.common.event;

import com.occultum.OccultumMod;
import com.occultum.common.divination.FamiliarSystem;
import com.occultum.common.divination.ProphecyEngine;
import com.occultum.common.infusion.InfusionPowerHandler;
import com.occultum.common.infusion.MysticBranchItem;
import com.occultum.common.poppet.PoppetHandler;
import com.occultum.common.poppet.TaglockKitItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Eventos Globais da Sprint 3:
 * - Interceptação de dano pelos 11 Poppets, Infusões e Profecias da Bola de Cristal.
 * - Verificação por tick de Ferramentas/Armaduras (Poppets), Habilidades de Infusão, Motor de Profecias e Familiares.
 * - Habilidades ativas de Infusão (clique esquerdo/direito/agachado) e Bombardeio Aéreo da Coruja Familiar.
 * - Fechadura Arcana (Colloportus) do Ramo Místico e registro de camas para o Kit Taglock.
 */
@EventBusSubscriber(modid = OccultumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Sprint3GameplayEvents {

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;

        if (PoppetHandler.onPlayerIncomingDamage(player, event.getSource(), event.getAmount())) {
            event.setCanceled(true);
            return;
        }

        if (InfusionPowerHandler.onPlayerIncomingDamage(player, event.getSource(), event.getAmount())) {
            event.setCanceled(true);
            return;
        }

        if (player.level() instanceof ServerLevel sl) {
            ProphecyEngine.onPlayerHurt(sl, player, event.getSource(), event.getAmount());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        PoppetHandler.checkEquipmentProtectionPoppets(player);
        InfusionPowerHandler.onPlayerTick(player);
        ProphecyEngine.onPlayerTick(player);
        FamiliarSystem.onPlayerTick(player);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (event.getTarget() instanceof LivingEntity target) {
            InfusionPowerHandler.onLeftClickEntity(player, target);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // Se agachado com mão vazia e Coruja Familiar com poção: ordena Bombardeio Aéreo
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && player.level() instanceof ServerLevel sl) {
            if (FamiliarSystem.tryCommandOwlAerialBombing(sl, player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && player.level() instanceof ServerLevel sl) {
            FamiliarSystem.tryCommandOwlAerialBombing(sl, player);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);

        // Verifica se a porta ou alçapão está selado com Colloportus (Clausura)
        if ((state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock)
                && MysticBranchItem.isDoorLocked(pos)) {
            event.setCanceled(true);
            if (!event.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("message.occultum.spell.door_locked")
                        .withStyle(ChatFormatting.RED), true);
            }
            return;
        }

        if (!event.getLevel().isClientSide && player.getItemInHand(event.getHand()).isEmpty()) {
            InfusionPowerHandler.onRightClickEmptyHand(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.getMainHandItem().isEmpty()) {
            InfusionPowerHandler.onRightClickEmptyHand(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerSleep(CanPlayerSleepEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            TaglockKitItem.recordBedSleep(event.getPos(), player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!player.level().isClientSide && player.level() instanceof ServerLevel sl) {
            ProphecyEngine.onBlockBroken(sl, player, event.getPos(), event.getState());
        }
    }
}
