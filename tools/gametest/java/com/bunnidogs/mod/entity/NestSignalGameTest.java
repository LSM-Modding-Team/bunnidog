package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.NestBlock;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Native comparators observe home tickets through moves, failed claims, saves, travel and death. */
public final class NestSignalGameTest {
    private NestSignalGameTest() {}
    private static ComparatorBlockEntity comparator(GameTestHelper h, BlockPos nest) {
        h.getLevel().setBlock(nest.east(),Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING,Direction.WEST),3);
        return (ComparatorBlockEntity)h.getLevel().getBlockEntity(nest.east());
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var first = h.absolutePos(new BlockPos(1,1,1)); var second = h.absolutePos(new BlockPos(5,1,1));
        level.setBlock(first,ModBlocks.NEST.get().defaultBlockState(),3); level.setBlock(second,ModBlocks.NEST.get().defaultBlockState(),3);
        var firstSignal = comparator(h,first); var secondSignal = comparator(h,second);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,4.5F); dog.setAge(0);
        var other = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4.5F,1,5.5F); other.setAge(0);
        h.runAfterDelay(3,() -> {
            h.assertTrue(firstSignal.getOutputSignal() == 0 && secondSignal.getOutputSignal() == 0,"Free Nests must have no comparator output");
            h.assertTrue(dog.moveHomeTo(level,first) == NestHome.MoveResult.MOVED && other.moveHomeTo(level,second) == NestHome.MoveResult.MOVED,
                    "Two real residents must claim their own reachable Nests");
            h.runAfterDelay(5,() -> {
                h.assertTrue(firstSignal.getOutputSignal() == 15 && secondSignal.getOutputSignal() == 15,"Native comparators must notice both claims while residents are away");
                h.assertTrue(dog.moveHomeTo(level,second) == NestHome.MoveResult.OCCUPIED && first.equals(dog.homeNest()),"Failed relocation must preserve the original claim");
                var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(out);
                dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));
                h.assertTrue(NestBlock.claimed(level,first) && first.equals(dog.homeNest()),"Native animal reload must retain its existing ticket rather than claim twice");
                other.releaseHomeForTravel(level);
                other.setAge(-24000); // Keep this released fixture from naturally reclaiming a Nest during the refresh wait.
                h.runAfterDelay(5,() -> {
                    h.assertTrue(firstSignal.getOutputSignal() == 15 && secondSignal.getOutputSignal() == 0,"Travel release must clear only its own Nest signal");
                    h.assertTrue(dog.moveHomeTo(level,second) == NestHome.MoveResult.MOVED,"A freed Nest must accept the waiting resident");
                    h.runAfterDelay(5,() -> {
                        h.assertTrue(firstSignal.getOutputSignal() == 0 && secondSignal.getOutputSignal() == 15,"Rehoming must update both real comparators");
                        dog.releaseHomeForTravel(level);
                        h.runAfterDelay(5,() -> {
                            h.assertTrue(firstSignal.getOutputSignal() == 0 && secondSignal.getOutputSignal() == 0,"Packing/travel release must free the old home signal");
                            h.assertTrue(dog.moveHomeTo(level,first) == NestHome.MoveResult.MOVED,"Released residents can claim a reachable home again");
                            h.runAfterDelay(5,() -> {
                                h.assertTrue(firstSignal.getOutputSignal() == 15,"A later successful claim must restore output");
                                level.getPoiManager().release(first); // Simulate a ticket changing without a loaded block notification.
                                h.assertTrue(!NestBlock.claimed(level,first),"The unnotified fixture must actually release its ticket");
                                h.runAfterDelay(105,() -> {
                                    h.assertTrue(firstSignal.getOutputSignal() == 0,"A native scheduled refresh must repair a stale comparator after an unnotified ticket release");
                                    h.assertTrue(level.getPoiManager().take(holder -> holder.is(ModPoi.NEST.getKey()),(holder,pos) -> pos.equals(first),first,1).isPresent(),
                                            "Restore the real ticket before exercising normal death release");
                                    NestBlock.ticketChanged(level,first); dog.hurtServer(level,level.damageSources().generic(),1000);
                                    h.runAfterDelay(5,() -> {
                                        h.assertTrue(!dog.isAlive() && firstSignal.getOutputSignal() == 0,"Native death must release its ticket and notify the comparator");
                                        var remote = new BlockPos(25000000,64,25000000); var chunks = level.getChunkSource();
                                        h.assertTrue(chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null,"Remote notification fixture must begin unloaded");
                                        NestBlock.ticketChanged(level,remote);
                                        h.assertTrue(!NestBlock.claimed(level,remote) && chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null,
                                                "Ticket notifications and reads must not load remote terrain");
                                        h.succeed();
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }
}
