package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * The "liquid pup" inside a bottle of infused guava juice (docs/DESIGN.md "3. Guava juice and infusion", Phase 4f): one
 * pup rolled from a {@link Seed} at the moment of infusion and then frozen: its genome, its bred stat points and the
 * quality it was rolled at. The seed is used up, so the price of a juice is a whole litter. What the juice does when drunk
 * is read off these three ({@link Juice}); nothing is rolled again at drinking time, so the tooltip never lies.
 *
 * <p>Text form (what the item stores): {@code genome/stats|quality}, for example
 * {@code 00-00-00-11-00-00/3,-1,0,0,0|4.5}. The stats and the quality may be left out (zeros), like in a {@link Seed}.
 * Pure Java, covered by tools/GenomeCheck.java.
 */
public record Infusion(Genome genome, Stats stats, double quality) {
    public Infusion {
        Objects.requireNonNull(genome, "genome");
        Objects.requireNonNull(stats, "stats");
        quality = Double.isNaN(quality) ? 0.0 : Math.max(0.0, Math.min(Seed.QUALITY_LIMIT, quality));
    }

    /**
     * Rolls the phantom pup of a seed: one Mendelian genome with its own mutation roll and the parents' average stats plus
     * the pair's drift at the seed's quality, exactly the rules of ONE pup of {@link Seed#hatch} (without the litter size and
     * the crowding limit, which a bottle does not have). Fortune does not apply: nobody harvests a bottle.
     */
    public static Infusion roll(Seed seed, RandomGenerator rng) {
        double[] drift = Drift.drift(seed.mother(), seed.father(), seed.quality());
        Genome.Offspring child = Genome.breed(seed.mother(), seed.father(), rng, seed.mutationChance());
        Stats stats = Drift.inherit(seed.motherStats(), seed.fatherStats(), drift, rng);
        return new Infusion(child.genome(), stats, seed.quality());
    }

    /** A richer carrier lifts potion quality, without changing the seed's inherited genome or stat drift. */
    public static Infusion roll(Seed seed, GuavaTier carrier, RandomGenerator rng) {
        Infusion child = roll(seed,rng);
        return new Infusion(child.genome(),child.stats(),Math.max(child.quality(),carrier.quality()));
    }

    /** The effects of this bottle: the saturation first, then the gene effects ({@link Juice#effects}). */
    public List<Juice.Spec> specs() {
        List<Juice.Spec> out = new ArrayList<>();
        out.add(Juice.infusedSaturation());
        out.addAll(Juice.effects(genome, stats, quality));
        return out;
    }

    /** How far the NEARBY effects reach, in blocks. */
    public double areaRadius() {
        return Juice.areaRadius(genome);
    }

    /** The short readable lines of the tooltip: one per gene effect (or "no effects") and the quality. */
    public List<String> summary() {
        List<String> lines = new ArrayList<>();
        List<Juice.Spec> effects = Juice.effects(genome, stats, quality);
        if (effects.isEmpty()) {
            lines.add(Texts.t("A bland juice: no effects"));
        }
        for (Juice.Spec s : effects) {
            lines.add(s.describe());
        }
        lines.add(String.format(Locale.ROOT, Texts.t("Infusion quality %.2f"), quality));
        return lines;
    }

    /** Canonical text form. Round-trips exactly through {@link #parse}. */
    @Override
    public String toString() {
        return genome + "/" + stats + "|" + Double.toString(quality);
    }

    /** Parses the text form; empty if the genome or the stats are malformed or the quality is not a finite number. */
    public static Optional<Infusion> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String[] parts = text.trim().split("\\|", -1);
        if (parts.length < 1 || parts.length > 2) {
            return Optional.empty();
        }
        String head = parts[0];
        int slash = head.indexOf('/');
        Optional<Genome> genome = Genome.parse(slash < 0 ? head : head.substring(0, slash));
        if (genome.isEmpty()) {
            return Optional.empty();
        }
        Stats stats = Stats.ZERO;
        if (slash >= 0 && !head.substring(slash + 1).isBlank()) {
            Optional<Stats> parsed = Stats.parse(head.substring(slash + 1));
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            stats = parsed.get();
        }
        double quality = 0.0;
        if (parts.length == 2 && !parts[1].isBlank()) {
            try {
                quality = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
            if (!Double.isFinite(quality)) {
                return Optional.empty();
            }
        }
        return Optional.of(new Infusion(genome.get(), stats, quality));
    }
}
