package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * What a bunnidog drops when it dies (docs/DROPS_SPECS.md, chunk D1). Pure Java, no Minecraft: items are named by
 * string id ({@code minecraft:rabbit_foot}), like {@link Juice} names effects; the glue
 * ({@code drop/BunnidogDrops}) turns an id into an item and skips (with a log line) one that does not exist.
 *
 * <p><b>The idea: loot is a readout of the animal.</b> The genes decide WHAT can drop and how well ({@link Line#innate}:
 * the multipliers the genes already give), the bred stat points decide HOW MUCH ({@link #yieldOf}). Nothing new is stored
 * on the animal: the roll reads the EXPRESSED genome and the {@link Stats}, plus the circumstances of the death
 * ({@link Context}).
 *
 * <p><b>Layers so far:</b> layer 0, the hide (D2: an adult always drops one pelt of its expressed coat, {@link #hideCount});
 * layer 1, the stat lines (D1, D4, D5), only those whose items exist yet: flesh, prime haunch, rabbit's foot, sinew, whisker and bone meal
 * ({@link #LINES}); and layer 2, the gene signatures (D3, D4: an ADULT killed by a player may drop an item that its rare or wild
 * alleles stand for, {@link #SIGNATURES}). Lines and signatures whose item does not exist yet are defined here (so the spec's
 * tables can be tested) but not active: Starlit Dust arrived with its item in D7b, so every line and every signature row is active (every stat line since D5). Layer 3, the Mutant
 * Marrow (D6b: one roll per EXPRESSED rare allele, {@link #MARROW_CHANCE}, adult + player kill), is here since D6b. Layer 4, the Tissue
 * Sample (D7a: one roll per kill, certain with the Calipers in the killer's off hand, {@link #TISSUE_CHANCE} otherwise; pups count),
 * is here since D7a. Layer 5, the five legends (D8d: one per stat, an ADULT killed by a player, from 100 bred points on, {@link #legendChance}), is here since D8d. A line that cannot drop for THIS animal (E = 0: no Leap points, so no sinew) is left
 * out of the tools' lists, so a plain animal does not show {@code sinew 0.00}.
 *
 * <p>Order of evaluation (spec 1.3): a drop never depends on another drop. Two functions share the same tables, so what a
 * tool shows is what a kill rolls: {@link #roll} for a real kill, {@link #expected} for the Calipers and the command.
 */
public final class Drops {
    private Drops() {}

    // ---- the numbers (starting values; one place, cheap to change) ----

    /** Each Looting level multiplies a stat line's expected count by (1 + this). A line with E = 0 stays 0. */
    public static final double LOOTING_PER_LEVEL = 0.15;
    /** A pup gives this fraction of every stat line (a litter culled at birth must not be the best way to farm). */
    public static final double PUP_FACTOR = 0.25;
    /** Beyond this many points over the onset a line grows with the square root instead of linearly. */
    public static final double KNEE = 20.0;
    /** Each Looting level is this chance of ONE extra pelt (so Looting III is 45%, and 7 levels make a second pelt certain). */
    public static final double HIDE_LOOTING_CHANCE_PER_LEVEL = 0.15;
    /** The pelt of a coat is the item {@code bunnidogs:<coat>_pelt} (one per allele of {@link Gene#COAT}, registered from the same table). */
    public static final String PELT_NAMESPACE = "bunnidogs:";
    public static final String PELT_SUFFIX = "_pelt";
    /** statFactor(p) = clamp(1 + p / {@link #STAT_FACTOR_PER_POINT}, {@link #STAT_FACTOR_MIN}, {@link #STAT_FACTOR_MAX}): a bred line is up to 3x likelier, one bred AGAINST its stat is halved. */
    public static final double STAT_FACTOR_PER_POINT = 50.0;
    public static final double STAT_FACTOR_MIN = 0.5;
    public static final double STAT_FACTOR_MAX = 3.0;
    /** worthFactor(w) = min({@link #WORTH_FACTOR_MAX}, 1 + w / {@link #WORTH_FACTOR_PER_POINT}), w = the animal's POSITIVE points over all five stats ("how heavily bred"). */
    public static final double WORTH_FACTOR_PER_POINT = 100.0;
    public static final double WORTH_FACTOR_MAX = 3.0;
    /** Each Looting level multiplies a signature's chance by (1 + this); the chance never exceeds {@link #CHANCE_CAP}. */
    public static final double CHANCE_LOOTING_PER_LEVEL = 0.10;
    public static final double CHANCE_CAP = 0.95;
    /** Layer 3 (spec 2.4): the Mutant Marrow. Every rare (mutation) allele the animal EXPRESSES is one roll at this flat chance, count 1. */
    public static final double MARROW_CHANCE = 0.25;
    public static final String MARROW_ID = "bunnidogs:mutant_marrow";
    public static final String MARROW_LABEL = "mutant marrow";
    /** Layer 4 (spec 2.5): the Tissue Sample. One roll per kill, count 1: certain with the Calipers in the killer's OFF hand, otherwise this chance. */
    public static final double TISSUE_CHANCE = 0.04;
    public static final double TISSUE_OFF_HAND_CHANCE = 1.0;
    public static final String TISSUE_ID = "bunnidogs:tissue_sample";
    public static final String TISSUE_LABEL = "tissue sample";
    /**
     * Layer 5 (spec 2.6): the legends, one per stat. {@code chance(p) = 0} below {@link #LEGEND_ONSET} points,
     * else {@code min(}{@link #LEGEND_CAP}{@code , }{@link #LEGEND_BASE}{@code + }{@link #LEGEND_PER_POINT}{@code x (p - onset))}:
     * 2% at 100 points, 4% at 200, 8% at 400, 15% from 750. Count 1, adults killed by a player only, NOT changed by Looting.
     */
    public static final int LEGEND_ONSET = 100;
    public static final double LEGEND_BASE = 0.02;
    public static final double LEGEND_PER_POINT = 0.0002;
    public static final double LEGEND_CAP = 0.15;
    /** The ways of taking that give the hide. Only DEATH exists today. */
    public static final Set<Source> HIDE_SOURCES = Set.copyOf(EnumSet.of(Source.DEATH));

    /** How a drop is taken. Only DEATH exists today; a living harvest adds values later (spec section 11). */
    public enum Source { DEATH }

    /** Which gene multiplier a line multiplies by. All of them already exist in {@link Traits}. */
    public enum Innate {
        HEALTH, SPEED, JUMP, HEARING, FERTILITY;

        double of(Traits traits) {
            return switch (this) {
                case HEALTH -> traits.healthMultiplier();
                case SPEED -> traits.speedMultiplier();
                case JUMP -> traits.jumpMultiplier();
                case HEARING -> traits.hearingMultiplier();
                case FERTILITY -> traits.fertility();
            };
        }
    }

    /**
     * One stat line (spec 1.4 and 2.1).
     *
     * @param key       short name for messages
     * @param label     English name for the Calipers and the command
     * @param stat      the bred stat whose points decide the amount
     * @param itemId    the item
     * @param cookedId  the item when the animal dies burning (like vanilla cows), or null
     * @param onset     points below which the line gives nothing from the curve (the haunch starts at 20); 0 = from the start
     * @param base      yield at zero points
     * @param per       yield per point up to {@link #KNEE} points above the onset
     * @param tail      square-root growth per sqrt(point) beyond the knee
     * @param floor     the least a bad line can yield (a line bred AGAINST its stat), before the innate multiplier
     * @param cap       the most one kill can drop of this line
     * @param innate    the gene multiplier
     * @param sources   the ways of taking that apply to this line
     */
    public record Line(String key, String label, Stat stat, String itemId, String cookedId, double onset, double base,
            double per, double tail, double floor, int cap, Innate innate, Set<Source> sources) {}

    private static Line line(String key, String label, Stat stat, String itemId, String cookedId, double onset, double base,
            double per, double tail, double floor, int cap, Innate innate) {
        return new Line(key, label, stat, itemId, cookedId, onset, base, per, tail, floor, cap, innate, Set.copyOf(EnumSet.of(Source.DEATH)));
    }

    public static final Line FLESH = line("flesh", "flesh", Stat.VITALITY, "bunnidogs:raw_bunnidog_flesh", "bunnidogs:cooked_bunnidog_flesh",
            0, 1.0, 0.10, 0.5, 0.5, 16, Innate.HEALTH);
    public static final Line HAUNCH = line("haunch", "prime haunch", Stat.VITALITY, "bunnidogs:prime_haunch", "bunnidogs:roast_haunch",
            20, 0.0, 0.02, 0.15, 0.0, 4, Innate.HEALTH);
    public static final Line FOOT = line("foot", "rabbit's foot", Stat.SWIFTNESS, "minecraft:rabbit_foot", null,
            0, 0.10, 0.05, 0.3, 0.0, 8, Innate.SPEED);
    public static final Line SINEW = line("sinew", "sinew", Stat.LEAP, "bunnidogs:sinew", null,
            0, 0.0, 0.08, 0.3, 0.0, 8, Innate.JUMP);
    public static final Line WHISKER = line("whisker", "whisker", Stat.SENSES, "bunnidogs:whisker", null,
            0, 0.0, 0.08, 0.3, 0.0, 8, Innate.HEARING);
    public static final Line BONE_MEAL = line("bone_meal", "bone meal", Stat.BLOOM, "minecraft:bone_meal", null,
            0, 0.5, 0.06, 0.3, 0.0, 12, Innate.FERTILITY);

    /** Every line the spec defines (the yield table is tested over all of them). */
    public static final List<Line> ALL_LINES = List.of(FLESH, HAUNCH, FOOT, SINEW, WHISKER, BONE_MEAL);

    /** The lines that are ACTIVE: only those whose items exist. D4 added sinew and whisker, D5 the haunch: since then this is every line of the spec, in its table order. */
    public static final List<Line> LINES = List.of(FLESH, HAUNCH, FOOT, SINEW, WHISKER, BONE_MEAL);

    /**
     * The circumstances of a death. {@code playerKill} is vanilla's "recently hit by a player"; {@code burning} is whether the
     * animal was on fire; {@code looting} is the killer's Looting level; {@code calipersOffHand} is for the Tissue Sample (D7a: the killer holds the Calipers in the off hand).
     * They are INPUTS here, not facts of the engine: nothing in this class assumes a death except through {@code source}.
     */
    public record Context(Source source, boolean baby, boolean playerKill, boolean burning, int looting, boolean calipersOffHand) {
        public Context {
            looting = Math.max(0, looting);
        }

        /** A plain adult killed by a player without Looting: what the Calipers and the command show. */
        public static final Context PLAIN_ADULT = new Context(Source.DEATH, false, true, false, 0, false);

        public Context withBaby(boolean b) {
            return new Context(source, b, playerKill, burning, looting, calipersOffHand);
        }
    }

    /** One stack to drop. */
    public record Drop(String itemId, int count) {}

    /**
     * What the average kill gives of one item, and how likely at least one is. {@code chanceOnly} marks a signature row, which the tools
     * print as a percentage ({@code gold nugget 13%}) instead of an average count.
     */
    public record Expectation(String itemId, String label, double mean, double chanceAtLeastOne, boolean chanceOnly) {}

    /**
     * One gene signature (spec 2.3): when the animal's EXPRESSED allele of {@code gene} is {@code allele}, a kill by a player may drop
     * {@code min}..{@code max} of {@code itemId} with chance {@code base} x a factor: {@link #worthFactor} when {@code stat} is null,
     * else {@link #statFactor} of that stat. Common alleles have no row (their effect is already in the innate multipliers).
     */
    public record Signature(Gene gene, String allele, String itemId, String label, int min, int max, double base, Stat stat, Set<Source> sources) {
        /** {@code coat:snow}, as in the spec. */
        public String key() {
            return gene.name().toLowerCase(Locale.ROOT) + ":" + allele;
        }
    }

    private static Signature sig(Gene gene, String allele, String itemId, String label, int min, int max, double base, Stat stat) {
        return new Signature(gene, allele, itemId, label, min, max, base, stat, Set.copyOf(EnumSet.of(Source.DEATH)));
    }

    /** All 24 rows of the spec's table, in its order. */
    public static final List<Signature> ALL_SIGNATURES = List.of(
            sig(Gene.COAT, "spotted", "minecraft:cookie", "cookie", 1, 3, 0.12, null),
            sig(Gene.COAT, "snow", "minecraft:snowball", "snowball", 4, 8, 0.30, null),
            sig(Gene.COAT, "snow", "minecraft:packed_ice", "packed ice", 1, 2, 0.08, null),
            sig(Gene.COAT, "sand", "minecraft:gold_nugget", "gold nugget", 1, 3, 0.10, null),
            sig(Gene.COAT, "moss", "minecraft:slime_ball", "slime ball", 1, 2, 0.10, null),
            sig(Gene.COAT, "pale", "minecraft:amethyst_shard", "amethyst shard", 1, 2, 0.10, null),
            sig(Gene.COAT, "slate", "minecraft:flint", "flint", 1, 3, 0.15, null),
            sig(Gene.COAT, "slate", "minecraft:raw_iron", "raw iron", 1, 1, 0.06, null),
            sig(Gene.COAT, "ember", "minecraft:blaze_powder", "blaze powder", 1, 2, 0.10, null),
            sig(Gene.COAT, "void", "minecraft:chorus_fruit", "chorus fruit", 1, 1, 0.12, null),
            sig(Gene.COAT, "void", "minecraft:ender_pearl", "ender pearl", 1, 1, 0.04, null),
            sig(Gene.COAT, "glow", "minecraft:glowstone_dust", "glowstone dust", 1, 3, 0.12, null),
            sig(Gene.EARS, "bat", "minecraft:phantom_membrane", "phantom membrane", 1, 1, 0.10, Stat.SENSES),
            sig(Gene.EARS, "huge", "minecraft:echo_shard", "echo shard", 1, 1, 0.04, Stat.SENSES),
            sig(Gene.LEGS, "fleet", "minecraft:sugar", "sugar", 1, 3, 0.20, Stat.SWIFTNESS),
            sig(Gene.LEGS, "springy", "minecraft:slime_ball", "slime ball", 1, 2, 0.15, Stat.LEAP),
            sig(Gene.LEGS, "hoofed", "minecraft:leather", "leather", 1, 1, 0.20, Stat.LEAP),
            sig(Gene.BUILD, "woolly", "minecraft:white_wool", "white wool", 1, 3, 0.30, Stat.VITALITY),
            sig(Gene.BUILD, "lean", "minecraft:bone", "bone", 1, 2, 0.25, Stat.VITALITY),
            sig(Gene.TEMPERAMENT, "feral", "bunnidogs:fang", "fang", 1, 1, 0.20, Stat.VITALITY),
            sig(Gene.TEMPERAMENT, "feral", "minecraft:bone", "bone", 1, 2, 0.20, Stat.VITALITY),
            sig(Gene.TEMPERAMENT, "skittish", "minecraft:feather", "feather", 1, 2, 0.25, Stat.SWIFTNESS),
            sig(Gene.TEMPERAMENT, "stoic", "minecraft:flint", "flint", 1, 2, 0.20, Stat.VITALITY),
            sig(Gene.FERTILITY, "starborn", "bunnidogs:starlit_dust", "starlit dust", 1, 2, 0.25, Stat.BLOOM));

    /** Items that do not exist yet: their rows are defined but not active. Remove an id here in the chunk that ships the item (the fang left in D4, starlit dust in D7b: it is empty now, kept for the next item that arrives late). */
    public static final Set<String> NOT_YET = Set.of();

    /** The ACTIVE signatures: every row whose item exists. */
    public static final List<Signature> SIGNATURES = ALL_SIGNATURES.stream().filter(s -> !NOT_YET.contains(s.itemId())).toList();

    // ---- the formulas ----

    /**
     * The stat-line yield BEFORE the innate multiplier and the cap (spec 1.4):
     * <pre>q = max(0, p - onset)   (with onset 0 a negative p just continues the linear part down to the floor)
     * yield = base + per*q                       if q &lt;= 20
     *       = base + per*20 + tail*sqrt(q - 20)  otherwise
     * yield = max(floor, yield)</pre>
     */
    public static double yieldOf(Line line, int points) {
        double q = line.onset() == 0.0 ? points : Math.max(0.0, points - line.onset());
        double y = q <= KNEE
                ? line.base() + line.per() * q
                : line.base() + line.per() * KNEE + line.tail() * Math.sqrt(q - KNEE);
        return Math.max(line.floor(), y);
    }

    /** The expected count E of a line for this animal and this death (spec 1.4), before stochastic rounding and the cap. */
    public static double expectedCount(Line line, Genome genome, Stats stats, Context ctx) {
        double innate = line.innate().of(Traits.of(genome));
        double e = innate * yieldOf(line, stats.get(line.stat())) * (1.0 + LOOTING_PER_LEVEL * ctx.looting());
        return ctx.baby() ? e * PUP_FACTOR : e;
    }

    /** Can this line drop anything for this animal at all? False when E = 0 (nothing bred into the stat, or the cap is below one): such a line is not listed by the tools. */
    public static boolean canDrop(Line line, Genome genome, Stats stats, Context ctx) {
        return line.cap() >= 1 && expectedCount(line, genome, stats, ctx) > 0.0;
    }

    /** Unbiased stochastic rounding of E, then the cap: {@code floor(E)} plus one more with probability {@code frac(E)}. */
    public static int count(double e, int cap, RandomGenerator rng) {
        int whole = (int) Math.floor(e);
        double frac = e - whole;
        int n = whole + (rng.nextDouble() < frac ? 1 : 0);
        return Math.min(cap, n);
    }

    /** Mean of {@link #count} over the dice (exact, no sampling), used for the readouts and the tests. */
    public static double meanCount(double e, int cap) {
        int whole = (int) Math.floor(e);
        double frac = e - whole;
        return Math.min(cap, whole) * (1.0 - frac) + Math.min(cap, whole + 1) * frac;
    }

    /** Probability that {@link #count} is at least one. */
    public static double chanceOfAny(double e, int cap) {
        if (cap < 1) {
            return 0.0;
        }
        return e >= 1.0 ? 1.0 : e;
    }

    // ---- layer 0: the hide ----

    /** The expressed coat's name ({@code tan}, {@code glow}, ...): what the pelt is made of. */
    public static String coatName(Genome genome) {
        return genome.expressed(Gene.COAT).name();
    }

    /** The item id of the pelt this animal drops: {@code bunnidogs:tan_pelt}. A coat that only the animal CARRIES (a hidden glow) never shows in the pelt. */
    public static String peltId(Genome genome) {
        return PELT_NAMESPACE + coatName(genome) + PELT_SUFFIX;
    }

    /** Expected pelts of an ADULT: one, plus {@link #HIDE_LOOTING_CHANCE_PER_LEVEL} per Looting level of one more (at most one more). */
    public static double hideMean(int looting) {
        return 1.0 + Math.min(1.0, HIDE_LOOTING_CHANCE_PER_LEVEL * Math.max(0, looting));
    }

    /** One pelt always, one more with the Looting chance. */
    public static int hideCount(int looting, RandomGenerator rng) {
        return 1 + (rng.nextDouble() < Math.min(1.0, HIDE_LOOTING_CHANCE_PER_LEVEL * Math.max(0, looting)) ? 1 : 0);
    }

    /** Does this death give a hide at all? An adult does, on ANY kind of death (a pup does not); no player kill is needed. */
    public static boolean givesHide(Context ctx) {
        return !ctx.baby() && HIDE_SOURCES.contains(ctx.source());
    }

    // ---- layer 2: the gene signatures ----

    /** {@code clamp(1 + p/50, 0.5, 3)}. */
    public static double statFactor(int points) {
        return Math.max(STAT_FACTOR_MIN, Math.min(STAT_FACTOR_MAX, 1.0 + points / STAT_FACTOR_PER_POINT));
    }

    /** The animal's POSITIVE points over all five stats: how heavily bred it is. */
    public static int worth(Stats stats) {
        int w = 0;
        for (Stat stat : Stat.values()) {
            w += Math.max(0, stats.get(stat));
        }
        return w;
    }

    /** {@code min(3, 1 + w/100)}. */
    public static double worthFactor(double worth) {
        return Math.min(WORTH_FACTOR_MAX, 1.0 + worth / WORTH_FACTOR_PER_POINT);
    }

    /** The chance of one signature: {@code min(0.95, base x factor x (1 + 0.10 x looting))}. */
    public static double chance(Signature s, Stats stats, Context ctx) {
        double factor = s.stat() == null ? worthFactor(worth(stats)) : statFactor(stats.get(s.stat()));
        return Math.min(CHANCE_CAP, s.base() * factor * (1.0 + CHANCE_LOOTING_PER_LEVEL * ctx.looting()));
    }

    /** Does this animal SHOW the allele of the row? Only the expressed allele counts (a carried one does nothing). */
    public static boolean matches(Signature s, Genome genome) {
        return genome.expressed(s.gene()).name().equals(s.allele());
    }

    /** Signatures need an adult AND a player kill (spec 2.3). */
    public static boolean givesSignatures(Context ctx) {
        return !ctx.baby() && ctx.playerKill() && ctx.source() == Source.DEATH;
    }

    // ---- layer 3: the Mutant Marrow ----

    /** How many genes SHOW a rare (mutation) allele, including Diet. Wild alleles and carried-but-hidden ones do not count. */
    public static int rareCount(Genome genome) {
        int n = 0;
        for (Gene gene : Gene.values()) {
            if (genome.expressed(gene).isRare()) {
                n++;
            }
        }
        return n;
    }

    /** Marrow needs an adult AND a player kill (spec 2.4, decision 16). Same filter as the signatures, kept as its own rule so the two can change apart. */
    public static boolean givesMarrow(Context ctx) {
        return !ctx.baby() && ctx.playerKill() && ctx.source() == Source.DEATH;
    }

    /** The chance of ONE marrow roll: {@code min(0.95, 0.25 x (1 + 0.10 x looting))} (the chance formula of spec 1.5, with the flat base of 2.4). */
    public static double marrowChance(Context ctx) {
        return Math.min(CHANCE_CAP, MARROW_CHANCE * (1.0 + CHANCE_LOOTING_PER_LEVEL * ctx.looting()));
    }

    /** The Tissue Sample needs a player kill (spec 2.5) but NOT an adult: a pup's genes are real, so a pup can give one too. */
    public static boolean givesTissue(Context ctx) {
        return ctx.playerKill() && ctx.source() == Source.DEATH;
    }

    /** The chance of the one Tissue Sample roll: 100% with the Calipers in the killer's off hand, else 4%. Looting does not change it. */
    public static double tissueChance(Context ctx) {
        return ctx.calipersOffHand() ? TISSUE_OFF_HAND_CHANCE : TISSUE_CHANCE;
    }

    // ---- layer 5: the legends ----

    /** One legend: the stat that earns it, its item id, and its name in the tools' text. */
    public record Legend(Stat stat, String itemId, String label) {}

    /** The five legends in the stat order (spec 2.6 and section 4): one per bred stat, each an EPIC plain item that the Trophy Pedestal can hold. */
    public static final List<Legend> LEGENDS = List.of(
            new Legend(Stat.VITALITY, "bunnidogs:titan_haunch", "titan haunch"),
            new Legend(Stat.SWIFTNESS, "bunnidogs:gilded_foot", "gilded foot"),
            new Legend(Stat.LEAP, "bunnidogs:moon_sinew", "moon sinew"),
            new Legend(Stat.SENSES, "bunnidogs:listening_shell", "listening shell"),
            new Legend(Stat.BLOOM, "bunnidogs:everbloom", "everbloom"));

    /** Legends need an adult AND a player kill (spec 2.6). Kept as its own rule, like the marrow's. */
    public static boolean givesLegends(Context ctx) {
        return !ctx.baby() && ctx.playerKill() && ctx.source() == Source.DEATH;
    }

    /** The chance of the one roll of a stat with {@code points} bred points: 0 below 100, then 2% rising 0.02% a point, never over 15%. Looting is not an input. */
    public static double legendChance(int points) {
        if (points < LEGEND_ONSET) {
            return 0.0;
        }
        return Math.min(LEGEND_CAP, LEGEND_BASE + LEGEND_PER_POINT * (points - LEGEND_ONSET));
    }

    // ---- the two entry points ----

    /** The real kill: layer 0 (the hide, adults only), layer 1 (the stat lines; they apply to pups too, at x0.25), layer 2 (the signatures) and layer 3 (the marrow; both: an adult killed by a player) and layer 4 (the Tissue Sample: a player kill, pups included) and layer 5 (the legends: an adult killed by a player, one roll per stat bred to 100 points). Layers 0 and 1 need no player kill. */
    public static List<Drop> roll(Genome genome, Stats stats, Context ctx, RandomGenerator rng) {
        List<Drop> drops = new ArrayList<>();
        if (givesHide(ctx)) {
            drops.add(new Drop(peltId(genome), hideCount(ctx.looting(), rng)));
        }
        for (Line line : LINES) {
            if (!line.sources().contains(ctx.source())) {
                continue;
            }
            int n = count(expectedCount(line, genome, stats, ctx), line.cap(), rng);
            if (n > 0) {
                String id = ctx.burning() && line.cookedId() != null ? line.cookedId() : line.itemId();
                drops.add(new Drop(id, n));
            }
        }
        if (givesSignatures(ctx)) {
            for (Signature s : SIGNATURES) {
                if (matches(s, genome) && s.sources().contains(ctx.source()) && rng.nextDouble() < chance(s, stats, ctx)) {
                    drops.add(new Drop(s.itemId(), s.min() + rng.nextInt(s.max() - s.min() + 1)));
                }
            }
        }
        if (givesMarrow(ctx)) {
            double chance = marrowChance(ctx);
            int marrow = 0;
            for (int i = rareCount(genome); i > 0; i--) {            // one independent roll per expressed rare allele
                if (rng.nextDouble() < chance) {
                    marrow++;
                }
            }
            if (marrow > 0) {
                drops.add(new Drop(MARROW_ID, marrow));
            }
        }
        if (givesTissue(ctx) && rng.nextDouble() < tissueChance(ctx)) {
            drops.add(new Drop(TISSUE_ID, 1));                       // layer 4: the glue gives the stack the animal's genes (BunnidogDrops)
        }
        if (givesLegends(ctx)) {
            for (Legend legend : LEGENDS) {                          // layer 5: one independent roll per stat
                double chance = legendChance(stats.get(legend.stat()));
                if (chance > 0.0 && rng.nextDouble() < chance) {
                    drops.add(new Drop(legend.itemId(), 1));
                }
            }
        }
        return drops;
    }

    /** The same rules without dice: what the Calipers line and {@code /bunnidogs drops} show. Raw items (never the burning variant). */
    public static List<Expectation> expected(Genome genome, Stats stats, Context ctx) {
        List<Expectation> out = new ArrayList<>();
        if (givesHide(ctx)) {
            out.add(new Expectation(peltId(genome), coatName(genome) + " pelt", hideMean(ctx.looting()), 1.0, false));
        }
        for (Line line : LINES) {
            if (!line.sources().contains(ctx.source())) {
                continue;
            }
            double e = expectedCount(line, genome, stats, ctx);
            if (!canDrop(line, genome, stats, ctx)) {
                continue;                                            // sinew from an animal with no Leap points is not worth a line of text
            }
            out.add(new Expectation(line.itemId(), line.label(), meanCount(e, line.cap()), chanceOfAny(e, line.cap()), false));
        }
        if (givesSignatures(ctx)) {
            for (Signature s : SIGNATURES) {
                if (matches(s, genome)) {
                    double chance = chance(s, stats, ctx);
                    out.add(new Expectation(s.itemId(), s.label(), chance * (s.min() + s.max()) / 2.0, chance, true));
                }
            }
        }
        int rare = rareCount(genome);
        if (givesMarrow(ctx) && rare > 0) {
            double chance = marrowChance(ctx);                       // an average count like a stat line (spec decision 26), not one percentage: there are several rolls
            out.add(new Expectation(MARROW_ID, MARROW_LABEL, rare * chance, 1.0 - Math.pow(1.0 - chance, rare), false));
        }
        if (givesTissue(ctx)) {
            double chance = tissueChance(ctx);                       // one roll, one percentage (like a signature)
            out.add(new Expectation(TISSUE_ID, TISSUE_LABEL, chance, chance, true));
        }
        if (givesLegends(ctx)) {
            for (Legend legend : LEGENDS) {                          // one percentage per legend, only for a stat bred to the onset (like the other rows, no 0% lines)
                double chance = legendChance(stats.get(legend.stat()));
                if (chance > 0.0) {
                    out.add(new Expectation(legend.itemId(), legend.label(), chance, chance, true));
                }
            }
        }
        return out;
    }

    /** The average count of each item over {@code kills} kills (for {@code /bunnidogs drops simulate n}); items that never dropped are listed with 0. */
    public static Map<String, Double> simulate(Genome genome, Stats stats, Context ctx, int kills, RandomGenerator rng) {
        Map<String, Long> totals = new LinkedHashMap<>();
        if (givesHide(ctx)) {
            totals.put(peltId(genome), 0L);
        }
        for (Line line : LINES) {
            if (line.sources().contains(ctx.source()) && canDrop(line, genome, stats, ctx)) {
                totals.put(ctx.burning() && line.cookedId() != null ? line.cookedId() : line.itemId(), 0L);
            }
        }
        if (givesSignatures(ctx)) {
            for (Signature s : SIGNATURES) {
                if (matches(s, genome)) {
                    totals.putIfAbsent(s.itemId(), 0L);
                }
            }
        }
        if (givesMarrow(ctx) && rareCount(genome) > 0) {
            totals.putIfAbsent(MARROW_ID, 0L);
        }
        if (givesTissue(ctx)) {
            totals.putIfAbsent(TISSUE_ID, 0L);
        }
        if (givesLegends(ctx)) {
            for (Legend legend : LEGENDS) {
                if (legendChance(stats.get(legend.stat())) > 0.0) {
                    totals.putIfAbsent(legend.itemId(), 0L);
                }
            }
        }
        for (int i = 0; i < kills; i++) {
            for (Drop d : roll(genome, stats, ctx, rng)) {
                totals.merge(d.itemId(), (long) d.count(), Long::sum);
            }
        }
        Map<String, Double> averages = new LinkedHashMap<>();
        totals.forEach((id, total) -> averages.put(id, kills == 0 ? 0.0 : (double) total / kills));
        return averages;
    }

    // ---- text for the tools ----

    /** One expectation as text: {@code "flesh 1.0"}, {@code "rabbit's foot 0.10 (10% for one)"}, a signature as {@code "gold nugget 13%"}. */
    public static String describe(Expectation e) {
        if (e.chanceOnly()) {
            return String.format(Locale.ROOT, "%s %d%%", Texts.t(e.label()), Math.round(e.chanceAtLeastOne() * 100.0));
        }
        if (e.mean() >= 1.0) {
            return String.format(Locale.ROOT, "%s %.1f", Texts.t(e.label()), e.mean());
        }
        return String.format(Locale.ROOT, Texts.t("%s %.2f (%d%% for one)"), Texts.t(e.label()), e.mean(), Math.round(e.chanceAtLeastOne() * 100.0));
    }

    /** The Calipers line: {@code "Drops (adult kill, no Looting): pelt: tan, flesh 1.0, rabbit's foot 0.10 (10% for one), bone meal 0.50 (50% for one)"}. A pup has no pelt. */
    public static String describe(Genome genome, Stats stats, boolean baby) {
        Context ctx = Context.PLAIN_ADULT.withBaby(baby);
        StringBuilder sb = new StringBuilder(Texts.t("Drops (%s kill, no Looting): ",baby ? Texts.t("pup") : Texts.t("adult")));
        boolean first = true;
        for (Expectation e : expected(genome, stats, ctx)) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(e.itemId().equals(peltId(genome)) ? Texts.t("pelt: ") + Texts.t(coatName(genome)) : describe(e));   // the hide is certain: no numbers
            first = false;
        }
        return sb.toString();
    }
}
