package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The forecast of a litter (Genetics Table T3, docs/TABLE_SPECS.md): what the pups of two Tissue Samples are likely to be like, worked
 * out with the SAME rules the game rolls them with, so the table can never promise something the seed will not do. Pure Java, nothing is rolled.
 *
 * <ul>
 *   <li><b>Looks</b> ({@link #looks}): Mendel. A pup takes one of each parent's two alleles of a gene (50% each, independently), so there are four equally
 *       likely pairs and the stronger one shows ({@link Genome#expressed}). The odds are therefore always 25%, 50%, 75% or 100%. They are the odds BEFORE a
 *       mutation ({@link #mutationChance}); a mutation changes one random gene of a pup, so it is told apart instead of being smeared over the odds.</li>
 *   <li><b>Litter</b>: the mean is {@link Breeding#expectedLitter}; a litter has the whole part of it or one more ({@link #litterLow} / {@link #litterHigh}).</li>
 *   <li><b>Stats</b> ({@link #expectedPoints}): each pup starts at the parents' average and moves by the pair's {@link Drift}; the seed's own maths.</li>
 * </ul>
 *
 * The forecast is for a seed made from plain guavas (guava quality 0): the only quality it counts is the parents' own starborn bonus, which the seed
 * adds by itself ({@link Seed#of}). Better guavas and Fortune raise it later; the forecast says so instead of guessing which guava you will use.
 * The two samples are symmetric: nothing here depends on which is called mother and which father.
 */
public record Cross(Tissue a, Tissue b) {
    /** One thing the pups can show for a gene, and the chance that a pup shows it. */
    public record Outcome(Allele allele, double chance) {}

    /** Carries at least one copy / has two matching copies, before mutation. */
    public record Inherited(Allele allele, double carried, double fixed) {}

    private static final int COMBINATIONS = 4;

    /** The seed these two would make on plain guavas (its quality is only the starborn bonus); the maths below is the seed's own. */
    public Seed seed() {
        return Seed.of(this.a.genome(), this.a.stats(), this.b.genome(), this.b.stats(), 0.0D);
    }

    /**
     * The looks a pup can have for {@code gene} and the chance of each, most likely first (the stronger allele first when equally likely).
     * The chances add up to 1; at most three alleles appear.
     */
    public List<Outcome> looks(Gene gene) {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        Map<Integer, Allele> alleles = new LinkedHashMap<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Allele fromA = this.a.genome().allele(gene, i);
                Allele fromB = this.b.genome().allele(gene, j);
                Allele shown = fromB.dominance() > fromA.dominance() ? fromB : fromA;       // the rule of Genome#expressed
                counts.merge(shown.id(), 1, Integer::sum);
                alleles.put(shown.id(), shown);
            }
        }
        List<Outcome> out = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            out.add(new Outcome(alleles.get(e.getKey()), e.getValue() / (double) COMBINATIONS));
        }
        out.sort(Comparator.comparingDouble(Outcome::chance).reversed().thenComparing(Comparator.comparingInt((Outcome o) -> o.allele().dominance()).reversed()));
        return out;
    }

    /** All alleles a pup can inherit, including recessive copies hidden by its look. */
    public List<Inherited> inherited(Gene gene) {
        Map<Integer, Integer> carried = new LinkedHashMap<>();
        Map<Integer, Integer> fixed = new LinkedHashMap<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int a = this.a.genome().alleleId(gene, i);
                int b = this.b.genome().alleleId(gene, j);
                carried.merge(a, 1, Integer::sum);
                if (a == b) fixed.merge(a, 1, Integer::sum);
                else carried.merge(b, 1, Integer::sum);
            }
        }
        return carried.entrySet().stream()
                .map(e -> new Inherited(gene.allele(e.getKey()), e.getValue() / (double) COMBINATIONS,
                        fixed.getOrDefault(e.getKey(), 0) / (double) COMBINATIONS))
                .sorted(Comparator.comparingDouble(Inherited::carried).reversed()
                        .thenComparing(Comparator.comparingInt((Inherited o) -> o.allele().dominance()).reversed()))
                .toList();
    }

    /** At least one success in an uncrowded litter, mixing the actual two possible litter sizes. */
    public double atLeastOneInLitter(double perPup) {
        double low = 1.0D - Math.pow(1.0D - perPup, litterLow());
        if (litterLow() == litterHigh()) return low;
        double high = 1.0D - Math.pow(1.0D - perPup, litterHigh());
        double extra = litter() - Math.floor(litter());
        return low * (1.0D - extra) + high * extra;
    }

    /** All seven pairs fixed, allowing any matching allele at each gene, before mutation. */
    public double fixedPupChance() {
        double chance = 1;
        for (Gene gene : Gene.values()) chance *= inherited(gene).stream().mapToDouble(Inherited::fixed).sum();
        return chance;
    }

    /** Mean litter size before crowding ({@link Breeding#expectedLitter}). */
    public double litter() {
        return Breeding.expectedLitter(this.a.genome(), this.b.genome());
    }

    /** The smallest litter that can be rolled: the whole part of the mean, at least 1 and at most {@link Breeding#MAX_LITTER}. */
    public int litterLow() {
        return Math.max(1, Math.min(Breeding.MAX_LITTER, (int) Math.floor(litter())));
    }

    /** The biggest litter that can be rolled: one more than {@link #litterLow} unless the mean is a whole number (same limits). */
    public int litterHigh() {
        double mean = litter();
        int whole = (int) Math.floor(mean);
        return Math.max(1, Math.min(Breeding.MAX_LITTER, mean > whole ? whole + 1 : whole));
    }

    /** The chance that a pup mutates (the seed's own: it grows with the parents' fertility). */
    public double mutationChance() {
        return seed().mutationChance();
    }

    /** The seed quality of this pair on plain guavas: only the starborn bonus. */
    public double quality() {
        return seed().quality();
    }

    /** What the pups' stats move by on top of the parents' average, in {@link Stat} order (the seed's own drift at {@link #quality()}). */
    public double[] drift() {
        return seed().drift(0.0D);
    }

    /** The average of the two parents' points for a stat, before the drift. */
    public double startPoints(Stat stat) {
        return (this.a.stats().get(stat) + this.b.stats().get(stat)) / 2.0D;
    }

    /** The points a pup is expected to have for a stat: the parents' average plus the drift (each pup is then rounded to whole points at random, so siblings differ). */
    public double expectedPoints(Stat stat) {
        return startPoints(stat) + drift()[stat.ordinal()];
    }
}
