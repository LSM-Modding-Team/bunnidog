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

/** U4: fitted rooms beneath landmarks and small surface connections, added to a retained underground network. */
public final class UndergroundRooms {
    private UndergroundRooms() {}
    public enum Type { PANTRY, CISTERN, MARKET, FARM_ENTRANCE, SKYLIGHT, HALL }
    public record Feature(Type type, Pos centre) {}
    public record Result(List<Feature> features, Set<DenShape.Pos> pathExclusions) {}
    private record Node(Pos pos, Node previous, int length) {}
    private static final int[][] AROUND = {{1,0},{-1,0},{0,1},{0,-1}};

    static Result add(Settlement settlement, TunnelShape.Terrain terrain, Map<Pos, DenShape.Kind> cells,
            Set<Integer> dens, List<TunnelShape.Mouth> mouths) {
        List<Feature> features = new ArrayList<>();
        Set<DenShape.Pos> excluded = new HashSet<>();
        if (settlement.level() < 3 || cells.isEmpty()) return new Result(List.of(), Set.of());
        for (Settlement.Prop prop : settlement.props()) {
            Type type = switch (prop.kind()) { case HAYSTACK -> Type.PANTRY; case WELL -> Type.CISTERN; case STALL -> Type.MARKET; default -> null; };
            if (type == null) continue;
            Integer top = flatPad(prop, terrain);
            if (top == null) continue;
            int half = type == Type.CISTERN ? 2 : 1;
            Pos centre = new Pos(prop.dx(), top - DenShape.DEPTH, prop.dz());
            Map<Pos, DenShape.Kind> room = room(centre, half, type, top);
            if (!fitRoom(terrain, cells, room, centre, half, type)) continue;
            Map<Pos, DenShape.Kind> merged = merge(cells, room);
            List<Pos> route = connect(settlement, terrain, cells, merged, prop, centre, half);
            if (route == null) continue;
            if (!route.isEmpty()) {
                Map<Pos, DenShape.Kind> corridor = CellarShape.build(settlement, terrain, merged, mouths, route.getFirst(), route);
                if (corridor.isEmpty() && route.stream().anyMatch(p -> !walks(merged, p))) continue;
                merged.putAll(corridor);
            }
            cells.clear(); cells.putAll(merged);
            features.add(new Feature(type, centre));
            if (type == Type.MARKET) addEntrance(settlement, prop, top, terrain, cells, mouths, features, excluded, true);
        }
        for (Settlement.Prop prop : settlement.props()) if (prop.kind() == PropShape.Kind.FARM) {
            Integer top = flatPad(prop, terrain);
            if (top != null) addEntrance(settlement, prop, top, terrain, cells, mouths, features, excluded, false);
        }
        for (int index : dens.stream().sorted().toList()) {
            Settlement.Cluster tree = settlement.clusters().get(index);
            Integer top = terrain.surface(tree.dx(), tree.dz());
            if (top == null) continue;
            int half = settlement.level() == 5 && index == 0 ? 3 : 2;
            int floor = top - DenShape.DEPTH;
            for (int[] corner : new int[][]{{half,half},{-half,half},{half,-half},{-half,-half}}) {
                int x = tree.dx() + corner[0], z = tree.dz() + corner[1];
                Integer surface = terrain.surface(x, z);
                if (surface == null || surface - 2 < floor + 3) continue;
                Map<Pos, DenShape.Kind> shaft = new LinkedHashMap<>();
                for (int y = floor + 3; y <= surface - 2; y++) shaft.put(new Pos(x, y, z), DenShape.Kind.AIR);
                shaft.put(new Pos(x, surface - 2, z), DenShape.Kind.CEILING_LANTERN);
                shaft.put(new Pos(x, surface - 1, z), DenShape.Kind.GLASS);
                if (!terrain.solid(new Pos(x, surface - 1, z)) || shaft.keySet().stream().anyMatch(p -> !CellarShape.safe(terrain, p)
                        || cells.containsKey(p) && cells.get(p) != DenShape.Kind.AIR && cells.get(p) != DenShape.Kind.FILL)) continue;
                cells.putAll(shaft);
                excluded.add(new DenShape.Pos(x,z));
                features.add(new Feature(Type.SKYLIGHT, new Pos(x, surface - 1, z)));
                break;
            }
        }
        return new Result(List.copyOf(features), Set.copyOf(excluded));
    }

