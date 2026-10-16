package com.bunnidogs.mod.genetics;

/** Brief, non-combat retreats. Bounds are independent of world access and navigation. */
public final class Refuge {
    private Refuge() {}
    public static final int RANGE = 8;
    public static final int SEARCH_INTERVAL = 40;
    public static final int PATH_ATTEMPTS = 8;
    public static final int MAX_VISIT = 200;
    public static final int QUIET_TICKS = 40;
    public static final int SAFE_DISTANCE = 4;
    public static boolean eligible(boolean adult, boolean home, boolean feral, boolean transported, boolean combat, boolean insideHome) {
        return adult && home && !feral && !transported && !combat && insideHome;
    }
    public static boolean safer(double currentSquared, double destinationSquared) {
        return Double.isFinite(currentSquared) && Double.isFinite(destinationSquared)
                && destinationSquared >= SAFE_DISTANCE*SAFE_DISTANCE && destinationSquared >= currentSquared+4;
    }
}
