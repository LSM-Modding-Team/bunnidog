package com.bunnidogs.mod.genetics;

/** Localized cognitive presentation. Saved keys and numeric allele IDs never pass through translation. */
public final class MindText {
    private MindText() {}
    public static String gene(CognitiveGene gene) { return Texts.t(gene.displayName()); }
    public static String allele(CognitiveAllele allele) { return Texts.t(allele.key()); }
    public static String locus(Locus locus) { return locus instanceof Gene body ? Plain.geneName(body) : gene((CognitiveGene) locus); }
    public static String pair(Genome genome, CognitiveGene gene) {
        return allele(genome.allele(gene, 0)) + " + " + allele(genome.allele(gene, 1));
    }
    public static String contribution(CognitiveAllele allele) {
        var c = allele.contribution();
        return Texts.t("One copy: memory %.2f, reasoning %.2f, handling %.2f, communication %.2f; average both copies, then add the four loci.",
                c.memory(), c.reasoning(), c.handling(), c.communication());
    }
    public static String summary(Genome genome) {
        var mind = genome.mind();
        return Texts.t("Mind: memory %.2f, reasoning %.2f, handling %.2f, communication %.2f (0–4).",
                mind.memory(), mind.reasoning(), mind.handling(), mind.communication());
    }
}
