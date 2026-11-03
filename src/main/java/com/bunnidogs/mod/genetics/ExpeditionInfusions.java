package com.bunnidogs.mod.genetics;

import java.util.Random;

/** Conditional operator-kit references, derived from selected homozygous zero-point founders.
 * These account for stat development and renewable refill separately; they do not measure founder selection,
 * replacement-parent maturation, failed siblings, farm throughput or a player's Survival history. */
public final class ExpeditionInfusions {
    private ExpeditionInfusions() {}

    public record Line(Genome genome, Stats parents, int richGenerations, int plainGenerations) {
        /** Plain feeding recomputes quality; setup quality is never inherited by the next seed. */
        public Seed seed() { return Seed.of(genome, parents, genome, parents, GuavaTier.PLAIN.quality()); }

        /** One frozen, reproducible roll, with a real golden carrier rather than an administrative quality override. */
        public Infusion bottle() { return Infusion.roll(seed(), GuavaTier.GOLDEN, new Random(0)); }
    }

    private static Genome fixed(Genome genome, Gene gene, String name) {
        int allele = gene.alleleNamed(name).id();
        return genome.with(gene, 0, allele).with(gene, 1, allele);
    }

    private static Genome strength() {
        var genome = fixed(Genome.DEFAULT, Gene.EARS, "short");
        genome = fixed(genome, Gene.BUILD, "woolly");
        genome = fixed(genome, Gene.COAT, "ember");
        return fixed(genome, Gene.TEMPERAMENT, "feral");
    }

    private static Genome recovery() {
        return fixed(fixed(Genome.DEFAULT, Gene.EARS, "bat"), Gene.LEGS, "short");
    }

    private static Line develop(Genome genome, int rich, int plain) {
        Stats parents = Stats.ZERO;
        var random = new Random(0);
        // Retaining the selected genotype is conditional; actual hatching/selection is checked by InfusionCheck.
        for (int generation = 0; generation < rich + plain; generation++) {
            double quality = generation < rich ? GuavaTier.NETHERITE.quality() : GuavaTier.PLAIN.quality();
            parents = Drift.inherit(parents, parents, Drift.drift(genome, genome, quality), random);
        }
        return new Line(genome, parents, rich, plain);
    }

    public static final Line LATE_STRENGTH = develop(strength(), 1, 2);
    public static final Line ENDGAME_STRENGTH = develop(strength(), 9, 1);
    public static final Line LATE_RECOVERY = develop(recovery(), 2, 1);
    public static final Line ENDGAME_RECOVERY = develop(recovery(), 13, 0);

    public static Infusion bottle(boolean endgame, boolean strength) {
        return (strength ? endgame ? ENDGAME_STRENGTH : LATE_STRENGTH
                : endgame ? ENDGAME_RECOVERY : LATE_RECOVERY).bottle();
    }
}
