package com.bunnidogs.mod.genetics;

/**
 * The guava ladder (docs/DESIGN.md "Seed & Guava loop" -> "2. Guava", Phase 4e). Pure Java, no Minecraft.
 *
 * <p>Each tier is one item: its {@link #id()} is the item id (and the texture, client item, model, lang key and recipe
 * names), its {@link #level()} is its place on the ladder (0..7, what a bunnidog remembers and what is saved) and its
 * {@link #quality()} is what a bunnidog fed that guava contributes to the seed. A seed's quality is the AVERAGE of the
 * two parents' qualities ({@link #average}), so feeding one parent a diamond guava (2) and the other a plain one (0)
 * gives quality 1. Quality has ONE job: it scales the stat drift ({@link Drift}).
 *
 * <h2>Why the qualities are 0, 1, 2, 4, 7, 12, 20 (session 6)</h2>
 * Quality is NOT the ladder position any more. What a quality point buys is not constant: gains grow by 0.5 per point
 * for ever, but costs shrink by 0.25 per point only until quality 4, where they are gone. For the standard +1/-1 trend
 * the net drift per generation is {@code 0.75 q} up to 4 and {@code 1 + 0.5 q} beyond (0, 0.75, 1.5, 3, 4.5, 7, 11 at
 * the tiers below). A straight 0..6 ladder therefore bought LESS per tier from nether star on (the old beacon and
 * dragon egg added 0.5 each, for 8 beacons and a dragon egg), while the recipes got dearer and dearer. The steps are
 * now 1, 1, 2, 3, 5, 8 (each the sum of the two before) and the rule the self-check enforces is: <b>the net drift
 * a tier adds on top of the one below never shrinks</b> (0.75, 0.75, 1.5, 1.5, 2.5, 4). In nether stars (the
 * currency of the top tiers) that is 0.75 quality per star for the nether star guava (4 stars), 0.63 for the beacon
 * guava (8 more) and the egg is one of a kind. Golden and diamond keep their old 1 and 2, so a Fortune III
 * tool (+1.5) is still worth about a golden-to-diamond guava.
 *
 * <h2>The joke tier (session 7)</h2>
 * The Command Block Guava tops the ladder at quality 33: the next Fibonacci-style step (13 on top of the egg's 20), which keeps
 * the rule above (a +1/-1 line gains 17.5 per generation, 6.5 more than the egg's 11; the old step was 4). Its recipe is the
 * dragon egg guava plus a command block, an item survival players cannot get: a creative-mode / {@code /give} luxury, which is
 * the point of the joke. Levels 0..6 keep their saved meaning; level 7 is appended.
 *
 * <p>The ladder is a table on purpose: adding a tier is one enum constant here, one palette in {@code art/gen_guava.py},
 * one recipe, one lang line and one line in the food tag; {@code tools/GenomeCheck.java} fails until all of them exist.
 * The Minecraft side ({@code ModItems}) registers one item per constant.
 */
public enum GuavaTier {
    PLAIN(0, 0.0, "guava", "Guava"),
    GOLDEN(1, 1.0, "golden_guava", "Golden Guava"),
    DIAMOND(2, 2.0, "diamond_guava", "Diamond Guava"),
    NETHERITE(3, 4.0, "netherite_guava", "Netherite Guava"),
    NETHER_STAR(4, 7.0, "nether_star_guava", "Nether Star Guava"),
    BEACON(5, 12.0, "beacon_guava", "Beacon Guava"),
    DRAGON_EGG(6, 20.0, "dragon_egg_guava", "Dragon Egg Guava"),
    COMMAND_BLOCK(7, 33.0, "command_block_guava", "Command Block Guava");

    private final int level;
    private final double quality;
    private final String id;
    private final String displayName;

    GuavaTier(int level, double quality, String id, String displayName) {
        this.level = level;
        this.quality = quality;
        this.id = id;
        this.displayName = displayName;
    }

    /** The place on the ladder, 0 (plain) to 7. This is what a bunnidog remembers and saves; {@link #quality()} is looked up from it. */
    public int level() {
        return level;
    }

    /** The seed quality a parent fed this guava contributes (0 for the plain guava): the table above, NOT the ladder position. */
    public double quality() {
        return quality;
    }

    /** {@link #quality()} as text for tooltips: whole numbers without a decimal point ("12"), others with one ("3.5"). */
    public String qualityText() {
        return quality == Math.rint(quality) ? Integer.toString((int) quality) : String.format(java.util.Locale.ROOT, "%.1f", quality);
    }

    /** Item id path in the {@code bunnidogs} namespace, e.g. {@code golden_guava}. */
    public String id() {
        return id;
    }

    /** English name; the same text is in {@code lang/en_us.json}. */
    public String displayName() {
        return displayName;
    }

    /** The tier one step up the ladder, or {@code null} at the top. */
    public GuavaTier next() {
        GuavaTier[] all = values();
        return level + 1 < all.length ? all[level + 1] : null;
    }

    /** The tier one step down the ladder (what the recipe of this tier starts from), or {@code null} for the plain guava. */
    public GuavaTier previous() {
        return level == 0 ? null : values()[level - 1];
    }

    /** The tier at this place on the ladder, or the nearest end of the ladder for a value outside it. */
    public static GuavaTier ofLevel(int level) {
        GuavaTier[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, level))];
    }

    /**
     * The guava part of a seed's quality: the average of the QUALITIES of the two parents' tiers (fractions allowed;
     * the arguments are ladder levels, as a bunnidog remembers them). A level outside the ladder counts as the nearest
     * end, so a hand-edited save can never lower the quality or go past the top.
     */
    public static double average(int motherLevel, int fatherLevel) {
        return (ofLevel(motherLevel).quality() + ofLevel(fatherLevel).quality()) / 2.0;
    }
}
