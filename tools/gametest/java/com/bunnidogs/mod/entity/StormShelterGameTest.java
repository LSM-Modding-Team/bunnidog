package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class StormShelterGameTest {
    private StormShelterGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            // Remove the empty template's barrier ceiling so this really tests sky exposure.
            for (int y = 1; y <= 8; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        for (int x = 0; x <= 3; x++) for (int z = 1; z <= 5; z++) h.setBlock(x,3,z,Blocks.STONE);
        h.setBlock(1,1,3,ModBlocks.NEST.get());
        h.setBlock(3,1,4,ModBlocks.FEEDING_TROUGH.get());
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(3,1,4)));
        trough.insert(new ItemStack(ModItems.GUAVA.get(),4));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),5.5F,1,3.5F);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock);
            float rain = level.getRainLevel(1), thunder = rain > 0 ? level.getThunderLevel(1)/rain : 0;
            try {
                level.clockManager().setTotalTicks(clock,1000);
                level.setRainLevel(0); level.setThunderLevel(0);
                var home = h.absolutePos(new BlockPos(1,1,3));
                h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"Shelter needs a real claimed home");
                h.assertTrue(!new StormShelterGoal(dog).canUse(),"Clear weather must keep ordinary idle routines");
                level.setRainLevel(1); level.setThunderLevel(1);
                h.assertTrue(level.isRainingAt(dog.blockPosition()) && level.canSeeSky(dog.blockPosition()),
                        "The starting animal must really be exposed to a native thunderstorm");
                var shelter = new StormShelterGoal(dog);
                h.assertTrue(shelter.canUse(),"An idle exposed adult must find reachable home cover");
                shelter.start();
                var path = dog.getNavigation().getPath();
                var end = path.getEndNode().asBlockPos();
                h.assertTrue(path.canReach() && !level.canSeeSky(end) && dog.foodWithinHome(end),
                        "A real complete native path must end beneath cover inside the home boundary");
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                dog.setLeashedTo(player,false);
                h.assertTrue(!shelter.canContinueToUse(),"A lead must immediately interrupt shelter");
                shelter.stop(); dog.removeLeash();
                dog.setPos(Vec3.atBottomCenterOf(end));
                var waiting = new StormShelterGoal(dog);
                h.assertTrue(waiting.canUse(),"An adult already under cover must be able to wait there");
                waiting.start(); waiting.tick();
                h.assertTrue(waiting.canContinueToUse() && dog.getNavigation().isDone(),"Shelter must hold the dry spot without another path");
                level.setThunderLevel(0);
                h.assertTrue(!waiting.canContinueToUse() && !new StormShelterGoal(dog).canUse(),
                        "Ordinary rain must release shelter instead of occupying the animal indefinitely");
                waiting.stop(); level.setThunderLevel(1);
                var idle = new StormShelterGoal(dog);
                var meal = new TroughGoal(dog);
                var selector = dog.getGoalSelector();
                selector.addGoal(8,idle); selector.addGoal(7,meal);
                selector.tick();
                h.assertTrue(selector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() == idle && g.isRunning()),
                        "Native goal arbitration must start shelter while care is unnecessary");
                dog.setHealth(dog.getMaxHealth()-2);
                selector.tick();
                h.assertTrue(selector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() == meal && g.isRunning())
                        && selector.getAvailableGoals().stream().noneMatch(g -> g.getGoal() == idle && g.isRunning()),
                        "A real healing-trough goal must preempt shelter during the same storm");
                selector.removeAllGoals(g -> true);
                h.assertTrue(dog.homeNest().equals(home) && trough.food().getCount() == 4 && dog.getTarget() == null,
                        "Shelter must preserve the Nest and stock without creating a combat target");
                var apex = ModEntities.APEX.get().create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                h.assertTrue(!new StormShelterGoal(apex).canUse(),"The Apex must not adopt domestic weather routines");
                h.succeed();
            } finally {
                level.setRainLevel(rain); level.setThunderLevel(thunder);
                level.clockManager().setTotalTicks(clock,previous);
            }
        });
    }
}
