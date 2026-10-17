package com.occultum.common.divination;

import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pedra Necromântica (Necro Stone - Seção 3 de 13_BRASEIRO_NECROMANCIA_E_ADIVINHACAO.md):
 * - Alcance de Mira Direta (Raytrace): 15 blocos.
 * - Raio de Ação dos Servos: 50 blocos.
 * - 1. Ordem de Ataque Focado (em Entidade Viva).
 * - 2. Ordem de Marcha e Posicionamento (em Bloco).
 * - 3. Reanimação Cadavérica (Sinergia com Infusão da Cova / grave).
 */
public class NecroStoneItem extends Item {

    public static final double RAYTRACE_RANGE = 15.0D;
    public static final double SERVANT_RADIUS = 50.0D;

    public NecroStoneItem(Properties properties) {
        super(properties);
    }

    public static boolean isServantOf(Mob mob, Player master) {
        if (mob.hasEffect(ModMobEffects.ENSLAVED)) {
            if (mob.getPersistentData().hasUUID("OccultumMasterUUID")) {
                return mob.getPersistentData().getUUID("OccultumMasterUUID").equals(master.getUUID());
            }
            return true;
        }
        return mob.getType().is(EntityTypeTags.UNDEAD)
                && mob.getPersistentData().hasUUID("OccultumMasterUUID")
                && mob.getPersistentData().getUUID("OccultumMasterUUID").equals(master.getUUID());
    }

    public static void bindServantToMaster(Mob mob, Player master) {
        mob.getPersistentData().putUUID("OccultumMasterUUID", master.getUUID());
        mob.addEffect(new MobEffectInstance(ModMobEffects.ENSLAVED, 72000, 0));
        mob.setPersistenceRequired();
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
            // Se o alvo for um morto-vivo não vinculado e o operador possuir Infusão da Cova, escraviza-o
            if (interactionTarget instanceof Mob mob && mob.getType().is(EntityTypeTags.UNDEAD) && !isServantOf(mob, player)) {
                if ("grave".equals(player.getData(ModAttachmentTypes.INFUSION_TYPE.get()))) {
                    bindServantToMaster(mob, player);
                    serverLevel.playSound(null, mob.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0F, 0.8F);
                    return InteractionResult.SUCCESS;
                }
            }
            commandFocusedAttack(serverLevel, player, interactionTarget);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 3. Reanimação Cadavérica (Sinergia com Infusão da Cova e agachado)
            if (player.isShiftKeyDown() && "grave".equals(player.getData(ModAttachmentTypes.INFUSION_TYPE.get()))) {
                Zombie risen = EntityType.ZOMBIE.create(serverLevel);
                if (risen != null) {
                    risen.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.getYRot(), 0);
                    bindServantToMaster(risen, player);
                    serverLevel.addFreshEntity(risen);
                    serverLevel.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 20, 0.3, 0.4, 0.3, 0.04);
                    serverLevel.playSound(null, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.PLAYERS, 1.0F, 0.7F);
                    return InteractionResult.SUCCESS;
                }
            }

            // 2. Ordem de Marcha e Posicionamento
            commandMarchToBlock(serverLevel, player, pos.above());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // Raytrace até 15 blocos para entidade ou bloco
            LivingEntity targetEntity = raytraceLivingTarget(serverLevel, player, RAYTRACE_RANGE);
            if (targetEntity != null) {
                commandFocusedAttack(serverLevel, player, targetEntity);
                return InteractionResultHolder.success(stack);
            }
            BlockHitResult blockHit = serverLevel.clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(RAYTRACE_RANGE)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
            ));
            if (blockHit.getType() != HitResult.Type.MISS) {
                commandMarchToBlock(serverLevel, player, blockHit.getBlockPos().above());
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void commandFocusedAttack(ServerLevel level, Player master, LivingEntity target) {
        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.5, 0.4, 0.1);
        level.playSound(null, target.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.5F, 1.6F);

        List<Mob> servants = level.getEntitiesOfClass(Mob.class, master.getBoundingBox().inflate(SERVANT_RADIUS),
                m -> isServantOf(m, master) && m != target);
        for (Mob servant : servants) {
            servant.setTarget(target);
            servant.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            servant.getNavigation().moveTo(target, 1.35D);
        }
    }

    public static void commandMarchToBlock(ServerLevel level, Player master, BlockPos guardPos) {
        level.sendParticles(ParticleTypes.WITCH, guardPos.getX() + 0.5, guardPos.getY() + 0.2, guardPos.getZ() + 0.5, 18, 0.3, 0.2, 0.3, 0.03);
        level.playSound(null, guardPos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.PLAYERS, 1.0F, 0.8F);

        List<Mob> servants = level.getEntitiesOfClass(Mob.class, master.getBoundingBox().inflate(SERVANT_RADIUS),
                m -> isServantOf(m, master));
        for (Mob servant : servants) {
            servant.setTarget(null);
            servant.getNavigation().moveTo(guardPos.getX() + 0.5, guardPos.getY(), guardPos.getZ() + 0.5, 1.2D);
        }
    }

    @Nullable
    private static LivingEntity raytraceLivingTarget(ServerLevel level, Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        AABB box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(range)).inflate(1.0D);
        LivingEntity closest = null;
        double best = range;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, le -> le != player && le.isAlive())) {
            Optional<Vec3> clip = e.getBoundingBox().inflate(0.3D).clip(eye, end);
            if (clip.isPresent()) {
                double d = eye.distanceTo(clip.get());
                if (d < best) {
                    best = d;
                    closest = e;
                }
            }
        }
        return closest;
    }
}
