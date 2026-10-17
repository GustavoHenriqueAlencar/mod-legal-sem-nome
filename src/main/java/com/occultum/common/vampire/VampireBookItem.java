package com.occultum.common.vampire;

import com.occultum.common.network.OccultumNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Observações de um Imortal (Livro dos Vampiros - Seção 2.2 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Teto inicial: Nível 3.
 * - Ao usar o livro enquanto possui Páginas do Livro dos Vampiros no inventário, encaderna e lê a página
 *   (níveis de conhecimento 0 a 9), elevando permanentemente o teto de evolução para página + 1 (até o Nível 10).
 * - Clicar agachado alterna a habilidade vampírica ativa (DRINK, MESMERIZE, SPEED, BAT, ULTIMATE).
 */
public class VampireBookItem extends Item {

    public VampireBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (player.isShiftKeyDown()) {
            VampirismSystem.cycleSelectedPower(player);
            return InteractionResultHolder.consume(stack);
        }

        int currentCap = player.getData(ModAttachmentTypes.VAMPIRE_LEVEL_CAP.get());
        if (currentCap < 10) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack slotStack = player.getInventory().getItem(i);
                if (slotStack.is(ModItems.VAMPIRE_BOOK_PAGE.get())) {
                    if (!player.isCreative()) {
                        slotStack.shrink(1);
                    }
                    int newCap = Math.min(10, currentCap + 1);
                    player.setData(ModAttachmentTypes.VAMPIRE_LEVEL_CAP.get(), newCap);
                    OccultumNetwork.syncToClient(player);
                    level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.displayClientMessage(
                            Component.translatable("message.occultum.vampire.book_page_read", newCap).withStyle(ChatFormatting.GOLD),
                            false
                    );
                    return InteractionResultHolder.consume(stack);
                }
            }
        }

        int lv = VampirismSystem.getVampireLevel(player);
        int blood = VampirismSystem.getBlood(player);
        int maxBlood = VampirismSystem.getMaxBlood(player);
        player.displayClientMessage(
                Component.translatable("message.occultum.vampire.book_status", lv, currentCap, blood, maxBlood).withStyle(ChatFormatting.DARK_RED),
                false
        );
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.occultum.vampire_book").withStyle(ChatFormatting.GRAY));
    }
}
