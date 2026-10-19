package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.List;
import com.bunnidogs.mod.genetics.Pen;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Loaded-only, read-only pair inspection. No path requests, lures, home changes or forced seed spawning. */
public final class PenBreeding {
    private PenBreeding() {}
    public record Check(Pen.Status status, List<Bunnidog> parents) {
        public Check { parents = List.copyOf(parents); }
    }
    public static Check inspect(ServerLevel level, BlockPos pos, Direction facing) {
        int reach = Pen.ISOLATION+1;
        if (!level.hasChunksAt(pos.offset(-reach,0,-reach),pos.offset(reach,0,reach))) return new Check(Pen.Status.UNLOADED,List.of());
        var center = Vec3.atCenterOf(pos);
        var parents = level.getEntitiesOfClass(Bunnidog.class,new AABB(pos).inflate(Pen.ISOLATION),
                dog -> dog.isAlive() && dog.acceptsCare() && !dog.isBaby());
        if (parents.size() != 2) return new Check(Pen.Status.PAIR,parents);
        Direction right = facing.getCounterClockWise();
        parents.sort(Comparator.comparingDouble((Bunnidog dog) -> (dog.getX()-center.x)*right.getStepX()
                +(dog.getZ()-center.z)*right.getStepZ()).thenComparing(Bunnidog::getUUID));
        var a = parents.get(0); var b = parents.get(1);
        if (a.distanceToSqr(center) > Pen.RANGE*Pen.RANGE || b.distanceToSqr(center) > Pen.RANGE*Pen.RANGE
                || a.distanceToSqr(b) >= Pen.PAIR_DISTANCE*Pen.PAIR_DISTANCE) return new Check(Pen.Status.TOO_FAR,parents);
        for (var dog : parents) {
            if (!dog.readyForPen()) return new Check(Pen.Status.UNAVAILABLE,parents);
            var nest = dog.homeNest();
            if (nest == null || !level.hasChunkAt(nest) || !level.getBlockState(nest).is(ModBlocks.NEST.get())
                    || !dog.foodWithinHome(dog.blockPosition())) return new Check(Pen.Status.HOME,parents);
            if (!level.getWorldBorder().isWithinBounds(dog.getBoundingBox()) || !HomeRoutes.standing(dog,dog.blockPosition()))
                return new Check(Pen.Status.UNSAFE,parents);
        }
        if (!a.hasLineOfSight(b) || !b.hasLineOfSight(a)) return new Check(Pen.Status.UNSAFE,parents);
        return new Check(Pen.Status.READY,parents);
    }
}
