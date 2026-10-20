package com.bunnidogs.mod.genetics;

/** Conservative work beside an explicitly enabled trough, independent of hunger and love. */
public final class Gardening {
    private Gardening() {}
    public static final int SEARCH_INTERVAL = 200;
    public static final int REST_TICKS = 1200;
    public static final int WORK_TICKS = 60;
    public static final int MAX_VISIT = 240;
    public static final int CROP_RADIUS = 3;
    public static final int MUSHROOM_RADIUS = 3;
    public static final int MIN_CULTIVATION_BLOOM = 10;
    public static final int MIN_NURSERY_BLOOM = 20;
    public static final int MIN_CHORUS_BLOOM = 20;
    public static final int COMPOST_RESERVE = 8;
    public static boolean cultivates(Stats stats) { return stats.get(Stat.BLOOM) >= MIN_CULTIVATION_BLOOM; }
    public static boolean nurses(Stats stats) { return stats.get(Stat.BLOOM) >= MIN_NURSERY_BLOOM; }
    public static boolean tendsChorus(Stats stats) { return stats.get(Stat.BLOOM) >= MIN_CHORUS_BLOOM; }
    public enum Status { READY, UNAVAILABLE, ADULT, FULL_HEALTH, HOME, DAYTIME, TRANSPORTED, DANGER, RESTING, OUTSIDE_HOME, GRIEFING_OFF, PAUSED, TRAIL }
    public static Status status(boolean adult, boolean healthy, boolean home, boolean night,
            boolean transported, boolean danger, boolean rested) {
        if (!adult) return Status.ADULT;
        if (!healthy) return Status.FULL_HEALTH;
        if (!home) return Status.HOME;
        if (transported) return Status.TRANSPORTED;
        if (danger) return Status.DANGER;
        if (night) return Status.DAYTIME;
        return rested ? Status.READY : Status.RESTING;
    }
}
