package com.bunnidogs.mod.society;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.*;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.saveddata.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** World-owned personal hunt receipts; neither a borrowed Crest nor player death transfers/erases them. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class HuntProgress extends SavedData {
    private final Map<String,Integer> hunts=new HashMap<>();
    public static final Codec<HuntProgress> CODEC=Codec.unboundedMap(Codec.STRING,Codec.intRange(0,63)).xmap(HuntProgress::new,h->h.hunts);
    private static final SavedDataType<HuntProgress> TYPE=new SavedDataType<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","apex_hunts"),HuntProgress::new,CODEC);
    private HuntProgress() {} private HuntProgress(Map<String,Integer> data) {hunts.putAll(data);}
    public static HuntProgress get(ServerLevel level) {return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);}
    public int marks(UUID player) {return hunts.getOrDefault(player.toString(),0);}
    public boolean qualified(UUID player) {return marks(player)==63;}
    public void award(UUID player,ApexKind kind,boolean flying) {int bit=flying ? 32 : 1<<kind.ordinal();hunts.merge(player.toString(),bit,(a,b)->a|b);setDirty();}
    public static Component summary(ServerPlayer p) {int mask=marksFor(p);return Component.translatable("tooltip.bunnidogs.hunt_marks",Integer.bitCount(mask&31),Component.translatable((mask&32)!=0 ? "care.bunnidogs.apex.ready" : "care.bunnidogs.apex.missing"));}
    public static Component missing(ServerPlayer player) {
        int mask=get(player.level()).marks(player.getUUID());var names=new ArrayList<Component>();
        for(var kind:ApexKind.values())if((mask&(1<<kind.ordinal()))==0)names.add(Component.translatable("entity.bunnidogs."+kind.id));
        if((mask&32)==0)names.add(Component.translatable("message.bunnidogs.hunt.flying"));
        return Component.translatable("message.bunnidogs.hunt.missing",net.minecraft.network.chat.ComponentUtils.formatList(names,Component.literal(", ")));
    }
    private static UUID keeper(Entity e) {return e instanceof ServerPlayer p ? p.getUUID() : e instanceof Bunnidog b ? b.trail().keeper() : null;}
    private static boolean eligible(Apex a) {return !(a instanceof CrownedApex) && !com.bunnidogs.mod.crown.CrownEncounter.member(a);}
    public static void credit(Apex a,UUID player,float amount) {
        if(player==null || !Float.isFinite(amount) || amount<=0 || !eligible(a) || a.getPersistentData().getBooleanOr("HuntPaid",false))return;
        var scores=a.getPersistentData().getCompoundOrEmpty("HuntScores");String key=player.toString();if(!scores.contains(key) && scores.size()>=32)return;
        scores.putFloat(key,Math.min(a.getMaxHealth()*2,scores.getFloatOr(key,0)+amount));a.getPersistentData().put("HuntScores",scores);
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Post event) {
        if(!(event.getEntity().level() instanceof ServerLevel))return;
        if(event.getEntity() instanceof Apex apex && eligible(apex)) {credit(apex,keeper(event.getSource().getEntity()),event.getHealthDamage());finish(apex);}
        if(event.getSource().getEntity() instanceof Apex apex && event.getEntity() instanceof ServerPlayer p && event.getBlockedDamage()>0 && p.distanceToSqr(apex)<96*96)credit(apex,p.getUUID(),event.getBlockedDamage()*.5F);
    }
    /** Only accepted healing of another contributor pays support, never healing oneself or an idle bystander. */
    public static void support(ServerPlayer healer,LivingEntity ally,float actual) {
        if(healer==ally || actual<=0 || healer.level()!=ally.level())return;UUID target=keeper(ally);if(target==null)return;
        for(var apex:healer.level().getEntitiesOfClass(Apex.class,healer.getBoundingBox().inflate(64),a->a.isAlive() && eligible(a)))
            if(apex.getPersistentData().getCompoundOrEmpty("HuntScores").getFloatOr(target.toString(),0)>0)credit(apex,healer.getUUID(),actual);
    }
    public static void finish(Apex apex) {
        if(!(apex.level() instanceof ServerLevel level) || !eligible(apex) || !apex.huntDeathConfirmed() || apex.getPersistentData().getBooleanOr("HuntPaid",false))return;
        apex.getPersistentData().putBoolean("HuntPaid",true);var scores=apex.getPersistentData().getCompoundOrEmpty("HuntScores");
        for(var key:scores.keySet()) {
            if(scores.getFloatOr(key,0)<Math.max(6,apex.getMaxHealth()*.02F))continue;
            UUID id;try {id=UUID.fromString(key);}catch(IllegalArgumentException ignored) {continue;}
            // Only this encounter's contributors count. Distance is not an independent entitlement.
            get(level).award(id,apex.kind(),apex.flying());
        }
    }
    public static void refresh(ServerPlayer p) {
        for(int i=0;i<p.getInventory().getContainerSize();i++) {var s=p.getInventory().getItem(i);if(s.is(com.bunnidogs.mod.registry.ModItems.APEX_CREST.get()))s.set(com.bunnidogs.mod.registry.ModItems.HUNT_MARKS.get(),marksFor(p));}
    }
    public static int marksFor(ServerPlayer p) {return get(p.level()).marks(p.getUUID());}
    @SubscribeEvent public static void tick(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof Apex apex && !apex.level().isClientSide())finish(apex);
    }
}
