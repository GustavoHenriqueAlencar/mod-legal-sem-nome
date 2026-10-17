package com.occultum.common.ritual;

import com.occultum.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validador geométrico estrito da Matriz 17x17 para os 3 anéis concêntricos de Magia de Círculo
 * conforme a Seção 2.2 de 05_MAGIA_DE_CIRCULO_E_RITUAIS.md:
 * - Pequeno (7x7): 16 glifos
 * - Médio (11x11): 28 glifos
 * - Grande (15x15): 40 glifos
 */
public final class CircleGeometryValidator {

    public enum ChalkColor {
        NONE(0),
        WHITE(1),      // Giz de Ritual (Branco)
        PURPLE(2),     // Giz do Outro Lugar (Roxo)
        RED(3),        // Giz Infernal (Vermelho)
        GOLD(4);       // Giz Dourado (Central)

        private final int id;

        ChalkColor(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ChalkColor byId(int id) {
            return switch (id) {
                case 1 -> WHITE;
                case 2 -> PURPLE;
                case 3 -> RED;
                case 4 -> GOLD;
                default -> NONE;
            };
        }

        public Block getBlock() {
            return switch (this) {
                case WHITE -> ModBlocks.RITUAL_CHALK_BLOCK.get();
                case PURPLE -> ModBlocks.OTHERWHERE_CHALK_BLOCK.get();
                case RED -> ModBlocks.INFERNAL_CHALK_BLOCK.get();
                case GOLD -> ModBlocks.GOLDEN_CHALK_BLOCK.get();
                case NONE -> Blocks.AIR;
            };
        }

        public static ChalkColor fromState(BlockState state) {
            if (state.is(ModBlocks.RITUAL_CHALK_BLOCK.get())) return WHITE;
            if (state.is(ModBlocks.OTHERWHERE_CHALK_BLOCK.get())) return PURPLE;
            if (state.is(ModBlocks.INFERNAL_CHALK_BLOCK.get())) return RED;
            if (state.is(ModBlocks.GOLDEN_CHALK_BLOCK.get())) return GOLD;
            return NONE;
        }
    }

    public record Offset2D(int dx, int dz) {}

    public static final List<Offset2D> SMALL_RING_OFFSETS;
    public static final List<Offset2D> MEDIUM_RING_OFFSETS;
    public static final List<Offset2D> LARGE_RING_OFFSETS;

    static {
        // Círculo Pequeno (7x7) - 16 Glifos:
        // Segmentos cardeais: (±3, -1..1), (-1..1, ±3) -> 12 blocos
        // Diagonais chanfradas: (±2, ±2) -> 4 blocos
        List<Offset2D> small = new ArrayList<>(16);
        for (int i = -1; i <= 1; i++) {
            small.add(new Offset2D(3, i));
            small.add(new Offset2D(-3, i));
            small.add(new Offset2D(i, 3));
            small.add(new Offset2D(i, -3));
        }
        small.add(new Offset2D(2, 2));
        small.add(new Offset2D(2, -2));
        small.add(new Offset2D(-2, 2));
        small.add(new Offset2D(-2, -2));
        SMALL_RING_OFFSETS = Collections.unmodifiableList(small);

        // Círculo Médio (11x11) - 28 Glifos:
        // Segmentos cardeais: (±5, -2..2), (-2..2, ±5) -> 20 blocos
        // Diagonais chanfradas: (±4, ±3), (±3, ±4) -> 8 blocos
        List<Offset2D> medium = new ArrayList<>(28);
        for (int i = -2; i <= 2; i++) {
            medium.add(new Offset2D(5, i));
            medium.add(new Offset2D(-5, i));
            medium.add(new Offset2D(i, 5));
            medium.add(new Offset2D(i, -5));
        }
        int[] signs = {-1, 1};
        for (int sx : signs) {
            for (int sz : signs) {
                medium.add(new Offset2D(4 * sx, 3 * sz));
                medium.add(new Offset2D(3 * sx, 4 * sz));
            }
        }
        MEDIUM_RING_OFFSETS = Collections.unmodifiableList(medium);

        // Círculo Grande (15x15) - 40 Glifos:
        // Segmentos cardeais: (±7, -3..3), (-3..3, ±7) -> 28 blocos
        // Diagonais chanfradas: (±6, ±4), (±5, ±5), (±4, ±6) -> 12 blocos
        List<Offset2D> large = new ArrayList<>(40);
        for (int i = -3; i <= 3; i++) {
            large.add(new Offset2D(7, i));
            large.add(new Offset2D(-7, i));
            large.add(new Offset2D(i, 7));
            large.add(new Offset2D(i, -7));
        }
        for (int sx : signs) {
            for (int sz : signs) {
                large.add(new Offset2D(6 * sx, 4 * sz));
                large.add(new Offset2D(5 * sx, 5 * sz));
                large.add(new Offset2D(4 * sx, 6 * sz));
            }
        }
        LARGE_RING_OFFSETS = Collections.unmodifiableList(large);
    }

