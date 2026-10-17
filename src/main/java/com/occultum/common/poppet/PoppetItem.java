package com.occultum.common.poppet;

import com.occultum.common.registry.ModDataComponentTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Catálogo Exaustivo dos 11 Poppets Místicos (1.000 pts de durabilidade) conforme a Seção 3 de 06_POPPETS_E_FETISHES.md:
 * - 1. Terra (Earth Protection)
 * - 2. Água (Water Protection)
 * - 3. Fogo (Fire Protection)
 * - 4. Fome (Hunger Protection)
 * - 5. Ferramenta (Tool Protection)
 * - 6. Morte (Death Protection)
 * - 7. Anti-Vodu (Voodoo Protection)
 * - 8. Vodu (Voodoo Poppet - com as 4 ações: alfinete, submersão em água, fogo/lava e arremesso cinético)
 * - 9. Vampírico (Vampiric Poppet - vínculo duplo)
 * - 10. Proteção de Poppets (Poppet Protection)
 * - 11. Armadura (Armor Protection)
 */
public class PoppetItem extends Item {

    public static final int MAX_POPPET_DURABILITY = 1000;

    public enum PoppetType {
        UNBOUND,
        EARTH_PROTECTION,
        WATER_PROTECTION,
        FIRE_PROTECTION,
        HUNGER_PROTECTION,
        TOOL_PROTECTION,
        DEATH_PROTECTION,
        VOODOO_PROTECTION,
        VOODOO,
        VAMPIRIC,
        POPPET_PROTECTION,
        ARMOR_PROTECTION
    }

    private final PoppetType poppetType;

    public PoppetItem(Properties properties, PoppetType poppetType) {
        super(properties.stacksTo(1).durability(MAX_POPPET_DURABILITY));
        this.poppetType = poppetType;
    }

    public PoppetType getPoppetType() {
        return poppetType;
    }

    public static boolean isBoundTo(ItemStack stack, Player player) {
        String sig = stack.get(ModDataComponentTypes.DNA_SIGNATURE.get());
        return sig != null && sig.equals(player.getUUID().toString());
    }

