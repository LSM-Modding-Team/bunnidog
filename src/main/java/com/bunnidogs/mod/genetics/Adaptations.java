package com.bunnidogs.mod.genetics;

/** Environmental benefits follow expressed inherited alleles, rather than a founder's birthplace. */
public final class Adaptations {
    private Adaptations() {}
    public static boolean fireProof(Genome genome) { return genome.expressed(Gene.COAT).name().equals("ember"); }
    public static boolean coldProof(Genome genome) {
        return genome.expressed(Gene.COAT).name().equals("snow") || genome.expressed(Gene.BUILD).name().equals("woolly");
    }
    public static float fallMultiplier(Genome genome) { return genome.expressed(Gene.LEGS).name().equals("hoofed") ? .5F : 1F; }
    public static String explanation(Gene gene, Allele allele) {
        if (gene == Gene.COAT && allele.name().equals("ember")) return "Immune to fire and lava.";
        if (gene == Gene.BUILD && allele.name().equals("woolly"))
            return "Does not freeze in powder snow. Healthy adults yield two wool with shears every ten minutes.";
        if (gene == Gene.COAT && allele.name().equals("snow"))
            return "Does not freeze in powder snow.";
        if (gene == Gene.LEGS && allele.name().equals("hoofed")) return "Takes half the usual fall damage.";
        return "";
    }
}
