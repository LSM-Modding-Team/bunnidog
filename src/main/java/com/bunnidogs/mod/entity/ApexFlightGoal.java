package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Continuous aerial orbit. Only the Brute dives, once per ascent/strike/recovery cycle. */
final class ApexFlightGoal extends Goal {
    private final Apex apex;
    private int orbit=80,diveTicks,warning;
    private boolean diving;
    private Vec3 idleOrigin;
    ApexFlightGoal(Apex apex) { this.apex=apex;setFlags(EnumSet.of(Flag.MOVE)); }
    @Override public boolean canUse() { return apex.flightActive(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void tick() {
        apex.getNavigation().stop();apex.setNoGravity(true);apex.resetFallDistance();
        var target=apex.getTarget();
        if(target==null || !target.isAlive()) {
            if(apex.wild()) {
                if(idleOrigin==null)idleOrigin=apex.position();
                double angle=apex.tickCount*.012;
                Vec3 point=idleOrigin.add(Math.cos(angle)*6,6,Math.sin(angle)*6);
                var at=BlockPos.containing(point);
                if(apex.level().hasChunkAt(at)) {
                    int floor=apex.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at.getX(),at.getZ());
                    point=new Vec3(point.x,Math.min(apex.level().getMaxY()-4,Math.max(point.y,floor+6)),point.z);fly(apex,point,.35);
                } else apex.setDeltaMovement(apex.getDeltaMovement().scale(.8));
            } else apex.setDeltaMovement(apex.getDeltaMovement().scale(.8).add(0,apex.onGround() ? .12 : 0,0));
            return;
        }
        idleOrigin=null;
        boolean brute=apex.kind()==ApexKind.BRUTE;
        if(brute && !diving && --orbit<=0 && apex.getY()>=target.getY()+5 && apex.hasLineOfSight(target)) { warning=8;orbit=80; }
        if(warning>0) {
            if(warning==8)((ServerLevel)apex.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,apex.getX(),apex.getY()+apex.getBbHeight(),apex.getZ(),4,.2,.15,.2,0);
            apex.setDeltaMovement(apex.getDeltaMovement().scale(.6));
            if(--warning==0) {diving=true;diveTicks=0;}
            else return;
        }
        double radius=apex.kind()==ApexKind.PRISM ? 16 : apex.kind()==ApexKind.BLAST && apex.retreatTicks()==0 && (apex.abilityWait()==0 || apex.casting()) ? 2.5 : 9;
        double angle=apex.tickCount*.012;
        Vec3 point=diving ? target.position().add(0,.5,0) : target.position().add(Math.cos(angle)*radius,apex.kind()==ApexKind.BLAST ? 6 : 8,Math.sin(angle)*radius);
        if(brute && !diving)point=target.position().add(Math.cos(angle)*7,9,Math.sin(angle)*7);
        if(diving && (++diveTicks>55 || apex.isWithinMeleeAttackRange(target) && apex.hasLineOfSight(target))) {
            if(apex.isWithinMeleeAttackRange(target) && apex.hasLineOfSight(target))apex.doHurtTarget((ServerLevel)apex.level(),target);
            diving=false;orbit=80;point=target.position().add(0,9,0);
        }
        if(diving) {
            Vec3 step=point.subtract(apex.position()).normalize().scale(1.35);
            Vec3 velocity=apex.getDeltaMovement().lerp(step,.7);
            // Previous contact is not a reason to abandon a destructive dive: clear each swept layer.
            if(ApexTerrain.clear((ServerLevel)apex.level(),apex,apex.position(),apex.position().add(velocity),apex.getBbWidth()/2+.2,apex.getBbHeight()+.1))
                apex.setDeltaMovement(velocity);
            else {diving=false;orbit=80;apex.setDeltaMovement(0,.2,0);}
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
