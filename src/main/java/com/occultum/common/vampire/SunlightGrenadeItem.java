package com.occultum.common.vampire;

import com.occultum.common.registry.ModAttachmentTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Granada Solar (Sunlight Grenade - Seção 2.3 Transição 4 -> 5 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Detona emitindo um clarão solar e chamas num raio de 5 blocos.
 * - Se um jogador Vampiro de Nível 4 sobreviver no raio de detonação de 10 Granadas Solares,
 *   conclui o "Batismo Solar" e ascende ao Nível 5 (Resistência à Queima Solar).
 */
public class SunlightGrenadeItem extends Item {

    public SunlightGrenadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel sl) {
            BlockHitResult hit = sl.clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(16.0D)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
            ));
            Vec3 center = hit.getLocation();
            sl.sendParticles(ParticleTypes.FLASH, center.x, center.y + 0.5D, center.z, 2, 0.0, 0.0, 0.0, 0.0);
            sl.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.5D, center.z, 45, 2.0, 1.0, 2.0, 0.08);
            sl.playSound(null, hit.getBlockPos(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.2F);

            AABB area = new AABB(hit.getBlockPos()).inflate(5.0D);
            for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, area)) {
                e.igniteForSeconds(5.0F);
                e.hurt(sl.damageSources().onFire(), 4.0F);

                if (e instanceof Player vp && vp.isAlive() && VampirismSystem.getVampireLevel(vp) == 4) {
                    int survived = vp.getData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get()) + 1;
                    vp.setData(ModAttachmentTypes.VAMPIRE_QUEST_COUNTER.get(), survived);
                    if (survived >= 10) {
                        VampirismSystem.tryAdvanceVampireLevel(vp, 5);
                    }
                }
            }
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
