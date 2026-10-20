package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Trail;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

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
    @Override public void tick() {
        if (!canContinueToUse()) return;
        var keeper=dog.trail().keeperPlayer();
        if (dog.trail().order()==Trail.Order.WAIT || keeper==null) { hold(); return; }
        dog.getLookControl().setLookAt(keeper,30,30);
        if (dog.distanceToSqr(keeper)<=6.25) { hold(); return; }
        var active=dog.getNavigation().getPath();
        if (active!=null && !HomeRoutes.trailSafe(dog,active)) dog.getNavigation().stop();
        if (--repath>0) return;
        repath=Trail.REPATH_TICKS;
        var around=new ArrayList<BlockPos>(); var centre=keeper.blockPosition();
        for (int[] offset:new int[][]{{-2,0},{2,0},{0,-2},{0,2},{-1,-1},{-1,1},{1,-1},{1,1}})
            for (int y:new int[]{0,1,-1}) {
                var pos=centre.offset(offset[0],y,offset[1]);
                if (HomeRoutes.trailStanding(dog,pos)) around.add(pos);
            }
        around.sort(Comparator.comparingDouble(pos->pos.distSqr(dog.blockPosition())));
        int attempts=0;
        for (var pos:around) {
            if (++attempts>4) break;
            Path path=dog.getNavigation().createPath(pos,0);
            if (!HomeRoutes.trailSafe(dog,path) || path.getEndNode()==null
                    || !HomeRoutes.trailStanding(dog,path.getEndNode().asBlockPos())
                    || path.getEndNode().asBlockPos().distSqr(centre)+1>=dog.blockPosition().distSqr(centre)) continue;
            dog.getNavigation().moveTo(path,1.1); return;
        }
        dog.getNavigation().stop();
    }
}
