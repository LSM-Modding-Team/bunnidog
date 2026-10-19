package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gardening;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Shared seed-stock and renewable-plant rules for direct meals and opted-in trough work. */
final class ResidentPlants {
    private ResidentPlants() {}
    static boolean spareCactus(Bunnidog dog, BlockPos pos) {
        var level = dog.level();
        if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.below()) || !level.hasChunkAt(pos.above())) return false;
        var state = level.getBlockState(pos);
        return state.is(Blocks.CACTUS) && level.getBlockState(pos.below()).is(Blocks.CACTUS)
                && level.getBlockState(pos.above()).isAir() && state.canSurvive(level,pos);
    }
    static boolean mushroom(BlockState state) {
        return state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM);
    }
    static boolean spareMushroom(Bunnidog dog, BlockPos pos) {
        var level = dog.level();
        if (!level.hasChunkAt(pos)) return false;
        var state = level.getBlockState(pos);
        if (!mushroom(state) || !state.canSurvive(level,pos)) return false;
        int radius = Gardening.MUSHROOM_RADIUS;
        for (var other : BlockPos.betweenClosed(pos.offset(-radius,-1,-radius),pos.offset(radius,1,radius))) {
            if (other.equals(pos) || pos.distSqr(other) > radius*radius || !level.hasChunkAt(other) || !dog.foodWithinHome(other)) continue;
            var companion = level.getBlockState(other);
            if (companion.is(state.getBlock()) && companion.canSurvive(level,other)) return true;
        }
        return false;
    }
    static boolean linkedMelon(Bunnidog dog, BlockPos fruit) {
        var level = dog.level();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos pos = fruit.relative(direction);
            if (!level.hasChunkAt(pos) || !dog.foodWithinHome(pos)) continue;
            var stem = level.getBlockState(pos);
            if (stem.is(Blocks.ATTACHED_MELON_STEM)
                    && stem.getValue(HorizontalDirectionalBlock.FACING) == direction.getOpposite()
                    && stem.canSurvive(level,pos)) return true;
        }
        return false;
    }
    static Vec3 sightPoint(Bunnidog dog, BlockPos pos) {
        var state = dog.level().getBlockState(pos);
        if (!state.is(Blocks.CACTUS)) return Vec3.atCenterOf(pos);
        // The base can hide a tall top's centre while its near vertical face remains visible.
        var eye = dog.getEyePosition(); var bounds = state.getCollisionShape(dog.level(),pos).bounds();
        return new Vec3(Math.clamp(eye.x(),pos.getX()+bounds.minX+.001,pos.getX()+bounds.maxX-.001),
                pos.getY()+(bounds.minY+bounds.maxY)/2,
                Math.clamp(eye.z(),pos.getZ()+bounds.minZ+.001,pos.getZ()+bounds.maxZ-.001));
    }
}
