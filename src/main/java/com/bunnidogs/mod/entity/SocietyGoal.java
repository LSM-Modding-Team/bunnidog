package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;

/** Real path/visible work at each station; competing safety, conversation and recall goals retain priority. */
final class SocietyGoal extends Goal {
    private final Bunnidog dog;private SocietyWork.Job job;private Path path;private BlockPos stand;private int ticks,work;private long search;
    SocietyGoal(Bunnidog d) {dog=d;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    @Override public boolean canUse() {
        if(dog.level().getGameTime()<search)return false;search=dog.level().getGameTime()+40;
        job=dog.society().plan();if(job==null)return false;
        for(var dir:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,-1,1,-2,-3}) {var p=job.target().relative(dir).offset(0,dy,0);if(!HomeRoutes.trailStanding(dog,p))continue;var route=dog.getNavigation().createPath(p,0);if(route!=null && route.canReach() && HomeRoutes.trailSafe(dog,route)) {path=route;stand=p;return true;}}
        job=null;return false;
    }
    @Override public boolean canContinueToUse() {return job!=null && dog.society().ready() && ticks<240 && HomeRoutes.trailStanding(dog,stand) && HomeRoutes.trailSafe(dog,path);}
    @Override public void start() {ticks=work=0;dog.getNavigation().moveTo(path,1);}
    @Override public boolean requiresUpdateEveryTick() {return true;}
    @Override public void tick() {
        if(!canContinueToUse())return;ticks++;dog.getLookControl().setLookAt(Vec3.atCenterOf(job.target()));
        var hit=dog.level().clip(new ClipContext(dog.getEyePosition(),Vec3.atCenterOf(job.target()),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dog));
        if(dog.distanceToSqr(Vec3.atCenterOf(job.target()))>16 || hit.getType()!=HitResult.Type.MISS && !hit.getBlockPos().equals(job.target())) {work=0;if(ticks%20==0)dog.getNavigation().moveTo(stand.getX()+.5,stand.getY(),stand.getZ()+.5,1);return;}
        dog.getNavigation().stop();if(++work>=(job.kind()==SocietyWork.Kind.TEACH ? 180 : 40)) {dog.society().perform(job);ticks=240;}
    }
    @Override public void stop() {dog.getNavigation().stop();job=null;path=null;stand=null;}
}
