package com.bunnidogs.mod.genetics;

/** Conservative work beside an explicitly enabled trough, independent of hunger and love. */
public final class Gardening {
    private Gardening() {}
    public static final int SEARCH_INTERVAL = 200;
    public static final int REST_TICKS = 1200;
    public static final int WORK_TICKS = 60;
    public static final int MAX_VISIT = 240;
    public static final int CROP_RADIUS = 3;
    public static boolean eligible(boolean adult, boolean healthy, boolean home, boolean night,
            boolean transported, boolean danger, boolean rested) {
        return adult && healthy && home && !night && !transported && !danger && rested;
    }
}
