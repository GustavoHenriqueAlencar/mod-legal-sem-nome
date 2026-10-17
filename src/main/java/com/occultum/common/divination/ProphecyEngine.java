package com.occultum.common.divination;

import com.occultum.common.entity.OwlFamiliarEntity;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Motor de Concretização Forçada de Profecias (Seção 2.2 e 2.3 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md):
 * Registra a profecia sorteada (1 das 9) nos dados persistentes do jogador com timer entre 9.600 e 36.000 ticks
 * (8 a 30 minutos). Se o jogador cumprir a condição espontaneamente, recompensa/concretiza na hora;
 * caso o timer expire, o motor força o evento a acontecer imediatamente no mundo!
 */
public final class ProphecyEngine {

    public enum ProphecyType {
        ARROW("arrow"),
        BURIED_TREASURE("buried_treasure"),
        FALL("fall"),
        FALL_IN_LOVE("fall_in_love"),
        FIGHT("fight"),
        MULTI_MINE("multi_mine"),
        NETHER_TRIP("nether_trip"),
        RESCUE("rescue"),
        WET("wet");

        private final String id;

        ProphecyType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static ProphecyType byId(String id) {
            for (ProphecyType t : values()) {
                if (t.id.equalsIgnoreCase(id) || t.name().equalsIgnoreCase(id)) {
                    return t;
                }
            }
            return null;
        }
    }

    private ProphecyEngine() {}

    public static void assignRandomProphecy(ServerLevel level, Player player) {
        ProphecyType[] vals = ProphecyType.values();
        ProphecyType chosen = vals[level.random.nextInt(vals.length)];
        // Prazo limite entre 9.600 e 36.000 ticks (8 a 30 minutos de jogo)
        int timerTicks = 9600 + level.random.nextInt(36000 - 9600 + 1);
        player.setData(ModAttachmentTypes.ACTIVE_PROPHECY.get(), chosen.getId());
        player.setData(ModAttachmentTypes.PROPHECY_TIMER.get(), timerTicks);

        player.displayClientMessage(
                Component.translatable("message.occultum.prophecy." + chosen.getId()).withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );
    }

