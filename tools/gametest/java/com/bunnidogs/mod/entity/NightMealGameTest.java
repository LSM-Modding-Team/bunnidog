package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class NightMealGameTest {
    private NightMealGameTest() {}
    private static void resetMeal(Bunnidog dog) {
        var level = dog.level();
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(output); var saved = output.buildResult(); saved.remove("ForageWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(2,1,2,ModBlocks.FEEDING_TROUGH.get()); h.setBlock(6,1,1,ModBlocks.FEEDING_TROUGH.get());
        var home = h.absolutePos(new BlockPos(1,1,1)); var food = h.absolutePos(new BlockPos(2,1,2));
        var nearby = (FeedingTroughBlockEntity)level.getBlockEntity(food);
        var distant = (FeedingTroughBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(6,1,1)));
        nearby.insert(new ItemStack(ModItems.GUAVA.get(),3)); distant.insert(new ItemStack(ModItems.GUAVA.get(),3));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2.5F,1,1.5F);
        dog.setGenome(Genome.DEFAULT); WorkerFixture.familiar(dog); dog.setAge(0); dog.setHealth(dog.getMaxHealth());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long previous = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,13000);
                h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"Night care needs a real home");
                var selector = dog.getGoalSelector(); selector.removeAllGoals(g -> true);
                var night = new NightHomeGoal(dog); var meal = new TroughGoal(dog);
                selector.addGoal(7,night); selector.addGoal(6,meal); selector.tick();
                h.assertTrue(selector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() == night && g.isRunning())
                        && nearby.food().getCount() == 3,"A healthy resident must retain normal night waiting without eating");
                dog.setHealth(dog.getMaxHealth()-Foraging.HEAL);
                h.assertTrue(dog.foragingStatus() == Foraging.Status.DAYTIME && dog.troughFeedingStatus() == Foraging.Status.NEST_MEAL
                        && !new GardenGoal(dog).canUse() && !new HuntGoal(dog).canUse() && !new GrazeGoal(dog).canUse(),
                        "Night trough readiness must not unlock outdoor food or work");
                selector.tick();
                h.assertTrue(selector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() == meal && g.isRunning())
                        && selector.getAvailableGoals().stream().noneMatch(g -> g.getGoal() == night && g.isRunning()),
                        "Native goal arbitration must let a short healing meal preempt night waiting");
                for (int tick = 1; tick < Foraging.GRAZE_TICKS; tick++) selector.tick();
                h.assertTrue(dog.getHealth() == dog.getMaxHealth() && nearby.food().getCount() == 2 && distant.food().getCount() == 3
                        && dog.forageWait() == Foraging.MEAL_COOLDOWN,"A night meal must consume exactly one real item for normal healing and shared rest");
                selector.tick();
                h.assertTrue(selector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() == night && g.isRunning()),"After eating, normal night waiting must resume");
                selector.removeAllGoals(g -> true); dog.setHealth(dog.getMaxHealth()-Foraging.HEAL);
                h.assertTrue(dog.troughFeedingStatus() == Foraging.Status.RESTING && !new TroughGoal(dog).canUse(),"New injury must not bypass shared meal recovery");
                resetMeal(dog); nearby.takeAll();
                h.assertTrue(!new TroughGoal(dog).canUse(),"A trough five blocks from home must not draw a night trip");
                nearby.insert(new ItemStack(ModItems.GUAVA.get(),2));
                dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(6,1,5))));
                h.assertTrue(dog.troughFeedingStatus() == Foraging.Status.DAYTIME && !new TroughGoal(dog).canUse(),"A resident outside the short night boundary must return home first");
                level.clockManager().setTotalTicks(clock,1000);
                h.assertTrue(new TroughGoal(dog).canUse(),"Daytime trough care must retain the wider home boundary");
                level.clockManager().setTotalTicks(clock,13000); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,1,1))));
                var interrupted = new TroughGoal(dog); h.assertTrue(interrupted.canUse(),"A ready near-home night meal must be selectable");
                interrupted.start(); dog.setLeashedTo(h.makeMockPlayer(GameType.SURVIVAL),false);
                h.assertTrue(!interrupted.canContinueToUse() && nearby.food().getCount() == 2 && dog.forageWait() == 0,"A lead must stop night care without spending stock or rest");
                interrupted.stop(); dog.removeLeash();
                for (Direction direction : Direction.Plane.HORIZONTAL) level.setBlock(food.relative(direction).below(),Blocks.MAGMA_BLOCK.defaultBlockState(),3);
                h.assertTrue(!new TroughGoal(dog).canUse(),"Damaging routes and standing spots must refuse a night meal");
                for (Direction direction : Direction.Plane.HORIZONTAL) level.setBlock(food.relative(direction).below(),Blocks.STONE.defaultBlockState(),3);
                var blocked = new TroughGoal(dog); h.assertTrue(blocked.canUse(),"Restoring safe floor must allow a short night meal"); blocked.start();
                for (Direction direction : Direction.Plane.HORIZONTAL) level.setBlock(food.relative(direction).above(),Blocks.STONE.defaultBlockState(),3);
                h.assertTrue(!blocked.canContinueToUse() && home.equals(dog.homeNest()) && nearby.food().getCount() == 2,
                        "Blocked standing room must cancel without food or home changes");
                blocked.stop();
                var apex = ModEntities.APEX.get().create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                h.assertTrue(apex.troughFeedingStatus() == Foraging.Status.UNAVAILABLE,"The Apex must remain excluded from domestic food");
            } finally { level.clockManager().setTotalTicks(clock,previous); }
            h.succeed();
        });
    }
}
