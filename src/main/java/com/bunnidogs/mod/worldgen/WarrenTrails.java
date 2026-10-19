package com.bunnidogs.mod.worldgen;

import java.util.ArrayList;
import java.util.List;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.EntranceTrail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Optional, wholly fitted trails for actual level-one mouths. Never dig a route through an obstacle. */
final class WarrenTrails {
    private WarrenTrails() {}
    private record Column(BlockPos ground, BlockState floor, BlockState feet, BlockState head) {}

    static boolean place(WorldGenLevel level, BlockPos trunk, Lineage style, DenShape.Dir direction, int radial, int doorDy) {
        for (int side : new int[]{1,-1}) {
            var fitted = fit(level,trunk,style,direction,radial,doorDy,side);
            if (fitted.isEmpty()) continue;
            for (var column : fitted) {
                var path = WarrenPalette.get(style,WarrenPalette.Role.PATH);
                if (!column.floor().equals(path)) level.setBlock(column.ground(),path,2);
                if (!column.feet().isAir()) level.setBlock(column.ground().above(),Blocks.AIR.defaultBlockState(),2);
                if (!column.head().isAir()) level.setBlock(column.ground().above(2),Blocks.AIR.defaultBlockState(),2);
            }
            return true;
        }
        return false;
    }
    private static List<Column> fit(WorldGenLevel level, BlockPos trunk, Lineage style, DenShape.Dir direction, int radial, int doorDy, int side) {
        var out = new ArrayList<Column>();
        int previous = trunk.getY()+doorDy;
        for (var column : EntranceTrail.route(direction,radial,side)) {
            var feet = surface(level,trunk,style,column);
            if (feet == null || Math.abs(feet.getY()-previous) > 1) return List.of();
            previous = feet.getY();
            var ground = feet.below(); var floor = level.getBlockState(ground);
            var body = level.getBlockState(feet); var head = level.getBlockState(feet.above());
            if (!naturalFloor(floor,style) || !clear(body) || !clear(head)) return List.of();
            out.add(new Column(ground,floor,body,head));
        }
        return List.copyOf(out);
    }
    private static BlockPos surface(WorldGenLevel level, BlockPos trunk, Lineage style, DenShape.Pos column) {
        for (int dy = WarrenFeature.SURFACE_RISE; dy >= -WarrenFeature.SURFACE_DROP; dy--) {
            var pos = trunk.offset(column.dx(),dy,column.dz()); var state = level.getBlockState(pos);
            if (clear(state)) continue;
            if (!naturalFloor(state,style) || !(state.isFaceSturdy(level,pos,Direction.UP) || state.is(Blocks.DIRT_PATH))
                    || !clear(level.getBlockState(pos.above())) || !clear(level.getBlockState(pos.above(2)))) return null;
            return pos.above();
        }
        return null;
    }
    private static boolean clear(BlockState state) {
        return !state.hasBlockEntity() && state.getFluidState().isEmpty() && (state.isAir() || state.canBeReplaced());
    }
    private static boolean naturalFloor(BlockState state, Lineage style) {
        if (state.hasBlockEntity() || !state.getFluidState().isEmpty()) return false;
        if (state.is(WarrenPalette.get(style,WarrenPalette.Role.PATH).getBlock())) return true;
        if (WarrenPalette.pathsOnSoilOnly(style)) return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT);
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.MUD) || state.is(Blocks.CLAY)
                || state.is(WarrenPalette.get(style,WarrenPalette.Role.FILL).getBlock());
    }
}
