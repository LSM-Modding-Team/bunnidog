package com.bunnidogs.mod.layout;

import com.bunnidogs.mod.genetics.Texts;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.bunnidogs.mod.genetics.Allele;
import com.bunnidogs.mod.genetics.Combat;
import com.bunnidogs.mod.genetics.Drift;
import com.bunnidogs.mod.genetics.Drops;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Readout;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.genetics.Units;

/**
 * The Animal Sheet (Phase 8 S3, docs/SHEET_SPECS.md section 4): ONE logged animal as one two-page spread. Left page = the
 * animal (header, portrait box, the genome code, one row per gene); right page = what it can do (stats in real units, where
 * its line drifts, its parents). Pure Java: everything is a {@link Page} list with pixel positions, built from {@link Plain}
 * and {@link Units}, so the numbers on the page are the numbers of the game. The 17-line budget of a page is in the
 * constants below; hover boxes (S5) and the real portrait (S4) come later.
 */
public final class SheetLayout {
    private SheetLayout() {}

    // left page, y in px
    static final int PORTRAIT_Y = 11;
    static final int PORTRAIT_H = 45;
    static final int GENOME_LABEL_Y = 59;
    static final int CODE_Y = 68;
    static final int ROWS_Y = 79;
    static final int LEGEND_Y = 145;
    static final int LABEL_W = 40;          // width of the gene-name column ("Temper." is about 38 px)

    /** The hover box of a Tissue Sample's portrait (Phase 8 S8). */
    static final String SAMPLE_HOVER = "A Tissue Sample holds the genes and the bred stats of the animal it was taken from. The picture shows that animal as an adult.";

    // right page, y in px
    static final int STATS_Y = 9;
    static final int NEXT_Y = 108;
    static final int PARENTS_Y = 135;

    /** The 7 x 6 heart icon, row by row ('X' = painted). The screen paints it with fills, so no texture or sprite name is needed. */
    public static final String[] HEART = {
            ".XX.XX.",
            "XXXXXXX",
            "XXXXXXX",
            ".XXXXX.",
            "..XXX..",
            "...X..." };

    /** Flat colour of each coat for the portrait box (the base fur colour of {@code art/gen_bunnidog.py}), ARGB. */
    private static final Map<String, Integer> COAT_COLORS = new LinkedHashMap<>();
    static {
        coat("tan", 0xE0B470);
        coat("cream", 0xF0E0BA);
        coat("brown", 0x8B613F);
        coat("spotted", 0xE0B470);
        coat("snow", 0xEEF3FA);
        coat("sand", 0xE8D094);
        coat("moss", 0x70844A);
        coat("glow", 0x48968C);
        coat("pale", 0xCACAD4);
        coat("slate", 0x7A7E86);
        coat("ember", 0x8E3426);
        coat("void", 0x2E2044);
    }

    private static void coat(String name, int rgb) {
        COAT_COLORS.put(name, 0xFF000000 | rgb);
    }

    /** The portrait colour of a coat (ARGB); a neutral grey for a coat nobody listed (the self-check fails for that). */
    public static int coatColor(String coat) {
        return COAT_COLORS.getOrDefault(coat, 0xFF9A9A9A);
    }

    public static boolean hasCoatColor(String coat) {
        return COAT_COLORS.containsKey(coat);
    }

    /**
     * Whose sheet it is (Phase 8 S8): it only changes the header, whether adult / pup is told, what hovering the portrait says and
     * what the bottom of the right page says about parents.
     */
    enum Kind { BOOK, SINGLE, SAMPLE, TABLE_A, TABLE_B, MOTHER, FATHER }

    /** The spread of logged animal {@code index} (0-based) of {@code book}. */
    public static Page.Spread sheet(Pedigree book, int index, Metrics m) {
        Pedigree.Entry e = book.entries().get(index);
        return new Page.Spread(left(book, index, e, Kind.BOOK, m), right(book, index, e, Kind.BOOK, m));
    }

    /**
     * The spread of ONE animal that is in no book (the Calipers window, Phase 8 S6): the same two pages, but no {@code #n} in the
     * header (the ear tag, or the word Bunnidog), no generation (it is only known inside a book) and no parent numbers or links.
     */
    public static Page.Spread single(Pedigree.Entry e, Metrics m) {
        return new Page.Spread(left(null, -1, e, Kind.SINGLE, m), right(null, -1, e, Kind.SINGLE, m));
    }

