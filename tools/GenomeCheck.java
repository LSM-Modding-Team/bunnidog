import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.TreeShape;
import com.bunnidogs.mod.layout.HouseShape;
import com.bunnidogs.mod.layout.PathShape;
import com.bunnidogs.mod.layout.PropShape;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.layout.WarrenShape;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Plain-Java self-check for the genetics package (no Minecraft, no JUnit, no Gradle involvement).
 * Run from the project root:
 *
 *   javac -d /tmp/gcheck src/main/java/com/bunnidogs/mod/genetics/*.java tools/GenomeCheck.java
 *   java -cp /tmp/gcheck GenomeCheck
 *
 * (If javac is missing: java -m jdk.compiler/com.sun.tools.javac.Main -d /tmp/gcheck <same files>)
 * Exits with code 1 if anything fails.
 */
public class GenomeCheck {
    static int checks = 0, failures = 0;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) {
            failures++;
            System.out.println("  FAIL: " + what);
        }
    }

    static void near(double actual, double expected, double tol, String what) {
        check(Math.abs(actual - expected) <= tol, what + " (expected " + expected + " +-" + tol + ", got " + actual + ")");
    }

    static Genome g(String s) {
        return Genome.parse(s).orElseThrow(() -> new IllegalStateException("bad test genome " + s));
    }

    public static void main(String[] args) throws Exception {
        tables();
        packingAndText();
        dominance();
        mendel();
        mutation();
        breeding();
        founders();
        lineages();
        phaseAllele();
        webbedAllele();
        fertilityMarks();
        stats();
        seeds();
        guavaTiers();
        guavaMemory();
        traits();
        determinism();
        texturesExist();
        itemResources();
        seedItemResources();
        seedCropResources();
        guavaTierResources();
        juice();
        juiceResources();
        layouts();
        settlements();
        houses();
        paths();
        props();
        palette();
        towers();
        meetingTree();
        guavaWood();
        guavaPlanks();
        guavaParts();
        uneven();
        devOptions();
        guavaTreeResources();
        calipers();
        pedigree();
        earTag();
        pedigreeTags();
        drops();
        pelts();
        signatures();
        materials();
        plainAndUnits();
        pedigreeV2();
        sheetLayout();
        sheetHover();
        calipersWindow();
        seedWindows();
        pedestal();
        haunch();
        mutagen();
        marrow();
        tissue();
        dust();
        legends();
        wornTag();
        geneticsTable();
        forecast();
        nest();
        nestClaim();
        nestWorldgen();
        nestNight();
        houseWood();
        den();
        System.out.println(checks + " checks, " + failures + " failures");
        System.exit(failures == 0 ? 0 : 1);
    }

    // ---------------------------------------------------------------------------------------------
    static void tables() {
        System.out.println("allele tables");
        for (Gene gene : Gene.values()) {
            Set<Integer> dom = new HashSet<>();
            Set<String> names = new HashSet<>();
            boolean anyCommon = false, anyRare = false;
            for (Allele a : gene.alleles()) {
                check(dom.add(a.dominance()), gene + ": dominance " + a.dominance() + " is not unique");
                check(names.add(a.name()), gene + ": duplicate name " + a.name());
                check(a.name().equals(a.name().toLowerCase()) && !a.name().isBlank(), gene + ": bad name '" + a.name() + "'");
                check(a.weight() >= 0, gene + ": negative weight");
                check(!a.isWild() || a.weight() == 0, gene + ": wild allele " + a.name() + " must have weight 0");
                check(a.isCommon() ^ a.isRare() ^ a.isWild(), gene + ": " + a.name() + " must be exactly one of common/rare/wild");
                check(a.id() == gene.alleles().indexOf(a), gene + ": id != index for " + a.name());
                anyCommon |= a.isCommon();
                anyRare |= a.isRare();
            }
            check(gene.alleleCount() <= Gene.MAX_ALLELES, gene + ": too many alleles for 4 bits");
            check(anyCommon, gene + ": needs at least one common allele");
            check(anyRare, gene + ": needs at least one rare allele (mutation target)");
        }
        check(Gene.COUNT * 8 <= 56, "genome must fit a 24-bit low word and 32-bit high word");
        for (Allele a : Gene.TEMPERAMENT.alleles()) {
            check(Temperament.fromCode(Math.round(a.value())).code() == Math.round(a.value()),
                    "temperament allele " + a.name() + " has no matching Temperament code");
        }
    }

    // ---------------------------------------------------------------------------------------------
    static void packingAndText() {
        System.out.println("packing / text form");
        Random rng = new Random(1);
        for (int i = 0; i < 2000; i++) {
            Genome a = Genome.randomFounder(rng);
            check(Genome.parse(a.toString()).equals(Optional.of(a)), "text round trip " + a);
            check(new Genome(a.packed()).equals(a), "packed round trip " + a);
            check(a.packed() >>> 56 == 0, "packed uses more than 56 bits");
            // The original split at bit 24 now has a signed 32-bit high word.
            int lo = (int) (a.packed() & 0xFFFFFFL);
            int hi = (int) (a.packed() >>> 24);
            check(lo >= 0, "the 24-bit low word stays non-negative");
            check((((hi & 0xFFFFFFFFL) << 24) | lo) == a.packed(), "split/join round trip");
        }
        Genome junk = new Genome(-1L);   // every nibble = 15, out of range for all genes
        for (Gene gene : Gene.values()) {
            for (int slot = 0; slot < 2; slot++) {
                check(junk.alleleId(gene, slot) < gene.alleleCount(), "sanitize left an invalid id in " + gene);
            }
        }
        check(g("00-01-20-00-03-11").toString().equals("00-01-20-00-03-11-00"), "canonical form kept");
        check(Genome.parse("0001200003 11").isPresent(), "separators are optional");
        check(Genome.parse("00_01:20 00-03-11").isPresent(), "mixed separators accepted");
        for (String bad : new String[]{"", "   ", "zz-zz-zz-zz-zz-zz", "00-00-00-00-00", "00-00-00-00-00-00-cc",
                "ff-00-00-00-00-00", "00-00-00-00-00-9x", "00-00-00-00-40-00"}) {
            check(Genome.parse(bad).isEmpty(), "should reject '" + bad + "'");
        }
        check(Genome.parse(null).isEmpty(), "null rejected");
        check(Genome.DEFAULT.toString().equals("00-00-00-00-00-00-00"), "default genome text");
    }

    // ---------------------------------------------------------------------------------------------
    static void dominance() {
        System.out.println("dominance");
        // ears: id 0 standard(dom 3), 1 long(2), 2 short(1), 3 huge(50, rare)
        Genome carrier = g("02-00-00-00-00-00");                  // standard + short
        check(carrier.expressed(Gene.EARS).name().equals("standard"), "standard hides short");
        check(carrier.hidden(Gene.EARS).map(Allele::name).orElse("").equals("short"), "short is the hidden allele");
        check(!carrier.isHomozygous(Gene.EARS), "carrier is heterozygous");

        check(g("12-00-00-00-00-00").expressed(Gene.EARS).name().equals("long"), "long beats short");
        check(g("20-00-00-00-00-00").expressed(Gene.EARS).name().equals("standard"), "slot order does not matter");
        check(g("22-00-00-00-00-00").expressed(Gene.EARS).name().equals("short"), "short shows when homozygous");
        check(g("22-00-00-00-00-00").hidden(Gene.EARS).isEmpty(), "homozygous has nothing hidden");
        check(g("03-00-00-00-00-00").expressed(Gene.EARS).name().equals("huge"), "rare huge is dominant");
        check(carrier.describe(Gene.EARS).equals("Ears: standard (carries short)"), "describe carrier: " + carrier.describe(Gene.EARS));
        check(g("00-00-00-00-00-00").describe(Gene.COAT).equals("Coat: tan"), "describe plain");
        Genome edited = carrier.with(Gene.COAT, 1, 3);
        check(edited.alleleId(Gene.COAT, 1) == 3 && edited.alleleId(Gene.COAT, 0) == 0, "with() edits one slot");
        check(edited.alleleId(Gene.EARS, 1) == 2, "with() leaves other genes alone");
        boolean threw = false;
        try { carrier.with(Gene.EARS, 0, 9); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "with() rejects out-of-range allele");
    }

    // ---------------------------------------------------------------------------------------------
    static void mendel() {
        System.out.println("mendelian inheritance (no mutation)");
        Random rng = new Random(42);
        Genome carrier = g("02-00-00-00-00-00");     // ears: standard + short
        int n = 40000, homoStd = 0, het = 0, homoShort = 0, shownShort = 0;
        for (int i = 0; i < n; i++) {
            Genome child = Genome.breed(carrier, carrier, rng, 0.0).genome();
            int a = child.alleleId(Gene.EARS, 0), b = child.alleleId(Gene.EARS, 1);
            if (a == 0 && b == 0) homoStd++;
            else if (a == 2 && b == 2) homoShort++;
            else het++;
            if (child.expressed(Gene.EARS).name().equals("short")) shownShort++;
        }
        near(homoStd / (double) n, 0.25, 0.012, "carrier x carrier: homozygous standard share");
        near(het / (double) n, 0.50, 0.012, "carrier x carrier: heterozygous share");
        near(homoShort / (double) n, 0.25, 0.012, "carrier x carrier: homozygous short share");
        near(shownShort / (double) n, 0.25, 0.012, "hidden recessive resurfaces in ~1/4 of offspring");

        // two visibly-normal parents can only produce a short-eared child if BOTH carry it
        Genome plain = g("00-00-00-00-00-00");
        for (int i = 0; i < 5000; i++) {
            check(!Genome.breed(plain, carrier, rng, 0.0).genome().expressed(Gene.EARS).name().equals("short"),
                    "short needs two carriers");
            if (failures > 20) return;
        }
        // slot 0 = mother's allele, slot 1 = father's allele
        Genome mom = g("00-00-00-00-00-00"), dad = g("22-11-22-11-22-11");
        for (int i = 0; i < 200; i++) {
            Genome child = Genome.breed(mom, dad, rng, 0.0).genome();
            for (Gene gene : Gene.values()) {
                check(child.alleleId(gene, 0) == 0, "slot 0 must come from the mother (" + gene + ")");
                check(child.alleleId(gene, 1) == dad.alleleId(gene, 0), "slot 1 must come from the father (" + gene + ")");
            }
        }
        // genes are inherited independently (no linkage): among dihybrid crosses all 4 combinations appear
        Genome dihybrid = g("02-02-00-00-00-00");
        int[] combos = new int[4];
        for (int i = 0; i < 20000; i++) {
            Genome child = Genome.breed(dihybrid, g("22-22-00-00-00-00"), rng, 0.0).genome();
            int ears = child.alleleId(Gene.EARS, 0) == 2 ? 1 : 0;
            int coat = child.alleleId(Gene.COAT, 0) == 2 ? 1 : 0;
            combos[ears * 2 + coat]++;
        }
        for (int c : combos) near(c / 20000.0, 0.25, 0.015, "independent assortment of ears/coat");
        // breeding true: two identical homozygous parents always give the same genome
        Genome pure = g("11-22-33-11-22-11");
        for (int i = 0; i < 300; i++) {
            check(Genome.breed(pure, pure, rng, 0.0).genome().equals(pure), "pure x pure breeds true");
        }
    }

    // ---------------------------------------------------------------------------------------------
    static void mutation() {
        System.out.println("mutation");
        Genome avg = g("00-00-00-00-00-00");        // fertility average x average = 1.0
        Genome swarm = g("00-00-00-00-00-33");      // swarm/swarm = 2.0 (rare dominant)
        Genome shy = g("00-00-00-00-00-22");        // shy/shy = 0.6
        near(Genome.mutationChance(avg, avg), 0.04, 1e-9, "average parents");
        near(Genome.mutationChance(swarm, swarm), 0.16, 1e-9, "swarm parents");
        near(Genome.mutationChance(shy, shy), 0.04 * 0.36, 1e-6, "shy parents");   // allele values are floats
        near(Genome.mutationChance(swarm, shy), 0.04 * 1.3 * 1.3, 1e-6, "mixed parents use the average");
        check(Genome.mutationChance(swarm, swarm) > Genome.mutationChance(avg, avg)
                && Genome.mutationChance(avg, avg) > Genome.mutationChance(shy, shy),
                "more fertile = less controlled evolution");
        check(Genome.mutationChance(avg, avg) <= Genome.MAX_MUTATION_CHANCE, "cap respected");

        Random rng = new Random(7);
        int n = 200000, hits = 0;
        int rareOk = 0;
        for (int i = 0; i < n; i++) {
            Genome.Offspring o = Genome.breed(avg, avg, rng);
            if (o.mutated()) {
                hits++;
                boolean hasRare = false;
                for (int slot = 0; slot < 2; slot++) {
                    hasRare |= o.genome().allele(o.mutatedGene(), slot).isRare();
                }
                if (hasRare) rareOk++;
            } else {
                check(o.genome().equals(avg), "no mutation from all-default parents means an all-default child");
                if (failures > 20) return;
            }
        }
        near(hits / (double) n, 0.04, 0.003, "observed mutation rate, average parents");
        check(rareOk == hits, "every mutation must introduce a rare allele into the hit gene");

        int swarmHits = 0;
        for (int i = 0; i < 50000; i++) if (Genome.breed(swarm, swarm, rng).mutated()) swarmHits++;
        near(swarmHits / 50000.0, 0.16, 0.008, "observed mutation rate, swarm parents");

        // forced mutation (future Mutagen): chance 1.0 always mutates and always changes exactly one slot
        for (int i = 0; i < 2000; i++) {
            Genome.Offspring o = Genome.breed(avg, avg, rng, 1.0);
            check(o.mutated(), "chance 1.0 always mutates");
            int changed = 0;
            for (Gene gene : Gene.values()) for (int s = 0; s < 2; s++) {
                if (o.genome().alleleId(gene, s) != 0) changed++;
            }
            check(changed == 1, "forced mutation changes exactly one allele slot, changed " + changed);
            if (failures > 20) return;
        }
        // chance 0.0 never mutates
        for (int i = 0; i < 2000; i++) check(!Genome.breed(swarm, swarm, rng, 0.0).mutated(), "chance 0 never mutates");
        // every gene can be hit
        Set<Gene> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) seen.add(Genome.breed(avg, avg, rng, 1.0).mutatedGene());
        check(seen.size() == Gene.COUNT, "mutations should reach all genes, reached " + seen);
    }

    // ---------------------------------------------------------------------------------------------
    static void breeding() {
        System.out.println("breeding");
        Genome avg = g("00-00-00-00-00-00");        // fertility 1.0
        Genome prolific = g("00-00-00-00-00-11");   // 1.5
        Genome shy = g("00-00-00-00-00-22");        // 0.6
        Genome swarm = g("00-00-00-00-00-33");      // 2.0
        Random rng = new Random(11);

        // expected litter = 2 x average fertility, symmetric in the parents
        near(Breeding.expectedLitter(avg, avg), 2.0, 1e-6, "average litter");
        near(Breeding.expectedLitter(prolific, prolific), 3.0, 1e-6, "prolific litter");
        near(Breeding.expectedLitter(shy, shy), 1.2, 1e-5, "shy litter");
        near(Breeding.expectedLitter(swarm, swarm), 4.0, 1e-6, "swarm litter");
        near(Breeding.expectedLitter(swarm, shy), 2.6, 1e-5, "mixed pair uses the average");
        near(Breeding.expectedLitter(shy, swarm), Breeding.expectedLitter(swarm, shy), 1e-9, "parent order is irrelevant");
        check(Math.ceil(Breeding.expectedLitter(swarm, swarm)) <= Breeding.MAX_LITTER, "litter cap must not truncate the best pair");

        // rolls: whole means are exact, fractional means round stochastically, always 1..MAX_LITTER
        for (int i = 0; i < 2000; i++) {
            check(Breeding.rollLitter(avg, avg, rng) == 2, "average x average is always 2 pups");
            check(Breeding.rollLitter(swarm, swarm, rng) == 4, "swarm x swarm is always 4 pups");
            if (failures > 20) return;
        }
        int n = 200000;
        long shySum = 0, mixedSum = 0;
        int shyTwos = 0, mixedThrees = 0;
        for (int i = 0; i < n; i++) {
            int a = Breeding.rollLitter(shy, shy, rng);
            int b = Breeding.rollLitter(swarm, shy, rng);
            check(a == 1 || a == 2, "shy litter is 1 or 2, got " + a);
            check(b == 2 || b == 3, "swarm x shy litter is 2 or 3, got " + b);
            shySum += a;
            mixedSum += b;
            if (a == 2) shyTwos++;
            if (b == 3) mixedThrees++;
            if (failures > 20) return;
        }
        near(shySum / (double) n, 1.2, 0.006, "observed shy litter mean");
        near(shyTwos / (double) n, 0.2, 0.006, "shy pairs get a second pup 20% of the time");
        near(mixedSum / (double) n, 2.6, 0.006, "observed mixed litter mean");
        near(mixedThrees / (double) n, 0.6, 0.006, "mixed pairs get a third pup 60% of the time");

        // crowding: the litter shrinks to the room left, never below 0
        for (int i = 0; i < 500; i++) {
            check(Breeding.litterSize(avg, avg, 0, rng) == 2, "empty area: full litter");
            check(Breeding.litterSize(avg, avg, Breeding.CROWD_LIMIT - 2, rng) == 2, "room for exactly 2: full litter of 2");
            check(Breeding.litterSize(avg, avg, Breeding.CROWD_LIMIT - 1, rng) == 1, "room for 1: litter shrinks to 1");
            check(Breeding.litterSize(swarm, swarm, Breeding.CROWD_LIMIT - 1, rng) == 1, "swarm litter also shrinks to the room");
            check(Breeding.litterSize(swarm, swarm, Breeding.CROWD_LIMIT, rng) == 0, "full: nothing is born");
            check(Breeding.litterSize(swarm, swarm, Breeding.CROWD_LIMIT + 25, rng) == 0, "over full: still 0, not negative");
            if (failures > 20) return;
        }

        // cooldown: more fertile = breeds again sooner, exact values for the four common alleles
        check(Breeding.cooldownTicks(1.0F) == 6000, "average cooldown is vanilla's 6000 ticks");
        check(Breeding.cooldownTicks(1.5F) == 4000, "prolific cooldown");
        check(Breeding.cooldownTicks(2.0F) == 3000, "swarm cooldown");
        check(Breeding.cooldownTicks(0.6F) == 10000, "shy cooldown");
        check(Breeding.cooldownTicks(0.6F) > Breeding.cooldownTicks(1.0F)
                && Breeding.cooldownTicks(1.0F) > Breeding.cooldownTicks(1.5F)
                && Breeding.cooldownTicks(1.5F) > Breeding.cooldownTicks(2.0F), "cooldown falls as fertility rises");
        check(Breeding.cooldownTicks(100.0F) == Breeding.MIN_COOLDOWN_TICKS, "cooldown never below the minimum");
        check(Breeding.cooldownTicks(0.0F) > 0 && Breeding.cooldownTicks(-3.0F) > 0, "nonsense fertility still gives a sane cooldown");
        for (Gene gene : new Gene[] {Gene.FERTILITY}) {
            for (Allele a : gene.alleles()) {
                check(Breeding.cooldownTicks(a.value()) >= Breeding.MIN_COOLDOWN_TICKS, "cooldown for allele " + a.name());
            }
        }

        // the design trade-off, end to end: mutations per LITTER grow much faster than fertility
        // (litter size x per-pup chance): shy 1.2 x 1.44% = 0.017, average 2 x 4% = 0.08, swarm 4 x 16% = 0.64
        double shyMut = mutationsPerLitter(shy, rng), avgMut = mutationsPerLitter(avg, rng), swarmMut = mutationsPerLitter(swarm, rng);
        near(shyMut, 1.2 * 0.04 * 0.36, 0.004, "mutations per shy litter");
        near(avgMut, 2 * 0.04, 0.006, "mutations per average litter");
        near(swarmMut, 4 * 0.16, 0.02, "mutations per swarm litter");
        check(shyMut < avgMut && avgMut < swarmMut, "fertile lines make more mutants per litter");

        // determinism: same seed, same litter sizes
        Random r1 = new Random(99), r2 = new Random(99);
        for (int i = 0; i < 500; i++) {
            check(Breeding.litterSize(shy, swarm, 3, r1) == Breeding.litterSize(shy, swarm, 3, r2), "same seed, same litter");
        }
    }

    /** Average number of mutated pups per litter for two parents with the same genome (all pups roll independently). */
    static double mutationsPerLitter(Genome parent, Random rng) {
        int litters = 100000;
        long mutants = 0;
        for (int i = 0; i < litters; i++) {
            int size = Breeding.rollLitter(parent, parent, rng);
            for (int p = 0; p < size; p++) {
                if (Genome.breed(parent, parent, rng).mutated()) mutants++;
            }
        }
        return mutants / (double) litters;
    }

    // ---------------------------------------------------------------------------------------------
    static void founders() {
        System.out.println("founders");
        Random rng = new Random(99);
        int n = 40000, standardEars = 0, shortEars = 0, loyal = 0;
        for (int i = 0; i < n; i++) {
            Genome f = Genome.randomFounder(rng);
            for (Gene gene : Gene.values()) for (int s = 0; s < 2; s++) {
                if (f.allele(gene, s).isRare()) { check(false, "founder has rare allele in " + gene); return; }
            }
            if (f.expressed(Gene.EARS).name().equals("standard")) standardEars++;
            if (f.expressed(Gene.EARS).name().equals("short")) shortEars++;
            if (f.traits().temperament() == Temperament.LOYAL) loyal++;
        }
        // weights 6:3:3 -> p(std)=0.5 per slot -> shows in 75%; short needs both slots short: 0.25*0.25
        near(standardEars / (double) n, 0.75, 0.012, "founders showing standard ears");
        near(shortEars / (double) n, 0.0625, 0.008, "founders showing short ears");
        // weights 5:3:2 -> p(loyal)=0.5 -> shows in 75%
        near(loyal / (double) n, 0.75, 0.012, "founders showing loyal temperament");
    }


    // ---------------------------------------------------------------------------------------------
    static void lineages() {
        System.out.println("lineages (Phase 3)");

        // --- save format: ids of the Phase 1/2 alleles never move (tables are append-only)
        String[][] original = {
                {"standard", "long", "short", "huge"}, {"tan", "cream", "brown", "spotted"},
                {"standard", "long", "short", "springy"}, {"average", "stocky", "slim", "chonker"},
                {"loyal", "skittish", "feral", "stoic"}, {"average", "prolific", "shy", "swarm"}};
        for (Gene gene : Gene.values()) {
            for (int i = 0; gene.ordinal() < Genome.LEGACY_GENES && i < original[gene.ordinal()].length; i++) {
                check(gene.allele(i).name().equals(original[gene.ordinal()][i]),
                        "save format: " + gene + " id " + i + " must stay '" + original[gene.ordinal()][i] + "'");
            }
        }
        check(Gene.COAT.alleleCount() == 12 && Gene.EARS.alleleCount() == 7, "expected allele counts changed");

        // --- table rules for wild alleles
        for (Gene gene : Gene.values()) {
            int maxCommon = 0, minRare = Integer.MAX_VALUE;
            for (Allele a : gene.alleles()) {
                if (a.isCommon()) maxCommon = Math.max(maxCommon, a.dominance());
                if (a.isRare()) minRare = Math.min(minRare, a.dominance());
            }
            for (Allele a : gene.alleles()) {
                if (a.isRare()) check(a.dominance() > maxCommon, gene + ": rare " + a.name() + " must outrank common alleles");
                if (a.isWild()) {
                    check(a.lineage() != Lineage.MEADOW, gene + ": " + a.name() + " cannot belong to MEADOW");
                    check(a.dominance() < minRare, gene + ": wild " + a.name() + " must rank below rare (mutations always show)");
                    if (!a.name().equals("glow")) {
                        check(a.dominance() > maxCommon, gene + ": wild " + a.name() + " must outrank common alleles (visible at a glance)");
                    }
                }
            }
        }
        check(Gene.COAT.alleleNamed("glow").dominance() < Gene.COAT.alleleNamed("brown").dominance(),
                "glow is the deliberately recessive wild coat");

        // --- recipes
        check(Founders.recipe(Lineage.MEADOW).isEmpty(), "meadow has no signatures");
        for (Lineage lineage : Lineage.values()) {
            List<Founders.Signature> recipe = Founders.recipe(lineage);
            check(recipe != null, lineage + " has a recipe");
            double[] chanceSum = new double[Gene.COUNT];
            boolean wildCoat = false;
            for (Founders.Signature sig : recipe) {
                Allele a = sig.resolve();
                chanceSum[sig.gene().ordinal()] += sig.chance();
                check(sig.chance() > 0 && sig.chance() <= 1, lineage + ": bad chance for " + a.name());
                check(a.isWild() ? a.lineage() == lineage : a.isCommon(),
                        lineage + ": signature " + a.name() + " must be a wild allele of this lineage or a common allele");
                wildCoat |= sig.gene() == Gene.COAT && a.isWild();
            }
            for (Gene gene : Gene.values()) {
                check(chanceSum[gene.ordinal()] <= 1.0 + 1e-9, lineage + ": signature chances in " + gene + " exceed 1");
            }
            if (lineage != Lineage.MEADOW) {
                check(wildCoat, lineage + " needs a wild coat so it is readable at a glance");
            }
            // every wild allele of the table is used by the recipe of its own lineage
        }
        for (Gene gene : Gene.values()) {
            for (Allele a : gene.alleles()) {
                if (!a.isWild()) continue;
                boolean used = false;
                for (Founders.Signature sig : Founders.recipe(a.lineage())) used |= sig.gene() == gene && sig.allele().equals(a.name());
                check(used, "wild allele " + gene + "/" + a.name() + " is never handed out by " + a.lineage());
            }
        }
        check(Lineage.fromKey("Tundra").orElse(null) == Lineage.TUNDRA && Lineage.fromKey("nope").isEmpty()
                && Lineage.fromKey(null).isEmpty(), "Lineage.fromKey");

        // --- meadow founders are the old founders, bit for bit
        for (long seed = 0; seed < 3000; seed++) {
            check(Founders.roll(Lineage.MEADOW, new Random(seed)).equals(Genome.randomFounder(new Random(seed))),
                    "meadow founder must equal randomFounder for seed " + seed);
            if (failures > 20) return;
        }

        // --- founders of every lineage
        for (Lineage lineage : Lineage.values()) {
            Random rng = new Random(1000 + lineage.ordinal());
            int n = 20000;
            int[][] hits = new int[Gene.COUNT][16];        // how often each allele id fills a slot, per gene
            for (int i = 0; i < n; i++) {
                Genome f = Genome.randomFounder(lineage, rng);
                check(Genome.parse(f.toString()).equals(Optional.of(f)), "founder text round trip " + f);
                for (Gene gene : Gene.values()) for (int slot = 0; slot < 2; slot++) {
                    Allele a = f.allele(gene, slot);
                    hits[gene.ordinal()][a.id()]++;
                    if (a.isRare()) { check(false, lineage + " founder carries a mutation-only allele " + a.name()); return; }
                    if (a.isWild() && a.lineage() != lineage) { check(false, lineage + " founder carries " + a.name() + " of " + a.lineage()); return; }
                }
            }
            // observed slot frequency of every signature allele = chance + (1 - chance sum) x its common-pool share
            for (Founders.Signature sig : Founders.recipe(lineage)) {
                Gene gene = sig.gene();
                double chanceSum = 0;
                for (Founders.Signature other : Founders.recipe(lineage)) if (other.gene() == gene) chanceSum += other.chance();
                int weightTotal = 0;
                for (Allele a : gene.alleles()) weightTotal += a.weight();
                Allele a = sig.resolve();
                double expected = sig.chance() + (1 - chanceSum) * (a.weight() / (double) weightTotal);
                near(hits[gene.ordinal()][a.id()] / (2.0 * n), expected, 0.012,
                        lineage + " slot frequency of " + gene + "/" + a.name());
            }
            // a lineage without a signature in a gene draws it from the ordinary common pool (weights as usual)
            boolean signedFertility = false;
            for (Founders.Signature sig : Founders.recipe(lineage)) signedFertility |= sig.gene() == Gene.FERTILITY;
            if (!signedFertility) {
                int total = 0;
                for (Allele a : Gene.FERTILITY.alleles()) total += a.weight();
                near(hits[Gene.FERTILITY.ordinal()][0] / (2.0 * n), Gene.FERTILITY.allele(0).weight() / (double) total, 0.012,
                        lineage + " unsigned fertility = common pool");
            }
        }

        // --- lineages show at a glance: a founder almost always has its wild coat expressed
        for (Lineage lineage : Lineage.values()) {
            if (lineage == Lineage.MEADOW || lineage == Lineage.SWAMP) continue;    // swamp: see the glow test below
            Random rng = new Random(77 + lineage.ordinal());
            int shown = 0, n = 5000;
            for (int i = 0; i < n; i++) if (Genome.randomFounder(lineage, rng).expressed(Gene.COAT).isWild()) shown++;
            check(shown / (double) n > 0.97, lineage + ": only " + shown / (double) n + " of founders show a wild coat");
        }

        // --- swamp: moss is dominant, glow is recessive (mostly carried, rarely seen)
        {
            Random rng = new Random(5);
            int n = 40000, glow = 0, carrier = 0, moss = 0;
            for (int i = 0; i < n; i++) {
                Genome f = Genome.randomFounder(Lineage.SWAMP, rng);
                if (f.expressed(Gene.COAT).name().equals("glow")) glow++;
                if (f.expressed(Gene.COAT).name().equals("moss")) moss++;
                if (f.hidden(Gene.COAT).map(Allele::name).orElse("").equals("glow")
                        || (f.isHomozygous(Gene.COAT) ? false : f.alleleId(Gene.COAT, 0) == Gene.COAT.alleleNamed("glow").id()
                                || f.alleleId(Gene.COAT, 1) == Gene.COAT.alleleNamed("glow").id())) carrier++;
            }
            near(glow / (double) n, 0.25 * 0.25, 0.008, "swamp founders showing glow (both slots glow)");
            near(carrier / (double) n, 2 * 0.25 * 0.75, 0.012, "swamp founders carrying exactly one glow");
            check(moss > glow * 4, "swamp founders mostly show moss, not glow");
            int glowId = Gene.COAT.alleleNamed("glow").id(), mossId = Gene.COAT.alleleNamed("moss").id();
            Genome carriers = g("00-00-00-00-00-00").with(Gene.COAT, 0, mossId).with(Gene.COAT, 1, glowId);
            check(carriers.expressed(Gene.COAT).name().equals("moss") && carriers.hidden(Gene.COAT).get().name().equals("glow"),
                    "moss/glow carrier shows moss and hides glow");
            int glowPups = 0, pups = 40000;
            for (int i = 0; i < pups; i++) {
                if (Genome.breed(carriers, carriers, rng, 0.0).genome().expressed(Gene.COAT).name().equals("glow")) glowPups++;
            }
            near(glowPups / (double) pups, 0.25, 0.01, "two moss/glow carriers give glow pups 1 in 4");
        }

        // --- mutation never produces a wild allele, in any gene
        {
            Random rng = new Random(11);
            Genome avg = g("00-00-00-00-00-00");
            for (int i = 0; i < 30000; i++) {
                Genome.Offspring o = Genome.breed(avg, avg, rng, 1.0);
                for (int slot = 0; slot < 2; slot++) {
                    Allele a = o.genome().allele(o.mutatedGene(), slot);
                    if (a.isWild()) { check(false, "mutation produced wild allele " + a.name()); return; }
                }
            }
            check(true, "mutation never produces wild alleles");
            // ...and wild alleles are never lost or invented by crossing two lineages (no mutation)
            Genome tundra = Genome.randomFounder(Lineage.TUNDRA, rng), desert = Genome.randomFounder(Lineage.DESERT, rng);
            for (int i = 0; i < 3000; i++) {
                Genome pup = Genome.breed(tundra, desert, rng, 0.0).genome();
                for (Gene gene : Gene.values()) {
                    check(pup.alleleId(gene, 0) == tundra.alleleId(gene, 0) || pup.alleleId(gene, 0) == tundra.alleleId(gene, 1),
                            "slot 0 allele must come from the mother, " + gene);
                    check(pup.alleleId(gene, 1) == desert.alleleId(gene, 0) || pup.alleleId(gene, 1) == desert.alleleId(gene, 1),
                            "slot 1 allele must come from the father, " + gene);
                }
            }
        }

        // --- wild alleles breed true and have real effects
        Genome snow = g("44-44-00-44-00-00");           // tiny ears, snow coat, woolly build (homozygous)
        Genome sandy = g("55-55-44-55-00-00");          // fennec ears, sand coat, fleet legs, lean build
        Random rng = new Random(21);
        for (int i = 0; i < 500; i++) {
            Genome pup = Genome.breed(snow, snow, rng, 0.0).genome();
            check(pup.equals(snow), "homozygous wild parents breed true");
        }
        Traits t = snow.traits();
        near(t.earScale(), 0.5, 1e-6, "tiny ears scale");
        near(t.hearingMultiplier(), 0.5, 1e-6, "tiny ears hear less");
        near(t.healthMultiplier(), 1 + 0.35 * 1.5, 1e-5, "woolly = tough");
        check(t.coatName().equals("snow"), "snow coat name");
        Traits d = sandy.traits();
        near(d.earScale(), 1.55, 1e-5, "fennec ears scale");
        near(d.speedMultiplier(), 1 + 0.35 * 0.6, 1e-5, "fleet legs are fast");
        near(d.jumpMultiplier(), 1 + 0.35 * 1.2, 1e-5, "fleet legs jump");
        near(d.healthMultiplier(), 1 - 0.3 * 1.5, 1e-5, "lean = fragile");
        check(d.healthMultiplier() > 0, "lean bunnidogs still have positive health");
        Traits h = g("00-00-55-00-00-00").traits();
        near(h.jumpMultiplier(), 1 + 0.3 * 1.2, 1e-5, "hoofed legs jump");
        check(h.jumpMultiplier() > h.speedMultiplier(), "hoofed: jump beats speed (goat legs)");

        // --- a wild allele beats a common one, a rare mutation beats a wild one
        check(g("04-00-00-00-00-00").expressed(Gene.EARS).name().equals("tiny"), "tiny beats standard");
        check(g("34-00-00-00-00-00").expressed(Gene.EARS).name().equals("huge"), "a mutation (huge) outranks a wild allele");
        check(g("46-00-00-00-00-00").expressed(Gene.EARS).name().equals("bat"), "wild vs wild: higher rank shows");

        // --- describe shows where a wild allele comes from
        check(g("44-00-00-00-00-00").describe(Gene.EARS).equals("Ears: tiny [tundra]"), "describe wild: " + g("44-00-00-00-00-00").describe(Gene.EARS));
        check(g("04-00-00-00-00-00").describe(Gene.EARS).equals("Ears: tiny [tundra] (carries standard)"), "describe wild carrier");
        check(g("00-4b-00-00-00-00").describe(Gene.COAT).equals("Coat: snow [tundra] (carries glow [swamp])"), "describe two wild alleles: " + g("00-4b-00-00-00-00").describe(Gene.COAT));

        // --- the /summon cheat sheet in docs/REFERENCE.md ("Debug cheat sheet") must say what it claims
        String[][] cheat = {
                {"44-44-00-44-00-00", "tundra", "tiny", "snow", "standard", "woolly"},
                {"55-55-44-55-00-00", "desert", "fennec", "sand", "fleet", "lean"},
                {"00-66-00-00-00-11", "swamp", "standard", "moss", "standard", "average"},
                {"00-6b-00-00-00-00", "swamp", "standard", "moss", "standard", "average"},   // moss/glow carrier
                {"00-bb-00-00-00-00", "swamp", "standard", "glow", "standard", "average"},   // glow shows
                {"66-77-00-00-11-00", "cave", "bat", "pale", "standard", "average"},
                {"00-88-55-11-00-00", "mountain", "standard", "slate", "hoofed", "stocky"},
                {"00-99-00-00-22-00", "nether", "standard", "ember", "standard", "average"},
                {"00-aa-00-00-11-00", "end", "standard", "void", "standard", "average"}};
        for (String[] row : cheat) {
            Genome c = g(row[0]);
            check(c.expressed(Gene.EARS).name().equals(row[2]) && c.expressed(Gene.COAT).name().equals(row[3])
                    && c.expressed(Gene.LEGS).name().equals(row[4]) && c.expressed(Gene.BUILD).name().equals(row[5]),
                    "cheat sheet genome " + row[0] + " (" + row[1] + ") expresses " + c.expressed(Gene.EARS).name() + "/"
                    + c.expressed(Gene.COAT).name() + "/" + c.expressed(Gene.LEGS).name() + "/" + c.expressed(Gene.BUILD).name());
        }
        check(g("00-00-00-00-11-00").traits().temperament() == Temperament.SKITTISH
                && g("00-99-00-00-22-00").traits().temperament() == Temperament.FERAL, "cheat sheet temperaments");
        check(g("00-66-00-00-00-11").traits().fertility() == 1.5f, "cheat sheet swamp fertility is prolific");
    }


    // ---------------------------------------------------------------------------------------------
    static void phaseAllele() {
        Allele phase = Gene.LEGS.alleleNamed("phase");
        check(phase.id() == 6 && phase.isWild() && phase.lineage() == Lineage.END && phase.weight() == 0,
                "Phase is appended at Legs id 6, End-only and outside the common/mutation pool");
        check(Founders.recipe(Lineage.END).stream().anyMatch(s -> s.gene() == Gene.LEGS && s.allele().equals("phase") && s.chance() == .75),
                "Fresh End founders carry the accepted Phase signature");
        var fixed = homozygous(Genome.DEFAULT,Gene.LEGS,"phase");
        var pups = new java.util.Random(52);
        for (int i = 0; i < 32; i++) {
            Genome pup = Genome.breed(fixed,Genome.DEFAULT,pups,0).genome();
            check(pup.alleleId(Gene.LEGS,0) == 6 && pup.alleleId(Gene.LEGS,1) == 0 && Adaptations.phasing(pup),
                    "An End Phase parent transmits its expressed dominant copy through ordinary inheritance");
        }
        check(!Adaptations.phasing(fixed.with(Gene.LEGS,1,Gene.LEGS.alleleNamed("springy").id())),
                "A hidden Phase copy must not grant the ability");
        check(Genome.parse("00-aa-66-00-11-44").orElseThrow().equals(fixed.with(Gene.COAT,0,10).with(Gene.COAT,1,10)
                .with(Gene.TEMPERAMENT,0,1).with(Gene.TEMPERAMENT,1,1).with(Gene.FERTILITY,0,4).with(Gene.FERTILITY,1,4)),
                "The ordinary six-pair text upgrade retains the new leg allele and supplies legacy plain Diet");
        check(Juice.effects(fixed,Stats.ZERO,0).stream().anyMatch(e -> e.id().equals("minecraft:slow_falling") && e.ticks() == 900),
                "Infused Phase juice yields its own real forty-five-second Slow Falling effect");
        check(Phasing.inRange(9,1) && Phasing.inRange(36,-1) && !Phasing.inRange(37,0)
                && !Phasing.inRange(8,0) && !Phasing.inRange(16,2) && !Phasing.inRange(Double.NaN,0)
                && Phasing.farther(4,16) && !Phasing.farther(4,15),"Blink range, level changes and useful escape distance stay bounded");
    }

    static void webbedAllele() {
        var allele = Gene.LEGS.alleleNamed("webbed");
        check(allele.id() == 7 && allele.isWild() && allele.lineage() == Lineage.SWAMP && allele.weight() == 0,
                "Webbed legs append at id 7 without replacing Phase or any saved leg allele");
        check(Founders.recipe(Lineage.SWAMP).stream().anyMatch(s -> s.gene() == Gene.LEGS && s.allele().equals("webbed") && s.chance() == .8),
                "Fresh swamp founders receive the swimming signature");
        var fixed = homozygous(Genome.DEFAULT,Gene.LEGS,"webbed");
        var rng = new java.util.Random(73);
        for (int i = 0; i < 32; i++) {
            var pup = Genome.breed(fixed,Genome.DEFAULT,rng,0).genome();
            check(pup.alleleId(Gene.LEGS,0) == 7 && pup.alleleId(Gene.LEGS,1) == 0 && Adaptations.swimBonus(pup) == .5,
                    "Ordinary breeding transmits expressed webbed feet");
        }
        check(Adaptations.swimBonus(fixed.with(Gene.LEGS,1,Gene.LEGS.alleleNamed("springy").id())) == 0,
                "Hidden swimming alleles cannot grant propulsion");
        near(fixed.traits().speedMultiplier(),.97,1e-6,"Webbed legs retain their small land-speed cost");
        near(fixed.traits().jumpMultiplier(),.94,1e-6,"Webbed legs retain their small jumping cost");
        var terms = Trends.terms(Gene.LEGS,allele);
        check(terms.size() == 2 && terms.get(0).stat() == Stat.SWIFTNESS && terms.get(1).stat() == Stat.LEAP,
                "Webbed breeding trades Leap for Swiftness");
        check(Juice.effects(fixed,Stats.ZERO,0).stream().anyMatch(e -> e.id().equals("minecraft:dolphins_grace") && e.ticks() == 1800),
                "Webbed infusion supplies ninety-second Dolphin's Grace");
    }

    static void fertilityMarks() {
        var marks = java.util.EnumSet.noneOf(FertilityMark.class);
        for (var allele : Gene.FERTILITY.alleles()) {
            var fixed = homozygous(Genome.DEFAULT,Gene.FERTILITY,allele.name());
            var mark = FertilityMark.of(fixed); marks.add(mark);
            check(Plain.effect(Gene.FERTILITY,allele).contains(mark.description()),"Sheets must explain the actual inherited fertility mark");
            check(Genome.parse(fixed.toString()).map(FertilityMark::of).orElseThrow() == mark,
                    "The phenotype uses the existing saved allele, without another appearance field");
        }
        check(marks.size() == Gene.FERTILITY.alleleCount(),"Every original fertility allele must have a distinct visible phenotype");
        int prolific = Gene.FERTILITY.alleleNamed("prolific").id(), average = Gene.FERTILITY.alleleNamed("average").id();
        check(FertilityMark.of(Genome.DEFAULT.with(Gene.FERTILITY,0,prolific).with(Gene.FERTILITY,1,average)) == FertilityMark.NONE,
                "The dominant average copy must hide prolific's rosette");
        int swarm = Gene.FERTILITY.alleleNamed("swarm").id();
        var pair = Genome.DEFAULT.with(Gene.FERTILITY,0,swarm).with(Gene.FERTILITY,1,prolific);
        check(FertilityMark.of(pair) == FertilityMark.SWARM,"A visible mutation must select only its own cluster");
        var rng = new java.util.Random(92);
        for (int i = 0; i < 32; i++) check(FertilityMark.of(Genome.breed(homozygous(Genome.DEFAULT,Gene.FERTILITY,"starborn"),
                Genome.DEFAULT,rng,0).genome()) == FertilityMark.STARBORN,"Breeding must transmit the dominant visible End star without changing fertility rules");
    }

    static void stats() {
        System.out.println("bred stats + Bloom (Phase 4a)");

        // --- the new End allele is appended (ids are save format) and wired to the End lineage only
        check(Gene.FERTILITY.alleleCount() == 5 && Gene.FERTILITY.allele(4).name().equals("starborn"), "starborn is Fertility id 4");
        Allele starborn = Gene.FERTILITY.alleleNamed("starborn");
        check(starborn.isWild() && starborn.lineage() == Lineage.END && starborn.weight() == 0, "starborn is a wild End allele");
        near(starborn.value(), 0.70, 1e-6, "starborn fertility multiplier");
        check(g("00-00-00-00-00-44").describe(Gene.FERTILITY).equals("Fertility: starborn [end]"), "describe starborn: " + g("00-00-00-00-00-44").describe(Gene.FERTILITY));
        boolean inEnd = false, elsewhere = false;
        for (Lineage l : Lineage.values()) {
            for (Founders.Signature sig : Founders.recipe(l)) {
                if (sig.allele().equals("starborn")) { if (l == Lineage.END) inEnd = true; else elsewhere = true; }
            }
        }
        check(inEnd && !elsewhere, "only End founders get starborn");
        {
            Random rng = new Random(9);
            int n = 20000, shown = 0;
            for (int i = 0; i < n; i++) if (Genome.randomFounder(Lineage.END, rng).expressed(Gene.FERTILITY).id() == starborn.id()) shown++;
            near(shown / (double) n, 1 - 0.6 * 0.6, 0.012, "End founders showing starborn (dominant, 40% per slot)");
            Random mut = new Random(10);
            Genome avg = g("00-00-00-00-00-00");
            for (int i = 0; i < 20000; i++) {
                Genome.Offspring o = Genome.breed(avg, avg, mut, 1.0);
                if (o.genome().alleleId(Gene.FERTILITY, 0) == starborn.id() || o.genome().alleleId(Gene.FERTILITY, 1) == starborn.id()) {
                    check(false, "a mutation created starborn"); break;
                }
            }
        }
        check(Breeding.cooldownTicks(starborn.value()) == 8571, "starborn parents rest longer: " + Breeding.cooldownTicks(starborn.value()));
        near(Breeding.expectedLitter(g("00-00-00-00-00-44"), g("00-00-00-00-00-44")), 1.4, 1e-6, "starborn litters are small");
        near(Genome.mutationChance(g("00-00-00-00-00-44"), g("00-00-00-00-00-44")), 0.04 * 0.49, 1e-9, "starborn mutates less");

        // --- Stat / Stats basics
        check(Stat.COUNT == 5 && Stat.fromKey("Bloom").orElse(null) == Stat.BLOOM && Stat.fromKey("x").isEmpty(), "Stat lookup");
        check(Stats.ZERO.isZero() && Stats.ZERO.toString().equals("0,0,0,0,0"), "zero stats");
        Stats sample = Stats.of(3, -1, 0, 2, 12);
        check(sample.toString().equals("3,-1,0,2,12") && Stats.parse(sample.toString()).equals(Optional.of(sample)), "stats text round trip");
        check(sample.get(Stat.SWIFTNESS) == -1 && sample.get(Stat.BLOOM) == 12, "stats getters");
        check(sample.with(Stat.LEAP, 7).get(Stat.LEAP) == 7 && sample.get(Stat.LEAP) == 0, "stats are immutable");
        check(Stats.parse("2, 5").equals(Optional.of(Stats.of(2, 5, 0, 0, 0))), "short stats text is padded (old saves)");
        check(Stats.parse("").isEmpty() && Stats.parse(null).isEmpty() && Stats.parse("1,2,3,4,5,6").isEmpty() && Stats.parse("a,b").isEmpty(),
                "bad stats text is rejected");
        check(Stats.of(5_000_000).get(Stat.VITALITY) == Stats.LIMIT, "points are clamped so ints cannot overflow");

        // --- curves
        for (Stat stat : Stat.values()) {
            if (stat == Stat.BLOOM) continue;
            near(Stats.attributeMultiplier(stat, 0), 1.0, 1e-9, stat + " x1 at zero points");
            double prev = -1;
            for (int p = -100000; p <= 100000; p += 37) {
                double m = Stats.attributeMultiplier(stat, p);
                check(m >= Stats.NEGATIVE_FLOOR - 1e-9 && m >= prev - 1e-12, stat + " curve monotone and above the floor at " + p);
                prev = m;
            }
            check(Stats.attributeMultiplier(stat, -1e9) == Stats.NEGATIVE_FLOOR, stat + " floor is reached, not passed");
            check(Stats.attributeMultiplier(stat, 1_000_000) > Stats.attributeMultiplier(stat, 1000), stat + " has no ceiling");
        }
        near(Stats.attributeMultiplier(Stat.VITALITY, 25), 2.0, 1e-9, "25 Vitality = x2");
        near(Stats.attributeMultiplier(Stat.VITALITY, 100), 5.0, 1e-9, "Vitality stays linear");
        near(Stats.attributeMultiplier(Stat.SWIFTNESS, 25), 2.0, 1e-9, "25 Swiftness = x2");
        near(Stats.attributeMultiplier(Stat.SWIFTNESS, 100), 2.0 + 0.15 * Math.sqrt(75), 1e-9, "Swiftness has a square-root tail");
        check(Stats.attributeMultiplier(Stat.LEAP, 100) < Stats.attributeMultiplier(Stat.VITALITY, 100) / 1.4, "Leap tail is much gentler than Vitality");
        near(Stats.of(0, 0, 0, 10, 0).multiplier(Stat.SENSES), 1.4, 1e-9, "Stats.multiplier");
        boolean threw = false;
        try { Stats.attributeMultiplier(Stat.BLOOM, 3); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "Bloom is not an attribute multiplier");

        // growth (Bloom)
        double prevGrowth = 0;
        for (int b = -500; b <= 100000; b += 3) {
            double m = Stats.growthMultiplier(b);
            check(m >= Stats.GROWTH_FLOOR - 1e-12 && m <= Stats.GROWTH_CAP + 1e-12 && m >= prevGrowth - 1e-12, "growth multiplier monotone and bounded at " + b);
            prevGrowth = m;
        }
        near(Stats.growthMultiplier(0), 1.0, 1e-12, "no Bloom = vanilla");
        near(Stats.growthMultiplier(20), 2.0, 1e-12, "20 Bloom = x2");
        near(Stats.growthMultiplier(-1000), 0.4, 1e-12, "negative Bloom slows down to 0.4x at worst");
        near(Growth.minutesToAdult(Stats.growthMultiplier(-1000)), 50.0, 1e-9, "a low-Bloom line takes 50 minutes");
        double[][] timeTable = {{0, 20.0}, {10, 13.3}, {20, 10.0}, {50, 5.9}, {100, 4.7}, {300, 3.2}, {1000, 2.0}};
        for (double[] row : timeTable) {
            near(Growth.minutesToAdult(Stats.growthMultiplier(row[0])), row[1], 0.06, "time to adult at Bloom " + row[0] + " (DESIGN table)");
        }
        near(Growth.minutesToAdult(Stats.growthMultiplier(1e9)), 1.0, 1e-9, "never an adult in under 60 seconds");

        // the per-tick maths the entity uses: simulate a baby from birth
        for (double m : new double[]{0.4, 0.75, 1.0, 1.5, 2.0, 4.24, 9.83, 20.0}) {
            int age = -Growth.BABY_TICKS, ticks = 0;
            double carry = 0;
            while (age < 0 && ticks < 200000) {
                age++;                                                        // vanilla
                Growth.Step step = Growth.step(carry, m);                     // ours
                carry = step.carry();
                age = Math.max(-Growth.BABY_TICKS, Math.min(0, age + step.extraTicks()));
                ticks++;
            }
            near(ticks, Growth.BABY_TICKS / m, 2.0, "simulated ticks to adult at x" + m);
        }
        check(Growth.step(0.0, 1.0).extraTicks() == 0 && Growth.step(0.0, 1.0).carry() == 0.0, "vanilla speed adds nothing");

        // --- trends: every allele of the five genes is either neutral (a default) or has a proper entry
        Set<String> neutral = Set.of("ears/standard", "legs/standard", "build/average", "temperament/loyal", "fertility/average");
        for (Gene gene : Gene.values()) {
            for (Allele a : gene.alleles()) {
                List<Trends.Term> terms = Trends.terms(gene, a);
                String id = gene.key() + "/" + a.name();
                if (gene == Gene.COAT || gene == Gene.DIET || neutral.contains(id)) {
                    check(terms.isEmpty() && !Trends.hasTrend(gene, a), id + " must have no trend");
                    continue;
                }
                check(Trends.hasTrend(gene, a) && !terms.isEmpty(), id + " needs a trend entry");
                if (a.name().equals("starborn")) continue;
                check(terms.size() == 2 && terms.get(0).kind() == Trends.Kind.GAIN && terms.get(1).kind() == Trends.Kind.COST, id + " = one gain and one cost");
                check(terms.get(0).stat() != terms.get(1).stat(), id + ": gain and cost hit different stats");
                double wantGain = a.isRare() ? 3.0 : a.isWild() ? 2.0 : 1.0;
                near(terms.get(0).amount(), wantGain, 1e-12, id + " gain size");
                near(terms.get(1).amount(), 1.0, 1e-12, id + " cost size");
            }
        }
        // the whole table from docs/DESIGN.md, so the doc and the code cannot drift apart
        String[] table = {
                "build/stocky VITALITY SWIFTNESS", "build/slim SWIFTNESS VITALITY", "build/chonker VITALITY SWIFTNESS",
                "build/woolly VITALITY LEAP", "build/lean SWIFTNESS VITALITY",
                "legs/long SWIFTNESS SENSES", "legs/short SENSES SWIFTNESS", "legs/springy LEAP SENSES",
                "legs/fleet SWIFTNESS VITALITY", "legs/hoofed LEAP SWIFTNESS", "legs/phase LEAP VITALITY", "legs/webbed SWIFTNESS LEAP",
                "ears/long SENSES VITALITY", "ears/short VITALITY SENSES", "ears/huge SENSES VITALITY",
                "ears/tiny VITALITY SENSES", "ears/fennec SENSES VITALITY", "ears/bat SENSES LEAP",
                "temperament/skittish SWIFTNESS VITALITY", "temperament/feral VITALITY SENSES", "temperament/stoic VITALITY SWIFTNESS",
                "fertility/prolific BLOOM SENSES", "fertility/shy SENSES BLOOM", "fertility/swarm BLOOM VITALITY"};
        for (String row : table) {
            String[] w = row.split(" ");
            String[] gp = w[0].split("/");
            Gene gene = null;
            for (Gene x : Gene.values()) if (x.key().equals(gp[0])) gene = x;
            List<Trends.Term> t = Trends.terms(gene, gene.alleleNamed(gp[1]));
            check(t.size() == 2 && t.get(0).stat().name().equals(w[1]) && t.get(1).stat().name().equals(w[2]), "DESIGN trend table: " + row);
        }
        List<Trends.Term> sb = Trends.terms(Gene.FERTILITY, starborn);
        check(sb.size() == 3 && sb.get(0).stat() == Stat.BLOOM && sb.get(0).kind() == Trends.Kind.GAIN && sb.get(0).amount() == 2.0
                && sb.get(1).stat() == Stat.VITALITY && sb.get(1).kind() == Trends.Kind.TOLL
                && sb.get(2).stat() == Stat.SENSES && sb.get(2).kind() == Trends.Kind.TOLL, "starborn = +2 Bloom, tolls on Vitality and Senses");

        // --- quality shapes the trends
        near(Drift.gainFactor(0), 1.0, 1e-12, "gain x1 at quality 0");
        near(Drift.gainFactor(2), 2.0, 1e-12, "gain x2 at quality 2");
        near(Drift.gainFactor(6), 4.0, 1e-12, "gain x4 at quality 6");
        near(Drift.costFactor(0), 1.0, 1e-12, "cost x1 at quality 0");
        near(Drift.costFactor(2), 0.5, 1e-12, "cost x0.5 at quality 2");
        near(Drift.costFactor(4), 0.0, 1e-12, "costs are gone at quality 4");
        near(Drift.costFactor(9), 0.0, 1e-12, "costs never turn into gains");
        near(Drift.gainFactor(-3), 1.0, 1e-12, "negative quality counts as 0");
        // a sturdy line, from DESIGN: +Vitality/-Swiftness per generation
        Genome sturdy = g("00-00-00-11-00-00");
        for (double[] row : new double[][]{{0, 1, -1}, {2, 2, -0.5}, {4, 3, 0}, {6, 4, 0}}) {
            double[] tr = Drift.trend(sturdy, row[0]);
            near(tr[Stat.VITALITY.ordinal()], row[1], 1e-12, "sturdy Vitality trend at quality " + row[0]);
            near(tr[Stat.SWIFTNESS.ordinal()], row[2], 1e-12, "sturdy Swiftness trend at quality " + row[0]);
        }
        // every ordinary allele: zero-sum at quality 0, net growth above it
        for (Gene gene : Gene.values()) {
            for (Allele a : gene.alleles()) {
                if (!a.isCommon() || !Trends.hasTrend(gene, a)) continue;
                Genome one = Genome.DEFAULT.with(gene, 0, a.id()).with(gene, 1, a.id());
                check(one.expressed(gene).id() == a.id(), "test genome expresses " + a.name());
                double sum0 = 0, sum2 = 0, sum4 = 0;
                for (double x : Drift.trend(one, 0)) sum0 += x;
                for (double x : Drift.trend(one, 2)) sum2 += x;
                double[] t4 = Drift.trend(one, 4);
                for (double x : t4) sum4 += x;
                near(sum0, 0.0, 1e-12, gene + "/" + a.name() + " is zero-sum at quality 0");
                check(sum2 > 0, gene + "/" + a.name() + " grows at quality 2");
                for (double x : t4) check(x >= 0, gene + "/" + a.name() + " has no cost left at quality 4");
                near(sum4, 3.0, 1e-12, gene + "/" + a.name() + " gains x3 at quality 4");
            }
        }
        // wild and rare alleles are net positive even at quality 0
        for (Gene gene : Gene.values()) {
            for (Allele a : gene.alleles()) {
                if (a.isCommon() || !Trends.hasTrend(gene, a) || a.name().equals("starborn")) continue;
                Genome one = Genome.DEFAULT.with(gene, 0, a.id()).with(gene, 1, a.id());
                double sum0 = 0;
                for (double x : Drift.trend(one, 0)) sum0 += x;
                near(sum0, a.isRare() ? 2.0 : 1.0, 1e-12, gene + "/" + a.name() + " net trend at quality 0");
            }
        }
        // several genes add up (DESIGN example): stocky build + long ears
        Genome mixed = g("11-00-00-11-00-00");
        double[] mt = Drift.trend(mixed, 0);
        near(mt[Stat.VITALITY.ordinal()], 0, 1e-12, "stocky + long ears: Vitality +1 -1");
        near(mt[Stat.SWIFTNESS.ordinal()], -1, 1e-12, "stocky + long ears: Swiftness -1");
        near(mt[Stat.SENSES.ordinal()], 1, 1e-12, "stocky + long ears: Senses +1");
        // only the EXPRESSED allele counts: average (dominant) hides stocky
        double[] hidden = Drift.trend(g("00-00-00-01-00-00"), 4);
        for (double x : hidden) near(x, 0.0, 1e-12, "a carried stocky allele has no trend");
        for (double x : Drift.trend(Genome.DEFAULT, 5)) near(x, 0.0, 1e-12, "an unbred default line stays put");
        // the starborn toll does not care about quality
        for (double q : new double[]{0, 2, 4, 10, 100}) {
            double[] tt = Drift.trend(g("00-00-00-00-00-44"), q);
            near(tt[Stat.VITALITY.ordinal()], -1.0, 1e-12, "starborn Vitality toll at quality " + q);
            near(tt[Stat.SENSES.ordinal()], -1.0, 1e-12, "starborn Senses toll at quality " + q);
            near(tt[Stat.BLOOM.ordinal()], 2.0 * Drift.gainFactor(q), 1e-9, "starborn Bloom gain at quality " + q);
        }
        // pair drift = average of the two trends
        double[] da = Drift.trend(sturdy, 2), db = Drift.trend(g("00-00-00-22-00-00"), 2);
        double[] dp = Drift.drift(sturdy, g("00-00-00-22-00-00"), 2);
        for (int i = 0; i < Stat.COUNT; i++) near(dp[i], (da[i] + db[i]) / 2.0, 1e-12, "pair drift is the average, stat " + i);

        // --- starborn converts Bloom into seed quality
        Genome sbGenome = g("00-00-00-00-00-44"), plain = g("00-00-00-00-00-00");
        near(Drift.qualityBonus(plain, Stats.of(0, 0, 0, 0, 500)), 0.0, 1e-12, "no starborn, no bonus");
        near(Drift.qualityBonus(sbGenome, Stats.of(0, 0, 0, 0, 100)), 3.0, 1e-12, "starborn with 100 Bloom = +3");
        near(Drift.qualityBonus(sbGenome, Stats.ZERO), 0.0, 1e-12, "starborn with no Bloom = +0");
        near(Drift.qualityBonus(sbGenome, Stats.of(0, 0, 0, 0, -40)), 0.0, 1e-12, "negative Bloom never gives negative quality");
        near(Drift.qualityBonus(g("00-00-00-00-00-04"), Stats.of(0, 0, 0, 0, 100)), 3.0, 1e-12, "starborn is dominant: one copy is enough");
        near(Drift.pairQualityBonus(sbGenome, Stats.of(0, 0, 0, 0, 100), sbGenome, Stats.of(0, 0, 0, 0, 100)), 3.0, 1e-12, "pure pair gets it all");
        near(Drift.pairQualityBonus(sbGenome, Stats.of(0, 0, 0, 0, 100), plain, Stats.of(0, 0, 0, 0, 100)), 1.5, 1e-12, "one starborn parent = half");

        // --- inheritance
        Random rng = new Random(2026);
        Stats parents = Stats.of(4, -2, 0, 7, 10);
        double[] whole = {1, 0, 0, -3, 2};
        for (int i = 0; i < 50; i++) {
            Stats kid = Drift.inherit(parents, parents, whole, rng);
            check(kid.equals(Stats.of(5, -2, 0, 4, 12)), "whole-number drift is exact: " + kid);
        }
        double[] frac = new double[Stat.COUNT];
        frac[Stat.VITALITY.ordinal()] = 0.5;
        frac[Stat.SWIFTNESS.ordinal()] = -0.25;
        frac[Stat.SENSES.ordinal()] = 2.3;
        double[] sum = new double[Stat.COUNT];
        int n = 100000;
        Stats pa = Stats.of(3, 0, 0, 0, 0), pb = Stats.of(4, 1, 0, -1, 0);
        for (int i = 0; i < n; i++) {
            Stats kid = Drift.inherit(pa, pb, frac, rng);
            for (Stat st : Stat.values()) sum[st.ordinal()] += kid.get(st);
            int v = kid.get(Stat.VITALITY);
            check(v == 4 || v == 5, "3.5 + 0.5 rounds to 4 or 5, got " + v);
        }
        near(sum[Stat.VITALITY.ordinal()] / n, 4.0, 0.01, "Vitality: mean of 3 and 4, plus 0.5 (=4.0)");
        near(sum[Stat.SWIFTNESS.ordinal()] / n, 0.5 - 0.25, 0.01, "negative drift rounds without bias");
        near(sum[Stat.SENSES.ordinal()] / n, -0.5 + 2.3, 0.01, "Senses: (0 + -1)/2 + 2.3");
        near(sum[Stat.BLOOM.ordinal()] / n, 0.0, 1e-12, "no drift, no parents' Bloom = 0");
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 400; i++) seen.add(Drift.inherit(pa, pb, frac, rng).get(Stat.SENSES));
        check(seen.size() == 2, "littermates differ: Senses takes both neighbouring whole values, saw " + seen);
        check(Drift.stochasticRound(-2.5, new Random(1)) >= -3 && Drift.stochasticRound(-2.5, new Random(1)) <= -2, "negative rounding stays adjacent");

        // --- the loop from DESIGN "5. Bloom": a pure line of starborn + stocky + long ears (drift per generation, no rounding)
        Genome bloomLine = g("11-00-00-11-00-44");
        double[][] docTable = {   // tier, gen, q, Bloom, Vitality, Senses, Swiftness (as printed in DESIGN.md)
                {0, 10, 1.5, 29, -3, 5, -8}, {0, 30, 3.2, 118, 14, 29, -16},
                {2, 10, 4.1, 53, 15, 16, -2}, {2, 30, 6.1, 197, 67, 68, -2},
                {4, 10, 6.5, 75, 28, 28, 0}, {4, 30, 8.8, 271, 106, 106, 0}};
        for (double[] row : docTable) {
            double[] pts = new double[Stat.COUNT];
            double q = 0;
            for (int gen = 1; gen <= (int) row[1]; gen++) {
                q = row[0] + Drift.STARBORN_FACTOR * Math.sqrt(Math.max(0, pts[Stat.BLOOM.ordinal()]));
                double[] d = Drift.drift(bloomLine, bloomLine, q);
                for (int i = 0; i < pts.length; i++) pts[i] += d[i];
            }
            String label = "loop tier " + (int) row[0] + " gen " + (int) row[1];
            near(pts[Stat.BLOOM.ordinal()], row[3], 1.0, label + " Bloom");
            near(pts[Stat.VITALITY.ordinal()], row[4], 1.0, label + " Vitality");
            near(pts[Stat.SENSES.ordinal()], row[5], 1.0, label + " Senses");
            near(pts[Stat.SWIFTNESS.ordinal()], row[6], 1.0, label + " Swiftness");
            near(q, row[2], 0.06, label + " quality");
        }
        // it grows without a ceiling but does not run away: quality stays modest after 200 generations
        {
            double bloom = 0, q = 0;
            double atFortyBloom = 0, atEightyBloom = 0;
            for (int gen = 1; gen <= 200; gen++) {
                q = 2 + Drift.STARBORN_FACTOR * Math.sqrt(Math.max(0, bloom));
                bloom += Drift.drift(bloomLine, bloomLine, q)[Stat.BLOOM.ordinal()];
                if (gen == 40) atFortyBloom = bloom;
                if (gen == 80) atEightyBloom = bloom;
            }
            check(atFortyBloom > 200 && atFortyBloom < 400, "Bloom after 40 generations at tier 2 stays in the hundreds: " + atFortyBloom);
            check(atEightyBloom > atFortyBloom * 1.5 && atEightyBloom < atFortyBloom * 4, "and grows steadily, not explosively: " + atFortyBloom + " -> " + atEightyBloom);
            check(bloom > atEightyBloom && q < 20, "no ceiling, but quality after 200 generations is only " + q);
        }
        // the same loop with a LINEAR bonus would run away (why the square root is essential; also documents the 1770 in DESIGN)
        {
            double bloom = 0;
            for (int gen = 1; gen <= 40; gen++) {
                double q = 2 + 0.1 * bloom;
                bloom += Drift.drift(bloomLine, bloomLine, q)[Stat.BLOOM.ordinal()];
            }
            check(bloom > 1000, "a linear bonus would run away (Bloom " + bloom + " after 40 generations)");
        }
    }

    // ---------------------------------------------------------------------------------------------
    static void seeds() {
        System.out.println("seeds (Phase 4c)");
        Random rng = new Random(4242);
        Genome avg = g("00-00-00-00-00-00"), stocky = g("00-00-00-11-00-00");
        Genome shy = g("00-00-00-00-00-22"), swarm = g("00-00-00-00-00-33");
        Genome starborn = g("00-aa-00-00-00-44");
        Stats zero = Stats.ZERO;

        // --- canonical text form, exact
        Seed s = new Seed(stocky, Stats.of(0, 0, 0, 0, 0), stocky, Stats.of(2, -1, 0, 0, 0), 1.5);
        check(s.toString().equals("00-00-00-11-00-00-00/0,0,0,0,0|00-00-00-11-00-00-00/2,-1,0,0,0|1.5"), "canonical text, got " + s);
        check(Seed.parse(s.toString()).equals(Optional.of(s)), "canonical text parses back to the same seed");

        // --- round trip for random seeds: every genome, stats vector and quality survives text
        List<Lineage> lineages = List.of(Lineage.values());
        for (int i = 0; i < 3000; i++) {
            Genome a = Genome.randomFounder(lineages.get(rng.nextInt(lineages.size())), rng);
            Genome b = Genome.randomFounder(lineages.get(rng.nextInt(lineages.size())), rng);
            Stats sa = Stats.of(rng.nextInt(300) - 60, rng.nextInt(300) - 60, rng.nextInt(80) - 20, rng.nextInt(300) - 60, rng.nextInt(400) - 60);
            Stats sb = Stats.of(rng.nextInt(300) - 60, rng.nextInt(300) - 60, rng.nextInt(80) - 20, rng.nextInt(300) - 60, rng.nextInt(400) - 60);
            double q = i % 7 == 0 ? 0.0 : rng.nextDouble() * 12.0;
            Seed seed = new Seed(a, sa, b, sb, q);
            check(Seed.parse(seed.toString()).equals(Optional.of(seed)), "random seed survives its text form: " + seed);
            if (failures > 20) return;
        }

        // --- lenient input: stats and quality are optional, whitespace is ignored
        Seed bare = Seed.parse("00-00-00-00-00-00|22-22-22-22-22-22").orElseThrow();
        check(bare.mother().equals(avg) && bare.father().equals(g("22-22-22-22-22-22")), "bare seed: two genomes");
        check(bare.motherStats().isZero() && bare.fatherStats().isZero() && bare.quality() == 0.0, "bare seed: zero stats and quality");
        Seed slashOnly = Seed.parse("00-00-00-00-00-00/|22-22-22-22-22-22/|").orElseThrow();
        check(slashOnly.equals(bare), "empty stats and empty quality read as zero");
        Seed quality = Seed.parse("00-00-00-00-00-00|22-22-22-22-22-22|2.5").orElseThrow();
        check(quality.quality() == 2.5 && quality.motherStats().isZero(), "quality without stats");
        Seed padded = Seed.parse("  00-00-00-00-00-00/3,-1 | 22-22-22-22-22-22/0,0,0,0,7 | 1  ").orElseThrow();
        check(padded.motherStats().equals(Stats.of(3, -1)) && padded.fatherStats().equals(Stats.of(0, 0, 0, 0, 7)) && padded.quality() == 1.0,
                "short stats are padded with zeros, spaces around parts are ignored");
        check(Seed.parse("00-00-00-00-00-00|22-22-22-22-22-22|-3").orElseThrow().quality() == 0.0, "a negative quality is clamped to 0");

        // --- junk is rejected, never guessed at
        String ok = "00-00-00-00-00-00";
        for (String junk : new String[] {"", "   ", "garbage", ok, ok + "|", "|" + ok, ok + "|" + ok + "|1|2", "zz-00-00-00-00-00|" + ok,
                ok + "|ff-ff-ff-ff-ff-ff", ok + "/1,2,3,4,5,6|" + ok, ok + "/a,b|" + ok, ok + "|" + ok + "/x", ok + "|" + ok + "|abc",
                ok + "|" + ok + "|NaN", ok + "|" + ok + "|Infinity", ok + "|" + ok + "|-Infinity", ok + "|" + ok + "|1,5"}) {
            check(Seed.parse(junk).isEmpty(), "junk must not parse: '" + junk + "'");
        }
        check(Seed.parse(null).isEmpty(), "null does not parse");

        // --- the constructor keeps quality sane and refuses missing parts
        check(new Seed(avg, zero, avg, zero, Double.NaN).quality() == 0.0, "NaN quality becomes 0");
        check(new Seed(avg, zero, avg, zero, -5).quality() == 0.0, "negative quality becomes 0");
        check(new Seed(avg, zero, avg, zero, Double.POSITIVE_INFINITY).quality() == Seed.QUALITY_LIMIT, "infinite quality is capped");
        boolean threw = false;
        try {
            new Seed(null, zero, avg, zero, 0);
        } catch (NullPointerException e) {
            threw = true;
        }
        check(threw, "a seed needs both parents");

        // --- Seed.of: quality = guava tier + the parents' average starborn bonus
        check(Seed.of(avg, zero, avg, zero, 0).quality() == 0.0, "plain pair, no guava tier: quality 0");
        check(Seed.of(avg, zero, avg, zero, 2.5).quality() == 2.5, "guava tier becomes quality");
        check(Seed.of(avg, zero, avg, zero, -1).quality() == 0.0, "a negative tier does not lower quality");
        Stats bloom100 = Stats.of(0, 0, 0, 0, 100);
        near(Seed.of(starborn, bloom100, starborn, bloom100, 0).quality(), 3.0, 1e-9, "starborn pair with Bloom 100: 0.3 x sqrt(100) = 3");
        near(Seed.of(starborn, bloom100, avg, zero, 0).quality(), 1.5, 1e-9, "one starborn parent gives half the bonus");
        near(Seed.of(starborn, bloom100, starborn, bloom100, 2).quality(), 5.0, 1e-9, "tier and starborn bonus add up");
        near(Seed.of(starborn, bloom100, avg, zero, 0).quality(), Drift.pairQualityBonus(starborn, bloom100, avg, zero), 1e-12, "same bonus as Drift");
        check(Seed.of(avg, Stats.of(0, 0, 0, 0, 100), avg, Stats.of(0, 0, 0, 0, 100), 0).quality() == 0.0, "Bloom without starborn adds no quality");

        // --- hatching: litter size follows Breeding (same numbers as the Phase 2 litter)
        for (int i = 0; i < 2000; i++) {
            check(new Seed(avg, zero, avg, zero, 0).hatch(0, 0, rng).size() == 2, "average x average seed hatches 2 pups");
            check(new Seed(swarm, zero, swarm, zero, 0).hatch(0, 0, rng).size() == 4, "swarm x swarm seed hatches 4 pups");
            if (failures > 20) return;
        }
        long shySum = 0;
        int trials = 100000;
        Seed shySeed = new Seed(shy, zero, shy, zero, 0);
        for (int i = 0; i < trials; i++) {
            shySum += shySeed.hatch(0, 0, rng).size();
        }
        near(shySum / (double) trials, 1.2, 0.01, "shy seed litter mean");
        // crowding guard moved to the hatching: room for 0, 1 and 2
        Seed avgSeed = new Seed(avg, zero, avg, zero, 0);
        check(avgSeed.hatch(0, Breeding.CROWD_LIMIT, rng).isEmpty(), "crowded: nothing hatches");
        check(avgSeed.hatch(0, Breeding.CROWD_LIMIT + 30, rng).isEmpty(), "over crowded: nothing hatches");
        check(avgSeed.hatch(0, Breeding.CROWD_LIMIT - 1, rng).size() == 1, "room for one: one pup");
        check(avgSeed.hatch(0, Breeding.CROWD_LIMIT - 2, rng).size() == 2, "room for two: two pups");

        // --- hatching: every pup gets one allele from each parent (Mendel), a mutation changes at most one slot
        Genome mixed = g("13-27-02-31-20-12"), other = g("31-72-20-13-02-21");
        Seed mixedSeed = new Seed(mixed, zero, other, zero, 0);
        int mutants = 0, pupsSeen = 0;
        for (int i = 0; i < 20000; i++) {
            for (Seed.Pup pup : mixedSeed.hatch(0, 0, rng)) {
                pupsSeen++;
                int bad = 0;
                for (Gene gene : Gene.values()) {
                    int m = pup.genome().alleleId(gene, 0), f = pup.genome().alleleId(gene, 1);
                    if (m != mixed.alleleId(gene, 0) && m != mixed.alleleId(gene, 1)) bad++;
                    if (f != other.alleleId(gene, 0) && f != other.alleleId(gene, 1)) bad++;
                }
                if (pup.mutated()) {
                    mutants++;
                    check(bad <= 1, "a mutated pup differs from its parents in at most one slot, got " + bad);
                } else {
                    check(bad == 0, "an unmutated pup only carries its parents' alleles, got " + bad + " foreign in " + pup.genome());
                }
                if (failures > 20) return;
            }
        }
        near(mutants / (double) pupsSeen, mixedSeed.mutationChance(), 0.004, "observed mutation rate matches the seed's mutation chance");

        // --- hatching: carriers give 1 in 4 recessive pups (the Phase 2 regression #3, through the seed path)
        Genome carrier = g("02-00-00-00-00-00");
        Seed carriers = new Seed(carrier, zero, carrier, zero, 0);
        int short_ = 0, total = 0;
        for (int i = 0; i < 60000; i++) {
            for (Seed.Pup pup : carriers.hatch(0, 0, rng)) {
                total++;
                if (pup.genome().expressed(Gene.EARS).name().equals("short")) short_++;
            }
        }
        near(short_ / (double) total, 0.25, 0.005, "two carriers: about 1 in 4 pups show the recessive");

        // --- hatching: stats = parents' average + drift, exact when the numbers are whole
        Seed sturdy0 = new Seed(stocky, zero, stocky, zero, 0.0);
        Seed sturdy4 = new Seed(stocky, zero, stocky, zero, 4.0);
        for (int i = 0; i < 300; i++) {
            for (Seed.Pup pup : sturdy0.hatch(0, 0, rng)) {
                check(pup.stats().equals(Stats.of(1, -1, 0, 0, 0)), "stocky pair at quality 0: Vitality +1, Swiftness -1, got " + pup.stats());
            }
            for (Seed.Pup pup : sturdy4.hatch(0, 0, rng)) {
                check(pup.stats().equals(Stats.of(3, 0, 0, 0, 0)), "stocky pair at quality 4: Vitality +3, no Swiftness cost, got " + pup.stats());
            }
            for (Seed.Pup pup : sturdy0.hatch(4.0, 0, rng)) {
                check(pup.stats().equals(Stats.of(3, 0, 0, 0, 0)), "Fortune bonus 4 on a plain seed equals a quality 4 seed, got " + pup.stats());
            }
            for (Seed.Pup pup : sturdy0.hatch(-9.0, 0, rng)) {
                check(pup.stats().equals(Stats.of(1, -1, 0, 0, 0)), "a negative bonus never lowers quality, got " + pup.stats());
            }
            if (failures > 20) return;
        }
        near(sturdy0.effectiveQuality(1.5), 1.5, 1e-12, "effective quality = seed + bonus");
        near(sturdy4.effectiveQuality(1.5), 5.5, 1e-12, "effective quality of a quality-4 seed with a bonus of 1.5");
        // parents' points average, and odd sums round both ways evenly
        Seed evenPair = new Seed(avg, Stats.of(10, 0, 0, 0, 0), avg, Stats.of(20, 0, 0, 0, 0), 0);
        Seed oddPair = new Seed(avg, Stats.of(10, 0, 0, 0, 0), avg, Stats.of(11, 0, 0, 0, 0), 0);
        long vitSum = 0;
        int vitN = 0;
        for (int i = 0; i < 50000; i++) {
            for (Seed.Pup pup : evenPair.hatch(0, 0, rng)) {
                check(pup.stats().get(Stat.VITALITY) == 15, "average of 10 and 20 is exactly 15");
            }
            for (Seed.Pup pup : oddPair.hatch(0, 0, rng)) {
                int v = pup.stats().get(Stat.VITALITY);
                check(v == 10 || v == 11, "average of 10 and 11 rounds to 10 or 11, got " + v);
                vitSum += v;
                vitN++;
            }
            if (failures > 20) return;
        }
        near(vitSum / (double) vitN, 10.5, 0.01, "unbiased rounding of 10.5");

        // --- Fortune (Phase 4d): 0.5 quality per level of the harvesting tool, never negative
        check(Seed.fortuneBonus(0) == 0.0 && Seed.fortuneBonus(-2) == 0.0, "no Fortune, no bonus (and never a penalty)");
        near(Seed.fortuneBonus(1), 0.5, 1e-12, "Fortune I adds 0.5");
        near(Seed.fortuneBonus(3), 1.5, 1e-12, "Fortune III adds 1.5");
        for (int level = 0; level <= 3; level++) {
            // a harvest with Fortune must be exactly a seed that was better by that much
            Seed better = new Seed(stocky, zero, stocky, zero, Seed.fortuneBonus(level));
            check(sturdy0.effectiveQuality(Seed.fortuneBonus(level)) == better.quality(), "Fortune " + level + " at harvest equals the same quality in the seed");
            check(java.util.Arrays.equals(sturdy0.drift(Seed.fortuneBonus(level)), better.drift(0.0)), "Fortune " + level + " gives the same drift as that quality");
        }
        double[] fortune3 = sturdy0.drift(Seed.fortuneBonus(3));
        near(fortune3[Stat.VITALITY.ordinal()], 1.75, 1e-9, "Fortune III on a plain stocky seed: Vitality gain 1 x (1 + 0.5 x 1.5) = 1.75");
        near(fortune3[Stat.SWIFTNESS.ordinal()], -0.625, 1e-9, "Fortune III on a plain stocky seed: Swiftness cost 0.625");

        // --- determinism: the same random source hatches the same litter
        Random r1 = new Random(77), r2 = new Random(77);
        for (int i = 0; i < 300; i++) {
            check(mixedSeed.hatch(0, 3, r1).equals(mixedSeed.hatch(0, 3, r2)), "same seed, same litter");
        }

        // --- the whole loop through the seed API: a pure stocky line, one generation per seed
        for (double tier : new double[] {0.0, 4.0}) {
            Genome mother = stocky, father = stocky;
            Stats ms = zero, fs = zero;
            int generations = 0;
            for (int guard = 0; generations < 10 && guard < 200; guard++) {
                Seed next = Seed.of(mother, ms, father, fs, tier);
                Seed.Pup pick = null;
                for (Seed.Pup pup : next.hatch(0, 0, rng)) {
                    if (!pup.mutated()) {
                        pick = pup;
                        break;
                    }
                }
                if (pick == null) continue;         // every pup mutated (4% each): try the next seed
                mother = pick.genome();
                father = pick.genome();
                ms = pick.stats();
                fs = pick.stats();
                generations++;
            }
            check(generations == 10, "loop ran 10 generations");
            if (tier == 0.0) {
                check(ms.get(Stat.VITALITY) == 10 && ms.get(Stat.SWIFTNESS) == -10, "tier 0, 10 generations: Vitality +10, Swiftness -10, got " + ms);
            } else {
                check(ms.get(Stat.VITALITY) == 30 && ms.get(Stat.SWIFTNESS) == 0, "tier 4, 10 generations: Vitality +30, no Swiftness cost, got " + ms);
            }
        }

        // --- the readable summary (item tooltip) and the drift text
        check(Seed.driftText(new double[Stat.COUNT]).equals("none"), "no drift reads 'none'");
        check(Seed.driftText(sturdy0.drift(0)).equals("Vitality +1.0, Speed -1.0"), "stocky drift text, got " + Seed.driftText(sturdy0.drift(0)));
        List<String> lines = new Seed(stocky, zero, stocky, zero, 1.5).summary();
        check(lines.size() == 4, "summary has four lines");
        check(lines.get(0).equals("Quality 1.50"), "summary quality line, got " + lines.get(0));
        check(lines.get(1).equals("Litter of about 2.0 pups"), "summary litter line, got " + lines.get(1));
        check(lines.get(2).equals("Mutation chance 4.0% per pup"), "summary mutation line, got " + lines.get(2));
        // quality 1.5: gain x (1 + 0.5 x 1.5) = 1.75, cost x max(0, 1 - 0.25 x 1.5) = 0.625; shown with one decimal
        double[] q15 = new Seed(stocky, zero, stocky, zero, 1.5).drift(0);
        near(q15[Stat.VITALITY.ordinal()], 1.75, 1e-9, "quality 1.5 raises the stocky Vitality gain to 1.75");
        near(q15[Stat.SWIFTNESS.ordinal()], -0.625, 1e-9, "quality 1.5 shrinks the stocky Swiftness cost to 0.625");
        check(lines.get(3).equals("Stats drift: Vitality +1.8, Speed -0.6"), "summary drift line, got " + lines.get(3));

        // --- a comma-decimal locale must not change the saved text (the player's PC may use one)
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            Seed de = new Seed(stocky, Stats.of(1, 2, 3, 4, 5), avg, zero, 2.75);
            check(de.toString().endsWith("|2.75"), "text form uses a decimal point in any locale, got " + de);
            check(Seed.parse(de.toString()).equals(Optional.of(de)), "and parses back in any locale");
            check(de.summary().get(0).equals("Quality 2.75"), "summary too, got " + de.summary().get(0));
        } finally {
            Locale.setDefault(before);
        }
    }


    // ---------------------------------------------------------------------------------------------
    static void guavaTiers() {
        System.out.println("guava tiers (Phase 4e, quality table of session 6)");
        Random rng = new Random(4747);
        GuavaTier[] all = GuavaTier.values();
        check(all.length == 8, "eight tiers: plain, golden, diamond, netherite, nether star, beacon, dragon egg, command block, got " + all.length);
        String[] expectedIds = {"guava", "golden_guava", "diamond_guava", "netherite_guava", "nether_star_guava", "beacon_guava", "dragon_egg_guava", "command_block_guava"};
        double[] Q = {0, 1, 2, 4, 7, 12, 20, 33};             // the quality table (session 6, command block added in session 7); levels 0..7 (saved by the bunnidog)
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < all.length; i++) {
            GuavaTier t = all[i];
            check(t.level() == i, t + ": level is its place on the ladder, got " + t.level());
            check(t.quality() == Q[i], t + ": quality " + Q[i] + ", got " + t.quality());
            check(t.qualityText().equals(Integer.toString((int) Q[i])), t + ": whole qualities print without a decimal point, got " + t.qualityText());
            check(t.id().equals(expectedIds[i]), t + ": id " + expectedIds[i] + ", got " + t.id());
            check(ids.add(t.id()), t + ": id is unique");
            check(t.id().matches("[a-z][a-z0-9_]*"), t + ": id is a legal resource path");
            check(!t.displayName().isBlank(), t + ": has a display name");
            check(GuavaTier.ofLevel(i) == t, t + ": ofLevel round trip");
            check(i == 0 ? t.previous() == null : t.previous() == all[i - 1], t + ": previous tier");
            check(i == all.length - 1 ? t.next() == null : t.next() == all[i + 1], t + ": next tier");
        }
        check(GuavaTier.ofLevel(-5) == GuavaTier.PLAIN && GuavaTier.ofLevel(99) == GuavaTier.COMMAND_BLOCK, "ofLevel clamps to the ends of the ladder");

        // --- the SHAPE of the table: what a tier is worth must keep up with what it costs (session 6)
        check(GuavaTier.PLAIN.quality() == 0.0, "the plain guava adds no quality");
        for (int i = 1; i < all.length; i++) {
            check(all[i].quality() > all[i - 1].quality(), all[i] + ": every tier is worth more than the one below");
            if (i >= 2) {
                double step = all[i].quality() - all[i - 1].quality();
                double before = all[i - 1].quality() - all[i - 2].quality();
                check(step >= before, all[i] + ": the quality step up (" + step + ") is never smaller than the step before it (" + before + ")");
            }
        }
        // The rule behind the numbers: for the standard +1/-1 trend the net drift per generation is gain - cost. Quality points
        // buy 0.75 each up to quality 4 (costs still shrinking) but only 0.5 beyond it (costs gone), so a straight 0..6 ladder
        // bought LESS per tier from nether star on. The table must make each tier add at least as much net drift as the last.
        double previousGain = 0.0;
        double previousNet = 0.0;
        for (int i = 1; i < all.length; i++) {
            double q = all[i].quality();
            double net = Drift.gainFactor(q) - Drift.costFactor(q);
            double gained = net - (Drift.gainFactor(all[i - 1].quality()) - Drift.costFactor(all[i - 1].quality()));
            check(gained >= previousGain - 1e-9, all[i] + ": adds " + gained + " net drift, at least what the tier below added (" + previousGain + ")");
            previousGain = gained;
            previousNet = net;
        }
        near(Drift.gainFactor(GuavaTier.DRAGON_EGG.quality()) - Drift.costFactor(GuavaTier.DRAGON_EGG.quality()), 11.0, 1e-9, "the dragon egg guava alone makes a +1/-1 line gain 11 per generation (x(1 + 0.5 x 20), no cost)");
        near(previousNet, 17.5, 1e-9, "the command block guava (the top) makes a +1/-1 line gain 17.5 per generation (x(1 + 0.5 x 33), no cost), 6.5 more than the egg");
        check(GuavaTier.COMMAND_BLOCK.quality() - GuavaTier.DRAGON_EGG.quality() == 13.0, "the command block adds the next Fibonacci-style step, 13 on top of the egg's 20");
        double[] straight = {0, 1, 2, 3, 4, 5, 6};           // the old table: proves the rule above has teeth
        boolean oldBroke = false;
        double oldPrev = 0.0;
        for (int i = 1; i < straight.length; i++) {
            double gained = (Drift.gainFactor(straight[i]) - Drift.costFactor(straight[i])) - (Drift.gainFactor(straight[i - 1]) - Drift.costFactor(straight[i - 1]));
            if (gained < oldPrev - 1e-9) oldBroke = true;
            oldPrev = gained;
        }
        check(oldBroke, "the old straight 0..6 table would FAIL the net-drift rule (the rule is not vacuous)");
        check(Drift.costFactor(GuavaTier.DIAMOND.quality()) > 0.0 && Drift.costFactor(GuavaTier.NETHERITE.quality()) == 0.0,
                "netherite is the first tier that removes every cost (its 32 ancient debris buy the no-downside milestone)");
        check(GuavaTier.BEACON.quality() > Drift.STARBORN_FACTOR * Math.sqrt(1000) && GuavaTier.DRAGON_EGG.quality() > Drift.STARBORN_FACTOR * Math.sqrt(1000),
                "the two absurd tiers beat what a starborn line gets out of Bloom 1000 (0.3 x sqrt = 9.5)");
        check(GuavaTier.NETHER_STAR.quality() - GuavaTier.NETHERITE.quality() == 3.0 && GuavaTier.BEACON.quality() - GuavaTier.NETHER_STAR.quality() == 5.0,
                "nether star adds 3 (4 stars = 0.75 a star), beacon adds 5 (8 more stars = 0.63 a star)");

        // --- the quality of a seed: the AVERAGE of the two parents' QUALITIES (fractions allowed), never the sum, never the levels
        for (int a = 0; a <= 7; a++) {
            for (int b = 0; b <= 7; b++) {
                double avg = GuavaTier.average(a, b);
                check(avg == (Q[a] + Q[b]) / 2.0, "average(" + a + "," + b + ")");
                check(avg == GuavaTier.average(b, a), "average is symmetric");
                check(avg >= Math.min(Q[a], Q[b]) && avg <= Math.max(Q[a], Q[b]), "average lies between the two tiers' qualities");
            }
        }
        check(GuavaTier.average(3, 4) == 5.5, "netherite + nether star guavas (4 and 7) give 5.5");
        check(GuavaTier.average(-4, 2) == 1.0 && GuavaTier.average(-4, -9) == 0.0, "a level below the ladder counts as the plain guava");
        check(GuavaTier.average(99, 0) == 16.5 && GuavaTier.average(7, 0) == 16.5 && GuavaTier.average(6, 0) == 10.0, "a level above the ladder counts as the top (33, so 16.5 with a plain guava), and a dragon egg with a plain guava is 10");

        // --- through the seed: tier -> quality, and quality has ONE job (the stat drift)
        Genome plain = g("00-00-00-00-00-00"), stocky = g("00-00-00-11-00-00"), starborn = g("00-aa-00-00-00-44");
        Stats zero = Stats.ZERO, bloom100 = Stats.of(0, 0, 0, 0, 100);
        Seed baseline = Seed.of(stocky, zero, stocky, zero, 0.0);
        for (int a = 0; a <= 7; a++) {
            for (int b = 0; b <= 7; b++) {
                double q = GuavaTier.average(a, b);
                Seed seed = Seed.of(stocky, zero, stocky, zero, q);
                check(seed.quality() == q, "tiers " + a + "+" + b + ": ordinary genes add no bonus, quality " + q + ", got " + seed.quality());
                double[] drift = seed.drift(0.0);
                near(drift[Stat.VITALITY.ordinal()], 1.0 * (1.0 + 0.5 * q), 1e-9, "tiers " + a + "+" + b + ": Vitality gain x(1 + 0.5q)");
                near(drift[Stat.SWIFTNESS.ordinal()], -1.0 * Math.max(0.0, 1.0 - 0.25 * q), 1e-9, "tiers " + a + "+" + b + ": Swiftness cost x max(0, 1 - 0.25q)");
                if (q >= 4.0) {
                    check(drift[Stat.SWIFTNESS.ordinal()] == 0.0, "tiers " + a + "+" + b + ": costs are gone from quality 4");
                }
                // ONE job: the litter and the mutation chance are exactly those of a quality-0 seed
                check(seed.expectedLitter() == baseline.expectedLitter(), "tiers " + a + "+" + b + ": litter size untouched");
                check(seed.mutationChance() == baseline.mutationChance(), "tiers " + a + "+" + b + ": mutation chance untouched");
            }
        }
        check(java.util.Arrays.equals(Seed.of(stocky, zero, stocky, zero, GuavaTier.average(3, 3)).drift(0.0),
                Seed.of(stocky, zero, stocky, zero, GuavaTier.average(3, 3)).drift(0.0)) && GuavaTier.average(3, 3) == 4.0, "two netherite guavas make a quality-4 seed");
        check(GuavaTier.average(6, 0) != GuavaTier.average(3, 3), "a dragon egg and a plain guava (10) are NOT two netherite ones (4): the ladder is not linear any more");
        near(Seed.of(starborn, bloom100, starborn, bloom100, GuavaTier.average(2, 4)).quality(), 7.5, 1e-9, "tiers 2+4 (quality 4.5) and the starborn bonus 3 add up to 7.5");

        // --- Fortune at harvest stacks on the guava part (golden and diamond kept their 1 and 2 on purpose)
        Seed golden = Seed.of(stocky, zero, stocky, zero, GuavaTier.average(1, 2));      // 1.5
        near(golden.effectiveQuality(Seed.fortuneBonus(3)), 3.0, 1e-9, "golden+diamond guavas (1.5) with a Fortune III tool (+1.5) harvest at 3.0");
        near(golden.effectiveQuality(Seed.fortuneBonus(0)), 1.5, 1e-9, "no Fortune: the seed's own quality");

        // --- a real ladder: every generation on netherite guavas (quality 4: gain x3, no cost) gains exactly 3 Vitality
        Genome mother = stocky, father = stocky;
        Stats ms = zero, fs = zero;
        int generations = 0;
        for (int guard = 0; generations < 5 && guard < 200; guard++) {
            Seed next = Seed.of(mother, ms, father, fs, GuavaTier.average(3, 3));
            Seed.Pup pick = null;
            for (Seed.Pup pup : next.hatch(0, 0, rng)) {
                if (!pup.mutated()) {
                    pick = pup;
                    break;
                }
            }
            if (pick == null) continue;
            mother = pick.genome();
            father = pick.genome();
            ms = pick.stats();
            fs = pick.stats();
            generations++;
        }
        check(generations == 5 && ms.get(Stat.VITALITY) == 15 && ms.get(Stat.SWIFTNESS) == 0, "5 generations on netherite guavas: Vitality +15, Swiftness 0, got " + ms);
        // ... and on nether star guavas (quality 7: gain x4.5): 4.5 a generation, rounded at random, so 20..25 after five
        mother = stocky;
        father = stocky;
        ms = zero;
        fs = zero;
        generations = 0;
        for (int guard = 0; generations < 5 && guard < 200; guard++) {
            Seed next = Seed.of(mother, ms, father, fs, GuavaTier.average(4, 4));
            Seed.Pup pick = null;
            for (Seed.Pup pup : next.hatch(0, 0, rng)) {
                if (!pup.mutated()) {
                    pick = pup;
                    break;
                }
            }
            if (pick == null) continue;
            mother = pick.genome();
            father = pick.genome();
            ms = pick.stats();
            fs = pick.stats();
            generations++;
        }
        check(generations == 5 && ms.get(Stat.VITALITY) >= 20 && ms.get(Stat.VITALITY) <= 25 && ms.get(Stat.SWIFTNESS) == 0,
                "5 generations on nether star guavas: Vitality between 20 and 25 (4.5 a generation), Swiftness 0, got " + ms);
    }


    // ---------------------------------------------------------------------------------------------
    static final java.util.Set<String> VANILLA_EFFECT_IDS = java.util.Set.of(
            "minecraft:speed", "minecraft:slowness", "minecraft:strength", "minecraft:jump_boost", "minecraft:regeneration",
            "minecraft:resistance", "minecraft:fire_resistance", "minecraft:water_breathing", "minecraft:invisibility",
            "minecraft:night_vision", "minecraft:absorption", "minecraft:glowing", "minecraft:levitation", "minecraft:luck",
            "minecraft:slow_falling", "minecraft:dolphins_grace", "minecraft:saturation");

    static void juice() {
        System.out.println("guava juice and infusion (Phase 4f)");
        Random rng = new Random(4848);

        // --- the table agrees with the gene tables
        for (var entry : Juice.DEFS.entrySet()) {
            String[] parts = entry.getKey().split(":");
            Gene gene = null;
            for (Gene gn : Gene.values()) if (gn.key().equals(parts[0])) gene = gn;
            check(gene != null, entry.getKey() + ": the gene exists");
            if (gene == null) continue;
            check(gene.alleles().stream().anyMatch(a -> a.name().equals(parts[1])), entry.getKey() + ": the allele exists");
            check(!entry.getValue().isEmpty(), entry.getKey() + ": has at least one effect");
        }
        // every wild and rare allele does something (spotted is the one deliberate blank: a joke coat)
        for (Gene gene : Gene.values()) {
            for (Allele a : gene.alleles()) {
                String key = Juice.key(gene, a);
                if ((a.isWild() || a.isRare()) && gene != Gene.DIET && !key.equals("coat:spotted")) {
                    check(Juice.DEFS.containsKey(key), key + ": a wild or rare allele has a juice effect");
                }
            }
        }
        // and the plain ones that must NOT (so most juices are modest): standard/average/tan/cream/brown/short/prolific/shy
        for (String plain : new String[] {"ears:standard", "ears:short", "coat:tan", "coat:cream", "coat:brown", "coat:spotted",
                "legs:standard", "legs:short", "build:average", "fertility:average", "fertility:prolific", "fertility:shy"}) {
            check(!Juice.DEFS.containsKey(plain), plain + ": a plain allele has no effect");
        }
        check(Juice.DEFS.size() == 30, "30 alleles have effects, got " + Juice.DEFS.size());

        // --- every def: real vanilla ids, sane caps, a drawback never scales
        for (var entry : Juice.DEFS.entrySet()) {
            for (Juice.Def def : entry.getValue()) {
                String what = entry.getKey() + " " + def.id() + ": ";
                check(VANILLA_EFFECT_IDS.contains(def.id()), what + "is a known vanilla effect id");
                check(def.baseSeconds() > 0 && def.maxSeconds() >= def.baseSeconds(), what + "base <= max seconds");
                check(def.maxAmp() >= 0 && def.maxAmp() <= 4, what + "amplifier cap 0..4");
                check(!def.name().isBlank(), what + "has a name");
                if (def.id().equals("minecraft:slowness")) {
                    check(def.maxAmp() == 0 && def.maxSeconds() <= 90, what + "the drawback never scales and stays short");
                }
                if (def.id().equals("minecraft:levitation")) {
                    check(def.maxAmp() == 0 && def.maxSeconds() <= 6, what + "levitation is a blink");
                }
                if (def.id().equals("minecraft:resistance")) {
                    check(def.maxAmp() <= 2, what + "resistance stops at III (never invulnerable)");
                }
                if (def.target() == Juice.Target.NEARBY) {
                    check(def.id().equals("minecraft:glowing"), what + "only Glowing is aimed at the mobs around");
                }
                // monotone in quality and in bred points; capped
                int lastAmp = -1;
                int lastTicks = -1;
                for (double q = 0; q <= 40; q += 0.5) {
                    Juice.Spec sp = Juice.spec(def, q, Stats.ZERO);
                    check(sp.amplifier() >= lastAmp && sp.ticks() >= lastTicks, what + "never weaker with more quality (q " + q + ")");
                    check(sp.amplifier() <= def.maxAmp() && sp.ticks() <= def.maxSeconds() * 20, what + "stays under its caps (q " + q + ")");
                    lastAmp = sp.amplifier();
                    lastTicks = sp.ticks();
                }
                if (def.stat() != null) {
                    int last = -1;
                    for (int pts = -5; pts <= 200; pts += 5) {
                        Juice.Spec sp = Juice.spec(def, 3.0, Stats.ZERO.with(def.stat(), pts));
                        check(sp.ticks() >= last && sp.ticks() <= def.maxSeconds() * 20, what + "more bred points never shorten it (" + pts + ")");
                        last = sp.ticks();
                    }
                    check(Juice.spec(def, 0, Stats.ZERO.with(def.stat(), -50)).ticks() == Juice.spec(def, 0, Stats.ZERO).ticks(), what + "negative points do not shorten it");
                } else {
                    check(Juice.spec(def, 2, Stats.of(99, 99, 99, 99, 99)).ticks() == Juice.spec(def, 2, Stats.ZERO).ticks(), what + "without a stat, points change nothing");
                }
                if (def.maxAmp() == 0) {
                    check(Juice.spec(def, 100, Stats.ZERO).amplifier() == 0, what + "an effect with cap 0 never scales");
                }
            }
        }

        // --- the numbers
        check(Juice.amplifierFor(0) == 0 && Juice.amplifierFor(3.99) == 0 && Juice.amplifierFor(4) == 1 && Juice.amplifierFor(7) == 1
                && Juice.amplifierFor(12) == 3 && Juice.amplifierFor(20) == 5 && Juice.amplifierFor(-3) == 0, "amplifier = floor(quality / 4)");
        near(Juice.durationFactor(0, 0), 1.0, 1e-12, "duration factor at nothing is 1");
        near(Juice.durationFactor(4, 25), 1.4 * 1.5, 1e-12, "quality 4 and 25 points: x1.4 x1.5");
        near(Juice.durationFactor(0, 1000), 2.0, 1e-12, "the stat bonus is capped at doubling");
        near(Juice.durationFactor(10, 0), 2.0, 1e-12, "quality 10 doubles the duration");

        Genome plain = g("00-00-00-00-00-00");
        List<Juice.Spec> bland = Juice.effects(plain, Stats.ZERO, 0);
        check(bland.size() == 1 && bland.get(0).id().equals("minecraft:regeneration") && bland.get(0).amplifier() == 0 && bland.get(0).ticks() == 400,
                "a plain loyal pup: Regeneration I for 20 s");
        check(bland.get(0).describe().equals("Regeneration 0:20"), "describe: 'Regeneration 0:20', got " + bland.get(0).describe());
        List<Juice.Spec> egg = Juice.effects(plain, Stats.ZERO, GuavaTier.DRAGON_EGG.quality());
        check(egg.get(0).amplifier() == 3 && egg.get(0).ticks() == 1200 && egg.get(0).describe().equals("Regeneration IV 1:00"),
                "the dragon egg tier (20): Regeneration IV for 1:00 (cap IV, 20 s x 3), got " + egg.get(0).describe());
        // starborn: both luck and glowing; swarm luck only
        List<Juice.Spec> star = Juice.effects(g("00-00-00-00-00-44"), Stats.of(0, 0, 0, 0, 50), 4);
        Juice.Spec starLuck = star.stream().filter(sp -> sp.id().equals("minecraft:luck")).findFirst().orElseThrow();
        check(starLuck.amplifier() == 1 && starLuck.ticks() == 33600,
                "starborn with Bloom 50 at quality 4: Luck II for 600 s x 1.4 x 2 = 28 minutes (33600 ticks), got " + starLuck.amplifier() + "/" + starLuck.ticks());
        near(Juice.effects(g("00-00-00-00-00-44"), Stats.of(0, 0, 0, 0, 50), 20).stream().filter(sp -> sp.id().equals("minecraft:luck")).findFirst().orElseThrow().ticks(), 36000, 0, "the 30-minute cap holds at quality 20");
        check(star.stream().anyMatch(sp -> sp.id().equals("minecraft:glowing") && sp.target() == Juice.Target.SELF), "starborn glows");

        // two genes with the same effect: one entry, the stronger numbers
        Genome tough = g("00-00-00-44-33-00");               // woolly build (resistance + absorption) + stoic temperament (resistance + slowness)
        List<Juice.Spec> merged = Juice.effects(tough, Stats.ZERO, 8);
        java.util.Map<String, Integer> count = new java.util.HashMap<>();
        for (Juice.Spec sp : merged) count.merge(sp.id() + "/" + sp.target(), 1, Integer::sum);
        check(count.values().stream().allMatch(n -> n == 1), "one entry per effect, got " + count);
        Juice.Spec resist = merged.stream().filter(sp -> sp.id().equals("minecraft:resistance")).findFirst().orElseThrow();
        check(resist.amplifier() == 2 && resist.ticks() == Math.round(60 * Juice.durationFactor(8, 0) * 20.0), "the longer resistance of the two wins (60 s, level III at quality 8)");
        check(merged.stream().anyMatch(sp -> sp.id().equals("minecraft:slowness") && sp.amplifier() == 0), "the stoic Slowness stays level I even at quality 8");

        // ... also when the EARLIER gene is the stronger one: glow coat (Glowing 60 s, ears-coat-legs-build-temperament-fertility order) + starborn (Glowing 30 s)
        List<Juice.Spec> bothGlow = Juice.effects(g("00-bb-00-00-00-44"), Stats.ZERO, 0);
        check(bothGlow.stream().filter(sp -> sp.id().equals("minecraft:glowing") && sp.target() == Juice.Target.SELF).count() == 1
                && bothGlow.stream().filter(sp -> sp.id().equals("minecraft:glowing")).findFirst().orElseThrow().ticks() == 1200,
                "glow coat + starborn: ONE Glowing, the longer 60 s (1200 ticks) wins although it comes from the earlier gene");

        // ... and when the LATER gene is the stronger one: snow coat (Absorption 60 s, no stat) + woolly build (Absorption 60 s, +50% from 25 Vitality points)
        List<Juice.Spec> bothAbsorb = Juice.effects(g("00-44-00-44-00-00"), Stats.of(25, 0, 0, 0, 0), 0);
        check(bothAbsorb.stream().filter(sp -> sp.id().equals("minecraft:absorption")).count() == 1
                && bothAbsorb.stream().filter(sp -> sp.id().equals("minecraft:absorption")).findFirst().orElseThrow().ticks() == 1800,
                "snow coat + woolly build: ONE Absorption, the longer 90 s (1800 ticks) wins although it comes from the later gene");

        // hearing: glowing on the mobs around, radius 8 x the ear scale
        Genome fennec = g("55-00-00-00-00-00");
        check(Juice.effects(fennec, Stats.ZERO, 0).stream().anyMatch(sp -> sp.target() == Juice.Target.NEARBY), "fennec ears glow the mobs around");
        near(Juice.areaRadius(fennec), 8.0 * 1.55, 1e-5, "the reach is 8 blocks x the ear scale (fennec 12.4)");
        check(Juice.effects(fennec, Stats.ZERO, 0).get(0).describe().endsWith("(on mobs nearby)"), "describe marks the area effects");

        // recessive: a glow carrier (moss shows) has the moss effects only, a glow pair sees in the dark and glows
        List<Juice.Spec> carrierJuice = Juice.effects(g("00-b6-00-00-00-00"), Stats.ZERO, 0);
        check(carrierJuice.stream().anyMatch(sp -> sp.id().equals("minecraft:dolphins_grace")) && carrierJuice.stream().noneMatch(sp -> sp.id().equals("minecraft:night_vision")),
                "a glow carrier shows moss: Dolphin's Grace, no Night Vision");
        List<Juice.Spec> glowJuice = Juice.effects(g("00-bb-00-00-00-00"), Stats.ZERO, 0);
        check(glowJuice.stream().anyMatch(sp -> sp.id().equals("minecraft:night_vision")) && glowJuice.stream().anyMatch(sp -> sp.id().equals("minecraft:glowing") && sp.target() == Juice.Target.SELF),
                "a glow pair: Night Vision and Glowing on yourself");

        // every temperament has an effect, so no pup makes an empty juice
        for (Allele t : Gene.TEMPERAMENT.alleles()) {
            check(Juice.DEFS.containsKey(Juice.key(Gene.TEMPERAMENT, t)), t.name() + ": every temperament has a juice effect");
        }

        // --- Infusion: text, parse, roll
        Infusion inf = new Infusion(g("00-00-00-11-00-00"), Stats.of(3, -1, 0, 2, 12), 4.5);
        check(inf.toString().equals("00-00-00-11-00-00-00/3,-1,0,2,12|4.5"), "text form: " + inf);
        check(Infusion.parse(inf.toString()).orElseThrow().equals(inf), "round trip");
        check(Infusion.parse("00-00-00-11-00-00").orElseThrow().equals(new Infusion(g("00-00-00-11-00-00"), Stats.ZERO, 0)), "stats and quality are optional");
        check(Infusion.parse("00-00-00-11-00-00|2.5").orElseThrow().quality() == 2.5, "quality without stats");
        for (String junk : new String[] {"", "x", "00-00-00-11-00-00/a,b|1", "00-00-00-11-00-00|abc", "00-00-00-11-00-00|1|2", "00-00-00-11-00-00|NaN", "00-00-00-11-00-00|Infinity", "zz-00-00-00-00-00"}) {
            check(Infusion.parse(junk).isEmpty(), "junk '" + junk + "' is rejected");
        }
        check(Infusion.parse(null).isEmpty(), "null is rejected");
        check(Infusion.parse("00-00-00-11-00-00|-4").orElseThrow().quality() == 0.0, "a negative quality becomes 0");
        check(new Infusion(plain, Stats.ZERO, Double.NaN).quality() == 0.0, "NaN quality becomes 0");
        check(Infusion.parse(Double.toString(1.5) + "").isEmpty(), "a bare number is not an infusion");
        java.util.Locale old = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("es-PE"));
            check(Infusion.parse(inf.toString()).orElseThrow().equals(inf) && inf.toString().contains("4.5"), "text form survives a comma-decimal locale");
        } finally {
            java.util.Locale.setDefault(old);
        }
        List<Juice.Spec> specs = inf.specs();
        check(specs.get(0).id().equals("minecraft:saturation") && specs.get(0).amplifier() == 0 && specs.get(0).ticks() == 1, "an infused bottle starts with a small Saturation");
        check(specs.size() == 1 + Juice.effects(inf.genome(), inf.stats(), inf.quality()).size(), "then the gene effects");
        check(Juice.plainSaturation().amplifier() == 1 && Juice.plainSaturation().id().equals("minecraft:saturation"), "plain juice: Saturation II only");
        check(inf.summary().get(inf.summary().size() - 1).equals("Made from a seed of quality 4.50"), "the summary ends with the quality");
        check(new Infusion(g("22-22-22-22-22-22"), Stats.ZERO, 0).summary().size() >= 3, "the all-recessive pup lists its effects and the quality");
        check(Juice.describe(plain, Stats.ZERO, 0).equals("Regeneration 0:20") && Juice.describe(g("22-22-22-22-22-22"), Stats.ZERO, 0).contains("Speed"),
                "describe lists effects, joined by commas");

        // roll: the seed's quality, the drift of ONE pup, Mendel
        Genome stocky = g("00-00-00-11-00-00");
        Seed s4 = new Seed(stocky, Stats.ZERO, stocky, Stats.ZERO, 4.0);
        int nonMutated = 0;
        for (int i = 0; i < 2000; i++) {
            Infusion r = Infusion.roll(s4, rng);
            check(r.quality() == 4.0, "an infusion keeps the seed's quality (no Fortune)");
            if (r.genome().equals(stocky)) {
                nonMutated++;
                check(r.stats().get(Stat.VITALITY) == 3 && r.stats().get(Stat.SWIFTNESS) == 0, "quality 4 drift: Vitality +3, Swiftness 0, got " + r.stats());
            }
        }
        check(nonMutated > 1800, "most rolls are unmutated copies of the pure parents, got " + nonMutated);
        Genome carrier = g("02-00-00-00-00-00");            // standard ears carrying short
        Seed carriers = new Seed(carrier, Stats.ZERO, carrier, Stats.ZERO, 0.0);
        int shortEars = 0;
        int n = 40000;
        for (int i = 0; i < n; i++) {
            Infusion r = Infusion.roll(carriers, rng);
            if (r.genome().expressed(Gene.EARS).name().equals("short")) shortEars++;
        }
        near(shortEars / (double) n, 0.25, 0.02, "two carriers: 1 in 4 liquid pups show short ears");
        // deterministic for a given random source
        check(Infusion.roll(s4, new Random(7)).equals(Infusion.roll(s4, new Random(7))), "same dice, same liquid pup");
        // it never changes the seed
        Seed copy = Seed.parse(s4.toString()).orElseThrow();
        Infusion.roll(copy, rng);
        check(copy.equals(s4), "rolling does not touch the seed");
        // a better seed makes a longer, stronger juice of the same genes
        Genome swift = g("00-00-44-00-00-00");              // fleet legs (speed)
        double lastTicks = -1;
        for (GuavaTier t : GuavaTier.values()) {
            Juice.Spec sp = Juice.effects(swift, Stats.ZERO, t.quality()).stream().filter(x -> x.id().equals("minecraft:speed")).findFirst().orElseThrow();
            check(sp.ticks() >= lastTicks, t + ": the speed juice never gets shorter up the ladder");
            lastTicks = sp.ticks();
        }
        check(Juice.effects(swift, Stats.ZERO, GuavaTier.NETHERITE.quality()).stream().filter(x -> x.id().equals("minecraft:speed")).findFirst().orElseThrow().amplifier() == 1,
                "a netherite pair makes Speed II");
        check(Juice.effects(swift, Stats.ZERO, GuavaTier.DRAGON_EGG.quality()).stream().filter(x -> x.id().equals("minecraft:speed")).findFirst().orElseThrow().amplifier() == 3,
                "the dragon egg tier hits the Speed IV cap");
    }

    // ---------------------------------------------------------------------------------------------
    static void guavaMemory() {
        System.out.println("guava memory of a bunnidog in love (Phase 4e)");
        GuavaMemory m = new GuavaMemory();
        check(!m.active() && m.tier() == 0 && m.ticksLeft() == 0, "a new memory is empty");
        m.tick(0);
        check(!m.active() && m.tier() == 0, "ticking an empty memory changes nothing");

        // remembers for as long as vanilla's love mode lasts
        m.remember(3);
        check(m.active() && m.tier() == 3 && m.ticksLeft() == GuavaMemory.TICKS && GuavaMemory.TICKS == 600, "fed a tier-3 guava: remembered for 600 ticks");
        for (int i = 1; i < GuavaMemory.TICKS; i++) {
            m.tick(0);
        }
        check(m.active() && m.tier() == 3 && m.ticksLeft() == 1, "599 ticks later it is still remembered, 1 tick left");
        m.tick(0);
        check(!m.active() && m.tier() == 0, "the 600th tick ends it: love has run out");

        // ends at once when the age leaves 0 (vanilla: a mating sets the age to the cooldown; a pup has a negative age)
        m.remember(5);
        m.tick(6000);
        check(!m.active() && m.tier() == 0, "an adult in its cooldown (age > 0) forgets at once");
        m.remember(5);
        m.tick(-1200);
        check(!m.active() && m.tier() == 0, "a pup (age < 0) never keeps a tier");
        m.remember(2);
        m.tick(0);
        check(m.active() && m.tier() == 2 && m.ticksLeft() == GuavaMemory.TICKS - 1, "age 0 keeps it");

        // a newer guava overwrites the memory and restarts the clock; junk tiers are harmless
        m.remember(6);
        m.remember(1);
        check(m.tier() == 1 && m.ticksLeft() == GuavaMemory.TICKS, "a newer guava overwrites the older one");
        m.remember(-3);
        check(m.active() && m.tier() == 0, "a negative tier is remembered as tier 0 (still in love)");
        m.remember(4);
        m.forget();
        check(!m.active() && m.tier() == 0 && m.ticksLeft() == 0, "forget() clears it (the mating does this)");

        // save and load: only a live memory survives
        m.restore(3, 250);
        check(m.active() && m.tier() == 3 && m.ticksLeft() == 250, "restore keeps tier and time left");
        m.restore(3, 0);
        check(!m.active() && m.tier() == 0, "a saved memory with no time left is empty");
        m.restore(3, -40);
        check(!m.active(), "a negative time is empty");
        m.restore(5, 100000);
        check(m.active() && m.tier() == 5 && m.ticksLeft() == GuavaMemory.TICKS, "a time above 600 is clamped to 600");
        m.restore(-2, 100);
        check(m.active() && m.tier() == 0 && m.ticksLeft() == 100, "a negative saved tier reads as 0");
        m.restore(3, 250);
        m.restore(7, 0);
        check(!m.active() && m.tier() == 0, "restoring an empty memory over a live one empties it");

        // the whole feeding-to-seed story with two parents
        GuavaMemory mother = new GuavaMemory(), father = new GuavaMemory();
        mother.remember(4);
        father.remember(2);
        double avgQuality = GuavaTier.average(mother.tier(), father.tier());
        check(avgQuality == 4.5, "a nether star guava (7) and a diamond guava (2) give quality 4.5");
        Genome plain = g("00-00-00-00-00-00");
        check(Seed.of(plain, Stats.ZERO, plain, Stats.ZERO, avgQuality).quality() == 4.5, "and the dropped seed has quality 4.5");
        mother.forget();
        father.forget();
        check(GuavaTier.average(mother.tier(), father.tier()) == 0.0, "after the mating both memories are empty: nothing leaks into the next love");

        // a parent whose love came from somewhere else (no guava): the tier of that parent counts as plain
        GuavaMemory fed = new GuavaMemory(), external = new GuavaMemory();
        fed.remember(6);
        check(GuavaTier.average(fed.tier(), external.tier()) == 10.0, "one dragon egg guava (20) and one parent in love by other means: quality 10");

        // a tier that outlived its love must not be used: 600 ticks pass, then a later love from another source
        GuavaMemory stale = new GuavaMemory();
        stale.remember(6);
        for (int i = 0; i < GuavaMemory.TICKS; i++) {
            stale.tick(0);
        }
        check(stale.tier() == 0, "an expired tier is 0 when a later love (command, other mod) leads to a mating");
    }

    // ---------------------------------------------------------------------------------------------
    static void traits() {
        System.out.println("traits");
        Traits base = Genome.DEFAULT.traits();
        near(base.healthMultiplier(), 1.0, 1e-6, "default health");
        near(base.speedMultiplier(), 1.0, 1e-6, "default speed");
        near(base.jumpMultiplier(), 1.0, 1e-6, "default jump");
        near(base.hearingMultiplier(), 1.0, 1e-6, "default hearing");
        check(base.temperament() == Temperament.LOYAL, "default temperament is loyal");
        check(base.coatName().equals("tan"), "default coat is tan");

        Traits chonk = g("00-00-00-33-00-00").traits();          // chonker/chonker
        near(chonk.buildScale(), 1.6, 1e-6, "chonker width");
        near(chonk.healthMultiplier(), 1.9, 1e-5, "chonker health");
        Traits slim = g("00-00-00-11-00-00").traits();           // stocky/stocky
        check(slim.healthMultiplier() > 1.0f, "stocky is tougher than average");
        Traits skinny = g("00-00-00-22-00-00").traits();         // slim/slim
        check(skinny.healthMultiplier() < 1.0f && skinny.healthMultiplier() > 0.5f, "slim is frailer but sane");

        Traits springy = g("00-00-33-00-00-00").traits();
        check(springy.speedMultiplier() > 1.2f && springy.jumpMultiplier() > springy.speedMultiplier(), "springy legs jump more than they run");
        Traits stumpy = g("00-00-22-00-00-00").traits();
        check(stumpy.speedMultiplier() < 1.0f && stumpy.jumpMultiplier() < stumpy.speedMultiplier(), "short legs are slower and jump worse");

        check(g("00-00-00-00-11-00").traits().temperament() == Temperament.SKITTISH, "skittish allele");
        check(g("00-00-00-00-22-00").traits().temperament() == Temperament.FERAL, "feral allele (recessive)");
        check(g("00-00-00-00-02-00").traits().temperament() == Temperament.LOYAL, "loyal hides feral");
        check(g("00-00-00-00-13-00").traits().temperament() == Temperament.STOIC, "rare stoic is dominant");
        check(g("00-22-00-00-00-00").traits().coatName().equals("brown"), "brown coat");
        check(g("00-03-00-00-00-00").traits().coatName().equals("spotted"), "spotted coat (rare dominant)");
        near(g("33-00-00-00-00-00").traits().earScale(), 1.75, 1e-6, "huge ears");
        near(g("22-00-00-00-00-00").traits().hearingMultiplier(), 0.65, 1e-6, "short ears hear less");
    }

    // ---------------------------------------------------------------------------------------------
    static void determinism() {
        System.out.println("determinism");
        Genome a = Genome.randomFounder(new Random(5)), b = Genome.randomFounder(new Random(6));
        Random r1 = new Random(1234), r2 = new Random(1234);
        for (int i = 0; i < 500; i++) {
            check(Genome.breed(a, b, r1).equals(Genome.breed(a, b, r2)), "same seed, same offspring");
        }
        check(Genome.randomFounder(new Random(3)).equals(Genome.randomFounder(new Random(3))), "same seed, same founder");
    }

    // ---------------------------------------------------------------------------------------------
    static void itemResources() throws Exception {
        System.out.println("guava item resources (Phase 4b)");
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        String clientItem = Files.readString(assets.resolve("items/guava.json"));
        check(clientItem.contains("\"minecraft:model\"") && clientItem.contains("\"bunnidogs:item/guava\""), "client item points at bunnidogs:item/guava");
        String model = Files.readString(assets.resolve("models/item/guava.json"));
        check(model.contains("\"minecraft:item/generated\"") && model.contains("\"layer0\": \"bunnidogs:item/guava\""), "item model uses the guava texture");
        Path texture = assets.resolve("textures/item/guava.png");
        check(Files.isRegularFile(texture), "guava texture exists");
        byte[] png = Files.readAllBytes(texture);
        check(png.length > 24 && (png[0] & 0xFF) == 0x89 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G', "guava texture is a PNG");
        int width = ((png[16] & 0xFF) << 24) | ((png[17] & 0xFF) << 16) | ((png[18] & 0xFF) << 8) | (png[19] & 0xFF);
        int height = ((png[20] & 0xFF) << 24) | ((png[21] & 0xFF) << 16) | ((png[22] & 0xFF) << 8) | (png[23] & 0xFF);
        check(width == 16 && height == 16, "guava texture is 16x16, got " + width + "x" + height);
        String tag = Files.readString(data.resolve("tags/item/bunnidog_food.json"));
        check(tag.contains("\"bunnidogs:guava\"") && !tag.contains("minecraft:carrot"), "bunnidog_food tag lists the guava and no carrot");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        check(lang.contains("\"item.bunnidogs.guava\""), "guava has a name");
        // the code and the resource names must agree (a rename in one place only would silently break the item)
        String items = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/registry/ModItems.java"));
        check(items.contains("registerGuava(GuavaTier.PLAIN)") && items.contains("\"bunnidog_food\""), "ModItems registers the plain guava (tier table) and the 'bunnidog_food' tag");
        String dog = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/entity/Bunnidog.java"));
        check(dog.contains("stack.is(ModItems.BUNNIDOG_FOOD)") && !dog.contains("Items.CARROT"), "bunnidogs eat the tag, not the carrot");
    }

    // ---------------------------------------------------------------------------------------------
    static void seedItemResources() throws Exception {
        System.out.println("seed item resources and wiring (Phase 4c)");
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path java = Path.of("src/main/java/com/bunnidogs/mod");
        String clientItem = Files.readString(assets.resolve("items/seed.json"));
        check(clientItem.contains("\"minecraft:model\"") && clientItem.contains("\"bunnidogs:item/seed\""), "client item points at bunnidogs:item/seed");
        String model = Files.readString(assets.resolve("models/item/seed.json"));
        check(model.contains("\"minecraft:item/generated\"") && model.contains("\"layer0\": \"bunnidogs:item/seed\""), "seed item model uses the seed texture");
        Path texture = assets.resolve("textures/item/seed.png");
        check(Files.isRegularFile(texture), "seed texture exists (run art/gen_seed.py)");
        byte[] png = Files.readAllBytes(texture);
        check(png.length > 24 && (png[0] & 0xFF) == 0x89 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G', "seed texture is a PNG");
        int width = ((png[16] & 0xFF) << 24) | ((png[17] & 0xFF) << 16) | ((png[18] & 0xFF) << 8) | (png[19] & 0xFF);
        int height = ((png[20] & 0xFF) << 24) | ((png[21] & 0xFF) << 16) | ((png[22] & 0xFF) << 8) | (png[23] & 0xFF);
        check(width == 16 && height == 16, "seed texture is 16x16, got " + width + "x" + height);
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.seed\""), "the seed has a name");
        String tag = Files.readString(data.resolve("tags/item/bunnidog_food.json"));
        check(!tag.contains("seed"), "bunnidogs do not eat seeds");

        // the code and the resource names must agree
        String items = Files.readString(java.resolve("registry/ModItems.java"));
        check(items.contains("registerItem(\"seed\", SeedItem::new"), "ModItems registers the item 'seed' with SeedItem");
        check(items.contains("registerComponentType(\"seed_data\""), "ModItems registers the component 'seed_data'");
        check(items.contains("persistent(Codec.STRING)") && items.contains("networkSynchronized(ByteBufCodecs.STRING_UTF8)"), "the payload is a string, saved and synced");
        check(items.contains("PARENT_AND_SEARCH_TABS") && items.contains("CreativeModeTabs.INGREDIENTS"), "a sample seed is in the creative Ingredients tab");
        String mod = Files.readString(java.resolve("BunnidogsMod.java"));
        check(mod.contains("ModItems.COMPONENTS.register(modEventBus)") && mod.contains("ModItems.ITEMS.register(modEventBus)"), "the mod registers the components and the items");
        String seedItem = Files.readString(java.resolve("item/SeedItem.java"));
        check(seedItem.contains("appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,"), "SeedItem overrides the 26.2 tooltip method");
        check(seedItem.contains("stack.set(ModItems.SEED_DATA.get(), seed.toString())") && seedItem.contains("Seed.parse(text)"), "SeedItem writes and reads the canonical text");

        // breeding drops a seed and no longer makes pups by itself
        String dog = Files.readString(java.resolve("entity/Bunnidog.java"));
        check(dog.contains("Seed.of(this.genome(), this.stats(), other.genome(), other.stats(),") && dog.contains("SeedItem.create(seed)"), "a mating drops a seed made of both parents");
        check(!dog.contains("pendingLitter") && !dog.contains("Drift.inherit") && !dog.contains("Genome.breed("), "the entity no longer rolls litters itself (Seed.hatch does)");
        check(dog.contains("public static List<Bunnidog> hatch(") && dog.contains("seed.hatch(extraQuality, nearby, rng)"), "Bunnidog.hatch is the one place pups come from");
        check(dog.contains("this.resetLove();") && dog.contains("other.resetLove();") && dog.contains("Breeding.cooldownTicks(other.traits().fertility())"), "parents still leave love mode and rest by their own fertility");
        check(!dog.contains("Items.CARROT"), "still no carrot in the entity");
        String commands = Files.readString(java.resolve("command/BunnidogsCommands.java"));
        check(commands.contains("Commands.literal(\"seed\")") && commands.contains("Commands.literal(\"hatch\")") && commands.contains("Bunnidog.hatch("), "the seed and hatch debug commands exist");
        check(commands.contains("stack.shrink(1)") && commands.contains("born.isEmpty()"), "hatching uses up the seed only when pups were born");
    }

    // ---------------------------------------------------------------------------------------------
    static void seedCropResources() throws Exception {
        System.out.println("seed crop resources and wiring (Phase 4d)");
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        // blockstate: all 8 ages, each with its OWN picture (session 6: a developing embryo, one look per age)
        String state = Files.readString(assets.resolve("blockstates/seed_crop.json"));
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"age=(\\d)\"\\s*:\\s*\\{\\s*\"model\"\\s*:\\s*\"bunnidogs:block/seed_crop_stage(\\d)\"").matcher(state);
        boolean[] seen = new boolean[8];
        int found = 0;
        while (m.find()) {
            int age = Integer.parseInt(m.group(1)), stage = Integer.parseInt(m.group(2));
            check(age >= 0 && age < 8 && stage == age, "age " + age + " shows its own stage " + stage);
            if (age >= 0 && age < 8) seen[age] = true;
            found++;
        }
        check(found == 8, "the blockstate has exactly 8 age variants, got " + found);
        for (int age = 0; age < 8; age++) check(seen[age], "blockstate covers age " + age);
        java.util.List<byte[]> cropPngs = new java.util.ArrayList<>();
        int[] opaque = new int[8];
        for (int stage = 0; stage < 8; stage++) {
            String model = Files.readString(assets.resolve("models/block/seed_crop_stage" + stage + ".json"));
            check(model.contains("\"minecraft:block/cross\""), "stage " + stage + " model uses the vanilla cross parent (ONE embryo in the middle of the block)");
            check(model.contains("\"cross\": \"bunnidogs:block/seed_crop_stage" + stage + "\""), "stage " + stage + " model uses its own texture");
            check(model.contains("\"render_type\": \"cutout\""), "stage " + stage + " model is cutout (transparent background)");
            byte[] png = Files.readAllBytes(assets.resolve("textures/block/seed_crop_stage" + stage + ".png"));
            cropPngs.add(png);
            check(png.length > 33 && (png[0] & 0xFF) == 0x89 && png[1] == 'P' && png[2] == 'N' && png[3] == 'G', "stage " + stage + " texture is a PNG");
            int w = ((png[16] & 0xFF) << 24) | ((png[17] & 0xFF) << 16) | ((png[18] & 0xFF) << 8) | (png[19] & 0xFF);
            int h = ((png[20] & 0xFF) << 24) | ((png[21] & 0xFF) << 16) | ((png[22] & 0xFF) << 8) | (png[23] & 0xFF);
            check(w == 16 && h == 16, "stage " + stage + " texture is 16x16, got " + w + "x" + h);
            check(png[25] == 6, "stage " + stage + " texture has an alpha channel (colour type 6), got " + png[25]);

            // the picture itself (decoded with ImageIO): a cord that stands on the ground and runs into the creature, no gaps, no half-transparent pixels
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));
            int[] rowCount = new int[16];
            boolean halfTransparent = false;
            boolean whiteEye = false;
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int argb = img.getRGB(x, y);
                    int alpha = argb >>> 24;
                    if (alpha != 0 && alpha != 255) halfTransparent = true;
                    if (alpha == 255) {
                        rowCount[y]++;
                        opaque[stage]++;
                        if ((argb & 0xFFFFFF) == 0xFFFFFF) whiteEye = true;
                    }
                }
            }
            check(!halfTransparent, "stage " + stage + ": every pixel is fully opaque or fully clear (cutout)");
            check(rowCount[15] >= 2, "stage " + stage + ": the cord stands on the ground");
            int top = 0;
            while (top < 16 && rowCount[top] == 0) top++;
            boolean connected = top < 16;
            for (int y = top; y < 16 && connected; y++) if (rowCount[y] == 0) connected = false;
            check(connected, "stage " + stage + ": cord and creature are ONE connected stem from row " + top + " to the ground (no capsule, no gap)");
            check(whiteEye == (stage == 7), "stage " + stage + ": only the ripe embryo has its eye open (white pixel)" );
        }
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                check(!java.util.Arrays.equals(cropPngs.get(i), cropPngs.get(j)), "crop pictures of ages " + i + " and " + j + " differ");
            }
            if (i > 0) check(opaque[i] >= opaque[i - 1], "age " + i + " is at least as big as age " + (i - 1) + " (a fetus developing): " + opaque[i] + " vs " + opaque[i - 1]);
        }
        check(opaque[7] > 2 * opaque[0], "the ripe embryo is much bigger than the bead at age 0");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"block.bunnidogs.seed_crop\""), "the crop has a name");

        // code <-> resources <-> each other
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("registerBlock(\"seed_crop\", SeedCropBlock::new") && blocks.contains("BLOCK_ENTITIES.register(\"seed_crop\""), "block and block entity are both 'seed_crop'");
        check(blocks.contains("noLootTable()") && blocks.contains("randomTicks()") && blocks.contains("noCollision()"), "the crop has no loot table, ticks randomly and has no collision");
        check(blocks.contains("new BlockEntityType<>(SeedCropBlockEntity::new, SEED_CROP.get())"), "the block entity type belongs to the crop block");
        String mod = Files.readString(src.resolve("BunnidogsMod.java"));
        int iBlocks = mod.indexOf("ModBlocks.BLOCKS.register(modEventBus)"), iBes = mod.indexOf("ModBlocks.BLOCK_ENTITIES.register(modEventBus)");
        check(iBlocks >= 0 && iBes > iBlocks, "blocks are registered before their block entity type");
        String crop = Files.readString(src.resolve("block/SeedCropBlock.java"));
        check(crop.contains("MAX_AGE = 7") && crop.contains("BlockStateProperties.AGE_7") && crop.contains("implements EntityBlock, BonemealableBlock"), "8 ages, a block entity, bone meal");
        check(crop.contains("import net.minecraft.world.level.block.BonemealableBlock;") && crop.contains("import net.minecraft.world.level.Level;"), "imports for bone meal");
        check(crop.contains("public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state)") && crop.contains("return !isRipe(state);"), "bone meal works until the crop is ripe");
        check(crop.contains("public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state)"), "bone meal always succeeds");
        check(crop.contains("public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state)")
                && crop.contains("Math.min(MAX_AGE, state.getValue(AGE) + boneMealAges(random))") && crop.contains("level.setBlock(pos, state.setValue(AGE, age), 2)"),
                "bone meal grows the crop by a few ages, never past ripe, and keeps the block entity (state change only)");
        check(crop.contains("BONEMEAL_MIN_AGES = 2") && crop.contains("BONEMEAL_MAX_AGES = 5") && crop.contains("BONEMEAL_MIN_AGES + random.nextInt(BONEMEAL_MAX_AGES - BONEMEAL_MIN_AGES + 1)"), "one bone meal is 2 to 5 ages");
        check(crop.contains("soil.is(Blocks.FARMLAND) || soil.is(BlockTags.DIRT)"), "the crop survives on farmland and dirt-like blocks");
        check(crop.contains("crop.seed().ifPresent(seed -> dropSeed(level, pos, seed))"), "a crop that loses its soil gives the seed back");
        String be = Files.readString(src.resolve("block/SeedCropBlockEntity.java"));
        check(be.contains("output.putString(TAG_SEED, seedText)") && be.contains("input.getStringOr(TAG_SEED, \"\")") && be.contains("\"Seed\""), "the block entity saves and loads its seed text");
        String harvest = Files.readString(src.resolve("block/SeedCropHarvest.java"));
        check(harvest.contains("import net.neoforged.neoforge.event.level.block.BreakBlockEvent;") && !harvest.contains("BlockEvent.BreakEvent"), "uses BreakBlockEvent (BlockEvent.BreakEvent no longer exists in 26.x)");
        check(harvest.contains("event.getLevel() instanceof ServerLevel"), "harvest acts on the server only (the event also fires on the client)");
        check(harvest.contains("SeedCropBlock.isRipe(state)") && harvest.contains("SeedCropBlock.dropSeed(level, pos, seed)"), "an unripe crop gives the seed back");
        check(harvest.contains("Seed.fortuneBonus(fortune)") && harvest.contains("Bunnidog.hatch(level, at, seed,"), "a ripe crop hatches the litter with the Fortune bonus");
        check(harvest.contains("event.setCanceled(true)") && harvest.contains("event.setNotifyClient(true)"), "a crowded harvest is cancelled and the client told to undo it");
        check(harvest.contains("@EventBusSubscriber(modid = BunnidogsMod.MODID)") && harvest.contains("@SubscribeEvent"), "the handler is registered like the commands");
        String seedItem = Files.readString(src.resolve("item/SeedItem.java"));
        check(seedItem.contains("public InteractionResult useOn(UseOnContext context)") && seedItem.contains("Blocks.FARMLAND") && seedItem.contains("Direction.UP"), "the seed plants on top of farmland only");
        check(seedItem.contains("crop.setSeed(seed.get())") && seedItem.contains("shrink(1)"), "planting copies the payload into the crop and uses up one seed");
        check(!Files.readString(src.resolve("registry/ModItems.java")).contains("SEED_CROP"), "the crop has no item form");
    }


    // ---------------------------------------------------------------------------------------------
    static String compact(String json) {
        return json.replaceAll("\\s+", "");
    }

    static int[] pngSize(byte[] png) {
        if (png.length < 24 || (png[0] & 0xFF) != 0x89 || png[1] != 'P' || png[2] != 'N' || png[3] != 'G') {
            return new int[] {-1, -1};
        }
        int width = ((png[16] & 0xFF) << 24) | ((png[17] & 0xFF) << 16) | ((png[18] & 0xFF) << 8) | (png[19] & 0xFF);
        int height = ((png[20] & 0xFF) << 24) | ((png[21] & 0xFF) << 16) | ((png[22] & 0xFF) << 8) | (png[23] & 0xFF);
        return new int[] {width, height};
    }

    static void guavaTierResources() throws Exception {
        System.out.println("guava tier resources, recipes and wiring (Phase 4e)");
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        String tag = Files.readString(data.resolve("tags/item/bunnidog_food.json"));
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        String art = Files.readString(Path.of("art/gen_guava.py"));
        java.util.List<byte[]> textures = new java.util.ArrayList<>();
        int lastArtPos = -1;

        for (GuavaTier tier : GuavaTier.values()) {
            String id = tier.id();
            String what = id + ": ";
            String clientItem = compact(Files.readString(assets.resolve("items/" + id + ".json")));
            check(clientItem.contains("\"type\":\"minecraft:model\"") && clientItem.contains("\"model\":\"bunnidogs:item/" + id + "\""), what + "client item points at its model");
            String model = compact(Files.readString(assets.resolve("models/item/" + id + ".json")));
            check(model.contains("\"parent\":\"minecraft:item/generated\"") && model.contains("\"layer0\":\"bunnidogs:item/" + id + "\""), what + "item model uses its own texture");
            Path texture = assets.resolve("textures/item/" + id + ".png");
            check(Files.isRegularFile(texture), what + "texture exists (run art/gen_guava.py)");
            byte[] png = Files.readAllBytes(texture);
            int[] size = pngSize(png);
            check(size[0] == 16 && size[1] == 16, what + "texture is a 16x16 PNG, got " + size[0] + "x" + size[1]);
            textures.add(png);
            check(java.util.regex.Pattern.compile("\"item\\.bunnidogs\\." + id + "\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(tier.displayName()) + "\"").matcher(lang).find(),
                    what + "lang name is '" + tier.displayName() + "'");
            check(tag.contains("\"bunnidogs:" + id + "\""), what + "is food for bunnidogs (in the tag)");
            check(items.contains("registerGuava(GuavaTier." + tier.name() + ")"), what + "ModItems registers it from the tier table");
            int at = art.indexOf("\"" + id + "\": dict(");
            check(at > lastArtPos, what + "has a palette in art/gen_guava.py, in ladder order");
            lastArtPos = at;
        }
        for (int i = 0; i < textures.size(); i++) {
            for (int j = i + 1; j < textures.size(); j++) {
                check(!java.util.Arrays.equals(textures.get(i), textures.get(j)),
                        "textures of " + GuavaTier.values()[i].id() + " and " + GuavaTier.values()[j].id() + " differ");
            }
        }
        int tagged = tag.split("\"bunnidogs:", -1).length - 1;
        check(tagged == GuavaTier.values().length && !tag.contains("minecraft:") && !tag.contains("bunnidogs:seed"), "the food tag lists exactly the eight guavas");

        // --- recipes: golden-apple style (8 around) for gold, diamond, netherite and beacon, 4 nether stars in a plus, and one dragon egg shapeless
        Path recipes = data.resolve("recipe");
        check(!Files.exists(recipes.resolve("guava.json")), "the plain guava has no recipe (it comes from the world, step 4g)");
        for (GuavaTier tier : GuavaTier.values()) {
            if (tier == GuavaTier.PLAIN) {
                continue;
            }
            String id = tier.id();
            String what = "recipe " + id + ": ";
            Path file = recipes.resolve(id + ".json");
            check(Files.isRegularFile(file), what + "exists");
            if (!Files.isRegularFile(file)) {
                continue;
            }
            String r = compact(Files.readString(file));
            String previous = "bunnidogs:" + tier.previous().id();
            check(r.contains("\"result\":{\"id\":\"bunnidogs:" + id + "\"}"), what + "makes one " + id);
            String material = switch (tier) {
                case GOLDEN -> "minecraft:gold_ingot";
                case DIAMOND -> "minecraft:diamond";
                case NETHERITE -> "minecraft:netherite_ingot";
                case NETHER_STAR -> "minecraft:nether_star";
                case BEACON -> "minecraft:beacon";
                case DRAGON_EGG -> "minecraft:dragon_egg";
                case COMMAND_BLOCK -> "minecraft:command_block";
                default -> "?";
            };
            // gold, diamond, netherite and (session 6) beacon: 8 around the previous tier; nether star: 4 in a plus (session 6);
            // dragon egg and command block (session 7): shapeless, the previous tier and the one egg / command block
            // (the plus has spaces in it, which compact() would strip: its pattern is matched on the raw text)
            String pattern = switch (tier) {
                case NETHER_STAR -> "\" # \",\"#X#\",\" # \"";
                case DRAGON_EGG, COMMAND_BLOCK -> "";
                default -> "\"pattern\":[\"###\",\"#X#\",\"###\"]";
            };
            String rawRecipe = Files.readString(file);
            boolean patternOk = tier == GuavaTier.NETHER_STAR
                    ? java.util.regex.Pattern.compile("\"pattern\"\\s*:\\s*\\[\\s*\" # \"\\s*,\\s*\"#X#\"\\s*,\\s*\" # \"\\s*\\]").matcher(rawRecipe).find()
                    : r.contains(pattern);
            if (tier != GuavaTier.DRAGON_EGG && tier != GuavaTier.COMMAND_BLOCK) {
                check(r.contains("\"type\":\"minecraft:crafting_shaped\""), what + "shaped");
                check(patternOk, what + (tier == GuavaTier.NETHER_STAR ? "4 nether stars in a plus around the guava" : "8 around, 1 in the middle"));
                check(r.contains("\"key\":{\"#\":\"" + material + "\",\"X\":\"" + previous + "\"}"), what + material + " around the " + previous);
                int slots = 0;
                String[] afterPattern = rawRecipe.split("\"pattern\"");
                check(afterPattern.length == 2, what + "has a pattern");
                if (afterPattern.length == 2) {
                    for (String row : afterPattern[1].split("\\]")[0].split(",")) {
                        for (char c : row.toCharArray()) if (c == '#') slots++;
                    }
                }
                check(slots == (tier == GuavaTier.NETHER_STAR ? 4 : 8), what + "uses " + (tier == GuavaTier.NETHER_STAR ? 4 : 8) + " of " + material + ", got " + slots);
            } else {
                check(r.contains("\"type\":\"minecraft:crafting_shapeless\""), what + "shapeless");
                check(r.contains("\"ingredients\":[\"" + previous + "\",\"" + material + "\"]"), what + previous + " + one " + material);
            }
            // a recipe never mentions another guava than the previous tier
            int guavaMentions = r.split("bunnidogs:", -1).length - 1;
            check(guavaMentions == 2, what + "mentions only its result and the previous tier, got " + guavaMentions + " mentions");
        }
        int recipeFiles;
        try (var stream = Files.list(recipes)) {
            recipeFiles = (int) stream.filter(f -> f.getFileName().toString().endsWith("_guava.json")).count();     // guava_juice.json is Phase 4f's
        }
        check(recipeFiles == GuavaTier.values().length - 1, "one recipe per tier above the plain guava and nothing else named *_guava, got " + recipeFiles);

        // --- the code: items, tooltip, creative tab
        String guavaItem = Files.readString(src.resolve("item/GuavaItem.java"));
        check(guavaItem.contains("stack.getItem() instanceof GuavaItem guava ? guava.tier().level() : 0"), "a stack's tier is its guava's LEVEL (the quality is looked up at the mating), 0 for any other food");
        check(guavaItem.contains("\"Seed quality \" + tier.qualityText()"), "the tooltip prints the quality from the table");
        check(items.contains("switch (tier.level())"), "the rarity follows the ladder level, not the quality");
        check(!guavaItem.contains("tier().quality() : 0"), "no stack is read as a quality any more");
        check(guavaItem.contains("appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,"), "GuavaItem overrides the 26.2 tooltip method");
        check(items.contains("public static final List<DeferredItem<GuavaItem>> GUAVAS = List.of("), "ModItems lists the guavas");
        int listAt = items.indexOf("GUAVAS = List.of(");
        int pos = listAt;
        for (GuavaTier tier : GuavaTier.values()) {
            String field = tier == GuavaTier.PLAIN ? "GUAVA" : tier.name() + "_GUAVA";
            check(items.contains("DeferredItem<GuavaItem> " + field + " = registerGuava(GuavaTier." + tier.name() + ");"), "field " + field + " for " + tier.name());
            int next = items.indexOf(field, pos + (pos == listAt ? 0 : 1));
            check(next >= pos, "GUAVAS lists " + field + " in ladder order");
            pos = Math.max(pos, next);
        }
        check(items.contains("for (DeferredItem<GuavaItem> guava : GUAVAS)") && items.contains("CreativeModeTabs.FOOD_AND_DRINKS"), "every guava is in the creative Food & Drinks tab");
        check(items.contains("props.food(GUAVA_FOOD).rarity(rarity)"), "every tier is food and has a rarity");

        // --- the entity: the fed tier is read before vanilla uses up the stack, remembered, averaged at the mating, forgotten after
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        check(dog.contains("public InteractionResult mobInteract(Player player, InteractionHand hand)"), "Bunnidog overrides mobInteract");
        int readTier = dog.indexOf("GuavaItem.tierOf(held)");
        int callSuper = dog.indexOf("super.mobInteract(player, hand)");
        check(readTier > 0 && callSuper > readTier, "the tier is read BEFORE super.mobInteract uses up the stack");
        check(dog.contains("readyForLove && result.consumesAction()") && dog.contains("this.guavaMemory.remember(tier)"), "the tier is remembered only when vanilla accepted the food for love");
        check(dog.contains("!this.isBaby() && this.getAge() == 0") && dog.contains("!this.guavaMemory.active()"), "pups, cooldown adults and adults already in love do not overwrite the tier");
        check(dog.contains("GuavaTier.average(this.guavaMemory.tier(), other.guavaMemory.tier())"), "the seed's guava quality is the average of both parents");
        check(dog.contains("other.stats(), guavaQuality)") && !dog.contains("other.stats(), 0.0D)"), "the seed is made with that quality (no more hard-coded 0)");
        check(dog.contains("this.guavaMemory.forget();") && dog.contains("other.guavaMemory.forget();"), "both memories are cleared at the mating");
        check(dog.contains("this.guavaMemory.tick(this.getAge())"), "the memory ticks with the entity's age");
        check(dog.contains("output.putInt(TAG_LOVE_TIER, ") && dog.contains("input.getIntOr(TAG_LOVE_TIER, 0)"), "the memory is saved and loaded");
        check(dog.contains("import net.minecraft.world.InteractionHand;") && dog.contains("import net.minecraft.world.InteractionResult;"), "imports for mobInteract");
        String commands = Files.readString(src.resolve("command/BunnidogsCommands.java"));
        check(commands.contains("dog.loveTierTicks() > 0") && commands.contains("In love on a tier-%d guava") && commands.contains("GuavaTier.ofLevel(dog.loveTier()).qualityText()"), "/bunnidogs genome shows the remembered tier and its quality");
    }


    // ---------------------------------------------------------------------------------------------
    static void juiceResources() throws Exception {
        System.out.println("juice resources and wiring (Phase 4f)");
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        String juiceItem = Files.readString(src.resolve("item/JuiceItem.java"));
        String seedItem = Files.readString(src.resolve("item/SeedItem.java"));

        java.util.List<byte[]> textures = new java.util.ArrayList<>();
        for (String id : new String[] {"guava_juice", "infused_guava_juice"}) {
            String what = id + ": ";
            String clientItem = compact(Files.readString(assets.resolve("items/" + id + ".json")));
            check(clientItem.contains("\"type\":\"minecraft:model\"") && clientItem.contains("\"model\":\"bunnidogs:item/" + id + "\""), what + "client item points at its model");
            String model = compact(Files.readString(assets.resolve("models/item/" + id + ".json")));
            check(model.contains("\"parent\":\"minecraft:item/generated\"") && model.contains("\"layer0\":\"bunnidogs:item/" + id + "\""), what + "item model uses its own texture");
            byte[] png = Files.readAllBytes(assets.resolve("textures/item/" + id + ".png"));
            textures.add(png);
            int[] size = pngSize(png);
            check(size[0] == 16 && size[1] == 16, what + "texture is a 16x16 PNG, got " + size[0] + "x" + size[1]);
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));
            int opaque = 0;
            boolean half = false;
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                int a = img.getRGB(x, y) >>> 24;
                if (a == 255) opaque++;
                if (a != 0 && a != 255) half = true;
            }
            check(opaque > 60 && !half, what + "a bottle of real pixels (" + opaque + "), no half-transparent ones");
            check(items.contains("\"" + id + "\""), what + "registered in ModItems");
            check(lang.contains("\"item.bunnidogs." + id + "\""), what + "has a name");
        }
        check(!java.util.Arrays.equals(textures.get(0), textures.get(1)), "the two bottles look different");
        check(lang.contains("\"item.bunnidogs.guava_juice\": \"Guava Juice\"") && lang.contains("\"item.bunnidogs.infused_guava_juice\": \"Infused Guava Juice\""), "the names");

        // the recipe: a plain guava and a glass bottle, nothing else; the infused juice has NO recipe (it is made by infusing)
        String recipe = compact(Files.readString(data.resolve("recipe/guava_juice.json")));
        check(recipe.contains("\"type\":\"minecraft:crafting_shapeless\"") && recipe.contains("\"ingredients\":[\"bunnidogs:guava\",\"minecraft:glass_bottle\"]")
                && recipe.contains("\"result\":{\"id\":\"bunnidogs:guava_juice\"}"), "guava + glass bottle -> guava juice (the plain guava only, no higher tier is wasted)");
        check(!Files.exists(data.resolve("recipe/infused_guava_juice.json")), "the infused juice has no recipe");
        // Each feature checks its recipes directly; unrelated additions must not
        // require changing a global recipe-file count in the guava juice check.
        check(!Files.readString(data.resolve("tags/item/bunnidog_food.json")).contains("juice"), "juice is not bunnidog food + 3 guava wood recipes (W1: planks, wood, stripped wood) + 3 guava wood part recipes (W2: stairs, slab, fence)");

        // ModItems
        check(items.contains("COMPONENTS.registerComponentType(\"infusion_data\"") && items.contains("INFUSION_DATA"), "the infusion payload is a string data component");
        check(items.contains("ITEMS.registerItem(\"guava_juice\", JuiceItem::new, props -> props.stacksTo(16))"), "plain juice: a JuiceItem, stacks to 16");
        check(items.contains("ITEMS.registerItem(\"infused_guava_juice\", JuiceItem::new,") && items.contains("props.stacksTo(1).rarity(Rarity.UNCOMMON)"), "infused juice: a JuiceItem, one per stack");
        check(items.contains("event.accept(GUAVA_JUICE.get())") && !items.contains("event.accept(INFUSED_JUICE"), "the plain juice is in the creative tab, the infused one only comes from infusing");

        // JuiceItem: the vanilla drinking flow with our own finish
        check(juiceItem.contains("public int getUseDuration(ItemStack stack, LivingEntity entity)") && juiceItem.contains("return ItemUseAnimation.DRINK;"), "a drinking animation");
        check(juiceItem.contains("player.startUsingItem(hand);") && juiceItem.contains("return InteractionResult.CONSUME;"), "using it starts the drink");
        check(juiceItem.contains("public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living)"), "finishUsingItem is overridden");
        int serverOnly = juiceItem.indexOf("if (!level.isClientSide()) {");
        int applyCall = juiceItem.indexOf("apply(level, living, spec, radius);");
        check(serverOnly > 0 && applyCall > serverOnly, "effects are applied on the server only");
        check(juiceItem.contains("level.registryAccess().lookupOrThrow(Registries.MOB_EFFECT)") && juiceItem.contains("ResourceKey.create(Registries.MOB_EFFECT, Identifier.parse(spec.id()))")
                && juiceItem.contains("catch (RuntimeException e)") && juiceItem.contains("return;"), "an effect is found by registry id; an unknown id is skipped, not a crash");
        check(juiceItem.contains("new MobEffectInstance(effect, spec.ticks(), spec.amplifier())"), "the effect instance takes the spec's duration and level");
        check(juiceItem.contains("if (other != drinker)") && juiceItem.contains("inflate(radius)"), "area effects reach the others around, not the drinker");
        check(juiceItem.contains("player.hasInfiniteMaterials()") && juiceItem.contains("new ItemStack(Items.GLASS_BOTTLE)") && juiceItem.contains("stack.shrink(1)"), "survival drinking uses the bottle up and hands back a glass bottle");
        check(juiceItem.contains("stack.is(ModItems.INFUSED_JUICE.get())") && juiceItem.contains("stack.set(ModItems.INFUSION_DATA.get(), infusion.toString())"), "payload read and written through the component");

        // SeedItem.use = the infusion
        check(seedItem.contains("public InteractionResult use(Level level, Player player, InteractionHand hand)"), "the seed overrides use()");
        check(seedItem.contains("hand != InteractionHand.MAIN_HAND") && seedItem.contains("player.getItemInHand(InteractionHand.OFF_HAND)") && seedItem.contains("juiceStack.is(ModItems.GUAVA_JUICE.get())"),
                "seed in the main hand, PLAIN juice in the off hand");
        check(seedItem.contains("Infusion.roll(seed.get(), new Random(level.getRandom().nextLong()))"), "one phantom pup is rolled from the seed");
        int roll = seedItem.indexOf("Infusion.roll(");
        int serverCheck = seedItem.lastIndexOf("if (!level.isClientSide())", roll);
        check(serverCheck > 0 && roll > serverCheck, "rolled on the server only");
        check(seedItem.contains("seedStack.shrink(1);") && seedItem.contains("juiceStack.shrink(1);") && seedItem.contains("if (player.hasInfiniteMaterials()) {\n                giveOrDrop(player, bottle);"), "one seed and one juice are used up (not in creative)");
        check(seedItem.contains("player.getInventory().add(stack)") && seedItem.contains("player.drop(stack, false)") && seedItem.contains("giveOrDrop(player, bottle)"), "the bottle goes to the inventory or the floor");
        check(seedItem.contains("seedStack.getCount() <= 1") && seedItem.contains("player.setItemInHand(hand, bottle)")
                && seedItem.indexOf("seedStack.getCount() <= 1") < seedItem.indexOf("seedStack.shrink(1)"), "a last seed is replaced by the bottle in the hand (session 12 fix)");
        check(seedItem.indexOf("public InteractionResult useOn(") < seedItem.indexOf("public InteractionResult use(Level"), "planting (useOn) stays and infusing is the separate use()");
        String commands = Files.readString(src.resolve("command/BunnidogsCommands.java"));
        check(commands.contains("Juice.describe(genome, dog.stats(), 0.0D)"), "/bunnidogs genome shows the juice of a genome");
    }


    // ---------------------------------------------------------------------------------------------
    static void layouts() {
        System.out.println("guava tree and warren layouts (Phase 4g)");
        Random rng = new Random(4949);
        java.util.Set<Integer> logHeights = new HashSet<>();
        java.util.Set<Integer> fruitCounts = new HashSet<>();
        int minLeaves = 999, maxLeaves = 0, maxCells = 0;
        for (int n = 0; n < 300; n++) {
            List<TreeShape.Cell> tree = TreeShape.generate(rng);
            java.util.Map<String, TreeShape.Cell> at = new java.util.HashMap<>();
            int logs = 0, leaves = 0, fruits = 0;
            for (TreeShape.Cell c : tree) {
                check(at.put(c.dx() + "," + c.dy() + "," + c.dz(), c) == null, "no two cells of a tree share a position");
                check(Math.abs(c.dx()) <= TreeShape.RADIUS && Math.abs(c.dz()) <= TreeShape.RADIUS && c.dy() >= 0, "a tree stays within 2 blocks to the side and above its base");
                switch (c.kind()) {
                    case LOG -> logs++;
                    case LEAVES -> leaves++;
                    case FRUIT -> fruits++;
                }
            }
            logHeights.add(logs);
            fruitCounts.add(fruits);
            check(logs == TreeShape.MIN_LOGS || logs == TreeShape.MIN_LOGS + 1, "the trunk has 3 or 4 logs, got " + logs);
            for (int dy = 0; dy < logs; dy++) {
                TreeShape.Cell c = at.get("0," + dy + ",0");
                check(c != null && c.kind() == TreeShape.Kind.LOG, "the trunk is one unbroken column from the base (dy " + dy + ")");
            }
            for (TreeShape.Cell c : tree) {
                if (c.kind() == TreeShape.Kind.LOG) check(c.dx() == 0 && c.dz() == 0, "logs are only in the trunk column");
            }
            check(at.get("0," + logs + ",0") != null && at.get("0," + logs + ",0").kind() == TreeShape.Kind.LEAVES, "the top of the trunk has a leaf on it");
            check(at.get("0," + (logs + 1) + ",0") != null, "and a leaf above that");
            int maxDy = 0;
            for (TreeShape.Cell c : tree) maxDy = Math.max(maxDy, c.dy());
            check(maxDy == logs + 1, "the tree is trunk + 2 blocks high, got " + maxDy);
            check(fruits >= 1 && fruits <= TreeShape.MAX_FRUITS, "1 to 3 fruits, got " + fruits);
            // every leaf is connected to the trunk through leaves (6-neighbourhood), so nothing floats and nothing decays apart
            java.util.Set<String> seen = new HashSet<>();
            java.util.ArrayDeque<TreeShape.Cell> queue = new java.util.ArrayDeque<>();
            for (TreeShape.Cell c : tree) if (c.kind() == TreeShape.Kind.LOG) { queue.add(c); seen.add(c.dx() + "," + c.dy() + "," + c.dz()); }
            int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            while (!queue.isEmpty()) {
                TreeShape.Cell c = queue.poll();
                for (int[] d : dirs) {
                    String k = (c.dx() + d[0]) + "," + (c.dy() + d[1]) + "," + (c.dz() + d[2]);
                    TreeShape.Cell o = at.get(k);
                    if (o != null && o.kind() == TreeShape.Kind.LEAVES && seen.add(k)) queue.add(o);
                }
            }
            check(seen.size() == logs + leaves, "all " + leaves + " leaves are connected to the trunk, reached " + (seen.size() - logs));
            for (TreeShape.Cell c : tree) {
                if (c.kind() == TreeShape.Kind.FRUIT) {
                    TreeShape.Cell above = at.get(c.dx() + "," + (c.dy() + 1) + "," + c.dz());
                    check(above != null && above.kind() == TreeShape.Kind.LEAVES, "a fruit hangs directly under a leaf");
                    check(c.dy() == logs - 3, "fruits hang under the lowest crown layer (dy " + (logs - 3) + "), got " + c.dy());
                    check(c.dx() != 0 || c.dz() != 0, "a fruit is never in the trunk column");
                }
            }
            minLeaves = Math.min(minLeaves, leaves);
            maxLeaves = Math.max(maxLeaves, leaves);
            maxCells = Math.max(maxCells, tree.size());
        }
        check(logHeights.equals(java.util.Set.of(3, 4)), "both trunk heights occur, got " + logHeights);
        check(fruitCounts.equals(java.util.Set.of(1, 2, 3)), "1, 2 and 3 fruits all occur, got " + fruitCounts);
        check(minLeaves >= 30 && maxLeaves <= 60, "a small tree: 30 to 60 leaves, got " + minLeaves + ".." + maxLeaves);
        check(maxCells < 70, "a tree is under 70 blocks, got " + maxCells);
        check(TreeShape.generate(new Random(7)).equals(TreeShape.generate(new Random(7))), "the same dice make the same tree");
        boolean differs = false;
        for (int i = 0; i < 20; i++) if (!TreeShape.generate(new Random(i)).equals(TreeShape.generate(new Random(i + 100)))) differs = true;
        check(differs, "different dice make different trees");

        java.util.Set<Integer> burrowCounts = new HashSet<>();
        java.util.Set<Integer> dogCounts = new HashSet<>();
        for (int n = 0; n < 300; n++) {
            WarrenShape w = WarrenShape.generate(rng);
            java.util.Set<WarrenShape.Spot> blocked = new HashSet<>();
            for (TreeShape.Cell c : w.tree()) if (c.dy() <= 1) blocked.add(new WarrenShape.Spot(c.dx(), c.dz()));
            check(blocked.contains(new WarrenShape.Spot(0, 0)), "the trunk column is blocked");
            check(w.patch().size() == 21 && new HashSet<>(w.patch()).size() == 21, "the patch is 21 distinct columns");
            for (WarrenShape.Spot s : w.patch()) check(s.dx() * s.dx() + s.dz() * s.dz() <= WarrenShape.PATCH_DISTANCE_SQUARED, "patch columns are within radius 2.2");
            int b = w.burrows().size(), d = w.dogs().size();
            burrowCounts.add(b);
            dogCounts.add(d);
            check(b >= WarrenShape.MIN_BURROWS && b <= WarrenShape.MAX_BURROWS, "3 or 4 burrows, got " + b);
            check(d >= WarrenShape.MIN_DOGS && d <= WarrenShape.MAX_DOGS, "2 to 4 bunnidogs, got " + d);
            check(new HashSet<>(w.burrows()).size() == b && new HashSet<>(w.dogs()).size() == d, "burrows are distinct, dogs are distinct");
            for (WarrenShape.Spot s : w.burrows()) {
                int d2 = s.dx() * s.dx() + s.dz() * s.dz();
                check(d2 >= WarrenShape.BURROW_MIN_SQUARED && d2 <= WarrenShape.BURROW_MAX_SQUARED, "a burrow is 2 to 3 blocks from the trunk");
                check(!blocked.contains(s), "a burrow is never under the trunk or a low fruit");
                for (WarrenShape.Spot o : w.burrows()) {
                    if (o != s) check(Math.max(Math.abs(s.dx() - o.dx()), Math.abs(s.dz() - o.dz())) >= 2, "burrows never touch each other");
                }
            }
            for (WarrenShape.Spot s : w.dogs()) {
                int d2 = s.dx() * s.dx() + s.dz() * s.dz();
                check(d2 >= 1 && d2 <= WarrenShape.DOG_MAX_SQUARED, "a bunnidog stands within 3 blocks of the trunk");
                check(!blocked.contains(s), "a bunnidog is never put under the trunk or a low-hanging fruit");
                check(!w.burrows().contains(s), "a bunnidog is never put in a burrow");
            }
        }
        check(burrowCounts.equals(java.util.Set.of(3, 4)), "3 and 4 burrows both occur, got " + burrowCounts);
        check(dogCounts.equals(java.util.Set.of(2, 3, 4)), "2, 3 and 4 bunnidogs all occur, got " + dogCounts);
        check(WarrenShape.generate(new Random(9)).equals(WarrenShape.generate(new Random(9))), "the same dice make the same warren");
    }


    // ---------------------------------------------------------------------------------------------
    static void settlements() throws Exception {
        System.out.println("settlements: development level = number of guava trees (Phase 6a)");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        Random rng = new Random(6161);
        check(Settlement.MIN_LEVEL == 1 && Settlement.MAX_LEVEL == 5 && Settlement.WEIGHTS.length == Settlement.MAX_LEVEL, "levels 1 to 5, one weight each");
        int total = 0;
        for (int w : Settlement.WEIGHTS) { check(w > 0, "every level can occur"); total += w; }
        for (int i = 1; i < Settlement.WEIGHTS.length; i++) check(Settlement.WEIGHTS[i] < Settlement.WEIGHTS[i - 1], "a higher level is rarer");
        check(total == 100, "the weights are percentages, got " + total);
        int[] seen = new int[Settlement.MAX_LEVEL + 1];
        for (int n = 0; n < 20000; n++) seen[Settlement.rollLevel(rng)]++;
        for (int lv = 1; lv <= Settlement.MAX_LEVEL; lv++) {
            near(seen[lv] / 20000.0, Settlement.WEIGHTS[lv - 1] / 100.0, 0.02, "level " + lv + " is rolled by its weight");
        }
        check(seen[0] == 0, "no level 0 from the roll");
        for (int lv = 0; lv <= 7; lv++) {
            int want = Math.max(1, Math.min(5, lv));
            for (int n = 0; n < 200; n++) {
                Settlement st = Settlement.generate(rng, lv);
                check(st.level() == want && st.clusters().size() == want, "level " + lv + " (clamped to " + want + ") = " + want + " trees, got " + st.clusters().size());
                check(st.clusters().get(0).dx() == 0 && st.clusters().get(0).dz() == 0, "the first cluster is the centre");
                int dogs = 0;
                for (Settlement.Cluster c : st.clusters()) {
                    check(c.shape().tree().stream().filter(cell -> cell.kind() == TreeShape.Kind.LOG).count() >= TreeShape.MIN_LOGS, "every cluster has its own guava tree");
                    dogs += c.shape().dogs().size();
                    // nothing of the cluster reaches past the safe extent from the settlement centre
                    int reach = Math.max(Math.abs(c.dx()), Math.abs(c.dz())) + 3;
                    check(reach <= Settlement.MAX_EXTENT, "a cluster stays within " + Settlement.MAX_EXTENT + " blocks of the centre, got " + reach);
                    for (WarrenShape.Spot s : c.shape().dogs()) check(Math.max(Math.abs(c.dx() + s.dx()), Math.abs(c.dz() + s.dz())) <= Settlement.MAX_EXTENT, "a bunnidog stays within the safe extent");
                    for (WarrenShape.Spot s : c.shape().burrows()) check(Math.max(Math.abs(c.dx() + s.dx()), Math.abs(c.dz() + s.dz())) <= Settlement.MAX_EXTENT, "a burrow stays within the safe extent");
                    for (TreeShape.Cell cell : c.shape().tree()) check(Math.max(Math.abs(c.dx() + cell.dx()), Math.abs(c.dz() + cell.dz())) <= Settlement.MAX_EXTENT, "a tree cell stays within the safe extent");
                }
                check(dogs >= want * WarrenShape.MIN_DOGS && dogs <= want * WarrenShape.MAX_DOGS, "more trees, more bunnidogs: " + dogs + " for " + want + " trees");
                for (int i = 0; i < st.clusters().size(); i++) {
                    for (int j = i + 1; j < st.clusters().size(); j++) {
                        Settlement.Cluster a = st.clusters().get(i), b = st.clusters().get(j);
                        int apart = Math.max(Math.abs(a.dx() - b.dx()), Math.abs(a.dz() - b.dz()));
                        check(apart >= Settlement.SPACING - 2 * Settlement.JITTER, "two trees stand at least " + (Settlement.SPACING - 2 * Settlement.JITTER) + " blocks apart (crowns, patches and burrows never overlap), got " + apart);
                    }
                }
            }
        }
        check(Settlement.generate(new Random(3), 4).equals(Settlement.generate(new Random(3), 4)), "the same dice make the same settlement");
        java.util.Set<String> shapes = new HashSet<>();
        for (int n = 0; n < 200; n++) { Settlement st = Settlement.generate(rng, 3); shapes.add(st.clusters().get(1).dx() + "," + st.clusters().get(1).dz()); }
        check(shapes.size() > 4, "the pattern is turned and jittered, got " + shapes.size() + " different spots for the second tree");

        // the glue, by grep: the centre decides, the others are best-effort, development = trees actually built
        String warren = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        check(warren.contains("Settlement.generate(rng) : Settlement.generate(rng, requestedLevel)"), "the level is rolled or forced by the command");
        check(warren.contains("return new Built(0, 0, 0, 0, 0);") && warren.contains("trees++;") && warren.contains("return new Built(trees, houses, dogs, paths, props);"), "the settlement returns the number of trees and houses really built");
        check(warren.indexOf("if (trees == 0 && cluster.dx() == 0 && cluster.dz() == 0) {") < warren.indexOf("continue;"), "only the centre can cancel the whole settlement");
        check(warren.contains("groundAt(level, floor.getX() + cluster.dx(), floor.getY(), floor.getZ() + cluster.dz(), surfaceOnly)"), "the other clusters look for ground at their own spot");
        String command = Files.readString(src.resolve("command/BunnidogsCommands.java"));
        check(command.contains("IntegerArgumentType.integer(Settlement.MIN_LEVEL, Settlement.MAX_LEVEL)"), "the command takes a level 1 to 5");
    }

    // ---------------------------------------------------------------------------------------------
    static void uneven() throws Exception {
        System.out.println("warrens on uneven ground (session 9)");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String warren = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        check(warren.contains("SURFACE_RISE = 6") && warren.contains("SURFACE_DROP = 8") && warren.contains("CAVE_RISE = 2") && warren.contains("CAVE_DROP = 3"), "windows: 6 up / 8 down on the surface, the old 2 / 3 in caves");
        int surfaceBranch = warren.indexOf("if (surface) {");
        int fluidRule = warren.indexOf("if (!state.getFluidState().isEmpty()) {\n                    return null;");
        int caveLoop = warren.indexOf("for (int dy = CAVE_RISE; dy >= -CAVE_DROP; dy--) {");
        check(surfaceBranch > 0 && surfaceBranch < fluidRule && fluidRule < caveLoop, "the surface column stops at a fluid (no digging into the cave under a lake) and the cave window is a separate loop");
        check(warren.contains("return isStandable(level, pos.above()) ? pos.above() : null;"), "the topmost block of the column is the ground, or the column has none");
        check(warren.contains("dogs >= WarrenShape.MIN_DOGS") && warren.contains("!warren.dogs().contains(spot)") && warren.contains("!warren.burrows().contains(spot)"), "steep ground: missing bunnidogs come from the patch, never on a burrow or a taken spot");
        check(warren.indexOf("if (top - low > HOUSE_MAX_SLOPE) {") < warren.indexOf("WarrenPalette.Role.FILL), FLAGS);")
                && warren.indexOf("BlockPos outside = base.offset(house.doorDx() * 2") < warren.indexOf("WarrenPalette.Role.FILL), FLAGS);"), "a house checks the slope and the doorway before it fills or builds anything");
        check(warren.contains("HOUSE_MAX_SLOPE = 3") && warren.contains("LOGGER.info(\"Warren built at"), "slope limit 3; every settlement is logged");
        String command = Files.readString(src.resolve("command/BunnidogsCommands.java"));
        check(command.contains("Commands.literal(\"surface\")") && command.contains("source.getLevel().getRandom(), surface, level)") && command.contains("built.dogs()"), "/bunnidogs warren [level] surface builds the surface kind and reports the bunnidogs");
    }

    // ---------------------------------------------------------------------------------------------
    static void houses() throws Exception {
        System.out.println("burrow houses in settlements (Phase 6b)");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        Random rng = new Random(6262);
        // the house itself, for all four door sides
        int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] side : sides) {
            List<HouseShape.Cell> cells = HouseShape.cells(side[0], side[1]);
            int walls = 0, roof = 0, open = 0, maxDy = 0;
            java.util.Set<String> at = new HashSet<>();
            for (HouseShape.Cell c : cells) {
                check(at.add(c.dx() + "," + c.dy() + "," + c.dz()), "no two cells of a house share a position");
                check(Math.abs(c.dx()) <= 1 && Math.abs(c.dz()) <= 1 && c.dy() >= 0, "a house is inside its 3x3 footprint and never under the ground");
                maxDy = Math.max(maxDy, c.dy());
                switch (c.kind()) {
                    case WALL -> walls++;
                    case ROOF -> roof++;
                    case OPEN -> open++;
                }
                if (c.kind() == HouseShape.Kind.WALL) check(c.dy() <= 1 && !(c.dx() == 0 && c.dz() == 0), "walls are the ring, two high");
                if (c.kind() == HouseShape.Kind.ROOF) check(c.dy() >= 2, "the roof is on top of the walls");
            }
            check(walls == 14 && roof == 10 && open == 4, "a house is 14 wall, 10 roof and 4 open blocks, got " + walls + "/" + roof + "/" + open);
            check(maxDy == HouseShape.HEIGHT - 1, "the house is " + HouseShape.HEIGHT + " blocks high");
            check(at.contains(side[0] + ",0," + side[1]) && at.contains(side[0] + ",1," + side[1]), "the door cell exists in both layers");
            for (HouseShape.Cell c : cells) {
                if (c.dx() == side[0] && c.dz() == side[1] && c.dy() <= 1) check(c.kind() == HouseShape.Kind.OPEN, "the doorway is open, two high");
                if (c.dx() == 0 && c.dz() == 0 && c.dy() <= 1) check(c.kind() == HouseShape.Kind.OPEN, "the hollow behind the door is open");
                if (c.dy() == 2) check(c.kind() == HouseShape.Kind.ROOF, "the roof covers the whole footprint, the doorway too");
            }
            // walls before roof before open cells, so that the world code can place them in this order
            int lastWall = -1, firstRoof = Integer.MAX_VALUE, lastRoof = -1, firstOpen = Integer.MAX_VALUE;
            for (int i = 0; i < cells.size(); i++) {
                switch (cells.get(i).kind()) {
                    case WALL -> lastWall = i;
                    case ROOF -> { firstRoof = Math.min(firstRoof, i); lastRoof = i; }
                    case OPEN -> firstOpen = Math.min(firstOpen, i);
                }
            }
            check(lastWall < firstRoof && lastRoof < firstOpen, "cells come walls, roof, open");
        }
        boolean refused = false;
        try { HouseShape.cells(1, 1); } catch (IllegalArgumentException e) { refused = true; }
        check(refused, "a diagonal door is refused");

        // how many houses a level gets
        check(Settlement.housesFor(0) == 0 && Settlement.housesFor(1) == 0 && Settlement.housesFor(2) == 2 && Settlement.housesFor(3) == 4
                && Settlement.housesFor(4) == 6 && Settlement.housesFor(5) == 8 && Settlement.housesFor(9) == Settlement.MAX_HOUSES, "houses: 0, 2, 4, 6, 8 for 1 to 5 trees, never more than the maximum");
        for (int t = 2; t <= 5; t++) check(Settlement.housesFor(t) > Settlement.housesFor(t - 1), "every extra tree brings more houses");

        // the layout of the houses of random settlements of every level
        for (int lv = 1; lv <= 5; lv++) {
            for (int n = 0; n < 400; n++) {
                Settlement st = Settlement.generate(rng, lv);
                if (lv == 1) { check(st.houses().isEmpty(), "a lone tree has no houses"); continue; }
                check(st.houses().size() == Settlement.MAX_HOUSES, "level " + lv + ": the full list of 8 candidate spots is there (spares), got " + st.houses().size());
                check(st.houses().size() >= Settlement.housesFor(lv), "enough candidates for the level");
                for (int i = 0; i < st.houses().size(); i++) {
                    Settlement.House h = st.houses().get(i);
                    check(Math.max(Math.abs(h.dx()), Math.abs(h.dz())) + 1 <= Settlement.MAX_EXTENT, "a house stays within the safe extent, got centre " + h.dx() + "," + h.dz());
                    for (Settlement.Cluster c : st.clusters()) {
                        check(Math.max(Math.abs(h.dx() - c.dx()), Math.abs(h.dz() - c.dz())) >= Settlement.HOUSE_CLEARANCE, "a house is " + Settlement.HOUSE_CLEARANCE + "+ blocks from every trunk (yard, crown and burrows never touch it)");
                    }
                    for (int j = i + 1; j < st.houses().size(); j++) {
                        Settlement.House o = st.houses().get(j);
                        check(Math.max(Math.abs(h.dx() - o.dx()), Math.abs(h.dz() - o.dz())) >= Settlement.HOUSE_SPACING, "two houses never touch");
                    }
                    check(Math.abs(h.doorDx()) + Math.abs(h.doorDz()) == 1, "the door faces one of the four sides");
                    // the door faces the nearest trunk
                    Settlement.Cluster nearest = st.clusters().get(0);
                    int best = Integer.MAX_VALUE;
                    for (Settlement.Cluster c : st.clusters()) {
                        int d = Math.max(Math.abs(h.dx() - c.dx()), Math.abs(h.dz() - c.dz()));
                        if (d < best) { best = d; nearest = c; }
                    }
                    int tx = nearest.dx() - h.dx(), tz = nearest.dz() - h.dz();
                    check(h.doorDx() * tx + h.doorDz() * tz > 0, "the door faces towards the nearest tree");
                }
            }
        }
        check(Settlement.generate(new Random(5), 5).equals(Settlement.generate(new Random(5), 5)), "the same dice make the same settlement, houses included");

        // the glue, by grep
        String warren = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        check(warren.contains("Settlement.housesFor(trees)"), "the number of houses follows the trees really built");
        check(warren.indexOf("trees++;") < warren.indexOf("Settlement.housesFor(trees)"), "houses come after all the trees");
        check(warren.contains("case WALL -> WarrenPalette.get(style, WarrenPalette.Role.WALL);") && warren.contains("case ROOF -> WarrenPalette.get(style, WarrenPalette.Role.ROOF);") && warren.contains("case OPEN -> Blocks.AIR.defaultBlockState();"), "walls and roof from the palette (coarse dirt and grass in the meadow), open cells air");
        check(warren.contains("GuavaTree.replaceable(level.getBlockState(base.offset(cell.dx(), cell.dy(), cell.dz())))") && warren.contains("if (top - low > HOUSE_MAX_SLOPE) {"), "a house needs a nearly level footprint and free space");
        check(warren.indexOf("if (!GuavaTree.replaceable(level.getBlockState(base.offset(cell.dx(), cell.dy(), cell.dz()))))") < warren.indexOf("level.setBlock(base.offset(cell.dx(), cell.dy(), cell.dz()), state, FLAGS);"), "a house checks everything before it places anything");
        String command = Files.readString(src.resolve("command/BunnidogsCommands.java"));
        check(command.contains("built.houses()"), "the command reports the houses");
    }

    // ---------------------------------------------------------------------------------------------
    static void meetingTree() throws Exception {
        System.out.println("meeting tree of the village level (Phase 6h)");
        Random rng = new Random(61);
        for (int i = 0; i < 300; i++) {
            List<TreeShape.Cell> tree = TreeShape.generate(rng, true);
            java.util.Map<String, TreeShape.Cell> at = new java.util.HashMap<>();
            int logs = 0, fruits = 0, maxDy = 0;
            for (TreeShape.Cell c : tree) {
                at.put(c.dx() + "," + c.dy() + "," + c.dz(), c);
                maxDy = Math.max(maxDy, c.dy());
                check(Math.abs(c.dx()) <= TreeShape.GRAND_RADIUS && Math.abs(c.dz()) <= TreeShape.GRAND_RADIUS && c.dy() >= 0, "the grand crown stays within 3 blocks to the side");
                if (c.kind() == TreeShape.Kind.LOG) { logs++; check(c.dx() == 0 && c.dz() == 0, "grand logs are only in the trunk column"); }
                if (c.kind() == TreeShape.Kind.FRUIT) { fruits++; check(c.dy() >= 2, "a grand fruit hangs at least 2 blocks up, at dy " + c.dy()); }
            }
            check(logs == TreeShape.GRAND_MIN_LOGS || logs == TreeShape.GRAND_MIN_LOGS + 1, "the grand trunk has 6 or 7 logs, got " + logs);
            check(fruits >= 3 && fruits <= TreeShape.GRAND_MAX_FRUITS, "3 to 6 grand fruits, got " + fruits);
            check(maxDy == logs + 1, "the grand tree tops out one above the 3x3 cap");
            // every leaf is connected to the trunk through leaves, and every fruit has a leaf above it
            Set<String> seen = new HashSet<>();
            java.util.ArrayDeque<TreeShape.Cell> queue = new java.util.ArrayDeque<>();
            for (TreeShape.Cell c : tree) if (c.kind() == TreeShape.Kind.LOG) { queue.add(c); seen.add(c.dx() + "," + c.dy() + "," + c.dz()); }
            int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            while (!queue.isEmpty()) {
                TreeShape.Cell c = queue.poll();
                for (int[] d : dirs) {
                    String k = (c.dx() + d[0]) + "," + (c.dy() + d[1]) + "," + (c.dz() + d[2]);
                    TreeShape.Cell o = at.get(k);
                    if (o != null && o.kind() == TreeShape.Kind.LEAVES && seen.add(k)) queue.add(o);
                }
            }
            for (TreeShape.Cell c : tree) {
                if (c.kind() == TreeShape.Kind.LEAVES) check(seen.contains(c.dx() + "," + c.dy() + "," + c.dz()), "every grand leaf is connected to the trunk");
                if (c.kind() == TreeShape.Kind.FRUIT) {
                    TreeShape.Cell above = at.get(c.dx() + "," + (c.dy() + 1) + "," + c.dz());
                    check(above != null && above.kind() == TreeShape.Kind.LEAVES, "a grand fruit hangs under a leaf");
                }
            }
            check(at.values().stream().anyMatch(c -> c.kind() == TreeShape.Kind.LEAVES && (Math.abs(c.dx()) == 3 || Math.abs(c.dz()) == 3)), "the grand crown really reaches radius 3");
            // the ordinary tree is untouched: the 1-argument form IS the grand = false form
            long seed = rng.nextLong();
            check(TreeShape.generate(new Random(seed)).equals(TreeShape.generate(new Random(seed), false)), "generate(rng) is generate(rng, false)");
        }
        // WarrenShape: the grand cluster keeps the trunk column and the ground around it free
        for (int i = 0; i < 100; i++) {
            WarrenShape w = WarrenShape.generate(rng, true);
            check(w.tree().stream().filter(c -> c.kind() == TreeShape.Kind.LOG).count() >= TreeShape.GRAND_MIN_LOGS, "the grand cluster has the grand trunk");
            check(w.burrows().size() >= WarrenShape.MIN_BURROWS && w.dogs().size() >= WarrenShape.MIN_DOGS, "the grand cluster still has burrows and dogs");
            for (WarrenShape.Spot s : w.burrows()) check(!(s.dx() == 0 && s.dz() == 0), "no burrow under the grand trunk");
            for (WarrenShape.Spot s : w.dogs()) check(!(s.dx() == 0 && s.dz() == 0), "no dog under the grand trunk");
        }
        // Settlement: ONLY the centre cluster of a level-5 village is grand
        for (int lv = 1; lv <= 5; lv++) {
            for (int i = 0; i < 100; i++) {
                Settlement st = Settlement.generate(rng, lv);
                for (int k = 0; k < st.clusters().size(); k++) {
                    long logs = st.clusters().get(k).shape().tree().stream().filter(c -> c.kind() == TreeShape.Kind.LOG).count();
                    boolean grand = logs >= TreeShape.GRAND_MIN_LOGS;
                    check(grand == (lv == 5 && k == 0), "level " + lv + " cluster " + k + (grand ? " is" : " is not") + " the meeting tree");
                }
            }
        }
        // the village's houses and landmarks still keep their distance from the (wider) crown
        for (int i = 0; i < 100; i++) {
            Settlement st = Settlement.generate(rng, 5);
            for (Settlement.House h : st.houses()) check(Math.max(Math.abs(h.dx()), Math.abs(h.dz())) - 1 > TreeShape.GRAND_RADIUS, "a house stays clear of the grand crown");
            for (Settlement.Prop p : st.props()) check(Math.max(Math.abs(p.dx()), Math.abs(p.dz())) - 1 > TreeShape.GRAND_RADIUS, "a landmark stays clear of the grand crown");
        }
        String shape = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/layout/Settlement.java"));
        check(shape.contains("i == 0 && n == MAX_LEVEL"), "Settlement makes only the centre of a level-5 settlement grand");
    }

    static void guavaWood() throws Exception {
        System.out.println("guava wood and decaying leaves (Phase 7a)");
        // distances: a hand-made tree, then the vanilla rule on random trees
        List<TreeShape.Cell> hand = List.of(
                new TreeShape.Cell(0, 0, 0, TreeShape.Kind.LOG), new TreeShape.Cell(0, 1, 0, TreeShape.Kind.LOG),
                new TreeShape.Cell(1, 1, 0, TreeShape.Kind.LEAVES), new TreeShape.Cell(2, 1, 0, TreeShape.Kind.LEAVES),
                new TreeShape.Cell(2, 2, 0, TreeShape.Kind.LEAVES), new TreeShape.Cell(0, 2, 0, TreeShape.Kind.LEAVES),
                new TreeShape.Cell(5, 2, 0, TreeShape.Kind.LEAVES));
        java.util.Map<TreeShape.Cell, Integer> hd = TreeShape.leafDistances(hand);
        check(hd.get(hand.get(2)) == 1 && hd.get(hand.get(3)) == 2 && hd.get(hand.get(4)) == 3 && hd.get(hand.get(5)) == 1, "distances 1, 2, 3 away from the logs, 1 on top of the trunk");
        check(hd.get(hand.get(6)) == TreeShape.MAX_LEAF_DISTANCE && hd.size() == 5, "a leaf no log reaches has distance 7 (it would decay); logs are not in the map");
        Random rng = new Random(71);
        for (int i = 0; i < 300; i++) {
            List<TreeShape.Cell> tree = TreeShape.generate(rng, i % 2 == 0);
            java.util.Map<TreeShape.Cell, Integer> d = TreeShape.leafDistances(tree);
            java.util.Map<String, TreeShape.Cell> at = new java.util.HashMap<>();
            for (TreeShape.Cell c : tree) at.put(c.dx() + "," + c.dy() + "," + c.dz(), c);
            for (java.util.Map.Entry<TreeShape.Cell, Integer> e : d.entrySet()) {
                TreeShape.Cell c = e.getKey();
                check(e.getValue() >= 1 && e.getValue() <= 6, "no generated leaf is out of reach of a log (would decay at once), got " + e.getValue());
                int best = 7;
                int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
                for (int[] dir : dirs) {
                    TreeShape.Cell o = at.get((c.dx() + dir[0]) + "," + (c.dy() + dir[1]) + "," + (c.dz() + dir[2]));
                    if (o == null) continue;
                    if (o.kind() == TreeShape.Kind.LOG) best = Math.min(best, 1);
                    else if (o.kind() == TreeShape.Kind.LEAVES) best = Math.min(best, d.get(o) + 1);
                }
                check(e.getValue() == Math.min(7, best), "distance = 1 + the smallest neighbour distance (vanilla's rule)");
            }
        }
        // resources
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data");
        for (String n : new String[]{"guava_log", "guava_leaves"}) {
            check(Files.exists(assets.resolve("blockstates/" + n + ".json")) && Files.exists(assets.resolve("models/block/" + n + ".json"))
                    && Files.exists(assets.resolve("items/" + n + ".json")) && Files.exists(data.resolve("bunnidogs/loot_table/blocks/" + n + ".json")), n + " has blockstate, model, client item and loot table");
            check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"block.bunnidogs." + n + "\"") && Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs." + n + "\""), n + " has its names");
        }
        for (String tex : new String[]{"guava_log", "guava_log_top", "guava_leaves"}) {
            byte[] png = Files.readAllBytes(assets.resolve("textures/block/" + tex + ".png"));
            int[] size = pngSize(png);
            check(size[0] == 16 && size[1] == 16, tex + " is 16x16");
        }
        check(Files.readString(data.resolve("minecraft/tags/block/logs.json")).contains("bunnidogs:guava_log"), "the log is in minecraft:logs (leaves measure their distance to that tag)");
        check(Files.readString(data.resolve("minecraft/tags/block/leaves.json")).contains("bunnidogs:guava_leaves"), "the leaves are in minecraft:leaves");
        check(Files.readString(data.resolve("minecraft/tags/block/mineable/axe.json")).contains("guava_log") && Files.readString(data.resolve("minecraft/tags/block/mineable/hoe.json")).contains("guava_leaves"), "axe for the log, hoe for the leaves");
        String blockstate = compact(Files.readString(assets.resolve("blockstates/guava_log.json")));
        check(blockstate.contains("\"axis=y\"") && blockstate.contains("\"axis=x\"") && blockstate.contains("\"axis=z\""), "the log has all three axes");
        check(Files.readString(assets.resolve("models/block/guava_leaves.json")).contains("\"render_type\": \"cutout\""), "the leaf model is cutout (holes)");
        // texture holes: the leaves have see-through pixels, the log has none
        java.awt.image.BufferedImage leaf = javax.imageio.ImageIO.read(assets.resolve("textures/block/guava_leaves.png").toFile());
        java.awt.image.BufferedImage log = javax.imageio.ImageIO.read(assets.resolve("textures/block/guava_log.png").toFile());
        int clear = 0, clearLog = 0;
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) { if ((leaf.getRGB(x, y) >>> 24) == 0) clear++; if ((log.getRGB(x, y) >>> 24) != 255) clearLog++; }
        check(clear >= 10 && clear <= 100 && clearLog == 0, "leaves have holes (" + clear + "), the log is fully opaque");
        // glue
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("registerBlock(\"guava_log\", RotatedPillarBlock::new") && blocks.contains("new TintedParticleLeavesBlock(0.0F, props)") && blocks.contains("registerBlock(\"guava_leaves\""), "both blocks are registered");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("registerSimpleBlockItem(ModBlocks.GUAVA_LOG)") && items.contains("registerSimpleBlockItem(ModBlocks.GUAVA_LEAVES)") && items.contains("event.accept(GUAVA_LOG.get())") && items.contains("event.accept(GUAVA_LEAVES.get())"), "both block items exist and are in the Natural Blocks tab");
        String tree = Files.readString(src.resolve("worldgen/GuavaTree.java"));
        check(tree.contains("TreeShape.leafDistances(cells)") && tree.contains("LeavesBlock.DISTANCE, distances.get(cell)"), "placed leaves get their computed distance");
    }

    static void guavaPlanks() throws Exception {
        System.out.println("guava planks, stripped log, wood (W1)");
        Path res = Path.of("src/main/resources");
        Path assets = res.resolve("assets/bunnidogs");
        Path data = res.resolve("data");
        String[] names = {"guava_planks", "stripped_guava_log", "guava_wood", "stripped_guava_wood"};
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        for (String n : names) {
            check(Files.exists(assets.resolve("blockstates/" + n + ".json")) && Files.exists(assets.resolve("models/block/" + n + ".json"))
                    && Files.exists(assets.resolve("items/" + n + ".json")) && Files.exists(data.resolve("bunnidogs/loot_table/blocks/" + n + ".json")), n + " has blockstate, model, client item and loot table");
            check(Files.readString(data.resolve("bunnidogs/loot_table/blocks/" + n + ".json")).contains("\"bunnidogs:" + n + "\""), n + " drops itself");
            check(lang.contains("\"block.bunnidogs." + n + "\"") && lang.contains("\"item.bunnidogs." + n + "\""), n + " has its names");
            check(Files.readString(data.resolve("minecraft/tags/block/mineable/axe.json")).contains("bunnidogs:" + n), n + " is axe-mineable");
        }
        for (String tex : new String[] {"guava_planks", "stripped_guava_log", "stripped_guava_log_top"}) {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(assets.resolve("textures/block/" + tex + ".png").toFile());
            boolean opaque = true;
            for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) opaque &= (img.getRGB(x, y) >>> 24) == 255;
            check(img.getWidth() == 16 && img.getHeight() == 16 && opaque, tex + ".png is 16x16 and fully opaque");
        }
        // the three pillar forms have all three axes; the planks have one variant
        for (String n : new String[] {"stripped_guava_log", "guava_wood", "stripped_guava_wood"}) {
            String st = compact(Files.readString(assets.resolve("blockstates/" + n + ".json")));
            check(st.contains("\"axis=y\"") && st.contains("\"axis=x\"") && st.contains("\"axis=z\""), n + " has all three axes");
        }
        // tags: the log forms are logs (leaves measure to them) and burn; the planks are planks, as items too
        for (String kind : new String[] {"block", "item"}) {
            String logs = Files.readString(data.resolve("minecraft/tags/" + kind + "/logs.json"));
            String burn = Files.readString(data.resolve("minecraft/tags/" + kind + "/logs_that_burn.json"));
            for (String n : new String[] {"stripped_guava_log", "guava_wood", "stripped_guava_wood"}) {
                check(logs.contains("bunnidogs:" + n) && burn.contains("bunnidogs:" + n), kind + " tags: " + n + " is a burning log");
            }
            check(burn.contains("bunnidogs:guava_log"), kind + " tags: the plain log burns too");
            check(Files.readString(data.resolve("minecraft/tags/" + kind + "/planks.json")).contains("bunnidogs:guava_planks"), kind + " tag minecraft:planks has the guava planks");
        }
        String guavaLogs = Files.readString(data.resolve("bunnidogs/tags/item/guava_logs.json"));
        check(guavaLogs.contains("bunnidogs:guava_log") && guavaLogs.contains("bunnidogs:stripped_guava_log") && guavaLogs.contains("bunnidogs:guava_wood") && guavaLogs.contains("bunnidogs:stripped_guava_wood"), "bunnidogs:guava_logs holds the four log forms");
        // recipes
        String planks = compact(Files.readString(data.resolve("bunnidogs/recipe/guava_planks.json")));
        check(planks.contains("\"minecraft:crafting_shapeless\"") && planks.contains("\"ingredients\":[\"#bunnidogs:guava_logs\"]") && planks.contains("\"id\":\"bunnidogs:guava_planks\",\"count\":4"), "any guava log -> 4 planks");
        String wood = compact(Files.readString(data.resolve("bunnidogs/recipe/guava_wood.json")));
        check(wood.contains("\"pattern\":[\"LL\",\"LL\"]") && wood.contains("\"L\":\"bunnidogs:guava_log\"") && wood.contains("\"id\":\"bunnidogs:guava_wood\",\"count\":3"), "4 logs -> 3 wood");
        String stripped = compact(Files.readString(data.resolve("bunnidogs/recipe/stripped_guava_wood.json")));
        check(stripped.contains("\"pattern\":[\"LL\",\"LL\"]") && stripped.contains("\"L\":\"bunnidogs:stripped_guava_log\"") && stripped.contains("\"id\":\"bunnidogs:stripped_guava_wood\",\"count\":3"), "4 stripped logs -> 3 stripped wood");
        // the Genetics Table already takes any planks, so it takes these through the tag
        check(Files.readString(data.resolve("bunnidogs/recipe/genetics_table.json")).contains("#minecraft:planks"), "the Genetics Table recipe takes #minecraft:planks");
        // glue
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("registerBlock(\"guava_planks\", Block::new,") && blocks.contains("registerBlock(\"stripped_guava_log\", RotatedPillarBlock::new,")
                && blocks.contains("registerBlock(\"guava_wood\", RotatedPillarBlock::new,") && blocks.contains("registerBlock(\"stripped_guava_wood\", RotatedPillarBlock::new,")
                && blocks.contains("import net.minecraft.world.level.block.Block;"), "the four blocks are registered (and Block is imported)");
        // W1b: the axe hook
        String strip = Files.readString(src.resolve("block/GuavaStripping.java"));
        check(strip.contains("@EventBusSubscriber(modid = BunnidogsMod.MODID)") && strip.contains("BlockEvent.BlockToolModificationEvent event") && strip.contains("event.getItemAbility() != ItemAbilities.AXE_STRIP"), "W1b: a static subscriber that only reacts to the axe-strip ability");
        check(strip.contains("state.is(ModBlocks.GUAVA_LOG.get())") && strip.contains("ModBlocks.STRIPPED_GUAVA_LOG.get()") && strip.contains("state.is(ModBlocks.GUAVA_WOOD.get())") && strip.contains("ModBlocks.STRIPPED_GUAVA_WOOD.get()"), "W1b: log -> stripped log, wood -> stripped wood");
        check(strip.contains("event.setFinalState(") && strip.contains("setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))"), "W1b: the axis is kept");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        for (String f : new String[] {"GUAVA_PLANKS", "STRIPPED_GUAVA_LOG", "GUAVA_WOOD", "STRIPPED_GUAVA_WOOD"}) {
            check(items.contains("registerSimpleBlockItem(ModBlocks." + f + ")") && items.contains("event.accept(" + f + ".get())"), f + ": block item registered and in a creative tab");
        }
    }

    static void guavaParts() throws Exception {
        System.out.println("guava stairs, slab, fence (W2)");
        Path res = Path.of("src/main/resources");
        Path assets = res.resolve("assets/bunnidogs");
        Path data = res.resolve("data");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        for (String n : new String[] {"guava_stairs", "guava_slab", "guava_fence"}) {
            check(Files.exists(assets.resolve("blockstates/" + n + ".json")) && Files.exists(assets.resolve("items/" + n + ".json")) && Files.exists(data.resolve("bunnidogs/loot_table/blocks/" + n + ".json"))
                    && Files.exists(data.resolve("bunnidogs/recipe/" + n + ".json")), n + " has blockstate, client item, loot table and recipe");
            check(lang.contains("\"block.bunnidogs." + n + "\"") && lang.contains("\"item.bunnidogs." + n + "\""), n + " has its names");
            check(Files.readString(data.resolve("minecraft/tags/block/mineable/axe.json")).contains("bunnidogs:" + n), n + " is axe-mineable");
        }
        // stairs: 40 variants (4 facings x 2 halves x 5 shapes), each pointing at one of the three models, every model exists
        String stairs = Files.readString(assets.resolve("blockstates/guava_stairs.json"));
        check(stairs.split("\"facing=", -1).length - 1 == 40, "the stairs blockstate has 40 variants");
        for (String f : new String[] {"east", "south", "west", "north"}) for (String h : new String[] {"bottom", "top"}) for (String sh : new String[] {"straight", "inner_left", "inner_right", "outer_left", "outer_right"}) {
            check(stairs.contains("\"facing=" + f + ",half=" + h + ",shape=" + sh + "\""), "stairs variant " + f + "/" + h + "/" + sh);
        }
        for (String m : new String[] {"guava_stairs", "guava_stairs_inner", "guava_stairs_outer", "guava_slab", "guava_slab_top", "guava_fence_post", "guava_fence_side", "guava_fence_inventory"}) {
            check(Files.readString(assets.resolve("models/block/" + m + ".json")).contains("bunnidogs:block/guava_planks"), "model " + m + " uses the planks texture");
        }
        String slab = compact(Files.readString(assets.resolve("blockstates/guava_slab.json")));
        check(slab.contains("\"type=bottom\"") && slab.contains("\"type=top\"") && slab.contains("\"type=double\":{\"model\":\"bunnidogs:block/guava_planks\"}"), "slab: bottom, top and a double slab that looks like planks");
        check(compact(Files.readString(data.resolve("bunnidogs/loot_table/blocks/guava_slab.json"))).contains("\"count\":2") && Files.readString(data.resolve("bunnidogs/loot_table/blocks/guava_slab.json")).contains("\"type\": \"double\""), "a double slab drops two");
        String fence = Files.readString(assets.resolve("blockstates/guava_fence.json"));
        check(fence.split("\"when\"", -1).length - 1 == 4 && fence.contains("\"y\": 270"), "fence: a post and four sides");
        check(Files.readString(assets.resolve("items/guava_fence.json")).contains("guava_fence_inventory"), "the fence item shows the inventory model");
        // recipes
        String rs = Files.readString(data.resolve("bunnidogs/recipe/guava_stairs.json"));       // not compacted: the pattern rows hold spaces
        check(rs.contains("\"P  \",") && rs.contains("\"PP \",") && rs.contains("\"PPP\"") && compact(rs).contains("\"id\":\"bunnidogs:guava_stairs\",\"count\":4"), "6 planks in a stair shape -> 4 stairs");
        String rl = compact(Files.readString(data.resolve("bunnidogs/recipe/guava_slab.json")));
        check(rl.contains("\"pattern\":[\"PPP\"]") && rl.contains("\"id\":\"bunnidogs:guava_slab\",\"count\":6"), "3 planks -> 6 slabs");
        String rf = compact(Files.readString(data.resolve("bunnidogs/recipe/guava_fence.json")));
        check(rf.contains("\"pattern\":[\"PSP\",\"PSP\"]") && rf.contains("\"minecraft:stick\"") && rf.contains("\"id\":\"bunnidogs:guava_fence\",\"count\":3"), "4 planks + 2 sticks -> 3 fences");
        // tags: vanilla's wooden families (fuel, fence joining) in block and item form, and the fence joins minecraft:fences
        for (String kind : new String[] {"block", "item"}) {
            check(Files.readString(data.resolve("minecraft/tags/" + kind + "/wooden_stairs.json")).contains("bunnidogs:guava_stairs")
                    && Files.readString(data.resolve("minecraft/tags/" + kind + "/wooden_slabs.json")).contains("bunnidogs:guava_slab")
                    && Files.readString(data.resolve("minecraft/tags/" + kind + "/wooden_fences.json")).contains("bunnidogs:guava_fence")
                    && Files.readString(data.resolve("minecraft/tags/" + kind + "/fences.json")).contains("bunnidogs:guava_fence"), kind + " tags: wooden stairs, slabs, fences and fences");
        }
        // glue
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("props -> new StairBlock(GUAVA_PLANKS.get().defaultBlockState(), props)") && blocks.contains("registerBlock(\"guava_slab\", SlabBlock::new,") && blocks.contains("registerBlock(\"guava_fence\", FenceBlock::new,"), "the three blocks are registered");
        check(blocks.indexOf("GUAVA_PLANKS = ") < blocks.indexOf("GUAVA_STAIRS = "), "the stairs are declared after the planks they copy");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        for (String f : new String[] {"GUAVA_STAIRS", "GUAVA_SLAB", "GUAVA_FENCE"}) {
            check(items.contains("registerSimpleBlockItem(ModBlocks." + f + ")") && items.contains("event.accept(" + f + ".get())"), f + ": block item registered and in a creative tab");
        }
    }

    static void towers() throws Exception {
        System.out.println("house forms by style (Phase 6g)");
        Path src = Path.of("src/main/java/com/bunnidogs/mod/worldgen");
        int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] side : sides) {
            List<HouseShape.Cell> cells = HouseShape.cells(side[0], side[1], HouseShape.Form.TOWER);
            long walls = cells.stream().filter(c -> c.kind() == HouseShape.Kind.WALL).count();
            long windows = cells.stream().filter(c -> c.kind() == HouseShape.Kind.WINDOW).count();
            long open = cells.stream().filter(c -> c.kind() == HouseShape.Kind.OPEN).count();
            long roof = cells.stream().filter(c -> c.kind() == HouseShape.Kind.ROOF).count();
            check(walls == 19 && windows == 3 && open == 5 && roof == 13 && cells.size() == 40, "tower door " + side[0] + "," + side[1] + ": 19 wall, 3 window, 5 open, 13 roof cells");
            Set<String> seen = new HashSet<>();
            boolean ok = true;
            int maxDy = 0;
            for (HouseShape.Cell c : cells) {
                ok &= seen.add(c.dx() + "," + c.dy() + "," + c.dz()) && Math.abs(c.dx()) <= 1 && Math.abs(c.dz()) <= 1;
                maxDy = Math.max(maxDy, c.dy());
                if (c.dx() == side[0] && c.dz() == side[1] && c.dy() <= 1) ok &= c.kind() == HouseShape.Kind.OPEN;
                if (c.dx() == side[0] && c.dz() == side[1] && c.dy() == 2) ok &= c.kind() == HouseShape.Kind.WALL;
                if (c.kind() == HouseShape.Kind.WINDOW) ok &= c.dy() == 1 && !(c.dx() == side[0] && c.dz() == side[1]);
            }
            check(ok && maxDy == HouseShape.TOWER_HEIGHT - 1, "tower door " + side[0] + "," + side[1] + ": no cell twice, door open two high with a lintel, windows never in the door side, " + HouseShape.TOWER_HEIGHT + " high");
        }
        check(HouseShape.cells(1, 0).equals(HouseShape.cells(1, 0, HouseShape.Form.MOUND)), "the plain call is still the mound");
        boolean refused = false;
        try { HouseShape.cells(1, 1, HouseShape.Form.TOWER); } catch (IllegalArgumentException e) { refused = true; }
        check(refused, "a tower door must face a side");
        String palette = Files.readString(src.resolve("WarrenPalette.java"));
        String warren = Files.readString(src.resolve("WarrenFeature.java"));
        check(palette.contains("case MEADOW, TUNDRA, SWAMP, CAVE -> HouseShape.Form.MOUND;") && palette.contains("case DESERT, MOUNTAIN, NETHER, END -> HouseShape.Form.TOWER;"), "mounds in the meadow, tundra, swamp and caves; towers elsewhere");
        check(warren.contains("WarrenPalette.houseForm(style)") && warren.contains("case WINDOW -> Blocks.GLASS.defaultBlockState();"), "the house takes its form from the style and its windows are glass");
    }

    static void palette() throws Exception {
        System.out.println("settlement styles by biome (Phase 6f)");
        Path src = Path.of("src/main/java/com/bunnidogs/mod/worldgen");
        String palette = Files.readString(src.resolve("WarrenPalette.java"));
        String warren = Files.readString(src.resolve("WarrenFeature.java"));
        String piece = Files.readString(src.resolve("WarrenPiece.java"));
        for (Lineage l : Lineage.values()) {
            check(palette.contains("put(Lineage." + l.name() + ","), l + " has a style");
        }
        check(palette.contains("put(Lineage.MEADOW,   Blocks.COARSE_DIRT,   Blocks.GRASS_BLOCK,   Blocks.DIRT,          Blocks.DIRT_PATH"), "the meadow keeps the old earth look (coarse dirt, grass, dirt path)");
        check(palette.contains("Lineage.DESERT,   Blocks.SANDSTONE") && palette.contains("Lineage.NETHER,   Blocks.NETHER_BRICKS") && palette.contains("Lineage.END,      Blocks.END_STONE_BRICKS"), "desert is sandstone, the Nether nether bricks, the End end stone bricks");
        check(!warren.contains("Blocks.COARSE_DIRT.defaultBlockState();\n                case ROOF") && !warren.contains("Blocks.DIRT_PATH") && warren.contains("WarrenPalette.get(style, WarrenPalette.Role.WALL)"), "houses and paths take their blocks from the palette");
        check(warren.contains("Lineage style = ModBiomeTags.lineageAt(level, floor);") && piece.contains("ModBiomeTags.lineageAt(world, new BlockPos(centreX, y, centreZ))"), "one style per settlement, from the biome at its centre");
    }

    static void props() throws Exception {
        System.out.println("landmarks of the village levels (Phase 6e)");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        Random rng = new Random(6565);
        check(PropShape.kindsFor(1).isEmpty() && PropShape.kindsFor(2).equals(List.of(PropShape.Kind.HAYSTACK))
                && PropShape.kindsFor(3).size() == 2 && PropShape.kindsFor(4).size() == 3 && PropShape.kindsFor(5).size() == 5, "landmarks per level: 0, 1, 2, 3, 5");
        check(PropShape.kindsFor(5).get(3) == PropShape.Kind.FARM && PropShape.kindsFor(5).get(4) == PropShape.Kind.PLAZA, "the village level adds the farm plot and the lamp plaza");
        // every landmark: inside 3x3, between dy -1 and TOP, no cell twice, ground-level cells first, all four turns keep the same cell count
        for (PropShape.Kind kind : PropShape.Kind.values()) {
            int count = PropShape.cells(kind, 0).size();
            for (int t = 0; t < 4; t++) {
                List<PropShape.Cell> cells = PropShape.cells(kind, t);
                Set<String> seen = new HashSet<>();
                int lastDy = -9;
                boolean ok = cells.size() == count;
                for (PropShape.Cell c : cells) {
                    ok &= Math.abs(c.dx()) <= PropShape.RADIUS && Math.abs(c.dz()) <= PropShape.RADIUS && c.dy() >= -1 && c.dy() <= PropShape.TOP;
                    ok &= seen.add(c.dx() + "," + c.dy() + "," + c.dz()) && c.dy() >= lastDy;
                    lastDy = c.dy();
                }
                check(ok, kind + " turned " + t + ": inside 3x3, no cell twice, lowest first");
            }
        }
        long wells = PropShape.cells(PropShape.Kind.WELL, 0).stream().filter(c -> c.mat() == PropShape.Mat.WATER).count();
        long farmland = PropShape.cells(PropShape.Kind.FARM, 0).stream().filter(c -> c.mat() == PropShape.Mat.FARMLAND).count();
        check(wells == 1 && farmland == 8 && PropShape.cells(PropShape.Kind.PLAZA, 0).stream().filter(c -> c.mat() == PropShape.Mat.LANTERN).count() == 4,
                "well: 1 water; farm: 8 farmland around the water; plaza: 4 lanterns");
        // every farmland cell has its crop on top, and a lantern always sits on a post
        List<PropShape.Cell> farm = PropShape.cells(PropShape.Kind.FARM, 1);
        boolean crops = true;
        for (PropShape.Cell c : farm) {
            if (c.mat() == PropShape.Mat.FARMLAND) {
                crops &= farm.stream().anyMatch(o -> o.dx() == c.dx() && o.dz() == c.dz() && o.dy() == 0);
            }
        }
        check(crops, "every farmland block has a crop above it");
        // 300 random settlements per level: all landmarks found, apart, clear of trunks and houses, inside the extent, none on a path
        for (int lv = 2; lv <= 5; lv++) {
            boolean all = true, apart = true, clear = true, inside = true, noPath = true;
            for (int i = 0; i < 300; i++) {
                Settlement st = Settlement.generate(rng, lv);
                all &= st.props().size() == PropShape.kindsFor(lv).size();
                List<Settlement.House> planned = st.houses().subList(0, Settlement.housesFor(lv));
                Set<String> path = new HashSet<>();
                for (PathShape.Cell c : PathShape.network(st.clusters(), planned, st.props())) { path.add(c.dx() + "," + c.dz()); }
                for (int a = 0; a < st.props().size(); a++) {
                    Settlement.Prop p = st.props().get(a);
                    inside &= Math.max(Math.abs(p.dx()), Math.abs(p.dz())) + PropShape.RADIUS <= Settlement.MAX_EXTENT - 1;
                    for (int b = a + 1; b < st.props().size(); b++) {
                        Settlement.Prop q = st.props().get(b);
                        apart &= Math.max(Math.abs(p.dx() - q.dx()), Math.abs(p.dz() - q.dz())) >= Settlement.PROP_SPACING;
                    }
                    for (Settlement.Cluster c : st.clusters()) { clear &= Math.max(Math.abs(p.dx() - c.dx()), Math.abs(p.dz() - c.dz())) >= Settlement.PROP_CLEARANCE; }
                    for (Settlement.House h : st.houses()) { clear &= Math.max(Math.abs(p.dx() - h.dx()), Math.abs(p.dz() - h.dz())) >= Settlement.PROP_SPACING; }
                    for (int dx = -1; dx <= 1; dx++) { for (int dz = -1; dz <= 1; dz++) { noPath &= !path.contains((p.dx() + dx) + "," + (p.dz() + dz)); } }
                }
            }
            check(all && apart && clear && inside && noPath, "level " + lv + ": every landmark has a spot, apart, clear of trunks and houses, inside the extent, off the paths");
        }
        check(Settlement.generate(new Random(5), 5).props().equals(Settlement.generate(new Random(5), 5).props()), "the same dice give the same landmarks");
        // the glue, by grep
        String warren = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        String piece = Files.readString(src.resolve("worldgen/WarrenPiece.java"));
        String structure = Files.readString(src.resolve("worldgen/WarrenStructure.java"));
        check(warren.contains("static boolean buildProp(") && warren.contains("PathShape.network(builtTrees, builtHouses, builtProps)")
                && warren.indexOf("buildProp(level, floor.getX()") < warren.indexOf("PathShape.network(builtTrees"), "the feature builds the landmarks before it lays the paths, and the paths avoid them");
        check(piece.contains("PROP = 3") && piece.contains("PathShape.network(s.clusters(), houses, s.props())") && structure.contains("WarrenPiece.PROP"), "the structure has one piece per landmark");
    }

    static void paths() throws Exception {
        System.out.println("footpaths in settlements (Phase 6c)");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        Random rng = new Random(6363);

        // an exact little settlement: three trees in a row, one house south of the centre tree with its door to the north
        WarrenShape bare = new WarrenShape(List.of(), List.of(), List.of(), List.of(), List.of());
        List<Settlement.Cluster> row = List.of(new Settlement.Cluster(0, 0, bare), new Settlement.Cluster(8, 0, bare), new Settlement.Cluster(-8, 0, bare));
        List<PathShape.Cell> spokes = PathShape.network(row, List.of());
        check(spokes.size() == 10, "two spokes of 5 blocks (the trunks and one block around them stay free), got " + spokes.size());
        check(spokes.contains(new PathShape.Cell(2, 0)) && spokes.contains(new PathShape.Cell(6, 0)) && spokes.contains(new PathShape.Cell(-2, 0)) && spokes.contains(new PathShape.Cell(-6, 0)), "the spokes run from the edge of one tree to the edge of the next");
        check(!spokes.contains(new PathShape.Cell(1, 0)) && !spokes.contains(new PathShape.Cell(7, 0)) && !spokes.contains(new PathShape.Cell(0, 0)), "no path at the trunk or next to it");
        List<PathShape.Cell> withHouse = PathShape.network(row, List.of(new Settlement.House(0, 6, 0, -1)));
        check(withHouse.size() == 13, "the house adds the 3 blocks between its door and the tree, got " + (withHouse.size() - 10));
        check(withHouse.contains(new PathShape.Cell(0, 4)) && withHouse.contains(new PathShape.Cell(0, 2)) && !withHouse.contains(new PathShape.Cell(0, 5)) && !withHouse.contains(new PathShape.Cell(0, 1)), "it starts right in front of the door, never on the house, and stops at the tree");
        check(PathShape.network(row.subList(0, 2), List.of()).isEmpty() && PathShape.network(row.subList(0, 1), List.of()).isEmpty(), "one or two trees have no paths");

        // random settlements of level 3 to 5, with everything built (the best case for the layout)
        int[] networkSizes = new int[3];
        for (int lv = 3; lv <= 5; lv++) {
            for (int n = 0; n < 100; n++) {
                Settlement st = Settlement.generate(rng, lv);
                List<Settlement.House> houses = st.houses().subList(0, Settlement.housesFor(lv));
                List<PathShape.Cell> net = PathShape.network(st.clusters(), houses);
                networkSizes[lv - 3] += net.size();
                check(!net.isEmpty() && new HashSet<>(net).size() == net.size(), "level " + lv + ": a network with no duplicates");
                for (PathShape.Cell c : net) {
                    check(Math.max(Math.abs(c.dx()), Math.abs(c.dz())) <= Settlement.MAX_EXTENT, "a path block is inside the 12-block limit");
                    for (Settlement.House h : houses) {
                        check(Math.max(Math.abs(c.dx() - h.dx()), Math.abs(c.dz() - h.dz())) > 1, "no path on a house");
                    }
                    for (Settlement.Cluster t : st.clusters()) {
                        check(Math.max(Math.abs(c.dx() - t.dx()), Math.abs(c.dz() - t.dz())) > PathShape.TRUNK_CLEARANCE, "no path at a trunk");
                        check(!t.shape().burrows().contains(new WarrenShape.Spot(c.dx() - t.dx(), c.dz() - t.dz())), "no path on a burrow hole");
                    }
                }
                for (int i = 1; i < st.clusters().size(); i++) {
                    Settlement.Cluster t = st.clusters().get(i);
                    boolean reaches = false;
                    for (PathShape.Cell c : net) {
                        int d = Math.max(Math.abs(c.dx() - t.dx()), Math.abs(c.dz() - t.dz()));
                        reaches |= d == 2 || d == 3;
                    }
                    check(reaches, "a spoke arrives at every tree");
                }
                for (Settlement.House h : houses) {
                    check(net.contains(new PathShape.Cell(h.dx() + 2 * h.doorDx(), h.dz() + 2 * h.doorDz())), "every house has a path in front of its door");
                }
            }
        }
        check(networkSizes[0] < networkSizes[2] && networkSizes[0] > 0, "a bigger settlement has more path (" + networkSizes[0] + " / " + networkSizes[1] + " / " + networkSizes[2] + " blocks over 100 settlements)");
        Settlement a = Settlement.generate(new Random(9), 5);
        check(PathShape.network(a.clusters(), a.houses()).equals(PathShape.network(a.clusters(), a.houses())), "the same settlement gets the same paths");

        // the glue, by grep
        String warren = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        check(warren.contains("PathShape.network(builtTrees, builtHouses, builtProps)") && warren.indexOf("builtHouses.add(house);") < warren.indexOf("PathShape.network(builtTrees, builtHouses, builtProps)"), "paths are worked out from the trees and houses really built, after the houses");
        check(warren.contains("level.setBlock(pos, WarrenPalette.get(style, WarrenPalette.Role.PATH), FLAGS);") && warren.contains("state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)") && warren.contains("state.isFaceSturdy(level, pos, Direction.UP)"), "a meadow path is laid only on grass, dirt or coarse dirt; other styles pave any solid ground");
        check(warren.indexOf("if (!soil || !GuavaTree.replaceable(level.getBlockState(above))) {") < warren.indexOf("WarrenPalette.Role.PATH)"), "the ground and the space above are checked before the block is laid");
        check(warren.contains("state.getBlock() instanceof LeavesBlock") && warren.contains("public record Built(int trees, int houses, int dogs, int paths, int props)"), "crowns are looked through; the settlement reports its path blocks");
        String command = Files.readString(src.resolve("command/BunnidogsCommands.java"));
        check(command.contains("built.paths()"), "the command reports the path blocks");
    }

    // ---------------------------------------------------------------------------------------------
    static void devOptions() throws Exception {
        System.out.println("dev client options (session 8)");
        Path options = Path.of("dev/options.txt");
        check(Files.exists(options), "dev/options.txt is in the project");
        String text = Files.readString(options);
        check(text.startsWith("version:") && text.contains("key_key.attack:") && text.contains("renderDistance:"), "dev/options.txt is a Minecraft options file");
        String gradle = Files.readString(Path.of("build.gradle"));
        check(gradle.contains("tasks.register('prepareClientOptions')") && gradle.contains("!runOptions.exists()"), "the options are copied only when run/options.txt is missing (changes made in game survive)");
        check(gradle.contains("it.name == 'runClient'") && gradle.contains("dependsOn(prepareClientOptions)"), "runClient runs the copy first");
        String optionsBlock = gradle.substring(gradle.indexOf("def devOptions"), gradle.indexOf("tasks.matching { it.name == 'runClient' }"));
        check(optionsBlock.contains("java.nio.file.Files.copy(devOptions.toPath(), runOptions.toPath())") && !optionsBlock.contains("project."), "the copy captures plain files, not the project (configuration cache is on)");
    }

    // ---------------------------------------------------------------------------------------------
    static void guavaTreeResources() throws Exception {
        System.out.println("guava tree and warren resources and wiring (Phase 4g)");
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));

        // --- textures: three fruit ages that grow, and a sapling; real pixels, cutout
        int[] opaque = new int[3];
        java.util.List<byte[]> pngs = new java.util.ArrayList<>();
        for (String name : new String[] {"guava_fruit_stage0", "guava_fruit_stage1", "guava_fruit_stage2", "guava_sapling"}) {
            byte[] png = Files.readAllBytes(assets.resolve("textures/block/" + name + ".png"));
            pngs.add(png);
            int[] size = pngSize(png);
            check(size[0] == 16 && size[1] == 16, name + ": 16x16, got " + size[0] + "x" + size[1]);
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));
            int count = 0;
            boolean half = false;
            int[] rows = new int[16];
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                int a = img.getRGB(x, y) >>> 24;
                if (a == 255) { count++; rows[y]++; }
                if (a != 0 && a != 255) half = true;
            }
            check(!half && count > 20, name + ": opaque or clear pixels only, and something there (" + count + ")");
            int top = 0;
            while (top < 16 && rows[top] == 0) top++;
            if (name.startsWith("guava_fruit")) {
                opaque[name.charAt(name.length() - 1) - '0'] = count;
                check(top == 0, name + ": the stem starts at the top of the block (it hangs from the leaves above)");
                boolean connected = true;
                int last = 15;
                while (last >= 0 && rows[last] == 0) last--;
                for (int y = top; y <= last; y++) if (rows[y] == 0) connected = false;
                check(connected, name + ": stem and fruit are one piece");
            } else {
                check(rows[15] > 0, name + ": the sapling stands on the ground (bottom row)");
            }
        }
        check(opaque[0] < opaque[1] && opaque[1] < opaque[2], "the fruit gets bigger with every age: " + opaque[0] + " < " + opaque[1] + " < " + opaque[2]);
        for (int i = 0; i < pngs.size(); i++) for (int j = i + 1; j < pngs.size(); j++) {
            check(!java.util.Arrays.equals(pngs.get(i), pngs.get(j)), "pictures " + i + " and " + j + " differ");
        }

        // --- models, blockstates, items, names
        for (int age = 0; age < 3; age++) {
            String model = Files.readString(assets.resolve("models/block/guava_fruit_stage" + age + ".json"));
            check(model.contains("\"minecraft:block/cross\"") && model.contains("\"render_type\": \"cutout\"") && model.contains("\"cross\": \"bunnidogs:block/guava_fruit_stage" + age + "\""),
                    "fruit stage " + age + ": a cutout cross model with its own texture");
        }
        String fruitState = compact(Files.readString(assets.resolve("blockstates/guava_fruit.json")));
        for (int age = 0; age < 3; age++) {
            check(fruitState.contains("\"age=" + age + "\":{\"model\":\"bunnidogs:block/guava_fruit_stage" + age + "\"}"), "fruit age " + age + " shows its own stage");
        }
        check(!fruitState.contains("age=3"), "the fruit has exactly 3 ages");
        String saplingModel = Files.readString(assets.resolve("models/block/guava_sapling.json"));
        check(saplingModel.contains("minecraft:block/cross") && saplingModel.contains("bunnidogs:block/guava_sapling") && saplingModel.contains("cutout"), "the sapling is a cutout cross");
        check(compact(Files.readString(assets.resolve("blockstates/guava_sapling.json"))).contains("\"\":{\"model\":\"bunnidogs:block/guava_sapling\"}"), "the sapling has one state");
        check(compact(Files.readString(assets.resolve("items/guava_sapling.json"))).contains("\"model\":\"bunnidogs:item/guava_sapling\""), "the sapling item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/guava_sapling.json"))).contains("\"layer0\":\"bunnidogs:block/guava_sapling\""), "the sapling item uses the block's picture");
        for (String key : new String[] {"block.bunnidogs.guava_fruit", "block.bunnidogs.guava_sapling", "item.bunnidogs.guava_sapling"}) {
            check(lang.contains("\"" + key + "\""), key + " has a name");
        }

        // --- loot tables and the recipe
        String fruitLoot = compact(Files.readString(data.resolve("loot_table/blocks/guava_fruit.json")));
        check(fruitLoot.contains("\"type\":\"minecraft:block\"") && fruitLoot.contains("\"name\":\"bunnidogs:guava\"") && fruitLoot.contains("\"condition\":\"minecraft:block_state_property\"")
                && fruitLoot.contains("\"block\":\"bunnidogs:guava_fruit\"") && fruitLoot.contains("\"properties\":{\"age\":\"2\"}"), "a ripe fruit (age 2) drops a plain guava, an unripe one nothing");
        String saplingLoot = compact(Files.readString(data.resolve("loot_table/blocks/guava_sapling.json")));
        check(saplingLoot.contains("\"name\":\"bunnidogs:guava_sapling\"") && !saplingLoot.contains("condition"), "a broken sapling drops itself");
        String saplingRecipe = compact(Files.readString(data.resolve("recipe/guava_sapling.json")));
        check(saplingRecipe.contains("crafting_shapeless") && saplingRecipe.contains("\"ingredients\":[\"bunnidogs:guava\"]") && saplingRecipe.contains("\"result\":{\"id\":\"bunnidogs:guava_sapling\"}"), "one plain guava makes one sapling");

        // --- worldgen data: one configured feature, four placed ones, four modifiers, tags that exist and do not overlap
        check(compact(Files.readString(data.resolve("worldgen/configured_feature/warren.json"))).equals("{\"type\":\"bunnidogs:warren\",\"config\":{}}"), "the configured feature is the warren with no config");
        check(compact(Files.readString(data.resolve("worldgen/configured_feature/surface_warren.json"))).equals("{\"type\":\"bunnidogs:surface_warren\",\"config\":{}}"), "the surface configured feature is the surface warren with no config");
        // since Phase 6d the surface and End warrens are the structure (settlement); only the cave and nether warrens stay features
        String[] kinds = {"underground", "nether"};
        String[] tags = {"#bunnidogs:lineage_cave", "#bunnidogs:lineage_nether"};
        String[] steps = {"underground_decoration", "surface_structures"};
        for (int i = 0; i < kinds.length; i++) {
            String placed = compact(Files.readString(data.resolve("worldgen/placed_feature/warren_" + kinds[i] + ".json")));
            boolean onTerrain = kinds[i].equals("surface") || kinds[i].equals("end");
            check(placed.contains("\"feature\":\"bunnidogs:" + (onTerrain ? "surface_warren" : "warren") + "\""), kinds[i] + ": places the warren feature (terrain kinds the surface one, which never digs down into caves)");
            check(placed.contains("\"type\":\"minecraft:rarity_filter\",\"chance\":") && placed.contains("\"minecraft:in_square\""), kinds[i] + ": rare, at a random spot of the chunk");
            check(placed.endsWith("{\"type\":\"minecraft:biome\"}]}"), kinds[i] + ": the last placement is the biome filter (so only biomes that carry it get one)");
            boolean surface = kinds[i].equals("surface") || kinds[i].equals("end");
            check(surface == placed.contains("\"heightmap\":\"WORLD_SURFACE_WG\""), kinds[i] + ": the surface kinds start at the terrain, the others at a random height");
            check(surface != placed.contains("minecraft:height_range"), kinds[i] + ": caves and nether use a height range");
            String modifier = compact(Files.readString(data.resolve("neoforge/biome_modifier/add_warren_" + kinds[i] + ".json")));
            check(modifier.contains("\"type\":\"neoforge:add_features\"") && modifier.contains("\"biomes\":\"" + tags[i] + "\"")
                    && modifier.contains("\"features\":\"bunnidogs:warren_" + kinds[i] + "\"") && modifier.contains("\"step\":\"" + steps[i] + "\""), kinds[i] + ": the biome modifier adds it in the right biomes and step");
            check(Files.exists(data.resolve("tags/worldgen/biome/" + tags[i].substring(tags[i].indexOf(':') + 1) + ".json")), kinds[i] + ": its biome tag exists");
        }
        check(Files.exists(data.resolve("worldgen/structure/warren_village.json")) && Files.exists(data.resolve("worldgen/structure_set/warren_village.json"))
                && Files.exists(data.resolve("worldgen/structure/warren_end_village.json")) && Files.exists(data.resolve("worldgen/structure_set/warren_end_village.json")), "the surface and End settlements are structures (Phase 6d)");
        String surfaceTag = compact(Files.readString(data.resolve("tags/worldgen/biome/warren_surface.json")));
        for (String lineage : new String[] {"meadow", "tundra", "desert", "swamp", "mountain"}) {
            check(surfaceTag.contains("\"#bunnidogs:lineage_" + lineage + "\"") && Files.exists(data.resolve("tags/worldgen/biome/lineage_" + lineage + ".json")), "surface warrens appear in the " + lineage + " lineage");
        }
        for (String lineage : new String[] {"cave", "nether", "end"}) {
            check(!surfaceTag.contains("lineage_" + lineage), "the " + lineage + " lineage has its own warrens, not the surface ones");
        }
        check(!Files.exists(data.resolve("neoforge/biome_modifier/add_wild_bunnidogs.json")) && !Files.exists(data.resolve("tags/worldgen/biome/wild_spawns.json")), "the Phase 3 random spawns are gone");
        try (var stream = Files.list(data.resolve("neoforge/biome_modifier"))) {
            check(stream.allMatch(f -> {
                try { return !Files.readString(f).contains("add_spawns"); } catch (java.io.IOException e) { return false; }
            }), "no biome modifier adds natural spawns any more");
        }

        // --- the code
        String fruit = Files.readString(src.resolve("block/GuavaFruitBlock.java"));
        check(fruit.contains("MAX_AGE = 2") && fruit.contains("BlockStateProperties.AGE_2") && fruit.contains("GROWTH_ODDS = 5"), "the fruit has 3 ages and grows 1 in 5");
        check(fruit.contains("instanceof LeavesBlock"), "the fruit needs leaves above it");
        check(fruit.contains("if (!isRipe(state)) {") && fruit.contains("return InteractionResult.PASS;"), "an unripe fruit ignores the click");
        check(fruit.contains("Block.popResource(level, pos, new ItemStack(ModItems.GUAVA.get()))") && fruit.contains("level.setBlock(pos, state.setValue(AGE, 0), 2)"), "picking a ripe fruit gives a plain guava and sends it back to green");
        check(fruit.indexOf("if (!level.isClientSide())") < fruit.indexOf("Block.popResource"), "picking happens on the server only");
        String sapling = Files.readString(src.resolve("block/GuavaSaplingBlock.java"));
        check(sapling.contains("implements BonemealableBlock") && sapling.contains("BONEMEAL_CHANCE = 0.45F") && sapling.contains("GROWTH_ODDS = 20"), "the sapling: bone meal 45%, 1 in 20 random ticks");
        check(sapling.contains("GuavaTree.fits(level, pos, cells)") && sapling.contains("GuavaTree.place(level, pos, cells)"), "the sapling grows a tree only if the trunk fits");
        check(sapling.contains("soil.is(Blocks.FARMLAND) || soil.is(BlockTags.DIRT)"), "the sapling needs dirt or farmland");
        String tree = Files.readString(src.resolve("worldgen/GuavaTree.java"));
        check(tree.contains("state.getFluidState().isEmpty()") && tree.contains("instanceof GuavaSaplingBlock"), "a tree never replaces a fluid, but replaces its own sapling");
        check(tree.contains("LeavesBlock.PERSISTENT, false") && !tree.contains("PERSISTENT, true"), "generated leaves DECAY (Phase 7a)");
        check(tree.contains("level.getBlockState(pos.above()).getBlock() instanceof LeavesBlock"), "a fruit is only placed under a leaf that was placed");
        check(tree.indexOf("GUAVA_LOG") < tree.indexOf("GUAVA_LEAVES") && tree.indexOf("GUAVA_LEAVES") < tree.indexOf("GUAVA_FRUIT") && !tree.contains("JUNGLE"), "guava logs, then guava leaves, then fruits, no jungle wood left");
        String warren = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        check(warren.contains("extends Feature<NoneFeatureConfiguration>") && warren.contains("return build(context.level(), context.origin(), context.random(), surfaceOnly);") && warren.contains("return buildSettlement(level, origin, random, surfaceOnly, 0).trees() > 0;"), "the warren is a feature that delegates to build() and then buildSettlement()");
        check(warren.contains("GuavaTree.fits(level, floor, warren.tree())") && warren.indexOf("GuavaTree.fits(") < warren.indexOf("GuavaTree.place("), "a cluster gives up before building anything if the trunk has no room");
        check(warren.contains("dog.setPersistenceRequired();") && warren.contains("EntitySpawnReason.STRUCTURE") && warren.contains("level.addFreshEntityWithPassengers(dog)"), "its bunnidogs never despawn");
        check(warren.contains("isFaceSturdy(level, below, Direction.UP)") && warren.contains("hardness > 3.0F") && warren.contains("state.hasBlockEntity()"), "floors must be solid; burrows only dig soft plain blocks");
        check(warren.contains("SEARCH_DEPTH = 40") && warren.contains("SURFACE_SEARCH_DEPTH = 1"), "the cave kind searches 40 blocks down, the surface kind only the start position");
        check(warren.contains("findFloor(level, origin, surfaceOnly ? SURFACE_SEARCH_DEPTH : SEARCH_DEPTH)") && warren.contains("for (int i = 0; i < depth; i++)"), "the floor search is bounded by the kind of warren");
        String features = Files.readString(src.resolve("registry/ModFeatures.java"));
        check(features.contains("FEATURES.register(\"warren\", () -> new WarrenFeature(NoneFeatureConfiguration.CODEC, false))")
                && features.contains("FEATURES.register(\"surface_warren\", () -> new WarrenFeature(NoneFeatureConfiguration.CODEC, true))"), "both warren features are registered");
        check(Files.readString(src.resolve("BunnidogsMod.java")).contains("ModFeatures.FEATURES.register(modEventBus);"), "on the mod bus");
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("registerBlock(\"guava_fruit\", GuavaFruitBlock::new") && blocks.contains("registerBlock(\"guava_sapling\", GuavaSaplingBlock::new"), "both blocks are registered");
        check(!blocks.substring(blocks.indexOf("\"guava_fruit\"")).contains("noLootTable"), "the guava blocks have loot tables");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_SAPLING)") && items.contains("CreativeModeTabs.NATURAL_BLOCKS"), "the sapling has an item and is in the creative tab");
        check(Files.readString(src.resolve("command/BunnidogsCommands.java")).contains("Commands.literal(\"warren\")") && Files.readString(src.resolve("command/BunnidogsCommands.java")).contains("WarrenFeature.buildSettlement(source.getLevel(), player.blockPosition(), source.getLevel().getRandom(), surface, level)"), "/bunnidogs warren [level] builds one at your feet");
        // the block's age count, the models, the blockstate and the loot table agree: AGE_2 means ages 0..2 = 3 models = "age":"2" is ripe
        java.util.regex.Matcher maxAge = java.util.regex.Pattern.compile("MAX_AGE = (\\d+)").matcher(fruit);
        check(maxAge.find(), "the fruit declares MAX_AGE");
        int ages = Integer.parseInt(maxAge.group(1)) + 1;
        long variants = java.util.regex.Pattern.compile("\"age=\\d\"").matcher(fruitState).results().count();
        long modelFiles;
        try (var stream = Files.list(assets.resolve("models/block"))) {
            modelFiles = stream.filter(f -> f.getFileName().toString().startsWith("guava_fruit_stage")).count();
        }
        check(ages == 3 && variants == ages && modelFiles == ages, "ages " + ages + ", blockstate variants " + variants + " and models " + modelFiles + " all agree");
        check(fruitLoot.contains("\"age\":\"" + (ages - 1) + "\""), "the loot table's ripe age is the last age");
    }

    // ---------------------------------------------------------------------------------------------
    static void calipers() throws Exception {
        System.out.println("calipers (Phase 5)");
        int genes = Gene.values().length;
        List<String> adult = Readout.animal(Genome.DEFAULT, Stats.ZERO, false, Optional.empty(), Optional.empty());
        check(adult.get(0).equals("Bunnidog (adult): tan, loyal"), "animal headline is the look in words: " + adult.get(0));
        check(adult.get(adult.size() - 1).equals("  Code: " + Genome.DEFAULT), "the raw code is the LAST line: " + adult.get(adult.size() - 1));
        check(adult.stream().limit(adult.size() - 1).noneMatch(l -> l.contains(Genome.DEFAULT.toString())), "the code appears nowhere else");
        check(adult.size() == genes + 6, "animal readout = headline + one line per gene + stats + next generation + drops + parents line + code, got " + adult.size());
        check(adult.get(genes + 4).equals("  No recorded parents (wild, or summoned)"), "no parents recorded: " + adult.get(genes + 4));
        check(adult.get(1).equals("  Ears: standard") && adult.get(2).equals("  Coat: tan"), "gene rows are words: " + adult.subList(1, 3));
        List<String> withParents = Readout.animal(Genome.DEFAULT, Stats.ZERO, false, Optional.of(Genome.DEFAULT), Optional.of(Genome.DEFAULT));
        check(withParents.size() == genes + 7 && withParents.get(genes + 4).equals("  Born of: mother " + Plain.look(Genome.DEFAULT)) && withParents.get(genes + 5).equals("           father " + Plain.look(Genome.DEFAULT)) && withParents.get(genes + 6).equals("  Code: " + Genome.DEFAULT), "parents are listed in words, the code stays last: " + withParents.subList(genes + 3, withParents.size()));
        check(Readout.animal(Genome.DEFAULT, Stats.ZERO, true, Optional.empty(), Optional.empty()).get(0).startsWith("Bunnidog (pup): "), "a pup is marked");
        check(adult.get(genes + 1).equals("  Stats: Vitality 0 (4.0 hearts), Speed 0 (10.8 blocks/s), Leap 0 (1.25 blocks high), Senses 0 (hears 16 blocks), Bloom 0 (grows up in 20.0 min)"), "stats line in real units: " + adult.get(genes + 1));
        check(Readout.animal(g("22-32-00-10-00-01"), Stats.of(3, 7, 0, 2, 12), false, Optional.empty(), Optional.empty()).get(genes + 1).startsWith("  Stats: Vitality +3 (4.5 hearts), Speed +7 (13.8 blocks/s), Leap 0 (1.25 blocks high), Senses +2 (hears 11 blocks), Bloom +12 (grows up in 12.5 min)"), "stats with points and units");
        Seed plain = new Seed(Genome.DEFAULT, Stats.ZERO, Genome.DEFAULT, Stats.ZERO, 0.0D);
        List<String> seedLines = Readout.seed(plain);
        check(seedLines.get(0).equals("Seed: Quality 0.00"), "seed headline: " + seedLines.get(0));
        check(seedLines.size() == 4 + 2 * (genes + 3), "seed readout = summary + two parents (look, gene rows, stats, code), got " + seedLines.size());
        check(seedLines.get(4).equals("Mother: tan, loyal") && seedLines.get(4 + genes + 2).equals("  Code: " + Genome.DEFAULT), "a parent is described in words, its code last: " + seedLines.get(4));

        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/calipers.png")));
        check(size[0] == 16 && size[1] == 16, "the calipers texture is 16x16");
        check(compact(Files.readString(assets.resolve("items/calipers.json"))).contains("\"model\":\"bunnidogs:item/calipers\""), "client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/calipers.json"))).contains("\"layer0\":\"bunnidogs:item/calipers\""), "model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.calipers\": \"Calipers\""), "lang entry");
        String recipe = compact(Files.readString(data.resolve("recipe/calipers.json")));
        check(recipe.contains("\"I\":\"minecraft:iron_ingot\"") && recipe.contains("\"C\":\"minecraft:copper_ingot\"") && recipe.contains("\"id\":\"bunnidogs:calipers\""), "recipe: iron + copper -> calipers");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("ITEMS.registerItem(\"calipers\", CalipersItem::new") && items.contains("CreativeModeTabs.TOOLS_AND_UTILITIES"), "registered and in the tools tab");
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        int calipersAt = dog.indexOf("held.getItem() instanceof CalipersItem");
        check(calipersAt > 0 && calipersAt < dog.indexOf("super.mobInteract(player, hand)"), "calipers are handled before vanilla's feeding code");
        check(dog.contains("Readout.animal(this.genome(), this.stats(), this.isBaby(), this.motherGenome(), this.fatherGenome(), this.earTag())"), "a bunnidog is read through Readout");
        check(dog.contains("pup.setParents(seed.mother(), seed.father());") && dog.contains("output.putString(TAG_MOTHER, this.motherGenome.toString());") && dog.contains("input.getStringOr(TAG_FATHER, \"\")"), "pups record their parents, saved and loaded");
        String item = Files.readString(src.resolve("item/CalipersItem.java"));
        check(item.contains("SeedItem.read(held)") && item.contains("instanceof SeedCropBlockEntity crop"), "seeds in the other hand and planted crops are read");
        check(item.indexOf("if (!level.isClientSide())") < item.indexOf("showSeed(player, seed.get())"), "messages are sent on the server only");
    }

    // ---------------------------------------------------------------------------------------------
    static void earTag() throws Exception {
        System.out.println("ear tag (Phase 5d)");
        // exact codes (reference values computed independently in Python)
        check(EarTag.code(0L, 0L).equals("BD-000000"), "code(0,0) = " + EarTag.code(0L, 0L));
        check(EarTag.code(1L, 2L).equals("BD-1E9809"), "code(1,2) = " + EarTag.code(1L, 2L));
        check(EarTag.code(0x0123456789ABCDEFL, 0x0FEDCBA987654321L).equals("BD-736C09"), "a typical uuid");
        check(EarTag.code(-1L, -1L).equals("BD-39C64B"), "all bits set (negative longs)");
        // format, determinism and spread over 300 random uuids
        Random rng = new Random(5);
        Set<String> seen = new HashSet<>();
        boolean allValid = true;
        for (int i = 0; i < 300; i++) {
            long most = rng.nextLong(), least = rng.nextLong();
            String code = EarTag.code(most, least);
            allValid &= EarTag.isValid(code) && code.equals(EarTag.code(most, least));
            seen.add(code);
        }
        check(allValid, "every code is BD- + 6 upper-case hex digits and repeatable");
        check(seen.size() == 300, "300 random uuids gave " + seen.size() + " different codes");
        // parsing
        check(EarTag.parse("BD-3F7A2C").equals(Optional.of("BD-3F7A2C")), "a saved code parses");
        check(EarTag.parse("").isEmpty() && EarTag.parse("bd-3f7a2c").isEmpty() && EarTag.parse("BD-3F7A2").isEmpty()
                && EarTag.parse("BD-3F7A2CC").isEmpty() && EarTag.parse("XX-3F7A2C").isEmpty() && EarTag.parse(null).isEmpty(), "malformed codes mean no tag");
        // the Calipers line
        List<String> plain = Readout.animal(Genome.DEFAULT, Stats.ZERO, false, Optional.empty(), Optional.empty());
        List<String> tagged = Readout.animal(Genome.DEFAULT, Stats.ZERO, false, Optional.empty(), Optional.empty(), Optional.of("BD-3F7A2C"));
        check(tagged.size() == plain.size() + 1 && tagged.get(1).equals("  Ear tag: BD-3F7A2C") && tagged.get(0).equals(plain.get(0)), "the tag is the line under the headline: " + tagged.get(1));
        check(Readout.animal(Genome.DEFAULT, Stats.ZERO, false, Optional.empty(), Optional.empty(), Optional.empty()).equals(plain), "no tag, no extra line");

        // resources and glue
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/ear_tag.png")));
        check(size[0] == 16 && size[1] == 16, "the ear tag texture is 16x16");
        check(compact(Files.readString(assets.resolve("items/ear_tag.json"))).contains("\"model\":\"bunnidogs:item/ear_tag\""), "client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/ear_tag.json"))).contains("\"layer0\":\"bunnidogs:item/ear_tag\""), "model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.ear_tag\": \"Ear Tag\""), "lang entry");
        String recipe = compact(Files.readString(data.resolve("recipe/ear_tag.json")));
        check(recipe.contains("\"minecraft:iron_nugget\"") && recipe.contains("\"minecraft:paper\"") && recipe.contains("\"id\":\"bunnidogs:ear_tag\"") && recipe.contains("\"count\":2"), "recipe: iron nugget + paper -> 2 ear tags");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("ITEMS.registerItem(\"ear_tag\", EarTagItem::new") && items.contains("event.accept(EAR_TAG.get())"), "registered and in the tools tab");
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        int tagAt = dog.indexOf("held.getItem() instanceof EarTagItem");
        check(tagAt > 0 && tagAt < dog.indexOf("super.mobInteract(player, hand)"), "the ear tag is handled before vanilla's feeding code");
        check(dog.contains("output.putString(TAG_EAR_TAG, this.earTag);") && dog.contains("EarTag.parse(input.getStringOr(TAG_EAR_TAG, \"\"))"), "the tag is saved and loaded");
        check(dog.contains("if (!this.hasCustomName())") && dog.indexOf("this.setCustomName(Component.literal(this.earTag))") > dog.indexOf("if (!this.hasCustomName())"), "an animal with its own name keeps it");
        String item = Files.readString(src.resolve("item/EarTagItem.java"));
        check(item.contains("if (!player.hasInfiniteMaterials()) {\n            stack.shrink(1);") && item.indexOf("code.isEmpty()") < item.indexOf("stack.shrink(1)"), "a tag is used up only when it was clipped on, and not in creative");
    }

    // ---------------------------------------------------------------------------------------------
    static void pedigreeTags() throws Exception {
        System.out.println("pedigree book with UUIDs and ear tags (Phase 5e)");
        Genome a = g("11-00-00-00-00-00"), b = g("22-00-00-00-00-00"), c = g("33-00-00-00-00-00");
        Optional<Genome> none = Optional.empty();
        UUID u1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), u2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        String t1 = "BD-3F7A2C";
        Pedigree.Entry legacyA = new Pedigree.Entry(a, none, none);
        Pedigree.Entry dog1 = new Pedigree.Entry(a, none, none, Optional.of(u1), Optional.empty());
        Pedigree.Entry dog2 = new Pedigree.Entry(a, none, none, Optional.of(u2), Optional.empty());
        Pedigree.Entry dog1Tagged = new Pedigree.Entry(a, none, none, Optional.of(u1), Optional.of(t1));

        // identical genes, different UUIDs = two animals; one UUID = one animal
        Pedigree book = new Pedigree();
        check(book.add(dog1) == Pedigree.Added.ADDED && book.add(dog2) == Pedigree.Added.ADDED && book.size() == 2, "identical genes, different uuids = two entries");
        check(book.add(dog1) == Pedigree.Added.ALREADY && book.size() == 2, "the same animal twice is one entry");
        // tagging a logged animal updates its entry in place, with no ear tag needed for identity
        check(book.add(dog1Tagged) == Pedigree.Added.UPDATED && book.size() == 2 && book.entries().get(0).tag().equals(Optional.of(t1)), "a tagged animal updates its own entry");
        check(book.add(dog1Tagged) == Pedigree.Added.ALREADY, "and then it is already logged");

        // a 5c entry (genes only) is taken over in place; entries without a uuid still behave as in 5c
        Pedigree old = new Pedigree();
        old.add(legacyA);
        old.add(new Pedigree.Entry(b, none, none));
        check(old.add(legacyA) == Pedigree.Added.ALREADY, "legacy entries are duplicates of each other by genes");
        check(old.add(dog1) == Pedigree.Added.UPDATED && old.size() == 2 && old.entries().get(0).id().equals(Optional.of(u1)), "a genes-only entry gets the uuid in place");
        check(old.add(dog2) == Pedigree.Added.ADDED && old.size() == 3, "a second animal with the same genes is a new entry");
        // a different genome is never taken over
        check(new Pedigree().add(new Pedigree.Entry(c, none, none, Optional.of(u1), Optional.empty())) == Pedigree.Added.ADDED, "a first uuid entry is added");

        // a full book can still update an entry but refuses a new animal
        Pedigree full = new Pedigree();
        full.add(legacyA);
        Set<Genome> distinct = new HashSet<>();
        distinct.add(a);
        Random rng = new Random(9);
        while (full.size() < Pedigree.MAX_ENTRIES) {
            Genome each = Genome.randomFounder(rng);
            if (distinct.add(each)) {
                full.add(new Pedigree.Entry(each, none, none));
            }
        }
        check(full.add(dog1) == Pedigree.Added.UPDATED && full.size() == Pedigree.MAX_ENTRIES, "a full book can still update an entry");
        check(full.add(dog2) == Pedigree.Added.FULL, "but not take a new animal");

        // an invalid tag means no tag
        check(new Pedigree.Entry(a, none, none, Optional.of(u1), Optional.of("nope")).tag().isEmpty(), "a malformed tag is dropped");

        // read-out: entries with no tag are exactly as before, tagged ones show the tag after the number
        Pedigree lines = new Pedigree();
        lines.add(new Pedigree.Entry(b, none, none, Optional.of(u2), Optional.empty()));
        lines.add(new Pedigree.Entry(c, Optional.of(a), Optional.of(b), Optional.of(u1), Optional.of(t1)));
        List<String> out = lines.lines();
        check(out.get(1).equals("#1  generation 0  " + Plain.look(b) + "  [" + b + "]"), "untagged line: " + out.get(1));
        check(out.get(3).equals("#2  " + t1 + "  generation 1  " + Plain.look(c) + "  [" + c + "]"), "tagged line: " + out.get(3));

        // text form
        Pedigree.Entry full5 = new Pedigree.Entry(c, Optional.of(a), Optional.of(b), Optional.of(u1), Optional.of(t1));
        check(full5.toString().equals(c + "#" + u1 + "@" + t1 + "<" + a + "+" + b), "entry text: " + full5);
        check(dog1.toString().equals(a + "#" + u1) && legacyA.toString().equals(a.toString()), "uuid only, and the old form unchanged");
        Pedigree back = Pedigree.parse(lines.toString());
        check(back.entries().equals(lines.entries()) && back.toString().equals(lines.toString()), "uuids and tags survive the text round trip");
        check(Pedigree.parse(a + ";" + b + "<" + a + "+" + c).size() == 2, "a book from before 5e still reads");
        check(Pedigree.parse(a + "#1-2-3-4-5").size() == 0 && Pedigree.parse(a + "#").size() == 0 && Pedigree.parse(a + "#" + u1 + "#" + u2).size() == 0
                && Pedigree.parse(a + "@nope").size() == 0 && Pedigree.parse(a + "@").size() == 0, "a malformed uuid or tag skips the piece");
        check(Pedigree.parse("junk;" + a + "#" + u1).size() == 1, "good pieces survive bad ones");
        // 24 full entries (uuid + tag + both parents) stay far below the 32767-character limit of a string component
        StringBuilder longest = new StringBuilder();
        for (int i = 0; i < Pedigree.MAX_ENTRIES; i++) {
            longest.append(i > 0 ? ";" : "").append(full5);
        }
        check(longest.length() < 4000, "the longest possible book is " + longest.length() + " characters");

        // glue
        String item = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/item/PedigreeBookItem.java"));
        check(item.contains("case UPDATED ->") && item.contains("result == Pedigree.Added.ADDED || result == Pedigree.Added.UPDATED) {\n            book.set("), "the book item saves after an update too");
        String dog = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/entity/Bunnidog.java"));
        check(dog.contains("new Pedigree.Entry(this.genome(), this.motherGenome(), this.fatherGenome(), Optional.of(this.getUUID()), this.earTag(), Optional.of(this.stats()), this.isBaby())"), "a bunnidog is logged with its uuid, tag, stats and pup flag");
    }

    // ---------------------------------------------------------------------------------------------
    static void pedigree() throws Exception {
        System.out.println("pedigree book (Phase 5c)");
        Genome a = g("11-00-00-00-00-00"), b = g("22-00-00-00-00-00"), c = g("33-00-00-00-00-00"), d = g("44-00-00-00-00-00");
        Optional<Genome> none = Optional.empty();
        Pedigree.Entry founderA = new Pedigree.Entry(a, none, none);
        Pedigree.Entry founderB = new Pedigree.Entry(b, none, none);
        Pedigree.Entry child = new Pedigree.Entry(c, Optional.of(a), Optional.of(b));
        Pedigree.Entry grandchild = new Pedigree.Entry(d, Optional.of(c), Optional.of(a));
        Pedigree book = new Pedigree();
        check(book.add(grandchild) == Pedigree.Added.ADDED && book.add(child) == Pedigree.Added.ADDED
                && book.add(founderA) == Pedigree.Added.ADDED && book.add(founderB) == Pedigree.Added.ADDED, "four animals are added, in any order");
        check(book.add(child) == Pedigree.Added.ALREADY && book.size() == 4, "the same animal twice is one entry");
        List<String> lines = book.lines();
        check(lines.get(0).equals("Pedigree Book: 4 of 24 animals logged"), "headline: " + lines.get(0));
        check(lines.get(1).equals("#1  generation 2  " + Plain.look(d) + "  [" + d + "]") && lines.get(2).equals("    mother #2, father #3"), "grandchild: generation 2, parents #2 and #3: " + lines.subList(1, 3));
        check(lines.get(3).equals("#2  generation 1  " + Plain.look(c) + "  [" + c + "]"), "child is generation 1: " + lines.get(3));
        check(lines.get(5).equals("#3  generation 0  " + Plain.look(a) + "  [" + a + "]") && lines.get(6).equals("    no recorded parents"), "founder: generation 0");
        Pedigree one = new Pedigree();
        one.add(child);
        check(one.lines().get(2).equals("    mother " + Plain.look(a) + " (not logged), father " + Plain.look(b) + " (not logged)"), "unlogged parents are shown by their looks: " + one.lines().get(2));
        check(one.lines().get(1).contains("generation 0"), "a child whose parents are not logged is generation 0");
        // an animal whose parents have its own genome (inbred line) must not loop or point at itself
        Pedigree inbred = new Pedigree();
        inbred.add(new Pedigree.Entry(a, Optional.of(a), Optional.of(a)));
        check(inbred.lines().get(1).contains("generation 0") && inbred.lines().get(2).contains("(not logged)"), "self-parent is not a loop: " + inbred.lines());
        Pedigree loop = new Pedigree();
        loop.add(new Pedigree.Entry(a, Optional.of(b), Optional.of(b)));
        loop.add(new Pedigree.Entry(b, Optional.of(a), Optional.of(a)));
        check(loop.lines().size() == 5, "a two-genome loop terminates");
        // the book is full at MAX_ENTRIES
        Pedigree full = new Pedigree();
        Random pick = new Random(7);
        Set<Genome> distinct = new java.util.LinkedHashSet<>();
        while (distinct.size() <= Pedigree.MAX_ENTRIES) {
            distinct.add(new Genome(pick.nextLong()));      // Genome resets invalid allele ids, so build them from random longs
        }
        int n = 0;
        for (Genome each : distinct) {
            Pedigree.Added result = full.add(new Pedigree.Entry(each, none, none));
            check(result == (n < Pedigree.MAX_ENTRIES ? Pedigree.Added.ADDED : Pedigree.Added.FULL), "entry " + n + " gives " + result);
            n++;
        }
        check(full.size() == Pedigree.MAX_ENTRIES, "a full book refuses more");
        // text form
        Pedigree back = Pedigree.parse(book.toString());
        check(back.toString().equals(book.toString()) && back.entries().equals(book.entries()), "text round trip: " + book);
        check(Pedigree.parse(null).size() == 0 && Pedigree.parse("").size() == 0 && Pedigree.parse("junk;<;11<22;" + a + "<" + b + "+;" + a + "<1+2+3").size() == 0, "malformed text gives an empty book");
        check(Pedigree.parse("junk;" + founderA).size() == 1, "good pieces survive bad ones");
        check(new Pedigree.Entry(a, Optional.of(b), none).mother().isEmpty(), "a lone parent is dropped: parents are a pair");

        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/pedigree_book.png")));
        check(size[0] == 16 && size[1] == 16, "the book texture is 16x16");
        check(compact(Files.readString(assets.resolve("items/pedigree_book.json"))).contains("\"model\":\"bunnidogs:item/pedigree_book\""), "client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/pedigree_book.json"))).contains("\"layer0\":\"bunnidogs:item/pedigree_book\""), "model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.pedigree_book\": \"Pedigree Book\""), "lang entry");
        String recipe = compact(Files.readString(data.resolve("recipe/pedigree_book.json")));
        check(recipe.contains("crafting_shapeless") && recipe.contains("\"minecraft:book\"") && recipe.contains("\"bunnidogs:guava\"") && recipe.contains("\"id\":\"bunnidogs:pedigree_book\""), "recipe: book + guava");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("registerComponentType(\"pedigree_data\"") && items.contains("ITEMS.registerItem(\"pedigree_book\", PedigreeBookItem::new") && items.contains("event.accept(PEDIGREE_BOOK.get())"), "component, item and tab entry");
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        int bookAt = dog.indexOf("held.getItem() instanceof PedigreeBookItem");
        check(bookAt > 0 && bookAt < dog.indexOf("super.mobInteract(player, hand)"), "the book is handled before vanilla's feeding code");
        check(dog.contains("PedigreeBookItem.log(player, held, new Pedigree.Entry("), "a bunnidog logs itself into the book");
        String item = Files.readString(src.resolve("item/PedigreeBookItem.java"));
        check(item.contains("book.set(ModItems.PEDIGREE_DATA.get(), log.toString())") && item.indexOf("log.add(entry)") < item.indexOf("book.set("), "the book is rewritten only after a successful add");
    }

    // ---------------------------------------------------------------------------------------------
    // Phase 8 S3: the book's pages (pure layout) and the screen glue
    /** Phase 8 S5: hover regions and code-pair geometry of the sheet (pure layout; the screen only draws them). */
    static void sheetHover() throws Exception {
        System.out.println("book hover boxes (Phase 8 S5)");
        com.bunnidogs.mod.layout.Metrics m = com.bunnidogs.mod.layout.Metrics.ESTIMATE;
        Optional<Genome> none = Optional.empty();
        Genome a = g("22-32-00-10-00-01");
        Stats st = Stats.of(3, -1, 0, 2, 12);
        Pedigree one = new Pedigree();
        one.add(new Pedigree.Entry(a, none, none, Optional.of(UUID.randomUUID()), Optional.empty(), Optional.of(st), false));
        com.bunnidogs.mod.layout.Page.Spread sp = com.bunnidogs.mod.layout.SheetLayout.sheet(one, 0, m);

        // left page: 6 pairs in code order, one gene row each with its pair index, and the whole code = how to read
        check(sp.left().pairs().size() == Gene.COUNT, "one code pair per gene");
        String[] code = a.toString().split("-");
        boolean pairsOk = true;
        for (int i = 0; i < Gene.COUNT; i++) {
            com.bunnidogs.mod.layout.Page.Pair p = sp.left().pairs().get(i);
            pairsOk &= p.w() == m.width(code[i]) && p.y() == 68 && (i == 0 || p.x() > sp.left().pairs().get(i - 1).x());
        }
        check(pairsOk, "pair geometry matches the code texts");
        int rows = 0;
        boolean codeBox = false;
        for (com.bunnidogs.mod.layout.Page.Hotspot h : sp.left().hotspots()) {
            if (h.pair() >= 0) {
                Gene gene = Gene.values()[h.pair()];
                check(h.text().equals(Plain.hover(a, gene)) && h.y() == 79 + 9 * h.pair() && h.w() == 130 && h.h() == 9, "gene row hotspot " + h.pair());
                rows++;
            } else if (h.y() == 68) {
                codeBox = h.text().equals(Plain.HOW_TO_READ);
            }
        }
        check(rows == Gene.COUNT && codeBox, "one row per gene and the code line");
        com.bunnidogs.mod.layout.Page.Hotspot art = sp.left().hotspots().stream().filter(h -> h.y() == 11).findFirst().orElseThrow();
        check(art.h() == 45 && art.pair() == -1 && art.text().equals(Drops.describe(a, st, false)), "the portrait box shows what the animal drops");
        Pedigree oldBook = new Pedigree();
        oldBook.add(new Pedigree.Entry(a, none, none));
        check(com.bunnidogs.mod.layout.SheetLayout.sheet(oldBook, 0, m).left().hotspots().stream().anyMatch(h -> h.y() == 11 && h.text().contains("not recorded")), "an old entry's portrait box says the drops cannot be worked out");

        // right page: one working per stat, stacked without gaps from y = 9; next litter; no parents recorded
        java.util.List<com.bunnidogs.mod.layout.Page.Hotspot> rh = sp.right().hotspots();
        int y = 9;
        int idx = 0;
        for (Stat stat : Stat.values()) {
            com.bunnidogs.mod.layout.Page.Hotspot h = rh.get(idx++);
            check(h.y() == y && h.text().equals(Units.working(stat, a, st)) && h.pair() == -1, "stat hotspot " + stat);
            y += h.h();
        }
        check(rh.size() == 6 && rh.get(5).y() == 108 && rh.get(5).text().equals(com.bunnidogs.mod.genetics.Readout.pushText(a)), "next litter hotspot after the five stats");

        // parents: the whole look in the hover box; an old entry (no stats) has no stat boxes
        Genome mom = g("11-00-00-00-00-00"), dad = g("22-00-00-00-00-00");
        Pedigree fam = new Pedigree();
        fam.add(new Pedigree.Entry(a, Optional.of(mom), Optional.of(dad), Optional.empty(), Optional.empty(), Optional.empty(), false));
        java.util.List<com.bunnidogs.mod.layout.Page.Hotspot> fh = com.bunnidogs.mod.layout.SheetLayout.sheet(fam, 0, m).right().hotspots();
        check(fh.size() == 3 && fh.get(1).text().startsWith("Mom: ") && fh.get(2).text().startsWith("Dad: ") && fh.get(1).y() == 135 && fh.get(2).y() == 144, "no stat boxes without stats; both parents have a box: " + fh);

        // every hotspot of random full books stays inside its page and no two on a page overlap
        Pedigree full = new Pedigree();
        java.util.Random rnd = new java.util.Random(5);
        while (full.size() < Pedigree.MAX_ENTRIES) {
            full.add(new Pedigree.Entry(new Genome(rnd.nextLong()), rnd.nextBoolean() ? none : Optional.of(g("11-00-00-00-00-00")), none, Optional.of(UUID.randomUUID()), Optional.empty(),
                    Optional.of(Stats.of(rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30)), rnd.nextBoolean()));
        }
        boolean inside = true, apart = true;
        for (int n = 0; n < full.size(); n++) {
            com.bunnidogs.mod.layout.Page.Spread s = com.bunnidogs.mod.layout.SheetLayout.sheet(full, n, m);
            for (com.bunnidogs.mod.layout.Page pg : new com.bunnidogs.mod.layout.Page[] { s.left(), s.right() }) {
                java.util.List<com.bunnidogs.mod.layout.Page.Hotspot> hs = pg.hotspots();
                for (int i = 0; i < hs.size(); i++) {
                    com.bunnidogs.mod.layout.Page.Hotspot h = hs.get(i);
                    inside &= h.x() >= 0 && h.y() >= 0 && h.x() + h.w() <= 130 && h.y() + h.h() <= 156 && !h.text().isEmpty();
                    for (int j = i + 1; j < hs.size(); j++) {
                        com.bunnidogs.mod.layout.Page.Hotspot o = hs.get(j);
                        apart &= h.x() + h.w() <= o.x() || o.x() + o.w() <= h.x() || h.y() + h.h() <= o.y() || o.y() + o.h() <= h.y();
                    }
                }
            }
        }
        check(inside && apart, "hotspots stay inside the page and never overlap");

        // Phase 8 S7: links. Every index line of a full book jumps to its sheet; a logged parent's line jumps to the parent's sheet
        com.bunnidogs.mod.layout.Page.Spread toc = com.bunnidogs.mod.layout.BookContents.contents(full, m);
        check(toc.left().links().size() == 14 && toc.right().links().size() == 10, "24 index links: 14 left, 10 right");
        boolean linksOk = true;
        for (int i = 0; i < 24; i++) {
            com.bunnidogs.mod.layout.Page pg = i < 14 ? toc.left() : toc.right();
            com.bunnidogs.mod.layout.Page.Link l = pg.links().get(i < 14 ? i : i - 14);
            int ly = i < 14 ? 27 + 9 * i : 9 * (i - 14);
            linksOk &= l.spread() == i + 1 && l.y() == ly && l.x() == 0 && l.w() == 130 && l.h() == 9;
        }
        check(linksOk, "index link i opens spread i + 1, on the line of its text");
        Pedigree kin = new Pedigree();
        kin.add(new Pedigree.Entry(g("11-00-00-00-00-00"), none, none, Optional.of(UUID.randomUUID()), Optional.empty(), Optional.of(Stats.ZERO), false));
        kin.add(new Pedigree.Entry(a, Optional.of(g("11-00-00-00-00-00")), Optional.of(g("22-00-00-00-00-00")), Optional.of(UUID.randomUUID()), Optional.empty(), Optional.of(st), true));
        java.util.List<com.bunnidogs.mod.layout.Page.Link> pl = com.bunnidogs.mod.layout.SheetLayout.sheet(kin, 1, m).right().links();
        check(pl.size() == 1 && pl.get(0).spread() == 1 && pl.get(0).y() == 135, "Mom is animal 1 (a link to spread 1); Dad is not in the book (no link): " + pl);
        check(com.bunnidogs.mod.layout.SheetLayout.sheet(kin, 0, m).right().links().isEmpty() && com.bunnidogs.mod.layout.SheetLayout.sheet(kin, 0, m).left().links().isEmpty(), "no links on a sheet without logged parents");
    }

    static void calipersWindow() throws Exception {
        System.out.println("Calipers window (Phase 8 S6)");
        com.bunnidogs.mod.layout.Metrics m = com.bunnidogs.mod.layout.Metrics.ESTIMATE;
        Optional<Genome> none = Optional.empty();
        Genome a = g("22-32-00-10-00-01"), mom = g("11-00-00-00-00-00"), dad = g("22-00-00-00-00-00");
        UUID u = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Pedigree.Entry full = new Pedigree.Entry(a, Optional.of(mom), Optional.of(dad), Optional.of(u), Optional.of("BD-3F7A2C"), Optional.of(Stats.of(3, -1, 0, 2, 12)), true);
        Pedigree.Entry plain = new Pedigree.Entry(a, none, none);

        // the wire text is the entry's own text: it reads back whole, and bad text gives nothing (never an error)
        check(Pedigree.parseEntry(full.toString()).equals(Optional.of(full)) && Pedigree.parseEntry(plain.toString()).equals(Optional.of(plain)), "an entry reads back from its text, with and without parents, tag and stats");
        check(Pedigree.parseEntry("").isEmpty() && Pedigree.parseEntry(null).isEmpty() && Pedigree.parseEntry("garbage").isEmpty() && Pedigree.parseEntry(";;").isEmpty(), "empty or bad text gives no entry");
        check(Pedigree.parseEntry(full + ";" + plain).equals(Optional.of(full)), "the first entry of a longer text is the one read");

        // the single sheet: no number, no generation, no parent numbers or links
        java.util.function.Function<com.bunnidogs.mod.layout.Page, java.util.List<String>> dump = pg -> {
            java.util.List<String> out = new java.util.ArrayList<>();
            for (com.bunnidogs.mod.layout.Page.Text t : pg.texts()) {
                out.add(t.y() + "|" + t.text());
            }
            return out;
        };
        com.bunnidogs.mod.layout.Page.Spread sp = com.bunnidogs.mod.layout.SheetLayout.single(full, m);
        java.util.List<String> left = dump.apply(sp.left()), right = dump.apply(sp.right());
        check(left.contains("0|BD-3F7A2C") && left.contains("0|pup") && left.contains("59|GENOME") && left.stream().noneMatch(t -> t.contains("#") || t.contains("generation")), "header = the ear tag and pup, no #n and no generation: " + left);
        check(left.contains("68|22") && left.contains("79|short") && left.contains("88|spotted") && sp.left().portrait() != null && sp.left().portrait().coat().equals("spotted") && sp.left().pairs().size() == Gene.COUNT, "the rest of the left page is the book's sheet (code, gene rows, portrait, pairs)");
        check(right.contains("0|STATS") && right.contains("9|Vitality") && right.contains("9|+3") && right.stream().anyMatch(t -> t.startsWith("135|Mom: ")) && right.stream().anyMatch(t -> t.startsWith("144|Dad: ")), "the right page shows stats and the parents as looks without numbers: " + right);
        check(sp.left().links().isEmpty() && sp.right().links().isEmpty(), "a single sheet has no links");
        check(dump.apply(com.bunnidogs.mod.layout.SheetLayout.single(plain, m).left()).contains("0|Bunnidog") && !dump.apply(com.bunnidogs.mod.layout.SheetLayout.single(plain, m).left()).contains("0|adult"), "no tag: the header says Bunnidog; no stats: no adult/pup");
        check(dump.apply(com.bunnidogs.mod.layout.SheetLayout.single(plain, m).right()).contains("135|No recorded parents"), "no parents recorded: said so");
        // a parent with the same genes as the animal is NOT numbered or linked (in a book it would link to its own sheet)
        Pedigree.Entry inbred = new Pedigree.Entry(a, Optional.of(a), Optional.of(a), Optional.empty(), Optional.empty(), Optional.of(Stats.ZERO), false);
        com.bunnidogs.mod.layout.Page.Spread ib = com.bunnidogs.mod.layout.SheetLayout.single(inbred, m);
        check(ib.right().links().isEmpty() && dump.apply(ib.right()).stream().noneMatch(t -> t.contains("#")), "an animal whose parents look like itself has no self-link");

        // random animals: every hotspot and text stays inside the 130 x 156 page (the same property as the book's sheets)
        java.util.Random rnd = new java.util.Random(6);
        boolean inside = true;
        for (int i = 0; i < 100; i++) {
            Pedigree.Entry e = new Pedigree.Entry(new Genome(rnd.nextLong()), rnd.nextBoolean() ? none : Optional.of(new Genome(rnd.nextLong())), none, Optional.of(UUID.randomUUID()), Optional.empty(),
                    rnd.nextBoolean() ? Optional.empty() : Optional.of(Stats.of(rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30)), rnd.nextBoolean());
            com.bunnidogs.mod.layout.Page.Spread s2 = com.bunnidogs.mod.layout.SheetLayout.single(e, m);
            for (com.bunnidogs.mod.layout.Page pg : new com.bunnidogs.mod.layout.Page[] { s2.left(), s2.right() }) {
                for (com.bunnidogs.mod.layout.Page.Hotspot h : pg.hotspots()) {
                    inside &= h.x() >= 0 && h.y() >= 0 && h.x() + h.w() <= 130 && h.y() + h.h() <= 156;
                }
            }
        }
        check(inside, "single sheets of random animals keep every hotspot inside the page");

        // ---- glue, by grep
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String payload = Files.readString(src.resolve("network/SheetPayload.java"));
        check(payload.contains("public record SheetPayload(String entry) implements CustomPacketPayload") && payload.contains("Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, \"sheet\")")
                && payload.contains("StreamCodec<ByteBuf, SheetPayload> STREAM_CODEC") && payload.contains("ByteBufCodecs.STRING_UTF8.map(SheetPayload::new, SheetPayload::entry)")
                && payload.contains("public CustomPacketPayload.Type<SheetPayload> type()"), "the payload is a record with a Type from an Identifier and a one-string codec, in the shape of NeoForge's own payloads");
        String net = Files.readString(src.resolve("network/ModNetwork.java"));
        int lambdaAt = net.indexOf("(payload, context) ->");
        check(net.contains("public static void register(RegisterPayloadHandlersEvent event)") && net.contains("event.registrar(VERSION)") && net.contains("registrar.playToClient(SheetPayload.TYPE, SheetPayload.STREAM_CODEC,")
                && lambdaAt > 0 && net.indexOf("ClientScreens::openSheet") > lambdaAt && net.contains("Pedigree.parseEntry(payload.entry())"), "the channel registers a client handler that opens the sheet, through a lambda");
        check(net.contains("PacketDistributor.sendToPlayer(player, new SheetPayload(entry.toString()))") && !net.contains("import net.minecraft.client"), "the server sends the entry text to one player; no Minecraft client class is imported by the network classes");
        check(!payload.contains("import net.minecraft.client"), "the payload has no client import either");
        String mod = Files.readString(src.resolve("BunnidogsMod.java"));
        check(mod.contains("modEventBus.addListener(ModNetwork::register);") && mod.contains("import com.bunnidogs.mod.network.ModNetwork;"), "the mod registers the channel on the mod bus");
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        int calipersAt = dog.indexOf("held.getItem() instanceof CalipersItem");
        int sendAt = dog.indexOf("ModNetwork.sendSheet(serverPlayer,");
        check(calipersAt > 0 && sendAt > calipersAt && sendAt < dog.indexOf("super.mobInteract(player, hand)") && dog.contains("player instanceof ServerPlayer serverPlayer") && dog.contains("import net.minecraft.server.level.ServerPlayer;"),
                "the Calipers send the animal to a real player, before vanilla's feeding code");
        check(dog.indexOf("CalipersItem.send(player, Readout.animal(") > sendAt, "a non-player keeps the chat readout as the fallback");
        String doors = Files.readString(src.resolve("client/ClientScreens.java"));
        check(doors.contains("public static void openSheet(Pedigree.Entry animal)") && doors.contains("Minecraft.getInstance().gui.setScreen(new PedigreeScreen(animal))"), "the door opens the single-sheet screen");
        String screen = Files.readString(src.resolve("client/screen/PedigreeScreen.java"));
        check(screen.contains("public PedigreeScreen(Pedigree.Entry animal)") && screen.indexOf("if (oneSheet()) {\n            refresh();") > 0
                && screen.indexOf("if (oneSheet()) {\n            refresh();") < screen.indexOf("Button.builder"), "a single sheet makes no buttons");
        check(screen.contains("SheetLayout.single(this.single,") && screen.contains("!oneSheet() && event.button() == 0"), "a single sheet draws its own layout and ignores link clicks");
        String tip = Files.readString(src.resolve("item/CalipersItem.java"));
        check(tip.contains("Right-click a bunnidog: its sheet opens"), "the Calipers tooltip says the sheet opens");
    }

    static void sheetLayout() throws Exception {
        System.out.println("book pages (Phase 8 S3)");
        com.bunnidogs.mod.layout.Metrics m = com.bunnidogs.mod.layout.Metrics.ESTIMATE;
        UUID u = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Optional<Genome> none = Optional.empty();

        // helpers: texts of a page as "x,y,text"
        java.util.function.Function<com.bunnidogs.mod.layout.Page, java.util.List<String>> dump = pg -> {
            java.util.List<String> out = new java.util.ArrayList<>();
            for (com.bunnidogs.mod.layout.Page.Text t : pg.texts()) {
                out.add(t.y() + "|" + t.text());
            }
            return out;
        };

        // ---- the user's example animal, exact
        Genome a = g("22-32-00-10-00-01");
        Pedigree one = new Pedigree();
        one.add(new Pedigree.Entry(a, none, none, Optional.of(u), Optional.of("BD-3F7A2C"), Optional.of(Stats.of(3, -1, 0, 2, 12)), false));
        com.bunnidogs.mod.layout.Page.Spread sp = com.bunnidogs.mod.layout.SheetLayout.sheet(one, 0, m);
        java.util.List<String> left = dump.apply(sp.left()), right = dump.apply(sp.right());
        check(left.contains("0|#1  BD-3F7A2C") && left.contains("0|adult") && left.contains("59|GENOME") && left.contains("59|generation 0"), "left header: number, ear tag, adult, generation: " + left);
        check(left.contains("68|22") && left.contains("68|32") && left.contains("68|00") && left.contains("68|10") && left.contains("68|01"), "the code, one text per pair");
        check(left.contains("79|Ears") && left.contains("79|short") && left.contains("88|Coat") && left.contains("88|spotted") && left.contains("88|/brown"), "ears and coat rows (the hidden gene after a slash): " + left);
        check(left.contains("106|Build") && left.contains("106|average") && left.contains("106|/stocky") && left.contains("115|Temper.") && left.contains("115|loyal") && left.contains("124|Fertil.") && left.contains("124|/prolific"), "build, temperament, fertility rows");
        check(left.contains("145|/x = carried gene") && sp.left().portrait() != null && sp.left().portrait().coat().equals("spotted"), "legend and a spotted portrait box");
        check(right.contains("0|STATS") && right.contains("9|Vitality") && right.contains("9|+3") && right.contains("36|Speed") && right.contains("36|-1") && right.contains("54|Leap") && right.contains("54|0"), "stat names and points: " + right);
        Stats st = Stats.of(3, -1, 0, 2, 12);
        check(right.contains("9|" + Units.unitText(Stat.VITALITY, Units.valueOf(Stat.VITALITY, a, st))) && right.contains("36|" + Units.unitText(Stat.SWIFTNESS, Units.valueOf(Stat.SWIFTNESS, a, st))), "the real values are Units' values");
        Units.HeartRow row = Units.heartRow(Units.hearts(a, st));
        check(sp.right().hearts().size() == 1 && sp.right().hearts().get(0).full() == row.full() && sp.right().hearts().get(0).half() == row.half() && sp.right().hearts().get(0).y() == 18, "one heart row under Vitality, from heartRow");
        check(right.contains("108|NEXT LITTER") && right.contains("135|No recorded parents") && right.contains("144|(wild or summoned)"), "next litter header and no parents: " + right);
        boolean tones = true;
        for (com.bunnidogs.mod.layout.Page.Text t : sp.right().texts()) {
            if (t.text().equals("+3")) tones &= t.tone() == com.bunnidogs.mod.layout.Page.Tone.GOOD;
            if (t.text().equals("-1")) tones &= t.tone() == com.bunnidogs.mod.layout.Page.Tone.BAD;
            if (t.text().equals("0")) tones &= t.tone() == com.bunnidogs.mod.layout.Page.Tone.MUTED;
        }
        check(tones, "points are green above 0, red below, grey at 0");

        // ---- a pup with logged parents, and an old entry without stats
        Genome mom = g("11-00-00-00-00-00"), dad = g("22-00-00-00-00-00");
        Pedigree fam = new Pedigree();
        fam.add(new Pedigree.Entry(mom, none, none, Optional.of(UUID.randomUUID()), Optional.empty(), Optional.of(Stats.ZERO), false));
        fam.add(new Pedigree.Entry(a, Optional.of(mom), Optional.of(dad), Optional.of(u), Optional.empty(), Optional.of(st), true));
        fam.add(new Pedigree.Entry(dad, none, none));            // an old entry: genes only
        check(fam.parentIndex(1, true) == 0 && fam.parentIndex(1, false) == 2 && fam.parentIndex(0, true) == -1 && fam.generationOf(1) == 1 && fam.generationOf(0) == 0, "Pedigree.parentIndex / generationOf");
        java.util.List<String> kid = dump.apply(com.bunnidogs.mod.layout.SheetLayout.sheet(fam, 1, m).right());
        check(kid.contains("135|" + com.bunnidogs.mod.layout.Page.fit("Mom #1: " + Plain.look(mom), 130, m)) && kid.contains("144|" + com.bunnidogs.mod.layout.Page.fit("Dad #3: " + Plain.look(dad), 130, m)) && kid.stream().anyMatch(t -> t.startsWith("135|Mom #1: ")) && kid.stream().anyMatch(t -> t.startsWith("144|Dad #3: ")), "parents show their book numbers: " + kid);
        check(dump.apply(com.bunnidogs.mod.layout.SheetLayout.sheet(fam, 1, m).left()).contains("0|adult") == false && dump.apply(com.bunnidogs.mod.layout.SheetLayout.sheet(fam, 1, m).left()).contains("0|pup"), "a pup says pup");
        com.bunnidogs.mod.layout.Page.Spread old = com.bunnidogs.mod.layout.SheetLayout.sheet(fam, 2, m);
        check(old.right().hearts().isEmpty() && dump.apply(old.right()).stream().anyMatch(t -> t.contains("Stats not recorded")) && dump.apply(old.left()).stream().noneMatch(t -> t.equals("0|adult") || t.equals("0|pup")), "an old entry says its stats are not recorded and has no age");

        // ---- contents, paging
        Pedigree full = new Pedigree();
        java.util.Random rnd = new java.util.Random(8);
        while (full.size() < Pedigree.MAX_ENTRIES) {
            full.add(new Pedigree.Entry(new Genome(rnd.nextLong()), none, none, Optional.of(UUID.randomUUID()), Optional.empty(),
                    Optional.of(Stats.of(rnd.nextInt(41) - 10, rnd.nextInt(41) - 10, rnd.nextInt(41) - 10, rnd.nextInt(41) - 10, rnd.nextInt(41) - 10)), rnd.nextBoolean()));
        }
        com.bunnidogs.mod.layout.Page.Spread toc = com.bunnidogs.mod.layout.BookContents.contents(full, m);
        check(dump.apply(toc.left()).contains("0|Pedigree Book") && dump.apply(toc.left()).contains("9|24 of 24 animals") && dump.apply(toc.left()).stream().anyMatch(t -> t.startsWith("27|#1  ")) && dump.apply(toc.left()).stream().anyMatch(t -> t.startsWith("144|#14  ")), "contents, left page: title, count, animals 1-14");
        check(dump.apply(toc.right()).stream().anyMatch(t -> t.startsWith("0|#15  ")) && dump.apply(toc.right()).stream().anyMatch(t -> t.startsWith("81|#24  ")) && dump.apply(toc.right()).contains("90|HOW TO READ A CODE"), "contents, right page: animals 15-24 and the guide header");
        StringBuilder guide = new StringBuilder();
        for (com.bunnidogs.mod.layout.Page.Text t : toc.right().texts()) {
            if (t.y() > 90) guide.append(guide.length() == 0 ? "" : " ").append(t.text());
        }
        check(guide.toString().equals(com.bunnidogs.mod.layout.BookContents.GUIDE) && !guide.toString().endsWith(".."), "the whole guide fits under a full index: " + guide);
        check(com.bunnidogs.mod.layout.BookContents.spreadCount(full) == 25 && com.bunnidogs.mod.layout.BookContents.spreadCount(new Pedigree()) == 1, "spread count = 1 + animals");
        check(dump.apply(com.bunnidogs.mod.layout.BookContents.spread(full, -3, m).left()).contains("0|Pedigree Book")
                && dump.apply(com.bunnidogs.mod.layout.BookContents.spread(full, 1, m).left()).get(0).startsWith("0|#1")
                && dump.apply(com.bunnidogs.mod.layout.BookContents.spread(full, 99, m).left()).get(0).startsWith("0|#24"), "paging: 0 = contents, n = animal n, out of range is clamped");

        // ---- every page of many random books stays on its page and nothing overlaps
        boolean inside = true, apart = true;
        int pages = 0;
        java.util.List<Pedigree> books = new java.util.ArrayList<>(java.util.List.of(full, fam, one, new Pedigree()));
        for (int b = 0; b < 8; b++) {
            Pedigree book = new Pedigree();
            while (book.size() < Pedigree.MAX_ENTRIES) {
                int big = rnd.nextInt(4) == 0 ? 1_000_000 : 40;
                Genome gg = new Genome(rnd.nextLong());
                book.add(new Pedigree.Entry(gg, rnd.nextBoolean() ? Optional.of(new Genome(rnd.nextLong())) : none, none, Optional.of(UUID.randomUUID()), rnd.nextBoolean() ? Optional.of(EarTag.code(rnd.nextLong(), rnd.nextLong())) : Optional.empty(),
                        rnd.nextInt(5) == 0 ? Optional.empty() : Optional.of(Stats.of(rnd.nextInt(2 * big + 1) - big, rnd.nextInt(2 * big + 1) - big, rnd.nextInt(2 * big + 1) - big, rnd.nextInt(2 * big + 1) - big, rnd.nextInt(2 * big + 1) - big)), rnd.nextBoolean()));
            }
            books.add(book);
        }
        for (Pedigree book : books) {
            for (int n = 0; n < com.bunnidogs.mod.layout.BookContents.spreadCount(book); n++) {
                com.bunnidogs.mod.layout.Page.Spread s2 = com.bunnidogs.mod.layout.BookContents.spread(book, n, m);
                for (com.bunnidogs.mod.layout.Page pg : new com.bunnidogs.mod.layout.Page[] { s2.left(), s2.right() }) {
                    pages++;
                    java.util.List<int[]> boxes = new java.util.ArrayList<>();     // x0, x1, y
                    for (com.bunnidogs.mod.layout.Page.Text t : pg.texts()) {
                        int w = m.width(t.text());
                        int x0 = t.right() ? t.x() - w : t.x();
                        inside &= x0 >= 0 && x0 + w <= com.bunnidogs.mod.layout.Page.WIDTH && t.y() >= 0 && t.y() + com.bunnidogs.mod.layout.Page.LINE <= com.bunnidogs.mod.layout.Page.HEIGHT;
                        boxes.add(new int[] { x0, x0 + w, t.y() });
                    }
                    for (com.bunnidogs.mod.layout.Page.Hearts h : pg.hearts()) {
                        int icons = h.times() > 0 ? 1 : h.full() + (h.half() ? 1 : 0);
                        inside &= h.x() + icons * 8 <= com.bunnidogs.mod.layout.Page.WIDTH && h.y() + com.bunnidogs.mod.layout.Page.LINE <= com.bunnidogs.mod.layout.Page.HEIGHT && icons >= 1 && icons <= Units.MAX_HEART_ICONS;
                        boxes.add(new int[] { h.x(), h.x() + icons * 8, h.y() });
                    }
                    com.bunnidogs.mod.layout.Page.Portrait po = pg.portrait();
                    if (po != null) {
                        inside &= po.x() >= 0 && po.x() + po.w() <= com.bunnidogs.mod.layout.Page.WIDTH && po.y() + po.h() < 68 && com.bunnidogs.mod.layout.SheetLayout.hasCoatColor(po.coat());
                    }
                    for (int i = 0; i < boxes.size(); i++) {
                        for (int j = i + 1; j < boxes.size(); j++) {
                            int[] p1 = boxes.get(i), p2 = boxes.get(j);
                            if (p1[2] == p2[2] && p1[0] < p2[1] && p2[0] < p1[1]) {
                                apart = false;
                            }
                        }
                    }
                }
            }
        }
        check(inside, "every text, heart row and portrait of " + pages + " pages is inside its page (130 x 156 px, estimated font)");
        check(apart, "nothing on a line overlaps anything else on that line");

        // ---- small pieces
        check(com.bunnidogs.mod.layout.Page.fit("hello world", 200, m).equals("hello world") && m.width(com.bunnidogs.mod.layout.Page.fit("hello wonderful world", 60, m)) <= 60 && com.bunnidogs.mod.layout.Page.fit("hello wonderful world", 60, m).endsWith(".."), "Page.fit");
        boolean wrapOk = true;
        for (String line : com.bunnidogs.mod.layout.Page.wrap("the quick brown fox jumps over the lazy dog again and again", 70, m)) {
            wrapOk &= m.width(line) <= 70;
        }
        check(wrapOk && String.join(" ", com.bunnidogs.mod.layout.Page.wrap("a b  c", 70, m)).equals("a b c"), "Page.wrap");
        boolean coats = true;
        for (Allele c : Gene.COAT.alleles()) {
            coats &= com.bunnidogs.mod.layout.SheetLayout.hasCoatColor(Plain.word(c));
        }
        check(coats, "every coat has a portrait colour");
        boolean heart = com.bunnidogs.mod.layout.SheetLayout.HEART.length == 6;
        for (String r : com.bunnidogs.mod.layout.SheetLayout.HEART) {
            heart &= r.length() == 7;
        }
        check(heart, "the heart bitmap is 7 x 6");
        for (java.nio.file.Path f : (Iterable<java.nio.file.Path>) Files.list(Path.of("src/main/java/com/bunnidogs/mod/layout"))::iterator) {
            if (f.toString().endsWith("Page.java") || f.toString().endsWith("Metrics.java") || f.toString().endsWith("SheetLayout.java") || f.toString().endsWith("BookContents.java")) {
                check(!Files.readString(f).contains("import net.minecraft") && !Files.readString(f).contains("import net.neoforged"), f.getFileName() + " imports no Minecraft class");
            }
        }

        // ---- glue, by grep
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String screen = Files.readString(src.resolve("client/screen/PedigreeScreen.java"));
        check(screen.contains("extends Screen") && screen.contains("public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)") && screen.contains("super.extractRenderState(graphics, mouseX, mouseY, partialTick);") && screen.contains("Button.builder(") && screen.contains(".bounds(") && screen.contains("this.previous.active =") && screen.contains("BookContents.spread(this.book, this.spread, text -> this.font.width(text))"), "the screen: names verified in NeoForge's tests");
        check(!screen.contains("blit(") && !screen.contains("RenderPipelines") && !screen.contains("Identifier"), "the screen uses no texture and no sprite name");
        String doors = Files.readString(src.resolve("client/ClientScreens.java"));
        check(doors.contains("Minecraft.getInstance().gui.setScreen(new PedigreeScreen(book))"), "the door opens the screen the way NeoForge's tests do");
        String bookItem = Files.readString(src.resolve("item/PedigreeBookItem.java"));
        int clientAt = bookItem.indexOf("else if (level.isClientSide())");
        check(clientAt > 0 && bookItem.indexOf("ClientScreens.openBook(log)") > clientAt && !bookItem.contains("CalipersItem.send(player, log.lines())"), "the book opens its window on the client only; the chat list is gone");
        check(bookItem.contains("if (!level.isClientSide()) {\n                player.sendSystemMessage(Component.literal(\"The book is empty."), "an empty book still answers in the chat, from the server");
    }

    // ---------------------------------------------------------------------------------------------
    static void pedestal() throws Exception {
        System.out.println("trophy pedestal (drops D8a)");
        Path res = Path.of("src/main/resources");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String model = Files.readString(res.resolve("assets/bunnidogs/models/block/trophy_pedestal.json"));
        for (String tex : new String[] {"trophy_pedestal", "trophy_pedestal_plate"}) {
            check(model.contains("\"bunnidogs:block/" + tex + "\""), "the model uses the texture " + tex);
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(res.resolve("assets/bunnidogs/textures/block/" + tex + ".png").toFile());
            check(img != null && img.getWidth() == 16 && img.getHeight() == 16, tex + ".png is 16x16");
        }
        check(model.contains("\"minecraft:block/block\"") && model.split("\"from\"", -1).length - 1 == 3, "the model is a parent block/block with 3 elements (base, column, plate)");
        check(Files.readString(res.resolve("assets/bunnidogs/blockstates/trophy_pedestal.json")).contains("bunnidogs:block/trophy_pedestal"), "blockstate points at the model");
        check(Files.readString(res.resolve("assets/bunnidogs/items/trophy_pedestal.json")).contains("bunnidogs:block/trophy_pedestal"), "item definition points at the block model");
        check(Files.readString(res.resolve("data/bunnidogs/loot_table/blocks/trophy_pedestal.json")).contains("\"bunnidogs:trophy_pedestal\""), "the loot table drops the pedestal itself");
        String recipe = Files.readString(res.resolve("data/bunnidogs/recipe/trophy_pedestal.json"));
        check(recipe.contains("minecraft:crafting_shaped") && recipe.contains("\"bunnidogs:trophy_pedestal\""), "the recipe is shaped and makes the pedestal");
        check(Files.readString(res.resolve("data/minecraft/tags/block/mineable/pickaxe.json")).contains("bunnidogs:trophy_pedestal"), "the pedestal is pickaxe-mineable");
        String lang = Files.readString(res.resolve("assets/bunnidogs/lang/en_us.json"));
        check(lang.contains("\"block.bunnidogs.trophy_pedestal\"") && lang.contains("\"item.bunnidogs.trophy_pedestal\""), "block and item names");
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("BLOCKS.registerBlock(\"trophy_pedestal\", TrophyPedestalBlock::new,") && blocks.contains(".noOcclusion()"), "the block is registered, noOcclusion");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        int tools = items.indexOf("CreativeModeTabs.TOOLS_AND_UTILITIES");
        check(items.contains("ITEMS.registerSimpleBlockItem(ModBlocks.TROPHY_PEDESTAL)") && tools > 0 && items.indexOf("event.accept(TROPHY_PEDESTAL.get())") > tools, "the block item exists and sits in the Tools tab");
        String block = Files.readString(src.resolve("block/TrophyPedestalBlock.java"));
        check(block.contains("extends Block") && block.contains("getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)") && block.contains("Shapes.box("), "the block class is plain, with a box outline");
        // D8b: the block entity that holds one item
        check(block.contains("extends Block implements EntityBlock") && block.contains("newBlockEntity(BlockPos pos, BlockState state)") && block.contains("new TrophyPedestalBlockEntity(pos, state)"), "the block is an EntityBlock that makes the pedestal's block entity");
        check(block.contains("protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)"), "the click is useWithoutItem with the signature that compiled for the guava");
        int put = block.indexOf("pedestal.setItem(inHand.copyWithCount(1))");
        int shrink = block.indexOf("inHand.shrink(1)");
        int take = block.indexOf("pedestal.takeItem()");
        check(put > 0 && shrink > put && take > shrink && block.contains("player.hasInfiniteMaterials()"), "put ONE on (copy first, then shrink unless creative), then the take-back branch");
        check(!block.contains("sendSystemMessage") && !block.contains("Component"), "D8c: the temporary chat lines of D8b are gone");
        String be = Files.readString(src.resolve("block/TrophyPedestalBlockEntity.java"));
        check(be.contains("extends BlockEntity") && be.contains("super(ModBlocks.TROPHY_PEDESTAL_BE.get(), pos, state)"), "the block entity uses its registered type");
        check(be.contains("output.store(TAG_ITEM, ItemStack.CODEC, this.held)") && be.contains("input.read(TAG_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY)") && be.indexOf("if (!this.held.isEmpty())") > 0 && be.indexOf("if (!this.held.isEmpty())") < be.indexOf("output.store(TAG_ITEM"), "the item is saved with the codec (never an empty stack) and read back");
        check(be.contains("public void preRemoveSideEffects(BlockPos pos, BlockState state)") && be.contains("Block.popResource(serverLevel, pos, this.held)"), "the item pops out when the pedestal goes");
        check(be.contains("this.setChanged()"), "changes mark the chunk dirty");
        // D8c: the picture (client sync + renderer)
        check(be.contains("public CompoundTag getUpdateTag(HolderLookup.Provider registries)") && be.contains("return this.saveWithoutMetadata(registries)") && be.contains("ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag)") && be.contains("public void onDataPacket(Connection net, ValueInput input)") && be.contains("this.handleUpdateTag(input)"), "the block entity sends its item to the client the way NeoForge's test block entity does");
        check(be.contains("sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL)") && be.indexOf("private void changed()") > 0 && be.indexOf("this.changed();") > 0 && be.split("this\\.changed\\(\\);", -1).length - 1 == 2, "both setItem and takeItem tell the clients (and the chunk)");
        String rstate = Files.readString(src.resolve("client/renderer/TrophyPedestalRenderState.java"));
        String rend = Files.readString(src.resolve("client/renderer/TrophyPedestalRenderer.java"));
        check(rstate.contains("extends BlockEntityRenderState") && rstate.contains("public ItemStackRenderState item;"), "the render state holds the item's render state");
        check(rend.contains("implements BlockEntityRenderer<TrophyPedestalBlockEntity, TrophyPedestalRenderState>") && rend.contains("context.itemModelResolver()") && rend.contains("public void submit(TrophyPedestalRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)") && rend.contains("public TrophyPedestalRenderState createRenderState()"), "the renderer has the three methods of the test renderer");
        check(rend.contains("BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay)") && rend.contains("updateForTopItem(stackState, stack, ItemDisplayContext.GROUND, null, null, 0)") && rend.contains("renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0)"), "the item is resolved and submitted the way the test does");
        check(rend.indexOf("pushPose()") > 0 && rend.indexOf("pushPose()") < rend.indexOf("Axis.YP.rotationDegrees(") && rend.indexOf("Axis.YP.rotationDegrees(") < rend.indexOf("renderState.item.submit(") && rend.indexOf("renderState.item.submit(") < rend.indexOf("popPose()") && rend.contains("if (renderState.item == null)"), "push, move, spin, draw, pop; nothing drawn when empty");
        String client = Files.readString(src.resolve("client/BunnidogsClient.java"));
        check(client.contains("event.registerBlockEntityRenderer(ModBlocks.TROPHY_PEDESTAL_BE.get(), TrophyPedestalRenderer::new)"), "the renderer is registered for the pedestal's block entity type");
        check(blocks.contains("TROPHY_PEDESTAL_BE =") && blocks.contains("BLOCK_ENTITIES.register(\"trophy_pedestal\", () -> new BlockEntityType<>(TrophyPedestalBlockEntity::new, TROPHY_PEDESTAL.get()))"), "the block entity type is registered for the pedestal");
    }

    // ---------------------------------------------------------------------------------------------
    static void texturesExist() throws Exception {
        System.out.println("coat textures");
        Path dir = Path.of("src/main/resources/assets/bunnidogs/textures/entity");
        for (Allele a : Gene.COAT.alleles()) {
            Path p = dir.resolve("bunnidog_" + a.name() + ".png");
            check(Files.isRegularFile(p), "missing texture for coat '" + a.name() + "': " + p + " (run art/gen_bunnidog.py)");
        }
    }
    // ---------------------------------------------------------------------------------------------
    static Genome homozygous(Genome base, Gene gene, String allele) {
        int id = gene.alleleNamed(allele).id();
        return base.with(gene, 0, id).with(gene, 1, id);
    }

    /** Drops, chunk D1 (docs/DROPS_SPECS.md): the engine's exact numbers, the glue by grep, the resources by file reading. */
    static void drops() throws Exception {
        System.out.println("drops (D1)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;

        // the yield table of spec 2.1, every cell (the spec prints two decimals; caps applied)
        int[] points = {-10, 0, 10, 20, 40, 60, 100, 300, 1000};
        double[][] table = {
            /* flesh   */ {0.50, 1.00, 2.00, 3.00, 5.24, 6.16, 7.47, 11.37, 16},
            /* haunch  */ {0, 0, 0, 0, 0.40, 1.07, 1.56, 2.82, 4},
            /* foot    */ {0, 0.10, 0.60, 1.10, 2.44, 3.00, 3.78, 6.12, 8},
            /* sinew   */ {0, 0, 0.80, 1.60, 2.94, 3.50, 4.28, 6.62, 8},
            /* whisker */ {0, 0, 0.80, 1.60, 2.94, 3.50, 4.28, 6.62, 8},
            /* bone    */ {0, 0.50, 1.10, 1.70, 3.04, 3.60, 4.38, 6.72, 11.09},
        };
        check(Drops.ALL_LINES.size() == table.length, "six lines in the spec's table");
        for (int i = 0; i < table.length; i++) {
            Drops.Line line = Drops.ALL_LINES.get(i);
            for (int j = 0; j < points.length; j++) {
                near(Math.min(line.cap(), Drops.yieldOf(line, points[j])), table[i][j], 0.0051, line.key() + " yield at " + points[j] + " points");
            }
        }
        near(Drops.yieldOf(Drops.FLESH, -100000), 0.5, 1e-12, "a line bred against its stat stops at the floor (flesh)");
        near(Drops.yieldOf(Drops.FOOT, -100000), 0.0, 1e-12, "a line bred against its stat stops at the floor (foot: nothing)");
        near(Drops.yieldOf(Drops.HAUNCH, 20), 0.0, 1e-12, "the haunch gives nothing up to its onset");
        check(Drops.yieldOf(Drops.HAUNCH, 21) > 0.0, "the haunch starts one point after its onset");
        check(Drops.LINES.equals(List.of(Drops.FLESH, Drops.HAUNCH, Drops.FOOT, Drops.SINEW, Drops.WHISKER, Drops.BONE_MEAL)) && Drops.LINES.equals(Drops.ALL_LINES), "D1 + D4 + D5: flesh, prime haunch, rabbit's foot, sinew, whisker, bone meal (every line of the spec)");
        for (Drops.Line line : Drops.ALL_LINES) {
            check(line.sources().contains(Drops.Source.DEATH), line.key() + " applies to a death");
        }

        // the five worked examples of spec 2.1 (expected counts E), made from real genes
        Genome racer = homozygous(homozygous(Genome.DEFAULT, Gene.LEGS, "fleet"), Gene.BUILD, "lean");
        Stats racerStats = Stats.of(-5, 30, 0, 0, 0);
        near(Drops.expectedCount(Drops.FOOT, racer, racerStats, adult), 2.48, 0.005, "desert racer: feet");
        near(Drops.expectedCount(Drops.FLESH, racer, racerStats, adult), 0.275, 0.001, "desert racer: flesh");
        Genome chonker = homozygous(Genome.DEFAULT, Gene.BUILD, "chonker");
        Stats chonkerStats = Stats.of(40, 0, 0, 0, 0);
        near(Drops.expectedCount(Drops.FLESH, chonker, chonkerStats, adult), 9.95, 0.005, "chonker: flesh");
        near(Drops.expectedCount(Drops.HAUNCH, chonker, chonkerStats, adult), 0.76, 0.005, "chonker: haunch");
        near(Drops.expectedCount(Drops.SINEW, homozygous(Genome.DEFAULT, Gene.LEGS, "hoofed"), Stats.of(0, 0, 20, 0, 0), adult), 2.18, 0.005, "hoofed: sinew");
        near(Drops.expectedCount(Drops.WHISKER, homozygous(Genome.DEFAULT, Gene.EARS, "bat"), Stats.of(0, 0, 0, 30, 0), adult), 3.70, 0.005, "bat-eared: whisker");
        near(Drops.expectedCount(Drops.BONE_MEAL, homozygous(Genome.DEFAULT, Gene.FERTILITY, "starborn"), Stats.of(0, 0, 0, 0, 120), adult), 3.29, 0.005, "starborn: bone meal");
        near(Drops.expectedCount(Drops.FOOT, Genome.DEFAULT, Stats.ZERO, adult), 0.10, 1e-9, "plain animal: a 10% rabbit's foot");
        near(Drops.expectedCount(Drops.FLESH, Genome.DEFAULT, Stats.ZERO, adult), 1.0, 1e-9, "plain animal: 1 flesh");
        near(Drops.expectedCount(Drops.FOOT, racer, racerStats, new Drops.Context(Drops.Source.DEATH, false, true, false, 3, false)), 3.59, 0.01, "Looting III multiplies E by 1.45");
        near(Drops.expectedCount(Drops.SINEW, Genome.DEFAULT, Stats.ZERO, new Drops.Context(Drops.Source.DEATH, false, true, false, 3, false)), 0.0, 1e-12, "a line with E = 0 stays 0 under Looting III");

        // pups: x0.25 on every stat line
        near(Drops.expectedCount(Drops.FLESH, chonker, chonkerStats, adult.withBaby(true)), 9.95 * 0.25, 0.005, "a pup gives a quarter");

        // stochastic rounding and caps
        Random rng = new Random(13);
        int low = 0, high = 0;
        double sum = 0;
        for (int i = 0; i < 2000; i++) {
            int n = Drops.count(2.48, 8, rng);
            check(n == 2 || n == 3, "2.48 rounds to 2 or 3, got " + n);
            sum += n;
            if (n == 2) low++; else high++;
        }
        near(sum / 2000, 2.48, 0.08, "stochastic rounding is unbiased over 2000 rolls (low " + low + ", high " + high + ")");
        near(Drops.meanCount(2.48, 8), 2.48, 1e-12, "exact mean of a count below the cap");
        near(Drops.meanCount(9.95, 8), 8.0, 1e-12, "exact mean never exceeds the cap");
        check(Drops.count(1.0e9, 16, rng) == 16, "a huge E is held by the cap");
        near(Drops.chanceOfAny(0.275, 16), 0.275, 1e-12, "chance of at least one below 1");
        near(Drops.chanceOfAny(2.48, 16), 1.0, 1e-12, "chance of at least one from 1 up");

        // the roll: only the three active lines, burning cooks the flesh, a pup is a quarter, a player kill is not needed for layer 1
        for (int i = 0; i < 200; i++) {
            for (Drops.Drop d : Drops.roll(racer, racerStats, adult, rng)) {
                check(d.count() > 0 && (d.itemId().equals("bunnidogs:raw_bunnidog_flesh") || d.itemId().equals("minecraft:rabbit_foot") || d.itemId().equals("minecraft:bone_meal") || d.itemId().equals("bunnidogs:tan_pelt") || d.itemId().equals("minecraft:sugar") || d.itemId().equals("minecraft:bone") || d.itemId().equals("bunnidogs:tissue_sample")), "only the pelt, the three D1 lines and the racer's signatures (fleet: sugar, lean: bone) drop, got " + d);
            }
        }
        boolean cooked = false, rawSeen = false;
        Drops.Context burning = new Drops.Context(Drops.Source.DEATH, false, true, true, 0, false);
        for (int i = 0; i < 50; i++) {
            for (Drops.Drop d : Drops.roll(chonker, chonkerStats, burning, rng)) {
                cooked |= d.itemId().equals("bunnidogs:cooked_bunnidog_flesh");
                rawSeen |= d.itemId().equals("bunnidogs:raw_bunnidog_flesh");
            }
        }
        check(cooked && !rawSeen, "a burning death drops COOKED flesh and no raw flesh");
        check(Drops.roll(racer, racerStats, burning, new Random(5)).stream().anyMatch(d -> d.itemId().equals("minecraft:rabbit_foot") && d.count() >= 2), "the racer's rabbit's feet do not change when burning");
        Drops.Context noPlayer = new Drops.Context(Drops.Source.DEATH, false, false, false, 0, false);
        check(Drops.roll(chonker, chonkerStats, adult, new Random(99)).equals(Drops.roll(chonker, chonkerStats, noPlayer, new Random(99))), "stat lines do not need a player kill (layers 2-5 will)");

        // the readouts (one table, so the tools show what a kill rolls)
        List<Drops.Expectation> plainExpected = Drops.expected(Genome.DEFAULT, Stats.ZERO, adult);
        check(plainExpected.size() == 5 && plainExpected.get(0).itemId().equals("bunnidogs:tan_pelt") && plainExpected.get(2).itemId().equals("minecraft:rabbit_foot"), "the pelt, then the three lines, in table order");
        near(plainExpected.get(2).mean(), 0.10, 1e-9, "plain animal: foot mean");
        near(plainExpected.get(2).chanceAtLeastOne(), 0.10, 1e-9, "plain animal: foot chance of one");
        check(Drops.describe(Genome.DEFAULT, Stats.ZERO, false).equals("Drops (adult kill, no Looting): pelt: tan, flesh 1.0, rabbit's foot 0.10 (10% for one), bone meal 0.50 (50% for one), tissue sample 4%"),
                "Calipers drops line: " + Drops.describe(Genome.DEFAULT, Stats.ZERO, false));
        check(Drops.describe(Genome.DEFAULT, Stats.ZERO, true).startsWith("Drops (pup kill, no Looting): flesh 0.25 (25% for one)"), "a pup shows its own quarter: " + Drops.describe(Genome.DEFAULT, Stats.ZERO, true));
        check(Readout.animal(Genome.DEFAULT, Stats.ZERO, false, Optional.empty(), Optional.empty()).contains("  " + Drops.describe(Genome.DEFAULT, Stats.ZERO, false)), "the Calipers read-out has the drops line");
        var sim = Drops.simulate(racer, racerStats, adult, 1000, new Random(7));
        near(sim.get("minecraft:rabbit_foot"), 2.48, 0.12, "simulate: average feet of 1000 kills");
        near(sim.get("bunnidogs:raw_bunnidog_flesh"), 0.275, 0.06, "simulate: average flesh of 1000 kills");
        check(sim.keySet().equals(Set.of("bunnidogs:tan_pelt", "bunnidogs:raw_bunnidog_flesh", "minecraft:rabbit_foot", "minecraft:bone_meal", "minecraft:sugar", "minecraft:bone", "bunnidogs:tissue_sample")), "simulate lists every active item (the racer's fleet legs bring sugar, its lean build bone)");
        near(sim.get("bunnidogs:tan_pelt"), 1.0, 1e-12, "simulate: exactly one pelt per adult kill");

        // resources: every modded item of the active lines has its texture, models, name; the cooking recipes
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        for (Drops.Line line : Drops.LINES) {
            for (String id : new String[] {line.itemId(), line.cookedId()}) {
                if (id == null || !id.startsWith("bunnidogs:")) {
                    continue;                                          // vanilla items need no files of ours
                }
                String name = id.substring("bunnidogs:".length());
                int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/" + name + ".png")));
                check(size[0] == 16 && size[1] == 16, name + " texture is 16x16");
                check(compact(Files.readString(assets.resolve("items/" + name + ".json"))).contains("\"model\":\"bunnidogs:item/" + name + "\""), name + " client item points at its model");
                check(compact(Files.readString(assets.resolve("models/item/" + name + ".json"))).contains("\"layer0\":\"bunnidogs:item/" + name + "\""), name + " model points at its texture");
                check(lang.contains("\"item.bunnidogs." + name + "\""), name + " has a name");
            }
        }
        for (String[] r : new String[][] {{"smelting", "200"}, {"smoking", "100"}, {"campfire_cooking", "600"}}) {
            String recipe = compact(Files.readString(data.resolve("recipe/cooked_bunnidog_flesh_from_" + r[0] + ".json")));
            check(recipe.contains("\"type\":\"minecraft:" + r[0] + "\"") && recipe.contains("\"ingredient\":\"bunnidogs:raw_bunnidog_flesh\"")
                    && recipe.contains("\"result\":{\"id\":\"bunnidogs:cooked_bunnidog_flesh\"}") && recipe.contains("\"cookingtime\":" + r[1])
                    && recipe.contains("\"experience\":0.35"), "cooking recipe " + r[0] + ": raw -> cooked, " + r[1] + " ticks");
        }

        // the glue, by its key lines and their order
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("ITEMS.registerItem(\"raw_bunnidog_flesh\", Item::new, props -> props.food(RAW_FLESH_FOOD))")
                && items.contains("ITEMS.registerItem(\"cooked_bunnidog_flesh\", Item::new, props -> props.food(COOKED_FLESH_FOOD))")
                && items.contains("event.accept(RAW_BUNNIDOG_FLESH.get())") && items.contains("event.accept(COOKED_BUNNIDOG_FLESH.get())"), "both meats are registered as food and in the food tab");
        check(items.contains("new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F)") && items.contains("new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F)"), "nutrition 3 / 0.3 raw, 6 / 0.6 cooked");
        String handler = Files.readString(src.resolve("drop/BunnidogDrops.java"));
        int rule = handler.indexOf("GameRules.MOB_DROPS"), roll = handler.indexOf("Drops.roll(");
        check(handler.contains("@SubscribeEvent") && handler.contains("LivingDropsEvent") && handler.contains("instanceof Bunnidog dog") && handler.contains("instanceof ServerLevel level"), "the handler listens for a bunnidog's drops on the server");
        check(rule > 0 && roll > rule, "the mobDrops game rule is checked before anything is rolled");
        check(handler.contains("event.isRecentlyHit()") && handler.contains("dog.isOnFire()") && handler.contains("dog.isBaby()") && handler.contains("Enchantments.LOOTING")
                && handler.contains("source.getEntity() instanceof LivingEntity killer"), "the context is read from the event, the animal and the killer's Looting");
        check(handler.contains("event.getDrops().add(") && !handler.contains("setCanceled"), "drops are added to the event's collection and nothing is cancelled");
        String commands = Files.readString(src.resolve("command/BunnidogsCommands.java"));
        check(commands.contains("Commands.literal(\"drops\")") && commands.contains("Commands.literal(\"simulate\")") && commands.contains("IntegerArgumentType.integer(1, MAX_SIMULATED_KILLS)")
                && commands.contains("MAX_SIMULATED_KILLS = 1000"), "/bunnidogs drops [simulate 1..1000]");
        check(Files.readString(src.resolve("genetics/Readout.java")).contains("lines.add(\"  \" + Drops.describe(genome, stats, baby));"), "Readout.animal adds the drops line");
    }
    // ---------------------------------------------------------------------------------------------
    /** Drops, chunk D2 (docs/DROPS_SPECS.md 2.2 and 3): the hide layer, and one pelt per coat from the coat table. */
    static void pelts() throws Exception {
        System.out.println("pelts (D2)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Drops.Context pup = adult.withBaby(true);
        Random rng = new Random(21);

        // which pelt: the EXPRESSED coat
        check(Drops.peltId(Genome.DEFAULT).equals("bunnidogs:tan_pelt"), "the default animal is tan");
        int glowId = Gene.COAT.alleleNamed("glow").id();
        Genome carrier = Genome.DEFAULT.with(Gene.COAT, 1, glowId);
        check(Drops.peltId(carrier).equals("bunnidogs:tan_pelt"), "a carrier of glow shows tan, so the pelt is tan");
        check(Drops.peltId(homozygous(Genome.DEFAULT, Gene.COAT, "glow")).equals("bunnidogs:glow_pelt"), "only a homozygous glow animal drops a glow pelt");
        for (Allele coat : Gene.COAT.alleles()) {
            Genome shown = homozygous(Genome.DEFAULT, Gene.COAT, coat.name());
            check(Drops.peltId(shown).equals("bunnidogs:" + coat.name() + "_pelt"), coat.name() + " pelt id");
        }

        // how many: one for an adult on any death, Looting adds a 15% chance of one more per level, none for a pup
        for (int i = 0; i < 200; i++) {
            List<Drops.Drop> drops = Drops.roll(Genome.DEFAULT, Stats.ZERO, adult, rng);
            check(drops.get(0).itemId().equals("bunnidogs:tan_pelt") && drops.get(0).count() == 1, "an adult drops exactly one pelt, first in the list: " + drops.get(0));
        }
        near(Drops.hideMean(0), 1.0, 1e-12, "no Looting: one pelt");
        near(Drops.hideMean(3), 1.45, 1e-12, "Looting III: 1.45 pelts on average");
        near(Drops.hideMean(7), 2.0, 1e-12, "seven levels make the second pelt certain");
        near(Drops.hideMean(50), 2.0, 1e-12, "never more than two pelts");
        double sum = 0;
        for (int i = 0; i < 2000; i++) {
            int n = Drops.hideCount(3, rng);
            check(n == 1 || n == 2, "Looting III gives one or two pelts, got " + n);
            sum += n;
        }
        near(sum / 2000, 1.45, 0.06, "Looting III averages 1.45 pelts over 2000 rolls");
        check(Drops.hideCount(0, rng) == 1 && Drops.hideCount(-4, rng) == 1, "no Looting (or a negative level) never gives a second pelt");
        check(Drops.roll(Genome.DEFAULT, Stats.ZERO, pup, rng).stream().noneMatch(d -> d.itemId().endsWith("_pelt")), "a pup drops no pelt");
        check(!Drops.givesHide(pup) && Drops.givesHide(adult), "only an adult gives the hide");
        Drops.Context noPlayer = new Drops.Context(Drops.Source.DEATH, false, false, false, 0, false);
        Drops.Context burning = new Drops.Context(Drops.Source.DEATH, false, true, true, 0, false);
        check(Drops.roll(Genome.DEFAULT, Stats.ZERO, noPlayer, rng).get(0).itemId().equals("bunnidogs:tan_pelt"), "no player kill needed: the hide drops on any death");
        check(Drops.roll(Genome.DEFAULT, Stats.ZERO, burning, rng).get(0).itemId().equals("bunnidogs:tan_pelt"), "a burning death still gives the pelt");
        check(Drops.expected(Genome.DEFAULT, Stats.ZERO, pup).stream().noneMatch(e -> e.itemId().endsWith("_pelt")), "a pup's expectations have no pelt");
        check(Drops.describe(Genome.DEFAULT, Stats.ZERO, true).indexOf("pelt") < 0, "a pup's Calipers line has no pelt");
        check(Drops.describe(homozygous(Genome.DEFAULT, Gene.COAT, "ember"), Stats.ZERO, false).startsWith("Drops (adult kill, no Looting): pelt: ember, flesh"), "the Calipers name the coat of the pelt");

        // one pelt per coat of the table: texture, client item, model, name, tag entry; all different pictures
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        String tag = compact(Files.readString(data.resolve("tags/item/pelts.json")));
        Set<String> pictures = new HashSet<>();
        for (Allele coat : Gene.COAT.alleles()) {
            String name = coat.name() + "_pelt";
            byte[] png = Files.readAllBytes(assets.resolve("textures/item/" + name + ".png"));
            int[] size = pngSize(png);
            check(size[0] == 16 && size[1] == 16, name + " texture is 16x16");
            pictures.add(java.util.Arrays.toString(png));
            check(compact(Files.readString(assets.resolve("items/" + name + ".json"))).contains("\"model\":\"bunnidogs:item/" + name + "\""), name + " client item points at its model");
            check(compact(Files.readString(assets.resolve("models/item/" + name + ".json"))).contains("\"layer0\":\"bunnidogs:item/" + name + "\""), name + " model points at its texture");
            check(lang.contains("\"item.bunnidogs." + name + "\""), name + " has a name");
            check(tag.contains("\"bunnidogs:" + name + "\""), name + " is in the tag bunnidogs:pelts");
            check(Files.exists(assets.resolve("textures/entity/bunnidog_" + coat.name() + ".png")), name + " belongs to a coat that has an entity texture");
        }
        check(pictures.size() == Gene.COAT.alleles().size(), "every pelt has its own picture, got " + pictures.size() + " different of " + Gene.COAT.alleles().size());
        int tagged = tag.split("\"bunnidogs:", -1).length - 1;
        check(tagged == Gene.COAT.alleles().size() && tag.contains("\"replace\":false"), "the tag holds exactly the pelts of the coat table, got " + tagged);

        // the leather recipe and the registration loop
        String recipe = compact(Files.readString(data.resolve("recipe/leather_from_bunnidog_pelts.json")));
        check(recipe.contains("\"type\":\"minecraft:crafting_shapeless\"") && recipe.contains("\"ingredients\":[\"#bunnidogs:pelts\",\"#bunnidogs:pelts\"]")
                && recipe.contains("\"result\":{\"id\":\"minecraft:leather\"}"), "any two pelts -> one leather");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("for (Allele coat : Gene.COAT.alleles())") && items.contains("coat.isRare() ? Rarity.RARE : coat.isWild() ? Rarity.UNCOMMON : Rarity.COMMON")
                && items.contains("ITEMS.registerItem(coat.name() + Drops.PELT_SUFFIX, Item::new, props -> props.rarity(rarity))"), "pelts are registered from the coat table, rarity by kind of coat");
        check(items.contains("for (DeferredItem<Item> pelt : PELTS.values())") && items.contains("event.accept(pelt.get())"), "the pelts are in a creative tab");
        int rare = 0, wild = 0, common = 0;
        for (Allele coat : Gene.COAT.alleles()) {
            if (coat.isRare()) rare++; else if (coat.isWild()) wild++; else common++;
        }
        check(common == 3 && rare == 1 && wild == 8, "3 common, 1 rare (spotted) and 8 wild coats, got " + common + "/" + rare + "/" + wild);
        check(Gene.COAT.alleleNamed("spotted").isRare(), "the rare pelt is the spotted one");
        String art = Files.readString(Path.of("art/gen_pelts.py"));
        check(art.contains("from gen_bunnidog import COATS") && art.contains("for coat, look in COATS.items()"), "the pelt art loops over the entity's coat table");
    }
    // ---------------------------------------------------------------------------------------------
    static Drops.Signature signatureRow(String key, String itemId) {
        for (Drops.Signature s : Drops.ALL_SIGNATURES) {
            if (s.key().equals(key) && s.itemId().equals(itemId)) {
                return s;
            }
        }
        throw new IllegalStateException("no signature " + key + " -> " + itemId);
    }

    /** Drops, chunk D3 (docs/DROPS_SPECS.md 1.5 and 2.3): the gene signatures. */
    static void signatures() throws Exception {
        System.out.println("signatures (D3)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Drops.Context pup = adult.withBaby(true);
        Drops.Context noPlayer = new Drops.Context(Drops.Source.DEATH, false, false, false, 0, false);
        Random rng = new Random(33);

        // the table: 24 rows of the spec, every one a rare or wild allele that exists, 23 active (D4)
        check(Drops.ALL_SIGNATURES.size() == 24, "24 signature rows, got " + Drops.ALL_SIGNATURES.size());
        Set<String> seen = new HashSet<>();
        for (Drops.Signature s : Drops.ALL_SIGNATURES) {
            Allele a = s.gene().alleleNamed(s.allele());                       // throws if the allele does not exist
            check(!a.isCommon() || s.gene() == Gene.TEMPERAMENT, s.key() + " is a rare or wild allele (common alleles have no row, except the temperaments skittish and feral: their effect is no numeric multiplier, spec 2.3 note)");
            check(s.min() >= 1 && s.max() >= s.min(), s.key() + " -> " + s.itemId() + ": count range");
            check(s.base() > 0.0 && s.base() < 1.0, s.key() + " -> " + s.itemId() + ": base chance");
            check(!s.label().isBlank() && s.itemId().contains(":"), s.key() + " -> " + s.itemId() + ": label and item id");
            check((s.stat() == null) == (s.gene() == Gene.COAT), s.key() + ": coat rows use the worth factor, the others a stat factor");
            check(s.sources().equals(Set.of(Drops.Source.DEATH)), s.key() + " applies to a death");
            check(seen.add(s.key() + "->" + s.itemId()), s.key() + " -> " + s.itemId() + " is listed once");
        }
        check(Drops.NOT_YET.isEmpty(), "every item of the tables exists: the fang shipped in D4, the starlit dust in D7b");
        Set<String> commonRows = new HashSet<>();
        for (Drops.Signature s : Drops.ALL_SIGNATURES) {
            if (s.gene().alleleNamed(s.allele()).isCommon()) commonRows.add(s.key());
        }
        check(commonRows.equals(Set.of("temperament:skittish", "temperament:feral")), "the only rows of COMMON alleles are skittish and feral, got " + commonRows);
        check(Drops.SIGNATURES.size() == 24 && Drops.SIGNATURES.stream().filter(s -> s.itemId().startsWith("bunnidogs:")).map(Drops.Signature::itemId).collect(java.util.stream.Collectors.toSet()).equals(Set.of("bunnidogs:fang", "bunnidogs:starlit_dust")), "24 active rows, the fang and the dust the only modded item among them");
        check(Drops.SIGNATURES.size() == Drops.ALL_SIGNATURES.size(), "no row waits any more");

        // the factors
        near(Drops.statFactor(-100), 0.5, 1e-12, "a line bred hard against the stat is halved, no further");
        near(Drops.statFactor(-25), 0.5, 1e-12, "statFactor(-25) = 0.5");
        near(Drops.statFactor(0), 1.0, 1e-12, "statFactor(0) = 1");
        near(Drops.statFactor(25), 1.5, 1e-12, "statFactor(25) = 1.5");
        near(Drops.statFactor(50), 2.0, 1e-12, "statFactor(50) = 2");
        near(Drops.statFactor(100), 3.0, 1e-12, "statFactor(100) = 3");
        near(Drops.statFactor(1000), 3.0, 1e-12, "statFactor is capped at 3");
        check(Drops.worth(Stats.of(10, -5, 20, 0, 3)) == 33 && Drops.worth(Stats.ZERO) == 0 && Drops.worth(Stats.of(-50, -50, -50, -50, -50)) == 0, "worth = the positive points only");
        near(Drops.worthFactor(0), 1.0, 1e-12, "worthFactor(0) = 1");
        near(Drops.worthFactor(30), 1.3, 1e-12, "worthFactor(30) = 1.3");
        near(Drops.worthFactor(100), 2.0, 1e-12, "worthFactor(100) = 2");
        near(Drops.worthFactor(5000), 3.0, 1e-12, "worthFactor is capped at 3");

        // the spec's examples, from real genes
        Genome sandRacer = homozygous(homozygous(Genome.DEFAULT, Gene.COAT, "sand"), Gene.LEGS, "fleet");
        Stats racerStats = Stats.of(0, 30, 0, 0, 0);
        Drops.Signature nugget = signatureRow("coat:sand", "minecraft:gold_nugget");
        near(Drops.chance(nugget, racerStats, adult), 0.13, 1e-9, "sand-coated racer (worth 30): gold nugget 10% x 1.3 = 13%");
        near(Drops.chance(nugget, racerStats, new Drops.Context(Drops.Source.DEATH, false, true, false, 1, false)), 0.143, 1e-9, "Looting I: x1.1");
        near(Drops.chance(signatureRow("legs:fleet", "minecraft:sugar"), Stats.of(0, 100, 0, 0, 0), adult), 0.60, 1e-9, "fleet legs at Swiftness +100: sugar 20% x 3 = 60%");
        near(Drops.chance(signatureRow("legs:fleet", "minecraft:sugar"), racerStats, adult), 0.32, 1e-9, "fleet legs at Swiftness +30: sugar 20% x 1.6 = 32%");
        near(Drops.chance(signatureRow("build:lean", "minecraft:bone"), Stats.of(-100, 0, 0, 0, 0), adult), 0.125, 1e-9, "lean bred against Vitality: bone 25% x 0.5");
        near(Drops.chance(signatureRow("temperament:skittish", "minecraft:feather"), Stats.of(0, 100, 0, 0, 0), adult), 0.75, 1e-9, "skittish at Swiftness +100: feather 25% x 3");
        near(Drops.chance(signatureRow("temperament:skittish", "minecraft:feather"), Stats.of(0, 100, 0, 0, 0), new Drops.Context(Drops.Source.DEATH, false, true, false, 10, false)), 0.95, 1e-12, "a chance never goes above 95%");

        // who matches: only the EXPRESSED allele
        Genome glowCarrier = Genome.DEFAULT.with(Gene.COAT, 1, Gene.COAT.alleleNamed("glow").id());
        check(Drops.expected(glowCarrier, Stats.ZERO, adult).stream().noneMatch(e -> e.chanceOnly() && !e.itemId().equals(Drops.TISSUE_ID)), "a carried glow allele has no signature");
        Genome glowShown = homozygous(Genome.DEFAULT, Gene.COAT, "glow");
        check(Drops.expected(glowShown, Stats.ZERO, adult).stream().anyMatch(e -> e.itemId().equals("minecraft:glowstone_dust")), "a homozygous glow animal has the glowstone row");
        check(Drops.expected(Genome.DEFAULT, Stats.ZERO, adult).stream().noneMatch(e -> e.chanceOnly() && !e.itemId().equals(Drops.TISSUE_ID)), "a plain animal has no signature at all");

        // the roll: frequencies, count ranges, filters
        Genome snow = homozygous(Genome.DEFAULT, Gene.COAT, "snow");
        int balls = 0, ice = 0, ballCount = 0;
        for (int i = 0; i < 4000; i++) {
            for (Drops.Drop d : Drops.roll(snow, Stats.ZERO, adult, rng)) {
                if (d.itemId().equals("minecraft:snowball")) {
                    balls++;
                    ballCount += d.count();
                    check(d.count() >= 4 && d.count() <= 8, "snowballs come 4 to 8, got " + d.count());
                } else if (d.itemId().equals("minecraft:packed_ice")) {
                    ice++;
                    check(d.count() >= 1 && d.count() <= 2, "packed ice comes 1 to 2, got " + d.count());
                }
            }
        }
        near(balls / 4000.0, 0.30, 0.04, "snowball chance of a plain snow animal over 4000 kills (" + balls + ")");
        near(ice / 4000.0, 0.08, 0.03, "packed ice chance over 4000 kills (" + ice + ")");
        near(ballCount / (double) balls, 6.0, 0.3, "snowballs average 6 when they drop");
        int glowDust = 0;
        for (int i = 0; i < 4000; i++) {
            for (Drops.Drop d : Drops.roll(glowShown, Stats.ZERO, adult, rng)) {
                glowDust += d.itemId().equals("minecraft:glowstone_dust") ? 1 : 0;
            }
        }
        near(glowDust / 4000.0, 0.12, 0.03, "glowstone dust chance of a glow animal over 4000 kills (" + glowDust + ")");
        boolean sawSignature = false;
        for (int i = 0; i < 600; i++) {
            sawSignature |= Drops.roll(snow, Stats.ZERO, pup, rng).stream().anyMatch(d -> d.itemId().equals("minecraft:snowball"));
            sawSignature |= Drops.roll(snow, Stats.ZERO, noPlayer, rng).stream().anyMatch(d -> d.itemId().equals("minecraft:snowball"));
            sawSignature |= Drops.roll(glowCarrier, Stats.ZERO, adult, rng).stream().anyMatch(d -> d.itemId().equals("minecraft:glowstone_dust"));
        }
        check(!sawSignature, "no signature for a pup, without a player kill, or for a carried allele (1800 rolls)");
        check(Drops.expected(snow, Stats.ZERO, pup).stream().noneMatch(e -> e.chanceOnly() && !e.itemId().equals(Drops.TISSUE_ID)) && Drops.expected(snow, Stats.ZERO, noPlayer).stream().noneMatch(e -> e.chanceOnly()),
                "the tools show no signature when a kill would not roll them");

        // several rows at once: slate coat + hoofed legs + feral temperament (the fang row is live since D4)
        Genome rough = homozygous(homozygous(homozygous(Genome.DEFAULT, Gene.COAT, "slate"), Gene.LEGS, "hoofed"), Gene.TEMPERAMENT, "feral");
        Set<String> items = new HashSet<>();
        for (Drops.Expectation e : Drops.expected(rough, Stats.ZERO, adult)) {
            if (e.chanceOnly() && !e.itemId().equals(Drops.TISSUE_ID)) {
                items.add(e.itemId());
            }
        }
        check(items.equals(Set.of("minecraft:flint", "minecraft:raw_iron", "minecraft:leather", "minecraft:bone", "bunnidogs:fang")), "slate + hoofed + feral: flint, raw iron, leather, bone and the fang, got " + items);

        // the readouts
        List<Drops.Expectation> racerExpected = Drops.expected(sandRacer, racerStats, adult);
        Drops.Expectation nuggetExpected = racerExpected.stream().filter(e -> e.itemId().equals("minecraft:gold_nugget")).findFirst().orElseThrow();
        near(nuggetExpected.chanceAtLeastOne(), 0.13, 1e-9, "expected: gold nugget chance");
        near(nuggetExpected.mean(), 0.26, 1e-9, "expected: gold nugget average count (chance x 2)");
        check(Drops.describe(nuggetExpected).equals("gold nugget 13%"), "a signature prints as a percentage: " + Drops.describe(nuggetExpected));
        String line = Drops.describe(sandRacer, racerStats, false);
        check(line.startsWith("Drops (adult kill, no Looting): pelt: sand, flesh ") && line.endsWith(", gold nugget 13%, sugar 32%, tissue sample 4%"), "the Calipers list the signatures, then the sample, in table order: " + line);
        check(Drops.describe(sandRacer, racerStats, true).indexOf('%') > 0 && Drops.describe(sandRacer, racerStats, true).indexOf("gold nugget") < 0, "a pup's line has no signature");
        var sim = Drops.simulate(sandRacer, racerStats, adult, 4000, new Random(5));
        near(sim.get("minecraft:gold_nugget"), 0.26, 0.06, "simulate: gold nuggets per kill over 4000 kills");
        near(sim.get("minecraft:sugar"), 0.64, 0.08, "simulate: sugar per kill over 4000 kills (32% x 2)");
        check(sim.containsKey("minecraft:gold_nugget") && sim.containsKey("minecraft:sugar") && !sim.containsKey("minecraft:snowball"), "simulate lists the animal's own signature items only");
    }
    // ---------------------------------------------------------------------------------------------
    /** Drops, chunk D4 (docs/DROPS_SPECS.md 2.1, 4, 8): sinew and whisker (stat lines) and the fang (feral signature) become active, with items, art and recipes. */
    static void materials() throws Exception {
        System.out.println("materials (D4)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Random rng = new Random(44);

        // the two lines are active, in the spec's table order, and read the stats the spec names
        check(Drops.LINES.contains(Drops.SINEW) && Drops.LINES.contains(Drops.WHISKER), "sinew and whisker are active");
        int iFoot = Drops.LINES.indexOf(Drops.FOOT), iSinew = Drops.LINES.indexOf(Drops.SINEW), iWhisker = Drops.LINES.indexOf(Drops.WHISKER), iBone = Drops.LINES.indexOf(Drops.BONE_MEAL);
        check(iFoot < iSinew && iSinew < iWhisker && iWhisker < iBone, "table order: foot, sinew, whisker, bone meal");
        check(Drops.SINEW.stat() == Stat.LEAP && Drops.WHISKER.stat() == Stat.SENSES, "sinew grows with Leap, whisker with Senses");
        check(Drops.SINEW.itemId().equals("bunnidogs:sinew") && Drops.WHISKER.itemId().equals("bunnidogs:whisker") && Drops.SINEW.cookedId() == null && Drops.WHISKER.cookedId() == null, "item ids; neither has a burning variant");

        // the spec's worked examples through the whole roll (not only expectedCount): hoofed Leap +20 -> E 2.18, bat-eared Senses +30 -> E 3.70
        Genome hoofed = homozygous(Genome.DEFAULT, Gene.LEGS, "hoofed");
        Stats leap20 = Stats.of(0, 0, 20, 0, 0);
        Genome bat = homozygous(Genome.DEFAULT, Gene.EARS, "bat");
        Stats senses30 = Stats.of(0, 0, 0, 30, 0);
        long sinew = 0, whiskers = 0;
        for (int i = 0; i < 3000; i++) {
            for (Drops.Drop d : Drops.roll(hoofed, leap20, adult, rng)) {
                if (d.itemId().equals("bunnidogs:sinew")) {
                    sinew += d.count();
                    check(d.count() == 2 || d.count() == 3, "2.18 sinew rounds to 2 or 3, got " + d.count());
                }
            }
            for (Drops.Drop d : Drops.roll(bat, senses30, adult, rng)) {
                if (d.itemId().equals("bunnidogs:whisker")) {
                    whiskers += d.count();
                    check(d.count() == 3 || d.count() == 4, "3.70 whiskers round to 3 or 4, got " + d.count());
                }
            }
        }
        near(sinew / 3000.0, 2.18, 0.08, "hoofed, Leap +20: about 2.18 sinew per kill (" + sinew + " in 3000)");
        near(whiskers / 3000.0, 3.70, 0.08, "bat-eared, Senses +30: about 3.70 whiskers per kill (" + whiskers + " in 3000)");
        check(Drops.roll(hoofed, Stats.of(0, 0, 1000, 0, 0), adult, rng).stream().filter(d -> d.itemId().equals("bunnidogs:sinew")).allMatch(d -> d.count() == 8), "a huge Leap hits the cap of 8");

        // a line that cannot drop is not listed, and never drops (the plain animal is the D1 line, unchanged)
        List<Drops.Expectation> plain = Drops.expected(Genome.DEFAULT, Stats.ZERO, adult);
        check(plain.stream().noneMatch(e -> e.itemId().equals("bunnidogs:sinew") || e.itemId().equals("bunnidogs:whisker")), "a plain animal lists no sinew and no whisker");
        check(!Drops.describe(Genome.DEFAULT, Stats.ZERO, false).contains("sinew") && !Drops.describe(Genome.DEFAULT, Stats.ZERO, false).contains("whisker"), "the plain Calipers line has no 0.00 lines");
        Drops.Context loot3 = new Drops.Context(Drops.Source.DEATH, false, true, false, 3, false);
        Stats against = Stats.of(0, 0, -50, -50, 0);
        for (int i = 0; i < 500; i++) {
            for (Drops.Context ctx : new Drops.Context[] {adult, loot3}) {
                check(Drops.roll(Genome.DEFAULT, Stats.ZERO, ctx, rng).stream().noneMatch(d -> d.itemId().equals("bunnidogs:sinew") || d.itemId().equals("bunnidogs:whisker")), "no sinew or whisker from an unbred animal, Looting III or not");
                check(Drops.roll(Genome.DEFAULT, against, ctx, rng).stream().noneMatch(d -> d.itemId().equals("bunnidogs:sinew") || d.itemId().equals("bunnidogs:whisker")), "none from a line bred against Leap and Senses either");
            }
        }
        check(Drops.expected(Genome.DEFAULT, against, adult).stream().noneMatch(e -> e.itemId().startsWith("bunnidogs:s") || e.itemId().equals("bunnidogs:whisker")), "the tools list nothing for those lines either");
        check(!Drops.simulate(Genome.DEFAULT, Stats.ZERO, adult, 50, rng).containsKey("bunnidogs:sinew") && !Drops.simulate(Genome.DEFAULT, Stats.ZERO, adult, 50, rng).containsKey("bunnidogs:whisker"), "simulate lists no sinew or whisker for a plain animal");
        check(Drops.simulate(hoofed, leap20, adult, 50, rng).containsKey("bunnidogs:sinew"), "simulate lists the sinew of a Leap-bred animal");

        // the readouts for a Leap animal: sinew sits between the foot and the bone meal, with its average; a pup gets a quarter
        String line = Drops.describe(hoofed, leap20, false);
        check(line.contains("sinew 2.2") && line.indexOf("rabbit's foot") < line.indexOf("sinew") && line.indexOf("sinew") < line.indexOf("bone meal"), "Calipers line of a Leap animal: " + line);
        String pupLine = Drops.describe(hoofed, leap20, true);
        check(pupLine.contains("sinew 0.54 (54% for one)"), "a pup of that line shows a quarter of E = 2.176 (1.36 x 1.6): " + pupLine);
        check(Drops.describe(bat, senses30, false).contains("whisker 3.7"), "Calipers line of a Senses animal: " + Drops.describe(bat, senses30, false));

        // the fang: the feral row is live (20% at Vitality 0, 60% at +100, always one), adult + player kill only, listed before the feral bone
        Genome feral = homozygous(Genome.DEFAULT, Gene.TEMPERAMENT, "feral");
        Drops.Signature fangRow = signatureRow("temperament:feral", "bunnidogs:fang");
        check(Drops.SIGNATURES.contains(fangRow) && !Drops.NOT_YET.contains("bunnidogs:fang"), "the fang row is active");
        near(Drops.chance(fangRow, Stats.ZERO, adult), 0.20, 1e-9, "feral fang: 20% at Vitality 0");
        near(Drops.chance(fangRow, Stats.of(100, 0, 0, 0, 0), adult), 0.60, 1e-9, "feral fang: 60% at Vitality +100");
        near(Drops.chance(fangRow, Stats.of(-100, 0, 0, 0, 0), adult), 0.10, 1e-9, "feral fang: halved when bred against Vitality");
        Drops.Context noPlayer = new Drops.Context(Drops.Source.DEATH, false, false, false, 0, false);
        int fangs = 0;
        for (int i = 0; i < 4000; i++) {
            for (Drops.Drop d : Drops.roll(feral, Stats.ZERO, adult, rng)) {
                if (d.itemId().equals("bunnidogs:fang")) {
                    fangs++;
                    check(d.count() == 1, "always one fang, got " + d.count());
                }
            }
        }
        near(fangs / 4000.0, 0.20, 0.03, "fang frequency of a feral animal over 4000 kills (" + fangs + ")");
        boolean fangWrongly = false;
        for (int i = 0; i < 1000; i++) {
            fangWrongly |= Drops.roll(feral, Stats.ZERO, adult.withBaby(true), rng).stream().anyMatch(d -> d.itemId().equals("bunnidogs:fang"));
            fangWrongly |= Drops.roll(feral, Stats.ZERO, noPlayer, rng).stream().anyMatch(d -> d.itemId().equals("bunnidogs:fang"));
            fangWrongly |= Drops.roll(Genome.DEFAULT.with(Gene.TEMPERAMENT, 1, Gene.TEMPERAMENT.alleleNamed("feral").id()), Stats.ZERO, adult, rng).stream().anyMatch(d -> d.itemId().equals("bunnidogs:fang"));
        }
        check(!fangWrongly, "no fang for a pup, without a player kill, or from a carried feral allele (3000 rolls)");
        String feralLine = Drops.describe(feral, Stats.ZERO, false);
        check(feralLine.contains("fang 20%") && feralLine.indexOf("fang 20%") < feralLine.indexOf("bone 20%"), "the Calipers show the fang before the feral bone: " + feralLine);
        check(Drops.ALL_SIGNATURES.stream().anyMatch(sg -> sg.itemId().equals("bunnidogs:starlit_dust")) && Drops.SIGNATURES.stream().anyMatch(sg -> sg.itemId().equals("bunnidogs:starlit_dust")), "the starlit dust row is active since D7b");

        // every ACTIVE modded id of the tables is a registered item (pelts excepted: they are registered in a loop and tested in pelts())
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        Set<String> modded = new HashSet<>();
        for (Drops.Line l : Drops.LINES) {
            modded.add(l.itemId());
            if (l.cookedId() != null) modded.add(l.cookedId());
        }
        for (Drops.Signature sg : Drops.SIGNATURES) {
            modded.add(sg.itemId());
        }
        modded.removeIf(id -> !id.startsWith("bunnidogs:"));
        check(modded.equals(Set.of("bunnidogs:raw_bunnidog_flesh", "bunnidogs:cooked_bunnidog_flesh", "bunnidogs:prime_haunch", "bunnidogs:roast_haunch", "bunnidogs:sinew", "bunnidogs:whisker", "bunnidogs:fang", "bunnidogs:starlit_dust")), "the active modded drops are flesh, cooked flesh, haunch, roast haunch, sinew, whisker, fang, starlit dust, got " + modded);
        for (String id : modded) {
            String name = id.substring("bunnidogs:".length());
            check(items.contains("ITEMS.registerItem(\"" + name + "\", "), name + " is registered");
        }
        check(items.contains("event.accept(SINEW.get())") && items.contains("event.accept(WHISKER.get())") && items.contains("event.accept(FANG.get())"), "the three materials are in a creative tab");

        // the three items: texture, client item, model, name; three different pictures
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        Set<String> pictures = new HashSet<>();
        for (String name : new String[] {"sinew", "whisker", "fang"}) {
            byte[] png = Files.readAllBytes(assets.resolve("textures/item/" + name + ".png"));
            int[] size = pngSize(png);
            check(size[0] == 16 && size[1] == 16, name + " texture is 16x16");
            pictures.add(java.util.Arrays.toString(png));
            check(compact(Files.readString(assets.resolve("items/" + name + ".json"))).contains("\"model\":\"bunnidogs:item/" + name + "\""), name + " client item points at its model");
            check(compact(Files.readString(assets.resolve("models/item/" + name + ".json"))).contains("\"layer0\":\"bunnidogs:item/" + name + "\""), name + " model points at its texture");
            check(lang.contains("\"item.bunnidogs." + name + "\": \"Bunnidog " + Character.toUpperCase(name.charAt(0)) + name.substring(1) + "\""), name + " has its name");
        }
        check(pictures.size() == 3, "three different pictures");
        String art = Files.readString(Path.of("art/gen_materials.py"));
        check(art.contains("\"sinew\"") && art.contains("\"whisker\"") && art.contains("\"fang\"") && Files.exists(Path.of("art/materials_preview.png")), "the art script and its preview exist");

        // the three recipes (every item ships with its use)
        String string = compact(Files.readString(data.resolve("recipe/string_from_sinew.json")));
        check(string.contains("\"type\":\"minecraft:crafting_shapeless\"") && string.contains("\"ingredients\":[\"bunnidogs:sinew\"]") && string.contains("\"result\":{\"id\":\"minecraft:string\",\"count\":2}"), "1 sinew -> 2 string");
        String brush = compact(Files.readString(data.resolve("recipe/brush_from_whisker.json")));
        check(brush.contains("\"type\":\"minecraft:crafting_shaped\"") && brush.contains("\"key\":{\"W\":\"bunnidogs:whisker\",\"C\":\"minecraft:copper_ingot\",\"S\":\"minecraft:stick\"}")
                && brush.contains("\"pattern\":[\"W\",\"C\",\"S\"]") && brush.contains("\"result\":{\"id\":\"minecraft:brush\"}"), "whisker over copper ingot over stick -> brush");
        String arrows = compact(Files.readString(data.resolve("recipe/arrows_from_fang.json")));
        check(arrows.contains("\"type\":\"minecraft:crafting_shaped\"") && arrows.contains("\"key\":{\"F\":\"bunnidogs:fang\",\"S\":\"minecraft:stick\",\"P\":\"minecraft:feather\"}")
                && arrows.contains("\"pattern\":[\"F\",\"S\",\"P\"]") && arrows.contains("\"result\":{\"id\":\"minecraft:arrow\",\"count\":4}"), "fang over stick over feather -> 4 arrows");

        // the handler did not change: it still adds whatever Drops.roll returns, looking ids up in the registry
        String handler = Files.readString(src.resolve("drop/BunnidogDrops.java"));
        check(handler.contains("Drops.roll(") && handler.contains("BuiltInRegistries.ITEM.getValue(Identifier.parse(drop.itemId()))"), "the handler needs no change for new items");
    }

    // ---------------------------------------------------------------------------------------------
    /** Drops, chunk D5 (docs/DROPS_SPECS.md 2.1, 4, 8): the haunch line (Vitality, onset 20) becomes active with prime/roast haunch, art and cooking recipes. */
    static void seedWindows() throws Exception {
        System.out.println("Seed and Tissue Sample windows (Phase 8 S8)");
        com.bunnidogs.mod.layout.Metrics m = com.bunnidogs.mod.layout.Metrics.ESTIMATE;
        java.util.function.Function<com.bunnidogs.mod.layout.Page, java.util.List<String>> dump = pg -> {
            java.util.List<String> out = new java.util.ArrayList<>();
            for (com.bunnidogs.mod.layout.Page.Text t : pg.texts()) {
                out.add(t.y() + "|" + t.text());
            }
            return out;
        };
        Genome mom = g("22-32-00-10-00-01"), dad = g("11-00-00-11-00-00");
        Seed seed = new Seed(mom, Stats.of(3, -1, 0, 2, 12), dad, Stats.ZERO, 3.5);

        // the folio: the seed, the mother, the father; out-of-range numbers are clamped
        check(com.bunnidogs.mod.layout.SeedSheet.spreadCount() == 3, "a seed window has three spreads");
        check(dump.apply(com.bunnidogs.mod.layout.SeedSheet.spread(seed, 99, m).left()).equals(dump.apply(com.bunnidogs.mod.layout.SeedSheet.spread(seed, 2, m).left()))
                && dump.apply(com.bunnidogs.mod.layout.SeedSheet.spread(seed, -4, m).left()).equals(dump.apply(com.bunnidogs.mod.layout.SeedSheet.spread(seed, 0, m).left())), "spread numbers are clamped into 0..2");

        // spread 0, left page: the numbers are the seed's own
        com.bunnidogs.mod.layout.Page.Spread ov = com.bunnidogs.mod.layout.SeedSheet.spread(seed, 0, m);
        java.util.List<String> left = dump.apply(ov.left()), right = dump.apply(ov.right());
        String litter = String.format(Locale.ROOT, "about %.1f pups", seed.expectedLitter());
        String mutation = String.format(Locale.ROOT, "%.1f%% per pup", seed.mutationChance() * 100.0);
        check(left.contains("0|BUNNIDOG SEED") && left.contains("14|Quality") && left.contains("14|3.50") && left.contains("37|Litter") && left.contains("37|" + litter)
                && left.contains("60|Mutation") && left.contains("60|" + mutation) && left.contains("83|NEXT LITTER"), "the seed page lists quality 3.50, the litter and the mutation chance of the seed: " + left);
        check(left.contains("92|" + com.bunnidogs.mod.layout.Page.wrap(Seed.driftText(seed.drift(0.0D)), 130, m).get(0)), "the drift lines are the seed's own drift text, wrapped: " + left);
        check(left.stream().noneMatch(t -> t.endsWith("|mutagen")) && left.contains("69|set by fertility"), "an untreated seed shows no mutagen mark");
        Seed treated = seed.withMutagen();
        java.util.List<String> tl = dump.apply(com.bunnidogs.mod.layout.SeedSheet.spread(treated, 0, m).left());
        check(tl.contains("0|mutagen") && tl.contains("60|50.0% per pup") && tl.contains("69|treated with Mutagen"), "a treated seed shows the mark and the 50% floor: " + tl);

        // hover texts carry the game's own constants
        java.util.function.IntFunction<String> hoverAt = y -> {
            for (com.bunnidogs.mod.layout.Page.Hotspot h : ov.left().hotspots()) {
                if (h.y() == y) {
                    return h.text();
                }
            }
            return "";
        };
        check(hoverAt.apply(14).contains("50% bigger") && hoverAt.apply(14).contains("25% smaller") && hoverAt.apply(14).contains("quality 4") && hoverAt.apply(14).contains("0.5 quality per level"), "the quality box: " + hoverAt.apply(14));
        check(hoverAt.apply(37).contains("2 x the parents' average fertility") && hoverAt.apply(37).contains("between 1 and 6") && hoverAt.apply(37).contains("40 or more") && hoverAt.apply(37).contains("16 blocks"), "the litter box: " + hoverAt.apply(37));
        check(hoverAt.apply(60).contains("at least 50%") && hoverAt.apply(60).contains("A Mutagen raises it"), "the mutation box (untreated)");
        check(com.bunnidogs.mod.layout.SeedSheet.spread(treated, 0, m).left().hotspots().stream().anyMatch(h -> h.y() == 60 && h.text().contains("is treated with a Mutagen") && h.text().contains("at least 50%")), "the mutation box (treated)");
        check(hoverAt.apply(83).contains(Seed.driftText(seed.drift(0.0D))), "the drift box");

        // spread 0, right page: both parents, each a link to its sheet
        check(right.contains("0|PARENTS") && right.contains("14|MOTHER") && right.contains("77|FATHER") && right.contains("59|22-32-00-10-00-01-00") && right.contains("122|11-00-00-11-00-00-00")
                && right.stream().anyMatch(t -> t.startsWith("50|Carries")) && right.stream().anyMatch(t -> t.startsWith("113|Carries")), "the parents page: names, codes, carried genes: " + right);
        check(ov.left().links().isEmpty() && ov.right().links().equals(java.util.List.of(new com.bunnidogs.mod.layout.Page.Link(0, 14, 130, 54, 1), new com.bunnidogs.mod.layout.Page.Link(0, 77, 130, 54, 2))),
                "the two parent blocks link to spreads 1 and 2, nothing else does");

        // spreads 1 and 2: a parent's sheet without a number, a generation or parents of its own
        for (int n = 1; n <= 2; n++) {
            com.bunnidogs.mod.layout.Page.Spread sp = com.bunnidogs.mod.layout.SeedSheet.spread(seed, n, m);
            java.util.List<String> l = dump.apply(sp.left()), r = dump.apply(sp.right());
            check(l.contains("0|" + (n == 1 ? "Seed: mother" : "Seed: father")) && l.contains("0|adult") && l.stream().noneMatch(t -> t.contains("#") || t.contains("generation")) && sp.left().pairs().size() == Gene.COUNT, "parent " + n + " header, adult, no # or generation: " + l);
            check(r.contains("0|STATS") && r.contains("9|Vitality") && r.contains(n == 1 ? "135|Mother of the seed" : "135|Father of the seed") && r.contains(n == 1 ? "144|Her parents: not kept" : "144|His parents: not kept"), "parent " + n + " right page: " + r);
            check(sp.left().links().isEmpty() && sp.right().links().isEmpty(), "a parent's sheet has no links");
        }
        check(com.bunnidogs.mod.layout.SeedSheet.spread(seed, 1, m).left().portrait().coat().equals("spotted") && com.bunnidogs.mod.layout.SeedSheet.spread(seed, 2, m).left().portrait().coat().equals("tan"), "each portrait is that parent's coat");
        check(dump.apply(com.bunnidogs.mod.layout.SeedSheet.spread(seed, 1, m).right()).contains("9|+3") && dump.apply(com.bunnidogs.mod.layout.SeedSheet.spread(seed, 2, m).right()).contains("9|0"), "each parent shows its own stats");
        Pedigree.Entry pe = com.bunnidogs.mod.layout.SeedSheet.parentEntry(seed, true);
        check(pe.genome().equals(mom) && pe.stats().equals(Optional.of(seed.motherStats())) && !pe.pup() && pe.mother().isEmpty() && pe.id().isEmpty() && pe.tag().isEmpty(), "the mother as a sheet entry: genes and stats, adult, nothing else");

        // the Tissue Sample window: the same sheet, no age, no family
        Tissue t = new Tissue(mom, Stats.of(3, -1, 0, 2, 12));
        com.bunnidogs.mod.layout.Page.Spread ts = com.bunnidogs.mod.layout.SheetLayout.sample(t, m);
        java.util.List<String> sl = dump.apply(ts.left()), sr = dump.apply(ts.right());
        check(sl.contains("0|Tissue sample") && sl.stream().noneMatch(x -> x.equals("0|adult") || x.equals("0|pup") || x.contains("#") || x.contains("generation")) && sl.contains("79|short") && sl.contains("88|spotted") && ts.left().pairs().size() == Gene.COUNT,
                "the sample sheet: header, no adult / pup, no # or generation, the gene rows: " + sl);
        check(sr.contains("9|Vitality") && sr.contains("9|+3") && sr.contains("135|A sample holds only") && sr.contains("144|this animal's own genes") && sr.stream().noneMatch(x -> x.contains("Mom") || x.contains("Dad") || x.contains("recorded")), "the sample's right page: stats, and what it does not know: " + sr);
        check(ts.left().hotspots().stream().anyMatch(h -> h.y() == 11 && h.text().startsWith("A Tissue Sample holds the genes")) && ts.left().links().isEmpty() && ts.right().links().isEmpty(), "hovering the sample's portrait explains the sample; no links");
        Pedigree.Entry se = com.bunnidogs.mod.layout.SheetLayout.sampleEntry(t);
        check(se.genome().equals(mom) && se.stats().equals(Optional.of(t.stats())) && !se.pup() && se.mother().isEmpty() && se.father().isEmpty() && se.id().isEmpty() && se.tag().isEmpty(), "the sample as a sheet entry");

        // the wire text: what the server sends is what the client parses
        check(Seed.parse(seed.toString()).equals(Optional.of(seed)) && Seed.parse(treated.toString()).equals(Optional.of(treated)) && Tissue.parse(t.toString()).equals(Optional.of(t)), "a seed and a sample read back whole from their text");

        // random seeds: every text, hotspot and link stays inside the 130 x 156 page, and no two hotspots of the seed page overlap
        java.util.Random rnd = new java.util.Random(8);
        boolean inside = true, apart = true;
        for (int i = 0; i < 100; i++) {
            Seed rs = new Seed(new Genome(rnd.nextLong()), Stats.of(rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30),
                    new Genome(rnd.nextLong()), Stats.of(rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30, rnd.nextInt(61) - 30),
                    rnd.nextDouble() * 40.0, rnd.nextBoolean());
            for (int n = 0; n < 3; n++) {
                com.bunnidogs.mod.layout.Page.Spread sp = com.bunnidogs.mod.layout.SeedSheet.spread(rs, n, m);
                for (com.bunnidogs.mod.layout.Page pg : new com.bunnidogs.mod.layout.Page[] { sp.left(), sp.right() }) {
                    for (com.bunnidogs.mod.layout.Page.Hotspot h : pg.hotspots()) {
                        inside &= h.x() >= 0 && h.y() >= 0 && h.x() + h.w() <= 130 && h.y() + h.h() <= 156;
                    }
                    for (com.bunnidogs.mod.layout.Page.Link k : pg.links()) {
                        inside &= k.x() >= 0 && k.y() >= 0 && k.x() + k.w() <= 130 && k.y() + k.h() <= 156;
                    }
                    if (n == 0) {
                        for (com.bunnidogs.mod.layout.Page.Text tx : pg.texts()) {
                            inside &= tx.right() ? tx.x() - m.width(tx.text()) >= 0 : tx.x() + m.width(tx.text()) <= 130;
                            inside &= tx.y() >= 0 && tx.y() + 9 <= 156;
                        }
                        java.util.List<com.bunnidogs.mod.layout.Page.Hotspot> hs = pg.hotspots();
                        for (int a = 0; a < hs.size(); a++) {
                            for (int b = a + 1; b < hs.size(); b++) {
                                apart &= hs.get(a).y() + hs.get(a).h() <= hs.get(b).y() || hs.get(b).y() + hs.get(b).h() <= hs.get(a).y();
                            }
                        }
                    }
                }
            }
            com.bunnidogs.mod.layout.Page.Spread st = com.bunnidogs.mod.layout.SheetLayout.sample(new Tissue(rs.mother(), rs.motherStats()), m);
            for (com.bunnidogs.mod.layout.Page pg : new com.bunnidogs.mod.layout.Page[] { st.left(), st.right() }) {
                for (com.bunnidogs.mod.layout.Page.Hotspot h : pg.hotspots()) {
                    inside &= h.x() >= 0 && h.y() >= 0 && h.x() + h.w() <= 130 && h.y() + h.h() <= 156;
                }
            }
        }
        check(inside, "random seeds and samples keep every text, hotspot and link inside the page");
        check(apart, "the hover regions of the seed page never overlap");

        // ---- glue, by grep
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        for (String[] p : new String[][] { { "SeedPayload", "seed", "seed" }, { "SamplePayload", "sample", "sample" } }) {
            String pay = Files.readString(src.resolve("network/" + p[0] + ".java"));
            check(pay.contains("public record " + p[0] + "(String " + p[1] + ") implements CustomPacketPayload") && pay.contains("Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, \"" + p[2] + "\")")
                    && pay.contains("StreamCodec<ByteBuf, " + p[0] + "> STREAM_CODEC") && pay.contains("ByteBufCodecs.STRING_UTF8.map(" + p[0] + "::new, " + p[0] + "::" + p[1] + ")")
                    && pay.contains("public CustomPacketPayload.Type<" + p[0] + "> type()") && !pay.contains("import net.minecraft.client"), p[0] + " has the shape of the sheet payload that compiled");
        }
        String net = Files.readString(src.resolve("network/ModNetwork.java"));
        check(net.contains("registrar.playToClient(SheetPayload.TYPE, SheetPayload.STREAM_CODEC,") && net.contains("registrar.playToClient(SeedPayload.TYPE, SeedPayload.STREAM_CODEC,") && net.contains("registrar.playToClient(SamplePayload.TYPE, SamplePayload.STREAM_CODEC,")
                && net.contains("Seed.parse(payload.seed()).ifPresent(ClientScreens::openSeed)") && net.contains("Tissue.parse(payload.sample()).ifPresent(ClientScreens::openSample)"), "the channel registers its payloads, each opening its window through a lambda (versioned network channel)");
        check(net.contains("PacketDistributor.sendToPlayer(player, new SeedPayload(seed.toString()))") && net.contains("PacketDistributor.sendToPlayer(player, new SamplePayload(sample.toString()))") && !net.contains("import net.minecraft.client"), "the server sends the text of the seed or the sample");
        String doors = Files.readString(src.resolve("client/ClientScreens.java"));
        check(doors.contains("Minecraft.getInstance().gui.setScreen(new PedigreeScreen(seed))") && doors.contains("Minecraft.getInstance().gui.setScreen(new PedigreeScreen(sample))"), "the client doors open the seed and the sample screens");
        String screen = Files.readString(src.resolve("client/screen/PedigreeScreen.java"));
        check(screen.contains("public PedigreeScreen(Tissue sample)") && screen.contains("public PedigreeScreen(Seed seed)") && screen.contains("SheetLayout.sample(this.sample,") && screen.contains("SeedSheet.spread(this.seed, this.spread,")
                && screen.contains("buildPortrait(SeedSheet.parentEntry(this.seed, this.spread == 1))") && screen.contains("buildPortrait(SheetLayout.sampleEntry(this.sample))"), "the screen has the two constructors and lays out and draws each");
        check(screen.contains("return this.single != null || this.sample != null;") && screen.contains("return this.seed != null ? SeedSheet.spreadCount() : BookContents.spreadCount(this.book);"), "one-sheet windows have no buttons; the seed window pages over three spreads");
        String cal = Files.readString(src.resolve("item/CalipersItem.java"));
        int guard = cal.indexOf("if (!level.isClientSide())");
        check(cal.contains("if (player instanceof ServerPlayer serverPlayer) {\n            ModNetwork.sendSeed(serverPlayer, seed);\n        } else {\n            send(player, Readout.seed(seed));")
                && cal.contains("ModNetwork.sendSample(serverPlayer, tissue);\n        } else {\n            send(player, Readout.tissue(tissue));")
                && guard > 0 && guard < cal.indexOf("showSeed(player, seed.get())") && cal.lastIndexOf("showSeed(player, seed.get())") > cal.indexOf("instanceof SeedCropBlockEntity crop")
                && cal.contains("import com.bunnidogs.mod.network.ModNetwork;") && cal.contains("import net.minecraft.server.level.ServerPlayer;"), "the Calipers open the seed / sample window for a real player (server side), the chat only for a non-player");
        check(cal.contains("Right-click a planted seed: its window opens") && !cal.contains("its genes in the chat"), "the Calipers tooltip says the windows open");
    }

    static void haunch() throws Exception {
        System.out.println("haunch (D5)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Drops.Context burning = new Drops.Context(Drops.Source.DEATH, false, true, true, 0, false);
        Drops.Context loot3 = new Drops.Context(Drops.Source.DEATH, false, true, false, 3, false);
        Random rng = new Random(55);

        // the line: second in the table, grows with Vitality from +20, prime when raw, roast when the animal dies burning
        check(Drops.LINES.indexOf(Drops.HAUNCH) == 1, "the haunch is the second line, after the flesh");
        check(Drops.HAUNCH.stat() == Stat.VITALITY && Drops.HAUNCH.itemId().equals("bunnidogs:prime_haunch") && Drops.HAUNCH.cookedId().equals("bunnidogs:roast_haunch"), "haunch: Vitality, prime / roast");

        // the spec's chonker (Vitality +40, build x1.9): E 0.76 through the whole roll, a 0 or 1 count, about 76% over 1500 kills; flesh still 9 or 10
        Genome chonker = homozygous(Genome.DEFAULT, Gene.BUILD, "chonker");
        Stats vit40 = Stats.of(40, 0, 0, 0, 0);
        int haunches = 0;
        for (int i = 0; i < 1500; i++) {
            for (Drops.Drop d : Drops.roll(chonker, vit40, adult, rng)) {
                if (d.itemId().equals("bunnidogs:prime_haunch")) {
                    haunches += d.count();
                    check(d.count() == 1, "0.76 haunch rounds to 0 or 1, got " + d.count());
                }
                if (d.itemId().equals("bunnidogs:raw_bunnidog_flesh")) {
                    check(d.count() == 9 || d.count() == 10, "9.95 flesh rounds to 9 or 10, got " + d.count());
                }
            }
        }
        near(haunches / 1500.0, 0.76, 0.05, "chonker, Vitality +40: about 0.76 haunch per kill (" + haunches + " in 1500)");

        // dying burning: roast haunch (and cooked flesh) instead of prime (and raw)
        boolean sawRoast = false, sawPrimeWhileBurning = false;
        for (int i = 0; i < 200; i++) {
            for (Drops.Drop d : Drops.roll(chonker, vit40, burning, rng)) {
                sawRoast |= d.itemId().equals("bunnidogs:roast_haunch");
                sawPrimeWhileBurning |= d.itemId().equals("bunnidogs:prime_haunch") || d.itemId().equals("bunnidogs:raw_bunnidog_flesh");
            }
        }
        check(sawRoast && !sawPrimeWhileBurning, "a burning chonker drops roast haunch and never the raw items");

        // nothing below the onset, also with Looting III or bred against Vitality (E = 0 stays 0)
        for (int i = 0; i < 400; i++) {
            for (Drops.Context ctx : new Drops.Context[] {adult, loot3}) {
                for (Stats low : new Stats[] {Stats.ZERO, Stats.of(20, 0, 0, 0, 0), Stats.of(-60, 0, 0, 0, 0)}) {
                    check(Drops.roll(chonker, low, ctx, rng).stream().noneMatch(d -> d.itemId().endsWith("_haunch")), "no haunch at Vitality 20 or below, Looting III or not");
                }
            }
        }
        // a pup gives a quarter: E = 0.19, shown as 19% for one
        String pupLine = Drops.describe(chonker, vit40, true);
        check(pupLine.contains("prime haunch 0.19 (19% for one)"), "a chonker pup shows a quarter of 0.76: " + pupLine);

        // the tools: a plain animal lists no haunch; the chonker lists it right after the flesh, before the rabbit's foot
        check(!Drops.describe(Genome.DEFAULT, Stats.ZERO, false).contains("haunch"), "the plain Calipers line has no haunch");
        check(Drops.expected(Genome.DEFAULT, Stats.of(15, 0, 0, 0, 0), adult).stream().noneMatch(e -> e.itemId().equals("bunnidogs:prime_haunch")), "Vitality +15 is under the onset: no haunch listed");
        String line = Drops.describe(chonker, vit40, false);
        check(line.contains("prime haunch 0.76 (76% for one)") && line.indexOf("flesh") < line.indexOf("prime haunch") && line.indexOf("prime haunch") < line.indexOf("rabbit's foot"), "Calipers line of the chonker: " + line);
        check(Drops.simulate(chonker, vit40, adult, 50, rng).containsKey("bunnidogs:prime_haunch") && !Drops.simulate(Genome.DEFAULT, Stats.ZERO, adult, 50, rng).containsKey("bunnidogs:prime_haunch"), "simulate lists the haunch only for a Vitality-bred animal");
        check(Drops.simulate(chonker, vit40, burning, 50, rng).containsKey("bunnidogs:roast_haunch"), "simulate of a burning kill lists the roast");

        // the two items: texture, client item, model, name, two different pictures; registration (food values of the spec), creative tab, art script
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        Set<String> pictures = new HashSet<>();
        for (String name : new String[] {"prime_haunch", "roast_haunch"}) {
            byte[] png = Files.readAllBytes(assets.resolve("textures/item/" + name + ".png"));
            int[] size = pngSize(png);
            check(size[0] == 16 && size[1] == 16, name + " texture is 16x16");
            pictures.add(java.util.Arrays.toString(png));
            check(compact(Files.readString(assets.resolve("items/" + name + ".json"))).contains("\"model\":\"bunnidogs:item/" + name + "\""), name + " client item points at its model");
            check(compact(Files.readString(assets.resolve("models/item/" + name + ".json"))).contains("\"layer0\":\"bunnidogs:item/" + name + "\""), name + " model points at its texture");
            check(lang.contains("\"item.bunnidogs." + name + "\""), name + " has a name");
        }
        check(pictures.size() == 2, "two different pictures");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("new FoodProperties.Builder().nutrition(6).saturationModifier(0.4F).build()") && items.contains("ITEMS.registerItem(\"prime_haunch\", Item::new, props -> props.food(PRIME_HAUNCH_FOOD))"), "prime haunch: nutrition 6, saturation 0.4");
        check(items.contains("new FoodProperties.Builder().nutrition(10).saturationModifier(1.0F).build()") && items.contains("ITEMS.registerItem(\"roast_haunch\", Item::new, props -> props.food(ROAST_HAUNCH_FOOD))"), "roast haunch: nutrition 10, saturation 1.0");
        check(items.indexOf("event.accept(COOKED_BUNNIDOG_FLESH.get())") < items.indexOf("event.accept(PRIME_HAUNCH.get())") && items.indexOf("event.accept(PRIME_HAUNCH.get())") < items.indexOf("event.accept(ROAST_HAUNCH.get())"), "the haunches follow the flesh in the Food tab");
        check(Files.readString(Path.of("art/gen_haunch.py")).contains("prime_haunch") && Files.exists(Path.of("art/haunch_preview.png")), "the art script and its preview exist");

        // the three cooking recipes (every item ships with its use): prime -> roast, a little more experience than the flesh
        for (String[] r : new String[][] {{"smelting", "200"}, {"smoking", "100"}, {"campfire_cooking", "600"}}) {
            String recipe = compact(Files.readString(data.resolve("recipe/roast_haunch_from_" + r[0] + ".json")));
            check(recipe.contains("\"type\":\"minecraft:" + r[0] + "\"") && recipe.contains("\"ingredient\":\"bunnidogs:prime_haunch\"")
                    && recipe.contains("\"result\":{\"id\":\"bunnidogs:roast_haunch\"}") && recipe.contains("\"cookingtime\":" + r[1])
                    && recipe.contains("\"experience\":0.5"), "cooking recipe " + r[0] + ": prime -> roast, " + r[1] + " ticks");
        }

        // the handler did not change
        String handler = Files.readString(src.resolve("drop/BunnidogDrops.java"));
        check(handler.contains("Drops.roll(") && handler.contains("BuiltInRegistries.ITEM.getValue(Identifier.parse(drop.itemId()))"), "the handler needs no change for new items");
    }

    // ---------------------------------------------------------------------------------------------
    /** Drops, chunk D6a (docs/DROPS_SPECS.md section 14): the Mutagen item, the seed's 4th text part, the raised mutation chance, the apply gesture. */
    static void mutagen() throws Exception {
        System.out.println("mutagen (D6a)");
        Random rng = new Random(66);
        Genome def = Genome.DEFAULT;
        Seed plain = new Seed(def, Stats.ZERO, def, Stats.ZERO, 1.5);
        Seed treated = plain.withMutagen();

        // text form: an untreated seed prints exactly as before (3 parts); a treated one has the word as a 4th part; both round-trip
        check(!plain.mutagen() && treated.mutagen(), "withMutagen sets the flag");
        check(plain.toString().split("\\|", -1).length == 3 && !plain.toString().contains("mutagen"), "an untreated seed still prints 3 parts: " + plain);
        check(treated.toString().equals(plain + "|mutagen"), "a treated seed is the plain text plus |mutagen: " + treated);
        check(Seed.parse(treated.toString()).equals(Optional.of(treated)) && Seed.parse(plain.toString()).equals(Optional.of(plain)), "both forms round-trip");
        check(treated.withMutagen() == treated, "a seed carries one Mutagen: treating it again changes nothing");
        check(!plain.equals(treated), "a treated seed is a different seed (different stack)");
        String ok = "00-00-00-00-00-00";
        check(Seed.parse(ok + "|" + ok).orElseThrow().mutagen() == false && Seed.parse(ok + "|" + ok + "|2.5").orElseThrow().mutagen() == false, "old 2- and 3-part seeds are untreated");
        Seed blank = Seed.parse(ok + "|" + ok + "||mutagen").orElseThrow();
        check(blank.mutagen() && blank.quality() == 0.0, "blank quality + mutagen is valid (quality 0)");
        check(Seed.parse(treated.toString()).orElseThrow().quality() == 1.5, "a treated seed keeps its quality through the text form");
        check(Seed.parse(" " + ok + "/3,-1 | " + ok + " | 1 | mutagen ").orElseThrow().mutagen(), "spaces around the word are fine, like the other parts");
        for (String junk : new String[] {ok + "|" + ok + "|1|Mutagen", ok + "|" + ok + "|1|mutagen2", ok + "|" + ok + "|1|", ok + "|" + ok + "|1|1", ok + "|" + ok + "|1|mutagen|x", ok + "|" + ok + "|1||"}) {
            check(Seed.parse(junk).isEmpty(), "junk 4th part does not parse: '" + junk + "'");
        }
        check(new Seed(def, Stats.ZERO, def, Stats.ZERO, 0.0, false).equals(new Seed(def, Stats.ZERO, def, Stats.ZERO, 0.0)), "the 5-argument constructor is the untreated seed");
        check(Seed.of(def, Stats.ZERO, def, Stats.ZERO, 0.0).mutagen() == false, "a seed dropped by a mating is untreated");

        // the chance: natural when untreated; at least the floor (the existing cap, 50%) when treated, tooltip/Calipers/command read the same number
        check(Seed.MUTAGEN_MUTATION_FLOOR == Genome.MAX_MUTATION_CHANCE && Seed.MUTAGEN_MUTATION_FLOOR == 0.5, "the floor is the existing 50% cap: no new number");
        near(plain.mutationChance(), Genome.mutationChance(def, def), 1e-12, "untreated: the parents' own chance (4% for average fertility)");
        near(treated.mutationChance(), 0.5, 1e-12, "treated: 50%");
        for (int i = 0; i < 50; i++) {
            Genome a = Genome.randomFounder(rng), b = Genome.randomFounder(rng);
            Seed x = new Seed(a, Stats.ZERO, b, Stats.ZERO, 0.0);
            check(x.withMutagen().mutationChance() >= x.mutationChance() && x.withMutagen().mutationChance() >= 0.5 - 1e-12, "treating never lowers the chance");
        }
        List<String> sum = plain.summary();
        check(sum.size() == 4 && !sum.contains("Treated with Mutagen"), "the untreated summary is unchanged (4 lines)");
        List<String> tsum = treated.summary();
        check(tsum.size() == 5 && tsum.contains("Mutation chance 50.0% per pup") && tsum.get(3).equals("Treated with Mutagen"), "the treated summary: 50% and a Treated line: " + tsum);
        check(Readout.seed(treated).contains("Treated with Mutagen") && !Readout.seed(plain).contains("Treated with Mutagen"), "the Calipers text shows the treatment");

        // the dice: pups of a treated seed mutate about half the time, plain ones about 4%; the phantom pup of an infusion follows the same chance
        int pups = 0, mutated = 0, plainPups = 0, plainMutated = 0;
        for (int i = 0; i < 600; i++) {
            for (Seed.Pup p : treated.hatch(0.0, 0, rng)) { pups++; if (p.mutated()) mutated++; }
            for (Seed.Pup p : plain.hatch(0.0, 0, rng)) { plainPups++; if (p.mutated()) plainMutated++; }
        }
        check(pups > 600 && plainPups > 600, "litters are not empty (no crowding)");
        near((double) mutated / pups, 0.5, 0.05, "treated: about half the pups mutate (" + mutated + " of " + pups + ")");
        near((double) plainMutated / plainPups, 0.04, 0.03, "untreated: about 4% (" + plainMutated + " of " + plainPups + ")");
        int phantomsChanged = 0, plainPhantomsChanged = 0;
        for (int i = 0; i < 1500; i++) {
            if (!Infusion.roll(treated, rng).genome().equals(def)) phantomsChanged++;
            if (!Infusion.roll(plain, rng).genome().equals(def)) plainPhantomsChanged++;
        }
        near(phantomsChanged / 1500.0, 0.5, 0.05, "infusion of a treated seed: about half the liquid pups carry a mutation (" + phantomsChanged + ")");
        near(plainPhantomsChanged / 1500.0, 0.04, 0.03, "infusion of a plain seed: about 4% (" + plainPhantomsChanged + ")");

        // the item: texture, client item, model, name, registration, tab, the tooltip class, the art script
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        check(pngSize(Files.readAllBytes(assets.resolve("textures/item/mutagen.png")))[0] == 16 && pngSize(Files.readAllBytes(assets.resolve("textures/item/mutagen.png")))[1] == 16, "mutagen texture is 16x16");
        check(compact(Files.readString(assets.resolve("items/mutagen.json"))).contains("\"model\":\"bunnidogs:item/mutagen\""), "mutagen client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/mutagen.json"))).contains("\"layer0\":\"bunnidogs:item/mutagen\""), "mutagen model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.mutagen\": \"Mutagen\""), "mutagen has its name");
        check(Files.readString(Path.of("art/gen_mutagen.py")).contains("mutagen") && Files.exists(Path.of("art/mutagen_preview.png")), "the art script and its preview exist");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("ITEMS.registerItem(\"mutagen\", MutagenItem::new,") && items.contains("props.stacksTo(16).rarity(Rarity.UNCOMMON)") && items.contains("event.accept(MUTAGEN.get())"), "registered (stack 16, uncommon) and in a creative tab");
        String mutagenItem = Files.readString(src.resolve("item/MutagenItem.java"));
        check(mutagenItem.contains("extends Item") && mutagenItem.contains("Hold a seed and this vial in the other hand, then use the seed"), "the Mutagen's tooltip explains the gesture");

        // the glue: the gesture is in SeedItem.use, before the juice branch; a treated seed refuses before anything is used up; every hatching goes through Seed
        String seedItem = Files.readString(src.resolve("item/SeedItem.java"));
        int iMut = seedItem.indexOf("juiceStack.is(ModItems.MUTAGEN.get())"), iJuice = seedItem.indexOf("juiceStack.is(ModItems.GUAVA_JUICE.get())");
        check(iMut > 0 && iJuice > iMut, "the Mutagen branch comes before the juice branch of SeedItem.use");
        check(seedItem.contains("hand == InteractionHand.MAIN_HAND && seed.isPresent() && juiceStack.is(ModItems.MUTAGEN.get())"), "main hand seed, Mutagen in the off hand");
        int iRefuse = seedItem.indexOf("if (seed.mutagen())"), iShrink = seedItem.indexOf("mutagenStack.shrink(1)");
        check(iRefuse > 0 && iShrink > iRefuse && seedItem.contains("create(seed.withMutagen())"), "an already treated seed refuses before the Mutagen is used up");
        String harvest = Files.readString(src.resolve("block/SeedCropHarvest.java")), commands = Files.readString(src.resolve("command/BunnidogsCommands.java")), dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        check(dog.contains("seed.hatch(extraQuality, nearby, rng)") && harvest.contains("Bunnidog.hatch(") && commands.contains("Bunnidog.hatch("), "crop, command and entity all hatch through Seed.hatch");
        check(Files.readString(src.resolve("genetics/Seed.java")).contains("Genome.breed(mother, father, rng, mutationChance())") && Files.readString(src.resolve("genetics/Infusion.java")).contains("seed.mutationChance()"), "hatch and infusion use the seed's chance");
        check(Files.readString(src.resolve("block/SeedCropBlockEntity.java")).contains("this.seedText = seed.toString()"), "a planted crop keeps the seed's full text, so the treatment survives planting");
    }

    /** Drops, chunk D6b (docs/DROPS_SPECS.md 2.4, 14): layer 3, the Mutant Marrow (one roll per expressed rare allele), its item, art and the Mutagen recipe. */
    static void marrow() throws Exception {
        System.out.println("marrow (D6b)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Drops.Context loot3 = new Drops.Context(Drops.Source.DEATH, false, true, false, 3, false);
        Drops.Context mobKill = new Drops.Context(Drops.Source.DEATH, false, false, false, 0, false);
        Random rng = new Random(66);

        // the rare alleles of the six genes; all six shown at once is the most a single animal can carry
        Genome six = Genome.DEFAULT;
        six = homozygous(six, Gene.EARS, "huge");
        six = homozygous(six, Gene.COAT, "spotted");
        six = homozygous(six, Gene.LEGS, "springy");
        six = homozygous(six, Gene.BUILD, "chonker");
        six = homozygous(six, Gene.TEMPERAMENT, "stoic");
        six = homozygous(six, Gene.FERTILITY, "swarm");
        check(Drops.rareCount(Genome.DEFAULT) == 0 && Drops.rareCount(six) == 6, "rare alleles shown: 0 for the default animal, 6 for the full mutant");
        check(Drops.rareCount(homozygous(Genome.DEFAULT, Gene.BUILD, "chonker")) == 1, "one rare gene counts once");
        // a rare allele is dominant: ONE carried copy already shows, so it counts
        Genome oneCopy = Genome.DEFAULT.with(Gene.EARS, 0, Gene.EARS.alleleNamed("huge").id());
        check(Drops.rareCount(oneCopy) == 1, "a single copy of a rare allele shows and counts");
        // wild alleles are not rare, and a hidden (recessive) one shows nothing
        check(Drops.rareCount(homozygous(Genome.DEFAULT, Gene.COAT, "snow")) == 0, "a wild allele (snow) is not a mutation");
        check(Drops.rareCount(homozygous(Genome.DEFAULT, Gene.FERTILITY, "starborn")) == 0, "starborn (wild, End) is not a mutation");
        check(Drops.MARROW_CHANCE == 0.25 && Drops.MARROW_ID.equals("bunnidogs:mutant_marrow"), "the spec's flat 25% and the item id");

        // the dice: six rare alleles, 25% each -> about 1.5 marrow a kill, never more than six, never from a pup / a mob kill
        int total = 0, most = 0, none = 0;
        for (int i = 0; i < 2000; i++) {
            int n = 0;
            for (Drops.Drop d : Drops.roll(six, Stats.ZERO, adult, rng)) {
                if (d.itemId().equals(Drops.MARROW_ID)) n += d.count();
            }
            total += n;
            most = Math.max(most, n);
            if (n == 0) none++;
        }
        near(total / 2000.0, 1.5, 0.1, "six rare alleles: about 1.5 marrow per kill (" + total + " in 2000)");
        near(none / 2000.0, Math.pow(0.75, 6), 0.03, "a full mutant gives no marrow about 18% of the time (" + none + " in 2000)");
        check(most <= 6, "never more than six marrow from one kill, got " + most);
        int loot = 0;
        for (int i = 0; i < 2000; i++) {
            for (Drops.Drop d : Drops.roll(six, Stats.ZERO, loot3, rng)) {
                if (d.itemId().equals(Drops.MARROW_ID)) loot += d.count();
            }
        }
        near(loot / 2000.0, 6 * 0.325, 0.12, "Looting III: 25% x 1.3 = 32.5% per roll, about 1.95 marrow (" + loot + " in 2000)");
        near(Drops.marrowChance(loot3), 0.325, 1e-9, "Looting III chance");
        near(Drops.marrowChance(adult), 0.25, 1e-9, "no Looting: 25%");
        for (int i = 0; i < 300; i++) {
            check(Drops.roll(six, Stats.ZERO, adult.withBaby(true), rng).stream().noneMatch(d -> d.itemId().equals(Drops.MARROW_ID)), "a pup never drops marrow");
            check(Drops.roll(six, Stats.ZERO, mobKill, rng).stream().noneMatch(d -> d.itemId().equals(Drops.MARROW_ID)), "no marrow without a player kill");
            check(Drops.roll(Genome.DEFAULT, Stats.ZERO, loot3, rng).stream().noneMatch(d -> d.itemId().equals(Drops.MARROW_ID)), "no marrow from an animal with no rare allele");
            check(Drops.roll(homozygous(Genome.DEFAULT, Gene.COAT, "snow"), Stats.ZERO, loot3, rng).stream().noneMatch(d -> d.itemId().equals(Drops.MARROW_ID)), "a wild-allele animal gives no marrow");
        }
        // the cap of the chance formula
        check(Drops.marrowChance(new Drops.Context(Drops.Source.DEATH, false, true, false, 100, false)) == Drops.CHANCE_CAP, "the chance is capped at 95%");

        // the tools: one rare allele -> "mutant marrow 0.25 (25% for one)", after the signatures; six -> a count over 1
        Genome chonker = homozygous(Genome.DEFAULT, Gene.BUILD, "chonker");
        String line = Drops.describe(chonker, Stats.ZERO, false);
        check(line.contains("mutant marrow 0.25 (25% for one)"), "Calipers line of a one-rare animal: " + line);
        Genome spotted = homozygous(Genome.DEFAULT, Gene.COAT, "spotted");
        String spottedLine = Drops.describe(spotted, Stats.ZERO, false);
        check(spottedLine.indexOf("cookie") > 0 && spottedLine.indexOf("cookie") < spottedLine.indexOf("mutant marrow"), "the marrow comes after the signatures: " + spottedLine);
        String sixLine = Drops.describe(six, Stats.ZERO, false);
        check(sixLine.contains("mutant marrow 1.5") && sixLine.endsWith("mutant marrow 1.5, tissue sample 4%"), "a full mutant shows an average count and the marrow is the last entry before the sample: " + sixLine);
        Drops.Expectation marrowE = Drops.expected(six, Stats.ZERO, adult).stream().filter(e -> e.itemId().equals(Drops.MARROW_ID)).findFirst().orElseThrow();
        near(marrowE.mean(), 1.5, 1e-9, "expected mean of six rolls");
        near(marrowE.chanceAtLeastOne(), 1.0 - Math.pow(0.75, 6), 1e-9, "chance of at least one marrow from six rolls");
        check(!marrowE.chanceOnly(), "marrow is shown as a count, not a single percentage (spec decision 26)");
        check(!Drops.describe(Genome.DEFAULT, Stats.ZERO, false).contains("marrow"), "the plain Calipers line has no marrow");
        check(!Drops.describe(chonker, Stats.ZERO, true).contains("marrow"), "a pup's line has no marrow");
        check(Drops.simulate(six, Stats.ZERO, adult, 50, rng).containsKey(Drops.MARROW_ID) && !Drops.simulate(Genome.DEFAULT, Stats.ZERO, adult, 50, rng).containsKey(Drops.MARROW_ID), "simulate lists the marrow only for a mutant");
        Map<String, Double> sim = Drops.simulate(six, Stats.ZERO, adult, 1000, rng);
        near(sim.get(Drops.MARROW_ID), 1.5, 0.15, "simulate over 1000 kills averages about 1.5 marrow");

        // the item: texture, client item, model, name, registration (RARE), tab (before the Mutagen), art script
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path data = Path.of("src/main/resources/data/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/mutant_marrow.png")));
        check(size[0] == 16 && size[1] == 16, "marrow texture is 16x16");
        check(compact(Files.readString(assets.resolve("items/mutant_marrow.json"))).contains("\"model\":\"bunnidogs:item/mutant_marrow\""), "marrow client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/mutant_marrow.json"))).contains("\"layer0\":\"bunnidogs:item/mutant_marrow\""), "marrow model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.mutant_marrow\": \"Mutant Marrow\""), "marrow has its name");
        check(Files.readString(Path.of("art/gen_marrow.py")).contains("mutant_marrow") && Files.exists(Path.of("art/marrow_preview.png")), "the art script and its preview exist");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("ITEMS.registerItem(\"mutant_marrow\", Item::new, props -> props.rarity(Rarity.RARE))"), "registered as a plain RARE item");
        check(items.indexOf("event.accept(FANG.get())") < items.indexOf("event.accept(MUTANT_MARROW.get())") && items.indexOf("event.accept(MUTANT_MARROW.get())") < items.indexOf("event.accept(MUTAGEN.get())"), "the marrow sits between the materials and the Mutagen in the Ingredients tab");
        check(!Drops.NOT_YET.contains(Drops.MARROW_ID), "the marrow is not on the not-yet list");

        // the recipe (the item ships with its use): marrow + fermented spider eye + glass bottle -> Mutagen, shapeless, one of each
        String recipe = compact(Files.readString(data.resolve("recipe/mutagen.json")));
        check(recipe.contains("\"type\":\"minecraft:crafting_shapeless\"")
                && recipe.contains("\"ingredients\":[\"bunnidogs:mutant_marrow\",\"minecraft:fermented_spider_eye\",\"minecraft:glass_bottle\"]")
                && recipe.contains("\"result\":{\"id\":\"bunnidogs:mutagen\"}"), "marrow + fermented spider eye + glass bottle -> one Mutagen");

        // the handler did not change
        String handler = Files.readString(src.resolve("drop/BunnidogDrops.java"));
        check(handler.contains("Drops.roll(") && handler.contains("BuiltInRegistries.ITEM.getValue(Identifier.parse(drop.itemId()))"), "the handler needs no change for new items");
    }

    /** Drops, chunk D7a (docs/DROPS_SPECS.md 2.5, decision 17): layer 4, the Tissue Sample (its payload, the roll, the off-hand rule, the Calipers reading and tooltip, the item). */
    static void tissue() throws Exception {
        System.out.println("tissue sample (D7a)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Drops.Context offHand = new Drops.Context(Drops.Source.DEATH, false, true, false, 0, true);
        Drops.Context mobKill = new Drops.Context(Drops.Source.DEATH, false, false, false, 0, true);
        Random rng = new Random(77);
        Genome g = homozygous(Genome.DEFAULT, Gene.COAT, "spotted");
        Stats st = Stats.of(12, -3, 0, 7, 40);

        // the payload: genome/stats text round-trips; the stats part may be missing; junk is refused
        Tissue t = new Tissue(g, st);
        check(Tissue.parse(t.toString()).orElseThrow().equals(t), "a tissue payload round-trips through its text: " + t);
        check(Tissue.parse(g.toString()).orElseThrow().equals(new Tissue(g, Stats.ZERO)), "a payload without stats means zero stats");
        check(Tissue.parse("nonsense").isEmpty() && Tissue.parse(null).isEmpty() && Tissue.parse(g + "/1,x").isEmpty(), "malformed payloads are refused");

        // the numbers: 4% without the Calipers in the off hand, certain with them, Looting changes nothing
        check(Drops.TISSUE_CHANCE == 0.04 && Drops.TISSUE_OFF_HAND_CHANCE == 1.0 && Drops.TISSUE_ID.equals("bunnidogs:tissue_sample"), "the spec's 4%, the certain off-hand roll and the item id");
        near(Drops.tissueChance(adult), 0.04, 1e-9, "no Calipers in the off hand: 4%");
        near(Drops.tissueChance(offHand), 1.0, 1e-9, "Calipers in the off hand: 100%");
        near(Drops.tissueChance(new Drops.Context(Drops.Source.DEATH, false, true, false, 3, false)), 0.04, 1e-9, "Looting does not change the sample chance");

        // the dice: certain with the off-hand Calipers, adult or pup, exactly one; about 4% without; never without a player kill
        int certain = 0, pupCertain = 0, plain = 0;
        for (int i = 0; i < 300; i++) {
            long n = Drops.roll(g, st, offHand, rng).stream().filter(d -> d.itemId().equals(Drops.TISSUE_ID)).count();
            long p = Drops.roll(g, st, offHand.withBaby(true), rng).stream().filter(d -> d.itemId().equals(Drops.TISSUE_ID)).count();
            certain += n == 1 ? 1 : 0;
            pupCertain += p == 1 ? 1 : 0;
        }
        check(certain == 300 && pupCertain == 300, "with the Calipers in the off hand every kill (adult or pup) gives exactly one sample: " + certain + " / " + pupCertain);
        for (int i = 0; i < 2000; i++) {
            for (Drops.Drop d : Drops.roll(g, st, adult, rng)) {
                if (d.itemId().equals(Drops.TISSUE_ID)) {
                    check(d.count() == 1, "one sample per kill");
                    plain++;
                }
            }
            check(Drops.roll(g, st, mobKill, rng).stream().noneMatch(d -> d.itemId().equals(Drops.TISSUE_ID)), "no sample without a player kill, even with the Calipers in the off hand");
        }
        near(plain / 2000.0, 0.04, 0.02, "without the Calipers about 4% of the kills give a sample (" + plain + " in 2000)");
        check(Drops.roll(g, st, adult.withBaby(true), new Random(1)).stream().allMatch(d -> !d.itemId().equals(Drops.PELT_NAMESPACE + "spotted_pelt")), "a pup still drops no pelt (the old layers are unchanged)");

        // the tools: "tissue sample 4%" last in the Calipers line (a percentage, like a signature), for pups too
        String line = Drops.describe(Genome.DEFAULT, Stats.ZERO, false);
        check(line.endsWith("tissue sample 4%"), "the Calipers line ends with the sample: " + line);
        check(Drops.describe(Genome.DEFAULT, Stats.ZERO, true).endsWith("tissue sample 4%"), "a pup's line shows the sample too");
        Drops.Expectation e = Drops.expected(Genome.DEFAULT, Stats.ZERO, adult).stream().filter(x -> x.itemId().equals(Drops.TISSUE_ID)).findFirst().orElseThrow();
        check(e.chanceOnly() && Math.abs(e.chanceAtLeastOne() - 0.04) < 1e-9, "the sample is one percentage");
        check(Drops.expected(Genome.DEFAULT, Stats.ZERO, mobKill).stream().noneMatch(x -> x.itemId().equals(Drops.TISSUE_ID)), "no sample line without a player kill");
        near(Drops.simulate(g, st, offHand, 200, rng).get(Drops.TISSUE_ID), 1.0, 1e-9, "simulate with the off-hand Calipers averages exactly one sample");

        // the Calipers read it
        List<String> read = Readout.tissue(t);
        check(read.get(0).equals("Tissue sample: " + Plain.look(g)) && read.get(read.size() - 1).equals("  Code: " + g), "headline in words, code last: " + read.get(0));
        check(read.size() == Gene.values().length + 4 && read.stream().anyMatch(l -> l.contains("Vitality +12")), "one line per gene, the stats and the drift: " + read.size() + " lines");

        // the item: texture, client item, model, name, registration, component, creative tab, art script
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/tissue_sample.png")));
        check(size[0] == 16 && size[1] == 16, "tissue sample texture is 16x16");
        check(compact(Files.readString(assets.resolve("items/tissue_sample.json"))).contains("\"model\":\"bunnidogs:item/tissue_sample\""), "client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/tissue_sample.json"))).contains("\"layer0\":\"bunnidogs:item/tissue_sample\""), "model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.tissue_sample\": \"Tissue Sample\""), "the sample has its name");
        check(Files.readString(Path.of("art/gen_tissue.py")).contains("tissue_sample") && Files.exists(Path.of("art/tissue_preview.png")), "the art script and its preview exist");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("COMPONENTS.registerComponentType(\"tissue_data\"") && items.contains("ITEMS.registerItem(\"tissue_sample\", TissueSampleItem::new"), "component and item are registered");
        check(items.contains("event.accept(TissueSampleItem.create(Genome.DEFAULT, Stats.ZERO), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)"), "one plain sample in the creative Ingredients tab");
        check(!Drops.NOT_YET.contains(Drops.TISSUE_ID), "the sample is not on the not-yet list");

        // the glue: the handler reads the OFF hand with the call that already compiled, and gives the stack the animal's genes
        String handler = Files.readString(src.resolve("drop/BunnidogDrops.java"));
        check(handler.contains("player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CALIPERS.get())"), "the handler looks for the Calipers in the killer's off hand");
        check(handler.contains("looting, calipersOffHand)") && !handler.contains("looting, false)"), "the flag is passed on instead of the old false");
        check(handler.contains("TissueSampleItem.create(dog.genome(), dog.stats())") && handler.indexOf("Drops.TISSUE_ID") < handler.indexOf("BuiltInRegistries.ITEM.getValue"), "the sample is made from the dying animal before the generic item lookup");
        String calipers = Files.readString(src.resolve("item/CalipersItem.java"));
        check(calipers.contains("TissueSampleItem.read(held)") && calipers.contains("showSample(player, tissue.get())"), "the Calipers read a sample in the other hand");
        check(calipers.contains("Off hand when you kill: a Tissue Sample"), "the Calipers tooltip explains the off-hand rule (decision 17)");
    }

    /** Drops, chunk D7b (docs/DROPS_SPECS.md 2.3, 4): Starlit Dust, the starborn signature row and the item that ripens a planted crop. */
    static void dust() throws Exception {
        System.out.println("starlit dust (D7b)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Random rng = new Random(88);

        // the row: starborn fertility, 1-2 dust, 25% base, scaled by Bloom; adults killed by a player only
        Genome star = homozygous(Genome.DEFAULT, Gene.FERTILITY, "starborn");
        Drops.Signature row = Drops.SIGNATURES.stream().filter(sg -> sg.itemId().equals("bunnidogs:starlit_dust")).findFirst().orElseThrow();
        check(row.min() == 1 && row.max() == 2 && Math.abs(row.base() - 0.25) < 1e-12 && row.key().equals("fertility:starborn"), "the dust row: starborn, 1-2, 25%");
        Drops.Expectation e = Drops.expected(star, Stats.ZERO, adult).stream().filter(x -> x.itemId().equals("bunnidogs:starlit_dust")).findFirst().orElseThrow();
        check(e.chanceOnly() && e.chanceAtLeastOne() > 0.2 && e.chanceAtLeastOne() < 0.3, "an unbred starborn animal shows the dust at about 25%: " + Drops.describe(e));
        check(Drops.expected(Genome.DEFAULT, Stats.ZERO, adult).stream().noneMatch(x -> x.itemId().equals("bunnidogs:starlit_dust")), "an ordinary animal shows no dust");
        int got = 0;
        for (int i = 0; i < 2000; i++) {
            for (Drops.Drop d : Drops.roll(star, Stats.ZERO, adult, rng)) {
                if (d.itemId().equals("bunnidogs:starlit_dust")) {
                    check(d.count() >= 1 && d.count() <= 2, "one or two dust");
                    got++;
                }
            }
            check(Drops.roll(star, Stats.ZERO, adult.withBaby(true), rng).stream().noneMatch(d -> d.itemId().equals("bunnidogs:starlit_dust")), "a pup gives no dust");
        }
        near(got / 2000.0, e.chanceAtLeastOne(), 0.04, "the dice follow the shown chance (" + got + " in 2000)");

        // the item: texture, client item, model, name, registration, tab, art script
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/starlit_dust.png")));
        check(size[0] == 16 && size[1] == 16, "starlit dust texture is 16x16");
        check(compact(Files.readString(assets.resolve("items/starlit_dust.json"))).contains("\"model\":\"bunnidogs:item/starlit_dust\""), "client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/starlit_dust.json"))).contains("\"layer0\":\"bunnidogs:item/starlit_dust\""), "model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.starlit_dust\": \"Starlit Dust\""), "the dust has its name");
        check(Files.readString(Path.of("art/gen_dust.py")).contains("starlit_dust") && Files.exists(Path.of("art/dust_preview.png")), "the art script and its preview exist");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("ITEMS.registerItem(\"starlit_dust\", StarlitDustItem::new") && items.contains("event.accept(STARLIT_DUST.get())"), "registered and in the creative tab");

        // the glue: a ripe crop and any other block pass, an unripe crop jumps to the last age, one dust is used up unless creative
        String item = Files.readString(src.resolve("item/StarlitDustItem.java"));
        check(item.contains("state.getBlock() instanceof SeedCropBlock") && item.contains("SeedCropBlock.isRipe(state)") && item.indexOf("return InteractionResult.PASS") < item.indexOf("SeedCropBlock.ripen(level, pos, state)"), "only an unripe crop is taken, the rest passes first");
        check(item.contains("player == null || !player.hasInfiniteMaterials()") && item.contains("context.getItemInHand().shrink(1)"), "one dust is used up, not in creative");
        check(item.indexOf("if (!level.isClientSide())") < item.indexOf("SeedCropBlock.ripen(level, pos, state)"), "the crop changes on the server only");
        String block = Files.readString(src.resolve("block/SeedCropBlock.java"));
        check(block.contains("level.setBlock(pos, state.setValue(AGE, MAX_AGE), 2);"), "ripen sets the last age and leaves the block entity alone");
    }

    /** Drops, chunk D8d (docs/DROPS_SPECS.md 2.6): layer 5, the five legends (the curve, the dice, the filters, the tools' text, the five items). */
    static void legends() throws Exception {
        System.out.println("legends (D8d)");
        Drops.Context adult = Drops.Context.PLAIN_ADULT;
        Drops.Context loot3 = new Drops.Context(Drops.Source.DEATH, false, true, false, 3, false);
        Drops.Context mobKill = new Drops.Context(Drops.Source.DEATH, false, false, false, 0, false);
        Random rng = new Random(808);
        String[] ids = {"bunnidogs:titan_haunch", "bunnidogs:gilded_foot", "bunnidogs:moon_sinew", "bunnidogs:listening_shell", "bunnidogs:everbloom"};

        // the table: one legend per stat, in the stat order
        check(Drops.LEGENDS.size() == 5, "five legends");
        for (int i = 0; i < 5; i++) {
            check(Drops.LEGENDS.get(i).stat() == Stat.values()[i] && Drops.LEGENDS.get(i).itemId().equals(ids[i]), "legend " + i + " belongs to " + Stat.values()[i] + " and is " + ids[i]);
        }

        // the curve of spec 2.6: 0 below 100, 2% at 100, 4% at 200, 8% at 400, 15% from 750 on
        near(Drops.legendChance(-50), 0.0, 0.0, "a negative stat gives no legend");
        near(Drops.legendChance(0), 0.0, 0.0, "no points, no legend");
        near(Drops.legendChance(99), 0.0, 0.0, "99 points is still nothing");
        near(Drops.legendChance(100), 0.02, 1e-9, "100 points: 2%");
        near(Drops.legendChance(200), 0.04, 1e-9, "200 points: 4%");
        near(Drops.legendChance(400), 0.08, 1e-9, "400 points: 8%");
        near(Drops.legendChance(750), 0.15, 1e-9, "750 points: 15%");
        near(Drops.legendChance(749), 0.1498, 1e-9, "749 points: just under the cap");
        near(Drops.legendChance(900_000), 0.15, 1e-9, "the cap holds: 15%");

        // the dice: Vitality 400 -> about 8% titan haunch, nothing else; a stat under 100 rolls nothing
        Stats vit = Stats.of(400, 99, 0, -20, 50);
        int[] got = new int[5];
        int n = 4000;
        for (int i = 0; i < n; i++) {
            for (Drops.Drop d : Drops.roll(Genome.DEFAULT, vit, adult, rng)) {
                for (int k = 0; k < 5; k++) {
                    if (d.itemId().equals(ids[k])) {
                        check(d.count() == 1, "a legend comes one at a time");
                        got[k]++;
                    }
                }
            }
        }
        near(got[0] / (double) n, 0.08, 0.02, "Vitality 400: about 8% titan haunch (" + got[0] + " in " + n + ")");
        check(got[1] == 0 && got[2] == 0 && got[3] == 0 && got[4] == 0, "the stats under 100 (99, 0, -20, 50) give no legend");

        // five stats at 750 roll five separate 15% dice
        Stats all = Stats.of(750, 750, 750, 750, 750);
        int[] five = new int[5];
        int both = 0;
        for (int i = 0; i < n; i++) {
            int here = 0;
            for (Drops.Drop d : Drops.roll(Genome.DEFAULT, all, adult, rng)) {
                for (int k = 0; k < 5; k++) {
                    if (d.itemId().equals(ids[k])) {
                        five[k]++;
                        here++;
                    }
                }
            }
            if (here >= 2) both++;
        }
        for (int k = 0; k < 5; k++) {
            near(five[k] / (double) n, 0.15, 0.025, "each stat rolls its own 15%: " + ids[k] + " (" + five[k] + " in " + n + ")");
        }
        check(both > 0, "a line bred in several stats can drop two legends at once (" + both + " kills)");

        // the filters: adult + player kill only; Looting changes nothing
        for (int i = 0; i < 400; i++) {
            check(Drops.roll(Genome.DEFAULT, all, adult.withBaby(true), rng).stream().noneMatch(d -> d.itemId().startsWith("bunnidogs:") && java.util.Arrays.asList(ids).contains(d.itemId())), "a pup gives no legend");
            check(Drops.roll(Genome.DEFAULT, all, mobKill, rng).stream().noneMatch(d -> java.util.Arrays.asList(ids).contains(d.itemId())), "no legend without a player kill");
        }
        check(!Drops.givesLegends(adult.withBaby(true)) && !Drops.givesLegends(mobKill) && Drops.givesLegends(adult) && Drops.givesLegends(loot3), "givesLegends: adult and player kill");
        Drops.Expectation plain = Drops.expected(Genome.DEFAULT, vit, adult).stream().filter(e -> e.itemId().equals(ids[0])).findFirst().orElseThrow();
        Drops.Expectation looted = Drops.expected(Genome.DEFAULT, vit, loot3).stream().filter(e -> e.itemId().equals(ids[0])).findFirst().orElseThrow();
        near(plain.chanceAtLeastOne(), looted.chanceAtLeastOne(), 0.0, "Looting III does not change a legend's chance");
        check(plain.chanceOnly() && plain.label().equals("titan haunch"), "a legend is shown as one percentage");

        // the tools: after the Tissue Sample, only the legends the animal can drop, nothing for a plain animal or a pup
        String line = Drops.describe(Genome.DEFAULT, vit, false);
        check(line.endsWith("tissue sample 4%, titan haunch 8%"), "Calipers line of a Vitality 400 animal: " + line);
        String full = Drops.describe(Genome.DEFAULT, all, false);
        check(full.endsWith("tissue sample 4%, titan haunch 15%, gilded foot 15%, moon sinew 15%, listening shell 15%, everbloom 15%"), "all five legends, in stat order, after the sample: " + full);
        check(Drops.describe(Genome.DEFAULT, Stats.ZERO, false).endsWith("tissue sample 4%"), "the plain animal's line still ends with the sample");
        check(!Drops.describe(Genome.DEFAULT, all, true).contains("titan haunch"), "a pup's line has no legend");
        Map<String, Double> sim = Drops.simulate(Genome.DEFAULT, vit, adult, 2000, rng);
        check(sim.containsKey(ids[0]) && !sim.containsKey(ids[1]) && !Drops.simulate(Genome.DEFAULT, Stats.ZERO, adult, 20, rng).containsKey(ids[0]), "simulate lists a legend only when the animal can drop it");
        near(sim.get(ids[0]), 0.08, 0.02, "simulate over 2000 kills averages about 0.08 titan haunch");

        // the items: texture, client item, model, name, registration (EPIC, plain), tab (after the dust), the art script
        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        String[] names = {"titan_haunch", "gilded_foot", "moon_sinew", "listening_shell", "everbloom"};
        String[] titles = {"Titan Haunch", "Gilded Foot", "Moon Sinew", "Listening Shell", "Everbloom"};
        String[] fields = {"TITAN_HAUNCH", "GILDED_FOOT", "MOON_SINEW", "LISTENING_SHELL", "EVERBLOOM"};
        int last = items.indexOf("event.accept(STARLIT_DUST.get())");
        for (int k = 0; k < 5; k++) {
            int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/" + names[k] + ".png")));
            check(size[0] == 16 && size[1] == 16, names[k] + " texture is 16x16");
            check(compact(Files.readString(assets.resolve("items/" + names[k] + ".json"))).contains("\"model\":\"bunnidogs:item/" + names[k] + "\""), names[k] + " client item points at its model");
            check(compact(Files.readString(assets.resolve("models/item/" + names[k] + ".json"))).contains("\"layer0\":\"bunnidogs:item/" + names[k] + "\""), names[k] + " model points at its texture");
            check(lang.contains("\"item.bunnidogs." + names[k] + "\": \"" + titles[k] + "\""), names[k] + " has its name");
            check(items.contains("ITEMS.registerItem(\"" + names[k] + "\", Item::new, props -> props.rarity(Rarity.EPIC))"), names[k] + " is a plain EPIC item");
            int at = items.indexOf("event.accept(" + fields[k] + ".get())");
            check(at > last, names[k] + " is in the creative tab after the dust");
            last = at;
        }
        check(Files.readString(Path.of("art/gen_legends.py")).contains("everbloom") && Files.exists(Path.of("art/legends_preview.png")), "the art script and its preview exist");
        check(!Drops.NOT_YET.contains(ids[0]), "the legends are not on the not-yet list");

        // the handler did not change: any item id becomes a stack, a missing item is skipped with a log line
        String handler = Files.readString(src.resolve("drop/BunnidogDrops.java"));
        check(handler.contains("Drops.roll(") && handler.contains("BuiltInRegistries.ITEM.getValue(Identifier.parse(drop.itemId()))"), "the handler needs no change for the legends");
    }

    /** Drops, chunk D8e (docs/DROPS_SPECS.md section 7): the Worn Ear Tag (the code it keeps, the item's files, the drop hook, the separation from the Ear Tag). */
    static void wornTag() throws Exception {
        System.out.println("worn ear tag (D8e)");
        // the code the item keeps is read through EarTag.parse: a real code survives, anything else is "blank"
        String real = EarTag.code(0x1234L, 0x5678L);
        check(EarTag.parse(real).equals(Optional.of(real)), "a code an animal wore reads back unchanged: " + real);
        check(EarTag.parse("BD-3F7A2C").isPresent(), "the creative sample code is a valid code");
        check(EarTag.parse("bd-3f7a2c").isEmpty() && EarTag.parse("BD-3F7A2").isEmpty() && EarTag.parse("").isEmpty() && EarTag.parse(null).isEmpty(), "a malformed or empty code reads as blank");

        Path assets = Path.of("src/main/resources/assets/bunnidogs");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        int[] size = pngSize(Files.readAllBytes(assets.resolve("textures/item/worn_ear_tag.png")));
        check(size[0] == 16 && size[1] == 16, "worn_ear_tag texture is 16x16");
        check(!java.util.Arrays.equals(Files.readAllBytes(assets.resolve("textures/item/worn_ear_tag.png")), Files.readAllBytes(assets.resolve("textures/item/ear_tag.png"))), "the worn tag does not look like the fresh one");
        check(compact(Files.readString(assets.resolve("items/worn_ear_tag.json"))).contains("\"model\":\"bunnidogs:item/worn_ear_tag\""), "client item points at its model");
        check(compact(Files.readString(assets.resolve("models/item/worn_ear_tag.json"))).contains("\"layer0\":\"bunnidogs:item/worn_ear_tag\""), "model points at its texture");
        check(Files.readString(assets.resolve("lang/en_us.json")).contains("\"item.bunnidogs.worn_ear_tag\": \"Worn Ear Tag\""), "the item has its name");
        check(Files.readString(Path.of("art/gen_worn_ear_tag.py")).contains("worn_ear_tag.png") && Files.exists(Path.of("art/worn_ear_tag_preview.png")), "the art script and its preview exist");

        // registration: a string component like the Tissue Sample's, an UNCOMMON item, one creative sample in the Tools tab right after the Ear Tag
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("COMPONENTS.registerComponentType(\"worn_tag_code\"") && items.contains("persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)"), "the code is a saved and synced string component");
        check(items.contains("ITEMS.registerItem(\"worn_ear_tag\", WornEarTagItem::new,\n            props -> props.rarity(Rarity.UNCOMMON))"), "the worn tag is an UNCOMMON item of its own class");
        int ear = items.indexOf("event.accept(EAR_TAG.get())");
        int worn = items.indexOf("event.accept(WornEarTagItem.create(\"BD-3F7A2C\"), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)");
        check(ear > 0 && worn > ear, "one creative sample, right after the Ear Tag");

        // the item: its own Item (so the Ear Tag's right-click on an animal never sees it), reads and writes the component
        String item = Files.readString(src.resolve("item/WornEarTagItem.java"));
        check(item.contains("class WornEarTagItem extends Item ") && !item.contains("extends EarTagItem"), "the worn tag is NOT an EarTagItem: it cannot be clipped onto an animal");
        check(item.contains("stack.set(ModItems.WORN_TAG_CODE.get(), code)") && item.contains("EarTag.parse(text)"), "create writes the code, read goes through EarTag.parse");
        String bunnidog = Files.readString(src.resolve("entity/Bunnidog.java"));
        check(bunnidog.contains("held.getItem() instanceof EarTagItem") && !bunnidog.contains("WornEarTagItem"), "the animal only reacts to the real Ear Tag");

        // the drop hook: after the rolled drops, certain (no age, no killer, no Looting condition on that line), the animal's own code
        String handler = Files.readString(src.resolve("drop/BunnidogDrops.java"));
        int roll = handler.indexOf("Drops.roll(");
        int hook = handler.indexOf("dog.earTag().ifPresent(code -> event.getDrops().add(");
        check(roll > 0 && hook > roll, "the worn tag is added after the rolled drops");
        String line = hook > 0 ? handler.substring(hook, handler.indexOf('\n', hook)) : "";
        check(line.contains("WornEarTagItem.create(code)") && !line.contains("isBaby") && !line.contains("isRecentlyHit") && !line.contains("looting"), "the hook is certain: no age, killer or Looting in it");
        check(handler.indexOf("GameRules.MOB_DROPS") > 0 && handler.indexOf("GameRules.MOB_DROPS") < hook, "the mobDrops game rule is still checked before it");
        check(!Drops.NOT_YET.contains("bunnidogs:worn_ear_tag"), "the worn tag is not a rolled drop (the engine is unchanged)");
    }

    /** Genetics Table, chunk T1 (docs/TABLE_SPECS.md): the click rules (the whole truth table), the block's files and the glue. */
    static void geneticsTable() throws Exception {
        System.out.println("genetics table (T1, T2)");
        // the click rules: every combination of the five facts (T2: two slots)
        int outcomes = 0;
        for (int bits = 0; bits < 32; bits++) {
            boolean sample = (bits & 1) != 0, empty = (bits & 2) != 0, sneak = (bits & 4) != 0, aFull = (bits & 8) != 0, bFull = (bits & 16) != 0;
            if (sample && empty) {
                continue;                                       // a hand cannot hold a sample and nothing
            }
            TableUse.Action act = TableUse.decide(sample, empty, sneak, aFull, bFull);
            int slot = TableUse.slot(act, aFull, bFull);
            outcomes++;
            if (sample) {
                check(act == (aFull && bFull ? TableUse.Action.SWAP : TableUse.Action.STORE), "a sample in hand " + (aFull && bFull ? "swaps" : "goes on") + " (A " + aFull + ", B " + bFull + ")");
                check(slot == (!aFull ? TableUse.SLOT_A : TableUse.SLOT_B), "a sample goes to the first free slot, a swap replaces B (A " + aFull + ", B " + bFull + ")");
            } else if (!aFull && !bFull) {
                check(act == TableUse.Action.NOTHING && slot == TableUse.NO_SLOT, "a click on an empty table does nothing without a sample");
            } else {
                check(act == (empty && sneak ? TableUse.Action.TAKE : TableUse.Action.READ), "a loaded table: sneak + empty hands take, any other click reads (empty " + empty + ", sneak " + sneak + ")");
                check(act == TableUse.Action.READ ? slot == TableUse.NO_SLOT : slot == (bFull ? TableUse.SLOT_B : TableUse.SLOT_A), "taking back empties B first, then A (A " + aFull + ", B " + bFull + ")");
            }
        }
        check(outcomes == 24, "24 possible situations were checked, got " + outcomes);
        check(TableUse.decide(false, true, true, true, false) == TableUse.Action.TAKE && TableUse.decide(false, true, false, false, true) == TableUse.Action.READ, "empty hand: sneak takes, plain click reads (one slot full)");
        check(TableUse.decide(false, false, false, true, true) == TableUse.Action.READ, "the Calipers (or any item) in hand read the samples");
        check(TableUse.slot(TableUse.Action.STORE, false, false) == TableUse.SLOT_A && TableUse.slot(TableUse.Action.STORE, true, false) == TableUse.SLOT_B, "A fills first, then B");
        check(TableUse.slot(TableUse.Action.TAKE, true, true) == TableUse.SLOT_B && TableUse.slot(TableUse.Action.TAKE, true, false) == TableUse.SLOT_A, "taking back empties B first, then A");

        Path res = Path.of("src/main/resources");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String model = Files.readString(res.resolve("assets/bunnidogs/models/block/genetics_table.json"));
        for (String tex : new String[] {"genetics_table", "genetics_table_top", "genetics_table_metal"}) {
            check(model.contains("\"bunnidogs:block/" + tex + "\""), "the model uses the texture " + tex);
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(res.resolve("assets/bunnidogs/textures/block/" + tex + ".png").toFile());
            check(img != null && img.getWidth() == 16 && img.getHeight() == 16, tex + ".png is 16x16");
        }
        check(model.contains("\"minecraft:block/block\"") && model.split("\"from\"", -1).length - 1 == 7, "the model has 7 elements (4 legs, the top, the scope base and tube)");
        check(Files.readString(res.resolve("assets/bunnidogs/blockstates/genetics_table.json")).contains("bunnidogs:block/genetics_table"), "blockstate points at the model");
        check(Files.readString(res.resolve("assets/bunnidogs/items/genetics_table.json")).contains("bunnidogs:block/genetics_table"), "item definition points at the block model");
        check(Files.readString(res.resolve("data/bunnidogs/loot_table/blocks/genetics_table.json")).contains("\"bunnidogs:genetics_table\""), "the loot table drops the table itself");
        check(Files.readString(res.resolve("data/minecraft/tags/block/mineable/axe.json")).contains("bunnidogs:genetics_table"), "the table is axe-mineable");
        String raw = Files.readString(res.resolve("data/bunnidogs/recipe/genetics_table.json"));     // raw text: the pattern row "P P" has a space (compact would eat it)
        String recipe = compact(raw);
        check(recipe.contains("\"minecraft:crafting_shaped\"") && raw.contains("\"WCW\"") && raw.contains("\"PPP\"") && raw.contains("\"P P\"") && recipe.contains("\"W\":\"bunnidogs:whisker\"")
                && recipe.contains("\"C\":\"minecraft:copper_ingot\"") && recipe.contains("\"P\":\"#minecraft:planks\"") && recipe.contains("\"result\":{\"id\":\"bunnidogs:genetics_table\"}"), "recipe: 2 whiskers, a copper ingot and 5 planks");
        String lang = Files.readString(res.resolve("assets/bunnidogs/lang/en_us.json"));
        check(lang.contains("\"block.bunnidogs.genetics_table\": \"Genetics Table\"") && lang.contains("\"item.bunnidogs.genetics_table\": \"Genetics Table\""), "block and item names");
        check(Files.readString(Path.of("art/gen_table.py")).contains("genetics_table_top") && Files.exists(Path.of("art/table_preview.png")) && Files.exists(Path.of("docs/TABLE_SPECS.md")), "the art script, its preview and the plan exist");

        // the glue
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("BLOCKS.registerBlock(\"genetics_table\", GeneticsTableBlock::new,") && blocks.contains("BLOCK_ENTITIES.register(\"genetics_table\", () -> new BlockEntityType<>(GeneticsTableBlockEntity::new, GENETICS_TABLE.get()))"), "the block and its block entity are registered");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        int ped = items.indexOf("event.accept(TROPHY_PEDESTAL.get())");
        int tab = items.indexOf("event.accept(GENETICS_TABLE.get())");
        check(items.contains("ITEMS.registerSimpleBlockItem(ModBlocks.GENETICS_TABLE)") && ped > 0 && tab > ped, "the block item exists and sits in the Tools tab after the pedestal");
        String block = Files.readString(src.resolve("block/GeneticsTableBlock.java"));
        check(block.contains("implements EntityBlock") && block.contains("TableUse.decide(readable, inHand.isEmpty(), player.isShiftKeyDown(), aFull, bFull)") && block.contains("int slot = TableUse.slot(action, aFull, bFull);"), "the block asks TableUse what a click does and on which slot");
        check(block.contains("TissueSampleItem.read(inHand).isPresent()") && block.contains("ModNetwork.sendTable(serverPlayer, sheet)") && block.contains("new TableSheet(TissueSampleItem.read(table.item(TableUse.SLOT_A)), TissueSampleItem.read(table.item(TableUse.SLOT_B)))"), "only a readable sample goes on, reading opens the table window with both slots");
        check(block.indexOf("level.isClientSide()") < block.indexOf("switch (action)"), "the server does the work, the client only answers the click");
        String be = Files.readString(src.resolve("block/GeneticsTableBlockEntity.java"));
        check(be.contains("output.store(TAG_A, ItemStack.CODEC, this.sampleA)") && be.contains("output.store(TAG_B, ItemStack.CODEC, this.sampleB)") && be.contains("input.read(TAG_A, ItemStack.CODEC).or(() -> input.read(TAG_T1, ItemStack.CODEC))") && be.contains("TAG_T1 = \"Item\"")
                && be.contains("input.read(TAG_B, ItemStack.CODEC)") && be.contains("ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag)") && be.contains("preRemoveSideEffects") && be.split("Block.popResource", -1).length - 1 == 2,
                "the block entity saves both slots (a T1 table loads its sample into A), syncs, and drops both samples");

        // T2: the table window (pure TableSheet) and the glue around it
        com.bunnidogs.mod.layout.Metrics m = com.bunnidogs.mod.layout.Metrics.ESTIMATE;
        java.util.function.Function<com.bunnidogs.mod.layout.Page, java.util.List<String>> dump = pg -> {
            java.util.List<String> out = new java.util.ArrayList<>();
            for (com.bunnidogs.mod.layout.Page.Text t : pg.texts()) {
                out.add(t.y() + "|" + t.text());
            }
            return out;
        };
        Tissue ta = new Tissue(g("22-32-00-10-00-01"), Stats.of(3, -1, 0, 2, 12)), tb = new Tissue(g("11-00-00-11-00-00"), Stats.ZERO);
        com.bunnidogs.mod.layout.TableSheet both = new com.bunnidogs.mod.layout.TableSheet(Optional.of(ta), Optional.of(tb));
        com.bunnidogs.mod.layout.TableSheet onlyA = new com.bunnidogs.mod.layout.TableSheet(Optional.of(ta), Optional.empty());
        com.bunnidogs.mod.layout.TableSheet onlyB = new com.bunnidogs.mod.layout.TableSheet(Optional.empty(), Optional.of(tb));
        check(both.spreadCount() == 5 && onlyA.spreadCount() == 1 && onlyB.spreadCount() == 1 && new com.bunnidogs.mod.layout.TableSheet(Optional.empty(), Optional.empty()).spreadCount() == 0, "one spread per sample on the table, plus the forecast when both are there (T3)");
        check(both.slotOf(0) == 0 && both.slotOf(1) == 1 && both.slotOf(9) == 1 && both.slotOf(-3) == 0 && onlyA.slotOf(0) == 0 && onlyA.slotOf(1) == 0 && onlyB.slotOf(0) == 1, "spread n shows slot n; with one sample, that sample's slot");
        check(both.sample(0).equals(ta) && both.sample(1).equals(tb) && onlyB.sample(0).equals(tb), "each spread shows its own sample");
        // the wire text round-trips; an empty slot is nothing; junk and an empty table open nothing
        check(both.toString().equals(ta + "|" + tb) && com.bunnidogs.mod.layout.TableSheet.parse(both.toString()).equals(Optional.of(both)), "both samples round-trip: " + both);
        check(onlyA.toString().equals(ta + "|") && com.bunnidogs.mod.layout.TableSheet.parse(onlyA.toString()).equals(Optional.of(onlyA)), "only A round-trips");
        check(onlyB.toString().equals("|" + tb) && com.bunnidogs.mod.layout.TableSheet.parse(onlyB.toString()).equals(Optional.of(onlyB)), "only B round-trips");
        check(com.bunnidogs.mod.layout.TableSheet.parse("|").isEmpty() && com.bunnidogs.mod.layout.TableSheet.parse("junk").isEmpty() && com.bunnidogs.mod.layout.TableSheet.parse(null).isEmpty() && com.bunnidogs.mod.layout.TableSheet.parse("x|y").isEmpty(), "nothing readable opens no window");
        // the sheets: the sample sheet with the slot in the header
        for (int n = 0; n < 2; n++) {
            com.bunnidogs.mod.layout.Page.Spread sp = both.spread(n, m);
            java.util.List<String> l = dump.apply(sp.left()), r = dump.apply(sp.right());
            check(l.contains("0|" + (n == 0 ? "Table: sample A" : "Table: sample B")) && l.stream().noneMatch(x -> x.equals("0|adult") || x.equals("0|pup") || x.contains("#") || x.contains("generation")) && sp.left().pairs().size() == Gene.COUNT,
                    "table spread " + n + " header, no adult / pup, no # or generation: " + l);
            check(r.contains("0|STATS") && r.contains("135|A sample holds only") && r.stream().noneMatch(x -> x.contains("Mom") || x.contains("Dad")), "table spread " + n + " right page says what a sample does not know");
            check(sp.left().links().isEmpty() && sp.right().links().isEmpty() && sp.left().hotspots().stream().anyMatch(h -> h.y() == 11 && h.text().startsWith("A Tissue Sample holds the genes")), "table spread " + n + ": no links, the portrait explains the sample");
        }
        check(both.spread(0, m).left().portrait().coat().equals("spotted") && both.spread(1, m).left().portrait().coat().equals("tan"), "each spread's portrait is that sample's coat");
        check(dump.apply(both.spread(0, m).right()).contains("9|+3") && dump.apply(both.spread(1, m).right()).contains("9|0"), "each spread shows its own stats");
        check(dump.apply(onlyB.spread(0, m).left()).contains("0|Table: sample B") && onlyB.entry(0).genome().equals(tb.genome()), "a lone sample on B is headed B");
        check(dump.apply(com.bunnidogs.mod.layout.SheetLayout.sample(ta, m).left()).contains("0|Tissue sample"), "the Calipers' sample window keeps its own header");

        // the wire and the screen
        String pay = Files.readString(src.resolve("network/TablePayload.java"));
        check(pay.contains("record TablePayload(String table) implements CustomPacketPayload") && pay.contains("Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, \"table\")") && pay.contains("ByteBufCodecs.STRING_UTF8.map(TablePayload::new, TablePayload::table)"), "the table payload is one string, like the sample payload");
        String net = Files.readString(src.resolve("network/ModNetwork.java"));
        check(net.contains("registrar.playToClient(TablePayload.TYPE, TablePayload.STREAM_CODEC,") && net.contains("TableSheet.parse(payload.table()).ifPresent(ClientScreens::openTable)")
                && net.contains("PacketDistributor.sendToPlayer(player, new TablePayload(table.toString()))"), "the channel registers the table payload (versioned channel) and the server sends the table's text");
        check(Files.readString(src.resolve("client/ClientScreens.java")).contains("client.gui.setScreen(new PedigreeScreen(table, splicing))"), "the client door opens the table screen");
        String screen = Files.readString(src.resolve("client/screen/PedigreeScreen.java"));
        check(screen.contains("public PedigreeScreen(TableSheet table)") && screen.contains("return this.table.spreadCount();") && screen.contains("this.table.spread(this.spread, text -> this.font.width(text))") && screen.contains("buildPortrait(this.table.entry(this.spread))")
                && screen.split("this\\.table = null;", -1).length - 1 == 4 && screen.contains("return this.single != null || this.sample != null;"), "the screen has the table constructor, pages over the samples and builds each portrait; it is NOT a one-sheet window (it has buttons)");
    }

    /** Genetics Table, chunk T3 (docs/TABLE_SPECS.md): the forecast maths (Cross), its layout (ForecastSheet) and its place in the table window. */
    static void forecast() throws Exception {
        System.out.println("forecast (T3)");
        Tissue ta = new Tissue(g("22-32-00-10-00-01"), Stats.of(3, -1, 0, 2, 12)), tb = new Tissue(g("11-00-00-11-00-00"), Stats.ZERO);
        Cross x = new Cross(ta, tb);
        java.util.function.Function<List<Cross.Outcome>, String> text = list -> {
            StringBuilder sb = new StringBuilder();
            for (Cross.Outcome o : list) {
                sb.append(sb.length() == 0 ? "" : ", ").append(o.allele().name()).append(' ').append(Math.round(o.chance() * 100));
            }
            return sb.toString();
        };
        // exact odds, worked by hand: A short/short x B long/long -> long; A spotted/brown x B tan/tan -> spotted or tan; ...
        check(text.apply(x.looks(Gene.EARS)).equals("long 100"), "ears: " + text.apply(x.looks(Gene.EARS)));
        check(text.apply(x.looks(Gene.COAT)).equals("spotted 50, tan 50"), "coat: " + text.apply(x.looks(Gene.COAT)));
        check(text.apply(x.looks(Gene.LEGS)).equals("standard 100"), "legs: " + text.apply(x.looks(Gene.LEGS)));
        check(text.apply(x.looks(Gene.BUILD)).equals("average 50, stocky 50"), "build: a carried stronger gene shows half the time: " + text.apply(x.looks(Gene.BUILD)));
        check(text.apply(x.looks(Gene.TEMPERAMENT)).equals("loyal 100") && text.apply(x.looks(Gene.FERTILITY)).equals("average 100"), "temperament and fertility");
        // a 3-way split: A (standard + short) x B (long + long) is long 50%, standard 50%; A (long + short) x B (standard + short) -> three looks
        Tissue earsLongShort = new Tissue(g("12-00-00-00-00-00"), Stats.ZERO), earsStdShort = new Tissue(g("02-00-00-00-00-00"), Stats.ZERO);
        check(text.apply(new Cross(earsLongShort, earsStdShort).looks(Gene.EARS)).equals("standard 50, long 25, short 25"), "three looks: " + text.apply(new Cross(earsLongShort, earsStdShort).looks(Gene.EARS)));
        // the odds are the game's: every pair of founders of every lineage, every gene, against the four real pup genomes
        java.util.Random rng = new java.util.Random(37);
        List<Genome> pool = new java.util.ArrayList<>();
        for (int i = 0; i < 40; i++) {
            pool.add(Genome.randomFounder(rng));
            for (Lineage l : Lineage.values()) {
                pool.add(Genome.randomFounder(l, rng));
            }
        }
        int pairs = 0;
        for (int i = 0; i < pool.size(); i++) {
            Genome ga = pool.get(i), gb = pool.get((i * 7 + 3) % pool.size());
            Cross c = new Cross(new Tissue(ga, Stats.ZERO), new Tissue(gb, Stats.ZERO));
            pairs++;
            for (Gene gene : Gene.values()) {
                int[] counts = new int[16];
                for (int s1 = 0; s1 < 2; s1++) {
                    for (int s2 = 0; s2 < 2; s2++) {
                        Genome pup = Genome.DEFAULT.with(gene, 0, ga.alleleId(gene, s1)).with(gene, 1, gb.alleleId(gene, s2));
                        counts[pup.expressed(gene).id()]++;
                    }
                }
                List<Cross.Outcome> looks = c.looks(gene);
                double sum = 0.0;
                boolean ordered = true;
                for (int k = 0; k < looks.size(); k++) {
                    check(Math.abs(looks.get(k).chance() - counts[looks.get(k).allele().id()] / 4.0) < 1e-12, "odds match the real pups: " + ga + " x " + gb + " " + gene);
                    sum += looks.get(k).chance();
                    ordered &= k == 0 || looks.get(k - 1).chance() >= looks.get(k).chance();
                }
                check(Math.abs(sum - 1.0) < 1e-12 && ordered && looks.size() >= 1 && looks.size() <= 3, "odds add up to 1, most likely first, at most three looks: " + ga + " x " + gb + " " + gene);
            }
        }
        check(pairs == pool.size(), "every pair was checked");

        // litter
        Tissue shy = new Tissue(g("00-00-00-00-00-22"), Stats.ZERO), swarm = new Tissue(g("00-00-00-00-00-33"), Stats.ZERO), prolific = new Tissue(g("00-00-00-00-00-11"), Stats.ZERO);
        check(new Cross(shy, shy).litterLow() == 1 && new Cross(shy, shy).litterHigh() == 2 && Math.abs(new Cross(shy, shy).litter() - 1.2) < 1e-6, "shy x shy: 1 or 2 pups, mean 1.2");
        check(new Cross(swarm, swarm).litterLow() == 4 && new Cross(swarm, swarm).litterHigh() == 4, "swarm x swarm: exactly 4");
        check(new Cross(prolific, prolific).litterLow() == 3 && new Cross(prolific, prolific).litterHigh() == 3, "prolific x prolific: exactly 3");
        check(new Cross(shy, swarm).litterLow() == 2 && new Cross(shy, swarm).litterHigh() == 3, "shy x swarm (mean 2.6): 2 or 3");
        int lo = 99, hi = 0;
        for (int i = 0; i < 400; i++) {
            int n = Breeding.rollLitter(shy.genome(), swarm.genome(), rng);
            lo = Math.min(lo, n);
            hi = Math.max(hi, n);
        }
        check(lo == 2 && hi == 3, "the real litter roll stays inside the forecast range: " + lo + ".." + hi);

        // mutation, quality, drift, expected points: the seed's own numbers
        check(Math.abs(x.mutationChance() - Genome.mutationChance(ta.genome(), tb.genome())) < 1e-12 && Math.abs(x.mutationChance() - 0.04) < 1e-12, "mutation chance is the game's (4% at average fertility)");
        check(x.quality() == 0.0, "plain guavas and no starborn: quality 0");
        Tissue star = new Tissue(g("00-00-00-00-00-44"), Stats.of(0, 0, 0, 0, 16));
        check(Math.abs(new Cross(star, tb).quality() - 0.6) < 1e-9 && Math.abs(new Cross(star, star).quality() - 1.2) < 1e-9, "the starborn bonus is the only quality: 0.3 x sqrt(16) = 1.2 (average with a plain parent: 0.6)");
        double[] drift = Drift.drift(ta.genome(), tb.genome(), 0.0);
        for (Stat stat : Stat.values()) {
            int i = stat.ordinal();
            check(Math.abs(x.drift()[i] - drift[i]) < 1e-12 && Math.abs(x.expectedPoints(stat) - ((ta.stats().get(stat) + tb.stats().get(stat)) / 2.0 + drift[i])) < 1e-12, "expected points = average + drift: " + stat);
            double total = 0.0;
            int n = 3000;
            for (int k = 0; k < n; k++) {
                total += Drift.inherit(ta.stats(), tb.stats(), x.drift(), rng).get(stat);
            }
            check(Math.abs(total / n - x.expectedPoints(stat)) < 0.1, "the real pups average the forecast points: " + stat + " " + (total / n) + " vs " + x.expectedPoints(stat));
        }
        // symmetric: nobody is 'the mother'
        Cross y = new Cross(tb, ta);
        boolean same = y.litter() == x.litter() && y.quality() == x.quality() && y.mutationChance() == x.mutationChance() && java.util.Arrays.equals(y.drift(), x.drift());
        for (Gene gene : Gene.values()) {
            same &= text.apply(y.looks(gene)).equals(text.apply(x.looks(gene)));
        }
        check(same, "A x B and B x A forecast the same litter");

        // the layout
        com.bunnidogs.mod.layout.Metrics m = com.bunnidogs.mod.layout.Metrics.ESTIMATE;
        java.util.function.Function<com.bunnidogs.mod.layout.Page, java.util.List<String>> dump = pg -> {
            java.util.List<String> out = new java.util.ArrayList<>();
            for (com.bunnidogs.mod.layout.Page.Text t : pg.texts()) {
                out.add(t.y() + "|" + t.text());
            }
            return out;
        };
        com.bunnidogs.mod.layout.Page.Spread sp = com.bunnidogs.mod.layout.ForecastSheet.spread(ta, tb, m);
        java.util.List<String> l = dump.apply(sp.left());
        check(l.contains("0|FORECAST") && l.contains("0|sample A x B") && l.contains("14|Litter") && l.contains("14|about 2.0 pups") && l.contains("23|exactly 2 pups") && l.contains("37|Mutation") && l.contains("37|4.0% per pup")
                && l.contains("60|Quality") && l.contains("60|0.00") && l.contains("69|on plain guavas") && l.contains("83|STATS"), "forecast left page: " + l);
        check(l.contains("92|Vitality") && l.contains("92|+3 / 0") && l.contains("101|Speed") && l.contains("101|-1 / 0") && l.contains("119|Senses") && l.contains("119|+2 / 0") && l.contains("128|Bloom") && l.contains("128|+12 / 0"),
                "each stat row: the two parents' points: " + l);
        String looksText = String.join(" ", dump.apply(sp.right()).stream().map(t -> t.substring(t.indexOf('|') + 1)).toList());
        check(looksText.contains("Ears: long 100%") && looksText.contains("Coat: spotted 50%, tan 50%") && looksText.contains("Legs: standard 100%") && looksText.contains("Build: average 50%, stocky 50%")
                && looksText.contains("Temper.: loyal 100%") && looksText.contains("Fertil.: average 100%") && looksText.contains("Odds before any mutation"), "forecast right page: " + looksText);
        check(sp.right().hotspots().size() == Gene.COUNT && sp.right().hotspots().get(1).text().startsWith("Coat. Sample A has spotted + brown, sample B has tan + tan.") && sp.right().hotspots().get(1).text().contains("spotted 50%, tan 50%")
                && sp.right().hotspots().get(1).text().contains("4.0% of pups mutate"), "each gene row has a hover with both parents' genes: " + sp.right().hotspots().get(1).text());
        check(sp.left().hotspots().stream().anyMatch(h -> h.y() == 14 && h.text().contains("between 1 and 6")) && sp.left().hotspots().stream().anyMatch(h -> h.y() == 60 && h.text().contains("plain guavas") && h.text().contains("50% bigger"))
                && sp.left().hotspots().stream().anyMatch(h -> h.y() == 92 && h.text().startsWith("Vitality: A has +3 points, B has 0, so a pup starts at +1.5.")) && sp.left().links().isEmpty() && sp.right().links().isEmpty() && sp.left().portrait() == null,
                "hover boxes carry the game's numbers; no links, no portrait");
        // nothing runs off the page, whatever the pair (wild and rare alleles make the longest names)
        int worst = 0;
        for (int i = 0; i < pool.size(); i++) {
            com.bunnidogs.mod.layout.Page.Spread f = com.bunnidogs.mod.layout.ForecastSheet.spread(new Tissue(pool.get(i), Stats.of(3, -2, 1, 0, 9)), new Tissue(pool.get((i * 11 + 5) % pool.size()), Stats.of(-4, 6, 0, 2, 0)), m);
            for (com.bunnidogs.mod.layout.Page pg : new com.bunnidogs.mod.layout.Page[] {f.left(), f.right()}) {
                for (com.bunnidogs.mod.layout.Page.Text t : pg.texts()) {
                    worst = Math.max(worst, t.y() + com.bunnidogs.mod.layout.Page.LINE);
                    check(t.y() + com.bunnidogs.mod.layout.Page.LINE <= com.bunnidogs.mod.layout.Page.HEIGHT, "text inside the page height: " + t);
                    check(t.right() || t.x() + m.width(t.text()) <= com.bunnidogs.mod.layout.Page.WIDTH, "left-aligned text inside the page width: " + t);
                }
            }
        }
        check(worst > 100, "the worst page reaches y " + worst);

        // the window: a third spread, only with both samples
        com.bunnidogs.mod.layout.TableSheet both = new com.bunnidogs.mod.layout.TableSheet(Optional.of(ta), Optional.of(tb));
        com.bunnidogs.mod.layout.TableSheet onlyA = new com.bunnidogs.mod.layout.TableSheet(Optional.of(ta), Optional.empty());
        check(both.spreadCount() == 5 && both.isForecast(2) && !both.isForecast(0) && !both.isForecast(1) && !onlyA.isForecast(2) && onlyA.spreadCount() == 1, "the forecast is spread 2, only with both samples");
        check(dump.apply(both.spread(2, m).left()).contains("0|FORECAST") && dump.apply(both.spread(0, m).left()).contains("0|Table: sample A") && dump.apply(both.spread(1, m).left()).contains("0|Table: sample B"), "the window pages A, B, then the forecast");
        String screen = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/client/screen/PedigreeScreen.java"));
        check(screen.contains("this.table.isAnalysis(this.spread) ? null : buildPortrait(this.table.entry(this.spread))"), "the screen draws no portrait on the forecast");
    }

    // ------------------------------------------------------------------ Phase 8, chunk S1: plain words and real units
    static void plainAndUnits() throws Exception {
        // the user's own example, word for word (docs/SHEET_SPECS.md S1)
        Genome ex = g("22-32-00-10-00-01");
        check(Plain.describe(ex).equals("short ears, spotted coat carrying brown, standard legs, average build carrying stocky, "
                + "loyal, average fertility carrying prolific, plain diet"), "the user's example decodes exactly: " + Plain.describe(ex));
        check(Plain.look(ex).equals("spotted, short ears, loyal"), "look phrase: " + Plain.look(ex));
        check(Plain.look(g("00-00-00-00-00-00")).equals("tan, loyal"), "an all-default animal is 'tan, loyal'");
        check(Plain.look(g("00-30-11-00-00-00")).equals("spotted, long legs, loyal"), "look phrase skips defaults, keeps coat and temperament");
        check(Plain.look(g("11-00-00-00-22-00")).equals("tan, long ears, feral"), "temperament is last: " + Plain.look(g("11-00-00-00-22-00")));
        check(Plain.look(g("10-00-10-10-00-10")).equals("tan, loyal"), "carried (hidden) genes are not in the look phrase: " + Plain.look(g("10-00-10-10-00-10")));
        check(Plain.carries(ex).equals(List.of("brown", "stocky", "prolific")), "carries: " + Plain.carries(ex));
        check(Plain.carriesText(ex).equals("Carries: brown, stocky, prolific") && Plain.carriesText(Genome.DEFAULT).equals("Carries nothing hidden"), "carries text");
        check(Plain.statName(Stat.SWIFTNESS).equals("Speed") && Plain.statName(Stat.VITALITY).equals("Vitality"), "Swiftness is called Speed for players");
        check(Plain.HOW_TO_READ.contains("first digit came from the mother") && Plain.HOW_TO_READ.contains("Ears, Coat, Legs, Build, Temperament, Fertility"), "how to read names the six genes in code order");

        // every allele of every gene has its note and a non-empty sentence (a future allele cannot be forgotten);
        // every gene order in HOW_TO_READ is the real one
        StringBuilder order = new StringBuilder();
        for (Gene gene : Gene.values()) {
            order.append(order.length() == 0 ? "" : ", ").append(gene.displayName());
            check(!Plain.shortGeneName(gene).isEmpty() && Plain.shortGeneName(gene).length() <= 8, "short gene label fits a row: " + gene);
            for (Allele a : gene.alleles()) {
                check(Plain.hasNote(gene, a), "Plain has a note for " + gene.key() + "/" + a.name());
                String effect = Plain.effect(gene, a);
                check(!effect.isBlank() && !Plain.phrase(gene, a).isBlank(), "sentence for " + gene.key() + "/" + a.name());
                check(!effect.contains("NaN") && !effect.contains("Infinity") && !effect.contains("  "), "clean sentence for " + gene.key() + "/" + a.name() + ": " + effect);
                if (a.isWild()) {
                    check(effect.contains(a.lineage().key() + " lineage"), "a wild allele names its lineage: " + a.name());
                }
                if (a.isRare()) {
                    check(effect.contains("rare mutation"), "a rare allele says so: " + a.name());
                }
            }
        }
        check(Plain.HOW_TO_READ.contains(order.toString()), "the gene order in the how-to is " + order);

        // sentences follow the numbers of the tables
        check(Plain.effect(Gene.EARS, Gene.EARS.alleleNamed("short")).equals("Hears 35% less far."), "short ears: " + Plain.effect(Gene.EARS, Gene.EARS.alleleNamed("short")));
        check(Plain.effect(Gene.EARS, Gene.EARS.alleleNamed("long")).equals("Hears 35% further."), "long ears");
        check(Plain.effect(Gene.LEGS, Gene.LEGS.alleleNamed("long")).equals("About 12% faster and 24% higher jumps."), "long legs: " + Plain.effect(Gene.LEGS, Gene.LEGS.alleleNamed("long")));
        check(Plain.effect(Gene.LEGS, Gene.LEGS.alleleNamed("short")).equals("About 12% slower and 24% lower jumps."), "short legs");
        check(Plain.effect(Gene.BUILD, Gene.BUILD.alleleNamed("stocky")).contains("About 38% more health."), "stocky: " + Plain.effect(Gene.BUILD, Gene.BUILD.alleleNamed("stocky")));
        check(Plain.effect(Gene.BUILD, Gene.BUILD.alleleNamed("slim")).contains("About 23% less health."), "slim: " + Plain.effect(Gene.BUILD, Gene.BUILD.alleleNamed("slim")));
        String prolific = Plain.effect(Gene.FERTILITY, Gene.FERTILITY.alleleNamed("prolific"));
        check(prolific.contains("litters 50% bigger") && prolific.contains("33% less rest") && prolific.contains("2.3 times"), "prolific: " + prolific);
        check(Plain.trend(Gene.BUILD, Gene.BUILD.alleleNamed("stocky")).equals("Each generation: +1 Vitality, -1 Speed."), "stocky trend: " + Plain.trend(Gene.BUILD, Gene.BUILD.alleleNamed("stocky")));
        check(Plain.trend(Gene.FERTILITY, Gene.FERTILITY.alleleNamed("starborn")).equals("Each generation: +2 Bloom, -1 Vitality, -1 Senses."), "starborn trend (tolls)");
        check(Plain.trend(Gene.COAT, Gene.COAT.alleleNamed("tan")).isEmpty() && Plain.trend(Gene.EARS, Gene.EARS.alleleNamed("standard")).isEmpty(), "no trend, no sentence");
        check(Plain.hover(ex, Gene.EARS).startsWith("Short ears: Hears 35% less far.") && Plain.hover(ex, Gene.EARS).contains("every pup gets it"), "hover of a pure gene: " + Plain.hover(ex, Gene.EARS));
        check(Plain.hover(ex, Gene.COAT).contains("Hidden \"brown\": about half of its pups get it."), "hover of a carried gene: " + Plain.hover(ex, Gene.COAT));
        // the behaviour words match the entity's goals (the grep is the guard if somebody changes a temperament)
        String dog = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/entity/Bunnidog.java"));
        check(dog.contains("temperament() != Temperament.FERAL && super.canUse()") && dog.contains("temperament() == Temperament.SKITTISH && super.canUse()"),
                "the entity still flees unless feral and keeps its distance only when skittish (what Plain says)");

        // ---- Units
        check(Units.BASE_HEALTH == extractConst(dog, "BASE_HEALTH") && Units.BASE_SPEED == extractConst(dog, "BASE_SPEED")
                && Units.BASE_JUMP == extractConst(dog, "BASE_JUMP") && Units.BASE_FOLLOW_RANGE == extractConst(dog, "BASE_FOLLOW_RANGE"),
                "Units copies the entity's base values");
        near(Units.jumpHeight(0.42), 1.2522, 0.001, "the vanilla jump of 0.42 is about 1.252 blocks");
        near(Units.jumpHeight(0.0), 0.0, 1e-12, "no jump strength, no height");
        check(Units.jumpHeight(0.6) > Units.jumpHeight(0.42) && Units.jumpHeight(0.3) < Units.jumpHeight(0.42), "jump height grows with the attribute");
        Genome plain = Genome.DEFAULT;
        near(Units.hearts(plain, Stats.ZERO), 4.0, 1e-9, "an average bunnidog has 4 hearts");
        near(Units.blocksPerSecond(plain, Stats.ZERO), 10.7925, 1e-9, "average speed 0.25 x 43.17");
        near(Units.senseBlocks(plain, Stats.ZERO), 16.0, 1e-9, "average senses 16 blocks");
        near(Units.bloomMinutes(Stats.ZERO), 20.0, 1e-9, "average bloom 20 minutes");
        Genome stocky = g("00-00-00-11-00-00");
        near(Units.hearts(stocky, Stats.ZERO), 4.0 * 1.375, 1e-6, "stocky: 5.5 hearts");
        near(Units.hearts(stocky, Stats.of(3)), 4.0 * 1.375 * 1.12, 1e-6, "stocky and +3 Vitality: x1.12 more");
        Genome longLegs = g("00-00-11-00-00-00");
        near(Units.blocksPerSecond(longLegs, Stats.of(0, 7)), 0.25 * 1.12 * 1.28 * 43.17, 1e-6, "long legs and +7 Speed");
        near(Units.jumpAttribute(longLegs, Stats.ZERO), 0.42 * 1.24, 1e-6, "long legs jump attribute");
        near(Units.senseBlocks(g("22-00-00-00-00-00"), Stats.of(0, 0, 0, 2)), 16 * 0.65 * 1.08, 1e-6, "short ears and +2 Senses");
        near(Units.bloomMinutes(Stats.of(0, 0, 0, 0, 12)), 12.5, 1e-9, "+12 Bloom is 12.5 minutes");
        // the same product the entity computes, for every gene combination of one gene at a time and a few stat values
        for (Gene gene : new Gene[] { Gene.BUILD, Gene.LEGS, Gene.EARS }) {
            for (Allele a : gene.alleles()) {
                Genome one = Genome.DEFAULT.with(gene, 0, a.id()).with(gene, 1, a.id());
                for (int p : new int[] { -40, -3, 0, 5, 30, 200 }) {
                    Stats st = Stats.of(p, p, p, p, p);
                    Traits t = one.traits();
                    near(Units.healthPoints(one, st), 8.0 * t.healthMultiplier() * st.multiplier(Stat.VITALITY), 1e-9, "hp as the entity: " + a.name() + " " + p);
                    near(Units.speedAttribute(one, st), 0.25 * t.speedMultiplier() * st.multiplier(Stat.SWIFTNESS), 1e-9, "speed as the entity: " + a.name() + " " + p);
                    near(Units.jumpAttribute(one, st), 0.42 * t.jumpMultiplier() * st.multiplier(Stat.LEAP), 1e-9, "jump as the entity: " + a.name() + " " + p);
                    near(Units.senseBlocks(one, st), 16.0 * t.hearingMultiplier() * st.multiplier(Stat.SENSES), 1e-9, "senses as the entity: " + a.name() + " " + p);
                }
            }
        }
        check(dog.contains("BASE_HEALTH * t.healthMultiplier() * st.multiplier(Stat.VITALITY)") && dog.contains("BASE_SPEED * t.speedMultiplier() * st.multiplier(Stat.SWIFTNESS)")
                && dog.contains("BASE_JUMP * t.jumpMultiplier() * st.multiplier(Stat.LEAP)") && dog.contains("BASE_FOLLOW_RANGE * t.hearingMultiplier() * st.multiplier(Stat.SENSES)"),
                "the entity still multiplies base x gene x stat exactly as Units does");
        // heart icons
        check(Units.heartRow(4.0).equals(new Units.HeartRow(4, false, 0)) && Units.heartRow(4.5).equals(new Units.HeartRow(4, true, 0)), "heart rows in halves");
        check(Units.heartRow(7.4).equals(new Units.HeartRow(7, true, 0)) && Units.heartRow(7.2).equals(new Units.HeartRow(7, false, 0)), "7.4 hearts is 7 and a half, 7.2 is 7");
        check(Units.heartRow(10.0).equals(new Units.HeartRow(10, false, 0)) && Units.heartRow(10.4).equals(new Units.HeartRow(1, false, 10)), "10 hearts are drawn, more is one icon and a multiple");
        check(Units.heartRow(12.4).equals(new Units.HeartRow(1, false, 12)) && Units.heartRow(0.0).equals(new Units.HeartRow(0, true, 0)), "x12 and never an empty row");
        // texts
        List<Units.Reading> readings = Units.readings(stocky, Stats.of(3, 0, 0, 2, 12));
        check(readings.size() == Stat.COUNT, "five readings");
        check(readings.get(0).valueText().equals("6.2 hearts") && readings.get(0).baseText().equals("base 4.0 hearts") && readings.get(0).points() == 3, "vitality reading: " + readings.get(0));
        check(readings.get(1).baseText().equals("base 10.8 blocks/s") && readings.get(2).baseText().equals("base 1.25 blocks high"), "speed and leap base texts");
        check(readings.get(3).valueText().equals("hears 17 blocks") && readings.get(3).baseText().equals("base 16 blocks"), "senses reading: " + readings.get(3));
        check(readings.get(4).valueText().equals("grows up in 12.5 min") && readings.get(4).baseText().equals("base 20.0 min"), "bloom reading: " + readings.get(4));
        check(Units.working(Stat.VITALITY, stocky, Stats.of(3)).equals("8 HP x 1.375 stocky build x 1.12 bred = 12.3 HP (2 HP = 1 heart)"), "the working of vitality: " + Units.working(Stat.VITALITY, stocky, Stats.of(3)));
        check(Units.num(1.375).equals("1.375"), "num keeps three decimals");
        check(Units.num(1.0).equals("1") && Units.num(8.0).equals("8") && Units.num(1.12).equals("1.12") && Units.num(0.3).equals("0.3"), "num trims zeros");
        for (Stat stat : Stat.values()) {
            check(!Units.working(stat, ex, Stats.of(1, 2, 3, 4, 5)).isBlank() && !Units.working(stat, ex, Stats.of(-50, -50, -50, -50, -50)).contains("NaN"), "working text for " + stat);
        }
        // the speed constant is ONE line, and flagged as assumed in the docs of the class
        String units = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/genetics/Units.java"));
        check(units.contains("BLOCKS_PER_SECOND_PER_SPEED = 43.17D") && units.contains("ASSUMED"), "the speed constant is one line and marked ASSUMED");
        // pure packages stay pure: S1 adds no Minecraft import and no glue
        for (String file : new String[] { "Plain.java", "Units.java" }) {
            String text = Files.readString(Path.of("src/main/java/com/bunnidogs/mod/genetics/" + file));
            check(!text.contains("import net.minecraft") && !text.contains("import net.neoforged"), file + " has no Minecraft import");
        }
    }

    static double extractConst(String source, String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("double " + name + " = ([0-9.]+)D").matcher(source);
        if (!m.find()) {
            throw new IllegalStateException("no constant " + name);
        }
        return Double.parseDouble(m.group(1));
    }

    // ------------------------------------------------------------------ Phase 8, chunk S2: entry v2 (stats + pup) and plain readouts
    static void pedigreeV2() throws Exception {
        System.out.println("pedigree entry v2 (Phase 8 S2)");
        Genome a = g("22-32-00-10-00-01"), m = g("11-00-00-00-00-00"), f = g("22-00-00-00-00-00");
        UUID u = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String tag = "BD-3F7A2C";
        Optional<Genome> none = Optional.empty();
        Stats st = Stats.of(3, -1, 0, 2, 12);

        // exact text form
        Pedigree.Entry full = new Pedigree.Entry(a, Optional.of(m), Optional.of(f), Optional.of(u), Optional.of(tag), Optional.of(st), true);
        check(full.toString().equals(a + "#" + u + "@" + tag + "$3,-1,0,2,12!<" + m + "+" + f), "the v2 text form: " + full);
        Pedigree.Entry adultEntry = new Pedigree.Entry(a, none, none, Optional.of(u), Optional.empty(), Optional.of(st), false);
        check(adultEntry.toString().equals(a + "#" + u + "$3,-1,0,2,12"), "an adult has no ! : " + adultEntry);
        check(new Pedigree.Entry(a, none, none, Optional.empty(), Optional.empty(), Optional.of(Stats.ZERO), true).toString().equals(a + "$0,0,0,0,0!"), "stats, no id, no tag, no parents");
        check(!new Pedigree.Entry(a, none, none, Optional.empty(), Optional.empty(), Optional.empty(), true).pup(), "a pup flag without stats is dropped (the text cannot carry it)");

        // round trip of every combination of the optional parts
        boolean ok = true;
        int combos = 0;
        for (int mask = 0; mask < 32; mask++) {
            boolean withId = (mask & 1) != 0, withTag = (mask & 2) != 0, withStats = (mask & 4) != 0, isPup = (mask & 8) != 0, withParents = (mask & 16) != 0;
            Pedigree.Entry e = new Pedigree.Entry(a, withParents ? Optional.of(m) : none, withParents ? Optional.of(f) : none,
                    withId ? Optional.of(u) : Optional.empty(), withTag ? Optional.of(tag) : Optional.empty(),
                    withStats ? Optional.of(st) : Optional.empty(), isPup);
            Pedigree book = new Pedigree();
            book.add(e);
            Pedigree back = Pedigree.parse(book.toString());
            ok &= back.size() == 1 && back.entries().get(0).equals(e) && back.toString().equals(book.toString());
            combos++;
        }
        check(ok && combos == 32, "all 32 combinations of id / tag / stats / pup / parents round-trip");
        // negative and large points
        Pedigree big = new Pedigree();
        big.add(new Pedigree.Entry(a, none, none, Optional.of(u), Optional.empty(), Optional.of(Stats.of(-1000000, 1000000, -7, 99999, -123456)), false));
        check(Pedigree.parse(big.toString()).entries().equals(big.entries()), "extreme points round-trip: " + big);

        // books from before still read, and print back exactly as they were
        String old5c = a + "<" + m + "+" + f + ";" + m;
        String old5e = a + "#" + u + "@" + tag + "<" + m + "+" + f + ";" + m + "#00000000-0000-0000-0000-000000000002";
        for (String text : new String[] { old5c, old5e }) {
            Pedigree p = Pedigree.parse(text);
            check(p.size() == 2 && p.entries().stream().allMatch(e -> e.stats().isEmpty() && !e.pup()), "an old book reads, no stats recorded: " + text);
            check(p.toString().equals(text), "and prints back unchanged: " + p);
        }

        // malformed snapshots skip the entry, never crash, never touch the others
        String[] bad = { a + "$", a + "$!", a + "$3,-1", a + "$3,-1,0,2", a + "$1,2,3,4,5,6", a + "$x,1,2,3,4", a + "$1,2,3,4,5!!", a + "$1,2,3,4,5$1,2,3,4,5",
                a + "$1,2,3,4,5@" + tag, a + "$1 2 3 4 5", a + "$1,2,3,4,5 !", a + "!", a + "$1,2,3,4,5!x", a + "$+1,2,3,4,5", a + "$01,2,3,4,5" };
        for (String text : bad) {
            Pedigree p = Pedigree.parse(text + ";" + m);
            check(p.size() == 1 && p.entries().get(0).genome().equals(m), "malformed entry skipped, the next one kept: '" + text + "' -> " + p);
        }
        check(Pedigree.parse(a + "$0,0,0,0,0!").entries().get(0).pup(), "the simplest good snapshot reads");

        // logging again brings the entry up to date: stats appear, a pup grows up, points change; same facts are ALREADY
        Pedigree book = new Pedigree();
        Pedigree.Entry noStats = new Pedigree.Entry(a, none, none, Optional.of(u), Optional.empty());
        check(book.add(noStats) == Pedigree.Added.ADDED, "a v1 entry is added");
        Pedigree.Entry asPup = new Pedigree.Entry(a, none, none, Optional.of(u), Optional.empty(), Optional.of(st), true);
        check(book.add(asPup) == Pedigree.Added.UPDATED && book.size() == 1 && book.entries().get(0).stats().equals(Optional.of(st)), "logging it again adds the stats (UPDATED, the book does not grow)");
        check(book.add(asPup) == Pedigree.Added.ALREADY, "the same snapshot is ALREADY");
        Pedigree.Entry grown = new Pedigree.Entry(a, none, none, Optional.of(u), Optional.empty(), Optional.of(st), false);
        check(book.add(grown) == Pedigree.Added.UPDATED && !book.entries().get(0).pup(), "a pup that grew up updates its entry");
        check(book.add(new Pedigree.Entry(a, none, none, Optional.of(u), Optional.empty(), Optional.of(Stats.of(4, -1, 0, 2, 12)), false)) == Pedigree.Added.UPDATED, "changed points update it");
        check(book.size() == 1, "still one animal");
        // a gene-only entry (5c) is taken over by the same animal logged with id and stats
        Pedigree takeover = new Pedigree();
        takeover.add(new Pedigree.Entry(a, none, none));
        check(takeover.add(asPup) == Pedigree.Added.UPDATED && takeover.size() == 1 && takeover.entries().get(0).id().isPresent(), "a 5c entry is taken over in place");
        // two different animals with the same genes and stats are two entries
        Pedigree twins = new Pedigree();
        twins.add(grown);
        check(twins.add(new Pedigree.Entry(a, none, none, Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001")), Optional.empty(), Optional.of(st), false)) == Pedigree.Added.ADDED && twins.size() == 2, "two animals, same genes and stats: two entries");

        // size: 24 worst-case entries stay far under the 32,767 character limit of a string component
        Pedigree worst = new Pedigree();
        for (int i = 0; i < Pedigree.MAX_ENTRIES; i++) {
            worst.add(new Pedigree.Entry(a, Optional.of(m), Optional.of(f), Optional.of(new UUID(i + 1, i + 7)), Optional.of(tag),
                    Optional.of(Stats.of(-1000000, -1000000, -1000000, -1000000, -1000000)), true));
        }
        check(worst.size() == Pedigree.MAX_ENTRIES && worst.toString().length() < 8000, "a full book of worst-case entries is " + worst.toString().length() + " characters");
        check(Pedigree.parse(worst.toString()).entries().equals(worst.entries()), "and round-trips");

        // the family list: the look in words, adult/pup when known, the code in brackets at the END
        Pedigree list = new Pedigree();
        list.add(asPup);
        list.add(new Pedigree.Entry(m, none, none));
        List<String> lines = list.lines();
        check(lines.get(1).equals("#1  generation 0  " + Plain.look(a) + " (pup)  [" + a + "]"), "family line of a pup: " + lines.get(1));
        check(lines.get(3).equals("#2  generation 0  " + Plain.look(m) + "  [" + m + "]"), "family line of an old entry: no adult/pup guess: " + lines.get(3));
        check(list.lines().get(1).indexOf(a.toString()) > list.lines().get(1).indexOf(Plain.look(a)), "the code comes after the words");

        // the glue, by grep
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String bookItem = Files.readString(src.resolve("item/PedigreeBookItem.java"));
        check(bookItem.contains("Plain.look(entry.genome())") && bookItem.contains("import com.bunnidogs.mod.genetics.Plain;"), "the log message names the animal in words");
        String tissueItem = Files.readString(src.resolve("item/TissueSampleItem.java"));
        check(tissueItem.contains("Plain.look(tissue.get().genome())") && tissueItem.indexOf("Plain.look(tissue.get().genome())") < tissueItem.indexOf("\"Code \" + tissue.get().genome()"), "the tissue tooltip: words first, code after");
        check(tissueItem.contains("import com.bunnidogs.mod.genetics.Plain;"), "the tooltip imports Plain");
        String readout = Files.readString(src.resolve("genetics/Readout.java"));
        check(readout.indexOf("lines.add(\"  Code: \" + genome);") > readout.indexOf("lines.add(\"  Born of: mother \""), "the animal readout ends with the code");
        check(!readout.contains("\"Bunnidog\" + (baby ? \" [pup]\" : \"\") + \"  genome \""), "the old headline with the raw code is gone");
    }

    static void nest() throws Exception {
        System.out.println("the Nest and its point of interest (H1)");
        check(Home.NEST_TICKETS == 1 && Home.NEST_RANGE == 1 && Home.SEARCH_RADIUS == 24 && Home.HOME_RADIUS == 16, "the home numbers of HOME_SPECS: 1 ticket, range 1, look within 24, home radius 16");
        Path res = Path.of("src/main/resources");
        Path assets = res.resolve("assets/bunnidogs");
        Path data = res.resolve("data");
        String lang = Files.readString(assets.resolve("lang/en_us.json"));
        check(lang.contains("\"block.bunnidogs.nest\": \"Nest\"") && lang.contains("\"item.bunnidogs.nest\": \"Nest\""), "lang names the block and the item");
        String state = Files.readString(assets.resolve("blockstates/nest.json"));
        check(state.contains("\"bunnidogs:block/nest\""), "blockstate points at the model");
        String model = Files.readString(assets.resolve("models/block/nest.json"));
        check(model.contains("minecraft:block/cube_bottom_top") && model.contains("bunnidogs:block/nest_top") && model.contains("bunnidogs:block/nest_side") && model.contains("bunnidogs:block/guava_planks"), "model: top, side and the planks as the bottom");
        for (String tex : new String[] {"nest_top", "nest_side", "guava_planks"}) {
            Path png = assets.resolve("textures/block/" + tex + ".png");
            check(Files.exists(png), tex + ".png exists");
            byte[] b = Files.readAllBytes(png);
            int w = ((b[16] & 255) << 24) | ((b[17] & 255) << 16) | ((b[18] & 255) << 8) | (b[19] & 255);
            int h = ((b[20] & 255) << 24) | ((b[21] & 255) << 16) | ((b[22] & 255) << 8) | (b[23] & 255);
            check(w == 16 && h == 16, tex + ".png is 16x16");
        }
        check(Files.readString(assets.resolve("items/nest.json")).contains("\"bunnidogs:block/nest\""), "client item shows the block model");
        check(Files.readString(data.resolve("bunnidogs/loot_table/blocks/nest.json")).contains("\"bunnidogs:nest\""), "loot: drops itself");
        String recipe = Files.readString(data.resolve("bunnidogs/recipe/nest.json"));
        check(recipe.contains("crafting_shaped") && recipe.contains("\"WWW\"") && recipe.contains("\"PPP\"") && recipe.contains("minecraft:wheat") && recipe.contains("bunnidogs:guava_planks") && recipe.contains("\"bunnidogs:nest\""), "recipe: wheat over guava planks");
        check(Files.readString(data.resolve("minecraft/tags/block/mineable/axe.json")).contains("\"bunnidogs:nest\""), "axe-mineable");
        // the glue, by grep
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String blocks = Files.readString(src.resolve("registry/ModBlocks.java"));
        check(blocks.contains("BLOCKS.registerBlock(\"nest\", NestBlock::new,"), "ModBlocks registers the native Nest interaction block (H5)");
        String items = Files.readString(src.resolve("registry/ModItems.java"));
        check(items.contains("NEST = ITEMS.registerSimpleBlockItem(ModBlocks.NEST)") && items.contains("event.accept(NEST.get())"), "ModItems: block item + creative tab");
        String poi = Files.readString(src.resolve("registry/ModPoi.java"));
        check(poi.contains("Registries.POINT_OF_INTEREST_TYPE") && poi.contains("POI_TYPES.register(\"nest\"") && poi.contains("ModBlocks.NEST.get().getStateDefinition().getPossibleStates()")
                && poi.contains("Home.NEST_TICKETS, Home.NEST_RANGE"), "ModPoi: the nest type covers every state of the block and uses the Home numbers");
        String mod = Files.readString(src.resolve("BunnidogsMod.java"));
        check(mod.contains("ModPoi.POI_TYPES.register(modEventBus);") && mod.indexOf("ModBlocks.BLOCKS.register(modEventBus);") < mod.indexOf("ModPoi.POI_TYPES.register(modEventBus);"), "the mod entry registers the POI types after the blocks");
        // V2a's hardcoded spawn home is gone; the go-home goal stays (idle until H2)
        check(!Files.readString(src.resolve("worldgen/WarrenFeature.java")).contains("restrictTo("), "worldgen gives no dog a home");
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        check(!dog.contains("restrictTo(") && dog.contains("addGoal(6, new GoHomeGoal(this))"), "Bunnidog itself gives no dog a home (NestHome does, H2); the go-home goal is still there");
        check(!Files.readString(src.resolve("layout/WarrenShape.java")).contains("HOME_RADIUS"), "the radius lives in Home now, not in WarrenShape");
    }

    /** H2: dogs claim, check and release a Nest (docs/HOME_SPECS.md R1-R7). */
    static void nestClaim() throws Exception {
        System.out.println("dogs claim a nest (H2)");
        // R1: only an adult without a nest wants one
        check(Home.wantsHome(true, false) && !Home.wantsHome(false, false) && !Home.wantsHome(true, true) && !Home.wantsHome(false, true), "R1: only an adult without a nest wants a home");
        // R2: every roll from 0 to 40 gives 100..140; out-of-range rolls are clamped
        boolean delayOk = true;
        for (int roll = 0; roll <= Home.LOOK_JITTER; roll++) {
            delayOk &= Home.lookDelay(roll) == 100 + roll;
        }
        check(delayOk && Home.lookDelay(-5) == 100 && Home.lookDelay(999) == 140, "R2: look delay 100 + roll (0-40), clamped");
        check(Home.LOOK_INTERVAL == 100 && Home.LOOK_JITTER == 40 && Home.CHECK_INTERVAL == 200 && Home.REJECT_MEMORY == 8, "the H2 numbers: look 100 + 0-40, check 200, remember 8 rejects");
        // the step of one timer tick, every combination
        boolean stepsOk = true;
        for (boolean adult : new boolean[] {false, true}) {
            for (boolean nest : new boolean[] {false, true}) {
                for (int left : new int[] {-3, 0, 1, 50}) {
                    Home.Step expect;
                    if (left > 0) {
                        expect = Home.Step.NONE;
                    } else if (nest) {
                        expect = Home.Step.CHECK;
                    } else {
                        expect = adult ? Home.Step.LOOK : Home.Step.NONE;
                    }
                    stepsOk &= Home.step(adult, nest, left) == expect;
                }
            }
        }
        check(stepsOk, "step(): nothing while the timer runs; then look (homeless adult), check (has a nest) or nothing (pup)");
        check(!Home.rejectsFull(7) && Home.rejectsFull(8) && Home.rejectsFull(20), "R4: the reject list is cleared at 8");

        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String nh = Files.readString(src.resolve("entity/NestHome.java"));
        // R3: ask the point-of-interest index for a free nest of our type within the search radius, skipping rejected ones
        check(nh.contains("level.getPoiManager()") && nh.contains("pois.take(") && nh.contains("holder.is(ModPoi.NEST.getKey())") && nh.contains("this.dog.blockPosition(), Home.SEARCH_RADIUS)")
                && nh.contains("!this.rejected.contains(pos.asLong())"), "R3: take a free nest within 24 blocks, not a rejected one");
        // R4: path check on the block above; unreachable -> ticket back at once, remembered
        check(nh.contains("createPath(pos.above(), 1)") && nh.contains("path.canReach()") && nh.indexOf("if (!this.canReach(pos))") < nh.indexOf("this.claim(pos);")
                && nh.indexOf("this.giveBack(level, pos);") < nh.indexOf("this.rejected.add(pos.asLong());"), "R4: unreachable nest -> ticket back, remembered, no claim");
        // R5: the claim writes vanilla's home with the Home radius
        check(nh.contains("this.nest = pos;") && !nh.contains("restrictTo") && !nh.contains("clearRestriction"), "R5: claim only remembers the nest (no vanilla home call: those names do not exist in this version)");
        // R6: the check reads the block; a missing nest is forgotten WITHOUT releasing (the point of interest is gone with the block)
        int check = nh.indexOf("private void check(ServerLevel level)");
        int checkEnd = nh.indexOf("/** R7");
        String checkBody = nh.substring(check, checkEnd);
        check(checkBody.contains("level.getBlockState(this.nest).is(ModBlocks.NEST.get())") && checkBody.contains("this.forget()") && !checkBody.contains("giveBack"), "R6: a missing nest block is forgotten, no release");
        check(nh.contains("this.nest = null;"), "forgetting clears the nest");
        // R7: release only while the block is still a nest, and a failed release never crashes the tick
        int rel = nh.indexOf("public void release(ServerLevel level)");
        String relBody = nh.substring(rel, nh.indexOf("private void forget()"));
        check(relBody.contains(".is(ModBlocks.NEST.get())") && relBody.indexOf(".is(ModBlocks.NEST.get())") < relBody.indexOf("this.giveBack(level, pos)"), "R7: release only if the nest block is still there");
        check(nh.contains("catch (RuntimeException e)") && nh.contains("level.getPoiManager().release(pos)"), "a failed give-back is logged, not thrown");
        check(nh.contains("NO_NEST = Integer.MIN_VALUE") && nh.contains("if (y == NO_NEST)"), "saved Y of Integer.MIN_VALUE means no nest");
        check(!nh.contains("jetbrains"), "no annotation import that the project does not use elsewhere");

        // glue in Bunnidog
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        int tickAt = dog.indexOf("this.guavaMemory.tick(this.getAge());");
        int nestTick = dog.indexOf("this.nestHome.tick(serverLevel, !this.isBaby());");
        check(tickAt > 0 && nestTick > tickAt && dog.contains("private final NestHome nestHome = new NestHome(this);"), "Bunnidog ticks its NestHome on the server, an adult flag from isBaby()");
        int die = dog.indexOf("public void die(DamageSource source)");
        check(die > 0 && dog.indexOf("super.die(source);", die) > die && dog.indexOf("super.die(source);", die) < dog.indexOf("this.nestHome.release(serverLevel);", die), "die(): super first, then the ticket goes back (R7)");
        check(dog.contains("import net.minecraft.world.damagesource.DamageSource;"), "DamageSource is imported");
        check(dog.contains("output.putInt(TAG_NEST_X, nest.getX());") && dog.contains("input.getIntOr(TAG_NEST_Y, NestHome.NO_NEST)") && dog.indexOf("super.readAdditionalSaveData(input);") < dog.indexOf("this.nestHome.restore("), "NBT: NestX/Y/Z written only with a nest, restored after super");
        check(dog.contains("addGoal(6, new GoHomeGoal(this))") && !dog.contains("MoveTowardsRestrictionGoal"), "the walk-back goal is our GoHomeGoal at priority 6");
        check(!dog.contains("restrictTo(") && !Files.readString(src.resolve("worldgen/WarrenFeature.java")).contains("restrictTo("), "no vanilla home is written anywhere");
        // R5 pure rules + the goal
        check(Home.RETURN_RADIUS == 8 && !Home.tooFar(16.0 * 16.0) && Home.tooFar(16.0 * 16.0 + 0.01) && Home.arrived(8.0 * 8.0) && !Home.arrived(8.0 * 8.0 + 0.01) && !Home.tooFar(0.0) && Home.arrived(0.0), "R5: too far above 16 blocks, arrived within 8");
        String goal = Files.readString(src.resolve("entity/GoHomeGoal.java"));
        check(goal.contains("extends Goal") && goal.contains("Goal.Flag.MOVE") && goal.contains("Home.tooFar(this.distSq())") && goal.contains("Home.arrived(this.distSq())") && goal.contains("this.dog.homeNest() != null"), "GoHomeGoal: only with a nest, uses the pure rules, moves");
        check(dog.contains("public BlockPos homeNest()"), "Bunnidog exposes homeNest() to the goal");
    }

    /** V1a: guava wood in the houses by development level (docs/FIXES_PLAN.md 5.2). */
    static void houseWood() throws Exception {
        System.out.println("houses of guava wood (V1a)");
        check(HouseShape.tierFor(0) == 0 && HouseShape.tierFor(1) == 0 && HouseShape.tierFor(2) == 1 && HouseShape.tierFor(3) == 2 && HouseShape.tierFor(4) == 3 && HouseShape.tierFor(5) == 4 && HouseShape.tierFor(9) == 4,
                "tiers: level 1 plain, 2 door frame, 3 cottage, 4 foundation, 5 loft");
        int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        boolean same = true, openKept = true, plainClean = true, counts = true, roofCovered = true, doorFrame = true;
        for (HouseShape.Form form : HouseShape.Form.values()) {
            for (int[] side : sides) {
                List<HouseShape.Cell> plain = HouseShape.cells(side[0], side[1], form);
                same &= HouseShape.cells(side[0], side[1], form, 0).equals(plain);
                for (HouseShape.Cell c : plain) {
                    plainClean &= c.kind() != HouseShape.Kind.BEAM && c.kind() != HouseShape.Kind.PLANK && c.kind() != HouseShape.Kind.PLANK_SLAB;
                }
                for (int tier = 1; tier <= 3; tier++) {
                    List<HouseShape.Cell> dressed = HouseShape.cells(side[0], side[1], form, tier);
                    same &= dressed.size() == plain.size();
                    for (int i = 0; i < plain.size(); i++) {
                        HouseShape.Cell a = plain.get(i), b = dressed.get(i);
                        same &= a.dx() == b.dx() && a.dy() == b.dy() && a.dz() == b.dz();
                        openKept &= (a.kind() == HouseShape.Kind.OPEN) == (b.kind() == HouseShape.Kind.OPEN);
                        if (a.kind() == HouseShape.Kind.WINDOW) openKept &= b.kind() == HouseShape.Kind.WINDOW;
                        if (b.dx() == side[0] && b.dz() == side[1] && b.dy() == 2) doorFrame &= b.kind() == HouseShape.Kind.BEAM;
                    }
                    long beam = dressed.stream().filter(c -> c.kind() == HouseShape.Kind.BEAM).count();
                    long plank = dressed.stream().filter(c -> c.kind() == HouseShape.Kind.PLANK).count();
                    long slab = dressed.stream().filter(c -> c.kind() == HouseShape.Kind.PLANK_SLAB).count();
                    long wall = dressed.stream().filter(c -> c.kind() == HouseShape.Kind.WALL).count();
                    if (form == HouseShape.Form.MOUND && tier == 1) counts &= beam == 5 && plank == 0 && slab == 0;
                    if (form == HouseShape.Form.MOUND && tier == 2) counts &= beam == 9 && plank == 4 && slab == 8 && wall == 3;
                    if (form == HouseShape.Form.TOWER && tier == 1) counts &= beam == 7 && plank == 0 && slab == 0;
                    if (form == HouseShape.Form.TOWER && tier == 2) counts &= beam == 13 && plank == 0 && slab == 0;
                    long found = dressed.stream().filter(c -> c.kind() == HouseShape.Kind.FOUNDATION).count();
                    if (tier <= 2) counts &= found == 0;
                    if (tier == 3) counts &= found == 3 && (form == HouseShape.Form.TOWER ? wall == 3 : wall == 0);   // a tower keeps the plain wall of its top layer
                    if (form == HouseShape.Form.MOUND && tier == 3) counts &= beam == 9 && plank == 4 && slab == 8;
                    if (form == HouseShape.Form.TOWER && tier == 3) counts &= beam == 13 && plank == 0 && slab == 0;
                    if (form == HouseShape.Form.MOUND) {
                        for (HouseShape.Cell c : dressed) {
                            if (c.dy() == 2) roofCovered &= c.kind() != HouseShape.Kind.OPEN && c.kind() != HouseShape.Kind.WALL;
                        }
                    }
                }
            }
        }
        check(same, "a dressed house has the same cells in the same order as the plain one (footprint, height and fit tests unchanged)");
        check(openKept, "the doorway, the hollow and the tower windows are never replaced by wood");
        check(plainClean, "tier 0 contains no guava wood at all (the old houses are unchanged)");
        check(counts, "block counts: mound frame 5 logs; cottage 9 logs + 4 planks + 8 slabs + 3 wall; tower frame 7 logs; tower cottage 13 logs");
        check(doorFrame, "the cell over the door is a log lintel in every tier");
        check(roofCovered, "the mound roof still covers the whole footprint");
        // which cells: the two posts beside a door at (1,0) are the corners (1,-1) and (1,1), at both wall heights
        List<HouseShape.Cell> frame = HouseShape.cells(1, 0, HouseShape.Form.MOUND, 1);
        boolean posts = true;
        for (HouseShape.Cell c : frame) {
            boolean expect = (c.dx() == 1 && Math.abs(c.dz()) == 1 && c.dy() <= 1) || (c.dx() == 1 && c.dz() == 0 && c.dy() == 2);
            posts &= (c.kind() == HouseShape.Kind.BEAM) == expect;
        }
        check(posts, "frame: logs exactly at the two door posts (two high) and the lintel");
        // source: palette, feature and piece
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String pal = Files.readString(src.resolve("worldgen/WarrenPalette.java"));
        check(pal.contains("BEAM, PLANK, PLANK_SLAB") && pal.contains("ModBlocks.GUAVA_LOG.get()") && pal.contains("ModBlocks.GUAVA_PLANKS.get()") && pal.contains("ModBlocks.GUAVA_SLAB.get()"), "palette: BEAM / PLANK / PLANK_SLAB are guava blocks in every lineage");
        String feat = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        check(feat.contains("HouseShape.tierFor(devLevel)") && feat.contains("case BEAM -> WarrenPalette.get(style, WarrenPalette.Role.BEAM)") && feat.contains("case PLANK -> ") && feat.contains("case PLANK_SLAB -> ")
                && feat.contains("house, surfaceOnly, style, trees)"), "buildHouse turns the new kinds into guava blocks and takes the tier from the level (trees built)");
        // House details and cellar clearance are exercised by WarrenNetworkCheck;
        // a buildHouse call's source spelling is not a save-compatibility test.
        houseLoft();
    }

    /** V1b: the loft of the level 5 mound cottage, the foundation in towers, and the landmarks in guava wood. */
    static void houseLoft() throws Exception {
        int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        boolean shape = true, order = true, doorway = true, noDouble = true;
        for (int[] side : sides) {
            List<HouseShape.Cell> loft = HouseShape.cells(side[0], side[1], HouseShape.Form.MOUND, HouseShape.TIER_LOFT);
            java.util.Map<HouseShape.Kind, Long> n = new java.util.EnumMap<>(HouseShape.Kind.class);
            java.util.Set<String> seen = new HashSet<>();
            int maxDy = 0;
            int lastGroup = 0;
            for (HouseShape.Cell c : loft) {
                n.merge(c.kind(), 1L, Long::sum);
                noDouble &= seen.add(c.dx() + "," + c.dy() + "," + c.dz());
                maxDy = Math.max(maxDy, c.dy());
                int group = c.kind() == HouseShape.Kind.OPEN ? 2 : (c.kind() == HouseShape.Kind.PLANK_SLAB || (c.kind() == HouseShape.Kind.PLANK && c.dy() >= 3)) ? 1 : 0;
                order &= group >= lastGroup;
                lastGroup = group;
                if (c.dx() == side[0] && c.dz() == side[1] && c.dy() <= 1) doorway &= c.kind() == HouseShape.Kind.OPEN;
                if (c.dx() == side[0] && c.dz() == side[1] && c.dy() == 2) doorway &= c.kind() == HouseShape.Kind.BEAM;
                if (c.kind() == HouseShape.Kind.WINDOW) doorway &= c.dy() == 2 && !(c.dx() == side[0] && c.dz() == side[1]) && Math.abs(c.dx()) + Math.abs(c.dz()) == 1;
                if (c.dx() == 0 && c.dz() == 0 && c.dy() <= 2) doorway &= c.kind() == HouseShape.Kind.OPEN;
            }
            shape &= loft.size() == 37 && maxDy == HouseShape.LOFT_HEIGHT - 1
                    && n.get(HouseShape.Kind.BEAM) == 13 && n.get(HouseShape.Kind.FOUNDATION) == 3 && n.get(HouseShape.Kind.PLANK) == 4
                    && n.get(HouseShape.Kind.WINDOW) == 3 && n.get(HouseShape.Kind.PLANK_SLAB) == 9 && n.get(HouseShape.Kind.OPEN) == 5
                    && !n.containsKey(HouseShape.Kind.WALL) && !n.containsKey(HouseShape.Kind.ROOF);
        }
        check(shape, "loft: 37 cells, 5 high; 13 logs, 3 foundation, 4 planks, 3 windows, 9 slabs, 5 open; no plain wall or roof left");
        check(noDouble && order, "loft: no cell twice, walls before roof before open cells");
        check(doorway, "loft: the door and the hollow stay open, the lintel is a log, windows only in the middle of the three other sides at dy 2, the hollow is open up to the loft");
        boolean towerKeep = true;
        for (int[] side : sides) {
            towerKeep &= HouseShape.cells(side[0], side[1], HouseShape.Form.TOWER, HouseShape.TIER_LOFT).equals(HouseShape.cells(side[0], side[1], HouseShape.Form.TOWER, HouseShape.TIER_FOUNDATION));
        }
        check(towerKeep, "a tower has no loft: level 5 gives it the same cells as level 4 (corner posts and a stone foundation)");
        // the landmarks
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String pal = Files.readString(src.resolve("worldgen/WarrenPalette.java"));
        check(pal.contains("case PLANK_SLAB, SLAB:") && pal.contains("case FENCE:") && pal.contains("ModBlocks.GUAVA_FENCE.get()") && !pal.contains("OAK_FENCE") && !pal.contains("COBBLESTONE_WALL") && !pal.contains("NETHER_BRICK_FENCE"),
                "palette: the posts and roofs of the landmarks are guava fence and slab in every lineage (the old per-lineage fences and slabs are gone)");
        int cols = 0;
        for (String line : pal.split("\n")) {
            if (line.trim().startsWith("put(Lineage.")) {
                int c = 0, at = 0;
                while ((at = line.indexOf("Blocks.", at)) >= 0) { c++; at += 7; }
                check(c == 11, "palette row has 11 blocks: " + line.trim().substring(0, 20));
                cols++;
            }
        }
        check(cols == 8, "palette: 8 lineage rows");
        check(pal.indexOf("HAY,") < pal.indexOf("BEAM, PLANK, PLANK_SLAB,") && pal.indexOf("SPROUT_C,") < pal.indexOf("BEAM, PLANK, PLANK_SLAB,"), "palette: the roles of the table come first, the guava roles after them (the table is indexed by ordinal)");
        List<PropShape.Kind> fenced = new java.util.ArrayList<>();
        for (PropShape.Kind k : PropShape.Kind.values()) {
            for (PropShape.Cell c : PropShape.cells(k, 0)) {
                if (c.mat() == PropShape.Mat.FENCE || c.mat() == PropShape.Mat.SLAB) { fenced.add(k); break; }
            }
        }
        boolean lowestOk = true;
        for (PropShape.Kind k : fenced) {
            int first = 99;
            for (int level = 1; level <= 5; level++) if (PropShape.kindsFor(level).contains(k)) { first = level; break; }
            lowestOk &= first >= 3;
        }
        check(fenced.size() == 3 && fenced.contains(PropShape.Kind.WELL) && fenced.contains(PropShape.Kind.STALL) && fenced.contains(PropShape.Kind.PLAZA) && lowestOk,
                "every landmark with a post or a slab (well, stall, plaza) exists only from level 3, so no village of level 1-2 gets guava props before its houses have wood");
        String feat = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        check(feat.contains("case FOUNDATION -> WarrenPalette.get(style, WarrenPalette.Role.RING_A);"), "buildHouse: the foundation is the lineage's first ring block");
    }

    /** H4: at night an adult with a nest walks home and stays (docs/HOME_SPECS.md R8). */
    static void nestNight() throws Exception {
        System.out.println("dogs go home at night (H4)");
        check(Home.DAY_LENGTH == 24000 && Home.DUSK == 12500 && Home.DAWN == 23000 && Home.NIGHT_RADIUS == 2 && Home.NIGHT_RETRY == 20, "R8 numbers: day 24000, dusk 12500, dawn 23000, radius 2, retry 20");
        check(!Home.isNight(0) && !Home.isNight(6000) && !Home.isNight(12499) && Home.isNight(12500) && Home.isNight(18000) && Home.isNight(22999) && !Home.isNight(23000) && !Home.isNight(23999), "R8: night is tick 12500 to 22999 of the day");
        check(Home.isNight(24000L * 7 + 13000) && !Home.isNight(24000L * 7 + 1000) && Home.isNight(-10000), "R8: later days and the day counter wrap (clock ticks mod 24000)");
        check(!Home.nightFar(0.0) && !Home.nightFar(4.0) && Home.nightFar(4.01) && Home.nightFar(100.0), "R8: farther than 2 blocks from the nest walks home");
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String goal = Files.readString(src.resolve("entity/NightHomeGoal.java"));
        check(goal.contains("extends Goal") && goal.contains("Goal.Flag.MOVE") && goal.contains("Home.isNight(level.clockManager().getTotalTicks(clock.get()))") && goal.contains("Home.nightFar(this.distSq())")
                && goal.contains("!this.dog.isBaby()") && goal.contains("this.dog.homeNest() != null") && goal.contains("defaultClock()"), "NightHomeGoal: adult with a nest, default clock, pure rules, moves");
        String dog = Files.readString(src.resolve("entity/Bunnidog.java"));
        int night = dog.indexOf("new NightHomeGoal(this)");
        int home = dog.indexOf("new GoHomeGoal(this)");
        check(night > 0 && home > night && dog.contains("addGoal(6, new NightHomeGoal(this))"), "NightHomeGoal is registered at priority 6, before GoHomeGoal");
    }

    /** H3: worldgen puts a Nest beside each burrow hole (docs/HOME_SPECS.md section 3). */
    static void nestWorldgen() throws Exception {
        System.out.println("warrens come with nests (H3)");
        int fewest = Integer.MAX_VALUE;
        for (int seed = 0; seed < 300; seed++) {
            for (boolean grand : new boolean[] {false, true}) {
                WarrenShape w = WarrenShape.generate(new Random(seed), grand);
                java.util.Set<WarrenShape.Spot> low = new HashSet<>();
                for (TreeShape.Cell c : w.tree()) if (c.dy() <= 2) low.add(new WarrenShape.Spot(c.dx(), c.dz()));
                java.util.Set<WarrenShape.Spot> seen = new HashSet<>();
                check(w.nests().size() <= w.burrows().size(), "at most one nest per burrow");
                fewest = Math.min(fewest, w.nests().size());
                for (WarrenShape.Spot n : w.nests()) {
                    check(seen.add(n), "no two nests on one column");
                    check(!low.contains(n), "no nest where the tree hangs within 2 of the ground (trunk included)");
                    check(!w.burrows().contains(n) && !w.dogs().contains(n), "a nest is never on a burrow or a dog's spot");
                    check(Math.max(Math.abs(n.dx()), Math.abs(n.dz())) <= WarrenShape.NEST_MAX_REACH, "a nest is within 3 columns of the trunk");
                    boolean beside = false;
                    for (WarrenShape.Spot b : w.burrows()) beside |= Math.abs(b.dx() - n.dx()) + Math.abs(b.dz() - n.dz()) == 1;
                    check(beside, "a nest is right beside a burrow hole");
                }
            }
        }
        check(fewest >= WarrenShape.MIN_DOGS, "every warren has at least as many nests as its minimum of dogs (fewest seen: " + fewest + ")");
        check(WarrenShape.generate(new Random(9)).equals(WarrenShape.generate(new Random(9))), "the same dice make the same nests");
        // a village's landmarks and burrow houses never meet a nest (spacing from every trunk: props 4+ columns from the footprint centre's trunk, houses 6+)
        for (int level = 1; level <= Settlement.MAX_LEVEL; level++) {
            for (int seed = 0; seed < 60; seed++) {
                Settlement st = Settlement.generate(new Random(seed * 31L + level), level);
                for (Settlement.Cluster c : st.clusters()) {
                    for (WarrenShape.Spot n : c.shape().nests()) {
                        int x = c.dx() + n.dx(), z = c.dz() + n.dz();
                        for (Settlement.House h : st.houses()) check(Math.max(Math.abs(x - h.dx()), Math.abs(z - h.dz())) > 1, "a nest is outside every house's 3x3");
                        for (Settlement.Prop p : st.props()) check(Math.max(Math.abs(x - p.dx()), Math.abs(z - p.dz())) > 1, "a nest is outside every landmark's 3x3");
                    }
                }
            }
        }
        // the glue, by grep: nests go in after the burrows are dug and BEFORE the dogs; the fallback dogs avoid them
        Path src = Path.of("src/main/java/com/bunnidogs/mod");
        String wf = Files.readString(src.resolve("worldgen/WarrenFeature.java"));
        int digAt = wf.indexOf("dig(level, ground.below(2));");
        int nestAt = wf.indexOf("for (WarrenShape.Spot spot : warren.nests())");
        int dogsAt = wf.indexOf("for (WarrenShape.Spot spot : warren.dogs())");
        check(digAt > 0 && nestAt > digAt && dogsAt > nestAt, "buildCluster: burrows, then nests, then dogs");
        check(wf.contains("ModBlocks.NEST.get().defaultBlockState(), FLAGS") && wf.contains("import com.bunnidogs.mod.registry.ModBlocks;"), "the nest is set with the same FLAGS as every other worldgen block");
        check(wf.contains("!warren.nests().contains(spot) && spawnDog("), "the fallback dogs are never put on a nest");
    }

    // U1 (docs/WARREN_PLAN.md): the den and its mouths, on a flat model world (ground blocks at dy <= -1), walked cell by cell
    static void den() throws Exception {
        System.out.println("level 1 dens and mouths (U1)");
        // the slope's heights: one down per block, never below the den floor, reaching it by the den's edge exactly when canReachDen says so
        for (int h = -3; h <= 3; h++) {
            for (int r = DenShape.MIN_RADIUS; r <= DenShape.MAX_RADIUS; r++) {
                boolean reaches = DenShape.standDy(h, r, DenShape.HALF) == -DenShape.DEPTH;
                check(reaches == DenShape.canReachDen(h, r), "canReachDen matches the standing level at the den's edge (h " + h + ", R " + r + ")");
                for (int u = DenShape.HALF; u < r; u++) {
                    int step = DenShape.standDy(h, r, u + 1) - DenShape.standDy(h, r, u);
                    check(step == 0 || step == 1, "the slope goes up by 0 or 1 per block moving outward");
                }
            }
        }
        check(DenShape.canReachDen(0, 7) && !DenShape.canReachDen(1, 7) && DenShape.canReachDen(2, 9) && DenShape.canReachDen(-3, 7), "flat ground needs R 7; ground 1 higher needs 8");
        for (int seed = 0; seed < 200; seed++) {
            DenShape.Den den = DenShape.generate(new Random(seed));
            check(new HashSet<>(den.dirs()).size() == 4, "the four sides come in some order");
            List<DenShape.Pos> nests = den.nestColumns();
            check(nests.size() >= DenShape.MIN_NESTS && nests.size() <= DenShape.MAX_NESTS && new HashSet<>(nests).size() == nests.size(), "3 or 4 nests, none twice");
            for (DenShape.Pos n : nests) {
                check(Math.abs(n.dx()) == DenShape.HALF && Math.abs(n.dz()) == DenShape.HALF, "a nest is in a corner of the den");
            }
            for (DenShape.Dir dir : DenShape.Dir.values()) {
                // the flat model: ground blocks everywhere at dy <= -1, except what the shape digs; the mound and the furnishing are solid
                int r = DenShape.MIN_RADIUS;
                Set<String> air = new HashSet<>();
                for (DenShape.Cell c : DenShape.denAir()) air.add(c.dx() + "," + c.dy() + "," + c.dz());
                for (DenShape.Cell c : DenShape.air(dir, r, 0)) check(air.add(c.dx() + "," + c.dy() + "," + c.dz()), "no air block listed twice");
                Set<String> solid = new HashSet<>();
                for (DenShape.Cell c : DenShape.mound(dir, r, 0)) {
                    String k = c.dx() + "," + c.dy() + "," + c.dz();
                    check(!air.contains(k), "the mound never fills its own hollow");
                    check(solid.add(k), "no mound block listed twice");
                }
                for (DenShape.Cell c : den.furnishing()) {
                    String k = c.dx() + "," + c.dy() + "," + c.dz();
                    if (c.kind() == DenShape.Kind.NEST) { air.remove(k); } else { solid.add(k); air.remove(k); }
                }
                // walk in from the doorstep: a standable cell has two free blocks above it and a solid one below; a step up needs three free in the lower column
                DenShape.Pos step = DenShape.at(dir, r + 1, 0);
                java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
                Set<String> seen = new HashSet<>();
                queue.add(new int[] {step.dx(), 0, step.dz()});
                seen.add(step.dx() + ",0," + step.dz());
                int[][] around = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                while (!queue.isEmpty()) {
                    int[] cur = queue.poll();
                    for (int[] a : around) {
                        int nx = cur[0] + a[0], nz = cur[2] + a[1];
                        if (Math.abs(nx) > 12 || Math.abs(nz) > 12) continue;
                        for (int dy = -1; dy <= 1; dy++) {
                            int ny = cur[1] + dy;
                            if (!standable(air, solid, nx, ny, nz)) continue;
                            int lo = Math.min(cur[1], ny);
                            int lowX = cur[1] <= ny ? cur[0] : nx, lowZ = cur[1] <= ny ? cur[2] : nz;
                            int highX = cur[1] <= ny ? nx : cur[0], highZ = cur[1] <= ny ? nz : cur[2];
                            int hi = Math.max(cur[1], ny);
                            boolean free = true;
                            for (int y = lo; y <= lo + (hi > lo ? 2 : 1); y++) free &= !isSolid(air, solid, lowX, y, lowZ);
                            for (int y = hi; y <= hi + 1; y++) free &= !isSolid(air, solid, highX, y, highZ);
                            if (free && seen.add(nx + "," + ny + "," + nz)) queue.add(new int[] {nx, ny, nz});
                        }
                    }
                }
                for (DenShape.Pos n : nests) {
                    check(seen.contains(n.dx() + "," + (-DenShape.DEPTH) + "," + n.dz()), "every nest can be walked to from the doorstep (seed " + seed + ", " + dir + ")");
                }
                // closed: nothing reachable above the doorstep level except the mound itself, and the slope outside the mound stays under the ground
                for (String k : seen) {
                    String[] t = k.split(",");
                    check(Integer.parseInt(t[1]) <= 0, "walking in never leads above the ground level");
                }
                for (DenShape.Cell c : DenShape.air(dir, r, 0)) {
                    DenShape.Pos p = new DenShape.Pos(c.dx(), c.dz());
                    boolean inMound = DenShape.padColumns(dir, r).contains(p);
                    check(inMound || c.dy() + 1 <= -1, "the slope outside the mound is under the ground (its air stays below dy -1)");
                }
            }
        }
        // U2 terrain behaviour is checked by tools/warren/WarrenNetworkCheck.java.
    }

    private static boolean isSolid(Set<String> air, Set<String> solid, int x, int y, int z) {
        String k = x + "," + y + "," + z;
        return solid.contains(k) || (y <= -1 && !air.contains(k));
    }

    private static boolean standable(Set<String> air, Set<String> solid, int x, int y, int z) {
        return !isSolid(air, solid, x, y, z) && !isSolid(air, solid, x, y + 1, z) && isSolid(air, solid, x, y - 1, z);
    }
}
