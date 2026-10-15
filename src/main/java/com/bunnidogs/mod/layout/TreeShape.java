package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * The shape of a guava tree (Phase 4g), as offsets from its base: pure Java, no Minecraft, covered by tools/GenomeCheck.java.
 * The world code ({@code worldgen/GuavaTree}) turns each {@link Cell} into a block: LOG = a guava log, LEAVES = guava leaves
 * (which decay, with the distance from {@link #leafDistances}), FRUIT = a {@code guava_fruit} block (age 0) that hangs under a leaf.
 *
 * <p>A small tree: a trunk of {@value #MIN_LOGS} or {@value #MIN_LOGS}+1 logs, a crown of two wide layers (5x5 without the
 * corners; the upper one with some trimmed edge cells), a 3x3 layer and a plus on top, and 1 to {@value #MAX_FRUITS}
 * fruits hanging under the LOWEST layer of the crown. The base cell (0, 0, 0) is where the sapling was, or the first air
 * block above the ground in a warren.
 */
public final class TreeShape {
    private TreeShape() {}

    public enum Kind { LOG, LEAVES, FRUIT }

    /** One block of the tree, relative to the base. */
    public record Cell(int dx, int dy, int dz, Kind kind) {}

    public static final int MIN_LOGS = 3;
    public static final int MAX_FRUITS = 3;
    /** The crown reaches this far to each side. */
    public static final int RADIUS = 2;
    /** The grand tree (6h, the meeting tree of a village): a trunk of this many logs or one more, a crown of this radius, up to this many fruits. */
    public static final int GRAND_MIN_LOGS = 6;
    public static final int GRAND_RADIUS = 3;
    public static final int GRAND_MAX_FRUITS = 6;

    private static long key(int dx, int dy, int dz) {
        return ((long) (dx + 8) << 40) | ((long) (dy + 8) << 20) | (dz + 8);
    }

    /** Vanilla leaves count up to this distance to a log (7 = no log within 6 blocks = the leaf decays). */
    public static final int MAX_LEAF_DISTANCE = 7;

    /**
     * The {@code distance} state of every LEAVES cell (Phase 7a), computed the way vanilla leaves do it: 1 for a leaf next to a log,
     * then +1 per step through other leaves (the 6 face neighbours), at most {@value #MAX_LEAF_DISTANCE} for a leaf that no log reaches.
     * A tree is placed without neighbour updates, so the world code must write the right distance itself or its leaves would decay at once.
     */
    public static Map<Cell, Integer> leafDistances(List<Cell> cells) {
        Map<Long, Cell> at = new LinkedHashMap<>();
        for (Cell c : cells) {
            at.put(key(c.dx(), c.dy(), c.dz()), c);
        }
        Map<Cell, Integer> dist = new LinkedHashMap<>();
        java.util.ArrayDeque<Cell> queue = new java.util.ArrayDeque<>();
        for (Cell c : cells) {
            if (c.kind() == Kind.LOG) {
                dist.put(c, 0);
                queue.add(c);
            }
        }
        int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        while (!queue.isEmpty()) {
            Cell c = queue.poll();
            int d = dist.get(c);
            if (d + 1 >= MAX_LEAF_DISTANCE) {
                continue;
            }
            for (int[] dir : dirs) {
                Cell o = at.get(key(c.dx() + dir[0], c.dy() + dir[1], c.dz() + dir[2]));
                if (o != null && o.kind() == Kind.LEAVES && !dist.containsKey(o)) {
                    dist.put(o, d + 1);
                    queue.add(o);
                }
            }
        }
        Map<Cell, Integer> out = new LinkedHashMap<>();
        for (Cell c : cells) {
            if (c.kind() == Kind.LEAVES) {
                out.put(c, dist.getOrDefault(c, MAX_LEAF_DISTANCE));
            }
        }
        return out;
    }

    /** Rolls an ordinary tree: the cells in the order logs, leaves, fruits, each cell once. */
    public static List<Cell> generate(RandomGenerator rng) {
        return generate(rng, false);
    }

    /**
     * Rolls a tree. The ordinary one (grand = false) is exactly the tree of Phase 4g. The GRAND one (6h) has a trunk of
     * {@value #GRAND_MIN_LOGS} or one more logs, a rounded crown of radius {@value #GRAND_RADIUS} in two layers with a narrower third
     * layer on top of them, the same 3x3 and plus above, and 3 to {@value #GRAND_MAX_FRUITS} fruits under the lowest layer
     * (at least 2 blocks above the ground, so a player can still pick them).
     */
    public static List<Cell> generate(RandomGenerator rng, boolean grand) {
        int radius = grand ? GRAND_RADIUS : RADIUS;
        int logs = (grand ? GRAND_MIN_LOGS : MIN_LOGS) + rng.nextInt(2);
        Map<Long, Cell> cells = new LinkedHashMap<>();
        for (int dy = 0; dy < logs; dy++) {
            cells.put(key(0, dy, 0), new Cell(0, dy, 0, Kind.LOG));
        }
        int layers = grand ? 3 : 2;
        int low = logs - layers;                  // the lowest crown layer: the logs below the top, so the crown sits around the top of the trunk
        for (int layer = 0; layer < layers; layer++) {
            int dy = low + layer;
            int r = layer == 2 ? RADIUS : radius;   // only the grand tree has a third layer, and it is narrower
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    boolean corner = dx * dx + dz * dz > r * r + 1;   // for r = 2 exactly the four corners; for r = 3 a rounded disc
                    boolean edge = Math.max(Math.abs(dx), Math.abs(dz)) == r;
                    if (corner || cells.containsKey(key(dx, dy, dz))) {
                        continue;
                    }
                    if (layer >= 1 && edge && rng.nextInt(4) == 0) {
                        continue;                 // the upper layers are a little ragged at their edge (the inner ring is never trimmed, so it stays connected)
                    }
                    cells.put(key(dx, dy, dz), new Cell(dx, dy, dz, Kind.LEAVES));
                }
            }
        }
        int top = logs;                            // 3x3 on top of the trunk, corners by chance
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                if (corner && rng.nextBoolean()) {
                    continue;
                }
                cells.put(key(dx, top, dz), new Cell(dx, top, dz, Kind.LEAVES));
            }
        }
        int cap = logs + 1;                        // a plus above
        cells.put(key(0, cap, 0), new Cell(0, cap, 0, Kind.LEAVES));
        cells.put(key(1, cap, 0), new Cell(1, cap, 0, Kind.LEAVES));
        cells.put(key(-1, cap, 0), new Cell(-1, cap, 0, Kind.LEAVES));
        cells.put(key(0, cap, 1), new Cell(0, cap, 1, Kind.LEAVES));
        cells.put(key(0, cap, -1), new Cell(0, cap, -1, Kind.LEAVES));

        List<Cell> candidates = new ArrayList<>();
        for (Cell c : cells.values()) {
            if (c.kind() == Kind.LEAVES && c.dy() == low) {
                candidates.add(c);
            }
        }
        Collections.shuffle(candidates, new java.util.Random(rng.nextLong()));
        int fruits = grand ? 3 + rng.nextInt(GRAND_MAX_FRUITS - 2) : 1 + rng.nextInt(MAX_FRUITS);
        List<Cell> out = new ArrayList<>(cells.values());
        for (int i = 0; i < fruits && i < candidates.size(); i++) {
            Cell leaf = candidates.get(i);
            out.add(new Cell(leaf.dx(), leaf.dy() - 1, leaf.dz(), Kind.FRUIT));
        }
        return out;
    }
}
