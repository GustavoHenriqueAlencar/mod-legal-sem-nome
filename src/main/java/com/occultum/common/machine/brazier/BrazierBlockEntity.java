package com.occultum.common.machine.brazier;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class BrazierBlockEntity extends BlockEntity {

    public static final double ALTAR_DRAIN_PER_TICK = 0.25D;

    public record BrazierRecipe(
            String id,
            Supplier<Item> ing1,
            Supplier<Item> ing2,
            Supplier<Item> ing3,
            int burnDurationTicks,
            boolean continuousAura
    ) {
        public boolean matches(NonNullList<ItemStack> slots) {
            List<Item> required = new ArrayList<>(List.of(ing1.get(), ing2.get(), ing3.get()));
            for (int i = 0; i < 3; i++) {
                Item s = slots.get(i).getItem();
                if (!required.remove(s)) {
                    return false;
                }
            }
            return required.isEmpty();
        }
    }

    private static List<BrazierRecipe> RECIPES;

    public static List<BrazierRecipe> getRecipes() {
        if (RECIPES == null) {
            RECIPES = List.of(
                    // 1. Spectres (30s = 600 ticks)
                    new BrazierRecipe("spectres", ModItems.WORMWOOD::get, ModItems.WOOL_OF_BAT::get, ModItems.GRAVEYARD_DUST::get, 600, false),
                    // 2. Banshees (30s = 600 ticks)
                    new BrazierRecipe("banshees", ModItems.WORMWOOD::get, ModItems.CONDENSED_FEAR::get, ModItems.GRAVEYARD_DUST::get, 600, false),
                    // 3. Poltergeist (45s = 900 ticks)
                    new BrazierRecipe("poltergeist", ModItems.WORMWOOD::get, ModItems.REFINED_EVIL::get, ModItems.FOCUSED_WILL::get, 900, false),
                    // 4. Smoke (5 min = 6,000 ticks)
                    new BrazierRecipe("smoke", () -> Items.GUNPOWDER, ModItems.QUICKLIME::get, () -> Items.COBWEB, 6000, true),
                    // 5. Strong (5 min = 6,000 ticks)
                    new BrazierRecipe("strong", ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.GOLDEN_CARROT, () -> Items.MAGMA_CREAM, 6000, true),
                    // 6. Tough (5 min = 6,000 ticks)
                    new BrazierRecipe("tough", ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.FERMENTED_SPIDER_EYE, () -> Items.MAGMA_CREAM, 6000, true),
                    // 7. Invisible (10 min = 12,000 ticks)
                    new BrazierRecipe("invisible", () -> Items.GOLDEN_CARROT, () -> Items.POTION, () -> Items.GLOWSTONE_DUST, 12000, true),
                    // 8. Wilting (1 min = 1,200 ticks)
                    new BrazierRecipe("wilting", ModItems.CONDENSED_FEAR::get, ModItems.ROTTEN_APPLE::get, ModItems.GRAVEYARD_DUST::get, 1200, true)
            );
        }
        return RECIPES;
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private boolean burning = false;
    private int burnTicks = 0;
    private int totalBurnTicks = 0;
    private String activeRecipeId = "";
    @Nullable
    private UUID operatorUuid;

    public BrazierBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BRAZIER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrazierBlockEntity be) {
        if (!be.burning) return;

        // Continuous Altar Power connection (16-block radius, Section 1.1)
        if (!AltarPowerNetwork.consumePower(level, pos, ALTAR_DRAIN_PER_TICK)) {
            be.abortBurn(level, pos, state);
            return;
        }

        be.burnTicks++;

        // Smoke & Spark particles every 100 ticks (5 seconds, Section 1.1)
        if (be.burnTicks % 100 == 0 && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.95, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.03);
            serverLevel.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.95, pos.getZ() + 0.5, 4, 0.15, 0.1, 0.15, 0.02);
        }

        // Apply continuous area aura every 20 ticks
        if (be.burnTicks % 20 == 0 && level instanceof ServerLevel serverLevel) {
            be.tickContinuousAura(serverLevel, pos);
        }

        if (be.burnTicks >= be.totalBurnTicks) {
            if (level instanceof ServerLevel serverLevel) {
                be.completeBurn(serverLevel, pos);
            }
            be.finishAndEmpty(level, pos, state);
        }
    }

    private void tickContinuousAura(ServerLevel level, BlockPos pos) {
        AABB area16 = new AABB(pos).inflate(16.0);
        switch (activeRecipeId) {
            case "smoke" -> {
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 25, 6.0, 2.0, 6.0, 0.01);
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area16)) {
                    if (operatorUuid != null && operatorUuid.equals(entity.getUUID())) continue;
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    if (entity instanceof Monster monster) {
                        monster.setTarget(null);
                    }
                }
            }
            case "strong" -> {
                for (Player player : level.getEntitiesOfClass(Player.class, area16)) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0, true, true));
                }
            }
            case "tough" -> {
                for (Player player : level.getEntitiesOfClass(Player.class, area16)) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, true, true));
                }
            }
            case "invisible" -> {
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area16)) {
                    if (entity instanceof Player || ! (entity instanceof Monster)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, true, false));
                    }
                }
            }
            case "wilting" -> {
                AABB area12 = new AABB(pos).inflate(12.0);
                for (BlockPos p : BlockPos.betweenClosed(pos.offset(-12, -2, -12), pos.offset(12, 3, 12))) {
                    BlockState st = level.getBlockState(p);
                    if (st.is(BlockTags.CROPS) || st.is(BlockTags.LEAVES)) {
                        if (level.random.nextInt(15) == 0) {
                            level.destroyBlock(p, false);
                        }
                    }
                }
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area12)) {
                    if (operatorUuid != null && operatorUuid.equals(entity.getUUID())) continue;
                    entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                }
            }
            default -> {
            }
        }
    }

    private void completeBurn(ServerLevel level, BlockPos pos) {
        switch (activeRecipeId) {
            case "spectres" -> {
                spawnApparition(level, pos, "Spectre", false);
                if (level.random.nextFloat() < 0.05F) {
                    spawnApparition(level, pos, "Poltergeist", true);
                }
            }
            case "banshees" -> {
                spawnBanshee(level, pos);
                if (level.random.nextFloat() < 0.05F) {
                    spawnApparition(level, pos, "Poltergeist", true);
                }
            }
            case "poltergeist" -> {
                spawnApparition(level, pos, "Poltergeist", true);
                disturbNearbyDoors(level, pos);
                if (level.random.nextFloat() < 0.05F) {
                    spawnApparition(level, pos, "Poltergeist", true);
                }
            }
            default -> {
            }
        }
    }

    private void spawnApparition(ServerLevel level, BlockPos pos, String name, boolean invisible) {
        Vex apparition = EntityType.VEX.create(level);
        if (apparition != null) {
            apparition.moveTo(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 0, 0);
            apparition.setCustomName(Component.literal(name));
            apparition.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            if (invisible) {
                apparition.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 24000, 0, false, false));
            }
            level.addFreshEntity(apparition);
            level.playSound(null, pos, SoundEvents.VEX_CHARGE, SoundSource.HOSTILE, 1.0F, 0.6F);
        }
    }

    private void spawnBanshee(ServerLevel level, BlockPos pos) {
        Phantom banshee = EntityType.PHANTOM.create(level);
        if (banshee != null) {
            banshee.moveTo(pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5, 0, 0);
            banshee.setCustomName(Component.literal("Banshee"));
            banshee.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(banshee);
            level.playSound(null, pos, SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, 1.2F, 1.4F);
        }
    }

    private void disturbNearbyDoors(ServerLevel level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-6, -2, -6), pos.offset(6, 3, 6))) {
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof DoorBlock door) {
                door.setOpen(null, level, st, p, !st.getValue(DoorBlock.OPEN));
            } else if (st.getBlock() instanceof TrapDoorBlock) {
                level.setBlockAndUpdate(p, st.cycle(TrapDoorBlock.OPEN));
            }
        }
    }

    public boolean tryIgnite(Player player) {
        if (burning) return false;
        Optional<BrazierRecipe> match = getRecipes().stream().filter(r -> r.matches(items)).findFirst();
        if (match.isEmpty()) return false;

        BrazierRecipe recipe = match.get();
        this.burning = true;
        this.burnTicks = 0;
        this.totalBurnTicks = recipe.burnDurationTicks();
        this.activeRecipeId = recipe.id();
        this.operatorUuid = player.getUUID();

        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(BrazierBlock.LIT, true), 3);
        }
        sync();
        return true;
    }

    private void abortBurn(Level level, BlockPos pos, BlockState state) {
        finishAndEmpty(level, pos, state);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.8F);
    }

    private void finishAndEmpty(Level level, BlockPos pos, BlockState state) {
        this.burning = false;
        this.burnTicks = 0;
        this.totalBurnTicks = 0;
        this.activeRecipeId = "";
        for (int i = 0; i < 3; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        if (state.getValue(BrazierBlock.LIT)) {
            level.setBlock(pos, state.setValue(BrazierBlock.LIT, false), 3);
        }
        sync();
    }

    public boolean insertIngredient(ItemStack stack) {
        for (int i = 0; i < 3; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.copyWithCount(1));
                stack.shrink(1);
                sync();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractLastItem() {
        if (burning) return ItemStack.EMPTY;
        for (int i = 2; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack out = items.get(i);
                items.set(i, ItemStack.EMPTY);
                sync();
                return out;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getSlot(int index) {
        return items.get(index);
    }

    public boolean isBurning() {
        return burning;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putBoolean("Burning", burning);
        tag.putInt("BurnTicks", burnTicks);
        tag.putInt("TotalBurnTicks", totalBurnTicks);
        tag.putString("ActiveRecipeId", activeRecipeId);
        if (operatorUuid != null) {
            tag.putUUID("OperatorUuid", operatorUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        burning = tag.getBoolean("Burning");
        burnTicks = tag.getInt("BurnTicks");
        totalBurnTicks = tag.getInt("TotalBurnTicks");
        activeRecipeId = tag.getString("ActiveRecipeId");
        operatorUuid = tag.hasUUID("OperatorUuid") ? tag.getUUID("OperatorUuid") : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
