package com.bunnidogs.mod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/** Swept hook/pull corridor. Bounded, loaded-only and subject to native block protection. */
final class ApexTerrain {
    private ApexTerrain() {}
    static boolean clear(ServerLevel level,net.minecraft.world.entity.LivingEntity actor,Vec3 from,Vec3 to,double radius,double height) {
        if(from.distanceToSqr(to)>1024)return false;
        var min=BlockPos.containing(Math.min(from.x,to.x)-radius,Math.min(from.y,to.y),Math.min(from.z,to.z)-radius);
        var max=BlockPos.containing(Math.max(from.x,to.x)+radius,Math.max(from.y,to.y)+height,Math.max(from.z,to.z)+radius);
        if(!level.hasChunksAt(min,max))return false;
        boolean grief=EventHooks.canEntityGrief(level,actor), blocked=false;
        int steps=Math.max(1,(int)Math.ceil(from.distanceTo(to)*2));
        var seen=new java.util.HashSet<BlockPos>();
        for(int i=0;i<=steps;i++) {
            var point=from.lerp(to,(double)i/steps);
            for(var pos:BlockPos.betweenClosed(BlockPos.containing(point.x-radius,point.y,point.z-radius),BlockPos.containing(point.x+radius,point.y+height,point.z+radius))) {
                if(!seen.add(pos.immutable()))continue;
                var state=level.getBlockState(pos);
                if(state.getCollisionShape(level,pos).isEmpty())continue;
                if(!grief || state.getDestroySpeed(level,pos)<0 || !state.canEntityDestroy(level,pos,actor)
                    || !EventHooks.onEntityDestroyBlock(actor,pos,state) || !level.destroyBlock(pos,true,actor))blocked=true;
            }
        }
        return !blocked;
    }
}
