package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Bounded low flight with regular landings; collision is still performed by native entity movement. */
final class ApexFlightGoal extends Goal {
    private final Apex apex;
    private int bite;
    ApexFlightGoal(Apex apex) { this.apex=apex; setFlags(EnumSet.of(Flag.MOVE)); }
    @Override public boolean canUse() { return apex.flightActive(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void tick() {
        var target=apex.getTarget(); if (target==null) return;
        apex.getNavigation().stop();
        double altitude=apex.kind()==ApexKind.BRUTE ? 3 : 5;
        Vec3 point=target.position().add(0,altitude,0);
        if (!apex.level().hasChunkAt(BlockPos.containing(point))) { apex.landFor(60); return; }
        int surface=apex.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)Math.floor(point.x),(int)Math.floor(point.z));
        point=new Vec3(point.x,Math.min(point.y,surface+10),point.z);
        Vec3 towards=point.subtract(apex.position());
        var ahead=apex.getBoundingBox().move(towards.normalize().scale(1.5));
        Vec3 desired=towards.normalize().scale(Math.min(.32,towards.length()*.06));
        if (!apex.level().hasChunkAt(BlockPos.containing(point)) || !apex.level().noCollision(apex,ahead)) {
            apex.landFor(60); return;
        }
        apex.setDeltaMovement(apex.getDeltaMovement().lerp(desired,.2));
        apex.getLookControl().setLookAt(target,15,15);
        if (bite>0) bite--;
        if (bite==0 && apex.distanceToSqr(target)<9 && apex.hasLineOfSight(target)) {
            apex.doHurtTarget((ServerLevel)apex.level(),target); bite=20;
        }
    }
    @Override public void stop() { apex.getNavigation().stop(); }
}
