package com.bunnidogs.mod.genetics;

/** Non-lethal diagnostics: recovery belongs to the animal, never its genome or breeding state. */
public final class Sampling {
    private Sampling() {}
    public static final int TOOL_USES = 32;
    public static final int RECOVERY_TICKS = 5 * 60 * 20;
    public static boolean healthy(float health, float maximum) { return health > 0 && maximum > 0 && health >= maximum / 2; }
}
