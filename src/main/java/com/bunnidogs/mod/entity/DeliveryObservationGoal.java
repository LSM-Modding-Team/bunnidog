package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** A learner accompanies the demonstrating player; fear, combat and explicit orders retain priority. */
final class DeliveryObservationGoal extends Goal {
    private final Bunnidog dog;private long search;
    DeliveryObservationGoal(Bunnidog dog) {this.dog=dog;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    @Override public boolean canUse() {return dog.deliveries().watching() && dog.deliveries().readyBody();}
    @Override public boolean canContinueToUse() {return canUse();}
    @Override public boolean requiresUpdateEveryTick() {return true;}
    @Override public void tick() {
        if(!(dog.level() instanceof ServerLevel level))return;var p=level.getPlayerByUUID(dog.deliveries().teacher());if(p==null || !p.isAlive()) {dog.deliveries().stopWatching();return;}
        dog.getLookControl().setLookAt(p);if(dog.distanceToSqr(p)<=16) {dog.getNavigation().stop();return;}
        if(level.getGameTime()<search)return;search=level.getGameTime()+20;
        if(dog.distanceToSqr(p)>24*24 || !level.hasChunksAt(dog.blockPosition().offset(-16,0,-16),dog.blockPosition().offset(16,0,16)))return;
        var route=dog.getNavigation().createPath(p,2);if(route!=null && route.canReach() && HomeRoutes.trailSafe(dog,route))dog.getNavigation().moveTo(route,1.1);
    }
    @Override public void stop() {dog.getNavigation().stop();}
}
