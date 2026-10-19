package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.item.TravelBasketItem;
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

/** Native personal work controls interrupt chores while preserving meals, saves and transport. */
public final class WorkPauseGameTest {
    private WorkPauseGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE); for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get()); var trough = h.absolutePos(new BlockPos(2,1,4)); var crop = h.absolutePos(new BlockPos(5,1,3));
        level.setBlock(trough,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
        h.setBlock(6,1,4,ModBlocks.FEEDING_TROUGH.get()); var stock = (FeedingTroughBlockEntity)level.getBlockEntity(trough); stock.insert(new ItemStack(Items.CARROT,9));
        level.setBlock(crop.below(),Blocks.FARMLAND.defaultBlockState(),3); var ripe = ((CropBlock)Blocks.CARROTS).getStateForAge(7); level.setBlock(crop,ripe,3);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4.5F,1,3.5F); dog.setAge(0);
        int forager = Gene.DIET.alleleNamed("forager").id(); var genes = Genome.DEFAULT.with(Gene.DIET,0,forager).with(Gene.DIET,1,forager);
        var stats = Stats.of(0,0,0,20,20); dog.setGenome(genes); dog.setStats(stats); dog.setHealth(dog.getMaxHealth());
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var hoe = new ItemStack(Items.IRON_HOE); hoe.setDamageValue(hoe.getMaxDamage()-1); player.setItemInHand(InteractionHand.OFF_HAND,hoe); player.setShiftKeyDown(true);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock); boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                var home = h.absolutePos(new BlockPos(1,1,1)); h.assertTrue(dog.moveHomeTo(level,home) == NestHome.MoveResult.MOVED,"Paused residents retain a real home");
                var job = new GardenGoal(dog); h.assertTrue(job.canUse(),"The fixture must have a real ready harvest"); job.start();
                player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
                h.assertTrue(!dog.workPaused() && job.canContinueToUse(),"Adventure cannot change a resident's work preference"); player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
                player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
                h.assertTrue(dog.workPaused() && dog.gardeningStatus() == Gardening.Status.PAUSED && !job.canContinueToUse()
                        && player.getOffhandItem() == hoe && hoe.getDamageValue() == hoe.getMaxDamage()-1,"Actual creative off-hand control must pause without wear or copy loss");
                for (int i = 0; i < Gardening.WORK_TICKS; i++) job.tick(); job.stop();
                h.assertTrue(level.getBlockState(crop).equals(ripe) && stock.food().getCount() == 9 && dog.gardeningWait() == 0
                        && dog.homeNest().equals(home) && WorkCensus.inspect(level,trough).orElseThrow().ready() == 0,"Pausing interrupts harvest without food, recovery or home changes");
                stock.setWorkMode(TroughWork.SUPPLY); h.assertTrue(!new SupplyGoal(dog).canUse(),"The same preference pauses supply jobs"); stock.setWorkMode(TroughWork.MIXED);
                dog.setHealth(dog.getMaxHealth()-4); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3,1,4))));
                h.assertTrue(dog.foragingStatus() == Foraging.Status.READY,"Personal work pause must leave injured residents ready for food");
                var meal = new TroughGoal(dog); h.assertTrue(meal.canUse(),"A paused worker still finds its meal trough"); meal.start();
                float health = dog.getHealth(); for (int i = 0; i < Foraging.GRAZE_TICKS; i++) meal.tick(); meal.stop();
                h.assertTrue(stock.food().getCount() == 8 && dog.getHealth() == health+Foraging.HEAL && dog.forageWait() == Foraging.MEAL_COOLDOWN && dog.workPaused(),"A normal meal still consumes/heals/rests while chores are paused");
                var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(output); var saved = output.buildResult();
                var restored = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD); restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
                h.assertTrue(restored.workPaused() && restored.genome().equals(genes) && restored.stats().equals(stats) && saved.getBooleanOr("WorkPaused",false),"The optional preference must preserve biology and native saves");
                saved.remove("WorkPaused"); restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved)); h.assertTrue(!restored.workPaused(),"Legacy data defaults to normal work even on reused entities");
                var basket = new ItemStack(ModItems.TRAVEL_BASKET.get()); var identity = dog.getUUID();
                h.assertTrue(TravelBasketItem.capture(level,basket,dog).success() && TravelBasketItem.release(level,h.absolutePos(new BlockPos(5,1,5)),basket).success(),"A paused animal must travel through an ordinary basket");
                var returned = (Bunnidog)level.getEntityInAnyDimension(identity); h.assertTrue(returned.workPaused() && returned.forageWait() == Foraging.MEAL_COOLDOWN,"Whole-animal travel retains the work preference and meal rest");
                player.interactOn(returned,InteractionHand.OFF_HAND,Vec3.ZERO); h.assertTrue(!returned.workPaused(),"The same hoe gesture resumes work");
                var pup = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4,1,1); pup.setAge(-1000); player.interactOn(pup,InteractionHand.OFF_HAND,Vec3.ZERO);
                h.assertTrue(pup.workPaused() && pup.isBaby(),"A pup may retain a future work preference without growth");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
