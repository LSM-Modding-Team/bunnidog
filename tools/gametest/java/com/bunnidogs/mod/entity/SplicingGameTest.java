package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Splicing;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.GeneSplicerItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class SplicingGameTest {
    private SplicingGameTest() {}
    public static void run(GameTestHelper h) {
        for (Gene gene : Gene.values()) {
            int allele = gene.alleleCount()-1;
            var donor = Genome.DEFAULT.with(gene,0,allele).with(gene,1,allele);
            var change = Splicing.plan(Genome.DEFAULT,donor,gene).orElseThrow();
            h.assertTrue(change.genome().equals(Genome.DEFAULT.with(gene,0,allele)) && !change.genome().isHomozygous(gene),
                    "Splicing must change exactly one allele and keep the other six genes intact");
            h.assertTrue(Splicing.plan(change.genome(),donor,gene).isEmpty(),"Repeated splicing must not bypass breeding to create a homozygote");
        }
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,1,3);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setStats(Stats.of(7,8,9,10,11));
        dog.setHealth(dog.getMaxHealth()*.75F);
        var stats = dog.stats();
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var tool = new ItemStack(ModItems.GENE_SPLICER.get());
        tool.set(ModItems.SPLICE_GENE.get(),Gene.BUILD.ordinal());
        player.setItemInHand(InteractionHand.MAIN_HAND,tool);
        int woolly = Gene.BUILD.alleleNamed("woolly").id();
        var donor = Genome.DEFAULT.with(Gene.BUILD,0,woolly).with(Gene.BUILD,1,woolly);
        player.setItemInHand(InteractionHand.OFF_HAND,TissueSampleItem.create(donor,Stats.of(999,999,999,999,999)));
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.genome().equals(Genome.DEFAULT) && tool.getDamageValue() == 0 && dog.splicingWait() == 0
                && !player.getOffhandItem().isEmpty(),"Missing Mutagen must refuse before consuming or editing anything");
        player.getInventory().add(new ItemStack(ModItems.MUTAGEN.get(),2));
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.genome().equals(Genome.DEFAULT.with(Gene.BUILD,0,woolly)) && dog.stats().equals(stats),
                "A splice inserts only the donor allele; its high bred stats must not be copied");
        h.assertTrue(Math.abs(dog.getHealth()/dog.getMaxHealth()-.75F) < .001F,"Changing build must preserve relative health");
        h.assertTrue(tool.getDamageValue() == 1 && player.getOffhandItem().isEmpty() && dog.splicingWait() == Splicing.RECOVERY_TICKS,
                "One successful splice consumes a sample, one tool use and starts recovery");
        int mutagen = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var item = player.getInventory().getItem(i);
            if (item.is(ModItems.MUTAGEN.get())) mutagen += item.getCount();
        }
        h.assertTrue(mutagen == 1,"Exactly one Mutagen must be consumed");
        int snow = Gene.COAT.alleleNamed("snow").id();
        player.setItemInHand(InteractionHand.OFF_HAND,TissueSampleItem.create(Genome.DEFAULT.with(Gene.COAT,0,snow).with(Gene.COAT,1,snow),Stats.ZERO));
        tool.set(ModItems.SPLICE_GENE.get(),Gene.COAT.ordinal());
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(dog.genome().alleleId(Gene.COAT,0) == 0 && !player.getOffhandItem().isEmpty() && tool.getDamageValue() == 1,
                "Recovery must refuse a different valid donor without cost");
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(out); var saved = out.buildResult();
        var restored = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        h.assertTrue(restored.splicingWait() == Splicing.RECOVERY_TICKS && restored.genome().equals(dog.genome()),"Edited genome and recovery must survive save/load");
        saved.remove("SpliceWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        tool.set(ModItems.SPLICE_GENE.get(),Gene.BUILD.ordinal());
        player.setItemInHand(InteractionHand.OFF_HAND,TissueSampleItem.create(donor,Stats.ZERO));
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(!dog.genome().isHomozygous(Gene.BUILD) && dog.splicingWait() == 0 && tool.getDamageValue() == 1
                && !player.getOffhandItem().isEmpty(),"Already-carried refusal must protect breeding and keep materials");
        player.setShiftKeyDown(true);
        tool.getItem().use(level,player,InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        h.assertTrue(GeneSplicerItem.selected(tool) == Gene.TEMPERAMENT,"Sneak-use must cycle the selected gene");
        var itemOut = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        itemOut.store("Tool",ItemStack.CODEC,tool);
        var savedTool = TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),itemOut.buildResult()).read("Tool",ItemStack.CODEC).orElseThrow();
        h.assertTrue(GeneSplicerItem.selected(savedTool) == Gene.TEMPERAMENT && savedTool.getDamageValue() == 1,
                "Tool selection and durability must persist in its component");
    }
}
