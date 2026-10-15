package com.bunnidogs.mod.genetics;

import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * A bunnidog's genes: 6 genes ({@link Gene}) x 2 alleles, packed into 48 bits of a long.
 * Layout: gene i occupies bits [8i, 8i+8): slot 0 in the low nibble, slot 1 in the high nibble.
 * By convention slot 0 came from the mother and slot 1 from the father (so a Pedigree can tell them apart later).
 *
 * <p>Immutable. Canonical text form (used in NBT, {@code /summon ... {Genome:"..."}} and debug output):
 * six 2-digit groups in gene order EARS COAT LEGS BUILD TEMPERAMENT FERTILITY, each digit an allele id in hex,
 * e.g. {@code 00-01-20-00-03-11}.
 */
public record Genome(long packed) {
    /** Chance per offspring of one mutation when both parents have average fertility (1.0). */
    public static final double BASE_MUTATION_CHANCE = 0.04;
    /** Mutation chance is never higher than this, however fertile the parents. */
    public static final double MAX_MUTATION_CHANCE = 0.5;

    /** The genome with the first allele everywhere: all-dominant defaults. Also what a client sees before sync. */
    public static final Genome DEFAULT = new Genome(0L);

    /** Unknown / out-of-range allele ids are replaced by 0, so a corrupt value can never crash rendering. */
    public Genome {
        packed = sanitize(packed);
    }

    private static int shift(Gene gene, int slot) {
        return gene.ordinal() * 8 + slot * 4;
    }

    private static long sanitize(long raw) {
        long out = 0;
        for (Gene g : Gene.values()) {
            for (int slot = 0; slot < 2; slot++) {
                int id = (int) ((raw >>> shift(g, slot)) & 0xF);
                if (id >= g.alleleCount()) {
                    id = 0;
                }
                out |= (long) id << shift(g, slot);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ reading

    /** Allele id (index into the gene's table) stored in the given slot (0 = mother's, 1 = father's). */
    public int alleleId(Gene gene, int slot) {
        return (int) ((packed >>> shift(gene, slot)) & 0xF);
    }

    public Allele allele(Gene gene, int slot) {
        return gene.allele(alleleId(gene, slot));
    }

    /** The allele that shows: the one with the higher dominance. */
    public Allele expressed(Gene gene) {
        Allele a = allele(gene, 0);
        Allele b = allele(gene, 1);
        return b.dominance() > a.dominance() ? b : a;
    }

    /** The allele that is hidden by dominance, if the two alleles differ (a "carrier"). */
    public Optional<Allele> hidden(Gene gene) {
        Allele a = allele(gene, 0);
        Allele b = allele(gene, 1);
        if (a.id() == b.id()) {
            return Optional.empty();
        }
        return Optional.of(b.dominance() > a.dominance() ? a : b);
    }

    public boolean isHomozygous(Gene gene) {
        return alleleId(gene, 0) == alleleId(gene, 1);
    }

    public Traits traits() {
        return Traits.of(this);
    }

    // ------------------------------------------------------------------ writing (returns a new genome)

    public Genome with(Gene gene, int slot, int alleleId) {
        if (slot < 0 || slot > 1 || alleleId < 0 || alleleId >= gene.alleleCount()) {
            throw new IllegalArgumentException("bad allele " + alleleId + " / slot " + slot + " for " + gene);
        }
        long cleared = packed & ~(0xFL << shift(gene, slot));
        return new Genome(cleared | ((long) alleleId << shift(gene, slot)));
    }

    // ------------------------------------------------------------------ text form

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Gene g : Gene.values()) {
            if (g.ordinal() > 0) {
                sb.append('-');
            }
            sb.append(Integer.toHexString(alleleId(g, 0))).append(Integer.toHexString(alleleId(g, 1)));
        }
        return sb.toString();
    }

    /** Parses the canonical text form. Separators ('-', '_', ':', space) are optional. Empty if malformed. */
    public static Optional<Genome> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String digits = text.replaceAll("[-_:\\s]", "");
        if (digits.length() != Gene.COUNT * 2) {
            return Optional.empty();
        }
        long out = 0;
        for (Gene g : Gene.values()) {
            for (int slot = 0; slot < 2; slot++) {
                int id = Character.digit(digits.charAt(g.ordinal() * 2 + slot), 16);
                if (id < 0 || id >= g.alleleCount()) {
                    return Optional.empty();
                }
                out |= (long) id << shift(g, slot);
            }
        }
        return Optional.of(new Genome(out));
    }

