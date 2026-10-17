package com.occultum.common.vampire;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Guirlanda de Alho (Garlic Garland - Seção 2.5.5 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Projeta uma força vetorial repulsiva contínua impedindo que vampiros se aproximem ou cruzem o vão.
 * - Se um vampiro desferir clique esquerdo para tentar minerar/quebrar a Guirlanda, ele é incendiado
 *   por 1 segundo e a mineração é cancelada.
 */
public class GarlicGarlandBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<GarlicGarlandBlock> CODEC = simpleCodec(GarlicGarlandBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public GarlicGarlandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && VampirismSystem.isVampire(player)) {
            player.igniteForSeconds(1.0F);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player player && VampirismSystem.isVampire(player)) {
            Vec3 away = player.position().subtract(Vec3.atCenterOf(pos)).normalize().scale(0.65D);
            player.setDeltaMovement(away.x, 0.25D, away.z);
            player.hurtMarked = true;
            player.igniteForSeconds(1.0F);
        }
    }
}
