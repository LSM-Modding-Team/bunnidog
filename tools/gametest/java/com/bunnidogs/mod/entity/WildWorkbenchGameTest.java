package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.CivicRecipes;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** A native paid physical table, commit-time workspace loss and exact older-glade compatibility. */
final class WildWorkbenchGameTest {
    static void safety(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var dog=c.dogs().getFirst();var at=home.wild().works().workbench();var cell=home.construction().cell(at);
        // Focused donated stock isolates workspace safety; the empty-store chain proves all inputs' origin.
        home.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),12));home.put(new ItemStack(Items.STICK,4));
        h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.AXE.ordinal()) && home.count(ModItems.GUAVA_PLANKS.get())==12,"An affordable larger recipe still needs a real physical workbench");
        h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.WORKBENCH.ordinal()) && home.count(ModItems.GUAVA_PLANKS.get())==8 && home.count(Items.CRAFTING_TABLE)==1,"The hand-sized native table recipe spends exactly four original planks");
        WildFamilyGameTest.at(dog,home.getBlockPos());h.assertTrue(dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,home.getBlockPos(),-1,home.getBlockState(),null,at)),"The actual table is withdrawn into one original carried building parcel");var exact=dog.society().workSupplies();
        Consumer<EntityMobGriefingEvent> veto=e->{if(e.getEntity()==dog)e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(veto);
        try {h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.BUILD,at) && ItemStack.matches(exact,dog.society().workSupplies()) && !home.construction().owned(at),"Canceled table placement retains its exact paid parcel and grants no construction receipt");}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.runAfterDelay(205,()->{
            h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.BUILD,at) && home.construction().owned(at) && home.wild().works().craftingAvailable(CivicRecipes.RecipeId.AXE),"The reachable actual native table is built only after successful paid placement");
            h.getLevel().setBlock(at.below(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.AXE.ordinal()) && home.count(ModItems.GUAVA_PLANKS.get())==8,"Power pauses the actual table without spending recipe materials");h.getLevel().setBlock(at.below(),Blocks.DIRT.defaultBlockState(),3);
            WildFamilyGameTest.at(dog,home.getBlockPos());dog.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(at.north(2)));var screen=home.getBlockPos().west();h.getLevel().setBlock(screen,Blocks.STONE.defaultBlockState(),3);h.getLevel().setBlock(screen.above(),Blocks.STONE.defaultBlockState(),3);
            var pupil=c.dogs().get(1);SocietyGameTest.rest(pupil);pupil.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(home.getBlockPos().east(2)));pupil.setOnGround(true);var lesson=new SocietyWork.Job(SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.AXE.ordinal(),home.getBlockState());h.assertTrue(dog.society().claim(lesson),"The original expert starts an actual larger recipe at its real work front");pupil.workshopLessons().tick();
            h.assertTrue(dog.getUUID().equals(pupil.workshopLessons().teacher()),"A pupil with real sight of its expert starts observing the claimed recipe");h.assertTrue(dog.society().perform(lesson),"The expert can see and reach both stock and table around the pupil's separate screen");
            h.assertTrue(pupil.learning().knowledge().progress(com.bunnidogs.mod.genetics.Learning.Routine.CRAFT)==0,"Seeing the expert/Hearth without seeing the table teaches no larger-recipe experience");dog.society().release();pupil.workshopLessons().cancel();h.getLevel().removeBlock(screen,false);h.getLevel().removeBlock(screen.above(),false);
            Consumer<EntityMobGriefingEvent> lost=e->{if(e.getEntity()==dog)h.getLevel().removeBlock(at,false);};NeoForge.EVENT_BUS.addListener(lost);
            try {h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.PICKAXE.ordinal()) && home.count(ModItems.GUAVA_PLANKS.get())==5 && home.count(Items.STICK)==2 && home.count(Items.WOODEN_PICKAXE)==0,"A native permission callback removing the workbench invalidates the larger recipe before any original input is spent");}finally {NeoForge.EVENT_BUS.unregister(lost);}
            h.assertTrue(!home.construction().canBuild(cell) && !home.wild().works().needsWorkbench(),"A deliberately removed paid table is not silently rebuilt");WildFamilyGameTest.clean(c);h.succeed();
        });
    }
    static void legacy(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var works=home.wild().works();var at=works.workbench();
        h.assertTrue(works.workbenchPlot() && works.cells().size()==9,"A new site's workbench/rest cell list is cached before compatibility loading");h.getLevel().setBlock(at,Blocks.CRAFTING_TABLE.defaultBlockState(),3);
        var saved=home.saveWithFullMetadata(h.getLevel().registryAccess());saved.remove("PrimitiveWorkbenchPlot");home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));home.wild().tick();
        h.assertTrue(works.enabled() && works.restPlots() && !works.workbenchPlot() && works.cells().size()==8 && works.cells().stream().noneMatch(cell->cell.item()==Items.CRAFTING_TABLE)
                && h.getLevel().getBlockState(at).is(Blocks.CRAFTING_TABLE) && !home.construction().owned(at),"A rest-era site retains its original eight plots and actual unrelated table, without new ownership or cached workbench permission");
        h.getLevel().removeBlock(at,false);home.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),3));home.put(new ItemStack(Items.STICK,2));
        h.assertTrue(WildFamilyGameTest.act(c,c.dogs().getFirst(),SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.AXE.ordinal()) && home.count(Items.WOODEN_AXE)==1,"Older glades retain their original paid crafting rules without acquiring a new missing-table dependency");
        var roundTrip=home.saveWithFullMetadata(h.getLevel().registryAccess());home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),roundTrip));h.assertTrue(!works.workbenchPlot() && works.cells().size()==8,"An older site's subsequent native save cannot turn on the new workbench plot");WildFamilyGameTest.clean(c);h.succeed();
    }
    private static boolean offered(WildFamilyGameTest.Site c,Bunnidog dog,CivicRecipes.RecipeId recipe) {WildFamilyGameTest.at(dog,c.home().getBlockPos());return dog.society().offers().stream().anyMatch(job->job.kind()==SocietyWork.Kind.CRAFT && job.index()==recipe.ordinal());}
    private static boolean fetch(WildFamilyGameTest.Site c,Bunnidog dog,net.minecraft.core.BlockPos at) {WildFamilyGameTest.at(dog,c.home().getBlockPos());return dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,c.home().getBlockPos(),-1,c.home().getBlockState(),null,at));}
    static void handoffs(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var carrier=c.dogs().getFirst();var producer=c.dogs().get(2);var works=home.wild().works();
        home.put(new ItemStack(Items.CRAFTING_TABLE));home.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),4));
        h.assertTrue(fetch(c,carrier,works.workbench()) && !offered(c,producer,CivicRecipes.RecipeId.WORKBENCH) && home.count(Items.CRAFTING_TABLE)==0 && carrier.society().workSupplies().is(Items.CRAFTING_TABLE),"The other craftsperson does not spend the last four planks duplicating a real workbench already walking to its original plot");
        var saved=home.saveWithFullMetadata(h.getLevel().registryAccess());home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));WildFamilyGameTest.at(carrier,home.getBlockPos());
        h.assertTrue(!offered(c,producer,CivicRecipes.RecipeId.WORKBENCH),"Whole-carrier/home reload preserves the actual in-flight table rather than relying on a transient claim");
        h.assertTrue(WildFamilyGameTest.act(c,carrier,SocietyWork.Kind.DELIVER,home.getBlockPos()) && WildFamilyGameTest.build(c,carrier,home.construction().cell(works.workbench())),"The same returned original table can be carried and built normally");
        home.put(new ItemStack(Items.FURNACE));home.put(new ItemStack(Items.COBBLESTONE,8));h.assertTrue(fetch(c,carrier,works.furnace()) && !offered(c,producer,CivicRecipes.RecipeId.FURNACE),"A real furnace parcel prevents spending another eight cobblestone on a duplicate");var stone=home.take(Items.COBBLESTONE,8);h.assertTrue(!works.needsStone(),"A real walking furnace also ends its finite quarry-material demand");home.put(stone);h.assertTrue(WildFamilyGameTest.act(c,carrier,SocietyWork.Kind.DELIVER,home.getBlockPos()),"The original furnace remains recoverable at its home");
        home.put(new ItemStack(Items.TORCH,4));home.put(new ItemStack(Items.CHARCOAL));home.put(new ItemStack(Items.STICK));var light=works.cells().stream().filter(cell->cell.item()==Items.TORCH).findFirst().orElseThrow();
        h.assertTrue(fetch(c,carrier,light.pos()) && home.count(Items.TORCH)==3 && !offered(c,producer,CivicRecipes.RecipeId.TORCH),"Three stored lights plus the real fourth walking parcel satisfy four plots without another charcoal/stick payment");h.assertTrue(WildFamilyGameTest.act(c,carrier,SocietyWork.Kind.DELIVER,home.getBlockPos()) && home.count(Items.TORCH)==4,"Returning the actual light preserves the original four-item budget");WildFamilyGameTest.clean(c);h.succeed();
    }
}
