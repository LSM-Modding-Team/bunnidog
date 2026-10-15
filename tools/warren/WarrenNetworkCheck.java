import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.HouseShape;
import com.bunnidogs.mod.layout.PathShape;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.layout.TunnelShape;
import com.bunnidogs.mod.layout.TunnelShape.Pos;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Behaviour checks against synthetic terrain; no Minecraft runtime or rendering required. */
public final class WarrenNetworkCheck {
    private static class Ground implements TunnelShape.Terrain {
        final Set<Pos> water = new HashSet<>();
        final Set<Pos> protectedBlocks = new HashSet<>();
        int height(int x, int z) { return 0; }
        public Integer surface(int x, int z) { return height(x, z); }
        public boolean diggable(Pos p) { return !water.contains(p) && !protectedBlocks.contains(p); }
        public boolean solid(Pos p) { return p.y() < height(p.x(), p.z()) && dry(p); }
        public boolean replaceable(Pos p) { return p.y() >= height(p.x(), p.z()) && diggable(p); }
        public boolean dry(Pos p) { return !water.contains(p); }
    }

    public static void main(String[] args) {
        int networks = 0;
        int cellarRamps = 0;
        for (int level = 2; level <= 5; level++) {
            int levelCellars = 0;
            for (long seed = 0; seed < 24; seed++) {
                Settlement settlement = Settlement.generate(new Random(seed), level);
                Ground terrain = new Ground();
                TunnelShape.Network network = TunnelShape.plan(settlement, seed, terrain);
                require(network.dens().isEmpty() || network.dens().size() == level, "Flat-ground tunnels must connect every planned den");
                require(network.equals(TunnelShape.plan(settlement, seed, terrain)), "Planning must be deterministic");
                try { reachable(network, terrain, true); }
                catch (AssertionError error) { throw new AssertionError("level " + level + ", seed " + seed + ": " + error.getMessage(), error); }
                chunkPartitions(network);
                checkCellars(settlement, network, terrain);
                levelCellars += network.cellars().size();
                for (Settlement.House house : settlement.houses().subList(0, Settlement.housesFor(level))) {
                    require(!TunnelShape.blocksHouse(network, house), "Network must leave planned houses and their doorsteps free");
                }
                if (!network.dens().isEmpty()) networks++;
            }
            require(levelCellars > 0, "Each settlement level should fit some cellar ramps");
            cellarRamps += levelCellars;
        }
        require(networks >= 85, "Most flat layouts must fit entrances around existing buildings");
        Settlement settlement = Settlement.generate(new Random(7), 5);
        Ground wet = new Ground();
        wet.water.add(new Pos(0, -5, 0));
        TunnelShape.Network avoided = TunnelShape.plan(settlement, 7, wet);
        require(!avoided.dens().contains(0), "A flooded central room must be omitted");
        require(avoided.cells().stream().noneMatch(c -> wet.water.contains(c.pos())), "No fluid may be overwritten");
        reachable(avoided, wet, false);

        Ground protectedTerrain = new Ground();
        protectedTerrain.protectedBlocks.add(new Pos(0, -4, 0));
        require(!TunnelShape.plan(settlement, 7, protectedTerrain).dens().contains(0), "A protected block must cancel its room");

        Ground blockedTrunk = new Ground();
        blockedTrunk.protectedBlocks.add(new Pos(0, 0, 0));
        require(!TunnelShape.plan(settlement, 7, blockedTrunk).dens().contains(0), "No den should be planned under a tree that cannot be placed");

        for (int axis = 0; axis < 4; axis++) {
            int direction = axis;
            Ground slope = new Ground() {
                @Override int height(int x, int z) {
                    return Math.floorDiv(switch (direction) { case 0 -> x; case 1 -> -x; case 2 -> z; default -> -z; }, 4);
                }
            };
            int fitted = 0;
            for (int seed = 0; seed < 8; seed++) {
                Settlement slopedLayout = Settlement.generate(new Random(seed), 5);
                TunnelShape.Network sloped = TunnelShape.plan(slopedLayout, seed, slope);
                if (sloped.dens().size() > 1) fitted++;
                reachable(sloped, slope, false);
                checkCellars(slopedLayout, sloped, slope);
            }
            require(fitted > 0, "Each slope direction should allow some village layouts; crowded ones may safely fall back");
        }

        Ground cliff = new Ground() {
            @Override int height(int x, int z) { return x >= 4 ? 8 : 0; }
        };
        reachable(TunnelShape.plan(settlement, 7, cliff), cliff, false);
        require(TunnelShape.plan(Settlement.generate(new Random(0), 1), 0, new Ground()).cells().isEmpty(), "Level 1 keeps its existing layout");
        System.out.println("Warren network OK: " + networks + " flat layouts, " + cellarRamps
                + " cellar ramps; sloped/cliff terrain, fluid/protected-block fallback, house/path clearance and chunk partitions");
    }

