package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The text the Calipers (Phase 5) print: one list of lines for a bunnidog and one for a seed. Pure Java, no Minecraft,
 * so the wording lives next to the genetics it describes. The first line of each list is the headline (the item colours
 * it differently); the rest are details.
 */
public final class Readout {
    private Readout() {}

    /**
     * Genome, every gene as the genome describes it, the bred stats, where the next generation will drift, and (Phase 5b)
     * the genomes of the parents if the bunnidog was born from a seed; wild and summoned ones have none recorded.
     */
    public static List<String> animal(Genome genome, Stats stats, boolean baby, Optional<Genome> mother, Optional<Genome> father) {
        return animal(genome, stats, baby, mother, father, Optional.empty());
    }

    /**
     * Same, and (Phase 5d) right under the headline the animal's Ear Tag code if it wears one. Phase 8 S2: plain words first
     * (the look phrase, one row per gene, the stats in real units, the parents' looks); the raw genome code is the LAST line.
     */
    public static List<String> animal(Genome genome, Stats stats, boolean baby, Optional<Genome> mother, Optional<Genome> father,
            Optional<String> earTag) {
        List<String> lines = new ArrayList<>();
        lines.add("Bunnidog (" + (baby ? "pup" : "adult") + "): " + Plain.look(genome));
        if (earTag.isPresent()) {
            lines.add("  Ear tag: " + earTag.get());
        }
        for (Gene gene : Gene.values()) {
            lines.add("  " + Plain.row(genome, gene));
        }
        lines.add("  " + statsText(genome, stats));
        lines.add("  " + Combat.describe(genome,stats));
        lines.add("  Next generation (identical partner, quality 0): " + pushText(genome));
        double bonus = Drift.qualityBonus(genome, stats);
        if (bonus > 0.0D) {
            lines.add(String.format(Locale.ROOT, "  Starborn: adds %.2f seed quality (half of it if only one parent has it)", bonus));
        }
        lines.add("  " + Drops.describe(genome, stats, baby));
        if (mother.isPresent() && father.isPresent()) {
            lines.add("  Born of: mother " + Plain.look(mother.get()));
            lines.add("           father " + Plain.look(father.get()));
        } else {
            lines.add("  No recorded parents (wild, or summoned)");
        }
        lines.add("  Code: " + genome);
        return lines;
    }

    /** What the seed will do (quality, litter, mutation, drift) and both parents gene by gene. */
    public static List<String> seed(Seed seed) {
        List<String> lines = new ArrayList<>();
        lines.add("Seed: " + seed.summary().get(0));
        lines.addAll(seed.summary().subList(1, seed.summary().size()));
        parent(lines, "Mother", seed.mother(), seed.motherStats());
        parent(lines, "Father", seed.father(), seed.fatherStats());
        return lines;
    }

    /** What a Tissue Sample holds (drops D7a): the animal in words, gene by gene, the bred stats and where the line would drift; the code last. No parents: a sample is the animal itself. */
    public static List<String> tissue(Tissue tissue) {
        List<String> lines = new ArrayList<>();
        lines.add("Tissue sample: " + Plain.look(tissue.genome()));
        for (Gene gene : Gene.values()) {
            lines.add("  " + Plain.row(tissue.genome(), gene));
        }
        lines.add("  " + statsText(tissue.genome(), tissue.stats()));
        lines.add("  " + Combat.describe(tissue.genome(),tissue.stats()));
        lines.add("  Next generation (identical partner, quality 0): " + pushText(tissue.genome()));
        lines.add("  Code: " + tissue.genome());
        return lines;
    }

    private static void parent(List<String> lines, String role, Genome genome, Stats stats) {
        lines.add(role + ": " + Plain.look(genome));
        for (Gene gene : Gene.values()) {
            lines.add("  " + Plain.row(genome, gene));
        }
        lines.add("  " + statsText(genome, stats));
        lines.add("  " + Combat.describe(genome,stats));
        lines.add("  Code: " + genome);
    }

    /**
     * {@code Stats: Vitality +3 (4.5 hearts), Speed 0 (10.8 blocks/s), Leap 0 (1.25 blocks high), Senses +2 (hears 17 blocks),
     * Bloom +12 (grows up in 12.5 min)}: the points, which are what breeding changes, and what they mean.
     */
    static String statsText(Genome genome, Stats stats) {
        StringBuilder sb = new StringBuilder("Stats: ");
        for (Units.Reading r : Units.readings(genome, stats)) {
            if (r.stat().ordinal() > 0) {
                sb.append(", ");
            }
            sb.append(Plain.statName(r.stat())).append(' ').append(r.points() == 0 ? "0" : String.format(Locale.ROOT, "%+d", r.points()))
                    .append(" (").append(r.valueText()).append(')');
        }
        return sb.toString();
    }

    /** "Vitality +1.0  Swiftness -0.5" or "no drift". */
    public static String pushText(Genome genome) {
        double[] trend = Drift.trend(genome, 0.0D);
        StringBuilder push = new StringBuilder();
        for (Stat stat : Stat.values()) {
            double t = trend[stat.ordinal()];
            if (Math.abs(t) > 1.0e-9) {
                push.append(String.format(Locale.ROOT, "%s %s%.1f  ", Plain.statName(stat), t > 0 ? "+" : "", t));
            }
        }
        return push.length() == 0 ? "no drift" : push.toString().trim();
    }
}
