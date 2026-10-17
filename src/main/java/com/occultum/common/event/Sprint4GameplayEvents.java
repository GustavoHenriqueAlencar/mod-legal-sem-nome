package com.occultum.common.event;

import com.occultum.OccultumMod;
import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.entity.NightmareEntity;
import com.occultum.common.mirror.MirrorDimensionManager;
import com.occultum.common.network.OccultumNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.spirit.DreamWeaverBlock;
import com.occultum.common.spirit.SpiritWorldManager;
import com.occultum.common.torment.TormentDimensionManager;
import com.occultum.common.vampire.VampirismSystem;
import com.occultum.common.werewolf.LycanthropySystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Eventos Globais da Sprint 4:
 * - Vampirismo: tick solar/regeneração/fome de sangue, imortalidade a 1.0 HP, morte por Estaca de Madeira,
 *   habilidades ativas (DRINK, MESMERIZE, SPEED, BAT, STORM, SWARM, FARM), quebra de pedra agachado,
 *   golpe cinético agachado e hitbox 0.5x0.5 em forma de Morcego.
 * - Licantropia: tick de lua cheia e atributos da Seção 3.4, Bote Vetorial (`Leap`), Teto Rígido de Dano
 *   (Damage Cap 4.0/3.0/2.0 HP), vulnerabilidade total a Prata (+5.0 puro, ignora cap e resistência),
 *   estraçalhar armadura no nível 9, uivo paralisante, escavação rápida, devorar carne e missões de lobos.
 * - Mundo Espiritual e Sonhos: retorno por morte no sonho, envio ao Tormento se abatido por `NightmareEntity`,
 *   e aplicação dos 5 Apanhadores de Sonhos (`Dream Weavers`) ao acordar.
 * - Dimensão do Espelho: interceptação dos 2 comandos vocais no chat (`"Mirror mirror send me home"` e
 *   `"Mirror mirror I give up"`).
 */
@EventBusSubscriber(modid = OccultumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class Sprint4GameplayEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        VampirismSystem.onPlayerTick(player);
        LycanthropySystem.onPlayerTick(player);

        if (player instanceof ServerPlayer sp && sp.tickCount % 20 == 0) {
            OccultumNetwork.syncToClient(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            OccultumNetwork.syncToClient(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            OccultumNetwork.syncToClient(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            OccultumNetwork.syncToClient(sp);
        }
    }

    /**
     * Ajusta dinamicamente a caixa de colisão (hitbox) para:
     * - Vampiro em Forma de Morcego: 0.5 x 0.5 blocos (Seção 2.4 de 08_VAMPIRISMO_E_LICANTROPIA.md).
     * - Lobisomem Quadrúpede (`WOLF`): 0.6 x 0.8 blocos.
     * - Homem-Lobo Bípede (`WOLFMAN`): 0.9 x 2.2 blocos.
     */
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getData(ModAttachmentTypes.VAMPIRE_BAT_FORM.get())) {
            event.setNewSize(EntityDimensions.scalable(0.5F, 0.5F));
            return;
        }
        String wolfForm = player.getData(ModAttachmentTypes.WEREWOLF_FORM.get());
        if ("WOLF".equals(wolfForm)) {
            event.setNewSize(EntityDimensions.scalable(0.6F, 0.8F));
        } else if ("WOLFMAN".equals(wolfForm)) {
            event.setNewSize(EntityDimensions.scalable(0.9F, 2.2F));
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        // 1. Regras de Dano e Imortalidade de Vampiro
        if (entity instanceof Player player && VampirismSystem.isVampire(player)) {
            if (VampirismSystem.onVampireIncomingDamage(player, event.getSource(), event.getAmount())) {
                event.setCanceled(true);
                return;
            }
        }

        // 2. Regras de Dano, Damage Cap e Vulnerabilidade à Prata do Lobisomem
        if (entity instanceof Player player && LycanthropySystem.getWerewolfLevel(player) >= 1) {
            float adjusted = LycanthropySystem.computeModifiedWerewolfDamage(player, event.getSource(), event.getAmount());
            event.setAmount(adjusted);
        }

        // 3. Progresso de missões de abate/infecção do Lobisomem e estraçalhar armadura no nível 9
        if (event.getSource().getEntity() instanceof Player attacker
                && LycanthropySystem.getWerewolfLevel(attacker) >= 1
                && attacker.level() instanceof ServerLevel sl) {
            LycanthropySystem.onWerewolfAttackEntity(sl, attacker, entity);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            LycanthropySystem.onPlayerJump(player);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(event.getTarget() instanceof LivingEntity target)) return;

        // Vampiro agachado na forma humana: Golpe Cinético Vampírico
        if (VampirismSystem.isVampire(player)) {
            VampirismSystem.onVampireAttackEntity(player, target);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel sl)
                || !(event.getTarget() instanceof LivingEntity target)) return;

        if (VampirismSystem.isVampire(player) && player.getItemInHand(event.getHand()).isEmpty()) {
            if (VampirismSystem.activateSelectedPower(sl, player, target)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel sl)) return;

        if (VampirismSystem.isVampire(player)) {
            if (VampirismSystem.onVampireLeftClickBlock(sl, player, event.getPos(), sl.getBlockState(event.getPos()))) {
                event.setCanceled(true);
                return;
            }
        }

        if (LycanthropySystem.getWerewolfLevel(player) >= 1) {
            if (LycanthropySystem.onWerewolfLeftClickBlock(sl, player, event.getPos(), sl.getBlockState(event.getPos()))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel sl)) return;

        if (VampirismSystem.isVampire(player) && player.getMainHandItem().isEmpty()) {
            if (player.isShiftKeyDown()) {
                VampirismSystem.cycleSelectedPower(player);
            } else {
                VampirismSystem.activateSelectedPower(sl, player, null);
            }
        } else if (LycanthropySystem.getWerewolfLevel(player) >= 3 && player.getMainHandItem().isEmpty()) {
            LycanthropySystem.performHowl(sl, player);
        }
    }

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        if (event.getAnimal() instanceof Wolf && event.getTamer() instanceof Player player && !player.level().isClientSide) {
            LycanthropySystem.onWolfTamed(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !(entity.level() instanceof ServerLevel sl)) return;

        // Eventos de abate por Vampiro (páginas do Livro dos Vampiros e Blazes no Nether) e por Lobisomem
        if (event.getSource().getEntity() instanceof Player killer) {
            VampirismSystem.onVampireKillEntity(sl, killer, entity);
            LycanthropySystem.onEntityKilled(sl, killer, entity);
        }

        // Morte de jogador no Mundo Espiritual ou por NightmareEntity
        if (entity instanceof ServerPlayer sp) {
            if (event.getSource().getEntity() instanceof NightmareEntity) {
                event.setCanceled(true);
                sp.setHealth(sp.getMaxHealth());
                if (sl.dimension() == OccultumDimensions.SPIRIT_WORLD_LEVEL) {
                    SpiritWorldManager.returnFromSpiritWorld(sp, true);
                }
                TormentDimensionManager.sendPlayerToTorment(sp);
                return;
            }
            if (sl.dimension() == OccultumDimensions.SPIRIT_WORLD_LEVEL) {
                event.setCanceled(true);
                sp.setHealth(sp.getMaxHealth());
                SpiritWorldManager.returnFromSpiritWorld(sp, true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.level() instanceof ServerLevel sl) {
            DreamWeaverBlock.applyWakeUpEffectsAround(sl, player.blockPosition(), player);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String raw = event.getRawText();
        if (MirrorDimensionManager.handleVoiceChatCommand(player, raw)) {
            event.setCanceled(true);
        }
    }
}
