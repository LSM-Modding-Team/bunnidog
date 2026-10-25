package com.bunnidogs.mod.layout;

import com.bunnidogs.mod.genetics.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/** Separate mind pages keep both contributing copies, capacity, inheritance and splice choices readable. */
public final class MindSheets {
    private MindSheets() {}
    private static final List<CapabilityRules.Task> ROUTINES = List.of(CapabilityRules.Task.HARVEST,
            CapabilityRules.Task.DELIVERY, CapabilityRules.Task.PLANTING, CapabilityRules.Task.NURSERY,CapabilityRules.Task.SCOUTING);

    public static Page.Spread individual(Genome genome, Metrics metrics) {
        return individual(genome, Texts.t("Bunnidog"), metrics);
    }
    public static Page.Spread individual(Genome genome, String source, Metrics metrics) {
        var left = new Page(); var right = new Page();
        left.text(0, 0, Texts.t("MIND GENES"), Page.Tone.HEAD);
        line(left, 9, source, Page.Tone.MUTED, metrics);
        var mind = genome.mind();
        for (var gene : CognitiveGene.values()) {
            int y = 27 + gene.ordinal() * 27;
            left.text(0, y, MindText.gene(gene), Page.Tone.HEAD);
            left.textRight(Page.WIDTH, y + 18, String.format(Locale.ROOT, "%.2f / 4", mind.get(gene)), Page.Tone.GOOD);
            line(left, y + 9, MindText.pair(genome, gene), Page.Tone.INK, metrics);
            left.hotspot(0, y, Page.WIDTH, 27, MindText.gene(gene) + ": " + MindText.pair(genome, gene) + ". "
                    + MindText.allele(genome.allele(gene, 0)) + ": " + MindText.contribution(genome.allele(gene, 0)) + " "
                    + MindText.allele(genome.allele(gene, 1)) + ": " + MindText.contribution(genome.allele(gene, 1)), -1);
        }
        line(left, 138, Texts.t("Both copies contribute."), Page.Tone.MUTED, metrics);
        left.text(0, 147, genome.mindCode(), Page.Tone.MUTED);
        right.text(0, 0, Texts.t("ROUTINE POTENTIAL"), Page.Tone.HEAD);
        for (int i = 0; i < ROUTINES.size(); i++) {
            var task = ROUTINES.get(i); var potential = CapabilityRules.potential(mind, task);
            int y = 18 + i * 22;
            line(right, y, task.label(), Page.Tone.HEAD, metrics);
            line(right, y + 9, CapabilityRules.describe(potential), potential == CapabilityRules.Potential.LIMITED ? Page.Tone.MUTED : Page.Tone.GOOD, metrics);
            right.hotspot(0, y, Page.WIDTH, 20, CapabilityRules.requirements(task, false) + " "
                    + Texts.t("With instruction and nearby support: ") + CapabilityRules.requirements(task, true)
                    + Texts.t(" Genes show capacity, not learned experience. Body traits, health, tools, stock and safe paths still matter."), -1);
        }
        line(right, 135, Texts.t("Potential is not training."), Page.Tone.MUTED, metrics);
        line(right, 147, Texts.t("Samples store no lessons."), Page.Tone.MUTED, metrics);
        return new Page.Spread(left, right);
    }

    public static Page.Spread forecast(Cross cross, Metrics metrics) {
        var forecast = new CapabilityForecast(cross);
        var left = new Page(); var right = new Page();
        left.text(0, 0, Texts.t("MIND FORECAST"), Page.Tone.HEAD);
        line(left, 9, cross.actualSeed().isPresent() ? Texts.t("actual seed") : Texts.t("sample A x B"), Page.Tone.MUTED, metrics);
        for (var gene : CognitiveGene.values()) {
            int y = 22 + gene.ordinal() * 27;
            left.text(0, y, MindText.gene(gene), Page.Tone.HEAD);
            line(left, y + 9, String.format(Locale.ROOT, "%.2f – %.2f / 4", forecast.minimum(gene), forecast.maximum(gene)), Page.Tone.INK, metrics);
            left.hotspot(0, y, Page.WIDTH, 24, Texts.t("Range across these parents' possible pups. Both inherited copies contribute; several genes affect each capacity. Quality and Fortune do not increase mind capacity."), -1);
        }
        line(left, 138, Texts.t("Before mutation."), Page.Tone.MUTED, metrics);
        left.hotspot(0, 138, Page.WIDTH, 18, cross.seed().mutationScope() + " "
                + Texts.t("Mutation chance: %.1f%% per pup. No lessons or community knowledge are inherited.", cross.mutationChance() * 100), -1);
        line(left, 147, Texts.t("Hover for mutation rules."), Page.Tone.MUTED, metrics);
        right.text(0, 0, Texts.t("PUP POTENTIAL"), Page.Tone.HEAD);
        for (int i = 0; i < ROUTINES.size(); i++) {
            var task = ROUTINES.get(i); int y = 14 + i * 24;
            double independent = forecast.chance(task, CapabilityRules.Potential.INDEPENDENT);
            double supported = forecast.chance(task, CapabilityRules.Potential.SUPPORTED);
            line(right, y, task.label(), Page.Tone.HEAD, metrics);
            line(right, y + 9, Texts.t("%s alone; %s guided", ForecastSheet.percent(independent),ForecastSheet.percent(supported)), Page.Tone.GOOD, metrics);
            right.hotspot(0, y, Page.WIDTH, 22, CapabilityRules.requirements(task, false) + " "
                    + Texts.t("Independent %s; supported only %s; beyond capacity %s. These are exact joint genetic odds before mutation, not chances of being born educated.",
                    ForecastSheet.percent(independent), ForecastSheet.percent(supported), ForecastSheet.percent(1 - independent - supported)), -1);
        }
        line(right, 138, Texts.t("All 12 fixed: %s", ForecastSheet.percent(cross.fullFixedPupChance())), Page.Tone.MUTED, metrics);
        right.hotspot(0, 138, Page.WIDTH, 18, Texts.t("Registry purity includes eight body and four mind pairs. Existing redstone compares the original seven body pairs."), -1);
        return new Page.Spread(left, right);
    }

