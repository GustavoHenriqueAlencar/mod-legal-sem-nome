package com.occultum.common.spirit;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Os 5 Apanhadores de Sonhos (Dream Weavers - Seção 5 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - Tapeçarias penduradas em paredes verticais sólidas.
 * - Raio de Ação: Caixa cúbica de raio 5 blocos (11x11x11 centrada na tapeçaria).
 * - Interação em Rede:
 *   * Se houver um Dream Weaver of Nightmares no raio de 5 blocos: todos os sonhos são convertidos em Pesadelos.
 *   * Se houver um Dream Weaver of Intensity no raio de 5 blocos: todos os efeitos são Potencializados.
 * - Catálogo Exato (Tabela 5.2):
 *   1. FLEET_FOOT: Bom = Speed I 7200t | Bom Pot = Speed II 4800t | Pesadelo = Slowness I 7200t | Pesadelo Pot = Slowness II 7200t
 *   2. IRON_ARM: Bom = Haste I 7200t | Bom Pot = Haste II 4800t | Pesadelo = Mining Fatigue I 7200t | Pesadelo Pot = Mining Fatigue II 7200t
 *   3. FASTING: Bom = Saturation I 4800t | Bom Pot = Saturation I 7200t | Pesadelo = Hunger I 4800t | Pesadelo Pot = Hunger II 4800t
 *   4. NIGHTMARES: Bom = Weakness I 1200t | Bom Pot = Weakness II 1200t | Pesadelo = Blindness I 1200t | Pesadelo Pot = Blindness II 1200t
 *   5. INTENSITY: Bom = Night Vision I 300t | Bom Pot = Night Vision I 300t | Pesadelo = Blindness I 300t | Pesadelo Pot = Blindness II 300t
 */
public class DreamWeaverBlock extends HorizontalDirectionalBlock {

    public enum WeaverType {
        FLEET_FOOT,
        IRON_ARM,
        FASTING,
        NIGHTMARES,
        INTENSITY
    }

    public static final MapCodec<DreamWeaverBlock> CODEC = simpleCodec(p -> new DreamWeaverBlock(p, WeaverType.FLEET_FOOT));
    private static final VoxelShape SHAPE = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 15.0D, 14.0D);

    private final WeaverType weaverType;

    public DreamWeaverBlock(Properties properties, WeaverType weaverType) {
        super(properties);
        this.weaverType = weaverType;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public WeaverType getWeaverType() {
        return weaverType;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Escaneia a caixa cúbica de raio 5 blocos (11x11x11) ao redor do jogador que acabou de acordar
     * e aplica todos os efeitos de rede dos Dream Weavers encontrados.
     */
    public static void applyWakeUpEffectsAround(ServerLevel level, BlockPos sleepPos, Player player) {
        List<WeaverType> found = new ArrayList<>();
        boolean hasNightmareWeaver = false;
        boolean hasIntensityWeaver = false;

        for (BlockPos p : BlockPos.betweenClosed(sleepPos.offset(-5, -5, -5), sleepPos.offset(5, 5, 5))) {
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof DreamWeaverBlock dw) {
                WeaverType type = dw.getWeaverType();
                found.add(type);
                if (type == WeaverType.NIGHTMARES) {
                    hasNightmareWeaver = true;
                } else if (type == WeaverType.INTENSITY) {
                    hasIntensityWeaver = true;
                }
            }
        }

        if (found.isEmpty()) return;

        for (WeaverType type : found) {
            applySingleWeaverEffect(player, type, hasNightmareWeaver, hasIntensityWeaver);
        }
    }

    public static void applySingleWeaverEffect(Player player, WeaverType type, boolean nightmareCond, boolean boostedCond) {
        switch (type) {
            case FLEET_FOOT -> {
                if (!nightmareCond) {
                    int dur = boostedCond ? 4800 : 7200;
                    int amp = boostedCond ? 1 : 0;
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur, amp));
                } else {
                    int amp = boostedCond ? 1 : 0;
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 7200, amp));
                }
            }
            case IRON_ARM -> {
                if (!nightmareCond) {
                    int dur = boostedCond ? 4800 : 7200;
                    int amp = boostedCond ? 1 : 0;
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, dur, amp));
                } else {
                    int amp = boostedCond ? 1 : 0;
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 7200, amp));
                }
            }
            case FASTING -> {
                if (!nightmareCond) {
                    int dur = boostedCond ? 7200 : 4800;
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, dur, 0));
                } else {
                    int amp = boostedCond ? 1 : 0;
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 4800, amp));
                }
            }
            case NIGHTMARES -> {
                int amp = boostedCond ? 1 : 0;
                if (!nightmareCond) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, amp));
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1200, amp));
                }
            }
            case INTENSITY -> {
                if (!nightmareCond) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0));
                } else {
                    int amp = boostedCond ? 1 : 0;
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, amp));
                }
            }
        }
    }
}
