package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Combat;
import com.bunnidogs.mod.genetics.Wings;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** Inherited wings work for real companions as well as temporary hostile brood. No wing mesh yet. */
final class WingFlightGoal extends Goal {
    private final Bunnidog dog;
    private int bite,orbit=40,diveTicks,warning;
    private boolean diving;
    WingFlightGoal(Bunnidog dog) { this.dog=dog;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    @Override public boolean canUse() {
        if(!Wings.expressed(dog.genome()) || dog.combatState()==3)return false;
        if(dog instanceof ApexMinion)return dog.getTarget()!=null && dog.getTarget().isAlive();
        return dog.combat().fighting() || dog.trail().order()==com.bunnidogs.mod.genetics.Trail.Order.FOLLOW && dog.trail().keeperPlayer()!=null && dog.conversation().viewer()==null;
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { dog.setNoGravity(true);bite=10;orbit=40;diving=false;warning=0; }
    @Override public void stop() { dog.setNoGravity(false);dog.getNavigation().stop(); }
    @Override public void tick() {
        dog.setNoGravity(true);dog.resetFallDistance();dog.getNavigation().stop();
        var enemy=dog instanceof ApexMinion ? dog.getTarget() : dog.combat().target();
        var target=enemy!=null ? enemy : dog.trail().keeperPlayer();if(target==null)return;
        double speed=Math.clamp(dog.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)*1.4,.3,.75);
        if(dog instanceof ApexMinion && enemy!=null) {
            broodDive(enemy,speed);return;
        }
        ApexFlightGoal.fly(dog,target.position().add(0,enemy==null ? 3 : .6,0),speed);
        dog.getLookControl().setLookAt(target,30,30);
        if(bite>0)bite--;
        if(enemy!=null && bite==0 && dog.isWithinMeleeAttackRange(enemy) && dog.hasLineOfSight(enemy)) {
            dog.doHurtTarget((ServerLevel)dog.level(),enemy);bite=Combat.profile(dog.genome(),dog.stats()).interval();
        }
    }
    private void broodDive(net.minecraft.world.entity.LivingEntity target,double speed) {
        dog.getLookControl().setLookAt(target,30,30);
        if(!diving && warning==0 && --orbit<=0 && dog.getY()>=target.getY()+3 && dog.hasLineOfSight(target))warning=8;
        if(warning>0) {
            if(warning==8)((ServerLevel)dog.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,dog.getX(),dog.getY()+dog.getBbHeight(),dog.getZ(),2,.1,.1,.1,0);
            dog.setDeltaMovement(dog.getDeltaMovement().scale(.6));
            if(--warning==0) {diving=true;diveTicks=0;}
            return;
        }
        if(diving) {
            boolean reach=dog.isWithinMeleeAttackRange(target) && dog.hasLineOfSight(target);
            if(reach || ++diveTicks>45 || dog.horizontalCollision) {
                if(reach)dog.doHurtTarget((ServerLevel)dog.level(),target);
                diving=false;orbit=Math.max(30,Combat.profile(dog.genome(),dog.stats()).interval()*2);
            } else {
                var desired=target.position().add(0,.4,0).subtract(dog.position()).normalize().scale(speed*1.6);
                dog.setDeltaMovement(dog.getDeltaMovement().lerp(desired,.7));return;
            }
        }
        double angle=dog.tickCount*.025+dog.getId();
        ApexFlightGoal.fly(dog,target.position().add(Math.cos(angle)*4,5,Math.sin(angle)*4),speed);
    }

}
