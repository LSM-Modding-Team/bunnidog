package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.bunnidogs.mod.layout.TunnelShape.Pos;

/** Optional room furniture, kept separate from the saved tunnel-cell IDs. */
public final class SettlementFixtures {
    private SettlementFixtures() {}
    // Saved IDs: append only.
    public enum Kind { TROUGH, BELL, NURSERY_CRADLE }
    public record Fixture(Pos pos, Kind kind) {}
    private static final int[][] AROUND = {{1,0},{-1,0},{0,1},{0,-1}};

    public static List<Fixture> plan(Settlement settlement, TunnelShape.Network network, List<Wayfinding.Sign> signs) {
        Map<Pos,DenShape.Kind> cells = new HashMap<>();
        network.cells().forEach(c -> cells.put(c.pos(),c.kind()));
        Set<Pos> reserved = new HashSet<>();
        signs.forEach(s -> { reserved.add(s.pos()); reserved.add(s.pos().offset(0,-1,0)); });
        List<Fixture> out = new ArrayList<>();
        for (int i : network.dens().stream().sorted().toList()) {
            var tree = settlement.clusters().get(i);
            int half = settlement.level() == 5 && i == 0 ? 3 : DenShape.HALF;
            var floor = network.cells().stream().filter(c -> c.kind() == DenShape.Kind.NEST
                    && Math.abs(c.pos().x()-tree.dx()) <= half && Math.abs(c.pos().z()-tree.dz()) <= half)
                    .mapToInt(c -> c.pos().y()+1).max();
            if (floor.isEmpty()) continue;
            Pos centre = new Pos(tree.dx(),floor.getAsInt(),tree.dz());
            if (i == 0 && settlement.level() == 5 && nursery(out,cells,reserved,centre,half)) continue;
            add(out,cells,reserved,centre,half,Kind.TROUGH);
            if (i == 0 && settlement.level() >= 3) add(out,cells,reserved,centre,half,Kind.BELL);
        }
        return List.copyOf(out);
    }

    /** The standalone level-one den has no saved network; its seed still determines the same room. */
    public static List<Fixture> den(DenShape.Den den) {
        Map<Pos,DenShape.Kind> cells = new HashMap<>();
        DenShape.denAir().forEach(c -> cells.put(new Pos(c.dx(),c.dy(),c.dz()),c.kind()));
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) cells.put(new Pos(x,-6,z),DenShape.Kind.FILL);
        den.furnishing().forEach(c -> cells.put(new Pos(c.dx(),c.dy(),c.dz()),c.kind()));
        List<Fixture> out = new ArrayList<>();
        add(out,cells,new HashSet<>(),new Pos(0,-DenShape.DEPTH,0),DenShape.HALF,Kind.TROUGH);
        return List.copyOf(out);
    }
    private static boolean standing(Map<Pos,DenShape.Kind> cells, Pos p) {
        return cells.get(p) == DenShape.Kind.AIR && cells.get(p.offset(0,1,0)) == DenShape.Kind.AIR
                && CellarShape.full(cells.get(p.offset(0,-1,0)));
    }
    private static boolean nursery(List<Fixture> out, Map<Pos,DenShape.Kind> cells, Set<Pos> reserved, Pos centre, int half) {
        // Replace the old northwest hay station's footprint, keeping doorways and broad aisles unchanged.
        Pos p = centre.offset(-half+1,0,-half+1);
        if (!standing(cells,p) || reserved.contains(p) || reserved.contains(p.offset(0,1,0))
                || cells.get(p.offset(0,-1,0)) == DenShape.Kind.NEST) return false;
        List<Pos> pad = new ArrayList<>();
        for (int x = 1; x <= 3; x++) for (int z = -1; z <= 1; z++) {
            Pos slot = p.offset(x,0,z); pad.add(slot);
            if (!standing(cells,slot) || reserved.contains(slot) || reserved.contains(slot.offset(0,1,0))) return false;
        }
        var furniture = new ArrayList<Fixture>(); var proposed = new HashSet<>(reserved);
        furniture.add(new Fixture(p,Kind.NURSERY_CRADLE)); proposed.add(p); proposed.addAll(pad);
        add(furniture,cells,proposed,centre,half,Kind.TROUGH);
        add(furniture,cells,proposed,centre,half,Kind.BELL);
        if (furniture.size() != 3) return false; // The nursery must also retain the hall's ordinary food/social furniture.
        out.addAll(furniture); reserved.addAll(proposed); return true;
    }
    private static void add(List<Fixture> out, Map<Pos,DenShape.Kind> cells, Set<Pos> reserved,
            Pos centre, int half, Kind kind) {
        List<Pos> candidates = new ArrayList<>();
        for (int dx = -half; dx <= half; dx++) for (int dz = -half; dz <= half; dz++) {
            // Leave door middles, the centre and floor Nests clear. Side furniture has at least two walking neighbours.
            if (Math.max(Math.abs(dx),Math.abs(dz)) != half || dx == 0 || dz == 0) continue;
            Pos p = centre.offset(dx,0,dz);
            if (Math.abs(dx) == half && cells.get(p.offset(Integer.signum(dx),0,0)) == DenShape.Kind.AIR
                    || Math.abs(dz) == half && cells.get(p.offset(0,0,Integer.signum(dz))) == DenShape.Kind.AIR) continue;
            if (reserved.contains(p) || reserved.contains(p.offset(0,1,0)) || !standing(cells,p)
                    || cells.get(p.offset(0,-1,0)) == DenShape.Kind.NEST) continue;
            int neighbours = 0;
            for (int[] d : AROUND) if (standing(cells,p.offset(d[0],0,d[1])) && !reserved.contains(p.offset(d[0],0,d[1]))) neighbours++;
            if (neighbours >= 2) candidates.add(p);
        }
        candidates.sort(Comparator.comparingInt(Pos::x).thenComparingInt(Pos::z));
        if (!candidates.isEmpty()) {
            Pos p = candidates.getFirst();
            out.add(new Fixture(p,kind)); reserved.add(p);
        }
    }
    /** Four integers per optional fixture. Old starts and unknown future IDs remain harmless. */
    public static int[] encode(List<Fixture> fixtures) {
        int[] out = new int[fixtures.size()*4]; int i = 0;
        for (var fixture : fixtures) {
            out[i++] = fixture.pos().x(); out[i++] = fixture.pos().y(); out[i++] = fixture.pos().z(); out[i++] = fixture.kind().ordinal();
        }
        return out;
    }
    public static List<Fixture> decode(int[] data) {
        List<Fixture> out = new ArrayList<>();
        for (int i = 0; i+4 <= data.length; i+=4) if (data[i+3] >= 0 && data[i+3] < Kind.values().length)
            out.add(new Fixture(new Pos(data[i],data[i+1],data[i+2]),Kind.values()[data[i+3]]));
        return List.copyOf(out);
    }
}
