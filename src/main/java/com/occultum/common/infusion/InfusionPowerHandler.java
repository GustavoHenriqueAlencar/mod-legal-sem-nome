package com.occultum.common.infusion;

import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Gerenciador das 4 Infusões Elementares do Jogador (Seção 1 e 2 de 07_INFUSOES_E_SIMBOLOS_DE_VARINHA.md):
 * - 2.1 Infusão da Luz (light): Prisão Cilíndrica de Luz (Clique Esq, 5 cargas), Refração Luminosa (+65% vel, Invisibilidade + purga de aggro em 20 blocos, 1 carga/30 ticks), Muralha/Redoma de Luz (Sneak + Clique Dir, 3 cargas).
 * - 2.2 Infusão da Terra (overworld): Imunidade a Queda Telúrica + Onda de Choque Sísmica em quedas grandes, Repulsão Cinética 10 blocos (Clique Esq, 4 cargas), Fenda Sísmica em anéis / Pedregulho explosivo (Clique Dir, 5 cargas).
 * - 2.3 Infusão do Fim / Outro Lugar (otherwhere): Troca Espacial de Posição (Clique Esq, 4 cargas), Salto Espacial Blink 32 blocos (Clique Dir, 1 carga), Bolsão de Vácuo 8s (Sneak + Clique Dir em criatura, 6 cargas).
 * - 2.4 Infusão Infernal / do Nether (nether): Imunidade Térmica Total + +30% velocidade em lava, Golpe Incendiário 10s (Clique Esq, 2 cargas), Lança-Chamas Contínuo 8 blocos / Bola de fogo de monstro absorvido (Clique Dir, 1 carga/10 ticks).
 */
public final class InfusionPowerHandler {

    private InfusionPowerHandler() {}

    public static boolean tryConsumeCharges(Player player, int charges) {
        if (player.isCreative()) return true;
        // Efeito Veneração (Worship) reduz ou anula custo de poder (Seção 6.1 #42 de outros.md)
        if (player.hasEffect(ModMobEffects.WORSHIP)) {
            charges = Math.max(0, charges - 2);
        }
        int current = player.getData(ModAttachmentTypes.INFUSION_POWER.get());
        if (current < charges) {
            return false;
        }
        player.setData(ModAttachmentTypes.INFUSION_POWER.get(), current - charges);
        return true;
    }

    public static void addCharges(Player player, int amount) {
        int current = player.getData(ModAttachmentTypes.INFUSION_POWER.get());
        int max = player.getData(ModAttachmentTypes.INFUSION_MAX_POWER.get());
        if (current < max) {
            player.setData(ModAttachmentTypes.INFUSION_POWER.get(), Math.min(max, current + amount));
        }
    }

