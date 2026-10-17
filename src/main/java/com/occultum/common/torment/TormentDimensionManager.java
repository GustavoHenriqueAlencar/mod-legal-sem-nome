package com.occultum.common.torment;

import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Gerador Procedural da Dimensão do Tormento (Seção 1 de `14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md`):
 * - 6 andares indexados de 0 a 5, com altura base `Y_base = 10 + nivel * 15` (Nível 0: Y=10..16, ..., Nível 5: Y=85..91).
 * - Ponto de spawn do jogador no andar: `X = 8.0`, `Y = 12 + nivel * 15`, `Z = 8.0`, com área segura de 3x3 limpa.
 * - Algoritmo Randomized Depth-First Search (Backtracker) em grade de 31x31 células:
 *   * Paredes de 6 blocos de altura de Força Mágica inquebrável (`FORCE_BLOCK`).
 *   * Piso e Teto de Pedra do Tormento (`TORMENT_STONE`).
 *   * Em cada célula de passagem do piso, 1% de chance determinística de gerar Areia das Almas (`SOUL_SAND`).
 * - 5 Câmaras Especiais do Andar:
 *   1. Câmara de Entrada (Sul): 7x7 centrada na entrada.
 *   2. Câmara de Fim de Corredor (Norte): 7x9 contendo o Portal do Tormento e a Arena (`X = 9.0, Z = 36.0`).
 *   3. Câmara Lateral Esquerda: 5x5 na metade da profundidade com deslocamento -5..+5 em Z, com Baú Reabastecedor.
 *   4. Câmara Lateral Direita: 5x5 simetricamente oposta com deslocamento -5..+5 em Z, com Baú Reabastecedor.
 *   5. Câmara Central do Labirinto: 7x7 em (X: 15, Z: 15), com o 3º Baú Reabastecedor.
 */
public final class TormentDimensionManager {

    private static final int GRID_SIZE = 31;

    private TormentDimensionManager() {}

    /**
     * Envia um jogador para um andar específico (0 a 5) da Dimensão do Tormento,
     * gerando proceduralmente o labirinto do andar e limpando a área segura 3x3 de entrada.
     */
    public static void sendPlayerToTorment(ServerPlayer player) {
        int randomFloor = player.getRandom().nextInt(6);
        sendPlayerToTorment(player, randomFloor);
    }

    public static void sendPlayerToTorment(ServerPlayer player, int floorIndex) {
        int floor = floorIndex < 0 ? player.getRandom().nextInt(6) : Math.max(0, Math.min(5, floorIndex));
        ServerLevel tormentLevel = player.server.getLevel(OccultumDimensions.TORMENT_LEVEL);
        if (tormentLevel == null) return;

        player.setData(ModAttachmentTypes.TORMENT_FLOOR.get(), floor);

        // Gera o labirinto completo do andar se ainda não estiver construído
        int yBase = 10 + floor * 15;
        BlockPos checkFloor = new BlockPos(8, yBase, 8);
        if (!tormentLevel.getBlockState(checkFloor).is(ModBlocks.TORMENT_STONE.get())) {
            generateTormentFloor(tormentLevel, floor);
        }

        // Limpar Área Segura de Entrada 3x3 ao redor de (8, yBase + 2, 8)
        int spawnY = 12 + floor * 15;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos p = new BlockPos(8 + dx, spawnY + dy, 8 + dz);
                    if (p.getY() > yBase && p.getY() < yBase + 6) {
                        tormentLevel.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        player.teleportTo(tormentLevel, 8.0D, spawnY, 8.0D, player.getYRot(), player.getXRot());
        tormentLevel.playSound(null, BlockPos.containing(8.0D, spawnY, 8.0D), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 0.8F);
    }

    /**
     * Constrói um andar completo (grade 31x31 + câmaras + arena em Z=36) na coordenada vertical `Y_base = 10 + floor * 15`.
     */
    public static void generateTormentFloor(ServerLevel level, int floor) {
        int yBase = 10 + floor * 15; // Piso em yBase; Corredores em yBase+1..yBase+5; Teto em yBase+6
        int yCeiling = yBase + 6;
        RandomSource rand = RandomSource.create(level.getSeed() ^ (floor * 0x9E3779B97F4A7C15L));

        // 1. Grade booleana 31x31 (true = passagem oca, false = parede de força mágica)
        boolean[][] passage = new boolean[GRID_SIZE][GRID_SIZE];
        generateRandomizedDfsMaze(passage, rand);

        BlockState stone = ModBlocks.TORMENT_STONE.get().defaultBlockState();
        BlockState forceWall = ModBlocks.TORMENT_STONE.get().defaultBlockState();
        BlockState soulSand = Blocks.SOUL_SAND.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 2. Esculpir as 5 Câmaras Especiais na grade 31x31
        // (a) Câmara de Entrada (Sul): 7x7 centrada em (8, 8)
        carveRect(passage, 5, 5, 11, 11);
        // (b) Câmara Central do Labirinto: 7x7 centrada no centro absoluto (15, 15)
        carveRect(passage, 12, 12, 18, 18);
        // (c) Câmara Lateral Esquerda: 5x5 na metade da profundidade com deslocamento -5..+5 em Z
        int leftZOffset = rand.nextInt(11) - 5;
        int leftCenterZ = Math.max(3, Math.min(27, 15 + leftZOffset));
        carveRect(passage, 2, leftCenterZ - 2, 6, leftCenterZ + 2);
        // (d) Câmara Lateral Direita: 5x5 simetricamente oposta com deslocamento -5..+5 em Z
        int rightZOffset = rand.nextInt(11) - 5;
        int rightCenterZ = Math.max(3, Math.min(27, 15 + rightZOffset));
        carveRect(passage, 24, rightCenterZ - 2, 28, rightCenterZ + 2);
        // (e) Corredor conector até a Câmara de Fim de Corredor / Arena ao Norte
        for (int z = 15; z < GRID_SIZE; z++) {
            passage[9][z] = true;
            passage[15][z] = true;
        }

        // 3. Construir blocos físicos da grade 31x31
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int z = 0; z < GRID_SIZE; z++) {
                boolean isOpen = passage[x][z] && x > 0 && x < GRID_SIZE - 1 && z > 0 && z < GRID_SIZE - 1;
                // Piso: 1% de chance de Areia das Almas nas passagens
                BlockState floorState = (isOpen && rand.nextInt(100) == 0) ? soulSand : stone;
                level.setBlock(new BlockPos(x, yBase, z), floorState, 2);
                // Teto
                level.setBlock(new BlockPos(x, yCeiling, z), stone, 2);
                // Paredes / Corredores de 5 blocos de altura interna (total 6 blocos com teto/base)
                BlockState fill = isOpen ? air : forceWall;
                for (int y = yBase + 1; y < yCeiling; y++) {
                    level.setBlock(new BlockPos(x, y, z), fill, 2);
                }
            }
        }

