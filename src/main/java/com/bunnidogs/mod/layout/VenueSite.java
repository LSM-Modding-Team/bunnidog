package com.bunnidogs.mod.layout;

import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/** Square, rotation-safe clearings for intact kennels and the larger public showground. */
public final class VenueSite {
    private VenueSite() {}
    public record Surface(int height, boolean dryGround) {}
    public static OptionalInt height(int radius, BiFunction<Integer,Integer,Surface> sample) {
        if (radius < 4 || radius > 24) return OptionalInt.empty();
        int[] coordinates = IntStream.rangeClosed(-radius,radius)
                .filter(p -> p == -radius || p == radius || p % 4 == 0).toArray();
        int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
        for (int x : coordinates) for (int z : coordinates) {
            var surface = sample.apply(x,z);
            if (surface == null || !surface.dryGround()) return OptionalInt.empty();
            low = Math.min(low,surface.height()); high = Math.max(high,surface.height());
            if (high-low > 1) return OptionalInt.empty();
        }
        return OptionalInt.of(high);
    }
}
