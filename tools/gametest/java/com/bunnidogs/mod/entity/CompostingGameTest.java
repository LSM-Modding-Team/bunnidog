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
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class CompostingGameTest {
    private CompostingGameTest() {}
    private static void ready(Bunnidog dog) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.level().registryAccess());
        dog.saveWithoutId(output); var saved = output.buildResult(); saved.remove("GardenWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,dog.level().registryAccess(),saved));
    }
    private static void work(GameTestHelper h, Bunnidog dog) {
        var goal = new GardenGoal(dog);
        h.assertTrue(goal.canUse(),"A ready resident must find the expected native work target"); goal.start();
        dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,3))));
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
        h.setBlock(6,0,4,Blocks.REDSTONE_BLOCK);
        h.setBlock(new BlockPos(5,0,4),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.ENABLED,false));
        h.setBlock(5,1,4,Blocks.COMPOSTER);
        var troughPos = h.absolutePos(new BlockPos(4,1,4)); var composter = troughPos.east();
        var stock = (FeedingTroughBlockEntity)level.getBlockEntity(troughPos);
        var output = (HopperBlockEntity)level.getBlockEntity(composter.below());
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,4.5F); dog.setAge(0);
        var forager = Genome.DEFAULT.with(Gene.DIET,0,2).with(Gene.DIET,1,2); dog.setGenome(forager);
        var carrots = new ItemStack(Items.CARROT,9); carrots.set(DataComponents.CUSTOM_NAME,Component.literal("Compost reserve"));
        stock.insert(carrots); stock.setFilter(carrots);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long previous = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"A composter worker needs a real claimed home");
                dog.setStats(Stats.of(0,0,0,0,9));
                h.assertTrue(!new GardenGoal(dog).canUse(),"Low Bloom must leave surplus food untouched");
                dog.setStats(Stats.of(0,0,0,0,10));
                var job = new GardenGoal(dog); h.assertTrue(job.canUse(),"Bloom +10 must find the reachable native composter"); job.start();
                try (var tx = Transaction.openRoot()) { stock.automation().extract(ItemResource.of(carrots),1,tx); tx.commit(); }
                h.assertTrue(!job.canContinueToUse() && stock.food().getCount() == 8 && dog.gardeningWait() == 0,
                        "Committed stock extraction down to the reserve must stop work before cost or recovery"); job.stop();
                h.assertTrue(!new GardenGoal(dog).canUse(),"Exactly eight items must remain reserved for food"); stock.insert(carrots.copyWithCount(1));
                job = new GardenGoal(dog); h.assertTrue(job.canUse(),"Restored surplus may be composted"); job.start(); dog.setStats(Stats.ZERO);
                h.assertTrue(!job.canContinueToUse() && stock.food().getCount() == 9,"Losing Bloom must interrupt composting immediately"); job.stop(); dog.setStats(Stats.of(0,0,0,0,10));
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
                h.assertTrue(!new GardenGoal(dog).canUse(),"Mob-griefing rules must also block surplus composting"); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                Consumer<LivingDestroyBlockEvent> protect = event -> { if (event.getEntity() == dog && event.getPos().equals(composter)) event.setCanceled(true); };
                NeoForge.EVENT_BUS.addListener(protect);
                try { work(h,dog); } finally { NeoForge.EVENT_BUS.unregister(protect); }
                h.assertTrue(stock.food().getCount() == 9 && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 0,
                        "Native block protection must preserve both compost and real input"); ready(dog);
                stock.takeAll(); stock.clearFilter(); stock.insert(new ItemStack(Items.CARROT,9));
                var crop = composter.north(); level.setBlock(crop.below(),Blocks.FARMLAND.defaultBlockState(),3);
                var plant = (CropBlock)Blocks.CARROTS; level.setBlock(crop,plant.getStateForAge(plant.getMaxAge()),3);
                work(h,dog);
                h.assertTrue(plant.getAge(level.getBlockState(crop)) == 0 && stock.food().getCount() == 10
                        && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 0,"Ripe harvesting must take priority over composting surplus");
                ready(dog); work(h,dog);
                h.assertTrue(plant.getAge(level.getBlockState(crop)) == 1 && stock.food().getCount() == 10
                        && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 0,"One-stage cultivation must take priority over composting surplus");
                level.setBlock(crop,Blocks.AIR.defaultBlockState(),3); ready(dog); stock.takeAll(); stock.insert(carrots); stock.setFilter(carrots);
                work(h,dog);
                h.assertTrue(stock.food().getCount() == 8 && ItemStack.isSameItemSameComponents(stock.food(),carrots)
                        && ItemStack.isSameItemSameComponents(stock.filter(),carrots) && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 1
                        && dog.gardeningWait() == Gardening.REST_TICKS && !new GardenGoal(dog).canUse(),
                        "Native first-layer work must consume one actual item, keep the named eight-item reserve/filter and share crop recovery");
                ready(dog); stock.takeAll(); stock.clearFilter(); stock.insert(new ItemStack(Items.COOKED_BEEF,9));
                dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,3).with(Gene.DIET,1,3));
                h.assertTrue(!new GardenGoal(dog).canUse(),"Compatible but non-compostable meat must never be spent");
                stock.takeAll(); stock.insert(carrots); stock.setFilter(carrots); dog.setGenome(forager);
                level.setBlock(composter,Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL,6),3);
                level.getRandom().setSeed(0); work(h,dog);
                h.assertTrue(stock.food().getCount() == 8 && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 6,
                        "A native failed compost chance still costs exactly one input and keeps the food reserve");
                ready(dog); stock.insert(carrots.copyWithCount(1)); level.getRandom().setSeed(4096); work(h,dog);
                h.assertTrue(stock.food().getCount() == 8 && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 7,
                        "A successful native compost chance must reach the maturation stage for one input");
            } finally {
                level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); level.clockManager().setTotalTicks(clock,previous);
            }
            h.runAfterDelay(22,() -> {
                h.assertTrue(level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == ComposterBlock.READY,
                        "The real scheduled composter tick must mature the resident's compost");
                level.setBlock(composter.east().below(),Blocks.STONE.defaultBlockState(),3);
                for (int tick = 0; tick < 20; tick++) HopperBlockEntity.pushItemsTick(level,composter.below(),output.getBlockState(),output);
                h.assertTrue(output.getItem(0).is(Items.BONE_MEAL) && output.getItem(0).getCount() == 1
                        && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 0 && stock.food().getCount() == 8,
                        "An actual native hopper must collect one bone meal and empty compost without touching the food reserve");
                h.assertTrue(Math.abs(ComposterBlock.getValue(new ItemStack(ModItems.GUAVA.get()))-.65F) < .0001F
                        && Math.abs(ComposterBlock.getValue(new ItemStack(ModItems.GUAVA_LEAVES.get()))-.3F) < .0001F
                        && Math.abs(ComposterBlock.getValue(new ItemStack(ModItems.GUAVA_SAPLING.get()))-.3F) < .0001F,
                        "The actual native data map must register ordinary guava fruit and foliage compost values");
                h.assertTrue(ModItems.GUAVAS.stream().skip(1).allMatch(tier -> ComposterBlock.getValue(new ItemStack(tier.get())) <= 0),
                        "Quality-infused guavas must stay outside automatic composting");
                long before = level.clockManager().getTotalTicks(clock);
                boolean rule = level.getGameRules().get(GameRules.MOB_GRIEFING);
                try {
                    level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                    ready(dog); dog.setGenome(Genome.DEFAULT); stock.takeAll(); stock.clearFilter(); stock.insert(new ItemStack(ModItems.GUAVA.get(),9));
                    work(h,dog);
                    h.assertTrue(stock.food().is(ModItems.GUAVA.get()) && stock.food().getCount() == 8
                            && level.getBlockState(composter).getValue(ComposterBlock.LEVEL) == 1 && dog.gardeningWait() == Gardening.REST_TICKS,
                            "A plain high-Bloom resident must compost its actual surplus guavas through the same native work path");
                } finally { level.clockManager().setTotalTicks(clock,before); level.getGameRules().set(GameRules.MOB_GRIEFING,rule,level.getServer()); }
                h.succeed();
            });
        });
    }
}
