package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;

/** V1c decorations, separate from the saved legacy house geometry. */
public final class HouseDetails {
    private HouseDetails() {}
    public enum Kind { DOOR_LOWER, DOOR_UPPER, SHUTTER, BEAM, LANTERN }
    public record Cell(int dx, int dy, int dz, int facingX, int facingZ, Kind kind) {}

    public static List<Cell> cells(int doorX, int doorZ, HouseShape.Form form, int level) {
        if (level < 2) return List.of();
        List<Cell> out = new ArrayList<>();
        out.add(new Cell(doorX,0,doorZ,doorX,doorZ,Kind.DOOR_LOWER));
        out.add(new Cell(doorX,1,doorZ,doorX,doorZ,Kind.DOOR_UPPER));
        if (level >= 4) {
            int y = form == HouseShape.Form.MOUND && level >= 5 ? 2 : 1;
            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                if (d[0] == doorX && d[1] == doorZ) continue;
                out.add(new Cell(2*d[0],y,2*d[1],d[0],d[1],Kind.SHUTTER));
            }
        }
        if (level >= 5) {
            out.add(new Cell(2*doorX,3,2*doorZ,doorX,doorZ,Kind.BEAM));
            out.add(new Cell(2*doorX,2,2*doorZ,doorX,doorZ,Kind.LANTERN));
        }
        return List.copyOf(out);
    }
}
