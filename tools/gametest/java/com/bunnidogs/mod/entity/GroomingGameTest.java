package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class GroomingGameTest {
    private GroomingGameTest() {}
    private static int whiskers(Player player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.WHISKER.get())) count += stack.getCount();
        }
        return count;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 3, 1, 3);
        dog.setGenome(Genome.DEFAULT); dog.setAge(0); dog.setStats(Stats.of(0,0,0,9,0));
        dog.setHealth(dog.getMaxHealth());
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var brush = new ItemStack(Items.BRUSH);
        player.setItemInHand(InteractionHand.MAIN_HAND, brush);
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(whiskers(player) == 0 && brush.getDamageValue() == 0 && dog.groomingWait() == 0,
                "Insufficient bred Senses must refuse without wear or recovery");
        dog.setStats(Stats.of(0,0,0,10,0)); dog.setAge(-100);
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(whiskers(player) == 0 && brush.getDamageValue() == 0, "Pups must keep their whiskers");
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()*.25F);
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(whiskers(player) == 0 && dog.groomingWait() == 0, "Injured adults must recover before grooming");
        dog.setHealth(dog.getMaxHealth()); float health = dog.getHealth();
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(whiskers(player) == 1 && brush.getDamageValue() == 1 && dog.groomingWait() == Grooming.RECOVERY_TICKS,
                "Native brushing must yield one whisker, wear the Brush once and start ten-minute recovery");
        h.assertTrue(dog.getHealth() == health && dog.getAge() == 0 && dog.genome().equals(Genome.DEFAULT)
                && dog.stats().equals(Stats.of(0,0,0,10,0)), "Grooming must preserve the animal");
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(whiskers(player) == 1 && brush.getDamageValue() == 1, "Repeated grooming must refuse without costs");
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        dog.saveWithoutId(out); var saved = out.buildResult();
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        h.assertTrue(dog.groomingWait() == Grooming.RECOVERY_TICKS, "Grooming recovery must survive save/load");
        saved.remove("GroomWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        h.assertTrue(dog.groomingWait() == 0, "Legacy animals must be ready for grooming");
        brush.setDamageValue(brush.getMaxDamage()-1);
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(player.getMainHandItem().isEmpty() && whiskers(player) == 2, "The last brush charge must produce one final whisker and break normally");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        var creative = h.makeMockPlayer(GameType.CREATIVE);
        GameType.CREATIVE.updatePlayerAbilities(creative.getAbilities());
        var creativeBrush = new ItemStack(Items.BRUSH);
        creative.setItemInHand(InteractionHand.MAIN_HAND, creativeBrush);
        creative.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(whiskers(creative) == 1 && creativeBrush.getDamageValue() == 0 && dog.groomingWait() == Grooming.RECOVERY_TICKS,
                "Creative must preserve the brush but keep animal recovery");
    }
}
