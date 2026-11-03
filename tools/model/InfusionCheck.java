import com.bunnidogs.mod.genetics.*;
import java.util.List;
import java.util.Random;

/** Focused rule and real inheritance checks; no claim of measured Survival production time. */
public final class InfusionCheck {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static Genome fixed(Genome genome, Gene gene, String name) {
        int id = gene.alleleNamed(name).id();
        return genome.with(gene, 0, id).with(gene, 1, id);
    }

    private static Juice.Spec effect(Genome genome, Stats stats, double quality, String name) {
        return Juice.effects(genome, stats, quality).stream().filter(s -> s.id().equals("minecraft:" + name))
                .findFirst().orElseThrow();
    }

    private static void potency() {
        var feral = fixed(Genome.DEFAULT, Gene.TEMPERAMENT, "feral");
        require(effect(feral, Stats.ZERO, 0, "strength").amplifier() == 0, "Unbred plain founders retain Strength I");
        require(effect(feral, Stats.of(116), 0, "strength").amplifier() == 29, "A real Q0 Vitality-116 individual reaches XXX");
        require(effect(feral, Stats.of(112), 4, "strength").amplifier() == 29, "Carrier quality and Vitality are alternatives");
        require(effect(feral, Stats.of(60), 4, "strength").amplifier() == 16, "Developed intermediate lines reach XVII");
        var ember = fixed(Genome.DEFAULT, Gene.COAT, "ember");
        require(effect(ember, Stats.of(116), 0, "strength").amplifier() == 29
                && effect(ember, Stats.of(116), 0, "strength").ticks() == 900,
                "Coat Strength gains potency without silently gaining a stat duration bonus");
        var duplicate = fixed(ember, Gene.TEMPERAMENT, "feral");
        var merged = Juice.effects(duplicate, Stats.of(116), 0).stream().filter(s -> s.id().endsWith(":strength")).toList();
        require(merged.size() == 1 && merged.getFirst().amplifier() == 29 && merged.getFirst().ticks() == 1800,
                "Duplicate Strength takes the strongest/longest contribution once");
        require(effect(Genome.DEFAULT, Stats.of(0, 0, 0, 112), 0, "regeneration").amplifier() == 3
                && effect(Genome.DEFAULT, Stats.of(0, 0, 0, 240), 0, "regeneration").amplifier() == 4,
                "Recovery uses Senses and its independent exponential thresholds");
        require(effect(Genome.DEFAULT, Stats.of(116), 0, "regeneration").amplifier() == 0,
                "Strength investment cannot stand in for recovery investment");
        var stocky = fixed(Genome.DEFAULT, Gene.BUILD, "stocky");
        require(effect(stocky, Stats.of(116), 0, "resistance").amplifier() == 1
                && effect(stocky, Stats.of(116), 1, "resistance").amplifier() == 2
                && effect(stocky, Stats.of(Stats.LIMIT), 1000, "resistance").amplifier() == 2,
                "Resistance retains fractional potency contributions and its existing III limit");
        require(effect(fixed(Genome.DEFAULT, Gene.COAT, "snow"), Stats.of(116), 0, "absorption").amplifier() == 7,
                "Snow Absorption shares Vitality potency, independently of its duration");
        require(effect(fixed(Genome.DEFAULT, Gene.LEGS, "long"), Stats.of(0, 64), 0, "speed").amplifier() == 4
                && effect(fixed(Genome.DEFAULT, Gene.LEGS, "springy"), Stats.of(0, 0, 64), 0, "jump_boost").amplifier() == 4
                && effect(fixed(Genome.DEFAULT, Gene.FERTILITY, "swarm"), Stats.of(0, 0, 0, 0, 64), 0, "luck").amplifier() == 4,
                "Movement and Luck use their own quarter-stat contributions");
        var maximum = Stats.of(Stats.LIMIT, Stats.LIMIT, Stats.LIMIT, Stats.LIMIT, Stats.LIMIT);
        require(Juice.effects(Genome.DEFAULT, maximum, 0).stream().noneMatch(s -> s.id().endsWith(":strength")),
                "Stats cannot supply an effect absent from the expressed genome");
        for (var defs : Juice.DEFS.values()) for (var def : defs) {
            var plain = Juice.spec(def, 0, Stats.ZERO);
            var negative = Juice.spec(def, 0, Stats.of(-100, -100, -100, -100, -100));
            var developed = Juice.spec(def, 0, maximum);
            require(plain.equals(negative), "Negative stats add no potion penalty: " + def.id());
            require(developed.amplifier() >= plain.amplifier() && developed.amplifier() <= 255,
                    "Potency stays monotone within native representation: " + def.id());
            if (def.maxAmp() == 0) require(developed.amplifier() == 0, "Fixed utilities/drawbacks never scale");
            if (def.stat() == null) require(developed.ticks() == plain.ticks(), "Statless durations stay unchanged");
        }
        require(effect(feral, maximum, 0, "strength").amplifier() == 255
                && effect(feral, Stats.of(120), 0, "strength").amplifier() == 30,
                "XXX remains a reference, not a new effect cap");
    }

