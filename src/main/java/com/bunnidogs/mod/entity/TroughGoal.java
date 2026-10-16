package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Index-based, loaded-only food search. Shares existing home/diet/meal recovery rules. */
final class TroughGoal extends Goal {
    private final Bunnidog dog;
    private BlockPos food, stand;
    private Path path;
    private long nextSearch;
    private int ticks, eating;
    TroughGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    private FeedingTroughBlockEntity trough(BlockPos pos) {
        return pos != null && this.dog.level() instanceof ServerLevel server && server.hasChunkAt(pos)
                && this.dog.foodWithinHome(pos) && server.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough
                && trough.canFeed(this.dog) ? trough : null;
    }
    @Override public boolean canUse() {
        if (!this.dog.canForage() || !(this.dog.level() instanceof ServerLevel server) || server.getGameTime() < this.nextSearch) return false;
        this.nextSearch = server.getGameTime()+Foraging.SEARCH_INTERVAL;
        this.food = null;
        var candidates = server.getPoiManager().findAll(h -> h.is(ModPoi.FEEDING_TROUGH.getKey()),p -> trough(p) != null,
                this.dog.blockPosition(),Foraging.SEARCH_RADIUS,PoiManager.Occupancy.ANY)
                .sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition()))).limit(2).toList();
        for (BlockPos p : candidates) for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos spot = p.relative(direction);
            if (!server.hasChunkAt(spot) || !this.dog.foodWithinHome(spot) || !server.getBlockState(spot).isAir()
                    || !server.getBlockState(spot.above()).isAir() || !server.getFluidState(spot).isEmpty()
                    || !server.getBlockState(spot.below()).isFaceSturdy(server,spot.below(),Direction.UP)) continue;
            Path candidate = this.dog.getNavigation().createPath(spot,0);
            if (candidate != null && candidate.canReach()) { this.food = p.immutable(); this.stand = spot; this.path = candidate; return true; }
        }
        return false;
    }
    @Override public boolean canContinueToUse() { return this.food != null && this.dog.canForage() && trough(this.food) != null && this.ticks < 200; }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { this.ticks = 0; this.eating = 0; this.dog.getNavigation().moveTo(this.path,1); }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++;
        this.dog.getLookControl().setLookAt(Vec3.atCenterOf(this.food));
        if (this.dog.distanceToSqr(Vec3.atCenterOf(this.food)) > 3.24) {
            this.eating = 0;
            if (this.ticks % 20 == 0 && !this.dog.getNavigation().moveTo(this.stand.getX()+.5,this.stand.getY(),this.stand.getZ()+.5,1)) this.ticks = 200;
            return;
        }
        this.dog.getNavigation().stop();
        var sight = this.dog.level().clip(new ClipContext(this.dog.getEyePosition(),Vec3.atCenterOf(this.food),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this.dog));
        if (sight.getType() != HitResult.Type.MISS && !sight.getBlockPos().equals(this.food)) { this.eating = 0; return; }
        if (++this.eating >= Foraging.GRAZE_TICKS) {
            var trough = trough(this.food);
            if (trough != null && trough.takeMeal(this.dog)) this.dog.ateForage(Foraging.HEAL);
            this.ticks = 200;
        }
    }
    @Override public void stop() { this.dog.getNavigation().stop(); this.food = null; this.stand = null; this.path = null; }
}