    public static void clearProphecy(Player player) {
        player.setData(ModAttachmentTypes.ACTIVE_PROPHECY.get(), "");
        player.setData(ModAttachmentTypes.PROPHECY_TIMER.get(), 0);
    }

    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel serverLevel)) return;
        String activeId = player.getData(ModAttachmentTypes.ACTIVE_PROPHECY.get());
        if (activeId.isEmpty()) return;

        ProphecyType type = ProphecyType.byId(activeId);
        if (type == null) {
            clearProphecy(player);
            return;
        }

        // Checagens espontâneas contínuas
        if (type == ProphecyType.NETHER_TRIP && serverLevel.dimension() == Level.NETHER) {
            clearProphecy(player);
            return;
        }
        if (type == ProphecyType.WET && player.isInWater()) {
            clearProphecy(player);
            return;
        }

        int timer = player.getData(ModAttachmentTypes.PROPHECY_TIMER.get()) - 1;
        if (timer <= 0) {
            // O tempo expirou -> Concretização Forçada pelo Motor!
            forceFulfillProphecy(serverLevel, player, type);
            clearProphecy(player);
        } else {
            player.setData(ModAttachmentTypes.PROPHECY_TIMER.get(), timer);
        }
    }

    public static void onBlockBroken(ServerLevel level, Player player, BlockPos pos, BlockState state) {
        String activeId = player.getData(ModAttachmentTypes.ACTIVE_PROPHECY.get());
        if (activeId.isEmpty()) return;
        ProphecyType type = ProphecyType.byId(activeId);
        if (type == ProphecyType.BURIED_TREASURE && pos.getY() < 30 && state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            spawnTreasureChest(level, pos);
            clearProphecy(player);
        } else if (type == ProphecyType.MULTI_MINE && (state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES))) {
            BlockState dropState = state;
            for (int i = 0; i < 2; i++) {
                net.minecraft.world.level.block.Block.dropResources(dropState, level, pos);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);
            clearProphecy(player);
        }
    }

    public static void onPlayerHurt(ServerLevel level, Player player, DamageSource source, float amount) {
        String activeId = player.getData(ModAttachmentTypes.ACTIVE_PROPHECY.get());
        if (activeId.isEmpty()) return;
        ProphecyType type = ProphecyType.byId(activeId);
        if (type == ProphecyType.ARROW && source.getDirectEntity() instanceof AbstractArrow) {
            clearProphecy(player);
        } else if (type == ProphecyType.FALL && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL) && amount >= 6.0F) {
            clearProphecy(player);
        } else if (type == ProphecyType.RESCUE && (player.getHealth() - amount) <= 4.0F) {
            triggerGuardianOwlRescue(level, player);
            clearProphecy(player);
        }
    }

    public static void forceFulfillProphecy(ServerLevel level, Player player, ProphecyType type) {
        BlockPos pos = player.blockPosition();
        switch (type) {
            case ARROW -> {
                // Esqueleto furtivo invisível atrás das costas do jogador
                Skeleton skel = EntityType.SKELETON.create(level);
                if (skel != null) {
                    Vec3 behind = player.position().subtract(player.getLookAngle().scale(3.0D));
                    skel.moveTo(behind.x, player.getY(), behind.z, player.getYRot(), 0);
                    skel.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    skel.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                    skel.setTarget(player);
                    level.addFreshEntity(skel);
                }
            }
            case BURIED_TREASURE -> spawnTreasureChest(level, pos.above());
            case FALL -> {
                // O chão sob os pés do jogador cede abruptamente (fosso de queda)
                for (int dy = 1; dy <= 7; dy++) {
                    BlockPos below = pos.below(dy);
                    if (!level.getBlockState(below).is(Blocks.BEDROCK)) {
                        level.setBlock(below, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            case FALL_IN_LOVE -> {
                Villager v = EntityType.VILLAGER.create(level);
                if (v != null) {
                    v.moveTo(pos.getX() + 1.5, pos.getY(), pos.getZ() + 1.5, 0, 0);
                    level.addFreshEntity(v);
                    level.sendParticles(ParticleTypes.HEART, v.getX(), v.getY() + 1.5, v.getZ(), 15, 0.4, 0.4, 0.4, 0.05);
                }
            }
            case FIGHT -> {
                Zombie elite = EntityType.ZOMBIE.create(level);
                if (elite != null) {
                    elite.moveTo(pos.getX() + 2.0, pos.getY(), pos.getZ() + 2.0, 0, 0);
                    elite.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    elite.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    elite.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                    elite.setTarget(player);
                    level.addFreshEntity(elite);
                }
            }
            case MULTI_MINE -> {
                player.spawnAtLocation(new ItemStack(Items.RAW_IRON, 6));
                player.spawnAtLocation(new ItemStack(Items.RAW_GOLD, 6));
                player.spawnAtLocation(new ItemStack(Items.DIAMOND, 3));
            }
            case NETHER_TRIP -> {
                Blaze blaze = EntityType.BLAZE.create(level);
                if (blaze != null) {
                    blaze.moveTo(pos.getX() + 2.5, pos.getY() + 1.0, pos.getZ() + 2.5, 0, 0);
                    blaze.setTarget(player);
                    level.addFreshEntity(blaze);
                }
            }
            case RESCUE -> triggerGuardianOwlRescue(level, player);
            case WET -> {
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                }
                level.setWeatherParameters(0, 6000, true, false);
            }
        }
        level.playSound(null, pos, SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void spawnTreasureChest(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
            chest.setItem(1, new ItemStack(Items.GOLD_INGOT, 8));
            chest.setItem(2, new ItemStack(ModItems.TEAR_OF_THE_GODDESS.get(), 2));
            chest.setItem(3, new ItemStack(ModItems.ROWAN_BERRIES.get(), 6));
        }
    }

    private static void triggerGuardianOwlRescue(ServerLevel level, Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
        OwlFamiliarEntity owl = ModEntityTypes.OWL_FAMILIAR.get().create(level);
        if (owl != null) {
            owl.moveTo(player.getX(), player.getY() + 2.0, player.getZ(), 0, 0);
            owl.tame(player);
            level.addFreshEntity(owl);
        }
        for (Monster m : level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(8.0D))) {
            m.hurt(level.damageSources().magic(), 6.0F);
            m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
        }
    }
}
