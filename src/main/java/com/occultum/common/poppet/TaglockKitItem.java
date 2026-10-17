package com.occultum.common.poppet;

import com.occultum.common.botany.wild.BloodRoseBlockEntity;
import com.occultum.common.botany.wild.LeechChestBlockEntity;
import com.occultum.common.registry.ModDataComponentTypes;
import com.occultum.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kit Taglock (Seção 2 de 06_POPPETS_E_FETISHES.md):
 * - Coleta furtiva em criaturas/jogadores com cálculo exato de ângulo de abordagem pelas costas (>120° e <240°),
 *   bônus agachado (60% costas / 10% frente) vs. em pé (30% costas / 1% frente) e +50% com Invisibilidade.
 * - Coleta em camas/caixões (e auto-coleta agachado na cama).
 * - Coleta no Baú Sanguessuga (Leech Chest) e Rosa de Sangue (Blood Rose).
 */
public class TaglockKitItem extends Item {

    private static final Map<BlockPos, SleepRecord> BED_SLEEP_RECORDS = new ConcurrentHashMap<>();

    public record SleepRecord(String uuid, String name) {}

    public TaglockKitItem(Properties properties) {
        super(properties);
    }

    public static void recordBedSleep(BlockPos bedPos, Player player) {
        BED_SLEEP_RECORDS.put(bedPos.immutable(), new SleepRecord(player.getUUID().toString(), player.getName().getString()));
    }

    public static boolean isFilled(ItemStack stack) {
        String sig = stack.get(ModDataComponentTypes.DNA_SIGNATURE.get());
        return sig != null && !sig.isEmpty();
    }

    public static ItemStack createFilledTaglock(String uuidString, String entityName) {
        ItemStack stack = new ItemStack(ModItems.TAGLOCK_KIT.get());
        stack.set(ModDataComponentTypes.DNA_SIGNATURE.get(), uuidString);
        stack.set(ModDataComponentTypes.DNA_TARGET_NAME.get(), entityName);
        return stack;
    }

