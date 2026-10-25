package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Real stock/entity journal rollback and commitment, including native stock notifications removing the trough. */
public final class FoodGatheringGameTest {
    private FoodGatheringGameTest() {}
    public static void run(GameTestHelper h) {
        LooseFoodGameTest.clear(h); var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(3,1,4));
        level.setBlock(pos,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
        var stock = (FeedingTroughBlockEntity)level.getBlockEntity(pos);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F); dog.setAge(0);
        int forager = Gene.DIET.alleleNamed("forager").id(); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,forager).with(Gene.DIET,1,forager));
        dog.setStats(Stats.of(0,0,0,9,0)); dog.setHealth(dog.getMaxHealth());WorkerFixture.familiar(dog);
        var original = new ItemStack(Items.CARROT,3); original.set(DataComponents.CUSTOM_NAME,Component.literal("Market carrots"));
        var item = LooseFoodGameTest.food(h,original.copy());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Gathering requires a real home");
                h.assertTrue(!new LooseFoodGoal(dog,true).canUse(),"Senses nine must not collect loose food"); dog.setStats(Stats.of(0,0,0,10,0));
                stock.setFilter(new ItemStack(Items.POTATO)); h.assertTrue(!new LooseFoodGoal(dog,true).canUse(),"Exact trough reservations apply to gathering"); stock.clearFilter(); stock.setFilter(original);
                var unit = original.copyWithCount(1);
                try (var transaction = Transaction.openRoot()) {
                    h.assertTrue(LooseFood.reserve(item,stock,unit,transaction) && item.getItem().getCount() == 2 && stock.food().getCount() == 1,"One native journal must reserve both source and destination");
                }
                h.assertTrue(ItemStack.matches(item.getItem(),original) && stock.food().isEmpty() && !item.isRemoved()
                        && level.getBlockState(pos).getValue(FeedingTroughBlock.FILL) == 0,"Aborted transfers must restore whole items and keep native stock signals unchanged");
                var interrupted = new LooseFoodGoal(dog,true); h.assertTrue(interrupted.canUse(),"A qualified worker must find loose matching food beside its enabled trough");
                Consumer<EntityMobGriefingEvent> powerAtCommit = event -> { if (event.getEntity() == dog) level.setBlock(pos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3); };
                NeoForge.EVENT_BUS.addListener(powerAtCommit);
                try { LooseFoodGameTest.work(h,dog,interrupted,Gardening.WORK_TICKS); } finally { NeoForge.EVENT_BUS.unregister(powerAtCommit); }
                h.assertTrue(stock.food().isEmpty() && ItemStack.matches(item.getItem(),original) && dog.gardeningWait() == 0,"Power added in protection callbacks must preserve both items and work recovery"); level.setBlock(pos.west(),Blocks.AIR.defaultBlockState(),3);
                var gather = new LooseFoodGoal(dog,true); h.assertTrue(gather.canUse(),"Unpaused stock must allow collection"); LooseFoodGameTest.work(h,dog,gather,Gardening.WORK_TICKS);
                h.assertTrue(ItemStack.matches(stock.food(),unit) && ItemStack.matches(item.getItem(),original.copyWithCount(2)) && dog.gardeningWait() == Gardening.REST_TICKS
                        && level.getBlockState(pos).getValue(FeedingTroughBlock.FILL) == 1,"One job must move one exact item and update native stock with shared garden rest");
                h.assertTrue(!new LooseFoodGoal(dog,true).canUse(),"Gathering must share rest with all garden work"); LooseFoodGameTest.ready(dog,"GardenWait");
                stock.insert(original.copyWithCount(15)); h.assertTrue(!new LooseFoodGoal(dog,true).canUse(),"Full troughs must keep loose stock intact");
                try (var transaction = Transaction.openRoot()) {
                    h.assertTrue(!LooseFood.reserve(item,stock,unit,transaction),"A full destination must refuse the reservation");
                }
                h.assertTrue(stock.food().getCount() == 16 && ItemStack.matches(item.getItem(),original.copyWithCount(2)),"A refused destination must roll back an already reserved ground item"); stock.takeAll();
                // Real fill notifications may remove the destination after commit. Its normal drops must conserve the paid item.
                Consumer<NeighborNotifyEvent> removeAtNotification = event -> {
                    if (event.getLevel() == level && event.getPos().equals(pos) && level.getBlockState(pos).is(ModBlocks.FEEDING_TROUGH.get())
                            && level.getBlockState(pos).getValue(FeedingTroughBlock.FILL) == 1) level.removeBlock(pos,false);
                };
                var finalJob = new LooseFoodGoal(dog,true); h.assertTrue(finalJob.canUse(),"An empty trough can accept one more paid item"); NeoForge.EVENT_BUS.addListener(removeAtNotification);
                try { LooseFoodGameTest.work(h,dog,finalJob,Gardening.WORK_TICKS); } finally { NeoForge.EVENT_BUS.unregister(removeAtNotification); }
                int total = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(4),e -> ItemStack.isSameItemSameComponents(e.getItem(),unit))
                        .stream().mapToInt(e -> e.getItem().getCount()).sum();
                h.assertTrue(level.getBlockState(pos).isAir() && item.getItem().getCount() == 1 && total == 2 && dog.gardeningWait() == Gardening.REST_TICKS,
                        "A trough removed by native stock notifications must drop exactly the paid item beside the remaining ground food");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
