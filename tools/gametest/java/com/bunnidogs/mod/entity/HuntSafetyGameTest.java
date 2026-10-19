package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** No named/led livestock, fresh route hazards, relocation interruption and a real dry-ground meal. */
public final class HuntSafetyGameTest {
    private HuntSafetyGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get()); h.setBlock(6,1,1,ModBlocks.NEST.get());
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F); dog.setAge(0);
        int hunter = Gene.DIET.alleleNamed("hunter").id(); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,hunter).with(Gene.DIET,1,hunter));
        dog.setHealth(dog.getMaxHealth()-6);
        var chicken = h.spawnWithNoFreeWill(EntityTypes.CHICKEN,6.5F,1,4.5F); chicken.setAge(0); chicken.setHealth(1);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                var home = h.absolutePos(new BlockPos(1,1,1));
                h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"A hunter must claim a real home");
                chicken.setCustomName(Component.literal("Player chicken")); h.assertTrue(!new HuntGoal(dog).canUse(),"Named livestock must never become autonomous prey"); chicken.setCustomName(null);
                var holder = h.makeMockPlayer(GameType.SURVIVAL); chicken.setLeashedTo(holder,false);
                h.assertTrue(!new HuntGoal(dog).canUse(),"Leashed livestock must remain under player control"); chicken.removeLeash();
                var boat = h.spawn(ModEntities.GUAVA_BOAT.get(),6.5F,1,4.5F);
                h.assertTrue(chicken.startRiding(boat) && !new HuntGoal(dog).canUse(),"Transported prey must remain untouched"); chicken.stopRiding(); boat.discard();
                chicken.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(6,1,4))));
                var goal = new HuntGoal(dog); h.assertTrue(goal.canUse(),"Ordinary wild prey must have a safe dry approach"); goal.start();
                var route = dog.getNavigation().getPath(); h.assertTrue(HomeRoutes.safe(dog,route,null),"Hunting must install a complete safe native route");
                int index = Math.min(route.getNodeCount()-1,route.getNextNodeIndex()+1); var hazard = route.getNode(index).asBlockPos().below();
                level.setBlock(hazard,Blocks.MAGMA_BLOCK.defaultBlockState(),3);
                h.assertTrue(!HomeRoutes.safe(dog,route,null) && !goal.canContinueToUse(),"A newly damaging floor must invalidate an existing path with cached safe nodes");
                goal.tick(); goal.stop();
                h.assertTrue(chicken.isAlive() && chicken.getHealth() == 1 && dog.getTarget() == null && dog.forageWait() == 0,"Unsafe pursuits must stop without damage, meal rest or stolen targets");
                level.setBlock(hazard,Blocks.STONE.defaultBlockState(),3);
                var renamed = new HuntGoal(dog); h.assertTrue(renamed.canUse(),"A repaired route must allow a new search"); renamed.start();
                chicken.setCustomName(Component.literal("Rescued chicken")); h.assertTrue(!renamed.canContinueToUse(),"Renaming during pursuit must protect the new pet"); renamed.stop(); chicken.setCustomName(null);
                var rehomed = new HuntGoal(dog); h.assertTrue(rehomed.canUse(),"A hunter must be eligible before relocation"); rehomed.start();
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(6,1,1))) == NestHome.MoveResult.MOVED && !rehomed.canContinueToUse(),"A new home must interrupt the old hunting plan"); rehomed.stop();
                var retaliation = new HuntGoal(dog); h.assertTrue(retaliation.canUse(),"A hunt can resume under the new home"); retaliation.start();
                var enemy = h.spawnWithNoFreeWill(EntityTypes.ZOMBIE,2,1,6); dog.setTarget(enemy); retaliation.stop();
                h.assertTrue(dog.getTarget() == enemy && chicken.isAlive(),"Stopping a hunt must not clear a replacement combat target"); dog.setTarget(null); enemy.discard();
                var meal = new HuntGoal(dog); h.assertTrue(meal.canUse(),"A safe hungry hunter must resume its meal"); meal.start();
                dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(5,1,4)))); float health = dog.getHealth();
                for (int i = 0; i < 4 && chicken.isAlive(); i++) meal.tick(); meal.stop();
                h.assertTrue(!chicken.isAlive() && dog.getHealth() == health+Foraging.HUNT_HEAL && dog.forageWait() == Foraging.MEAL_COOLDOWN,
                        "One real native wild-prey kill must heal four HP and share the saved meal recovery");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); }
        });
    }
}
