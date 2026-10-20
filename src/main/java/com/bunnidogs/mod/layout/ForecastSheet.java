package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bunnidogs.mod.genetics.Cross;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.genetics.Tissue;

/**
 * The forecast spread of the Genetics Table window (T3, docs/TABLE_SPECS.md): what a litter of sample A x sample B would be like. The numbers are
 * {@link Cross}'s (the game's own rules); this class only lays them out, so what the self-check reads is what the player sees.
 * Left page: litter size, mutation chance, seed quality on plain guavas, and the expected points of each stat. Right page: the odds of each look,
 * one block per gene. Every row has a hover box with the working (both parents' genes, the points each parent has).
 */
public final class ForecastSheet {
    private ForecastSheet() {}

    // left page, y in px
    static final int LITTER_Y = 14;
    static final int MUTATION_Y = 37;
    static final int QUALITY_Y = 60;
    static final int STATS_HEAD_Y = 83;
    static final int STATS_Y = 92;
    static final int STAT_LABEL_W = 50;
    static final int NOTE_Y = 138;

    // right page, y in px
    static final int LOOKS_Y = 11;
    static final int BLOCK_MAX_LINES = 2;
    static final int FOOT_Y = 138;
    static final int BLOCK_GAP = Math.max(0, (FOOT_Y - LOOKS_Y - Gene.COUNT * Page.LINE * BLOCK_MAX_LINES) / Gene.COUNT);

    public static Page.Spread spread(Tissue a, Tissue b, Metrics m) {
        return spread(new Cross(a,b),m);
    }
    public static Page.Spread spread(Cross cross, Metrics m) {
        return new Page.Spread(left(cross, m), right(cross, m));
    }

    // ------------------------------------------------------------------ left page

    private static Page left(Cross cross, Metrics m) {
        Page page = new Page();
        page.text(0, 0, "FORECAST", Page.Tone.HEAD);
        page.textRight(Page.WIDTH, 0, cross.actualSeed().isPresent() ? "actual seed" : "sample A x B", Page.Tone.MUTED);

        String range = cross.litterLow() == cross.litterHigh() ? "exactly " + cross.litterLow() + " pups" : cross.litterLow() + " or " + cross.litterHigh() + " pups";
        row(page, LITTER_Y, "Litter", String.format(Locale.ROOT, "about %.1f pups", cross.litter()), Page.Tone.INK, range,
                SeedSheet.litterHover(), m);
        row(page, MUTATION_Y, "Mutation", String.format(Locale.ROOT, "%.1f%% per pup", cross.mutationChance() * 100.0), Page.Tone.INK,
                cross.seed().mutagen() ? cross.actualSeed().isPresent() ? "treated seed" : "Mutagen preview" : "set by fertility",
                SeedSheet.mutationHover(cross.seed()), m);
        double quality = cross.effectiveQuality();
        row(page, QUALITY_Y, "Quality", String.format(Locale.ROOT, "%.2f", quality), quality > 0.0D ? Page.Tone.GOOD : Page.Tone.MUTED,
                cross.actualSeed().isPresent() ? "saved; no extra Fortune" : cross.plan().fortune() > 0 ? "includes Fortune "+cross.plan().fortune() : cross.plan().guavaQuality() > 0 ? "planned feed" : quality > 0.0D ? "starborn bonus only" : "on plain guavas",
                cross.actualSeed().isPresent() ? "This actual seed keeps its saved quality and Mutagen treatment. Its original guavas and starborn bonus are already included; held feed cannot change them. "
                        +"These points use no additional crop Fortune. A Cradle hatches at this saved quality; Fortune only helps when cutting a ripe planted crop. "+SeedSheet.qualityHover()
                : String.format(Locale.ROOT,"Planned feed: %s. Guavas add %.2f quality; the parents' starborn bonus adds %.2f. Seed quality %.2f. "
                        + "Planned Fortune %d adds %.2f at crop harvest: hatching quality %.2f. Cradles get no Fortune bonus. "
                        + "Held items are preview cues, never spent. Samples are snapshots. ",cross.plan().guavas(),cross.plan().guavaQuality(),
                        cross.quality()-cross.plan().guavaQuality(),cross.quality(),cross.plan().fortune(),cross.plan().fortuneBonus(),quality)+SeedSheet.qualityHover(), m);

        page.text(0, STATS_HEAD_Y, "STATS", Page.Tone.HEAD);
        page.text(STAT_LABEL_W, STATS_HEAD_Y, "A / B", Page.Tone.MUTED);
        page.textRight(Page.WIDTH, STATS_HEAD_Y, "pup", Page.Tone.MUTED);
        double[] drift = cross.drift();
        for (Stat stat : Stat.values()) {
            int y = STATS_Y + Page.LINE * stat.ordinal();
            int pa = cross.a().stats().get(stat);
            int pb = cross.b().stats().get(stat);
            double expected = cross.expectedPoints(stat);
            page.text(0, y, Plain.statName(stat), Page.Tone.INK);
            page.text(STAT_LABEL_W, y, Page.fit(signed(pa) + " / " + signed(pb), Page.WIDTH - STAT_LABEL_W - 30, m), Page.Tone.MUTED);
            page.textRight(Page.WIDTH, y, signed(expected), tone(expected));
            page.hotspot(0, y, Page.WIDTH, Page.LINE, String.format(Locale.ROOT,
                    "%s: A has %s points, B has %s, so a pup starts at %s. The pair's drift adds %s (hatching quality %.2f), so about %s points. "
                            + "Each pup is rounded to whole points at random, so siblings differ by a point.",
                    Plain.statName(stat), signed(pa), signed(pb), signed(cross.startPoints(stat)), signed(drift[stat.ordinal()]), cross.effectiveQuality(), signed(expected)), -1);
        }

        List<String> note = Page.wrap("A pup: the parents' average plus the drift.", Page.WIDTH, m);
        for (int i = 0; i < Math.min(2, note.size()); i++) {
            page.text(0, NOTE_Y + Page.LINE * i, note.get(i), Page.Tone.MUTED);
        }
        return page;
    }