    private CircleGeometryValidator() {}

    public record DetectedRings(ChalkColor small, ChalkColor medium, ChalkColor large) {
        public boolean matches(ChalkColor reqSmall, ChalkColor reqMed, ChalkColor reqLarge) {
            if (reqSmall != ChalkColor.NONE && small != reqSmall) return false;
            if (reqMed != ChalkColor.NONE && medium != reqMed) return false;
            if (reqLarge != ChalkColor.NONE && large != reqLarge) return false;
            return true;
        }

        public boolean hasAnyRing() {
            return small != ChalkColor.NONE || medium != ChalkColor.NONE || large != ChalkColor.NONE;
        }

        public int maxRadius() {
            if (large != ChalkColor.NONE) return 7;
            if (medium != ChalkColor.NONE) return 5;
            if (small != ChalkColor.NONE) return 3;
            return 3;
        }

        public int packMask() {
            return (small.getId() & 0xF) | ((medium.getId() & 0xF) << 4) | ((large.getId() & 0xF) << 8);
        }

        public static DetectedRings unpackMask(int mask) {
            return new DetectedRings(
                    ChalkColor.byId(mask & 0xF),
                    ChalkColor.byId((mask >> 4) & 0xF),
                    ChalkColor.byId((mask >> 8) & 0xF)
            );
        }
    }

    /**
     * Valida um anel completo (16, 28 ou 40 glifos). Todos os blocos nas coordenadas relativas
     * devem existir e possuir exatamente a mesma cor de giz (Branco, Roxo ou Vermelho).
     * Qualquer falha ou cor divergente invalida o anel (retorna NONE).
     */
    public static ChalkColor validateRing(Level level, BlockPos center, List<Offset2D> offsets) {
        ChalkColor expected = ChalkColor.NONE;
        for (Offset2D off : offsets) {
            BlockPos p = center.offset(off.dx(), 0, off.dz());
            ChalkColor c = ChalkColor.fromState(level.getBlockState(p));
            if (c != ChalkColor.WHITE && c != ChalkColor.PURPLE && c != ChalkColor.RED) {
                return ChalkColor.NONE;
            }
            if (expected == ChalkColor.NONE) {
                expected = c;
            } else if (expected != c) {
                return ChalkColor.NONE;
            }
        }
        return expected;
    }

    public static DetectedRings detectAllRings(Level level, BlockPos center) {
        ChalkColor s = validateRing(level, center, SMALL_RING_OFFSETS);
        ChalkColor m = validateRing(level, center, MEDIUM_RING_OFFSETS);
        ChalkColor l = validateRing(level, center, LARGE_RING_OFFSETS);
        return new DetectedRings(s, m, l);
    }

    public static void clearRing(Level level, BlockPos center, List<Offset2D> offsets) {
        for (Offset2D off : offsets) {
            BlockPos p = center.offset(off.dx(), 0, off.dz());
            ChalkColor c = ChalkColor.fromState(level.getBlockState(p));
            if (c != ChalkColor.NONE && c != ChalkColor.GOLD) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    public static void stampRing(Level level, BlockPos center, List<Offset2D> offsets, ChalkColor color) {
        if (color == ChalkColor.NONE) return;
        Block block = color.getBlock();
        for (Offset2D off : offsets) {
            BlockPos p = center.offset(off.dx(), 0, off.dz());
            if ((level.getBlockState(p).isAir() || level.getBlockState(p).canBeReplaced())
                    && level.getBlockState(p.below()).isSolidRender(level, p.below())) {
                level.setBlock(p, block.defaultBlockState(), 3);
            }
        }
    }
}
