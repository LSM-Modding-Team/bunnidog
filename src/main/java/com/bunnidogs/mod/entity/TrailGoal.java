package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Trail;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Holds the ordered MOVE slot, walking real safe paths near the keeper without teleporting or loading terrain. */
final class TrailGoal extends Goal {
    private final Bunnidog dog;
    private int repath;
    TrailGoal(Bunnidog dog) { this.dog=dog; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    @Override public boolean canUse() { return dog.trail().canWalk(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { repath=0; dog.getNavigation().stop(); }
    @Override public void stop() { dog.getNavigation().stop(); }
    private void hold() {
        dog.getNavigation().stop(); dog.setXxa(0); dog.setZza(0); dog.setSpeed(0);
    }
    private BlockPos followPlace(net.minecraft.server.level.ServerPlayer keeper) {
        int slot=dog.combat().partySlot();
        if (slot<0) return keeper.blockPosition();
        var forward=keeper.getLookAngle().multiply(1,0,1);
        forward=forward.lengthSqr()<.01 ? new Vec3(0,0,1) : forward.normalize();
        var side=new Vec3(-forward.z,0,forward.x);
        return BlockPos.containing(keeper.position().subtract(forward.scale(slot==2 ? 3 : 1.8))
                .add(side.scale(slot==0 ? -2 : slot==1 ? 2 : 0)));
    }
    private boolean crowded(BlockPos pos) {
        var box=dog.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(dog.position())).inflate(.15,0,.15);
        return !dog.level().getEntitiesOfClass(Bunnidog.class,box,other->other!=dog && other.isAlive()
                && dog.trail().keeper().equals(other.trail().keeper())).isEmpty();
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        var keeper=dog.trail().keeperPlayer();
        if (keeper==null) { hold(); return; }
        if (dog.trail().order()==Trail.Order.WAIT) {
            var anchor=dog.combat().hold();
            if (anchor==null || dog.distanceToSqr(Vec3.atBottomCenterOf(anchor))<=.64) { hold(); return; }
            if (dog.blockPosition().distSqr(anchor)>64) { dog.combat().recall(keeper); hold(); return; }
            if (--repath>0) return;
            repath=Trail.REPATH_TICKS;
            if (!HomeRoutes.trailStanding(dog,anchor)) { hold(); return; }
            var path=dog.getNavigation().createPath(anchor,0);
            if (dog.combat().rally(path) && path.canReach()) dog.getNavigation().moveTo(path,1.1);
            else hold();
            return;
        }
        dog.getLookControl().setLookAt(keeper,30,30);
        var place=followPlace(keeper);
        boolean close=dog.combat().selected() ? dog.blockPosition().distSqr(place)<=2.25 && !crowded(dog.blockPosition()) : dog.distanceToSqr(keeper)<=6.25;
        if (close && dog.combat().rally(dog.blockPosition())) { hold(); return; }
        var active=dog.getNavigation().getPath();
        if (active!=null && !dog.combat().rally(active)) dog.getNavigation().stop();
        if (--repath>0) return;
        repath=Trail.REPATH_TICKS;
        var around=new ArrayList<BlockPos>(); var centre=keeper.blockPosition();
        for (int[] offset:new int[][]{{-2,0},{2,0},{0,-2},{0,2},{-1,-1},{-1,1},{1,-1},{1,1}})
            for (int y:new int[]{0,1,-1}) {
                var pos=centre.offset(offset[0],y,offset[1]);
                if (HomeRoutes.trailStanding(dog,pos) && dog.combat().rally(pos)) around.add(pos);
            }
        if (dog.combat().selected()) {
            for (int y:new int[]{0,1,-1}) if (HomeRoutes.trailStanding(dog,place.above(y)) && dog.combat().rally(place.above(y))) around.add(place.above(y));
            around.removeIf(this::crowded);
        }
        around.sort(Comparator.comparingDouble(pos->pos.distSqr(place)+.1*pos.distSqr(dog.blockPosition())));
        int attempts=0;
        for (var pos:around) {
            if (++attempts>4) break;
            Path path=dog.getNavigation().createPath(pos,0);
            if (!dog.combat().rally(path) || path.getEndNode()==null
                    || !HomeRoutes.trailStanding(dog,path.getEndNode().asBlockPos())
                    || dog.distanceToSqr(keeper)>(dog.combat().selected() ? 25 : 6.25)
                    && path.getEndNode().asBlockPos().distSqr(centre)+1>=dog.blockPosition().distSqr(centre)) continue;
            dog.getNavigation().moveTo(path,1.1); return;
        }
        dog.getNavigation().stop();
    }
}
