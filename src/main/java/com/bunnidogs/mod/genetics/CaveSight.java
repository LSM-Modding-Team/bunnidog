package com.bunnidogs.mod.genetics;

/** The deferred cave-vision trait gives a nearby keeper a paid, deliberate benefit rather than redundant mob sight. */
public final class CaveSight {
    private CaveSight() {}
    public static final int MIN_SENSES=20, MAX_LIGHT=7;
    public static final int DURATION_TICKS=60*20, RECOVERY_TICKS=30*20;
    public enum Status { UNAVAILABLE, BUSY, ADULT, HEALTH, TRAITS, COMBAT, RESTING, BRIGHT, ALREADY, READY }
    public static boolean qualified(Genome genome,Stats stats) {
        return genome.expressed(Gene.COAT).name().equals("pale") && stats.get(Stat.SENSES)>=MIN_SENSES;
    }
}
