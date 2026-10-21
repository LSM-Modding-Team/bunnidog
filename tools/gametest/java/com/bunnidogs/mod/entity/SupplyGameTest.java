package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Full native source/destination journals, worker paths and post-commit container removal. */
public final class SupplyGameTest {
    private SupplyGameTest() {}
    private record Context(ServerLevel level, BlockPos from, BlockPos to, FeedingTroughBlockEntity source,
                           FeedingTroughBlockEntity target, Bunnidog dog, ItemStack food) {}
    private static Context setup(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE); for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get()); var from = h.absolutePos(new BlockPos(2,1,4)); var to = h.absolutePos(new BlockPos(6,1,4));
        level.setBlock(from,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
        level.setBlock(to,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);
        var source = (FeedingTroughBlockEntity)level.getBlockEntity(from); var target = (FeedingTroughBlockEntity)level.getBlockEntity(to);
        var food = new ItemStack(Items.CARROT,11); food.set(DataComponents.CUSTOM_NAME,Component.literal("Nursery supplies"));
        source.insert(food); source.setFilter(food); source.setWorkMode(TroughWork.SUPPLY); target.setFilter(food);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),4.5F,1,3.5F); dog.setAge(0);
        int forager = Gene.DIET.alleleNamed("forager").id(); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,forager).with(Gene.DIET,1,forager)); WorkerFixture.familiar(dog);
        dog.setStats(Stats.of(0,0,0,20,0)); dog.setHealth(dog.getMaxHealth());
        return new Context(level,from,to,source,target,dog,food);
    }
    private static void day(GameTestHelper h, Context c, Runnable run) {
        h.runAfterDelay(2,() -> {
            var clock = c.level.dimensionType().defaultClock().orElseThrow(); long time = c.level.clockManager().getTotalTicks(clock);
            boolean griefing = c.level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                c.level.clockManager().setTotalTicks(clock,1000); c.level.getGameRules().set(GameRules.MOB_GRIEFING,true,c.level.getServer());
                h.assertTrue(c.dog.moveHomeTo(c.level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Supply workers need an actual claimed home");
                run.run(); h.succeed();
            } finally { c.level.clockManager().setTotalTicks(clock,time); c.level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,c.level.getServer()); }
        });
    }
    private static void work(GameTestHelper h, Context c) {
        var job = new SupplyGoal(c.dog); h.assertTrue(job.canUse(),"Expected one supply job between the two real dishes"); job.start();
        h.assertTrue(HomeRoutes.safe(c.dog,c.dog.getNavigation().getPath(),null),"Supply requires a complete safe route");
        c.dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) job.tick(); job.stop();
    }
    public static void run(GameTestHelper h) {
        var c = setup(h); day(h,c,() -> {
            c.dog.setStats(Stats.of(0,0,0,19,0)); h.assertTrue(!new SupplyGoal(c.dog).canUse() && WorkCensus.inspect(c.level,c.from).orElseThrow().ready() == 0,"Supply needs Senses +20 in both work and roll call");
            c.dog.setStats(Stats.of(0,0,0,20,0)); c.source.insert(c.food.copyWithCount(5));
            h.assertTrue(WorkCensus.inspect(c.level,c.from).orElseThrow().ready() == 1,"A full supply source has useful surplus rather than a capacity refusal");
            c.source.takeAll(); c.source.insert(c.food);
            c.source.setWorkMode(TroughWork.MIXED); h.assertTrue(!new SupplyGoal(c.dog).canUse(),"Old mixed policy must not redistribute food"); c.source.setWorkMode(TroughWork.SUPPLY);
            c.target.setWorkMode(TroughWork.SUPPLY); h.assertTrue(!new SupplyGoal(c.dog).canUse(),"Supply sources are not other sources' destinations"); c.target.setWorkMode(TroughWork.MIXED);
            for (var dish : new BlockPos[]{c.from,c.to}) {
                c.level.setBlock(dish.north(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3); h.assertTrue(!new SupplyGoal(c.dog).canUse(),"Power on either side must refuse supply"); c.level.removeBlock(dish.north(),false);
            }
            c.target.clearFilter(); c.target.setFilter(new ItemStack(Items.CARROT)); h.assertTrue(!new SupplyGoal(c.dog).canUse(),"Destination filters match full components"); c.target.clearFilter(); c.target.setFilter(c.food);
            c.source.takeAll(); c.source.insert(c.food.copyWithCount(8)); h.assertTrue(!new SupplyGoal(c.dog).canUse(),"Eight source food remain reserved");
            c.source.takeAll(); c.source.clearFilter(); c.source.insert(new ItemStack(ModItems.GOLDEN_GUAVA.get(),9)); h.assertTrue(!new SupplyGoal(c.dog).canUse(),"Supply must leave upgraded breeding guavas");
            c.source.takeAll(); c.source.insert(c.food); c.source.setFilter(c.food);
            var saved = c.source.saveWithFullMetadata(c.level.registryAccess());
            var loaded = (FeedingTroughBlockEntity)BlockEntity.loadStatic(c.from,c.source.getBlockState(),saved,c.level.registryAccess());
            h.assertTrue(loaded.workMode() == TroughWork.SUPPLY && ItemStack.matches(loaded.food(),c.food),"Supply mode and whole stock retain the existing native save");
            try (var transaction = Transaction.openRoot()) {
                h.assertTrue(TroughSupply.reserve(c.source,c.target,c.food.copyWithCount(1),transaction) && c.source.food().getCount() == 10 && c.target.food().getCount() == 1,"One journal reserves both real stacks");
            }
            h.assertTrue(ItemStack.matches(c.source.food(),c.food) && c.target.food().isEmpty(),"Aborting a paired transfer must restore both exact stacks");
            c.target.insert(c.food.copyWithCount(16));
            try (var transaction = Transaction.openRoot()) { h.assertTrue(!TroughSupply.reserve(c.source,c.target,c.food.copyWithCount(1),transaction),"A full native destination must reject the reservation"); }
            h.assertTrue(ItemStack.matches(c.source.food(),c.food) && c.target.food().getCount() == 16,"Destination refusal must roll back the already reserved source"); c.target.takeAll();
            float health = c.dog.getHealth(); work(h,c);
            h.assertTrue(c.source.food().getCount() == 10 && ItemStack.matches(c.target.food(),c.food.copyWithCount(1)) && c.dog.getHealth() == health
                    && ItemStack.isSameItemSameComponents(c.source.filter(),c.food) && ItemStack.isSameItemSameComponents(c.target.filter(),c.food)
                    && c.dog.gardeningWait() == Gardening.REST_TICKS && !new SupplyGoal(c.dog).canUse(),"One job moves one named food, preserves filters/health and shares garden rest");
            LooseFoodGameTest.ready(c.dog,"GardenWait"); c.target.insert(c.food.copyWithCount(6)); work(h,c);
            LooseFoodGameTest.ready(c.dog,"GardenWait");
            h.assertTrue(c.source.food().getCount() == 9 && c.target.food().getCount() == 8 && !new SupplyGoal(c.dog).canUse()
                    && c.level.getEntitiesOfClass(ItemEntity.class,new AABB(c.from).inflate(6)).isEmpty(),"The destination ceiling stops at eight with no loose or carried copy");
        });
    }
    public static void callbacks(GameTestHelper h) {
        var c = setup(h); day(h,c,() -> {
            Consumer<EntityMobGriefingEvent> pause = event -> { if (event.getEntity() == c.dog) c.source.setWorkMode(TroughWork.GATHER); };
            NeoForge.EVENT_BUS.addListener(pause);
            try { work(h,c); } finally { NeoForge.EVENT_BUS.unregister(pause); }
            h.assertTrue(ItemStack.matches(c.source.food(),c.food) && c.target.food().isEmpty() && c.dog.gardeningWait() == 0,"Mode changed by protection must preserve stock and work rest"); c.source.setWorkMode(TroughWork.SUPPLY);
            Consumer<EntityMobGriefingEvent> reserve = event -> { if (event.getEntity() == c.dog) { c.target.clearFilter(); c.target.setFilter(new ItemStack(Items.CARROT)); } };
            NeoForge.EVENT_BUS.addListener(reserve);
            try { work(h,c); } finally { NeoForge.EVENT_BUS.unregister(reserve); }
            h.assertTrue(ItemStack.matches(c.source.food(),c.food) && c.target.food().isEmpty() && c.dog.gardeningWait() == 0,"An exact filter changed by protection must cancel without input loss"); c.target.clearFilter(); c.target.setFilter(c.food);
            boolean[] removed = {false};
            Consumer<NeighborNotifyEvent> remove = event -> {
                if (!removed[0] && event.getLevel() == c.level && event.getPos().equals(c.from) && c.source.food().getCount() == 10) {
                    removed[0] = true; c.level.removeBlock(c.from,false); c.level.removeBlock(c.to,false);
                }
            };
            NeoForge.EVENT_BUS.addListener(remove);
            try { work(h,c); } finally { NeoForge.EVENT_BUS.unregister(remove); }
            var drops = c.level.getEntitiesOfClass(ItemEntity.class,new AABB(c.from).inflate(6),e -> ItemStack.isSameItemSameComponents(e.getItem(),c.food));
            h.assertTrue(removed[0] && drops.stream().mapToInt(e -> e.getItem().getCount()).sum() == 11 && c.source.food().isEmpty() && c.target.food().isEmpty()
                    && c.level.getBlockState(c.from).isAir() && c.level.getBlockState(c.to).isAir() && c.dog.gardeningWait() == Gardening.REST_TICKS,
                    "Removing both troughs in committed stock notifications must drop exactly their eleven real items without a ghost or lost transfer");
        });
    }
}
