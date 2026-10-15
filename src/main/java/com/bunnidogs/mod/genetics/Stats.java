package com.bunnidogs.mod.genetics;

import java.util.Arrays;
import java.util.Optional;

/**
 * The bred stat points of one bunnidog (see {@link Stat}): immutable, five signed whole numbers, all zero for a
 * bunnidog nobody has bred yet. Saved as text, e.g. {@code Stats:"3,-1,0,2,12"} (Vitality, Swiftness, Leap,
 * Senses, Bloom), so it is hand-editable: {@code /summon} and {@code /data merge entity} both work with it.
 *
 * <p>Also holds the curves that turn points into gameplay numbers. Starting values, all tunable:
 * <ul>
 *   <li>Vitality and Senses: +4% per point, without limit;</li>
 *   <li>Swiftness and Leap: +4% per point up to 25 points (x2), then a square-root tail (a huge speed or jump
 *       breaks vanilla movement) that is still unbounded;</li>
 *   <li>below zero every stat loses 4% per point but never drops under {@value #NEGATIVE_FLOOR}x;</li>
 *   <li>Bloom is not an attribute: it speeds up growing up, see {@link #growthMultiplier(double)}.</li>
 * </ul>
 */
public final class Stats {
    public static final Stats ZERO = new Stats(new int[Stat.COUNT]);

    /** Points are clamped to this range only so int arithmetic can never overflow. */
    public static final int LIMIT = 1_000_000;

    public static final double PER_POINT = 0.04;
    public static final double NEGATIVE_FLOOR = 0.3;
    public static final double TAIL_START = 25.0;
    public static final double TAIL_SCALE = 0.15;

    /** Growth multiplier: +5% per Bloom point up to 20 points (x2), then a square-root tail. */
    public static final double BLOOM_PER_POINT = 0.05;
    public static final double BLOOM_TAIL_START = 20.0;
    public static final double BLOOM_TAIL_DIVISOR = 4.0;
    public static final double GROWTH_FLOOR = 0.4;
    /** The 60 second floor: nobody grows up in under Growth.MIN_BABY_TICKS, so the multiplier is capped here. */
    public static final double GROWTH_CAP = (double) Growth.BABY_TICKS / Growth.MIN_BABY_TICKS;

    private final int[] points;

    private Stats(int[] points) {
        this.points = points;
    }

    /** Builds stats from up to five numbers in {@link Stat} order; missing ones are 0, extra ones are ignored. */
    public static Stats of(int... values) {
        int[] out = new int[Stat.COUNT];
        for (int i = 0; i < out.length && i < values.length; i++) {
            out[i] = Math.max(-LIMIT, Math.min(LIMIT, values[i]));
        }
        return new Stats(out);
    }

    public int get(Stat stat) {
        return points[stat.ordinal()];
    }

    public Stats with(Stat stat, int value) {
        int[] copy = points.clone();
        copy[stat.ordinal()] = Math.max(-LIMIT, Math.min(LIMIT, value));
        return new Stats(copy);
    }

    public boolean isZero() {
        for (int p : points) {
            if (p != 0) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ curves

    /** The multiplier a stat's points put on the matching attribute (Vitality, Swiftness, Leap, Senses only). */
    public double multiplier(Stat stat) {
        return attributeMultiplier(stat, get(stat));
    }

    /** How many times faster than vanilla this bunnidog's pups grow up (Bloom). */
    public double growthMultiplier() {
        return growthMultiplier(get(Stat.BLOOM));
    }

    public static double attributeMultiplier(Stat stat, double p) {
        if (stat == Stat.BLOOM) {
            throw new IllegalArgumentException("Bloom is not an attribute multiplier, see growthMultiplier");
        }
        double linear = 1.0 + PER_POINT * p;
        if (p < 0) {
            return Math.max(NEGATIVE_FLOOR, linear);
        }
        if ((stat == Stat.SWIFTNESS || stat == Stat.LEAP) && p > TAIL_START) {
            return (1.0 + PER_POINT * TAIL_START) + TAIL_SCALE * Math.sqrt(p - TAIL_START);
        }
        return linear;
    }

    public static double growthMultiplier(double bloom) {
        double m;
        if (bloom < 0) {
            m = Math.max(GROWTH_FLOOR, 1.0 + BLOOM_PER_POINT * bloom);
        } else if (bloom <= BLOOM_TAIL_START) {
            m = 1.0 + BLOOM_PER_POINT * bloom;
        } else {
            m = (1.0 + BLOOM_PER_POINT * BLOOM_TAIL_START) + Math.sqrt(bloom - BLOOM_TAIL_START) / BLOOM_TAIL_DIVISOR;
        }
        return Math.min(GROWTH_CAP, m);
    }

    // ------------------------------------------------------------------ text form

    /** Canonical text form: five comma-separated numbers in {@link Stat} order, e.g. {@code 3,-1,0,2,12}. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(points[i]);
        }
        return sb.toString();
    }

    /**
     * Parses the text form. Separators are commas, semicolons or spaces; fewer than five numbers are padded with 0
     * (so a save from before a stat existed still loads). Empty if the text has no number, more than five, or junk.
     */
    public static Optional<Stats> parse(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String[] parts = text.trim().split("[,;\\s]+");
        if (parts.length > Stat.COUNT) {
            return Optional.empty();
        }
        int[] values = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                values[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return Optional.of(of(values));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Stats other && Arrays.equals(points, other.points);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(points);
    }
}
