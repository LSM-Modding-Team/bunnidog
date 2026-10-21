import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;
import java.util.*;

/** Full-schema contracts and exact joint inheritance, independent of Minecraft. */
public final class CognitionCheck {
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void near(double actual, double expected) { require(Math.abs(actual - expected) < 1e-10, actual + " != " + expected); }
    private static Genome fixedMind(int id) {
        Genome genome = Genome.DEFAULT;
        for (var gene : CognitiveGene.values()) for (int slot = 0; slot < 2; slot++) genome = genome.with(gene, slot, id);
        return genome;
    }
    private static Pedigree.Entry entry(Genome genome) {
        return new Pedigree.Entry(genome, Optional.of(Genome.DEFAULT), Optional.of(Genome.DEFAULT),
                Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000987")), Optional.of("BD-123ABC"), Optional.of(Stats.ZERO), false);
    }
    public static void main(String[] args) {
        var body = Genome.parse("33-22-33-33-33-33").orElseThrow();
        require(body.bodyCode().equals("33-22-33-33-33-33-00") && body.mindPacked() == CognitiveGene.LEGACY_HELPER,
                "Six-pair migration must retain every body copy and add the uniform helper mind");
        require(Genome.parse(body.bodyCode()).orElseThrow().equals(body), "Seven-pair migration must agree with six-pair migration");
        for (var task : List.of(CapabilityRules.Task.HARVEST, CapabilityRules.Task.DELIVERY, CapabilityRules.Task.PLANTING, CapabilityRules.Task.NURSERY))
            require(CapabilityRules.potential(body.mind(), task) == CapabilityRules.Potential.INDEPENDENT, "Legacy helper must retain familiar work: " + task);
        require(CapabilityRules.potential(body.mind(), CapabilityRules.Task.PLANNING) == CapabilityRules.Potential.LIMITED, "Migration must not grant a civic planner");
        require(fixedMind(0).mind().equals(new MindProfile(.25, .25, .25, .25)), "Lowest genotype must impose real limits");
        require(fixedMind(3).mind().equals(new MindProfile(4, 4, 4, 4)), "Highest combined potential must be attainable");
        var mixed = body;
        for (var gene : CognitiveGene.values()) {
            require(Locus.all().get(gene.index()) == gene, "Saved full-locus order");
            for (var allele : gene.alleles()) for (int slot = 0; slot < 2; slot++) {
                var changed = mixed.with(gene, slot, allele.id());
                require(changed.packed() == body.packed(), "Cognitive bits must never overwrite body genes");
                require(Genome.parse(changed.toString()).orElseThrow().equals(changed), "Complete schema must round-trip every allele/copy");
                require(Genome.fromSynced(changed.syncLow(), changed.syncHigh(), changed.mindPacked()).equals(changed), "Three-word sync must retain all 88 bits");
                require(changed.with(Gene.COAT, 0, 0).mindPacked() == changed.mindPacked(), "Body edits must retain the mind");
            }
            mixed = mixed.with(gene, 0, 0).with(gene, 1, 3);
        }
        var sample = new Tissue(mixed, Stats.of(1, 2, 3, 4, 5));
        require(Tissue.parse(sample.toString()).orElseThrow().equals(sample), "Tissue must retain a complete mind");
        var infusion = new Infusion(mixed, sample.stats(), 4.5);
        require(Infusion.parse(infusion.toString()).orElseThrow().equals(infusion), "Frozen infusion must retain a complete mind");
        var book = new Pedigree(); book.add(entry(mixed));
        var names = new LineageNames(); names.rename(mixed, "Luna Perú"); book.setLineageNames(names);
        require(RecordFilter.indices(book, "mind:memory=2.125 mind:encyclopedic").equals(List.of(0)), "Capacity and contributing-allele filters combine without treating mind as a bred stat");
        require(RecordFilter.indices(book, "mind:memory>2.125").isEmpty() && RecordFilter.indices(book, "carried:encyclopedic").isEmpty(),
                "Capacity comparisons are exact; cognitive copies never masquerade as hidden body alleles");
        require(Pedigree.parse(book.toString()).entries().equals(book.entries()), "Parent and animal genomes must not collide with pedigree delimiters");
        require(LineageNames.parse(names.toString()).name(mixed).orElseThrow().equals("Luna Perú"), "Full identity must retain keeper names");
        var oldNames = body.bodyCode() + "~THVuYQ";
        require(LineageNames.parse(oldNames).name(body).orElseThrow().equals("Luna"), "Legacy names must resolve to the same helper genotype");
        var seed = Seed.of(mixed, sample.stats(), fixedMind(2), Stats.ZERO, 3).withMutagen();
        require(!seed.legacyMutation() && Seed.parse(seed.toString()).orElseThrow().equals(seed), "New seeds use all eleven mutation targets");
        var oldSeed = Seed.parse(body.bodyCode() + "/1,2,3,4,5|" + body.bodyCode() + "|3|mutagen").orElseThrow();
        require(oldSeed.legacyMutation() && Seed.parse(oldSeed.toString()).orElseThrow().equals(oldSeed), "Rewriting or treating an old seed must retain its mutation law");
        require(oldSeed.withMutagen().legacyMutation() && new Cross(oldSeed).seed().equals(oldSeed), "Actual-seed comparisons must retain the saved law");
        for (String invalid : List.of("g3." + body.bodyCode() + ".11-11-11-11", "g2." + body.bodyCode(),
                "g2." + body.bodyCode() + ".11-11-11-ff", "g2.00-00-00-00-00-00.11-11-11-11", "g2." + body.bodyCode() + ".11-11-11-11.extra"))
            require(Genome.parse(invalid).isEmpty(), "Malformed/unknown schema must not become a default genome: " + invalid);
        require(Genome.containsUnsupportedSchema(book + ";g3.opaque<" + body) && !Genome.containsUnsupportedSchema(book.toString()), "Protect future archive entries without blocking g2");
        for (var gene : CognitiveGene.values()) {
            var donor = Genome.DEFAULT.with(gene, 0, 0).with(gene, 1, 3);
            var first = Splicing.plan(Genome.DEFAULT, donor, gene, 0).orElseThrow();
            var second = Splicing.plan(Genome.DEFAULT, donor, gene, 1).orElseThrow();
            require(first.genome().alleleId(gene, first.slot()) == 0 && second.genome().alleleId(gene, second.slot()) == 3,
                    "Cognitive splices must use the chosen donor copy, never fictitious dominance");
            require(first.genome().packed() == Genome.DEFAULT.packed() && first.genome().mindPacked() != second.genome().mindPacked(), "Mind splicing preserves body but changes full identity");
        }
        require(Purebreds.eligibility(entry(mixed)) == Purebreds.Result.MIXED && Purebreds.fixedCount(mixed) == 7,
                "Registry tests all eleven pairs while body qualifications still use seven");
        require(PedigreeSignal.matchingPairs(mixed, body) == 7 && PedigreeSignal.signal(7, false) == 15,
                "Legacy detector circuits deliberately ignore cognitive pairs");
        var cross = new Cross(sample, new Tissue(mixed, Stats.ZERO));
        var forecast = new CapabilityForecast(mixed, mixed);
        require(forecast.outcomes().size() == 256, "Ordered heterozygous transmissions must remain bounded at 256");
        near(forecast.outcomes().stream().mapToDouble(CapabilityForecast.Outcome::chance).sum(), 1);
        for (var task : CapabilityRules.Task.values()) {
            double independent = 0, supported = 0;
            for (int code = 0; code < 256; code++) {
                // Independent oracle directly builds the four possible ordered parental transmissions at each locus.
                int mindWord = 0;
                for (int locus = 0; locus < 4; locus++) {
                    int bits = (code >>> (2 * locus)) & 3;
                    mindWord |= (bits % 2 == 0 ? 0 : 3) << (8 * locus);
                    mindWord |= (bits < 2 ? 0 : 3) << (8 * locus + 4);
                }
                var potential = CapabilityRules.potential(new Genome(0, mindWord).mind(), task);
                if (potential == CapabilityRules.Potential.INDEPENDENT) independent += 1.0 / 256;
                if (potential == CapabilityRules.Potential.SUPPORTED) supported += 1.0 / 256;
            }
            near(forecast.chance(task, CapabilityRules.Potential.INDEPENDENT), independent);
            near(forecast.chance(task, CapabilityRules.Potential.SUPPORTED), supported);
        }
        near(forecast.fixedMindChance(), 1.0 / 16);
        near(cross.fullFixedPupChance(), cross.fixedPupChance() / 16);
        var rng = new Random(1993); var seen = EnumSet.noneOf(CognitiveGene.class); int mindMutations = 0;
        for (int i = 0; i < 2200; i++) {
            var result = Genome.breed(Genome.DEFAULT, Genome.DEFAULT, rng, 1);
            int differences = 0;
            for (var locus : Locus.all()) for (int copy = 0; copy < 2; copy++)
                if (result.genome().alleleId(locus, copy) != Genome.DEFAULT.alleleId(locus, copy)) differences++;
            require(differences == 1 && result.mutated(), "Exactly one mutation, never one per locus");
            if (result.mutatedGene() instanceof CognitiveGene gene) { seen.add(gene); mindMutations++; }
            require(Genome.breed(Genome.DEFAULT, Genome.DEFAULT, rng, 1, true).mutatedGene() instanceof Gene, "Legacy seeds must not acquire cognitive mutations");
        }
        require(seen.size() == 4 && mindMutations > 650 && mindMutations < 950, "All cognitive mutation targets must be reachable with the documented 4/11 share");
        for (var lineage : Lineage.values()) {
            var minds = new HashSet<Integer>();
            for (int i = 0; i < 100; i++) minds.add(Genome.randomFounder(lineage, rng).mindPacked());
            require(minds.size() > 40, "New founders need a varied mind in every lineage: " + lineage);
        }
        require(Learning.parse(Learning.EMPTY.toString()).orElseThrow().equals(Learning.EMPTY)
                && Learning.parse(Learning.LEGACY_FAMILIAR.toString()).orElseThrow().equals(Learning.LEGACY_FAMILIAR), "Personal knowledge has its own bounded schema");
        require(Learning.parse("l2.6-6-6-6").isEmpty() && Learning.parse("l1.7-0-0-0").isEmpty(), "Unknown or out-of-range lessons must not become mastery");
        var practice = Learning.EMPTY.practice(Genome.DEFAULT.mind(), Learning.Routine.HARVEST);
        require(practice.progress(Learning.Routine.HARVEST) == 5 && !practice.knows(Learning.Routine.HARVEST), "A practical helper needs real repeated practice");
        practice = practice.practice(Genome.DEFAULT.mind(), Learning.Routine.HARVEST);
        require(practice.independent(Genome.DEFAULT.mind(), Learning.Routine.HARVEST)
                && !practice.independent(fixedMind(0).mind(), Learning.Routine.HARVEST), "Knowledge cannot replace genetic capacity");
        require(practice.progress(Learning.Routine.DELIVERY) == 0 && Learning.EMPTY.practice(fixedMind(0).mind(), Learning.Routine.HARVEST).equals(Learning.EMPTY),
                "A lesson neither uploads unrelated routines nor bypasses a genuine limit");
        require(Learning.EMPTY.practice(fixedMind(3).mind(), Learning.Routine.HARVEST).knows(Learning.Routine.HARVEST), "Stronger retention and communication learn more quickly");
        System.out.println("Cognition OK: full codecs, legacy law, dosage, exact joint inheritance, single mutation, personal learning and explicit body signals.");
    }
}
