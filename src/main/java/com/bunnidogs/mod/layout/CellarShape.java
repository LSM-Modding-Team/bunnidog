package com.bunnidogs.mod.layout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bunnidogs.mod.layout.TunnelShape.Pos;

/** U3: a covered stair ramp beside a house's doorstep, connected to an existing underground walking column. */
public final class CellarShape {
    private CellarShape() {}

    public record Cellar(int house, Pos doorstep, List<Pos> steps) {}
    public record Result(List<Cellar> cellars, Set<DenShape.Pos> pathExclusions) {}
    private record Node(Pos pos, Node previous, int length) {}
    private static final int[][] AROUND = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int MAX_STEPS = 10;

    /** Adds only complete, fitted ramps to cells. Failed candidates leave the network untouched. */
    static Result add(Settlement settlement, TunnelShape.Terrain terrain, Map<Pos, DenShape.Kind> cells, List<TunnelShape.Mouth> mouths) {
        if (cells.isEmpty()) return new Result(List.of(), Set.of());
        List<Cellar> cellars = new ArrayList<>();
        Set<DenShape.Pos> excluded = new HashSet<>();
        int count = Math.min(settlement.houses().size(), Settlement.housesFor(settlement.level()));
        for (int i = 0; i < count; i++) {
            Settlement.House house = settlement.houses().get(i);
            Integer top = houseTop(settlement, house, terrain);
            if (top == null) continue;
            Pos doorstep = new Pos(house.dx() + 2 * house.doorDx(), top, house.dz() + 2 * house.doorDz());
            if (!terrain.replaceable(doorstep) || !terrain.replaceable(doorstep.offset(0, 1, 0))
                    || !terrain.solid(doorstep.offset(0, -1, 0))) continue;
            List<Pos> steps = find(settlement, house, terrain, cells, mouths, doorstep);
            if (steps.isEmpty()) continue;
            Map<Pos, DenShape.Kind> ramp = build(settlement, terrain, cells, mouths, doorstep, steps);
            if (ramp.isEmpty()) continue;
            cells.putAll(ramp);
            ramp.keySet().forEach(p -> excluded.add(new DenShape.Pos(p.x(), p.z())));
            cellars.add(new Cellar(i, doorstep, List.copyOf(steps)));
        }
        return new Result(List.copyOf(cellars), Set.copyOf(excluded));
    }

