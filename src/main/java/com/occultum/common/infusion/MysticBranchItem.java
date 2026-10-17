package com.occultum.common.infusion;

import com.occultum.common.registry.ModMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.occultum.common.infusion.MysticBranchItem.Stroke.*;

/**
 * Ramo Místico (Mystic Branch) e Catálogo Exaustivo dos 31 Feitiços Gestuais
 * conforme Seção 3 e Seção 4 de 07_INFUSOES_E_SIMBOLOS_DE_VARINHA.md.
 * Captura movimentos angulares de Yaw/Pitch discretizados nas 8 direções (Strokes 0..7)
 * enquanto segura o botão direito e dispara o feitiço correspondente ao soltar.
 */
public class MysticBranchItem extends Item {

    public enum Stroke {
        UP(0),
        DOWN(1),
        RIGHT(2),
        LEFT(3),
        UP_RIGHT(4),
        DOWN_LEFT(5),
        UP_LEFT(6),
        DOWN_RIGHT(7);

        private final int code;

        Stroke(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public record CastState(float lastYaw, float lastPitch, List<Stroke> strokes) {}

    private static final Map<UUID, CastState> ACTIVE_CASTS = new ConcurrentHashMap<>();
    private static final Set<BlockPos> COLLOPORTUS_LOCKED_DOORS = ConcurrentHashMap.newKeySet();

    public record BranchSpell(
            int number,
            String arcaneLatinName,
            List<Stroke> sequence,
            int tier,
            int infusionCost
    ) {}

    private static List<BranchSpell> SPELLS;

    public static List<BranchSpell> getSpells() {
        if (SPELLS == null) {
            List<BranchSpell> s = new ArrayList<>();
            // #1: Accio -> Tracto Arcanum (Níveis I, II, III)
            s.add(new BranchSpell(1, "Tracto Arcanum I", List.of(DOWN, LEFT, UP, RIGHT, RIGHT, DOWN), 1, 1));
            s.add(new BranchSpell(1, "Tracto Arcanum II", List.of(DOWN, LEFT, UP, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN), 2, 2));
            s.add(new BranchSpell(1, "Tracto Arcanum III", List.of(LEFT, LEFT, UP, UP, UP, RIGHT, RIGHT, RIGHT, DOWN, DOWN, DOWN), 3, 3));
            // #2: Aguamenti -> Fons Aquae (Níveis I, II)
            s.add(new BranchSpell(2, "Fons Aquae I", List.of(LEFT, DOWN, RIGHT, UP, LEFT), 1, 1));
            s.add(new BranchSpell(2, "Fons Aquae II", List.of(LEFT, DOWN, DOWN, RIGHT, UP, UP, LEFT), 2, 2));
            // #3: Alohomora -> Reserare Ostium (Níveis I, II)
            s.add(new BranchSpell(3, "Reserare Ostium I", List.of(RIGHT, UP, RIGHT, RIGHT, DOWN), 1, 1));
            s.add(new BranchSpell(3, "Reserare Ostium II", List.of(RIGHT, UP, UP, RIGHT, RIGHT, DOWN, DOWN), 2, 2));
            // #4: Avada Kedavra -> Exitus Letalis (50 cargas, 1000 dano puro)
            s.add(new BranchSpell(4, "Exitus Letalis", List.of(DOWN, DOWN, RIGHT, RIGHT, UP, UP, LEFT, LEFT, LEFT, LEFT, DOWN, DOWN, RIGHT), 1, 50));
            // #5: Cave Inimicum -> Vallum Hostis
            s.add(new BranchSpell(5, "Vallum Hostis", List.of(UP, RIGHT, DOWN, LEFT, UP), 1, 3));
            // #6: Colloportus -> Obsera Claustrum (Níveis I, II)
            s.add(new BranchSpell(6, "Obsera Claustrum I", List.of(LEFT, LEFT, DOWN, DOWN, RIGHT), 1, 1));
            s.add(new BranchSpell(6, "Obsera Claustrum II", List.of(LEFT, LEFT, LEFT, DOWN, DOWN, RIGHT, RIGHT), 2, 2));
            // #7: Confundus -> Mens Turbata (Níveis I, II)
            s.add(new BranchSpell(7, "Mens Turbata I", List.of(DOWN, LEFT, LEFT, UP, UP, RIGHT), 1, 2));
            s.add(new BranchSpell(7, "Mens Turbata II", List.of(RIGHT, LEFT, LEFT, LEFT, UP, UP, UP, RIGHT, RIGHT), 2, 4));
            // #8: Crucio -> Dolor Perpetuus (Níveis I, II)
            s.add(new BranchSpell(8, "Dolor Perpetuus I", List.of(DOWN, DOWN, LEFT, DOWN, DOWN, RIGHT), 1, 5));
            s.add(new BranchSpell(8, "Dolor Perpetuus II", List.of(LEFT, DOWN, LEFT, LEFT, LEFT, DOWN, DOWN, DOWN, DOWN, RIGHT, RIGHT, RIGHT), 2, 8));
            // #9: Defodio -> Scindo Petram (Níveis I, II)
            s.add(new BranchSpell(9, "Scindo Petram I", List.of(DOWN, UP, UP, LEFT, DOWN), 1, 1));
            s.add(new BranchSpell(9, "Scindo Petram II", List.of(RIGHT, UP, UP, UP, LEFT, LEFT, DOWN, DOWN, RIGHT), 2, 3));
            // #10: Ennervate -> Suscito Vigor
            s.add(new BranchSpell(10, "Suscito Vigor", List.of(DOWN, UP, LEFT, UP, RIGHT, LEFT, UP, RIGHT), 1, 2));
            // #11: Episkey -> Sano Vulnus (Níveis I, II)
            s.add(new BranchSpell(11, "Sano Vulnus I", List.of(DOWN, RIGHT, UP, LEFT, DOWN, DOWN, RIGHT), 1, 2));
            s.add(new BranchSpell(11, "Sano Vulnus II", List.of(LEFT, RIGHT, RIGHT, UP, UP, LEFT, LEFT, DOWN, DOWN, DOWN, DOWN, RIGHT, RIGHT), 2, 4));
            // #12: Expelliarmus -> Exuo Telum (Níveis I, II)
            s.add(new BranchSpell(12, "Exuo Telum I", List.of(DOWN, UP, UP, DOWN), 1, 2));
            s.add(new BranchSpell(12, "Exuo Telum II", List.of(RIGHT, UP, UP, UP, UP, DOWN, DOWN, DOWN), 2, 4));
            // #13: Flagrate -> Signum Ignis
            s.add(new BranchSpell(13, "Signum Ignis", List.of(RIGHT, UP, RIGHT, LEFT, UP, RIGHT), 1, 1));
            // #14: Flipendo -> Impulsus Vis (Níveis I, II)
            s.add(new BranchSpell(14, "Impulsus Vis I", List.of(DOWN, RIGHT, RIGHT, LEFT), 1, 1));
            s.add(new BranchSpell(14, "Impulsus Vis II", List.of(LEFT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, LEFT, LEFT, LEFT, LEFT), 2, 3));
            // #15: Impedimenta -> Tardus Gradus
            s.add(new BranchSpell(15, "Tardus Gradus", List.of(RIGHT, DOWN, LEFT, UP, RIGHT), 1, 2));
            // #16: Imperio -> Dominus Mentis
            s.add(new BranchSpell(16, "Dominus Mentis", List.of(UP, RIGHT, DOWN, LEFT, UP, RIGHT, DOWN), 1, 10));
            // #17: Incendio -> Flamma Vorax
            s.add(new BranchSpell(17, "Flamma Vorax", List.of(DOWN, RIGHT, UP, DOWN), 1, 2));
            // #18: Lumos -> Lux Aeterna
            s.add(new BranchSpell(18, "Lux Aeterna", List.of(UP, RIGHT, DOWN, LEFT), 1, 1));
            // #19: Meteolojinx Recanto -> Serenitas Caeli
            s.add(new BranchSpell(19, "Serenitas Caeli", List.of(UP, UP, DOWN, DOWN, LEFT, RIGHT), 1, 10));
            // #20: Nox -> Umbra Extinguo
            s.add(new BranchSpell(20, "Umbra Extinguo", List.of(DOWN, LEFT, UP, RIGHT), 1, 1));
            // #21: Protego -> Scutum Reflexum
            s.add(new BranchSpell(21, "Scutum Reflexum", List.of(UP, UP, DOWN), 1, 2));
            // #22: Stupefy -> Stupor Fulmen
            s.add(new BranchSpell(22, "Stupor Fulmen", List.of(RIGHT, RIGHT, DOWN, DOWN), 1, 2));
            // #23: Ignianima -> Ignis Animae
            s.add(new BranchSpell(23, "Ignis Animae", List.of(DOWN, RIGHT, UP, RIGHT, DOWN), 1, 5));
            // #24: Carnosa Diem -> Sanguis Raptus
            s.add(new BranchSpell(24, "Sanguis Raptus", List.of(UP, LEFT, DOWN, RIGHT, UP), 1, 3));
            // #25: Morsmordre -> Calvaria Caeli
            s.add(new BranchSpell(25, "Calvaria Caeli", List.of(UP, UP, RIGHT, DOWN, LEFT, UP, RIGHT, DOWN, DOWN), 1, 20));
            // #26: Tormentum -> Implosio Umbrae
            s.add(new BranchSpell(26, "Implosio Umbrae", List.of(DOWN, DOWN, LEFT, UP, RIGHT, DOWN), 1, 4));
            // #27: Leonard: Gula -> Vorago Famis
            s.add(new BranchSpell(27, "Vorago Famis", List.of(LEFT, DOWN, RIGHT, UP, DOWN, LEFT), 1, 15));
            // #28: Leonard: Ira -> Ira Infernus
            s.add(new BranchSpell(28, "Ira Infernus", List.of(UP, DOWN, RIGHT, LEFT, UP, DOWN), 1, 15));
            // #29: Leonard: Avareza -> Avaritia Spolium
            s.add(new BranchSpell(29, "Avaritia Spolium", List.of(RIGHT, UP, LEFT, DOWN, RIGHT, UP), 1, 15));
            // #30: Leonard: Orgulho -> Superbia Ignis
            s.add(new BranchSpell(30, "Superbia Ignis", List.of(UP, UP, DOWN, DOWN, RIGHT, RIGHT, LEFT, LEFT), 1, 15));
            // #31: Attraho -> Tracto Continuum
            s.add(new BranchSpell(31, "Tracto Continuum", List.of(UP, DOWN, UP, DOWN, LEFT), 1, 2));

            SPELLS = Collections.unmodifiableList(s);
        }
        return SPELLS;
    }

    public static boolean isDoorLocked(BlockPos pos) {
        return COLLOPORTUS_LOCKED_DOORS.contains(pos);
    }

    public MysticBranchItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            ACTIVE_CASTS.put(player.getUUID(), new CastState(player.getYRot(), player.getXRot(), new ArrayList<>()));
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) return;
        CastState state = ACTIVE_CASTS.get(player.getUUID());
        if (state == null) return;

