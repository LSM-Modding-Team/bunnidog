package com.bunnidogs.mod.genetics;

/** Deliberate two-parent feeding, with the actual mating left to native courtship. */
public final class Pen {
    private Pen() {}
    public static final int RANGE = 4;
    public static final int ISOLATION = 12;
    public static final double PAIR_DISTANCE = 3;
    public enum Status { READY, PAIR, TOO_FAR, UNAVAILABLE, HOME, UNSAFE, UNLOADED, PROTECTED, EMPTY, CHANGED, BUSY, FED }
}
