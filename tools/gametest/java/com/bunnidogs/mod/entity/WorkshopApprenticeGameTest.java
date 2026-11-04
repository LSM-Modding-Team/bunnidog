package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.CivicRecipes;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Actual started expert work, one walking pupil, original paid parcels and transient witnesses. */
final class WorkshopApprenticeGameTest {
    private static Bunnidog pupil(SocietyGameTest.Context c,int east) {
        var d=ModEntities.BUNNIDOG.get().create(c.h().getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        d.setGenome(SocietyGameTest.bright());d.setStats(Stats.of(0,0,0,40,40));d.setAge(0);d.setHealth(d.getMaxHealth());d.setNoAi(true);at(d,c.tree().getBlockPos().east(east));c.h().getLevel().addFreshEntity(d);
        c.h().assertTrue(d.learning().knowledge().equals(Learning.EMPTY) && d.society().invite(c.p(),c.tree(),false),"A new household pupil begins without inherited or gifted education");return d;
    }
    private static void at(Bunnidog d,BlockPos pos) {d.snapTo(Vec3.atBottomCenterOf(pos));d.setOnGround(true);}
    private static SocietyWork.Job craft(SocietyGameTest.Context c,CivicRecipes.RecipeId recipe) {return new SocietyWork.Job(SocietyWork.Kind.CRAFT,c.tree().getBlockPos(),recipe.ordinal(),c.tree().getBlockState());}
    private static void claim(SocietyGameTest.Context c,SocietyWork.Job job) {c.worker().society().release();c.h().assertTrue(c.worker().society().claim(job),"The original expert starts and owns the real work front");}
    private static void finish(SocietyGameTest.Context c) {c.tree().residents().forEach(Bunnidog::discard);c.p().discard();c.h().succeed();}
    static void recipe(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);at(c.worker(),c.tree().getBlockPos().east());var first=pupil(c,3);var second=pupil(c,4);c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get(),2));var job=craft(c,CivicRecipes.RecipeId.PLANKS);claim(c,job);
        first.workshopLessons().tick();second.workshopLessons().tick();
        h.assertTrue(c.worker().getUUID().equals(first.workshopLessons().teacher()) && !second.workshopLessons().apprenticing() && first.learning().knowledge().equals(Learning.EMPTY) && first.society().offers().isEmpty(),"Only one pupil follows each actual expert; claiming or watching alone grants no skill or competing work");
        h.assertTrue(c.worker().society().perform(job) && c.tree().count(ModItems.GUAVA_LOG.get())==1 && c.tree().count(ModItems.GUAVA_PLANKS.get())==4 && first.learning().knowledge().knows(Learning.Routine.CRAFT) && first.learning().knowledge().progress(Learning.Routine.GARDEN)==0 && second.learning().knowledge().equals(Learning.EMPTY),"Only the actual visible committed recipe teaches its selected pupil, with one original payment and no copied gardening knowledge");c.worker().discard();
        h.assertTrue(first.society().perform(new SocietyWork.Job(SocietyWork.Kind.CRAFT,c.tree().getBlockPos(),CivicRecipes.RecipeId.STICKS.ordinal(),c.tree().getBlockState())) && c.tree().count(Items.STICK)==4,"The observer's personally acquired skill remains useful after its expert is gone");finish(c);
    }
    static void rules(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);at(c.worker(),c.tree().getBlockPos().east());var d=pupil(c,3);c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get(),2));var job=craft(c,CivicRecipes.RecipeId.PLANKS);claim(c,job);d.workshopLessons().tick();
        Consumer<EntityMobGriefingEvent> denied=e->{if(e.getEntity()==c.worker())e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(denied);
        try {h.assertTrue(!c.worker().society().perform(job) && c.tree().count(ModItems.GUAVA_LOG.get())==2 && d.learning().knowledge().equals(Learning.EMPTY),"A canceled expert operation creates neither goods nor observed knowledge");}finally{NeoForge.EVENT_BUS.unregister(denied);}
        Consumer<EntityMobGriefingEvent> moved=e->{if(e.getEntity()==c.worker())at(d,c.tree().getBlockPos().east(23));};NeoForge.EVENT_BUS.addListener(moved);
        try {h.assertTrue(c.worker().society().perform(job) && c.tree().count(ModItems.GUAVA_LOG.get())==1 && d.learning().knowledge().equals(Learning.EMPTY),"A departing pupil cannot learn a paid operation it no longer witnesses; the expert's legitimate costs remain real");}finally{NeoForge.EVENT_BUS.unregister(moved);}
        h.runAfterDelay(85,()->{at(d,c.tree().getBlockPos().east(3));d.setGenome(new Genome(d.genome().packed(),0));d.workshopLessons().tick();h.assertTrue(!d.workshopLessons().apprenticing() && c.worker().society().perform(job) && d.learning().knowledge().equals(Learning.EMPTY),"A limiting splice ends apprenticeship and cannot be bypassed by a previously selected expert");finish(c);});
    }
    static void parcel(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);at(c.worker(),c.tree().getBlockPos().east());var d=pupil(c,3);var cell=c.tree().shelter().getFirst();c.tree().put(new ItemStack(cell.item(),3));
        var pickup=new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,c.tree().getBlockPos(),-1,c.tree().getBlockState(),null,cell.pos());claim(c,pickup);d.workshopLessons().tick();h.assertTrue(c.worker().society().perform(pickup) && d.learning().knowledge().equals(Learning.EMPTY),"Witnessing an original paid pickup alone gives no building experience");
        var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());d.saveWithoutId(saved);d.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved.buildResult()));h.assertTrue(!d.workshopLessons().apprenticing(),"Reload retires the unsaved lesson and its old parcel witness");
        h.runAfterDelay(85,()->{
            at(c.worker(),cell.pos().east());at(d,cell.pos().east(2));var place=new SocietyWork.Job(SocietyWork.Kind.BUILD,cell.pos(),-1,h.getLevel().getBlockState(cell.pos()));claim(c,place);d.workshopLessons().tick();
            h.assertTrue(c.worker().society().perform(place) && c.tree().construction().owned(cell.pos()) && d.learning().knowledge().equals(Learning.EMPTY),"A new observer cannot learn placement without having witnessed that exact original pickup");
            h.runAfterDelay(85,()->{
                var next=c.tree().shelter().get(1);at(c.worker(),c.tree().getBlockPos().east());at(d,c.tree().getBlockPos().east(3));var fetch=new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,c.tree().getBlockPos(),-1,c.tree().getBlockState(),null,next.pos());claim(c,fetch);
                h.assertTrue(c.worker().society().perform(fetch) && d.learning().knowledge().equals(Learning.EMPTY),"A fresh complete lesson begins with another real original material");
                h.runAfterDelay(85,()->{at(c.worker(),next.pos().east());at(d,next.pos().east(2));var complete=new SocietyWork.Job(SocietyWork.Kind.BUILD,next.pos(),-1,h.getLevel().getBlockState(next.pos()));claim(c,complete);
                    h.assertTrue(c.worker().society().perform(complete) && c.tree().construction().owned(next.pos()) && c.tree().count(cell.item())==1 && d.learning().knowledge().knows(Learning.Routine.BUILD) && d.learning().knowledge().progress(Learning.Routine.CRAFT)==0,"Both witnessed paid walking legs and actual placement teach only building, preserving exact source stock and ownership receipts");finish(c);});
            });
        });
    }
    static void walking(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var d=pupil(c,7);var start=d.position();c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get(),64));c.p().snapTo(c.p().position().add(20,0,0));c.worker().setNoAi(false);d.setNoAi(false);
        h.succeedWhen(()->{
            h.assertTrue(d.learning().knowledge().knows(Learning.Routine.CRAFT) && d.position().distanceToSqr(start)>.25 && c.tree().count(ModItems.GUAVA_LOG.get())<64 && c.tree().count(ModItems.GUAVA_PLANKS.get())>=4 && d.learning().knowledge().progress(Learning.Routine.GARDEN)==0,"The actual pupil movement goal follows a working native expert and learns from its paid recipe: position="+d.position()+", teacher="+d.workshopLessons().teacher()+", active="+c.worker().society().active()+", skill="+d.learning().knowledge());c.tree().residents().forEach(Bunnidog::discard);c.p().discard();
        });
    }
}
