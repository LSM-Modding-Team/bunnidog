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
 * tree, coarse-dirt patch, burrows, wild bunnidogs), a burrow house, a landmark, the footpaths or the underground network.
 * The surface layout is rebuilt from its seed. U2 saves the fitted underground cells and each cluster's den flag as well,
 * so terrain checks and elevations never change with chunk order or a saved-piece reload.
 *
 * <p>ANCHOR-CHUNK RULE: Minecraft calls {@link #postProcess} once for every chunk the bounding box touches, but this piece builds
 * clusters, houses and landmarks in the call for their anchor chunk. Paths and tunnels instead write each column only in
 * its owning chunk. Underground cells remain within Settlement.MAX_EXTENT; their bounding box includes every planned Y.
 * Old saves without U2 fields retain their original generation behavior.
 */
public class WarrenPiece extends StructurePiece {
    public static final int CLUSTER = 0;
    public static final int HOUSE = 1;
    public static final int PATH = 2;
    public static final int PROP = 3;
    public static final int TUNNEL = 4;
    /** Bounding boxes reach this far around the anchor (tree + patch + the mouths of a level 1 den, U1); the path box is {@link Settlement#MAX_EXTENT}. */
    private static final int REACH = com.bunnidogs.mod.layout.EntranceTrail.REACH;
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
    private final boolean networkDen;
    private final int[] tunnelCells;
    private final int[] pathExclusions;
    private final int[] wayfinding;
    private final int[] lifeFixtures;
    private final boolean woodDetails;
    private final int generationVersion;

    public WarrenPiece(int kind, int index, long seed, int level, int centreX, int centreZ, int y) {
        this(kind, index, seed, level, centreX, centreZ, y, false, new int[0]);
    }

    public WarrenPiece(int kind, int index, long seed, int level, int centreX, int centreZ, int y, boolean networkDen, int[] tunnelCells) {
        this(kind, index, seed, level, centreX, centreZ, y, networkDen, tunnelCells, new int[0]);
    }

    public WarrenPiece(int kind, int index, long seed, int level, int centreX, int centreZ, int y, boolean networkDen, int[] tunnelCells, int[] pathExclusions) {
        this(kind,index,seed,level,centreX,centreZ,y,networkDen,tunnelCells,pathExclusions,new int[0]);
    }

    public WarrenPiece(int kind, int index, long seed, int level, int centreX, int centreZ, int y, boolean networkDen,
            int[] tunnelCells, int[] pathExclusions, int[] wayfinding) {
        this(kind,index,seed,level,centreX,centreZ,y,networkDen,tunnelCells,pathExclusions,wayfinding,new int[0]);
    }

    public WarrenPiece(int kind, int index, long seed, int level, int centreX, int centreZ, int y, boolean networkDen,
            int[] tunnelCells, int[] pathExclusions, int[] wayfinding, int[] lifeFixtures) {
        super(ModStructures.WARREN_PIECE.get(), 0, boxFor(kind, index, seed, level, centreX, centreZ, y, tunnelCells));
        this.kind = kind;
        this.index = index;
        this.seed = seed;
        this.level = level;
        this.centreX = centreX;
        this.centreZ = centreZ;
        this.y = y;
        this.networkDen = networkDen;
        this.tunnelCells = tunnelCells.clone();
        this.pathExclusions = pathExclusions.clone();
        this.wayfinding = wayfinding.clone();
        this.lifeFixtures = lifeFixtures.clone();
        this.woodDetails = true;
        this.generationVersion = 7;
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
        this.networkDen = tag.getBooleanOr("NetworkDen", false);
        this.tunnelCells = tag.getIntArray("TunnelCells").orElseGet(() -> new int[0]);
        this.pathExclusions = tag.getIntArray("PathExclusions").orElseGet(() -> new int[0]);
        this.wayfinding = tag.getIntArray("Wayfinding").orElseGet(() -> new int[0]);
        this.lifeFixtures = tag.getIntArray("LifeFixtures").orElseGet(() -> new int[0]);
        this.woodDetails = tag.getBooleanOr("WoodDetails",false);
        this.generationVersion = tag.getIntOr("WarrenVersion",1);
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

    private static BoundingBox boxFor(int kind, int index, long seed, int level, int cx, int cz, int y, int[] cells) {
        int[] a = anchor(kind, index, layout(seed, level));
        int reach = kind == PATH || kind == TUNNEL ? Settlement.MAX_EXTENT : kind == HOUSE || kind == PROP ? 3 : REACH;
        int x = cx + a[0];
        int z = cz + a[1];
        int bottom = y - BELOW, top = y + ABOVE;
        for (int i = 0; i + 4 <= cells.length; i += 4) {
            bottom = Math.min(bottom, y + cells[i + 1]);
            top = Math.max(top, y + cells[i + 1]);
        }
        return new BoundingBox(x - reach, bottom, z - reach, x + reach, top, z + reach);
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
        tag.putBoolean("NetworkDen", networkDen);
        if (tunnelCells.length > 0) tag.putIntArray("TunnelCells", tunnelCells);
        if (wayfinding.length > 0) tag.putIntArray("Wayfinding",wayfinding);
        if (lifeFixtures.length > 0) tag.putIntArray("LifeFixtures",lifeFixtures);
        if (pathExclusions.length > 0) tag.putIntArray("PathExclusions", pathExclusions);
        tag.putBoolean("WoodDetails",woodDetails);
        tag.putInt("WarrenVersion",generationVersion);
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureManager structures, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        Settlement s = layout(seed, level);
        // every piece of a settlement takes the style of the biome at ITS CENTRE (Phase 6f), so a village never mixes styles
        Lineage style = ModBiomeTags.lineageAt(world, new BlockPos(centreX, y, centreZ));
        if (kind == TUNNEL) {
            WarrenTunnels.place(world, new BlockPos(centreX, y, centreZ), tunnelCells, chunkBox, style);
            WarrenSigns.place(world,new BlockPos(centreX,y,centreZ),wayfinding,chunkBox);
            WarrenFixtures.place(world,new BlockPos(centreX,y,centreZ),lifeFixtures,chunkBox);
            return;
        }
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
                WarrenFeature.buildCluster(world, ground, s.clusters().get(index).shape(), new Random(seed + 31L * index), true, style,
                        level == 1 && (style != Lineage.END || generationVersion < 2), networkDen,generationVersion >= 3,generationVersion >= 7);
            }
        } else if (kind == PROP) {
            WarrenFeature.buildProp(world, x, y, z, s.props().get(index), true, style,generationVersion >= 4,generationVersion >= 5,generationVersion >= 6);
        } else {
            WarrenFeature.buildHouse(world, x, y, z, s.houses().get(index), true, style, level,woodDetails);
        }
    }

    /** Footpaths (Phase 6c) of the planned trees and houses, each column laid by the chunk that owns it. */
    private void layPaths(WorldGenLevel world, Settlement s, BoundingBox chunkBox, Lineage style) {
        List<Settlement.House> houses = new ArrayList<>(s.houses().subList(0, houseCount(s, level)));
        for (PathShape.Cell cell : PathShape.network(s.clusters(), houses, s.props())) {
            if (pathExcluded(cell.dx(), cell.dz())) continue;
            int x = centreX + cell.dx();
            int z = centreZ + cell.dz();
            if (owns(chunkBox, x, z)) {
                WarrenFeature.layPath(world, x, y, z, true, style);
            }
        }
    }

    private boolean pathExcluded(int x, int z) {
        for (int i = 0; i + 2 <= pathExclusions.length; i += 2) {
            if (pathExclusions[i] == x && pathExclusions[i + 1] == z) return true;
        }
        return false;
    }

    /** How many houses a settlement of this level plans (the old Feature tried spare spots when one did not fit; pieces cannot). */
    public static int houseCount(Settlement s, int level) {
        return Math.min(s.houses().size(), Settlement.housesFor(level));
    }

    private static boolean owns(BoundingBox chunkBox, int x, int z) {
        return x >= chunkBox.minX() && x <= chunkBox.maxX() && z >= chunkBox.minZ() && z <= chunkBox.maxZ();
    }
}
