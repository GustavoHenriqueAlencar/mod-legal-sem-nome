package com.occultum.common.divination;

import com.occultum.common.entity.OwlFamiliarEntity;
import com.occultum.common.entity.ToadFamiliarEntity;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Sistema Completo de Familiares (Seção 4 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md):
 * - Ritual #05 de Vinculação de Familiar (transmuta Jaguatirica/Gato, Sapo ou Coruja em Familiar imortal).
 * - Ritual #06 de Invocação/Ressurreição de Familiar.
 * - Gato Familiar (4.2.1): +1 nível de potência de maldições (40% de eficácia adicional) e Repulsão de Creepers em 10 blocos.
 * - Sapo Familiar (4.2.2): Dobro de poções engarrafadas, redução de 50% de falha, Salto Elevado II e Imunidade a Veneno.
 * - Coruja Familiar (4.2.3): Maestria em Voo de Vassoura (0.90 -> 1.20 blocos/tick, +0.20 aceleração, frenagem instantânea),
 *   Suporte Aéreo em Combate, Bombardeio Aéreo com Poções e Visão Espectral Compartilhada.
 */
public class FamiliarSystem {

    public static boolean isValidFamiliarCandidate(LivingEntity entity) {
        return entity instanceof Cat
                || entity instanceof Ocelot
                || entity instanceof ToadFamiliarEntity
                || entity instanceof OwlFamiliarEntity;
    }

    public static void bindFamiliar(ServerLevel level, Player player, LivingEntity candidate) {
        String type;
        TamableAnimal boundEntity = null;

        if (candidate instanceof Ocelot ocelot) {
            type = "cat";
            Cat cat = EntityType.CAT.create(level);
            if (cat != null) {
                cat.moveTo(ocelot.getX(), ocelot.getY(), ocelot.getZ(), ocelot.getYRot(), ocelot.getXRot());
                cat.tame(player);
                cat.setCustomName(Component.translatable("entity.occultum.cat_familiar", player.getName()).withStyle(ChatFormatting.LIGHT_PURPLE));
                level.addFreshEntity(cat);
                boundEntity = cat;
            }
            ocelot.discard();
        } else if (candidate instanceof Cat cat) {
            type = "cat";
            cat.tame(player);
            cat.setCustomName(Component.translatable("entity.occultum.cat_familiar", player.getName()).withStyle(ChatFormatting.LIGHT_PURPLE));
            boundEntity = cat;
        } else if (candidate instanceof ToadFamiliarEntity toad) {
            type = "toad";
            toad.tame(player);
            toad.setCustomName(Component.translatable("entity.occultum.toad_familiar_bound", player.getName()).withStyle(ChatFormatting.GREEN));
            boundEntity = toad;
        } else if (candidate instanceof OwlFamiliarEntity owl) {
            type = "owl";
            owl.tame(player);
            owl.setCustomName(Component.translatable("entity.occultum.owl_familiar_bound", player.getName()).withStyle(ChatFormatting.GOLD));
            boundEntity = owl;
        } else {
            return;
        }

        player.setData(ModAttachmentTypes.FAMILIAR_TYPE.get(), type);
        if (boundEntity != null) {
            boundEntity.setHealth(boundEntity.getMaxHealth());
            level.sendParticles(ParticleTypes.HEART, boundEntity.getX(), boundEntity.getY() + 0.6, boundEntity.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
            level.sendParticles(ParticleTypes.ENCHANT, boundEntity.getX(), boundEntity.getY() + 0.5, boundEntity.getZ(), 35, 0.4, 0.4, 0.4, 0.1);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.3F);
        player.displayClientMessage(Component.translatable("message.occultum.familiar.bound." + type)
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    public static void summonOrReviveFamiliar(ServerLevel level, Player player, BlockPos pos) {
        String type = player.getData(ModAttachmentTypes.FAMILIAR_TYPE.get());
        if (type == null || type.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.occultum.familiar.none")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        // Verifica se o familiar já existe carregado no nível
        List<TamableAnimal> existing = level.getEntitiesOfClass(TamableAnimal.class, new AABB(pos).inflate(128.0D),
                e -> e.isOwnedBy(player) && matchesFamiliarType(e, type));
        if (!existing.isEmpty()) {
            TamableAnimal fam = existing.getFirst();
            fam.setOrderedToSit(false);
            fam.teleportTo(pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5);
            fam.setHealth(fam.getMaxHealth());
            level.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.3, 0.4, 0.3, 0.1);
            return;
        }

        TamableAnimal spawned = switch (type) {
            case "cat" -> EntityType.CAT.create(level);
            case "toad" -> ModEntityTypes.TOAD_FAMILIAR.get().create(level);
            case "owl" -> ModEntityTypes.OWL_FAMILIAR.get().create(level);
            default -> null;
        };

        if (spawned != null) {
            spawned.moveTo(pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, player.getYRot(), 0.0F);
            spawned.tame(player);
            spawned.setHealth(spawned.getMaxHealth());
            spawned.setCustomName(Component.translatable("entity.occultum." + type + "_familiar_bound", player.getName()).withStyle(ChatFormatting.LIGHT_PURPLE));
            level.addFreshEntity(spawned);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.05);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.1F);
        }
    }

    private static boolean matchesFamiliarType(TamableAnimal entity, String type) {
        return switch (type) {
            case "cat" -> entity instanceof Cat;
            case "toad" -> entity instanceof ToadFamiliarEntity;
            case "owl" -> entity instanceof OwlFamiliarEntity;
            default -> false;
        };
    }

    /**
     * Bônus passivos dos familiares executados a cada segundo no tick do servidor.
     */
    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) return;
        String familiar = player.getData(ModAttachmentTypes.FAMILIAR_TYPE.get());
        if (familiar == null || familiar.isEmpty()) return;

