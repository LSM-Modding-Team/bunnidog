package com.bunnidogs.mod.genetics;

/** A deliberate source keeps its meal reserve while stocking a nearby feeding dish. */
public final class Supplying {
    public static final int MIN_SENSES = 20;
    public static final int RESERVE = 8;
    public static final int RANGE = 4;
    public static final int REACH_SQUARED = 9;
    private Supplying() {}
    public static boolean qualified(Stats stats) { return stats.get(Stat.SENSES) >= MIN_SENSES; }
}
