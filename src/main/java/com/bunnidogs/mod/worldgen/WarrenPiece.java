package com.bunnidogs.mod.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.PathShape;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.registry.ModBiomeTags;
import com.bunnidogs.mod.registry.ModStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * One piece of a bunnidog settlement structure (Phase 6d, the port of the warren Feature to a real Structure): a tree cluster (guava
 * tree, coarse-dirt patch, burrows, wild bunnidogs), a burrow house, or the footpath network. A piece stores only
 * {@code (kind, index, layoutSeed, level, centre x/z, y)}; the layout ({@link Settlement}, pure Java) is rebuilt from the seed every time,
 * so a settlement differs between instances and nothing but a few ints is saved.
 *
 * <p>ANCHOR-CHUNK RULE: Minecraft calls {@link #postProcess} once for every chunk the bounding box touches, but this piece builds
 * everything in ONE call, the one for the chunk that holds its anchor column (cluster/house centre, path: the first column of the
 * network). A piece reaches at most ~6 blocks (path: 12) from its anchor, and a chunk is generated with its 3x3 neighbourhood, so
 * the writes and the bunnidog spawns are legal, and nothing is built or spawned twice. The path is laid column by column, each in
 * the chunk that owns the column. Each piece gives up quietly (like the old Feature) where its ground is water, too steep or taken.
 */
public class WarrenPiece extends StructurePiece {
    public static final int CLUSTER = 0;
    public static final int HOUSE = 1;
    public static final int PATH = 2;
    public static final int PROP = 3;
    /** Bounding boxes reach this far around the anchor (tree + patch + the mouths of a level 1 den, U1); the path box is {@link Settlement#MAX_EXTENT}. */
    private static final int REACH = 9;
    private static final int BELOW = 10;
    private static final int ABOVE = 24;

    private final int kind;
    private final int index;
    private final long seed;
    private final int level;
    /** Centre of the settlement (x, z) and the floor height y of THIS piece's anchor (the centre's for the path). */
    private final int centreX;
    private final int centreZ;
    private final int y;

    public WarrenPiece(int kind, int index, long seed, int level, int centreX, int centreZ, int y) {
        super(ModStructures.WARREN_PIECE.get(), 0, boxFor(kind, index, seed, level, centreX, centreZ, y));
        this.kind = kind;
        this.index = index;
        this.seed = seed;
        this.level = level;
        this.centreX = centreX;
        this.centreZ = centreZ;
        this.y = y;
    }

    public WarrenPiece(CompoundTag tag) {
        super(ModStructures.WARREN_PIECE.get(), tag);
        this.kind = tag.getIntOr("Kind", CLUSTER);
        this.index = tag.getIntOr("Index", 0);
        this.seed = tag.getLongOr("Seed", 0L);
        this.level = tag.getIntOr("Level", Settlement.MIN_LEVEL);
        this.centreX = tag.getIntOr("CX", 0);
        this.centreZ = tag.getIntOr("CZ", 0);
        this.y = tag.getIntOr("Y", 64);
    }

    /** The layout of the settlement this piece belongs to: the same for every piece and every call (same seed, same level). */
    private static Settlement layout(long seed, int level) {
        return Settlement.generate(new Random(seed), level);
    }

    /** Where the anchor column of a piece is, as {dx, dz} from the centre of the settlement. */
    private static int[] anchor(int kind, int index, Settlement s) {
        if (kind == CLUSTER) {
            Settlement.Cluster c = s.clusters().get(index);
            return new int[] {c.dx(), c.dz()};
        }
        if (kind == HOUSE) {
            Settlement.House h = s.houses().get(index);
            return new int[] {h.dx(), h.dz()};
        }
        if (kind == PROP) {
            Settlement.Prop p = s.props().get(index);
            return new int[] {p.dx(), p.dz()};
        }
        return new int[] {0, 0};
    }

    private static BoundingBox boxFor(int kind, int index, long seed, int level, int cx, int cz, int y) {
        int[] a = anchor(kind, index, layout(seed, level));
        int reach = kind == PATH ? Settlement.MAX_EXTENT : kind == HOUSE || kind == PROP ? 3 : REACH;
        int x = cx + a[0];
        int z = cz + a[1];
        return new BoundingBox(x - reach, y - BELOW, z - reach, x + reach, y + ABOVE, z + reach);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("Kind", kind);
        tag.putInt("Index", index);
        tag.putLong("Seed", seed);
        tag.putInt("Level", level);
        tag.putInt("CX", centreX);
        tag.putInt("CZ", centreZ);
        tag.putInt("Y", y);
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager structures, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        Settlement s = layout(seed, level);
        // every piece of a settlement takes the style of the biome at ITS CENTRE (Phase 6f), so a village never mixes styles
        Lineage style = ModBiomeTags.lineageAt(world, new BlockPos(centreX, y, centreZ));
        if (kind == PATH) {
            layPaths(world, s, chunkBox, style);
            return;
        }
        int[] a = anchor(kind, index, s);
        int x = centreX + a[0];
        int z = centreZ + a[1];
        if (!owns(chunkBox, x, z)) {
            return;
        }
        if (kind == CLUSTER) {
            BlockPos ground = WarrenFeature.groundAt(world, x, y, z, true);
            if (ground != null) {
                WarrenFeature.buildCluster(world, ground, s.clusters().get(index).shape(), new Random(seed + 31L * index), true, style, level == 1);
            }
        } else if (kind == PROP) {
            WarrenFeature.buildProp(world, x, y, z, s.props().get(index), true, style);
        } else {
            WarrenFeature.buildHouse(world, x, y, z, s.houses().get(index), true, style, level);
        }
    }

    /** Footpaths (Phase 6c) of the planned trees and houses, each column laid by the chunk that owns it. */
    private void layPaths(WorldGenLevel world, Settlement s, BoundingBox chunkBox, Lineage style) {
        List<Settlement.House> houses = new ArrayList<>(s.houses().subList(0, houseCount(s, level)));
        for (PathShape.Cell cell : PathShape.network(s.clusters(), houses, s.props())) {
            int x = centreX + cell.dx();
            int z = centreZ + cell.dz();
            if (owns(chunkBox, x, z)) {
                WarrenFeature.layPath(world, x, y, z, true, style);
            }
        }
    }

    /** How many houses a settlement of this level plans (the old Feature tried spare spots when one did not fit; pieces cannot). */
    public static int houseCount(Settlement s, int level) {
        return Math.min(s.houses().size(), Settlement.housesFor(level));
    }

    private static boolean owns(BoundingBox chunkBox, int x, int z) {
        return x >= chunkBox.minX() && x <= chunkBox.maxX() && z >= chunkBox.minZ() && z <= chunkBox.maxZ();
    }
}
