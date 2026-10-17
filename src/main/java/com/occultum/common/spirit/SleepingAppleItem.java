package com.occultum.common.spirit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Maçã Adormecida (Sleeping Apple - Seção 3.1.2 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - Chance Base de Pesadelo: 1.0 (100.0%).
 * - Causa adormecimento forçado imediato e arremessa o jogador diretamente em um Pesadelo Forçado
 *   no Mundo Espiritual, ignorando mitigações convencionais.
 */
public class SleepingAppleItem extends Item {

    public SleepingAppleItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            SpiritWorldManager.enterSpiritWorld(sp, true);
        }
        return result;
    }
}
