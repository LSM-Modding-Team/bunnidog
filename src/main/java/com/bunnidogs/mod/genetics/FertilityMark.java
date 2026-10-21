package com.bunnidogs.mod.genetics;

/** An inherited phenotype, independent of love, age, breeding recovery or bred Bloom points. */
public enum FertilityMark {
    NONE("An unmarked tail tip"),
    PROLIFIC("One open rose-colored tail-tip rosette"),
    SHY("One small closed green tail-tip bud"),
    SWARM("Three small rose-colored tail-tip rosettes"),
    STARBORN("One violet star-shaped tail-tip flower");

    private final String description;
    FertilityMark(String description) { this.description = description; }
    public String description() { return Texts.t(this.description); }
    public static FertilityMark of(Genome genome) { return of(genome.expressed(Gene.FERTILITY)); }
    public static FertilityMark of(Allele allele) {
        return switch (allele.name()) {
            case "prolific" -> PROLIFIC;
            case "shy" -> SHY;
            case "swarm" -> SWARM;
            case "starborn" -> STARBORN;
            default -> NONE;
        };
    }
}
