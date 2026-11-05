package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.CivicRecipes;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** A deliberate common-store pause retains actual parcels, food, original timber and personal practice. */
final class HearthPauseGameTest {
    private static void paused(WildFamilyGameTest.Site c,boolean lock,boolean value) {
        var home=c.home();var level=c.h().getLevel();
        if(!lock) {level.setBlock(home.getBlockPos().east(),value ? Blocks.REDSTONE_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState(),3);return;}
        var tag=home.saveWithFullMetadata(level.registryAccess());tag.remove("lock");
        if(value) {var key=new net.minecraft.nbt.CompoundTag();key.putString("items","minecraft:tripwire_hook");tag.put("lock",key);}
        home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));
    }
    static void pause(GameTestHelper h,boolean lock) {
        var c=PrimitiveFurnitureGameTest.setup(h,false);var home=c.home();var worker=c.dogs().getFirst();var crafter=c.dogs().get(1);
        h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.CLIP_WOOD,home.wild().timber().getFirst()),"The paused transfer begins with an actual original timber harvest");
        var item=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(home.getBlockPos()).inflate(24),e->e.getItem().is(ModItems.GUAVA_LOG.get())).getFirst();item.setNoPickUpDelay();SocietyGameTest.rest(worker);worker.snapTo(item.position().add(1,0,0));worker.setOnGround(true);
        h.assertTrue(worker.society().perform(new SocietyWork.Job(SocietyWork.Kind.GATHER_FIELD,item.blockPosition(),item.getId(),h.getLevel().getBlockState(item.blockPosition()))),"The real public source becomes one original walking parcel before the pause");var exact=worker.society().workSupplies();var knowledge=worker.learning().knowledge();
        // Explicit policy stock, separate from the actual original walking log and bounded timber receipt.
        home.put(new ItemStack(ModItems.GUAVA_LOG.get(),2));home.put(new ItemStack(ModItems.GUAVA.get(),2));var next=home.wild().timber().get(1);var before=h.getLevel().getBlockState(next);
        paused(c,lock,true);
        try {
            WildFamilyGameTest.at(worker,home.getBlockPos());var offers=worker.society().offers();
            boolean delivered=WildFamilyGameTest.act(c,worker,SocietyWork.Kind.DELIVER,home.getBlockPos());
            boolean ate=WildFamilyGameTest.act(c,worker,SocietyWork.Kind.MEAL,home.getBlockPos());
            boolean harvested=WildFamilyGameTest.act(c,crafter,SocietyWork.Kind.CLIP_WOOD,next);
            boolean crafted=WildFamilyGameTest.act(c,crafter,SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.PLANKS.ordinal());
            h.assertTrue((lock ? home.isLocked() : h.getLevel().hasNeighborSignal(home.getBlockPos())) && offers.isEmpty() && !delivered && !ate && !harvested && !crafted && ItemStack.matches(exact,worker.society().workSupplies()) && home.count(ModItems.GUAVA_LOG.get())==2 && home.count(ModItems.GUAVA.get())==2 && home.count(ModItems.GUAVA_PLANKS.get())==0 && home.wild().timberAvailable(next) && h.getLevel().getBlockState(next).equals(before) && worker.learning().knowledge().equals(knowledge),"The actual "+(lock ? "native container lock" : "root redstone signal")+" pauses offers, cargo returns, meals, finite harvesting and paid recipes without losing or crediting material/practice");
        } finally {paused(c,lock,false);}
        h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.DELIVER,home.getBlockPos()) && home.count(ModItems.GUAVA_LOG.get())==3 && WildFamilyGameTest.act(c,crafter,SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.PLANKS.ordinal()) && home.count(ModItems.GUAVA_LOG.get())==2 && home.count(ModItems.GUAVA_PLANKS.get())==4 && WildFamilyGameTest.act(c,worker,SocietyWork.Kind.MEAL,home.getBlockPos()) && home.count(ModItems.GUAVA.get())==1 && home.wild().timberAvailable(next) && worker.stats().equals(Stats.ZERO),"Removing the actual pause permits one exact parcel return, one paid native recipe and one real meal; original unused timber remains intact");WildFamilyGameTest.clean(c);h.succeed();
    }
    static void meals(GameTestHelper h) {
        var c=PrimitiveFurnitureGameTest.setup(h,false);var home=c.home();var worker=c.dogs().getFirst();var plain=worker.genome();int grazer=Gene.DIET.alleleNamed("grazer").id();var genome=plain.with(Gene.DIET,0,grazer).with(Gene.DIET,1,grazer);worker.setGenome(genome);home.put(new ItemStack(Items.WHEAT,2));worker.setHealth(worker.getMaxHealth()*.85F);float health=worker.getHealth();var original=home.getItem(0).copy();
        h.assertTrue(worker.canEat(original),"The actual Grazer fixture accepts its sole real grain meal; guavas remain universal food");var privateFood=original.copy();privateFood.set(DataComponents.CUSTOM_NAME,Component.literal("Saved family grain"));
        for(int mode=0;mode<4;mode++) {
            final int kind=mode;boolean[] called={false};Consumer<EntityMobGriefingEvent> change=e->{if(e.getEntity()==worker) {called[0]=true;
                if(kind==0)e.setCanGrief(false);
                else if(kind==1)home.setItem(0,privateFood.copy());
                else if(kind==2)worker.setGenome(plain);
                else paused(c,false,true);
            }};NeoForge.EVENT_BUS.addListener(change);
            try {boolean ate=WildFamilyGameTest.act(c,worker,SocietyWork.Kind.MEAL,home.getBlockPos());int meals=home.saveWithFullMetadata(h.getLevel().registryAccess()).getIntArray("Evidence").orElseThrow()[2];h.assertTrue(!ate && called[0] && ItemStack.matches(home.getItem(0),kind==1 ? privateFood : original) && worker.getHealth()==health && meals==0,"Native meal cancellation or callback changes to source components, actual Diet or root power preserve the original bill, health and meal receipt; mode="+kind+", ate="+ate+", callback="+called[0]+", stock="+home.getItem(0)+", health="+worker.getHealth()+"/"+health+", meals="+meals);}
            finally {NeoForge.EVENT_BUS.unregister(change);paused(c,false,false);worker.setGenome(genome);home.setItem(0,original.copy());}
        }
        var other=c.dogs().get(1);other.setGenome(genome);other.setHealth(health);boolean[] entered={false},inner={false};Consumer<EntityMobGriefingEvent> share=e->{if(e.getEntity()==worker && !entered[0]) {entered[0]=true;inner[0]=WildFamilyGameTest.act(c,other,SocietyWork.Kind.MEAL,home.getBlockPos());}};NeoForge.EVENT_BUS.addListener(share);
        try {h.assertTrue(!WildFamilyGameTest.act(c,worker,SocietyWork.Kind.MEAL,home.getBlockPos()) && entered[0] && inner[0] && home.count(Items.WHEAT)==1 && worker.getHealth()==health && other.getHealth()>health && home.saveWithFullMetadata(h.getLevel().registryAccess()).getIntArray("Evidence").orElseThrow()[2]==1,"A callback's second real eater pays one real meal; the outer operation refuses its changed original slot without another debit, heal or receipt");}finally {NeoForge.EVENT_BUS.unregister(share);}
        h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.MEAL,home.getBlockPos()) && home.count(Items.WHEAT)==0 && worker.getHealth()>health && home.saveWithFullMetadata(h.getLevel().registryAccess()).getIntArray("Evidence").orElseThrow()[2]==2 && worker.learning().knowledge().equals(Learning.EMPTY) && worker.stats().equals(Stats.ZERO),"The actual remaining food later pays exactly one successful meal without granting education or body points");WildFamilyGameTest.clean(c);h.succeed();
    }
}
