package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** Follow the real local expert through safe loaded paths; no movement or parcel is simulated. */
final class WorkshopLessonGoal extends Goal {
    private final Bunnidog dog;private long search;private int blocked;
    WorkshopLessonGoal(Bunnidog dog) {this.dog=dog;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    @Override public boolean canUse() {return dog.workshopLessons().instructor()!=null;}
    @Override public boolean canContinueToUse() {return canUse();}
    @Override public void start() {blocked=0;search=0;}
    @Override public boolean requiresUpdateEveryTick() {return true;}
    @Override public void tick() {
        var mentor=dog.workshopLessons().instructor();if(mentor==null)return;dog.getLookControl().setLookAt(mentor,30,30);
        if(dog.distanceToSqr(mentor)<=6.25) {dog.getNavigation().stop();dog.workshopLessons().activity(BunnidogLearning.Activity.WATCHING);return;}
        dog.workshopLessons().activity(BunnidogLearning.Activity.WALKING);
        if(dog.level().getGameTime()<search)return;search=dog.level().getGameTime()+10;
        var path=dog.getNavigation().createPath(mentor,1);
        if(path!=null && path.canReach() && HomeRoutes.trailSafe(dog,path)) {blocked=0;dog.getNavigation().moveTo(path,1.1);}else if(++blocked>=3)dog.workshopLessons().cancel();
    }
    @Override public void stop() {dog.getNavigation().stop();}
}
