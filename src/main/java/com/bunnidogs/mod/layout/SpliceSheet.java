package com.bunnidogs.mod.layout;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Splicing;
import com.bunnidogs.mod.genetics.Tissue;

/** Read-only plans for both directions. No items, animal state or random rolls are touched. */
public final class SpliceSheet {
    private SpliceSheet() {}

    public static Page.Spread spread(Tissue a, Tissue b, Metrics metrics) {
        return new Page.Spread(page(a.genome(), b.genome(), true, metrics), page(b.genome(), a.genome(), false, metrics));
    }

    private static Page page(Genome recipient, Genome donor, boolean intoA, Metrics metrics) {
        Page page = new Page();
        page.text(0, 0, "SPLICE INTO " + (intoA ? "A" : "B"), Page.Tone.HEAD);
        page.text(0, 9, "donor: sample " + (intoA ? "B" : "A"), Page.Tone.MUTED);
        for (Gene gene : Gene.values()) {
            int y = 20 + gene.ordinal() * 18;
            var source = donor.expressed(gene);
            var plan = Splicing.plan(recipient, donor, gene);
            String hover = Plain.geneName(gene) + ": recipient has " + Plain.label(recipient.allele(gene, 0))
                    + " + " + Plain.label(recipient.allele(gene, 1)) + ". Donor expresses " + Plain.label(source) + ". ";
            String note;
            if (plan.isEmpty()) {
                page.textRight(Page.WIDTH, y, pair(recipient, gene) + " (kept)", Page.Tone.MUTED);
                note = "already carried";
                hover += "Already carried: the tool refuses this gene without cost. Breed carriers to fix two copies.";
            } else {
                var change = plan.orElseThrow();
                boolean shown = change.genome().expressed(gene).id() == source.id();
                page.textRight(Page.WIDTH, y, pair(recipient, gene) + " > " + pair(change.genome(), gene), Page.Tone.GOOD);
                note = Plain.word(source) + (shown ? " shows" : " hidden");
                hover += "Replace copy " + (change.slot() + 1) + " (the less dominant copy; first on a tie). Result: "
                        + Plain.label(change.genome().allele(gene, 0)) + " + " + Plain.label(change.genome().allele(gene, 1))
                        + "; " + Plain.label(change.genome().expressed(gene)) + " shows. One sample, one Mutagen and one tool use. "
                        + "Use the matching gene on a healthy adult; one minute recovery. Recipient bred stats and relative health stay.";
            }
            page.text(0, y, Plain.shortGeneName(gene), Page.Tone.INK);
            page.text(0, y + Page.LINE, Page.fit(note, Page.WIDTH, metrics), Page.Tone.MUTED);
            page.hotspot(0, y, Page.WIDTH, 18, hover + " Samples are snapshots; a live animal's genes may have changed.", -1);
        }
        page.text(0, 147, Page.fit("Read-only; samples stay.", Page.WIDTH, metrics), Page.Tone.MUTED);
        return page;
    }

    private static String pair(Genome genome, Gene gene) {
        return Integer.toHexString(genome.alleleId(gene, 0)) + Integer.toHexString(genome.alleleId(gene, 1));
    }
}
