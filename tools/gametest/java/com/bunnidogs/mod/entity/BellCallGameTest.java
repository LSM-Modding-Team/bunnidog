package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;

public final class BellCallGameTest {
    private BellCallGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(1,1,6,ModBlocks.NEST.get());
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,1,4);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth());
        var bell = h.absolutePos(new BlockPos(4,1,4));
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,6000);
                var home = h.absolutePos(new BlockPos(1,1,1));
                h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"A called resident must keep a real home");
                var gathering = new BellGatherGoal(dog);
                h.assertTrue(!gathering.canUse(),"A failed noon lookup must establish the normal search backoff");
                h.setBlock(4,1,4,Blocks.BELL);
                level.clockManager().setTotalTicks(clock,1000);
                var nativeBell = (BellBlock)Blocks.BELL;
                dog.setHealth(dog.getMaxHealth()-1);
                nativeBell.attemptToRing(player,level,bell,Direction.NORTH);
                h.assertTrue(dog.calledBell() == null,"Injured adults must not queue social calls");
                dog.setHealth(dog.getMaxHealth()); dog.setLeashedTo(player,false);
                nativeBell.attemptToRing(player,level,bell,Direction.NORTH);
                h.assertTrue(dog.calledBell() == null,"Leading an animal must prevent queued calls");
                dog.removeLeash();
                h.assertTrue(nativeBell.attemptToRing(player,level,bell,Direction.NORTH) && bell.equals(dog.calledBell()),
                        "A real native ring must reach the resident through the game-event bus");
                h.assertTrue(gathering.canUse() && dog.calledBell() == null,
                        "One call must bypass failed-search backoff outside noon and be consumed only once");
                gathering.start();
                var route = dog.getNavigation().getPath();
                h.assertTrue(route.canReach() && HomeRoutes.safe(dog,route,null) && gathering.canContinueToUse(),
                        "A called morning visit must use a complete loaded safe home-bounded native path");
                nativeBell.attemptToRing(level,bell,Direction.NORTH);
                h.assertTrue(dog.calledBell() == null,"Rapid redstone-style rings must not continually queue visits");
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,6))) == NestHome.MoveResult.MOVED
                        && !gathering.canContinueToUse(),"Changing homes must immediately interrupt the old visit");
                gathering.stop();
                h.assertTrue(!gathering.canUse(),"A call must not bypass the existing post-visit rest");
                level.clockManager().setTotalTicks(clock,13000);
                nativeBell.attemptToRing(level,bell,Direction.NORTH);
                h.assertTrue(!BellGatherGoal.willing(dog) && dog.calledBell() == null,"Night-home routines must take precedence over calls");
                var apex = ModEntities.APEX.get().create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                apex.hearBell(bell);
                h.assertTrue(apex.calledBell() == null,"The Apex must never queue a domestic bell visit");
                h.assertTrue(dog.getHealth() == dog.getMaxHealth() && dog.getTarget() == null,"Social calls must neither injure nor arm residents");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,previous); }
        });
    }
}
