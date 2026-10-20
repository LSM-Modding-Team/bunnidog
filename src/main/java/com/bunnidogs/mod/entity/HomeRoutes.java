package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/** Small shared navigation checks; never request terrain or change home tickets. */
final class HomeRoutes {
    private HomeRoutes() {}
    private static final int[][] NEAR_HOME = {{0,0},{-1,0},{1,0},{0,-1},{0,1},{-2,0},{2,0},{0,-2},{0,2}};
    static List<BlockPos> nearHome(BlockPos home) {
        var out = new ArrayList<BlockPos>();
        for (int[] offset : NEAR_HOME) for (int up = 0; up <= 1; up++) out.add(home.offset(offset[0],up,offset[1]));
        return List.copyOf(out);
    }
    static boolean standing(Bunnidog dog, BlockPos pos) {
        return pos!=null && dog.foodWithinHome(pos) && trailStanding(dog,pos);
    }
    static boolean trailStanding(Bunnidog dog, BlockPos pos) {
        var level = dog.level();
        return pos != null && level.hasChunkAt(pos) && level.hasChunkAt(pos.above()) && level.hasChunkAt(pos.below())
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()
                && (level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP)
                    || level.getBlockState(pos.below()).is(Blocks.POWDER_SNOW) && snowFooting(dog)) && dryCell(dog,pos);
    }
    private static boolean snowFooting(Bunnidog dog) { return !dog.isDescending() && PowderSnowBlock.canEntityWalkOnPowderSnow(dog); }
    private static boolean dangerous(Bunnidog dog, PathType type) {
        return switch (type) {
            case WATER, LAVA, FIRE, FIRE_IN_NEIGHBOR, DAMAGING, DAMAGE_CAUTIOUS, POWDER_SNOW -> true;
            case ON_TOP_OF_POWDER_SNOW -> !snowFooting(dog);
            default -> false;
        };
    }
    private static boolean hazardous(Bunnidog dog, PathfindingContext context, BlockPos pos, PathType type) {
        if (dangerous(dog,type)) return true;
        // Native top-of-powder classification skips neighbors; retain the same fire/hazard veto as dry ground.
        return type == PathType.ON_TOP_OF_POWDER_SNOW
                && dangerous(dog,WalkNodeEvaluator.checkNeighbourBlocks(context,pos.getX(),pos.getY(),pos.getZ(),type));
    }
    static boolean dryCell(Bunnidog dog, BlockPos pos) {
        if (!dog.level().hasChunksAt(pos.offset(-1,-1,-1),pos.offset(1,1,1))) return false;
        var context = new PathfindingContext(dog.level(),dog);
        return !hazardous(dog,context,pos,WalkNodeEvaluator.getPathTypeStatic(context,pos.mutable()));
    }
    static boolean visible(Bunnidog dog, Entity mob) {
        BlockPos from = dog.blockPosition(), to = mob.blockPosition();
        var low = new BlockPos(Math.min(from.getX(),to.getX()),Math.min(from.getY(),to.getY()),Math.min(from.getZ(),to.getZ()));
        var high = new BlockPos(Math.max(from.getX(),to.getX()),Math.max(from.getY(),to.getY()),Math.max(from.getZ(),to.getZ()));
        return dog.level().hasChunksAt(low,high) && dog.getSensing().hasLineOfSight(mob);
    }
    static boolean safe(Bunnidog dog, Path route, Mob threat) {
        return safe(dog,route,threat,true);
    }
    static boolean trailSafe(Bunnidog dog,Path route) { return safe(dog,route,null,false); }
    private static boolean safe(Bunnidog dog,Path route,Mob threat,boolean homeBound) {
        if (route == null || homeBound && !route.canReach()) return false;
        var context = new PathfindingContext(dog.level(),dog);
        double clearance = threat == null ? 0 : Math.min(4,dog.blockPosition().distSqr(threat.blockPosition()));
        for (int i = route.getNextNodeIndex(); i < route.getNodeCount(); i++) {
            var node = route.getNode(i);
            if (!dog.level().hasChunkAt(node.asBlockPos()) || homeBound && !dog.foodWithinHome(node.asBlockPos())
                    || threat != null && node.asBlockPos().distSqr(threat.blockPosition()) < clearance) return false;
            var pos = node.asBlockPos();
            if (dangerous(dog,node.type) || !dog.level().hasChunksAt(pos.offset(-1,-1,-1),pos.offset(1,1,1))) return false;
            var current = WalkNodeEvaluator.getPathTypeStatic(context,pos.mutable());
            if (hazardous(dog,context,pos,current)
                    || (node.type == PathType.WALKABLE || node.type == PathType.ON_TOP_OF_POWDER_SNOW) && current == PathType.OPEN) return false;
        }
        return true;
    }
}