    public static Page.Spread inheritance(Cross cross, Metrics metrics) {
        return new Page.Spread(inheritancePage(cross, false, metrics), inheritancePage(cross, true, metrics));
    }

    private static Page inheritancePage(Cross cross, boolean fixed, Metrics metrics) {
        var page = new Page();
        page.text(0, 0, fixed ? Texts.t("TWO MIND COPIES") : Texts.t("INHERITS ALLELE"), Page.Tone.HEAD);
        for (var gene : CognitiveGene.values()) {
            var counts = new LinkedHashMap<Integer, Integer>();
            for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) {
                int a = cross.a().genome().alleleId(gene, i), b = cross.b().genome().alleleId(gene, j);
                if (fixed) { if (a == b) counts.merge(a, 1, Integer::sum); }
                else { counts.merge(a, 1, Integer::sum); if (a != b) counts.merge(b, 1, Integer::sum); }
            }
            int y = 18 + gene.ordinal() * 30;
            page.text(0, y, MindText.gene(gene), Page.Tone.HEAD);
            var entries = new ArrayList<String>();
            counts.forEach((id, n) -> entries.add(MindText.allele(gene.allele(id)) + " " + ForecastSheet.percent(n / 4.0)));
            String text = entries.isEmpty() ? Texts.t("none possible") : String.join(", ", entries);
            lines(page, y + 9, text, 2, metrics);
            page.hotspot(0, y, Page.WIDTH, 30, text + Texts.t(". Per pup, before mutation. Both copies contribute; neither is hidden. At-least-one-copy odds can overlap."), -1);
        }
        line(page, 147, Texts.t("Before mutation."), Page.Tone.MUTED, metrics);
        return page;
    }

    public static Page.Spread splicing(Tissue a, Tissue b, Metrics metrics) {
        return new Page.Spread(splicePage(a.genome(), b.genome(), true, metrics), splicePage(b.genome(), a.genome(), false, metrics));
    }

    private static Page splicePage(Genome recipient, Genome donor, boolean intoA, Metrics metrics) {
        var page = new Page();
        page.text(0, 0, Texts.t("MIND SPLICE INTO ") + (intoA ? "A" : "B"), Page.Tone.HEAD);
        for (var gene : CognitiveGene.values()) {
            int y = 16 + gene.ordinal() * 30;
            page.text(0, y, MindText.gene(gene), Page.Tone.HEAD);
            for (int copy = 0; copy < 2; copy++) {
                var plan = Splicing.plan(recipient, donor, gene, copy);
                String text = Texts.t("Copy %s: %s", copy + 1, MindText.allele(donor.allele(gene, copy)));
                line(page, y + 9 + copy * 9, text, plan.isPresent() ? Page.Tone.GOOD : Page.Tone.MUTED, metrics);
                String hover = text + ". " + (plan.isEmpty() ? Texts.t("Already carried; no cost.")
                        : Texts.t("Replaces recipient copy %s, the lower contribution to this capacity (first on a tie). ", plan.get().slot() + 1)
                        + MindText.summary(plan.get().genome()) + " "
                        + Texts.t("Knowledge stays personal; a new capacity does not teach a routine. Body genes, bred stats and relative health stay. Costs one sample, one Mutagen and one tool use."));
                page.hotspot(0, y + 9 + copy * 9, Page.WIDTH, 9, hover, -1);
            }
        }
        line(page, 138, Texts.t("Choose donor copy 1 or 2."), Page.Tone.MUTED, metrics);
        line(page, 147, Texts.t("Read-only; samples stay."), Page.Tone.MUTED, metrics);
        return page;
    }

    private static void line(Page page, int y, String text, Page.Tone tone, Metrics metrics) {
        String fitted = Page.fit(text, Page.WIDTH, metrics);
        page.text(0, y, fitted, tone);
        if (!fitted.equals(text)) page.hotspot(0, y, Page.WIDTH, Page.LINE, text, -1);
    }
    private static void lines(Page page, int y, String text, int limit, Metrics metrics) {
        var wrapped = Page.wrap(text, Page.WIDTH, metrics);
        for (int i = 0; i < Math.min(limit, wrapped.size()); i++)
            line(page, y + i * Page.LINE, wrapped.get(i) + (i == limit - 1 && wrapped.size() > limit ? ".." : ""), Page.Tone.INK, metrics);
    }
}