    private static void checkCellars(Settlement settlement, TunnelShape.Network network, Ground terrain) {
        for (HouseShape.Form form : HouseShape.Form.values()) {
            Map<Pos, DenShape.Kind> built = new HashMap<>();
            network.cells().forEach(c -> built.put(c.pos(), c.kind()));
            for (var cellar : network.cellars()) {
                Settlement.House house = settlement.houses().get(cellar.house());
                int top = cellar.doorstep().y();
                for (HouseShape.Cell c : HouseShape.cells(house.doorDx(), house.doorDz(), form, HouseShape.tierFor(settlement.level()))) {
                    built.put(new Pos(house.dx() + c.dx(), top + c.dy(), house.dz() + c.dz()),
                            c.kind() == HouseShape.Kind.OPEN ? DenShape.Kind.AIR : DenShape.Kind.FILL);
                }
                built.put(new Pos(house.dx(), top - 1, house.dz()), DenShape.Kind.NEST);
            }
            // Model the surface paving after the house pieces, respecting the exclusions consumed by both world generators.
            for (PathShape.Cell p : PathShape.network(settlement.clusters(), settlement.houses().subList(0, Settlement.housesFor(settlement.level())), settlement.props())) {
                if (!network.pathExclusions().contains(new DenShape.Pos(p.dx(), p.dz()))) {
                    built.put(new Pos(p.dx(), terrain.height(p.dx(), p.dz()) - 1, p.dz()), DenShape.Kind.FILL);
                }
            }
            for (var cellar : network.cellars()) {
                Pos previous = cellar.doorstep();
                require(standable(previous, built, terrain), "A cellar must leave its house's doorstep usable");
                for (Pos step : cellar.steps()) {
                    require(Math.abs(step.x() - previous.x()) + Math.abs(step.z() - previous.z()) == 1
                            && step.y() <= previous.y() && previous.y() - step.y() <= 1, "A cellar ramp must descend in single steps");
                    require(standable(step, built, terrain), "House or path generation closed a cellar step: " + step);
                    if (step.y() != previous.y()) require(!solid(new Pos(step.x(), step.y() + 2, step.z()), built, terrain), "A cellar jump needs three-high clearance");
                    previous = step;
                }
                require(previous.y() <= cellar.doorstep().y() - 3, "A cellar must lead underground");
            }
        }
    }

