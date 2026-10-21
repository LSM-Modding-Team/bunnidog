package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * The payload of a bunnidog seed (docs/DESIGN.md "Seed & Guava loop", Phase 4c): both parents' genomes and bred
 * stats, plus the seed quality. Immutable, pure Java (no Minecraft), covered by tools/GenomeCheck.java.
 *
 * <p>The dice are NOT rolled when the seed is made. The seed only remembers its two parents; {@link #hatch} rolls the
 * litter later (planted crop in step 4d, {@code /bunnidogs hatch} until then). That is what lets a seed be inspected
 * ("these parents, these odds"), carried to another biome, and planted anywhere.
 *
 * <p>Quality is {@code the guava part (GuavaTier.average of the two tiers' qualities) + the parents' average starborn bonus}, fixed when the
 * seed drops. A Fortune bonus is added at harvest ({@code extraQuality} of {@link #hatch}, see {@link #fortuneBonus}).
 *
 * <p>Text form (this is what the item stores, and what {@code /give} accepts):
 * {@code motherGenome/motherStats|fatherGenome/fatherStats|quality}, for example
 * {@code 00-00-00-11-00-00/0,0,0,0,0|00-00-00-11-00-00/2,-1,0,0,0|1.5}. The stats part and the quality may be left
 * out (zeros), so {@code 00-00-00-00-00-00|22-22-22-22-22-22} is a valid seed too.
 *
 * <p><b>Mutagen (drops D6a):</b> a seed treated with a Mutagen has a 4th part, the word {@code mutagen}:
 * {@code mother/stats|father/stats|quality|mutagen} ({@code a|b||mutagen} is valid too: blank quality = 0). A seed WITHOUT the part
 * prints exactly as it always did, so old seeds, item stacks and planted crops are unchanged. The treatment raises the mutation
 * chance of every pup of the seed to at least {@link #MUTAGEN_MUTATION_FLOOR}, and a seed carries at most one.
 */
public record Seed(Genome mother, Stats motherStats, Genome father, Stats fatherStats, double quality, boolean mutagen) {
    /** Quality is clamped to this range only so that maths on it can never overflow (like {@link Stats#LIMIT}). */
    public static final double QUALITY_LIMIT = 1_000_000.0;

    /** The word of the optional 4th text part of a seed treated with a Mutagen. */
    public static final String MUTAGEN_WORD = "mutagen";

    /** A treated seed's pups mutate with at least this chance each: the existing cap, so the Mutagen adds no new number (docs/DROPS_SPECS.md 14). */
    public static final double MUTAGEN_MUTATION_FLOOR = Genome.MAX_MUTATION_CHANCE;

    public Seed {
        Objects.requireNonNull(mother, "mother");
        Objects.requireNonNull(motherStats, "motherStats");
        Objects.requireNonNull(father, "father");
        Objects.requireNonNull(fatherStats, "fatherStats");
        quality = Double.isNaN(quality) ? 0.0 : Math.max(0.0, Math.min(QUALITY_LIMIT, quality));
    }

    /** An untreated seed (the form every seed had before the Mutagen existed). */
    public Seed(Genome mother, Stats motherStats, Genome father, Stats fatherStats, double quality) {
        this(mother, motherStats, father, fatherStats, quality, false);
    }

    /** The same seed treated with a Mutagen (the same seed again if it already was: a seed carries one). */
    public Seed withMutagen() {
        return mutagen ? this : new Seed(mother, motherStats, father, fatherStats, quality, true);
    }

    /**
     * The seed a mating of these two parents drops. {@code guavaQuality} is the guava part of the quality:
     * {@link GuavaTier#average} of the two tiers the parents were fed (0 for plain guavas or none). The starborn bonus of the parents is added here, once, and stays in the seed.
     */
    public static Seed of(Genome mother, Stats motherStats, Genome father, Stats fatherStats, double guavaQuality) {
        double bonus = Drift.pairQualityBonus(mother, motherStats, father, fatherStats);
        return new Seed(mother, motherStats, father, fatherStats, Math.max(0.0, guavaQuality) + bonus);
    }

    // ------------------------------------------------------------------ hatching

    /** One pup of a litter: its rolled genome, its stat points and, if a mutation hit it, which gene. */
    public record Pup(Genome genome, Stats stats, Gene mutatedGene) {
        public boolean mutated() {
            return mutatedGene != null;
        }
    }

    /**
     * Rolls the litter of this seed: {@link Breeding#litterSize} pups (2 x the parents' average fertility, 1..6,
     * shrunk to the room left under the crowding limit, so an empty list means "too crowded, nothing is born"), each
     * with its own Mendelian genome and mutation roll, and stats = the parents' average + the pair's drift at this
     * seed's quality, rounded per pup. Same rules as the Phase 2 litter; only the moment moved.
     *
     * @param extraQuality added to the seed's quality for this hatching only (the Fortune bonus of step 4d; 0 = none)
     * @param nearby       how many bunnidogs are within {@link Breeding#CROWD_RADIUS} of the place it hatches
     */
    public List<Pup> hatch(double extraQuality, int nearby, RandomGenerator rng) {
        int litter = Breeding.litterSize(mother, father, nearby, rng);
        List<Pup> out = new ArrayList<>(Math.max(litter, 0));
        if (litter <= 0) {
            return out;
        }
        double[] drift = Drift.drift(mother, father, effectiveQuality(extraQuality));
        for (int i = 0; i < litter; i++) {
            Genome.Offspring child = Genome.breed(mother, father, rng, mutationChance());
            out.add(new Pup(child.genome(), Drift.inherit(motherStats, fatherStats, drift, rng), child.mutatedGene()));
        }
        return out;
    }

    /** Quality a Fortune enchantment on the harvesting tool adds to ONE hatching, per level (design: 0.5 per level). */
    public static final double FORTUNE_QUALITY_PER_LEVEL = 0.5;

    /** The {@code extraQuality} for a harvest with a tool of this Fortune level; a level of 0 or less adds nothing. */
    public static double fortuneBonus(int fortuneLevel) {
        return Math.max(0, fortuneLevel) * FORTUNE_QUALITY_PER_LEVEL;
    }

    /** The quality a hatching uses: the seed's own plus the bonus of the moment (never below the seed's own). */
    public double effectiveQuality(double extraQuality) {
        return quality + Math.max(0.0, extraQuality);
    }

    // ------------------------------------------------------------------ reading it (tooltip, command)

    /** Mean litter size of this seed, before crowding. */
    public double expectedLitter() {
        return Breeding.expectedLitter(mother, father);
    }

    /** Chance that a pup of this seed mutates: the parents' own chance, or at least {@link #MUTAGEN_MUTATION_FLOOR} when treated with a Mutagen. */
    public double mutationChance() {
        double natural = Genome.mutationChance(mother, father);
        return mutagen ? Math.max(natural, MUTAGEN_MUTATION_FLOOR) : natural;
    }

    /** What the pups' stats will move by, on average, at this seed's quality plus {@code extraQuality}. */
    public double[] drift(double extraQuality) {
        return Drift.drift(mother, father, effectiveQuality(extraQuality));
    }

    /** "Vitality +1.0, Speed -1.0", or "none" when nothing moves. */
    public static String driftText(double[] drift) {
        StringBuilder sb = new StringBuilder();
        for (Stat stat : Stat.values()) {
            double d = drift[stat.ordinal()];
            if (Math.abs(d) > 1.0e-9) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(String.format(Locale.ROOT, "%s %+.1f", Plain.statName(stat), d));
            }
        }
        return sb.length() == 0 ? Texts.t("none") : sb.toString();
    }

    /** The short readable summary the item tooltip shows: quality, litter, mutation chance and where the stats will drift. */
    public List<String> summary() {
        List<String> lines = new ArrayList<>();
        lines.add(String.format(Locale.ROOT, Texts.t("Quality %.2f"), quality));
        lines.add(String.format(Locale.ROOT, Texts.t("Litter of about %.1f pups"), expectedLitter()));
        lines.add(String.format(Locale.ROOT, Texts.t("Mutation chance %.1f%% per pup"), mutationChance() * 100.0));
        if (mutagen) {
            lines.add(Texts.t("Treated with Mutagen"));
        }
        lines.add(Texts.t("Stats drift: ") + driftText(drift(0.0)));
        return lines;
    }

    // ------------------------------------------------------------------ text form

    /** Canonical text form, see the class comment. Round-trips exactly through {@link #parse}. */
    @Override
    public String toString() {
        return mother + "/" + motherStats + "|" + father + "/" + fatherStats + "|" + Double.toString(quality) + (mutagen ? "|" + MUTAGEN_WORD : "");
    }

    /**
     * Parses the text form. The stats of a parent and the quality are optional (zero), and a 4th part must be exactly the word
     * {@code mutagen}. Empty if a genome or the stats are malformed, the quality is not a finite number, the 4th part is anything
     * else, or there are not two to four {@code |}-separated parts.
     */
    public static Optional<Seed> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String[] parts = text.trim().split("\\|", -1);
        if (parts.length < 2 || parts.length > 4 || (parts.length == 4 && !parts[3].trim().equals(MUTAGEN_WORD))) {
            return Optional.empty();
        }
        Optional<Parent> mother = parseParent(parts[0]);
        Optional<Parent> father = parseParent(parts[1]);
        if (mother.isEmpty() || father.isEmpty()) {
            return Optional.empty();
        }
        double quality = 0.0;
        if (parts.length >= 3 && !parts[2].isBlank()) {
            try {
                quality = Double.parseDouble(parts[2].trim());
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
            if (!Double.isFinite(quality)) {
                return Optional.empty();
            }
        }
        return Optional.of(new Seed(mother.get().genome(), mother.get().stats(),
                father.get().genome(), father.get().stats(), quality, parts.length == 4));
    }

    private record Parent(Genome genome, Stats stats) {}

    private static Optional<Parent> parseParent(String text) {
        int slash = text.indexOf('/');
        Optional<Genome> genome = Genome.parse(slash < 0 ? text : text.substring(0, slash));
        if (genome.isEmpty()) {
            return Optional.empty();
        }
        Stats stats = Stats.ZERO;
        if (slash >= 0 && !text.substring(slash + 1).isBlank()) {
            Optional<Stats> parsed = Stats.parse(text.substring(slash + 1));
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            stats = parsed.get();
        }
        return Optional.of(new Parent(genome.get(), stats));
    }
}
