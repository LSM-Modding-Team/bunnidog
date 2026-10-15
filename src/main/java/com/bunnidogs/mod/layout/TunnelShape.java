package com.bunnidogs.mod.layout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Terrain-tested underground settlement layout. Coordinates are relative to the settlement's surface centre. */
public final class TunnelShape {
    private TunnelShape() {}

    public record Pos(int x, int y, int z) {
        Pos offset(int dx, int dy, int dz) { return new Pos(x + dx, y + dy, z + dz); }
    }

    public record Cell(Pos pos, DenShape.Kind kind) {}
    public record Mouth(int room, Pos doorstep) {}

    /** Immutable plan: only rooms connected to a fitted surface entrance survive. */
    public record Network(List<Cell> cells, Set<Integer> dens, List<Mouth> mouths,
            List<CellarShape.Cellar> cellars, Set<DenShape.Pos> pathExclusions, List<UndergroundRooms.Feature> features) {
        public static Network empty() { return new Network(List.of(), Set.of(), List.of(), List.of(), Set.of(), List.of()); }
    }

    /** Spare house candidates must not close a fitted mound or its doorstep in the command/feature generator. */
    public static boolean blocksHouse(Network network, Settlement.House house) {
        for (DenShape.Pos p : network.pathExclusions()) if (houseOccupies(house, p.dx(), p.dz())) return true;
        return network.mouths().stream().anyMatch(m -> houseOccupies(house, m.doorstep().x(), m.doorstep().z()));
    }

    private static boolean houseOccupies(Settlement.House h, int x, int z) {
        return Math.max(Math.abs(x - h.dx()), Math.abs(z - h.dz())) <= 1 || x == h.dx() + 2 * h.doorDx() && z == h.dz() + 2 * h.doorDz();
    }

    /** Read-only terrain, before any settlement blocks are written; surface is the first free Y, or null. */
    public interface Terrain {
        Integer surface(int x, int z);
        boolean diggable(Pos pos);
        boolean solid(Pos pos);
        boolean replaceable(Pos pos);
        boolean dry(Pos pos);
    }

    private record Room(int index, Settlement.Cluster tree, int surface, int half, int height, DenShape.Den den, Map<Pos, DenShape.Kind> cells) {
        int floor() { return surface - DenShape.DEPTH; }
        boolean contains(int x, int z) {
            return Math.max(Math.abs(x - tree.dx()), Math.abs(z - tree.dz())) <= half;
        }
    }

    private record Link(int from, int to, Map<Pos, DenShape.Kind> cells) {}
    private record Entrance(int room, Pos doorstep, Map<Pos, DenShape.Kind> cells) {}
    private static final int[][] AROUND = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int MAX_PAD_SLOPE = 3;

    /** Dens under the trees, spokes below the surface paths, and entrances clear of the planned surface buildings. */
    public static Network plan(Settlement settlement, long seed, Terrain terrain) {
        return plan(settlement,seed,terrain,false);
    }

    /** U6b: cave, Nether and End floors use visible open stair cuts rather than surface entrance mounds. */
    public static Network planOpen(Settlement settlement, long seed, Terrain terrain) {
        return plan(settlement,seed,terrain,true);
    }

