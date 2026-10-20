package com.bunnidogs.mod.registry;

import java.util.EnumMap;
import java.util.Map;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Lineage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

/**
 * Which wild lineage lives where. This is DATA, not code: every lineage owns a biome tag
 * {@code bunnidogs:lineage_<key>} (files in {@code data/bunnidogs/tags/worldgen/biome/}). Since Phase 4g these tags decide WHERE WARRENS
 * appear (the tag {@code bunnidogs:warren_surface} unites the surface lineages; caves, the nether and the end islands use their own lineage
 * tag, see data/bunnidogs/neoforge/biome_modifier/add_warren_*.json) and which lineage the bunnidogs of a warren belong to. Other mods and
 * data packs can add their biomes to these tags. A biome that is in no lineage tag counts as {@link Lineage#MEADOW}.
 */
public final class ModBiomeTags {
    private ModBiomeTags() {}

    private static final Map<Lineage, TagKey<Biome>> LINEAGE_TAGS = new EnumMap<>(Lineage.class);

    static {
        for (Lineage lineage : Lineage.values()) {
            LINEAGE_TAGS.put(lineage, TagKey.create(Registries.BIOME,
                    Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "lineage_" + lineage.key())));
        }
    }

    /** If a biome is (wrongly) in two lineage tags, the first of these wins. MEADOW is the fallback, not listed. */
    private static final Lineage[] PRIORITY = {
            Lineage.NETHER, Lineage.END, Lineage.CAVE, Lineage.SWAMP, Lineage.DESERT, Lineage.TUNDRA, Lineage.MOUNTAIN, Lineage.ISLAND};

    public static TagKey<Biome> lineage(Lineage lineage) {
        return LINEAGE_TAGS.get(lineage);
    }

    /** The lineage of the wild population that lives in the biome at {@code pos}. */
    public static Lineage lineageAt(LevelReader level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        for (Lineage lineage : PRIORITY) {
            if (biome.is(LINEAGE_TAGS.get(lineage))) {
                return lineage;
            }
        }
        return Lineage.MEADOW;
    }

    /**
     * Extra rule for NATURAL spawning, on top of the placement type (solid ground, room to stand). Surface lineages
     * need open sky (no bunnidogs in dark cellars under a plains); cave and nether bunnidogs live where there is none.
     * Since Phase 4g the mod adds no natural spawns of its own (warrens replaced them); the rule stays so that a data pack that
     * adds a spawner for bunnidogs gets sensible placement.
     */
    public static boolean canSpawnNaturally(LevelReader level, BlockPos pos) {
        Lineage lineage = lineageAt(level, pos);
        return lineage == Lineage.CAVE || lineage == Lineage.NETHER || level.canSeeSky(pos);
    }
}
