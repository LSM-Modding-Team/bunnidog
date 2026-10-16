package com.bunnidogs.mod.genetics;

/** Balanced bred-stat milestones. Tier IDs are saved: append only. */
public final class Show {
    private Show() {}
    public static final double RANGE = 6;
    public enum Tier {
        NONE(0, "none"), BRONZE(5, "bronze"), SILVER(20, "silver"), GOLD(50, "gold");
        private final int minimum;
        private final String key;
        Tier(int minimum, String key) { this.minimum = minimum; this.key = key; }
        public int minimum() { return minimum; }
        public String key() { return key; }
    }
    public static int minimum(Stats stats) {
        int lowest = Stats.LIMIT;
        for (Stat stat : Stat.values()) lowest = Math.min(lowest, stats.get(stat));
        return lowest;
    }
    public static Tier rank(Stats stats) {
        int minimum = minimum(stats);
        if (minimum >= Tier.GOLD.minimum()) return Tier.GOLD;
        if (minimum >= Tier.SILVER.minimum()) return Tier.SILVER;
        if (minimum >= Tier.BRONZE.minimum()) return Tier.BRONZE;
        return Tier.NONE;
    }
}
