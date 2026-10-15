package com.bunnidogs.mod.genetics;

/**
 * One variant of a {@link Gene}. Alleles are defined in code as a table inside {@link Gene}; a genome only stores
 * the allele's {@code id} (its index in that table, 0..15).
 *
 * <p>Three kinds of allele, by how they enter the population:
 * <ul>
 *   <li><b>common</b> ({@code weight > 0}): part of every ordinary founder;</li>
 *   <li><b>rare</b> ({@code weight == 0}, no lineage): mutation-only, a brand-new allele;</li>
 *   <li><b>wild</b> ({@code weight == 0}, has a lineage): carried only by the founders of that {@link Lineage}.
 *       Never produced by mutation, so it exists nowhere else unless a breeder moves it.</li>
 * </ul>
 *
 * @param id        index in the gene's table (stable! it is saved to disk, never reorder existing alleles)
 * @param name      lowercase name, also used for texture names (coat) and debug output
 * @param dominance higher wins; unique within a gene so the expressed allele is always well defined. Not saved, so
 *                  ranks may be renumbered freely (the current scheme: common 1..3, wild 10+, rare 50+, "hidden wild" 0)
 * @param value     what the allele means; the gene decides how to read it (scale factor, code, ...)
 * @param weight    how common it is in ordinary random founders. 0 = rare or wild (see above)
 * @param lineage   the wild lineage that owns this allele, or {@code null} for common and rare alleles
 */
public record Allele(int id, String name, int dominance, float value, int weight, Lineage lineage) {
    /** Mutation-only: not in ordinary founders, not owned by a lineage. These are what mutations turn into. */
    public boolean isRare() {
        return weight == 0 && lineage == null;
    }

    /** Owned by a wild lineage: only that lineage's founders carry it; mutation never produces it. */
    public boolean isWild() {
        return lineage != null;
    }

    /** Part of ordinary founders. */
    public boolean isCommon() {
        return weight > 0;
    }
}
