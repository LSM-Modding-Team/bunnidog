package com.bunnidogs.mod.genetics;

import java.util.Locale;
import java.util.Optional;

/**
 * A wild lineage: an isolated population of bunnidogs that lives in one kind of place and carries alleles that
 * exist nowhere else (see docs/DESIGN.md "Wild lineages"). The founder recipes live in {@link Founders}; which
 * biomes belong to which lineage is DATA (biome tags {@code bunnidogs:lineage_<key>}), see docs/REFERENCE.md.md.
 *
 * <p>Pure Java, no Minecraft imports. The enum order is not saved anywhere (genomes store allele ids only), but the
 * {@link #key()} is used in tag and file names, so do not rename keys.
 */
public enum Lineage {
    /** The default population: plains, forests, savanna. Its founders are the plain common-allele founders. */
    MEADOW("meadow"),
    TUNDRA("tundra"),
    DESERT("desert"),
    SWAMP("swamp"),
    CAVE("cave"),
    MOUNTAIN("mountain"),
    NETHER("nether"),
    END("end"),
    /** Ocean-refuge founders are explicitly assigned; an ordinary ocean spawn is not an island discovery. */
    ISLAND("island");

    private final String key;

    Lineage(String key) {
        this.key = key;
    }

    /** Lowercase name used in biome tag ids ({@code lineage_<key>}) and in debug output. */
    public String key() {
        return key;
    }

    public static Optional<Lineage> fromKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        String k = key.toLowerCase(Locale.ROOT);
        for (Lineage l : values()) {
            if (l.key.equals(k)) {
                return Optional.of(l);
            }
        }
        return Optional.empty();
    }
}
