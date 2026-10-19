package com.bunnidogs.mod.genetics;

/** A small animal carries native bundles, with room determined by its expressed build. */
public final class Carrying {
    private Carrying() {}
    public static final int MAX_BUNDLES = 4;
    public static final int SETTLE_TICKS = 100;
    public enum Status { UNAVAILABLE, ADULT, DANGER, FULL, READY }
    public static int capacity(Genome genome) { return capacity(genome.expressed(Gene.BUILD).name()); }
    public static int capacity(String build) {
        return switch (build) { case "lean", "slim" -> 1; case "stocky", "woolly" -> 3; case "chonker" -> 4; default -> 2; };
    }
    public static int slowdown(int count, int capacity) { return 25*Math.clamp(count-capacity,0,3); }
    public static String explanation(Allele build) {
        int count = capacity(build.name());
        return "Calm adults can carry "+count+" native bundle"+(count == 1 ? "" : "s")
                +". Equip a bundle; sneak with both hands empty to retrieve the last. Existing cargo survives a smaller build and Travel Baskets; each excess bundle slows movement by 25%, up to 75%.";
    }
}
