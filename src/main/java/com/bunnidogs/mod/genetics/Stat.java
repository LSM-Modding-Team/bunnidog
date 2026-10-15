package com.bunnidogs.mod.genetics;

import java.util.Locale;
import java.util.Optional;

/**
 * The five BRED stats (docs/DESIGN.md "4. Bred stats" and "5. Bloom"). Unlike the genes, which are fixed by the
 * alleles, a stat is a whole number of <i>points</i> that a bloodline gains or loses generation after generation
 * (see {@link Drift}). Stats have no upper limit.
 *
 * <p>The order is SAVED (see {@link Stats#toString()}): only ever append.
 */
public enum Stat {
    /** Max health. */
    VITALITY("vitality", "Vitality"),
    /** Movement speed. */
    SWIFTNESS("swiftness", "Swiftness"),
    /** Jump strength. */
    LEAP("leap", "Leap"),
    /** Follow/awareness range. */
    SENSES("senses", "Senses"),
    /** How fast pups grow up; for starborn parents also a seed-quality bonus. */
    BLOOM("bloom", "Bloom");

    public static final int COUNT = values().length;

    private final String key;
    private final String displayName;

    Stat(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<Stat> fromKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        String k = key.toLowerCase(Locale.ROOT);
        for (Stat s : values()) {
            if (s.key.equals(k)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
}
