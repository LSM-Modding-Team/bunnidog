package com.bunnidogs.mod.layout;

import java.util.OptionalInt;
import java.util.function.BiFunction;

/** A small abandoned barn fits dry clearings rather than bridging lakes or cutting hills. */
public final class BarnSite {
    private BarnSite() {}
    public static final int RADIUS = 10;
    public static final int MAX_SLOPE = 1;
    public record Surface(int height, boolean dryGround) {}
    public static OptionalInt height(BiFunction<Integer,Integer,Surface> sample) {
        int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
        for (int x = -RADIUS; x <= RADIUS; x += 5) for (int z = -RADIUS; z <= RADIUS; z += 5) {
            Surface surface = sample.apply(x,z);
            if (surface == null || !surface.dryGround()) return OptionalInt.empty();
            low = Math.min(low,surface.height()); high = Math.max(high,surface.height());
            if (high-low > MAX_SLOPE) return OptionalInt.empty();
        }
        return OptionalInt.of(high);
    }
}
