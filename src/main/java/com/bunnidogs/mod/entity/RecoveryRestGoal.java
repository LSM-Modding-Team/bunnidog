package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.genetics.RecoveryRest;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Below every meal/work/home goal: badly hurt residents wait near home rather than strolling. */
final class RecoveryRestGoal extends Goal {
    private final Bunnidog dog;
    private BlockPos home, destination;
    private Path path;
    private long nextSearch;
    private boolean blocked;
    RecoveryRestGoal(Bunnidog dog) { this.dog = dog; setFlags(EnumSet.of(Flag.MOVE)); }
    private boolean wanted() { return !this.dog.trail().ordered() && this.dog.recoveryRestStatus() == RecoveryRest.Status.READY; }
    private boolean validHome() {
        return this.home != null && this.home.equals(this.dog.homeNest()) && this.dog.level().hasChunkAt(this.home)
                && this.dog.level().getBlockState(this.home).is(ModBlocks.NEST.get());
    }
    private boolean standing(BlockPos pos) {
        return pos != null && RecoveryRest.nearNest(pos.distSqr(this.home)) && HomeRoutes.standing(this.dog,pos);
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime()+RecoveryRest.SEARCH_INTERVAL+(this.dog.getUUID().hashCode()&15);
        this.home = this.dog.homeNest(); this.path = null; this.destination = null;
        if (!validHome()) return false;
        if (standing(this.dog.blockPosition())) { this.destination = this.dog.blockPosition(); return true; }
        var candidates = HomeRoutes.nearHome(this.home).stream().filter(this::standing)
                .sorted(Comparator.comparingDouble((BlockPos pos) -> pos.distSqr(this.home))
                        .thenComparingDouble(pos -> pos.distSqr(this.dog.blockPosition()))).toList();
        int attempts = 0;
        for (var pos : candidates) {
            if (++attempts > RecoveryRest.PATH_ATTEMPTS) break;
            var route = this.dog.getNavigation().createPath(pos,0);
            if (!HomeRoutes.safe(this.dog,route,null)) continue;
            this.destination = pos; this.path = route; return true;
        }
        return false;
    }
    @Override public boolean canContinueToUse() { return !this.blocked && wanted() && validHome() && standing(this.destination); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() {
        this.blocked = false;
        if (this.path == null) this.dog.getNavigation().stop(); else this.dog.getNavigation().moveTo(this.path,.8);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        if (RecoveryRest.nearNest(this.dog.blockPosition().distSqr(this.home))
                && this.dog.distanceToSqr(Vec3.atBottomCenterOf(this.destination)) <= .64) this.dog.getNavigation().stop();
        else if (this.dog.getNavigation().isDone() || !HomeRoutes.safe(this.dog,this.dog.getNavigation().getPath(),null)) {
            this.dog.getNavigation().stop(); this.blocked = true;
        }
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.home = null; this.destination = null; this.path = null;
        this.nextSearch = this.dog.level().getGameTime()+RecoveryRest.SEARCH_INTERVAL;
    }
}
