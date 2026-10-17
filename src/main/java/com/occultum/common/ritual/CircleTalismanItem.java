package com.occultum.common.ritual;

import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModDataComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Seção 3.5 de 05_MAGIA_DE_CIRCULO_E_RITUAIS.md:
 * Talismã de Círculo (Circle Talisman) que armazena a matriz geométrica de círculos absorvidos
 * (Rituais #01 e #45) e estampa instantaneamente todos os círculos em outro terreno plano.
 */
public class CircleTalismanItem extends Item {

    public CircleTalismanItem(Properties properties) {
        super(properties);
    }

    public static int getStoredMask(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.CIRCLE_TALISMAN_MASK.get(), 0);
    }

    public static void setStoredMask(ItemStack stack, int mask) {
        if (mask <= 0) {
            stack.remove(ModDataComponentTypes.CIRCLE_TALISMAN_MASK.get());
        } else {
            stack.set(ModDataComponentTypes.CIRCLE_TALISMAN_MASK.get(), mask);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getStoredMask(stack) > 0;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        int mask = getStoredMask(stack);
        if (mask <= 0) {
            return InteractionResult.PASS;
        }

        BlockPos clicked = context.getClickedPos();
        BlockPos center = level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.above();

        if (!level.isClientSide) {
            CircleGeometryValidator.DetectedRings rings = CircleGeometryValidator.DetectedRings.unpackMask(mask);
            // Estampa o Glifo Dourado central e todos os anéis armazenados
            if (level.getBlockState(center).isAir() || level.getBlockState(center).canBeReplaced()) {
                level.setBlock(center, ModBlocks.GOLDEN_CHALK_BLOCK.get().defaultBlockState(), 3);
            }
            if (rings.small() != CircleGeometryValidator.ChalkColor.NONE) {
                CircleGeometryValidator.stampRing(level, center, CircleGeometryValidator.SMALL_RING_OFFSETS, rings.small());
            }
            if (rings.medium() != CircleGeometryValidator.ChalkColor.NONE) {
                CircleGeometryValidator.stampRing(level, center, CircleGeometryValidator.MEDIUM_RING_OFFSETS, rings.medium());
            }
            if (rings.large() != CircleGeometryValidator.ChalkColor.NONE) {
                CircleGeometryValidator.stampRing(level, center, CircleGeometryValidator.LARGE_RING_OFFSETS, rings.large());
            }
            level.playSound(null, center, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.1F);
            Player player = context.getPlayer();
            if (player == null || !player.isCreative()) {
                setStoredMask(stack, 0);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int mask = getStoredMask(stack);
        if (mask <= 0) {
            tooltip.add(Component.translatable("tooltip.occultum.circle_talisman.empty").withStyle(ChatFormatting.GRAY));
        } else {
            CircleGeometryValidator.DetectedRings rings = CircleGeometryValidator.DetectedRings.unpackMask(mask);
            tooltip.add(Component.translatable("tooltip.occultum.circle_talisman.stored",
                    rings.small().name(), rings.medium().name(), rings.large().name()).withStyle(ChatFormatting.GOLD));
        }
    }
}
