package com.occultum.common.torment;

import com.mojang.serialization.MapCodec;
import com.occultum.common.dimension.OccultumDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O Portal do Tormento (Torment Portal - Seção 1.6 de 14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md):
 * - Se o jogador estiver na Dimensão do Tormento:
 *   * 95% de Chance: Fuga bem-sucedida! Teleporta para a última cama no Overworld (ou spawn global)
 *     procurando bloco sólido seguro com pelo menos 2 blocos de ar acima.
 *   * 5% de Chance: Condenação renovada! Jogado em outro andar aleatório do Tormento (0 a 5).
 * - Se o jogador estiver fora do Tormento e tocar o bloco do portal: é sugado para o Nível 1 do Tormento.
 */
public class TormentPortalBlock extends Block {

    public static final MapCodec<TormentPortalBlock> CODEC = simpleCodec(TormentPortalBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public TormentPortalBlock(Properties properties) {
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
        if (level.isClientSide || !(entity instanceof ServerPlayer sp)) return;
        if (sp.isOnPortalCooldown()) return;
        sp.setPortalCooldown();

        if (level.dimension() == OccultumDimensions.TORMENT_LEVEL) {
            if (level.random.nextFloat() < 0.95F) {
                // 95% de chance: Fuga bem-sucedida para a cama ou spawn global no Overworld
                ServerLevel overworld = sp.server.overworld();
                BlockPos target = sp.getRespawnPosition();
                if (target == null || sp.getRespawnDimension() != Level.OVERWORLD) {
                    target = overworld.getSharedSpawnPos();
                }
                BlockPos safe = findSafeLandingSpot(overworld, target);
                sp.teleportTo(overworld, safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D, sp.getYRot(), sp.getXRot());
                overworld.playSound(null, safe, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.9F, 1.1F);
                sp.displayClientMessage(Component.translatable("message.occultum.torment.escaped").withStyle(ChatFormatting.GREEN), false);
            } else {
                // 5% de chance: Condenação renovada em um novo andar aleatório (0 a 5)
                int newFloor = level.random.nextInt(6);
                TormentDimensionManager.sendPlayerToTorment(sp, newFloor);
                sp.displayClientMessage(Component.translatable("message.occultum.torment.renewed").withStyle(ChatFormatting.DARK_RED), false);
            }
        } else {
            // Fora do Tormento: sugado sumariamente para o Nível 1 do Tormento
            TormentDimensionManager.sendPlayerToTorment(sp, 1);
        }
    }

    public static BlockPos findSafeLandingSpot(ServerLevel level, BlockPos origin) {
        for (int r = 0; r <= 6; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = -2; dy <= 4; dy++) {
                        BlockPos check = origin.offset(dx, dy, dz);
                        if (level.getBlockState(check.below()).isSolid()
                                && level.getBlockState(check).isAir()
                                && level.getBlockState(check.above()).isAir()) {
                            return check;
                        }
                    }
                }
            }
        }
        return origin.above();
    }
}