    private static Integer flatPad(Settlement.Prop prop, TunnelShape.Terrain terrain) {
        Integer top = terrain.surface(prop.dx(), prop.dz());
        if (top == null) return null;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            Integer y = terrain.surface(prop.dx() + dx, prop.dz() + dz);
            if (!top.equals(y)) return null;
        }
        for (PropShape.Cell c : PropShape.cells(prop.kind(), prop.turns())) {
            if (c.dy() >= 0 && !terrain.replaceable(new Pos(prop.dx() + c.dx(), top + c.dy(), prop.dz() + c.dz()))) return null;
        }
        return top;
    }

    private static Map<Pos, DenShape.Kind> room(Pos centre, int half, Type type, int surface) {
        Map<Pos, DenShape.Kind> out = new LinkedHashMap<>();
        for (int dx = -half-1; dx <= half+1; dx++) for (int dz = -half-1; dz <= half+1; dz++) {
            for (int y = -1; y <= 3; y++) {
                boolean air = Math.abs(dx) <= half && Math.abs(dz) <= half && y >= 0 && y < 3;
                out.put(centre.offset(dx,y,dz), air ? DenShape.Kind.AIR : DenShape.Kind.FILL);
            }
        }
        if (type == Type.CISTERN) {
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                out.put(centre.offset(dx,-2,dz), DenShape.Kind.FILL);
                out.put(centre.offset(dx,-1,dz), DenShape.Kind.WATER);
            }
            // A glass-lined, filled shaft joins the surface well's source without spilling onto the dry perimeter ledge.
            for (int y = centre.y(); y <= surface - 2; y++) {
                out.put(new Pos(centre.x(),y,centre.z()), DenShape.Kind.WATER);
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0) out.put(new Pos(centre.x()+dx,y,centre.z()+dz), DenShape.Kind.GLASS);
                }
            }
            out.put(centre.offset(-2,1,-2), DenShape.Kind.LANTERN);
            out.put(centre.offset(-2,0,-2), DenShape.Kind.HAY);
        } else {
            out.put(centre.offset(-1,0,-1), DenShape.Kind.BARREL);
            out.put(centre.offset(1,0,1), DenShape.Kind.HAY);
            out.put(centre.offset(1,1,1), DenShape.Kind.LANTERN);
        }
        return out;
    }

    private static boolean fitRoom(TunnelShape.Terrain terrain, Map<Pos, DenShape.Kind> existing,
            Map<Pos, DenShape.Kind> room, Pos centre, int half, Type type) {
        for (var e : room.entrySet()) {
            Pos p = e.getKey();
            if (Math.max(Math.abs(p.x()), Math.abs(p.z())) > Settlement.MAX_EXTENT || !CellarShape.safe(terrain, p)) return false;
            DenShape.Kind old = existing.get(p);
            if ((e.getValue() == DenShape.Kind.WATER || e.getValue() == DenShape.Kind.GLASS) && old != null && old != DenShape.Kind.FILL) return false;
        }
        for (int dx = -half; dx <= half; dx++) for (int dz = -half; dz <= half; dz++) {
            if (!terrain.solid(centre.offset(dx, -1, dz)) || !terrain.solid(centre.offset(dx,3,dz))) return false;
            if (type == Type.CISTERN && Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && !terrain.solid(centre.offset(dx,-2,dz))) return false;
        }
        return true;
    }

    private static Map<Pos, DenShape.Kind> merge(Map<Pos, DenShape.Kind> existing, Map<Pos, DenShape.Kind> room) {
        Map<Pos, DenShape.Kind> merged = new LinkedHashMap<>(existing);
        for (var e : room.entrySet()) {
            DenShape.Kind old = existing.get(e.getKey());
            if (old == null || old == DenShape.Kind.FILL || old == DenShape.Kind.AIR && e.getValue() == DenShape.Kind.AIR) merged.put(e.getKey(), e.getValue());
        }
        return merged;
    }

    private static List<Pos> connect(Settlement settlement, TunnelShape.Terrain terrain, Map<Pos, DenShape.Kind> target,
            Map<Pos, DenShape.Kind> merged, Settlement.Prop prop, Pos centre, int half) {
        ArrayDeque<Node> queue = new ArrayDeque<>();
        Set<Pos> seen = new HashSet<>();
        for (int dx = -half; dx <= half; dx++) for (int dz = -half; dz <= half; dz++) {
            Pos p = centre.offset(dx,0,dz);
            if (walks(merged,p)) { queue.add(new Node(p,null,0)); seen.add(p); }
        }
        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (walks(target,node.pos())) {
                List<Pos> route = new ArrayList<>();
                for (Node n = node; n != null; n = n.previous()) route.add(n.pos());
                Collections.reverse(route);
                return route;
            }
            if (node.length() >= 8) continue;
            for (int[] d : AROUND) for (int dy : new int[]{0,-1,1}) {
                Pos p = node.pos().offset(d[0],dy,d[1]);
                if (Math.max(Math.abs(p.x()), Math.abs(p.z())) >= Settlement.MAX_EXTENT || Math.abs(p.y()-centre.y()) > 3 || seen.contains(p)
                        || !CellarShape.safe(terrain,p) || !support(terrain,merged,p.offset(0,-1,0))) continue;
                boolean allowed = true;
                for (Settlement.House h : settlement.houses()) if (Math.max(Math.abs(p.x()-h.dx()),Math.abs(p.z()-h.dz())) <= 1) allowed = false;
                for (Settlement.Prop other : settlement.props()) if (other != prop && Math.max(Math.abs(p.x()-other.dx()),Math.abs(p.z()-other.dz())) <= 1 && !walks(target,p)) allowed = false;
                for (int y = 0; y < 3; y++) {
                    Pos q = p.offset(0,y,0);
                    DenShape.Kind kind = merged.get(q);
                    if (!CellarShape.safe(terrain,q) || kind != null && kind != DenShape.Kind.FILL && kind != DenShape.Kind.AIR) allowed = false;
                }
                if (merged.get(p.offset(0,3,0)) == DenShape.Kind.AIR || !CellarShape.safe(terrain,p.offset(0,3,0))) allowed = false;
                if (!terrain.solid(p.offset(0,3,0))) allowed = false;
                for (Settlement.Cluster t : settlement.clusters()) if (Math.max(Math.abs(p.x()-t.dx()), Math.abs(p.z()-t.dz())) <= 3) {
                    Integer surface = terrain.surface(p.x(),p.z());
                    if (surface == null || p.y()+3 >= surface-1) allowed = false;
                }
                if (allowed && seen.add(p)) queue.add(new Node(p,node,node.length()+1));
            }
        }
        return null;
    }

    private static boolean support(TunnelShape.Terrain terrain, Map<Pos, DenShape.Kind> cells, Pos p) {
        return cells.containsKey(p) ? CellarShape.full(cells.get(p)) : terrain.solid(p);
    }

    private static boolean walks(Map<Pos, DenShape.Kind> cells, Pos p) {
        return cells.get(p) == DenShape.Kind.AIR && cells.get(p.offset(0,1,0)) == DenShape.Kind.AIR
                && cells.get(p.offset(0,2,0)) == DenShape.Kind.AIR && CellarShape.full(cells.get(p.offset(0,-1,0)));
    }

    private static void addEntrance(Settlement settlement, Settlement.Prop prop, int top, TunnelShape.Terrain terrain,
            Map<Pos, DenShape.Kind> cells, List<TunnelShape.Mouth> mouths, List<Feature> features, Set<DenShape.Pos> excluded, boolean hatch) {
        for (int[] d : AROUND) {
            Settlement.House guide = new Settlement.House(prop.dx(),prop.dz(),d[0],d[1]);
            Pos start = new Pos(prop.dx()+2*d[0],top,prop.dz()+2*d[1]);
            if (!terrain.replaceable(start) || !terrain.replaceable(start.offset(0,1,0)) || !terrain.solid(start.offset(0,-1,0))) continue;
            if (settlement.houses().stream().anyMatch(h -> Math.max(Math.abs(start.x()-h.dx()),Math.abs(start.z()-h.dz())) <= 1
                    || start.x() == h.dx()+2*h.doorDx() && start.z() == h.dz()+2*h.doorDz())) continue;
            List<Pos> steps = CellarShape.find(settlement,guide,terrain,cells,mouths,start);
            if (steps.isEmpty()) continue;
            Map<Pos, DenShape.Kind> ramp = CellarShape.build(settlement,terrain,cells,mouths,start,steps);
            if (ramp.isEmpty()) continue;
            if (hatch) {
                Pos first = steps.getFirst();
                ramp.put(first.offset(0,1,0), first.x() == start.x() ? DenShape.Kind.HATCH_X : DenShape.Kind.HATCH_Z);
            }
            cells.putAll(ramp);
            ramp.keySet().forEach(p -> excluded.add(new DenShape.Pos(p.x(),p.z())));
            features.add(new Feature(hatch ? Type.MARKET : Type.FARM_ENTRANCE,start));
            mouths.add(new TunnelShape.Mouth(-1,start));
            return;
        }
    }
}
