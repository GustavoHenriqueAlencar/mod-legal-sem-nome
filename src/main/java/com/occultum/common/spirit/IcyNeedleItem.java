package com.occultum.common.spirit;

import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.registry.ModAttachmentTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Agulha Gélida (Icy Needle - Seção 3.3, 7.2 e 7.3 de 09_MUNDO_ESPIRITUAL_E_SONHOS.md):
 * - Atravessa a barreira dimensional (Overworld <-> Mundo Espiritual).
 * - Quando usada no Mundo Espiritual: espeta a mão do jogador, consome 1 unidade e realiza o Despertar Seguro,
 *   transferindo os itens da lista branca e restaurando o inventário original do Overworld.
 * - Quando usada em Forma Fantasma no Overworld: puxa a forma espectral de volta para o Mundo Espiritual.
 */
public class IcyNeedleItem extends Item {

    public IcyNeedleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean inSpirit = level.dimension() == OccultumDimensions.SPIRIT_WORLD_LEVEL;
        boolean isGhost = player.getData(ModAttachmentTypes.SPIRIT_GHOST_FORM.get());

        if (!inSpirit && !isGhost) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            level.playSound(null, sp.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.4F);
            if (!sp.isCreative()) {
                stack.shrink(1);
            }
            SpiritWorldManager.returnFromSpiritWorld(sp, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
