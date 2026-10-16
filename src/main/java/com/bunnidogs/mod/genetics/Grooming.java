package com.bunnidogs.mod.genetics;

/** Loose-whisker care: only a developed Senses line, with conservative renewable yield. */
public final class Grooming {
    private Grooming() {}
    public static final int MIN_SENSES = 10;
    public static final int COUNT = 1;
    public static final int RECOVERY_TICKS = 10 * 60 * 20;
    public static boolean developed(Stats stats) { return stats.get(Stat.SENSES) >= MIN_SENSES; }
}
