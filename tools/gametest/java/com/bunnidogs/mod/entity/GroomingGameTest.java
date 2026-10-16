package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class GroomingGameTest {
    private GroomingGameTest() {}
    private static int whiskers(Player player) {
        return count(player,ModItems.WHISKER.get());
    }
    private static int count(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
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
        dust(h);
    }
    private static void dust(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),5,1,3);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var brush = new ItemStack(Items.BRUSH);
        player.setItemInHand(InteractionHand.MAIN_HAND,brush);
        int glow = Gene.COAT.alleleNamed("glow").id(), starborn = Gene.FERTILITY.alleleNamed("starborn").id();
        dog.setAge(0); dog.setStats(Stats.ZERO); dog.setGenome(Genome.DEFAULT.with(Gene.COAT,1,glow));
        dog.setHealth(dog.getMaxHealth());
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(count(player,Items.GLOWSTONE_DUST) == 0 && dog.groomingWait() == 0 && brush.getDamageValue() == 0,
                "Hidden glow must not yield dust or spend wear/recovery");
        dog.setGenome(dog.genome().with(Gene.COAT,0,glow));
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(count(player,Items.GLOWSTONE_DUST) == 1 && whiskers(player) == 0
                && dog.groomingWait() == Grooming.RECOVERY_TICKS && brush.getDamageValue() == 1,
                "Expressed glow must yield one dust without requiring bred Senses");
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(count(player,Items.GLOWSTONE_DUST) == 1 && brush.getDamageValue() == 1,"Dust shares the ordinary grooming recovery");

        var star = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),5,1,5);
        var starGenome = Genome.DEFAULT.with(Gene.FERTILITY,0,starborn).with(Gene.FERTILITY,1,starborn);
        star.setGenome(starGenome.with(Gene.FERTILITY,1,Gene.FERTILITY.alleleNamed("swarm").id()));
        star.setStats(Stats.of(0,0,0,0,20)); star.setAge(0); star.setHealth(star.getMaxHealth());
        player.interactOn(star,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(count(player,ModItems.STARLIT_DUST.get()) == 0 && star.groomingWait() == 0,"A hidden starborn allele must not yield dust");
        star.setGenome(starGenome); star.setStats(Stats.of(0,0,0,0,19));
        player.interactOn(star,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(count(player,ModItems.STARLIT_DUST.get()) == 0 && star.groomingWait() == 0,"Starlit care must require Bloom +20");
        star.setStats(Stats.of(0,0,0,0,20));
        player.interactOn(star,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(count(player,ModItems.STARLIT_DUST.get()) == 1 && brush.getDamageValue() == 2,
                "The Bloom threshold must yield exactly one Starlit Dust with one tool wear");

        var mixed = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,1,5);
        var mixedGenome = starGenome.with(Gene.COAT,0,glow).with(Gene.COAT,1,glow);
        mixed.setGenome(mixedGenome); mixed.setStats(Stats.of(0,0,0,10,20)); mixed.setAge(0); mixed.setHealth(mixed.getMaxHealth());
        var full = h.makeMockPlayer(GameType.SURVIVAL);
        full.setPos(mixed.position());
        for (int i = 0; i < full.getInventory().getContainerSize(); i++) full.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        var fullBrush = new ItemStack(Items.BRUSH); full.setItemInHand(InteractionHand.MAIN_HAND,fullBrush);
        float health = mixed.getHealth();
        full.interactOn(mixed,InteractionHand.MAIN_HAND,Vec3.ZERO);
        var drops = level.getEntitiesOfClass(ItemEntity.class,mixed.getBoundingBox().inflate(3));
        for (var item : new Item[]{ModItems.WHISKER.get(),Items.GLOWSTONE_DUST,ModItems.STARLIT_DUST.get()}) {
            h.assertTrue(drops.stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                    "Each qualifying product must drop exactly once when the inventory is full: " + item);
        }
        h.assertTrue(fullBrush.getDamageValue() == 1 && mixed.groomingWait() == Grooming.RECOVERY_TICKS
                && mixed.getHealth() == health && mixed.genome().equals(mixedGenome) && mixed.stats().equals(Stats.of(0,0,0,10,20)),
                "Combined products must share one wear/recovery and preserve the living line");
    }
}
