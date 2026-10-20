import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Save-id/lineage contracts and pure ocean fitting; optional art export uses the actual authored Java layout. */
public final class IslandCheck {
    private static void require(boolean value,String message) { if (!value) throw new AssertionError(message); }
    private static Genome fixed(Genome genome,Gene gene,String allele) {
        int id=gene.alleleNamed(allele).id(); return genome.with(gene,0,id).with(gene,1,id);
    }
    public static void main(String[] args) throws Exception {
        var coat=Gene.COAT.alleleNamed("seal"); var legs=Gene.LEGS.alleleNamed("paddle"); var diet=Gene.DIET.alleleNamed("shore_forager");
        require(coat.id()==12 && legs.id()==8 && diet.id()==12,"New alleles append saved ids without shifting old genes");
        String[] oldCoats={"tan","cream","brown","spotted","snow","sand","moss","pale","slate","ember","void","glow"};
        for (int i=0;i<oldCoats.length;i++) require(Gene.COAT.allele(i).name().equals(oldCoats[i]),"Existing coat save ids are stable");
        require(Gene.LEGS.allele(6).name().equals("phase") && Gene.LEGS.allele(7).name().equals("webbed")
                && Gene.DIET.allele(11).name().equals("chorus_eater"),"Prior wild leg/diet ids must remain stable");
        for (var allele:List.of(coat,legs,diet)) require(allele.isWild() && !allele.isRare() && allele.weight()==0
                && allele.lineage()==Lineage.ISLAND,"The island requires exploration rather than a common/mutation roll");
        var island=fixed(fixed(fixed(Genome.DEFAULT,Gene.COAT,"seal"),Gene.LEGS,"paddle"),Gene.DIET,"shore_forager");
        require(Genome.parse(island.toString()).orElseThrow().equals(island)
                && Genome.fromSynced(island.syncLow(),island.syncHigh()).equals(island),"Appended nibble ids round-trip without changing genome/sync formats");
        var child=Genome.breed(island,Genome.DEFAULT,new Random(38),0).genome();
        require(child.alleleId(Gene.COAT,0)==12 && child.alleleId(Gene.COAT,1)==0
                && child.alleleId(Gene.LEGS,0)==8 && child.alleleId(Gene.LEGS,1)==0
                && child.alleleId(Gene.DIET,0)==12 && child.alleleId(Gene.DIET,1)==0,"Crossbreeding conserves each maternal/paternal island allele");
        require(Adaptations.oxygenBonus(island)==1 && Adaptations.swimBonus(island)==1 && Adaptations.swimmingFeet(island)
                && island.traits().legScale()==.85F && Diet.of(island)==Diet.SHORE_FORAGER,"Visible island traits carry their intended mechanical tradeoffs");
        var hidden=island.with(Gene.COAT,1,Gene.COAT.alleleNamed("spotted").id()).with(Gene.LEGS,1,Gene.LEGS.alleleNamed("springy").id());
        require(Adaptations.oxygenBonus(hidden)==0 && !Adaptations.swimmingFeet(hidden),"Mutation expression masks coastal traits normally");
        require(Juice.effects(island,Stats.ZERO,0).stream().anyMatch(s->s.id().equals("minecraft:water_breathing")),"Inherited coat reaches the existing player infusion loop");
        int seen=0;
        for (var lineage:Lineage.values()) {
            var founder=Genome.randomFounder(lineage,new Random(427));
            for (var gene:Gene.values()) for (int slot=0;slot<2;slot++) {
                var allele=gene.allele(founder.alleleId(gene,slot));
                require(!allele.isRare() && (!allele.isWild() || allele.lineage()==lineage),"Every founder stays in its own wild/common pool");
                if (allele.lineage()==Lineage.ISLAND) seen++;
            }
        }
        require(seen>=3,"The island founder recipe supplies its own coastal genes");
        require(IslandSite.bed((x,z)->new IslandSite.Column(50,true,true)).orElseThrow()==49,"An open shallow-ocean site has a known rooted bed");
        require(IslandSite.bed((x,z)->new IslandSite.Column(62,true,true)).isEmpty()
                && IslandSite.bed((x,z)->new IslandSite.Column(20,true,true)).isEmpty()
                && IslandSite.bed((x,z)->new IslandSite.Column(50,x!=96||z!=0,true)).isEmpty()
                && IslandSite.bed((x,z)->new IslandSite.Column(50,true,x!=0||z!=0)).isEmpty(),"Shoals, deep beds, sampled mainland and an exposed center refuse a refuge");
        for (int bed=-33;bed<=-5;bed++) require(IslandShape.bottom(0,0,bed)==bed,"The island core reaches every supported seabed depth");
        var cells=IslandShape.decorations(4711); var positions=new HashSet<IslandShape.Spot>();
        for (var c:cells) require(positions.add(new IslandShape.Spot(c.dx(),c.dy(),c.dz()))
                && Math.abs(c.dx())<=IslandShape.RADIUS && Math.abs(c.dz())<=IslandShape.RADIUS
                && c.dy()<=IslandShape.ABOVE,"Final authored cells have unique bounded locations");
        for (var spot:IslandShape.FOUNDERS) require(cells.stream().anyMatch(c->c.dx()==spot.dx()&&c.dz()==spot.dz()
                && c.dy()==spot.dy()-1&&c.material()==IslandShape.Mat.NEST),"Each actual founder starts over a sheltered floor Nest");
        require(cells.equals(IslandShape.decorations(4711)),"Saved layout seeds regenerate the same native fixtures");
        var pale=fixed(Genome.DEFAULT,Gene.COAT,"pale");
        require(CaveSight.qualified(pale,Stats.of(0,0,0,20,0)) && !CaveSight.qualified(pale,Stats.of(0,0,0,19,0))
                && !CaveSight.qualified(pale.with(Gene.COAT,1,coat.id()),Stats.of(0,0,0,20,0)),"Cave guidance requires real Senses breeding and expressed pale fur");
        var library=new Pedigree();
        for (var lineage:Lineage.values()) if (lineage!=Lineage.MEADOW) {
            var signature=Founders.recipe(lineage).stream().filter(s->s.resolve().isWild()).findFirst().orElseThrow();
            var genome=Genome.DEFAULT.with(signature.gene(),1,signature.resolve().id());
            library.add(new Pedigree.Entry(genome,Optional.empty(),Optional.empty(),Optional.of(UUID.randomUUID()),Optional.empty()));
        }
        require(GeneticDiscoveries.wildLineages(library).size()==8 && GeneticDiscoveries.wildLineageCount()==8
                && !GeneticDiscoveries.hasBredMutation(library),"The saved animal snapshots cover eight wild origins, never their unlogged parents");
        var mutant=Genome.DEFAULT.with(Gene.COAT,1,Gene.COAT.alleleNamed("spotted").id());
        library.add(new Pedigree.Entry(mutant,Optional.of(Genome.DEFAULT),Optional.of(Genome.DEFAULT),Optional.empty(),Optional.empty()));
        require(GeneticDiscoveries.hasBredMutation(library),"A genuine bred snapshot can carry the original mutation-only discovery goal");
        if (args.length>0) {
            var columns=new ArrayList<Object>(); long seed=4711;
            for (int x=-IslandShape.RADIUS;x<=IslandShape.RADIUS;x++) for (int z=-IslandShape.RADIUS;z<=IslandShape.RADIUS;z++) {
                if (!IslandShape.contains(x,z,seed)) continue;
                int top=IslandShape.top(x,z);
                columns.add(List.of(x,z,IslandShape.bottom(x,z,-16),top,IslandShape.terrain(x,top,z,seed).name()));
            }
            Files.writeString(Path.of(args[0]),new GsonBuilder().create().toJson(Map.of("seed",seed,"bed",-16,"columns",columns,"decorations",cells,"founders",IslandShape.FOUNDERS))+"\n");
        }
        System.out.println("Island rules OK: appended saves, inherited traits, founder isolation, supported ocean fitting and authored layout.");
    }
}
