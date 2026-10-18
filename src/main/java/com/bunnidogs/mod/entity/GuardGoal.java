package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.genetics.Guarding;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModResidentTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.pathfinder.Path;

/** An opted-in resident owns only its guard target and follows complete dry routes near its Nest. */
final class GuardGoal extends Goal {
    private final Bunnidog dog;
    private Mob intruder;
    private BlockPos home;
    private Path path;
    private long nextSearch;
    private int ticks, biteWait;
    GuardGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    private boolean wanted() {
        var status = this.dog.guardingStatus();
        return (status == Guarding.Status.READY || status == Guarding.Status.ACTIVE)
                && this.dog.level().hasChunkAt(this.dog.homeNest())
                && this.dog.level().getBlockState(this.dog.homeNest()).is(ModBlocks.NEST.get());
    }
    private boolean valid(Mob mob) {
        return mob != null && mob.isAlive() && mob instanceof Enemy && mob.is(ModResidentTags.GUARD_INTRUDERS)
                && !(mob instanceof Bunnidog) && !(mob instanceof TamableAnimal) && !this.dog.isAlliedTo(mob)
                && Guarding.within(mob.blockPosition().distSqr(this.dog.homeNest()))
                && this.dog.distanceToSqr(mob) <= Guarding.RADIUS*Guarding.RADIUS && HomeRoutes.visible(this.dog,mob);
    }
    private boolean safe(Path route) {
        if (!HomeRoutes.safe(this.dog,route,null)) return false;
        for (int i = route.getNextNodeIndex(); i < route.getNodeCount(); i++)
            if (!Guarding.within(route.getNode(i).asBlockPos().distSqr(this.home))) return false;
        return true;
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.getTarget() != null || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime()+Guarding.SEARCH_INTERVAL;
        this.home = this.dog.homeNest();
        var candidates = this.dog.level().getEntitiesOfClass(Mob.class,this.dog.getBoundingBox().inflate(Guarding.RADIUS),this::valid)
                .stream().sorted(Comparator.comparingDouble(this.dog::distanceToSqr)).limit(Guarding.PATH_ATTEMPTS).toList();
        for (var mob : candidates) {
            var route = this.dog.getNavigation().createPath(mob,0);
            if (!safe(route)) continue;
            this.intruder = mob; this.path = route; return true;
        }
        this.intruder = null; this.path = null; return false;
    }
    @Override public boolean canContinueToUse() {
        return wanted() && this.home != null && this.home.equals(this.dog.homeNest())
                && this.dog.getTarget() == this.intruder && valid(this.intruder) && this.ticks < Guarding.MAX_PURSUIT;
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() {
        this.ticks = 0; this.biteWait = 0;
        this.dog.guardTarget(this.intruder); this.dog.setTarget(this.intruder);
        this.dog.getNavigation().moveTo(this.path,1.2);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++; this.biteWait--;
        this.dog.getLookControl().setLookAt(this.intruder,30,30);
        if (this.ticks % Guarding.REPATH_INTERVAL == 0) {
            var route = this.dog.getNavigation().createPath(this.intruder,0);
            if (!safe(route)) { this.ticks = Guarding.MAX_PURSUIT; this.dog.getNavigation().stop(); return; }
            this.dog.getNavigation().moveTo(route,1.2);
        } else if (!this.dog.isWithinMeleeAttackRange(this.intruder) && !safe(this.dog.getNavigation().getPath())) {
            this.ticks = Guarding.MAX_PURSUIT; this.dog.getNavigation().stop(); return;
        }
        if (this.biteWait <= 0 && this.dog.isWithinMeleeAttackRange(this.intruder)
                && this.dog.level() instanceof ServerLevel server) {
            this.biteWait = Guarding.BITE_INTERVAL;
            this.dog.doHurtTarget(server,this.intruder);
        }
    }
    @Override public void stop() {
        this.dog.getNavigation().stop();
        if (this.dog.getTarget() == this.intruder) this.dog.setTarget(null);
        this.dog.guardTarget(null); this.intruder = null; this.home = null; this.path = null;
        this.nextSearch = this.dog.level().getGameTime()+Guarding.SEARCH_INTERVAL;
    }
}
