package com.bunnidogs.mod.worldgen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.layout.TunnelShape;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModBiomeTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Minecraft adapters for the pure underground planner. Natural structures save the plan before any pieces run. */
final class WarrenTunnels {
    private WarrenTunnels() {}
    private static final int STRIDE = 4;
    private static final DenShape.Kind[] KINDS = DenShape.Kind.values();
    private static final ResourceKey<LootTable> PANTRY_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("bunnidogs", "chests/warren_pantry"));

    static TunnelShape.Network plan(WorldGenLevel world, BlockPos origin, Settlement layout, long seed, boolean surface, boolean open) {
        TunnelShape.Terrain terrain = new Terrain(origin, world, p -> world.getBlockState(p)) {
            @Override public Integer surface(int x, int z) {
                BlockPos floor = WarrenFeature.groundAt(world, origin.getX() + x, origin.getY(), origin.getZ() + z, surface);
                return floor == null ? null : floor.getY() - origin.getY();
            }
        };
        return open ? TunnelShape.planOpen(layout,seed,terrain) : TunnelShape.plan(layout,seed,terrain);
    }

    static TunnelShape.Network plan(Structure.GenerationContext context, BlockPos origin, Settlement layout, long seed) {
        // Base columns do not depend on feature/piece order, and can be sampled without loading neighbouring chunks.
        Map<Long, NoiseColumn> columns = new HashMap<>();
        Function<BlockPos, BlockState> reader = p -> columns.computeIfAbsent(key(p.getX(), p.getZ()),
                ignored -> context.chunkGenerator().getBaseColumn(p.getX(), p.getZ(), context.heightAccessor(), context.randomState())).getBlock(p.getY());
        TunnelShape.Terrain terrain = new Terrain(origin, EmptyBlockGetter.INSTANCE, reader) {
            private final Map<Long, Integer> heights = new HashMap<>();

            @Override public Integer surface(int x, int z) {
                int absoluteX = origin.getX() + x, absoluteZ = origin.getZ() + z;
                int y = heights.computeIfAbsent(key(x, z), ignored -> context.chunkGenerator().getBaseHeight(absoluteX, absoluteZ,
                        Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState())) - origin.getY();
                TunnelShape.Pos p = new TunnelShape.Pos(x, y, z);
                return solid(new TunnelShape.Pos(x, y - 1, z)) && replaceable(p) && replaceable(new TunnelShape.Pos(x, y + 1, z)) ? y : null;
            }
        };
        boolean end = context.biomeSource().getNoiseBiome(origin.getX() >> 2, origin.getY() >> 2, origin.getZ() >> 2,
                context.randomState().sampler()).is(ModBiomeTags.lineage(Lineage.END));
        return end ? TunnelShape.planOpen(layout,seed,terrain) : TunnelShape.plan(layout,seed,terrain);
    }

    private abstract static class Terrain implements TunnelShape.Terrain {
        private final BlockPos origin;
        private final BlockGetter getter;
        private final Function<BlockPos, BlockState> reader;

        Terrain(BlockPos origin, BlockGetter getter, Function<BlockPos, BlockState> reader) {
            this.origin = origin;
            this.getter = getter;
            this.reader = reader;
        }

        private BlockPos position(TunnelShape.Pos p) { return origin.offset(p.x(), p.y(), p.z()); }
        private BlockState state(TunnelShape.Pos p) { return reader.apply(position(p)); }
        @Override public boolean diggable(TunnelShape.Pos p) {
            BlockState state = state(p);
            if (!state.getFluidState().isEmpty() || state.hasBlockEntity()) return false;
            if (state.isAir()) return true;
            float hardness = state.getDestroySpeed(getter, position(p));
            return hardness >= 0.0F && hardness <= 3.0F;
        }
        @Override public boolean solid(TunnelShape.Pos p) {
            BlockState state = state(p);
            return state.getFluidState().isEmpty() && state.isCollisionShapeFullBlock(getter, position(p));
        }
        @Override public boolean replaceable(TunnelShape.Pos p) { return GuavaTree.replaceable(state(p)) && !state(p).hasBlockEntity(); }
        @Override public boolean dry(TunnelShape.Pos p) { return state(p).getFluidState().isEmpty(); }
    }

    /** Fixed tuples, rather than regenerated terrain queries: piece reloads preserve exact heights and fitted entrances. */
    static int[] encode(TunnelShape.Network network) {
        int[] out = new int[network.cells().size() * STRIDE];
        int i = 0;
        for (TunnelShape.Cell cell : network.cells()) {
            out[i++] = cell.pos().x(); out[i++] = cell.pos().y(); out[i++] = cell.pos().z(); out[i++] = cell.kind().ordinal();
        }
        return out;
    }

    static int[] pathExclusions(TunnelShape.Network network) {
        int[] out = new int[network.pathExclusions().size() * 2];
        int i = 0;
        // Stable ordering makes saved plans reproducible, too.
        for (DenShape.Pos p : network.pathExclusions().stream().sorted(java.util.Comparator.comparingInt(DenShape.Pos::dx).thenComparingInt(DenShape.Pos::dz)).toList()) {
            out[i++] = p.dx(); out[i++] = p.dz();
        }
        return out;
    }

    /** Places whole columns only in their owning chunk; null chunkBox is the command/feature path. */
    static void place(WorldGenLevel world, BlockPos origin, int[] data, BoundingBox chunkBox, Lineage style) {
        Map<Long, List<Integer>> columns = new java.util.LinkedHashMap<>();
        for (int i = 0; i + STRIDE <= data.length; i += STRIDE) {
            int x = origin.getX() + data[i], z = origin.getZ() + data[i + 2];
            if (chunkBox == null || x >= chunkBox.minX() && x <= chunkBox.maxX() && z >= chunkBox.minZ() && z <= chunkBox.maxZ()) {
                columns.computeIfAbsent(key(x, z), ignored -> new java.util.ArrayList<>()).add(i);
            }
        }
        for (List<Integer> column : columns.values()) {
            // A later structure/feature must not make us destroy a container or open an aquifer.
            boolean safe = true;
            for (int i : column) {
                BlockPos pos = origin.offset(data[i], data[i + 1], data[i + 2]);
                BlockState existing = world.getBlockState(pos);
                float hardness = existing.getDestroySpeed(world, pos);
                if (existing.hasBlockEntity() || !existing.getFluidState().isEmpty() || hardness < 0.0F || hardness > 3.0F) { safe = false; break; }
            }
            if (!safe) continue;
            for (int i : column) {
                int kind = data[i + 3];
                if (kind >= 0 && kind < KINDS.length) {
                    BlockPos pos = origin.offset(data[i], data[i + 1], data[i + 2]);
                    world.setBlock(pos, state(KINDS[kind], style), 2);
                    // Only newly placed barrels reach here: preflight rejects existing block entities.
                    if (KINDS[kind] == DenShape.Kind.BARREL && world.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity barrel) {
                        barrel.setLootTable(PANTRY_LOOT);
                        barrel.setLootTableSeed(world.getSeed() ^ pos.asLong());
                        barrel.setChanged();
                    }
                }
            }
        }
    }

    static BlockState state(DenShape.Kind kind, Lineage style) {
        return switch (kind) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case FILL -> WarrenPalette.get(style, WarrenPalette.Role.FILL);
            case WALL -> WarrenPalette.get(style, WarrenPalette.Role.WALL);
            case ROOF -> WarrenPalette.get(style, WarrenPalette.Role.ROOF);
            case BEAM -> WarrenPalette.get(style, WarrenPalette.Role.BEAM);
            case NEST -> ModBlocks.NEST.get().defaultBlockState();
            case HAY -> WarrenPalette.get(style, WarrenPalette.Role.HAY);
            case LANTERN -> Blocks.LANTERN.defaultBlockState();
            case CEILING_LANTERN -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
            case BARREL -> Blocks.BARREL.defaultBlockState();
            case WATER -> Blocks.WATER.defaultBlockState();
            case GLASS -> Blocks.GLASS.defaultBlockState();
            case ROOTS -> Blocks.HANGING_ROOTS.defaultBlockState();
            case HATCH_X, HATCH_Z -> ModBlocks.GUAVA_TRAPDOOR.get().defaultBlockState().setValue(TrapDoorBlock.OPEN, true)
                    .setValue(TrapDoorBlock.FACING, kind == DenShape.Kind.HATCH_X ? Direction.EAST : Direction.NORTH);
        };
    }

    private static long key(int x, int z) { return (long) x << 32 ^ (z & 0xffffffffL); }
}
