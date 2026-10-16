package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Fleece;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class FleeceGameTest {
    private FleeceGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,1,3);
        dog.setGenome(Genome.DEFAULT); dog.setStats(Stats.ZERO); dog.setHealth(dog.getMaxHealth());
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var shears = new ItemStack(Items.SHEARS);
        player.setItemInHand(InteractionHand.MAIN_HAND,shears);
        h.assertTrue(!dog.isShearable(player,shears,level,dog.blockPosition()),"Ordinary builds must not yield fleece");
        int woolly = Gene.BUILD.alleleNamed("woolly").id();
        dog.setGenome(Genome.DEFAULT.with(Gene.BUILD,0,woolly).with(Gene.BUILD,1,woolly));
        dog.setHealth(dog.getMaxHealth()); dog.setAge(-1000);
        h.assertTrue(!dog.isShearable(player,shears,level,dog.blockPosition()),"Pups must keep their fleece");
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()/2-.1F);
        h.assertTrue(!dog.isShearable(player,shears,level,dog.blockPosition()),"An injured woolly dog must recover first");
        dog.setHealth(dog.getMaxHealth());
        float health = dog.getHealth();
        h.assertTrue(player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction(),"Vanilla shears must gather woolly fleece through IShearable");
        int wool = level.getEntitiesOfClass(ItemEntity.class,dog.getBoundingBox().inflate(3)).stream()
                .filter(e -> e.getItem().is(Items.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE))).mapToInt(e -> e.getItem().getCount()).sum();
        h.assertTrue(wool == Fleece.COUNT && shears.getDamageValue() == 1 && dog.getHealth() == health,"One use must yield two wool, wear shears once and preserve health");
        h.assertTrue(dog.fleeceWait() == Fleece.REGROW_TICKS && !dog.isShearable(player,shears,level,dog.blockPosition()),"Fleece recovery must prevent immediate repeat harvests");
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(shears.getDamageValue() == 1,"An unready fleece must not wear shears");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(output);
        var saved = output.buildResult();
        var restored = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        h.assertTrue(restored.fleeceWait() == Fleece.REGROW_TICKS && !restored.isShearable(player,shears,level,restored.blockPosition()),"Save/load must retain recovery and the synced readiness state");
        saved.remove("FleeceWait");
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        h.assertTrue(restored.isShearable(player,shears,level,restored.blockPosition()),"Legacy woolly adults must start ready");
        saved.putInt("FleeceWait",1);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        restored.setUUID(java.util.UUID.randomUUID());
        level.addFreshEntity(restored);
        h.runAfterDelay(3, () -> {
            h.assertTrue(restored.fleeceWait() == 0 && restored.isShearable(player,shears,level,restored.blockPosition()),"A server tick must regrow fleece and restore readiness");
            h.succeed();
        });
    }
}
