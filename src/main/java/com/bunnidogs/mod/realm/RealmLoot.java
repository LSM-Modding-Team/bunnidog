package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.entity.Apex;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.item.ApexCrestItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Wild rewards are useful stackable expedition income; ritual trophies and temporary swarms stay distinct. */
public final class RealmLoot {
    public static void drops(LivingDropsEvent event,Apex apex,ServerLevel level) {
        if(!level.getGameRules().get(GameRules.MOB_DROPS) || !event.isRecentlyHit())return;
        parts(event,apex,level);
        if(apex.getRandom().nextInt(16)==0)add(event,apex,level,ApexCrestItem.create(new Tissue(apex.genome(),apex.stats())));
    }
    public static void parts(LivingDropsEvent event,Apex apex,ServerLevel level) {
        if(!level.getGameRules().get(GameRules.MOB_DROPS) || !event.isRecentlyHit())return;
        var reagent=switch(apex.kind()) {case BRUTE->ModItems.APEX_FANG;case PRISM->ModItems.PRISM_LENS;case BROOD->ModItems.CHORUS_HEART;case BLAST->ModItems.VOLATILE_HEART;case SUPPORT->ModItems.HEART_SPOOL;};
        var remnant=new ItemStack(reagent.get(),4+apex.getRandom().nextInt(5));if(!apex.flying())remnant.set(ModItems.GROUND_REMNANT.get(),true);
        add(event,apex,level,remnant);
        add(event,apex,level,new ItemStack(ModItems.APEX_MARROW.get(),2+apex.getRandom().nextInt(3)));
        if(apex.flying())add(event,apex,level,new ItemStack(ModItems.AERIAL_MEMBRANE.get(),2+apex.getRandom().nextInt(3)));
    }
    private static void add(LivingDropsEvent event,Apex apex,ServerLevel level,ItemStack stack) {event.getDrops().add(new ItemEntity(level,apex.getX(),apex.getY(),apex.getZ(),stack));}
    private RealmLoot() {}
}
