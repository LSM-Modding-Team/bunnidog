package com.bunnidogs.mod.worldgen;

import java.util.Optional;
import java.util.Random;

import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.layout.TunnelShape;
import com.bunnidogs.mod.registry.ModStructures;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * The bunnidog settlement as a real {@link Structure} (Phase 6d). It replaces the surface and End placed features of the warren: where it
 * appears is data (data/bunnidogs/worldgen/structure and structure_set: biomes come from the lineage tags), what it looks like is
 * {@link Settlement} (1 to 5 guava trees, houses from 2 trees on, footpaths from 3) turned into {@link WarrenPiece}s. The layout is
 * rolled from a seed taken from the world seed + chunk, so every settlement is different but the same world always gives the same ones.
 * The cave and nether warrens stay worldgen Features (they search for a floor underground, which a structure start cannot do).
 */
public class WarrenStructure extends Structure {
    public static final MapCodec<WarrenStructure> CODEC = simpleCodec(WarrenStructure::new);

    public WarrenStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        long layoutSeed = context.random().nextLong();
        int level = Settlement.rollLevel(new Random(layoutSeed ^ 0x5DEECE66DL));
        int cx = chunk.getMinBlockX() + 4 + context.random().nextInt(8);
        int cz = chunk.getMinBlockZ() + 4 + context.random().nextInt(8);
        int cy = height(context, cx, cz);
        Settlement layout = Settlement.generate(new Random(layoutSeed), level);
        BlockPos origin = new BlockPos(cx, cy, cz);
        TunnelShape.Network underground = WarrenTunnels.plan(context, origin, layout, layoutSeed);
        var signs = com.bunnidogs.mod.layout.Wayfinding.plan(layout,underground,cx,cz);
        var fixtures = com.bunnidogs.mod.layout.SettlementFixtures.plan(layout,underground,signs);
        return Optional.of(new GenerationStub(new BlockPos(cx, cy, cz), builder -> {
            if (!underground.cells().isEmpty()) {
                builder.addPiece(new WarrenPiece(WarrenPiece.TUNNEL, 0, layoutSeed, level, cx, cz, cy, false, WarrenTunnels.encode(underground),new int[0],
                        com.bunnidogs.mod.layout.Wayfinding.encode(signs),
                        com.bunnidogs.mod.layout.SettlementFixtures.encode(fixtures)));
            }
            for (int i = 0; i < layout.clusters().size(); i++) {
                Settlement.Cluster c = layout.clusters().get(i);
                builder.addPiece(new WarrenPiece(WarrenPiece.CLUSTER, i, layoutSeed, level, cx, cz,
                        height(context, cx + c.dx(), cz + c.dz()), underground.dens().contains(i), new int[0]));
            }
            for (int i = 0; i < WarrenPiece.houseCount(layout, level); i++) {
                Settlement.House h = layout.houses().get(i);
                builder.addPiece(new WarrenPiece(WarrenPiece.HOUSE, i, layoutSeed, level, cx, cz, height(context, cx + h.dx(), cz + h.dz())));
            }
            for (int i = 0; i < layout.props().size(); i++) {
                Settlement.Prop p = layout.props().get(i);
                builder.addPiece(new WarrenPiece(WarrenPiece.PROP, i, layoutSeed, level, cx, cz, height(context, cx + p.dx(), cz + p.dz())));
            }
            builder.addPiece(new WarrenPiece(WarrenPiece.PATH, 0, layoutSeed, level, cx, cz, cy,
                    false, new int[0], WarrenTunnels.pathExclusions(underground)));
        }));
    }

    /** The first free block above the terrain at (x, z), from the noise (so it does not change when pieces are built). */
    private static int height(GenerationContext context, int x, int z) {
        return context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.WARREN_VILLAGE.get();
    }
}
