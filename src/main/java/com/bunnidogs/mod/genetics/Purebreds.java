package com.bunnidogs.mod.genetics;

/** Registry rules. A stable line fixes all seven genes, including Diet; it need not match a wild lineage. */
public final class Purebreds {
    private Purebreds() {}

    public enum Result { ADDED, ALREADY, FULL, PUP, PARENTS, MIXED }

    public static int fixedCount(Genome genome) {
        int fixed = 0;
        for (Gene gene : Gene.values()) if (genome.isHomozygous(gene)) fixed++;
        return fixed;
    }

    public static String describe(Genome genome) {
        java.util.List<String> mixed = new java.util.ArrayList<>();
        for (Gene gene : Gene.values()) if (!genome.isHomozygous(gene)) mixed.add(Plain.geneName(gene));
        return fixedCount(genome) + Texts.t(" of ") + Gene.COUNT + Texts.t(" gene pairs fixed. ")
                + (mixed.isEmpty() ? Texts.t("A mate with this same genome breeds true before mutation; bred stats still drift. ")
                        : Texts.t("Mixed pairs: ") + String.join(", ", mixed) + Texts.t(". The Genetics Table shows the odds of fixing both copies. "))
                + Texts.t("The Purebred Registry accepts adults with recorded birth parents and all seven pairs fixed, including Diet.");
    }

    public static Result eligibility(Pedigree.Entry entry) {
        if (entry.pup()) return Result.PUP;
        if (entry.mother().isEmpty() || entry.id().isEmpty() || entry.stats().isEmpty()) return Result.PARENTS;
        return fixedCount(entry.genome()) == Gene.COUNT ? Result.ADDED : Result.MIXED;
    }

    /** Register one distinct, stable genome. Keep its first representative and its historical snapshot. */
    public static Result add(Pedigree registry, Pedigree.Entry entry) {
        Result eligible = eligibility(entry);
        if (eligible != Result.ADDED) return eligible;
        for (Pedigree.Entry known : registry.entries()) {
            if (known.genome().equals(entry.genome()) || known.id().equals(entry.id())) return Result.ALREADY;
        }
        if (registry.size() >= Pedigree.MAX_ENTRIES) return Result.FULL;
        registry.add(entry);
        return Result.ADDED;
    }

    /** Uses the existing pedigree snapshot format; invalid, duplicate or unfinished records are ignored. */
    public static Pedigree read(String text) {
        Pedigree registry = new Pedigree();
        for (Pedigree.Entry entry : Pedigree.parse(text).entries()) add(registry, entry);
        return registry;
    }
}
