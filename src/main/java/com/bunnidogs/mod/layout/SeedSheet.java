package com.bunnidogs.mod.layout;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.bunnidogs.mod.genetics.Breeding;
import com.bunnidogs.mod.genetics.Drift;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Seed;

/**
 * The seed window (Phase 8 S8): a seed as a three-spread folio. Spread 0 is the seed itself (left page: quality, litter, mutation
 * chance, where the pups' stats will drift; right page: the two parents, each a link to its own sheet), spread 1 is the mother's
 * sheet and spread 2 the father's ({@link SheetLayout#seedParent}). Pure Java, like the book: the screen only draws the lists.
 * Every number comes from the seed and the constants of {@link Drift} and {@link Breeding}, so the hover texts cannot disagree
 * with the game.
 */
public final class SeedSheet {
    private SeedSheet() {}

    /** The overview and one sheet per parent. */
    public static final int SPREADS = 3;

    // left page, y in px
    static final int QUALITY_Y = 14;
    static final int LITTER_Y = 37;
    static final int MUTATION_Y = 60;
    static final int DRIFT_Y = 83;
    static final int DRIFT_LINES = 3;
    static final int NOTE_Y = 138;

    // right page, y in px
    static final int MOTHER_Y = 14;
    static final int FATHER_Y = 77;
    static final int BLOCK_H = 54;          // header + 3 look lines + carried + code

    public static int spreadCount() {
        return SPREADS;
    }

