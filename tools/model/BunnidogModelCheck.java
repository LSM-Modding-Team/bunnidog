import com.bunnidogs.mod.client.model.BunnidogMesh;
import com.bunnidogs.mod.client.model.BunnidogModel;
import com.bunnidogs.mod.client.model.BunnidogModelLayer;
import com.bunnidogs.mod.client.renderer.BunnidogRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

public class BunnidogModelCheck {
    private static int faces;
    private static int vertices;
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static float ground(ModelPart root) {
        float[] maxY = {-Float.MAX_VALUE};
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            for (var poly : cube.polygons) for (var vertex : poly.vertices()) {
                Vector3f position = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
                maxY[0] = Math.max(maxY[0], position.y() * 16);
            }
        });
        return maxY[0];
    }
    public static void main(String[] args) {
        ModelPart root = BunnidogMesh.load(BunnidogModelLayer.createBodyLayer().bakeRoot());
        root.visit(new PoseStack(), (pose, path, index, cube) -> {
            for (var poly : cube.polygons) {
                faces++;
                require(Math.abs(poly.normal().length() - 1) < .001, "Non-unit normal: " + path);
                var vs = poly.vertices();
                Vector3f a = new Vector3f(vs[2].x() - vs[0].x(), vs[2].y() - vs[0].y(), vs[2].z() - vs[0].z());
                Vector3f b = new Vector3f(vs[3].x() - vs[1].x(), vs[3].y() - vs[1].y(), vs[3].z() - vs[1].z());
                require(a.cross(b).dot(poly.normal()) > 0, "Inverted face: " + path);
                for (var vertex : vs) {
                    vertices++;
                    require(Float.isFinite(vertex.x() + vertex.y() + vertex.z()), "Non-finite vertex");
                    require(vertex.u() > 0 && vertex.u() < 1 && vertex.v() > 0 && vertex.v() < 1, "Invalid UV");
                }
            }
        });
        require(faces == 2172, "Lightweight mesh budget changed: " + faces);
        for (var coat : com.bunnidogs.mod.genetics.Gene.COAT.alleles()) {
            try (var stream = BunnidogModelCheck.class.getResourceAsStream("/assets/bunnidogs/textures/entity/bunnidog_" + coat.name() + ".png")) {
                require(stream != null, "Missing coat texture: " + coat.name());
                var image = javax.imageio.ImageIO.read(stream);
                int expectedSize = 512;
                require(image != null && image.getWidth() == expectedSize && image.getHeight() == expectedSize, "Wrong atlas size");
            } catch (java.io.IOException e) { throw new AssertionError(e); }
        }
        BunnidogModel model = new BunnidogModel(root);
        BunnidogRenderState state = new BunnidogRenderState();
        state.earScale = state.legScale = state.buildScale = 1;
        model.setupAnim(state);
        int[] rendered = {0};
        float[] pending = new float[5];
        var sharedNormals = new java.util.HashMap<String, Vector3f>();
        var sharedUvs = new java.util.HashMap<String, float[]>();
        var frontRings = new java.util.TreeMap<Float, Vector3f>();
        VertexConsumer consumer = (VertexConsumer) Proxy.newProxyInstance(
                VertexConsumer.class.getClassLoader(), new Class<?>[]{VertexConsumer.class}, (proxy, method, values) -> {
                    if (method.isDefault()) return InvocationHandler.invokeDefault(proxy, method, values);
                    if (method.getName().equals("addVertex") && values.length == 3) {
                        for (int i = 0; i < 3; i++) pending[i] = (Float) values[i];
                    }
                    if (method.getName().equals("setUv")) {
                        pending[3] = (Float) values[0];
                        pending[4] = (Float) values[1];
                    }
                    if (method.getName().equals("setNormal")) {
                        float nx = (Float) values[0], ny = (Float) values[1], nz = (Float) values[2];
                        require(Float.isFinite(nx + ny + nz), "Non-finite rendered normal");
                        require(Math.abs(nx * nx + ny * ny + nz * nz - 1) < .01, "Non-unit rendered normal");
                        rendered[0]++;
                        // The continuous head chart occupies the top 224 texel rows.
                        if (pending[4] <= 225.5F / 512 + .00001F) {
                            String key = Math.round(pending[0] * 100000) + ":" + Math.round(pending[1] * 100000) + ":" + Math.round(pending[2] * 100000);
                            Vector3f normal = new Vector3f(nx, ny, nz);
                            Vector3f previous = sharedNormals.putIfAbsent(key, normal);
                            require(previous == null || previous.distance(normal) < .001, "Head lighting splits at shared vertex");
                            float[] uv = sharedUvs.putIfAbsent(key, new float[]{pending[3], pending[4]});
                            // The sole allowed UV wrap is at the rear/side chart edge.
                            if (uv != null && Math.abs(uv[0] - pending[3]) < .9) {
                                require(Math.abs(uv[0] - pending[3]) < .00001 && Math.abs(uv[1] - pending[4]) < .00001,
                                        "Head texture splits at shared vertex");
                            }
                            if (Math.abs(pending[3] - 129.25F / 512) < .00001) {
                                frontRings.put(pending[1], normal);
                            }
                        }
                    }
                    return proxy;
                });
        root.render(new PoseStack(), consumer, 0xF000F0, 0);
        require(rendered[0] == vertices, "Custom smooth renderer dropped vertices");
        java.util.Map.Entry<Float, Vector3f> previousRing = null;
        int closeRings = 0;
        for (var ring : frontRings.entrySet()) {
            if (previousRing != null && (ring.getKey() - previousRing.getKey()) * 16 < .045F) {
                require(ring.getValue().distance(previousRing.getValue()) < .12F,
                        "Abrupt lighting across nearby head rings");
                closeRings++;
            }
            previousRing = ring;
        }
        require(closeRings > 0, "Head ring continuity was not exercised");
        float adultGround = ground(root);
        require(Math.abs(adultGround - 24) < .1, "Adult paws not on ground: " + adultGround);
        for (float leg : new float[]{.75F, 1.35F}) {
            state.legScale = leg;
            model.setupAnim(state);
            require(Math.abs(ground(root) - adultGround) < .001, "Leg gene moves feet off ground");
        }
        state.legScale = 1;
        state.isBaby = true;
        model.setupAnim(state);
        require(Math.abs(ground(root) - 24) < .1, "Baby paws not on ground");
        state.isBaby = false;
        model.setupAnim(state);
        require(root.xScale == 1 && root.getChild("head").xScale == 1, "Baby scaling leaks into adult");
        System.out.println("Model OK: " + faces + " faces, " + vertices + " vertices; winding, continuous head UVs/normals, smooth render path, gene ground contact and baby reset passed.");
    }
}
