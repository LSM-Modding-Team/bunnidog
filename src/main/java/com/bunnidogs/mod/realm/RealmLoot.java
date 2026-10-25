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
        var reagent=switch(apex.kind()) {case BRUTE->ModItems.APEX_MARROW;case PRISM->ModItems.PRISM_LENS;case BROOD->ModItems.CHORUS_HEART;case BLAST->ModItems.VOLATILE_HEART;case SUPPORT->ModItems.HEART_SPOOL;};
        add(event,apex,level,new ItemStack(reagent.get(),2+apex.getRandom().nextInt(3)));
        add(event,apex,level,new ItemStack(Items.EMERALD,4+apex.getRandom().nextInt(9)));
        add(event,apex,level,new ItemStack(ModItems.STARLIT_DUST.get(),2+apex.getRandom().nextInt(4)));
        if(apex.flying())add(event,apex,level,new ItemStack(ModItems.AERIAL_MEMBRANE.get(),1+apex.getRandom().nextInt(2)));
        if(apex.getRandom().nextInt(16)==0)add(event,apex,level,ApexCrestItem.create(new Tissue(apex.genome(),apex.stats())));
    }
    private static void add(LivingDropsEvent event,Apex apex,ServerLevel level,ItemStack stack) {event.getDrops().add(new ItemEntity(level,apex.getX(),apex.getY(),apex.getZ(),stack));}
    private RealmLoot() {}
}
