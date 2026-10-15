package com.occultum.common.botany.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;

public abstract class BaseWitchCropBlock extends BushBlock implements BonemealableBlock {
    @Override
    protected abstract MapCodec<? extends BaseWitchCropBlock> codec();
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)
    };

    private final Supplier<? extends Item> seedSupplier;

    public BaseWitchCropBlock(Properties properties, Supplier<? extends Item> seedSupplier) {
        super(properties);
        this.seedSupplier = seedSupplier;
        this.registerDefaultState(this.stateDefinition.any().setValue(getAgeProperty(), 0));
    }

    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return 4;
    }

    public int getAge(BlockState state) {
        return state.getValue(this.getAgeProperty());
    }

    public BlockState getStateForAge(int age) {
        return this.defaultBlockState().setValue(this.getAgeProperty(), age);
    }

    public boolean isMaxAge(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(this.getAgeProperty())];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof FarmBlock;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public ItemLike getBaseSeedId() {
        return seedSupplier.get();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Regra 2.1: Requisito de iluminação mínima de 9 no bloco superior (Y+1)
        if (level.getRawBrightness(pos.above(), 0) >= 9) {
            int currentAge = this.getAge(state);
            if (currentAge < this.getMaxAge()) {
                float growthRate = getGrowthSpeed(this, level, pos);
                // Fórmula de velocidade de crescimento (Seção 2.1):
                // Chance = 1 / (floor(25 / F_taxa) + 1)
                int denominator = Mth.floor(25.0F / growthRate) + 1;
                if (random.nextInt(Math.max(1, denominator)) == 0) {
                    level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
                }
            }
        }
    }

    protected static float getGrowthSpeed(Block block, BlockGetter level, BlockPos pos) {
        float rate = 1.0F;
        BlockPos below = pos.below();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                float points = 0.0F;
                BlockState soil = level.getBlockState(below.offset(x, 0, z));
                if (soil.getBlock() instanceof FarmBlock) {
                    points = 1.0F;
                    if (soil.hasProperty(FarmBlock.MOISTURE) && soil.getValue(FarmBlock.MOISTURE) > 0) {
                        points = 3.0F; // Solo arado hidratado
                    }
                }
                if (x != 0 || z != 0) {
                    points /= 4.0F;
                }
                rate += points;
            }
        }

        // Penalidade por culturas iguais nas diagonais
        BlockPos northWest = pos.north().west();
        BlockPos southWest = pos.south().west();
        BlockPos northEast = pos.north().east();
        BlockPos southEast = pos.south().east();
        boolean hasDiagonalSame = level.getBlockState(northWest).is(block)
                || level.getBlockState(southWest).is(block)
                || level.getBlockState(northEast).is(block)
                || level.getBlockState(southEast).is(block);

        if (hasDiagonalSame) {
            rate /= 2.0F;
        }

        return Math.max(0.1F, rate);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !this.isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int nextAge = Math.min(this.getMaxAge(), this.getAge(state) + 1);
        level.setBlock(pos, this.getStateForAge(nextAge), 2);
    }
}