        float dyaw = wrapDegrees(player.getYRot() - state.lastYaw());
        float dpitch = player.getXRot() - state.lastPitch();
        double mag = Math.hypot(dyaw, dpitch);

        // Quando o movimento angular excede o limiar de 10 graus, registra o traço discretizado nas 8 direções
        if (mag >= 10.0D && state.strokes().size() < 16) {
            Stroke detected = classifyStroke(dyaw, dpitch);
            List<Stroke> list = state.strokes();
            list.add(detected);
            ACTIVE_CASTS.put(player.getUUID(), new CastState(player.getYRot(), player.getXRot(), list));
            if (level instanceof ServerLevel sl) {
                Vec3 tip = player.getEyePosition().add(player.getLookAngle().scale(1.2D));
                sl.sendParticles(ParticleTypes.ENCHANTED_HIT, tip.x, tip.y, tip.z, 3, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    public static Stroke classifyStroke(float dyaw, float dpitch) {
        // Pitch negativo = olhar para cima (UP); Pitch positivo = olhar para baixo (DOWN)
        // Yaw positivo = direita (RIGHT); Yaw negativo = esquerda (LEFT)
        double absYaw = Math.abs(dyaw);
        double absPitch = Math.abs(dpitch);
        if (absPitch > absYaw * 1.75D) {
            return dpitch < 0 ? UP : DOWN;
        }
        if (absYaw > absPitch * 1.75D) {
            return dyaw > 0 ? RIGHT : LEFT;
        }
        if (dpitch < 0 && dyaw > 0) return UP_RIGHT;
        if (dpitch < 0 && dyaw < 0) return UP_LEFT;
        if (dpitch > 0 && dyaw > 0) return DOWN_RIGHT;
        return DOWN_LEFT;
    }

    private static float wrapDegrees(float deg) {
        deg = deg % 360.0F;
        if (deg >= 180.0F) deg -= 360.0F;
        if (deg < -180.0F) deg += 360.0F;
        return deg;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide || !(livingEntity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CastState state = ACTIVE_CASTS.remove(player.getUUID());
        if (state == null || state.strokes().isEmpty()) {
            return;
        }

        BranchSpell matched = findMatchingSpell(state.strokes());
        if (matched == null) {
            // Seção 3.1: Sequência inválida -> ruído seco de faísca morta e nenhum feitiço disparado
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7F, 1.4F);
            return;
        }

        if (!InfusionPowerHandler.tryConsumeCharges(player, matched.infusionCost())) {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.PLAYERS, 1.0F, 0.6F);
            player.displayClientMessage(Component.translatable("message.occultum.branch.no_power", matched.infusionCost()).withStyle(ChatFormatting.RED), true);
            return;
        }

        executeBranchSpell(serverLevel, player, matched);
        player.displayClientMessage(Component.literal(matched.arcaneLatinName()).withStyle(ChatFormatting.AQUA), true);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.1F);
    }

    @Nullable
    public static BranchSpell findMatchingSpell(List<Stroke> drawn) {
        for (BranchSpell spell : getSpells()) {
            if (spell.sequence().equals(drawn)) {
                return spell;
            }
        }
        return null;
    }

    public static void executeBranchSpell(ServerLevel level, Player caster, BranchSpell spell) {
        double maxDist = spell.tier() >= 2 ? 32.0D : 20.0D;
        BlockHitResult blockHit = level.clip(new ClipContext(caster.getEyePosition(),
                caster.getEyePosition().add(caster.getLookAngle().scale(maxDist)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
        BlockPos targetPos = blockHit.getBlockPos();
        LivingEntity targetEntity = findLookedAtEntity(level, caster, maxDist);

        switch (spell.number()) {
            case 1 -> { // #1 Accio (Tracto Arcanum I/II/III)
                double r = spell.tier() == 1 ? 16.0 : (spell.tier() == 2 ? 32.0 : 45.0);
                for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, caster.getBoundingBox().inflate(r))) {
                    ie.teleportTo(caster.getX(), caster.getY() + 0.5, caster.getZ());
                }
                if (targetEntity != null) {
                    Vec3 pull = caster.position().subtract(targetEntity.position()).normalize().scale(0.8 * spell.tier());
                    targetEntity.setDeltaMovement(pull.x, 0.35, pull.z);
                    targetEntity.hurtMarked = true;
                }
            }
            case 2 -> { // #2 Aguamenti (Fons Aquae I/II)
                BlockState st = level.getBlockState(targetPos);
                if (st.is(Blocks.CAULDRON)) {
                    level.setBlock(targetPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), 3);
                } else if (spell.tier() >= 2 && level.getBlockState(targetPos.above()).isAir()) {
                    level.setBlock(targetPos.above(), Blocks.WATER.defaultBlockState(), 3);
                }
                if (targetEntity != null) {
                    targetEntity.clearFire();
                    targetEntity.knockback(0.6, caster.getX() - targetEntity.getX(), caster.getZ() - targetEntity.getZ());
                }
            }
            case 3 -> { // #3 Alohomora (Reserare Ostium I/II)
                COLLOPORTUS_LOCKED_DOORS.remove(targetPos);
                COLLOPORTUS_LOCKED_DOORS.remove(targetPos.below());
                BlockState st = level.getBlockState(targetPos);
                if (st.getBlock() instanceof DoorBlock door) {
                    door.setOpen(caster, level, st, targetPos, !st.getValue(DoorBlock.OPEN));
                } else if (st.getBlock() instanceof TrapDoorBlock) {
                    level.setBlock(targetPos, st.cycle(TrapDoorBlock.OPEN), 3);
                }
            }
            case 4 -> { // #4 Avada Kedavra (Exitus Letalis): 1000 pontos de dano puro
                if (targetEntity != null) {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, targetEntity.getX(), targetEntity.getY() + 1.0, targetEntity.getZ(), 50, 0.5, 0.8, 0.5, 0.1);
                    targetEntity.hurt(level.damageSources().fellOutOfWorld(), 1000.0F);
                }
            }
            case 5 -> { // #5 Cave Inimicum (Vallum Hostis)
                caster.addEffect(new MobEffectInstance(ModMobEffects.REFLECT_PROJECTILES, 300, 0));
                caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
            }
            case 6 -> { // #6 Colloportus (Obsera Claustrum I/II)
                BlockState st = level.getBlockState(targetPos);
                if (st.getBlock() instanceof DoorBlock door) {
                    door.setOpen(caster, level, st, targetPos, false);
                    COLLOPORTUS_LOCKED_DOORS.add(targetPos.immutable());
                }
            }
            case 7 -> { // #7 Confundus (Mens Turbata I/II)
                int dur = spell.tier() >= 2 ? 400 : 200;
                if (targetEntity != null) {
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, dur, 1));
                    if (targetEntity instanceof Monster m) {
                        List<Monster> others = level.getEntitiesOfClass(Monster.class, m.getBoundingBox().inflate(10.0), o -> o != m);
                        if (!others.isEmpty()) m.setTarget(others.get(0));
                    }
                }
            }
            case 8 -> { // #8 Crucio (Dolor Perpetuus I/II)
                if (targetEntity != null) {
                    int dur = spell.tier() >= 2 ? 240 : 160;
                    targetEntity.hurt(level.damageSources().magic(), 6.0F);
                    targetEntity.addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, dur, 0));
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, dur, 2));
                }
            }
            case 9 -> { // #9 Defodio (Scindo Petram I/II)
                int r = spell.tier() >= 2 ? 1 : 0;
                for (BlockPos p : BlockPos.betweenClosed(targetPos.offset(-r, -r, -r), targetPos.offset(r, r, r))) {
                    BlockState st = level.getBlockState(p);
                    if (!st.isAir() && st.getDestroySpeed(level, p) >= 0 && st.getDestroySpeed(level, p) < 50.0F) {
                        level.destroyBlock(p, true, caster);
                    }
                }
            }
            case 10 -> { // #10 Ennervate (Suscito Vigor)
                LivingEntity recipient = targetEntity != null ? targetEntity : caster;
                recipient.removeEffect(ModMobEffects.PARALYSIS);
                recipient.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                recipient.removeEffect(MobEffects.BLINDNESS);
                recipient.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            }
            case 11 -> { // #11 Episkey (Sano Vulnus I/II)
                LivingEntity recipient = targetEntity != null ? targetEntity : caster;
                recipient.heal(spell.tier() >= 2 ? 14.0F : 6.0F);
            }
            case 12 -> { // #12 Expelliarmus (Exuo Telum I/II)
                if (targetEntity != null) {
                    ItemStack held = targetEntity.getMainHandItem();
                    if (!held.isEmpty()) {
                        targetEntity.spawnAtLocation(held.copy());
                        targetEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                    if (spell.tier() >= 2) {
                        Vec3 kb = targetEntity.position().subtract(caster.position()).normalize().scale(1.1);
                        targetEntity.setDeltaMovement(kb.x, 0.4, kb.z);
                        targetEntity.hurtMarked = true;
                    }
                }
            }
            case 13 -> { // #13 Flagrate (Signum Ignis)
                BlockPos placePos = targetPos.relative(blockHit.getDirection());
                if (level.getBlockState(placePos).isAir()) {
                    level.setBlock(placePos, Blocks.TORCH.defaultBlockState(), 3);
                }
            }
            case 14 -> { // #14 Flipendo (Impulsus Vis I/II)
                if (targetEntity != null) {
                    double scale = spell.tier() >= 2 ? 1.8D : 1.0D;
                    Vec3 kb = targetEntity.position().subtract(caster.position()).normalize().scale(scale);
                    targetEntity.hurt(level.damageSources().magic(), 4.0F);
                    targetEntity.setDeltaMovement(kb.x, 0.45D, kb.z);
                    targetEntity.hurtMarked = true;
                }
            }
            case 15 -> { // #15 Impedimenta (Tardus Gradus)
                if (targetEntity != null) {
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 3));
                }
            }
            case 16 -> { // #16 Imperio (Dominus Mentis)
                if (targetEntity != null) {
                    targetEntity.addEffect(new MobEffectInstance(ModMobEffects.ENSLAVED, 1200, 0));
                    targetEntity.getPersistentData().putUUID("OccultumMasterUUID", caster.getUUID());
                }
            }
            case 17 -> { // #17 Incendio (Flamma Vorax)
                if (targetEntity != null) {
                    targetEntity.igniteForSeconds(10.0F);
                } else {
                    BlockPos p = targetPos.relative(blockHit.getDirection());
                    if (level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }
            case 18 -> { // #18 Lumos (Lux Aeterna)
                BlockPos p = targetPos.relative(blockHit.getDirection());
                if (level.getBlockState(p).isAir()) {
                    level.setBlock(p, Blocks.LIGHT.defaultBlockState(), 3);
                }
                caster.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
            }
            case 19 -> { // #19 Meteolojinx Recanto (Serenitas Caeli)
                level.setWeatherParameters(12000, 0, false, false);
            }
            case 20 -> { // #20 Nox (Umbra Extinguo)
                caster.removeEffect(MobEffects.NIGHT_VISION);
                for (BlockPos p : BlockPos.betweenClosed(caster.blockPosition().offset(-10, -4, -10), caster.blockPosition().offset(10, 4, 10))) {
                    BlockState st = level.getBlockState(p);
                    if (st.is(Blocks.TORCH) || st.is(Blocks.WALL_TORCH) || st.is(Blocks.LIGHT)) {
                        level.destroyBlock(p, true);
                    }
                }
            }
            case 21 -> { // #21 Protego (Scutum Reflexum)
                caster.addEffect(new MobEffectInstance(ModMobEffects.REFLECT_PROJECTILES, 60, 1));
                caster.addEffect(new MobEffectInstance(ModMobEffects.REFLECT_DAMAGE, 60, 1));
            }
            case 22 -> { // #22 Stupefy (Stupor Fulmen)
                if (targetEntity != null) {
                    targetEntity.hurt(level.damageSources().magic(), 4.0F);
                    targetEntity.addEffect(new MobEffectInstance(ModMobEffects.PARALYSIS, 100, 0));
                }
            }
            case 23 -> { // #23 Ignianima (Ignis Animae)
                if (targetEntity != null) {
                    targetEntity.hurt(level.damageSources().fellOutOfWorld(), 8.0F);
                    targetEntity.igniteForSeconds(8.0F);
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, targetEntity.getX(), targetEntity.getY() + 1.0, targetEntity.getZ(), 25, 0.3, 0.5, 0.3, 0.03);
                }
            }
            case 24 -> { // #24 Carnosa Diem (Sanguis Raptus)
                if (targetEntity != null) {
                    targetEntity.hurt(level.damageSources().magic(), 6.0F);
                    caster.heal(6.0F);
                    caster.getFoodData().eat(3, 0.5F);
                }
            }
            case 25 -> { // #25 Morsmordre (Calvaria Caeli)
                Vec3 sky = caster.position().add(0, 25, 0);
                level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, sky.x, sky.y, sky.z, 120, 4.0, 4.0, 4.0, 0.02);
                level.playSound(null, caster.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 2.0F, 0.6F);
            }
            case 26 -> { // #26 Tormentum (Implosio Umbrae)
                BlockPos center = targetEntity != null ? targetEntity.blockPosition() : targetPos;
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(4.0D))) {
                    if (e != caster) {
                        e.hurt(level.damageSources().magic(), 8.0F);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 1));
                    }
                }
                level.sendParticles(ParticleTypes.LARGE_SMOKE, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 35, 1.5, 1.0, 1.5, 0.05);
            }
            case 27 -> { // #27 Leonard: Gula (Vorago Famis)
                if (targetEntity != null) {
                    if (targetEntity instanceof Player vp) {
                        vp.getFoodData().setFoodLevel(0);
                    }
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 3));
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
                }
            }
            case 28 -> { // #28 Leonard: Ira (Ira Infernus): saraivada quádrupla de bolas de fogo
                Vec3 look = caster.getLookAngle();
                for (int i = -1; i <= 2; i++) {
                    Vec3 dir = look.yRot(i * 0.15F);
                    SmallFireball fb = new SmallFireball(level, caster, dir);
                    fb.setPos(caster.getX() + dir.x, caster.getEyeY() - 0.1, caster.getZ() + dir.z);
                    level.addFreshEntity(fb);
                }
            }
            case 29 -> { // #29 Leonard: Avareza (Avaritia Spolium): rouba buffs ativos do alvo
                if (targetEntity != null) {
                    List<MobEffectInstance> buffs = targetEntity.getActiveEffects().stream()
                            .filter(inst -> inst.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL)
                            .toList();
                    for (MobEffectInstance inst : buffs) {
                        caster.addEffect(new MobEffectInstance(inst));
                        targetEntity.removeEffect(inst.getEffect());
                    }
                }
            }
            case 30 -> { // #30 Leonard: Orgulho (Superbia Ignis): onda de repulsão + teleporta 8 blocos para trás
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(6.0D))) {
                    if (e != caster) {
                        Vec3 push = e.position().subtract(caster.position()).normalize().scale(1.4D);
                        e.setDeltaMovement(push.x, 0.5D, push.z);
                        e.igniteForSeconds(5.0F);
                        e.hurtMarked = true;
                    }
                }
                Vec3 back = caster.position().subtract(caster.getLookAngle().scale(8.0D));
                caster.teleportTo(back.x, caster.getY(), back.z);
            }
            case 31 -> { // #31 Attraho (Tracto Continuum)
                if (targetEntity != null) {
                    Vec3 pull = caster.position().subtract(targetEntity.position()).normalize().scale(1.25D);
                    targetEntity.setDeltaMovement(pull.x, 0.3D, pull.z);
                    targetEntity.hurtMarked = true;
                }
            }
        }
    }

    @Nullable
    private static LivingEntity findLookedAtEntity(ServerLevel level, Player caster, double maxDist) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = eye.add(look.scale(maxDist));
        AABB searchBox = caster.getBoundingBox().expandTowards(look.scale(maxDist)).inflate(1.5D);
        LivingEntity best = null;
        double bestDist = maxDist;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != caster && e.isAlive())) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.4D).clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceTo(hit.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = candidate;
                }
            }
        }
        return best;
    }
}
