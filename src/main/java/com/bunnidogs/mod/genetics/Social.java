package com.bunnidogs.mod.genetics;

/** Short daytime gatherings; no tickets, stat gains or new needs. */
public final class Social {
    private Social() {}
    public static final int SEARCH_INTERVAL = 200;
    public static final int BELL_RANGE = 12;
    public static final int VISIT_TICKS = 400;
    public static final int REST_TICKS = 1200;
    public static boolean gatheringTime(long ticks) { long day = Math.floorMod(ticks,24000L); return day >= 6000 && day < 9000; }
}
