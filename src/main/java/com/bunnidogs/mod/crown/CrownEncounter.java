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
    @SubscribeEvent public static void tick(EntityTickEvent.Pre event) {
        var entity=event.getEntity();if(!(entity.level() instanceof ServerLevel level) || !member(entity) || entity.tickCount%20!=0 || !entity.isAlive())return;
        var seal=controller(entity);
        if(seal==null && CrownTickets.restoreController(entity,level,BlockPos.of(entity.getPersistentData().getLongOr("CrownHome",0))))return;
        if(seal==null || !seal.active() || !seal.contains(entity.position(),8))entity.discard();
    }
    public static void death(Apex apex) {var seal=controller(apex);if(seal!=null)seal.defeated(apex);}
    private CrownEncounter() {}
}
