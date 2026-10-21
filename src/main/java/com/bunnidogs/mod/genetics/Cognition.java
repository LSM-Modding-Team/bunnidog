package com.bunnidogs.mod.genetics;

import java.util.random.RandomGenerator;

/** Dosage/codominance: average the two copies at each locus, then sum their overlapping contributions. */
public final class Cognition {
    private Cognition() {}
    public static MindProfile of(Genome genome) {
        double memory = 0, reasoning = 0, handling = 0, communication = 0;
        for (var gene : CognitiveGene.values()) for (int slot = 0; slot < 2; slot++) {
            var c = genome.allele(gene, slot).contribution();
            memory += c.memory() / 2; reasoning += c.reasoning() / 2;
            handling += c.handling() / 2; communication += c.communication() / 2;
        }
        return new MindProfile(memory, reasoning, handling, communication);
    }
    public static Genome founder(Genome body, RandomGenerator rng) {
        Genome genome = body;
        for (var gene : CognitiveGene.values()) for (int slot = 0; slot < 2; slot++)
            genome = genome.with(gene, slot, gene.randomCommonAlleleId(rng));
        return genome;
    }
}