    private static Network plan(Settlement settlement, long seed, Terrain terrain, boolean open) {
        if (!open && settlement.level() < 2) return Network.empty();
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < settlement.clusters().size(); i++) {
            Settlement.Cluster tree = settlement.clusters().get(i);
            Integer top = terrain.surface(tree.dx(), tree.dz());
            if (top == null) continue;
            boolean trunkFits = true;
            for (TreeShape.Cell cell : tree.shape().tree()) {
                if (cell.kind() == TreeShape.Kind.LOG && !terrain.replaceable(new Pos(tree.dx() + cell.dx(), top + cell.dy(), tree.dz() + cell.dz()))) {
                    trunkFits = false;
                    break;
                }
            }
            if (!trunkFits) continue;
            boolean hall = settlement.level() == 5 && i == 0;
            int half = hall ? 3 : DenShape.HALF;
            int height = hall ? 4 : DenShape.HEIGHT;
            Map<Pos, DenShape.Kind> cells = roomCells(tree.dx(), top - DenShape.DEPTH, tree.dz(), half, height);
            if (fits(cells, terrain) && supported(cells, terrain)) {
                rooms.add(new Room(i, tree, top, half, height, DenShape.generate(new Random(seed + 31L * i)), cells));
            }
        }
        List<Link> links = new ArrayList<>();
        Room centre = rooms.stream().filter(r -> r.index() == 0).findFirst().orElse(null);
        if (centre != null) {
            for (Room room : rooms) {
                if (room == centre) continue;
                Map<Pos, DenShape.Kind> cells = linkCells(centre, room);
                if (!cells.isEmpty() && fits(cells, terrain) && supported(cells, terrain)) {
                    links.add(new Link(centre.index(), room.index(), cells));
                }
            }
        }
        if (settlement.level() == 5) {
            List<Room> outer = rooms.stream().filter(r -> r.index() != 0)
                    .sorted(java.util.Comparator.comparingDouble(r -> Math.atan2(r.tree().dz(),r.tree().dx()))).toList();
            if (outer.size() == 4) for (int i = 0; i < outer.size(); i++) {
                Room from = outer.get(i), to = outer.get((i+1)%outer.size());
                // Different elevations already have stepped spokes; avoid cutting
                // a second route through another corridor's stair supports.
                if (from.floor() != to.floor()) continue;
                Map<Pos,DenShape.Kind> ring = linkCells(from,to,true);
                if (!ring.isEmpty() && fits(ring,terrain) && supported(ring,terrain)) links.add(new Link(from.index(),to.index(),ring));
            }
        }
        List<Entrance> entrances = new ArrayList<>();
        for (Room room : rooms) {
            int count = 0;
            for (DenShape.Dir dir : room.den().dirs()) {
                boolean found = false;
                for (int shift : new int[]{0, -1, 1, -2, 2}) {
                    for (int radius = DenShape.MIN_RADIUS; radius <= DenShape.MAX_RADIUS; radius++) {
                        Entrance entrance = open ? openEntrance(settlement,room,dir,radius,shift,rooms,links,entrances,terrain)
                                : entrance(settlement, room, dir, radius, shift, rooms, links, entrances, terrain);
                        if (entrance != null) {
                            entrances.add(entrance);
                            count++;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (count == DenShape.MOUTHS) break;
            }
        }
        Set<Integer> reachable = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (Entrance e : entrances) if (reachable.add(e.room())) queue.add(e.room());
        while (!queue.isEmpty()) {
            int index = queue.remove();
            for (Link link : links) {
                int next = link.from() == index ? link.to() : link.to() == index ? link.from() : -1;
                if (next >= 0 && reachable.add(next)) queue.add(next);
            }
        }
        Map<Pos, DenShape.Kind> out = new LinkedHashMap<>();
        for (Room room : rooms) if (reachable.contains(room.index())) out.putAll(room.cells());
        for (Link link : links) {
            if (reachable.contains(link.from()) && reachable.contains(link.to())) {
                for (var cell : link.cells().entrySet()) {
                    // Intersecting corridors share their air volume. A side wall
                    // must not seal a previously carved spoke or hall opening.
                    if (out.get(cell.getKey()) != DenShape.Kind.AIR) out.put(cell.getKey(),cell.getValue());
                }
            }
        }
        for (Entrance e : entrances) out.putAll(e.cells());
        for (Room room : rooms) {
            if (!reachable.contains(room.index())) continue;
            if (room.half() == 3) {
                furnishHall(out, room);
                continue;
            }
            for (DenShape.Cell c : room.den().furnishing()) {
                // Jittered spokes may enter one block off the middle of a side. Keep lights/bedding away from all room edges.
                int dx = c.kind() == DenShape.Kind.NEST ? c.dx() : c.dx() > 0 ? 0 : 1;
                int dz = c.kind() == DenShape.Kind.NEST ? c.dz() : 0;
                out.put(new Pos(room.tree().dx() + dx, room.surface() + c.dy(), room.tree().dz() + dz), c.kind());
            }
        }
        List<Mouth> mouths = new ArrayList<>(entrances.stream().map(e -> new Mouth(e.room(), e.doorstep())).toList());
        UndergroundRooms.Result extras = open ? new UndergroundRooms.Result(List.of(),Set.of())
                : UndergroundRooms.add(settlement, terrain, out, reachable, mouths);
        CellarShape.Result cellars = CellarShape.add(settlement, terrain, out, mouths);
        Set<DenShape.Pos> excluded = new HashSet<>(cellars.pathExclusions());
        excluded.addAll(extras.pathExclusions());
        for (Entrance e : entrances) e.cells().keySet().forEach(p -> excluded.add(new DenShape.Pos(p.x(), p.z())));
        List<UndergroundRooms.Feature> features = new ArrayList<>(extras.features());
        if (settlement.level() == 5 && reachable.contains(0)) {
            Room hall = rooms.stream().filter(r -> r.index() == 0).findFirst().orElseThrow();
            features.add(new UndergroundRooms.Feature(UndergroundRooms.Type.HALL, new Pos(0,hall.floor(),0)));
        }
        return new Network(out.entrySet().stream().map(e -> new Cell(e.getKey(), e.getValue())).toList(),
                Set.copyOf(reachable), List.copyOf(mouths), cellars.cellars(), Set.copyOf(excluded), List.copyOf(features));
    }

    private static void furnishHall(Map<Pos, DenShape.Kind> out, Room room) {
        int x = room.tree().dx(), z = room.tree().dz(), floor = room.floor();
        for (int[] p : new int[][]{{-3,-3},{0,-3},{3,-3},{-3,0},{3,0},{-3,3},{0,3},{3,3}}) {
            out.put(new Pos(x + p[0], floor - 1, z + p[1]), DenShape.Kind.NEST);
        }
        for (int dx : new int[]{-2, 2}) for (int dz : new int[]{-2, 2}) {
            out.put(new Pos(x + dx, floor, z + dz), DenShape.Kind.HAY);
            out.put(new Pos(x + dx, floor + 1, z + dz), DenShape.Kind.LANTERN);
        }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            out.put(new Pos(x + dx, floor + 3, z + dz), DenShape.Kind.ROOTS);
        }
    }

    private static Map<Pos, DenShape.Kind> roomCells(int x, int floor, int z, int half, int height) {
        Map<Pos, DenShape.Kind> out = new LinkedHashMap<>();
        int shell = half + 1;
        for (int dx = -shell; dx <= shell; dx++) {
            for (int dz = -shell; dz <= shell; dz++) {
                for (int dy = -1; dy <= height; dy++) {
                    boolean inside = Math.abs(dx) <= half && Math.abs(dz) <= half && dy >= 0 && dy < height;
                    out.put(new Pos(x + dx, floor + dy, z + dz), inside ? DenShape.Kind.AIR : DenShape.Kind.FILL);
                }
            }
        }
        return out;
    }

    private static Entrance openEntrance(Settlement settlement, Room room, DenShape.Dir dir, int radius, int shift,
            List<Room> rooms, List<Link> links, List<Entrance> previous, Terrain terrain) {
        int ox = room.tree().dx() - dir.dz * shift, oz = room.tree().dz() + dir.dx * shift;
        DenShape.Pos edge = DenShape.at(dir,radius,0);
        Integer doorY = terrain.surface(ox+edge.dx(),oz+edge.dz());
        if (doorY == null || radius < room.half()+DenShape.DEPTH+doorY-room.surface()) return null;
        DenShape.Pos outer = DenShape.at(dir,radius+1,0);
        Integer stepY = terrain.surface(ox+outer.dx(),oz+outer.dz());
        if (stepY == null || stepY > doorY || doorY-stepY > 1) return null;
        Pos doorstep = new Pos(ox+outer.dx(),stepY,oz+outer.dz());
        if (!terrain.solid(doorstep.offset(0,-1,0))) return null;
        for (int y = stepY; y <= doorY+1; y++) if (!terrain.replaceable(new Pos(doorstep.x(),y,doorstep.z()))) return null;
        Map<Pos,DenShape.Kind> cells = new LinkedHashMap<>();
        for (int u = radius; u > room.half(); u--) {
            DenShape.Pos column = DenShape.at(dir,u,0);
            int x = ox+column.dx(), z = oz+column.dz();
            int stand = room.surface()+DenShape.standDy(doorY-room.surface(),radius,u);
            if (!terrain.solid(new Pos(x,stand-1,z))) return null;
            Integer surface = terrain.surface(x,z);
            if (surface == null) return null;
            for (int y = stand; y < stand+(u == radius ? 2 : 3); y++) cells.put(new Pos(x,y,z),DenShape.Kind.AIR);
            if (stand+3 < surface) cells.put(new Pos(x,stand+3,z),DenShape.Kind.FILL);
        }
        Set<DenShape.Pos> columns = new HashSet<>();
        cells.keySet().forEach(p -> columns.add(new DenShape.Pos(p.x(),p.z())));
        columns.add(new DenShape.Pos(doorstep.x(),doorstep.z()));
        for (DenShape.Pos p : columns) {
            if (Math.max(Math.abs(p.dx()),Math.abs(p.dz())) > Settlement.MAX_EXTENT) return null;
            for (Settlement.Cluster t : settlement.clusters()) if (t != room.tree() && Math.max(Math.abs(p.dx()-t.dx()),Math.abs(p.dz()-t.dz())) <= 3) return null;
            for (Settlement.House h : settlement.houses()) if (houseOccupies(h,p.dx(),p.dz())) return null;
            for (Settlement.Prop prop : settlement.props()) if (Math.max(Math.abs(p.dx()-prop.dx()),Math.abs(p.dz()-prop.dz())) <= 1) return null;
            for (Room other : rooms) if (other != room && other.contains(p.dx(),p.dz())) return null;
            for (Link link : links) if (link.cells().keySet().stream().anyMatch(q -> q.x() == p.dx() && q.z() == p.dz())) return null;
            for (Entrance e : previous) if (e.doorstep().x() == p.dx() && e.doorstep().z() == p.dz()
                    || e.cells().keySet().stream().anyMatch(q -> q.x()==p.dx() && q.z()==p.dz())) return null;
        }
        return fits(cells,terrain) ? new Entrance(room.index(),doorstep,cells) : null;
    }

    /** Follow the same long-axis-first spoke as PathShape; room interiors retain their own level floors. */
    private static Map<Pos, DenShape.Kind> linkCells(Room from, Room to) {
        return linkCells(from,to,false);
    }

    private static Map<Pos, DenShape.Kind> linkCells(Room from, Room to, boolean perimeter) {
        List<DenShape.Pos> route = new ArrayList<>();
        int x = from.tree().dx(), z = from.tree().dz();
        route.add(new DenShape.Pos(x, z));
        if (perimeter) {
            // Loop around the hall, inside the satellite anchors. The outer corners are
            // beneath the houses and leave no solid ground for their descending ramps.
            int cornerX = Math.abs(x) > Math.abs(z) ? x - Integer.signum(x) * 3
                    : to.tree().dx() - Integer.signum(to.tree().dx()) * 3;
            int cornerZ = Math.abs(z) > Math.abs(x) ? z - Integer.signum(z) * 3
                    : to.tree().dz() - Integer.signum(to.tree().dz()) * 3;
            int[][] waypoints = Math.abs(x) > Math.abs(z)
                    ? new int[][]{{cornerX,z},{cornerX,cornerZ},{to.tree().dx(),cornerZ},{to.tree().dx(),to.tree().dz()}}
                    : new int[][]{{x,cornerZ},{cornerX,cornerZ},{cornerX,to.tree().dz()},{to.tree().dx(),to.tree().dz()}};
            for (int[] point : waypoints) {
                while (x != point[0]) { x += Integer.signum(point[0] - x); route.add(new DenShape.Pos(x,z)); }
                while (z != point[1]) { z += Integer.signum(point[1] - z); route.add(new DenShape.Pos(x,z)); }
            }
        } else {
            boolean alongX = Math.abs(to.tree().dx() - x) >= Math.abs(to.tree().dz() - z);
            for (int axis = 0; axis < 2; axis++) {
                if (alongX == (axis == 0)) {
                    while (x != to.tree().dx()) { x += Integer.signum(to.tree().dx() - x); route.add(new DenShape.Pos(x, z)); }
                } else {
                    while (z != to.tree().dz()) { z += Integer.signum(to.tree().dz() - z); route.add(new DenShape.Pos(x, z)); }
                }
            }
        }
        List<DenShape.Pos> gap = route.stream().filter(p -> !from.contains(p.dx(), p.dz()) && !to.contains(p.dx(), p.dz())).toList();
        int rise = to.floor() - from.floor();
        if (gap.isEmpty() || Math.abs(rise) > gap.size() + 1) return Map.of();
        Map<Pos, DenShape.Kind> out = new LinkedHashMap<>();
        List<Pos> stands = new ArrayList<>();
        for (int i = 0; i < gap.size(); i++) {
            DenShape.Pos p = gap.get(i);
            int floor = from.floor() + Math.floorDiv(rise * (i + 1), gap.size() + 1);
            stands.add(new Pos(p.dx(), floor, p.dz()));
        }
        // Shell first; carve the whole corridor afterwards so walls never seal a bend or a step.
        for (Pos stand : stands) {
            out.put(stand.offset(0, -1, 0), DenShape.Kind.FILL);
            out.put(stand.offset(0, 3, 0), DenShape.Kind.FILL);
            for (int[] d : AROUND) {
                int nx = stand.x() + d[0], nz = stand.z() + d[1];
                if (from.contains(nx, nz) || to.contains(nx, nz)) continue;
                for (int dy = 0; dy < 3; dy++) out.put(new Pos(nx, stand.y() + dy, nz), DenShape.Kind.FILL);
            }
        }
        for (Pos stand : stands) {
            out.put(stand.offset(0, -1, 0), DenShape.Kind.FILL);
            for (int dy = 0; dy < 3; dy++) out.put(stand.offset(0, dy, 0), DenShape.Kind.AIR);
        }
        // Spokes are short; the longer perimeter ring also has recessed lamps every eight columns.
        if (perimeter) for (int i = 3; i < stands.size(); i += 8) {
            Pos stand = stands.get(i);
            for (int[] d : AROUND) {
                Pos lamp = stand.offset(d[0],1,d[1]);
                if (stands.stream().anyMatch(p -> p.x() == lamp.x() && p.z() == lamp.z())) continue;
                if (out.get(lamp) == DenShape.Kind.FILL && out.get(lamp.offset(0,-1,0)) == DenShape.Kind.FILL) {
                    out.put(lamp,DenShape.Kind.LANTERN);
                    break;
                }
            }
        }
        return out;
    }

    private static Entrance entrance(Settlement settlement, Room room, DenShape.Dir dir, int radius, int shift,
            List<Room> rooms, List<Link> links, List<Entrance> entrances, Terrain terrain) {
        int ox = room.tree().dx() - dir.dz * shift;
        int oz = room.tree().dz() + dir.dx * shift;
        int top = Integer.MIN_VALUE, low = Integer.MAX_VALUE;
        for (DenShape.Pos p : DenShape.padColumns(dir, radius)) {
            Integer y = terrain.surface(ox + p.dx(), oz + p.dz());
            if (y == null) return null;
            top = Math.max(top, y);
            low = Math.min(low, y);
        }
        int doorDy = top - room.surface();
        if (top - low > MAX_PAD_SLOPE || radius < room.half() + DenShape.DEPTH + doorDy) return null;
        Map<Pos, DenShape.Kind> cells = new LinkedHashMap<>();
        for (DenShape.Pos p : DenShape.padColumns(dir, radius)) {
            int x = ox + p.dx(), z = oz + p.dz();
            for (int y = terrain.surface(x, z); y < top; y++) cells.put(new Pos(x, y, z), DenShape.Kind.FILL);
        }
        for (DenShape.Cell c : DenShape.mound(dir, radius, doorDy)) {
            Pos pos = new Pos(ox + c.dx(), room.surface() + c.dy(), oz + c.dz());
            if (!terrain.replaceable(pos)) return null;
            cells.put(pos, c.kind());
        }
        for (DenShape.Cell c : DenShape.air(dir, radius, doorDy)) {
            cells.put(new Pos(ox + c.dx(), room.surface() + c.dy(), oz + c.dz()), DenShape.Kind.AIR);
        }
        for (int u : DenShape.tunnelRadials(radius)) {
            DenShape.Pos p = DenShape.at(dir, u, 0);
            int x = ox + p.dx(), z = oz + p.dz();
            int ceiling = room.surface() + DenShape.standDy(doorDy, radius, u) + 3;
            Integer surface = terrain.surface(x, z);
            if (surface == null || ceiling >= surface) return null;
            cells.put(new Pos(x, ceiling, z), DenShape.Kind.FILL);
        }
        DenShape.Pos step = DenShape.at(dir, radius + 1, 0);
        int stepX = ox + step.dx(), stepZ = oz + step.dz();
        Integer stepY = terrain.surface(stepX, stepZ);
        if (stepY == null || stepY > top || top - stepY > 1) return null;
        Pos doorstep = new Pos(stepX, stepY, stepZ);
        for (int y = stepY; y <= top + 1; y++) if (!terrain.replaceable(new Pos(stepX, y, stepZ))) return null;
        if (!terrain.solid(doorstep.offset(0, -1, 0))) return null;
        Set<DenShape.Pos> columns = new HashSet<>();
        cells.keySet().forEach(p -> columns.add(new DenShape.Pos(p.x(), p.z())));
        columns.add(new DenShape.Pos(doorstep.x(), doorstep.z()));
        for (DenShape.Pos p : columns) {
            if (Math.max(Math.abs(p.dx()), Math.abs(p.dz())) > Settlement.MAX_EXTENT) return null;
            for (Settlement.Cluster tree : settlement.clusters()) {
                // Keep the patches, trunks and crowns free, including trees with no fitted den.
                if (tree != room.tree() && Math.max(Math.abs(p.dx() - tree.dx()), Math.abs(p.dz() - tree.dz())) <= 3) return null;
            }
            for (Settlement.House h : settlement.houses().subList(0, Math.min(settlement.houses().size(), Settlement.housesFor(settlement.level())))) {
                if (houseOccupies(h, p.dx(), p.dz())) return null;
            }
            for (Settlement.Prop prop : settlement.props()) {
                if (Math.max(Math.abs(p.dx() - prop.dx()), Math.abs(p.dz() - prop.dz())) <= PropShape.RADIUS) return null;
            }
            for (Room other : rooms) if (other != room && other.contains(p.dx(), p.dz())) return null;
            for (Link link : links) {
                if (link.cells().keySet().stream().anyMatch(c -> c.x() == p.dx() && c.z() == p.dz())) return null;
            }
            for (Entrance other : entrances) {
                if (other.doorstep().x() == p.dx() && other.doorstep().z() == p.dz()
                        || other.cells().keySet().stream().anyMatch(c -> c.x() == p.dx() && c.z() == p.dz())) return null;
            }
        }
        if (!fits(cells, terrain)) return null;
        // Every step needs a supporting floor, including the slope inside the mound.
        Map<DenShape.Pos, Integer> bottom = new HashMap<>();
        for (var e : cells.entrySet()) if (e.getValue() == DenShape.Kind.AIR) {
            bottom.merge(new DenShape.Pos(e.getKey().x(), e.getKey().z()), e.getKey().y(), Math::min);
        }
        for (var e : bottom.entrySet()) {
            Pos support = new Pos(e.getKey().dx(), e.getValue() - 1, e.getKey().dz());
            if (cells.get(support) != DenShape.Kind.FILL && !terrain.solid(support)) return null;
        }
        return new Entrance(room.index(), doorstep, cells);
    }

    private static boolean fits(Map<Pos, DenShape.Kind> cells, Terrain terrain) {
        for (Pos p : cells.keySet()) {
            if (Math.max(Math.abs(p.x()), Math.abs(p.z())) > Settlement.MAX_EXTENT || !terrain.diggable(p) || !terrain.dry(p)) return false;
            for (int[] d : AROUND) if (!terrain.dry(p.offset(d[0], 0, d[1]))) return false;
            if (!terrain.dry(p.offset(0, -1, 0)) || !terrain.dry(p.offset(0, 1, 0))) return false;
        }
        return true;
    }

    private static boolean supported(Map<Pos, DenShape.Kind> cells, Terrain terrain) {
        for (var e : cells.entrySet()) {
            Pos p = e.getKey();
            if (e.getValue() == DenShape.Kind.AIR) {
                if (cells.get(p.offset(0, -1, 0)) == DenShape.Kind.FILL && !terrain.solid(p.offset(0, -1, 0))) return false;
                if (cells.get(p.offset(0, 1, 0)) == DenShape.Kind.FILL && !terrain.solid(p.offset(0, 1, 0))) return false;
            }
        }
        return true;
    }
}
