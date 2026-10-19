package com.bunnidogs.mod.genetics;

/** Bounded loose-food use; every meal/work job keeps the existing animal recovery. */
public final class Scavenging {
    private Scavenging() {}
    public static final int MIN_ITEM_AGE = 200;
    public static final int MIN_GATHERING_SENSES = 10;
    public static final int TROUGH_RADIUS = 3;
    public static final int PATH_ATTEMPTS = 8;
    public static final int MAX_VISIT = 200;
    public static boolean oldEnough(int age) { return age >= MIN_ITEM_AGE; }
    public static boolean gathers(Stats stats) { return stats.get(Stat.SENSES) >= MIN_GATHERING_SENSES; }
}
