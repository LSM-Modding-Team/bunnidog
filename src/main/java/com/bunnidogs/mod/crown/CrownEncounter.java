package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Explicit dungeon origins never turn temporary packs into ordinary ritual or wild rewards. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class CrownEncounter {
    public static boolean member(Entity entity) {return (entity instanceof Apex || entity instanceof ApexMinion) && entity.getPersistentData().contains("CrownHome");}
    public static void bind(Entity entity,CrownSealBlockEntity seal) {
        var data=entity.getPersistentData();data.putLong("CrownHome",seal.getBlockPos().asLong());data.putString("CrownRun",seal.run().toString());data.putInt("CrownWing",seal.wing());
        if(entity instanceof Mob mob)mob.setPersistenceRequired();
    }
    public static CrownSealBlockEntity controller(Entity entity) {
        if(!(entity.level() instanceof ServerLevel level) || !member(entity))return null;
        var home=BlockPos.of(entity.getPersistentData().getLongOr("CrownHome",0));
        if(!level.hasChunkAt(home) || !(level.getBlockEntity(home) instanceof CrownSealBlockEntity seal) || seal.run()==null || !seal.run().toString().equals(entity.getPersistentData().getStringOr("CrownRun","")))return null;
        return seal;
    }
    public static UUID keeper(Entity entity) {
        if(entity instanceof ServerPlayer p)return p.getUUID();
        return entity instanceof Bunnidog dog ? dog.trail().keeper() : null;
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Post event) {
        var seal=controller(event.getEntity());var owner=keeper(event.getSource().getEntity());
        if(seal!=null && owner!=null && event.getHealthDamage()>0)seal.credit(owner,event.getHealthDamage());
        if(seal!=null)seal.resolveVictory();
        var attacking=event.getSource().getEntity();var encounter=attacking==null ? null : controller(attacking);
        if(encounter!=null && event.getEntity() instanceof ServerPlayer player && event.getBlockedDamage()>0)encounter.credit(player.getUUID(),event.getBlockedDamage()*.5F);
    }
    public static boolean allowedTarget(Entity actor,Entity target) {
        if(!member(actor))return true;var seal=controller(actor);return seal!=null && seal.active() && target!=null && seal.movementBounds().contains(target.position());
    }
    public static net.minecraft.world.phys.Vec3 destination(Bunnidog mob,net.minecraft.world.phys.Vec3 point) {
        if(!member(mob))return point;var seal=controller(mob);if(seal==null)return mob.position();var b=seal.movementBounds();double inset=mob.getBbWidth()/2+.1;
        return new net.minecraft.world.phys.Vec3(Math.clamp(point.x,b.minX+inset,b.maxX-inset),Math.clamp(point.y,b.minY+.01,b.maxY-mob.getBbHeight()-.01),Math.clamp(point.z,b.minZ+inset,b.maxZ-inset));
    }
    private static double axis(double p,double delta,double low,double high) {
        if(p<low)return Math.max(0,Math.min(delta,high-p));
        if(p>high)return Math.min(0,Math.max(delta,low-p));
        return Math.clamp(p+delta,low,high)-p;
    }
    public static net.minecraft.world.phys.Vec3 limit(Bunnidog mob,net.minecraft.world.phys.Vec3 motion) {
        var seal=controller(mob);if(seal==null)return net.minecraft.world.phys.Vec3.ZERO;
        var b=seal.movementBounds();double inset=mob.getBbWidth()/2+.06;
        return new net.minecraft.world.phys.Vec3(axis(mob.getX(),motion.x,b.minX+inset,b.maxX-inset),axis(mob.getY(),motion.y,b.minY+.01,b.maxY-mob.getBbHeight()-.01),axis(mob.getZ(),motion.z,b.minZ+inset,b.maxZ-inset));
    }
    @SubscribeEvent public static void tick(EntityTickEvent.Pre event) {
        var entity=event.getEntity();if(!(entity.level() instanceof ServerLevel level) || !member(entity) || !entity.isAlive())return;
        var seal=controller(entity);
        if(seal==null) {if(entity.tickCount%20==0 && !CrownTickets.restoreController(entity,level,BlockPos.of(entity.getPersistentData().getLongOr("CrownHome",0))))entity.discard();return;}
        if(!seal.active()) {entity.discard();return;}
        if(entity instanceof Mob mob) {
            if(mob.getTarget()!=null && !allowedTarget(mob,mob.getTarget())) {mob.setTarget(null);mob.getNavigation().stop();}
            var b=seal.movementBounds();
            if(!b.contains(mob.position())) {
                double inset=mob.getBbWidth()/2+.2;double x=Math.clamp(mob.getX(),b.minX+inset,b.maxX-inset),y=Math.clamp(mob.getY(),b.minY+.1,b.maxY-mob.getBbHeight()-.1),z=Math.clamp(mob.getZ(),b.minZ+inset,b.maxZ-inset);
                mob.setTarget(null);mob.getNavigation().moveTo(x,y,z,1.1);
                if(mob.isNoGravity())mob.setDeltaMovement(new net.minecraft.world.phys.Vec3(x,y,z).subtract(mob.position()).normalize().scale(.35));
            }
        }
    }
    public static void death(Apex apex) {var seal=controller(apex);if(seal!=null)seal.defeated(apex);}
    private CrownEncounter() {}
}
