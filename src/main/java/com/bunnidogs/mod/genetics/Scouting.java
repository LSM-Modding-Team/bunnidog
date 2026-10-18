package com.bunnidogs.mod.genetics;

/** Cave hearing becomes a short-range survey only after deliberate Senses breeding. */
public final class Scouting {
    private Scouting() {}
    public static final int MIN_SENSES = 10;
    public static final int RADIUS = 6;
    public static final int RECOVERY_TICKS = 30*20;
    public enum Status { UNAVAILABLE, ADULT, HEALTH, TRAITS, COMBAT, RESTING, READY }
    public static boolean qualified(Genome genome, Stats stats) {
        return genome.expressed(Gene.EARS).name().equals("bat") && stats.get(Stat.SENSES) >= MIN_SENSES;
    }
}
