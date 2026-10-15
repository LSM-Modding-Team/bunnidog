package com.bunnidogs.mod.genetics;

/**
 * What one bunnidog remembers of the guava that put it in love (Phase 4e). Pure Java, no Minecraft; the entity owns one
 * and forwards three events to it: it was fed ({@link #remember}), a tick passed ({@link #tick}) and it mated
 * ({@link #forget}). When the pair mates, the seed's quality uses {@link GuavaTier#average} of the two memories.
 *
 * <p>The memory copies the life of vanilla's love mode, which it must never outlive: love lasts {@link #TICKS} ticks
 * (30 s) and ends at once when the animal's age is no longer 0 (a mating sets it to the breeding cooldown). So a tier
 * remembered from a guava can never leak into a later love that came from somewhere else. While it is not active,
 * {@link #tier()} is 0, which is the plain guava's quality.
 */
public final class GuavaMemory {
    /** Vanilla's love mode lasts 600 ticks; the memory lasts as long. */
    public static final int TICKS = 600;

    private int tier;
    private int ticksLeft;

    /** A guava of this tier has just put the bunnidog in love (a negative tier counts as 0). Overwrites any older memory. */
    public void remember(int guavaTier) {
        this.tier = Math.max(0, guavaTier);
        this.ticksLeft = TICKS;
    }

    /**
     * One game tick. {@code age} is the animal's age (0 = an adult that may breed; a pup is negative, an adult that
     * just bred is positive). The memory ends when its time is up or the age has left 0.
     */
    public void tick(int age) {
        if (this.ticksLeft <= 0) {
            return;
        }
        this.ticksLeft--;
        if (this.ticksLeft == 0 || age != 0) {
            this.forget();
        }
    }

    /** The remembered tier, or 0 when there is no memory. */
    public int tier() {
        return this.ticksLeft > 0 ? this.tier : 0;
    }

    /** Ticks the memory has left; 0 = no memory. */
    public int ticksLeft() {
        return this.ticksLeft;
    }

    public boolean active() {
        return this.ticksLeft > 0;
    }

    public void forget() {
        this.tier = 0;
        this.ticksLeft = 0;
    }

    /** Loads a saved memory. Anything that is not a live memory (no time left, junk values) means "no memory". */
    public void restore(int savedTier, int savedTicksLeft) {
        int ticks = Math.max(0, Math.min(TICKS, savedTicksLeft));
        if (ticks == 0) {
            this.forget();
            return;
        }
        this.tier = Math.max(0, savedTier);
        this.ticksLeft = ticks;
    }
}