    private static Integer houseTop(Settlement settlement, Settlement.House house, TunnelShape.Terrain terrain) {
        int top = Integer.MIN_VALUE, low = Integer.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            Integer y = terrain.surface(house.dx() + dx, house.dz() + dz);
            if (y == null) return null;
            top = Math.max(top, y);
            low = Math.min(low, y);
        }
        if (top - low > 3) return null;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            int x = house.dx() + dx, z = house.dz() + dz;
            for (int y = terrain.surface(x, z); y < top; y++) if (!terrain.replaceable(new Pos(x, y, z))) return null;
        }
        // Both biome forms must fit; the world adapter chooses the style after planning.
        for (HouseShape.Form form : HouseShape.Form.values()) {
            for (HouseShape.Cell cell : HouseShape.cells(house.doorDx(), house.doorDz(), form, HouseShape.tierFor(settlement.level()))) {
                if (!terrain.replaceable(new Pos(house.dx() + cell.dx(), top + cell.dy(), house.dz() + cell.dz()))) return null;
            }
        }
        return top;
    }

    static List<Pos> find(Settlement settlement, Settlement.House house, TunnelShape.Terrain terrain,
            Map<Pos, DenShape.Kind> cells, List<TunnelShape.Mouth> mouths, Pos start) {
        ArrayDeque<Node> queue = new ArrayDeque<>();
        Set<Pos> seen = new HashSet<>();
        queue.add(new Node(start, null, 0));
        seen.add(start);
        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (node.length() >= MAX_STEPS) continue;
            for (int[] d : AROUND) {
                // The first step branches sideways, leaving the front of the doorstep open to the yard.
                if (node.previous() == null) {
                    if (d[0] * house.doorDx() + d[1] * house.doorDz() != 0) continue;
                }
                for (int drop : new int[]{1, 0}) {
                    Pos next = node.pos().offset(d[0], -drop, d[1]);
                    if (next.y() < start.y() - 8 || Math.max(Math.abs(next.x()), Math.abs(next.z())) >= Settlement.MAX_EXTENT
                            || seen.contains(next) || revisitsColumn(node, next) || reserved(settlement, next.x(), next.z())) continue;
                    if (mouths.stream().anyMatch(m -> m.doorstep().x() == next.x() && m.doorstep().z() == next.z())) continue;
                    if (!standFits(settlement, terrain, cells, next)) continue;
                    Node child = new Node(next, node, node.length() + 1);
                    if (next.y() <= start.y() - 3 && walkingColumn(cells, next)) {
                        List<Pos> route = new ArrayList<>();
                        for (Node n = child; n.previous() != null; n = n.previous()) route.add(n.pos());
                        Collections.reverse(route);
                        // Shell validation handles nearby buildings and existing tunnels as a whole.
                        if (!build(settlement, terrain, cells, mouths, start, route).isEmpty()) return route;
                    }
                    if (seen.add(next)) queue.add(child);
                }
            }
        }
        return List.of();
    }

    private static boolean revisitsColumn(Node node, Pos next) {
        for (Node n = node; n != null; n = n.previous()) if (n.pos().x() == next.x() && n.pos().z() == next.z()) return true;
        return false;
    }

    private static boolean standFits(Settlement settlement, TunnelShape.Terrain terrain, Map<Pos, DenShape.Kind> cells, Pos stand) {
        Pos floor = stand.offset(0, -1, 0);
        DenShape.Kind support = cells.get(floor);
        if (support == null ? !terrain.solid(floor) : !full(support)) return false;
        for (int y = -1; y <= 3; y++) {
            Pos p = stand.offset(0, y, 0);
            if (!safe(terrain, p)) return false;
            DenShape.Kind existing = cells.get(p);
            if (y >= 0 && y < 3 && existing != null && existing != DenShape.Kind.AIR && existing != DenShape.Kind.FILL) return false;
            if (y == 3 && existing == DenShape.Kind.AIR) return false;
        }
        for (Settlement.Cluster tree : settlement.clusters()) {
            if (Math.max(Math.abs(stand.x() - tree.dx()), Math.abs(stand.z() - tree.dz())) > 3) continue;
            Integer surface = terrain.surface(stand.x(), stand.z());
            if (surface == null || stand.y() + 3 >= surface - 1) return false;
        }
        return true;
    }

    private static boolean walkingColumn(Map<Pos, DenShape.Kind> cells, Pos p) {
        return cells.get(p) == DenShape.Kind.AIR && cells.get(p.offset(0, 1, 0)) == DenShape.Kind.AIR
                && cells.get(p.offset(0, 2, 0)) == DenShape.Kind.AIR && full(cells.get(p.offset(0, -1, 0)));
    }

    static boolean full(DenShape.Kind kind) {
        return kind != null && switch (kind) {
            case AIR, LANTERN, CEILING_LANTERN, WATER, ROOTS, HATCH_X, HATCH_Z -> false;
            default -> true;
        };
    }

    private static boolean reserved(Settlement settlement, int x, int z) {
        for (Settlement.House h : settlement.houses()) {
            if (Math.max(Math.abs(x - h.dx()), Math.abs(z - h.dz())) <= 1 || x == h.dx() + 2 * h.doorDx() && z == h.dz() + 2 * h.doorDz()) return true;
        }
        for (Settlement.Prop p : settlement.props()) if (Math.max(Math.abs(x - p.dx()), Math.abs(z - p.dz())) <= PropShape.RADIUS) return true;
        return false;
    }

    static Map<Pos, DenShape.Kind> build(Settlement settlement, TunnelShape.Terrain terrain, Map<Pos, DenShape.Kind> existing,
            List<TunnelShape.Mouth> mouths, Pos start, List<Pos> steps) {
        Map<Pos, DenShape.Kind> out = new LinkedHashMap<>();
        for (Pos step : steps) {
            if (walkingColumn(existing, step)) continue;
            for (int dy : new int[]{-1, 3}) {
                Pos p = step.offset(0, dy, 0);
                if (existing.get(p) == DenShape.Kind.AIR) return Map.of();
                if (!existing.containsKey(p)) out.put(p, DenShape.Kind.FILL);
            }
            for (int[] d : AROUND) for (int y = 0; y < 3; y++) {
                Pos p = step.offset(d[0], y, d[1]);
                if (p.x() == start.x() && p.z() == start.z() || reserved(settlement, p.x(), p.z())
                        || mouths.stream().anyMatch(m -> m.doorstep().x() == p.x() && m.doorstep().z() == p.z())) continue;
                if (!existing.containsKey(p)) out.put(p, DenShape.Kind.FILL);
            }
        }
        for (Pos step : steps) {
            if (walkingColumn(existing, step)) continue;
            for (int y = 0; y < 3; y++) out.put(step.offset(0, y, 0), DenShape.Kind.AIR);
        }
        // A lamp recessed into one new side wall lights the steps without occupying a walking column.
        Pos middle = steps.get(steps.size() / 2);
        for (int[] d : AROUND) {
            Pos lamp = middle.offset(d[0], 1, d[1]);
            if (steps.stream().anyMatch(step -> step.x() == lamp.x() && step.z() == lamp.z())) continue;
            if (out.get(lamp) == DenShape.Kind.FILL && out.get(lamp.offset(0, -1, 0)) == DenShape.Kind.FILL) {
                out.put(lamp, DenShape.Kind.LANTERN);
                break;
            }
        }
        for (Pos p : out.keySet()) {
            if (Math.max(Math.abs(p.x()), Math.abs(p.z())) > Settlement.MAX_EXTENT || !safe(terrain, p)) return Map.of();
        }
        // Close parallel runs or tight turns must not replace another step's supporting floor/ceiling.
        for (Pos step : steps) {
            DenShape.Kind floor = out.getOrDefault(step.offset(0, -1, 0), existing.get(step.offset(0, -1, 0)));
            if (floor == DenShape.Kind.AIR) return Map.of();
            for (int y = 0; y < 3; y++) {
                if (out.getOrDefault(step.offset(0, y, 0), existing.get(step.offset(0, y, 0))) != DenShape.Kind.AIR) return Map.of();
            }
        }
        return out;
    }

    static boolean safe(TunnelShape.Terrain terrain, Pos p) {
        if (!terrain.diggable(p) || !terrain.dry(p)) return false;
        for (int[] d : AROUND) if (!terrain.dry(p.offset(d[0], 0, d[1]))) return false;
        return terrain.dry(p.offset(0, -1, 0)) && terrain.dry(p.offset(0, 1, 0));
    }
}
