package com.bunnidogs.mod.genetics;

/**
 * Where a bunnidog lives (docs/HOME_SPECS.md, session 41): the numbers and the pure decisions of the home rules. A dog claims a
 * free Nest block, the way a villager claims a bed. H1 held the numbers; H2 adds the decisions of R1, R2 and R6 here so the
 * self-check can test them. The Minecraft side (the point-of-interest calls, the path check) lives in {@code entity/NestHome}.
 */
public final class Home {
    private Home() {}

    /** Dogs one Nest can hold (the point-of-interest type's tickets): one dog, one nest. */
    public static final int NEST_TICKETS = 1;
    /** How close a dog must stand to use a Nest, in blocks (the point-of-interest type's valid range). */
    public static final int NEST_RANGE = 1;
    /** R3: a homeless dog looks for a free nest within this many blocks of itself. */
    public static final int SEARCH_RADIUS = 24;
    /** R5: a dog with a home is called back when it is more than this many blocks from its nest. */
    public static final int HOME_RADIUS = 16;
    /** R5: ... and walks until it is within this many blocks of the nest again. */
    public static final int RETURN_RADIUS = 8;

    /** R2: a homeless adult looks for a nest about every this many ticks ... */
    public static final int LOOK_INTERVAL = 100;
    /** R2: ... plus a random 0 to this many, so a herd does not all look on the same tick. */
    public static final int LOOK_JITTER = 40;
    /** R6: a dog with a nest checks that the block is still there every this many ticks. */
    public static final int CHECK_INTERVAL = 200;
    /** R4: nests that were claimed and found unreachable are remembered (and skipped) up to this many, then forgotten. */
    public static final int REJECT_MEMORY = 8;

    /** R5: true when a dog at squared distance {@code distSq} from its nest is too far away and must walk home. */
    public static boolean tooFar(double distSq) {
        return distSq > (double) HOME_RADIUS * HOME_RADIUS;
    }

    /** R5: true when a dog walking home has arrived (close enough to stop). */
    public static boolean arrived(double distSq) {
        return distSq <= (double) RETURN_RADIUS * RETURN_RADIUS;
    }

    /** R1: only an ADULT without a nest looks for one; pups follow a parent, a dog with a nest has what it needs. */
    public static boolean wantsHome(boolean adult, boolean hasNest) {
        return adult && !hasNest;
    }

    /** R2: ticks until the next look; {@code roll} is a random number from 0 to {@link #LOOK_JITTER} (anything else is clamped). */
    public static int lookDelay(int roll) {
        return LOOK_INTERVAL + Math.max(0, Math.min(LOOK_JITTER, roll));
    }

    /** What the timer asks for when it runs out (R2, R6): a homeless adult looks, a dog with a nest checks it, a pup does nothing. */
    public enum Step { NONE, LOOK, CHECK }

    /** Decides the step of one timer tick: nothing until {@code ticksLeft} reaches 0, then the step that fits the dog. */
    public static Step step(boolean adult, boolean hasNest, int ticksLeft) {
        if (ticksLeft > 0) {
            return Step.NONE;
        }
        if (hasNest) {
            return Step.CHECK;
        }
        return wantsHome(adult, false) ? Step.LOOK : Step.NONE;
    }

    /** R4 memory: true when {@code count} rejected nests is the point where the list is cleared so old rejects get another chance. */
    public static boolean rejectsFull(int count) {
        return count >= REJECT_MEMORY;
    }

    // ------------------------------------------------------------------ H4: the night (R8)

    /** R8: ticks in one day of the overworld clock. */
    public static final int DAY_LENGTH = 24000;
    /** R8: from this tick of the day (dusk) an adult with a nest walks home ... */
    public static final int DUSK = 12500;
    /** R8: ... and stays until this tick (dawn). Rain does not count: only the clock does. */
    public static final int DAWN = 23000;
    /** R8: at night the dog stays within this many blocks of its nest. */
    public static final int NIGHT_RADIUS = 2;
    /** R8: a dog that cannot find a path home asks again only every this many ticks (a path search every tick would cost too much). */
    public static final int NIGHT_RETRY = 20;

    /** R8: true when the clock reading {@code clockTicks} (total ticks of the dimension's default clock) falls between dusk and dawn. */
    public static boolean isNight(long clockTicks) {
        long t = Math.floorMod(clockTicks, (long) DAY_LENGTH);
        return t >= DUSK && t < DAWN;
    }

    /** R8: true when a dog at squared distance {@code distSq} from its nest is farther than the night radius and must walk home. */
    public static boolean nightFar(double distSq) {
        return distSq > (double) NIGHT_RADIUS * NIGHT_RADIUS;
    }
}
