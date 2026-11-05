package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.*;

/** Original tool identity, native wear, private keepsakes and callback-time recovery. */
final class CivicToolGameTest {
    private static void ripe(SocietyGameTest.Context c) {c.h().getLevel().setBlock(c.tree().orchard().getFirst(),ModBlocks.GUAVA_COPPICE.get().defaultBlockState().setValue(CoppiceBlock.AGE,7),3);}
    private static boolean prune(SocietyGameTest.Context c) {return SocietyGameTest.act(c,SocietyWork.Kind.PRUNE,c.tree().orchard().getFirst());}
    private static ItemStack named() {var tool=new ItemStack(Items.WOODEN_AXE);tool.set(DataComponents.CUSTOM_NAME,Component.literal("Remembered family axe"));return tool;}
    private static void finish(SocietyGameTest.Context c) {c.worker().discard();c.p().discard();c.h().getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(c.tree().getBlockPos()).inflate(24)).forEach(ItemEntity::discard);c.h().succeed();}
    static void custody(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var home=c.tree();var privateAxe=named();var shared=new ItemStack(Items.WOODEN_AXE);shared.setDamageValue(5);home.setItem(0,privateAxe.copy());home.setItem(1,shared.copy());ripe(c);
        var saved=home.saveWithFullMetadata(h.getLevel().registryAccess());home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        h.assertTrue(CivicTools.axeSlot(home)==1 && prune(c) && ItemStack.matches(home.getItem(0),privateAxe) && home.getItem(1).getDamageValue()==6,"The native saved worn public tool takes one wear; an earlier named keepsake remains exactly untouched");
        ripe(c);shared.setDamageValue(shared.getMaxDamage()-1);home.setItem(1,shared.copy());
        h.assertTrue(prune(c) && CivicTools.axeSlot(home)<0 && home.count(Items.WOODEN_AXE)==1 && ItemStack.matches(home.getItem(0),privateAxe),"The final paid use breaks only the real shared axe; a private axe cannot substitute for it");
        ripe(c);home.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),3));home.put(new ItemStack(Items.STICK,2));WildFamilyGameTest.at(c.worker(),home.getBlockPos());
        h.assertTrue(c.worker().society().offers().stream().noneMatch(j->j.kind()==SocietyWork.Kind.PRUNE) && c.worker().society().offers().stream().anyMatch(j->j.kind()==SocietyWork.Kind.CRAFT && j.index()==CivicRecipes.RecipeId.AXE.ordinal()),"A named axe neither authorizes pruning nor suppresses the actual replacement-tool need");
        h.assertTrue(c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.CRAFT,home.getBlockPos(),CivicRecipes.RecipeId.AXE.ordinal(),home.getBlockState())) && CivicTools.axeSlot(home)>=0 && home.count(ModItems.GUAVA_PLANKS.get())==0 && home.count(Items.STICK)==0 && ItemStack.matches(home.getItem(0),privateAxe),"A real native replacement recipe pays its own inputs while the private tool stays intact");
        h.getLevel().setBlock(home.getBlockPos().east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);ripe(c);var exact=home.getItem(CivicTools.axeSlot(home)).copy();
        h.assertTrue(!prune(c) && ItemStack.matches(exact,home.getItem(CivicTools.axeSlot(home))) && h.getLevel().getBlockState(home.orchard().getFirst()).getValue(CoppiceBlock.AGE)==7,"Powered common stock pauses actual shared-tool use before crop reset or tool wear");finish(c);
    }
    static void callbacks(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var home=c.tree();var worker=c.worker();var shared=new ItemStack(Items.WOODEN_AXE);shared.setDamageValue(5);home.setItem(0,shared.copy());ripe(c);var privateAxe=named();
        Consumer<EntityMobGriefingEvent> swap=e->{if(e.getEntity()==worker) {home.setItem(0,privateAxe.copy());home.setItem(1,shared.copy());}};NeoForge.EVENT_BUS.addListener(swap);
        try {h.assertTrue(!prune(c) && ItemStack.matches(home.getItem(0),privateAxe) && ItemStack.matches(home.getItem(1),shared) && h.getLevel().getBlockState(home.orchard().getFirst()).getValue(CoppiceBlock.AGE)==7,"A callback moving the original tool cannot switch payment to a named replacement or harvest without the proposed tool");}finally {NeoForge.EVENT_BUS.unregister(swap);}
        var genome=worker.genome();Consumer<EntityMobGriefingEvent> limit=e->{if(e.getEntity()==worker)worker.setGenome(new Genome(genome.packed(),0));};NeoForge.EVENT_BUS.addListener(limit);
        try {h.assertTrue(!prune(c) && ItemStack.matches(home.getItem(1),shared),"A protection callback removing harvest capacity refuses before original tool wear");}finally {NeoForge.EVENT_BUS.unregister(limit);worker.setGenome(genome);}
        boolean[] witnessed={false};Consumer<EntityJoinLevelEvent> fill=e->{if(!witnessed[0] && e.getEntity() instanceof ItemEntity item && item.getItem().is(ModItems.GUAVA.get()) && home.site().toString().equals(item.getItem().get(ModItems.FARM_ORIGIN.get()))) {witnessed[0]=CivicTools.axeSlot(home)<0 && ItemStack.matches(home.getItem(0),privateAxe);for(int slot=1;slot<home.getContainerSize();slot++)home.setItem(slot,new ItemStack(Items.COBBLESTONE,64));}};NeoForge.EVENT_BUS.addListener(fill);
        try {h.assertTrue(prune(c) && witnessed[0],"The actual original shared tool stays reserved while native output callbacks run");}finally {NeoForge.EVENT_BUS.unregister(fill);}
        var worn=shared.copy();worn.setDamageValue(6);var recovered=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(home.getBlockPos()).inflate(24),item->ItemStack.matches(item.getItem(),worn));
        h.assertTrue(recovered.size()==1 && ItemStack.matches(home.getItem(0),privateAxe) && CivicTools.axeSlot(home)<0 && !prune(c),"A callback filling the source preserves the private tool and recovers the one actual worn public axe physically; replay cannot duplicate it");finish(c);
    }
}
