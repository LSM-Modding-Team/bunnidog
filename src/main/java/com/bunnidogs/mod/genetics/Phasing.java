package com.bunnidogs.mod.genetics;

/** The End's inherited defensive movement. Damage and ordinary falling remain meaningful. */
public final class Phasing {
    private Phasing() {}
    public static final int RECOVERY_TICKS = 400;
    public static final int MIN_RANGE = 3;
    public static final int RANGE = 6;
    public static final int HEIGHT = 1;
    public static final int DISTANCE_GAIN = 2;

    public static boolean farther(double beforeSquared, double afterSquared) {
        return Math.sqrt(afterSquared) >= Math.sqrt(beforeSquared)+DISTANCE_GAIN;
    }
    public static boolean inRange(double horizontalSquared, double height) {
        return Double.isFinite(horizontalSquared) && Double.isFinite(height)
                && horizontalSquared >= MIN_RANGE*MIN_RANGE && horizontalSquared <= RANGE*RANGE
                && Math.abs(height) <= HEIGHT;
    }
}
