package com.occultum.common.vampire;

import com.occultum.common.entity.ElleEntity;
import com.occultum.common.entity.VampireServantEntity;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.registry.ModItems;
import com.occultum.common.registry.ModMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Cálice de Vidro (Wine Glass / Glass Goblet - Seção 2.3 de 08_VAMPIRISMO_E_LICANTROPIA.md):
 * - Cálice vazio: sacrifica Galinha para colher Sangue de Galinha; ou em Nível >= 9 sangra a si próprio
 *   (transfere 125 unidades de sangue do vampiro para o cálice, criando Sangue Vampírico Puro).
 * - Cálice com Sangue de Galinha: clicado à noite a céu aberto no Crânio central da estrutura 7x7,
 *   dispara um raio e invoca Elle (a Mensageira de Lilith).
 * - Cálice com Sangue de Lilith: ao ser bebido, desperta o Vampirismo (Nível 0 -> 1) ou cura o vampiro.
 * - Cálice com Sangue Vampírico Puro: clicado em um Aldeão/Guarda hipnotizado (Mesmerize 5) com 0 de sangue
 *   a menos de 4 blocos de um Caixão, cria a Prole Vampírica e promove o vampiro ao Nível 10 (O Progenitor).
 */
public class GlassGobletItem extends Item {

    public enum GobletFluid {
        EMPTY,
        CHICKEN_BLOOD,
        LILITH_BLOOD,
        VAMPIRE_BLOOD
    }

    private final GobletFluid fluid;

    public GlassGobletItem(GobletFluid fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
    }

    public GobletFluid getFluid() {
        return fluid;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return (fluid == GobletFluid.LILITH_BLOOD || fluid == GobletFluid.VAMPIRE_BLOOD) ? 32 : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return (fluid == GobletFluid.LILITH_BLOOD || fluid == GobletFluid.VAMPIRE_BLOOD) ? UseAnim.DRINK : UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (fluid == GobletFluid.LILITH_BLOOD || fluid == GobletFluid.VAMPIRE_BLOOD) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        // Nível >= 9 (Soberania do Sangue): sangrar a si próprio transferindo 125 unidades de sangue para o cálice
        if (fluid == GobletFluid.EMPTY && VampirismSystem.getVampireLevel(player) >= 9) {
            if (VampirismSystem.getBlood(player) >= 125) {
                if (!level.isClientSide) {
                    VampirismSystem.addBlood(player, -125);
                    level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 0.9F);
                }
                return InteractionResultHolder.sidedSuccess(replaceGoblet(player, stack, new ItemStack(ModItems.GLASS_GOBLET_VAMPIRE_BLOOD.get())), level.isClientSide);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (fluid == GobletFluid.LILITH_BLOOD) {
                // O Despertar (0 -> 1): torna-se Vampiro nível 1 com 125 de sangue inicial
                if (VampirismSystem.getVampireLevel(player) == 0) {
                    VampirismSystem.setVampireLevel(player, 1);
                    player.displayClientMessage(Component.translatable("message.occultum.vampire.awakened").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
                } else {
                    VampirismSystem.addBlood(player, 500);
                }
            } else if (fluid == GobletFluid.VAMPIRE_BLOOD) {
                if (VampirismSystem.getVampireLevel(player) == 0) {
                    VampirismSystem.setVampireLevel(player, 1);
                } else {
                    VampirismSystem.addBlood(player, 125);
                }
            }
        }
        if (entity instanceof Player player) {
            return replaceGoblet(player, stack, new ItemStack(ModItems.GLASS_GOBLET.get()));
        }
        return new ItemStack(ModItems.GLASS_GOBLET.get());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (fluid == GobletFluid.EMPTY && target instanceof Chicken chicken && chicken.isAlive()) {
            if (!level.isClientSide) {
                chicken.hurt(level.damageSources().playerAttack(player), 100.0F);
                level.playSound(null, chicken.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.setItemInHand(hand, replaceGoblet(player, stack, new ItemStack(ModItems.GLASS_GOBLET_CHICKEN_BLOOD.get())));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Missão 9 -> 10 (O Progenitor - A Criação da Prole):
        // 1. Caixão a menos de 4 blocos da vítima (Aldeão ou Guarda)
        // 2. Vítima hipnotizada com Mesmerize nível 5 (amplificador >= 4)
        // 3. Vítima com 0 unidades de sangue
        // 4. Clicar com Cálice de Sangue Vampírico Puro
        if (fluid == GobletFluid.VAMPIRE_BLOOD && (target instanceof Villager || target instanceof IronGolem)) {
            if (!level.isClientSide && level instanceof ServerLevel sl) {
                int targetBlood = target.getPersistentData().contains("OccultumVillagerBlood")
                        ? target.getPersistentData().getInt("OccultumVillagerBlood")
                        : 500;
                var paralysis = target.getEffect(ModMobEffects.PARALYSIS);
                boolean mesmerizedLv5 = paralysis != null && paralysis.getAmplifier() >= 4;
                boolean nearCoffin = isNearCoffin(sl, target.blockPosition(), 4);

                if (nearCoffin && mesmerizedLv5 && targetBlood == 0) {
                    VampireServantEntity progeny = ModEntityTypes.VAMPIRE_SERVANT.get().create(sl);
                    if (progeny != null) {
                        progeny.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                        progeny.setOwnerUUID(player.getUUID());
                        sl.addFreshEntity(progeny);
                    }
                    target.discard();
                    sl.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0, target.getZ(), 35, 0.4, 0.6, 0.4, 0.05);
                    sl.playSound(null, target.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8F, 1.2F);

                    if (VampirismSystem.getVampireLevel(player) == 9) {
                        VampirismSystem.tryAdvanceVampireLevel(player, 10);
                    }
                    player.setItemInHand(hand, replaceGoblet(player, stack, new ItemStack(ModItems.GLASS_GOBLET.get())));
                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(Component.translatable("message.occultum.vampire.progeny_invalid").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static boolean isNearCoffin(ServerLevel level, BlockPos pos, int radius) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-radius, -2, -radius), pos.offset(radius, 2, radius))) {
            if (level.getBlockState(p).is(ModBlocks.COFFIN.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();

        // Ritual 0 -> 1: Clicar no crânio da estrutura 7x7 à noite a céu aberto no Overworld com Sangue de Galinha
        if (fluid == GobletFluid.CHICKEN_BLOOD && level instanceof ServerLevel sl) {
            if (sl.dimension() == Level.OVERWORLD && sl.isNight() && sl.canSeeSky(pos)
                    && VampirismSystem.validateSkullAltar7x7(sl, pos)) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                    bolt.setVisualOnly(true);
                    sl.addFreshEntity(bolt);
                }
                ElleEntity elle = ModEntityTypes.ELLE.get().create(sl);
                if (elle != null) {
                    elle.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.0F, 0.0F);
                    elle.setSummonerUUID(player.getUUID());
                    sl.addFreshEntity(elle);
                }
                player.setItemInHand(context.getHand(), replaceGoblet(player, stack, new ItemStack(ModItems.GLASS_GOBLET.get())));
                player.displayClientMessage(Component.translatable("message.occultum.vampire.elle_summoned").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static ItemStack replaceGoblet(Player player, ItemStack current, ItemStack result) {
        if (!player.isCreative()) {
            current.shrink(1);
        }
        if (current.isEmpty()) {
            return result;
        }
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
        return current;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.occultum.goblet." + fluid.name().toLowerCase()).withStyle(ChatFormatting.DARK_RED));
    }
}
