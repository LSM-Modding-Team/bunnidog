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
    public static void main(String[] args) {
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
}
