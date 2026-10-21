import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/** Language contracts and real pure readouts; never starts a client or edits a save. */
public final class LocalizationCheck {
    private static void require(boolean value,String message) { if (!value) throw new AssertionError(message); }
    private static Map<String,String> language(String locale) throws IOException {
        var stream=LocalizationCheck.class.getResourceAsStream("/assets/bunnidogs/lang/"+locale+".json");
        require(stream!=null,"Missing locale: "+locale);
        var result=new LinkedHashMap<String,String>();
        try (var reader=new JsonReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
            reader.beginObject();
            while (reader.hasNext()) { String key=reader.nextName(); require(!result.containsKey(key),"Duplicate translation: "+key); result.put(key,reader.nextString()); }
            reader.endObject();
        }
        return result;
    }
    private static final Pattern FORMATS=Pattern.compile("%(?:(\\d+)\\$)?[+\\-0#,(]*\\d*(?:\\.\\d+)?([sdf])|%%");
    private static List<String> formats(String text) {
        var out=new ArrayList<String>(); var matcher=FORMATS.matcher(text); int next=1;
        while (matcher.find()) if (!matcher.group().equals("%%")) out.add((matcher.group(1)==null ? next++ : Integer.parseInt(matcher.group(1)))+":"+matcher.group(2));
        Collections.sort(out);return out;
    }
    private static final Set<String> OVERFLOWS=new TreeSet<>();
    private static void page(Page page,String label) {
        for (var line:page.texts()) {
            int width=Metrics.ESTIMATE.width(line.text()); int x=line.right() ? line.x()-width : line.x();
            if (x<0 || x+width>Page.WIDTH) OVERFLOWS.add("Page width: "+line.text()+" ("+x+".."+(x+width)+")");
            require(line.y()>=0 && line.y()+Page.LINE<=Page.HEIGHT,label+" vertical overflow: "+line.text());
        }
        var lines=page.texts();
        for (int i=0;i<lines.size();i++) for (int j=i+1;j<lines.size();j++) {
            var a=lines.get(i);var b=lines.get(j);if (a.y()!=b.y()) continue;
            int aw=Metrics.ESTIMATE.width(a.text()),bw=Metrics.ESTIMATE.width(b.text());
            int ax=a.right() ? a.x()-aw : a.x(),bx=b.right() ? b.x()-bw : b.x();
            if (aw>0 && bw>0 && ax<bx+bw && bx<ax+aw) OVERFLOWS.add("Overlapping labels: "+a.text()+" / "+b.text());
        }
        if (page.portrait()!=null) require(Gene.COAT.alleles().stream().anyMatch(a->a.name().equals(page.portrait().coat())),"Localized coat must never replace a portrait allele ID");
    }
    private static void spread(Page.Spread spread,String label) { page(spread.left(),label+" left");page(spread.right(),label+" right"); }
    private static Genome fixed(Genome genome,Gene gene,String name) { int id=gene.alleleNamed(name).id();return genome.with(gene,0,id).with(gene,1,id); }
    private static void dump(PrintWriter out,Page.Spread spread,String label) {
        out.println("SPREAD "+label);
        for (var side:List.of("L","R")) {
            out.println("PAGE "+side); Page page=side.equals("L") ? spread.left() : spread.right();
            for (var t:page.texts()) out.printf("T %d %d %s %s %s%n",t.x(),t.y(),t.tone(),t.right(),t.text());
            for (var h:page.hearts()) out.printf("H %d %d %d %s %d%n",h.x(),h.y(),h.full(),h.half(),h.times());
            var p=page.portrait();if (p!=null) out.printf("P %d %d %d %d %d%n",p.x(),p.y(),p.w(),p.h(),SheetLayout.coatColor(p.coat()));
        }
    }
    public static void main(String[] args) throws Exception {
        var en=language("en_us");var es=language("es_pe");
        require(en.keySet().equals(es.keySet()),"Spanish and English keys must match");
        for (var key:en.keySet()) require(formats(en.get(key)).equals(formats(es.get(key))),"Changed placeholders: "+key+" / "+en.get(key)+" -> "+es.get(key));
        for (var locale:List.of("ar","bo","cl","co","cr","cu","do","ec","es","gq","gt","hn","mx","ni","pa","pr","py","sv","us","uy","ve"))
            require(es.equals(language("es_"+locale)),"Spanish variants must share the canonical translation: "+locale);
        for (var key:es.keySet()) {
            if (key.matches("guide\\.bunnidogs\\.page\\.[0-9]+\\.preview"))
                require(Page.wrap(es.get(key),114,Metrics.ESTIMATE).size()<=6,"Guide preview too long: "+key);
            if (key.startsWith("guide.bunnidogs.index.")) require(Metrics.ESTIMATE.width(es.get(key))<=114,"Guide contents label too long: "+key+" / "+es.get(key));
        }
        var island=fixed(fixed(fixed(Genome.DEFAULT,Gene.COAT,"seal"),Gene.LEGS,"paddle"),Gene.DIET,"shore_forager");
        var other=fixed(fixed(fixed(Genome.DEFAULT,Gene.COAT,"pale"),Gene.EARS,"bat"),Gene.BUILD,"stocky");
        var stats=Stats.of(20,10,20,20,20); var seed=new Seed(island,stats,other,Stats.ZERO,4.25,true);
        var entry=new Pedigree.Entry(island,Optional.of(island),Optional.of(other),Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000123")),Optional.of("BD-123ABC"),Optional.of(stats),false);
        var book=new Pedigree(); book.add(entry);
        var names=new LineageNames();names.rename(island,"Luna %s");book.setLineageNames(names);
        var savedBook=book.toString(); var savedSeed=seed.toString();
        var table=new TableSheet(Optional.of(new Tissue(island,stats)),Optional.of(new Tissue(other,Stats.ZERO)),BreedingPlan.PLAIN,Optional.of(Commissions.Request.COASTAL_SCOUT));
        var savedTable=table.toString();
        var snapshots=new ArrayList<Page.Spread>();
        Texts.using((key,fallback)->{
            require(en.containsKey(key),"Uncataloged runtime message: "+fallback);
            // Mirror Minecraft's conversion of numeric patterns on language load.
            return es.get(key).replaceAll("%(\\d+\\$)?[\\d.]*[df]","%$1s");
        },()->{
            require(Plain.word(Gene.COAT.alleleNamed("seal")).equals("de foca"),"Spanish alleles must be used in readouts");
            require(Units.unitText(Stat.VITALITY,4.25).equals("4.3 corazones"),"Minecraft normalization must retain numeric precision");
            require(Texts.t("%s carrying %s","Luna %s","BD-123ABC").equals("Luna %s portador de BD-123ABC"),"User text must not be reinterpreted as a format or message key");
            for (var gene:Gene.values()) for (var allele:gene.alleles()) {
                Plain.effect(gene,allele);Plain.trend(gene,allele); Plain.hover(island.with(gene,0,allele.id()),gene);
            }
            Readout.animal(island,stats,false,Optional.of(other),Optional.of(island));
            for (var def:Juice.DEFS.values()) for (var value:def) Juice.spec(value,4,stats).describe();
            new Infusion(island,stats,4).summary();
            snapshots.add(BookContents.contents(book,Metrics.ESTIMATE));
            snapshots.add(SheetLayout.single(entry,Metrics.ESTIMATE));
            snapshots.add(RegistryContents.spread(book,0,Metrics.ESTIMATE));
            for (int i=0;i<SeedSheet.spreadCount();i++) snapshots.add(SeedSheet.spread(seed,i,Metrics.ESTIMATE));
            for (var goal:Commissions.Request.values()) {
                var view=new TableSheet(table.a(),table.b(),table.plan(),Optional.of(goal));
                for (int i=0;i<view.spreadCount();i++) snapshots.add(view.spread(i,Metrics.ESTIMATE));
            }
            for (int i=0;i<snapshots.size();i++) spread(snapshots.get(i),"Spanish spread "+i);
            require(BookContents.contents(book,Metrics.ESTIMATE).left().texts().stream().anyMatch(t->t.text().contains("Luna %s")),"Player tags must survive translation");
            Texts.using((key,fallback)->fallback,()->{ require(Plain.geneName(Gene.COAT).equals("Coat"),"Nested language scope");return null; });
            require(Plain.geneName(Gene.COAT).equals("Pelaje"),"Restore enclosing locale");
            return null;
        });
        require(OVERFLOWS.isEmpty(),String.join("\n",OVERFLOWS));
        require(Plain.geneName(Gene.COAT).equals("Coat"),"Translation must not leak to default/server threads");
        require(book.toString().equals(savedBook) && seed.toString().equals(savedSeed) && table.toString().equals(savedTable),"Locale must not change DNA, book or seed wire/save forms");
        try { Texts.using((key,fallback)->"test",()->{throw new IllegalStateException("expected");}); } catch (IllegalStateException expected) { }
        require(Plain.geneName(Gene.COAT).equals("Coat"),"Failed presentation must restore the locale");
        if (args.length>0) try (var out=new PrintWriter(Files.newBufferedWriter(Path.of(args[0]),StandardCharsets.UTF_8))) {
            for (int i=0;i<Math.min(9,snapshots.size());i++) dump(out,snapshots.get(i),"Spanish "+i);
        }
        System.out.println("Localization OK: "+es.size()+" Spanish entries, shared variants, formatted readouts, safe page bounds and unchanged saves.");
    }
}
