package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * U1 (docs/WARREN_PLAN.md): the DEN under the tree of a level 1 warren and the MOUTHS that lead down into it, as offsets from the
 * trunk's floor: pure Java, covered by tools/GenomeCheck.java. Offsets are (dx, dy, dz) with (0, 0, 0) = the first air block above
 * the ground at the trunk (so the ground block of the trunk is dy -1).
 *
 * <p>The DEN is a room 5x5 wide and {@value #HEIGHT} high whose standing level is dy {@code -DEPTH}; two solid blocks of ceiling stay
 * under the grass. Its four corners hold the Nests (three or four of them, replacing the floor block), a hay block with a lantern on it
 * gives light, a second hay block is bedding.
 *
 * <p>A MOUTH is a small earth mound ({@value #MOUND_LENGTH} blocks long along an axis, 3 wide) with a doorway at its outer end, facing
 * away from the tree. Inside, a slope of one-block steps (one down per block, 3 high so that a player or a dog can jump up a step under the ceiling, 1 wide) runs back toward the trunk and down to the
 * den's floor; it enters the den in the middle of one side. The door is at "radial" distance R from the trunk (R from
 * {@value #MIN_RADIUS} to {@value #MAX_RADIUS}): on flat ground R = {@value #MIN_RADIUS}; if the ground at the door is {@code doorDy}
 * higher than at the trunk the slope needs R >= {@code 7 + doorDy} to reach the den floor ({@link #canReachDen}); if it is lower, the
 * slope reaches the floor early and runs level into the den.
 */
public final class DenShape {
    private DenShape() {}

    /** The den's interior reaches this far from the trunk column to each side (5x5). */
    public static final int HALF = 2;
    /** The den's floor is this far below the trunk's floor level (the standing level is dy -DEPTH). */
    public static final int DEPTH = 5;
    /** The den's interior is this high (standing level up to dy -DEPTH + HEIGHT - 1; the lining is the next layer). */
    public static final int HEIGHT = 3;
    /** Distance of the door from the trunk column on flat ground, and the farthest a door is put (uneven ground). */
    public static final int MIN_RADIUS = 7;
    public static final int MAX_RADIUS = 9;
    /** The mound covers this many columns along the axis (the door column and the three behind it), 3 across. */
    public static final int MOUND_LENGTH = 4;
    public static final int MIN_NESTS = 3;
    public static final int MAX_NESTS = 4;
    /** A level 1 warren gets this many mouths (fewer if the ground allows no more). */
    public static final int MOUTHS = 2;

    public enum Dir {
        EAST(1, 0), WEST(-1, 0), SOUTH(0, 1), NORTH(0, -1);

        public final int dx;
        public final int dz;

        Dir(int dx, int dz) {
            this.dx = dx;
            this.dz = dz;
        }
    }

    /** What a block of the shape is made of; the world code turns each into a block of the settlement's style. */
    public enum Kind {
        /** Nothing: dig it. */ AIR,
        /** The ring of the mound ({@code Role.WALL}). */ WALL,
        /** The roof and ridge of the mound ({@code Role.ROOF}). */ ROOF,
        /** A guava log: the door posts and the lintel. */ BEAM,
        /** The style's fill block: the den's ceiling lining. */ FILL,
        /** A Nest block. */ NEST,
        /** A hay block of the style. */ HAY,
        /** A lantern standing on the block below it. */ LANTERN,
        // Append only: saved underground cell tuples use these ordinals.
        BARREL, WATER, GLASS, ROOTS, HATCH_X, HATCH_Z, CEILING_LANTERN
    }

    public record Cell(int dx, int dy, int dz, Kind kind) {}

    /** A column (dx, dz) from the trunk. */
    public record Pos(int dx, int dz) {}

    /**
     * One den: the order in which the four sides are tried for a mouth, and the furnishing (nests, hay, lantern).
     *
     * @param dirs       the four directions, shuffled: the first {@value #MOUTHS} that fit the ground get a mouth
     * @param furnishing nests (at dy {@code -DEPTH - 1}, the floor block), hay (at the standing level) and the lantern on a hay block
     */
    public record Den(List<Dir> dirs, List<Cell> furnishing) {
        public List<Pos> nestColumns() {
            List<Pos> out = new ArrayList<>();
            for (Cell c : furnishing) {
                if (c.kind() == Kind.NEST) {
                    out.add(new Pos(c.dx(), c.dz()));
                }
            }
            return out;
        }
    }

    public static Den generate(RandomGenerator rng) {
        java.util.Random shuffler = new java.util.Random(rng.nextLong());
        List<Dir> dirs = new ArrayList<>(List.of(Dir.values()));
        Collections.shuffle(dirs, shuffler);
        List<Pos> corners = new ArrayList<>(List.of(new Pos(HALF, HALF), new Pos(HALF, -HALF), new Pos(-HALF, HALF), new Pos(-HALF, -HALF)));
        Collections.shuffle(corners, shuffler);
        int nests = MIN_NESTS + rng.nextInt(MAX_NESTS - MIN_NESTS + 1);
        List<Cell> furnishing = new ArrayList<>();
        for (int i = 0; i < nests; i++) {
            Pos c = corners.get(i);
            furnishing.add(new Cell(c.dx(), -DEPTH - 1, c.dz(), Kind.NEST));
        }
        // bedding and light on the sides that are not the middle of a side (the middles are the doors of the mouths)
        furnishing.add(new Cell(1, -DEPTH, HALF, Kind.HAY));
        furnishing.add(new Cell(1, -DEPTH + 1, HALF, Kind.LANTERN));
        furnishing.add(new Cell(-1, -DEPTH, -HALF, Kind.HAY));
        return new Den(List.copyOf(dirs), List.copyOf(furnishing));
    }

    // ------------------------------------------------------------------ the den

    /** Every air block of the den's interior. */
    public static List<Cell> denAir() {
        List<Cell> out = new ArrayList<>();
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                for (int dy = -DEPTH; dy < -DEPTH + HEIGHT; dy++) {
                    out.add(new Cell(dx, dy, dz, Kind.AIR));
                }
            }
        }
        return List.copyOf(out);
    }

    /** The layer above the interior (dy {@code -DEPTH + HEIGHT}): lined with the style's fill block so no sand or gravel can drop in. */
    public static List<Cell> denLining() {
        List<Cell> out = new ArrayList<>();
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                out.add(new Cell(dx, -DEPTH + HEIGHT, dz, Kind.FILL));
            }
        }
        return List.copyOf(out);
    }

    // ------------------------------------------------------------------ a mouth

    /** The column at {@code u} columns from the trunk along {@code d} and {@code v} columns to the side. */
    public static Pos at(Dir d, int u, int v) {
        return new Pos(d.dx * u - d.dz * v, d.dz * u + d.dx * v);
    }

    /** True if a slope whose door is at radial distance {@code radial} and {@code doorDy} above the trunk's floor level reaches the den's floor by the den's edge. */
    public static boolean canReachDen(int doorDy, int radial) {
        return radial >= HALF + DEPTH + doorDy;
    }

    /** The standing level (dy from the trunk's floor) of the slope at radial distance {@code u}: one down per block, never below the den's floor. */
    public static int standDy(int doorDy, int radial, int u) {
        return Math.max(-DEPTH, doorDy - (radial - u));
    }

    /** The columns of the mound's footprint (the ground under them is levelled up to the door's level). */
    public static List<Pos> padColumns(Dir d, int radial) {
        List<Pos> out = new ArrayList<>();
        for (int u = radial - MOUND_LENGTH + 1; u <= radial; u++) {
            for (int v = -1; v <= 1; v++) {
                out.add(at(d, u, v));
            }
        }
        return List.copyOf(out);
    }

    /**
     * The solid blocks of a mound whose door is at radial distance {@code radial}, its ground at {@code doorDy}: two layers of wall round
     * the hollow (a guava log at each side of the door), a roof layer (a guava log lintel over the door) and a ridge of two more roof
     * blocks. The column behind the hollow is walled too, so the mound is closed except for the door.
     */
    public static List<Cell> mound(Dir d, int radial, int doorDy) {
        List<Cell> out = new ArrayList<>();
        for (int u = radial - MOUND_LENGTH + 1; u <= radial; u++) {
            for (int v = -1; v <= 1; v++) {
                Pos p = at(d, u, v);
                boolean hollow = v == 0 && u >= radial - 2;
                if (!hollow) {
                    Kind wall = u == radial && v != 0 ? Kind.BEAM : Kind.WALL;
                    out.add(new Cell(p.dx(), doorDy, p.dz(), wall));
                    out.add(new Cell(p.dx(), doorDy + 1, p.dz(), wall));
                }
                out.add(new Cell(p.dx(), doorDy + 2, p.dz(), u == radial && v == 0 ? Kind.BEAM : Kind.ROOF));
                if (v == 0 && (u == radial - 1 || u == radial - 2)) {
                    out.add(new Cell(p.dx(), doorDy + 3, p.dz(), Kind.ROOF));
                }
            }
        }
        return List.copyOf(out);
    }

    /**
     * The air of a mouth: the hollow inside the mound (the three door-side columns, from the standing level up to just under the roof)
     * and the covered slope behind it, down to (not including) the den's edge column. Each slope column is 3 high ({@code stand} to {@code stand + 2}; a step up needs the room above the lower column).
     */
    public static List<Cell> air(Dir d, int radial, int doorDy) {
        List<Cell> out = new ArrayList<>();
        for (int u = radial; u > HALF; u--) {
            Pos p = at(d, u, 0);
            int stand = standDy(doorDy, radial, u);
            int top = u >= radial - 2 ? doorDy + 1 : stand + 2;
            for (int dy = stand; dy <= top; dy++) {
                out.add(new Cell(p.dx(), dy, p.dz(), Kind.AIR));
            }
        }
        return List.copyOf(out);
    }

    /** The tunnel columns outside the mound (radial distance from {@code HALF + 1} to {@code radial - MOUND_LENGTH}), where the ceiling (at {@code stand + 3}) is ground. */
    public static List<Integer> tunnelRadials(int radial) {
        List<Integer> out = new ArrayList<>();
        for (int u = radial - MOUND_LENGTH; u > HALF; u--) {
            out.add(u);
        }
        return List.copyOf(out);
    }
}
