package com.occultum.common.fetish;

import com.mojang.serialization.MapCodec;
import com.occultum.common.poppet.TaglockKitItem;
import com.occultum.common.registry.ModBlockEntities;
import com.occultum.common.registry.ModDataComponentTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Bloco para os 3 tipos de Fetiches (Seção 6 de 06_POPPETS_E_FETISHES.md):
 * - Espantalho de Fetiche (Scarecrow Fetish)
 * - Ídolo Trent (Trent Effigy)
 * - Escada da Bruxa (Witch's Ladder)
 */
public class FetishBlock extends BaseEntityBlock {
    public static final MapCodec<FetishBlock> CODEC = simpleCodec(p -> new FetishBlock(p, FetishType.SCARECROW));
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);

    public enum FetishType {
        SCARECROW,
        TRENT_EFFIGY,
        WITCHS_LADDER
    }

    private final FetishType fetishType;

    public FetishBlock(Properties properties, FetishType fetishType) {
        super(properties);
        this.fetishType = fetishType;
    }

    public FetishType getFetishType() {
        return fetishType;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FetishBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.FETISH_BE.get(), FetishBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FetishBlockEntity fetish) {
            if (placer instanceof Player p) {
                fetish.setOwnerUuid(p.getUUID().toString());
            }
            String effectStr = stack.get(ModDataComponentTypes.FETISH_SPIRIT_EFFECT.get());
            if (effectStr != null && !effectStr.isEmpty()) {
                try {
                    fetish.setSpiritEffect(FetishBlockEntity.SpiritEffect.valueOf(effectStr));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FetishBlockEntity fetish) {
            // Clicar com Kit Taglock vinculado adiciona/remove a assinatura da Lista Branca/Negra do fetiche
            if (stack.is(ModItems.TAGLOCK_KIT.get()) && TaglockKitItem.isFilled(stack)) {
                String sig = stack.get(ModDataComponentTypes.DNA_SIGNATURE.get());
                String name = stack.getOrDefault(ModDataComponentTypes.DNA_TARGET_NAME.get(), sig);
                fetish.toggleListedPlayer(player, sig, name);
                return ItemInteractionResult.SUCCESS;
            }
            // Clicar com Pedra Espectral cheia vincula ou altera o efeito de espírito diretamente
            if (stack.is(ModItems.SPECTRAL_STONE.get())) {
                String critters = stack.getOrDefault(ModDataComponentTypes.CAPTURED_CRITTER.get(), "spectre:3");
                FetishBlockEntity.SpiritEffect next = FetishBlockEntity.SpiritEffect.fromCapturedString(critters, fetish.getSpiritEffect().next());
                fetish.setSpiritEffect(next);
                return ItemInteractionResult.SUCCESS;
            }
            if (player.isShiftKeyDown()) {
                fetish.cycleSpiritEffect(player);
            } else {
                fetish.cycleOperationMode(player);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FetishBlockEntity fetish) {
            if (player.isShiftKeyDown()) {
                fetish.cycleSpiritEffect(player);
            } else {
                fetish.cycleOperationMode(player);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
