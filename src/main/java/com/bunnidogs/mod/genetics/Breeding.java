package com.bunnidogs.mod.genetics;

import java.util.random.RandomGenerator;

/**
 * Phase 2 breeding rules that do not need Minecraft: how big a litter is, how long parents rest afterwards, and
 * the crowding guard. The entity ({@code Bunnidog}) only feeds numbers in and acts on the answers, so all of this
 * is covered by {@code tools/GenomeCheck.java}.
 *
 * <p>Fertility (the expressed Fertility allele's multiplier: shy 0.6, average 1.0, prolific 1.5, swarm 2.0) is the
 * "how often they breed and litter size" gene from DESIGN.md:
 * <ul>
 *   <li>litter: on average {@value #PUPS_PER_FERTILITY} x the parents' average fertility, 1..{@value #MAX_LITTER} pups;</li>
 *   <li>cooldown: each parent rests {@value #BASE_COOLDOWN_TICKS} / (its own fertility) ticks before it can breed again;</li>
 *   <li>and the existing trade-off in {@link Genome#mutationChance}: every pup rolls its own mutation, at a chance
 *       that grows with fertility squared, so a swarm litter mutates far more often than a shy one.</li>
 * </ul>
 */
public final class Breeding {
    private Breeding() {}

    /** Vanilla animals rest 5 minutes after breeding (6000 ticks); average-fertility bunnidogs do the same. */
    public static final int BASE_COOLDOWN_TICKS = 6000;
    /** However fertile, nobody breeds more often than once a minute. */
    public static final int MIN_COOLDOWN_TICKS = 1200;
    /** Mean litter size at fertility 1.0. */
    public static final double PUPS_PER_FERTILITY = 2.0;
    /** Hard cap on one litter. */
    public static final int MAX_LITTER = 6;
    /**
     * Crowding guard, purely to keep worlds playable: if this many bunnidogs (pups included) already live within
     * {@link #CROWD_RADIUS} blocks, no more are born there. Not a gameplay mechanic, a safety net for swarm lines.
     */
    public static final int CROWD_LIMIT = 40;
    public static final double CROWD_RADIUS = 16.0;

    /** Mean litter size for this pair, before crowding: 2 x average fertility (shy x shy 1.2 ... swarm x swarm 4.0). */
    public static double expectedLitter(Genome a, Genome b) {
        double fertility = (a.traits().fertility() + b.traits().fertility()) / 2.0;
        return PUPS_PER_FERTILITY * fertility;
    }

    /**
     * One litter roll, before crowding. The mean is {@link #expectedLitter} rounded stochastically (2.6 gives 2 or
     * 3 pups, 3 with 60%), then limited to 1..{@link #MAX_LITTER}. A successful mating always makes at least one pup.
     */
    public static int rollLitter(Genome a, Genome b, RandomGenerator rng) {
        double mean = expectedLitter(a, b);
        int whole = (int) Math.floor(mean);
        int size = whole + (rng.nextDouble() < mean - whole ? 1 : 0);
        return Math.max(1, Math.min(MAX_LITTER, size));
    }

    /**
     * The litter that is actually born: {@link #rollLitter} shrunk to whatever room is left under the crowding
     * limit. 0 means "too crowded, nothing is born" (the parents still rest).
     *
     * @param nearby how many bunnidogs (both parents and all pups included) are within {@link #CROWD_RADIUS}
     */
    public static int litterSize(Genome a, Genome b, int nearby, RandomGenerator rng) {
        int room = Math.max(0, CROWD_LIMIT - nearby);
        return Math.min(rollLitter(a, b, rng), room);
    }

    /** Ticks a parent with this fertility multiplier rests after a mating. Shy 10000, average 6000, swarm 3000. */
    public static int cooldownTicks(float fertility) {
        double f = Math.max(0.1, fertility);
        return Math.max(MIN_COOLDOWN_TICKS, (int) Math.round(BASE_COOLDOWN_TICKS / f));
    }
}
