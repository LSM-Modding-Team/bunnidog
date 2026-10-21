package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Real caliper use, selected-mode eligibility and exact read-only/loaded-only boundaries. */
public final class WorkCensusGameTest {
    private WorkCensusGameTest() {}
    private static Bunnidog resident(GameTestHelper h, float x, int senses, int bloom) {
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),x,1,3.5F); dog.setAge(0); dog.setGenome(Genome.DEFAULT); WorkerFixture.familiar(dog);
        dog.setStats(Stats.of(0,0,0,senses,bloom)); dog.setHealth(dog.getMaxHealth()); return dog;
    }
    private static net.minecraft.nbt.CompoundTag save(Bunnidog dog) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.level().registryAccess()); dog.saveWithoutId(out); return out.buildResult();
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get()); h.setBlock(6,1,1,ModBlocks.NEST.get());
        var pos = h.absolutePos(new BlockPos(3,1,4));
        level.setBlock(pos,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
        var stock = (FeedingTroughBlockEntity)level.getBlockEntity(pos); stock.setWorkMode(TroughWork.NURSERY);
        stock.insert(new ItemStack(ModItems.GUAVA.get(),9)); stock.setFilter(new ItemStack(ModItems.GUAVA.get()));
        var nurse = resident(h,3.5F,0,20); var learner = resident(h,4.5F,0,19); var pup = resident(h,2.5F,10,20); pup.setAge(-24000);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                h.assertTrue(nurse.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED
                        && learner.moveHomeTo(level,h.absolutePos(new BlockPos(6,1,1))) == NestHome.MoveResult.MOVED,"Report fixtures need real claimed homes");
                var report = WorkCensus.inspect(level,pos).orElseThrow();
                h.assertTrue(report.total() == 3 && report.ready() == 1 && report.workers().getFirst().dog() == nurse
                        && report.workers().stream().noneMatch(row -> row.dog() == pup && row.ready()),"Nursery reports must show the +20 adult first and refuse low Bloom/pups");
                var player = h.makeMockServerPlayerInLevel(); var calipers = new ItemStack(ModItems.CALIPERS.get()); var tool = calipers.copy();
                player.setItemInHand(InteractionHand.OFF_HAND,calipers); player.setShiftKeyDown(true);
                var foodBefore = stock.saveWithFullMetadata(level.registryAccess()); var dogBefore = save(nurse); var path = nurse.getNavigation().getPath(); var tag = nurse.earTag();
                var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
                h.assertTrue(player.gameMode.useItemOn(player,level,calipers,InteractionHand.OFF_HAND,hit).consumesAction(),"Real crouched off-hand Calipers must open the work report");
                h.assertTrue(stock.saveWithFullMetadata(level.registryAccess()).equals(foodBefore) && save(nurse).equals(dogBefore)
                        && nurse.getNavigation().getPath() == path && nurse.earTag().equals(tag) && ItemStack.matches(calipers,tool),
                        "Inspection must preserve whole food/filter/mode, animal data, tags, navigation and held tool");
                stock.setWorkMode(TroughWork.GATHER);
                h.assertTrue(WorkCensus.inspect(level,pos).orElseThrow().ready() == 0,"Gather mode must require Senses even for a high-Bloom nurse");
                learner.setStats(Stats.of(0,0,0,10,19));
                h.assertTrue(WorkCensus.inspect(level,pos).orElseThrow().ready() == 1,"A +10 Senses adult must become eligible to search for loose food");
                level.setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                h.assertTrue(WorkCensus.inspect(level,pos).orElseThrow().paused() && WorkCensus.inspect(level,pos).orElseThrow().ready() == 0,"Power must pause report eligibility without touching stock"); level.removeBlock(pos.west(),false);
                level.setBlock(pos,level.getBlockState(pos).setValue(FeedingTroughBlock.TENDING,false),3);
                h.assertTrue(!WorkCensus.inspect(level,pos).orElseThrow().enabled() && WorkCensus.inspect(level,pos).orElseThrow().ready() == 0,"Disabled work must be visible in the report");
                var remote = new BlockPos(25000000,64,25000000); var chunks = level.getChunkSource();
                h.assertTrue(chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null && WorkCensus.inspect(level,remote).isEmpty()
                        && chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null,"Remote reports must never request unloaded terrain");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); }
        });
    }
}
