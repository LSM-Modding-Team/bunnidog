package com.bunnidogs.mod.genetics;

/** A low-priority weather routine: food, work and player control stay more important. */
public final class StormShelter {
    private StormShelter() {}
    public static final int SEARCH_INTERVAL = 100;
    public static final int PATH_ATTEMPTS = 4;
    public static boolean eligible(boolean adult, boolean home, boolean transported, boolean busy, boolean insideHome, boolean thunder) {
        return adult && home && !transported && !busy && insideHome && thunder;
    }
}
