package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Refuge;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public final class RefugeGameTest {
    private RefugeGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        h.setBlock(2,1,3,ModBlocks.NEST.get());
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),5.5F,1,3.5F);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth());
        var cow = h.spawnWithNoFreeWill(EntityTypes.COW,6,1,3);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                h.assertTrue(!new RefugeGoal(dog).canUse(),"Homeless animals must retain their existing routines");
                var home = h.absolutePos(new BlockPos(2,1,3));
                h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"Refuge needs a real claimed Nest");
                h.assertTrue(!new RefugeGoal(dog).canUse(),"Passive untagged neighbours must not scare residents");
                cow.discard();
                var threat = h.spawnWithNoFreeWill(EntityTypes.HUSK,6.5F,1,3.5F);
                var goal = new RefugeGoal(dog);
                h.assertTrue(goal.canUse(),"A non-feral resident must find a loaded dry route away from a visible threat");
                float health = dog.getHealth(), threatHealth = threat.getHealth();
                goal.start();
                var route = dog.getNavigation().getPath();
                var end = route.getEndNode().asBlockPos();
                h.assertTrue(route.canReach() && end.distSqr(threat.blockPosition()) >= Refuge.SAFE_DISTANCE*Refuge.SAFE_DISTANCE
                        && dog.foodWithinHome(end),"The real native destination must be farther from danger and inside the home boundary");
                h.assertTrue(dog.seekingRefuge() && dog.getTarget() == null && dog.foragingStatus() == Foraging.Status.DANGER
                        && dog.gardeningStatus() == Gardening.Status.DANGER && !new BellGatherGoal(dog).canUse(),
                        "Refuge must pause meals/work/gatherings without creating an attack target");
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                dog.setLeashedTo(player,false);
                h.assertTrue(!goal.canContinueToUse(),"A lead must immediately interrupt the retreat");
                goal.stop(); dog.removeLeash();
                h.assertTrue(!dog.seekingRefuge() && dog.homeNest().equals(home),"Stopping must clear only the transient retreat, preserving home");
                var changed = new RefugeGoal(dog);
                h.assertTrue(changed.canUse(),"A free resident may retreat again");
                changed.start();
                int feral = Gene.TEMPERAMENT.alleleNamed("feral").id();
                dog.setGenome(Genome.DEFAULT.with(Gene.TEMPERAMENT,0,feral).with(Gene.TEMPERAMENT,1,feral));
                h.assertTrue(!changed.canContinueToUse(),"Feral expression must keep its established retaliation behavior");
                changed.stop();
                h.assertTrue(!new RefugeGoal(dog).canUse(),"Feral residents must not start this non-combat routine");
                dog.setGenome(Genome.DEFAULT);
                var blocked = new RefugeGoal(dog);
                h.assertTrue(blocked.canUse(),"A new safe route must remain available");
                blocked.start();
                var blockedSpot = dog.getNavigation().getPath().getEndNode().asBlockPos();
                level.setBlock(blockedSpot,Blocks.STONE.defaultBlockState(),2);
                h.assertTrue(!blocked.canContinueToUse(),"An occupied refuge destination must immediately cancel the retreat");
                blocked.stop(); level.setBlock(blockedSpot,Blocks.AIR.defaultBlockState(),2);
                var quiet = new RefugeGoal(dog);
                h.assertTrue(quiet.canUse(),"Restored non-feral expression must restore refuge eligibility");
                quiet.start();
                threat.setPos(dog.getX()+20,dog.getY(),dog.getZ());
                for (int tick = 0; tick < Refuge.QUIET_TICKS; tick++) quiet.tick();
                h.assertTrue(!quiet.canContinueToUse(),"Two seconds without nearby visible danger must end refuge");
                quiet.stop();
                h.assertTrue(!dog.seekingRefuge() && dog.getHealth() == health && threat.getHealth() == threatHealth
                        && dog.getTarget() == null && dog.homeNest().equals(home),"Retreats must preserve both creatures and the home, without attacks/healing");
                var apex = ModEntities.APEX.get().create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                h.assertTrue(!new RefugeGoal(apex).canUse(),"The Apex must never adopt domestic refuge");
                h.succeed();
            } finally {
                level.clockManager().setTotalTicks(clock,previous);
            }
        });
    }
}
