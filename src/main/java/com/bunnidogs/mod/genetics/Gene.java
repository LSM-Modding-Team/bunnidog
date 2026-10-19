package com.bunnidogs.mod.genetics;

import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;

/**
 * The seven gene slots and their allele tables. New slots are appended, never inserted.
 *
 * <p>THE TABLES ARE SAVE-FORMAT: an allele's position in its table is its id, stored in every saved genome.
 * Append new alleles at the end (max 16 per gene); never reorder or delete existing ones.
 * Dominance ranks must be unique within a gene (checked by tools/GenomeCheck.java).
 * Wild alleles ({@link Allele#isWild()}) belong to one {@link Lineage}; who carries them is decided in {@link Founders}.
 *
 * <p>Pure Java on purpose: no Minecraft imports, so the whole package can be compiled and tested outside the game.
 */
public enum Gene {
    // Ids are positions in each table and are SAVED: only ever append. Dominance is not saved (renumber freely):
    // common alleles 1..3, wild-lineage alleles 10+, rare mutation alleles 50 (a mutation always shows), and a
    // deliberately recessive wild allele gets 0 (only shows when homozygous).
    EARS("ears",
            spec("standard", 3, 1.00f, 6),
            spec("long",     2, 1.35f, 3),
            spec("short",    1, 0.65f, 3),
            spec("huge",    50, 1.75f, 0),               // rare, dominant: shows up at once
            wild("tiny",    10, 0.50f, Lineage.TUNDRA),  // small ears keep the heat in
            wild("fennec",  11, 1.55f, Lineage.DESERT),  // big radiator ears (and big hearing range)
            wild("bat",     12, 1.45f, Lineage.CAVE)),   // big listening ears (and big hearing range)
    COAT("coat",                                          // value unused; the allele NAME picks the texture
            spec("tan",      3, 0f, 6),
            spec("cream",    2, 0f, 3),
            spec("brown",    1, 0f, 3),
            spec("spotted", 50, 0f, 0),
            wild("snow",    10, 0f, Lineage.TUNDRA),
            wild("sand",    11, 0f, Lineage.DESERT),
            wild("moss",    12, 0f, Lineage.SWAMP),
            wild("pale",    13, 0f, Lineage.CAVE),
            wild("slate",   14, 0f, Lineage.MOUNTAIN),
            wild("ember",   15, 0f, Lineage.NETHER),
            wild("void",    16, 0f, Lineage.END),
            wild("glow",     0, 0f, Lineage.SWAMP)),     // RECESSIVE: swamp bunnidogs mostly just carry it
    LEGS("legs",                                          // value = leg scale (speed + jump + visible length)
            spec("standard", 3, 1.00f, 6),
            spec("long",     2, 1.20f, 3),
            spec("short",    1, 0.80f, 3),
            spec("springy", 50, 1.50f, 0),
            wild("fleet",   10, 1.35f, Lineage.DESERT),  // fast
            wild("hoofed",  11, 1.30f, Lineage.MOUNTAIN), // goat legs: long jumps
            wild("phase",   12, 1.00f, Lineage.END)),     // short defensive blinks, not longer physical legs
    BUILD("build",                                        // value = chonk (health + visible width)
            spec("average",  3, 1.00f, 6),
            spec("stocky",   2, 1.25f, 3),
            spec("slim",     1, 0.85f, 3),
            spec("chonker", 50, 1.60f, 0),
            wild("woolly",  10, 1.35f, Lineage.TUNDRA),  // thick and tough
            wild("lean",    11, 0.70f, Lineage.DESERT)), // light and fragile
    TEMPERAMENT("temperament",                            // value = Temperament code
            spec("loyal",    3, 0f, 5),
            spec("skittish", 2, 1f, 3),
            spec("feral",    1, 2f, 2),
            spec("stoic",   50, 3f, 0)),
    FERTILITY("fertility",                                // value = breeding multiplier (litter size, cooldown, mutation)
            spec("average",  3, 1.00f, 6),
            spec("prolific", 2, 1.50f, 2),
            spec("shy",      1, 0.60f, 3),
            spec("swarm",   50, 2.00f, 0),
            wild("starborn", 10, 0.70f, Lineage.END)),  // End only: fewer, better pups; converts Bloom into seed quality (Drift)
    DIET("diet",                                           // value unused; Diet resolves the stable allele name
            spec("plain",           1, 0f, 8),
            spec("grazer",          3, 0f, 3),
            spec("forager",         2, 0f, 2),
            spec("hunter",          4, 0f, 1),
            spec("omnivore",       50, 0f, 0),
            wild("fisher",         10, 0f, Lineage.TUNDRA),
            wild("desert_browser", 11, 0f, Lineage.DESERT),
            wild("mushroom_eater", 12, 0f, Lineage.SWAMP),
            wild("cave_forager",   13, 0f, Lineage.CAVE),
            wild("mountain_grazer",14, 0f, Lineage.MOUNTAIN),
            wild("nether_forager", 15, 0f, Lineage.NETHER),
            wild("chorus_eater",   16, 0f, Lineage.END));

