package com.bunnidogs.mod.genetics;

import java.util.random.RandomGenerator;

/**
 * How a pair's stats change in the next generation (docs/DESIGN.md "4. Bred stats"). The PARENTS' genes set the
 * direction (their expressed alleles' {@link Trends}); quality sets the size:
 * <ul>
 *   <li>gains are multiplied by {@code 1 + 0.5 q};</li>
 *   <li>costs by {@code max(0, 1 - 0.25 q)}, so they are gone at quality 4;</li>
 *   <li>tolls (only {@code starborn} has them) are not touched by quality.</li>
 * </ul>
 * At quality 0 every ordinary trend is zero-sum: a line can be re-specialised but not grown. A pup's points are the
 * average of its parents' points plus the average of the two parents' drifts, rounded to whole points at random
 * (so siblings differ).
 *
 * <p>Quality here is {@code guava tier + starborn bonus + Fortune}. In Phase 4a only the starborn bonus exists:
 * {@link #pairQualityBonus}. Pure Java, checked by tools/GenomeCheck.java.
 */
public final class Drift {
    private Drift() {}

    public static final double GAIN_PER_QUALITY = 0.5;
    public static final double COST_RELIEF_PER_QUALITY = 0.25;
    /** A starborn parent adds this x sqrt(its Bloom) to the seed quality (square root: a linear bonus runs away). */
    public static final double STARBORN_FACTOR = 0.3;

    private static final int STARBORN_ID = Gene.FERTILITY.alleleNamed("starborn").id();

    public static double gainFactor(double quality) {
        return 1.0 + GAIN_PER_QUALITY * Math.max(0.0, quality);
    }

    public static double costFactor(double quality) {
        return Math.max(0.0, 1.0 - COST_RELIEF_PER_QUALITY * Math.max(0.0, quality));
    }

    /** What one parent's genes push per generation at this quality, in {@link Stat} order (gains +, costs and tolls -). */
    public static double[] trend(Genome parent, double quality) {
        double gain = gainFactor(quality);
        double cost = costFactor(quality);
        double[] out = new double[Stat.COUNT];
        for (Gene gene : Gene.values()) {
            for (Trends.Term term : Trends.terms(gene, parent.expressed(gene))) {
                out[term.stat().ordinal()] += switch (term.kind()) {
                    case GAIN -> term.amount() * gain;
                    case COST -> -term.amount() * cost;
                    case TOLL -> -term.amount();
                };
            }
        }
        return out;
    }

    /** The drift of a pair: the average of the two parents' trends. */
    public static double[] drift(Genome a, Genome b, double quality) {
        double[] ta = trend(a, quality);
        double[] tb = trend(b, quality);
        double[] out = new double[Stat.COUNT];
        for (int i = 0; i < out.length; i++) {
            out[i] = (ta[i] + tb[i]) / 2.0;
        }
        return out;
    }

    /** The seed-quality bonus one parent brings: 0.3 x sqrt(Bloom) if it EXPRESSES starborn, else nothing. */
    public static double qualityBonus(Genome parent, Stats stats) {
        if (parent.expressed(Gene.FERTILITY).id() != STARBORN_ID) {
            return 0.0;
        }
        return STARBORN_FACTOR * Math.sqrt(Math.max(0, stats.get(Stat.BLOOM)));
    }

    /** The bonus the seed gets: the AVERAGE of both parents' bonuses (a pure starborn pair gets all of it). */
    public static double pairQualityBonus(Genome a, Stats sa, Genome b, Stats sb) {
        return (qualityBonus(a, sa) + qualityBonus(b, sb)) / 2.0;
    }

    /** One pup's stats: the parents' average plus the pair's drift, each stat rounded to whole points at random. */
    public static Stats inherit(Stats a, Stats b, double[] drift, RandomGenerator rng) {
        int[] out = new int[Stat.COUNT];
        for (Stat stat : Stat.values()) {
            int i = stat.ordinal();
            out[i] = stochasticRound((a.get(stat) + b.get(stat)) / 2.0 + drift[i], rng);
        }
        return Stats.of(out);
    }

    /** 2.3 gives 2 (70%) or 3 (30%); whole numbers are exact. Unbiased, also for negatives. */
    public static int stochasticRound(double x, RandomGenerator rng) {
        double floor = Math.floor(x);
        return (int) floor + (rng.nextDouble() < x - floor ? 1 : 0);
    }
}