        // Gato Familiar (4.2.1): Repulsão Absoluta de Creepers num raio de 10 blocos ao redor do mestre
        if ("cat".equals(familiar) && player.tickCount % 10 == 0) {
            for (Creeper creeper : level.getEntitiesOfClass(Creeper.class, player.getBoundingBox().inflate(10.0D))) {
                creeper.setTarget(null);
                creeper.setSwellDir(-1);
                Vec3 away = creeper.position().subtract(player.position()).normalize().scale(0.55D);
                creeper.setDeltaMovement(away.x, creeper.getDeltaMovement().y, away.z);
                creeper.getNavigation().moveTo(creeper.getX() + away.x * 8.0D, creeper.getY(), creeper.getZ() + away.z * 8.0D, 1.35D);
            }
        }

        // Sapo Familiar (4.2.2): Salto Elevado II e Imunidade a Veneno enquanto ativo perto ou vinculado
        if ("toad".equals(familiar) && player.tickCount % 20 == 0) {
            if (player.hasEffect(MobEffects.POISON)) {
                player.removeEffect(MobEffects.POISON);
            }
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, true, false));
        }
    }

    /**
     * Retorna o bônus de amplificação de maldições concedido pelo Gato Familiar (+1 nível / +40% eficácia).
     */
    public static int getCurseAmplifierBonus(Player player) {
        if (player == null) return 0;
        return "cat".equals(player.getData(ModAttachmentTypes.FAMILIAR_TYPE.get())) ? 1 : 0;
    }

    /**
     * Comanda a Coruja Familiar ativa do jogador a realizar um Bombardeio Aéreo com Poções no bloco mirado.
     */
    public static boolean tryCommandOwlAerialBombing(ServerLevel level, Player player) {
        if (!"owl".equals(player.getData(ModAttachmentTypes.FAMILIAR_TYPE.get()))) return false;
        List<OwlFamiliarEntity> owls = level.getEntitiesOfClass(OwlFamiliarEntity.class, player.getBoundingBox().inflate(48.0D),
                o -> o.isOwnedBy(player) && !o.getHeldPotion().isEmpty());
        if (owls.isEmpty()) return false;

        HitResult hit = player.pick(32.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
            OwlFamiliarEntity owl = owls.getFirst();
            owl.orderAerialBombing(bhr.getBlockPos());
            level.playSound(null, player.blockPosition(), SoundEvents.PARROT_FLY, SoundSource.PLAYERS, 1.0F, 0.8F);
            player.displayClientMessage(Component.translatable("message.occultum.owl.bombing_ordered")
                    .withStyle(ChatFormatting.GOLD), true);
            return true;
        }
        return false;
    }
}
