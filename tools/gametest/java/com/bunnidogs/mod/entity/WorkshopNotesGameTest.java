package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Paid authored examples, physical book journals and later practical work, never uploaded skill. */
final class WorkshopNotesGameTest {
    private static void at(Bunnidog dog,BlockPos target) {SocietyGameTest.rest(dog);dog.snapTo(Vec3.atBottomCenterOf(target.east()));dog.setOnGround(true);}
    private static boolean craft(SocietyGameTest.Context c,Bunnidog dog,CivicRecipes.RecipeId recipe) {at(dog,c.tree().getBlockPos());return dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.CRAFT,c.tree().getBlockPos(),recipe.ordinal(),c.tree().getBlockState()));}
    private static Bunnidog pupil(SocietyGameTest.Context c,HearthTreeBlockEntity home,int east) {
        var dog=ModEntities.BUNNIDOG.get().create(c.h().getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);dog.setGenome(SocietyGameTest.bright());dog.setStats(Stats.of(0,0,0,40,40));dog.setAge(0);dog.setHealth(dog.getMaxHealth());dog.setNoAi(true);dog.snapTo(Vec3.atBottomCenterOf(home.getBlockPos().east(east)));dog.setOnGround(true);c.h().getLevel().addFreshEntity(dog);
        c.h().assertTrue(dog.society().invite(c.p(),home,false) && dog.learning().knowledge().equals(Learning.EMPTY),"The new reader starts as a real registered resident with no gifted education");return dog;
    }
    private static int prepare(SocietyGameTest.Context c) {
        c.h().assertTrue(c.worker().workshopLore().archiveJob(c.tree())==null,"Old familiar skills and independent capacity alone do not claim a practised recipe");c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get(),2));
        c.h().assertTrue(craft(c,c.worker(),CivicRecipes.RecipeId.PLANKS),"The actual expert pays for the recipe before describing its inputs/output");
        var blank=new ItemStack(Items.WRITABLE_BOOK);blank.set(DataComponents.CUSTOM_NAME,Component.literal("The family workshop"));c.tree().put(blank);at(c.worker(),c.tree().getBlockPos());
        var job=c.worker().workshopLore().archiveJob(c.tree());c.h().assertTrue(job!=null,"One completed native recipe survives whole-animal save/load as an eligible real example");return job.index();
    }
    private static ItemStack record(SocietyGameTest.Context c,int slot) {
        at(c.worker(),c.tree().getBlockPos());c.h().assertTrue(c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.RECORD_CRAFT,c.tree().getBlockPos(),slot,c.tree().getBlockState())),"One original blank Book and Quill becomes the expert's physical recipe notes");return c.tree().getItem(slot).copy();
    }
    private static ItemStack roundTrip(GameTestHelper h,ItemStack book) {var ops=RegistryOps.create(NbtOps.INSTANCE,h.getLevel().registryAccess());var encoded=ItemStack.CODEC.encodeStart(ops,book).getOrThrow();var restored=ItemStack.CODEC.parse(ops,encoded).getOrThrow();h.assertTrue(encoded.equals(ItemStack.CODEC.encodeStart(ops,restored).getOrThrow()),"Native item/component serialization preserves the exact physical book");return restored;}
    private static void finish(SocietyGameTest.Context c) {c.tree().residents().forEach(Bunnidog::discard);c.p().discard();c.h().succeed();}
    static void recipe(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);int slot=prepare(c);for(int i=0;i<c.tree().getContainerSize();i++)if(c.tree().getItem(i).isEmpty())c.tree().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        var book=record(c,slot);var restored=roundTrip(h,book);var notes=CraftNotes.read(restored,h.getLevel()).orElseThrow();
        h.assertTrue(restored.getHoverName().getString().equals("The family workshop") && notes.hearth().equals(c.tree().site()) && notes.owner().equals(c.p().getUUID()) && notes.author().equals(c.worker().getUUID())
                && notes.recipe()==CivicRecipes.RecipeId.PLANKS && notes.ingredients().size()==1 && notes.ingredients().getFirst().is(ModItems.GUAVA_LOG.get()) && notes.product().is(ModItems.GUAVA_PLANKS.get()) && notes.product().getCount()==4,"Even in a full store, one original slot commits the named book with the exact actual paid recipe and original authority");
        c.worker().discard();var d=pupil(c,c.tree(),3);
        h.assertTrue(d.workshopLore().guides(c.tree(),CivicRecipes.RecipeId.PLANKS) && !d.society().canCraft(CivicRecipes.RecipeId.STICKS) && d.learning().knowledge().equals(Learning.EMPTY),"A book allows only its recorded practical attempt and grants no skill merely by being read");
        h.assertTrue(craft(c,d,CivicRecipes.RecipeId.PLANKS) && c.tree().count(ModItems.GUAVA_LOG.get())==0 && c.tree().count(ModItems.GUAVA_PLANKS.get())==8 && d.learning().knowledge().knows(Learning.Routine.CRAFT) && d.learning().knowledge().progress(Learning.Routine.GARDEN)==0 && ItemStack.matches(book,c.tree().getItem(slot)),"After expert loss the beginner learns only through its own paid native recipe; the book, original cost and unrelated skills stay exact");
        c.tree().setItem(slot,ItemStack.EMPTY);h.assertTrue(craft(c,d,CivicRecipes.RecipeId.STICKS) && c.tree().count(Items.STICK)==4,"Personally learned independent work remains possible after the physical notes are removed");finish(c);
    }
    static void safety(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);int slot=prepare(c);var blank=c.tree().getItem(slot).copy();
        Consumer<EntityMobGriefingEvent> denied=e->{if(e.getEntity()==c.worker()) {h.assertTrue(!c.worker().workshopLore().record(c.tree(),slot),"A reentrant archive request cannot spend the same book twice");e.setCanGrief(false);}};NeoForge.EVENT_BUS.addListener(denied);
        try {h.assertTrue(!c.worker().workshopLore().record(c.tree(),slot) && ItemStack.matches(blank,c.tree().getItem(slot)),"Native protection leaves the exact blank cover and stock untouched");}finally {NeoForge.EVENT_BUS.unregister(denied);}
        var book=record(c,slot);c.worker().discard();var d=pupil(c,c.tree(),3);int logs=c.tree().count(ModItems.GUAVA_LOG.get());
        Consumer<EntityMobGriefingEvent> removed=e->{if(e.getEntity()==d)c.tree().setItem(slot,ItemStack.EMPTY);};NeoForge.EVENT_BUS.addListener(removed);
        try {h.assertTrue(!craft(c,d,CivicRecipes.RecipeId.PLANKS) && c.tree().count(ModItems.GUAVA_LOG.get())==logs && d.learning().knowledge().equals(Learning.EMPTY),"A callback removing the original instructions prevents recipe payment and imagined learning");}finally {NeoForge.EVENT_BUS.unregister(removed);}
        c.tree().setItem(slot,book);h.assertTrue(craft(c,d,CivicRecipes.RecipeId.PLANKS) && d.learning().knowledge().knows(Learning.Routine.CRAFT),"Restored actual instructions allow a new legitimate paid attempt");finish(c);
    }
    static void boundaries(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);int slot=prepare(c);var book=record(c,slot);c.worker().setWorkPaused(true);var d=pupil(c,c.tree(),3);var supported=Genome.DEFAULT.with(CognitiveGene.MEMORY,0,0);d.setGenome(supported);
        h.assertTrue(CapabilityRules.potential(d.mind(),CapabilityRules.Task.CRAFTING)==CapabilityRules.Potential.SUPPORTED && !d.society().canCraft(CivicRecipes.RecipeId.PLANKS),"A supported mind still needs an actual local instructor; a book cannot make it independent");d.setGenome(new Genome(d.genome().packed(),0));
        h.assertTrue(!d.society().canCraft(CivicRecipes.RecipeId.PLANKS),"Limited cognition cannot be bypassed by a physical recipe");d.setGenome(SocietyGameTest.bright());d.setStats(Stats.ZERO);
        h.assertTrue(!d.society().canCraft(CivicRecipes.RecipeId.PLANKS),"A player household's physical Bloom readiness remains required");d.setStats(Stats.of(0,0,0,40,40));
        h.getLevel().setBlock(c.tree().getBlockPos().above(),net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState(),3);h.assertTrue(!d.society().canCraft(CivicRecipes.RecipeId.PLANKS),"Powered common stock cannot provide a book-guided work opportunity");h.getLevel().removeBlock(c.tree().getBlockPos().above(),false);
        h.assertTrue(d.society().canCraft(CivicRecipes.RecipeId.PLANKS) && d.learning().knowledge().equals(Learning.EMPTY),"Restoring the real book opportunity still grants no skill by itself");
        String opaque="{\"version\":2,\"future\":\"retained\"}";var future=book.copy();future.set(ModItems.CRAFT_NOTES_DATA.get(),opaque);c.tree().setItem(slot,roundTrip(h,future));
        h.assertTrue(CraftNotes.read(c.tree().getItem(slot),h.getLevel()).isEmpty() && !d.society().canCraft(CivicRecipes.RecipeId.PLANKS) && opaque.equals(c.tree().getItem(slot).get(ModItems.CRAFT_NOTES_DATA.get())) && d.learning().knowledge().equals(Learning.EMPTY),"An unreadable future book stays exact opaque stock and supplies no knowledge");c.tree().setItem(slot,book);
        var otherAt=c.tree().getBlockPos().west(7);h.getLevel().setBlock(otherAt,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);var other=(HearthTreeBlockEntity)h.getLevel().getBlockEntity(otherAt);other.found(c.p());other.put(book);var outsider=pupil(c,other,1);
        h.assertTrue(!outsider.workshopLore().guides(other,CivicRecipes.RecipeId.PLANKS),"A copied book in another Hearth grants no instruction authority even with the same keeper");outsider.discard();
        c.worker().setWorkPaused(false);at(c.worker(),c.tree().getBlockPos());c.tree().setItem(slot,ItemStack.EMPTY);for(int i=0;i<8;i++)c.tree().setItem(i,new ItemStack(ModItems.CRAFT_NOTES.get()));c.tree().setItem(8,new ItemStack(Items.WRITABLE_BOOK));
        h.assertTrue(c.worker().workshopLore().archiveJob(c.tree())==null && !c.worker().workshopLore().record(c.tree(),8) && c.tree().getItem(8).is(Items.WRITABLE_BOOK),"Eight physical records, including unreadable ones, bound archival stock without consuming another book");for(int i=0;i<8;i++)c.tree().setItem(i,ItemStack.EMPTY);
        var journal=new ItemStack(Items.WRITABLE_BOOK);journal.set(DataComponents.WRITABLE_BOOK_CONTENT,new WritableBookContent(List.of(Filterable.passThrough("Private journal"))));c.tree().setItem(8,journal.copy());
        h.assertTrue(!c.worker().workshopLore().record(c.tree(),8) && ItemStack.matches(journal,c.tree().getItem(8)),"A resident never overwrites an existing private journal");
        var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());c.worker().saveWithoutId(saved);var body=saved.buildResult();var futureProof=new net.minecraft.nbt.ListTag();futureProof.add(net.minecraft.nbt.StringTag.valueOf(opaque));body.put("CraftProven",futureProof);c.worker().load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),body));c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get()));
        h.assertTrue(craft(c,c.worker(),CivicRecipes.RecipeId.PLANKS),"An old unknown example does not prevent legitimate current paid work");var after=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());c.worker().saveWithoutId(after);
        h.assertTrue(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),after.buildResult()).read("CraftProven",com.mojang.serialization.Codec.STRING.listOf()).orElse(List.of()).contains(opaque),"Native body reload and a new actual recipe preserve opaque future examples exactly");finish(c);
    }
    static void walking(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);int slot=prepare(c);c.tree().take(ModItems.GUAVA_LOG.get(),1);c.tree().take(ModItems.GUAVA_PLANKS.get(),4);c.worker().snapTo(Vec3.atBottomCenterOf(c.tree().getBlockPos().east(6)));c.worker().setNoAi(false);c.p().snapTo(c.p().position().add(20,0,0));
        var reader=new Bunnidog[1];var start=new Vec3[1];
        h.startSequence().thenWaitUntil(()->h.assertTrue(c.tree().getItem(slot).is(ModItems.CRAFT_NOTES.get()),"The native expert goal walks to its Hearth and writes a real paid book"))
                .thenExecute(()->{c.worker().discard();reader[0]=pupil(c,c.tree(),7);start[0]=reader[0].position();c.tree().put(new ItemStack(ModItems.GUAVA_LOG.get(),2));reader[0].setNoAi(false);})
                .thenWaitUntil(()->h.assertTrue(reader[0].learning().knowledge().knows(Learning.Routine.CRAFT) && reader[0].position().distanceToSqr(start[0])>.25 && c.tree().count(ModItems.GUAVA_LOG.get())<2 && c.tree().count(ModItems.GUAVA_PLANKS.get())>=4 && reader[0].learning().knowledge().progress(Learning.Routine.GARDEN)==0,"After expert loss a native beginner walks to the physical notes and learns through its actual paid recipe"))
                .thenExecute(()->{c.tree().residents().forEach(Bunnidog::discard);c.p().discard();}).thenSucceed();
    }
}
