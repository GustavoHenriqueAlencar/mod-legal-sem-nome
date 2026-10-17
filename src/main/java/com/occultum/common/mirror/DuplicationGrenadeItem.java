package com.occultum.common.mirror;

import com.occultum.common.entity.ReflectionDemonEntity;
import com.occultum.common.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Granada de Duplicação (`14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md` Seção 5.3):
 * - Fabricada clicando no Espelho Místico com uma Esfera de Quartzo + 2.000 de poder do Altar.
 * - Ao ser arremessada, explode no ponto de mira invocando uma cópia idêntica do jogador (`ReflectionDemonEntity`
 *   em modo amigável por 60 segundos = 1.200 ticks) para lutar ao seu lado.
 */
public class DuplicationGrenadeItem extends Item {

    public DuplicationGrenadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 spawnPos = player.position().add(player.getLookAngle().scale(2.5D));
            ReflectionDemonEntity clone = ModEntityTypes.REFLECTION_DEMON.get().create(serverLevel);
            if (clone != null) {
                clone.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0F);
                clone.setFriendlyClone(true, 1200);
                clone.mimicPlayer(player);

                AABB box = clone.getBoundingBox().inflate(16.0D);
                List<LivingEntity> hostiles = serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != player && e != clone && e instanceof Enemy && e.isAlive());
                if (!hostiles.isEmpty()) {
                    clone.setTarget(hostiles.get(0));
                }
                serverLevel.addFreshEntity(clone);
                serverLevel.sendParticles(ParticleTypes.CLOUD, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 25, 0.4, 0.5, 0.4, 0.05);
                serverLevel.playSound(null, clone.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