    /** Human-readable one-liner per gene, e.g. "Ears: standard (carries short)". Wild alleles show their lineage. */
    public String describe(Gene gene) {
        String s = gene.displayName() + ": " + label(expressed(gene));
        Optional<Allele> h = hidden(gene);
        return h.map(allele -> s + " (carries " + label(allele) + ")").orElse(s);
    }

    /** "tiny [tundra]" for a wild allele, plain name otherwise. */
    private static String label(Allele allele) {
        return allele.isWild() ? allele.name() + " [" + allele.lineage().key() + "]" : allele.name();
    }

    // ------------------------------------------------------------------ creation

    /** A wild-caught founder of the given lineage (see {@link Founders}). MEADOW = the plain common pool. */
    public static Genome randomFounder(Lineage lineage, RandomGenerator rng) {
        return Founders.roll(lineage, rng);
    }

    /** A wild-caught founder of the default (meadow) lineage: every allele drawn from the common pool. */
    public static Genome randomFounder(RandomGenerator rng) {
        long out = 0;
        for (Gene g : Gene.values()) {
            for (int slot = 0; slot < 2; slot++) {
                out |= (long) g.randomCommonAlleleId(rng) << shift(g, slot);
            }
        }
        return new Genome(out);
    }

    /** Result of a breeding: the child's genome and, if a mutation happened, which gene it hit. */
    public record Offspring(Genome genome, Gene mutatedGene) {
        public boolean mutated() {
            return mutatedGene != null;
        }
    }

    /**
     * Per-offspring mutation chance for these parents. Fertility is the trade-off from DESIGN.md:
     * fertile parents make more litters but LESS CONTROLLED evolution, so chance grows with the square of the
     * parents' average fertility (0.6 -> 1.4%, 1.0 -> 4%, 2.0 -> 16%).
     */
    public static double mutationChance(Genome a, Genome b) {
        double f = (a.traits().fertility() + b.traits().fertility()) / 2.0;
        return Math.min(MAX_MUTATION_CHANCE, BASE_MUTATION_CHANCE * f * f);
    }

    public static Offspring breed(Genome mother, Genome father, RandomGenerator rng) {
        return breed(mother, father, rng, mutationChance(mother, father));
    }

    /**
     * Mendelian inheritance: for every gene the child gets one random allele of the mother (slot 0) and one of the
     * father (slot 1), independently per gene. Then, with probability {@code mutationChance}, one random allele
     * slot of one random gene is replaced by a brand-new (rare) allele. Pass 1.0 to force a mutation (Mutagen).
     */
    public static Offspring breed(Genome mother, Genome father, RandomGenerator rng, double mutationChance) {
        long out = 0;
        for (Gene g : Gene.values()) {
            int fromMother = mother.alleleId(g, rng.nextBoolean() ? 0 : 1);
            int fromFather = father.alleleId(g, rng.nextBoolean() ? 0 : 1);
            out |= (long) fromMother << shift(g, 0);
            out |= (long) fromFather << shift(g, 1);
        }
        Genome child = new Genome(out);
        Gene mutated = null;
        if (rng.nextDouble() < mutationChance) {
            mutated = Gene.values()[rng.nextInt(Gene.COUNT)];
            int slot = rng.nextInt(2);
            child = child.with(mutated, slot, mutated.randomMutationAlleleId(child.alleleId(mutated, slot), rng));
        }
        return new Offspring(child, mutated);
    }
}
