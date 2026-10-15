package com.bunnidogs.mod.genetics;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Founder recipes: what a wild-caught bunnidog of each {@link Lineage} is made of.
 *
 * <p>A founder starts from the ordinary common-allele pool (exactly like {@link Genome#randomFounder(RandomGenerator)}).
 * A lineage then bends that pool with a few <b>signatures</b>: for every one of the two allele slots of a signed
 * gene, with the signature's {@code chance} the slot gets the signature allele instead. A gene can carry several
 * signatures (their chances add up, at most 1; the rest goes to the common pool).
 *
 * <p>Signature alleles are either wild alleles of that lineage (exist nowhere else) or existing common alleles
 * that the lineage simply favours (nether founders are mostly feral). Mutation-only rare alleles never appear in a
 * founder. Wild alleles are dominant, so a wild founder shows its lineage at a glance; the deliberate exception is
 * the swamp's recessive {@code glow} coat, which most swamp bunnidogs only carry.
 *
 * <p>Pure Java; the recipes are validated by tools/GenomeCheck.java.
 */
public final class Founders {
    private Founders() {}

    /** One line of a recipe: in {@code gene}, each allele slot becomes {@code allele} with probability {@code chance}. */
    public record Signature(Gene gene, String allele, double chance) {
        public Allele resolve() {
            return gene.alleleNamed(allele);
        }
    }

    private static Signature sig(Gene gene, String allele, double chance) {
        return new Signature(gene, allele, chance);
    }

    private static final Map<Lineage, List<Signature>> RECIPES = new EnumMap<>(Lineage.class);

    static {
        RECIPES.put(Lineage.MEADOW, List.of());                       // the plain common pool
        RECIPES.put(Lineage.TUNDRA, List.of(
                sig(Gene.COAT, "snow", 0.90),
                sig(Gene.EARS, "tiny", 0.80),
                sig(Gene.BUILD, "woolly", 0.70)));
        RECIPES.put(Lineage.DESERT, List.of(
                sig(Gene.COAT, "sand", 0.90),
                sig(Gene.EARS, "fennec", 0.80),
                sig(Gene.LEGS, "fleet", 0.80),
                sig(Gene.BUILD, "lean", 0.70)));
        RECIPES.put(Lineage.SWAMP, List.of(
                sig(Gene.COAT, "moss", 0.60),
                sig(Gene.COAT, "glow", 0.25),                         // recessive: mostly carried, rarely shown
                sig(Gene.FERTILITY, "prolific", 0.50)));
        RECIPES.put(Lineage.CAVE, List.of(
                sig(Gene.COAT, "pale", 0.90),
                sig(Gene.EARS, "bat", 0.80),
                sig(Gene.TEMPERAMENT, "skittish", 0.50)));
        RECIPES.put(Lineage.MOUNTAIN, List.of(
                sig(Gene.COAT, "slate", 0.90),
                sig(Gene.LEGS, "hoofed", 0.85),
                sig(Gene.BUILD, "stocky", 0.50)));
        RECIPES.put(Lineage.NETHER, List.of(
                sig(Gene.COAT, "ember", 0.90),
                sig(Gene.TEMPERAMENT, "feral", 0.85)));
        RECIPES.put(Lineage.END, List.of(
                sig(Gene.COAT, "void", 0.90),
                sig(Gene.TEMPERAMENT, "skittish", 0.40),
                sig(Gene.FERTILITY, "starborn", 0.40)));       // the seed-quality gene: nowhere else (see Drift)
    }

    /** The recipe of a lineage (empty for MEADOW). */
    public static List<Signature> recipe(Lineage lineage) {
        return RECIPES.get(lineage);
    }

    /**
     * A wild-caught founder of this lineage. For MEADOW this consumes the random stream exactly like
     * {@link Genome#randomFounder(RandomGenerator)} (same seed, same genome), which the self-check verifies.
     */
    public static Genome roll(Lineage lineage, RandomGenerator rng) {
        List<Signature> recipe = RECIPES.get(lineage);
        Genome genome = Genome.DEFAULT;
        for (Gene gene : Gene.values()) {
            for (int slot = 0; slot < 2; slot++) {
                int id = signedAllele(recipe, gene, rng);
                if (id < 0) {
                    id = gene.randomCommonAlleleId(rng);
                }
                genome = genome.with(gene, slot, id);
            }
        }
        return genome;
    }

    /** The allele id a signature picks for one slot, or -1 for "none, use the common pool". No random draw if unsigned. */
    private static int signedAllele(List<Signature> recipe, Gene gene, RandomGenerator rng) {
        double roll = -1;
        double edge = 0;
        for (Signature s : recipe) {
            if (s.gene() != gene) {
                continue;
            }
            if (roll < 0) {
                roll = rng.nextDouble();
            }
            edge += s.chance();
            if (roll < edge) {
                return s.resolve().id();
            }
        }
        return -1;
    }
}