    private static void references() {
        for (var line : List.of(ExpeditionInfusions.LATE_STRENGTH, ExpeditionInfusions.ENDGAME_STRENGTH,
                ExpeditionInfusions.LATE_RECOVERY, ExpeditionInfusions.ENDGAME_RECOVERY)) {
            Stats parents = Stats.ZERO;
            for (int generation = 0; generation < line.richGenerations() + line.plainGenerations(); generation++) {
                double feed = generation < line.richGenerations() ? GuavaTier.NETHERITE.quality() : 0;
                var seed = Seed.of(line.genome(), parents, line.genome(), parents, feed);
                var retained = seed.hatch(0, 0, new Random(0)).stream()
                        .filter(p -> p.genome().equals(line.genome())).findFirst().orElseThrow();
                parents = retained.stats();
            }
            require(parents.equals(line.parents()), "Actual selected hatchlings reproduce the documented donor stats");
            var bottle = line.bottle();
            require(line.seed().quality() == 0 && bottle.quality() == 1 && bottle.genome().equals(line.genome()),
                    "Reference bottles use plain-fed parents, one actual unmutated roll and a golden carrier");
            require(line.parents().get(Stat.BLOOM) == 0, "No invented high Bloom or inherited archive quality");
            require(Infusion.parse(bottle.toString()).orElseThrow().equals(bottle), "Frozen reference records round-trip");
        }
        var strength = ExpeditionInfusions.ENDGAME_STRENGTH.bottle();
        var recovery = ExpeditionInfusions.ENDGAME_RECOVERY.bottle();
        require(strength.stats().get(Stat.VITALITY) == 116 && recovery.stats().get(Stat.SENSES) == 240,
                "Distinct donor investments produce the actual 116/240 reference individuals");
        require(effect(strength.genome(), strength.stats(), strength.quality(), "strength").amplifier() == 29
                && effect(recovery.genome(), recovery.stats(), recovery.quality(), "regeneration").amplifier() == 4,
                "Endgame retains Strength XXX and specialised Regeneration V");
        require(effect(strength.genome(), strength.stats(), 1, "strength").ticks()
                >= 2 * effect(recovery.genome(), recovery.stats(), 1, "regeneration").ticks(),
                "Endgame's one-strength/two-recovery stock ratio matches effect durations");
    }

    private static void savesAndCapture() {
        var legacy = new Infusion(ExpeditionInfusions.ENDGAME_STRENGTH.genome(), Stats.of(80, 70, 75, 80, 60), 116);
        String data = legacy.toString();
        var loaded = Infusion.parse(data).orElseThrow();
        loaded.specs();
        require(loaded.toString().equals(data) && effect(loaded.genome(), loaded.stats(), loaded.quality(), "strength").amplifier() == 49,
                "Legacy/admin quality and stats stay intact and use the current rules without pretending to be earned");
        var captured = Seed.captured(legacy.genome(), Stats.of(116, -4, 12, 3, 0));
        var exact = Infusion.roll(captured, new Random(13));
        require(exact.genome().equals(captured.mother()) && exact.stats().equals(captured.motherStats()) && exact.quality() == 0,
                "Paid captured individuals preserve exact identity, stats and quality zero");
        var seed = ExpeditionInfusions.ENDGAME_STRENGTH.seed();
        var plain = Infusion.roll(seed, new Random(7));
        var rich = Infusion.roll(seed, GuavaTier.NETHERITE, new Random(7));
        require(plain.genome().equals(rich.genome()) && plain.stats().equals(rich.stats()) && rich.quality() == 4 && seed.quality() == 0,
                "The carrier changes potency quality only, never the seed, genes or inherited stat drift");
    }

    public static void main(String[] args) {
        potency(); references(); savesAndCapture();
        System.out.println("Infusions: stat potency, actual donor hatchlings, saved records and exact capture passed.");
    }
}
