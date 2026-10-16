package com.bunnidogs.mod.genetics;

import java.util.Optional;

/** Introgress one donor allele. Fixing it into a homozygous pair remains a breeding task. */
public final class Splicing {
    private Splicing() {}
    public static final int TOOL_USES = 16;
    public static final int RECOVERY_TICKS = 60 * 20;
    public record Change(Genome genome, int slot, Allele donor) {}
    public static Optional<Change> plan(Genome recipient, Genome donor, Gene gene) {
        Allele allele = donor.expressed(gene);
        if (recipient.alleleId(gene,0) == allele.id() || recipient.alleleId(gene,1) == allele.id()) return Optional.empty();
        int slot = recipient.allele(gene,0).dominance() <= recipient.allele(gene,1).dominance() ? 0 : 1;
        return Optional.of(new Change(recipient.with(gene,slot,allele.id()),slot,allele));
    }
}
