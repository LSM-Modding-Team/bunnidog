package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * A settlement of wild bunnidogs (Phase 6a): one or more {@link WarrenShape} clusters, each with its own guava tree. The
 * DEVELOPMENT LEVEL of a settlement IS its number of guava trees (the user's rule): level 1 is a lone tree with a few
 * bunnidogs (the warren of Phase 4g), level {@value #MAX_LEVEL} is the largest. Phase 6b adds burrow houses ({@link HouseShape})
 * to settlements of two or more trees: {@value #HOUSES_PER_LEVEL} per tree beyond the first. Later chunks add paths, a market and
 * the rest of the village; this class decides how many trees and houses there are and where they stand. Pure Java, covered by
 * tools/GenomeCheck.java.
 *
 * <p>Clusters stand on a fixed pattern of anchors (a plus sign, {@value #SPACING} blocks between neighbours), turned by a random
 * quarter turn, each moved by at most {@value #JITTER} block. Cluster 0 is always the centre (0, 0); the world code builds it
 * first and gives up the whole settlement if it does not fit, while every other cluster is best-effort (it is skipped when the
 * ground there is not flat or the trunk has no room, so the REAL level of a settlement is the number of trees that were built).
 *
 * @param level    the rolled development level, 1 to {@value #MAX_LEVEL}
 * @param clusters one per tree, the centre first
 * @param props    the landmarks ({@link PropShape#kindsFor(int)} of the rolled level, in that order; one is left out if no spot is free)
 * @param houses   candidate spots for burrow houses, best first (empty for level 1); the world code builds the first
 *                 {@link #housesFor(int)} of them that fit, the others are spares for uneven ground
 */
public record Settlement(int level, List<Cluster> clusters, List<House> houses, List<Prop> props) {
    /** A landmark (Phase 6e, {@link PropShape}) whose 3x3 footprint is centred at (dx, dz), turned {@code turns} quarter turns. */
    public record Prop(PropShape.Kind kind, int dx, int dz, int turns) {}

    /** One tree with its patch, burrows and bunnidogs, standing at (dx, dz) from the centre of the settlement. */
    public record Cluster(int dx, int dz, WarrenShape shape) {}

    /** A burrow house with the centre of its 3x3 footprint at (dx, dz) from the centre of the settlement; its door faces (doorDx, doorDz). */
    public record House(int dx, int dz, int doorDx, int doorDz) {}

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    /** Blocks between a cluster and its neighbours on the pattern. */
    public static final int SPACING = 8;
    /** Each anchor is moved by up to this many blocks along each axis. */
    public static final int JITTER = 1;
    /**
     * Nothing of a settlement is ever further than this from its centre (anchor + jitter + the farthest thing of a cluster).
     * A worldgen feature may only touch the 3x3 chunks around the chunk it runs in, which is at least 16 blocks from its start
     * position in every direction; staying inside 12 leaves a margin. Bigger settlements need a different mechanism (a Structure).
     */
    public static final int MAX_EXTENT = 12;
    /** Burrow houses of a settlement: this many for every tree beyond the first (2 trees = 2 houses ... 5 trees = 8). */
    public static final int HOUSES_PER_LEVEL = 2;
    public static final int MAX_HOUSES = HOUSES_PER_LEVEL * (MAX_LEVEL - 1);
    /** A house centre is at least this far (in the larger of dx and dz) from every trunk: the yard of a tree reaches 3, the house 1, and one block stays free. */
    public static final int HOUSE_CLEARANCE = 6;
    /** Two house centres are at least this far apart: a 3-wide house plus one free block. */
    public static final int HOUSE_SPACING = 4;

    /** The weight of each level in the roll, index 0 = level 1: 55% a lone tree, down to 2% for the largest. */
    public static final int[] WEIGHTS = {55, 25, 12, 6, 2};

    /** Where cluster i stands before the quarter turn, i = 0..4: the centre, then east, west, south, north. */
    private static final int[][] ANCHORS = {{0, 0}, {SPACING, 0}, {-SPACING, 0}, {0, SPACING}, {0, -SPACING}};

    /** How many burrow houses a settlement with this many trees (really built) gets: 0, 2, 4, 6, 8. */
    public static int housesFor(int trees) {
        return Math.max(0, Math.min(MAX_HOUSES, HOUSES_PER_LEVEL * (trees - 1)));
    }

    /** A development level, 1 to 5, by the weights above. */
    public static int rollLevel(RandomGenerator rng) {
        int total = 0;
        for (int w : WEIGHTS) {
            total += w;
        }
        int roll = rng.nextInt(total);
        for (int i = 0; i < WEIGHTS.length; i++) {
            roll -= WEIGHTS[i];
            if (roll < 0) {
                return i + MIN_LEVEL;
            }
        }
        return MIN_LEVEL;
    }

    /** A settlement of a random level. */
    public static Settlement generate(RandomGenerator rng) {
        return generate(rng, rollLevel(rng));
    }

    /** A settlement of the given level (clamped to 1..5): that many clusters, the centre first. */
    public static Settlement generate(RandomGenerator rng, int level) {
        int n = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        int turns = rng.nextInt(4);
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int x = ANCHORS[i][0];
            int z = ANCHORS[i][1];
            for (int t = 0; t < turns; t++) {
                int nx = -z;
                z = x;
                x = nx;
            }
            if (i > 0) {
                x += rng.nextInt(2 * JITTER + 1) - JITTER;
                z += rng.nextInt(2 * JITTER + 1) - JITTER;
            }
            boolean meetingTree = i == 0 && n == MAX_LEVEL;   // 6h: the middle tree of a village (level 5) is the grand one
            clusters.add(new Cluster(x, z, WarrenShape.generate(rng, meetingTree)));
        }
        List<Prop> props = props(rng, n, clusters);
        return new Settlement(n, List.copyOf(clusters), n >= 2 ? houses(rng, clusters, props) : List.of(), props);
    }

    /** A landmark centre is at least this far (larger of dx and dz) from every trunk: the yard reaches 3, the 3x3 landmark 1, one block stays free. */
    public static final int PROP_CLEARANCE = 5;
    /** ... from every other landmark and from every burrow house (two 3x3 footprints and a free block between them). */
    public static final int PROP_SPACING = 4;
    /** ... and from every block of the spokes between the trees (the footprints must not touch). */
    public static final int PROP_PATH_GAP = 2;

    /**
     * The landmarks of a settlement of this level: the free spots (clear of trunks and of the spokes between the trees, inside the
     * safe extent) nearest to the centre first, so the well and the plaza stand near the middle; each kind of
     * {@link PropShape#kindsFor(int)} takes the next spot that is far enough from the landmarks already placed. A kind with no spot is left out.
     * The landmarks are placed BEFORE the houses (the houses keep away from them), so the largest settlements always have room for all of them.
     */
    private static List<Prop> props(RandomGenerator rng, int level, List<Cluster> clusters) {
        List<PropShape.Kind> kinds = PropShape.kindsFor(level);
        if (kinds.isEmpty()) {
            return List.of();
        }
        List<PathShape.Cell> path = PathShape.network(clusters, List.of());
        int reach = MAX_EXTENT - 1 - PropShape.RADIUS;
        List<int[]> spots = new ArrayList<>(); // {dx, dz, distance from the centre * 4 + noise}
        for (int x = -reach; x <= reach; x++) {
            for (int z = -reach; z <= reach; z++) {
                if (clear(x, z, clusters, path)) {
                    spots.add(new int[] {x, z, (x * x + z * z) * 4 + rng.nextInt(4)});
                }
            }
        }
        spots.sort((a, b) -> Integer.compare(a[2], b[2]));
        List<Prop> props = new ArrayList<>();
        for (PropShape.Kind kind : kinds) {
            for (int[] s : spots) {
                boolean apart = true;
                for (Prop p : props) {
                    if (Math.max(Math.abs(s[0] - p.dx()), Math.abs(s[1] - p.dz())) < PROP_SPACING) {
                        apart = false;
                        break;
                    }
                }
                if (apart) {
                    props.add(new Prop(kind, s[0], s[1], rng.nextInt(4)));
                    break;
                }
            }
        }
        return List.copyOf(props);
    }

    private static boolean clear(int x, int z, List<Cluster> clusters, List<PathShape.Cell> path) {
        for (Cluster c : clusters) {
            if (Math.max(Math.abs(x - c.dx()), Math.abs(z - c.dz())) < PROP_CLEARANCE) {
                return false;
            }
        }
        for (PathShape.Cell c : path) {
            if (Math.max(Math.abs(x - c.dx()), Math.abs(z - c.dz())) < PROP_PATH_GAP) {
                return false;
            }
        }
        return true;
    }

    /**
     * Up to {@link #MAX_HOUSES} house spots: every column within {@code MAX_EXTENT - 1} of the centre (so the 3x3 house stays inside
     * {@link #MAX_EXTENT}) that is {@link #HOUSE_CLEARANCE} from every trunk, taken nearest to a trunk first (a little noise breaks
     * ties, so the houses huddle around the trees), each at least {@link #HOUSE_SPACING} from the ones already taken. The door faces
     * the nearest trunk.
     */
    private static List<House> houses(RandomGenerator rng, List<Cluster> clusters, List<Prop> props) {
        int reach = MAX_EXTENT - 1;
        List<int[]> candidates = new ArrayList<>(); // {dx, dz, nearest trunk distance * 4 + noise}
        for (int x = -reach; x <= reach; x++) {
            for (int z = -reach; z <= reach; z++) {
                int nearest = Integer.MAX_VALUE;
                for (Cluster c : clusters) {
                    nearest = Math.min(nearest, Math.max(Math.abs(x - c.dx()), Math.abs(z - c.dz())));
                }
                boolean freeOfProps = true;
                for (Prop p : props) {
                    if (Math.max(Math.abs(x - p.dx()), Math.abs(z - p.dz())) < PROP_SPACING) {
                        freeOfProps = false;
                    }
                }
                if (nearest >= HOUSE_CLEARANCE && freeOfProps) {
                    candidates.add(new int[] {x, z, nearest * 4 + rng.nextInt(4)});
                }
            }
        }
        candidates.sort((a, b) -> Integer.compare(a[2], b[2]));
        List<House> houses = new ArrayList<>();
        for (int[] c : candidates) {
            if (houses.size() == MAX_HOUSES) {
                break;
            }
            boolean apart = true;
            for (House h : houses) {
                if (Math.max(Math.abs(c[0] - h.dx()), Math.abs(c[1] - h.dz())) < HOUSE_SPACING) {
                    apart = false;
                    break;
                }
            }
            if (apart) {
                houses.add(doorTowardsNearestTrunk(c[0], c[1], clusters));
            }
        }
        return List.copyOf(houses);
    }

    private static House doorTowardsNearestTrunk(int x, int z, List<Cluster> clusters) {
        Cluster nearest = clusters.get(0);
        int best = Integer.MAX_VALUE;
        for (Cluster c : clusters) {
            int d = Math.max(Math.abs(x - c.dx()), Math.abs(z - c.dz()));
            if (d < best) {
                best = d;
                nearest = c;
            }
        }
        int tx = nearest.dx() - x;
        int tz = nearest.dz() - z;
        return Math.abs(tx) >= Math.abs(tz) ? new House(x, z, Integer.signum(tx), 0) : new House(x, z, 0, Integer.signum(tz));
    }
}
