package com.occultum.common.ritual;

import com.occultum.common.botany.tree.MysticSaplingBlock;
import com.occultum.common.divination.FamiliarSystem;
import com.occultum.common.fetish.FetishBlockEntity;
import com.occultum.common.poppet.PoppetHandler;
import com.occultum.common.poppet.TaglockKitItem;
import com.occultum.common.registry.*;
import com.occultum.common.ritual.CircleGeometryValidator.ChalkColor;
import com.occultum.common.ritual.CircleGeometryValidator.DetectedRings;
import com.occultum.common.ritual.GoldenChalkBlockEntity.RitualItemSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Catálogo Exaustivo e Funcional dos 96 Rituais de Magia de Círculo
 * conforme a Seção 4 de 05_MAGIA_DE_CIRCULO_E_RITUAIS.md (#01 a #96).
 */
public final class CircleRitualRegistry {

    public enum TimeWeatherCondition {
        NONE,
        DAY_ONLY,
        NIGHT_ONLY,
        THUNDER_ONLY,
        OVERWORLD_NIGHT_ONLY,
        FULL_MOON_NIGHT_ONLY,
        NOON_SUN_ONLY
    }

    public record ConsumedRitualContext(
            ServerLevel level,
            BlockPos center,
            Player activator,
            DetectedRings rings,
            List<ItemStack> consumedItems,
            BlockPos firstFocusPos,
            @Nullable LivingEntity sacrificedEntity,
            int covenSize
    ) {
        public void spawnResult(ItemStack stack) {
            ItemEntity ie = new ItemEntity(level, center.getX() + 0.5, center.getY() + 0.6, center.getZ() + 0.5, stack);
            ie.setDeltaMovement(0.0, 0.2, 0.0);
            level.addFreshEntity(ie);
        }

        @Nullable
        public LivingEntity findTaglockTarget() {
            for (ItemStack s : consumedItems) {
                LivingEntity target = TaglockKitItem.resolveTarget(level.getServer(), s);
                if (target != null) return target;
            }
            return null;
        }

        @Nullable
        public String findTaglockUuidString() {
            for (ItemStack s : consumedItems) {
                String sig = s.get(ModDataComponentTypes.DNA_SIGNATURE.get());
                if (sig != null && !sig.isEmpty()) return sig;
            }
            return null;
        }

        @Nullable
        public GlobalPos findBoundCoordinates() {
            for (ItemStack s : consumedItems) {
                GlobalPos gp = s.get(ModDataComponentTypes.BOUND_COORDINATES.get());
                if (gp != null) return gp;
            }
            return null;
        }
    }

    public record RitualDefinition(
            int id,
            String name,
            ChalkColor smallRing,
            ChalkColor mediumRing,
            ChalkColor largeRing,
            int initialAltarCost,
            int upkeepCostPerSecond,
            int fixedDurationTicks,
            int requiredCoven,
            TimeWeatherCondition condition,
            List<Supplier<Item>> focusItems,
            @Nullable Predicate<LivingEntity> sacrificePredicate,
            boolean consumeSacrifice,
            Consumer<ConsumedRitualContext> initialAction,
            @Nullable UpkeepAction upkeepAction
    ) {
        public boolean matches(ServerLevel level, BlockPos center, Player player, DetectedRings rings,
                               List<RitualItemSource> availableItems, List<LivingEntity> livingInCircle, int covenSize) {
            if (!rings.matches(smallRing, mediumRing, largeRing)) {
                return false;
            }
            // Para rituais #01 e #45 (captura de matriz), exige que haja pelo menos 1 anel desenhado
            if ((id == 1 || id == 45) && !rings.hasAnyRing()) {
                return false;
            }
            if (covenSize < requiredCoven) {
                return false;
            }
            if (!checkCondition(level, center)) {
                return false;
            }
            if (sacrificePredicate != null) {
                boolean foundLiving = false;
                for (LivingEntity le : livingInCircle) {
                    if (sacrificePredicate.test(le)) {
                        foundLiving = true;
                        break;
                    }
                }
                if (!foundLiving) return false;
            }
            // Verificar lista de itens de foco (com multiplicidade)
            List<ItemStack> pool = new ArrayList<>();
            for (RitualItemSource src : availableItems) {
                pool.add(src.stack().copy());
            }
            for (Supplier<Item> reqSup : focusItems) {
                Item req = reqSup.get();
                boolean matchedOne = false;
                for (ItemStack s : pool) {
                    if (!s.isEmpty() && s.is(req)) {
                        s.shrink(1);
                        matchedOne = true;
                        break;
                    }
                }
                if (!matchedOne) return false;
            }
            return true;
        }

        private boolean checkCondition(ServerLevel level, BlockPos center) {
            return switch (condition) {
                case NONE -> true;
                case DAY_ONLY -> level.isDay();
                case NIGHT_ONLY -> level.isNight();
                case THUNDER_ONLY -> level.isThundering();
                case OVERWORLD_NIGHT_ONLY -> level.dimension() == Level.OVERWORLD && level.isNight();
                case FULL_MOON_NIGHT_ONLY -> level.isNight() && level.getMoonPhase() == 0;
                case NOON_SUN_ONLY -> level.isDay() && level.canSeeSky(center);
            };
        }

        public ConsumedRitualContext consumeInputs(ServerLevel level, BlockPos center, Player player,
                                                   DetectedRings rings, List<RitualItemSource> availableItems,
                                                   List<LivingEntity> livingInCircle, int covenSize) {
            List<ItemStack> consumed = new ArrayList<>();
            BlockPos firstFocusPos = center;
            boolean first = true;
            for (Supplier<Item> reqSup : focusItems) {
                Item req = reqSup.get();
                for (RitualItemSource src : availableItems) {
                    if (!src.stack().isEmpty() && src.stack().is(req)) {
                        if (first) {
                            firstFocusPos = src.pos();
                            first = false;
                        }
                        consumed.add(src.consumeOne());
                        break;
                    }
                }
            }

            LivingEntity sacrificed = null;
            if (sacrificePredicate != null) {
                for (LivingEntity le : livingInCircle) {
                    if (sacrificePredicate.test(le)) {
                        sacrificed = le;
                        if (consumeSacrifice) {
                            level.sendParticles(ParticleTypes.CRIMSON_SPORE, le.getX(), le.getY() + 0.8, le.getZ(), 35, 0.4, 0.5, 0.4, 0.05);
                            level.sendParticles(ParticleTypes.LARGE_SMOKE, le.getX(), le.getY() + 0.8, le.getZ(), 20, 0.3, 0.4, 0.3, 0.02);
                            le.discard();
                        }
                        break;
                    }
                }
            }

            return new ConsumedRitualContext(level, center, player, rings, consumed, firstFocusPos, sacrificed, covenSize);
        }

        public void executeInitial(ConsumedRitualContext ctx) {
            initialAction.accept(ctx);
        }

        public void executeUpkeepTick(ServerLevel level, BlockPos center, @Nullable Player activator, int tickCount, int covenSize) {
            if (upkeepAction != null) {
                upkeepAction.tick(level, center, activator, tickCount, covenSize);
            }
        }
    }

    @FunctionalInterface
    public interface UpkeepAction {
        void tick(ServerLevel level, BlockPos center, @Nullable Player activator, int tickCount, int covenSize);
    }

    private static List<RitualDefinition> RITUALS;
    private static Map<Integer, RitualDefinition> BY_ID;

    public static List<RitualDefinition> getAllRituals() {
        if (RITUALS == null) {
            initRituals();
        }
        return RITUALS;
    }

    @Nullable
    public static RitualDefinition getById(int id) {
        getAllRituals();
        return BY_ID.get(id);
    }

    @Nullable
    public static RitualDefinition findMatchingRitual(ServerLevel level, BlockPos center, Player player,
                                                      DetectedRings rings, List<RitualItemSource> availableItems,
                                                      List<LivingEntity> livingInCircle, int covenSize) {
        // Ordenar por maior especificidade (mais itens de foco + mais anéis exigidos primeiro)
        List<RitualDefinition> candidates = new ArrayList<>(getAllRituals());
        candidates.sort(Comparator.comparingInt((RitualDefinition r) -> r.focusItems().size() * 10
                + (r.smallRing() != ChalkColor.NONE ? 1 : 0)
                + (r.mediumRing() != ChalkColor.NONE ? 2 : 0)
                + (r.largeRing() != ChalkColor.NONE ? 3 : 0)
                + (r.sacrificePredicate() != null ? 5 : 0)).reversed());

        for (RitualDefinition def : candidates) {
            if (def.matches(level, center, player, rings, availableItems, livingInCircle, covenSize)) {
                return def;
            }
        }
        return null;
    }

    private static void initRituals() {
        List<RitualDefinition> list = new ArrayList<>(96);

        // #01: Ritual de Vinculação (Talismã de Círculo)
        list.add(new RitualDefinition(1, "bind_circle_talisman",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.CIRCLE_TALISMAN::get), null, false,
                CircleRitualRegistry::absorbCirclesIntoTalisman, null));

        // #02: Ritual de Vinculação de Pedra de Rumo (Waystone)
        list.add(new RitualDefinition(2, "bind_waystone",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                500, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.WAYSTONE::get), null, false,
                CircleRitualRegistry::bindWaystoneToCircle, null));

        // #03: Ritual de Carregamento de Pedra Sintonizada
        list.add(new RitualDefinition(3, "charge_attuned_stone",
                ChalkColor.WHITE, ChalkColor.WHITE, ChalkColor.NONE,
                2000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.ATTUNED_STONE::get), null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.CHARGED_ATTUNED_STONE.get())), null));

        // #04: Ritual de Recarga Contínua de Infusão (4000 inicial + 40/s contínuo)
        list.add(new RitualDefinition(4, "infusion_recharge",
                ChalkColor.WHITE, ChalkColor.WHITE, ChalkColor.NONE,
                4000, 40, -1, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.GOLDEN_THREAD::get), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    if (tick % 20 == 0) {
                        for (Player p : level.getEntitiesOfClass(Player.class, new AABB(center).inflate(5.0))) {
                            if (!p.getData(ModAttachmentTypes.INFUSION_TYPE.get()).isEmpty()) {
                                int cur = p.getData(ModAttachmentTypes.INFUSION_POWER.get());
                                int max = p.getData(ModAttachmentTypes.INFUSION_MAX_POWER.get());
                                if (cur < max) {
                                    p.setData(ModAttachmentTypes.INFUSION_POWER.get(), Math.min(max, cur + 40));
                                }
                            }
                        }
                        level.sendParticles(ParticleTypes.ENCHANT, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 12, 2.0, 0.5, 2.0, 0.1);
                    }
                }));

        // #05: Ritual de Teletransporte para Pedra de Rumo
        list.add(new RitualDefinition(5, "teleport_waystone",
                ChalkColor.PURPLE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.BOUND_WAYSTONE::get), null, false,
                CircleRitualRegistry::teleportToWaystone, null));

        // #06: Ritual de Convocação de Criatura / Jogador Vinculado
        list.add(new RitualDefinition(6, "summon_bound_entity",
                ChalkColor.NONE, ChalkColor.PURPLE, ChalkColor.NONE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.TAGLOCK_KIT::get, () -> Items.ENDER_PEARL), null, false,
                ctx -> {
                    LivingEntity target = ctx.findTaglockTarget();
                    if (target != null) {
                        target.teleportTo(ctx.center().getX() + 0.5, ctx.center().getY() + 0.2, ctx.center().getZ() + 0.5);
                        ctx.level().playSound(null, ctx.center(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    ctx.spawnResult(new ItemStack(ModItems.TAGLOCK_KIT.get()));
                }, null));

        // #07: Ritual de Transposição de Minérios (Ferro e Ouro)
        list.add(new RitualDefinition(7, "transpose_ores",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.PURPLE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.ENDER_PEARL, () -> Items.IRON_PICKAXE), null, false,
                ctx -> {
                    int maxOres = 30 + ctx.covenSize() * 10;
                    int extracted = 0;
                    for (int y = ctx.center().getY() - 1; y >= ctx.level().getMinBuildHeight() && extracted < maxOres; y--) {
                        for (int dx = -8; dx <= 8 && extracted < maxOres; dx++) {
                            for (int dz = -8; dz <= 8 && extracted < maxOres; dz++) {
                                BlockPos p = new BlockPos(ctx.center().getX() + dx, y, ctx.center().getZ() + dz);
                                BlockState st = ctx.level().getBlockState(p);
                                if (st.is(BlockTags.IRON_ORES) || st.is(BlockTags.GOLD_ORES) || st.is(BlockTags.COPPER_ORES)) {
                                    ctx.spawnResult(new ItemStack(st.getBlock().asItem()));
                                    ctx.level().setBlock(p, Blocks.STONE.defaultBlockState(), 3);
                                    extracted++;
                                }
                            }
                        }
                    }
                }, null));

        // #08: Ritual de Santidade (Círculo de Repulsão - 500 + 16/s)
        list.add(new RitualDefinition(8, "sanctity_repel",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                500, 16, -1, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.FEATHER), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    Vec3 c = Vec3.atCenterOf(center);
                    for (Monster m : level.getEntitiesOfClass(Monster.class, new AABB(center).inflate(4.5))) {
                        Vec3 diff = m.position().subtract(c);
                        Vec3 dir = diff.lengthSqr() < 0.01 ? new Vec3(0.8, 0.3, 0.0) : diff.normalize().scale(0.8);
                        m.setDeltaMovement(dir.x, 0.25, dir.z);
                        m.hurtMarked = true;
                    }
                }));

        // #09: Ritual de Aprisionamento (Círculo de Atração - 500 + 16/s)
        list.add(new RitualDefinition(9, "imprisonment_cage",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                500, 16, -1, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.SLIME_BALL), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    Vec3 c = Vec3.atCenterOf(center);
                    for (Monster m : level.getEntitiesOfClass(Monster.class, new AABB(center).inflate(5.0))) {
                        double dist = m.position().distanceTo(c);
                        if (dist > 2.8) {
                            Vec3 pull = c.subtract(m.position()).normalize().scale(0.6);
                            m.setDeltaMovement(pull.x, 0.1, pull.z);
                            m.hurtMarked = true;
                        }
                    }
                }));

        // #10: Ritual de Barreira de Proteção Pequena (500 + 24/s)
        list.add(new RitualDefinition(10, "small_protection_dome",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                500, 24, -1, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.OBSIDIAN), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> maintainProtectionDome(level, center, 4.0, 5.0, false, activator)));

        // #11: Ritual de Barreira Impenetrável Total (1000 + 28/s)
        list.add(new RitualDefinition(11, "total_impenetrable_dome",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                1000, 28, -1, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.OBSIDIAN, ModItems.TEAR_OF_THE_GODDESS::get), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> maintainProtectionDome(level, center, 6.0, 6.0, true, activator)));

        // #12: Ritual de Barreira Portátil Temporária (1500, 60s fixos)
        list.add(new RitualDefinition(12, "portable_temporary_dome",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                1500, 0, 1200, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.OBSIDIAN, () -> Items.REDSTONE), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> maintainProtectionDome(level, center, 6.0, 6.0, true, activator)));

        // #13: Ritual da Fúria da Terra (Erupção Vulcânica)
        list.add(new RitualDefinition(13, "earth_wrath_volcano",
                ChalkColor.RED, ChalkColor.NONE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.SMOOTH_STONE, () -> Items.LAVA_BUCKET), null, false,
                ctx -> generateVolcanoCone(ctx.level(), ctx.center(), 8, 8), null));

        // #14: Ritual da Tempestade Concentrada Interna (1000/s)
        list.add(new RitualDefinition(14, "focused_storm_inner",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 1000, 200, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.IRON_AXE), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    if (tick % 20 == 0) {
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                        if (bolt != null) {
                            double ox = (level.random.nextDouble() - 0.5) * 4.0;
                            double oz = (level.random.nextDouble() - 0.5) * 4.0;
                            bolt.moveTo(center.getX() + 0.5 + ox, center.getY(), center.getZ() + 0.5 + oz);
                            level.addFreshEntity(bolt);
                        }
                    }
                }));

        // #15: Ritual da Tempestade Concentrada Externa (2000/s)
        list.add(new RitualDefinition(15, "focused_storm_outer",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                2000, 2000, 200, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.DIAMOND_AXE), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> strikeOuterRingLightning(level, center, tick)));

        // #16: Ritual da Tempestade Portátil (2500 inicial, 45s fixos)
        list.add(new RitualDefinition(16, "portable_storm",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                2500, 0, 900, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.DIAMOND_AXE, () -> Items.REDSTONE), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> strikeOuterRingLightning(level, center, tick)));

        // #17: Ritual do Eclipse Solar Total
        list.add(new RitualDefinition(17, "total_solar_eclipse",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                3000, 0, 0, 0, TimeWeatherCondition.DAY_ONLY,
                List.of(() -> Items.GOLD_INGOT, () -> Items.GLOWSTONE_DUST), null, false,
                ctx -> ctx.level().setDayTime(18000L), null));

        // #18: Ritual do Eclipse Portátil
        list.add(new RitualDefinition(18, "portable_eclipse",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                4000, 0, 0, 0, TimeWeatherCondition.DAY_ONLY,
                List.of(() -> Items.DIAMOND_PICKAXE), null, false,
                ctx -> ctx.level().setDayTime(18000L), null));

        // #19: Ritual da Fenda Telúrica (Rachar a Terra)
        list.add(new RitualDefinition(19, "part_earth_fissure",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                4000, 0, 0, 2, TimeWeatherCondition.NONE,
                List.of(() -> Items.IRON_SHOVEL, () -> Items.GUNPOWDER), null, false,
                ctx -> {
                    BlockPos delta = ctx.firstFocusPos().subtract(ctx.center());
                    Direction dir = Direction.getNearest(delta.getX(), 0, delta.getZ());
                    if (dir.getAxis().isVertical()) dir = Direction.NORTH;
                    Direction perp = dir.getClockWise();
                    for (int i = 4; i <= 64; i++) {
                        BlockPos spine = ctx.center().relative(dir, i);
                        for (int w = -2; w <= 2; w++) {
                            for (int d = 0; d <= 10; d++) {
                                BlockPos dig = spine.relative(perp, w).below(d);
                                if (!ctx.level().getBlockState(dig).is(Blocks.BEDROCK)) {
                                    ctx.level().setBlock(dig, Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                }, null));

        // #20: Ritual de Erguer Coluna de Rocha Pequena (raio 4, altura 8)
        list.add(new RitualDefinition(20, "raise_small_column",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.SMOOTH_STONE, () -> Items.DIRT), null, false,
                ctx -> raiseStoneColumn(ctx.level(), ctx.center(), 4, 8), null));

        // #21: Ritual de Banimento Demoníaco Portátil (raio 9)
        list.add(new RitualDefinition(21, "banish_demon_portable",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                2000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.ROTTEN_FLESH, () -> Items.BLAZE_POWDER), null, false,
                ctx -> banishNetherCreatures(ctx.level(), ctx.center(), 9), null));

        // #22: Ritual de Banimento Demoníaco Maior (raio 16)
        list.add(new RitualDefinition(22, "banish_demon_greater",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.ROTTEN_FLESH, () -> Items.GHAST_TEAR), null, false,
                ctx -> banishNetherCreatures(ctx.level(), ctx.center(), 16), null));

        // #23: Ritual de Invocação de Demônio (Econômico - Sacrifício de Aldeão)
        list.add(new RitualDefinition(23, "summon_demon_sacrifice",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.RED,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.DEMON_HEART::get, () -> Items.NETHERRACK),
                le -> le instanceof Villager, true,
                ctx -> summonSuperiorDemon(ctx.level(), ctx.center()), null));

        // #24: Ritual de Invocação de Demônio (Sem Sacrifício Vivo)
        list.add(new RitualDefinition(24, "summon_demon_bloodless",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.RED,
                8000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.REFINED_EVIL::get, ModItems.DEMON_HEART::get, () -> Items.BLAZE_ROD, () -> Items.BLAZE_ROD),
                null, false,
                ctx -> summonSuperiorDemon(ctx.level(), ctx.center()), null));

        // #25: Ritual de Invocação do Wither (Econômico - Sacrifício de Aldeão)
        list.add(new RitualDefinition(25, "summon_wither_sacrifice",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                8000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.WITHER_SKELETON_SKULL, () -> Items.SOUL_SAND),
                le -> le instanceof Villager, true,
                ctx -> summonWitherBoss(ctx.level(), ctx.center()), null));

        // #26: Ritual de Invocação do Wither (Sem Sacrifício Vivo)
        list.add(new RitualDefinition(26, "summon_wither_bloodless",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                12000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.WITHER_SKELETON_SKULL, () -> Items.WITHER_SKELETON_SKULL, () -> Items.WITHER_SKELETON_SKULL,
                        () -> Items.SOUL_SAND, () -> Items.SOUL_SAND, () -> Items.SOUL_SAND, () -> Items.SOUL_SAND,
                        ModItems.TEAR_OF_THE_GODDESS::get),
                null, false,
                ctx -> summonWitherBoss(ctx.level(), ctx.center()), null));

        // #27: Ritual de Infusão da Luz (Infusion of Light)
        list.add(new RitualDefinition(27, "infusion_of_light",
                ChalkColor.WHITE, ChalkColor.WHITE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.GLOWSTONE_DUST, () -> Items.GLISTERING_MELON_SLICE),
                null, false,
                ctx -> applyPlayerInfusion(ctx.activator(), "light", 200), null));

        // #28: Ritual de Infusão da Terra (Infusion of the Overworld)
        list.add(new RitualDefinition(28, "infusion_of_overworld",
                ChalkColor.WHITE, ChalkColor.WHITE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.ENT_BRANCH::get, ModItems.MANDRAKE_ROOT::get, () -> Items.CLAY_BALL),
                null, false,
                ctx -> applyPlayerInfusion(ctx.activator(), "overworld", 250), null));

        // #29: Ritual de Infusão do Fim (Infusion of the Ender / Otherwhere)
        list.add(new RitualDefinition(29, "infusion_of_otherwhere",
                ChalkColor.WHITE, ChalkColor.PURPLE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.ENDER_PEARL, () -> Items.ENDER_EYE, ModItems.ENDER_DEW::get),
                null, false,
                ctx -> applyPlayerInfusion(ctx.activator(), "otherwhere", 300), null));

        // #30: Ritual de Infusão Infernal / da Besta (Infusion of the Nether)
        list.add(new RitualDefinition(30, "infusion_of_nether",
                ChalkColor.WHITE, ChalkColor.RED, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.BLAZE_POWDER, ModItems.DEMONIC_BLOOD::get, () -> Items.MAGMA_CREAM),
                null, false,
                ctx -> applyPlayerInfusion(ctx.activator(), "nether", 300), null));

        // #31: Ritual de Encantamento da Vassoura Voadora
        list.add(new RitualDefinition(31, "enchant_broom",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                5000, 0, 0, 0, TimeWeatherCondition.NIGHT_ONLY,
                List.of(ModItems.BROOM::get, ModItems.BREATH_OF_THE_GODDESS::get), null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.ENCHANTED_BROOM.get())), null));

        // #32: Ritual de Criação da Pedra Necromântica
        list.add(new RitualDefinition(32, "create_necro_stone",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.RED,
                8000, 0, 0, 0, TimeWeatherCondition.NIGHT_ONLY,
                List.of(ModItems.CHARGED_ATTUNED_STONE::get, () -> Items.BONE, () -> Items.WITHER_SKELETON_SKULL),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.NECRO_STONE.get())), null));

        // #33: Ritual de Invocação de Familiar Perdido
        list.add(new RitualDefinition(33, "summon_lost_familiar",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                2000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.GOLDEN_THREAD::get), null, false,
                ctx -> FamiliarSystem.summonOrReviveFamiliar(ctx.level(), ctx.activator(), ctx.center()), null));

        // #34: Ritual de Duplicação de Pedra de Rumo Vinculada
        list.add(new RitualDefinition(34, "duplicate_waystone",
                ChalkColor.PURPLE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.BOUND_WAYSTONE::get, ModItems.WAYSTONE::get), null, false,
                ctx -> {
                    GlobalPos gp = ctx.findBoundCoordinates();
                    for (int i = 0; i < 2; i++) {
                        ItemStack out = new ItemStack(ModItems.BOUND_WAYSTONE.get());
                        if (gp != null) out.set(ModDataComponentTypes.BOUND_COORDINATES.get(), gp);
                        ctx.spawnResult(out);
                    }
                }, null));

        // #35: Ritual da Fertilidade da Terra (Econômico - Sacrifício de Vaca/Ovelha)
        list.add(new RitualDefinition(35, "fertility_sacrifice",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.BONE_MEAL, () -> Items.WHEAT_SEEDS, () -> Items.GLISTERING_MELON_SLICE),
                le -> le instanceof Cow || le instanceof Sheep, true,
                ctx -> applyFertilityWave(ctx.level(), ctx.center(), ctx.activator(), 50), null));

        // #36: Ritual da Fertilidade da Terra (Sem Sacrifício Vivo)
        list.add(new RitualDefinition(36, "fertility_bloodless",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.BONE_MEAL, () -> Items.BONE_MEAL, () -> Items.BONE_MEAL, () -> Items.BONE_MEAL,
                        () -> Items.GOLDEN_APPLE, () -> Items.PUMPKIN_SEEDS),
                null, false,
                ctx -> applyFertilityWave(ctx.level(), ctx.center(), ctx.activator(), 50), null));

        // #37: Ritual da Praga Devastadora (Blight)
        list.add(new RitualDefinition(37, "blight_plague",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.RED,
                8000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.POISONOUS_POTATO, () -> Items.FERMENTED_SPIDER_EYE, () -> Items.ROTTEN_FLESH),
                null, false,
                ctx -> applyBlightWave(ctx.level(), ctx.center(), 40), null));

        // #38: Ritual da Cegueira das Trevas
        list.add(new RitualDefinition(38, "darkness_blindness",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.INK_SAC, () -> Items.FERMENTED_SPIDER_EYE), null, false,
                ctx -> {
                    for (LivingEntity e : ctx.level().getEntitiesOfClass(LivingEntity.class, new AABB(ctx.center()).inflate(80.0))) {
                        if (e != ctx.activator()) {
                            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1200, 1));
                            e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 1200, 1));
                        }
                    }
                }, null));

        // #39: Ritual do Inferno na Terra (Hell on Earth - 10000 + 200/s)
        list.add(new RitualDefinition(39, "hell_on_earth",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.RED,
                10000, 200, -1, 0, TimeWeatherCondition.OVERWORLD_NIGHT_ONLY,
                List.of(() -> Items.NETHERRACK, () -> Items.BLAZE_POWDER, () -> Items.GHAST_TEAR),
                le -> le instanceof Villager, true,
                ctx -> transformAreaToNether(ctx.level(), ctx.center(), 20),
                (level, center, activator, tick, coven) -> {
                    if (tick % 100 == 0) {
                        transformAreaToNether(level, center, 20);
                        Blaze blaze = EntityType.BLAZE.create(level);
                        if (blaze != null) {
                            blaze.moveTo(center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5, 0, 0);
                            level.addFreshEntity(blaze);
                        }
                    }
                }));

        // #40: Ritual de Convocação de Bruxa Selvagem
        list.add(new RitualDefinition(40, "summon_wild_witch",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.NETHER_WART, () -> Items.SPIDER_EYE, () -> Items.POTION), null, false,
                ctx -> {
                    Witch witch = EntityType.WITCH.create(ctx.level());
                    if (witch != null) {
                        witch.moveTo(ctx.center().getX() + 0.5, ctx.center().getY() + 0.2, ctx.center().getZ() + 0.5, 0, 0);
                        ctx.level().addFreshEntity(witch);
                    }
                    int coven = ctx.activator().getData(ModAttachmentTypes.COVEN_LEVEL.get());
                    if (coven < 6) {
                        ctx.activator().setData(ModAttachmentTypes.COVEN_LEVEL.get(), coven + 1);
                    }
                }, null));

        // #41: Ritual de Vinculação Rápida de Pedra de Rumo
        list.add(new RitualDefinition(41, "fast_bind_waystone",
                ChalkColor.PURPLE, ChalkColor.NONE, ChalkColor.NONE,
                500, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.WAYSTONE::get), null, false,
                CircleRitualRegistry::bindWaystoneToCircle, null));

        // #42: Ritual de Criação de Par de Pedras de Rumo Vinculadas
        list.add(new RitualDefinition(42, "bind_waystone_pair",
                ChalkColor.NONE, ChalkColor.PURPLE, ChalkColor.NONE,
                1500, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.WAYSTONE::get, ModItems.WAYSTONE::get, () -> Items.ENDER_PEARL), null, false,
                ctx -> {
                    GlobalPos gp = GlobalPos.of(ctx.level().dimension(), ctx.center());
                    for (int i = 0; i < 2; i++) {
                        ItemStack s = new ItemStack(ModItems.BOUND_WAYSTONE.get());
                        s.set(ModDataComponentTypes.BOUND_COORDINATES.get(), gp);
                        ctx.spawnResult(s);
                    }
                }, null));

        // #43: Ritual do Poder da Natureza (Aceleração Botânica - 4000 + 150/s)
        list.add(new RitualDefinition(43, "nature_power_growth",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                4000, 150, -1, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.ENT_BRANCH::get, ModItems.MANDRAKE_ROOT::get, () -> Items.BONE_MEAL), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    if (tick % 20 == 0) {
                        for (int i = 0; i < 25; i++) {
                            BlockPos p = center.offset(level.random.nextInt(29) - 14, level.random.nextInt(9) - 4, level.random.nextInt(29) - 14);
                            BlockState st = level.getBlockState(p);
                            if (st.getBlock() instanceof BonemealableBlock bm && bm.isValidBonemealTarget(level, p, st)) {
                                bm.performBonemeal(level, level.random, p, st);
                            }
                        }
                    }
                }));

        // #44: Ritual da Reencarnação Anterior (Recuperar Pertences da Morte)
        list.add(new RitualDefinition(44, "prior_incarnation",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.TEAR_OF_THE_GODDESS::get, ModItems.GOLDEN_THREAD::get, () -> Items.BONE), null, false,
                ctx -> {
                    Optional<GlobalPos> lastDeath = ctx.activator().getLastDeathLocation();
                    if (lastDeath.isPresent() && lastDeath.get().dimension() == ctx.level().dimension()) {
                        BlockPos dp = lastDeath.get().pos();
                        for (ItemEntity ie : ctx.level().getEntitiesOfClass(ItemEntity.class, new AABB(dp).inflate(16.0))) {
                            ie.teleportTo(ctx.center().getX() + 0.5, ctx.center().getY() + 0.5, ctx.center().getZ() + 0.5);
                        }
                    }
                }, null));

        // #45: Ritual de Captura de Matriz Ritualística
        list.add(new RitualDefinition(45, "capture_ritual_matrix",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.CIRCLE_TALISMAN::get, () -> Items.REDSTONE), null, false,
                CircleRitualRegistry::absorbCirclesIntoTalisman, null));

        // #46: Ritual de Erguer Coluna Média (raio 6, altura 8)
        list.add(new RitualDefinition(46, "raise_medium_column",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                2000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.SMOOTH_STONE, () -> Items.IRON_INGOT), null, false,
                ctx -> raiseStoneColumn(ctx.level(), ctx.center(), 6, 8), null));

        // #47: Ritual de Erguer Coluna Grande (raio 9, altura 8)
        list.add(new RitualDefinition(47, "raise_large_column",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.SMOOTH_STONE, () -> Items.GOLD_INGOT), null, false,
                ctx -> raiseStoneColumn(ctx.level(), ctx.center(), 9, 8), null));

        // #48: Maldição do Infortúnio Tempestuosa
        list.add(new RitualDefinition(48, "curse_misfortune_storm",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                4000, 0, 0, 3, TimeWeatherCondition.THUNDER_ONLY,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.REEK_OF_MISFORTUNE::get, () -> Items.FERMENTED_SPIDER_EYE),
                null, false,
                ctx -> applyCurseToTaglock(ctx, MobEffects.UNLUCK, 24000, 1), null));

        // #49: Maldição do Infortúnio Padrão
        list.add(new RitualDefinition(49, "curse_misfortune_standard",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                6000, 0, 0, 3, TimeWeatherCondition.NONE,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.REEK_OF_MISFORTUNE::get, ModItems.REFINED_EVIL::get),
                null, false,
                ctx -> applyCurseToTaglock(ctx, MobEffects.UNLUCK, 24000, 1), null));

        // #50: Ritual de Criação do Ramo Místico (Mystic Branch)
        list.add(new RitualDefinition(50, "create_mystic_branch",
                ChalkColor.WHITE, ChalkColor.WHITE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NIGHT_ONLY,
                List.of(ModItems.ENT_BRANCH::get, ModItems.GOLDEN_HEART::get, ModItems.TEAR_OF_THE_GODDESS::get),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.MYSTIC_BRANCH.get())), null));

        // #51: Ritual do Cozimento Rápido de Alimentos (500 + 5/s)
        list.add(new RitualDefinition(51, "fast_cooking_single",
                ChalkColor.RED, ChalkColor.NONE, ChalkColor.NONE,
                500, 5, -1, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.BLAZE_POWDER, () -> Items.COAL), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    if (tick % 20 == 0) smeltItemsInCircle(level, center, 1);
                }));

        // #52: Maldição da Insanidade Tempestuosa
        list.add(new RitualDefinition(52, "curse_insanity_storm",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                4000, 0, 0, 3, TimeWeatherCondition.THUNDER_ONLY,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.DISTURBED_COTTON::get, () -> Items.FERMENTED_SPIDER_EYE),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.INSANITY, 24000, 0), null));

        // #53: Maldição da Insanidade Padrão
        list.add(new RitualDefinition(53, "curse_insanity_standard",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                6000, 0, 0, 3, TimeWeatherCondition.NONE,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.DISTURBED_COTTON::get, ModItems.REFINED_EVIL::get),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.INSANITY, 24000, 0), null));

        // #54: Ritual de Vinculação de Familiar (Bind Familiar)
        list.add(new RitualDefinition(54, "bind_familiar",
                ChalkColor.WHITE, ChalkColor.WHITE, ChalkColor.NONE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.GOLDEN_THREAD::get, () -> Items.DIAMOND),
                FamiliarSystem::isValidFamiliarCandidate, false,
                ctx -> {
                    if (ctx.sacrificedEntity() != null) {
                        FamiliarSystem.bindFamiliar(ctx.level(), ctx.activator(), ctx.sacrificedEntity());
                    }
                }, null));

        // #55: Ritual de Convocação do Familiar
        list.add(new RitualDefinition(55, "call_familiar",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.GOLDEN_THREAD::get, () -> Items.FEATHER), null, false,
                ctx -> FamiliarSystem.summonOrReviveFamiliar(ctx.level(), ctx.activator(), ctx.center()), null));

        // #56: Maldição da Degradação de Poppets (Curse Poppets)
        list.add(new RitualDefinition(56, "curse_degrade_poppets",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                5000, 0, 0, 3, TimeWeatherCondition.NONE,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.ICY_NEEDLE::get, ModItems.FOUL_FUME::get),
                null, false,
                ctx -> {
                    LivingEntity target = ctx.findTaglockTarget();
                    if (target instanceof Player victim) {
                        PoppetHandler.degradeTargetPoppets(ctx.level().getServer(), victim, 450);
                    }
                    ctx.spawnResult(new ItemStack(ModItems.TAGLOCK_KIT.get()));
                }, null));

        // #57: Ritual de Criação da Bola de Cristal
        list.add(new RitualDefinition(57, "create_crystal_ball",
                ChalkColor.WHITE, ChalkColor.WHITE, ChalkColor.NONE,
                5000, 0, 0, 0, TimeWeatherCondition.NIGHT_ONLY,
                List.of(() -> Items.QUARTZ_BLOCK, ModItems.TEAR_OF_THE_GODDESS::get, () -> Items.GLASS),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.CRYSTAL_BALL.get())), null));

        // #58: Ritual de Cozimento Rápido Duplo (500 + 5/s)
        list.add(new RitualDefinition(58, "fast_cooking_double",
                ChalkColor.RED, ChalkColor.NONE, ChalkColor.NONE,
                500, 5, -1, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.BLAZE_POWDER, () -> Items.BLAZE_POWDER, () -> Items.COAL_BLOCK), null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    if (tick % 10 == 0) smeltItemsInCircle(level, center, 2);
                }));

        // #59: Maldição do Afundamento Tempestuosa
        list.add(new RitualDefinition(59, "curse_sinking_storm",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                4000, 0, 0, 3, TimeWeatherCondition.THUNDER_ONLY,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.DISTURBED_COTTON::get, () -> Items.DIRT),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.SINKING, 24000, 0), null));

        // #60: Maldição do Afundamento Padrão
        list.add(new RitualDefinition(60, "curse_sinking_standard",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                6000, 0, 0, 3, TimeWeatherCondition.NONE,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.DISTURBED_COTTON::get, ModItems.REFINED_EVIL::get, () -> Items.DIRT),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.SINKING, 24000, 0), null));

        // #61: Ritual de Criação da Pedra de Vidência (Seer Stone)
        list.add(new RitualDefinition(61, "create_seer_stone",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                3000, 0, 0, 0, TimeWeatherCondition.NIGHT_ONLY,
                List.of(ModItems.CHARGED_ATTUNED_STONE::get, ModItems.TEAR_OF_THE_GODDESS::get),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.SEER_STONE.get())), null));

        // #62: Maldição do Superaquecimento Tempestuosa
        list.add(new RitualDefinition(62, "curse_overheating_storm",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                4000, 0, 0, 3, TimeWeatherCondition.THUNDER_ONLY,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.EMBER_MOSS_ITEM::get, () -> Items.FERMENTED_SPIDER_EYE),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.OVERHEATING, 24000, 0), null));

        // #63: Maldição do Superaquecimento Padrão
        list.add(new RitualDefinition(63, "curse_overheating_standard",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                6000, 0, 0, 3, TimeWeatherCondition.NONE,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.EMBER_MOSS_ITEM::get, ModItems.REFINED_EVIL::get),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.OVERHEATING, 24000, 0), null));

        // #64: Ritual da Mudança Climática de Bioma
        list.add(new RitualDefinition(64, "biome_climate_shift",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                8000, 0, 0, 4, TimeWeatherCondition.NONE,
                List.of(() -> Items.PAPER, ModItems.TEAR_OF_THE_GODDESS::get, ModItems.ENT_BRANCH::get),
                null, false,
                ctx -> {
                    for (BlockPos p : BlockPos.betweenClosed(ctx.center().offset(-16, -2, -16), ctx.center().offset(16, 2, 16))) {
                        if (ctx.level().getBlockState(p).is(Blocks.SAND)) {
                            ctx.level().setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                        } else if (ctx.level().getBlockState(p).is(Blocks.SNOW)) {
                            ctx.level().setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }, null));

        // #65: Ritual da Esfera de Gelo Perpétuo
        list.add(new RitualDefinition(65, "perpetual_ice_sphere",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                5000, 0, 0, 2, TimeWeatherCondition.NONE,
                List.of(ModItems.FROZEN_HEART::get, ModItems.ICY_NEEDLE::get, () -> Items.ICE),
                null, false,
                ctx -> {
                    int r = 8;
                    for (int dx = -r; dx <= r; dx++) {
                        for (int dy = -r; dy <= r; dy++) {
                            for (int dz = -r; dz <= r; dz++) {
                                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                                if (dist >= r - 1.0 && dist <= r + 0.4) {
                                    BlockPos p = ctx.center().offset(dx, dy, dz);
                                    if (ctx.level().getBlockState(p).isAir() || ctx.level().getBlockState(p).is(Blocks.WATER)) {
                                        ctx.level().setBlock(p, Blocks.BLUE_ICE.defaultBlockState(), 3);
                                    }
                                }
                            }
                        }
                    }
                }, null));

        // #66: Ritual da Chuva de Sapos (Rain of Toads)
        list.add(new RitualDefinition(66, "rain_of_toads",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                3000, 0, 200, 1, TimeWeatherCondition.NONE,
                List.of(ModItems.TOE_OF_FROG::get, () -> Items.SPIDER_EYE, () -> Items.CLAY_BALL),
                null, false,
                ctx -> {},
                (level, center, activator, tick, coven) -> {
                    if (tick % 10 == 0) {
                        var toad = ModEntityTypes.TOAD_FAMILIAR.get().create(level);
                        if (toad != null) {
                            double ox = (level.random.nextDouble() - 0.5) * 24.0;
                            double oz = (level.random.nextDouble() - 0.5) * 24.0;
                            toad.moveTo(center.getX() + 0.5 + ox, center.getY() + 12.0, center.getZ() + 0.5 + oz, 0, 0);
                            level.addFreshEntity(toad);
                        }
                        for (Monster m : level.getEntitiesOfClass(Monster.class, new AABB(center).inflate(16.0))) {
                            m.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
                        }
                    }
                }));

        // #67: Ritual de Transmutação de Glifos (Glyphic Transformation)
        list.add(new RitualDefinition(67, "glyphic_transformation",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                500, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.OTHERWHERE_CHALK::get), null, false,
                ctx -> CircleGeometryValidator.stampRing(ctx.level(), ctx.center(), CircleGeometryValidator.SMALL_RING_OFFSETS, ChalkColor.PURPLE),
                null));

        // #68: Ritual de Convocação de Criaturas do Campo
        list.add(new RitualDefinition(68, "call_of_the_wild",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                2000, 0, 0, 3, TimeWeatherCondition.NONE,
                List.of(() -> Items.WHEAT, () -> Items.WHEAT_SEEDS, () -> Items.CARROT),
                null, false,
                ctx -> {
                    for (Animal a : ctx.level().getEntitiesOfClass(Animal.class, new AABB(ctx.center()).inflate(64.0))) {
                        a.getNavigation().moveTo(ctx.center().getX() + 0.5, ctx.center().getY(), ctx.center().getZ() + 0.5, 1.35D);
                    }
                }, null));

        // #69: Ritual de Fixação de Duração Mística (Manifest Duration)
        list.add(new RitualDefinition(69, "manifest_duration",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                2000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.REDSTONE, ModItems.TEAR_OF_THE_GODDESS::get),
                null, false,
                ctx -> {
                    for (MobEffectInstance inst : new ArrayList<>(ctx.activator().getActiveEffects())) {
                        ctx.activator().addEffect(new MobEffectInstance(inst.getEffect(), inst.getDuration() + 150, inst.getAmplifier()));
                    }
                }, null));

        // #70: Ritual de Florestamento de Carvalho
        list.add(new RitualDefinition(70, "forest_oak",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.OAK_SAPLING, () -> Items.BONE_MEAL), null, false,
                ctx -> growForestAround(ctx.level(), ctx.center(), Blocks.OAK_LOG, Blocks.OAK_LEAVES, 20), null));

        // #71: Ritual de Florestamento de Pinheiro (Spruce)
        list.add(new RitualDefinition(71, "forest_spruce",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.SPRUCE_SAPLING, () -> Items.BONE_MEAL), null, false,
                ctx -> growForestAround(ctx.level(), ctx.center(), Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES, 20), null));

        // #72: Ritual de Florestamento de Bétula (Birch)
        list.add(new RitualDefinition(72, "forest_birch",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.BIRCH_SAPLING, () -> Items.BONE_MEAL), null, false,
                ctx -> growForestAround(ctx.level(), ctx.center(), Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 20), null));

        // #73: Ritual de Florestamento de Selva (Jungle)
        list.add(new RitualDefinition(73, "forest_jungle",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                3500, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.JUNGLE_SAPLING, () -> Items.BONE_MEAL), null, false,
                ctx -> growForestAround(ctx.level(), ctx.center(), Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES, 20), null));

        // #74: Ritual de Florestamento de Rowan
        list.add(new RitualDefinition(74, "forest_rowan",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.ROWAN_SAPLING::get, () -> Items.BONE_MEAL, ModItems.ROWAN_BERRIES::get), null, false,
                ctx -> growForestAround(ctx.level(), ctx.center(), ModBlocks.ROWAN_LOG.get(), ModBlocks.ROWAN_LEAVES.get(), 20), null));

        // #75: Ritual de Florestamento de Amieiro (Alder)
        list.add(new RitualDefinition(75, "forest_alder",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.ALDER_SAPLING::get, () -> Items.BONE_MEAL), null, false,
                ctx -> growForestAround(ctx.level(), ctx.center(), ModBlocks.ALDER_LOG.get(), ModBlocks.ALDER_LEAVES.get(), 20), null));

        // #76: Ritual de Florestamento de Espinheiro (Hawthorn)
        list.add(new RitualDefinition(76, "forest_hawthorn",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.HAWTHORN_SAPLING::get, () -> Items.BONE_MEAL), null, false,
                ctx -> growForestAround(ctx.level(), ctx.center(), ModBlocks.HAWTHORN_LOG.get(), ModBlocks.HAWTHORN_LEAVES.get(), 20), null));

        // #77: Ritual da Erupção Vulcânica Maior
        list.add(new RitualDefinition(77, "greater_volcano_eruption",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                8000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.LAVA_BUCKET, () -> Items.MAGMA_BLOCK, () -> Items.BLAZE_POWDER), null, false,
                ctx -> generateVolcanoCone(ctx.level(), ctx.center(), 10, 10), null));

        // #78: Maldição do Pesadelo Acordado Tempestuosa
        list.add(new RitualDefinition(78, "curse_waking_nightmare_storm",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.RED,
                6000, 0, 0, 3, TimeWeatherCondition.THUNDER_ONLY,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.MELLIFLUOUS_HUNGER::get, () -> Items.FERMENTED_SPIDER_EYE),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.WAKING_NIGHTMARE, 24000, 0), null));

        // #79: Maldição do Pesadelo Acordado Padrão
        list.add(new RitualDefinition(79, "curse_waking_nightmare_standard",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.RED,
                8000, 0, 0, 3, TimeWeatherCondition.NONE,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.MELLIFLUOUS_HUNGER::get, ModItems.REFINED_EVIL::get),
                null, false,
                ctx -> applyCurseToTaglock(ctx, ModMobEffects.WAKING_NIGHTMARE, 24000, 0), null));

        // #80: Ritual de Criação da Poção do Voo Místico (Brew of Soaring)
        list.add(new RitualDefinition(80, "create_brew_of_soaring",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.WOOL_OF_BAT::get, () -> Items.FEATHER, ModItems.BREATH_OF_THE_GODDESS::get),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.BREW_OF_SOARING.get())), null));

        // #81: Ritual de Criação da Poção da Cova (Brew of the Grave)
        list.add(new RitualDefinition(81, "create_brew_of_the_grave",
                ChalkColor.WHITE, ChalkColor.NONE, ChalkColor.NONE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.TORMENTED_TWINE::get, () -> Items.DIRT, () -> Items.BONE),
                null, false,
                ctx -> {
                    ctx.spawnResult(new ItemStack(ModItems.BREW_OF_THE_GRAVE.get()));
                    applyPlayerInfusion(ctx.activator(), "grave", 200);
                }, null));

        // #82: Ritual de Forja da Pedra Espectral (Spectral Stone)
        list.add(new RitualDefinition(82, "forge_spectral_stone",
                ChalkColor.NONE, ChalkColor.PURPLE, ChalkColor.NONE,
                5000, 0, 0, 0, TimeWeatherCondition.NIGHT_ONLY,
                List.of(ModItems.CHARGED_ATTUNED_STONE::get, ModItems.SPECTRAL_DUST::get, ModItems.TEAR_OF_THE_GODDESS::get),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.SPECTRAL_STONE.get())), null));

        // #83: Ritual de Aprisionamento de Fantasmas na Pedra Espectral
        list.add(new RitualDefinition(83, "bind_ghost_to_spectral_stone",
                ChalkColor.PURPLE, ChalkColor.NONE, ChalkColor.NONE,
                2000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.SPECTRAL_STONE::get),
                le -> le instanceof Monster, true,
                ctx -> {
                    ItemStack filled = new ItemStack(ModItems.SPECTRAL_STONE.get());
                    filled.set(ModDataComponentTypes.CAPTURED_CRITTER.get(), "spectre:3,banshee:2,poltergeist:2");
                    ctx.spawnResult(filled);
                }, null));

        // #84: Ritual de Vinculação de Espíritos ao Espantalho Protetor (Fetish Scarecrow)
        list.add(new RitualDefinition(84, "bind_fetish_scarecrow",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.SCARECROW_FETISH::get, ModItems.SPECTRAL_STONE::get),
                null, false,
                ctx -> bindSpiritToFetishItemOrBlock(ctx, ModItems.SCARECROW_FETISH.get(), FetishBlockEntity.SpiritEffect.SENTINEL), null));

        // #85: Ritual de Vinculação de Espíritos à Efígie de Altar (Trent Effigy)
        list.add(new RitualDefinition(85, "bind_fetish_trent_effigy",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.TRENT_EFFIGY::get, ModItems.SPECTRAL_STONE::get),
                null, false,
                ctx -> bindSpiritToFetishItemOrBlock(ctx, ModItems.TRENT_EFFIGY.get(), FetishBlockEntity.SpiritEffect.POPPET_ENHANCEMENT), null));

        // #86: Ritual de Vinculação de Espíritos ao Totem de Corrupção (Witch's Ladder)
        list.add(new RitualDefinition(86, "bind_fetish_witchs_ladder",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                5000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.WITCHS_LADDER::get, ModItems.SPECTRAL_STONE::get),
                null, false,
                ctx -> bindSpiritToFetishItemOrBlock(ctx, ModItems.WITCHS_LADDER.get(), FetishBlockEntity.SpiritEffect.SCREAMER), null));

        // #87: Ritual de Invocação de Diabrete / Goblin (Imp)
        list.add(new RitualDefinition(87, "summon_imp",
                ChalkColor.RED, ChalkColor.NONE, ChalkColor.NONE,
                4000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.TORMENTED_TWINE::get, () -> Items.GOLD_NUGGET, ModItems.ODD_PORKCHOP_RAW::get),
                null, false,
                ctx -> {
                    ZombieVillager imp = EntityType.ZOMBIE_VILLAGER.create(ctx.level());
                    if (imp != null) {
                        imp.setBaby(true);
                        imp.setCustomName(Component.translatable("entity.occultum.imp"));
                        imp.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0));
                        imp.moveTo(ctx.center().getX() + 0.5, ctx.center().getY() + 0.2, ctx.center().getZ() + 0.5, 0, 0);
                        ctx.level().addFreshEntity(imp);
                    }
                }, null));

        // #88: Ritual de Vinculação de Pedra de Rumo a Jogador (Player Waystone)
        list.add(new RitualDefinition(88, "bind_player_waystone",
                ChalkColor.NONE, ChalkColor.PURPLE, ChalkColor.NONE,
                3000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.WAYSTONE::get, ModItems.TAGLOCK_KIT::get),
                null, false,
                ctx -> {
                    String sig = ctx.findTaglockUuidString();
                    LivingEntity target = ctx.findTaglockTarget();
                    ItemStack out = new ItemStack(ModItems.BOUND_WAYSTONE.get());
                    if (sig != null) out.set(ModDataComponentTypes.DNA_SIGNATURE.get(), sig);
                    if (target != null) {
                        out.set(ModDataComponentTypes.BOUND_COORDINATES.get(), GlobalPos.of(target.level().dimension(), target.blockPosition()));
                    }
                    ctx.spawnResult(out);
                    ctx.spawnResult(new ItemStack(ModItems.TAGLOCK_KIT.get()));
                }, null));

        // #89: Ritual de Duplicação de Pedra de Rumo Vinculada a Jogador
        list.add(new RitualDefinition(89, "duplicate_player_waystone",
                ChalkColor.NONE, ChalkColor.PURPLE, ChalkColor.NONE,
                2000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.BOUND_WAYSTONE::get, ModItems.WAYSTONE::get),
                null, false,
                ctx -> {
                    String sig = ctx.findTaglockUuidString();
                    GlobalPos gp = ctx.findBoundCoordinates();
                    for (int i = 0; i < 2; i++) {
                        ItemStack out = new ItemStack(ModItems.BOUND_WAYSTONE.get());
                        if (sig != null) out.set(ModDataComponentTypes.DNA_SIGNATURE.get(), sig);
                        if (gp != null) out.set(ModDataComponentTypes.BOUND_COORDINATES.get(), gp);
                        ctx.spawnResult(out);
                    }
                }, null));

        // #90: Ritual de Criação da Estátua de Adoração dos Goblins (Statue of Worship)
        list.add(new RitualDefinition(90, "create_statue_of_worship",
                ChalkColor.NONE, ChalkColor.WHITE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.KOBOLDITE_PENTACLE::get, () -> Items.GOLD_BLOCK, () -> Items.SMOOTH_STONE),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.KOBOLDITE_PENTACLE.get(), 2)), null));

        // #91: Ritual de Teletransporte Rápido para Pedra de Rumo
        list.add(new RitualDefinition(91, "fast_teleport_waystone",
                ChalkColor.PURPLE, ChalkColor.NONE, ChalkColor.NONE,
                1000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.BOUND_WAYSTONE::get, () -> Items.FEATHER),
                null, false,
                CircleRitualRegistry::teleportToWaystone, null));

        // #92: Maldição do Lobo (Transformar em Lobisomem - Nível Avançado)
        list.add(new RitualDefinition(92, "curse_of_the_wolf_advanced",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.RED,
                8000, 0, 0, 6, TimeWeatherCondition.FULL_MOON_NIGHT_ONLY,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.DOG_TONGUE::get, ModItems.WOLFSBANE_FLOWER::get),
                le -> le instanceof Wolf w && w.isTame(), true,
                ctx -> {
                    LivingEntity target = ctx.findTaglockTarget();
                    if (target instanceof Player p) {
                        int lvl = p.getData(ModAttachmentTypes.WEREWOLF_LEVEL.get());
                        p.setData(ModAttachmentTypes.WEREWOLF_LEVEL.get(), Math.max(1, lvl + 1));
                    }
                    ctx.spawnResult(new ItemStack(ModItems.TAGLOCK_KIT.get()));
                }, null));

        // #93: Maldição do Lobo (Infecção Primária por Mordida Espiritual)
        list.add(new RitualDefinition(93, "curse_of_the_wolf_infection",
                ChalkColor.NONE, ChalkColor.RED, ChalkColor.NONE,
                6000, 0, 0, 4, TimeWeatherCondition.NIGHT_ONLY,
                List.of(ModItems.TAGLOCK_KIT::get, ModItems.DOG_TONGUE::get, ModItems.DEMONIC_BLOOD::get),
                le -> le instanceof Wolf, true,
                ctx -> {
                    LivingEntity target = ctx.findTaglockTarget();
                    if (target instanceof Player p) {
                        if (p.getData(ModAttachmentTypes.WEREWOLF_LEVEL.get()) == 0) {
                            p.setData(ModAttachmentTypes.WEREWOLF_LEVEL.get(), 1);
                        }
                    }
                    ctx.spawnResult(new ItemStack(ModItems.TAGLOCK_KIT.get()));
                }, null));

        // #94: Ritual de Purificação e Remoção do Vampirismo
        list.add(new RitualDefinition(94, "cure_vampirism",
                ChalkColor.NONE, ChalkColor.NONE, ChalkColor.WHITE,
                10000, 0, 0, 6, TimeWeatherCondition.NOON_SUN_ONLY,
                List.of(ModItems.TEAR_OF_THE_GODDESS::get, ModItems.GARLIC::get, () -> Items.ENCHANTED_GOLDEN_APPLE),
                null, false,
                ctx -> {
                    ctx.activator().setData(ModAttachmentTypes.VAMPIRE_LEVEL.get(), 0);
                    ctx.activator().setData(ModAttachmentTypes.BLOOD_LEVEL.get(), 0);
                    ctx.activator().setRemainingFireTicks(100);
                }, null));

        // #95: Ritual de Criação do Espelho Místico (Mirror)
        list.add(new RitualDefinition(95, "create_mystic_mirror",
                ChalkColor.NONE, ChalkColor.PURPLE, ChalkColor.NONE,
                6000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(() -> Items.GLASS_PANE, ModItems.SILVER_DUST::get, ModItems.TEAR_OF_THE_GODDESS::get),
                null, false,
                ctx -> ctx.spawnResult(new ItemStack(ModItems.MYSTIC_MIRROR.get())), null));

        // #96: Ritual de Invocação do Reflexo Sombrio (Mirror Reflection)
        list.add(new RitualDefinition(96, "summon_mirror_reflection",
                ChalkColor.NONE, ChalkColor.PURPLE, ChalkColor.NONE,
                8000, 0, 0, 0, TimeWeatherCondition.NONE,
                List.of(ModItems.MYSTIC_MIRROR::get, ModItems.REFINED_EVIL::get, ModItems.TAGLOCK_KIT::get),
                null, false,
                ctx -> {
                    Skeleton clone = EntityType.SKELETON.create(ctx.level());
                    if (clone != null) {
                        clone.setCustomName(Component.translatable("entity.occultum.mirror_reflection", ctx.activator().getName()));
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            clone.setItemSlot(slot, ctx.activator().getItemBySlot(slot).copy());
                        }
                        clone.moveTo(ctx.center().getX() + 0.5, ctx.center().getY() + 0.2, ctx.center().getZ() + 0.5, 0, 0);
                        ctx.level().addFreshEntity(clone);
                    }
                    ctx.spawnResult(new ItemStack(ModItems.TAGLOCK_KIT.get()));
                }, null));

        RITUALS = Collections.unmodifiableList(list);
        Map<Integer, RitualDefinition> map = new HashMap<>();
        for (RitualDefinition d : list) {
            map.put(d.id(), d);
        }
        BY_ID = Collections.unmodifiableMap(map);
    }

    private static void absorbCirclesIntoTalisman(ConsumedRitualContext ctx) {
        int mask = ctx.rings().packMask();
        CircleGeometryValidator.clearRing(ctx.level(), ctx.center(), CircleGeometryValidator.SMALL_RING_OFFSETS);
        CircleGeometryValidator.clearRing(ctx.level(), ctx.center(), CircleGeometryValidator.MEDIUM_RING_OFFSETS);
        CircleGeometryValidator.clearRing(ctx.level(), ctx.center(), CircleGeometryValidator.LARGE_RING_OFFSETS);
        ctx.level().setBlock(ctx.center(), Blocks.AIR.defaultBlockState(), 3);

        ItemStack out = new ItemStack(ModItems.CIRCLE_TALISMAN.get());
        CircleTalismanItem.setStoredMask(out, mask);
        ctx.spawnResult(out);
    }

    private static void bindWaystoneToCircle(ConsumedRitualContext ctx) {
        ItemStack bound = new ItemStack(ModItems.BOUND_WAYSTONE.get());
        bound.set(ModDataComponentTypes.BOUND_COORDINATES.get(), GlobalPos.of(ctx.level().dimension(), ctx.center()));
        ctx.spawnResult(bound);
    }

    private static void teleportToWaystone(ConsumedRitualContext ctx) {
        GlobalPos gp = ctx.findBoundCoordinates();
        if (gp != null) {
            ServerLevel destLevel = ctx.level().getServer().getLevel(gp.dimension());
            if (destLevel != null) {
                BlockPos dest = gp.pos();
                for (Entity e : ctx.level().getEntitiesOfClass(Entity.class, new AABB(ctx.center()).inflate(3.0))) {
                    if (e instanceof LivingEntity le) {
                        if (destLevel == ctx.level()) {
                            le.teleportTo(dest.getX() + 0.5, dest.getY() + 0.2, dest.getZ() + 0.5);
                        } else if (le instanceof ServerPlayer sp) {
                            sp.teleportTo(destLevel, dest.getX() + 0.5, dest.getY() + 0.2, dest.getZ() + 0.5, sp.getYRot(), sp.getXRot());
                        }
                    }
                }
                destLevel.playSound(null, dest, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            ItemStack returned = new ItemStack(ModItems.BOUND_WAYSTONE.get());
            returned.set(ModDataComponentTypes.BOUND_COORDINATES.get(), gp);
            ctx.spawnResult(returned);
        }
    }

    private static void maintainProtectionDome(ServerLevel level, BlockPos center, double radius, double height, boolean repelAll, @Nullable Player owner) {
        Vec3 c = Vec3.atCenterOf(center);
        AABB box = new AABB(center).inflate(radius + 1.0, height, radius + 1.0);
        for (Entity e : level.getEntities(null, box)) {
            if (e == owner) continue;
            if (e instanceof Projectile proj) {
                proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.0));
                proj.hurtMarked = true;
            } else if (repelAll || e instanceof Monster) {
                double dist = e.position().distanceTo(c);
                if (dist <= radius + 0.5) {
                    Vec3 push = e.position().subtract(c).normalize().scale(0.85);
                    e.setDeltaMovement(push.x, 0.25, push.z);
                    e.hurtMarked = true;
                }
            }
        }
    }

    private static void generateVolcanoCone(ServerLevel level, BlockPos center, int radius, int height) {
        for (int y = 0; y <= height; y++) {
            int layerRadius = Math.max(1, radius - (y * (radius - 2) / Math.max(1, height)));
            for (int dx = -layerRadius; dx <= layerRadius; dx++) {
                for (int dz = -layerRadius; dz <= layerRadius; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    BlockPos p = center.offset(dx, y, dz);
                    if (d <= 1.2) {
                        level.setBlock(p, Blocks.LAVA.defaultBlockState(), 3);
                    } else if (d <= layerRadius) {
                        level.setBlock(p, Blocks.BASALT.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void strikeOuterRingLightning(ServerLevel level, BlockPos center, int tick) {
        if (tick % 20 == 0) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double dist = 5.0 + level.random.nextDouble() * 13.0;
            double lx = center.getX() + 0.5 + Math.cos(angle) * dist;
            double lz = center.getZ() + 0.5 + Math.sin(angle) * dist;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(lx, center.getY(), lz);
                level.addFreshEntity(bolt);
            }
        }
    }

    private static void raiseStoneColumn(ServerLevel level, BlockPos center, int radius, int height) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    for (int dy = 1; dy <= height; dy++) {
                        BlockPos p = center.offset(dx, dy, dz);
                        if (level.getBlockState(p).isAir() || level.getBlockState(p).canBeReplaced()) {
                            level.setBlock(p, Blocks.STONE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(radius))) {
            le.teleportTo(le.getX(), le.getY() + height, le.getZ());
        }
    }

    private static void banishNetherCreatures(ServerLevel level, BlockPos center, int radius) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(radius))) {
            if (e instanceof Blaze || e instanceof Ghast || e instanceof MagmaCube || e instanceof WitherSkeleton || e instanceof net.minecraft.world.entity.monster.piglin.Piglin) {
                level.sendParticles(ParticleTypes.SMOKE, e.getX(), e.getY() + 0.5, e.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
                e.discard();
            }
        }
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 3, radius))) {
            if (level.getBlockState(p).is(Blocks.FIRE)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static void summonSuperiorDemon(ServerLevel level, BlockPos center) {
        WitherSkeleton demon = EntityType.WITHER_SKELETON.create(level);
        if (demon != null) {
            demon.setCustomName(Component.translatable("entity.occultum.superior_demon"));
            demon.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            demon.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 999999, 1));
            demon.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 0));
            demon.moveTo(center.getX() + 0.5, center.getY() + 0.2, center.getZ() + 0.5, 0, 0);
            level.addFreshEntity(demon);
        }
    }

    private static void summonWitherBoss(ServerLevel level, BlockPos center) {
        WitherBoss wither = EntityType.WITHER.create(level);
        if (wither != null) {
            wither.moveTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 0, 0);
            wither.makeInvulnerable();
            level.addFreshEntity(wither);
        }
    }

    private static void applyPlayerInfusion(Player player, String type, int maxPower) {
        player.setData(ModAttachmentTypes.INFUSION_TYPE.get(), type);
        player.setData(ModAttachmentTypes.INFUSION_MAX_POWER.get(), maxPower);
        player.setData(ModAttachmentTypes.INFUSION_POWER.get(), maxPower);
    }

    private static void applyFertilityWave(ServerLevel level, BlockPos center, Player activator, int radius) {
        for (int i = 0; i < 120; i++) {
            BlockPos p = center.offset(level.random.nextInt(radius * 2 + 1) - radius,
                    level.random.nextInt(15) - 7,
                    level.random.nextInt(radius * 2 + 1) - radius);
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof BonemealableBlock bm && bm.isValidBonemealTarget(level, p, st)) {
                bm.performBonemeal(level, level.random, p, st);
            }
        }
        for (Animal animal : level.getEntitiesOfClass(Animal.class, new AABB(center).inflate(radius, 15.0, radius))) {
            animal.setInLove(activator);
        }
    }

    private static void applyBlightWave(ServerLevel level, BlockPos center, int radius) {
        for (int i = 0; i < 200; i++) {
            BlockPos p = center.offset(level.random.nextInt(radius * 2 + 1) - radius,
                    level.random.nextInt(15) - 7,
                    level.random.nextInt(radius * 2 + 1) - radius);
            BlockState st = level.getBlockState(p);
            if (st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.FARMLAND)) {
                level.setBlock(p, Blocks.COARSE_DIRT.defaultBlockState(), 3);
            } else if (st.is(BlockTags.LEAVES) || st.is(BlockTags.CROPS)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static void transformAreaToNether(ServerLevel level, BlockPos center, int radius) {
        for (int i = 0; i < 60; i++) {
            BlockPos p = center.offset(level.random.nextInt(radius * 2 + 1) - radius,
                    level.random.nextInt(7) - 3,
                    level.random.nextInt(radius * 2 + 1) - radius);
            BlockState st = level.getBlockState(p);
            if (st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.DIRT) || st.is(Blocks.STONE)) {
                level.setBlock(p, Blocks.NETHERRACK.defaultBlockState(), 3);
                if (level.getBlockState(p.above()).isAir() && level.random.nextFloat() < 0.25F) {
                    level.setBlock(p.above(), Blocks.FIRE.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void applyCurseToTaglock(ConsumedRitualContext ctx, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int baseAmp) {
        LivingEntity target = ctx.findTaglockTarget();
        if (target != null) {
            // Bônus de Gato Familiar (+1 nível de potência em maldições, Seção 4.2.1 de 13_BRASEIRO)
            int catBonus = "cat".equalsIgnoreCase(ctx.activator().getData(ModAttachmentTypes.FAMILIAR_TYPE.get())) ? 1 : 0;
            boolean blockedByPoppet = false;
            if (target instanceof Player victim) {
                blockedByPoppet = PoppetHandler.tryInterceptVoodooOrCurse(ctx.level().getServer(), victim, 75, catBonus > 0);
            }
            if (!blockedByPoppet) {
                target.addEffect(new MobEffectInstance(effect, duration, baseAmp + catBonus));
            }
        }
        ctx.spawnResult(new ItemStack(ModItems.TAGLOCK_KIT.get()));
    }

    private static void smeltItemsInCircle(ServerLevel level, BlockPos center, int maxPerCycle) {
        int smelted = 0;
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, new AABB(center).inflate(5.0))) {
            if (smelted >= maxPerCycle) break;
            ItemStack in = ie.getItem();
            Optional<RecipeHolder<SmeltingRecipe>> rec = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(in), level);
            if (rec.isPresent()) {
                ItemStack out = rec.get().value().getResultItem(level.registryAccess()).copyWithCount(in.getCount());
                ie.setItem(out);
                level.sendParticles(ParticleTypes.FLAME, ie.getX(), ie.getY() + 0.2, ie.getZ(), 8, 0.1, 0.1, 0.1, 0.02);
                smelted++;
            }
        }
    }

    private static void growForestAround(ServerLevel level, BlockPos center, Block logBlock, Block leavesBlock, int radius) {
        for (int i = 0; i < 12; i++) {
            int dx = level.random.nextInt(radius * 2 + 1) - radius;
            int dz = level.random.nextInt(radius * 2 + 1) - radius;
            if (dx * dx + dz * dz < 16) continue;
            BlockPos base = center.offset(dx, 0, dz);
            if (!level.getBlockState(base).isAir()) continue;
            if (!level.getBlockState(base.below()).is(BlockTags.DIRT)) continue;

            int trunkH = 4 + level.random.nextInt(3);
            for (int y = 0; y < trunkH; y++) {
                level.setBlock(base.above(y), logBlock.defaultBlockState(), 3);
            }
            for (int lx = -2; lx <= 2; lx++) {
                for (int lz = -2; lz <= 2; lz++) {
                    for (int ly = trunkH - 2; ly <= trunkH + 1; ly++) {
                        if (Math.abs(lx) == 2 && Math.abs(lz) == 2 && ly > trunkH - 1) continue;
                        BlockPos lp = base.offset(lx, ly, lz);
                        if (level.getBlockState(lp).isAir()) {
                            level.setBlock(lp, leavesBlock.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private static void bindSpiritToFetishItemOrBlock(ConsumedRitualContext ctx, Item fetishItem, FetishBlockEntity.SpiritEffect defaultEffect) {
        FetishBlockEntity.SpiritEffect resolved = defaultEffect;
        for (ItemStack s : ctx.consumedItems()) {
            String critter = s.get(ModDataComponentTypes.CAPTURED_CRITTER.get());
            if (critter != null) {
                resolved = FetishBlockEntity.SpiritEffect.fromCapturedString(critter, defaultEffect);
            }
        }
        ItemStack out = new ItemStack(fetishItem);
        out.set(ModDataComponentTypes.FETISH_SPIRIT_EFFECT.get(), resolved.name());
        ctx.spawnResult(out);
    }
}
