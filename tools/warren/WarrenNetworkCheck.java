import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.HouseShape;
import com.bunnidogs.mod.layout.HouseDetails;
import com.bunnidogs.mod.layout.PathShape;
import com.bunnidogs.mod.layout.PropShape;
import com.bunnidogs.mod.layout.RegionalFarms;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.layout.TunnelShape;
import com.bunnidogs.mod.layout.TunnelShape.Pos;
import com.bunnidogs.mod.layout.UndergroundRooms;
import com.bunnidogs.mod.layout.Wayfinding;
import com.bunnidogs.mod.layout.SettlementFixtures;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Behaviour checks against synthetic terrain; no Minecraft runtime or rendering required. */
public final class WarrenNetworkCheck {
    private static int nurseryFixtures;
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
        entranceTrails();
        for (int turns = 0; turns < 4; turns++) {
            for (var style : Lineage.values()) for (boolean garden : new boolean[]{false,true}) {
                var regional = RegionalFarms.cells(style,turns,garden);
                require(regional.stream().allMatch(c -> Math.abs(c.dx()) <= 1 && Math.abs(c.dz()) <= 1 && c.dy() >= -1 && c.dy() <= PropShape.TOP),
                        "Regional farms must fit the existing reservation");
                require(regional.stream().map(c -> new Pos(c.dx(),c.dy(),c.dz())).distinct().count() == regional.size(),"Regional cells cannot overlap");
                require(regional.stream().filter(c -> c.dy() == -1).count() == 9,"Every regional ground column needs support");
                require(regional.stream().filter(c -> c.mat() == PropShape.Mat.TROUGH).count() == (garden ? 1 : 0),"Garden opt-in controls the only trough");
                if (style == Lineage.MEADOW || style == Lineage.TUNDRA || style == Lineage.MOUNTAIN || style == Lineage.ISLAND)
                    require(regional.equals(PropShape.cells(PropShape.Kind.FARM,turns,garden)),"Ordinary agricultural layouts retain their geometry");
                else require(regional.stream().noneMatch(c -> c.mat() == PropShape.Mat.WATER),"Regional beds need no unsupported water source");
                var current = RegionalFarms.cells(style,turns,garden,true);
                if (style != Lineage.MEADOW) require(current.equals(regional),"Version six must retain other regional plots");
                else {
                    require(current.stream().allMatch(c -> Math.abs(c.dx()) <= 1 && Math.abs(c.dz()) <= 1 && c.dy() >= -1 && c.dy() <= 0),"Melon beds must fit the old reservation");
                    require(current.stream().map(c -> new Pos(c.dx(),c.dy(),c.dz())).distinct().count() == current.size(),"Melon bed cells cannot overlap");
                    require(current.stream().filter(c -> c.dy() == -1).count() == 9 && current.stream().filter(c -> c.mat() == PropShape.Mat.WATER).count() == 1,"Melon beds need all nine supports and one native water source");
                    var stems = current.stream().filter(c -> c.mat() == PropShape.Mat.MELON_STEM).toList();
                    var spaces = current.stream().filter(c -> c.mat() == PropShape.Mat.FRUIT_SPACE).toList();
                    require(stems.size() == 2 && spaces.size() == 2,"Meadow beds need two stems and two reserved fruit gaps");
                    require(spaces.stream().allMatch(p -> stems.stream().anyMatch(s -> Math.abs(s.dx()-p.dx())+Math.abs(s.dz()-p.dz()) == 1)),"Each rotated fruit gap must touch a stem");
                    require(current.stream().filter(c -> c.mat() == PropShape.Mat.TROUGH).count() == (garden ? 1 : 0),"Melon beds keep their optional single corner trough");
                }
            }
            var farm = PropShape.cells(PropShape.Kind.FARM,turns,true);
            require(farm.stream().filter(c -> c.mat() == PropShape.Mat.TROUGH).count() == 1,"Each rotated working farm needs one trough");
            require(farm.stream().allMatch(c -> Math.abs(c.dx()) <= 1 && Math.abs(c.dz()) <= 1),"Working farms must retain the old footprint");
            require(farm.stream().filter(c -> c.mat() == PropShape.Mat.WHEAT || c.mat() == PropShape.Mat.CARROTS
                    || c.mat() == PropShape.Mat.POTATOES).count() == 7,"The corner trough must retain seven crops");
        }
        int networks = 0;
        int cellarRamps = 0;
        Map<UndergroundRooms.Type, Integer> features = new java.util.EnumMap<>(UndergroundRooms.Type.class);
        Set<Wayfinding.Label> labels = java.util.EnumSet.noneOf(Wayfinding.Label.class);
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
                if (seed < 3) {
                    checkWayfinding(settlement,network,labels);
                    checkFixtures(settlement,network,terrain);
                }
                checkCellars(settlement, network, terrain);
                levelCellars += network.cellars().size();
                network.features().forEach(f -> features.merge(f.type(),1,Integer::sum));
                if (level == 5 && network.dens().contains(0)) {
                    long nursery = network.cells().stream().filter(c -> c.kind() == DenShape.Kind.NEST && c.pos().y() == -6
                            && Math.max(Math.abs(c.pos().x()),Math.abs(c.pos().z())) <= 3).count();
                    require(nursery == 8,"The grand-tree hall must keep eight nursery Nests");
                    if (network.features().stream().anyMatch(f -> f.type() == UndergroundRooms.Type.LOWER_HALL)) {
                        require(network.cells().stream().filter(c -> c.kind() == DenShape.Kind.NEST && c.pos().y() == -12).count() == 4,
                                "The lower commons must have four additional floor Nests");
                    }
                    Settlement.Cluster east = settlement.clusters().stream().max(java.util.Comparator.comparingInt(Settlement.Cluster::dx)).orElseThrow();
                    Settlement.Cluster south = settlement.clusters().stream().max(java.util.Comparator.comparingInt(Settlement.Cluster::dz)).orElseThrow();
                    require(network.cells().stream().anyMatch(c -> c.pos().equals(new Pos(east.dx()-3,-5,south.dz()-3)) && c.kind() == DenShape.Kind.AIR),
                            "A level-five village must have the outer tunnel ring, too");
                }
                for (Settlement.House house : settlement.houses().subList(0, Settlement.housesFor(level))) {
                    require(!TunnelShape.blocksHouse(network, house), "Network must leave planned houses and their doorsteps free");
                }
                if (!network.dens().isEmpty()) networks++;
            }
            require(levelCellars > 0, "Each settlement level should fit some cellar ramps");
            cellarRamps += levelCellars;
        }

        require(labels.containsAll(java.util.EnumSet.allOf(Wayfinding.Label.class)),"Representative fitted warrens must label all room/exit types: " + labels);
        require(nurseryFixtures > 0,"Representative grand-tree halls must include a fitted nursery where ordinary furniture still fits");
        require(networks >= 85, "Most flat layouts must fit entrances around existing buildings");
        for (UndergroundRooms.Type type : UndergroundRooms.Type.values()) require(features.getOrDefault(type,0) > 0, "Missing planned feature: " + type);
        Settlement settlement = Settlement.generate(new Random(7), 5);
        Ground wet = new Ground();
        wet.water.add(new Pos(0, -5, 0));
        TunnelShape.Network avoided = TunnelShape.plan(settlement, 7, wet);
        require(!avoided.dens().contains(0), "A flooded central room must be omitted");
        require(avoided.cells().stream().noneMatch(c -> wet.water.contains(c.pos())), "No fluid may be overwritten");
        reachable(avoided, wet, false);

        Ground deepProtected = new Ground();
        deepProtected.protectedBlocks.add(new Pos(0,-11,0));
        TunnelShape.Network upperOnly = TunnelShape.plan(settlement,7,deepProtected);
        require(upperOnly.features().stream().noneMatch(f -> f.type() == UndergroundRooms.Type.LOWER_HALL),
                "A protected lower room must leave the upper network without U7");
        require(upperOnly.cells().stream().noneMatch(c -> deepProtected.protectedBlocks.contains(c.pos())),"Do not touch protected deep blocks");
        reachable(upperOnly,deepProtected,false);
        Ground deepWet = new Ground();
        deepWet.water.add(new Pos(0,-11,0));
        TunnelShape.Network dryUpper = TunnelShape.plan(settlement,7,deepWet);
        require(dryUpper.features().stream().noneMatch(f -> f.type() == UndergroundRooms.Type.LOWER_HALL),"Deep fluid cancels only the lower commons");
        reachable(dryUpper,deepWet,false);

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
        for (int level = 1; level <= 5; level++) {
            Ground cave = new Ground() {
                @Override public boolean solid(Pos p) { return p.y() >= 6 || super.solid(p); }
                @Override public boolean replaceable(Pos p) { return p.y() < 6 && super.replaceable(p); }
            };
            for (int seed = 0; seed < 8; seed++) {
                TunnelShape.Network open = TunnelShape.planOpen(Settlement.generate(new Random(seed),level),seed,cave);
                if (seed == 1) require(!open.dens().isEmpty(), "Cave/Nether/End floors need accessible dens at level " + level);
                require(open.cells().stream().noneMatch(c -> c.kind() == DenShape.Kind.WALL || c.kind() == DenShape.Kind.ROOF || c.kind() == DenShape.Kind.BEAM), "Open entrances must not add surface mounds");
                try { reachable(open,cave,false); }
                catch (AssertionError error) { throw new AssertionError("open level " + level + ", seed " + seed + ": " + error.getMessage(),error); }
            }
        }
        System.out.println("Warren network OK: " + networks + " flat layouts, " + cellarRamps
                + " cellar ramps, " + features + "; terrain fallback, house/path clearance and chunk partitions");
    }

    private static void entranceTrails() {
        for (var direction : DenShape.Dir.values()) for (int radial = DenShape.MIN_RADIUS; radial <= DenShape.MAX_RADIUS; radial++)
            for (int side : new int[]{1,-1}) {
                var route = com.bunnidogs.mod.layout.EntranceTrail.route(direction,radial,side);
                require(new HashSet<>(route).size() == route.size(),"Entrance trails cannot double back");
                require(route.getFirst().equals(DenShape.at(direction,radial+1,0)),"Trails start outside the outward-facing door");
                var end = route.getLast();
                require(Math.abs(end.dx()) == 2 && Math.abs(end.dz()) == 2,"Trails reach the tree-patch edge without entering the low canopy");
                for (int i = 0; i < route.size(); i++) {
                    var p = route.get(i);
                    require(Math.max(Math.abs(p.dx()),Math.abs(p.dz())) <= com.bunnidogs.mod.layout.EntranceTrail.REACH,
                            "Trail writes must fit the new cluster bounding box");
                    require(!DenShape.padColumns(direction,radial).contains(p),"Trails must go around the complete mound footprint");
                    require(Math.max(Math.abs(p.dx()),Math.abs(p.dz())) > PathShape.TRUNK_CLEARANCE,"Trails preserve the trunk clearance");
                    if (i > 0) {
                        var before = route.get(i-1);
                        require(Math.abs(before.dx()-p.dx())+Math.abs(before.dz()-p.dz()) == 1,"Every trail is a connected walking route");
                    }
                }
            }
    }

    private static void checkWayfinding(Settlement settlement, TunnelShape.Network network, Set<Wayfinding.Label> labels) {
        var signs = Wayfinding.plan(settlement,network,17,-13);
        require(signs.equals(Wayfinding.plan(settlement,network,17,-13)),"Sign placement must be deterministic");
        require(signs.equals(Wayfinding.decode(Wayfinding.encode(signs))),"Saved sign plans must round-trip");
        require(Wayfinding.decode(new int[]{0,0,0,99,0,0,0}).isEmpty(),"Malformed/future sign tuples must be ignored");
        Map<Pos,DenShape.Kind> cells = new HashMap<>(); network.cells().forEach(c -> cells.put(c.pos(),c.kind()));
        Set<Pos> unique = new HashSet<>();
        for (var sign : signs) {
            var p = sign.pos(); var support = sign.support();
            require(unique.add(p) && cells.get(p) == DenShape.Kind.AIR,"Signs must occupy distinct planned air cells");
            require(cells.get(support) == DenShape.Kind.FILL || cells.get(support) == DenShape.Kind.WALL || cells.get(support) == DenShape.Kind.BEAM,
                    "Signs must attach to structural walls, not Nests or furniture");
            require(Math.floorDiv(17+p.x(),16) == Math.floorDiv(17+support.x(),16)
                    && Math.floorDiv(-13+p.z(),16) == Math.floorDiv(-13+support.z(),16),"Sign/support must share their owning chunk");
            labels.add(sign.label());
        }
    }

    private static void checkFixtures(Settlement settlement, TunnelShape.Network network, Ground terrain) {
        var signs = Wayfinding.plan(settlement,network,17,-13);
        var fixtures = SettlementFixtures.plan(settlement,network,signs);
        require(fixtures.equals(SettlementFixtures.decode(SettlementFixtures.encode(fixtures))),"Fixture plans must persist exactly");
        require(SettlementFixtures.decode(new int[]{0,0,0,99,0}).isEmpty(),"Unknown/truncated furniture must be skipped");
        if (!network.dens().isEmpty()) {
            require(fixtures.stream().filter(f -> f.kind() == SettlementFixtures.Kind.TROUGH).count() == network.dens().size(),
                    "Representative fitted dens must each have one trough");
            require(fixtures.stream().filter(f -> f.kind() == SettlementFixtures.Kind.BELL).count()
                    == (settlement.level() >= 3 && network.dens().contains(0) ? 1 : 0),"Developed central rooms need exactly one bell");
        }
        Map<Pos,DenShape.Kind> cells = new HashMap<>(); network.cells().forEach(c -> cells.put(c.pos(),c.kind()));
        var unique = new HashSet<Pos>();
        long cradles = fixtures.stream().filter(f -> f.kind() == SettlementFixtures.Kind.NURSERY_CRADLE).count();
        nurseryFixtures += (int)cradles;
        require(cradles <= 1 && (cradles == 0 || settlement.level() == 5 && network.dens().contains(0)),"Only a fitted grand-tree nursery may have a Cradle");
        for (var fixture : fixtures) if (fixture.kind() == SettlementFixtures.Kind.NURSERY_CRADLE) {
            for (int x = 1; x <= 3; x++) for (int z = -1; z <= 1; z++) {
                var p = fixture.pos(); var slot = new Pos(p.x()+x,p.y(),p.z()+z);
                var head = new Pos(slot.x(),slot.y()+1,slot.z()); var floor = new Pos(slot.x(),slot.y()-1,slot.z());
                require(cells.get(slot) == DenShape.Kind.AIR && cells.get(head) == DenShape.Kind.AIR,
                        "A planned nursery hatch pad must have complete two-block air");
                require(cells.get(floor) != null && cells.get(floor) != DenShape.Kind.AIR && cells.get(floor) != DenShape.Kind.WATER,
                        "A planned nursery pad must retain dry floor support");
                require(fixtures.stream().noneMatch(other -> other.pos().equals(slot) || other.pos().equals(head))
                        && signs.stream().noneMatch(sign -> sign.pos().equals(slot) || sign.pos().equals(head)),
                        "A nursery pad must leave labels and other furniture clear");
            }
        }
        for (var fixture : fixtures) {
            var p = fixture.pos();
            require(unique.add(p) && cells.get(p) == DenShape.Kind.AIR,"Furniture must occupy distinct spare air cells");
            require(cells.get(new Pos(p.x(),p.y()-1,p.z())) != DenShape.Kind.NEST,"Furniture must leave floor Nests free");
            require(signs.stream().noneMatch(s -> s.pos().x() == p.x() && s.pos().z() == p.z()
                    && Math.abs(s.pos().y()-p.y()) <= 1),"Furniture must leave labels clear");
            cells.put(p,DenShape.Kind.HAY); // Conservative full-block obstacle for every native fixture shape.
        }
        var furnished = new TunnelShape.Network(cells.entrySet().stream().map(e -> new TunnelShape.Cell(e.getKey(),e.getValue())).toList(),
                network.dens(),network.mouths(),network.cellars(),network.pathExclusions(),network.features());
        reachable(furnished,terrain,true);
        require(SettlementFixtures.den(DenShape.generate(new Random(4))).size() == 1,"A standalone den must fit its trough too");
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
                for (HouseDetails.Cell detail : HouseDetails.cells(house.doorDx(),house.doorDz(),form,settlement.level())) {
                    Pos p = new Pos(house.dx()+detail.dx(),top+detail.dy(),house.dz()+detail.dz());
                    if (built.containsKey(p) ? built.get(p) != DenShape.Kind.AIR : !terrain.replaceable(p)) continue;
                    built.put(p,switch (detail.kind()) {
                        case DOOR_LOWER, DOOR_UPPER, SHUTTER -> DenShape.Kind.HATCH_X;
                        case BEAM -> DenShape.Kind.BEAM;
                        case LANTERN -> DenShape.Kind.CEILING_LANTERN;
                    });
                }
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
            if (joined) for (UndergroundRooms.Feature f : network.features()) {
                Pos p = f.type() == UndergroundRooms.Type.CISTERN ? new Pos(f.centre().x()+2,f.centre().y(),f.centre().z()) : f.centre();
                if (f.type() != UndergroundRooms.Type.SKYLIGHT) require(seen.contains(p), "A landmark room or hall must be reachable: " + f);
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
        for (TunnelShape.Cell c : network.cells()) if (c.kind() == DenShape.Kind.WATER) {
            for (int[] d : new int[][]{{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,-1,0}}) {
                Pos p = new Pos(c.pos().x()+d[0],c.pos().y()+d[1],c.pos().z()+d[2]);
                require(cells.get(p) == DenShape.Kind.WATER || solid(p,cells,terrain), "Cistern water must be contained: " + p);
            }
        }
    }

    private static boolean solid(Pos p, Map<Pos, DenShape.Kind> cells, Ground terrain) {
        DenShape.Kind kind = cells.get(p);
        return kind == null ? terrain.solid(p) : switch (kind) {
            case AIR, WATER, ROOTS, HATCH_X, HATCH_Z -> false;
            default -> true;
        };
    }

    private static boolean standable(Pos p, Map<Pos, DenShape.Kind> cells, Ground terrain) {
        Pos below = new Pos(p.x(), p.y() - 1, p.z());
        DenShape.Kind floor = cells.get(below);
        boolean supported = floor == null ? terrain.solid(below) : switch (floor) {
            case AIR, WATER, ROOTS, HATCH_X, HATCH_Z, LANTERN, CEILING_LANTERN -> false;
            default -> true;
        };
        return !solid(p, cells, terrain) && !solid(new Pos(p.x(), p.y() + 1, p.z()), cells, terrain) && supported;
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
