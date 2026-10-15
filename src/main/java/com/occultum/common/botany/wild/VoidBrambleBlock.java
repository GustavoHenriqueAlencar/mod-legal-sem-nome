package com.occultum.common.botany.wild;

import com.occultum.common.altar.AltarPowerNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class VoidBrambleBlock extends Block {

    public VoidBrambleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            AltarPowerNetwork.registerVoidBramble(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                AltarPowerNetwork.unregisterVoidBramble(level, pos);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            // Seção 4.6: Teleporte forçado aleatório de até 500 blocos
            double angle = level.random.nextDouble() * 2.0 * Math.PI;
            double distance = 100.0 + level.random.nextDouble() * 400.0; // Até 500 blocos
            double targetX = pos.getX() + Math.cos(angle) * distance;
            double targetZ = pos.getZ() + Math.sin(angle) * distance;
            double targetY = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, new BlockPos((int) targetX, 0, (int) targetZ)).getY();

            level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            living.teleportTo(targetX, targetY, targetZ);
            level.playSound(null, targetX, targetY, targetZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        super.entityInside(state, level, pos, entity);
    }
}
