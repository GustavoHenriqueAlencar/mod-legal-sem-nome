package com.occultum.common.werewolf;

import com.mojang.serialization.MapCodec;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O Altar da Estátua do Lobisomem (Wolf Altar - Seção 3.1 e 3.2 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Se um humano comum (Nível 0) clicar na estátua: aplica Cegueira por 60 segundos (1.200 ticks).
 * - Processa todas as oferendas e missões do Nível 1 ao 10:
 *   * 1 -> 2: 3 Barras de Ouro -> entrega Amuleto da Lua (Moon Charm).
 *   * 2 -> 3: 30 Carnes de Carneiro Cruas (Raw Mutton).
 *   * 3 -> 4: 10 Línguas de Cão (Dog Tongue).
 *   * 4 -> 5: Entrega a Trompa da Caçada (Horn of the Hunt) e promove ao Nível 5 após abater o Horned Huntsman.
 *   * 5 -> 6: 10 Monstros Hostis abatidos à noite em forma de Homem-Lobo.
 *   * 6 -> 7: Uivar em 16 Chunks diferentes à noite.
 *   * 7 -> 8: Domesticar 6 Lobos na forma de Lobo quadrúpede.
 *   * 8 -> 9: Exterminar 30 Homens-Porco Zumbis no Nether em forma de Homem-Lobo.
 *   * 9 -> 10: Abater 1 Aldeão ou 1 Jogador Humano em forma lupina.
 * - Jogadores nível >= 2 podem trocar 3 Barras de Ouro adicionais a qualquer momento por um novo Amuleto da Lua.
 */
public class WolfAltarBlock extends Block {

    public static final MapCodec<WolfAltarBlock> CODEC = simpleCodec(WolfAltarBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public WolfAltarBlock(Properties properties) {
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        int lw = LycanthropySystem.getWerewolfLevel(player);

        // Humano comum (Nível 0): rejeitado como indigno e amaldiçoado com Cegueira por 60 segundos (1.200 ticks)
        if (lw <= 0) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1200, 0));
            level.playSound(null, pos, SoundEvents.WOLF_GROWL, SoundSource.BLOCKS, 1.2F, 0.6F);
            player.displayClientMessage(
                    Component.translatable("message.occultum.wolf_altar.unworthy_blindness").withStyle(ChatFormatting.DARK_RED),
                    false
            );
            return ItemInteractionResult.SUCCESS;
        }

        // Troca de 3 Barras de Ouro por novo Amuleto da Lua para lobisomens nível >= 2
        if (lw >= 2 && stack.is(Items.GOLD_INGOT) && stack.getCount() >= 3) {
            if (!player.isCreative()) {
                stack.shrink(3);
            }
            giveItem(player, new ItemStack(ModItems.MOON_CHARM.get()));
            level.playSound(null, pos, SoundEvents.WOLF_HOWL, SoundSource.BLOCKS, 1.0F, 1.1F);
            return ItemInteractionResult.SUCCESS;
        }

        int counter = player.getData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get());

        switch (lw) {
            case 1 -> {
                // 1 -> 2: Entregar 3 Barras de Ouro -> recebe Amuleto da Lua e sobe para Nível 2
                int added = consumeFromHandUpTo(player, stack, Items.GOLD_INGOT, 3 - counter);
                counter += added;
                player.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), counter);
                if (counter >= 3) {
                    LycanthropySystem.setWerewolfLevel(player, 2);
                    giveItem(player, new ItemStack(ModItems.MOON_CHARM.get()));
                    notifyPromotion(level, pos, player, 2);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest1", counter, 3).withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 2 -> {
                // 2 -> 3: Entregar 30 Carnes de Carneiro Cruas (Raw Mutton)
                int added = consumeFromHandUpTo(player, stack, Items.MUTTON, 30 - counter);
                counter += added;
                player.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), counter);
                if (counter >= 30) {
                    LycanthropySystem.setWerewolfLevel(player, 3);
                    notifyPromotion(level, pos, player, 3);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest2", counter, 30).withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 3 -> {
                // 3 -> 4: Entregar 10 Línguas de Cão (Dog Tongue)
                int added = consumeFromHandUpTo(player, stack, ModItems.DOG_TONGUE.get(), 10 - counter);
                counter += added;
                player.setData(ModAttachmentTypes.WEREWOLF_QUEST_COUNTER.get(), counter);
                if (counter >= 10) {
                    LycanthropySystem.setWerewolfLevel(player, 4);
                    notifyPromotion(level, pos, player, 4);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest3", counter, 10).withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 4 -> {
                // 4 -> 5: Entrega a Trompa da Caçada e verifica se já derrotou o Horned Huntsman
                if (counter >= 1) {
                    LycanthropySystem.setWerewolfLevel(player, 5);
                    notifyPromotion(level, pos, player, 5);
                } else {
                    if (!hasItem(player, ModItems.HORN_OF_THE_HUNT.get())) {
                        giveItem(player, new ItemStack(ModItems.HORN_OF_THE_HUNT.get()));
                    }
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest4").withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 5 -> {
                // 5 -> 6: 10 Monstros Hostis durante a noite no Overworld em forma de Homem-Lobo
                if (counter >= 10) {
                    LycanthropySystem.setWerewolfLevel(player, 6);
                    notifyPromotion(level, pos, player, 6);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest5", counter, 10).withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 6 -> {
                // 6 -> 7: Uivar em 16 Chunks diferentes
                if (counter >= 16) {
                    LycanthropySystem.setWerewolfLevel(player, 7);
                    notifyPromotion(level, pos, player, 7);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest6", counter, 16).withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 7 -> {
                // 7 -> 8: Domesticar 6 Lobos na forma de Lobo quadrúpede
                if (counter >= 6) {
                    LycanthropySystem.setWerewolfLevel(player, 8);
                    notifyPromotion(level, pos, player, 8);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest7", counter, 6).withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 8 -> {
                // 8 -> 9: Exterminar 30 Zombie Pigmen no Nether em forma de Homem-Lobo
                if (counter >= 30) {
                    LycanthropySystem.setWerewolfLevel(player, 9);
                    notifyPromotion(level, pos, player, 9);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest8", counter, 30).withStyle(ChatFormatting.GOLD), false);
                }
            }
            case 9 -> {
                // 9 -> 10: Abater 1 Aldeão ou 1 Jogador Humano em forma de lobo
                if (counter >= 1) {
                    LycanthropySystem.setWerewolfLevel(player, 10);
                    notifyPromotion(level, pos, player, 10);
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.wolf_altar.quest9").withStyle(ChatFormatting.GOLD), false);
                }
            }
            default -> player.displayClientMessage(
                    Component.translatable("message.occultum.wolf_altar.alpha_complete").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    false
            );
        }

        return ItemInteractionResult.SUCCESS;
    }

    private static int consumeFromHandUpTo(Player player, ItemStack stack, Item targetItem, int needed) {
        if (needed <= 0 || !stack.is(targetItem)) return 0;
        int take = Math.min(needed, stack.getCount());
        if (!player.isCreative()) {
            stack.shrink(take);
        }
        return take;
    }

    private static boolean hasItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) return true;
        }
        return false;
    }

    private static void giveItem(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void notifyPromotion(Level level, BlockPos pos, Player player, int newLevel) {
        level.playSound(null, pos, SoundEvents.WOLF_HOWL, SoundSource.BLOCKS, 1.5F, 0.9F);
        level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(
                Component.translatable("message.occultum.werewolf.level_up", newLevel).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                false
        );
    }
}
