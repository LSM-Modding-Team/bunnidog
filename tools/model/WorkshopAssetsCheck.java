import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.genetics.SeedQuality;
import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

/** Shipped workshop model/texture references, complete visual variants and saved coat-id mapping. */
public final class WorkshopAssetsCheck {
    private static final Set<String> visited = new HashSet<>();
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static JsonObject json(String path) {
        var stream = WorkshopAssetsCheck.class.getResourceAsStream("/"+path);
        require(stream != null,"Missing resource: "+path);
        try (var reader = new InputStreamReader(stream,StandardCharsets.UTF_8)) { return JsonParser.parseReader(reader).getAsJsonObject(); }
        catch (java.io.IOException e) { throw new AssertionError(e); }
    }
    private static void refs(JsonElement element, String key) {
        if (element.isJsonObject()) element.getAsJsonObject().entrySet().forEach(e -> refs(e.getValue(),e.getKey()));
        else if (element.isJsonArray()) element.getAsJsonArray().forEach(e -> refs(e,key));
        else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (!value.startsWith("bunnidogs:")) return;
            String path = value.substring("bunnidogs:".length());
            if (key.equals("model") || key.equals("parent")) {
                if (visited.add(value)) refs(json("assets/bunnidogs/models/"+path+".json"),"");
            } else if (path.startsWith("block/") || path.startsWith("item/")) {
                var stream = WorkshopAssetsCheck.class.getResourceAsStream("/assets/bunnidogs/textures/"+path+".png");
                require(stream != null,"Missing texture: "+value);
                try (stream) { require(ImageIO.read(stream) != null,"Unreadable texture: "+value); }
                catch (java.io.IOException e) { throw new AssertionError(e); }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        for (String block : new String[]{"juicer","specimen_jar","breeding_pen"}) {
            refs(json("assets/bunnidogs/blockstates/"+block+".json"),"");
            refs(json("assets/bunnidogs/items/"+block+".json"),"");
        }
        var pen = json("assets/bunnidogs/blockstates/breeding_pen.json").getAsJsonObject("variants");
        require(pen.size() == 16,"All four independently occupied mangers need all four pen orientations");
        for (String facing : new String[]{"north","east","south","west"}) for (int fill = 0; fill < 4; fill++)
            require(pen.has("facing="+facing+",fill="+fill),"Missing pen state");
        var press = json("assets/bunnidogs/blockstates/juicer.json").getAsJsonObject("variants");
        require(press.size() == 8,"Both working states need all four press orientations");
        for (String facing : new String[]{"north","east","south","west"}) for (boolean working : new boolean[]{false,true})
            require(press.has("facing="+facing+",working="+working),"Missing press state");
        var jars = json("assets/bunnidogs/blockstates/specimen_jar.json").getAsJsonObject("variants");
        require(jars.size() == Gene.COAT.alleles().size()+1 && jars.has("specimen=0"),"Jars need one empty visual and one per saved coat");
        for (var coat : Gene.COAT.alleles()) {
            String model = jars.getAsJsonObject("specimen="+(coat.id()+1)).get("model").getAsString().substring("bunnidogs:".length());
            var textures = json("assets/bunnidogs/models/"+model+".json").getAsJsonObject("textures");
            require(textures.get("sample").getAsString().equals("bunnidogs:block/specimen_"+coat.name()),"Jar color must follow saved allele id: "+coat.name());
            String rug = coat.name()+"_pelt_rug";
            var states = json("assets/bunnidogs/blockstates/"+rug+".json"); refs(states,"");
            require(states.getAsJsonObject("variants").size() == 4,"Every coat rug needs all orientations");
            refs(json("assets/bunnidogs/items/"+rug+".json"),"");
            try (var stream = WorkshopAssetsCheck.class.getResourceAsStream("/assets/bunnidogs/textures/block/"+rug+".png")) {
                var image = ImageIO.read(stream); int clear = 0, solid = 0;
                for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
                    if ((image.getRGB(x,y) >>> 24) == 0) clear++; else solid++;
                require(clear > 0 && solid > 0,"A hide rug needs both fur and a transparent outline");
            }
        }
        for (var tier : GuavaTier.values()) refs(json("assets/bunnidogs/items/"+tier.id()+"_juice.json"),"");
        var seed = json("assets/bunnidogs/items/seed.json").getAsJsonObject("model"); refs(seed,"");
        require(seed.get("type").getAsString().equals("minecraft:range_dispatch") && seed.get("property").getAsString().equals("bunnidogs:seed_quality"),"Seeds must dispatch from their actual stored quality property");
        var bands = seed.getAsJsonArray("entries"); require(bands.size() == GuavaTier.values().length,"Every quality band needs a seed look");
        for (var tier : GuavaTier.values()) {
            require(bands.get(tier.level()).getAsJsonObject().get("threshold").getAsDouble() == tier.quality(),"Seed artwork thresholds follow the actual quality ladder");
            require(SeedQuality.band(tier.quality()) == tier,"An exact quality threshold selects its own band");
            if (tier.previous() != null) require(SeedQuality.band(Math.nextDown(tier.quality())) == tier.previous(),"Fractions below a threshold must not round up");
        }
        require(SeedQuality.band(1.5) == GuavaTier.GOLDEN && SeedQuality.band(32.99) == GuavaTier.DRAGON_EGG,"Intermediate seed qualities retain their lower look");
        System.out.println("Workshop assets OK: press variants, saved coat-id jars, all rug/carrier models and texture references.");
    }
}