    /** A parent of the seed as a sheet entry: its genes and bred stats, told as an adult (a parent had to be one to mate), nothing else. */
    public static Pedigree.Entry parentEntry(Seed seed, boolean mother) {
        return new Pedigree.Entry(mother ? seed.mother() : seed.father(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(mother ? seed.motherStats() : seed.fatherStats()), false);
    }

    /** Spread {@code n} of the seed window (0 = the seed, 1 = the mother, 2 = the father); {@code n} is clamped into range. */
    public static Page.Spread spread(Seed seed, int n, Metrics m) {
        int clamped = Math.max(0, Math.min(n, SPREADS - 1));
        return clamped == 0 ? overview(seed, m) : SheetLayout.seedParent(seed, clamped == 1, m);
    }

    // ------------------------------------------------------------------ hover texts (numbers from the game's own constants)

    static String qualityHover() {
        return String.format(Locale.ROOT, "Each point of quality makes a stat's good drift %d%% bigger and its bad drift %d%% smaller "
                + "(the bad drift is gone at quality %d). Fortune on the tool that cuts the crop adds %.1f quality per level.",
                Math.round(Drift.GAIN_PER_QUALITY * 100.0), Math.round(Drift.COST_RELIEF_PER_QUALITY * 100.0),
                Math.round(1.0 / Drift.COST_RELIEF_PER_QUALITY), Seed.FORTUNE_QUALITY_PER_LEVEL);
    }

    static String litterHover() {
        return String.format(Locale.ROOT, "Pups born when the ripe crop is cut: %.0f x the parents' average fertility, between 1 and %d. "
                + "Fewer if %d or more bunnidogs already live within %.0f blocks.",
                Breeding.PUPS_PER_FERTILITY, Breeding.MAX_LITTER, Breeding.CROWD_LIMIT, Breeding.CROWD_RADIUS);
    }

    static String mutationHover(Seed seed) {
        long floor = Math.round(Seed.MUTAGEN_MUTATION_FLOOR * 100.0);
        return seed.mutagen()
                ? "The chance that a pup gets a new mutation. The seed is treated with a Mutagen, so it is at least " + floor + "%."
                : "The chance that a pup gets a new mutation. It follows the parents' fertility. A Mutagen raises it to at least " + floor + "%.";
    }

    static String driftHover(Seed seed) {
        return "How far the pups' stats move from the parents' average, at this seed's quality: " + Seed.driftText(seed.drift(0.0D))
                + ". Fortune when the crop is cut moves them further.";
    }

    // ------------------------------------------------------------------ spread 0: the seed

    private static Page.Spread overview(Seed seed, Metrics m) {
        Page left = new Page();
        Page right = new Page();

        left.text(0, 0, "BUNNIDOG SEED", Page.Tone.HEAD);
        if (seed.mutagen()) {
            left.textRight(Page.WIDTH, 0, "mutagen", Page.Tone.GOOD);
        }

        row(left, QUALITY_Y, "Quality", String.format(Locale.ROOT, "%.2f", seed.quality()),
                seed.quality() > 0.0D ? Page.Tone.GOOD : Page.Tone.MUTED, "guavas and starborn", qualityHover(), m);
        row(left, LITTER_Y, "Litter", String.format(Locale.ROOT, "about %.1f pups", seed.expectedLitter()), Page.Tone.INK,
                "born when it is cut", litterHover(), m);
        row(left, MUTATION_Y, "Mutation", String.format(Locale.ROOT, "%.1f%% per pup", seed.mutationChance() * 100.0), seed.mutagen() ? Page.Tone.GOOD : Page.Tone.INK,
                seed.mutagen() ? "treated with Mutagen" : "set by fertility", mutationHover(seed), m);

        left.text(0, DRIFT_Y, "NEXT LITTER", Page.Tone.HEAD);
        List<String> drift = Page.wrap(Seed.driftText(seed.drift(0.0D)), Page.WIDTH, m);
        for (int i = 0; i < Math.min(DRIFT_LINES, drift.size()); i++) {
            String s = drift.get(i);
            if (i == DRIFT_LINES - 1 && drift.size() > DRIFT_LINES) {
                s = Page.fit(s + "..", Page.WIDTH, m);
            }
            left.text(0, DRIFT_Y + Page.LINE * (i + 1), s, Page.Tone.INK);
        }
        left.hotspot(0, DRIFT_Y, Page.WIDTH, Page.LINE * (DRIFT_LINES + 1), driftHover(seed), -1);

        List<String> note = Page.wrap("Fortune on the tool adds quality when you cut the crop.", Page.WIDTH, m);
        for (int i = 0; i < Math.min(2, note.size()); i++) {
            left.text(0, NOTE_Y + Page.LINE * i, note.get(i), Page.Tone.MUTED);
        }

        right.text(0, 0, "PARENTS", Page.Tone.HEAD);
        right.textRight(Page.WIDTH, 0, "click for the sheet", Page.Tone.MUTED);
        parent(right, MOTHER_Y, "MOTHER", seed.mother(), 1, "Her", m);
        parent(right, FATHER_Y, "FATHER", seed.father(), 2, "His", m);
        return new Page.Spread(left, right);
    }

    /** One labelled value: {@code Quality ........ 3.50} and a muted line under it, with a hover box over both lines. */
    private static void row(Page page, int y, String label, String value, Page.Tone tone, String sub, String hover, Metrics m) {
        page.text(0, y, label, Page.Tone.INK);
        page.textRight(Page.WIDTH, y, value, tone);
        page.text(0, y + Page.LINE, Page.fit(sub, Page.WIDTH, m), Page.Tone.MUTED);
        page.hotspot(0, y, Page.WIDTH, Page.LINE * 2, hover, -1);
    }

    /** A parent block: its name, its look (up to three lines), what it carries and its code; a click opens its sheet. */
    private static void parent(Page page, int y, String role, Genome genome, int spread, String pronoun, Metrics m) {
        page.text(0, y, role, Page.Tone.HEAD);
        String look = Plain.look(genome);
        List<String> lines = Page.wrap(look, Page.WIDTH, m);
        for (int i = 0; i < Math.min(3, lines.size()); i++) {
            String s = lines.get(i);
            if (i == 2 && lines.size() > 3) {
                s = Page.fit(s + "..", Page.WIDTH, m);
            }
            page.text(0, y + Page.LINE * (i + 1), s, Page.Tone.INK);
        }
        page.text(0, y + Page.LINE * 4, Page.fit(Plain.carriesText(genome), Page.WIDTH, m), Page.Tone.MUTED);
        page.text(0, y + Page.LINE * 5, genome.toString(), Page.Tone.MUTED);
        page.hotspot(0, y, Page.WIDTH, BLOCK_H, role.charAt(0) + role.substring(1).toLowerCase(Locale.ROOT) + ": " + look + ". " + Plain.carriesText(genome) + ". Click to open "
                + pronoun.toLowerCase(Locale.ROOT) + " sheet.", -1);
        page.link(0, y, Page.WIDTH, BLOCK_H, spread);
    }
}
