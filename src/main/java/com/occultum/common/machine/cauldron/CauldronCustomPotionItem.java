package com.occultum.common.machine.cauldron;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModDataComponentTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class CauldronCustomPotionItem extends Item {

    public record CatalogEffect(
            String id,
            Supplier<Item> ingredient,
            int levelCost,
            int altarPower,
            int baseDurationTicks,
            int defaultColor
    ) {}

    private static List<CatalogEffect> CATALOG;

    public static List<CatalogEffect> getCatalog() {
        if (CATALOG == null) {
            List<CatalogEffect> list = new ArrayList<>();
            // Section 5.1: Levels 1 to 2
            list.add(new CatalogEffect("snow", () -> Items.SNOWBALL, 1, 0, 3600, 0xE0F8FF));
            list.add(new CatalogEffect("swimming", () -> Items.COD, 1, 0, 3600, 0x3A88D8));
            list.add(new CatalogEffect("ender_inhibition", ModItems.ENDER_DEW::get, 1, 200, 2400, 0x266A5A));
            list.add(new CatalogEffect("moonshine", () -> Items.WHEAT, 1, 0, 1800, 0xD4A843));
            list.add(new CatalogEffect("extinguish", () -> Items.COAL, 1, 0, 0, 0x506070));
            list.add(new CatalogEffect("dissipate", () -> Items.SMOOTH_STONE, 1, 0, 0, 0xB8C0C8));
            list.add(new CatalogEffect("flowers", () -> Items.DANDELION, 1, 200, 0, 0xF5E042));
            list.add(new CatalogEffect("fertilization", () -> Items.BONE_MEAL, 1, 250, 0, 0xE8F0E0));
            list.add(new CatalogEffect("harvesting", () -> Items.APPLE, 1, 0, 0, 0xD83838));
            list.add(new CatalogEffect("tilling", () -> Items.DIRT, 1, 0, 0, 0x7A5438));
            list.add(new CatalogEffect("planting", () -> Items.WHEAT_SEEDS, 1, 0, 0, 0x62B848));
            list.add(new CatalogEffect("pruning", () -> Items.BROWN_MUSHROOM, 1, 0, 0, 0x8A6448));
            list.add(new CatalogEffect("felling", () -> Items.STRING, 1, 0, 0, 0xC8C8C8));
            list.add(new CatalogEffect("pulverisation", () -> Items.FLINT, 1, 250, 0, 0x585860));
            list.add(new CatalogEffect("tidehold", () -> Items.SAND, 1, 0, 0, 0xE4D8A0));
            list.add(new CatalogEffect("lilify", () -> Items.LILY_PAD, 1, 200, 0, 0x2A8A3A));
            list.add(new CatalogEffect("wolfsbane", ModItems.WOLFSBANE::get, 1, 0, 2400, 0x7A42C8));
            list.add(new CatalogEffect("remove_debuffs", ModItems.PURIFIED_MILK::get, 2, 200, 0, 0xFFFFFF));
            list.add(new CatalogEffect("remove_buffs", ModItems.REEK_OF_MISFORTUNE::get, 2, 250, 0, 0x4A3A58));
            list.add(new CatalogEffect("lavahold", () -> Items.COBBLESTONE, 2, 100, 0, 0xE06020));
            list.add(new CatalogEffect("repel_attacker", ModItems.WILD_BRAMBLE_ITEM::get, 2, 250, 2400, 0x388A38));
            list.add(new CatalogEffect("gas_mask", () -> Items.GRAVEL, 2, 100, 3600, 0x808080));
            list.add(new CatalogEffect("poison", () -> Items.SPIDER_EYE, 2, 0, 900, 0x4E9331));
            list.add(new CatalogEffect("speed", () -> Items.SUGAR, 2, 100, 3600, 0x7CAFC6));
            list.add(new CatalogEffect("water_breathing", () -> Items.PUFFERFISH, 2, 100, 3600, 0x2E5299));
            list.add(new CatalogEffect("fire_resistance", () -> Items.MAGMA_CREAM, 2, 100, 3600, 0xE49A3A));
            list.add(new CatalogEffect("night_vision", () -> Items.GOLDEN_CARROT, 2, 200, 3600, 0x1F1FA1));
            list.add(new CatalogEffect("regeneration", () -> Items.GHAST_TEAR, 2, 200, 900, 0xCD5CAB));
            list.add(new CatalogEffect("strength", () -> Items.BLAZE_POWDER, 2, 200, 3600, 0x932423));
            list.add(new CatalogEffect("instant_health", () -> Items.GLISTERING_MELON_SLICE, 2, 200, 0, 0xF82423));
            list.add(new CatalogEffect("floating", () -> Items.SUGAR_CANE, 2, 250, 1200, 0xA8F0B8));
            list.add(new CatalogEffect("jump_boost", () -> Items.LEATHER, 2, 200, 3600, 0x22FF4C));
            list.add(new CatalogEffect("feather_fall", () -> Items.FEATHER, 2, 100, 3600, 0xF0F4FF));
            list.add(new CatalogEffect("poison_weapons", () -> Items.RED_MUSHROOM, 2, 200, 2400, 0x68A028));
            list.add(new CatalogEffect("reflect_projectiles", () -> Items.COBWEB, 2, 250, 2400, 0xD8E0E8));
            list.add(new CatalogEffect("bat_burst", ModItems.BAT_BALL::get, 2, 1000, 0, 0x2D2338));
            list.add(new CatalogEffect("bodega", ModItems.OWLETS_WING::get, 2, 1000, 0, 0x9E7A56));
            list.add(new CatalogEffect("air_hike", ModItems.BREATH_OF_THE_GODDESS::get, 2, 750, 2400, 0xB8F0FF));
            list.add(new CatalogEffect("frog_tongue", () -> Items.SLIME_BALL, 2, 150, 0, 0x58C848));
            list.add(new CatalogEffect("harm_undead", () -> Items.MELON, 2, 200, 0, 0xF05030));
            list.add(new CatalogEffect("harm_insects", () -> Items.RED_TULIP, 2, 200, 0, 0xB83050));
            list.add(new CatalogEffect("erosion", ModItems.OIL_OF_VITRIOL::get, 2, 200, 0, 0x5A9032));
            list.add(new CatalogEffect("levelling", () -> Items.PUMPKIN, 2, 200, 0, 0xD87A20));
            list.add(new CatalogEffect("vines", () -> Items.VINE, 2, 150, 0, 0x2D7A2D));
            list.add(new CatalogEffect("thorns", () -> Items.CACTUS, 2, 150, 2400, 0x3B8A28));
            list.add(new CatalogEffect("sprouting", ModItems.ENT_TWIG::get, 2, 350, 1200, 0x628A32));
            list.add(new CatalogEffect("cold", ModItems.ICY_NEEDLE::get, 2, 200, 900, 0x70D0FF));
            list.add(new CatalogEffect("knockback", () -> Items.STICK, 2, 200, 0, 0xA88858));
            list.add(new CatalogEffect("instant_tree", () -> Items.OAK_SAPLING, 2, 200, 0, 0x389038));

            // Section 5.2: Advanced Effects, Curses & Transmutations (Levels 3 to 8)
            list.add(new CatalogEffect("animal_attraction", ModItems.GOLDEN_HEART::get, 4, 500, 2400, 0xFFD838));
            list.add(new CatalogEffect("harm_werewolves", ModItems.SILVER_DUST::get, 4, 1000, 0, 0xD0E0F0));
            list.add(new CatalogEffect("weaken_vampires", ModItems.GARLIC::get, 4, 500, 1800, 0xF4F0D8));
            list.add(new CatalogEffect("wither", () -> Items.WITHER_SKELETON_SKULL, 4, 200, 800, 0x352A27));
            list.add(new CatalogEffect("inferno", ModItems.GLINT_WEED_ITEM::get, 3, 750, 300, 0xFF5500));
            list.add(new CatalogEffect("fear", ModItems.DEMONIC_BLOOD::get, 2, 500, 1200, 0x9A1818));
            list.add(new CatalogEffect("blindness", () -> Items.INK_SAC, 4, 1000, 900, 0x181820));
            list.add(new CatalogEffect("love", () -> Items.POPPY, 4, 500, 0, 0xFF3870));
            list.add(new CatalogEffect("paralysis", ModItems.DEMON_HEART::get, 4, 750, 400, 0x701040));
            list.add(new CatalogEffect("potion_master", ModItems.DROP_OF_LUCK::get, 4, 5000, 3600, 0x30E878));
            list.add(new CatalogEffect("diseased", () -> Items.ROTTEN_FLESH, 4, 2000, 1800, 0x6A7830));
            list.add(new CatalogEffect("sinking", ModItems.DISTURBED_COTTON::get, 4, 3000, 1200, 0x384868));
            list.add(new CatalogEffect("overheating", ModItems.EMBER_MOSS_ITEM::get, 4, 3000, 1200, 0xFF6A10));
            list.add(new CatalogEffect("waking_nightmare", ModItems.MELLIFLUOUS_HUNGER::get, 4, 10000, 3600, 0x42124A));
            list.add(new CatalogEffect("frogs_leg", ModItems.TOE_OF_FROG::get, 4, 500, 3600, 0x48B848));
            list.add(new CatalogEffect("absorption", () -> Items.GOLDEN_APPLE, 4, 1000, 2400, 0xF8C820));
            list.add(new CatalogEffect("health_boost", () -> Items.ENCHANTED_GOLDEN_APPLE, 4, 1000, 3600, 0xF87823));
            list.add(new CatalogEffect("wasting", ModItems.SUBDUED_SPIRIT::get, 4, 500, 1200, 0x586860));
            list.add(new CatalogEffect("revealing", ModItems.ODOUR_OF_PURITY::get, 4, 100, 2400, 0xE8F8FF));
            list.add(new CatalogEffect("stout_belly", ModItems.FOUL_FUME::get, 4, 1000, 3600, 0x685838));
            list.add(new CatalogEffect("blight", () -> Items.POISONOUS_POTATO, 4, 2000, 0, 0x6A6028));
            list.add(new CatalogEffect("transpose", () -> Items.ENDER_PEARL, 4, 1000, 0, 0x167868));
            list.add(new CatalogEffect("transpose_ore", () -> Items.IRON_INGOT, 4, 2000, 0, 0xC8C8D0));
            list.add(new CatalogEffect("volatility", () -> Items.SHORT_GRASS, 4, 1000, 1800, 0xC85020));
            list.add(new CatalogEffect("darkness_allergy", () -> Items.JACK_O_LANTERN, 4, 4000, 1200, 0x382048));
            list.add(new CatalogEffect("absorb_magic", ModItems.WHIFF_OF_MAGIC::get, 4, 2000, 2400, 0xA848F0));
            list.add(new CatalogEffect("raising", () -> Items.BONE, 4, 2000, 0, 0x583868));
            list.add(new CatalogEffect("raise_land", () -> Items.QUARTZ, 4, 2000, 0, 0xE8E0D8));
            list.add(new CatalogEffect("harm_demons", () -> Items.GOLD_BLOCK, 5, 500, 0, 0xFFE840));
            list.add(new CatalogEffect("ice_shell", ModItems.FROZEN_HEART::get, 5, 500, 1200, 0x50B8FF));
            list.add(new CatalogEffect("reflect_damage", ModItems.SPECTRAL_DUST::get, 5, 2000, 1800, 0x90D8E8));
            list.add(new CatalogEffect("hellgate", ModItems.REFINED_EVIL::get, 5, 3000, 0, 0xB01010));
            list.add(new CatalogEffect("blast", () -> Items.GOLD_INGOT, 5, 500, 0, 0xFF8818));
            list.add(new CatalogEffect("poison_toad", () -> Items.EMERALD_BLOCK, 5, 500, 0, 0x20B848));
            list.add(new CatalogEffect("ice_world", () -> Items.ENDER_EYE, 5, 2000, 0, 0x40A0FF));
            list.add(new CatalogEffect("drain_magic", ModItems.CONDENSED_FEAR::get, 6, 1000, 0, 0x3A1850));
            list.add(new CatalogEffect("fortune", () -> Items.CLAY_BALL, 6, 1000, 3600, 0x8898B8));
            list.add(new CatalogEffect("sun_allergy", () -> Items.SALMON, 6, 1000, 1200, 0xFF9030));
            list.add(new CatalogEffect("ill_fitting", ModItems.BLOOD_BRAMBLE_ITEM::get, 6, 8000, 72000, 0xA01828));
            list.add(new CatalogEffect("reincarnate", ModItems.HINT_OF_REBIRTH::get, 6, 2500, 6000, 0xFFE890));
            list.add(new CatalogEffect("duration_boost", ModItems.CREEPER_HEART::get, 6, 5000, 0, 0x38D048));
            list.add(new CatalogEffect("resizing", () -> Items.EMERALD, 6, 2500, 3600, 0x18D868));
            list.add(new CatalogEffect("steal_buffs", () -> Items.SKELETON_SKULL, 6, 100, 0, 0xC0C0C0));
            list.add(new CatalogEffect("keep_inventory", ModItems.FOCUSED_WILL::get, 8, 10000, 6000, 0x48E0FF));
            list.add(new CatalogEffect("keep_effects", ModItems.REDSTONE_SOUP::get, 8, 10000, 6000, 0xFF3858));
            list.add(new CatalogEffect("spread_debuffs", () -> Items.CREEPER_HEAD, 8, 2000, 0, 0x289028));
            CATALOG = list;
        }
        return CATALOG;
    }

    public static Optional<CatalogEffect> findByIngredient(Item item) {
        return getCatalog().stream().filter(e -> e.ingredient().get() == item).findFirst();
    }

    public static Optional<CatalogEffect> findById(String id) {
        return getCatalog().stream().filter(e -> e.id().equals(id)).findFirst();
    }

    public CauldronCustomPotionItem(Properties properties) {
        super(properties);
    }

    public static CauldronPotionData getData(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.CAULDRON_POTION.get(), CauldronPotionData.EMPTY);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        CauldronPotionData data = getData(stack);
        if (data.dispersal() == CauldronPotionData.DispersalType.DRINKABLE) {
            return data.getDrinkTicks();
        }
        return 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        CauldronPotionData data = getData(stack);
        return data.dispersal() == CauldronPotionData.DispersalType.DRINKABLE ? UseAnim.DRINK : UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CauldronPotionData data = getData(stack);

        if (data.dispersal() == CauldronPotionData.DispersalType.DRINKABLE) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else {
            if (!level.isClientSide) {
                HitResult hit = player.pick(20.0D, 0.0F, false);
                Vec3 pos = hit.getType() != HitResult.Type.MISS ? hit.getLocation() : player.position().add(player.getLookAngle().scale(6.0D));
                BlockPos targetPos = BlockPos.containing(pos);
                deployDispersal((ServerLevel) level, player, targetPos, data);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.8F, 0.9F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        CauldronPotionData data = getData(stack);
        if (!level.isClientSide && entityLiving instanceof Player player) {
            if (!data.disableEntityEffects()) {
                for (CauldronPotionData.EffectEntry entry : data.effects()) {
                    applyEffectToEntity((ServerLevel) level, player, player, entry, data.noParticles());
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (stack.isEmpty()) {
                    return bottle;
                }
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
            }
        }
        return stack;
    }

    public static void deployDispersal(ServerLevel level, LivingEntity thrower, BlockPos center, CauldronPotionData data) {
        int radius = 3 + data.extentBonus();
        level.playSound(null, center, SoundEvents.SPLASH_POTION_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, data.colorRgb()),
                center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 45, radius * 0.4, 0.5, radius * 0.4, 0.15);

        if (data.dispersal() == CauldronPotionData.DispersalType.GAS || data.dispersal() == CauldronPotionData.DispersalType.LIQUID
                || data.dispersal() == CauldronPotionData.DispersalType.TRIGGER) {
            AreaEffectCloud cloud = new AreaEffectCloud(level, center.getX() + 0.5, center.getY() + 0.2, center.getZ() + 0.5);
            cloud.setOwner(thrower);
            cloud.setRadius(data.dispersal() == CauldronPotionData.DispersalType.TRIGGER ? 1.5F : radius);
            int lingerSeconds = switch (data.lingerBonus()) {
                case 1 -> 15;
                case 2 -> 30;
                case 3 -> 60;
                default -> 8;
            };
            cloud.setDuration(lingerSeconds * 20);
            cloud.setWaitTime(data.dispersal() == CauldronPotionData.DispersalType.TRIGGER ? 0 : 10);
            level.addFreshEntity(cloud);
        }

        if (!data.disableBlockEffects()) {
            for (CauldronPotionData.EffectEntry entry : data.effects()) {
                applyEffectToBlocks(level, thrower, center, radius, entry);
            }
        }
        if (!data.disableEntityEffects()) {
            AABB area = new AABB(center).inflate(radius);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
                for (CauldronPotionData.EffectEntry entry : data.effects()) {
                    applyEffectToEntity(level, thrower, target, entry, data.noParticles());
                }
            }
        }
    }

    public static void applyEffectToEntity(ServerLevel level, LivingEntity thrower, LivingEntity target,
                                           CauldronPotionData.EffectEntry entry, boolean hideParticles) {
        Optional<CatalogEffect> catOpt = findById(entry.effectId());
        int baseDuration = catOpt.map(CatalogEffect::baseDurationTicks).orElse(1200);
        int multiplier = switch (entry.durationTier()) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            default -> 1;
        };
        int duration = baseDuration * multiplier;
        int amp = Math.max(0, entry.power());
        boolean inv = entry.inverted();

        switch (entry.effectId()) {
            case "snow", "cold", "ice_world" -> {
                if (inv) target.igniteForSeconds(5 + amp * 3);
                else target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Math.max(200, duration), amp, false, !hideParticles));
            }
            case "swimming" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.MOVEMENT_SLOWDOWN : MobEffects.DOLPHINS_GRACE, duration, amp, false, !hideParticles));
            case "moonshine" -> {
                if (!inv) {
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, !hideParticles));
                    target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amp, false, !hideParticles));
                } else {
                    target.removeEffect(MobEffects.CONFUSION);
                }
            }
            case "extinguish" -> {
                if (inv) target.igniteForSeconds(6 + amp * 2);
                else target.clearFire();
            }
            case "wolfsbane" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.DAMAGE_BOOST : MobEffects.WEAKNESS, duration, 2, false, !hideParticles));
            case "remove_debuffs" -> {
                List<MobEffectInstance> toRemove = target.getActiveEffects().stream()
                        .filter(inst -> inv ? inst.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL
                                : inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                        .toList();
                toRemove.forEach(inst -> target.removeEffect(inst.getEffect()));
            }
            case "remove_buffs" -> {
                List<MobEffectInstance> toRemove = target.getActiveEffects().stream()
                        .filter(inst -> inv ? inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL
                                : inst.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL)
                        .toList();
                toRemove.forEach(inst -> target.removeEffect(inst.getEffect()));
            }
            case "poison", "diseased", "poison_toad" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.REGENERATION : MobEffects.POISON, Math.max(200, duration), amp, false, !hideParticles));
            case "speed" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.MOVEMENT_SLOWDOWN : MobEffects.MOVEMENT_SPEED, duration, amp, false, !hideParticles));
            case "water_breathing" -> {
                if (inv) target.setAirSupply(0);
                else target.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, amp, false, !hideParticles));
            }
            case "fire_resistance", "lavahold" -> target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Math.max(200, duration), amp, false, !hideParticles));
            case "night_vision" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.BLINDNESS : MobEffects.NIGHT_VISION, duration, amp, false, !hideParticles));
            case "regeneration" -> {
                if (inv) target.hurt(level.damageSources().magic(), 6.0F * (amp + 1));
                else target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, amp, false, !hideParticles));
            }
            case "strength" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.WEAKNESS : MobEffects.DAMAGE_BOOST, duration, amp, false, !hideParticles));
            case "instant_health" -> {
                float amount = 4.0F * (1 << amp);
                if (inv) target.hurt(level.damageSources().magic(), amount);
                else target.heal(amount);
            }
            case "floating" -> target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, amp, false, !hideParticles));
            case "jump_boost", "frogs_leg", "air_hike" -> target.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, amp + 1, false, !hideParticles));
            case "feather_fall" -> target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, duration, amp, false, !hideParticles));
            case "bat_burst" -> {
                for (int i = 0; i < 12; i++) {
                    Bat bat = EntityType.BAT.create(level);
                    if (bat != null) {
                        bat.moveTo(target.getX(), target.getY() + 1.0, target.getZ(), 0, 0);
                        level.addFreshEntity(bat);
                    }
                }
                Vec3 back = target.getLookAngle().reverse().scale(8.0);
                target.teleportTo(target.getX() + back.x, target.getY(), target.getZ() + back.z);
            }
            case "bodega" -> {
                target.setDeltaMovement(target.getDeltaMovement().x, 1.6D, target.getDeltaMovement().z);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, !hideParticles));
            }
            case "frog_tongue" -> {
                if (thrower != null && thrower != target) {
                    Vec3 pull = thrower.position().subtract(target.position()).normalize().scale(inv ? -1.8 : 1.8);
                    target.setDeltaMovement(pull.x, 0.45, pull.z);
                    target.hurtMarked = true;
                }
            }
            case "harm_undead" -> {
                if (target.getType().is(EntityTypeTags.UNDEAD)) {
                    if (inv) target.heal(12.0F * (amp + 1));
                    else target.hurt(level.damageSources().magic(), 12.0F * (amp + 1));
                }
            }
            case "harm_insects" -> {
                if (target.getType().is(EntityTypeTags.ARTHROPOD)) {
                    if (inv) target.heal(12.0F * (amp + 1));
                    else target.hurt(level.damageSources().magic(), 12.0F * (amp + 1));
                }
            }
            case "knockback" -> {
                Vec3 dir = thrower != null ? target.position().subtract(thrower.position()).normalize() : new Vec3(0, 1, 0);
                double scale = inv ? -1.8 : 2.2;
                target.setDeltaMovement(dir.x * scale, 0.5, dir.z * scale);
                target.hurtMarked = true;
            }
            case "animal_attraction", "love" -> {
                if (target instanceof Animal animal && !inv) {
                    animal.setInLove(thrower instanceof Player p ? p : null);
                }
            }
            case "harm_werewolves" -> target.hurt(level.damageSources().magic(), 16.0F);
            case "weaken_vampires", "wasting" -> {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amp, false, !hideParticles));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amp, false, !hideParticles));
            }
            case "wither" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.REGENERATION : MobEffects.WITHER, duration, amp, false, !hideParticles));
            case "inferno" -> {
                if (inv) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 2, false, !hideParticles));
                else target.igniteForSeconds(15 * (amp + 1));
            }
            case "blindness", "darkness_allergy" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.NIGHT_VISION : MobEffects.BLINDNESS, duration, amp, false, !hideParticles));
            case "paralysis" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.MOVEMENT_SPEED : MobEffects.MOVEMENT_SLOWDOWN, duration, inv ? 3 : 6, false, !hideParticles));
            case "potion_master", "fortune" -> target.addEffect(new MobEffectInstance(MobEffects.LUCK, duration, amp, false, !hideParticles));
            case "absorption" -> target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, amp, false, !hideParticles));
            case "health_boost", "reincarnate", "keep_inventory", "keep_effects" -> target.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, duration, amp + 1, false, !hideParticles));
            case "revealing" -> {
                if (inv) target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, false, !hideParticles));
                else {
                    target.removeEffect(MobEffects.INVISIBILITY);
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, !hideParticles));
                }
            }
            case "stout_belly" -> target.addEffect(new MobEffectInstance(inv ? MobEffects.HUNGER : MobEffects.SATURATION, duration, amp, false, !hideParticles));
            case "transpose" -> {
                if (thrower != null && thrower != target && !inv) {
                    Vec3 a = thrower.position();
                    Vec3 b = target.position();
                    thrower.teleportTo(b.x, b.y, b.z);
                    target.teleportTo(a.x, a.y, a.z);
                }
            }
            case "harm_demons" -> {
                if (inv) target.heal(30.0F);
                else target.hurt(level.damageSources().magic(), 30.0F);
            }
            case "ill_fitting" -> {
                if (!inv) {
                    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                        ItemStack eq = target.getItemBySlot(slot);
                        if (!eq.isEmpty()) {
                            target.spawnAtLocation(eq.copy());
                            target.setItemSlot(slot, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
            case "duration_boost" -> {
                List<MobEffectInstance> boosted = new ArrayList<>();
                for (MobEffectInstance inst : target.getActiveEffects()) {
                    if (inst.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL) {
                        int newDur = inv ? inst.getDuration() / 2 : (int) (inst.getDuration() * 1.5);
                        boosted.add(new MobEffectInstance(inst.getEffect(), newDur, inst.getAmplifier(), inst.isAmbient(), inst.isVisible()));
                    }
                }
                boosted.forEach(target::addEffect);
            }
            case "steal_buffs" -> {
                if (thrower != null && thrower != target) {
                    List<MobEffectInstance> stolen = target.getActiveEffects().stream()
                            .filter(inst -> inst.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL)
                            .limit(3)
                            .toList();
                    for (MobEffectInstance inst : stolen) {
                        target.removeEffect(inst.getEffect());
                        thrower.addEffect(new MobEffectInstance(inst));
                    }
                }
            }
            default -> target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Math.max(200, duration), amp, false, !hideParticles));
        }
    }

    public static void applyEffectToBlocks(ServerLevel level, LivingEntity thrower, BlockPos center, int radius,
                                           CauldronPotionData.EffectEntry entry) {
        int r = Math.min(12, radius + entry.power());
        boolean inv = entry.inverted();

        switch (entry.effectId()) {
            case "snow", "cold", "ice_world" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
                    BlockState st = level.getBlockState(p);
                    if (inv) {
                        if (st.is(Blocks.ICE) || st.is(Blocks.FROSTED_ICE)) level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                        else if (st.is(Blocks.SNOW)) level.removeBlock(p, false);
                    } else {
                        if (st.is(Blocks.WATER)) level.setBlockAndUpdate(p, Blocks.ICE.defaultBlockState());
                        else if (st.isAir() && level.getBlockState(p.below()).isSolidRender(level, p.below())) {
                            level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
                        }
                    }
                }
            }
            case "extinguish", "inferno" -> {
                boolean ignite = entry.effectId().equals("inferno") ^ inv;
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
                    if (ignite) {
                        if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender(level, p.below()) && level.random.nextInt(3) == 0) {
                            level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                        }
                    } else if (level.getBlockState(p).is(Blocks.FIRE)) {
                        level.removeBlock(p, false);
                    }
                }
            }
            case "flowers" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
                    if (level.getBlockState(p).is(Blocks.GRASS_BLOCK) && level.getBlockState(p.above()).isAir() && level.random.nextInt(4) == 0) {
                        level.setBlockAndUpdate(p.above(), (level.random.nextBoolean() ? Blocks.DANDELION : Blocks.POPPY).defaultBlockState());
                    }
                }
            }
            case "fertilization" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
                    BlockState st = level.getBlockState(p);
                    if (!inv && st.getBlock() instanceof BonemealableBlock bm && bm.isValidBonemealTarget(level, p, st)) {
                        bm.performBonemeal(level, level.random, p, st);
                    }
                }
            }
            case "harvesting" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
                    BlockState st = level.getBlockState(p);
                    if (st.getBlock() instanceof CropBlock crop && crop.isMaxAge(st)) {
                        level.destroyBlock(p, true);
                    }
                }
            }
            case "tilling" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
                    BlockState st = level.getBlockState(p);
                    if (!inv && (st.is(Blocks.DIRT) || st.is(Blocks.GRASS_BLOCK)) && level.getBlockState(p.above()).isAir()) {
                        level.setBlockAndUpdate(p, Blocks.FARMLAND.defaultBlockState());
                    } else if (inv && st.is(Blocks.FARMLAND)) {
                        level.setBlockAndUpdate(p, Blocks.DIRT.defaultBlockState());
                    }
                }
            }
            case "pruning" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 3, r))) {
                    if (level.getBlockState(p).is(BlockTags.LEAVES)) {
                        level.destroyBlock(p, true);
                    }
                }
            }
            case "felling" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 12, r))) {
                    if (level.getBlockState(p).is(BlockTags.LOGS)) {
                        level.destroyBlock(p, true);
                    }
                }
            }
            case "pulverisation" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
                    BlockState st = level.getBlockState(p);
                    if (st.is(Blocks.STONE)) level.setBlockAndUpdate(p, Blocks.COBBLESTONE.defaultBlockState());
                    else if (st.is(Blocks.COBBLESTONE)) level.setBlockAndUpdate(p, Blocks.GRAVEL.defaultBlockState());
                    else if (st.is(Blocks.GRAVEL)) level.setBlockAndUpdate(p, Blocks.SAND.defaultBlockState());
                }
            }
            case "tidehold" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
                    if (level.getBlockState(p).is(Blocks.WATER)) {
                        level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            case "lilify" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
                    if (level.getBlockState(p).is(Blocks.WATER) && level.getBlockState(p.above()).isAir()) {
                        level.setBlockAndUpdate(p.above(), Blocks.LILY_PAD.defaultBlockState());
                    }
                }
            }
            case "lavahold" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
                    if (level.getBlockState(p).is(Blocks.LAVA)) {
                        level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                    }
                }
            }
            case "raising" -> {
                for (int i = 0; i < 2 + entry.power(); i++) {
                    Zombie z = EntityType.ZOMBIE.create(level);
                    if (z != null) {
                        z.moveTo(center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, 0, 0);
                        z.finalizeSpawn(level, level.getCurrentDifficultyAt(center), MobSpawnType.MOB_SUMMONED, null);
                        level.addFreshEntity(z);
                    }
                }
            }
            case "raise_land" -> {
                int height = 4 + entry.power() * 2;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = 0; dy < height; dy++) {
                            BlockPos pos = center.offset(dx, dy, dz);
                            if (level.getBlockState(pos).isAir()) {
                                level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                            }
                        }
                    }
                }
            }
            case "ice_shell" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 3, 3))) {
                    double dist = p.distSqr(center);
                    if (dist >= 4.0 && dist <= 10.0 && level.getBlockState(p).isAir()) {
                        level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                    }
                }
            }
            case "blast" -> level.explode(thrower, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                    2.0F + 1.5F * entry.power(), Level.ExplosionInteraction.BLOCK);
            case "blight" -> {
                for (BlockPos p : BlockPos.betweenClosed(center.offset(-6, -2, -6), center.offset(6, 2, 6))) {
                    BlockState st = level.getBlockState(p);
                    if (st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.FARMLAND)) {
                        level.setBlockAndUpdate(p, Blocks.COARSE_DIRT.defaultBlockState());
                    } else if (st.is(BlockTags.CROPS) || st.is(BlockTags.LEAVES)) {
                        level.removeBlock(p, false);
                    }
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CauldronPotionData data = getData(stack);
        tooltipComponents.add(Component.translatable("tooltip.occultum.dispersal." + data.dispersal().name().toLowerCase())
                .withStyle(ChatFormatting.AQUA));
        for (CauldronPotionData.EffectEntry e : data.effects()) {
            String roman = switch (e.power()) {
                case 1 -> " II";
                case 2 -> " III";
                case 3 -> " IV";
                default -> e.power() > 3 ? " " + (e.power() + 1) : " I";
            };
            String invTag = e.inverted() ? " [Inv]" : "";
            tooltipComponents.add(Component.literal("• ")
                    .append(Component.translatable("effect.occultum.cauldron." + e.effectId()))
                    .append(Component.literal(roman + invTag))
                    .withStyle(e.inverted() ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE));
        }
    }
}
