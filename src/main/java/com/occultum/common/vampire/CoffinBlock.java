package com.occultum.common.vampire;

import com.occultum.common.poppet.TaglockKitItem;
import com.occultum.common.spirit.DreamWeaverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Caixão Vampírico (Coffin Block - Seção 2.3 e 2.5.3 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Permite que vampiros durmam durante o dia (ou humanos à noite) registrando ponto de retorno para o Ultimate FARM.
 * - Registra o DNA para o Kit Taglock (Seção 2.1.B de 06_POPPETS_E_FETISHES.md).
 * - Se um vampiro estiver dormindo no caixão e for atingido por uma Estaca de Madeira, sofre morte instantânea.
 */
public class CoffinBlock extends BedBlock {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public CoffinBlock(Properties properties) {
        super(DyeColor.BLACK, properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            TaglockKitItem.recordBedSleep(pos, sp);
            sp.setRespawnPosition(sl.dimension(), pos, sp.getYRot(), true, true);
            sp.startSleeping(pos);
            // Ao repousar no caixão, ativa os Apanhadores de Sonhos (Dream Weavers) num raio de 5 blocos
            DreamWeaverBlock.applyWakeUpEffectsAround(sl, pos, sp);
        }
        return InteractionResult.SUCCESS;
    }
}
