package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import com.bunnidogs.mod.layout.TunnelShape.Pos;

/** Small wall labels for fitted rooms and exits. Sign/support pairs always belong to the same chunk. */
public final class Wayfinding {
    private Wayfinding() {}
    // IDs are saved. Append new entries; never reorder or reuse an ID.
    public enum Label {
        DEN("Den", "Resting Nests"), NURSERY("Nursery", "Grand tree hall"),
        PANTRY("Pantry", "Warren supplies"), CISTERN("Cistern", "Water below"),
        MARKET("Market", "Surface hatch"), FARM("Farm", "Surface stairs"),
        COMMONS("Lower commons", "More Nests"), EXIT("Way out", "Surface stairs");
        private final String title, detail;
        Label(String title, String detail) { this.title = title; this.detail = detail; }
        public String title() { return this.title; }
        public String detail() { return this.detail; }
    }
    public enum Facing {
        NORTH(0,-1), EAST(1,0), SOUTH(0,1), WEST(-1,0);
        public final int dx, dz;
        Facing(int dx, int dz) { this.dx = dx; this.dz = dz; }
    }
    public record Sign(Pos pos, Facing facing, Label label) {
        public Pos support() { return pos.offset(-facing.dx,0,-facing.dz); }
    }
    private record Candidate(Sign sign, int distance) {}

    public static List<Sign> plan(Settlement settlement, TunnelShape.Network network, int originX, int originZ) {
        Map<Pos,DenShape.Kind> cells = new HashMap<>();
        network.cells().forEach(c -> cells.put(c.pos(),c.kind()));
        List<Sign> signs = new ArrayList<>();
        for (int index : network.dens().stream().sorted().toList()) {
            var tree = settlement.clusters().get(index);
            var nest = network.cells().stream().filter(c -> c.kind() == DenShape.Kind.NEST
                    && Math.abs(c.pos().x()-tree.dx()) <= 3 && Math.abs(c.pos().z()-tree.dz()) <= 3)
                    .max(Comparator.comparingInt(c -> c.pos().y()));
            if (nest.isPresent()) add(signs,cells,new Pos(tree.dx(),nest.get().pos().y()+2,tree.dz()),
                    settlement.level() == 5 && index == 0 ? Label.NURSERY : Label.DEN,4,1,originX,originZ);
        }
        for (var feature : network.features()) {
            Label label = switch (feature.type()) {
                case PANTRY -> Label.PANTRY;
                case CISTERN -> Label.CISTERN;
                case MARKET -> Label.MARKET;
                case FARM_ENTRANCE -> Label.FARM;
                case LOWER_HALL -> Label.COMMONS;
                default -> null;
            };
            if (label != null) add(signs,cells,feature.centre().offset(0,1,0),label,4,2,originX,originZ);
        }
        var roomsWithExit = new HashSet<Integer>();
        for (var mouth : network.mouths()) if (!roomsWithExit.contains(mouth.room())) {
            if (add(signs,cells,mouth.doorstep().offset(0,-1,0),Label.EXIT,4,3,originX,originZ)) roomsWithExit.add(mouth.room());
        }
        return List.copyOf(signs);
    }
    private static boolean wall(DenShape.Kind kind) {
        return kind == DenShape.Kind.FILL || kind == DenShape.Kind.WALL || kind == DenShape.Kind.BEAM;
    }
    private static boolean add(List<Sign> signs, Map<Pos,DenShape.Kind> cells, Pos centre, Label label,
            int horizontal, int vertical, int originX, int originZ) {
        List<Candidate> candidates = new ArrayList<>();
        for (var cell : cells.entrySet()) {
            Pos p = cell.getKey();
            int dx = p.x()-centre.x(), dy = p.y()-centre.y(), dz = p.z()-centre.z();
            if (cell.getValue() != DenShape.Kind.AIR || Math.abs(dx) > horizontal || Math.abs(dz) > horizontal || Math.abs(dy) > vertical
                    || cells.get(p.offset(0,-1,0)) != DenShape.Kind.AIR || cells.get(p.offset(0,1,0)) != DenShape.Kind.AIR
                    || !CellarShape.full(cells.get(p.offset(0,-2,0)))) continue;
            if (signs.stream().anyMatch(s -> Math.abs(s.pos().x()-p.x()) + Math.abs(s.pos().y()-p.y()) + Math.abs(s.pos().z()-p.z()) < 3)) continue;
            for (Facing facing : Facing.values()) {
                Sign sign = new Sign(p,facing,label);
                Pos support = sign.support();
                if (wall(cells.get(support)) && Math.floorDiv(originX+p.x(),16) == Math.floorDiv(originX+support.x(),16)
                        && Math.floorDiv(originZ+p.z(),16) == Math.floorDiv(originZ+support.z(),16)) {
                    candidates.add(new Candidate(sign,dx*dx + dz*dz + 4*dy*dy));
                }
            }
        }
        var best = candidates.stream().min(Comparator.comparingInt(Candidate::distance)
                .thenComparingInt(c -> c.sign().pos().x()).thenComparingInt(c -> c.sign().pos().y())
                .thenComparingInt(c -> c.sign().pos().z()).thenComparingInt(c -> c.sign().facing().ordinal()));
        best.ifPresent(c -> signs.add(c.sign()));
        return best.isPresent();
    }
    /** Five integers per sign: relative position, facing ID, label ID. Malformed/future tuples are skipped. */
    public static int[] encode(List<Sign> signs) {
        int[] out = new int[signs.size()*5]; int i = 0;
        for (Sign sign : signs) {
            out[i++] = sign.pos().x(); out[i++] = sign.pos().y(); out[i++] = sign.pos().z();
            out[i++] = sign.facing().ordinal(); out[i++] = sign.label().ordinal();
        }
        return out;
    }
    public static List<Sign> decode(int[] data) {
        List<Sign> out = new ArrayList<>();
        for (int i = 0; i+5 <= data.length; i+=5) if (data[i+3] >= 0 && data[i+3] < Facing.values().length
                && data[i+4] >= 0 && data[i+4] < Label.values().length) {
            out.add(new Sign(new Pos(data[i],data[i+1],data[i+2]),Facing.values()[data[i+3]],Label.values()[data[i+4]]));
        }
        return List.copyOf(out);
    }
}
