package com.bunnidogs.mod.genetics;

/** Existing expressed fur colors acquire local cover; no new allele or saved state. */
public final class Camouflage {
    private Camouflage() {}
    public static final double VISIBILITY = .5;
    public static final int DISTURBED_TICKS = 100;
    public enum Cover {
        NONE("no cover"), SAND("sand"), EARTH("exposed earth"), GREEN("grass, moss or leaves"),
        SNOW("snow"), STONE("pale stone"), DARK_STONE("dark stone"), NETHER("netherrack"), OBSIDIAN("obsidian");
        private final String ground;
        Cover(String ground) { this.ground = ground; }
        public String key() { return name().toLowerCase(java.util.Locale.ROOT); }
        public String ground() { return this.ground; }
    }
    public enum Status { UNAVAILABLE, LUMINOUS, DISTURBED, EXPOSED, BLENDED }
    public static Cover cover(Genome genome) { return cover(genome.expressed(Gene.COAT).name()); }
    public static Cover cover(String coat) {
        return switch (coat) {
            case "tan", "cream", "sand" -> Cover.SAND;
            case "brown", "spotted" -> Cover.EARTH;
            case "moss" -> Cover.GREEN;
            case "snow" -> Cover.SNOW;
            case "pale", "seal" -> Cover.STONE;
            case "slate" -> Cover.DARK_STONE;
            case "ember" -> Cover.NETHER;
            case "void" -> Cover.OBSIDIAN;
            default -> Cover.NONE;
        };
    }
    public static String explanation(Allele coat) {
        Cover cover = cover(coat.name());
        return cover == Cover.NONE ? "Its light prevents camouflage."
                : "When healthy and calm on " + cover.ground()
                + ", halves native visual detection range and leaves no walking vibrations. Injury, combat, love, Glowing, water, transport and carried bundles break cover; attacks and landings still make vibrations.";
    }
}