    @Nullable
    public static LivingEntity resolveTarget(@Nullable MinecraftServer server, ItemStack stack) {
        if (server == null) return null;
        String sig = stack.get(ModDataComponentTypes.DNA_SIGNATURE.get());
        if (sig == null || sig.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(sig);
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) return player;
            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(uuid);
                if (e instanceof LivingEntity living && living.isAlive()) {
                    return living;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player collector, LivingEntity target, InteractionHand hand) {
        Level level = collector.level();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Cálculo furtivo para alvos jogadores humanos (Seção 2.1.A)
        if (target instanceof Player targetPlayer) {
            double approachAngle = computeRelativeApproachAngleDegrees(collector, targetPlayer);
            boolean fromBehind = approachAngle > 120.0D && approachAngle < 240.0D;
            boolean sneaking = collector.isShiftKeyDown();

            double stealthChance;
            if (fromBehind) {
                stealthChance = sneaking ? 0.60D : 0.30D;
            } else {
                stealthChance = sneaking ? 0.10D : 0.01D;
            }
            if (collector.hasEffect(MobEffects.INVISIBILITY)) {
                stealthChance += 0.50D;
            }

            boolean stealthSuccess = level.random.nextDouble() < Math.min(1.0D, stealthChance);
            if (!stealthSuccess) {
                // Falha / Detecção: som estridente de tesoura e mensagem de alerta para a vítima
                level.playSound(null, targetPlayer.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.4F);
                targetPlayer.displayClientMessage(
                        Component.translatable("message.occultum.taglock.detected").withStyle(ChatFormatting.RED),
                        false
                );
                collector.displayClientMessage(
                        Component.translatable("message.occultum.taglock.failed").withStyle(ChatFormatting.YELLOW),
                        true
                );
                return InteractionResult.FAIL;
            }
        }

        giveFilledTaglock(collector, hand, stack, target.getUUID().toString(), target.getName().getString());
        return InteractionResult.CONSUME;
    }

    /**
     * Calcula o ângulo em graus [0..360) entre o vetor de olhar horizontal do alvo e o vetor
     * que aponta do alvo para o coletor. Quando o coletor está exatamente atrás das costas do alvo,
     * o ângulo é 180° (dentro do arco > 120° e < 240°).
     */
    public static double computeRelativeApproachAngleDegrees(Player collector, LivingEntity target) {
        Vec3 targetLook = Vec3.directionFromRotation(0.0F, target.getYHeadRot()).normalize();
        Vec3 toCollector = new Vec3(collector.getX() - target.getX(), 0.0D, collector.getZ() - target.getZ());
        if (toCollector.lengthSqr() < 1.0E-5D) {
            return 0.0D;
        }
        toCollector = toCollector.normalize();
        double dot = Math.max(-1.0D, Math.min(1.0D, targetLook.dot(toCollector)));
        double angleDeg = Math.toDegrees(Math.acos(dot)); // 0 (frente) a 180 (costas)
        // Mapeia simetricamente no intervalo [0, 360) onde 180 é exatamente atrás
        double crossY = targetLook.x * toCollector.z - targetLook.z * toCollector.x;
        return crossY >= 0 ? angleDeg : (360.0D - angleDeg);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        InteractionHand hand = context.getHand();
        BlockState state = level.getBlockState(pos);

        // B. Coleta em Leitos de Sono (Camas) e Auto-Coleta agachado
        if (state.is(BlockTags.BEDS)) {
            if (!level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    // Auto-Coleta agachado na cama
                    giveFilledTaglock(player, hand, stack, player.getUUID().toString(), player.getName().getString());
                    level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.7F, 1.2F);
                    return InteractionResult.SUCCESS;
                }
                BlockPos headPos = pos;
                if (state.getBlock() instanceof BedBlock) {
                    Direction dir = BedBlock.getConnectedDirection(state);
                    SleepRecord rec = BED_SLEEP_RECORDS.get(pos);
                    if (rec == null && dir != null) {
                        rec = BED_SLEEP_RECORDS.get(pos.relative(dir));
                    }
                    if (rec != null) {
                        giveFilledTaglock(player, hand, stack, rec.uuid(), rec.name());
                        level.playSound(null, headPos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.7F, 1.2F);
                        return InteractionResult.SUCCESS;
                    }
                }
                // Caso ninguém tenha dormido na cama ainda, coleta do próprio jogador
                giveFilledTaglock(player, hand, stack, player.getUUID().toString(), player.getName().getString());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // C. Coleta no Baú Sanguessuga (Leech Chest)
        if (level.getBlockEntity(pos) instanceof LeechChestBlockEntity chest) {
            if (!level.isClientSide) {
                if (!chest.getLastVictimSignature().isEmpty()) {
                    giveFilledTaglock(player, hand, stack, chest.getLastVictimSignature(), chest.getLastVictimName());
                    chest.clearVictimSample();
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // D. Coleta na Rosa de Sangue (Blood Rose)
        if (level.getBlockEntity(pos) instanceof BloodRoseBlockEntity rose) {
            if (!level.isClientSide && !rose.getLastVictimSignature().isEmpty()) {
                giveFilledTaglock(player, hand, stack, rose.getLastVictimSignature(), rose.getLastVictimName());
                rose.clearVictimSample();
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.1F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    private static void giveFilledTaglock(Player player, InteractionHand hand, ItemStack stack, String uuid, String name) {
        ItemStack filled = createFilledTaglock(uuid, name);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            player.setItemInHand(hand, filled);
        } else if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isFilled(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String name = stack.get(ModDataComponentTypes.DNA_TARGET_NAME.get());
        String sig = stack.get(ModDataComponentTypes.DNA_SIGNATURE.get());
        if (name != null && !name.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.occultum.taglock.bound", name).withStyle(ChatFormatting.RED));
        } else if (sig != null && !sig.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.occultum.taglock.bound", sig).withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("tooltip.occultum.taglock.empty").withStyle(ChatFormatting.GRAY));
        }
    }
}
