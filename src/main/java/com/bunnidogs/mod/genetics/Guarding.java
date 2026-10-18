package com.bunnidogs.mod.genetics;

/** Opt-in feral Nest defense, deliberately smaller than the ordinary daytime home boundary. */
public final class Guarding {
    private Guarding() {}
    public static final int RADIUS = 8;
    public static final int SEARCH_INTERVAL = 40;
    public static final int PATH_ATTEMPTS = 4;
    public static final int REPATH_INTERVAL = 10;
    public static final int MAX_PURSUIT = 200;
    public static final int BITE_INTERVAL = 20;
    public enum Status { UNAVAILABLE, DISABLED, ADULT, FERAL, HOME, TRANSPORTED, HEALTH, COMBAT, OUTSIDE_HOME, READY, ACTIVE }
    public static boolean within(double squaredDistance) { return squaredDistance <= RADIUS*RADIUS; }
}
