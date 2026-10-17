package com.occultum.common.mirror;

import com.occultum.common.altar.AltarPowerNetwork;
import com.occultum.common.dimension.OccultumDimensions;
import com.occultum.common.entity.ReflectionDemonEntity;
import com.occultum.common.registry.ModAttachmentTypes;
import com.occultum.common.registry.ModBlocks;
import com.occultum.common.registry.ModEntityTypes;
import com.occultum.common.torment.TormentPortalBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Gerenciador Completo da Dimensão do Espelho e Vinculação Celular (`14_DIMENSOES_DO_TORMENTO_E_ESPELHO.md` Seções 3, 4 e 5):
 * - Grade Celular Volumétrica por Chunk (16x16x16):
 *   * Coordenadas relativas X e Z: 0..3 e 13..15 = paredes sólidas de Parede de Espelho (`MIRROR_WALL`);
 *     4..12 = corredor interno oco de passagem.
 *   * Coordenada relativa Y (módulo 16): 0..5 = piso sólido (6 blocos); 6..14 = 9 blocos de altura livre; 15 = teto sólido.
 *   * Comporta até 15 níveis verticais empilhados (cellY de 0 a 14).
 * - Vinculação em Espiral de até 256 células:
 *   * Encontra uma célula oca livre na Dimensão do Espelho, esculpe a câmara se necessário,
 *     posiciona um Espelho Inquebrável (`UNBREAKABLE_MIRROR`) em `X = cellX*16 + 4, Y = cellY*16 + 8, Z = cellZ*16 + 8`
 *     e vincula bidirecionalmente.
 * - Deslocamento pelo Espelho:
 *   * A. Travessia Alinhada na mesma dimensão: horizontal até 32 blocos (direção oposta) ou vertical 2 a 15 blocos
 *     acima/abaixo (mesma direção, invertendo yaw em 180°).
 *   * B. Penetração de Paredes na Dimensão do Espelho: varre 1 a 9 blocos à frente, consome 3.000 de poder do Altar
 *     e atravessa a parede sólida.
 *   * C. Travessia Interdimensional: aplica cooldown de 60 ticks no espelho de destino e gera o `ReflectionDemonEntity`
 *     em `(dimX + 4, dimY, dimZ)` se ainda não purificado.
 * - Comandos Vocais no Chat:
 *   * `"Mirror mirror send me home"` (cooldown 5 min = 6.000 ticks) -> retorna à célula de entrada.
 *   * `"Mirror mirror I give up"` (cooldown 60 min = 72.000 ticks) -> retorna à cama/spawn no Overworld.
 */
public final class MirrorDimensionManager {

    private static final Set<Long> OCCUPIED_CELLS = new HashSet<>();

    private MirrorDimensionManager() {}

    private static long packCell(int cellX, int cellY, int cellZ) {
        return (((long) cellX & 0x1FFFFFL) << 42) | (((long) cellY & 0xFFFL) << 21) | ((long) cellZ & 0x1FFFFFL);
    }

