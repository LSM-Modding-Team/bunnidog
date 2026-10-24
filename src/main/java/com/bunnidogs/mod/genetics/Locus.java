package com.bunnidogs.mod.genetics;

import java.util.List;
import java.util.random.RandomGenerator;

/** Stable full-genotype order. Body loci retain their original IDs and dominance rules. */
public sealed interface Locus permits Gene, CognitiveGene {
    int COUNT = 12;
    String key();
    String displayName();
    int alleleCount();
    int randomCommonAlleleId(RandomGenerator rng);
    int randomMutationAlleleId(int currentId, RandomGenerator rng);

    default int index() {
        return this instanceof Gene body ? body==Gene.WINGS ? 11 : body.ordinal() : 7 + ((CognitiveGene) this).ordinal();
    }

    static List<Locus> all() { return Order.ALL; }

    final class Order {
        private Order() {}
        private static final List<Locus> ALL = List.of(Gene.EARS, Gene.COAT, Gene.LEGS, Gene.BUILD,
                Gene.TEMPERAMENT, Gene.FERTILITY, Gene.DIET, CognitiveGene.MEMORY,
                CognitiveGene.REASONING, CognitiveGene.HANDLING, CognitiveGene.COMMUNICATION, Gene.WINGS);
    }
}
