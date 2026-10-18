package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

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
                var calipers = new ItemStack(ModItems.CALIPERS.get());
                calipers.set(DataComponents.CUSTOM_NAME,Component.literal("Bell readout"));
                player.setItemInHand(InteractionHand.OFF_HAND,calipers); player.setShiftKeyDown(true);
                var before = calipers.copy(); var genes = dog.genome(); var stats = dog.stats();
                dog.applyEarTag();
                var census = ResidentCensus.nearby(level,bell);
                h.assertTrue(census.contains(dog),"Bell roll call must include an adult with a real nearby claimed home");
                // This native mock Player has no connection; use the real item entry with the server-level readout.
                calipers.getItem().useOn(new UseOnContext(level,player,InteractionHand.OFF_HAND,calipers,
                        new BlockHitResult(Vec3.atCenterOf(bell),Direction.UP,bell,false)));
                h.assertTrue(dog.calledBell() == null && home.equals(dog.homeNest()) && ItemStack.matches(before,calipers)
                        && dog.genome().equals(genes) && dog.stats().equals(stats),
                        "Sneak-Calipers roll call must not ring, queue visits, change homes/biology or alter the tool");
                dog.setAge(-100);
                h.assertTrue(!ResidentCensus.nearby(level,bell).contains(dog),"Roll call excludes pups even with a cached home");
                dog.setAge(0);
                var remote = bell.offset(4096,0,4096);
                h.assertTrue(!level.hasChunkAt(remote) && ResidentCensus.nearby(level,remote).isEmpty() && !level.hasChunkAt(remote),
                        "Roll call must never request remote terrain");
                player.setShiftKeyDown(false); player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
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
