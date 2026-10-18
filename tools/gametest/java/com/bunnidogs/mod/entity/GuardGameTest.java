package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Guarding;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.item.CalipersItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class GuardGameTest {
    private GuardGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        var home = h.absolutePos(new BlockPos(1,1,1));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2.5F,1,3.5F);
        dog.setGenome(Genome.DEFAULT); dog.setAge(0);
        var player = h.makeMockServerPlayerInLevel();
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setShiftKeyDown(true);
        var bone = new ItemStack(Items.BONE,3); bone.set(DataComponents.CUSTOM_NAME,Component.literal("Guard command"));
        player.setItemInHand(InteractionHand.OFF_HAND,bone);
        int feral = Gene.TEMPERAMENT.alleleNamed("feral").id();
        var genome = Genome.DEFAULT.with(Gene.TEMPERAMENT,0,feral).with(Gene.TEMPERAMENT,1,feral);
        h.runAfterDelay(2,() -> {
            h.assertTrue(!dog.guardEnabled() && dog.guardingStatus() == Guarding.Status.DISABLED,
                    "Existing animals must begin with guarding disabled");
            dog.mobInteract(player,InteractionHand.OFF_HAND);
            h.assertTrue(!dog.guardEnabled(),"An ordinary loyal animal must refuse a guard command");
            dog.setGenome(genome); dog.setAge(-100);
            dog.mobInteract(player,InteractionHand.OFF_HAND);
            h.assertTrue(!dog.guardEnabled(),"Pups must refuse guard commands");
            dog.setAge(0); dog.mobInteract(player,InteractionHand.OFF_HAND);
            h.assertTrue(!dog.guardEnabled(),"A homeless feral must refuse a guard command");
            h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"A guard must use a real claimed Nest");
            player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
            dog.mobInteract(player,InteractionHand.OFF_HAND);
            h.assertTrue(!dog.guardEnabled(),"Adventure permissions must prevent changing guarding");
            player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            dog.mobInteract(player,InteractionHand.OFF_HAND);
            h.assertTrue(dog.guardEnabled() && dog.guardingStatus() == Guarding.Status.READY && bone.getCount() == 3
                    && bone.getHoverName().getString().equals("Guard command"),"Off-hand commands must enable without spending or rewriting a bone");
            var cow = h.spawnWithNoFreeWill(EntityTypes.COW,3,1,4);
            var creeper = h.spawnWithNoFreeWill(EntityTypes.CREEPER,4,1,3);
            h.assertTrue(!new GuardGoal(dog).canUse() && dog.getTarget() == null,
                    "Guards must leave passive neighbours and unselected explosive mobs alone");
            creeper.discard();
            var husk = h.spawnWithNoFreeWill(EntityTypes.HUSK,6.5F,1,3.5F);
            var goal = new GuardGoal(dog);
            h.assertTrue(goal.canUse() && dog.getTarget() == null,"Visible selected intruders need a safe route without pre-start target mutation");
            goal.start();
            h.assertTrue(dog.getTarget() == husk && dog.guardingStatus() == Guarding.Status.ACTIVE
                    && home.equals(dog.homeNest()),"A guard owns its intruder target while retaining its home");
            dog.setLeashedTo(player,false);
            h.assertTrue(!goal.canContinueToUse(),"Player leads must interrupt an active guard job");
            goal.stop();
            h.assertTrue(dog.getTarget() == null && dog.guardEnabled() && !new GuardGoal(dog).canUse(),
                    "Stopping for a lead clears only the job target and preserves the preference");
            dog.removeLeash();
            goal = new GuardGoal(dog); h.assertTrue(goal.canUse(),"A released guard must be able to resume"); goal.start();
            dog.setGenome(Genome.DEFAULT.with(Gene.TEMPERAMENT,1,feral));
            h.assertTrue(!goal.canContinueToUse() && dog.guardingStatus() == Guarding.Status.FERAL,
                    "A hidden feral copy must stop guarding immediately");
            goal.stop(); dog.setGenome(genome);
            dog.setHealth(dog.getMaxHealth()/2-.25F);
            h.assertTrue(!new GuardGoal(dog).canUse() && dog.guardingStatus() == Guarding.Status.HEALTH,
                    "An injured guard below half health must refuse a fresh job");
            dog.setHealth(dog.getMaxHealth());
            goal = new GuardGoal(dog); h.assertTrue(goal.canUse(),"A healthy guard must resume"); goal.start();
            dog.setTarget(cow); goal.stop();
            h.assertTrue(dog.getTarget() == cow && !new GuardGoal(dog).canUse(),"Guard cleanup must preserve a new external combat target");
            dog.setTarget(null);
            goal = new GuardGoal(dog); h.assertTrue(goal.canUse(),"A free guard must resume"); goal.start();
            husk.setPos(Vec3.atBottomCenterOf(home.offset(9,0,0)));
            h.assertTrue(!goal.canContinueToUse(),"Intruders leaving the eight-block Nest boundary must not be pursued");
            goal.stop(); husk.setPos(h.absoluteVec(new Vec3(6.5,1,3.5)));
            for (int z = 0; z < 8; z++) h.setBlock(4,0,z,Blocks.WATER);
            h.assertTrue(!new GuardGoal(dog).canUse(),"A native route across a water trench must be refused");
            for (int z = 0; z < 8; z++) h.setBlock(4,0,z,Blocks.STONE);
            husk.setPos(dog.position().add(.6,0,0)); husk.setHealth(1);
            float health = dog.getHealth();
            goal = new GuardGoal(dog); h.assertTrue(goal.canUse(),"A reachable adjacent intruder must be defendable");
            goal.start(); goal.tick(); goal.stop();
            h.assertTrue(!husk.isAlive() && dog.getHealth() == health && dog.forageWait() == 0 && home.equals(dog.homeNest()),
                    "A real native guard bite must kill without free hunting healing or a home change");
            var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            dog.saveWithoutId(saved);
            var loaded = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.COMMAND);
            loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            h.assertTrue(loaded.guardEnabled() && loaded.getTarget() == null,"Native loading retains the choice, not a transient target");
            var legacy = saved.buildResult().copy(); legacy.remove("GuardNest");
            loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),legacy));
            h.assertTrue(!loaded.guardEnabled(),"Legacy saves must default guarding off");
            CalipersItem.showCare(player,dog);
            h.assertTrue(dog.guardEnabled() && dog.getTarget() == null && dog.getHealth() == health && bone.getCount() == 3,
                    "Calipers status must be read-only");
            dog.setGenome(Genome.DEFAULT); dog.mobInteract(player,InteractionHand.OFF_HAND);
            h.assertTrue(!dog.guardEnabled(),"Players must be able to disable a saved preference after changing temperament");
            h.succeed();
        });
    }
}
