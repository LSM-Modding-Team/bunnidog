package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** Walk beside the actual teacher; never teleport a pupil or copy a route from an absent teacher. */
final class CourierLessonGoal extends Goal {
    private final Bunnidog dog;private long search;private int blocked;
    CourierLessonGoal(Bunnidog dog) {this.dog=dog;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    @Override public boolean canUse() {return dog.courierLore().apprenticing() && dog.courierLore().instructor()!=null;}
    @Override public boolean canContinueToUse() {return canUse();}
    @Override public boolean requiresUpdateEveryTick() {return true;}
    @Override public void start() {blocked=0;}
    @Override public void tick() {
        var teacher=dog.courierLore().instructor();if(teacher==null)return;dog.getLookControl().setLookAt(teacher,30,30);
        if(dog.distanceToSqr(teacher)<=6.25){dog.getNavigation().stop();return;}
        if(dog.level().getGameTime()<search)return;search=dog.level().getGameTime()+10;
        var route=dog.getNavigation().createPath(teacher,1);if(route!=null && route.canReach() && HomeRoutes.trailSafe(dog,route)) {blocked=0;dog.getNavigation().moveTo(route,1.1);}else if(++blocked>=3)dog.courierLore().cancel();
    }
    @Override public void stop() {dog.getNavigation().stop();}
}