    /** Number of gene slots. */
    public static final int COUNT = values().length;
    /** Ids are stored in 4 bits. */
    public static final int MAX_ALLELES = 16;

    private record Spec(String name, int dominance, float value, int weight, Lineage lineage) {}

    /** A common (weight > 0) or, with weight 0, a rare mutation-only allele. */
    private static Spec spec(String name, int dominance, float value, int weight) {
        return new Spec(name, dominance, value, weight, null);
    }

    /** An allele that only the founders of {@code lineage} carry (weight 0, never a mutation target). */
    private static Spec wild(String name, int dominance, float value, Lineage lineage) {
        return new Spec(name, dominance, value, 0, lineage);
    }

    private final String key;
    private final List<Allele> alleles;
    private final int commonWeightTotal;

    Gene(String key, Spec... specs) {
        if (specs.length > MAX_ALLELES) {
            throw new IllegalStateException(key + " has more than " + MAX_ALLELES + " alleles");
        }
        this.key = key;
        Allele[] table = new Allele[specs.length];
        int total = 0;
        for (int i = 0; i < specs.length; i++) {
            Spec s = specs[i];
            table[i] = new Allele(i, s.name(), s.dominance(), s.value(), s.weight(), s.lineage());
            total += s.weight();
        }
        this.alleles = List.of(table);
        this.commonWeightTotal = total;
    }

    public String key() {
        return key;
    }

    /** "Ears", "Coat", ... for messages. */
    public String displayName() {
        return key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);
    }

    public List<Allele> alleles() {
        return alleles;
    }

    public int alleleCount() {
        return alleles.size();
    }

    public Allele allele(int id) {
        return alleles.get(id);
    }

    /** The allele with this name; throws if there is none (recipes in {@link Founders} are checked by the self-check). */
    public Allele alleleNamed(String name) {
        for (Allele a : alleles) {
            if (a.name().equals(name)) {
                return a;
            }
        }
        throw new IllegalArgumentException("no " + key + " allele named '" + name + "'");
    }

    /** Picks an allele id for a random founder: weighted by {@link Allele#weight()}, rare alleles never. */
    public int randomCommonAlleleId(RandomGenerator rng) {
        int roll = rng.nextInt(commonWeightTotal);
        for (Allele a : alleles) {
            roll -= a.weight();
            if (roll < 0) {
                return a.id();
            }
        }
        throw new IllegalStateException("unreachable: weights inconsistent for " + key);
    }

    /**
     * Picks the allele a mutation turns {@code currentId} into: a RARE allele other than the current one if there
     * is any ("brand-new allele"), otherwise any different NON-wild allele. Wild-lineage alleles are never a
     * mutation target, they exist only in their own lineage.
     */
    public int randomMutationAlleleId(int currentId, RandomGenerator rng) {
        int[] pool = new int[alleles.size()];
        int n = 0;
        for (Allele a : alleles) {
            if (a.isRare() && a.id() != currentId) {
                pool[n++] = a.id();
            }
        }
        if (n == 0) {
            for (Allele a : alleles) {
                if (a.id() != currentId && !a.isWild()) {
                    pool[n++] = a.id();
                }
            }
        }
        return pool[rng.nextInt(n)];
    }
}
