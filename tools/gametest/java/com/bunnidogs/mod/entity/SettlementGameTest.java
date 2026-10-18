package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class SettlementGameTest {
    private SettlementGameTest() {}
    public static void run(GameTestHelper h) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(4,1,4,ModBlocks.FEEDING_TROUGH.get());
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(4,1,4));
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(pos);
        var food = new ItemStack(Items.CARROT,64);
        food.set(DataComponents.CUSTOM_NAME,Component.literal("Garden carrots"));
        h.assertTrue(trough.insert(food) == 16 && trough.food().getCount() == 16
                && level.getBlockState(pos).getValue(FeedingTroughBlock.FILL) == 3,"Trough must cap stock and show its fill level");
        h.assertTrue(trough.insert(new ItemStack(Items.POTATO)) == 0 && trough.insert(new ItemStack(Items.CARROT)) == 0,
                "Different food/components must not merge with existing stock");
        var restored = (FeedingTroughBlockEntity)BlockEntity.loadStatic(pos,trough.getBlockState(),trough.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
        h.assertTrue(ItemStack.matches(restored.food(),trough.food()),"Food components and count must survive native block-entity save/load");
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,1,4);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth()-4);
        h.assertTrue(!trough.canFeed(dog),"Plain dogs must refuse another diet's stored food");
        dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,2).with(Gene.DIET,1,2));
        h.assertTrue(trough.canFeed(dog),"Expressed foragers must accept their stored produce");
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                h.assertTrue(dog.foragingStatus() == Foraging.Status.HOME,"An injured adult without a Nest must explain its missing home");
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Resident must claim its existing Nest");
                h.assertTrue(dog.foragingStatus() == Foraging.Status.READY,"Food AI and snapshot must agree when ready to search");
                var reader = h.makeMockPlayer(GameType.SURVIVAL);
                var homeBefore = dog.homeNest();
                CalipersItem.showCare(reader,dog);
                h.assertTrue(dog.getHealth() == dog.getMaxHealth()-4 && dog.forageWait() == 0
                        && dog.homeNest().equals(homeBefore) && trough.food().getCount() == 16,
                        "Home/food inspection must not change health, recovery, the Nest or stock");
                var goal = new TroughGoal(dog);
                h.assertTrue(goal.canUse(),"An injured resident must find a loaded reachable matching trough");
                float health = dog.getHealth();
                goal.start(); goal.tick();
                var power = pos.north(); level.setBlock(power,Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                h.assertTrue(trough.residentsPaused() && !goal.canContinueToUse() && !trough.takeMeal(dog),
                        "Native power must interrupt a meal and refuse the actual food reservation");
                for (int i = 0; i < Foraging.GRAZE_TICKS; i++) goal.tick();
                goal.stop();
                h.assertTrue(dog.getHealth() == health && dog.forageWait() == 0 && trough.food().getCount() == 16
                        && !new TroughGoal(dog).canUse(),"A paused meal preserves health, stock and recovery and prevents new searches");
                try (var tx = Transaction.openRoot()) {
                    h.assertTrue(trough.automation().extract(ItemResource.of(food),1,tx) == 1,"Automation may drain paused stock"); tx.commit();
                }
                try (var tx = Transaction.openRoot()) {
                    h.assertTrue(trough.automation().insert(ItemResource.of(food),1,tx) == 1,"Automation may refill paused stock"); tx.commit();
                }
                h.assertTrue(trough.residentsPaused() && trough.food().getCount() == 16 && trough.stockSignal() == 15,
                        "Power keeps native inventory transfers, named stock and comparator output available");
                level.setBlock(power,Blocks.AIR.defaultBlockState(),3);
                goal = new TroughGoal(dog); h.assertTrue(goal.canUse(),"Removing power must make the preserved meal available again");
                goal.start();
                for (int i = 0; i < Foraging.GRAZE_TICKS; i++) goal.tick();
                goal.stop();
                h.assertTrue(dog.getHealth() == health+Foraging.HEAL && trough.food().getCount() == 15 && !dog.canForage(),
                        "One meal must heal two HP, consume one item and start shared meal recovery");
                CalipersItem.showCare(reader,dog);
                h.assertTrue(dog.foragingStatus() == Foraging.Status.RESTING && dog.forageWait() == Foraging.MEAL_COOLDOWN,
                        "Reading shared meal recovery must neither restart nor bypass it");
                dog.setHealth(dog.getMaxHealth());
                h.assertTrue(dog.foragingStatus() == Foraging.Status.HEALTHY && dog.forageWait() == Foraging.MEAL_COOLDOWN,
                        "A fully healed animal must retain visible food recovery without claiming hunger");
                dog.setHealth(health+Foraging.HEAL);
                var beforeMove = dog.position();
                dog.setPos(homeBefore.getX()+18.5,homeBefore.getY(),homeBefore.getZ()+.5);
                h.assertTrue(dog.foragingStatus() == Foraging.Status.OUTSIDE_HOME,"Snapshot and food AI must share the home boundary");
                dog.setPos(beforeMove);
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                player.setShiftKeyDown(true); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                h.useBlock(new BlockPos(4,1,4),player);
                h.assertTrue(trough.food().isEmpty() && level.getBlockState(pos).getValue(FeedingTroughBlock.FILL) == 0,
                        "Sneak-empty use must withdraw the stock and clear the visible fill");
                dog.setHealth(dog.getMaxHealth());
                level.clockManager().setTotalTicks(clock,6000);
                h.setBlock(4,1,4,Blocks.BELL);
                // Native POI updates for the replacement are processed before gathering lookup.
                h.runAfterDelay(2,() -> {
                    try {
                        var gathering = new BellGatherGoal(dog);
                        h.assertTrue(gathering.canUse(),"A healthy resident must find a nearby bell and a reachable standing spot at noon");
                        var home = dog.homeNest();
                        gathering.start();
                        dog.setLeashedTo(player,false);
                        h.assertTrue(!gathering.canContinueToUse(),"Leads must interrupt gathering without changing the Nest");
                        gathering.stop(); dog.removeLeash();
                        h.assertTrue(home.equals(dog.homeNest()) && !gathering.canUse(),"Gathering must preserve the home and impose a rest interval");
                        level.clockManager().setTotalTicks(clock,13000);
                        h.assertTrue(!new BellGatherGoal(dog).canUse(),"Night must prevent gathering");
                        level.clockManager().setTotalTicks(clock,6000); dog.setHealth(dog.getMaxHealth()-1);
                        h.assertTrue(!new BellGatherGoal(dog).canUse(),"Injured residents must prioritize care over social visits");
                        h.succeed();
                    } finally { level.clockManager().setTotalTicks(clock,previous); }
                });
            } catch (Throwable failure) {
                level.clockManager().setTotalTicks(clock,previous); throw failure;
            }
        });
    }
}
