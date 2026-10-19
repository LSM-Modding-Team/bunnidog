package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;

/** U1's missing worn trail: leave the outward-facing door, round its mound and reach the tree patch. */
public final class EntranceTrail {
    private EntranceTrail() {}
    public static final int REACH = DenShape.MAX_RADIUS+1;

    /** Local trunk-relative columns, in walking order. The two mirrored alternatives preserve the whole mound. */
    public static List<DenShape.Pos> route(DenShape.Dir direction, int radial, int side) {
        if (radial < DenShape.MIN_RADIUS || radial > DenShape.MAX_RADIUS || Math.abs(side) != 1)
            throw new IllegalArgumentException("Invalid entrance trail");
        var out = new ArrayList<DenShape.Pos>();
        for (int v = 0; v <= 2; v++) out.add(DenShape.at(direction,radial+1,side*v));
        for (int u = radial; u >= 2; u--) out.add(DenShape.at(direction,u,side*2));
        return List.copyOf(out);
    }
}
