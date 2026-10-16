import com.bunnidogs.mod.client.BunnidogsClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;

/** Native boat layers and the actual shipped guava atlases/crafting data, without a client window. */
public final class BoatAssetsCheck {
    private static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static JsonObject json(String path) {
        try (var stream = BoatAssetsCheck.class.getResourceAsStream("/" + path)) {
            require(stream != null, "Missing resource: " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) { throw new AssertionError(e); }
    }
    private static void tagged(String domain, String tag, String id) {
        require(json("data/minecraft/tags/" + domain + "/" + tag + ".json").getAsJsonArray("values").asList()
                .stream().anyMatch(e -> e.getAsString().equals(id)), "Missing vehicle tag: " + id);
    }
    public static void main(String[] args) throws Exception {
        for (boolean chest : new boolean[]{false, true}) {
            String name = chest ? "guava_chest_boat" : "guava_boat", id = "bunnidogs:" + name;
            var layer = chest ? BunnidogsClient.GUAVA_CHEST_BOAT_LAYER : BunnidogsClient.GUAVA_BOAT_LAYER;
            // BoatRenderer resolves its texture directly from the layer's model Identifier.
            Identifier texture = layer.model().withPath(p -> "textures/entity/" + p + ".png");
            require(texture.getNamespace().equals("bunnidogs"), "Boat renderer must use our namespace");
            try (var stream = BoatAssetsCheck.class.getResourceAsStream("/assets/bunnidogs/" + texture.getPath())) {
                require(stream != null, "Missing renderer atlas: " + texture);
                var atlas = ImageIO.read(stream);
                require(atlas.getWidth() == 128 && atlas.getHeight() == (chest ? 128 : 64), "Native boat UV dimensions");
            }
            var root = (chest ? BoatModel.createChestBoatModel() : BoatModel.createBoatModel()).bakeRoot();
            for (String part : new String[]{"bottom", "front", "back", "left", "right", "left_paddle", "right_paddle"})
                require(root.hasChild(part), "Missing native boat part: " + part);
            if (chest) for (String part : new String[]{"chest_bottom", "chest_lid", "chest_lock"})
                require(root.hasChild(part), "Missing native chest part: " + part);
            var model = new BoatModel(root);
            var state = new BoatRenderState();
            state.rowingTimeLeft = 1.2F; state.rowingTimeRight = 2.4F;
            model.setupAnim(state);
            require(Float.isFinite(root.getChild("left_paddle").yRot + root.getChild("right_paddle").zRot), "Native paddle animation");
            var item = json("assets/bunnidogs/items/" + name + ".json");
            require(item.getAsJsonObject("model").get("model").getAsString().equals("bunnidogs:item/" + name), "Wrong boat item model");
            var icon = json("assets/bunnidogs/models/item/" + name + ".json");
            require(icon.getAsJsonObject("textures").get("layer0").getAsString().equals("bunnidogs:item/" + name), "Wrong boat icon");
            try (var stream = BoatAssetsCheck.class.getResourceAsStream("/assets/bunnidogs/textures/item/" + name + ".png")) {
                require(stream != null, "Missing boat icon");
                var image = ImageIO.read(stream);
                require(image.getWidth() == 16 && image.getHeight() == 16, "Boat icon dimensions");
            }
            var recipe = json("data/bunnidogs/recipe/" + name + ".json");
            require(recipe.getAsJsonObject("result").get("id").getAsString().equals(id), "Wrong boat recipe result");
            if (chest) {
                var ingredients = recipe.getAsJsonArray("ingredients").asList().stream().map(e -> e.getAsString()).toList();
                require(ingredients.size() == 2 && ingredients.contains("minecraft:chest") && ingredients.contains("bunnidogs:guava_boat"), "Chest boat ingredients");
            } else {
                require(recipe.getAsJsonObject("key").get("#").getAsString().equals("bunnidogs:guava_planks"), "Boat wood ingredient");
                require(recipe.getAsJsonArray("pattern").toString().equals("[\"# #\",\"###\"]"), "Five-plank boat recipe");
            }
            var unlock = json("data/bunnidogs/advancement/recipes/transportation/" + name + ".json");
            require(unlock.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString().equals(id), "Wrong boat unlock");
            tagged("item", chest ? "chest_boats" : "boats", id);
            tagged("entity_type", "boat", id);
        }
        System.out.println("Guava boats OK: native layers, atlas/renderer paths, paddles, chest parts, icons, crafting/unlocks and tags.");
    }
}
