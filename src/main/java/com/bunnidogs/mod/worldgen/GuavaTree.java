package com.bunnidogs.mod.worldgen;

import java.util.List;
import java.util.Map;

import com.bunnidogs.mod.block.GuavaSaplingBlock;
import com.bunnidogs.mod.layout.TreeShape;
import com.bunnidogs.mod.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds a guava tree ({@link TreeShape}) in the world (Phase 4g): from a sapling that grew, and in the middle of every warren.
 * Guava logs and guava leaves (Phase 7a; before that jungle wood with PERSISTENT leaves). The leaves are NOT persistent: they decay when
 * the logs are cut, and the distance state is written explicitly because nobody updates it while a chunk is generated. Plus
 * {@code guava_fruit} blocks hanging under the lowest leaves. Works on any {@link LevelAccessor}, so the world
 * generator and a running server use the same code.
 */
public final class GuavaTree {
    private GuavaTree() {}

    /** The flag for setBlock: send the change to clients, no neighbour updates (what worldgen features use). */
    private static final int FLAGS = 2;

    /** Whether a block may be overwritten by the tree: air, grass and other replaceable plants, snow layers, the sapling itself; never a fluid. */
    public static boolean replaceable(BlockState state) {
        return state.getFluidState().isEmpty()
                && (state.isAir() || state.canBeReplaced() || state.getBlock() instanceof GuavaSaplingBlock);
    }

    /** True if every LOG cell of the tree can be placed. Leaves and fruit that do not fit are simply left out later. */
    public static boolean fits(LevelAccessor level, BlockPos base, List<TreeShape.Cell> cells) {
        for (TreeShape.Cell cell : cells) {
            if (cell.kind() == TreeShape.Kind.LOG && !replaceable(level.getBlockState(offset(base, cell)))) {
                return false;
            }
        }
        return true;
    }

    /** Places the tree: logs first, then the leaves that fit, then the fruits that have a leaf above them. */
    public static void place(LevelAccessor level, BlockPos base, List<TreeShape.Cell> cells) {
        for (TreeShape.Cell cell : cells) {
            if (cell.kind() == TreeShape.Kind.LOG) {
                level.setBlock(offset(base, cell), ModBlocks.GUAVA_LOG.get().defaultBlockState(), FLAGS);
            }
        }
        // Phase 7a: guava leaves DECAY like vanilla ones (not persistent). The tree is placed without neighbour updates, so every leaf
        // gets its distance to the nearest log written here (TreeShape.leafDistances); a wrong 7 would make it decay at once.
        BlockState leaves = ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, false);
        Map<TreeShape.Cell, Integer> distances = TreeShape.leafDistances(cells);
        for (TreeShape.Cell cell : cells) {
            if (cell.kind() == TreeShape.Kind.LEAVES && replaceable(level.getBlockState(offset(base, cell)))) {
                level.setBlock(offset(base, cell), leaves.setValue(LeavesBlock.DISTANCE, distances.get(cell)), FLAGS);
            }
        }
        for (TreeShape.Cell cell : cells) {
            if (cell.kind() == TreeShape.Kind.FRUIT) {
                BlockPos pos = offset(base, cell);
                if (replaceable(level.getBlockState(pos)) && level.getBlockState(pos.above()).getBlock() instanceof LeavesBlock) {
                    level.setBlock(pos, ModBlocks.GUAVA_FRUIT.get().defaultBlockState(), FLAGS);
                }
            }
        }
    }

    private static BlockPos offset(BlockPos base, TreeShape.Cell cell) {
        return base.offset(cell.dx(), cell.dy(), cell.dz());
    }
}
