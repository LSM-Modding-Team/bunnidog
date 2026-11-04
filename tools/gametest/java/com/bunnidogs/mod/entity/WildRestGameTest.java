package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Learning;
import com.bunnidogs.mod.registry.*;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Fresh paid rest sites, canceled real work and exact isolation of both older glade revisions. */
final class WildRestGameTest {
    static void safety(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var dog=c.dogs().getFirst();var field=home.wild().fields().getFirst();
        Consumer<EntityMobGriefingEvent> veto=e->{if(e.getEntity()==dog)e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(veto);
        try {h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.HARVEST_FIELD,field) && dog.learning().knowledge().equals(Learning.EMPTY),"A native canceled harvest grants neither harvest nor replanting experience");}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.HARVEST_FIELD,field) && dog.learning().knowledge().knows(Learning.Routine.HARVEST) && dog.learning().knowledge().knows(Learning.Routine.GARDEN),"A primitive independently capable worker learns the two actual successful actions, including spending a native returned planting seed");
        // Donated material isolates native plot/parcel safety; the separate empty-store chain proves its recipe.
        home.put(new ItemStack(ModItems.NEST.get()));var cell=home.wild().works().cells().stream().filter(p->p.item()==ModItems.NEST.get()).findFirst().orElseThrow();
        WildFamilyGameTest.at(dog,home.getBlockPos());h.assertTrue(dog.homeNest()==null && dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,home.getBlockPos(),-1,home.getBlockState(),null,cell.pos())),"A resident can fetch its first Nest without already having a personal home");var exact=dog.society().workSupplies();
        NeoForge.EVENT_BUS.addListener(veto);
        try {h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.BUILD,cell.pos()) && ItemStack.matches(exact,dog.society().workSupplies()) && !home.construction().owned(cell.pos()) && dog.learning().knowledge().progress(Learning.Routine.BUILD)==0,"Native canceled initial Nest placement retains its exact paid parcel, no receipt and no building practice");}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.runAfterDelay(205,()->{
            h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.BUILD,cell.pos()) && home.construction().owned(cell.pos()) && dog.society().cargoEmpty() && dog.learning().knowledge().knows(Learning.Routine.BUILD),"A valid later paid placement earns the initial Nest and actual building experience once");
            h.getLevel().removeBlock(cell.pos(),false);home.construction().observe();h.assertTrue(!home.construction().canBuild(cell),"Player removal of a resident-made initial Nest does not authorize automatic rebuilding");WildFamilyGameTest.clean(c);h.succeed();
        });
    }
    static void legacy(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var works=home.wild().works();var bed=home.wild().beds().getFirst();
        h.assertTrue(works.restPlots() && works.cells().stream().anyMatch(cell->cell.pos().equals(bed)),"The new site's actual rest plots are cached before compatibility loading");
        h.getLevel().setBlock(bed,ModBlocks.NEST.get().defaultBlockState(),3);var original=h.getLevel().getBlockEntity(bed);var saved=home.saveWithFullMetadata(h.getLevel().registryAccess());saved.remove("PrimitiveRestPlots");
        home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));home.wild().tick();
        h.assertTrue(works.enabled() && !works.restPlots() && works.cells().size()==5 && works.cells().stream().noneMatch(cell->cell.item()==ModItems.NEST.get())
                && h.getLevel().getBlockState(bed).is(ModBlocks.NEST.get()) && h.getLevel().getBlockEntity(bed)==original && !home.construction().owned(bed),"A workshop-era save retains its original Nest and five workshop plots; missing rest revision clears cached new plots without fabricating ownership");
        var roundTrip=home.saveWithFullMetadata(h.getLevel().registryAccess());home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),roundTrip));
        h.assertTrue(!works.restPlots() && works.cells().size()==5 && home.wild().beds().stream().filter(pos->!pos.equals(bed)).allMatch(pos->h.getLevel().getBlockState(pos).is(Blocks.AIR)),"An old site's subsequent saves cannot acquire new rest permissions or spawn replacement Nests");WildFamilyGameTest.clean(c);h.succeed();
    }
}
