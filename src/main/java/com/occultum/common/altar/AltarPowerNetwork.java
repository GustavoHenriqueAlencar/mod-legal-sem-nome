package com.occultum.common.altar;

import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AltarPowerNetwork {
    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_ALTARS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<BlockPos>> VOID_BRAMBLES = new ConcurrentHashMap<>();

    public static void registerAltar(Level level, BlockPos pos) {
        ACTIVE_ALTARS.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void unregisterAltar(Level level, BlockPos pos) {
        Set<BlockPos> altars = ACTIVE_ALTARS.get(level.dimension());
        if (altars != null) {
            altars.remove(pos);
        }
    }

    public static void registerVoidBramble(Level level, BlockPos pos) {
        VOID_BRAMBLES.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    public static void unregisterVoidBramble(Level level, BlockPos pos) {
        Set<BlockPos> brambles = VOID_BRAMBLES.get(level.dimension());
        if (brambles != null) {
            brambles.remove(pos);
        }
    }

    public static boolean isSuppressedByVoidBramble(Level level, BlockPos pos) {
        Set<BlockPos> brambles = VOID_BRAMBLES.get(level.dimension());
        if (brambles == null || brambles.isEmpty()) return false;
        double maxDistSq = 32.0 * 32.0; // 1024
        for (BlockPos bPos : brambles) {
            if (bPos.distSqr(pos) <= maxDistSq) {
                // Verify the block is still Void Bramble
                if (level.getBlockState(bPos).is(ModBlocks.VOID_BRAMBLE.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tenta consumir energia mágica de um altar próximo em regime atômico (tudo ou nada).
     * @return true se a energia foi debitada com sucesso, false caso contrário.
     */
    public static boolean consumePower(Level level, BlockPos consumerPos, int amount) {
        return consumePower(level, consumerPos, (double) amount);
    }

    public static boolean tryConsumeNearestAltarPower(Level level, BlockPos consumerPos, int radiusIgnored, int amount) {
        return consumePower(level, consumerPos, (double) amount);
    }

    public static boolean consumePower(Level level, BlockPos consumerPos, double amount) {
        if (level.isClientSide) return false;
        if (amount <= 0.0D) return true;

        // Se o consumidor estiver dentro de 32 blocos de um Espinho do Vazio, falha imediatamente
        if (isSuppressedByVoidBramble(level, consumerPos)) {
            return false;
        }

        AltarBlockEntity altar = findNearestAvailableAltar(level, consumerPos, amount);
        if (altar != null) {
            return altar.tryConsumePower(amount);
        }
        return false;
    }

    public static AltarBlockEntity findNearestAvailableAltar(Level level, BlockPos consumerPos, int minPower) {
        return findNearestAvailableAltar(level, consumerPos, (double) minPower);
    }

    public static AltarBlockEntity findNearestAvailableAltar(Level level, BlockPos consumerPos, double minPower) {
        Set<BlockPos> altars = ACTIVE_ALTARS.get(level.dimension());
        if (altars == null || altars.isEmpty()) return null;

        List<AltarBlockEntity> candidateAltars = new ArrayList<>();
        Iterator<BlockPos> iterator = altars.iterator();

        while (iterator.hasNext()) {
            BlockPos altarPos = iterator.next();
            BlockEntity be = level.getBlockEntity(altarPos);
            if (be instanceof AltarBlockEntity altar && altar.isFormed()) {
                // Se o altar estiver dentro de 32 blocos de um Espinho do Vazio, ele não consegue operar
                if (isSuppressedByVoidBramble(level, altarPos)) {
                    continue;
                }

                double distSq = altarPos.distSqr(consumerPos);
                int maxRadius = 16 * altar.getRangeMultiplier();
                if (distSq <= (double) (maxRadius * maxRadius)) {
                    if (altar.getCurrentPowerExact() + 1e-6D >= minPower) {
                        candidateAltars.add(altar);
                    }
                }
            } else {
                iterator.remove();
            }
        }

        if (candidateAltars.isEmpty()) return null;

        // Ordena por distância quadrática ascendente e seleciona o mais próximo
        candidateAltars.sort(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(consumerPos)));
        return candidateAltars.get(0);
    }
}
