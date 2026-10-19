package com.bunnidogs.mod.layout;

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
        left.text(0, 0, "Purebred Registry", Page.Tone.HEAD);
        left.text(0, Page.LINE, query == null || query.isBlank() ? registry.size() + " of " + Pedigree.MAX_ENTRIES + " stable lines"
                : matches.size() + " of " + registry.size() + " match", Page.Tone.MUTED);
        for (int row = 0; row < matches.size(); row++) {
            int i = matches.get(row);
            var entry = registry.entries().get(i);
            String whole = "#" + (i + 1) + "  " + registry.lineageName(entry.genome()).orElseGet(() -> entry.tag().orElse(Plain.look(entry.genome())));
            Page page = row < 14 ? left : right;
            int y = row < 14 ? 27 + Page.LINE * row : Page.LINE * (row - 14);
            page.text(0, y, Page.fit(whole, Page.WIDTH, metrics), Page.Tone.INK);
            page.link(0, y, Page.WIDTH, Page.LINE, i + 1);
            page.hotspot(0, y, Page.WIDTH, Page.LINE, "Line #" + (i + 1) + ": " + Plain.look(entry.genome())
                    + ". All " + Gene.COUNT + " genes have matching copies. Recorded genome: " + entry.genome()
                    + ". The first representative's adult stats and parents are a snapshot, not a live record.", -1);
        }
        right.text(0, 99, "REGISTER A LINE", Page.Tone.HEAD);
        List<String> guide = Page.wrap("Use on a bred adult with matching copies in all 7 genes. Air opens. One entry per genome.", Page.WIDTH, metrics);
        for (int i = 0; i < Math.min(5, guide.size()); i++) right.text(0, 108 + Page.LINE * i, guide.get(i), Page.Tone.MUTED);
        right.hotspot(0, 99, Page.WIDTH, 54, "Breed two matching copies in every gene, including Diet. The adult must have recorded birth parents. "
                + "Calipers reveal mixed pairs; the Genetics Table forecasts two-copy odds. Cross two representatives of the same registered genome "
                + "to breed true before mutation. Bred stats still drift. The Gene Splicer introduces an allele, but cannot fix a pair. "
                + "Registering does not consume the book or change the animal.", -1);
        return new Page.Spread(left, right);
    }
}
