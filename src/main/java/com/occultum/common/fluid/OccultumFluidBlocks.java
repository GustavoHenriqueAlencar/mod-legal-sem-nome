package com.occultum.common.fluid;

import com.occultum.common.config.OccultumConfig;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModFluids;
import com.occultum.common.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;

public class OccultumFluidBlocks {

    /**
     * 4.1 Espírito Fluido (Flowing Spirit):
     * - Criaturas Vivas e Jogadores: Regeneração II por 5s (100 ticks) a cada segundo de imersão.
     * - Criaturas Mortas-Vivas e Vampiros: Fraqueza II por 15s (300 ticks).
     */
    public static class FlowingSpiritBlock extends LiquidBlock {
        public FlowingSpiritBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
            super(fluid.get(), properties);
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            if (!level.isClientSide && entity instanceof LivingEntity living && entity.tickCount % 20 == 0) {
                boolean isUndeadOrVampire = living.getType().is(EntityTypeTags.UNDEAD)
                        || living.getData(ModAttachmentTypes.VAMPIRE_LEVEL) > 0;
                if (isUndeadOrVampire) {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
                } else {
                    living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                }
            }
        }
    }

    /**
     * 4.2 Lágrimas Ocas (Hollow Tears):
     * - Criaturas Vivas e Jogadores: Fraqueza II por 5s (100 ticks) por contato.
     * - Criaturas Mortas-Vivas: Regeneração II por 5s (100 ticks).
     * - Solidificação em Cascata: transmuta toda a coluna conectada de até 64 blocos em blocos sólidos.
     */
    public static class HollowTearsBlock extends LiquidBlock {
        public HollowTearsBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
            super(fluid.get(), properties);
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            if (!level.isClientSide && entity instanceof LivingEntity living) {
                boolean isUndead = living.getType().is(EntityTypeTags.UNDEAD)
                        || living.getData(ModAttachmentTypes.VAMPIRE_LEVEL) > 0;
                if (isUndead) {
                    living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                } else {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                }
            }
        }

        public static int solidifyCascade(Level level, BlockPos startPos, BlockState replacementState) {
            if (level.isClientSide) return 0;
            Queue<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();
            queue.add(startPos);
            visited.add(startPos);
            int converted = 0;

            while (!queue.isEmpty() && converted < 256) {
                BlockPos current = queue.poll();
                if (Math.abs(current.getY() - startPos.getY()) > 64) {
                    continue;
                }
                BlockState state = level.getBlockState(current);
                if (state.is(ModBlocks.HOLLOW_TEARS_BLOCK.get())
                        || state.getFluidState().is(ModFluids.HOLLOW_TEARS.get())
                        || state.getFluidState().is(ModFluids.FLOWING_HOLLOW_TEARS.get())) {
                    level.setBlock(current, replacementState, 3);
                    converted++;

                    for (Direction dir : Direction.values()) {
                        BlockPos next = current.relative(dir);
                        if (!visited.contains(next)) {
                            BlockState nextState = level.getBlockState(next);
                            if (nextState.is(ModBlocks.HOLLOW_TEARS_BLOCK.get())
                                    || nextState.getFluidState().is(ModFluids.HOLLOW_TEARS.get())
                                    || nextState.getFluidState().is(ModFluids.FLOWING_HOLLOW_TEARS.get())) {
                                visited.add(next);
                                queue.add(next);
                            }
                        }
                    }
                }
            }
            return converted;
        }
    }

    /**
     * 4.3 Doença Líquida (Disease Liquid):
     * - Teste a cada 20 ticks: 33% de chance de aplicar Diseased por 1200 a 4800 ticks.
     * - Imunidades: Mortos-vivos, Vampiros e Lobisomens transformados.
     * - Evaporação ambiental: 10% de chance no randomTick se não estiver adjacente a uma fonte.
     */
    public static class DiseaseLiquidBlock extends LiquidBlock {
        public DiseaseLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
            super(fluid.get(), properties);
        }

        @Override
        public boolean isRandomlyTicking(BlockState state) {
            return true;
        }

        @Override
        public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            super.randomTick(state, level, pos, random);
            if (!state.getFluidState().isSource()) {
                boolean adjacentToSource = false;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (level.getFluidState(pos.relative(dir)).isSource()
                            && level.getBlockState(pos.relative(dir)).is(this)) {
                        adjacentToSource = true;
                        break;
                    }
                }
                if (!adjacentToSource && random.nextFloat() < 0.10F) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            } else if (random.nextFloat() < 0.10F) {
                boolean hasNeighborSource = false;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(pos.relative(dir)).is(this)
                            && level.getFluidState(pos.relative(dir)).isSource()) {
                        hasNeighborSource = true;
                        break;
                    }
                }
                if (!hasNeighborSource) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            if (!level.isClientSide && entity instanceof LivingEntity living && entity.tickCount % 20 == 0) {
                boolean isImmune = living.getType().is(EntityTypeTags.UNDEAD)
                        || living.getData(ModAttachmentTypes.VAMPIRE_LEVEL) > 0
                        || living.getData(ModAttachmentTypes.WEREWOLF_LEVEL) > 0;
                if (!isImmune && level.random.nextFloat() < 0.33F) {
                    int duration = 1200 + level.random.nextInt(3601); // 1200 a 4800 ticks (1 a 4 min)
                    living.addEffect(new MobEffectInstance(ModMobEffects.DISEASED, duration, 0));
                }
            }
        }
    }
}
