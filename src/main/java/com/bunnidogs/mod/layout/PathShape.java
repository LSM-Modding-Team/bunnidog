package com.bunnidogs.mod.layout;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The footpaths of a settlement (Phase 6c), as columns (dx, dz) from the centre of the settlement: pure Java, covered by
 * tools/GenomeCheck.java. Paths only exist from {@value #MIN_TREES} guava trees on (the user's plan: a lone tree or a pair is a
 * warren, a bigger settlement has a street plan). They are worked out AFTER the world code knows which trees and houses were really
 * built, so a path never leads to a house that did not fit.
 *
 * <p>The network: a SPOKE from the centre tree to every other tree (a straight run along the longer axis, with the small jog of
 * the jittered anchor at the far end), then for every house a short path from the block in front of its door to the nearest block
 * of the network built so far (it leaves the door straight, then turns). So the houses hang off the spokes like side streets.
 *
 * <p>Kept free of path: the 3x3 of every house (the roof is a grass block and would turn into path), the burrow holes of the trees
 * (a hole is not ground) and the trunk with one block around it (the path ends at the edge of the tree). Nothing is ever further
 * than {@link Settlement#MAX_EXTENT} from the centre. The world code decides block by block where there is ground to lay it on.
 */
public final class PathShape {
    private PathShape() {}

    /** A column of the network: dx to the east, dz to the south of the centre of the settlement. */
    public record Cell(int dx, int dz) {}

    /** Paths exist from this many trees (really built) on. */
    public static final int MIN_TREES = 3;
    /** Columns this close (the larger of dx and dz) to a trunk stay free: the path stops at the edge of the tree. */
    public static final int TRUNK_CLEARANCE = 1;

    /**
     * The path columns of a settlement whose trees and houses were really built. {@code trees} are the clusters that were built,
     * the centre first; {@code houses} the houses that were built. Fewer than {@link #MIN_TREES} trees give no paths. The result has
     * no duplicates; spokes come first, then the house paths in the order of the houses.
     */
    public static List<Cell> network(List<Settlement.Cluster> trees, List<Settlement.House> houses) {
        return network(trees, houses, List.of());
    }

    /** As {@link #network(List, List)}, and the 3x3 footprint of every landmark ({@link Settlement.Prop}, Phase 6e) is kept free as well. */
    public static List<Cell> network(List<Settlement.Cluster> trees, List<Settlement.House> houses, List<Settlement.Prop> props) {
        if (trees.size() < MIN_TREES) {
            return List.of();
        }
        Set<Cell> reserved = new HashSet<>();
        for (Settlement.Cluster tree : trees) {
            for (int x = -TRUNK_CLEARANCE; x <= TRUNK_CLEARANCE; x++) {
                for (int z = -TRUNK_CLEARANCE; z <= TRUNK_CLEARANCE; z++) {
                    reserved.add(new Cell(tree.dx() + x, tree.dz() + z));
                }
            }
            for (WarrenShape.Spot burrow : tree.shape().burrows()) {
                reserved.add(new Cell(tree.dx() + burrow.dx(), tree.dz() + burrow.dz()));
            }
        }
        for (Settlement.House house : houses) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    reserved.add(new Cell(house.dx() + x, house.dz() + z));
                }
            }
        }
        for (Settlement.Prop prop : props) {
            for (int x = -PropShape.RADIUS; x <= PropShape.RADIUS; x++) {
                for (int z = -PropShape.RADIUS; z <= PropShape.RADIUS; z++) {
                    reserved.add(new Cell(prop.dx() + x, prop.dz() + z));
                }
            }
        }
        Set<Cell> cells = new LinkedHashSet<>();
        Settlement.Cluster centre = trees.get(0);
        for (int i = 1; i < trees.size(); i++) {
            Settlement.Cluster tree = trees.get(i);
            boolean alongX = Math.abs(tree.dx() - centre.dx()) >= Math.abs(tree.dz() - centre.dz());
            route(cells, reserved, centre.dx(), centre.dz(), tree.dx(), tree.dz(), alongX);
        }
        for (Settlement.House house : houses) {
            int startX = house.dx() + 2 * house.doorDx();
            int startZ = house.dz() + 2 * house.doorDz();
            Cell target = nearest(cells, trees, startX, startZ);
            // leave the door straight; only turn first when the target lies BEHIND the door
            boolean doorAlongX = house.doorDx() != 0;
            int ahead = doorAlongX ? (target.dx() - startX) * house.doorDx() : (target.dz() - startZ) * house.doorDz();
            boolean alongX = ahead >= 0 ? doorAlongX : !doorAlongX;
            route(cells, reserved, startX, startZ, target.dx(), target.dz(), alongX);
        }
        return List.copyOf(cells);
    }

    /** The cell of the network nearest (Manhattan) to (x, z); with an empty network, the nearest trunk. */
    private static Cell nearest(Set<Cell> cells, List<Settlement.Cluster> trees, int x, int z) {
        Cell best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Cell c : cells) {
            int d = Math.abs(c.dx() - x) + Math.abs(c.dz() - z);
            if (d < bestDistance) {
                bestDistance = d;
                best = c;
            }
        }
        if (best != null) {
            return best;
        }
        for (Settlement.Cluster t : trees) {
            int d = Math.abs(t.dx() - x) + Math.abs(t.dz() - z);
            if (d < bestDistance) {
                bestDistance = d;
                best = new Cell(t.dx(), t.dz());
            }
        }
        return best;
    }

    /** An L-shaped run from (x, z) to (tx, tz), the whole of the first axis first; cells that are reserved or too far out are left out. */
    private static void route(Set<Cell> out, Set<Cell> reserved, int x, int z, int tx, int tz, boolean alongXFirst) {
        add(out, reserved, x, z);
        if (alongXFirst) {
            while (x != tx) {
                x += Integer.signum(tx - x);
                add(out, reserved, x, z);
            }
            while (z != tz) {
                z += Integer.signum(tz - z);
                add(out, reserved, x, z);
            }
        } else {
            while (z != tz) {
                z += Integer.signum(tz - z);
                add(out, reserved, x, z);
            }
            while (x != tx) {
                x += Integer.signum(tx - x);
                add(out, reserved, x, z);
            }
        }
    }

    private static void add(Set<Cell> out, Set<Cell> reserved, int x, int z) {
        Cell cell = new Cell(x, z);
        if (Math.max(Math.abs(x), Math.abs(z)) <= Settlement.MAX_EXTENT && !reserved.contains(cell)) {
            out.add(cell);
        }
    }
}