    /**
     * Se o jogador estiver segurando um Kit Taglock preenchido na mão secundária (offhand) e clicar
     * com botão direito segurando um Poppet, vincula o DNA ao Poppet (ou o 2º Taglock no Poppet Vampírico)
     * e devolve o Kit Taglock vazio limpo.
     * Caso seja um Poppet de Vodu já vinculado, executa a Ação 1 (Espetar com Alfinete: 4 dano perfurante, 25 pts).
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack poppetStack = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack offStack = player.getItemInHand(otherHand);

        // Vinculação rápida com Kit Taglock na outra mão
        if (offStack.is(ModItems.TAGLOCK_KIT.get()) && TaglockKitItem.isFilled(offStack)) {
            if (!level.isClientSide) {
                String sig = offStack.get(ModDataComponentTypes.DNA_SIGNATURE.get());
                String name = offStack.getOrDefault(ModDataComponentTypes.DNA_TARGET_NAME.get(), "Unknown");
                String primarySig = poppetStack.get(ModDataComponentTypes.DNA_SIGNATURE.get());

                if (poppetType == PoppetType.VAMPIRIC && primarySig != null && !primarySig.isEmpty()) {
                    poppetStack.set(ModDataComponentTypes.SECONDARY_DNA_SIGNATURE.get(), sig);
                    poppetStack.set(ModDataComponentTypes.SECONDARY_DNA_TARGET_NAME.get(), name);
                } else {
                    poppetStack.set(ModDataComponentTypes.DNA_SIGNATURE.get(), sig);
                    poppetStack.set(ModDataComponentTypes.DNA_TARGET_NAME.get(), name);
                }
                // Limpa o Kit Taglock para reuso (Seção 2.2)
                offStack.remove(ModDataComponentTypes.DNA_SIGNATURE.get());
                offStack.remove(ModDataComponentTypes.DNA_TARGET_NAME.get());
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            }
            return InteractionResultHolder.sidedSuccess(poppetStack, level.isClientSide);
        }

        // Ação 1 do Poppet de Vodu: Espetar com Alfinete (Clique Direito no Ar -> 4 dano perfurante, 25 durabilidade)
        if (poppetType == PoppetType.VOODOO && TaglockKitItem.isFilled(poppetStack)) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                LivingEntity victim = TaglockKitItem.resolveTarget(serverLevel.getServer(), poppetStack);
                if (victim != null) {
                    boolean catBonus = "cat".equalsIgnoreCase(player.getData(com.occultum.common.registry.ModAttachmentTypes.FAMILIAR_TYPE.get()));
                    boolean intercepted = victim instanceof Player vp
                            && PoppetHandler.tryInterceptVoodooOrCurse(serverLevel.getServer(), vp, 75, catBonus);
                    if (!intercepted) {
                        victim.hurt(serverLevel.damageSources().fellOutOfWorld(), 4.0F + (catBonus ? 2.0F : 0.0F));
                        victim.knockback(0.5D, player.getX() - victim.getX(), player.getZ() - victim.getZ());
                    }
                    PoppetHandler.consumePoppetDurability(poppetStack, 25, serverLevel, player.blockPosition());
                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, SoundSource.PLAYERS, 1.0F, 0.9F);
                }
            }
            return InteractionResultHolder.sidedSuccess(poppetStack, level.isClientSide);
        }

        return InteractionResultHolder.pass(poppetStack);
    }

    /**
     * Ações 3 e 4 do Poppet de Vodu (Seção 3.2):
     * - Ação 3 (Imersão em Fogo/Lava/Fogueira): 10s de chamas na vítima, consome 50 pts.
     * - Ação 4 (Arremesso Cinético mirando no chão): arremessa a vítima a 6 blocos de altura, consome 40 pts.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (poppetType != PoppetType.VOODOO || !TaglockKitItem.isFilled(stack)) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && player != null) {
            LivingEntity victim = TaglockKitItem.resolveTarget(serverLevel.getServer(), stack);
            if (victim == null) return InteractionResult.FAIL;

            boolean catBonus = "cat".equalsIgnoreCase(player.getData(com.occultum.common.registry.ModAttachmentTypes.FAMILIAR_TYPE.get()));
            boolean intercepted = victim instanceof Player vp
                    && PoppetHandler.tryInterceptVoodooOrCurse(serverLevel.getServer(), vp, 75, catBonus);

            BlockState clickedState = level.getBlockState(pos);
            boolean isFireBlock = clickedState.is(BlockTags.FIRE) || clickedState.is(BlockTags.CAMPFIRES)
                    || clickedState.is(Blocks.LAVA) || clickedState.is(Blocks.MAGMA_BLOCK)
                    || level.getFluidState(pos).is(FluidTags.LAVA);

            if (isFireBlock) {
                // Ação 3: Imersão em Fogo / Lava -> 10s de chamas, 50 pts
                if (!intercepted) {
                    victim.igniteForSeconds(10.0F);
                }
                PoppetHandler.consumePoppetDurability(stack, 50, serverLevel, pos);
                serverLevel.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                // Ação 4: Arremesso Cinético -> arremessa a vítima a 6 blocos de altura (vy ~ 1.15), 40 pts
                if (!intercepted) {
                    victim.setDeltaMovement(victim.getDeltaMovement().x, 1.15D, victim.getDeltaMovement().z);
                    victim.hurtMarked = true;
                }
                PoppetHandler.consumePoppetDurability(stack, 40, serverLevel, pos);
                serverLevel.playSound(null, pos, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Ação 2 do Poppet de Vodu (Submersão em Água):
     * Se o portador segurar o Poppet debaixo d'água enquanto estiver agachado, a vítima vinculada
     * começa a se afogar instantaneamente em terra seca. Consome 20 pts de durabilidade por segundo.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;
        if (poppetType == PoppetType.VOODOO && isSelected && entity instanceof Player holder) {
            if (holder.isUnderWater() && holder.isShiftKeyDown() && level.getGameTime() % 20 == 0 && TaglockKitItem.isFilled(stack)) {
                LivingEntity victim = TaglockKitItem.resolveTarget(serverLevel.getServer(), stack);
                if (victim != null) {
                    boolean catBonus = "cat".equalsIgnoreCase(holder.getData(com.occultum.common.registry.ModAttachmentTypes.FAMILIAR_TYPE.get()));
                    boolean intercepted = victim instanceof Player vp
                            && PoppetHandler.tryInterceptVoodooOrCurse(serverLevel.getServer(), vp, 50, catBonus);
                    if (!intercepted) {
                        victim.setAirSupply(Math.max(-20, victim.getAirSupply() - 90));
                        victim.hurt(serverLevel.damageSources().drown(), 2.0F);
                    }
                    PoppetHandler.consumePoppetDurability(stack, 20, serverLevel, holder.blockPosition());
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String name = stack.get(ModDataComponentTypes.DNA_TARGET_NAME.get());
        if (name != null && !name.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.occultum.poppet.bound_primary", name).withStyle(ChatFormatting.GOLD));
        }
        if (poppetType == PoppetType.VAMPIRIC) {
            String victimName = stack.get(ModDataComponentTypes.SECONDARY_DNA_TARGET_NAME.get());
            if (victimName != null && !victimName.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.occultum.poppet.bound_victim", victimName).withStyle(ChatFormatting.DARK_RED));
            }
        }
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("tooltip.occultum.poppet.durability", remaining, stack.getMaxDamage()).withStyle(ChatFormatting.GRAY));
    }
}
