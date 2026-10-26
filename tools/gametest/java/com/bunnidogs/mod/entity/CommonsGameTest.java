package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Real common-store recipes, claimed jobs, walking construction parcels and durable work fronts. */
public final class CommonsGameTest {
    private static void at(Bunnidog d,BlockPos pos) {d.snapTo(Vec3.atBottomCenterOf(pos.east()));d.setOnGround(true);}
    private static void finish(SocietyGameTest.Context c) {c.tree().residents().forEach(Bunnidog::discard);c.p().discard();c.h().succeed();}
    private static Bunnidog member(SocietyGameTest.Context c,Genome genome) {
        var d=ModEntities.BUNNIDOG.get().create(c.h().getLevel(),EntitySpawnReason.COMMAND);d.setGenome(genome);d.setStats(Stats.of(0,0,0,40,40));d.setHealth(d.getMaxHealth());d.setAge(0);d.setNoAi(true);at(d,c.tree().getBlockPos().west(2));c.h().getLevel().addFreshEntity(d);WorkerFixture.familiar(d);c.h().assertTrue(d.society().invite(c.p(),c.tree(),false),"The worker joins the real same-owner commons");return d;
    }
    private static boolean craft(SocietyGameTest.Context c,CivicRecipes.RecipeId recipe) {SocietyGameTest.rest(c.worker());at(c.worker(),c.tree().getBlockPos());return c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.CRAFT,c.tree().getBlockPos(),recipe.ordinal(),c.tree().getBlockState()));}
    private static boolean collect(SocietyGameTest.Context c,Bunnidog d,BlockPos pos) {SocietyGameTest.rest(d);at(d,c.tree().getBlockPos());return d.society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,c.tree().getBlockPos(),-1,c.tree().getBlockState(),null,pos));}
    private static boolean place(SocietyGameTest.Context c,Bunnidog d,BlockPos pos) {SocietyGameTest.rest(d);at(d,pos);return d.society().perform(new SocietyWork.Job(SocietyWork.Kind.BUILD,pos,-1,c.h().getLevel().getBlockState(pos)));}
    public static void claims(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();t.put(new ItemStack(ModItems.GUAVA_LOG.get(),4));t.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),32));var other=member(c,SocietyGameTest.bright());
        var first=c.worker().society().offers().stream().filter(j->j.kind()==SocietyWork.Kind.CRAFT).findFirst().orElseThrow();h.assertTrue(c.worker().society().claim(first),"A started operation claims one real workbench");
        h.assertTrue(other.society().offers().stream().noneMatch(j->j.kind()==SocietyWork.Kind.CRAFT) && other.society().offers().stream().anyMatch(j->j.kind()==SocietyWork.Kind.FETCH_BUILD),"A second resident selects a complementary work front rather than the occupied bench");
        at(other,t.getBlockPos());int logs=t.count(ModItems.GUAVA_LOG.get());h.assertTrue(!other.society().perform(first) && t.count(ModItems.GUAVA_LOG.get())==logs,"A stale unowned offer cannot spend another worker's claimed resources");
        c.worker().setWorkPaused(true);h.assertTrue(other.society().claim(first),"An explicit pause invalidates the old worker's lease");SocietyGameTest.rest(other);h.assertTrue(other.society().active()==null && t.jobs().available(c.worker(),first),"Whole-animal reload releases transient work instead of restoring a second active claim");finish(c);
    }
    public static void recipes(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var named=new ItemStack(ModItems.GUAVA_LOG.get());named.set(DataComponents.CUSTOM_NAME,Component.literal("Family keepsake"));t.setItem(0,named.copy());t.setItem(1,new ItemStack(ModItems.GUAVA_LOG.get()));
        h.assertTrue(craft(c,CivicRecipes.RecipeId.PLANKS) && ItemStack.matches(named,t.getItem(0)) && t.count(ModItems.GUAVA_PLANKS.get())==4,"The actual loaded plank recipe consumes donated ordinary wood and preserves private named stock");
        boolean[] rejected={false};Consumer<EntityMobGriefingEvent> reenter=e->{if(e.getEntity()==c.worker())rejected[0]=!c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.CRAFT,t.getBlockPos(),CivicRecipes.RecipeId.STICKS.ordinal(),t.getBlockState()));};NeoForge.EVENT_BUS.addListener(reenter);
        try {h.assertTrue(craft(c,CivicRecipes.RecipeId.STICKS) && rejected[0] && t.count(Items.STICK)==4 && t.count(ModItems.GUAVA_PLANKS.get())==2,"The native stick recipe has exact costs and rejects recursive work during its permission callback");}finally{NeoForge.EVENT_BUS.unregister(reenter);}
        for(int slot=0;slot<t.getContainerSize();slot++)t.setItem(slot,new ItemStack(Items.STONE,64));t.setItem(0,new ItemStack(ModItems.GUAVA_LOG.get()));
        h.assertTrue(craft(c,CivicRecipes.RecipeId.PLANKS) && t.getItem(0).is(ModItems.GUAVA_PLANKS.get()) && t.getItem(0).getCount()==4,"The native transaction can use the slot freed by its last real ingredient in a full store");t.setItem(0,new ItemStack(ModItems.GUAVA_LOG.get(),2));
        h.assertTrue(!craft(c,CivicRecipes.RecipeId.PLANKS) && t.getItem(0).getCount()==2,"A full output rolls the shared journal back without consuming either log");finish(c);
    }
    public static void scarcity(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var trough=t.getBlockPos().west(4);h.getLevel().setBlock(trough,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);t.link(trough);var stock=(com.bunnidogs.mod.block.FeedingTroughBlockEntity)h.getLevel().getBlockEntity(trough);stock.insert(new ItemStack(ModItems.GUAVA.get(),16));
        // Spend the real six-operation shift on ordinary paid crafts, then remove unused donated stock.
        t.put(new ItemStack(ModItems.GUAVA_LOG.get(),6));for(int i=0;i<6;i++)h.assertTrue(craft(c,CivicRecipes.RecipeId.PLANKS),"A real operation spends one share of the support shift");for(int i=0;i<27;i++)t.setItem(i,ItemStack.EMPTY);SocietyGameTest.rest(c.worker());
        h.assertTrue(c.worker().society().plan().kind()==SocietyWork.Kind.HAUL,"An exhausted courier can rescue reachable food instead of deadlocking beside a full feeder");h.assertTrue(SocietyGameTest.act(c,SocietyWork.Kind.HAUL,trough) && SocietyGameTest.act(c,SocietyWork.Kind.DELIVER,t.getBlockPos()),"The emergency ration is still collected and walked back as actual cargo");SocietyGameTest.rest(c.worker());
        h.assertTrue(c.worker().society().plan().kind()==SocietyWork.Kind.MEAL && SocietyGameTest.act(c,SocietyWork.Kind.MEAL,t.getBlockPos()) && !c.worker().isInLove() && stock.food().getCount()==15 && t.count(ModItems.GUAVA.get())==0,"One actual ration restores the shift without breeding or consuming the feeder reserve");finish(c);
    }
    public static void parcel(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var cell=t.shelter().getFirst();t.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),8));
        h.assertTrue(collect(c,c.worker(),cell.pos()) && t.count(cell.item())==7 && c.worker().society().workSupplies().getCount()==1,"Building begins by extracting one actual block into a saved carrier");SocietyGameTest.rest(c.worker());
        Consumer<EntityMobGriefingEvent> refuse=e->{if(e.getEntity()==c.worker())e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(refuse);
        try {h.assertTrue(!SocietyGameTest.act(c,SocietyWork.Kind.DELIVER,t.getBlockPos()) && c.worker().society().workSupplies().getCount()==1 && t.count(cell.item())==7,"A native return veto retains the original paid construction parcel and common stock");}finally{NeoForge.EVENT_BUS.unregister(refuse);}
        h.assertTrue(c.worker().society().workSupplies().getCount()==1 && place(c,c.worker(),cell.pos()) && t.count(cell.item())==7 && h.getLevel().getBlockState(cell.pos()).equals(cell.state()) && t.construction().owned(cell.pos()),"The exact reloaded parcel pays one physical placed block and creates one ownership receipt");
        var next=t.shelter().get(1);h.assertTrue(collect(c,c.worker(),next.pos()),"A second construction parcel is real before source loss");h.getLevel().setBlock(t.getBlockPos(),Blocks.AIR.defaultBlockState(),3);c.p().snapTo(c.worker().position().add(1,0,0));
        com.bunnidogs.mod.menu.CareMenu.open(c.p(),c.worker());var menu=(com.bunnidogs.mod.menu.CareMenu)c.p().containerMenu;h.assertTrue(menu.clickMenuButton(c.p(),menu.buttonId(com.bunnidogs.mod.menu.CarePanels.CARGO)) && menu.clickMenuButton(c.p(),menu.buttonId(com.bunnidogs.mod.menu.CarePanels.TAKE_SUPPLIES)) && c.worker().society().cargoEmpty(),"The original owner can recover stranded construction stock through actual native cargo controls");c.worker().discard();c.p().discard();h.succeed();
    }
    public static void fitting(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var pos=c.tree().getBlockPos();h.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);h.getLevel().setBlock(pos,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);var t=(HearthTreeBlockEntity)h.getLevel().getBlockEntity(pos);t.found(c.p());
        var obstacle=pos.south(8);h.getLevel().setBlock(obstacle,Blocks.CHEST.defaultBlockState(),3);((net.minecraft.world.Container)h.getLevel().getBlockEntity(obstacle)).setItem(0,new ItemStack(Items.DIAMOND));t.survey(c.p());t.survey(c.p());
        h.assertTrue(t.authorized() && t.shelter().stream().allMatch(cell->cell.pos().getX()<pos.getX()) && ((net.minecraft.world.Container)h.getLevel().getBlockEntity(obstacle)).getItem(0).is(Items.DIAMOND),"An obstructed southern site chooses a clear western room without clearing another build or its stock");
        c.worker().discard();var data=t.saveWithFullMetadata(h.getLevel().registryAccess());var restored=(HearthTreeBlockEntity)BlockEntity.loadStatic(pos,t.getBlockState(),data,h.getLevel().registryAccess());h.assertTrue(restored.construction().cells().equals(t.construction().cells()),"Native worksite saves retain the exact chosen orientation and cells");c.p().discard();h.succeed();
    }
    public static void repair(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var cell=t.shelter().getFirst();t.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),8));h.assertTrue(collect(c,c.worker(),cell.pos()) && place(c,c.worker(),cell.pos()),"An actual paid block establishes the original project receipt");h.getLevel().removeBlock(cell.pos(),false);
        h.assertTrue(!t.construction().canBuild(cell),"A player's removed wall is respected instead of rebuilt repeatedly");t.survey(c.p());t.survey(c.p());h.assertTrue(t.construction().canBuild(cell) && collect(c,c.worker(),cell.pos()) && place(c,c.worker(),cell.pos()),"A deliberate repair survey grants a fresh paid material delivery");
        at(c.worker(),t.getBlockPos().east(4));h.getLevel().explode(null,cell.pos().getX()+.5,cell.pos().getY()+.5,cell.pos().getZ()+.5,2,Level.ExplosionInteraction.BLOCK);
        h.runAfterDelay(3,()->{h.assertTrue(h.getLevel().getBlockState(cell.pos()).isAir() && t.construction().needsRepair(cell.pos()) && t.construction().canBuild(cell),"Only a confirmed native explosion loss authorizes autonomous paid restoration");finish(c);});
    }
    public static void helper(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);Genome supported=null;
        for(int code=0;code<256 && supported==null;code++) {int word=0;for(int i=0;i<4;i++)word|=(((code>>>(i*2))&3)*17)<<(i*8);var g=new Genome(Genome.DEFAULT.packed(),word);if(CapabilityRules.potential(g.mind(),CapabilityRules.Task.BUILDING)==CapabilityRules.Potential.SUPPORTED && CapabilityRules.potential(g.mind(),CapabilityRules.Task.PLANTING)==CapabilityRules.Potential.INDEPENDENT)supported=g;}
        h.assertTrue(supported!=null,"Actual common cognitive alleles support a useful supervised builder");var helper=member(c,supported);var cell=c.tree().shelter().getFirst();c.tree().put(new ItemStack(cell.item(),4));at(c.worker(),c.tree().getBlockPos());
        h.assertTrue(collect(c,helper,cell.pos()),"A known plan and actual nearby master enable a supported builder to collect its instructions/material");at(c.worker(),c.tree().getBlockPos().west(20));h.assertTrue(!place(c,helper,cell.pos()) && helper.society().workSupplies().getCount()==1,"Losing the real supervisor neither removes inherited limits nor deletes paid cargo");
        helper.setGenome(new Genome(helper.genome().packed(),0));h.assertTrue(!place(c,helper,cell.pos()),"Previously learned words cannot grant building to a limited mind");finish(c);
    }
    public static void planning(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();t.put(new ItemStack(ModItems.GUAVA_LOG.get(),4));
        h.assertTrue(SocietyGameTest.act(c,SocietyWork.Kind.PLAN,t.getBlockPos()) && t.agenda().need()==HearthAgenda.Need.FOOD && t.agenda().steps().contains(CivicRecipes.RecipeId.AXE) && t.count(ModItems.GUAVA_LOG.get())==4,"A capable planner records a concrete local food/tool dependency without producing resources");
        var copy=(HearthTreeBlockEntity)BlockEntity.loadStatic(t.getBlockPos(),t.getBlockState(),t.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());h.assertTrue(copy.agenda().steps().equals(t.agenda().steps()) && copy.agenda().author().equals(c.worker().getUUID()),"The actual public instruction and its provenance survive native Hearth saves");
        Genome follower=null;for(int code=0;code<256 && follower==null;code++) {int word=0;for(int i=0;i<4;i++)word|=(((code>>>(i*2))&3)*17)<<(i*8);var g=new Genome(Genome.DEFAULT.packed(),word);if(CapabilityRules.potential(g.mind(),CapabilityRules.Task.PLANTING)==CapabilityRules.Potential.INDEPENDENT && CapabilityRules.potential(g.mind(),CapabilityRules.Task.PLANNING)!=CapabilityRules.Potential.INDEPENDENT)follower=g;}
        h.assertTrue(follower!=null,"An ordinary trained craftsperson can be useful without the architect's inherited capacities");var worker=member(c,follower);c.worker().discard();
        for(int turn=0;turn<6 && t.count(Items.WOODEN_AXE)==0;turn++) {SocietyGameTest.rest(worker);at(worker,t.getBlockPos());var job=worker.society().plan();h.assertTrue(job!=null && job.kind()==SocietyWork.Kind.CRAFT && worker.society().perform(job),"The remaining craftsperson executes real affordable dependency steps after the planner leaves");}
        h.assertTrue(t.count(Items.WOODEN_AXE)==1 && t.count(ModItems.GUAVA_LOG.get())==2 && t.count(ModItems.GUAVA_PLANKS.get())==3 && t.count(Items.STICK)==2,"The handed-off plan closes with exact native log/plank/stick/axe conservation");t.put(new ItemStack(ModItems.GUAVA.get(),32));h.assertTrue(t.agenda().boost(new SocietyWork.Job(SocietyWork.Kind.PRUNE,t.orchard().getFirst(),-1,Blocks.AIR.defaultBlockState()))==0,"A stale food instruction stops changing priorities when actual needs change");finish(c);
    }
    public static void planningLimits(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);c.worker().setGenome(new Genome(c.worker().genome().packed(),0));h.assertTrue(!SocietyGameTest.act(c,SocietyWork.Kind.PLAN,c.tree().getBlockPos()) && c.tree().agenda().author()==null,"Familiar routines alone cannot bypass the real multi-gene planning requirement");finish(c);
    }
    public static void ai(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();t.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),64));t.put(new ItemStack(ModItems.NEST.get(),4));t.put(new ItemStack(ModItems.GUAVA.get(),64));t.put(new ItemStack(Items.WOODEN_AXE));c.worker().setNoAi(false);c.p().snapTo(c.p().position().add(18,0,0));
        h.succeedWhen(()->{h.assertTrue(t.shelter().stream().anyMatch(cell->t.construction().owned(cell.pos()) && h.getLevel().getBlockState(cell.pos()).equals(cell.state())),"Native AI must walk to its store, collect a real construction parcel and finish the physical work front: job="+c.worker().society().active()+", stock="+c.worker().society().workSupplies()+", at="+c.worker().position());c.worker().discard();c.p().discard();});
    }
}
