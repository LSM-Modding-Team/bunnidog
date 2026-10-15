package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * The burrow house of a settlement (Phase 6b), as offsets from the centre of its 3x3 footprint: pure Java, covered by
 * tools/GenomeCheck.java. A small earth mound: a ring of WALL blocks two high around a hollow centre, one ring cell facing the
 * door direction left OPEN (a doorway two high, the hollow behind it), a ROOF of 3x3 blocks on top and one more ROOF block in the
 * middle so the roof is a little hill. The world code ({@code worldgen/WarrenFeature}) turns WALL into coarse dirt, ROOF into a
 * grass block and OPEN into air. Base cell (0, 0, 0) is the centre column at ground level (the first air block above the ground).
 */
public final class HouseShape {
    private HouseShape() {}

    public enum Kind {
        WALL, ROOF, OPEN, WINDOW,
        /** V1a: a guava log (door posts, lintel, corner posts). */ BEAM,
        /** V1a: a guava plank (the upper part of a cottage wall, the middle of its roof). */ PLANK,
        /** V1a: a guava slab (the roof of a cottage). */ PLANK_SLAB,
        /** V1b: the lineage's stone (its ring block: cobblestone, stone bricks, sandstone, mud bricks ...) in the lowest wall layer: the foundation course. */ FOUNDATION
    }

    /**
     * The two forms of a house (Phase 6g). MOUND is the earth mound of Phase 6b. TOWER is a flat-roofed block three walls high with
     * glass windows in the middle of the three sides without the door and a battlement of four blocks on the roof corners.
     */
    public enum Form { MOUND, TOWER }

    /** The tower is this high (3 wall layers + the flat roof + the battlements = dy 0..4). */
    public static final int TOWER_HEIGHT = 5;

    /** One block of the house, relative to the centre of the footprint. */
    public record Cell(int dx, int dy, int dz, Kind kind) {}

    /** The house is this high (ground level + 2 wall layers + roof + the top of the hill = dy 0..3). */
    public static final int HEIGHT = 4;

    /**
     * The cells of a house whose door is on the side {@code (doorDx, doorDz)}, which must be one of (1,0), (-1,0), (0,1), (0,-1).
     * Order: walls, roof, then the open cells (so the world code can place them in this order).
     */
    public static List<Cell> cells(int doorDx, int doorDz) {
        return cells(doorDx, doorDz, Form.MOUND);
    }

