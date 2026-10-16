package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bunnidogs.mod.genetics.Cross;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Tissue;

/** The table's hidden-allele forecast. Uses the same four parental combinations as native breeding. */
public final class InheritanceSheet {
    private InheritanceSheet() {}

    public static Page.Spread spread(Tissue a, Tissue b, Metrics metrics) {
        Cross cross = new Cross(a, b);
        return new Page.Spread(page(cross, false, metrics), page(cross, true, metrics));
    }

    private static Page page(Cross cross, boolean fixed, Metrics metrics) {
        Page page = new Page();
        page.text(0, 0, fixed ? "TWO COPIES" : "CARRIES ALLELE", Page.Tone.HEAD);
        if (fixed) {
            String all = String.format(Locale.ROOT, "%.2f%%", cross.fixedPupChance() * 100);
            page.textRight(Page.WIDTH, 0, all, Page.Tone.GOOD);
            page.hotspot(0, 0, Page.WIDTH, Page.LINE, "All seven gene pairs fixed: " + all + " per pup, or "
                    + String.format(Locale.ROOT, "%.2f%%", cross.atLeastOneInLitter(cross.fixedPupChance()) * 100)
                    + " for at least one in an uncrowded litter. Each gene is inherited independently. This can produce different stable genomes; "
                    + "it does not promise one particular registered line. Before mutation; grow an adult before registering it.", -1);
        }
        for (Gene gene : Gene.values()) {
            List<String> summary = new ArrayList<>();
            StringBuilder hover = new StringBuilder(Plain.geneName(gene)).append(fixed
                    ? ": two identical copies (homozygous), so this gene breeds true with the same pair. "
                    : ": at least one copy, including hidden and homozygous copies. These odds overlap; a pup can carry two different alleles. ");
            for (Cross.Inherited odds : cross.inherited(gene)) {
                double probability = fixed ? odds.fixed() : odds.carried();
                if (probability == 0) continue;
                summary.add(Plain.word(odds.allele()) + " " + ForecastSheet.percent(probability));
                hover.append(Plain.label(odds.allele())).append(": ")
                        .append(ForecastSheet.percent(probability)).append(" per pup; ")
                        .append(String.format(Locale.ROOT, "%.1f%%", cross.atLeastOneInLitter(probability) * 100))
                        .append(" for at least one in an uncrowded litter. ");
            }
            if (summary.isEmpty()) {
                summary.add("none possible");
                hover.append("These parents share no allele in this gene, so every pup has two different copies. ");
            }
            hover.append("Before mutation; crowding can reduce the litter.");
            int y = 11 + gene.ordinal() * 18;
            List<String> lines = Page.wrap(Plain.shortGeneName(gene) + ": " + String.join(", ", summary), Page.WIDTH, metrics);
            for (int i = 0; i < Math.min(2, lines.size()); i++) {
                String line = lines.get(i);
                if (i == 1 && lines.size() > 2) line = Page.fit(line + "..", Page.WIDTH, metrics);
                page.text(0, y + i * Page.LINE, line, Page.Tone.INK);
            }
            page.hotspot(0, y, Page.WIDTH, 18, hover.toString(), -1);
        }
        page.text(0, 138, Page.fit("Per pup; before mutation.", Page.WIDTH, metrics), Page.Tone.MUTED);
        page.text(0, 147, Page.fit("Hover for litter odds.", Page.WIDTH, metrics), Page.Tone.MUTED);
        return page;
    }
}
