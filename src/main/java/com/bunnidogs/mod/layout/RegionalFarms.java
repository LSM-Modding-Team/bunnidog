package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.PropShape.Cell;
import com.bunnidogs.mod.layout.PropShape.Mat;

/** Versioned farm variants, all inside the original 3x3 footprint and three-block height reservation. */
public final class RegionalFarms {
    private RegionalFarms() {}
    /** Underground features omit sky/water landmarks but can fit their water-free regional farm. */
    public static boolean eligible(PropShape.Kind kind, Lineage style, boolean surface) {
        return surface || kind == PropShape.Kind.FARM && (style == Lineage.CAVE || style == Lineage.NETHER);
    }
    private static void ground(List<Cell> out, Mat soil, boolean garden) {
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
            out.add(new Cell(x,-1,z,garden && x == -1 && z == -1 ? Mat.FILL : soil));
        if (garden) out.add(new Cell(-1,0,-1,Mat.TROUGH));
    }
    public static List<Cell> cells(Lineage style, int turns, boolean garden) {
        return cells(style,turns,garden,false);
    }
    public static List<Cell> cells(Lineage style, int turns, boolean garden, boolean melonBeds) {
        var raw = new ArrayList<Cell>();
        switch (style) {
            case MEADOW -> {
                if (!melonBeds) return PropShape.cells(PropShape.Kind.FARM,turns,garden);
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                    boolean trough = garden && x == -1 && z == -1;
                    boolean fruit = z == 1 && x != 0;
                    raw.add(new Cell(x,-1,z,x == 0 && z == 0 ? Mat.WATER : trough || fruit ? Mat.FILL : Mat.FARMLAND));
                    if (x == 0 && z == 0) continue;
                    raw.add(new Cell(x,0,z,trough ? Mat.TROUGH : fruit ? Mat.FRUIT_SPACE : z == 0 ? Mat.MELON_STEM
                            : x == 0 ? z < 0 ? Mat.POTATOES : Mat.CARROTS : Mat.WHEAT));
                }
            }
            case DESERT -> {
                ground(raw,Mat.SAND,garden);
                raw.add(new Cell(0,0,0,Mat.CACTUS)); raw.add(new Cell(0,1,0,Mat.CACTUS));
            }
            case NETHER -> {
                ground(raw,Mat.SOUL_SAND,garden);
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
                    if (!(x == 0 && z == 0) && !(garden && x == -1 && z == -1)) raw.add(new Cell(x,0,z,Mat.NETHER_WART));
            }
            case SWAMP -> {
                ground(raw,Mat.MYCELIUM,garden);
                raw.add(new Cell(-1,0,0,Mat.RED_MUSHROOM)); raw.add(new Cell(1,0,0,Mat.RED_MUSHROOM));
                raw.add(new Cell(0,0,-1,Mat.BROWN_MUSHROOM)); raw.add(new Cell(0,0,1,Mat.BROWN_MUSHROOM));
            }
            case CAVE -> {
                ground(raw,Mat.FILL,garden);
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) raw.add(new Cell(x,2,z,Mat.PLANK));
                for (int x : new int[]{-1,1}) for (int y = 0; y <= 1; y++) raw.add(new Cell(x,y,1,Mat.PLANK));
                raw.add(new Cell(0,1,-1,Mat.GLOW_VINE)); raw.add(new Cell(0,1,0,Mat.GLOW_VINE));
            }
            case END -> { ground(raw,Mat.END_STONE,garden); raw.add(new Cell(0,0,0,Mat.CHORUS_FLOWER)); }
            default -> { return PropShape.cells(PropShape.Kind.FARM,turns,garden); }
        }
        var out = new ArrayList<Cell>();
        for (var c : raw) {
            int x = c.dx(), z = c.dz();
            for (int t = 0; t < Math.floorMod(turns,4); t++) { int next = -z; z = x; x = next; }
            out.add(new Cell(x,c.dy(),z,c.mat()));
        }
        out.sort(Comparator.comparingInt(Cell::dy));
        return List.copyOf(out);
    }
}
