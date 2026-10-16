package com.bunnidogs.mod.layout;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.bunnidogs.mod.layout.TunnelShape.Pos;

/** U7: a fitted lower commons beneath a grand-tree hall; full-block stairs preserve all upper Nests. */
final class LowerHall {
    private LowerHall() {}
    private static final int[][] AROUND = {{1,0},{-1,0},{0,1},{0,-1}};

    static UndergroundRooms.Feature add(TunnelShape.Terrain terrain, Map<Pos,DenShape.Kind> existing, int hallFloor) {
        int floor = hallFloor - 6;
        Map<Pos,DenShape.Kind> addition = new LinkedHashMap<>();
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) for (int y = -1; y <= 3; y++) {
            boolean inside = Math.abs(x) <= 2 && Math.abs(z) <= 2 && y >= 0 && y < 3;
            addition.put(new Pos(x,floor+y,z),inside ? DenShape.Kind.AIR : DenShape.Kind.FILL);
        }
        // The first three cuts are inside the hall, clear of nursery Nests and hay.
        // The remaining stairwell passes beside it, then enters the lower room's east wall.
        List<Pos> steps = List.of(new Pos(1,hallFloor-1,1),new Pos(2,hallFloor-2,1),new Pos(3,hallFloor-3,1),
                new Pos(4,hallFloor-4,1),new Pos(4,hallFloor-5,0),new Pos(4,floor,-1),new Pos(3,floor,-1),new Pos(2,floor,-1));
        for (Pos step : steps) {
            addition.put(step.offset(0,-1,0),DenShape.Kind.FILL);
            Pos roof = step.offset(0,3,0);
            if (existing.get(roof) != DenShape.Kind.AIR) addition.put(roof,DenShape.Kind.FILL);
            for (int[] d : AROUND) for (int y = 0; y < 3; y++) {
                Pos p = step.offset(d[0],y,d[1]);
                if (!existing.containsKey(p)) addition.putIfAbsent(p,DenShape.Kind.FILL);
            }
        }
        for (Pos step : steps) for (int y = 0; y < 3; y++) addition.put(step.offset(0,y,0),DenShape.Kind.AIR);
        for (int x : new int[]{-2,2}) for (int z : new int[]{-2,2}) addition.put(new Pos(x,floor-1,z),DenShape.Kind.NEST);
        addition.put(new Pos(-1,floor,-1),DenShape.Kind.BARREL);
        addition.put(new Pos(1,floor,1),DenShape.Kind.HAY);
        addition.put(new Pos(1,floor+1,1),DenShape.Kind.LANTERN);

        for (var cell : addition.entrySet()) {
            Pos p = cell.getKey();
            if (!CellarShape.safe(terrain,p)) return null;
            DenShape.Kind old = existing.get(p);
            if (old != null && old != DenShape.Kind.AIR && old != DenShape.Kind.FILL) return null;
            if (old == DenShape.Kind.AIR && cell.getValue() != DenShape.Kind.AIR) return null;
        }
        Map<Pos,DenShape.Kind> merged = new HashMap<>(existing);
        merged.putAll(addition);
        // Keep deep room floors/roofs supported, and never open underneath another corridor outside the hall.
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            if (!terrain.solid(new Pos(x,floor-1,z)) || !terrain.solid(new Pos(x,floor+3,z))) return null;
        }
        for (var cell : existing.entrySet()) {
            Pos p = cell.getKey();
            if (Math.max(Math.abs(p.x()),Math.abs(p.z())) <= 3 || cell.getValue() != DenShape.Kind.AIR) continue;
            if (CellarShape.full(existing.get(p.offset(0,-1,0))) && !CellarShape.full(merged.get(p.offset(0,-1,0)))) return null;
        }
        for (Pos step : steps) {
            if (!terrain.solid(step.offset(0,-1,0)) || !CellarShape.full(merged.get(step.offset(0,-1,0)))) return null;
            for (int y = 0; y < 3; y++) if (merged.get(step.offset(0,y,0)) != DenShape.Kind.AIR) return null;
        }
        existing.putAll(addition);
        return new UndergroundRooms.Feature(UndergroundRooms.Type.LOWER_HALL,new Pos(0,floor,0));
    }
}
