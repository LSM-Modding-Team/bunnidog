package com.bunnidogs.mod.genetics;

/** A serious injury changes idle behavior, never healing or food requirements. */
public final class RecoveryRest {
    private RecoveryRest() {}
    public static final int SEARCH_INTERVAL = 100;
    public static final int PATH_ATTEMPTS = 4;
    public static final int NEST_RADIUS = 2;
    public enum Status { UNAVAILABLE, ADULT, HEALTH, HOME, TRANSPORTED, DANGER, NIGHT, WEATHER, OUTSIDE_HOME, READY }
    public static boolean needed(float health, float maximum) { return maximum > 0 && health > 0 && health < maximum*.5F; }
    public static boolean nearNest(double squared) { return squared >= 0 && squared <= NEST_RADIUS*NEST_RADIUS; }
}
