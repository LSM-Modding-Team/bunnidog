import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;

/** Exact reference crosses and the pages players read; no random/mutation stress loop. */
public final class BreedingLabCheck {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static void near(double actual, double expected) {
        require(Math.abs(actual - expected) < 1e-6, "Expected " + expected + ", got " + actual);
    }
    private static Tissue tissue(Genome genome) { return new Tissue(genome, Stats.ZERO); }
    private static Genome pair(Gene gene, int a, int b) { return Genome.DEFAULT.with(gene, 0, a).with(gene, 1, b); }
    private static Pedigree.Entry entry(Genome genome, int id) {
        return new Pedigree.Entry(genome, Optional.of(Genome.DEFAULT), Optional.of(Genome.DEFAULT),
                Optional.of(new UUID(0, id)), Optional.empty(), Optional.of(Stats.ZERO), false);
    }
    private static Cross.Inherited allele(Cross cross, Gene gene, int id) {
        return cross.inherited(gene).stream().filter(o -> o.allele().id() == id).findFirst().orElseThrow();
    }
    private static void page(Page page) {
        for (Page.Text text : page.texts()) {
            int width = Metrics.ESTIMATE.width(text.text());
            require(text.y() >= 0 && text.y() + Page.LINE <= Page.HEIGHT, "Text outside page: " + text);
            require(text.right() ? text.x() - width >= 0 && text.x() <= Page.WIDTH
                    : text.x() >= 0 && text.x() + width <= Page.WIDTH, "Text outside width: " + text);
        }
        for (Page.Hotspot hot : page.hotspots()) require(hot.y() + hot.h() <= Page.HEIGHT, "Hover outside page");
    }
    private static void recordFilters() {
        int bat = Gene.EARS.alleleNamed("bat").id(), huge = Gene.EARS.alleleNamed("huge").id();
        var expressed = pair(Gene.EARS,bat,bat);
        var hidden = pair(Gene.EARS,huge,bat);
        var book = new Pedigree();
        book.add(entry(Genome.DEFAULT,1));
        book.add(new Pedigree.Entry(hidden,Optional.empty(),Optional.empty(),Optional.of(new UUID(0,2)),
                Optional.of("BD-AAAA02"),Optional.of(Stats.of(0,0,0,25,0)),false));
        book.add(new Pedigree.Entry(expressed,Optional.of(Genome.DEFAULT),Optional.of(hidden),Optional.of(new UUID(0,3)),
                Optional.of("BD-AAAA03"),Optional.of(Stats.of(1,-2,0,20,15)),false));
        book.add(new Pedigree.Entry(expressed,Optional.empty(),Optional.empty(),Optional.of(new UUID(0,4)),
                Optional.empty(),Optional.of(Stats.of(0,0,0,40,30)),true));
        book.add(new Pedigree.Entry(Genome.DEFAULT,Optional.empty(),Optional.empty()));
        String original = book.toString();
        require(RecordFilter.indices(book,"").equals(List.of(0,1,2,3,4)),"Clearing a query must restore original order");
        require(RecordFilter.indices(book,"bat").equals(List.of(1,2,3)),"Trait words must find both expressed and hidden copies");
        require(RecordFilter.indices(book,"  SHOWN:bat adult senses>=20  ").equals(List.of(2)),"Terms must combine case-insensitively for real scout candidates");
        require(RecordFilter.indices(book,"carried:bat").equals(List.of(1)),"Carrier queries must exclude expressed/fixed copies");
        require(RecordFilter.indices(book,"tag:bd-aaaa03 senses=20 bloom>10").equals(List.of(2)),"Tags and exact/strict bred-stat comparisons must combine");
        require(RecordFilter.indices(book,"speed<0 vitality>=1").equals(List.of(2)),"Speed aliases and negative bred points must be searchable");
        require(RecordFilter.indices(book,"senses<=0").equals(List.of(0)) && RecordFilter.indices(book,"pup").equals(List.of(3)),
                "Unknown legacy stats/age must not masquerade as zero points or an adult");
        require(RecordFilter.indices(book,"unknown").equals(List.of(4)),"Legacy records must remain discoverable");
        require(RecordFilter.indices(book,"senses>=999999999999999999999999999").isEmpty()
                && RecordFilter.indices(book,"senses>=oops").isEmpty(),"Incomplete/oversized comparisons must safely match nothing");
        var contents = BookContents.spread(book,0,Metrics.ESTIMATE,"shown:bat adult senses>=20");
        require(contents.left().links().size() == 1 && contents.left().links().getFirst().spread() == 3,
                "Filtered contents must retain original animal numbers and link destinations");
        require(RecordFilter.turn(book,"shown:bat adult",0,1) == 3 && RecordFilter.turn(book,"shown:bat adult",3,1) == 3
                && RecordFilter.turn(book,"shown:bat adult",3,-1) == 0,"Arrows must visit matches and stop at the result boundaries");
        require(RecordFilter.turn(book,"shown:bat adult",2,1) == 3 && RecordFilter.turn(book,"",0,1) == 1,
                "Parent-link detours and clearing the filter must retain useful original-order navigation");
        var registry = RegistryContents.spread(book,0,Metrics.ESTIMATE,"tag:bd-aaaa03");
        require(registry.left().links().size() == 1 && registry.left().links().getFirst().spread() == 3,
                "Registry queries must retain original representative links");
        require(BookContents.spread(book,3,Metrics.ESTIMATE,"shown:bat adult").right().links()
                .equals(BookContents.spread(book,3,Metrics.ESTIMATE).right().links()),"Unmatched parents must keep their original sheet links");
        page(contents.left()); page(contents.right()); page(registry.left()); page(registry.right());
        require(book.toString().equals(original),"Queries must not rewrite saved records, identities or parents");
    }
    private static void plannedCrosses() {
        int star = Gene.FERTILITY.alleleNamed("starborn").id();
        Tissue a = new Tissue(pair(Gene.FERTILITY,star,star),Stats.of(1,2,3,4,40));
        Tissue b = new Tissue(Genome.DEFAULT,Stats.of(5,6,7,8,20));
        var choices = new BreedingPlan(1,2,true,3);
        var cross = new Cross(a,b,choices);
        var expected = Seed.of(a.genome(),a.stats(),b.genome(),b.stats(),GuavaTier.average(1,2)).withMutagen();
        require(cross.seed().equals(expected),"Plans must use the real seed's guava average, starborn bonus and Mutagen flag exactly once");
        near(choices.guavaQuality(),1.5); near(cross.mutationChance(),Seed.MUTAGEN_MUTATION_FLOOR);
        near(cross.effectiveQuality(),expected.quality()+1.5);
        for (Stat stat : Stat.values()) near(cross.expectedPoints(stat),cross.startPoints(stat)+expected.drift(1.5)[stat.ordinal()]);
        var ordinary = new Cross(a,b);
        for (Gene gene : Gene.values()) require(cross.looks(gene).equals(ordinary.looks(gene))
                && cross.inherited(gene).equals(ordinary.inherited(gene)),"Guavas and treatment cannot alter pre-mutation Mendelian odds");
        near(cross.litter(),ordinary.litter());
        var table = new TableSheet(Optional.of(a),Optional.of(b),choices);
        require(TableSheet.parse(table.toString()).orElseThrow().equals(table),"Extended plans must round-trip with both exact sample snapshots");
        require(TableSheet.parse(new TableSheet(Optional.of(a),Optional.of(b)).toString()).orElseThrow().plan().equals(BreedingPlan.PLAIN),
                "Old A|B remains an untreated plain-guava plan");
        for (String invalid : List.of("-1,0,0","0,8,0","1,2,maybe","NaN,0,0","1,2","1,2,1,-1","1,2,1,256","1,2,1,1,1"))
            require(TableSheet.parse(a+"|"+b+"|"+invalid).isEmpty(),"Malformed plans must refuse: "+invalid);
        for (var choice : List.of(BreedingPlan.PLAIN,choices,new BreedingPlan(7,7,true,255))) {
            var spread = new TableSheet(Optional.of(a),Optional.of(b),choice).spread(TableSheet.FORECAST,Metrics.ESTIMATE);
            page(spread.left()); page(spread.right());
        }
        var forecast = table.spread(TableSheet.FORECAST,Metrics.ESTIMATE);
        require(forecast.left().hotspots().stream().anyMatch(h -> h.text().contains("Golden Guava + Diamond Guava"))
                && forecast.right().hotspots().stream().allMatch(h -> h.text().contains("50.0%")),
                "Visible hover working must name the exact feed pair and planned mutation rate");
    }
    public static void main(String[] args) {
        recordFilters();
        namedLines();
        plannedCrosses();
        require(!Social.gatheringTime(5999) && Social.gatheringTime(6000) && Social.gatheringTime(8999)
                && !Social.gatheringTime(9000) && Social.gatheringTime(30000), "Gathering window must wrap with the native day");
        require(Show.rank(Stats.ZERO) == Show.Tier.NONE, "Unbred stats must not earn a ribbon");
        require(Show.rank(Stats.of(5,5,5,5,5)) == Show.Tier.BRONZE, "Bronze boundary");
        require(Show.rank(Stats.of(20,20,20,20,20)) == Show.Tier.SILVER, "Silver boundary");
        require(Show.rank(Stats.of(50,50,50,50,50)) == Show.Tier.GOLD, "Gold boundary");
        for (Stat stat : Stat.values()) {
            require(Show.rank(Stats.of(50,50,50,50,50).with(stat,4)) == Show.Tier.NONE, "Every stat must qualify");
            require(Show.rank(Stats.of(50,50,50,50,50).with(stat,19)) == Show.Tier.BRONZE, "One stat below silver must limit the award");
            require(Show.rank(Stats.of(50,50,50,50,50).with(stat,49)) == Show.Tier.SILVER, "One stat below gold must limit the award");
        }
        for (Gene gene : Gene.values()) {
            Cross carriers = new Cross(tissue(pair(gene, 0, 1)), tissue(pair(gene, 0, 1)));
            near(allele(carriers, gene, 0).carried(), .75);
            near(allele(carriers, gene, 0).fixed(), .25);
            near(allele(carriers, gene, 1).carried(), .75);
            near(allele(carriers, gene, 1).fixed(), .25);
            Cross disjoint = new Cross(tissue(pair(gene, 0, 1)), tissue(pair(gene, 2, 3)));
            require(disjoint.inherited(gene).size() == 4, "Both copies of both parents must be visible");
            for (var odds : disjoint.inherited(gene)) { near(odds.carried(), .5); near(odds.fixed(), 0); }
            require(disjoint.inherited(gene).equals(new Cross(disjoint.b(), disjoint.a()).inherited(gene)), "Reversing samples must preserve inherited odds");
            Cross fixed = new Cross(tissue(pair(gene, 1, 1)), tissue(pair(gene, 1, 1)));
            near(allele(fixed, gene, 1).carried(), 1);
            near(allele(fixed, gene, 1).fixed(), 1);
        }
        int glow = Gene.COAT.alleleNamed("glow").id();
        Cross recessive = new Cross(tissue(pair(Gene.COAT, 0, glow)), tissue(pair(Gene.COAT, 0, glow)));
        near(allele(recessive, Gene.COAT, glow).carried(), .75);
        near(allele(recessive, Gene.COAT, glow).fixed(), .25);
        near(recessive.looks(Gene.COAT).stream().filter(o -> o.allele().id() == glow).findFirst().orElseThrow().chance(), .25);
        near(new Cross(tissue(Genome.DEFAULT), tissue(Genome.DEFAULT)).atLeastOneInLitter(.25), .4375);
        int prolific = Gene.FERTILITY.alleleNamed("prolific").id();
        Cross fractional = new Cross(tissue(Genome.DEFAULT), tissue(pair(Gene.FERTILITY, prolific, prolific)));
        near(fractional.litter(), 2.5);
        near(fractional.atLeastOneInLitter(.25), .5078125);
        near(fractional.atLeastOneInLitter(0), 0);
        near(fractional.atLeastOneInLitter(1), 1);
        near(new Cross(tissue(Genome.DEFAULT), tissue(Genome.DEFAULT)).fixedPupChance(), 1);
        near(new Cross(tissue(pair(Gene.COAT, 0, 0)), tissue(pair(Gene.COAT, 1, 1))).fixedPupChance(), 0);
        Genome allCarriers = Genome.DEFAULT;
        for (Gene gene : Gene.values()) allCarriers = allCarriers.with(gene, 1, 1);
        near(new Cross(tissue(allCarriers), tissue(allCarriers)).fixedPupChance(), 1.0 / 128);

        Genome mixedA = Genome.DEFAULT, mixedB = Genome.DEFAULT;
        for (Gene gene : Gene.values()) {
            mixedA = mixedA.with(gene, 0, 0).with(gene, 1, 1);
            mixedB = mixedB.with(gene, 0, gene.alleleCount() - 1).with(gene, 1, 2);
        }
        TableSheet table = new TableSheet(Optional.of(tissue(mixedA)), Optional.of(tissue(mixedB)));
        require(TableSheet.parse(table.toString()).orElseThrow().equals(table), "Old A|B payload must remain exact");
        require(table.spreadCount() == 5 && table.isForecast(2) && !table.isForecast(3) && table.isAnalysis(4), "Table analysis must have distinct pages");
        for (int n = 2; n < table.spreadCount(); n++) {
            Page.Spread spread = table.spread(n, Metrics.ESTIMATE);
            require(spread.left().portrait() == null && spread.right().portrait() == null, "Analysis has no false individual portrait");
            page(spread.left()); page(spread.right());
        }
        Page.Spread plan = table.spread(TableSheet.SPLICING, Metrics.ESTIMATE);
        require(plan.left().hotspots().size() == Gene.COUNT && plan.right().hotspots().size() == Gene.COUNT, "Both splice directions need all genes");
        require(plan.left().hotspots().getFirst().text().contains("Replace copy 2"), "Preview must replace the less dominant copy");
        TableSheet same = new TableSheet(Optional.of(tissue(Genome.DEFAULT)), Optional.of(tissue(Genome.DEFAULT)));
        require(same.spread(TableSheet.SPLICING, Metrics.ESTIMATE).left().hotspots().stream().allMatch(h -> h.text().contains("Already carried")), "Refused splices must be explicit");

        Pedigree registry = new Pedigree();
        for (int i = 0; i < Pedigree.MAX_ENTRIES; i++) {
            Genome g = pair(Gene.COAT, i % Gene.COAT.alleleCount(), i % Gene.COAT.alleleCount());
            g = g.with(Gene.EARS, 0, i / Gene.COAT.alleleCount()).with(Gene.EARS, 1, i / Gene.COAT.alleleCount());
            require(Purebreds.add(registry, entry(g, i + 1)) == Purebreds.Result.ADDED, "Distinct stable lines must register");
        }
        require(Purebreds.add(registry, entry(Genome.DEFAULT, 99)) == Purebreds.Result.ALREADY, "Duplicates must not fill the registry");
        require(Purebreds.add(registry, entry(pair(Gene.EARS, 3, 3), 100)) == Purebreds.Result.FULL, "Registry must stay bounded");
        require(Purebreds.read(registry.toString()).entries().equals(registry.entries()), "Registry snapshots must round-trip");
        require(Purebreds.eligibility(entry(pair(Gene.DIET, 0, 1), 101)) == Purebreds.Result.MIXED, "Diet must be fixed too");
        require(Purebreds.describe(pair(Gene.DIET, 0, 1)).contains("Mixed pairs: Diet"), "Calipers guide must identify unfinished genes");
        page(RegistryContents.spread(new Pedigree(), 0, Metrics.ESTIMATE).right());
        var index = RegistryContents.spread(registry, 0, Metrics.ESTIMATE);
        page(index.left()); page(index.right());
        require(index.left().links().size() + index.right().links().size() == Pedigree.MAX_ENTRIES, "Full index must link every line");
        System.out.println("Breeding lab OK: recessive/carrier/fixed odds, integer/fractional litters, splice pages and stable-line registry.");
    }