    /** The cells of a house of the given form; same door rules and same order (walls, roof, open cells) as {@link #cells(int, int)}. */
    public static List<Cell> cells(int doorDx, int doorDz, Form form) {
        if (form == Form.TOWER) {
            return tower(doorDx, doorDz);
        }
        if (Math.abs(doorDx) + Math.abs(doorDz) != 1) {
            throw new IllegalArgumentException("the door must face one of the four sides, got " + doorDx + "," + doorDz);
        }
        List<Cell> walls = new ArrayList<>();
        List<Cell> roof = new ArrayList<>();
        List<Cell> open = new ArrayList<>();
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean centre = dx == 0 && dz == 0;
                    boolean door = dx == doorDx && dz == doorDz;
                    if (centre || door) {
                        open.add(new Cell(dx, dy, dz, Kind.OPEN));
                    } else {
                        walls.add(new Cell(dx, dy, dz, Kind.WALL));
                    }
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                roof.add(new Cell(dx, 2, dz, Kind.ROOF));
            }
        }
        roof.add(new Cell(0, 3, 0, Kind.ROOF));
        List<Cell> all = new ArrayList<>(walls);
        all.addAll(roof);
        all.addAll(open);
        return List.copyOf(all);
    }

    // ------------------------------------------------------------------ V1a: guava wood by development level

    /** V1a: no guava wood (the plain house of Phase 6b / 6g). */
    public static final int TIER_PLAIN = 0;
    /** V1a: level 2: the same house with a guava-log door frame (a post on each side of the door and a lintel over it). */
    public static final int TIER_FRAME = 1;
    /** V1a: level 3: the guava cottage (mound form) or the tower with guava corner posts (tower form). */
    public static final int TIER_COTTAGE = 2;
    /** V1b: level 4: the cottage on a foundation course of the lineage's stone (both forms). */
    public static final int TIER_FOUNDATION = 3;
    /** V1b: level 5, the village: the mound cottage grows a loft (a second room with windows under a higher roof); the tower has no loft and only keeps the foundation. */
    public static final int TIER_LOFT = 4;
    /** V1b: a mound house with a loft is this high (ground level + 3 wall layers + roof + the top of the hill = dy 0..4). */
    public static final int LOFT_HEIGHT = 5;

    /**
     * The wood tier of a house in a settlement of this development level (docs/FIXES_PLAN.md 5.2): nothing below level 2, a door frame at
     * level 2, the cottage at level 3, the cottage on a stone foundation at level 4, and at level 5 also a loft.
     */
    public static int tierFor(int level) {
        if (level <= 1) {
            return TIER_PLAIN;
        }
        if (level >= 5) {
            return TIER_LOFT;
        }
        return level - 1;
    }

    /**
     * The cells of a house of the given form AND wood tier: the same positions as {@link #cells(int, int, Form)} (so the footprint, the
     * height and every "does it fit" test are unchanged), with some kinds replaced by guava wood. Order is unchanged too.
     * <ul>
     * <li>FRAME (all forms): the two wall cells beside the door, at every height of the wall, become BEAM; so does the cell over the door.</li>
     * <li>COTTAGE, mound: also the other two corners are BEAM; the upper wall layer (dy 1) of the four side cells is PLANK (the lower
     * layer stays the lineage's wall); the roof (dy 2) is PLANK_SLAB with a PLANK block in the middle and one PLANK_SLAB on top of it.</li>
     * <li>COTTAGE, tower: also the two back corners are BEAM, so all four corner posts are guava logs, three high.</li>
     * <li>FOUNDATION (tier 3 and up, both forms): the wall cells of the lowest layer that are still plain WALL become FOUNDATION (stone).</li>
     * <li>LOFT (tier 4, mound only; see {@link #loft}): a different shape, {@link #LOFT_HEIGHT} high.</li>
     * </ul>
     */
    public static List<Cell> cells(int doorDx, int doorDz, Form form, int tier) {
        List<Cell> base = cells(doorDx, doorDz, form);
        if (tier <= TIER_PLAIN) {
            return base;
        }
        if (tier >= TIER_LOFT && form == Form.MOUND) {
            return loft(doorDx, doorDz);
        }
        int wallTop = form == Form.TOWER ? 2 : 1;
        List<Cell> out = new ArrayList<>(base.size());
        for (Cell c : base) {
            out.add(new Cell(c.dx(), c.dy(), c.dz(), dress(c, doorDx, doorDz, form, tier, wallTop)));
        }
        return List.copyOf(out);
    }

    private static Kind dress(Cell c, int doorDx, int doorDz, Form form, int tier, int wallTop) {
        Kind k = c.kind();
        if (k == Kind.OPEN) {
            return k;
        }
        boolean corner = Math.abs(c.dx()) == 1 && Math.abs(c.dz()) == 1;
        boolean doorSide = doorDx != 0 ? c.dx() == doorDx : c.dz() == doorDz;
        boolean lintel = c.dx() == doorDx && c.dz() == doorDz && c.dy() == 2;
        if (lintel) {
            return Kind.BEAM;
        }
        if (k == Kind.WALL && corner && c.dy() <= wallTop && (doorSide || tier >= TIER_COTTAGE)) {
            return Kind.BEAM;
        }
        if (tier >= TIER_FOUNDATION && k == Kind.WALL && c.dy() == 0) {
            return Kind.FOUNDATION;
        }
        if (tier >= TIER_COTTAGE && form == Form.MOUND) {
            if (k == Kind.WALL && c.dy() == 1) {
                return Kind.PLANK;
            }
            if (k == Kind.ROOF) {
                return c.dx() == 0 && c.dz() == 0 && c.dy() == 2 ? Kind.PLANK : Kind.PLANK_SLAB;
            }
        }
        return k;
    }

    /**
     * V1b: the mound cottage with a loft (the house of a level 5 village), {@link #LOFT_HEIGHT} high on the same 3x3 footprint. Layers:
     * dy 0 and 1 are the foundation cottage (log corners, stone foundation / planks, the doorway and the hollow open); dy 2 is the loft
     * (log corners, the log lintel over the door, a glass window in the middle of the three other sides, the hollow open so the loft is
     * one room with the ground floor); dy 3 is the roof of guava slabs with one plank block in the middle; dy 4 is one more slab on top.
     * Order: walls, roof, open cells, like the plain house.
     */
    private static List<Cell> loft(int doorDx, int doorDz) {
        List<Cell> below = cells(doorDx, doorDz, Form.MOUND, TIER_FOUNDATION);
        List<Cell> walls = new ArrayList<>();
        List<Cell> roof = new ArrayList<>();
        List<Cell> open = new ArrayList<>();
        for (Cell c : below) {
            if (c.dy() >= 2) {
                continue;   // the old roof is replaced by the loft and its own roof
            }
            (c.kind() == Kind.OPEN ? open : walls).add(c);
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean centre = dx == 0 && dz == 0;
                boolean corner = dx != 0 && dz != 0;
                boolean door = dx == doorDx && dz == doorDz;
                if (centre) {
                    open.add(new Cell(dx, 2, dz, Kind.OPEN));
                } else if (corner || door) {
                    walls.add(new Cell(dx, 2, dz, Kind.BEAM));
                } else {
                    walls.add(new Cell(dx, 2, dz, Kind.WINDOW));
                }
                roof.add(new Cell(dx, 3, dz, centre ? Kind.PLANK : Kind.PLANK_SLAB));
            }
        }
        roof.add(new Cell(0, 4, 0, Kind.PLANK_SLAB));
        List<Cell> all = new ArrayList<>(walls);
        all.addAll(roof);
        all.addAll(open);
        return List.copyOf(all);
    }

    private static List<Cell> tower(int doorDx, int doorDz) {
        if (Math.abs(doorDx) + Math.abs(doorDz) != 1) {
            throw new IllegalArgumentException("the door must face one of the four sides, got " + doorDx + "," + doorDz);
        }
        List<Cell> walls = new ArrayList<>();
        List<Cell> roof = new ArrayList<>();
        List<Cell> open = new ArrayList<>();
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean centre = dx == 0 && dz == 0;
                    boolean door = dx == doorDx && dz == doorDz;
                    boolean middleOfSide = Math.abs(dx) + Math.abs(dz) == 1;
                    if (centre || (door && dy <= 1)) {
                        open.add(new Cell(dx, dy, dz, Kind.OPEN));
                    } else if (middleOfSide && !door && dy == 1) {
                        walls.add(new Cell(dx, dy, dz, Kind.WINDOW));
                    } else {
                        walls.add(new Cell(dx, dy, dz, Kind.WALL));
                    }
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                roof.add(new Cell(dx, 3, dz, Kind.ROOF));
            }
        }
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                roof.add(new Cell(dx, 4, dz, Kind.ROOF));
            }
        }
        List<Cell> all = new ArrayList<>(walls);
        all.addAll(roof);
        all.addAll(open);
        return List.copyOf(all);
    }
}
