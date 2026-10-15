package com.bunnidogs.mod.genetics;

/**
 * The phenotype: what a {@link Genome} actually looks like / does, after dominance is applied.
 * Multipliers are relative to the bunnidog's base stats (which live in the entity class, not here).
 */
public record Traits(float earScale, int coat, float legScale, float buildScale,
                     Temperament temperament, float fertility) {

    public static Traits of(Genome genome) {
        return new Traits(
                genome.expressed(Gene.EARS).value(),
                genome.expressed(Gene.COAT).id(),
                genome.expressed(Gene.LEGS).value(),
                genome.expressed(Gene.BUILD).value(),
                Temperament.fromCode(Math.round(genome.expressed(Gene.TEMPERAMENT).value())),
                genome.expressed(Gene.FERTILITY).value());
    }

    /** Coat allele name, i.e. the texture suffix: textures/entity/bunnidog_&lt;name&gt;.png */
    public String coatName() {
        return Gene.COAT.allele(coat).name();
    }

    // ---- mechanical effects (Phase 1) ----

    /** Build: chonkier = tougher. 0.85 -> 0.78x, 1.6 -> 1.9x. */
    public float healthMultiplier() {
        return 1.0F + (buildScale - 1.0F) * 1.5F;
    }

    /** Legs: longer = faster. */
    public float speedMultiplier() {
        return 1.0F + (legScale - 1.0F) * 0.6F;
    }

    /** Legs: longer = jumps higher (more strongly than speed). */
    public float jumpMultiplier() {
        return 1.0F + (legScale - 1.0F) * 1.2F;
    }

    /** Ears: bigger = notices things from further away (scales the mob's follow/awareness range). */
    public float hearingMultiplier() {
        return earScale;
    }
}
