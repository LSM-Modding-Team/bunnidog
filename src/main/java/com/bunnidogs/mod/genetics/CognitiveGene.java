package com.bunnidogs.mod.genetics;

import java.util.List;
import java.util.random.RandomGenerator;

/** Four diploid loci, separately packed from the eight body loci. Both copies contribute equally. */
public enum CognitiveGene implements Locus {
    MEMORY("memory", "Memory",
            spec("fleeting", .25, 0, 0, 0, 3),
            spec("familiar", 1.5, .25, 0, .25, 6),
            spec("retentive", 2.5, .25, .25, 0, 3),
            spec("encyclopedic", 3, .5, 0, .25, 1)),
    REASONING("reasoning", "Reasoning",
            spec("immediate", 0, .25, 0, 0, 3),
            spec("practical", .25, 1.5, .25, 0, 6),
            spec("inventive", 0, 2.5, .25, .25, 3),
            spec("strategic", .5, 3, .5, .5, 1)),
    HANDLING("handling", "Handling",
            spec("impulsive", 0, 0, .25, 0, 3),
            spec("careful", .25, 0, 1.5, .25, 6),
            spec("deft", 0, .25, 2.5, .25, 3),
            spec("dexterous", .25, .25, 3, .25, 1)),
    COMMUNICATION("communication", "Communication",
            spec("solitary", 0, 0, 0, .25, 3),
            spec("attentive", .25, .25, 0, 1.5, 6),
            spec("expressive", .25, 0, .25, 2.5, 3),
            spec("eloquent", .25, .25, .5, 3, 1));

    public static final int COUNT = 4;
    /** One deterministic, heritable profile for every pre-cognition genome. No random migration. */
    public static final int LEGACY_HELPER = 0x11111111;
    private record Spec(String key, MindProfile contribution, int weight) {}
    private static Spec spec(String key, double memory, double reasoning, double handling, double communication, int weight) {
        return new Spec(key, new MindProfile(memory, reasoning, handling, communication), weight);
    }
    private final String key, displayName;
    private final List<CognitiveAllele> alleles;
    private final int weight;
    CognitiveGene(String key, String displayName, Spec... specs) {
        this.key = key; this.displayName = displayName;
        var table = new java.util.ArrayList<CognitiveAllele>();
        int total = 0;
        for (int id = 0; id < specs.length; id++) {
            var spec = specs[id];
            table.add(new CognitiveAllele(id, spec.key, spec.contribution, spec.weight));
            total += spec.weight;
        }
        if (table.size() > 16 || total <= 0) throw new IllegalStateException(key);
        alleles = List.copyOf(table); weight = total;
    }
    @Override public String key() { return key; }
    @Override public String displayName() { return displayName; }
    public List<CognitiveAllele> alleles() { return alleles; }
    public CognitiveAllele allele(int id) { return alleles.get(id); }
    @Override public int alleleCount() { return alleles.size(); }
    @Override public int randomCommonAlleleId(RandomGenerator rng) {
        int roll = rng.nextInt(weight);
        for (var allele : alleles) if ((roll -= allele.founderWeight()) < 0) return allele.id();
        throw new IllegalStateException(key);
    }
    /** A mutation changes one copy; higher potential remains attainable through ordinary founders and breeding. */
    @Override public int randomMutationAlleleId(int currentId, RandomGenerator rng) {
        int roll = rng.nextInt(alleles.size() - 1);
        return roll >= currentId ? roll + 1 : roll;
    }
}