    /** One labelled value with a muted line under it and a hover box over both lines (the seed window's row). */
    private static void row(Page page, int y, String label, String value, Page.Tone tone, String sub, String hover, Metrics m) {
        page.text(0, y, label, Page.Tone.INK);
        page.textRight(Page.WIDTH, y, value, tone);
        page.text(0, y + Page.LINE, Page.fit(sub, Page.WIDTH, m), Page.Tone.MUTED);
        page.hotspot(0, y, Page.WIDTH, Page.LINE * 2, hover, -1);
    }

    // ------------------------------------------------------------------ right page

    private static Page right(Cross cross, Metrics m) {
        Page page = new Page();
        page.text(0, 0, "PUP LOOKS", Page.Tone.HEAD);
        page.textRight(Page.WIDTH, 0, "odds per pup", Page.Tone.MUTED);

        int y = LOOKS_Y;
        for (Gene gene : Gene.values()) {
            List<Cross.Outcome> looks = cross.looks(gene);
            List<String> parts = new ArrayList<>();
            List<String> full = new ArrayList<>();
            for (Cross.Outcome o : looks) {
                parts.add(Plain.word(o.allele()) + " " + percent(o.chance()));
                full.add(Plain.label(o.allele()) + " " + percent(o.chance()));
            }
            List<String> lines = Page.wrap(Plain.shortGeneName(gene) + ": " + String.join(", ", parts), Page.WIDTH, m);
            int shown = Math.min(BLOCK_MAX_LINES, lines.size());
            for (int i = 0; i < shown; i++) {
                String s = lines.get(i);
                if (i == shown - 1 && lines.size() > shown) {
                    s = Page.fit(s + "..", Page.WIDTH, m);
                }
                page.text(0, y + Page.LINE * i, s, Page.Tone.INK);
            }
            page.hotspot(0, y, Page.WIDTH, Page.LINE * shown, hover(cross, gene, full), -1);
            y += Page.LINE * shown + BLOCK_GAP;
        }

        List<String> foot = List.of("Before mutation.","Hover for genes.");
        for (int i = 0; i < Math.min(2, foot.size()); i++) {
            page.text(0, FOOT_Y + Page.LINE * i, foot.get(i), Page.Tone.MUTED);
        }
        return page;
    }

    /** The working behind a gene's odds: both parents' two genes, the rule, the result and the mutation caveat. */
    static String hover(Cross cross, Gene gene, List<String> outcomes) {
        Genome ga = cross.a().genome();
        Genome gb = cross.b().genome();
        return String.format(Locale.ROOT, "%s. %s has %s + %s, %s has %s + %s. Each pup takes one of each parent's two genes at random and the stronger one shows: %s. "
                        + "Odds are before mutation (%.1f%% of pups mutate one random gene).",
                Plain.geneName(gene),cross.actualSeed().isPresent() ? "Mother" : "Sample A", Plain.label(ga.allele(gene, 0)), Plain.label(ga.allele(gene, 1)),
                cross.actualSeed().isPresent() ? "father" : "sample B", Plain.label(gb.allele(gene, 0)), Plain.label(gb.allele(gene, 1)),
                String.join(", ", outcomes), cross.mutationChance() * 100.0);
    }

    // ------------------------------------------------------------------ small formatting helpers

    static String percent(double chance) {
        return Math.round(chance * 100.0) + "%";
    }

    static String signed(int points) {
        return points == 0 ? "0" : String.format(Locale.ROOT, "%+d", points);
    }

    /** One decimal with a sign, and a plain 0.0 when it rounds to nothing (no \"+0.0\", no \"-0.0\"). */
    static String signed(double points) {
        return Math.abs(points) < 0.05D ? "0.0" : String.format(Locale.ROOT, "%+.1f", points);
    }

    private static Page.Tone tone(double points) {
        return points >= 0.05D ? Page.Tone.GOOD : points <= -0.05D ? Page.Tone.BAD : Page.Tone.MUTED;
    }
}
