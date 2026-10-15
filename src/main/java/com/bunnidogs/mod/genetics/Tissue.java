package com.bunnidogs.mod.genetics;

import java.util.Optional;

/**
 * The payload of a Tissue Sample (drops D7a, docs/DROPS_SPECS.md 2.5): the genome and the bred stats of the animal it was taken
 * from, nothing else. Pure Java, like {@link Infusion} and {@link Seed}: the item keeps it in the data component
 * {@code bunnidogs:tissue_data} as the canonical text {@code genome/stats} (for example {@code 33-33-33-33-33-33/0,0,0,0,0}).
 * The Calipers read it ({@link Readout#tissue}); the Gene Splicer of a later chunk will use it.
 */
public record Tissue(Genome genome, Stats stats) {
    /** Canonical text form. Round-trips exactly through {@link #parse}. */
    @Override
    public String toString() {
        return genome + "/" + stats;
    }

    /**
     * Parses the text form. The stats part may be missing (then zero), like in an infusion; empty if the genome or the stats
     * are malformed.
     */
    public static Optional<Tissue> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String head = text.trim();
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
        return Optional.of(new Tissue(genome.get(), stats));
    }
}
