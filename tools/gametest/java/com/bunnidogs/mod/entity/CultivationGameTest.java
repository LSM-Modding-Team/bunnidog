package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class CultivationGameTest {
    private CultivationGameTest() {}
    private static void ready(Bunnidog dog) {
        var level = dog.level();
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(output);
        var saved = output.buildResult(); saved.remove("GardenWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    private static void work(GameTestHelper h, Bunnidog dog) {
        var goal = new GardenGoal(dog);
        h.assertTrue(goal.canUse(),"A ready resident must find the expected native garden task");
        goal.start();
        dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(6,1,4))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) goal.tick();
        goal.stop();
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(new BlockPos(4,1,4),ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true));
        h.setBlock(5,0,4,Blocks.FARMLAND);
        var crop = h.absolutePos(new BlockPos(5,1,4));
        var carrots = (CropBlock)Blocks.CARROTS;
        level.setBlock(crop,carrots.getStateForAge(0),3);
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(4,1,4)));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,4.5F);
        var forager = Genome.DEFAULT.with(Gene.DIET,0,2).with(Gene.DIET,1,2);
        dog.setGenome(forager); dog.setAge(0);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Cultivation needs a real claimed Nest");
                dog.setStats(Stats.of(0,0,0,0,9));
                h.assertTrue(!new GardenGoal(dog).canUse(),"Bloom below +10 must leave unripe plants intact");
                dog.setStats(Stats.of(0,0,0,0,10));
                trough.setFilter(new ItemStack(Items.POTATO));
                h.assertTrue(!new GardenGoal(dog).canUse(),"An empty wrong-filter trough must refuse growth as well as harvesting");
                trough.clearFilter(); trough.setFilter(new ItemStack(Items.CARROT));
                var interrupted = new GardenGoal(dog);
                h.assertTrue(interrupted.canUse(),"Bloom +10 must enable an unripe matching crop");
                interrupted.start(); dog.setStats(Stats.ZERO);
                h.assertTrue(!interrupted.canContinueToUse() && carrots.getAge(level.getBlockState(crop)) == 0
                        && dog.gardeningWait() == 0,"Losing Bloom must stop cultivation without touching plants or recovery");
                interrupted.stop(); dog.setStats(Stats.of(0,0,0,0,10));
                work(h,dog);
                h.assertTrue(carrots.getAge(level.getBlockState(crop)) == 1 && trough.food().isEmpty()
                        && dog.gardeningWait() == Gardening.REST_TICKS && !new GardenGoal(dog).canUse(),
                        "One cultivation must advance exactly one stage, create no food and share work rest");
                ready(dog);
                h.setBlock(5,0,5,Blocks.FARMLAND);
                var ripe = h.absolutePos(new BlockPos(5,1,5));
                level.setBlock(ripe,carrots.getStateForAge(carrots.getMaxAge()),3);
                work(h,dog);
                h.assertTrue(carrots.getAge(level.getBlockState(crop)) == 1 && carrots.getAge(level.getBlockState(ripe)) == 0
                        && trough.food().is(Items.CARROT) && trough.food().getCount() == 1,"Ripe harvests must take priority over nearby growth tasks");
                level.setBlock(ripe,Blocks.AIR.defaultBlockState(),3); trough.takeAll(); trough.clearFilter(); ready(dog);
                level.setBlock(crop.below(),Blocks.DIRT.defaultBlockState(),3);
                level.setBlock(crop,Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE,3),3);
                dog.setStats(Stats.ZERO);
                work(h,dog);
                h.assertTrue(level.getBlockState(crop).getValue(SweetBerryBushBlock.AGE) == 1 && trough.food().is(Items.SWEET_BERRIES)
                        && trough.food().getCount() == 1,"An ordinary forager must gather one ripe berry and retain the growing bush");
                trough.takeAll(); ready(dog); dog.setStats(Stats.of(0,0,0,0,10)); dog.setGenome(Genome.DEFAULT);
                level.setBlock(crop,Blocks.AIR.defaultBlockState(),3);
                level.setBlock(crop.below(),Blocks.FARMLAND.defaultBlockState(),3);
                level.setBlock(crop,carrots.getStateForAge(0),3);
                h.assertTrue(!new GardenGoal(dog).canUse(),"High Bloom must not bypass Diet");
                level.setBlock(crop,ModBlocks.SEED_CROP.get().defaultBlockState(),3);
                h.assertTrue(!new GardenGoal(dog).canUse(),"Bunnidog embryo crops must never receive autonomous cultivation");
                level.setBlock(crop,Blocks.AIR.defaultBlockState(),3);
                level.setBlock(crop.above(2),Blocks.OAK_LEAVES.defaultBlockState(),3);
                level.setBlock(crop.above(),ModBlocks.GUAVA_FRUIT.get().defaultBlockState(),3);
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
                h.assertTrue(!new GardenGoal(dog).canUse(),"Cultivation must respect mob-griefing");
                level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                work(h,dog);
                h.assertTrue(level.getBlockState(crop.above()).getValue(GuavaFruitBlock.AGE) == 1 && trough.food().isEmpty(),
                        "A plain high-Bloom line can cultivate a hanging guava without harvesting it");
                ready(dog); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,8).with(Gene.DIET,1,8));
                level.setBlock(crop.above(2),Blocks.STONE.defaultBlockState(),3);
                level.setBlock(crop.above(),Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES,false),3);
                work(h,dog);
                h.assertTrue(level.getBlockState(crop.above()).getValue(CaveVines.BERRIES) && trough.food().isEmpty(),
                        "A high-Bloom cave forager can fruit an existing vine without extending it or creating stock");
            } finally {
                level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer());
                level.clockManager().setTotalTicks(clock,previous);
            }
            h.succeed();
        });
    }
}
