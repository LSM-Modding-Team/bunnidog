package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * What a bottle of infused guava juice does when drunk (docs/DESIGN.md "3. Guava juice and infusion", Phase 4f).
 * Pure Java, no Minecraft: effects are named by their vanilla registry id ({@code minecraft:night_vision}); the glue
 * ({@code item/JuiceItem}) looks the id up and skips (with a log line) one that does not exist.
 *
 * <p>The juice is a frozen "liquid pup" ({@link Infusion}): a rolled genome, its bred stat points and the seed's quality.
 * Its EXPRESSED alleles decide which effects it has ({@link #DEFS}); the numbers follow three rules:
 * <ul>
 *   <li><b>Amplifier</b> of an effect that scales: {@code floor(quality / 4)}, at most the effect's own cap
 *       ({@link #AMP_STEP}). So plain and golden seeds give level I, a netherite pair level II, nether star level II
 *       and up, only the beacon and dragon egg tiers reach the high levels. Effects that are a drawback or a trick
 *       (Slowness, Levitation, Night Vision ...) never scale.</li>
 *   <li><b>Duration</b> = the effect's base seconds x (1 + 0.1 quality) x (1 + 0.02 per bred point of the stat that belongs
 *       to the gene, at most doubled), never above the effect's own maximum. A heavily bred line makes longer juice.</li>
 *   <li><b>Price:</b> a seed (a whole litter) plus a juice, so the strongest juices need the strongest seeds.</li>
 * </ul>
 * Several genes can bring the same effect; the strongest amplifier and the longest duration win (no stacking).
 * Every juice also restores a little hunger ({@code minecraft:saturation}); a plain, uninfused juice is only that.
 */
public final class Juice {
    private Juice() {}

    /** One amplifier level per this much quality. */
    public static final double AMP_STEP = 4.0;
    /** Duration grows by this fraction of the base per quality point. */
    public static final double DURATION_PER_QUALITY = 0.1;
    /** Duration grows by this fraction of the base per bred point of the gene's stat, at most {@link #STAT_BONUS_CAP} in total. */
    public static final double DURATION_PER_POINT = 0.02;
    public static final double STAT_BONUS_CAP = 1.0;
    /** Hearing-range effects (Glowing on the mobs around you) reach this many blocks times the ear scale. */
    public static final double AREA_RADIUS = 8.0;
    /** The saturation effect every juice carries: amplifier of a plain juice and of an infused one. */
    public static final int PLAIN_SATURATION_AMP = 1;
    public static final int INFUSED_SATURATION_AMP = 0;
    public static final String SATURATION = "minecraft:saturation";

    /** Where an effect lands. */
    public enum Target {
        /** The drinker. */
        SELF,
        /** Every other living thing within {@link #areaRadius}. */
        NEARBY
    }

    /**
     * The recipe of one effect of one allele.
     *
     * @param id          vanilla effect id
     * @param name        English name for the tooltip
     * @param baseSeconds duration at quality 0 and no bred points
     * @param maxSeconds  hard cap on the duration
     * @param maxAmp      hard cap on the amplifier (0 = level I); 0 also means "does not scale"
     * @param stat        the bred stat whose points lengthen the effect, or null
     * @param target      who gets it
     */
    public record Def(String id, String name, int baseSeconds, int maxSeconds, int maxAmp, Stat stat, Target target) {}

    /** One effect of one bottle, with the numbers already worked out. {@code ticks} is the duration in game ticks. */
    public record Spec(String id, String name, int amplifier, int ticks, Target target) {
        /** "Night Vision II 1:30" (the level is left out at I). */
        public String describe() {
            int seconds = ticks / 20;
            String level = amplifier == 0 ? "" : " " + roman(amplifier + 1);
            String where = target == Target.NEARBY ? " (on mobs nearby)" : "";
            return String.format(Locale.ROOT, "%s%s %d:%02d%s", name, level, seconds / 60, seconds % 60, where);
        }
    }

    private static Def d(String id, String name, int base, int max, int maxAmp, Stat stat, Target target) {
        return new Def("minecraft:" + id, name, base, max, maxAmp, stat, target);
    }

    private static Def self(String id, String name, int base, int max, int maxAmp, Stat stat) {
        return d(id, name, base, max, maxAmp, stat, Target.SELF);
    }

    private static Def nearby(String id, String name, int base, int max, Stat stat) {
        return d(id, name, base, max, 0, stat, Target.NEARBY);
    }

    /**
     * The table: gene name + expressed allele name -> its effects. Every allele that is not listed has none (the plain
     * ones: standard, tan, cream, brown, spotted, short legs, average, prolific ...). Keys are
     * {@code "<gene>:<allele>"}. Kept in one place so the self-check can test it against {@link Gene}.
     */
    public static final Map<String, List<Def>> DEFS = new LinkedHashMap<>();

    static {
        // ears: hearing = you notice the mobs around you (Glowing on them); bat ears also see in the dark; tiny ears are cozy
        DEFS.put("ears:long", List.of(nearby("glowing", "Glowing", 15, 90, Stat.SENSES)));
        DEFS.put("ears:huge", List.of(nearby("glowing", "Glowing", 30, 120, Stat.SENSES)));
        DEFS.put("ears:fennec", List.of(nearby("glowing", "Glowing", 30, 120, Stat.SENSES)));
        DEFS.put("ears:bat", List.of(nearby("glowing", "Glowing", 30, 120, Stat.SENSES),
                self("night_vision", "Night Vision", 90, 600, 0, null)));
        DEFS.put("ears:tiny", List.of(self("resistance", "Resistance", 30, 300, 2, Stat.VITALITY)));
        // coat: one effect per wild coat, the plain coats do nothing
        DEFS.put("coat:snow", List.of(self("absorption", "Absorption", 60, 300, 4, null)));
        DEFS.put("coat:sand", List.of(self("fire_resistance", "Fire Resistance", 180, 900, 0, null)));
        DEFS.put("coat:moss", List.of(self("dolphins_grace", "Dolphin's Grace", 60, 300, 0, null),
                self("water_breathing", "Water Breathing", 120, 600, 0, null)));
        DEFS.put("coat:glow", List.of(self("night_vision", "Night Vision", 120, 600, 0, null),
                self("glowing", "Glowing", 60, 300, 0, null)));
        DEFS.put("coat:pale", List.of(self("night_vision", "Night Vision", 180, 900, 0, null)));
        DEFS.put("coat:slate", List.of(self("slow_falling", "Slow Falling", 90, 600, 0, null)));
        DEFS.put("coat:ember", List.of(self("fire_resistance", "Fire Resistance", 120, 900, 0, null),
                self("strength", "Strength", 45, 300, 3, null)));
        DEFS.put("coat:void", List.of(self("levitation", "Levitation", 3, 6, 0, null)));      // a blink, not a flight
        // legs: speed and jump
        DEFS.put("legs:long", List.of(self("speed", "Speed", 45, 300, 3, Stat.SWIFTNESS)));
        DEFS.put("legs:springy", List.of(self("jump_boost", "Jump Boost", 60, 300, 3, Stat.LEAP)));
        DEFS.put("legs:fleet", List.of(self("speed", "Speed", 75, 360, 3, Stat.SWIFTNESS)));
        DEFS.put("legs:hoofed", List.of(self("jump_boost", "Jump Boost", 75, 360, 3, Stat.LEAP)));
        // build: toughness, or speed for the light ones; the chonker pays for it with Slowness (never scales)
        DEFS.put("build:stocky", List.of(self("resistance", "Resistance", 45, 300, 2, Stat.VITALITY)));
        DEFS.put("build:slim", List.of(self("speed", "Speed", 30, 300, 3, Stat.SWIFTNESS)));
        DEFS.put("build:chonker", List.of(self("absorption", "Absorption", 90, 600, 4, Stat.VITALITY),
                self("slowness", "Slowness", 20, 60, 0, null)));
        DEFS.put("build:woolly", List.of(self("resistance", "Resistance", 60, 300, 2, Stat.VITALITY),
                self("absorption", "Absorption", 60, 300, 4, Stat.VITALITY)));
        DEFS.put("build:lean", List.of(self("speed", "Speed", 60, 300, 3, Stat.SWIFTNESS)));
        // temperament: loyal is the common one, so most juices have a little Regeneration
        DEFS.put("temperament:loyal", List.of(self("regeneration", "Regeneration", 20, 120, 3, Stat.SENSES)));
        DEFS.put("temperament:skittish", List.of(self("invisibility", "Invisibility", 45, 300, 0, null)));
        DEFS.put("temperament:feral", List.of(self("strength", "Strength", 45, 300, 3, Stat.VITALITY)));
        DEFS.put("temperament:stoic", List.of(self("resistance", "Resistance", 60, 300, 2, Stat.VITALITY),
                self("slowness", "Slowness", 30, 90, 0, null)));
        // fertility: luck
        DEFS.put("fertility:swarm", List.of(self("luck", "Luck", 300, 1800, 4, Stat.BLOOM)));
        DEFS.put("fertility:starborn", List.of(self("luck", "Luck", 600, 1800, 4, Stat.BLOOM),
                self("glowing", "Glowing", 30, 120, 0, null)));
    }

    /** The key of an allele in {@link #DEFS}. */
    public static String key(Gene gene, Allele allele) {
        return gene.key() + ":" + allele.name();
    }

    /** The level an effect that scales reaches at this quality, before its own cap. */
    public static int amplifierFor(double quality) {
        return (int) Math.floor(Math.max(0.0, quality) / AMP_STEP);
    }

    /** The factor a duration gets from quality and from the bred points of the effect's stat. */
    public static double durationFactor(double quality, int statPoints) {
        double q = 1.0 + DURATION_PER_QUALITY * Math.max(0.0, quality);
        double s = 1.0 + Math.min(STAT_BONUS_CAP, DURATION_PER_POINT * Math.max(0, statPoints));
        return q * s;
    }

    /** One def worked out for a quality and a stat block. */
    public static Spec spec(Def def, double quality, Stats stats) {
        int amp = Math.min(def.maxAmp(), amplifierFor(quality));
        int points = def.stat() == null ? 0 : stats.get(def.stat());
        double seconds = Math.min(def.maxSeconds(), def.baseSeconds() * durationFactor(quality, points));
        return new Spec(def.id(), def.name(), amp, (int) Math.round(seconds * 20.0), def.target());
    }

    /**
     * The effects of the liquid pup (without the saturation): one per effect id, in gene order (ears, coat, legs, build,
     * temperament, fertility). When two genes bring the same effect the higher amplifier and the longer duration win.
     */
    public static List<Spec> effects(Genome genome, Stats stats, double quality) {
        Map<String, Spec> byId = new LinkedHashMap<>();
        for (Gene gene : Gene.values()) {
            List<Def> defs = DEFS.get(key(gene, genome.expressed(gene)));
            if (defs == null) {
                continue;
            }
            for (Def def : defs) {
                Spec fresh = spec(def, quality, stats);
                Spec old = byId.get(def.id() + "/" + def.target());
                if (old == null) {
                    byId.put(def.id() + "/" + def.target(), fresh);
                } else {
                    byId.put(def.id() + "/" + def.target(), new Spec(old.id(), old.name(),
                            Math.max(old.amplifier(), fresh.amplifier()), Math.max(old.ticks(), fresh.ticks()), old.target()));
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    /** How far the NEARBY effects of this genome reach, in blocks. */
    public static double areaRadius(Genome genome) {
        return AREA_RADIUS * genome.traits().hearingMultiplier();
    }

    /** What the plain, uninfused guava juice does: only the saturation. */
    public static Spec plainSaturation() {
        return new Spec(SATURATION, "Saturation", PLAIN_SATURATION_AMP, 1, Target.SELF);
    }

    /** The saturation every infused juice carries on top of its effects. */
    public static Spec infusedSaturation() {
        return new Spec(SATURATION, "Saturation", INFUSED_SATURATION_AMP, 1, Target.SELF);
    }

    /** "Regeneration 0:20, Speed II 0:45" or "no effects" (for the genome command and the tooltip). */
    public static String describe(Genome genome, Stats stats, double quality) {
        List<Spec> specs = effects(genome, stats, quality);
        if (specs.isEmpty()) {
            return "no effects";
        }
        StringBuilder sb = new StringBuilder();
        for (Spec s : specs) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s.describe());
        }
        return sb.toString();
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(n);
        };
    }
}
