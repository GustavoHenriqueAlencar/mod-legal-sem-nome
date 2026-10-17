package com.occultum.common.vampire;

import com.mojang.serialization.MapCodec;
import com.occultum.common.network.OccultumNetwork;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Crisol de Sangue (Blood Crucible - Seção 2.4 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Acumula até 20 níveis de essência vital concentrada.
 * - Com 20/20 níveis e jogador Vampiro Nível 10:
 *   * Alcachofra Glacial / Globo de Alcachofra d'Água -> Sintoniza STORM (Tempestade Sombria)
 *   * Lã de Morcego (Wool of Bat) -> Sintoniza SWARM (Enxame Predador)
 *   * Sela (Saddle) -> Sintoniza FARM (Retorno ao Rebanho)
 */
public class BloodCrucibleBlock extends BaseEntityBlock {

    public static final MapCodec<BloodCrucibleBlock> CODEC = simpleCodec(BloodCrucibleBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    public BloodCrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodCrucibleBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BloodCrucibleBlockEntity crucible)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        // Depositar sangue no Crisol (Cálice cheio ou drenando 100 de sangue próprio do vampiro)
        if (stack.is(ModItems.GLASS_GOBLET_VAMPIRE_BLOOD.get()) || stack.is(ModItems.GLASS_GOBLET_CHICKEN_BLOOD.get())
                || stack.is(ModItems.DEMONIC_BLOOD.get())) {
            if (crucible.getBloodLevels() < 20) {
                crucible.setBloodLevels(Math.min(20, crucible.getBloodLevels() + 4));
                if (!player.isCreative()) {
                    if (stack.getItem() instanceof GlassGobletItem) {
                        player.setItemInHand(hand, new ItemStack(ModItems.GLASS_GOBLET.get()));
                    } else {
                        stack.shrink(1);
                    }
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("message.occultum.crucible.level", crucible.getBloodLevels()), true);
                return ItemInteractionResult.SUCCESS;
            }
        }

        // Sintonização de Poder Supremo (Nível 10 com Crisol 20/20)
        if (crucible.getBloodLevels() >= 20 && VampirismSystem.getVampireLevel(player) >= 10) {
            String attuned = null;
            if (stack.is(ModItems.WATER_ARTICHOKE.get()) || stack.is(ModItems.ICY_NEEDLE.get())) {
                attuned = "STORM";
            } else if (stack.is(ModItems.WOOL_OF_BAT.get())) {
                attuned = "SWARM";
            } else if (stack.is(Items.SADDLE)) {
                attuned = "FARM";
            }

            if (attuned != null) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                crucible.setBloodLevels(0);
                player.setData(ModAttachmentTypes.VAMPIRE_ULTIMATE_TYPE.get(), attuned);
                int charges = player.getData(ModAttachmentTypes.VAMPIRE_ULTIMATE_CHARGES.get()) + 3;
                player.setData(ModAttachmentTypes.VAMPIRE_ULTIMATE_CHARGES.get(), charges);
                player.setData(ModAttachmentTypes.VAMPIRE_SELECTED_POWER.get(), "ULTIMATE");
                OccultumNetwork.syncToClient(player);
                level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8F, 1.2F);
                player.displayClientMessage(
                        Component.translatable("message.occultum.crucible.attuned", attuned).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                        false
                );
                return ItemInteractionResult.SUCCESS;
            }
        }

        // Clique com mão vazia pelo vampiro transfere 100 de sangue para +2 níveis no crisol
        if (stack.isEmpty() && VampirismSystem.getVampireLevel(player) >= 1) {
            if (crucible.getBloodLevels() < 20 && VampirismSystem.getBlood(player) >= 100) {
                VampirismSystem.addBlood(player, -100);
                crucible.setBloodLevels(Math.min(20, crucible.getBloodLevels() + 2));
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.8F, 0.9F);
                player.displayClientMessage(Component.translatable("message.occultum.crucible.level", crucible.getBloodLevels()), true);
                return ItemInteractionResult.SUCCESS;
            }
        }

        player.displayClientMessage(Component.translatable("message.occultum.crucible.level", crucible.getBloodLevels()), true);
        return ItemInteractionResult.SUCCESS;
    }
}
