package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Continuous aerial orbit. Only the Brute dives, once per ascent/strike/recovery cycle. */
final class ApexFlightGoal extends Goal {
    private final Apex apex;
    private int orbit=80,diveTicks;
    private boolean diving;
    ApexFlightGoal(Apex apex) { this.apex=apex;setFlags(EnumSet.of(Flag.MOVE)); }
    @Override public boolean canUse() { return apex.flightActive(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void tick() {
        apex.getNavigation().stop();apex.setNoGravity(true);apex.resetFallDistance();
        var target=apex.getTarget();
        if(target==null || !target.isAlive()) { apex.setDeltaMovement(apex.getDeltaMovement().scale(.8).add(0,apex.onGround() ? .12 : 0,0));return; }
        boolean brute=apex.kind()==ApexKind.BRUTE;
        if(brute && !diving && --orbit<=0 && apex.getY()>=target.getY()+5 && apex.hasLineOfSight(target)) { diving=true;diveTicks=0; }
        double radius=apex.kind()==ApexKind.PRISM ? 18 : apex.kind()==ApexKind.BLAST && apex.retreatTicks()==0 && (apex.abilityWait()==0 || apex.casting()) ? 3 : 11;
        double angle=apex.tickCount*.012;
        Vec3 point=diving ? target.position().add(0,.5,0) : target.position().add(Math.cos(angle)*radius,apex.kind()==ApexKind.BLAST ? 7 : 9,Math.sin(angle)*radius);
        if(brute && !diving)point=target.position().add(Math.cos(angle)*8,10,Math.sin(angle)*8);
        if(diving && (++diveTicks>55 || apex.horizontalCollision || apex.onGround() || apex.isWithinMeleeAttackRange(target))) {
            if(apex.isWithinMeleeAttackRange(target) && apex.hasLineOfSight(target))apex.doHurtTarget((ServerLevel)apex.level(),target);
            diving=false;orbit=80;point=target.position().add(0,10,0);
        }
        if(diving) {
            Vec3 step=point.subtract(apex.position()).normalize().scale(2.2);
            if(ApexTerrain.clear((ServerLevel)apex.level(),apex,apex.position(),apex.position().add(step),apex.getBbWidth()/2+.15,apex.getBbHeight()))
                apex.setDeltaMovement(apex.getDeltaMovement().lerp(step,.7));
        } else fly(apex,point,.48);
        if(!apex.casting())apex.getLookControl().setLookAt(target,20,20);
    }
    static void fly(Bunnidog mob,Vec3 point,double speed) {
        Vec3 towards=point.subtract(mob.position());
        if(!mob.level().hasChunksAt(BlockPos.containing(mob.position()).offset(-2,-2,-2),BlockPos.containing(point).offset(2,2,2))) { mob.setDeltaMovement(mob.getDeltaMovement().scale(.5));return; }
        Vec3 desired=towards.normalize().scale(Math.min(speed,towards.length()*.08));
        if(!mob.level().noCollision(mob,mob.getBoundingBox().move(desired.scale(2)))) {
            // Rise around an obstruction, never drop deliberately or force an unloaded chunk.
            desired=new Vec3(-desired.z*.5,.3,desired.x*.5);
            if(!mob.level().noCollision(mob,mob.getBoundingBox().move(desired.scale(2))))desired=Vec3.ZERO;
        }
        mob.setDeltaMovement(mob.getDeltaMovement().lerp(desired,.25));
    }
    @Override public void stop() { apex.getNavigation().stop(); }
}