        // Garantir passagem aberta entre a Câmara Central e as Câmaras Laterais
        for (int x = 4; x <= 26; x++) {
            for (int y = yBase + 1; y < yCeiling; y++) {
                level.setBlock(new BlockPos(x, y, 15), air, 2);
            }
        }
        for (int z = Math.min(15, leftCenterZ); z <= Math.max(15, leftCenterZ); z++) {
            for (int y = yBase + 1; y < yCeiling; y++) {
                level.setBlock(new BlockPos(4, y, z), air, 2);
            }
        }
        for (int z = Math.min(15, rightCenterZ); z <= Math.max(15, rightCenterZ); z++) {
            for (int y = yBase + 1; y < yCeiling; y++) {
                level.setBlock(new BlockPos(26, y, z), air, 2);
            }
        }

        // 4. Câmara de Fim de Corredor (Norte, 7x9) e Arena do Soberano do Tormento em (X = 9, Z = 36)
        // Abre corredor de Z=29 até Z=40 para conectar com a câmara de 7x9 centrada em X=9, Z=36
        for (int x = 5; x <= 13; x++) {
            for (int z = 30; z <= 40; z++) {
                boolean border = (x == 5 || x == 13 || z == 40);
                level.setBlock(new BlockPos(x, yBase, z), stone, 2);
                level.setBlock(new BlockPos(x, yCeiling, z), stone, 2);
                for (int y = yBase + 1; y < yCeiling; y++) {
                    level.setBlock(new BlockPos(x, y, z), border ? forceWall : air, 2);
                }
            }
        }

        // 5. Posicionar os 3 Baús Reabastecedores (Refilling Chests)
        BlockState chestState = ModBlocks.REFILLING_CHEST.get().defaultBlockState();
        placeRefillingChest(level, new BlockPos(15, yBase + 1, 15), chestState);
        placeRefillingChest(level, new BlockPos(4, yBase + 1, leftCenterZ), chestState);
        placeRefillingChest(level, new BlockPos(26, yBase + 1, rightCenterZ), chestState);

        // 6. Posicionar o Portal do Tormento no fundo da Câmara Norte (X=9, Z=39)
        BlockState portalState = ModBlocks.TORMENT_PORTAL.get().defaultBlockState();
        level.setBlock(new BlockPos(9, yBase + 1, 39), portalState, 2);
        level.setBlock(new BlockPos(9, yBase + 2, 39), portalState, 2);
    }

    private static void placeRefillingChest(ServerLevel level, BlockPos pos, BlockState chestState) {
        level.setBlock(pos, chestState, 3);
        if (level.getBlockEntity(pos) instanceof RefillingChestBlockEntity chest) {
            chest.refillLoot(level);
        }
    }

    private static void carveRect(boolean[][] grid, int x0, int z0, int x1, int z1) {
        for (int x = Math.max(1, x0); x <= Math.min(GRID_SIZE - 2, x1); x++) {
            for (int z = Math.max(1, z0); z <= Math.min(GRID_SIZE - 2, z1); z++) {
                grid[x][z] = true;
            }
        }
    }

    /**
     * Randomized Depth-First Search (Backtracker) em grade 31x31.
     */
    private static void generateRandomizedDfsMaze(boolean[][] passage, RandomSource rand) {
        Deque<int[]> stack = new ArrayDeque<>();
        passage[1][1] = true;
        stack.push(new int[]{1, 1});

        int[][] dirs = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int cx = cur[0];
            int cz = cur[1];

            List<int[]> neighbors = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = cx + d[0];
                int nz = cz + d[1];
                if (nx > 0 && nx < GRID_SIZE - 1 && nz > 0 && nz < GRID_SIZE - 1 && !passage[nx][nz]) {
                    neighbors.add(d);
                }
            }

            if (!neighbors.isEmpty()) {
                int[] chosen = neighbors.get(rand.nextInt(neighbors.size()));
                int wx = cx + chosen[0] / 2;
                int wz = cz + chosen[1] / 2;
                int nx = cx + chosen[0];
                int nz = cz + chosen[1];
                passage[wx][wz] = true;
                passage[nx][nz] = true;
                stack.push(new int[]{nx, nz});
            } else {
                stack.pop();
            }
        }
    }
}
