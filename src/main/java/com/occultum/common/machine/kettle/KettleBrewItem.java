package com.occultum.common.machine.kettle;

import com.occultum.common.fluid.OccultumFluidBlocks;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KettleBrewItem extends Item {
    public enum BrewType {
        VINES, WEBS, THORNS, INK, SPROUTING, EROSION, RAISING, GROTESQUE,
        LOVE, ICE, DEPTHS, THE_DEPTHS, INFECTION, SLEEPING, FLOWING_SPIRIT, WASTING,
        BATS, SUBSTITUTION, REVEALING, SOLID_DIRT, SOLID_STONE, SOLID_SAND,
        SOLID_SANDSTONE, HOLLOW_TEARS_EROSION, CURSED_LEAPING, FROGS_TONGUE, HITCHCOCK
    }

    public static final Class<BrewType> BrewEffect = BrewType.class;

    private final BrewType brewType;

    public KettleBrewItem(Properties properties, BrewType brewType) {
        super(properties);
        this.brewType = brewType == BrewType.THE_DEPTHS ? BrewType.DEPTHS : brewType;
    }

    public KettleBrewItem(BrewType brewType, boolean splash, Properties properties) {
        this(properties, brewType);
    }

    public BrewType getBrewType() {
        return brewType;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return switch (brewType) {
            case GROTESQUE, DEPTHS, SLEEPING -> UseAnim.DRINK;
            default -> UseAnim.NONE;
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide) {
            boolean applied = applyWorldBrewEffect(level, pos, player);
            if (applied) {
                level.playSound(null, pos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (player == null || !player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (brewType == BrewType.GROTESQUE || brewType == BrewType.DEPTHS || brewType == BrewType.SLEEPING) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (!level.isClientSide) {
            BlockHitResult hit = level.clip(new ClipContext(player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(24.0D)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
            BlockPos target = hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS
                    ? hit.getBlockPos()
                    : player.blockPosition();
            boolean hollowBrew = isHollowTearsBrew();
            boolean applied = applyWorldBrewEffect(level, target, player);
            if (applied) {
                level.playSound(null, target, SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            } else if (hollowBrew) {
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private boolean isHollowTearsBrew() {
        return brewType == BrewType.SOLID_DIRT || brewType == BrewType.SOLID_STONE
                || brewType == BrewType.SOLID_SAND || brewType == BrewType.SOLID_SANDSTONE
                || brewType == BrewType.HOLLOW_TEARS_EROSION;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            switch (brewType) {
                case GROTESQUE -> entity.addEffect(new MobEffectInstance(ModMobEffects.GROTESQUE, 2400, 0));
                case DEPTHS -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 6000, 0));
                    entity.addEffect(new MobEffectInstance(ModMobEffects.SWIMMING, 6000, 0));
                }
                case SLEEPING -> entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                default -> {}
            }
        }
        if (entity instanceof Player player && !player.isCreative()) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }

    private boolean applyWorldBrewEffect(Level level, BlockPos center, @Nullable Player user) {
        AABB area = new AABB(center).inflate(4.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        switch (brewType) {
            case VINES -> {
                for (LivingEntity e : entities) {
                    e.addEffect(new MobEffectInstance(ModMobEffects.WRAPPED_IN_VINE, 300, 0));
                }
            }
            case WEBS -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 1, 1))) {
                    if (level.getBlockState(p).isAir()) {
                        level.setBlock(p, Blocks.COBWEB.defaultBlockState(), 3);
                    }
                }
            }
            case THORNS -> {
                for (LivingEntity e : entities) {
                    e.addEffect(new MobEffectInstance(ModMobEffects.SPIKED, 400, 0));
                }
                if (level.getBlockState(center.above()).isAir()) {
                    level.setBlock(center.above(), Blocks.CACTUS.defaultBlockState(), 3);
                }
            }
            case INK -> {
                for (LivingEntity e : entities) {
                    if (e != user) e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0));
                }
            }
            case SPROUTING -> {
                for (LivingEntity e : entities) {
                    e.addEffect(new MobEffectInstance(ModMobEffects.SPROUTING, 300, 0));
                }
            }
            case EROSION -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                    BlockState st = level.getBlockState(p);
                    if (st.is(Blocks.STONE) || st.is(Blocks.STONE_BRICKS)) {
                        level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    } else if (st.is(Blocks.COBBLESTONE)) {
                        level.setBlock(p, Blocks.GRAVEL.defaultBlockState(), 3);
                    } else if (st.is(Blocks.GRAVEL) || st.is(Blocks.SANDSTONE)) {
                        level.setBlock(p, Blocks.SAND.defaultBlockState(), 3);
                    }
                }
            }
            case RAISING -> {
                Zombie zombie = EntityType.ZOMBIE.create(level);
                if (zombie != null) {
                    zombie.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, 0.0F, 0.0F);
                    zombie.addEffect(new MobEffectInstance(ModMobEffects.ENSLAVED, 12000, 0));
                    level.addFreshEntity(zombie);
                }
            }
            case LOVE -> {
                for (Animal animal : level.getEntitiesOfClass(Animal.class, area)) {
                    animal.setInLove(user);
                }
            }
            case ICE -> {
                for (LivingEntity e : entities) {
                    if (e != user) e.addEffect(new MobEffectInstance(ModMobEffects.CHILLED, 400, 1));
                }
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                    if (level.getBlockState(p).is(Blocks.WATER)) {
                        level.setBlock(p, Blocks.ICE.defaultBlockState(), 3);
                    }
                }
            }
            case INFECTION -> {
                for (LivingEntity e : entities) {
                    if (e != user) e.addEffect(new MobEffectInstance(ModMobEffects.DISEASED, 1200, 0));
                }
            }
            case FLOWING_SPIRIT -> {
                if (level.getBlockState(center.above()).isAir()) {
                    level.setBlock(center.above(), ModBlocks.FLOWING_SPIRIT_BLOCK.get().defaultBlockState(), 3);
                }
            }
            case WASTING -> {
                for (LivingEntity e : entities) {
                    if (e != user) {
                        e.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 2));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 1));
                    }
                }
            }
            case BATS -> {
                for (int i = 0; i < 6; i++) {
                    Bat bat = EntityType.BAT.create(level);
                    if (bat != null) {
                        bat.moveTo(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D, 0.0F, 0.0F);
                        level.addFreshEntity(bat);
                    }
                }
            }
            case SUBSTITUTION -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                    if (level.getBlockState(p).is(Blocks.DIRT) || level.getBlockState(p).is(Blocks.GRASS_BLOCK)) {
                        level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 3);
                    }
                }
            }
            case REVEALING -> {
                for (LivingEntity e : entities) {
                    e.removeEffect(MobEffects.INVISIBILITY);
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 400, 0));
                }
            }
            case SOLID_DIRT -> {
                return OccultumFluidBlocks.HollowTearsBlock.solidifyCascade(level, center, Blocks.DIRT.defaultBlockState()) > 0;
            }
            case SOLID_STONE -> {
                return OccultumFluidBlocks.HollowTearsBlock.solidifyCascade(level, center, Blocks.STONE.defaultBlockState()) > 0;
            }
            case SOLID_SAND -> {
                return OccultumFluidBlocks.HollowTearsBlock.solidifyCascade(level, center, Blocks.SAND.defaultBlockState()) > 0;
            }
            case SOLID_SANDSTONE -> {
                return OccultumFluidBlocks.HollowTearsBlock.solidifyCascade(level, center, Blocks.SANDSTONE.defaultBlockState()) > 0;
            }
            case HOLLOW_TEARS_EROSION -> {
                return OccultumFluidBlocks.HollowTearsBlock.solidifyCascade(level, center, Blocks.AIR.defaultBlockState()) > 0;
            }
            case CURSED_LEAPING -> {
                for (LivingEntity e : entities) {
                    e.setDeltaMovement(e.getDeltaMovement().x, 1.4D, e.getDeltaMovement().z);
                    e.hurtMarked = true;
                }
            }
            case FROGS_TONGUE -> {
                if (user != null) {
                    for (LivingEntity e : entities) {
                        if (e != user) {
                            double dx = (user.getX() - e.getX()) * 0.35D;
                            double dy = (user.getY() - e.getY()) * 0.25D + 0.3D;
                            double dz = (user.getZ() - e.getZ()) * 0.35D;
                            e.setDeltaMovement(dx, dy, dz);
                            e.hurtMarked = true;
                        }
                    }
                }
            }
            case HITCHCOCK -> {
                for (LivingEntity e : entities) {
                    if (e != user) {
                        e.hurt(level.damageSources().magic(), 6.0F);
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                    }
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
