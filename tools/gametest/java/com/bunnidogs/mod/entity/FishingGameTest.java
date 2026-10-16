package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModDietTags;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;

public final class FishingGameTest {
    private FishingGameTest() {}
    private static Genome diet(String name) {
        int id = Gene.DIET.alleleNamed(name).id();
        return Genome.DEFAULT.with(Gene.DIET,0,id).with(Gene.DIET,1,id);
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE); h.setBlock(x,1,z,Blocks.STONE);
        }
        h.setBlock(1,2,1,ModBlocks.NEST.get());
        h.setBlock(5,1,4,Blocks.WATER); h.setBlock(5,1,5,Blocks.WATER);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4.5F,2,4.5F);
        dog.setGenome(diet("fisher")); dog.setAge(0); dog.setHealth(dog.getMaxHealth()-6);
        var chicken = h.spawnWithNoFreeWill(EntityTypes.CHICKEN,4,2,5);
        chicken.setAge(0);
        // Native melee goals throttle their first canUse check for twenty game ticks.
        h.runAfterDelay(22,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,2,1))) == NestHome.MoveResult.MOVED,
                        "A fisher must claim a real reachable Nest");
                h.assertTrue(!new HuntGoal(dog).canUse(),"Fisher residents must not hunt ordinary land prey");
                chicken.discard();
                var puffer = h.spawnWithNoFreeWill(EntityTypes.PUFFERFISH,5.5F,1.8F,4.5F);
                h.assertTrue(!new HuntGoal(dog).canUse(),"Default fishing prey must exclude defensive pufferfish");
                puffer.discard();
                var fish = h.spawnWithNoFreeWill(EntityTypes.COD,5.5F,1.8F,4.5F);
                fish.setHealth(1);
                dog.setHealth(dog.getMaxHealth());
                h.assertTrue(!new HuntGoal(dog).canUse(),"Healthy residents must not kill fish for sport");
                dog.setHealth(dog.getMaxHealth()-6);
                level.clockManager().setTotalTicks(clock,13000);
                h.assertTrue(!new HuntGoal(dog).canUse(),"Fishing must stop at night");
                level.clockManager().setTotalTicks(clock,1000);
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                dog.setLeashedTo(player,false);
                h.assertTrue(!new HuntGoal(dog).canUse(),"Leads must pause fishing");
                dog.removeLeash();
                var interrupted = new HuntGoal(dog);
                h.assertTrue(interrupted.canUse() && dog.getTarget() == fish,"An injured fisher must select the reachable shore fish: forage="
                        + dog.canForage() + ", time=" + level.getGameTime() + ", reach=" + dog.isWithinMeleeAttackRange(fish)
                        + ", sight=" + dog.getSensing().hasLineOfSight(fish) + ", tagged=" + fish.is(ModDietTags.FISH_PREY));
                interrupted.start(); dog.setGenome(diet("hunter"));
                h.assertTrue(!interrupted.canContinueToUse(),"Changing to a land-hunting Diet must interrupt fish predation");
                interrupted.stop();
                h.assertTrue(fish.isAlive() && fish.getHealth() == 1 && dog.getTarget() == null,"An interrupted fish job must preserve fish and clear only its target");
                dog.setGenome(diet("fisher"));
                var goal = new HuntGoal(dog);
                h.assertTrue(goal.canUse(),"A rested fisher must be able to resume a fish job");
                float health = dog.getHealth();
                goal.start();
                for (int i = 0; i < 4 && fish.isAlive(); i++) goal.tick();
                goal.stop();
                h.assertTrue(!fish.isAlive() && dog.getHealth() == health+Foraging.HUNT_HEAL
                        && dog.homeNest().equals(h.absolutePos(new BlockPos(1,2,1))),
                        "A real native prey kill must heal once and preserve home: fish=" + fish.getHealth() + ", health="
                                + dog.getHealth() + ", before=" + health + ", reach=" + dog.isWithinMeleeAttackRange(fish));
                var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
                dog.saveWithoutId(saved);
                h.assertTrue(saved.buildResult().getIntOr("ForageWait",0) == Foraging.MEAL_COOLDOWN,
                        "Fishing must use the existing saved meal recovery");
                var second = h.spawnWithNoFreeWill(EntityTypes.SALMON,5.5F,1.8F,4.5F);
                dog.setHealth(dog.getMaxHealth()-6);
                h.assertTrue(!new HuntGoal(dog).canUse() && second.isAlive(),"Meal recovery must prevent chain fishing");
                h.succeed();
            } finally {
                level.clockManager().setTotalTicks(clock,previous);
            }
        });
    }
}
