package com.occultum.common.botany.crop;

import com.occultum.common.entity.MandrakeEntity;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

public class MandrakeCropBlock extends BaseWitchCropBlock {
    public static final MapCodec<MandrakeCropBlock> CODEC = simpleCodec(MandrakeCropBlock::new);

    @Override
    protected MapCodec<? extends BaseWitchCropBlock> codec() {
        return CODEC;
    }

    public MandrakeCropBlock(Properties properties) {
        super(properties, ModItems.MANDRAKE_SEEDS);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isMaxAge(state)) {
            boolean isDay = level.isDay();
            boolean isPeaceful = level.getDifficulty() == Difficulty.PEACEFUL;
            float awakeChance = isDay ? 0.90F : 0.10F;

            if (!isPeaceful && level.random.nextFloat() < awakeChance) {
                // 90% de dia / 10% de noite: Desperta como monstro Mandrágora viva
                MandrakeEntity mandrake = ModEntityTypes.MANDRAKE.get().create(level);
                if (mandrake != null) {
                    mandrake.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                    mandrake.setDeltaMovement(0, 0.35, 0); // Ejetada para fora da terra
                    mandrake.setTarget(player);
                    level.addFreshEntity(mandrake);

                    // Efeitos sonoros e partículas de estrondo
                    level.playSound(null, pos, SoundEvents.GHAST_SCREAM, SoundSource.HOSTILE, 1.0F, 1.2F);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            } else {
                // Colheita pacífica do item
                popResource(level, pos, new ItemStack(ModItems.MANDRAKE_ROOT.get()));
                int seeds = 1 + level.random.nextInt(4);
                popResource(level, pos, new ItemStack(ModItems.MANDRAKE_SEEDS.get(), seeds));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
