package com.occultum.common.ritual;

import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Bloco de glifo de giz (Branco Ritual, Roxo Outro Lugar, Vermelho Infernal).
 * Se quebrar durante um ritual contínuo nas redondezas, notifica o Glifo Dourado central
 * para colapsar imediatamente o ritual (Seção 5.3 de 05_MAGIA_DE_CIRCULO_E_RITUAIS.md).
 */
public class ChalkGlyphBlock extends CarpetBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 11);

    private final CircleGeometryValidator.ChalkColor chalkColor;

    public ChalkGlyphBlock(Properties properties, CircleGeometryValidator.ChalkColor chalkColor) {
        super(properties);
        this.chalkColor = chalkColor;
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    public CircleGeometryValidator.ChalkColor getChalkColor() {
        return chalkColor;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && !oldState.is(this)) {
            int v = Math.floorMod(pos.hashCode(), 12);
            level.setBlock(pos, state.setValue(VARIANT, v), 2);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            // Seção 5.3: Quebra física de glifos durante ritual contínuo colapsa a matriz no mesmo instante
            for (BlockPos check : BlockPos.betweenClosed(pos.offset(-7, 0, -7), pos.offset(7, 0, 7))) {
                if (level.getBlockState(check).is(ModBlocks.GOLDEN_CHALK_BLOCK.get())) {
                    if (level.getBlockEntity(check) instanceof GoldenChalkBlockEntity golden && golden.isActive()) {
                        golden.onGlyphBroken(pos);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
