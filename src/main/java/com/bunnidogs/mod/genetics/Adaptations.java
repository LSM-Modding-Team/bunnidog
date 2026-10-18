package com.bunnidogs.mod.genetics;

/** Environmental benefits follow expressed inherited alleles, rather than a founder's birthplace. */
public final class Adaptations {
    private Adaptations() {}
    public static boolean fireProof(Genome genome) { return genome.expressed(Gene.COAT).name().equals("ember"); }
    public static boolean coldProof(Genome genome) {
        return genome.expressed(Gene.COAT).name().equals("snow") || genome.expressed(Gene.BUILD).name().equals("woolly");
    }
    public static boolean snowFooted(Genome genome) { return genome.expressed(Gene.COAT).name().equals("snow"); }
    public static float fallMultiplier(Genome genome) { return genome.expressed(Gene.LEGS).name().equals("hoofed") ? .5F : 1F; }
    public static boolean poisonProof(Genome genome) { return genome.expressed(Gene.COAT).name().equals("moss"); }
    public static boolean cactusProof(Genome genome) { return genome.expressed(Gene.COAT).name().equals("sand"); }
    public static boolean levitationProof(Genome genome) { return genome.expressed(Gene.COAT).name().equals("void"); }
    public static double armor(Genome genome) { return genome.expressed(Gene.COAT).name().equals("slate") ? 4 : 0; }
    public static double knockbackResistance(Genome genome) {
        return switch (genome.expressed(Gene.BUILD).name()) { case "stocky" -> .15; case "chonker" -> .3; default -> 0; };
    }
    public static double stepBonus(Genome genome) { return genome.expressed(Gene.LEGS).name().equals("hoofed") ? .4 : 0; }
    public static double movementEfficiency(Genome genome) {
        return (genome.expressed(Gene.LEGS).name().equals("fleet") ? .25 : 0)
                + (genome.expressed(Gene.BUILD).name().equals("lean") ? .5 : 0);
    }
    public static boolean luminous(Genome genome) { return genome.expressed(Gene.COAT).name().equals("glow"); }
    public static int blockLight(Genome genome, int environmental) { return luminous(genome) ? 15 : environmental; }
    public static String explanation(Gene gene, Allele allele) {
        if (gene == Gene.EARS && allele.name().equals("bat")) return "Healthy adults with Senses +10 can mark nearby ore: sneak-use a compass. At +20, a mineral in the other hand selects its ore family. Six-block range, thirty-second shared recovery.";
        if (gene == Gene.COAT && allele.name().equals("ember")) return "Immune to fire and lava.";
        if (gene == Gene.BUILD && allele.name().equals("woolly"))
            return "Does not freeze in powder snow. Healthy adults yield two wool in their expressed coat's palette with shears every ten minutes.";
        if (gene == Gene.COAT && allele.name().equals("snow"))
            return "Does not freeze in powder snow. Walks on its surface and can climb out; descending and long falls retain native snow collision rules.";
        if (gene == Gene.COAT && allele.name().equals("moss")) return "Immune to poison, but not wither or other magic.";
        if (gene == Gene.COAT && allele.name().equals("sand")) return "Immune to cactus damage; other thorns still hurt.";
        if (gene == Gene.COAT && allele.name().equals("void")) return "Immune to levitation; shulker impacts and falls still hurt.";
        if (gene == Gene.LEGS && allele.name().equals("hoofed")) return "Takes half the usual fall damage. Adds 0.4 step height, letting ordinary adults walk up one-block ledges.";
        if (gene == Gene.LEGS && allele.name().equals("fleet")) return "Reduces slowing from soul sand and honey by 25%; combines with lean build.";
        if (gene == Gene.BUILD && allele.name().equals("lean")) return "Halves slowing from soul sand and honey; combines with fleet legs. Does not prevent cobwebs or Slowness.";
        if (gene == Gene.COAT && allele.name().equals("slate")) return "+4 natural armor against ordinary attacks; armor-bypassing damage still hurts.";
        if (gene == Gene.COAT && allele.name().equals("glow")) return "Self-lit coat in darkness; does not light nearby blocks. Brush healthy adults for one glowstone dust every ten minutes.";
        if (gene == Gene.BUILD && allele.name().equals("stocky")) return "+15% knockback resistance.";
        if (gene == Gene.BUILD && allele.name().equals("chonker")) return "+30% knockback resistance.";
        return "";
    }
}
