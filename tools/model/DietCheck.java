import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Compatibility and behavior contracts for the seventh gene and food policies, without starting Minecraft. */
public final class DietCheck {
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static Genome genome(String text) { return Genome.parse(text).orElseThrow(); }
    private static Genome diet(Genome base, String name) {
        int id = Gene.DIET.alleleNamed(name).id();
        return base.with(Gene.DIET,0,id).with(Gene.DIET,1,id);
    }

    public static void main(String[] args) throws Exception {
        String old = "33-22-33-33-33-33";
        Genome legacy = genome(old);
        require(legacy.bodyCode().equals(old+"-00") && Diet.of(legacy) == Diet.PLAIN,"Legacy genomes must keep six pairs and gain plain diet");
        require(genome(old.replace("-","")).equals(legacy),"Unseparated old genomes must still load");
        for (Allele a : Gene.DIET.alleles()) for (Allele b : Gene.DIET.alleles()) {
            Genome g = legacy.with(Gene.DIET,0,a.id()).with(Gene.DIET,1,b.id());
            require(Genome.fromSynced(g.syncLow(),g.syncHigh()).equals(g),"Client sync lost the diet sign bit: "+g);
            require(genome(g.toString()).equals(g),"New genome text must round-trip");
            require((g.packed() & 0xFFFFFFFFFFFFL) == legacy.packed(),"Diet must not move any saved old gene");
            require(g.traits().equals(legacy.traits()),"Diet must not change body or fertility multipliers");
            require(Trends.terms(Gene.DIET,g.expressed(Gene.DIET)).isEmpty(),"Food preferences do not add stat drift");
            require(Juice.effects(g,Stats.ZERO,0).equals(Juice.effects(legacy,Stats.ZERO,0)),"Diet must not change juice recipes");
        }
        require(diet(legacy,"chorus_eater").syncHigh() < 0,"Exercise a real negative sync high word");
        require(Genome.parse(old+"-dd").isEmpty() && Genome.parse(old+"-0").isEmpty(),"Malformed diets must be rejected");
        require((new Genome(-1L).packed() >>> 56) == 0,"Sanitization must clear unused bits");

        Seed seed = Seed.parse(old+"/1,2,3,4,5|"+old+"/5,4,3,2,1|4.5|mutagen").orElseThrow();
        require(seed.mother().equals(legacy) && seed.father().equals(legacy) && seed.mutagen(),"Legacy seeds must retain parents, stats, quality and treatment");
        require(Seed.parse(seed.toString()).orElseThrow().equals(seed),"Upgraded seeds must round-trip");
        Tissue tissue = Tissue.parse(old+"/1,2,3,4,5").orElseThrow();
        require(tissue.genome().equals(legacy) && Tissue.parse(tissue.toString()).orElseThrow().equals(tissue),"Old tissue samples must survive rewriting");
        Infusion infusion = Infusion.parse(old+"/1,2,3,4,5|4.5").orElseThrow();
        require(infusion.genome().equals(legacy) && Infusion.parse(infusion.toString()).orElseThrow().equals(infusion),"Old infused juice must survive rewriting");
        Pedigree book = Pedigree.parse(old+"<"+old+"+"+old);
        require(book.entries().size() == 1 && book.entries().getFirst().genome().equals(legacy),"Legacy pedigree entry must survive");
        require(Pedigree.parse(book.toString()).entries().equals(book.entries()),"Upgraded pedigree must round-trip");
        TableSheet table = TableSheet.parse(old+"/1,2,3,4,5|"+old+"/5,4,3,2,1").orElseThrow();
        require(table.spreadCount() == 5 && table.sample(0).genome().equals(legacy),"Old Genetics Table samples must load with current analysis spreads");

        Genome a = Genome.DEFAULT.with(Gene.DIET,1,Gene.DIET.alleleNamed("grazer").id());
        Genome b = Genome.DEFAULT.with(Gene.DIET,1,Gene.DIET.alleleNamed("hunter").id());
        Cross cross = new Cross(new Tissue(a,Stats.ZERO),new Tissue(b,Stats.ZERO));
        Map<String,Double> odds = new HashMap<>();
        cross.looks(Gene.DIET).forEach(o -> odds.put(o.allele().name(),o.chance()));
        require(odds.equals(Map.of("plain",0.25,"grazer",0.25,"hunter",0.5)),"Diet forecast must follow dominance and Mendelian odds");
        Random rng = new Random(64271);
        for (int n = 0; n < 256; n++) {
            Genome child = Genome.breed(a,b,rng,0).genome();
            require(Set.of(0,1).contains(child.alleleId(Gene.DIET,0)) && Set.of(0,3).contains(child.alleleId(Gene.DIET,1)),"Diet alleles must come from the correct parent");
        }
        for (Lineage lineage : Lineage.values()) {
            Genome founder = Genome.randomFounder(lineage,new Random(84));
            require(Diet.of(founder) != null,"Every lineage diet must resolve");
            require(lineage == Lineage.MEADOW || Founders.recipe(lineage).stream().anyMatch(s -> s.gene() == Gene.DIET),"Wild lineages must carry their own diet signature");
        }

        for (Diet d : Diet.values()) {
            require(Plain.hasNote(Gene.DIET,Gene.DIET.alleleNamed(d.key())),"Every diet needs readable food guidance");
            for (String tag : d.tags()) {
                String path = "/data/bunnidogs/tags/item/diet/"+tag+".json";
                var stream = DietCheck.class.getResourceAsStream(path);
                require(stream != null,"Missing diet tag: "+tag);
                try (var reader = new InputStreamReader(stream,StandardCharsets.UTF_8)) {
                    var data = JsonParser.parseReader(reader).getAsJsonObject();
                    require(!data.get("replace").getAsBoolean() && !data.getAsJsonArray("values").isEmpty(),"Diet tags must be extendable and contain foods");
                }
            }
        }

        require(Foraging.handFood(true,false,true,false) && Foraging.handFood(true,false,false,true),"Diet food heals adults or grows pups");
        require(!Foraging.handFood(true,false,false,false) && !Foraging.handFood(true,true,true,true) && !Foraging.handFood(false,false,true,true),"Do not waste food or route guavas through healing");
        require(Foraging.canForage(true,true,true,false,false),"A wounded adult with a home can seek food by day");
        for (int flag = 0; flag < 5; flag++) {
            boolean[] v = {true,true,true,false,false}; v[flag] = !v[flag];
            require(!Foraging.canForage(v[0],v[1],v[2],v[3],v[4]),"Each forage gate matters");
        }
        require(Foraging.insideHome(256) && !Foraging.insideHome(257) && !Foraging.insideHome(Double.NaN),"Foraging must stay within the home boundary");
        require(Foraging.canGraze(Diet.GRAZER,Foraging.Meal.HAY,false),"Non-destructive hay feeding is allowed without mob griefing");
        require(!Foraging.canGraze(Diet.GRAZER,Foraging.Meal.GRASS,false) && !Foraging.canGraze(Diet.FORAGER,Foraging.Meal.PRODUCE,false),"World-changing meals obey mob griefing");
        require(Foraging.canGraze(Diet.GRAZER,Foraging.Meal.WHEAT,true) && !Foraging.canGraze(Diet.GRAZER,Foraging.Meal.PRODUCE,true),"Grazers eat wheat, not root crops");
        require(Foraging.canGraze(Diet.FORAGER,Foraging.Meal.PRODUCE,true) && !Foraging.canGraze(Diet.FORAGER,Foraging.Meal.HAY,true),"Foragers eat produce, not hay");
        require(!Foraging.canGraze(Diet.PLAIN,Foraging.Meal.HAY,true) && Diet.OMNIVORE.hunts() && Diet.MOUNTAIN_GRAZER.grazesHay(),"Diet activities follow expressed food preferences");
        require(Diet.FISHER.fishes() && Diet.FISHER.seeksPrey() && !Diet.FISHER.hunts()
                && !Diet.HUNTER.fishes() && !Diet.OMNIVORE.fishes(),"Fishing must retain its own food/prey preference");
        require(com.bunnidogs.mod.genetics.Refuge.safer(1,16) && !com.bunnidogs.mod.genetics.Refuge.safer(25,16)
                && !com.bunnidogs.mod.genetics.Refuge.safer(1,Double.POSITIVE_INFINITY),"A refuge destination must be finite, safe and farther from danger");
        require(Foraging.prey(true,false,false,false,false,false),"Tagged adult prey is eligible");
        require(!Foraging.prey(false,false,false,false,false,false),"Untagged creatures are not prey");
        for (int flag = 1; flag < 6; flag++) {
            boolean[] v = {true,false,false,false,false,false}; v[flag] = true;
            require(!Foraging.prey(v[0],v[1],v[2],v[3],v[4],v[5]),"Protect babies, players, bunnidogs, tame animals and allies even if tagged");
        }

        // Worst-case two-line outcomes for all seven genes: no overflow into the footer.
        Genome mixedA = Genome.DEFAULT, mixedB = Genome.DEFAULT;
        for (Gene gene : Gene.values()) {
            mixedA = mixedA.with(gene,0,0).with(gene,1,1);
            mixedB = mixedB.with(gene,0,2).with(gene,1,3);
        }
        Page.Spread forecast = ForecastSheet.spread(new Tissue(mixedA,Stats.ZERO),new Tissue(mixedB,Stats.ZERO),Metrics.ESTIMATE);
        require(forecast.right().hotspots().size() == Gene.COUNT,"Forecast must include every gene");
        for (Page.Hotspot h : forecast.right().hotspots()) require(h.y()+h.h() <= 138,"Forecast rows must not overlap the footer");
        for (Diet d : Diet.values()) {
            Page page = SheetLayout.sample(new Tissue(diet(legacy,d.key()),Stats.ZERO),Metrics.ESTIMATE).left();
            require(page.pairs().size() == Gene.COUNT,"Animal/sample sheets need seven linked code pairs");
            var rows = page.hotspots().stream().filter(h -> h.pair() >= 0).toList();
            require(rows.size() == Gene.COUNT,"Animal/sample sheets need a Diet row");
            require(rows.getLast().y()+rows.getLast().h() <= 145,"Gene rows must leave the legend clear");
            for (Page.Text t : page.texts()) require(t.y()+Page.LINE <= Page.HEIGHT,"Sheet text outside its page");
        }
        System.out.println("Diets OK: legacy payloads, signed sync, inheritance/forecast, food/home/prey policies, tags and seven-row page clearance.");
    }
}
