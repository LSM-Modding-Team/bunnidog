package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.RecoveryRest;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Safe native home paths, idle waiting and actual priority handoff to healing meals. */
public final class RecoveryRestGameTest {
    private RecoveryRestGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        h.setBlock(1,1,3,ModBlocks.NEST.get()); h.setBlock(3,1,5,ModBlocks.FEEDING_TROUGH.get());
        var home = h.absolutePos(new BlockPos(1,1,3));
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(3,1,5)));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),5.5F,1,3.5F);
        dog.setGenome(Genome.DEFAULT); dog.setAge(0); dog.setHealth(dog.getMaxHealth()*.4F);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long previous = level.clockManager().getTotalTicks(clock);
            float rain = level.getRainLevel(1), thunder = rain > 0 ? level.getThunderLevel(1)/rain : 0;
            try {
                level.clockManager().setTotalTicks(clock,1000);
                level.setRainLevel(0); level.setThunderLevel(0);
                h.assertTrue(!new RecoveryRestGoal(dog).canUse(),"An unhomed animal must not select a resting destination");
                h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"Recovery must use an ordinary real claimed Nest");
                dog.setAge(-100);
                h.assertTrue(!new RecoveryRestGoal(dog).canUse(),"Pups retain their parent-following behavior");
                dog.setAge(0); dog.setHealth(dog.getMaxHealth()*.5F);
                h.assertTrue(dog.recoveryRestStatus() == RecoveryRest.Status.HEALTH && !new RecoveryRestGoal(dog).canUse(),
                        "Half health does not trigger serious-injury rest");
                dog.setHealth(dog.getMaxHealth()*.4F);
                var rest = new RecoveryRestGoal(dog);
                h.assertTrue(rest.canUse(),"A badly hurt free adult must find a dry place near its Nest");
                rest.start(); var route = dog.getNavigation().getPath(); var end = route.getEndNode().asBlockPos();
                h.assertTrue(route.canReach() && HomeRoutes.safe(dog,route,null) && RecoveryRest.nearNest(end.distSqr(home)),
                        "A complete loaded hazard-avoiding native route must end inside the two-block Nest radius");
                float health = dog.getHealth(); var genes = dog.genome(); var stats = dog.stats(); int age = dog.getAge();
                dog.setPos(Vec3.atBottomCenterOf(end)); rest.tick();
                h.assertTrue(rest.canContinueToUse() && dog.getNavigation().isDone() && dog.getHealth() == health
                        && home.equals(dog.homeNest()) && dog.genome().equals(genes) && dog.stats().equals(stats) && dog.getAge() == age
                        && dog.forageWait() == 0 && trough.food().isEmpty(),"Rest holds a safe place without healing, growth, food, rest cost or home/biology edits");
                level.setRainLevel(1); level.setThunderLevel(1);
                h.assertTrue(!rest.canContinueToUse() && dog.recoveryRestStatus() == RecoveryRest.Status.WEATHER,
                        "A thunderstorm must release recovery rest so the existing shelter routine can take over");
                level.setRainLevel(0); level.setThunderLevel(0);
                var player = h.makeMockPlayer(GameType.SURVIVAL); dog.setLeashedTo(player,false);
                h.assertTrue(!rest.canContinueToUse(),"A lead must immediately release idle rest"); rest.stop(); dog.removeLeash();
                var target = h.spawnWithNoFreeWill(EntityTypes.COW,6,1,5); dog.setTarget(target);
                h.assertTrue(dog.recoveryRestStatus() == RecoveryRest.Status.DANGER && !new RecoveryRestGoal(dog).canUse(),
                        "Combat must prevent convalescent rest"); dog.setTarget(null); target.discard();
                level.clockManager().setTotalTicks(clock,13000);
                h.assertTrue(!new RecoveryRestGoal(dog).canUse(),"Native night-home routines retain priority");
                level.clockManager().setTotalTicks(clock,1000);
                var idle = new RecoveryRestGoal(dog); var selector = dog.getGoalSelector(); selector.addGoal(8,idle); selector.tick();
                h.assertTrue(selector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() == idle && g.isRunning()),
                        "Native arbitration must start recovery rest while no meal can be found");
                trough.insert(new ItemStack(ModItems.GUAVA.get(),3)); var meal = new TroughGoal(dog); selector.addGoal(6,meal); selector.tick();
                h.assertTrue(selector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() == meal && g.isRunning())
                        && selector.getAvailableGoals().stream().noneMatch(g -> g.getGoal() == idle && g.isRunning()),
                        "An actual food-seeking goal must preempt recovery rest without waiting for healing");
                selector.removeAllGoals(g -> true); dog.getNavigation().stop();
                dog.setHealth(dog.getMaxHealth());
                h.assertTrue(!new RecoveryRestGoal(dog).canUse(),"Healing must restore normal idle behavior");
                dog.setHealth(health); var blocked = new RecoveryRestGoal(dog);
                h.assertTrue(blocked.canUse(),"Rest remains available after native goal cleanup"); blocked.start();
                level.setBlock(dog.blockPosition(),Blocks.WATER.defaultBlockState(),3);
                h.assertTrue(!blocked.canContinueToUse(),"A resting place that floods must stop being safe immediately"); blocked.stop();
                var apex = ModEntities.APEX.get().create(level,EntitySpawnReason.COMMAND); apex.setHealth(apex.getMaxHealth()*.4F);
                h.assertTrue(!new RecoveryRestGoal(apex).canUse(),"The Apex must never use domestic recovery routines");
                h.assertTrue(dog.getHealth() == health && trough.food().getCount() == 3 && home.equals(dog.homeNest()),
                        "Preempted/refused rest must preserve health, real food stock and the home");
                h.succeed();
            } finally {
                level.clockManager().setTotalTicks(clock,previous); level.setRainLevel(rain); level.setThunderLevel(thunder);
            }
        });
    }
}
