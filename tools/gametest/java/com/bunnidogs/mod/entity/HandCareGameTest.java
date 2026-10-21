package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

/** Actual hands distinguish guava care from love, including paused workers and callback refills. */
public final class HandCareGameTest {
    private HandCareGameTest() {}
    private static CompoundTag saved(Bunnidog dog) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.level().registryAccess()); dog.saveWithoutId(out); return out.buildResult();
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel(); var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,2,3.5F);
        var stats = Stats.of(5,6,7,8,9); dog.setGenome(Genome.DEFAULT); dog.setStats(stats); dog.setAge(1234); dog.setHealth(dog.getMaxHealth()-4);
        dog.setParents(Genome.DEFAULT,Genome.DEFAULT); dog.applyEarTag(); var identity = dog.getUUID(); var tag = dog.earTag();
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_HOE)); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        dog.ateForage(0); dog.finishedGardening(); int foodRest = dog.forageWait(), workRest = dog.gardeningWait();
        var guavas = new ItemStack(ModItems.GUAVA.get(),3); guavas.set(DataComponents.CUSTOM_NAME,Component.literal("Care guavas")); player.setItemInHand(InteractionHand.OFF_HAND,guavas);
        float health = dog.getHealth(); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(guavas.getCount() == 2 && guavas.getHoverName().getString().equals("Care guavas") && dog.getHealth() == health+com.bunnidogs.mod.genetics.Combat.mealHealing(dog.getMaxHealth())
                && dog.getAge() == 1234 && !dog.isInLove() && dog.workPaused() && dog.forageWait() == foodRest && dog.gardeningWait() == workRest,
                "Sneak off-hand care must pay one exact plain guava, heal its proportional meal and retain pause/breeding cooldown/recoveries");
        dog.setAge(0); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(guavas.getCount() == 1 && dog.getHealth() == dog.getMaxHealth() && !dog.isInLove() && !saved(dog).contains("LoveTierTicks"),"Even a breed-ready adult must receive care without love or a quality memory");
        player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(guavas.getCount() == 1 && !dog.isInLove(),"Healthy adults keep a medical guava without falling through to breeding");
        var golden = new ItemStack(ModItems.GOLDEN_GUAVA.get(),2); golden.set(DataComponents.CUSTOM_NAME,Component.literal("Keep for breeding")); player.setItemInHand(InteractionHand.OFF_HAND,golden); dog.setHealth(dog.getMaxHealth()-4);
        health = dog.getHealth(); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(golden.getCount() == 2 && dog.getHealth() == health && !dog.isInLove(),"Sneak care keeps upgraded guavas without healing or breeding");
        dog.setAge(-1000); var pupFood = new ItemStack(ModItems.GUAVA.get(),2); player.setItemInHand(InteractionHand.OFF_HAND,pupFood); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(pupFood.getCount() == 1 && dog.getHealth() == health+com.bunnidogs.mod.genetics.Combat.mealHealing(dog.getMaxHealth()) && dog.getAge() > -1000 && dog.isBaby() && !dog.isInLove(),"One plain medical guava heals and grows a pup through the existing hand-food rule");
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()-4); player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); player.setItemInHand(InteractionHand.OFF_HAND,guavas);
        health = dog.getHealth(); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(guavas.getCount() == 1 && player.getOffhandItem() == guavas && dog.getHealth() == health+com.bunnidogs.mod.genetics.Combat.mealHealing(dog.getMaxHealth()) && !dog.isInLove(),"Creative off-hand care changes the real animal while preserving the actual guava");
        player.setShiftKeyDown(false); player.setItemInHand(InteractionHand.OFF_HAND,golden); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(dog.isInLove() && saved(dog).getIntOr("LoveTier",-1) == 1,"Normal upgraded-guava feeding still starts breeding with its original tier");
        int love = dog.getInLoveTime(), memory = saved(dog).getIntOr("LoveTierTicks",0); dog.setHealth(dog.getMaxHealth()-4); player.setShiftKeyDown(true); player.setItemInHand(InteractionHand.OFF_HAND,guavas);
        player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(dog.getInLoveTime() == love && saved(dog).getIntOr("LoveTier",-1) == 1 && saved(dog).getIntOr("LoveTierTicks",0) == memory,"Care preserves an already planned love tier and its exact remaining time");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); var last = new ItemStack(ModItems.GUAVA.get()); player.setItemInHand(InteractionHand.OFF_HAND,last); dog.setHealth(dog.getMaxHealth()-4);
        boolean[] paidFirst = {false}; Consumer<LivingHealEvent> refill = event -> {
            if (event.getEntity() == dog) { paidFirst[0] = last.isEmpty(); player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.DIAMOND,3)); }
        };
        NeoForge.EVENT_BUS.addListener(refill);
        try { player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO); } finally { NeoForge.EVENT_BUS.unregister(refill); }
        h.assertTrue(paidFirst[0] && last.isEmpty() && player.getOffhandItem().is(Items.DIAMOND) && player.getOffhandItem().getCount() == 3
                && dog.genome().equals(Genome.DEFAULT) && dog.stats().equals(stats) && dog.getUUID().equals(identity) && dog.earTag().equals(tag)
                && dog.forageWait() == foodRest && dog.gardeningWait() == workRest,"A native heal callback must see the paid food, retain replacement hand stock and leave biology/rests intact");
        var apex = ModEntities.APEX.get().create(level,EntitySpawnReason.COMMAND); var refused = new ItemStack(ModItems.GUAVA.get()); player.setItemInHand(InteractionHand.OFF_HAND,refused); float bossHealth = apex.getHealth();
        player.interactOn(apex,InteractionHand.OFF_HAND,Vec3.ZERO); h.assertTrue(refused.getCount() == 1 && apex.getHealth() == bossHealth && !apex.isInLove(),"The Apex still refuses all direct care food");
    }
}
