import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;

/** Checks the shipped interactive-wood data, including state properties intentionally ignored by visual variants. */
public final class WoodAssetsCheck {
    private static final String[] PARTS = {"door","trapdoor","button","pressure_plate","fence_gate"};
    private static JsonObject json(String path) {
        var stream = WoodAssetsCheck.class.getResourceAsStream("/"+path);
        require(stream != null,"Missing resource: "+path);
        try (var reader = new InputStreamReader(stream,StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException e) { throw new AssertionError(e); }
    }
    private static void require(boolean valid,String message) { if (!valid) throw new AssertionError(message); }
    private static Map<String,List<String>> properties(String part) {
        Map<String,List<String>> out = new LinkedHashMap<>();
        out.put("powered",List.of("false","true"));
        if (!part.equals("pressure_plate")) out.put("facing",List.of("north","east","south","west"));
        switch (part) {
            case "door" -> { out.put("half",List.of("lower","upper")); out.put("hinge",List.of("left","right")); out.put("open",List.of("false","true")); }
            case "trapdoor" -> { out.put("half",List.of("bottom","top")); out.put("open",List.of("false","true")); out.put("waterlogged",List.of("false","true")); }
            case "button" -> out.put("face",List.of("floor","wall","ceiling"));
            case "fence_gate" -> { out.put("open",List.of("false","true")); out.put("in_wall",List.of("false","true")); }
        }
        return out;
    }
    private static void states(List<Map.Entry<String,List<String>>> fields,int at,Map<String,String> state,JsonObject variants) {
        if (at == fields.size()) {
            int matches = 0;
            for (String key : variants.keySet()) {
                boolean match = true;
                for (String property : key.split(",")) {
                    String[] pair = property.split("=",2);
                    match &= pair.length == 2 && Objects.equals(state.get(pair[0]),pair[1]);
                }
                if (match) matches++;
            }
            require(matches == 1,"Missing or ambiguous visual state: "+state);
            return;
        }
        var field = fields.get(at);
        for (String value : field.getValue()) { state.put(field.getKey(),value); states(fields,at+1,state,variants); }
    }
    private static final Set<String> visited = new HashSet<>();
    private static void refs(JsonElement element,String key) {
        if (element.isJsonObject()) element.getAsJsonObject().entrySet().forEach(e -> refs(e.getValue(),e.getKey()));
        else if (element.isJsonArray()) element.getAsJsonArray().forEach(e -> refs(e,key));
        else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            if (!value.startsWith("bunnidogs:")) return;
            String local = value.substring("bunnidogs:".length());
            if (key.equals("model") || key.equals("parent")) {
                if (visited.add(value)) refs(json("assets/bunnidogs/models/"+local+".json"),"");
            } else if (local.startsWith("block/") || local.startsWith("item/")) {
                var stream = WoodAssetsCheck.class.getResourceAsStream("/assets/bunnidogs/textures/"+local+".png");
                require(stream != null,"Missing texture: "+value);
                try { stream.close(); } catch (java.io.IOException e) { throw new AssertionError(e); }
            }
        }
    }
    private static void tag(String domain,String name,String id) {
        require(json("data/minecraft/tags/"+domain+"/"+name+".json").getAsJsonArray("values")
                .asList().stream().anyMatch(e -> e.getAsString().equals(id)),"Missing "+name+": "+id);
    }
    private static void texture(String name,int width,int height) {
        try (var stream = WoodAssetsCheck.class.getResourceAsStream("/assets/bunnidogs/textures/"+name+".png")) {
            require(stream != null,"Missing sign texture: "+name);
            var image = ImageIO.read(stream);
            require(image != null && image.getWidth() == width && image.getHeight() == height,"Wrong sign UV dimensions: "+name);
            require(image.getColorModel().hasAlpha(),"Sign chains/posts require transparency: "+name);
        } catch (java.io.IOException e) { throw new AssertionError(e); }
    }
    private static void signs() {
        String[] parts = {"sign","wall_sign","hanging_sign","wall_hanging_sign"};
        String[] families = {"standing_signs","wall_signs","ceiling_hanging_signs","wall_hanging_signs"};
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i], name = "guava_"+part, id = "bunnidogs:"+name;
            Map<String,List<String>> fields = new LinkedHashMap<>();
            fields.put("waterlogged",List.of("false","true"));
            if (part.startsWith("wall")) fields.put("facing",List.of("north","east","south","west"));
            else fields.put("rotation",java.util.stream.IntStream.range(0,16).mapToObj(Integer::toString).toList());
            if (part.equals("hanging_sign")) fields.put("attached",List.of("false","true"));
            var blockstate = json("assets/bunnidogs/blockstates/"+name+".json");
            states(new ArrayList<>(fields.entrySet()),0,new HashMap<>(),blockstate.getAsJsonObject("variants"));
            refs(blockstate,"");
            for (var variant : blockstate.getAsJsonObject("variants").asMap().values()) {
                String model = variant.getAsJsonObject().get("model").getAsString().replace("bunnidogs:","");
                var geometry = json("assets/bunnidogs/models/"+model+".json");
                require(geometry.get("render_type").getAsString().equals("minecraft:cutout"),
                        "Sign chain/post transparency must use cutout: "+model);
                require(geometry.getAsJsonObject("textures").get("particle").getAsString()
                        .equals(part.contains("hanging") ? "bunnidogs:block/stripped_guava_log" : "bunnidogs:block/guava_planks"),"Wrong sign break particles: "+model);
            }
            tag("block",families[i],id); tag("block","mineable/axe",id);
        }
        for (String part : List.of("sign","hanging_sign")) {
            String name = "guava_"+part, id = "bunnidogs:"+name;
            refs(json("assets/bunnidogs/items/"+name+".json"),"");
            var recipe = json("data/bunnidogs/recipe/"+name+".json");
            boolean hanging = part.equals("hanging_sign");
            require(recipe.getAsJsonObject("result").get("id").getAsString().equals(id),"Wrong sign recipe result");
            require(recipe.getAsJsonObject("result").get("count").getAsInt() == (hanging ? 6 : 3),"Wrong sign recipe yield");
            var key = recipe.getAsJsonObject("key");
            require(key.get("#").getAsString().equals(hanging ? "bunnidogs:stripped_guava_log" : "bunnidogs:guava_planks"),"Wrong sign wood");
            require(key.get("X").getAsString().equals(hanging ? "minecraft:iron_chain" : "minecraft:stick"),"Wrong sign hardware");
            var loot = json("data/bunnidogs/loot_table/blocks/"+name+".json");
            require(loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0)
                    .getAsJsonObject().get("name").getAsString().equals(id),"Wrong sign drop");
            var unlock = json("data/bunnidogs/advancement/recipes/decorations/"+name+".json");
            require(unlock.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString().equals(id),"Wrong sign recipe unlock");
            tag("item",hanging ? "hanging_signs" : "signs",id);
            texture("block/"+name,32,32); texture("item/"+name,16,16);
        }
        // Namespaced WoodType names resolve directly to these native editor backgrounds.
        texture("gui/signs/guava",24,26); texture("gui/hanging_signs/guava",16,16);
    }
    public static void main(String[] args) {
        String[] tags = {"wooden_doors","wooden_trapdoors","wooden_buttons","wooden_pressure_plates","fence_gates"};
        for (int i = 0; i < PARTS.length; i++) {
            String name = "guava_"+PARTS[i], id = "bunnidogs:"+name;
            JsonObject blockstate = json("assets/bunnidogs/blockstates/"+name+".json");
            states(new ArrayList<>(properties(PARTS[i]).entrySet()),0,new HashMap<>(),blockstate.getAsJsonObject("variants"));
            refs(blockstate,""); refs(json("assets/bunnidogs/items/"+name+".json"),"");
            JsonObject recipe = json("data/bunnidogs/recipe/"+name+".json");
            require(recipe.getAsJsonObject("result").get("id").getAsString().equals(id),"Wrong recipe result");
            require(recipe.getAsJsonObject("key").get("P").getAsString().equals("bunnidogs:guava_planks"),"Recipe must use guava planks");
            json("data/bunnidogs/loot_table/blocks/"+name+".json");
            for (String domain : List.of("block","item")) require(json("data/minecraft/tags/"+domain+"/"+tags[i]+".json")
                    .getAsJsonArray("values").asList().stream().anyMatch(e -> e.getAsString().equals(id)),"Missing vanilla wood tag: "+id);
        }
        JsonObject door = json("data/bunnidogs/loot_table/blocks/guava_door.json");
        var conditions = door.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0)
                .getAsJsonObject().getAsJsonArray("conditions");
        require(conditions.asList().stream().anyMatch(e -> e.getAsJsonObject().has("properties")
                && e.getAsJsonObject().getAsJsonObject("properties").get("half").getAsString().equals("lower")),"A door must drop only once, from its lower half");
        signs();
        System.out.println("Guava mechanisms and signs OK: complete states/attachments, model/texture/editor links, recipes/unlocks, wood tags and drops.");
    }
}
