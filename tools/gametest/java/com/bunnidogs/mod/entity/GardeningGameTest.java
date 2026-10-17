package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class GardeningGameTest {
    private GardeningGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(4,1,4,ModBlocks.FEEDING_TROUGH.get());
        h.setBlock(5,0,4,Blocks.FARMLAND);
        var carrot = (CropBlock)Blocks.CARROTS;
        var ripe = carrot.getStateForAge(carrot.getMaxAge());
        h.setBlock(new BlockPos(5,1,4),ripe);
        var troughPos = h.absolutePos(new BlockPos(4,1,4));
        var cropPos = h.absolutePos(new BlockPos(5,1,4));
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(troughPos);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,1,4);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth());
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var hoe = new ItemStack(Items.WOODEN_HOE);
        player.setItemInHand(InteractionHand.MAIN_HAND,hoe);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,
                        "A gardener must have a native claimed home");
                h.assertTrue(!new GardenGoal(dog).canUse(),"Ordinary troughs must not opt existing farms into gardening");
                h.useBlock(new BlockPos(4,1,4),player);
                h.assertTrue(level.getBlockState(troughPos).getValue(FeedingTroughBlock.TENDING) && hoe.getDamageValue() == 0,
                        "Native hoe use must enable gardening without tool wear");
                h.assertTrue(!new GardenGoal(dog).canUse(),"Plain dogs must not tend another diet's carrots");
                dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,2).with(Gene.DIET,1,2));
                dog.setHealth(dog.getMaxHealth()-1);
                h.assertTrue(!new GardenGoal(dog).canUse() && dog.gardeningStatus() == Gardening.Status.FULL_HEALTH,
                        "AI and work readout must agree that injury blocks work");
                dog.setHealth(dog.getMaxHealth());
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
                h.assertTrue(!new GardenGoal(dog).canUse() && dog.gardeningStatus() == Gardening.Status.GRIEFING_OFF,
                        "AI and readout must agree when mob-griefing prevents crop tending");
                level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                var beforeRead = dog.homeNest();
                CalipersItem.showCare(player,dog);
                h.assertTrue(dog.gardeningStatus() == Gardening.Status.READY && dog.homeNest().equals(beforeRead)
                        && dog.gardeningWait() == 0 && trough.food().isEmpty() && level.getBlockState(cropPos).equals(ripe),
                        "A readiness snapshot must leave crops, stock, home and work recovery untouched");
                var goal = new GardenGoal(dog);
                h.assertTrue(trough.setFilter(new ItemStack(Items.POTATO)) && !goal.canUse() && level.getBlockState(cropPos).equals(ripe),
                        "An empty filtered trough must not make a gardener harvest the wrong crop");
                trough.clearFilter(); goal = new GardenGoal(dog);
                h.assertTrue(goal.canUse(),"A healthy forager must find the enabled trough and reachable ripe carrots");
                goal.start();
                dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(5,1,3))));
                for (int i = 0; i < Gardening.WORK_TICKS; i++) goal.tick();
                goal.stop();
                h.assertTrue(level.getBlockState(cropPos).equals(carrot.getStateForAge(0)) && trough.food().is(Items.CARROT)
                        && trough.food().getCount() == 1 && dog.gardeningWait() == Gardening.REST_TICKS,
                        "One job must reset one crop, stock exactly one produce and start saved recovery");
                CalipersItem.showCare(player,dog);
                h.assertTrue(!new GardenGoal(dog).canUse() && dog.gardeningStatus() == Gardening.Status.RESTING
                        && dog.gardeningWait() == Gardening.REST_TICKS,"Reading recovery must not reset or bypass the second-job refusal");
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
                dog.saveWithoutId(output);
                var saved = output.buildResult();
                var restored = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
                restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
                h.assertTrue(restored.gardeningWait() == Gardening.REST_TICKS,"Native saves must retain work recovery");
                saved.remove("GardenWait");
                dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
                h.assertTrue(dog.gardeningWait() == 0,"Legacy missing recovery must mean ready");
                level.setBlock(cropPos,ripe,3);
                trough.takeAll(); trough.insert(new ItemStack(Items.CARROT,16));
                h.assertTrue(!new GardenGoal(dog).canUse(),"A full trough must leave ripe crops intact");
                trough.takeAll(); trough.insert(new ItemStack(Items.POTATO));
                h.assertTrue(!new GardenGoal(dog).canUse(),"Different stock must prevent another crop from being harvested");
                trough.takeAll();
                level.clockManager().setTotalTicks(clock,13000);
                h.assertTrue(!new GardenGoal(dog).canUse() && dog.gardeningStatus() == Gardening.Status.DAYTIME,
                        "AI and work readout must agree about night rest");
                level.clockManager().setTotalTicks(clock,1000);
                var interrupted = new GardenGoal(dog);
                h.assertTrue(interrupted.canUse(),"Ready residents may resume a garden job by day");
                interrupted.start();
                dog.setGenome(Genome.DEFAULT);
                h.assertTrue(!interrupted.canContinueToUse(),"Changing the expressed Diet must immediately interrupt an incompatible crop job");
                dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,2).with(Gene.DIET,1,2));
                dog.setLeashedTo(player,false);
                h.assertTrue(!interrupted.canContinueToUse() && dog.gardeningStatus() == Gardening.Status.TRANSPORTED,
                        "A lead must immediately interrupt gardening and explain the pause");
                interrupted.stop(); dog.removeLeash();
                h.assertTrue(level.getBlockState(cropPos).equals(ripe) && trough.food().isEmpty()
                        && dog.homeNest().equals(h.absolutePos(new BlockPos(1,1,1))),"Interruptions must preserve crops, stock and home");
                // Reachable hanging fruit lets even a plain line maintain a small guava orchard.
                level.setBlock(cropPos,Blocks.AIR.defaultBlockState(),3);
                var fruitPos = cropPos.above();
                level.setBlock(fruitPos.above(),Blocks.OAK_LEAVES.defaultBlockState(),3);
                level.setBlock(fruitPos,ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(GuavaFruitBlock.AGE,2),3);
                dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth());
                var fruit = new GardenGoal(dog);
                h.assertTrue(fruit.canUse(),"Plain lines must recognize a low reachable ripe guava");
                fruit.start();
                for (int i = 0; i < Gardening.WORK_TICKS; i++) fruit.tick();
                fruit.stop();
                h.assertTrue(level.getBlockState(fruitPos).getValue(GuavaFruitBlock.AGE) == 0
                        && trough.food().is(ModItems.GUAVA.get()) && trough.food().getCount() == 1,
                        "Picking fruit must retain the plant and stock one guava");
                h.useBlock(new BlockPos(4,1,4),player);
                h.assertTrue(!level.getBlockState(troughPos).getValue(FeedingTroughBlock.TENDING),"A second hoe use must switch gardening off");
                h.succeed();
            } finally {
                level.clockManager().setTotalTicks(clock,previous);
                level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer());
            }
        });
    }
}