    private static void namedLines() {
        var book = new Pedigree(); book.add(entry(Genome.DEFAULT,101));
        var names = new LineageNames(); String label = "W".repeat(LineageNames.MAX_CODE_POINTS);
        require(names.rename(Genome.DEFAULT,label),"Maximum-length keeper name must be accepted"); book.setLineageNames(names);
        require(LineageNames.parse(names.toString()).name(Genome.DEFAULT).orElseThrow().equals(label),"Names round-trip separately from legacy pedigree text");
        var contents = BookContents.contents(book,Metrics.ESTIMATE);
        var registry = RegistryContents.spread(book,0,Metrics.ESTIMATE);
        var sheet = SheetLayout.sheet(book,0,Metrics.ESTIMATE);
        for (var spread : List.of(contents,registry,sheet)) { page(spread.left()); page(spread.right()); }
        require(contents.left().hotspots().stream().anyMatch(h -> h.text().contains(label))
                && sheet.left().hotspots().stream().anyMatch(h -> h.text().contains(label)),"Clipped names must remain readable in full hovers");
        require(RecordFilter.indices(book,"line:WWW").equals(List.of(0)),"Named lines participate in local original-order search");
        require(names.rename(Genome.DEFAULT,"adult vitality>=100"),"Literal words that resemble query syntax remain valid personal labels"); book.setLineageNames(names);
        require(RecordFilter.indices(book,"vitality>=100").isEmpty() && RecordFilter.indices(book,"pup").isEmpty(),"Names cannot bypass age or numeric predicates");
        var old = Pedigree.parse(book.toString());
        require(old.entries().equals(book.entries()) && old.lineageName(Genome.DEFAULT).isEmpty(),"Names do not rewrite or extend the old snapshot format");
    }
}
