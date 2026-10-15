package com.occultum.common.botany.tree;

import com.occultum.common.entity.EntEntity;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MysticLogBlock extends RotatedPillarBlock {

    public MysticLogBlock(Properties properties) {
        super(properties);
    }

    public static boolean isMysticLog(BlockState state) {
        return state.is(ModBlocks.ROWAN_LOG.get()) || state.is(ModBlocks.ROWAN_WOOD.get())
                || state.is(ModBlocks.ALDER_LOG.get()) || state.is(ModBlocks.ALDER_WOOD.get())
                || state.is(ModBlocks.HAWTHORN_LOG.get()) || state.is(ModBlocks.HAWTHORN_WOOD.get());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            checkAndSpawnEnt(level, pos, player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public static void checkAndSpawnEnt(Level level, BlockPos brokenPos, Player player) {
        // Seção 3.2: Contagem de troncos místicos conectados nas 6 faces vizinhas
        int neighborMysticLogs = 0;
        for (Direction dir : Direction.values()) {
            if (isMysticLog(level.getBlockState(brokenPos.relative(dir)))) {
                neighborMysticLogs++;
            }
        }

        // Fórmula exata: Chance = min(0.01 + (0.01 * Troncos Vizinhos), 0.05)
        float spawnChance = Math.min(0.01F + (0.01F * neighborMysticLogs), 0.05F);

        if (level.random.nextFloat() < spawnChance) {
            // Procura posição livre no solo com pelo menos 3 blocos de ar vertical entre 8 e 16 blocos do jogador
            BlockPos spawnPos = findEntSpawnPos(level, player.blockPosition());
            if (spawnPos != null) {
                EntEntity ent = ModEntityTypes.ENT.get().create(level);
                if (ent != null) {
                    ent.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                    ent.setTarget(player);
                    level.addFreshEntity(ent);

                    // Efeitos sonoros de cavalo esqueleto e partículas de fumaça negra
                    level.playSound(null, spawnPos, SoundEvents.SKELETON_HORSE_DEATH, SoundSource.HOSTILE, 1.5F, 0.8F);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5, 30, 0.5, 1.0, 0.5, 0.1);
                    }
                }
            }
        }
    }

    private static BlockPos findEntSpawnPos(Level level, BlockPos playerPos) {
        for (int attempts = 0; attempts < 16; attempts++) {
            int dist = 8 + level.random.nextInt(9); // 8 a 16 blocos
            double angle = level.random.nextDouble() * 2.0 * Math.PI;
            int x = playerPos.getX() + (int) (Math.cos(angle) * dist);
            int z = playerPos.getZ() + (int) (Math.sin(angle) * dist);
            int startY = playerPos.getY();

            for (int dy = -4; dy <= 4; dy++) {
                BlockPos checkPos = new BlockPos(x, startY + dy, z);
                if (level.getBlockState(checkPos.below()).isSolid() &&
                        level.isEmptyBlock(checkPos) &&
                        level.isEmptyBlock(checkPos.above()) &&
                        level.isEmptyBlock(checkPos.above(2))) {
                    return checkPos;
                }
            }
        }
        return null;
    }
}
