package com.bunnidogs.mod.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * U1 (docs/WARREN_PLAN.md): the den under the tree of a level 1 surface warren and the mounds (mouths) that lead down into it. The shape
 * is {@link DenShape} (pure); this class only reads the ground, decides whether it fits, and sets blocks.
 *
 * <p>Everything is tested BEFORE the first block is set, so {@link #build} either builds the den with at least one mouth or changes nothing
 * (and answers false, so the caller falls back to the old surface holes). The den needs: ground under all 25 of its columns (a solid floor
 * and a solid layer above it), no fluid in or beside it, and nothing harder than stone to dig. A mouth is tried on each side in the order
 * the layout gives, at {@link DenShape#MIN_RADIUS} to {@link DenShape#MAX_RADIUS} columns from the trunk, and needs: ground under all 12
 * columns of its mound (at most {@link WarrenFeature#HOUSE_MAX_SLOPE} blocks of difference, the low ones are filled up), a free mound and
 * doorstep, and a covered slope (3 high) with a solid floor and ceiling and no fluid beside it.
 */
final class WarrenDen {
    private static final int FLAGS = 2;

    private WarrenDen() {}

    /** One mouth that passed every test: its side, the door's distance from the trunk, its ground level and the ground it stands on. */
    private record Plan(DenShape.Dir dir, int radial, int doorDy, int top, List<BlockPos> grounds, Map<Integer, Integer> surfaceBlockY) {}

    /** Digs the den under {@code floor} (the first air block above the ground at the trunk) and builds its mouths; false, changing nothing, if the den does not fit. */
    static boolean build(WorldGenLevel level, BlockPos floor, Lineage style, Random rng) {
        DenShape.Den den = DenShape.generate(rng);
        if (!denFits(level, floor)) {
            return false;
        }
        List<Plan> plans = new ArrayList<>();
        for (DenShape.Dir dir : den.dirs()) {
            if (plans.size() == DenShape.MOUTHS) {
                break;
            }
            Plan plan = plan(level, floor, dir);
            if (plan != null) {
                plans.add(plan);
            }
        }
        if (plans.isEmpty()) {
            return false;
        }
        for (DenShape.Cell c : DenShape.denAir()) {
            set(level, floor, c, Blocks.AIR.defaultBlockState());
        }
        for (DenShape.Cell c : DenShape.denLining()) {
            set(level, floor, c, state(c.kind(), style));
        }
        for (Plan plan : plans) {
            writeMouth(level, floor, style, plan);
        }
        for (DenShape.Cell c : den.furnishing()) {
            set(level, floor, c, state(c.kind(), style));
        }
        return true;
    }

    // ------------------------------------------------------------------ tests (read only)

    private static boolean denFits(WorldGenLevel level, BlockPos floor) {
        for (DenShape.Cell c : DenShape.denAir()) {
            if (!clearable(level, floor.offset(c.dx(), c.dy(), c.dz()))) {
                return false;
            }
        }
        for (int dx = -DenShape.HALF; dx <= DenShape.HALF; dx++) {
            for (int dz = -DenShape.HALF; dz <= DenShape.HALF; dz++) {
                if (!solid(level.getBlockState(floor.offset(dx, -DenShape.DEPTH + DenShape.HEIGHT, dz)))
                        || !solid(level.getBlockState(floor.offset(dx, -DenShape.DEPTH - 1, dz)))) {
                    return false;
                }
            }
        }
        int ring = DenShape.HALF + 1;
        for (int dx = -ring; dx <= ring; dx++) {
            for (int dz = -ring; dz <= ring; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                    continue;
                }
                for (int dy = -DenShape.DEPTH - 1; dy <= -DenShape.DEPTH + DenShape.HEIGHT; dy++) {
                    if (!level.getBlockState(floor.offset(dx, dy, dz)).getFluidState().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static Plan plan(WorldGenLevel level, BlockPos floor, DenShape.Dir dir) {
        for (int radial = DenShape.MIN_RADIUS; radial <= DenShape.MAX_RADIUS; radial++) {
            Plan plan = planAt(level, floor, dir, radial);
            if (plan != null) {
                return plan;
            }
        }
        return null;
    }

    private static Plan planAt(WorldGenLevel level, BlockPos floor, DenShape.Dir dir, int radial) {
        List<BlockPos> grounds = new ArrayList<>();
        int top = Integer.MIN_VALUE;
        int low = Integer.MAX_VALUE;
        for (DenShape.Pos p : DenShape.padColumns(dir, radial)) {
            BlockPos g = WarrenFeature.groundAt(level, floor.getX() + p.dx(), floor.getY(), floor.getZ() + p.dz(), true);
            if (g == null) {
                return null;
            }
            grounds.add(g);
            top = Math.max(top, g.getY());
            low = Math.min(low, g.getY());
        }
        if (top - low > WarrenFeature.HOUSE_MAX_SLOPE) {
            return null;
        }
        int doorDy = top - floor.getY();
        if (!DenShape.canReachDen(doorDy, radial)) {
            return null;
        }
        for (BlockPos g : grounds) {
            for (int y = g.getY(); y < top; y++) {
                if (!GuavaTree.replaceable(level.getBlockState(new BlockPos(g.getX(), y, g.getZ())))) {
                    return null;
                }
            }
        }
        for (DenShape.Cell c : DenShape.mound(dir, radial, doorDy)) {
            if (!GuavaTree.replaceable(level.getBlockState(floor.offset(c.dx(), c.dy(), c.dz())))) {
                return null;
            }
        }
        for (DenShape.Cell c : DenShape.air(dir, radial, doorDy)) {
            if (!clearable(level, floor.offset(c.dx(), c.dy(), c.dz()))) {
                return null;
            }
        }
        DenShape.Pos door = DenShape.at(dir, radial + 1, 0);
        BlockPos doorstep = floor.offset(door.dx(), doorDy, door.dz());
        if (!GuavaTree.replaceable(level.getBlockState(doorstep)) || !GuavaTree.replaceable(level.getBlockState(doorstep.above()))) {
            return null;
        }
        // the covered slope outside the mound: its ceiling must be underground, solid; its floor solid; no fluid beside it
        Map<Integer, Integer> surface = new HashMap<>();
        for (int u : DenShape.tunnelRadials(radial)) {
            DenShape.Pos col = DenShape.at(dir, u, 0);
            BlockPos g = WarrenFeature.groundAt(level, floor.getX() + col.dx(), floor.getY(), floor.getZ() + col.dz(), true);
            if (g == null) {
                return null;
            }
            int stand = floor.getY() + DenShape.standDy(doorDy, radial, u);
            int surfaceBlock = g.getY() - 1;
            if (stand + 3 > surfaceBlock) {
                return null;
            }
            int x = floor.getX() + col.dx();
            int z = floor.getZ() + col.dz();
            if (!solid(level.getBlockState(new BlockPos(x, stand - 1, z))) || !solid(level.getBlockState(new BlockPos(x, stand + 3, z)))) {
                return null;
            }
            for (int v = -1; v <= 1; v += 2) {
                DenShape.Pos side = DenShape.at(dir, u, v);
                for (int y = stand - 1; y <= stand + 3; y++) {
                    if (!level.getBlockState(new BlockPos(floor.getX() + side.dx(), y, floor.getZ() + side.dz())).getFluidState().isEmpty()) {
                        return null;
                    }
                }
            }
            surface.put(u, surfaceBlock);
        }
        return new Plan(dir, radial, doorDy, top, grounds, surface);
    }

    // ------------------------------------------------------------------ writes

    private static void writeMouth(WorldGenLevel level, BlockPos floor, Lineage style, Plan plan) {
        for (BlockPos g : plan.grounds()) {
            for (int y = g.getY(); y < plan.top(); y++) {
                level.setBlock(new BlockPos(g.getX(), y, g.getZ()), WarrenPalette.get(style, WarrenPalette.Role.FILL), FLAGS);
            }
        }
        for (DenShape.Cell c : DenShape.mound(plan.dir(), plan.radial(), plan.doorDy())) {
            set(level, floor, c, state(c.kind(), style));
        }
        for (DenShape.Cell c : DenShape.air(plan.dir(), plan.radial(), plan.doorDy())) {
            set(level, floor, c, Blocks.AIR.defaultBlockState());
        }
        for (int u : DenShape.tunnelRadials(plan.radial())) {
            DenShape.Pos col = DenShape.at(plan.dir(), u, 0);
            int ceiling = floor.getY() + DenShape.standDy(plan.doorDy(), plan.radial(), u) + 3;
            if (ceiling < plan.surfaceBlockY().get(u)) {
                level.setBlock(new BlockPos(floor.getX() + col.dx(), ceiling, floor.getZ() + col.dz()), WarrenPalette.get(style, WarrenPalette.Role.FILL), FLAGS);
            }
        }
    }

    private static void set(WorldGenLevel level, BlockPos floor, DenShape.Cell c, BlockState state) {
        level.setBlock(floor.offset(c.dx(), c.dy(), c.dz()), state, FLAGS);
    }

    private static BlockState state(DenShape.Kind kind, Lineage style) {
        return WarrenTunnels.state(kind, style);
    }

    /** Air, or something soft and plain to dig out: not a fluid, not a container, nothing harder than stone (the rule of the old burrow holes). */
    private static boolean clearable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        if (!state.getFluidState().isEmpty() || state.hasBlockEntity()) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        return hardness >= 0.0F && hardness <= 3.0F;
    }

    /** Something that holds: not air, not a fluid. */
    private static boolean solid(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }
}
