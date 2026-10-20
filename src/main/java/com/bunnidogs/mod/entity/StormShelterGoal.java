package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.genetics.StormShelter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Idle adults wait under cover during thunder, yielding to every care/work/home goal. */
final class StormShelterGoal extends Goal {
    private final Bunnidog dog;
    private BlockPos home, destination;
    private Path path;
    private long nextSearch;
    private boolean blocked;
    StormShelterGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE)); }
    private boolean wanted() {
        return !this.dog.trail().ordered() && this.dog.acceptsCare() && this.dog.isAlive() && this.dog.level() instanceof ServerLevel server
                && StormShelter.eligible(!this.dog.isBaby(),this.dog.homeNest() != null,this.dog.beingTransported(),
                        this.dog.getTarget() != null || this.dog.seekingRefuge(),this.dog.foodWithinHome(this.dog.blockPosition()),server.isThundering());
    }
    private boolean covered(BlockPos pos) {
        return HomeRoutes.standing(this.dog,pos) && !this.dog.level().canSeeSky(pos);
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime()+StormShelter.SEARCH_INTERVAL+(this.dog.getUUID().hashCode()&15);
        this.home = this.dog.homeNest(); this.path = null; this.destination = null;
        if (covered(this.dog.blockPosition())) {
            this.destination = this.dog.blockPosition(); return true;
        }
        if (!this.dog.level().isRainingAt(this.dog.blockPosition())) return false;
        var candidates = HomeRoutes.nearHome(this.home).stream().filter(this::covered)
                .sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition()))).toList();
        int attempts = 0;
        for (BlockPos pos : candidates) {
            if (++attempts > StormShelter.PATH_ATTEMPTS) break;
            Path route = this.dog.getNavigation().createPath(pos,0);
            if (!HomeRoutes.safe(this.dog,route,null)) continue;
            this.destination = pos; this.path = route; return true;
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        return !this.blocked && wanted() && this.home != null && this.home.equals(this.dog.homeNest()) && covered(this.destination);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() {
        this.blocked = false;
        if (this.path == null) this.dog.getNavigation().stop();
        else this.dog.getNavigation().moveTo(this.path,1);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        if (this.dog.distanceToSqr(Vec3.atBottomCenterOf(this.destination)) <= 1) this.dog.getNavigation().stop();
        else if (this.dog.getNavigation().isDone() || !HomeRoutes.safe(this.dog,this.dog.getNavigation().getPath(),null)) {
            this.dog.getNavigation().stop(); this.blocked = true;
        }
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.home = null; this.destination = null; this.path = null;
        this.nextSearch = this.dog.level().getGameTime()+StormShelter.SEARCH_INTERVAL;
    }
}