    /**
     * Atualização passiva a cada tick do servidor para o jogador infundido.
     */
    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel serverLevel)) return;
        String type = player.getData(ModAttachmentTypes.INFUSION_TYPE.get());
        if (type.isEmpty()) return;

        // Recarga passiva lenta em contato com o elemento nativo (Seção 1.1.3)
        if (serverLevel.getGameTime() % 100 == 0) {
            boolean recharge = switch (type) {
                case "light" -> serverLevel.isDay() && serverLevel.canSeeSky(player.blockPosition());
                case "overworld" -> serverLevel.getBlockState(player.blockPosition().below()).is(BlockTags.DIRT);
                case "nether" -> player.isOnFire() || player.isInLava() || serverLevel.dimension() == Level.NETHER;
                case "otherwhere" -> serverLevel.dimension() == Level.END || serverLevel.isNight();
                default -> false;
            };
            if (recharge) {
                addCharges(player, 2);
            }
        }

        // Infusão Infernal: Imunidade a fogo/lava e +30% velocidade em lava (Seção 2.4)
        if ("nether".equals(type)) {
            if (player.getRemainingFireTicks() > 0) {
                player.clearFire();
            }
            if (player.isInLava()) {
                Vec3 dm = player.getDeltaMovement();
                player.setDeltaMovement(dm.x * 1.30D, dm.y, dm.z * 1.30D);
                player.hurtMarked = true;
            }
        }
    }

    /**
     * Intercepta danos (Queda Telúrica para Overworld, Fogo/Lava para Nether, Absorção Mágica).
     */
    public static boolean onPlayerIncomingDamage(Player player, DamageSource source, float amount) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel serverLevel)) return false;
        String type = player.getData(ModAttachmentTypes.INFUSION_TYPE.get());

        // Efeito Absorção Mágica (Absorb Magic): reduz dano mágico e recarrega infusão
        if (player.hasEffect(ModMobEffects.ABSORB_MAGIC) && source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            addCharges(player, Math.max(1, (int) amount));
        }

        // 2.2 Infusão da Terra: Imunidade a Queda sobre terra/grama/argila + Onda de Choque Sísmica
        if ("overworld".equals(type) && source.is(DamageTypeTags.IS_FALL)) {
            BlockState below = serverLevel.getBlockState(player.blockPosition().below());
            if (below.is(BlockTags.DIRT) || below.is(Blocks.CLAY) || below.is(Blocks.STONE) || below.is(BlockTags.BASE_STONE_OVERWORLD)) {
                if (amount >= 6.0F) {
                    // Onda de choque sísmica em raio de 6 blocos
                    for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0D))) {
                        if (e != player) {
                            e.hurt(serverLevel.damageSources().playerAttack(player), amount * 0.5F);
                            e.setDeltaMovement(e.getDeltaMovement().x, 0.75D, e.getDeltaMovement().z);
                            e.hurtMarked = true;
                        }
                    }
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 6, 2.0, 0.2, 2.0, 0.05);
                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                }
                player.resetFallDistance();
                return true;
            }
        }

        // 2.4 Infusão Infernal: 100% imune a fogo, lava e calor
        if ("nether".equals(type) && source.is(DamageTypeTags.IS_FIRE)) {
            player.clearFire();
            return true;
        }

        return false;
    }

    /**
     * Habilidades de Clique Esquerdo em Entidade Viva (Seção 2.1 a 2.4).
     */
    public static void onLeftClickEntity(Player player, Entity targetEntity) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel serverLevel)) return;
        if (!(targetEntity instanceof LivingEntity target)) return;
        // Só dispara a habilidade corporal se estiver com a mão vazia
        if (!player.getMainHandItem().isEmpty()) return;

        String type = player.getData(ModAttachmentTypes.INFUSION_TYPE.get());
        switch (type) {
            case "light" -> {
                // Prisão Cilíndrica de Luz (5 cargas): teleporta 4 blocos acima do chão e imobiliza por 10s (200 ticks)
                if (tryConsumeCharges(player, 5)) {
                    target.teleportTo(target.getX(), target.getY() + 4.0D, target.getZ());
                    target.addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, 200, 0));
                    target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 220, 2));
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                    serverLevel.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 35, 1.2, 1.5, 1.2, 0.02);
                    serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
                }
            }
            case "overworld" -> {
                // Repulsão Cinética (4 cargas): arremessa a 10 blocos horizontalmente com força 1,5 e atordoa
                if (tryConsumeCharges(player, 4)) {
                    Vec3 dir = target.position().subtract(player.position()).normalize().scale(1.5D);
                    target.setDeltaMovement(dir.x, 0.55D, dir.z);
                    target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3));
                    serverLevel.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 0.8F);
                }
            }
            case "otherwhere" -> {
                // Troca Espacial de Posição (4 cargas): troca (X, Y, Z) do jogador com o alvo
                if (tryConsumeCharges(player, 4)) {
                    Vec3 playerPos = player.position();
                    Vec3 targetPos = target.position();
                    target.teleportTo(playerPos.x, playerPos.y, playerPos.z);
                    player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.1F);
                }
            }
            case "nether" -> {
                // Golpe Incendiário (2 cargas): chamas eternas por 10s + dano de fogo demoníaco penetrante
                if (tryConsumeCharges(player, 2)) {
                    target.igniteForSeconds(10.0F);
                    target.hurt(serverLevel.damageSources().magic(), 5.0F);
                    serverLevel.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                }
            }
        }
    }

    /**
     * Habilidades de Clique Direito / Agachado com mão vazia (Seção 2.1 a 2.4).
     */
    public static boolean onRightClickEmptyHand(Player player) {
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel serverLevel)) return false;
        String type = player.getData(ModAttachmentTypes.INFUSION_TYPE.get());
        if (type.isEmpty()) return false;

        boolean sneaking = player.isShiftKeyDown();

        switch (type) {
            case "light" -> {
                if (sneaking) {
                    // Soltar Botão Direito Agachado (3 cargas): Muralha de Escudo de Luz ou Redoma em aliado (16 blocos)
                    if (tryConsumeCharges(player, 3)) {
                        BlockHitResult hit = serverLevel.clip(new ClipContext(player.getEyePosition(),
                                player.getEyePosition().add(player.getLookAngle().scale(16.0D)),
                                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                        BlockPos center = hit.getBlockPos().above();
                        for (LivingEntity ally : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(3.0D))) {
                            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
                            ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
                        }
                        serverLevel.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, 50, 2.0, 1.5, 2.0, 0.02);
                        serverLevel.playSound(null, center, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.3F);
                        return true;
                    }
                } else {
                    // Refração Luminosa (1 carga): +65% velocidade, Invisibilidade e purga aggro de monstros em 20 blocos
                    if (tryConsumeCharges(player, 1)) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
                        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0));
                        for (Monster m : serverLevel.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(20.0D))) {
                            if (m.getTarget() == player) {
                                m.setTarget(null);
                            }
                        }
                        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0F, 1.2F);
                        return true;
                    }
                }
            }
            case "overworld" -> {
                // Segurar e Soltar Botão Direito (5 cargas): Fenda Sísmica expansiva até 10 blocos
                if (tryConsumeCharges(player, 5)) {
                    for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10.0D))) {
                        if (e != player) {
                            e.hurt(serverLevel.damageSources().playerAttack(player), 6.0F);
                            e.setDeltaMovement(e.getDeltaMovement().x, 0.9D, e.getDeltaMovement().z);
                            e.hurtMarked = true;
                        }
                    }
                    serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 0.2, player.getZ(), 30, 4.0, 0.2, 4.0, 0.02);
                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
                    return true;
                }
            }
            case "otherwhere" -> {
                if (sneaking) {
                    // Bolsão de Vácuo (6 cargas): bane criaturas próximas por 8s e atordoa/desarma
                    if (tryConsumeCharges(player, 6)) {
                        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0D), e -> e != player);
                        if (!targets.isEmpty()) {
                            LivingEntity victim = targets.get(0);
                            victim.addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, 160, 0));
                            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
                            victim.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0));
                            serverLevel.sendParticles(ParticleTypes.PORTAL, victim.getX(), victim.getY() + 1.0, victim.getZ(), 40, 0.4, 0.8, 0.4, 0.2);
                            serverLevel.playSound(null, victim.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.6F);
                            return true;
                        }
                    }
                } else {
                    // Salto Espacial - Blink (1 carga): Raytrace direto de até 32 blocos
                    if (tryConsumeCharges(player, 1)) {
                        BlockHitResult hit = serverLevel.clip(new ClipContext(player.getEyePosition(),
                                player.getEyePosition().add(player.getLookAngle().scale(32.0D)),
                                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                        Vec3 dest = hit.getType() != HitResult.Type.MISS
                                ? Vec3.atBottomCenterOf(hit.getBlockPos().relative(hit.getDirection()))
                                : player.getEyePosition().add(player.getLookAngle().scale(32.0D));
                        serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.3, 0.6, 0.3, 0.1);
                        player.teleportTo(dest.x, dest.y, dest.z);
                        player.resetFallDistance();
                        serverLevel.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y + 1.0, dest.z, 25, 0.3, 0.6, 0.3, 0.1);
                        serverLevel.playSound(null, BlockPos.containing(dest), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        return true;
                    }
                }
            }
            case "nether" -> {
                if (sneaking) {
                    // Poder de Monstro Absorvido: dispara bola de fogo explosiva (2 cargas)
                    if (tryConsumeCharges(player, 2)) {
                        Vec3 look = player.getLookAngle();
                        LargeFireball fb = new LargeFireball(serverLevel, player, look, 1);
                        fb.setPos(player.getX() + look.x * 1.5, player.getEyeY() - 0.1, player.getZ() + look.z * 1.5);
                        serverLevel.addFreshEntity(fb);
                        serverLevel.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        return true;
                    }
                } else {
                    // Lança-Chamas Contínuo (1 carga): cone de chamas de até 8 blocos à frente, derrete gelo/neve e incendeia
                    if (tryConsumeCharges(player, 1)) {
                        Vec3 eye = player.getEyePosition();
                        Vec3 look = player.getLookAngle();
                        for (int d = 1; d <= 8; d++) {
                            Vec3 pt = eye.add(look.scale(d));
                            BlockPos bp = BlockPos.containing(pt);
                            serverLevel.sendParticles(ParticleTypes.FLAME, pt.x, pt.y, pt.z, 6, 0.3, 0.3, 0.3, 0.04);
                            BlockState st = serverLevel.getBlockState(bp);
                            if (st.is(Blocks.ICE) || st.is(Blocks.FROSTED_ICE)) {
                                serverLevel.setBlock(bp, Blocks.WATER.defaultBlockState(), 3);
                            } else if (st.is(Blocks.SNOW)) {
                                serverLevel.setBlock(bp, Blocks.AIR.defaultBlockState(), 3);
                            }
                            for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(bp).inflate(1.5D))) {
                                if (e != player) {
                                    e.igniteForSeconds(6.0F);
                                    e.hurt(serverLevel.damageSources().inFire(), 4.0F);
                                }
                            }
                        }
                        serverLevel.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
