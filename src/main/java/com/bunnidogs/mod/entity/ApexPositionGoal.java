package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Ground specialists own spacing instead of inheriting the Brute's bite/chase. */
final class ApexPositionGoal extends Goal {
    private final Apex apex;
    private int repath;
    ApexPositionGoal(Apex apex) { this.apex=apex;setFlags(EnumSet.of(Flag.MOVE)); }
    @Override public boolean canUse() { return !apex.flying() && apex.kind()!=ApexKind.BRUTE && apex.getTarget()!=null && apex.getTarget().isAlive(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { repath=0; }
    @Override public void stop() { apex.getNavigation().stop(); }
    @Override public void tick() {
        var target=apex.getTarget();if(target==null)return;
        if(apex.casting()) { apex.getNavigation().stop();return; }
        if(--repath>0)return;repath=10;
        boolean rush=apex.kind()==ApexKind.BLAST && apex.retreatTicks()==0 && apex.abilityWait()==0;
        double near=rush ? 3 : apex.kind()==ApexKind.PRISM ? 16 : 10;
        double far=rush ? 4 : apex.kind()==ApexKind.PRISM ? 24 : 16;
        double distance=apex.distanceTo(target);
        if(distance<near) {
            Vec3 away=apex.position().subtract(target.position()).multiply(1,0,1).normalize();
            if(away.lengthSqr()<.01)away=new Vec3(1,0,0);
            Vec3 at=apex.position().add(away.scale(6));
            if(apex.level().hasChunkAt(BlockPos.containing(at)))apex.getNavigation().moveTo(at.x,at.y,at.z,1.2);
        } else if(distance>far || !apex.hasLineOfSight(target) && apex.kind()!=ApexKind.PRISM)apex.getNavigation().moveTo(target,rush ? 1.3 : 1);
        else apex.getNavigation().stop();
    }
}
