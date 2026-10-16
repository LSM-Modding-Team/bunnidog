package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * The landmarks of a settlement (Phase 6e), as offsets from the centre column of a 3x3 footprint: pure Java, covered by
 * tools/GenomeCheck.java. Every landmark fits in 3x3 columns so that it stands in the gaps between the trees and houses.
 * Which landmark a settlement has depends on its level ({@link #kindsFor(int)}): the higher the level, the more there is, and each
 * level brings something NEW instead of one more dirt hut:
 * <ul>
 * <li>level 2: a HAYSTACK with a barrel (the feed store);</li>
 * <li>level 3: a WELL (cobblestone ring, water inside, a little roof on four posts);</li>
 * <li>level 4: a market STALL (four posts, a slab canopy, barrel, pumpkin and melon on the counter);</li>
 * <li>level 5, the village: a FARM plot (water, wheat and roots, optionally one fitted corner trough for resident gardening)
 *     and a lamp PLAZA (paving, a hay bale to sit on and four lanterns on posts).</li>
 * </ul>
 * Cell (0, 0, 0) is the first air block above the ground; {@code dy = -1} is the ground block itself (paving, water, farmland).
 * The world code ({@code worldgen/WarrenFeature#buildProp}) turns each {@link Mat} into a block.
 */
public final class PropShape {
    private PropShape() {}

    public enum Kind { HAYSTACK, WELL, STALL, FARM, PLAZA }

    public enum Mat { HAY, BARREL, COBBLE, MOSSY, WATER, FENCE, SLAB, LANTERN, PUMPKIN, MELON, FARMLAND, WHEAT, CARROTS, POTATOES, FILL, TROUGH }

    /** One block of a landmark, relative to the centre column. */
    public record Cell(int dx, int dy, int dz, Mat mat) {}

    /** Every landmark fits in 3x3 columns: it reaches this far from its centre. */
    public static final int RADIUS = 1;
    /** The tallest landmark (the well, with its roof) is this high: dy -1 to {@value}. */
    public static final int TOP = 3;

    /** The landmarks a settlement of this level (= number of guava trees really built) has: none, 1, 2, 3 and 5 of them. */
    public static List<Kind> kindsFor(int level) {
        List<Kind> kinds = new ArrayList<>();
        if (level >= 2) {
            kinds.add(Kind.HAYSTACK);
        }
        if (level >= 3) {
            kinds.add(Kind.WELL);
        }
        if (level >= 4) {
            kinds.add(Kind.STALL);
        }
        if (level >= 5) {
            kinds.add(Kind.FARM);
            kinds.add(Kind.PLAZA);
        }
        return List.copyOf(kinds);
    }

    /** The cells of a landmark turned {@code turns} quarter turns (only the stall and the haystack are not symmetric). Order: ground level up. */
    public static List<Cell> cells(Kind kind, int turns) {
        return cells(kind,turns,false);
    }
    /** New starts may replace one corner crop and its farmland, without extending the footprint. */
    public static List<Cell> cells(Kind kind, int turns, boolean garden) {
        List<Cell> raw = new ArrayList<>();
        switch (kind) {
            case HAYSTACK -> {
                for (int[] p : new int[][] {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {0, 0}, {-1, 1}}) {
                    raw.add(new Cell(p[0], 0, p[1], Mat.HAY));
                }
                raw.add(new Cell(1, 0, 0, Mat.BARREL));
                raw.add(new Cell(1, 0, 1, Mat.BARREL));
                raw.add(new Cell(-1, 1, -1, Mat.HAY));
                raw.add(new Cell(0, 1, -1, Mat.HAY));
                raw.add(new Cell(-1, 1, 0, Mat.HAY));
                raw.add(new Cell(-1, 2, -1, Mat.HAY));
            }
            case WELL -> {
                raw.add(new Cell(0, -1, 0, Mat.WATER));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dz != 0) {
                            raw.add(new Cell(dx, 0, dz, dx != 0 && dz != 0 ? Mat.COBBLE : Mat.MOSSY));
                        }
                    }
                }
                for (int dy = 1; dy <= 2; dy++) {
                    for (int sx = -1; sx <= 1; sx += 2) {
                        for (int sz = -1; sz <= 1; sz += 2) {
                            raw.add(new Cell(sx, dy, sz, Mat.FENCE));
                        }
                    }
                }
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        raw.add(new Cell(dx, 3, dz, Mat.SLAB));
                    }
                }
            }
            case STALL -> {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int sx = -1; sx <= 1; sx += 2) {
                        for (int sz = -1; sz <= 1; sz += 2) {
                            raw.add(new Cell(sx, dy, sz, Mat.FENCE));
                        }
                    }
                }
                raw.add(new Cell(0, 0, -1, Mat.BARREL));
                raw.add(new Cell(-1, 0, 0, Mat.PUMPKIN));
                raw.add(new Cell(1, 0, 0, Mat.MELON));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        raw.add(new Cell(dx, 2, dz, Mat.SLAB));
                    }
                }
            }
            case FARM -> {
                raw.add(new Cell(0, -1, 0, Mat.WATER));
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dz != 0) {
                            boolean trough = garden && dx == -1 && dz == -1;
                            raw.add(new Cell(dx, -1, dz, trough ? Mat.FILL : Mat.FARMLAND));
                            raw.add(new Cell(dx, 0, dz, trough ? Mat.TROUGH : dz == 0 ? Mat.CARROTS : dx == 0 ? Mat.POTATOES : Mat.WHEAT));
                        }
                    }
                }
            }
            case PLAZA -> {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        raw.add(new Cell(dx, -1, dz, (dx + dz) % 2 == 0 ? Mat.COBBLE : Mat.MOSSY));
                    }
                }
                raw.add(new Cell(0, 0, 0, Mat.HAY));
                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sz = -1; sz <= 1; sz += 2) {
                        raw.add(new Cell(sx, 0, sz, Mat.FENCE));
                        raw.add(new Cell(sx, 1, sz, Mat.FENCE));
                        raw.add(new Cell(sx, 2, sz, Mat.LANTERN));
                    }
                }
            }
        }
        List<Cell> out = new ArrayList<>();
        for (Cell c : raw) {
            int x = c.dx();
            int z = c.dz();
            for (int t = 0; t < ((turns % 4) + 4) % 4; t++) {
                int nx = -z;
                z = x;
                x = nx;
            }
            out.add(new Cell(x, c.dy(), z, c.mat()));
        }
        out.sort((a, b) -> Integer.compare(a.dy(), b.dy()));
        return List.copyOf(out);
    }
}
