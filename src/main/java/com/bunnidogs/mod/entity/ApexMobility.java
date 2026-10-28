package com.bunnidogs.mod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Loaded-world swimming and a chosen, colliding jump arc; never teleports through a blocked corridor. */
final class ApexMobility {
    private final Apex apex;
    private Vec3 launch,landing;
    private LivingEntity victim;
    private int elapsed,duration;
    private double height;
    ApexMobility(Apex apex) { this.apex=apex; }
    boolean leaping() { return duration>0; }
    void leap(LivingEntity target) {
        leap(target,target.position());
    }
    void leap(LivingEntity target,Vec3 fixedLanding) {
        leap(target,fixedLanding,1);
    }
    void leap(LivingEntity target,Vec3 fixedLanding,double tempo) {
        launch=apex.position();landing=com.bunnidogs.mod.crown.CrownEncounter.destination(apex,fixedLanding);victim=target;elapsed=0;
        double distance=launch.distanceTo(landing);
        duration=Math.max(16,(int)Math.ceil(Math.clamp((int)Math.ceil(distance*.7),18,40)/tempo));
        height=Math.clamp(3.5+distance*.13+Math.max(0,landing.y-launch.y)*.4,3.5,12);
        apex.getNavigation().stop();apex.setOnGround(false);apex.resetFallDistance();
    }
    void stopLeap() {
        boolean landed=leaping();duration=0;victim=null;apex.setNoGravity(apex.flying());
        if(landed && apex.kind()==ApexKind.BRUTE && !apex.flying()) {
            apex.charging(false);
            var target=apex.getTarget();
            if(target!=null && target.isAlive())apex.getNavigation().moveTo(target,1);
        }
    }
    void tick(ServerLevel level) {
        if(leaping()) {
            apex.setNoGravity(true);apex.resetFallDistance();
            double t=Math.min(1,(double)++elapsed/duration);
            Vec3 point=launch.lerp(landing,t).add(0,4*height*t*(1-t),0);
            Vec3 step=point.subtract(apex.position());
            if(step.lengthSqr()>16 || !ApexTerrain.clear(level,apex,apex.position(),point,apex.getBbWidth()/2+.15,apex.getBbHeight())) {
                stopLeap();apex.setDeltaMovement(0,0,0);return;
            }
            apex.setDeltaMovement(step);apex.hurtMarked=true;
            if(apex.kind()==ApexKind.BRUTE && victim!=null && elapsed>duration/2 && apex.isWithinMeleeAttackRange(victim) && apex.hasLineOfSight(victim)) {
                apex.doHurtTarget(level,victim);victim=null;
            }
            if(elapsed>=duration)stopLeap();
            return;
        }
        swim(apex,apex.getTarget(),apex.flying(),apex.casting(),apex.kind()!=ApexKind.BRUTE && apex.kind()!=ApexKind.BLAST);
    }
    static void swim(Bunnidog mob,LivingEntity target,boolean aerial,boolean casting,boolean keepDistance) {
        if(!mob.isInWater())return;
        mob.resetFallDistance();
        Vec3 toward=target!=null && target.isAlive() ? target.getEyePosition().subtract(mob.getEyePosition()) : new Vec3(0,1,0);
        if(!mob.level().hasChunkAt(BlockPos.containing(mob.position().add(toward.normalize().scale(2)))))return;
        double vertical=aerial ? .45 : Math.clamp(toward.y*.15+.12,-.25,.35);
        double horizontal=casting ? .05 : .48;
        Vec3 flat=toward.multiply(1,0,1).normalize();
        if(!aerial && target!=null && keepDistance && mob.distanceToSqr(target)<64)flat=flat.scale(-1);
        mob.setDeltaMovement(mob.getDeltaMovement().lerp(flat.scale(horizontal).add(0,vertical,0),.55));
    }
}
