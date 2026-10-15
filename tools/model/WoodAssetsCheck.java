import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
        System.out.println("Guava mechanisms OK: complete state coverage, model/texture links, recipes, wood tags and lower-half-only door loot.");
    }
}
