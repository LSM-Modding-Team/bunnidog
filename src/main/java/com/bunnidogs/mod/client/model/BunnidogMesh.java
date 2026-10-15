package com.bunnidogs.mod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Reference-sculpted surfaces on the normal animated ModelPart skeleton. */
public final class BunnidogMesh {
    private BunnidogMesh() {}

    public static ModelPart load(ModelPart skeleton) {
        InputStream stream = BunnidogMesh.class.getResourceAsStream(
                "/assets/bunnidogs/models/entity/bunnidog.mesh");
        if (stream == null) throw new IllegalStateException("Missing bunnidog mesh");
        try (DataInputStream in = new DataInputStream(stream)) {
            if (in.readInt() != 0x42444D32) throw new IOException("Invalid bunnidog mesh version");
            Map<String, List<ModelPart.Cube>> geometry = new HashMap<>();
            Map<String, String> parents = new HashMap<>();
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                String name = in.readUTF();
                parents.put(name, in.readUTF());
                List<ModelPart.Cube> cubes = new ArrayList<>();
                int faces = in.readInt();
                for (int f = 0; f < faces; f += 6) {
                    int batch = Math.min(6, faces - f);
                    SmoothCube cube = new SmoothCube(batch);
                    for (int j = 0; j < batch; j++) {
                        ModelPart.Vertex[] vertices = new ModelPart.Vertex[4];
                        for (int v = 3; v >= 0; v--) {
                            // Reverse winding after converting Y-up to Y-down.
                            vertices[v] = new ModelPart.Vertex(in.readFloat(), in.readFloat(),
                                    in.readFloat(), in.readFloat(), in.readFloat());
                            cube.smoothNormals[j][v] = new Vector3f(
                                    in.readFloat(), in.readFloat(), in.readFloat());
                        }
                        Vector3f faceNormal = new Vector3f(in.readFloat(), in.readFloat(), in.readFloat());
                        cube.polygons[j] = new ModelPart.Polygon(vertices, faceNormal);
                    }
                    cubes.add(cube);
                }
                geometry.put(name, cubes);
            }
            if (in.read() != -1) throw new IOException("Trailing bunnidog mesh data");
            return attach(skeleton, "root", geometry, parents);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load bunnidog mesh", e);
        }
    }

    private static ModelPart attach(ModelPart skeleton, String name,
            Map<String, List<ModelPart.Cube>> geometry, Map<String, String> parents) {
        Map<String, ModelPart> children = new HashMap<>();
        for (var entry : parents.entrySet()) {
            if (entry.getValue().equals(name)) {
                String child = entry.getKey();
                children.put(child, attach(skeleton.getChild(child), child, geometry, parents));
            }
        }
        ModelPart result = new ModelPart(geometry.getOrDefault(name, List.of()), children);
        result.setInitialPose(skeleton.getInitialPose());
        result.loadPose(skeleton.getInitialPose());
        return result;
    }

    private static EnumSet<Direction> slots(int count) {
        EnumSet<Direction> directions = EnumSet.noneOf(Direction.class);
        for (int i = 0; i < count; i++) directions.add(Direction.values()[i]);
        return directions;
    }

    /** Per-vertex normals remove the faceted lighting of ordinary model cubes. */
    private static final class SmoothCube extends ModelPart.Cube {
        private final Vector3f[][] smoothNormals;
        // Models render on the client render thread. Reuse scratch storage to
        // avoid allocating temporary vectors for every patch on every entity.
        private final Vector3f scratch = new Vector3f();

        private SmoothCube(int faces) {
            super(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, 512, 512, slots(faces));
            smoothNormals = new Vector3f[faces][4];
        }

        @Override
        public void compile(PoseStack.Pose pose, VertexConsumer builder,
                int lightCoords, int overlayCoords, int color) {
            Matrix4f matrix = pose.pose();
            for (int f = 0; f < polygons.length; f++) {
                ModelPart.Vertex[] vertices = polygons[f].vertices();
                for (int v = 0; v < vertices.length; v++) {
                    Vector3f n = pose.transformNormal(smoothNormals[f][v], scratch);
                    float nx = n.x(), ny = n.y(), nz = n.z();
                    ModelPart.Vertex vertex = vertices[v];
                    Vector3f pos = matrix.transformPosition(
                            vertex.worldX(), vertex.worldY(), vertex.worldZ(), scratch);
                    builder.addVertex(pos.x(), pos.y(), pos.z(), color, vertex.u(), vertex.v(),
                            overlayCoords, lightCoords, nx, ny, nz);
                }
            }
        }
    }
}
