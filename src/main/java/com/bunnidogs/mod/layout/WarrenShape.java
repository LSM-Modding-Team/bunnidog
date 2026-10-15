package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * The layout of a warren (Phase 4g), as offsets (east, south) from the tree's base: pure Java, covered by tools/GenomeCheck.java.
 * A warren is one guava tree in the middle, a patch of worn earth around it, 3 or 4 burrow holes a few blocks out and 2 to 4
 * wild bunnidogs standing around. The world code ({@code worldgen/WarrenFeature}) finds the ground for each spot.
 *
 * @param tree    the guava tree ({@link TreeShape})
 * @param patch   the columns whose grass or dirt becomes coarse dirt (a disc of radius about 2)
 * @param burrows the columns that get a hole 2 deep; never next to each other, and never under the trunk or a low-hanging fruit
 * @param dogs    the columns the bunnidogs are put on; never under the trunk or a low-hanging fruit, and never on a burrow
 * @param nests   H3 (docs/HOME_SPECS.md): one column per burrow, right next to its hole, that gets a Nest block (the home a wild dog
 *                claims); never the trunk, a burrow, a dog's spot or another nest, never where the tree hangs within 2 of the ground
 *                (a dog stands ON the nest and needs two free blocks above it), and at most {@link #NEST_MAX_REACH} columns from the trunk
 *                (so a village's landmarks, which keep 4 columns from every trunk, never meet one)
 */
public record WarrenShape(List<TreeShape.Cell> tree, List<Spot> patch, List<Spot> burrows, List<Spot> dogs, List<Spot> nests) {
    /** A column next to the tree: dx to the east, dz to the south. */
    public record Spot(int dx, int dz) {
        int distanceSquared() {
            return dx * dx + dz * dz;
        }
    }

    public static final int MIN_BURROWS = 3;
    public static final int MAX_BURROWS = 4;
    public static final int MIN_DOGS = 2;
    public static final int MAX_DOGS = 4;
    /** The patch is every column within this squared distance of the trunk (5 = a disc of radius 2.2: 21 columns). */
    public static final int PATCH_DISTANCE_SQUARED = 5;
    /** Burrows are 2 to 3 blocks from the trunk (squared distance 4 to 10). */
    public static final int BURROW_MIN_SQUARED = 4;
    public static final int BURROW_MAX_SQUARED = 10;
    /** Bunnidogs stand within 3 blocks of the trunk. */
    public static final int DOG_MAX_SQUARED = 9;
    /** H3: a nest is at most this many columns (larger of dx and dz) from the trunk. */
    public static final int NEST_MAX_REACH = 3;

    public static WarrenShape generate(RandomGenerator rng) {
        return generate(rng, false);
    }

    /** As {@link #generate(RandomGenerator)}; {@code grand} gives the cluster the meeting tree of a village ({@link TreeShape#generate(RandomGenerator, boolean)}, 6h). */
    public static WarrenShape generate(RandomGenerator rng, boolean grand) {
        java.util.Random shuffler = new java.util.Random(rng.nextLong());
        List<TreeShape.Cell> tree = TreeShape.generate(rng, grand);
        // the columns where the tree has something within reach of the ground (the trunk, and a fruit that hangs low next to it):
        // nothing else is put there
        Set<Spot> blocked = new HashSet<>();
        for (TreeShape.Cell c : tree) {
            if (c.dy() <= 1) {
                blocked.add(new Spot(c.dx(), c.dz()));
            }
        }
        List<Spot> patch = new ArrayList<>();
        List<Spot> ring = new ArrayList<>();
        List<Spot> near = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                Spot s = new Spot(dx, dz);
                int d2 = s.distanceSquared();
                if (d2 <= PATCH_DISTANCE_SQUARED) {
                    patch.add(s);
                }
                if (blocked.contains(s)) {
                    continue;
                }
                if (d2 >= BURROW_MIN_SQUARED && d2 <= BURROW_MAX_SQUARED) {
                    ring.add(s);
                }
                if (d2 > 0 && d2 <= DOG_MAX_SQUARED) {
                    near.add(s);
                }
            }
        }
        Collections.shuffle(ring, shuffler);
        int wanted = MIN_BURROWS + rng.nextInt(MAX_BURROWS - MIN_BURROWS + 1);
        List<Spot> burrows = new ArrayList<>();
        for (Spot s : ring) {
            if (burrows.size() == wanted) {
                break;
            }
            boolean apart = true;
            for (Spot other : burrows) {
                if (Math.max(Math.abs(s.dx() - other.dx()), Math.abs(s.dz() - other.dz())) < 2) {
                    apart = false;
                }
            }
            if (apart) {
                burrows.add(s);
            }
        }
        Set<Spot> taken = new HashSet<>(burrows);
        Collections.shuffle(near, shuffler);
        int dogCount = MIN_DOGS + rng.nextInt(MAX_DOGS - MIN_DOGS + 1);
        List<Spot> dogs = new ArrayList<>();
        for (Spot s : near) {
            if (dogs.size() == dogCount) {
                break;
            }
            if (!taken.contains(s)) {
                dogs.add(s);
            }
        }
        return new WarrenShape(tree, patch, burrows, dogs, nests(tree, burrows, dogs, shuffler));
    }

    /**
     * H3: the nest columns. Each burrow, in order, takes the free neighbour (the four columns beside its hole) that is nearest the trunk,
     * so the nests sit inward, among the dogs; a burrow with no free neighbour gets none. Uses {@code shuffler} only AFTER the tree,
     * the burrows and the dogs were chosen, so those are the same as before H3 for the same dice.
     */
    private static List<Spot> nests(List<TreeShape.Cell> tree, List<Spot> burrows, List<Spot> dogs, java.util.Random shuffler) {
        Set<Spot> low = new HashSet<>();   // the tree is within 2 of the ground here: no room for a dog on top of a nest
        for (TreeShape.Cell c : tree) {
            if (c.dy() <= 2) {
                low.add(new Spot(c.dx(), c.dz()));
            }
        }
        Set<Spot> occupied = new HashSet<>(burrows);
        occupied.addAll(dogs);
        int[][] beside = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<Spot> nests = new ArrayList<>();
        for (Spot burrow : burrows) {
            List<Spot> options = new ArrayList<>();
            for (int[] step : beside) {
                Spot s = new Spot(burrow.dx() + step[0], burrow.dz() + step[1]);
                if (low.contains(s) || occupied.contains(s) || nests.contains(s)) {
                    continue;
                }
                if (Math.max(Math.abs(s.dx()), Math.abs(s.dz())) > NEST_MAX_REACH) {
                    continue;
                }
                options.add(s);
            }
            Collections.shuffle(options, shuffler);
            Spot best = null;
            for (Spot s : options) {
                if (best == null || s.distanceSquared() < best.distanceSquared()) {
                    best = s;
                }
            }
            if (best != null) {
                nests.add(best);
            }
        }
        return nests;
    }
}
