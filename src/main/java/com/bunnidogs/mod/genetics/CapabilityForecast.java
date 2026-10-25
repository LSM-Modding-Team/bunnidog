package com.bunnidogs.mod.genetics;

import java.util.LinkedHashMap;
import java.util.List;

/** Exact joint capacity odds, at most 256 transmissions. Never samples RNG or enumerates body crosses. */
public final class CapabilityForecast {
    public record Outcome(int mindPacked, MindProfile mind, double chance) {}
    private final List<Outcome> outcomes;
    public CapabilityForecast(Genome mother,Genome father) {this(mother,father,false);}
    public CapabilityForecast(Cross cross) {this(cross.a().genome(),cross.b().genome(),cross.seed().captured());}
    private CapabilityForecast(Genome mother, Genome father,boolean captured) {
        if(captured) {outcomes=List.of(new Outcome(mother.mindPacked(),mother.mind(),1));return;}
        var counts = new LinkedHashMap<Integer, Integer>();
        for (int roll = 0; roll < 256; roll++) {
            Genome child = new Genome(0, 0);
            for (var gene : CognitiveGene.values()) {
                int transmission = (roll >>> (gene.ordinal() * 2)) & 3;
                child = child.with(gene, 0, mother.alleleId(gene, transmission & 1))
                        .with(gene, 1, father.alleleId(gene, transmission >>> 1));
            }
            counts.merge(child.mindPacked(), 1, Integer::sum);
        }
        outcomes = counts.entrySet().stream().map(entry -> {
            var genome = new Genome(0, entry.getKey());
            return new Outcome(genome.mindPacked(), genome.mind(), entry.getValue() / 256.0);
        }).toList();
    }
    public List<Outcome> outcomes() { return outcomes; }
    /** Independent and supported-only odds are disjoint; unsupported remainder is genuinely limited. */
    public double chance(CapabilityRules.Task task, CapabilityRules.Potential potential) {
        return outcomes.stream().filter(o -> CapabilityRules.potential(o.mind(), task) == potential)
                .mapToDouble(Outcome::chance).sum();
    }
    public double minimum(CognitiveGene axis) { return outcomes.stream().mapToDouble(o -> o.mind().get(axis)).min().orElseThrow(); }
    public double maximum(CognitiveGene axis) { return outcomes.stream().mapToDouble(o -> o.mind().get(axis)).max().orElseThrow(); }
    public double fixedMindChance() {
        return outcomes.stream().filter(o -> java.util.Arrays.stream(CognitiveGene.values()).allMatch(new Genome(0, o.mindPacked())::isHomozygous))
                .mapToDouble(Outcome::chance).sum();
    }
}
