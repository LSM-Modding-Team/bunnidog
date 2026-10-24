package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Combat;
import com.bunnidogs.mod.genetics.Wings;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** Inherited wings work for real companions as well as temporary hostile brood. No wing mesh yet. */
final class WingFlightGoal extends Goal {
    private final Bunnidog dog;
    private int bite;
    WingFlightGoal(Bunnidog dog) { this.dog=dog;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    @Override public boolean canUse() {
        if(!Wings.expressed(dog.genome()) || dog.combatState()==3)return false;
        if(dog instanceof ApexMinion)return dog.getTarget()!=null && dog.getTarget().isAlive();
        return dog.combat().fighting() || dog.trail().order()==com.bunnidogs.mod.genetics.Trail.Order.FOLLOW && dog.trail().keeperPlayer()!=null && dog.conversation().viewer()==null;
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { dog.setNoGravity(true);bite=10; }
    @Override public void stop() { dog.setNoGravity(false);dog.getNavigation().stop(); }
    @Override public void tick() {
        dog.setNoGravity(true);dog.resetFallDistance();dog.getNavigation().stop();
        var enemy=dog instanceof ApexMinion ? dog.getTarget() : dog.combat().target();
        var target=enemy!=null ? enemy : dog.trail().keeperPlayer();if(target==null)return;
        double speed=Math.clamp(dog.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)*1.4,.3,.75);
        ApexFlightGoal.fly(dog,target.position().add(0,enemy==null ? 3 : .6,0),speed);
        dog.getLookControl().setLookAt(target,30,30);
        if(bite>0)bite--;
        if(enemy!=null && bite==0 && dog.isWithinMeleeAttackRange(enemy) && dog.hasLineOfSight(enemy)) {
            dog.doHurtTarget((ServerLevel)dog.level(),enemy);bite=Combat.profile(dog.genome(),dog.stats()).interval();
        }
    }
}
