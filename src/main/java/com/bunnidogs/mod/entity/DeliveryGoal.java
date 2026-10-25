package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.*;

/** Two actual walking legs, a small owned parcel, and safe local retries. Never requests remote chunks. */
final class DeliveryGoal extends Goal {
    private final Bunnidog dog;
    private BlockPos target,stand;
    private Path path;
    private boolean pickup;
    private int elapsed,work;
    private long search;
    DeliveryGoal(Bunnidog dog) {this.dog=dog;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    @Override public boolean canUse() {
        var memory=dog.deliveries();if(!memory.canWork() || dog.level().getGameTime()<search)return false;search=dog.level().getGameTime()+60;
        var src=memory.endpoint(true);var dst=memory.endpoint(false);
        pickup=memory.cargo().isEmpty();
        if(dst==null || pickup && src==null) {memory.report(DeliveryMemory.State.CHANGED_STORE);return false;}
        if(!memory.allowed(dst) || pickup && !memory.allowed(src)) {memory.report(DeliveryMemory.State.PAUSED_STORE);return false;}
        pickup=memory.cargo().isEmpty();target=(pickup ? src : dst).stop().at().pos();
        if(dog.distanceToSqr(Vec3.atCenterOf(target))>48*48 || !loaded(target)) {memory.report(DeliveryMemory.State.BLOCKED);return false;}
        if(pickup && !memory.hasShipment())return false;
        // Before taking anything, prove both stops are presently reachable from the same loaded walkable area.
        if(pickup && approach(dst.stop().at().pos())==null) {memory.report(DeliveryMemory.State.BLOCKED);return false;}
        path=approach(target);if(path==null) {memory.report(DeliveryMemory.State.BLOCKED);return false;}return true;
    }
    private boolean loaded(BlockPos at) {var from=dog.blockPosition();return dog.level().hasChunksAt(new BlockPos(Math.min(from.getX(),at.getX())-16,from.getY(),Math.min(from.getZ(),at.getZ())-16),new BlockPos(Math.max(from.getX(),at.getX())+16,from.getY(),Math.max(from.getZ(),at.getZ())+16));}
    private Path approach(BlockPos at) {
        if(!loaded(at))return null;
        for(var d:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,-1,1}) {
            var p=at.relative(d).offset(0,dy,0);if(!HomeRoutes.trailStanding(dog,p))continue;var route=dog.getNavigation().createPath(p,0);
            if(route!=null && route.canReach() && HomeRoutes.trailSafe(dog,route)) {stand=p;return route;}
        }return null;
    }
    @Override public boolean canContinueToUse() {return target!=null && dog.deliveries().canWork() && elapsed<300 && loaded(target) && HomeRoutes.trailStanding(dog,stand) && HomeRoutes.trailSafe(dog,path) && dog.deliveries().allowed(dog.deliveries().endpoint(pickup));}
    @Override public void start() {elapsed=work=0;dog.deliveries().walking();dog.getNavigation().moveTo(path,1);}
    @Override public boolean requiresUpdateEveryTick() {return true;}
    @Override public void tick() {
        if(!canContinueToUse())return;elapsed++;dog.getLookControl().setLookAt(Vec3.atCenterOf(target));
        var hit=dog.level().clip(new ClipContext(dog.getEyePosition(),Vec3.atCenterOf(target),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dog));
        if(dog.distanceToSqr(Vec3.atCenterOf(target))>6.25 || hit.getType()!=HitResult.Type.MISS && !hit.getBlockPos().equals(target)) {work=0;if(elapsed%20==0) {var next=approach(target);if(next==null) {dog.deliveries().report(DeliveryMemory.State.BLOCKED);elapsed=300;}else {path=next;dog.getNavigation().moveTo(path,1);}}return;}
        dog.getNavigation().stop();if(++work>=30) {if(pickup)dog.deliveries().pickup();else dog.deliveries().deposit();elapsed=300;}
    }
    @Override public void stop() {dog.getNavigation().stop();target=null;stand=null;path=null;}
}
