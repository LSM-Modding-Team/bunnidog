package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.ModItems;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Original ground item and actual carried return must retain live capacity and the original authority. */
final class CivicHaulGameTest {
    private static ItemEntity log(WildFamilyGameTest.Site c) {
        var dog=c.dogs().getFirst();c.h().assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.CLIP_WOOD,c.home().wild().timber().getFirst()),"This transfer starts with a native original timber harvest");
        var item=c.h().getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(c.home().getBlockPos()).inflate(24),e->e.getItem().is(ModItems.GUAVA_LOG.get())).getFirst();item.setNoPickUpDelay();return item;
    }
    private static boolean collect(WildFamilyGameTest.Site c,ItemEntity item) {
        var dog=c.dogs().getFirst();SocietyGameTest.rest(dog);dog.snapTo(item.position().add(1,0,0));dog.setOnGround(true);
        return dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.GATHER_FIELD,item.blockPosition(),item.getId(),dog.level().getBlockState(item.blockPosition())));
    }
    private static void owner(WildFamilyGameTest.Site c,UUID owner) {
        var home=c.home();var tag=home.saveWithFullMetadata(c.h().getLevel().registryAccess());tag.putString("Owner",owner.toString());home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,c.h().getLevel().registryAccess(),tag));
    }
    static void capacity(GameTestHelper h) {
        var c=PrimitiveFurnitureGameTest.setup(h,false);var dog=c.dogs().getFirst();var item=log(c);var exact=item.getItem().copy();var knowledge=dog.learning().knowledge();var genome=dog.genome();boolean[] called={false};
        Consumer<EntityMobGriefingEvent> change=e->{if(e.getEntity()==dog) {called[0]=true;dog.setGenome(new Genome(genome.packed(),0));}};NeoForge.EVENT_BUS.addListener(change);
        try {h.assertTrue(!collect(c,item) && called[0] && !dog.society().gathers() && item.isAlive() && ItemStack.matches(exact,item.getItem()) && dog.society().cargoEmpty() && dog.learning().knowledge().equals(knowledge),"Losing actual independent hauling capacity in native protection refuses before source debit, cargo or practice");}finally {NeoForge.EVENT_BUS.unregister(change);dog.setGenome(genome);}
        h.assertTrue(collect(c,item) && !item.isAlive() && dog.society().workSupplies().is(ModItems.GUAVA_LOG.get()) && dog.society().workSupplies().getCount()==1 && WildFamilyGameTest.act(c,dog,SocietyWork.Kind.DELIVER,c.home().getBlockPos()) && c.home().count(ModItems.GUAVA_LOG.get())==1 && dog.stats().equals(Stats.ZERO),"The original qualified worker can retry and physically deliver exactly its real original log, with no point grant");WildFamilyGameTest.clean(c);h.succeed();
    }
    static void authority(GameTestHelper h) {
        var c=PrimitiveFurnitureGameTest.setup(h,false);var home=c.home();var dog=c.dogs().getFirst();var item=log(c);var originalOwner=home.owner();var replacement=UUID.randomUUID();var exact=item.getItem().copy();boolean[] called={false};
        Consumer<EntityMobGriefingEvent> transfer=e->{if(e.getEntity()==dog) {called[0]=true;owner(c,replacement);item.setTarget(replacement);}};NeoForge.EVENT_BUS.addListener(transfer);
        try {h.assertTrue(!collect(c,item) && called[0] && item.isAlive() && ItemStack.matches(exact,item.getItem()) && dog.society().cargoEmpty(),"A callback transferring both household and ground-item ownership cannot silently authorize the original pickup under a different owner");}finally {NeoForge.EVENT_BUS.unregister(transfer);owner(c,originalOwner);item.setTarget(originalOwner);}
        h.assertTrue(collect(c,item),"The same real original item remains available after original authority returns");var parcel=dog.society().workSupplies();called[0]=false;
        Consumer<EntityMobGriefingEvent> switchOwner=e->{if(e.getEntity()==dog) {called[0]=true;owner(c,replacement);}};NeoForge.EVENT_BUS.addListener(switchOwner);
        try {h.assertTrue(!WildFamilyGameTest.act(c,dog,SocietyWork.Kind.DELIVER,home.getBlockPos()) && called[0] && ItemStack.matches(parcel,dog.society().workSupplies()) && home.count(ModItems.GUAVA_LOG.get())==0,"A native callback changing the cache owner preserves the original owner's actual walking parcel instead of donating it to the replacement");}finally {NeoForge.EVENT_BUS.unregister(switchOwner);owner(c,originalOwner);}
        h.assertTrue(WildFamilyGameTest.act(c,dog,SocietyWork.Kind.DELIVER,home.getBlockPos()) && home.count(ModItems.GUAVA_LOG.get())==1 && dog.society().cargoEmpty(),"Recovery returns exactly one paid parcel to its original owner");WildFamilyGameTest.clean(c);h.succeed();
    }
}