    /** Walk from every entrance: two-high clearance, solid support, and three-high clearance when jumping a step. */
    private static void reachable(TunnelShape.Network network, Ground terrain, boolean joined) {
        Map<Pos, DenShape.Kind> cells = new HashMap<>();
        network.cells().forEach(c -> {
            require(cells.put(c.pos(), c.kind()) == null, "Duplicate block position");
            require(Math.max(Math.abs(c.pos().x()), Math.abs(c.pos().z())) <= Settlement.MAX_EXTENT, "Plan outside safe extent");
        });
        for (TunnelShape.Mouth mouth : network.mouths()) {
            Set<Pos> seen = new HashSet<>();
            ArrayDeque<Pos> queue = new ArrayDeque<>();
            require(standable(mouth.doorstep(), cells, terrain), "Doorstep must be standable");
            seen.add(mouth.doorstep());
            queue.add(mouth.doorstep());
            while (!queue.isEmpty()) {
                Pos p = queue.remove();
                for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    for (int dy = -1; dy <= 1; dy++) {
                        Pos next = new Pos(p.x() + dir[0], p.y() + dy, p.z() + dir[1]);
                        if (Math.max(Math.abs(next.x()), Math.abs(next.z())) > Settlement.MAX_EXTENT || next.y() < -16 || next.y() > 16) continue;
                        if (!standable(next, cells, terrain)) continue;
                        Pos lower = dy > 0 ? p : next;
                        if (dy != 0 && solid(new Pos(lower.x(), lower.y() + 2, lower.z()), cells, terrain)) continue;
                        if (seen.add(next)) queue.add(next);
                    }
                }
            }
            // A disconnected component may have its own entrance; every entrance must at least lead into a den.
            require(network.cells().stream().filter(c -> c.kind() == DenShape.Kind.NEST)
                    .anyMatch(c -> seen.contains(new Pos(c.pos().x(), c.pos().y() + 1, c.pos().z()))), "An entrance leads to no nest");
            if (joined) for (TunnelShape.Cell c : network.cells()) if (c.kind() == DenShape.Kind.NEST) {
                require(seen.contains(new Pos(c.pos().x(), c.pos().y() + 1, c.pos().z())), "Dens must share one traversable network");
            }
        }
        Set<Pos> allReachable = new HashSet<>();
        // Walk from all entrances together to check every retained nest, including separate fitted components.
        ArrayDeque<Pos> queue = new ArrayDeque<>();
        network.mouths().forEach(m -> { allReachable.add(m.doorstep()); queue.add(m.doorstep()); });
        while (!queue.isEmpty()) {
            Pos p = queue.remove();
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) for (int dy = -1; dy <= 1; dy++) {
                Pos n = new Pos(p.x() + d[0], p.y() + dy, p.z() + d[1]);
                if (Math.max(Math.abs(n.x()), Math.abs(n.z())) > 12 || Math.abs(n.y()) > 16 || !standable(n, cells, terrain)) continue;
                Pos lower = dy > 0 ? p : n;
                if (dy != 0 && solid(new Pos(lower.x(), lower.y() + 2, lower.z()), cells, terrain)) continue;
                if (allReachable.add(n)) queue.add(n);
            }
        }
        for (TunnelShape.Cell c : network.cells()) if (c.kind() == DenShape.Kind.NEST) {
            require(allReachable.contains(new Pos(c.pos().x(), c.pos().y() + 1, c.pos().z())), "Unreachable nest at " + c.pos());
        }
    }

    private static boolean solid(Pos p, Map<Pos, DenShape.Kind> cells, Ground terrain) {
        DenShape.Kind kind = cells.get(p);
        return kind == null ? terrain.solid(p) : kind != DenShape.Kind.AIR;
    }

    private static boolean standable(Pos p, Map<Pos, DenShape.Kind> cells, Ground terrain) {
        return !solid(p, cells, terrain) && !solid(new Pos(p.x(), p.y() + 1, p.z()), cells, terrain)
                && solid(new Pos(p.x(), p.y() - 1, p.z()), cells, terrain);
    }

    private static void chunkPartitions(TunnelShape.Network network) {
        // Move the same settlement across all border positions, including negative chunk coordinates.
        for (int offset : new int[]{-17, -1, 0, 7, 15, 16}) {
            Map<Pos, Integer> writes = new HashMap<>();
            for (int cx = -3; cx <= 2; cx++) for (int cz = -3; cz <= 2; cz++) {
                for (TunnelShape.Cell cell : network.cells()) {
                    Pos p = cell.pos();
                    if (Math.floorDiv(p.x() + offset, 16) == cx && Math.floorDiv(p.z() - offset, 16) == cz) writes.merge(p, 1, Integer::sum);
                }
            }
            require(writes.size() == network.cells().size() && writes.values().stream().allMatch(n -> n == 1), "Every block must have exactly one owning chunk");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
