package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Real units for the Animal Sheet (docs/SHEET_SPECS.md section 6): what a stat means in hearts, blocks per second,
 * blocks high, blocks of hearing and minutes. Pure Java, so the numbers on the sheet are the numbers the self-check tests.
 *
 * <p>The final value of an attribute is {@code base x gene factor ({@link Traits}) x bred factor ({@link Stats})}: the
 * very same product {@code Bunnidog#applyStats} sets, so the sheet cannot disagree with the game (the base values below
 * are copies of the {@code Bunnidog.BASE_*} constants, which this package cannot import; the self-check compares them).
 *
 * <p><b>ASSUMED, to be calibrated in game:</b> {@link #BLOCKS_PER_SECOND_PER_SPEED}. The speed shown is the top speed at
 * a goal speed modifier of 1.0 (walking); goals that flee or chase multiply it.
 */
public final class Units {
    private Units() {}

    // ---- copies of Bunnidog.BASE_* (checked against the entity's source by tools/GenomeCheck.java)
    public static final double BASE_HEALTH = 8.0D;
    public static final double BASE_SPEED = 0.25D;
    public static final double BASE_JUMP = 0.42D;
    public static final double BASE_FOLLOW_RANGE = 16.0D;

    /** One heart is two health points. */
    public static final double HP_PER_HEART = 2.0D;
    /**
     * ASSUMED: blocks per second for a movement-speed attribute of 1.0 (the vanilla player walks at 0.1 = 4.317 blocks/s,
     * a horse at 0.3375 = 14.6 blocks/s). CALIBRATE: time a bunnidog over 20 flat blocks and change this one line.
     */
    public static final double BLOCKS_PER_SECOND_PER_SPEED = 43.17D;
    /** Vanilla vertical motion per tick: velocity = (velocity - GRAVITY) x DRAG. */
    public static final double GRAVITY = 0.08D;
    public static final double DRAG = 0.98D;
    /** Heart icons shown at most; beyond that one icon and a multiplier. */
    public static final int MAX_HEART_ICONS = 10;

    // ------------------------------------------------------------------ the five real values

    /** Max health in health points. */
    public static double healthPoints(Genome genome, Stats stats) {
        return BASE_HEALTH * genome.traits().healthMultiplier() * stats.multiplier(Stat.VITALITY);
    }

    public static double hearts(Genome genome, Stats stats) {
        return healthPoints(genome, stats) / HP_PER_HEART;
    }

    /** The movement-speed attribute (before conversion to blocks per second). */
    public static double speedAttribute(Genome genome, Stats stats) {
        return BASE_SPEED * genome.traits().speedMultiplier() * stats.multiplier(Stat.SWIFTNESS);
    }

    public static double blocksPerSecond(Genome genome, Stats stats) {
        return speedAttribute(genome, stats) * BLOCKS_PER_SECOND_PER_SPEED;
    }

    /** The jump-strength attribute (the initial upward velocity in blocks per tick). */
    public static double jumpAttribute(Genome genome, Stats stats) {
        return BASE_JUMP * genome.traits().jumpMultiplier() * stats.multiplier(Stat.LEAP);
    }

    /**
     * Height of a jump in blocks, from a small simulation of vanilla vertical motion: the velocity is set to the
     * attribute, then each tick the entity moves by it and {@code velocity = (velocity - 0.08) x 0.98}. The vanilla
     * default 0.42 gives about 1.252 blocks.
     */
    public static double jumpHeight(double jumpAttribute) {
        double y = 0.0D;
        double v = Math.max(0.0D, jumpAttribute);
        for (int tick = 0; tick < 100_000 && v > 0.0D; tick++) {
            y += v;
            v = (v - GRAVITY) * DRAG;
        }
        return y;
    }

    public static double jumpHeight(Genome genome, Stats stats) {
        return jumpHeight(jumpAttribute(genome, stats));
    }

    /** The follow-range attribute, in blocks: how far away it notices things. */
    public static double senseBlocks(Genome genome, Stats stats) {
        return BASE_FOLLOW_RANGE * genome.traits().hearingMultiplier() * stats.multiplier(Stat.SENSES);
    }

    public static double bloomMinutes(Stats stats) {
        return Growth.minutesToAdult(stats.growthMultiplier());
    }

    // ------------------------------------------------------------------ hearts as icons

    /**
     * A row of heart icons: {@code full} whole hearts and maybe one half. Up to {@link #MAX_HEART_ICONS} icons are drawn
     * literally ({@code times} is 0); more than that is one icon and {@code times} = the hearts rounded ("x12").
     * There is always at least a half heart, so the row is never empty.
     */
    public record HeartRow(int full, boolean half, int times) {}

    public static HeartRow heartRow(double hearts) {
        int halves = Math.max(1, (int) Math.round(hearts * 2.0D));
        if (halves > MAX_HEART_ICONS * 2) {
            return new HeartRow(1, false, (int) Math.round(hearts));
        }
        return new HeartRow(halves / 2, halves % 2 == 1, 0);
    }

    // ------------------------------------------------------------------ one stat, as the sheet shows it

    /**
     * One stat row of the sheet. {@code valueText} is the real value ("7.4 hearts"), {@code baseText} the plain average
     * bunnidog's ("base 4.0 hearts") so that "+3" means something.
     */
    public record Reading(Stat stat, int points, double value, double base, String valueText, String baseText) {}

    public static Reading reading(Stat stat, Genome genome, Stats stats) {
        double value = valueOf(stat, genome, stats);
        double base = valueOf(stat, Genome.DEFAULT, Stats.ZERO);
        return new Reading(stat, stats.get(stat), value, base, text(stat, value), "base " + unitText(stat, base));
    }

    /** All five readings in {@link Stat} order. */
    public static List<Reading> readings(Genome genome, Stats stats) {
        List<Reading> out = new ArrayList<>(Stat.COUNT);
        for (Stat stat : Stat.values()) {
            out.add(reading(stat, genome, stats));
        }
        return out;
    }

    /** The real value in the stat's own unit (hearts, blocks/s, blocks, blocks, minutes). */
    public static double valueOf(Stat stat, Genome genome, Stats stats) {
        return switch (stat) {
            case VITALITY -> hearts(genome, stats);
            case SWIFTNESS -> blocksPerSecond(genome, stats);
            case LEAP -> jumpHeight(genome, stats);
            case SENSES -> senseBlocks(genome, stats);
            case BLOOM -> bloomMinutes(stats);
        };
    }

    /** The value with its unit only: "7.4 hearts", "13.8 blocks/s", "1.25 blocks high", "21 blocks", "9.4 min". */
    public static String unitText(Stat stat, double value) {
        return switch (stat) {
            case VITALITY -> String.format(Locale.ROOT, "%.1f hearts", value);
            case SWIFTNESS -> String.format(Locale.ROOT, "%.1f blocks/s", value);
            case LEAP -> String.format(Locale.ROOT, "%.2f blocks high", value);
            case SENSES -> String.format(Locale.ROOT, "%d blocks", Math.round(value));
            case BLOOM -> String.format(Locale.ROOT, "%.1f min", value);
        };
    }

    /** The sentence form the sheet shows: unit text, with "hears" and "grows up in" for the last two. */
    public static String text(Stat stat, double value) {
        return switch (stat) {
            case SENSES -> "hears " + unitText(stat, value);
            case BLOOM -> "grows up in " + unitText(stat, value);
            default -> unitText(stat, value);
        };
    }

    // ------------------------------------------------------------------ the working (hover text)

    /**
     * The multiplication behind a stat, e.g. {@code 8 HP x 1.375 stocky build x 1.12 bred = 12.4 HP}. The gene factor is
     * named after the gene that causes it; Bloom has no gene factor.
     */
    public static String working(Stat stat, Genome genome, Stats stats) {
        Traits t = genome.traits();
        return switch (stat) {
            case VITALITY -> String.format(Locale.ROOT, "%s HP x %s %s build x %s bred = %.1f HP (2 HP = 1 heart)",
                    num(BASE_HEALTH), num(t.healthMultiplier()), Plain.word(genome.expressed(Gene.BUILD)),
                    num(stats.multiplier(Stat.VITALITY)), healthPoints(genome, stats));
            case SWIFTNESS -> String.format(Locale.ROOT, "speed %s x %s %s legs x %s bred = %s, which is %.1f blocks/s",
                    num(BASE_SPEED), num(t.speedMultiplier()), Plain.word(genome.expressed(Gene.LEGS)),
                    num(stats.multiplier(Stat.SWIFTNESS)), num(speedAttribute(genome, stats)), blocksPerSecond(genome, stats));
            case LEAP -> String.format(Locale.ROOT, "jump %s x %s %s legs x %s bred = %s, which is %.2f blocks high",
                    num(BASE_JUMP), num(t.jumpMultiplier()), Plain.word(genome.expressed(Gene.LEGS)),
                    num(stats.multiplier(Stat.LEAP)), num(jumpAttribute(genome, stats)), jumpHeight(genome, stats));
            case SENSES -> String.format(Locale.ROOT, "%s blocks x %s %s ears x %s bred = %.1f blocks",
                    num(BASE_FOLLOW_RANGE), num(t.hearingMultiplier()), Plain.word(genome.expressed(Gene.EARS)),
                    num(stats.multiplier(Stat.SENSES)), senseBlocks(genome, stats));
            case BLOOM -> String.format(Locale.ROOT, "%s min as a pup / %s growth speed = %.1f min",
                    num(Growth.minutesToAdult(1.0D)), num(stats.growthMultiplier()), bloomMinutes(stats));
        };
    }

    /** A factor with up to 3 decimals and no trailing zeros: 1.375, 1.12, 1, 8. */
    public static String num(double x) {
        String s = String.format(Locale.ROOT, "%.3f", x);
        if (s.indexOf('.') >= 0) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}
