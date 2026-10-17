package com.occultum.common.spirit;

import com.mojang.serialization.MapCodec;
import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Portal Espiritual (Spirit Portal 4x4 - Seção 7.3 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - Moldura vertical de 4x4 blocos (abertura central de 2x2) feita de Pedra Tormentosa / Moldura Ritual
 *   e preenchida com Espírito Fluido (Flowing Spirit).
 * - Ao cruzar o Portal Espiritual a partir do Mundo Espiritual, o jogador projeta-se de volta ao Overworld
 *   em Forma Fantasma / Espectral enquanto seu corpo físico continua dormindo.
 */
public class SpiritPortalBlock extends Block {

    public static final MapCodec<SpiritPortalBlock> CODEC = simpleCodec(SpiritPortalBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);

    public SpiritPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            if (level.dimension() == OccultumDimensions.SPIRIT_WORLD_LEVEL) {
                SpiritWorldManager.manifestAsGhostInOverworld(sp);
            }
        }
    }

    /**
     * Verifica e ativa uma moldura 4x4 (com abertura 2x2) quando Espírito Fluido é colocado no interior.
     */
    public static boolean tryActivatePortalFrame4x4(ServerLevel level, BlockPos fluidPos) {
        for (int ox = -1; ox <= 0; ox++) {
            for (int oy = -1; oy <= 0; oy++) {
                BlockPos bottomLeft = fluidPos.offset(ox, oy, 0);
                if (checkAndFillFrameX(level, bottomLeft)) return true;
                BlockPos bottomLeftZ = fluidPos.offset(0, oy, ox);
                if (checkAndFillFrameZ(level, bottomLeftZ)) return true;
            }
        }
        return false;
    }

    private static boolean checkAndFillFrameX(ServerLevel level, BlockPos innerBottomLeft) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                boolean border = (dx == -1 || dx == 2 || dy == -1 || dy == 2);
                BlockPos p = innerBottomLeft.offset(dx, dy, 0);
                if (border) {
                    if (!isFrameBlock(level.getBlockState(p))) return false;
                }
            }
        }
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                level.setBlock(innerBottomLeft.offset(dx, dy, 0), ModBlocks.SPIRIT_PORTAL.get().defaultBlockState(), 3);
            }
        }
        return true;
    }

    private static boolean checkAndFillFrameZ(ServerLevel level, BlockPos innerBottomLeft) {
        for (int dz = -1; dz <= 2; dz++) {
            for (int dy = -1; dy <= 2; dy++) {
                boolean border = (dz == -1 || dz == 2 || dy == -1 || dy == 2);
                BlockPos p = innerBottomLeft.offset(0, dy, dz);
                if (border) {
                    if (!isFrameBlock(level.getBlockState(p))) return false;
                }
            }
        }
        for (int dz = 0; dz <= 1; dz++) {
            for (int dy = 0; dy <= 1; dy++) {
                level.setBlock(innerBottomLeft.offset(0, dy, dz), ModBlocks.SPIRIT_PORTAL.get().defaultBlockState(), 3);
            }
        }
        return true;
    }

    private static boolean isFrameBlock(BlockState st) {
        return st.is(ModBlocks.TORMENT_STONE.get())
                || st.is(Blocks.SNOW_BLOCK)
                || st.is(Blocks.QUARTZ_BLOCK)
                || st.is(Blocks.STONE_BRICKS);
    }
}
