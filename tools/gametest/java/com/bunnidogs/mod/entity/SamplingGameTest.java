package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Stats;
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

public final class SamplingGameTest {
    private SamplingGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2,1,2);
        dog.setAge(-12000);
        dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,11).with(Gene.DIET,1,11));
        dog.setStats(Stats.of(7,8,9,10,11));
        dog.setHealth(dog.getMaxHealth());
        var genome = dog.genome(); var stats = dog.stats();
        float health = dog.getHealth(); int age = dog.getAge();
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var syringe = new ItemStack(ModItems.SYRINGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND,syringe);
        h.assertTrue(player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction(),"Syringe must work through native entity interaction");
        ItemStack sample = ItemStack.EMPTY;
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.TISSUE_SAMPLE.get())) { sample = stack; count += stack.getCount(); }
        }
        var tissue = TissueSampleItem.read(sample).orElseThrow();
        h.assertTrue(count == 1 && tissue.genome().equals(genome) && tissue.stats().equals(stats),"A live pup's exact seven-gene genome and bred stats must be sampled");
        h.assertTrue(dog.isAlive() && dog.getHealth() == health && dog.getAge() == age && dog.genome().equals(genome)
                && dog.stats().equals(stats),"Sampling must preserve life, health, age and breeding data");
        h.assertTrue(syringe.getDamageValue() == 1 && dog.samplingWait() == Sampling.RECOVERY_TICKS,"A successful sample uses one charge and starts animal recovery");
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(syringe.getDamageValue() == 1,"Recovery refusal must not wear the tool");
        int after = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.TISSUE_SAMPLE.get())) after += stack.getCount();
        }
        h.assertTrue(after == 1,"Recovery must prevent duplicate samples");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(output);
        var saved = output.buildResult();
        var restored = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        h.assertTrue(restored.samplingWait() == Sampling.RECOVERY_TICKS,"Saving and loading must retain recovery");
        saved.remove("SampleWait");
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        h.assertTrue(restored.samplingWait() == 0,"Legacy animals without a sampling timer must be ready");
        restored.setHealth(restored.getMaxHealth()/2-.1F);
        player.interactOn(restored,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(restored.samplingWait() == 0 && syringe.getDamageValue() == 1,"Injured animals must refuse without tool wear or starting recovery");
        restored.setHealth(restored.getMaxHealth()/2);
        syringe.setDamageValue(Sampling.TOOL_USES-1);
        player.interactOn(restored,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(player.getMainHandItem().isEmpty() && restored.samplingWait() > 0,"The last charge must yield a sample and break the syringe normally");
        var creative = h.makeMockPlayer(GameType.CREATIVE);
        GameType.CREATIVE.updatePlayerAbilities(creative.getAbilities());
        var fresh = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4,1,4);
        fresh.setHealth(fresh.getMaxHealth());
        creative.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.SYRINGE.get()));
        creative.interactOn(fresh,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(creative.getMainHandItem().getDamageValue() == 0 && fresh.samplingWait() > 0,"Creative preserves the syringe while keeping animal recovery");
    }
}
