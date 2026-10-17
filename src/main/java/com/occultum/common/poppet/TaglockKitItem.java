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
    public static UUID getBoundEntityUUID(ItemStack stack) {
        String sig = stack.get(ModDataComponentTypes.DNA_SIGNATURE.get());
        if (sig == null || sig.isEmpty()) return null;
        try {
            return UUID.fromString(sig);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Nullable
    public static String getBoundEntityName(ItemStack stack) {
        String name = stack.get(ModDataComponentTypes.DNA_TARGET_NAME.get());
        return (name == null || name.isEmpty()) ? null : name;
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

        // Cálculo furtivo para alvos jogadores humanos (Seção 2.1.A de 06_POPPETS_E_FETISHES.md)
        if (target instanceof Player targetPlayer) {
            double stealthChance = calculateStealthChance(collector, targetPlayer);
            boolean stealthSuccess = level.random.nextDouble() < stealthChance;
            if (!stealthSuccess) {
                // Falha / Detecção: som estridente de corte metálico de tesouras e mensagem de alerta no chat
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
     * Calcula a probabilidade exata [0.0..1.0] de sucesso furtivo conforme a Seção 2.1.A:
     * - Costas (ângulo entre 120° e 240° em relação à visão do alvo): 60% agachado, 30% em pé.
     * - Frontal ou lateral: 10% agachado, 1% em pé.
     * - Bônus de Invisibilidade: +50% na chance final.
     */
    public static double calculateStealthChance(Player collector, LivingEntity target) {
        double approachAngle = computeRelativeApproachAngleDegrees(collector, target);
        boolean fromBehind = approachAngle >= 120.0D && approachAngle <= 240.0D;
        boolean sneaking = collector.isCrouching() || collector.isShiftKeyDown();

        double stealthChance;
        if (fromBehind) {
            stealthChance = sneaking ? 0.60D : 0.30D;
        } else {
            stealthChance = sneaking ? 0.10D : 0.01D;
        }
        if (collector.hasEffect(MobEffects.INVISIBILITY) || collector.isInvisible()) {
            stealthChance += 0.50D;
        }
        return Math.min(1.0D, stealthChance);
    }

    /**
     * Calcula o ângulo horizontal em graus [0..360) entre a direção da visão do alvo (YRot/YHeadRot)
     * e o vetor que aponta do alvo para o coletor.
     * - 0° = coletor diretamente na frente do rosto do alvo.
     * - 180° = coletor exatamente atrás das costas do alvo (arco traseiro: 120° a 240°).
     */
    public static double computeRelativeApproachAngleDegrees(Player collector, LivingEntity target) {
        double dx = collector.getX() - target.getX();
        double dz = collector.getZ() - target.getZ();
        if (dx * dx + dz * dz < 1.0E-6D) {
            return 0.0D;
        }
        // Em Minecraft: yaw 0 = +Z (Sul), yaw 90 = -X (Oeste), yaw 180 = -Z (Norte), yaw 270 = +X (Leste)
        double angleToCollectorDeg = Math.toDegrees(Math.atan2(-dx, dz));
        double targetYawDeg = target.getYHeadRot();
        double diff = (angleToCollectorDeg - targetYawDeg) % 360.0D;
        if (diff < 0.0D) {
            diff += 360.0D;
        }
        return diff;
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