    /** The animal a Tissue Sample stands for, as a sheet entry: its genes and bred stats, nothing else (no parents, no tag, no age). */
    public static Pedigree.Entry sampleEntry(Tissue t) {
        return new Pedigree.Entry(t.genome(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(t.stats()), false);
    }

    /**
     * The Tissue Sample window (Phase 8 S8): the same two pages as an animal's sheet, headed {@code Tissue sample}, with no
     * adult / pup (a sample does not know), no generation and no parents (it is the animal itself, nothing is said about its family).
     */
    public static Page.Spread sample(Tissue t, Metrics m) {
        Pedigree.Entry e = sampleEntry(t);
        return new Page.Spread(left(null, -1, e, Kind.SAMPLE, m), right(null, -1, e, Kind.SAMPLE, m));
    }

    /**
     * One sample of the Genetics Table window (T2): the sample sheet of {@link #sample}, headed {@code Table: sample A} / {@code Table: sample B}
     * after the slot it stands on.
     */
    public static Page.Spread tableSample(Tissue t, boolean slotA, Metrics m) {
        Pedigree.Entry e = sampleEntry(t);
        Kind kind = slotA ? Kind.TABLE_A : Kind.TABLE_B;
        return new Page.Spread(left(null, -1, e, kind, m), right(null, -1, e, kind, m));
    }

    /** A sample sheet: a Tissue Sample on its own or on a Genetics Table slot (they differ only in the header). */
    private static boolean isSample(Kind kind) {
        return kind == Kind.SAMPLE || kind == Kind.TABLE_A || kind == Kind.TABLE_B;
    }

    /**
     * One parent of a seed as a sheet (Phase 8 S8): the mother (spread 1 of the seed window) or the father (spread 2). Headed
     * {@code Seed: mother} / {@code Seed: father}; a parent was an adult when it mated, so it is told as one; its own parents are not
     * kept in the seed, so the bottom of the page says that instead of listing them.
     */
    public static Page.Spread seedParent(Seed seed, boolean mother, Metrics m) {
        Pedigree.Entry e = SeedSheet.parentEntry(seed, mother);
        Kind kind = mother ? Kind.MOTHER : Kind.FATHER;
        return new Page.Spread(left(null, -1, e, kind, m), right(null, -1, e, kind, m));
    }

    // ------------------------------------------------------------------ left page: the animal

    private static Page left(Pedigree book, int index, Pedigree.Entry e, Kind kind, Metrics m) {
        Genome g = e.genome();
        Page page = new Page();
        String header = switch (kind) {
            case BOOK -> "#" + (index + 1) + book.lineageName(g).map(name -> "  "+name).orElseGet(() -> e.tag().map(t -> "  " + t).orElse(""));
            case SINGLE -> e.tag().orElse(Texts.t("Bunnidog"));
            case SAMPLE -> Texts.t("Tissue sample");
            case TABLE_A -> Texts.t("Table: sample A");
            case TABLE_B -> Texts.t("Table: sample B");
            case MOTHER -> Texts.t("Seed: mother");
            case FATHER -> Texts.t("Seed: father");
        };
        int headerWidth = Page.WIDTH-(e.stats().isPresent() && !isSample(kind) ? m.width(e.pup() ? Texts.t("pup") : Texts.t("adult"))+4 : 0);
        page.text(0, 0, Page.fit(header,headerWidth,m), Page.Tone.HEAD);
        if (book != null) page.hotspot(0,0,headerWidth,Page.LINE,header+e.tag().map(t -> Texts.t(". Ear tag: ")+t).orElse("")+Texts.t(". Keeper names label this exact recorded genome; they do not change inherited traits."),-1);
        if (e.stats().isPresent() && !isSample(kind)) {
            page.textRight(Page.WIDTH, 0, e.pup() ? Texts.t("pup") : Texts.t("adult"), Page.Tone.MUTED);
        }

        page.portrait(0, PORTRAIT_Y, Page.WIDTH, PORTRAIT_H, g.expressed(Gene.COAT).name());
        // hover the animal: what it drops when killed (the Calipers line); an old entry has no stats to work it out from;
        // a sample is not an animal that can be killed, so it explains itself instead
        page.hotspot(0, PORTRAIT_Y, Page.WIDTH, PORTRAIT_H,
                isSample(kind) ? Texts.t(SAMPLE_HOVER)
                        : e.stats().isPresent() ? Drops.describe(g, e.stats().get(), e.pup())
                                : Texts.t("Drops cannot be worked out: stats not recorded. Right-click this animal with the book again."), -1);

        page.text(0, GENOME_LABEL_Y, Texts.t("GENOME"), Page.Tone.HEAD);
        page.hotspot(0, GENOME_LABEL_Y, Page.WIDTH, Page.LINE, Purebreds.describe(g), -1);
        if (book != null) {
            page.textRight(Page.WIDTH, GENOME_LABEL_Y, Texts.t("generation ") + book.generationOf(index), Page.Tone.MUTED);
        }
        String[] pairs = g.bodyCode().split("-");
        int x = 0;
        for (int i = 0; i < pairs.length; i++) {
            if (i > 0) {
                page.text(x, CODE_Y, "-", Page.Tone.MUTED);
                x += m.width("-");
            }
            page.text(x, CODE_Y, pairs[i], Page.Tone.INK);
            page.pair(x, CODE_Y, m.width(pairs[i]));
            x += m.width(pairs[i]);
        }
        page.hotspot(0, CODE_Y, x, Page.LINE, Texts.t(Plain.HOW_TO_READ), -1);

        int y = ROWS_Y;
        int geneIndex = 0;
        for (Gene gene : Gene.values()) {
            String combat=gene==Gene.TEMPERAMENT && e.stats().isPresent() ? " " + Combat.describe(g,e.stats().get()) : "";
            page.hotspot(0, y, Page.WIDTH, Page.LINE, Plain.hover(g, gene)+combat, geneIndex++);
            page.text(0, y, Plain.shortGeneName(gene), Page.Tone.HEAD);
            int room = Page.WIDTH - LABEL_W;
            String shown = Page.fit(Plain.word(g.expressed(gene)), room, m);
            page.text(LABEL_W, y, shown, Page.Tone.INK);
            Optional<Allele> hidden = g.hidden(gene);
            if (hidden.isPresent()) {
                int left = room - m.width(shown);
                page.text(LABEL_W + m.width(shown), y, Page.fit("/" + Plain.word(hidden.get()), left, m), Page.Tone.MUTED);
            }
            y += Page.LINE;
        }
        page.text(0, LEGEND_Y, Texts.t("/x = carried gene"), Page.Tone.MUTED);
        return page;
    }

    // ------------------------------------------------------------------ right page: what it can do

    private static Page right(Pedigree book, int index, Pedigree.Entry e, Kind kind, Metrics m) {
        Genome g = e.genome();
        Page page = new Page();
        page.text(0, 0, Texts.t("STATS"), Page.Tone.HEAD);
        page.textRight(Page.WIDTH, 0, Texts.t("pts = real value"), Page.Tone.MUTED);

        if (e.stats().isPresent()) {
            stats(page, g, e, m);
        } else {
            int y = STATS_Y;
            for (String line : Page.wrap(Texts.t("Stats not recorded: right-click this animal with the book again."), Page.WIDTH, m)) {
                page.text(0, y, line, Page.Tone.MUTED);
                y += Page.LINE;
            }
        }

        // where the line drifts
        page.text(0, NEXT_Y, Texts.t("NEXT LITTER"), Page.Tone.HEAD);
        double bonus = e.stats().isPresent() ? Drift.qualityBonus(g, e.stats().get()) : 0.0D;
        int driftLines = bonus > 0.0D ? 1 : 2;
        List<String> drift = Page.wrap(Readout.pushText(g), Page.WIDTH, m);
        for (int i = 0; i < Math.min(driftLines, drift.size()); i++) {
            String s = drift.get(i);
            if (i == driftLines - 1 && drift.size() > driftLines) {
                s = Page.fit(s + "..", Page.WIDTH, m);
            }
            page.text(0, NEXT_Y + Page.LINE * (i + 1), s, Page.Tone.INK);
        }
        if (bonus > 0.0D) {
            page.text(0, NEXT_Y + Page.LINE * 2, Page.fit(String.format(Locale.ROOT, Texts.t("Starborn: +%.2f quality"), bonus), Page.WIDTH, m), Page.Tone.GOOD);
        }

        page.hotspot(0, NEXT_Y, Page.WIDTH, Page.LINE * 3, Readout.pushText(g), -1);

        // a sample and a seed's parent say what they do not know instead of "no recorded parents"
        if (isSample(kind)) {
            page.text(0, PARENTS_Y, Page.fit(Texts.t("A sample holds only"), Page.WIDTH, m), Page.Tone.MUTED);
            page.text(0, PARENTS_Y + Page.LINE, Page.fit(Texts.t("this animal's own genes"), Page.WIDTH, m), Page.Tone.MUTED);
            return page;
        }
        if (kind == Kind.MOTHER || kind == Kind.FATHER) {
            boolean mother = kind == Kind.MOTHER;
            page.text(0, PARENTS_Y, Page.fit(mother ? Texts.t("Mother of the seed") : Texts.t("Father of the seed"), Page.WIDTH, m), Page.Tone.MUTED);
            page.text(0, PARENTS_Y + Page.LINE, Page.fit(mother ? Texts.t("Her parents: not kept") : Texts.t("His parents: not kept"), Page.WIDTH, m), Page.Tone.MUTED);
            return page;
        }

        // parents (each line also has a hover box with the whole look, since the page clips a long one)
        if (e.mother().isPresent() && e.father().isPresent()) {
            int momIndex = book == null ? -1 : book.parentIndex(index, true);
            int dadIndex = book == null ? -1 : book.parentIndex(index, false);
            String mom = parent(Texts.t("Mom"), momIndex, e.mother().get());
            String dad = parent(Texts.t("Dad"), dadIndex, e.father().get());
            page.text(0, PARENTS_Y, Page.fit(mom, Page.WIDTH, m), Page.Tone.INK);
            page.text(0, PARENTS_Y + Page.LINE, Page.fit(dad, Page.WIDTH, m), Page.Tone.INK);
            page.hotspot(0, PARENTS_Y, Page.WIDTH, Page.LINE, mom, -1);
            page.hotspot(0, PARENTS_Y + Page.LINE, Page.WIDTH, Page.LINE, dad, -1);
            if (momIndex >= 0) {
                page.link(0, PARENTS_Y, Page.WIDTH, Page.LINE, momIndex + 1);          // click: open her sheet
            }
            if (dadIndex >= 0) {
                page.link(0, PARENTS_Y + Page.LINE, Page.WIDTH, Page.LINE, dadIndex + 1);
            }
        } else {
            page.text(0, PARENTS_Y, Texts.t("No recorded parents"), Page.Tone.MUTED);
            page.text(0, PARENTS_Y + Page.LINE, Texts.t("(wild or summoned)"), Page.Tone.MUTED);
        }
        return page;
    }

    /**
     * The five stats: line 1 = name, points (green above 0, red below) and the real value at the right edge; line 2 = the plain
     * bunnidog's value in grey. Vitality draws its hearts on a line of its own. If the value does not fit next to the name it
     * takes the grey line instead (the baseline then waits for the hover box of S5).
     */
    private static void stats(Page page, Genome g, Pedigree.Entry e, Metrics m) {
        int y = STATS_Y;
        for (Units.Reading r : Units.readings(g, e.stats().get())) {
            int top = y;
            String name = Plain.statName(r.stat());
            String pts = r.points() == 0 ? "0" : String.format(Locale.ROOT, "%+d", r.points());
            Page.Tone tone = r.points() > 0 ? Page.Tone.GOOD : r.points() < 0 ? Page.Tone.BAD : Page.Tone.MUTED;
            String value = Units.unitText(r.stat(), r.value());
            boolean beside = m.width(name) + 6 + m.width(pts) + 8 + m.width(value) <= Page.WIDTH;

            page.text(0, y, name, Page.Tone.INK);
            page.text(m.width(name) + 6, y, pts, tone);
            if (beside) {
                page.textRight(Page.WIDTH, y, value, Page.Tone.INK);
            }
            int line = y + Page.LINE;
            if (r.stat() == Stat.VITALITY) {
                Units.HeartRow row = Units.heartRow(r.value());
                page.hearts(0, line, row.full(), row.half(), row.times());
                if (row.times() > 0) {
                    page.text(10, line, "x" + row.times(), Page.Tone.INK);
                }
                line += Page.LINE;
            }
            if (beside) {
                page.text(0, line, Page.fit(r.baseText(), Page.WIDTH, m), Page.Tone.MUTED);
            } else {
                page.text(0, line, Page.fit(value, Page.WIDTH, m), Page.Tone.INK);
            }
            y = line + Page.LINE;
            String care = r.stat() == Stat.SENSES ? Texts.t(" Healthy adults bred to Senses +10 yield one loose whisker with a Brush every ten minutes.") : "";
            var profile=Combat.profile(g,e.stats().get());
            String combat=r.stat()==Stat.VITALITY ? String.format(Locale.ROOT,Texts.t(" Adult base bite %.1f HP, every %.1f s before defenses."),profile.bite(),profile.interval()/20.0)
                    : r.stat()==Stat.BLOOM ? String.format(Locale.ROOT,Texts.t(" Combat injury: %.1f minutes of safe loaded recovery, with at least half health."),Combat.recoveryTicks(e.stats().get())/1200.0) : "";
            page.hotspot(0, top, Page.WIDTH, y - top, Units.working(r.stat(), g, e.stats().get()) + care + combat, -1);
        }
    }

    /** {@code Mom #3: tan, long ears} when she is in the book, {@code Mom: slate, hoofed} when she is not (the page clips a long look; the hover box of S5 will show it whole). */
    private static String parent(String role, int index, Genome genome) {
        return role + (index >= 0 ? " #" + (index + 1) : "") + ": " + Plain.look(genome);
    }
}
