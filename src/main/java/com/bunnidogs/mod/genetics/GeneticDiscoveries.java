package com.bunnidogs.mod.genetics;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Progress comes from actual saved animal snapshots, including hidden copies, without inventing birthplace data. */
public final class GeneticDiscoveries {
    private GeneticDiscoveries() {}
    public static int wildLineageCount() { return Lineage.values().length-1; }
    public static Set<Lineage> wildLineages(Pedigree book) {
        var seen=EnumSet.noneOf(Lineage.class);
        for (var entry:book.entries()) for (var gene:Gene.values()) for (int slot=0;slot<2;slot++) {
            var lineage=gene.allele(entry.genome().alleleId(gene,slot)).lineage();
            if (lineage!=null) seen.add(lineage);
        }
        return Collections.unmodifiableSet(seen);
    }
    public static boolean hasBredMutation(Pedigree book) {
        return book.entries().stream().anyMatch(entry->entry.mother().isPresent() && entry.father().isPresent()
                && java.util.Arrays.stream(Gene.values()).anyMatch(gene->gene.allele(entry.genome().alleleId(gene,0)).isRare()
                    || gene.allele(entry.genome().alleleId(gene,1)).isRare()));
    }
}
