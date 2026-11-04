package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.CivicRecipes;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Ordinary helper-profile craftspeople furnish original primitive plots without acquiring architecture. */
final class PrimitiveFurnitureGameTest {
    private static WildFamilyGameTest.Site setup(GameTestHelper h) {
        var initial=WildFamilyGameTest.setup(h,false,false);var home=initial.home();var saved=home.saveWithFullMetadata(h.getLevel().registryAccess());var founders=new net.minecraft.nbt.ListTag();for(int i=0;i<3;i++)founders.add(net.minecraft.nbt.StringTag.valueOf(Genome.DEFAULT.toString()));saved.put("WildFounders",founders);home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        for(int i=0;i<3;i++)home.wild().tick();var dogs=home.residents();h.assertTrue(dogs.size()==3 && home.wild().founders().stream().allMatch(Genome.DEFAULT::equals),"The actual frozen ordinary helper-profile founders spawn once, rather than receiving an elite mind or a reroll");
        dogs.forEach(d->{d.setNoAi(true);d.setOnGround(true);h.assertTrue(d.stats().equals(Stats.ZERO) && d.learning().knowledge().equals(Learning.EMPTY) && CapabilityRules.potential(d.mind(),CapabilityRules.Task.BUILDING)==CapabilityRules.Potential.LIMITED && CapabilityRules.potential(d.mind(),CapabilityRules.Task.CRAFTING)==CapabilityRules.Potential.INDEPENDENT,"The untrained ordinary profile can learn crafting but remains genetically limited at architecture");});return new WildFamilyGameTest.Site(h,home,dogs);
    }
    private static boolean craft(WildFamilyGameTest.Site c,Bunnidog dog,CivicRecipes.RecipeId recipe) {return WildFamilyGameTest.act(c,dog,SocietyWork.Kind.CRAFT,c.home().getBlockPos(),recipe.ordinal());}
    static void furniture(GameTestHelper h) {
        var c=setup(h);var home=c.home();var dog=c.dogs().getFirst();for(var log:home.wild().timber())h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.CLIP_WOOD,log),"The ordinary founder harvests actual original fallen logs");WildFamilyGameTest.collectAll(c,dog);
        for(var recipe:new CivicRecipes.RecipeId[]{CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.STICKS,CivicRecipes.RecipeId.WORKBENCH})h.assertTrue(craft(c,dog,recipe),"Personal craft experience comes from the actual paid primitive recipe: "+recipe);
        h.assertTrue(WildFamilyGameTest.build(c,dog,home.construction().cell(home.wild().works().workbench())) && craft(c,dog,CivicRecipes.RecipeId.AXE),"A trained ordinary craftsperson carries and places its paid primitive table before making the native axe");
        for(var field:home.wild().fields())h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.HARVEST_FIELD,field),"Actual native grain and returned planting seeds supply bedding");WildFamilyGameTest.collectAll(c,dog);var bed=home.wild().beds().getFirst();
        h.assertTrue(craft(c,dog,CivicRecipes.RecipeId.NEST) && WildFamilyGameTest.build(c,dog,home.construction().cell(bed)) && home.construction().owned(bed) && home.count(ModItems.NEST.get())==0,"The original three logs and actual wheat pay for a real carried-and-placed first Nest, without an architect or stocked inputs");
        h.assertTrue(dog.learning().knowledge().knows(Learning.Routine.CRAFT) && dog.learning().knowledge().progress(Learning.Routine.BUILD)==0 && dog.stats().equals(Stats.ZERO),"Primitive furniture does not upload building experience or bred points");
        var wall=home.shelter().stream().filter(cell->cell.item()==ModItems.GUAVA_PLANKS.get()).findFirst().orElseThrow();home.put(new ItemStack(ModItems.GUAVA_PLANKS.get()));WildFamilyGameTest.at(dog,home.getBlockPos());
        h.assertTrue(!dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,home.getBlockPos(),-1,home.getBlockState(),null,wall.pos())) && home.count(ModItems.GUAVA_PLANKS.get())==1 && dog.society().cargoEmpty(),"The same craftsperson cannot expand this exception into rooms, walls or general architecture");WildFamilyGameTest.clean(c);h.succeed();
    }
    static void limits(GameTestHelper h) {
        var c=setup(h);var home=c.home();var dog=c.dogs().getFirst();var at=home.wild().works().workbench();home.put(new ItemStack(Items.CRAFTING_TABLE));WildFamilyGameTest.at(dog,home.getBlockPos());
        h.assertTrue(!dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,home.getBlockPos(),-1,home.getBlockState(),null,at)),"Independent potential and a donated table alone do not supply actual craft experience");home.put(new ItemStack(ModItems.GUAVA_LOG.get(),2));h.assertTrue(craft(c,dog,CivicRecipes.RecipeId.PLANKS) && craft(c,dog,CivicRecipes.RecipeId.PLANKS),"Two real hand-sized recipes teach the ordinary craftsperson");
        dog.setGenome(Genome.DEFAULT.with(CognitiveGene.HANDLING,0,2));var foreman=c.dogs().get(2);foreman.setGenome(SocietyGameTest.bright());var known=net.minecraft.world.level.storage.TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());foreman.saveWithoutId(known);var record=known.buildResult();record.putString("Learning",Learning.LEGACY_FAMILIAR.toString());foreman.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),record));var bed=home.wild().beds().getFirst();foreman.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(bed.east(2)));home.put(new ItemStack(ModItems.NEST.get()));
        h.assertTrue(CapabilityRules.potential(dog.mind(),CapabilityRules.Task.BUILDING)==CapabilityRules.Potential.SUPPORTED && WildFamilyGameTest.build(c,dog,home.construction().cell(bed)) && dog.society().guided(Learning.Routine.BUILD) && dog.learning().knowledge().progress(Learning.Routine.BUILD)==0,"A nearby real foreman cannot turn zero-Bloom furniture assembly into architecture experience below its physical readiness gate");WildFamilyGameTest.at(dog,home.getBlockPos());
        h.assertTrue(dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_BUILD,home.getBlockPos(),-1,home.getBlockState(),null,at)),"The genuinely learned craftsperson fetches the actual original primitive material");var exact=dog.society().workSupplies();
        Consumer<EntityMobGriefingEvent> splice=e->{if(e.getEntity()==dog)dog.setGenome(new Genome(dog.genome().packed(),0));};NeoForge.EVENT_BUS.addListener(splice);
        try {h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.BUILD,at) && ItemStack.matches(exact,dog.society().workSupplies()) && !home.construction().owned(at),"A callback limiting craft capacity invalidates primitive placement before payment while retaining the original parcel");}finally {NeoForge.EVENT_BUS.unregister(splice);}
        h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.DELIVER,home.getBlockPos()) && home.count(Items.CRAFTING_TABLE)==1 && dog.learning().knowledge().progress(Learning.Routine.BUILD)==0,"Safety recovery returns the exact table even after capacity loss, without fictitious architecture");WildFamilyGameTest.clean(c);h.succeed();
    }
    static void ai(GameTestHelper h) {
        var c=setup(h);c.dogs().forEach(d->d.setNoAi(false));long started=h.getLevel().getGameTime();
        h.succeedWhen(()->{var home=c.home();h.assertTrue(home.construction().owned(home.wild().works().workbench()) && home.count(Items.WOODEN_AXE)==1 && home.wild().beds().stream().anyMatch(home.construction()::owned) && c.dogs().stream().anyMatch(d->d.homeNest()!=null && home.wild().beds().contains(d.homeNest()) && home.construction().owned(d.homeNest())),"Ordinary native founders must walk, learn real crafting, make tools and build/claim their first paid Nest without an architect; logs="+home.count(ModItems.GUAVA_LOG.get())+", planks="+home.count(ModItems.GUAVA_PLANKS.get())+", wheat="+home.count(Items.WHEAT)+", jobs="+c.dogs().stream().map(d->String.valueOf(d.society().active())).toList());h.assertTrue(c.dogs().stream().allMatch(d->d.learning().knowledge().progress(Learning.Routine.BUILD)==0 && d.stats().equals(Stats.ZERO)),"The autonomous ordinary family retains its real architectural and physical limits");com.bunnidogs.mod.BunnidogsMod.LOGGER.info("PRIMITIVE_FURNITURE_COMPLETE loaded_ticks={} helper_mind={} guavas_left={}",h.getLevel().getGameTime()-started,c.dogs().getFirst().mind(),home.count(ModItems.GUAVA.get()));WildFamilyGameTest.clean(c);});
    }
}
