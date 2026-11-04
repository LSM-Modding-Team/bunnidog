package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.CivicRecipes;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Paid practical professions, original stock/cargo and explicit old-worker compatibility. */
final class WorkshopLearningGameTest {
    private static void knowledge(Bunnidog dog,Learning learning) {
        var tag=new net.minecraft.nbt.CompoundTag();tag.putString("Learning",learning.toString());
        dog.learning().restore(TagValueInput.create(ProblemReporter.DISCARDING,dog.level().registryAccess(),tag),false);
    }
    private static SocietyGameTest.Context beginner(GameTestHelper h,Genome genome) {
        var c=SocietyGameTest.setup(h);c.worker().setGenome(genome);knowledge(c.worker(),Learning.EMPTY);
        c.worker().snapTo(Vec3.atBottomCenterOf(c.tree().getBlockPos().east()));c.worker().setOnGround(true);return c;
    }
    private static void finish(SocietyGameTest.Context c) {c.tree().residents().forEach(Bunnidog::discard);c.p().discard();c.h().succeed();}
    private static SocietyWork.Job craft(SocietyGameTest.Context c,CivicRecipes.RecipeId recipe) {return new SocietyWork.Job(SocietyWork.Kind.CRAFT,c.tree().getBlockPos(),recipe.ordinal(),c.tree().getBlockState());}
    private static void lesson(SocietyGameTest.Context c,Learning.Routine routine) {
        c.p().snapTo(c.worker().position().add(1,0,0));CareMenu.open(c.p(),c.worker());var menu=(CareMenu)c.p().containerMenu;
        c.h().assertTrue(menu.clickMenuButton(c.p(),menu.buttonId(CarePanels.ROLES)) && menu.clickMenuButton(c.p(),menu.buttonId(LearningPanel.OPEN)) && menu.clickMenuButton(c.p(),menu.buttonId(LearningPanel.WORKSHOP)),"Workshop lessons use the real bounded conversation history");
        c.h().assertTrue(menu.view().controls().size()<=12 && menu.clickMenuButton(c.p(),menu.buttonId(LearningPanel.teachId(routine))) && c.p().containerMenu!=menu,"Starting practical household instruction closes the actual conversation");
    }
    static void teaching(GameTestHelper h) {
        var c=beginner(h,Genome.DEFAULT);var d=c.worker();var tree=c.tree();tree.setItem(0,new ItemStack(ModItems.GUAVA_LOG.get(),2));var job=craft(c,CivicRecipes.RecipeId.PLANKS);
        h.assertTrue(!d.society().perform(job) && tree.count(ModItems.GUAVA_LOG.get())==2,"Stock, physical stats and capacity alone do not grant a new workshop profession");lesson(c,Learning.Routine.CRAFT);
        h.assertTrue(d.homeNest()==null && d.society().perform(job) && tree.count(ModItems.GUAVA_LOG.get())==1 && tree.count(ModItems.GUAVA_PLANKS.get())==4 && d.learning().knowledge().progress(Learning.Routine.CRAFT)==5 && d.learning().knowledge().progress(Learning.Routine.GARDEN)==0,"One paid native recipe teaches only practical crafting, without requiring a pre-existing Nest or garden lessons");
        h.runAfterDelay(85,()->{
            Consumer<EntityMobGriefingEvent> departing=e->{if(e.getEntity()==d)c.p().snapTo(d.position().add(20,0,0));};NeoForge.EVENT_BUS.addListener(departing);
            try {h.assertTrue(!d.society().perform(job) && tree.count(ModItems.GUAVA_LOG.get())==1 && tree.count(ModItems.GUAVA_PLANKS.get())==4 && d.learning().knowledge().progress(Learning.Routine.CRAFT)==5,"Losing the real instructor during a native permission callback cannot spend ingredients or advance skill");}finally{NeoForge.EVENT_BUS.unregister(departing);}
            c.p().snapTo(d.position().add(1,0,0));h.assertTrue(d.society().perform(job) && d.learning().knowledge().knows(Learning.Routine.CRAFT) && d.learning().lesson()==null,"The second actual recipe graduates an independent helper");
            h.runAfterDelay(85,()->{c.p().snapTo(d.position().add(20,0,0));h.assertTrue(d.society().perform(craft(c,CivicRecipes.RecipeId.STICKS)) && tree.count(Items.STICK)==4 && tree.count(ModItems.GUAVA_PLANKS.get())==6 && d.learning().knowledge().progress(Learning.Routine.BUILD)==0,"A trained craftsperson works after the guide leaves without gaining free building or farming experience");finish(c);});
        });
    }
    static void building(GameTestHelper h) {
        var c=beginner(h,SocietyGameTest.bright());var d=c.worker();var cell=c.tree().shelter().getFirst();c.tree().put(new ItemStack(cell.item(),3));lesson(c,Learning.Routine.BUILD);
        var pickup=new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,c.tree().getBlockPos(),-1,c.tree().getBlockState(),null,cell.pos());
        h.assertTrue(d.society().offers().stream().anyMatch(j->j.kind()==SocietyWork.Kind.FETCH_BUILD) && d.society().perform(pickup) && d.learning().knowledge().progress(Learning.Routine.BUILD)==0 && d.society().workSupplies().getCount()==1,"A construction lesson offers its own work and collects one real material without awarding pickup-only practice");
        h.runAfterDelay(85,()->{
            d.snapTo(Vec3.atBottomCenterOf(cell.pos().east()));d.setOnGround(true);c.p().snapTo(d.position().add(20,0,0));var place=new SocietyWork.Job(SocietyWork.Kind.BUILD,cell.pos(),-1,h.getLevel().getBlockState(cell.pos()));
            h.assertTrue(!d.society().perform(place) && d.society().workSupplies().getCount()==1 && d.learning().knowledge().progress(Learning.Routine.BUILD)==0,"An absent guide leaves the exact construction parcel recoverable and grants no lesson");
            c.p().snapTo(d.position().add(1,0,0));h.assertTrue(d.society().perform(place) && h.getLevel().getBlockState(cell.pos()).equals(cell.state()) && c.tree().construction().owned(cell.pos()) && c.tree().count(cell.item())==2 && d.society().cargoEmpty() && d.learning().knowledge().knows(Learning.Routine.BUILD) && !d.learning().knowledge().knows(Learning.Routine.CRAFT),"Actual paid placement and its original ownership receipt teach building separately from crafting");finish(c);
        });
    }
    static void replacement(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var mentor=c.worker();var d=ModEntities.BUNNIDOG.get().create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        d.setGenome(SocietyGameTest.bright());d.setStats(Stats.of(0,0,0,40,40));d.setAge(0);d.setHealth(d.getMaxHealth());d.setNoAi(true);d.snapTo(Vec3.atBottomCenterOf(c.tree().getBlockPos().east()));d.setOnGround(true);h.getLevel().addFreshEntity(d);knowledge(d,Learning.EMPTY);
        h.assertTrue(d.society().invite(c.p(),c.tree(),false),"The apprentice joins the same real household");var apprentice=new SocietyGameTest.Context(h,c.tree(),c.p(),d);c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get(),2));
        h.assertTrue(!mentor.society().perform(new SocietyWork.Job(SocietyWork.Kind.TEACH,d.blockPosition(),Learning.Routine.CRAFT.ordinal(),h.getLevel().getBlockState(d.blockPosition()),new SocietyWork.Patient(d.getUUID(),null))),"Workshop knowledge cannot be granted by the old timed lecture job");
        h.assertTrue(d.society().perform(craft(apprentice,CivicRecipes.RecipeId.PLANKS)) && d.learning().knowledge().knows(Learning.Routine.CRAFT) && d.learning().knowledge().progress(Learning.Routine.GARDEN)==0,"Local resident guidance enables real paid apprenticeship without uploading a gardener's unrelated skill");mentor.discard();
        h.runAfterDelay(85,()->{h.assertTrue(d.society().perform(craft(apprentice,CivicRecipes.RecipeId.STICKS)) && c.tree().count(Items.STICK)==4,"A personally trained replacement still performs the native recipe after its original expert is gone");finish(apprentice);});
    }
    static void migration(GameTestHelper h) {
        var c=beginner(h,SocietyGameTest.bright());var d=c.worker();
        for(boolean baby:new boolean[]{false,true}) {
            d.setAge(baby ? -12000 : 0);var tag=new net.minecraft.nbt.CompoundTag();tag.putString("Learning","l2.1-2-6-4-3");d.learning().restore(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),tag),false);
            h.assertTrue(d.learning().knowledge().knows(Learning.Routine.CRAFT)==!baby && d.learning().knowledge().knows(Learning.Routine.BUILD)==!baby && d.learning().knowledge().progress(Learning.Routine.GARDEN)==6 && d.learning().knowledge().progress(Learning.Routine.SCOUT)==3,"Only old adults with mastered gardening retain the workshop jobs they previously performed");
        }
        d.setAge(0);var emptyGarden=Learning.parse("l3.0-0-6-0-0-0-0").orElseThrow();knowledge(d,emptyGarden);h.assertTrue(!d.learning().canWork(Learning.Routine.CRAFT) && !d.learning().canWork(Learning.Routine.BUILD),"New-schema gardening alone never grants either workshop profession");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());d.learning().save(out);h.assertTrue(out.buildResult().getStringOr("Learning","").equals(emptyGarden.toString()),"New explicit beginner skills survive their exact native save schema");
        var opaque=new net.minecraft.nbt.CompoundTag();opaque.putString("Learning","l9.future-personal-record");d.learning().restore(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),opaque),false);var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());d.learning().save(saved);h.assertTrue(saved.buildResult().getStringOr("Learning","").equals("l9.future-personal-record") && !d.learning().canWork(Learning.Routine.CRAFT),"Unknown future knowledge stays opaque rather than becoming free legacy mastery");
        c.p().setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ModItems.CALIPERS.get()));var work=CareReadingPanel.view(c.p(),d,2);var workshop=CareReadingPanel.view(c.p(),d,5);h.assertTrue(work.rows().size()<=12 && workshop.rows().size()==4,"Both actual tool-gated measurement purposes retain the twelve-row wire bound");finish(c);
    }
    static void walking(GameTestHelper h) {
        var c=beginner(h,SocietyGameTest.bright());var d=c.worker();c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get(),2));d.snapTo(Vec3.atBottomCenterOf(c.tree().getBlockPos().east(6)));c.p().snapTo(d.position().add(1,0,0));lesson(c,Learning.Routine.CRAFT);d.setNoAi(false);
        h.succeedWhen(()->{
            h.assertTrue(d.learning().knowledge().knows(Learning.Routine.CRAFT) && c.tree().count(ModItems.GUAVA_LOG.get())==1 && c.tree().count(ModItems.GUAVA_PLANKS.get())==4 && d.learning().knowledge().progress(Learning.Routine.GARDEN)==0,"The native movement/work goal approaches the original Hearth, completes one actual recipe and graduates its focused craft lesson: position="+d.position()+", active="+d.society().active()+", lesson="+d.learning().lesson()+", knowledge="+d.learning().knowledge());
            c.tree().residents().forEach(Bunnidog::discard);c.p().discard();
        });
    }
}
