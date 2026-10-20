package com.bunnidogs.mod.layout;

import java.util.OptionalInt;
import java.util.function.BiFunction;

/** A new refuge grows from a shallow ocean bed, away from sampled mainland/coastal columns. */
public final class IslandSite {
    private IslandSite() {}
    public static final int SEA_LEVEL = 63;
    public static final int RADIUS = 24;
    public static final int MIN_DEPTH = 4, MAX_DEPTH = 32;
    public record Column(int bedFirstFree, boolean ocean, boolean openWater) {}
    public static OptionalInt bed(BiFunction<Integer,Integer,Column> sample) {
        int bottom = Integer.MAX_VALUE;
        for (int x = -RADIUS; x <= RADIUS; x += 8) for (int z = -RADIUS; z <= RADIUS; z += 8) {
            if (x*x+z*z > RADIUS*RADIUS) continue;
            var column = sample.apply(x,z);
            if (!water(column)) return OptionalInt.empty();
            int depth = SEA_LEVEL-column.bedFirstFree();
            if (depth < MIN_DEPTH || depth > MAX_DEPTH) return OptionalInt.empty();
            bottom = Math.min(bottom,column.bedFirstFree()-1);
        }
        for (int radius : new int[]{64,96}) for (int ray = 0; ray < 16; ray++) {
            double angle = ray*Math.PI/8;
            if (!water(sample.apply((int)Math.round(Math.cos(angle)*radius),(int)Math.round(Math.sin(angle)*radius))))
                return OptionalInt.empty();
        }
        return OptionalInt.of(bottom);
    }
    private static boolean water(Column column) { return column != null && column.ocean() && column.openWater(); }
}
