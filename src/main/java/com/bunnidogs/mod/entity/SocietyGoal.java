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
        for(var offer:dog.society().offers()) {
            job=offer;
            if(job.kind()==SocietyWork.Kind.MEDICINE) {var patient=dog.society().medicalPatient(job.patient());if(patient==null || !reachable(patient.blockPosition())) {dog.society().hearth().jobs().unreachable(dog,job);continue;}}
            if(job.destination()!=null && !reachable(job.destination())) {dog.society().hearth().jobs().unreachable(dog,job);continue;}
            for(var dir:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,-1,1,-2,-3}) {var p=job.target().relative(dir).offset(0,dy,0);if(!HomeRoutes.trailStanding(dog,p))continue;var route=dog.getNavigation().createPath(p,0);if(route!=null && route.canReach() && HomeRoutes.trailSafe(dog,route)) {path=route;stand=p;return true;}}
            dog.society().hearth().jobs().unreachable(dog,job);
        }
        job=null;return false;
    }
    private boolean reachable(BlockPos target) {for(var dir:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,-1,1,-2,-3}) {var at=target.relative(dir).offset(0,dy,0);if(!HomeRoutes.trailStanding(dog,at))continue;var path=dog.getNavigation().createPath(at,0);if(path!=null && path.canReach() && HomeRoutes.trailSafe(dog,path))return true;}return false;}
    @Override public boolean canContinueToUse() {return job!=null && dog.society().owns(job) && dog.society().ready() && ticks<240 && HomeRoutes.trailStanding(dog,stand) && HomeRoutes.trailSafe(dog,path);}
    @Override public void start() {ticks=work=0;if(!dog.society().claim(job)) {job=null;return;}dog.courierLore().desk(job.kind()==SocietyWork.Kind.RECORD_ROUTE || job.kind()==SocietyWork.Kind.STUDY_ROUTE);dog.getNavigation().moveTo(path,1);}
    @Override public boolean requiresUpdateEveryTick() {return true;}
    @Override public void tick() {
        if(!canContinueToUse())return;ticks++;
        if(job.kind()==SocietyWork.Kind.GATHER_FIELD) {
            var home=dog.society().hearth();var level=(net.minecraft.server.level.ServerLevel)dog.level();
            if(!(level.getEntity(job.index()) instanceof net.minecraft.world.entity.item.ItemEntity item) || !com.bunnidogs.mod.society.CivicFields.available(home,item)) {ticks=240;return;}
            dog.getLookControl().setLookAt(item,30,30);
            if(dog.distanceToSqr(item)>9 || !com.bunnidogs.mod.society.CivicFields.visible(dog,item)) {work=0;if(ticks%20==0) {var route=dog.getNavigation().createPath(item,1);if(route!=null && HomeRoutes.trailSafe(dog,route))dog.getNavigation().moveTo(route,1);}return;}
            dog.getNavigation().stop();if(++work>=40) {dog.society().perform(new SocietyWork.Job(job.kind(),item.blockPosition(),item.getId(),level.getBlockState(item.blockPosition())));ticks=240;}return;
        }
        dog.getLookControl().setLookAt(Vec3.atCenterOf(job.target()));
        var hit=dog.level().clip(new ClipContext(dog.getEyePosition(),Vec3.atCenterOf(job.target()),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dog));
        if(dog.distanceToSqr(Vec3.atCenterOf(job.target()))>(job.kind()==SocietyWork.Kind.CARE ? 6.25 : 16) || hit.getType()!=HitResult.Type.MISS && !hit.getBlockPos().equals(job.target())) {work=0;if(ticks%20==0)dog.getNavigation().moveTo(stand.getX()+.5,stand.getY(),stand.getZ()+.5,1);return;}
        dog.getNavigation().stop();if(++work>=(job.kind()==SocietyWork.Kind.TEACH ? 180 : 40)) {dog.society().perform(job);ticks=240;}
    }
    @Override public void stop() {dog.society().release();dog.courierLore().desk(false);dog.getNavigation().stop();job=null;path=null;stand=null;}
}
