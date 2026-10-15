package com.bunnidogs.mod.genetics;

/**
 * Growing up faster or slower (Bloom), as pure maths so it is covered by tools/GenomeCheck.java.
 *
 * <p>Vanilla: a baby starts at age {@value #BABY_TICKS} below zero and gains one age tick per game tick, so it is
 * an adult after 20 minutes. The entity adds {@link Step#extraTicks()} on top of that every tick (negative to
 * slow down). {@link Stats#growthMultiplier(double)} is capped so nobody grows up in under
 * {@value #MIN_BABY_TICKS} ticks (60 seconds).
 */
public final class Growth {
    private Growth() {}

    public static final int BABY_TICKS = 24000;
    public static final int MIN_BABY_TICKS = 1200;

    /** {@code extraTicks} to add to the baby's age this tick; {@code carry} is the fraction to keep for next tick. */
    public record Step(int extraTicks, double carry) {}

    /** One tick of ageing at this multiplier (1.0 = vanilla). */
    public static Step step(double carry, double multiplier) {
        double total = carry + (multiplier - 1.0);
        int whole = (int) total;            // truncates towards zero, so slowing down is symmetrical
        return new Step(whole, total - whole);
    }

    /** Minutes a baby needs to grow up at this multiplier (vanilla 20.0). */
    public static double minutesToAdult(double multiplier) {
        return BABY_TICKS / multiplier / 1200.0;
    }
}
