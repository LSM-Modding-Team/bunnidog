package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Which way an allele pushes a bloodline's stats, generation after generation (docs/DESIGN.md "4. Bred stats").
 * An allele's trend is one <b>gain</b> and one <b>cost</b>, e.g. stocky: +Vitality, -Swiftness. Sizes by kind of
 * allele: ordinary +1/-1 (zero-sum), wild-lineage +2/-1, rare mutation +3/-1. The default alleles (standard,
 * average, loyal) have no trend, so an unbred line stays put. Coat and Diet have no trend at all.
 *
 * <p>{@code starborn} (the End's Fertility allele) is the one special case: +2 Bloom, and instead of a cost two
 * <b>tolls</b> (-1 Vitality, -1 Senses) that quality does not shrink (see {@link Drift}).
 *
 * <p>Only the EXPRESSED allele of a gene counts, like every other trait. Pure Java, checked by tools/GenomeCheck.java.
 */
public final class Trends {
    private Trends() {}

    /** How quality treats a term: gains grow with it, costs shrink with it, tolls ignore it. */
    public enum Kind { GAIN, COST, TOLL }

    /** {@code amount} is per generation at quality 0 and always positive; the kind gives the sign. */
    public record Term(Stat stat, double amount, Kind kind) {}

    private record Entry(Stat gain, Stat cost, List<Stat> tolls) {}

    private static final Map<String, Entry> TABLE = new HashMap<>();

    private static String key(Gene gene, String allele) {
        return gene.key() + "/" + allele;
    }

    private static void put(Gene gene, String allele, Stat gain, Stat cost) {
        TABLE.put(key(gene, allele), new Entry(gain, cost, List.of()));
    }

    private static void putWithTolls(Gene gene, String allele, Stat gain, Stat... tolls) {
        TABLE.put(key(gene, allele), new Entry(gain, null, List.of(tolls)));
    }

    static {
        // Build
        put(Gene.BUILD, "stocky", Stat.VITALITY, Stat.SWIFTNESS);
        put(Gene.BUILD, "slim", Stat.SWIFTNESS, Stat.VITALITY);
        put(Gene.BUILD, "chonker", Stat.VITALITY, Stat.SWIFTNESS);
        put(Gene.BUILD, "woolly", Stat.VITALITY, Stat.LEAP);
        put(Gene.BUILD, "lean", Stat.SWIFTNESS, Stat.VITALITY);
        // Legs
        put(Gene.LEGS, "long", Stat.SWIFTNESS, Stat.SENSES);
        put(Gene.LEGS, "short", Stat.SENSES, Stat.SWIFTNESS);
        put(Gene.LEGS, "springy", Stat.LEAP, Stat.SENSES);
        put(Gene.LEGS, "fleet", Stat.SWIFTNESS, Stat.VITALITY);
        put(Gene.LEGS, "hoofed", Stat.LEAP, Stat.SWIFTNESS);
        put(Gene.LEGS, "phase", Stat.LEAP, Stat.VITALITY);
        // Ears
        put(Gene.EARS, "long", Stat.SENSES, Stat.VITALITY);
        put(Gene.EARS, "short", Stat.VITALITY, Stat.SENSES);
        put(Gene.EARS, "huge", Stat.SENSES, Stat.VITALITY);
        put(Gene.EARS, "tiny", Stat.VITALITY, Stat.SENSES);
        put(Gene.EARS, "fennec", Stat.SENSES, Stat.VITALITY);
        put(Gene.EARS, "bat", Stat.SENSES, Stat.LEAP);
        // Temperament
        put(Gene.TEMPERAMENT, "skittish", Stat.SWIFTNESS, Stat.VITALITY);
        put(Gene.TEMPERAMENT, "feral", Stat.VITALITY, Stat.SENSES);
        put(Gene.TEMPERAMENT, "stoic", Stat.VITALITY, Stat.SWIFTNESS);
        // Fertility (Bloom)
        put(Gene.FERTILITY, "prolific", Stat.BLOOM, Stat.SENSES);
        put(Gene.FERTILITY, "shy", Stat.SENSES, Stat.BLOOM);
        put(Gene.FERTILITY, "swarm", Stat.BLOOM, Stat.VITALITY);
        putWithTolls(Gene.FERTILITY, "starborn", Stat.BLOOM, Stat.VITALITY, Stat.SENSES);
    }

    /** True if this allele has a trend entry (the default alleles and all of Coat do not). */
    public static boolean hasTrend(Gene gene, Allele allele) {
        return TABLE.containsKey(key(gene, allele.name()));
    }

    /** The allele's terms at quality 0, gain first; empty for an allele without a trend. */
    public static List<Term> terms(Gene gene, Allele allele) {
        Entry e = TABLE.get(key(gene, allele.name()));
        if (e == null) {
            return List.of();
        }
        List<Term> out = new ArrayList<>(3);
        out.add(new Term(e.gain(), gainAmount(allele), Kind.GAIN));
        if (e.cost() != null) {
            out.add(new Term(e.cost(), 1.0, Kind.COST));
        }
        for (Stat toll : e.tolls()) {
            out.add(new Term(toll, 1.0, Kind.TOLL));
        }
        return out;
    }

    /** Rare mutation alleles gain 3, wild-lineage alleles 2, ordinary ones 1. */
    static double gainAmount(Allele allele) {
        if (allele.isRare()) {
            return 3.0;
        }
        return allele.isWild() ? 2.0 : 1.0;
    }
}
