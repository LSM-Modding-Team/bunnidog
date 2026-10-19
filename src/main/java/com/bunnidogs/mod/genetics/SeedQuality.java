package com.bunnidogs.mod.genetics;

/** Inventory color follows the highest guava-quality threshold reached, never a rounded-up investment. */
public final class SeedQuality {
    private SeedQuality() {}
    public static GuavaTier band(double quality) {
        GuavaTier band = GuavaTier.PLAIN;
        if (!Double.isFinite(quality)) return band;
        for (var tier : GuavaTier.values()) if (quality >= tier.quality()) band = tier;
        return band;
    }
    /** Native model properties are floats: encode the chosen band so near-boundary doubles cannot round upward. */
    public static float modelValue(double quality) { return (float)band(quality).quality(); }
}
