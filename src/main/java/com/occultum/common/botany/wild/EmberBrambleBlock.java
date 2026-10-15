package com.occultum.common.botany.wild;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EmberBrambleBlock extends Block {

    public EmberBrambleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living) {
            // Seção 4.7: Causa dano perfurante e incinera
            living.hurt(level.damageSources().cactus(), 2.0F);
            if (!living.fireImmune()) {
                living.igniteForSeconds(3);
            }
        }
        super.entityInside(state, level, pos, entity);
    }
}
