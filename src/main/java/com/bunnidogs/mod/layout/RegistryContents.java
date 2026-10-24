package com.bunnidogs.mod.layout;

import com.bunnidogs.mod.genetics.Texts;

import java.util.List;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.RecordFilter;

/** Index of stable lines and the requirements, including useful instructions in an empty registry. */
public final class RegistryContents {
    private RegistryContents() {}

    public static Page.Spread spread(Pedigree registry, int n, Metrics metrics) {
        return spread(registry,n,metrics,"");
    }
    public static Page.Spread spread(Pedigree registry, int n, Metrics metrics, String query) {
        int clamped = Math.max(0, Math.min(n, registry.size()));
        if (clamped > 0) return SheetLayout.sheet(registry, clamped - 1, metrics);
        Page left = new Page();
        Page right = new Page();
        var matches = RecordFilter.indices(registry,query);
        left.text(0, 0, Texts.t("Purebred Registry"), Page.Tone.HEAD);
        left.text(0, Page.LINE, query == null || query.isBlank() ? registry.size() + Texts.t(" of ") + Pedigree.MAX_ENTRIES + Texts.t(" stable lines")
                : matches.size() + Texts.t(" of ") + registry.size() + Texts.t(" match"), Page.Tone.MUTED);
        for (int row = 0; row < matches.size(); row++) {
            int i = matches.get(row);
            var entry = registry.entries().get(i);
            String whole = "#" + (i + 1) + "  " + registry.lineageName(entry.genome()).orElseGet(() -> entry.tag().orElse(Plain.look(entry.genome())));
            Page page = row < 14 ? left : right;
            int y = row < 14 ? 27 + Page.LINE * row : Page.LINE * (row - 14);
            page.text(0, y, Page.fit(whole, Page.WIDTH, metrics), Page.Tone.INK);
            page.link(0, y, Page.WIDTH, Page.LINE, i + 1);
            page.hotspot(0, y, Page.WIDTH, Page.LINE, Texts.t("Line #") + (i + 1) + ": " + Plain.look(entry.genome())
                    + Texts.t(". All ") + com.bunnidogs.mod.genetics.Locus.COUNT + Texts.t(" genes have matching copies. Recorded genome: ") + entry.genome()
                    + Texts.t(". The first representative's adult stats and parents are a snapshot, not a live record."), -1);
        }
        right.text(0, 99, Texts.t("REGISTER A LINE"), Page.Tone.HEAD);
        List<String> guide = Page.wrap(Texts.t("Use on a bred adult with all 12 pairs fixed. Air opens. One entry per genome."), Page.WIDTH, metrics);
        for (int i = 0; i < Math.min(5, guide.size()); i++) right.text(0, 108 + Page.LINE * i, guide.get(i), Page.Tone.MUTED);
        right.hotspot(0, 99, Page.WIDTH, 54, Texts.t("Breed matching copies in all eight body and four mind genes. The adult needs recorded birth parents. Calipers and the Table show full purity. Bred stats still drift, and learned routines are personal. A splice introduces one copy; breed to fix the pair. Redstone compares the original seven body pairs."), -1);
        return new Page.Spread(left, right);
    }
}