    /**
     * Seção 3.2: Constrói a estrutura volumétrica exata de uma célula 16x16x16 na Dimensão do Espelho:
     * - X, Z em [0..3] ou [13..15] -> Parede de Espelho sólida; [4..12] -> espaço interno oco.
     * - Y relativo em [0..5] -> piso de 6 blocos; [6..14] -> 9 blocos de ar; [15] -> teto sólido.
     */
    public static void ensureMirrorCellConstructed(ServerLevel mirrorLevel, int cellX, int cellY, int cellZ) {
        int baseX = cellX * 16;
        int baseY = cellY * 16;
        int baseZ = cellZ * 16;

        BlockPos checkWall = new BlockPos(baseX, baseY + 5, baseZ);
        if (mirrorLevel.getBlockState(checkWall).is(ModBlocks.MIRROR_WALL.get())) {
            return;
        }

        BlockState wall = ModBlocks.MIRROR_WALL.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int rx = 0; rx < 16; rx++) {
            for (int rz = 0; rz < 16; rz++) {
                boolean isHorizontalWall = (rx <= 3 || rx >= 13 || rz <= 3 || rz >= 13);
                for (int ry = 0; ry < 16; ry++) {
                    boolean isFloorOrCeiling = (ry <= 5 || ry == 15);
                    BlockState targetState = (isHorizontalWall || isFloorOrCeiling) ? wall : air;
                    mirrorLevel.setBlock(new BlockPos(baseX + rx, baseY + ry, baseZ + rz), targetState, 2);
                }
            }
        }
    }

    /**
     * Seção 3.4: Busca em espiral por uma célula não ocupada na Dimensão do Espelho (raio de até 256 células,
     * varrendo até 15 níveis verticais), gera o Espelho Inquebrável em `(cellX*16 + 4, cellY*16 + 8, cellZ*16 + 8)`
     * e vincula bidirecionalmente.
     */
    public static BlockPos linkMaterialMirrorToCell(ServerLevel materialLevel, BlockPos materialPos, MysticMirrorBlockEntity materialBe) {
        ServerLevel mirrorLevel = materialLevel.getServer().getLevel(OccultumDimensions.MIRROR_LEVEL);
        if (mirrorLevel == null) return materialPos;

        int originCellX = materialPos.getX() >> 4;
        int originCellZ = materialPos.getZ() >> 4;

        int chosenCellX = originCellX;
        int chosenCellY = 1;
        int chosenCellZ = originCellZ;
        boolean found = false;

        // Busca em espiral até raio 256 células e alturas 1..14
        outer:
        for (int radius = 0; radius <= 256; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int cx = originCellX + dx;
                    int cz = originCellZ + dz;
                    for (int cy = 1; cy <= 14; cy++) {
                        long key = packCell(cx, cy, cz);
                        BlockPos anchorPos = new BlockPos(cx * 16 + 4, cy * 16 + 8, cz * 16 + 8);
                        if (!OCCUPIED_CELLS.contains(key) && !mirrorLevel.getBlockState(anchorPos).is(ModBlocks.UNBREAKABLE_MIRROR.get())) {
                            chosenCellX = cx;
                            chosenCellY = cy;
                            chosenCellZ = cz;
                            OCCUPIED_CELLS.add(key);
                            found = true;
                            break outer;
                        }
                    }
                }
            }
        }

        ensureMirrorCellConstructed(mirrorLevel, chosenCellX, chosenCellY, chosenCellZ);
        // Garante piso da altura 6..7 abaixo do espelho em Y+8 para apoio
        BlockPos unbreakablePos = new BlockPos(chosenCellX * 16 + 4, chosenCellY * 16 + 6, chosenCellZ * 16 + 8);
        BlockState lower = ModBlocks.UNBREAKABLE_MIRROR.get().defaultBlockState()
                .setValue(MysticMirrorBlock.FACING, Direction.EAST)
                .setValue(MysticMirrorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(MysticMirrorBlock.HALF, DoubleBlockHalf.UPPER);

        mirrorLevel.setBlock(unbreakablePos, lower, 3);
        mirrorLevel.setBlock(unbreakablePos.above(), upper, 3);

        materialBe.setLinkedPos(unbreakablePos);
        materialBe.setLinkedDimension(OccultumDimensions.MIRROR_LEVEL.location().toString());

        if (mirrorLevel.getBlockEntity(unbreakablePos) instanceof MysticMirrorBlockEntity cellBe) {
            cellBe.setLinkedPos(materialPos);
            cellBe.setLinkedDimension(materialLevel.dimension().location().toString());
            cellBe.setDemonSlain(materialBe.isDemonSlain());
        }

        return unbreakablePos;
    }

    /**
     * Seção 3.5: Processa a colisão de um jogador caminhando de frente para o espelho.
     */
    public static void handlePlayerStepIntoMirror(ServerLevel level, BlockPos mirrorBasePos, BlockState state, ServerPlayer player) {
        if (!(level.getBlockEntity(mirrorBasePos) instanceof MysticMirrorBlockEntity mirrorBe)) return;
        if (mirrorBe.getCooldownTicks() > 0 || player.isOnPortalCooldown()) return;

        Direction facing = state.getValue(MysticMirrorBlock.FACING);
        boolean inMirrorDim = level.dimension() == OccultumDimensions.MIRROR_LEVEL;

        // A. Travessia Alinhada na Mesma Dimensão (se o demônio já foi derrotado OU já estiver na Dimensão do Espelho)
        if (mirrorBe.isDemonSlain() || inMirrorDim) {
            // 1. Eixo Horizontal: até 32 blocos na direção para onde a face do espelho aponta (espelho oposto)
            for (int dist = 2; dist <= 32; dist++) {
                BlockPos check = mirrorBasePos.relative(facing, dist);
                BlockState st = level.getBlockState(check);
                if (st.getBlock() instanceof MysticMirrorBlock && st.getValue(MysticMirrorBlock.HALF) == DoubleBlockHalf.LOWER) {
                    if (st.getValue(MysticMirrorBlock.FACING) == facing.getOpposite()) {
                        teleportInFrontOfMirror(level, check, facing.getOpposite(), player, false);
                        return;
                    }
                }
            }
            // 2. Eixo Vertical: entre 2 e 15 blocos acima e depois 2 a 15 blocos abaixo (mesma direção, invertendo yaw em 180°)
            for (int dy = 2; dy <= 15; dy++) {
                BlockPos upCheck = mirrorBasePos.above(dy);
                if (isSameFacingLowerMirror(level, upCheck, facing)) {
                    teleportInFrontOfMirror(level, upCheck, facing, player, true);
                    return;
                }
            }
            for (int dy = 2; dy <= 15; dy++) {
                BlockPos downCheck = mirrorBasePos.below(dy);
                if (isSameFacingLowerMirror(level, downCheck, facing)) {
                    teleportInFrontOfMirror(level, downCheck, facing, player, true);
                    return;
                }
            }
        }

        // B. Penetração de Paredes na Dimensão do Espelho (consome 3.000 de poder do Altar e atravessa 1 a 9 blocos)
        if (inMirrorDim) {
            Direction behindMirror = facing.getOpposite();
            for (int d = 1; d <= 9; d++) {
                BlockPos check = mirrorBasePos.relative(behindMirror, d);
                if (level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                    if (AltarPowerNetwork.tryConsumeNearestAltarPower(level, mirrorBasePos, 32, 3000)) {
                        mirrorBe.setCooldownTicks(60);
                        player.setPortalCooldown();
                        player.teleportTo(level, check.getX() + 0.5D, check.getY(), check.getZ() + 0.5D, player.getYRot(), player.getXRot());
                        level.playSound(null, check, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
                        return;
                    }
                    break;
                }
            }
        }

        // C. Travessia Interdimensional
        if (!inMirrorDim) {
            BlockPos destMirrorPos = mirrorBe.getLinkedPos();
            if (destMirrorPos == null) {
                destMirrorPos = linkMaterialMirrorToCell(level, mirrorBasePos, mirrorBe);
            }
            ServerLevel mirrorLevel = level.getServer().getLevel(OccultumDimensions.MIRROR_LEVEL);
            if (mirrorLevel == null) return;

            int cellX = destMirrorPos.getX() >> 4;
            int cellY = destMirrorPos.getY() >> 4;
            int cellZ = destMirrorPos.getZ() >> 4;
            ensureMirrorCellConstructed(mirrorLevel, cellX, cellY, cellZ);

            if (mirrorLevel.getBlockEntity(destMirrorPos) instanceof MysticMirrorBlockEntity destBe) {
                destBe.setCooldownTicks(60); // 60 ticks (3 segundos) de recarga no espelho de destino
            }
            mirrorBe.setCooldownTicks(60);
            player.setPortalCooldown();

            double spawnX = destMirrorPos.getX() + 1.5D;
            double spawnY = destMirrorPos.getY();
            double spawnZ = destMirrorPos.getZ() + 0.5D;

            // Registra coordenadas da célula de entrada para o comando vocal "Mirror mirror send me home"
            player.setData(ModAttachmentTypes.MIRROR_ENTRY_POS.get(),
                    ((int) spawnX) + "," + ((int) spawnY) + "," + ((int) spawnZ));

            player.teleportTo(mirrorLevel, spawnX, spawnY, spawnZ, -90.0F, player.getXRot());
            mirrorLevel.playSound(null, destMirrorPos, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
            mirrorLevel.sendParticles(ParticleTypes.SPLASH, spawnX, spawnY + 1.0D, spawnZ, 25, 0.3, 0.5, 0.3, 0.05);

            // Se a célula ainda não teve seu Demônio do Reflexo derrotado, gera-o no centro da câmara (dimX + 4, dimY, dimZ)
            if (!mirrorBe.isDemonSlain()) {
                ReflectionDemonEntity demon = ModEntityTypes.REFLECTION_DEMON.get().create(mirrorLevel);
                if (demon != null) {
                    double centerX = destMirrorPos.getX() + 4.0D;
                    double centerY = destMirrorPos.getY();
                    double centerZ = destMirrorPos.getZ() + 0.5D;
                    demon.moveTo(centerX, centerY, centerZ, 90.0F, 0.0F);
                    demon.setBoundMirrorPos(destMirrorPos);
                    demon.setMaterialMirrorPos(mirrorBasePos);
                    demon.mimicPlayer(player);
                    demon.setTarget(player);
                    mirrorLevel.addFreshEntity(demon);
                }
            }
        } else {
            // Retorno da Dimensão do Espelho para o Mundo Material (bloqueado se o demônio ainda não foi derrotado!)
            if (!mirrorBe.isDemonSlain()) {
                player.displayClientMessage(Component.translatable("message.occultum.mirror.demon_blocking").withStyle(ChatFormatting.RED), true);
                return;
            }
            BlockPos matPos = mirrorBe.getLinkedPos();
            ServerLevel overworld = level.getServer().overworld();
            if (matPos != null) {
                if (overworld.getBlockEntity(matPos) instanceof MysticMirrorBlockEntity matBe) {
                    matBe.setCooldownTicks(60);
                }
                mirrorBe.setCooldownTicks(60);
                player.setPortalCooldown();
                player.teleportTo(overworld, matPos.getX() + 0.5D, matPos.getY(), matPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
                overworld.playSound(null, matPos, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private static boolean isSameFacingLowerMirror(ServerLevel level, BlockPos pos, Direction facing) {
        BlockState st = level.getBlockState(pos);
        return st.getBlock() instanceof MysticMirrorBlock
                && st.getValue(MysticMirrorBlock.HALF) == DoubleBlockHalf.LOWER
                && st.getValue(MysticMirrorBlock.FACING) == facing;
    }

    private static void teleportInFrontOfMirror(ServerLevel level, BlockPos destMirrorPos, Direction destFacing,
                                                ServerPlayer player, boolean invertYaw180) {
        if (level.getBlockEntity(destMirrorPos) instanceof MysticMirrorBlockEntity destBe) {
            destBe.setCooldownTicks(60);
        }
        BlockPos front = destMirrorPos.relative(destFacing);
        float newYaw = invertYaw180 ? (player.getYRot() + 180.0F) % 360.0F : destFacing.toYRot();
        player.setPortalCooldown();
        player.teleportTo(level, front.getX() + 0.5D, front.getY(), front.getZ() + 0.5D, newYaw, player.getXRot());
        level.playSound(null, front, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.SPLASH, front.getX() + 0.5D, front.getY() + 1.0D, front.getZ() + 0.5D, 20, 0.3, 0.5, 0.3, 0.05);
    }

    /**
     * Seção 5.4: Comandos Vocais de Fuga no Chat:
     * - "Mirror mirror send me home" (cooldown de 5 minutos = 6.000 ticks) -> retorna à célula de entrada.
     * - "Mirror mirror I give up" (cooldown de 60 minutos = 72.000 ticks) -> retorna à cama/spawn do Overworld.
     */
    public static boolean handleVoiceChatCommand(ServerPlayer player, String rawMessage) {
        if (player.level().dimension() != OccultumDimensions.MIRROR_LEVEL) {
            return false;
        }
        String normalized = rawMessage.replaceAll("[^a-zA-Z ]", "").trim().replaceAll("\\s+", " ");
        long now = player.level().getGameTime();

        if (normalized.equalsIgnoreCase("Mirror mirror send me home")) {
            long readyAt = player.getData(ModAttachmentTypes.MIRROR_SEND_HOME_COOLDOWN.get());
            if (now < readyAt) {
                long remSeconds = (readyAt - now) / 20L;
                player.displayClientMessage(
                        Component.translatable("message.occultum.mirror.voice_cooldown", remSeconds).withStyle(ChatFormatting.YELLOW),
                        false
                );
                return true;
            }
            String entry = player.getData(ModAttachmentTypes.MIRROR_ENTRY_POS.get());
            if (entry != null && !entry.isEmpty()) {
                String[] parts = entry.split(",");
                if (parts.length == 3) {
                    try {
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);
                        player.setData(ModAttachmentTypes.MIRROR_SEND_HOME_COOLDOWN.get(), now + 6000L); // 5 min = 6.000 ticks
                        ServerLevel sl = player.serverLevel();
                        player.teleportTo(sl, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
                        sl.playSound(null, new BlockPos(x, y, z), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
                        sl.sendParticles(ParticleTypes.SPLASH, x + 0.5D, y + 1.0D, z + 0.5D, 25, 0.3, 0.5, 0.3, 0.05);
                        return true;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } else if (normalized.equalsIgnoreCase("Mirror mirror I give up")) {
            long readyAt = player.getData(ModAttachmentTypes.MIRROR_GIVE_UP_COOLDOWN.get());
            if (now < readyAt) {
                long remSeconds = (readyAt - now) / 20L;
                player.displayClientMessage(
                        Component.translatable("message.occultum.mirror.voice_cooldown", remSeconds).withStyle(ChatFormatting.YELLOW),
                        false
                );
                return true;
            }
            player.setData(ModAttachmentTypes.MIRROR_GIVE_UP_COOLDOWN.get(), now + 72000L); // 60 min = 72.000 ticks
            ServerLevel overworld = player.server.overworld();
            BlockPos target = player.getRespawnPosition();
            if (target == null || player.getRespawnDimension() != Level.OVERWORLD) {
                target = overworld.getSharedSpawnPos();
            }
            BlockPos safe = TormentPortalBlock.findSafeLandingSpot(overworld, target);
            player.teleportTo(overworld, safe.getX() + 0.5D, safe.getY(), safe.getZ() + 0.5D, player.getYRot(), player.getXRot());
            overworld.playSound(null, safe, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }
}
