package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;

/** Actual controls, real food costs and callback-safe restoration/nursery jobs. */
public final class ResidentWorkGameTest {
    private ResidentWorkGameTest() {}
    private record Context(ServerLevel level, BlockPos trough, BlockPos bed, Bunnidog dog) {
        FeedingTroughBlockEntity stock() { return (FeedingTroughBlockEntity)this.level.getBlockEntity(this.trough); }
    }
    private static Context setup(GameTestHelper h, TroughWork mode) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(new BlockPos(3,1,4),ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F); dog.setAge(0); dog.setGenome(Genome.DEFAULT);
        dog.setStats(Stats.of(0,0,0,0,10)); dog.setHealth(dog.getMaxHealth());
        var context = new Context(h.getLevel(),h.absolutePos(new BlockPos(3,1,4)),h.absolutePos(new BlockPos(5,1,4)),dog);
        context.stock().setWorkMode(mode); return context;
    }
    private static void ready(Bunnidog dog) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.level().registryAccess()); dog.saveWithoutId(out);
        var saved = out.buildResult(); saved.remove("GardenWait"); dog.load(TagValueInput.create(ProblemReporter.DISCARDING,dog.level().registryAccess(),saved));
    }
    private static void day(GameTestHelper h, Context c, Runnable run) {
        h.runAfterDelay(2,() -> {
            var clock = c.level.dimensionType().defaultClock().orElseThrow(); long time = c.level.clockManager().getTotalTicks(clock);
            boolean griefing = c.level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                c.level.clockManager().setTotalTicks(clock,1000); c.level.getGameRules().set(GameRules.MOB_GRIEFING,true,c.level.getServer());
                h.assertTrue(c.dog.moveHomeTo(c.level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Workers need a real reachable claimed Nest");
                run.run(); h.succeed();
            } finally { c.level.clockManager().setTotalTicks(clock,time); c.level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,c.level.getServer()); }
        });
    }
    private static void work(GameTestHelper h, Context c) {
        var goal = new GardenGoal(c.dog); h.assertTrue(goal.canUse(),"Expected an eligible resident work target"); goal.start();
        h.assertTrue(HomeRoutes.safe(c.dog,c.dog.getNavigation().getPath(),null),"Work must choose a complete safe native route");
        c.dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) goal.tick(); goal.stop();
    }
    public static void modes(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(3,1,3)); h.setBlock(3,1,3,ModBlocks.FEEDING_TROUGH.get());
        var stock = (FeedingTroughBlockEntity)level.getBlockEntity(pos); var player = h.makeMockServerPlayerInLevel();
        var hoe = new ItemStack(Items.IRON_HOE); player.setItemInHand(InteractionHand.OFF_HAND,hoe); player.setShiftKeyDown(true);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        h.assertTrue(stock.workMode() == TroughWork.MIXED,"Old/new troughs must retain the existing mixed routine by default");
        h.assertTrue(player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit).consumesAction()
                && stock.workMode() == TroughWork.GARDEN && level.getBlockState(pos).getValue(FeedingTroughBlock.TENDING),
                "Actual crouched off-hand hoe use must select and enable Garden mode");
        player.setShiftKeyDown(false); player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit);
        h.assertTrue(!level.getBlockState(pos).getValue(FeedingTroughBlock.TENDING) && stock.workMode() == TroughWork.GARDEN,"Ordinary hoe use pauses without losing the selected mode");
        player.setShiftKeyDown(true);
        for (var expected : new TroughWork[]{TroughWork.GATHER,TroughWork.COMPOST,TroughWork.NURSERY,TroughWork.MIXED}) {
            player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit);
            h.assertTrue(stock.workMode() == expected && hoe.getDamageValue() == 0,"Mode cycling must be stable and spend no tool durability");
        }
        var named = new ItemStack(Items.CARROT,9); named.set(DataComponents.CUSTOM_NAME,Component.literal("Work reserve"));
        stock.setFilter(named); stock.insert(named); stock.setWorkMode(TroughWork.NURSERY);
        var saved = stock.saveWithFullMetadata(level.registryAccess());
        var loaded = (FeedingTroughBlockEntity)BlockEntity.loadStatic(pos,stock.getBlockState(),saved,level.registryAccess());
        h.assertTrue(loaded.workMode() == TroughWork.NURSERY && ItemStack.matches(stock.food(),loaded.food()) && ItemStack.matches(stock.filter(),loaded.filter()),
                "Native saves must preserve mode and exact food/filter without another inventory");
        saved.remove("WorkMode"); var legacy = (FeedingTroughBlockEntity)BlockEntity.loadStatic(pos,stock.getBlockState(),saved,level.registryAccess());
        saved.putString("WorkMode","unknown_future_mode"); var unknown = (FeedingTroughBlockEntity)BlockEntity.loadStatic(pos,stock.getBlockState(),saved,level.registryAccess());
        h.assertTrue(legacy.workMode() == TroughWork.MIXED && unknown.workMode() == TroughWork.MIXED,"Legacy and unknown modes must safely preserve mixed work");
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); player.gameMode.useItemOn(player,level,hoe,InteractionHand.OFF_HAND,hit);
        h.assertTrue(stock.workMode() == TroughWork.NURSERY && stock.food().getCount() == 9,"Adventure mode must not alter the work policy or its food");
        h.succeed();
    }
    public static void restoration(GameTestHelper h) {
        var c = setup(h,TroughWork.GARDEN); var level = c.level; var dog = c.dog;
        int forager = Gene.DIET.alleleNamed("forager").id(); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,forager).with(Gene.DIET,1,forager));
        level.setBlock(c.bed.below(),Blocks.FARMLAND.defaultBlockState(),3);
        var named = new ItemStack(Items.CARROT,9); named.set(DataComponents.CUSTOM_NAME,Component.literal("Seed carrots"));
        c.stock().setFilter(named); c.stock().insert(named.copyWithCount(8));
        day(h,c,() -> {
            h.assertTrue(!new GardenGoal(dog).canUse(),"Replanting must leave the eight-food reserve intact"); c.stock().insert(named.copyWithCount(1));
            dog.setStats(Stats.of(0,0,0,0,9)); h.assertTrue(!new GardenGoal(dog).canUse(),"Bloom below +10 must not restore gaps"); dog.setStats(Stats.of(0,0,0,0,10));
            c.stock().setWorkMode(TroughWork.MIXED); h.assertTrue(!new GardenGoal(dog).canUse(),"Existing mixed work must never start the new food-spending planting job"); c.stock().setWorkMode(TroughWork.GARDEN);
            Consumer<EntityPlaceEvent> veto = event -> { if (event.getEntity() == dog && event.getPos().equals(c.bed)) event.setCanceled(true); };
            NeoForge.EVENT_BUS.addListener(veto); try { work(h,c); } finally { NeoForge.EVENT_BUS.unregister(veto); }
            h.assertTrue(level.getBlockState(c.bed).isAir() && c.stock().food().getCount() == 9 && dog.gardeningWait() == 0,"Native placement refusal must refund the exact reserved food without recovery");
            Consumer<EntityPlaceEvent> replace = event -> { if (event.getEntity() == dog && event.getPos().equals(c.bed)) level.setBlock(c.bed,Blocks.CHEST.defaultBlockState(),3); };
            NeoForge.EVENT_BUS.addListener(replace); try { work(h,c); } finally { NeoForge.EVENT_BUS.unregister(replace); }
            h.assertTrue(level.getBlockEntity(c.bed) != null && level.getBlockState(c.bed).is(Blocks.CHEST) && c.stock().food().getCount() == 9,"A protection callback's new container must survive without food loss"); level.removeBlock(c.bed,false);
            Consumer<EntityPlaceEvent> removeSource = event -> { if (event.getEntity() == dog && event.getPos().equals(c.bed)) level.removeBlock(c.trough,false); };
            NeoForge.EVENT_BUS.addListener(removeSource); try { work(h,c); } finally { NeoForge.EVENT_BUS.unregister(removeSource); }
            var drops = level.getEntitiesOfClass(ItemEntity.class,new AABB(c.trough).inflate(4),e -> e.getItem().is(Items.CARROT));
            h.assertTrue(level.getBlockState(c.bed).isAir() && drops.stream().mapToInt(e -> e.getItem().getCount()).sum() == 9
                    && drops.stream().allMatch(e -> ItemStack.isSameItemSameComponents(e.getItem(),named)),"Reentrant source removal must preserve the eight stored and one reserved named item exactly once"); drops.forEach(ItemEntity::discard);
            level.setBlock(c.trough,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
            c.stock().setWorkMode(TroughWork.GARDEN); c.stock().setFilter(named); c.stock().insert(named); work(h,c);
            h.assertTrue(level.getBlockState(c.bed).is(Blocks.CARROTS) && ((CropBlock)Blocks.CARROTS).getAge(level.getBlockState(c.bed)) == 0
                    && c.stock().food().getCount() == 8 && ItemStack.isSameItemSameComponents(c.stock().food(),named)
                    && dog.gardeningWait() == Gardening.REST_TICKS,"Restoring one carrot must cost one real item, keep exact remainder/filter and share work recovery");
            h.assertTrue(!new GardenGoal(dog).canUse(),"Planting must obey shared recovery"); ready(dog); c.stock().takeAll(); c.stock().clearFilter();
            dog.setGenome(Genome.DEFAULT); c.stock().insert(new ItemStack(ModItems.GUAVA.get(),9)); level.removeBlock(c.bed,false);
            level.setBlock(c.bed.above(),Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3); work(h,c);
            h.assertTrue(level.getBlockState(c.bed).is(ModBlocks.GUAVA_FRUIT.get()) && c.stock().food().getCount() == 8,"Plain lines must establish hanging fruit for exactly one ordinary surplus guava");
            level.removeBlock(c.bed,false); level.removeBlock(c.bed.above(),false); level.setBlock(c.bed.below(),Blocks.SAND.defaultBlockState(),3);
            dog.setPos(Vec3.atBottomCenterOf(c.bed));
            h.assertTrue(ResidentPlanting.plan(level,c.bed,new ItemStack(Items.CACTUS)) == null,"Cactus restoration must not place a damaging plant through a living animal");
            h.assertTrue(ResidentPlanting.plan(level,c.bed,new ItemStack(ModItems.GOLDEN_GUAVA.get())) == null
                    && ResidentPlanting.plan(level,c.bed,SeedItem.create(new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,1))) == null,
                    "Upgraded guavas and embryo seeds must never become resident planting inputs");
        });
    }
    public static void nursery(GameTestHelper h) {
        var c = setup(h,TroughWork.NURSERY); var level = c.level; var dog = c.dog;
        level.setBlock(c.bed,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3); var cradle = (NurseryCradleBlockEntity)level.getBlockEntity(c.bed);
        var seed = SeedItem.create(new Seed(Genome.DEFAULT,Stats.of(1,2,3,4,5),Genome.DEFAULT,Stats.of(5,4,3,2,1),2.5,true));
        seed.set(DataComponents.CUSTOM_NAME,Component.literal("Nursery pedigree")); cradle.insert(seed);
        c.stock().insert(new ItemStack(ModItems.GUAVA.get(),9)); dog.setStats(Stats.of(0,0,0,0,19));
        day(h,c,() -> {
            h.assertTrue(!new GardenGoal(dog).canUse(),"Nursery work requires Bloom +20"); dog.setStats(Stats.of(0,0,0,0,20));
            c.stock().setWorkMode(TroughWork.MIXED); h.assertTrue(!new GardenGoal(dog).canUse(),"Nursery spending must require its selected mode"); c.stock().setWorkMode(TroughWork.NURSERY);
            c.stock().takeAll(); c.stock().insert(new ItemStack(ModItems.GOLDEN_GUAVA.get(),9)); h.assertTrue(!new GardenGoal(dog).canUse(),"Nurses must preserve upgraded breeding food"); c.stock().takeAll();
            c.stock().insert(new ItemStack(ModItems.GUAVA.get(),8)); h.assertTrue(!new GardenGoal(dog).canUse(),"Nursery work must retain its eight-guava food reserve"); c.stock().insert(new ItemStack(ModItems.GUAVA.get(),3));
            var replacement = SeedItem.create(new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,7));
            Consumer<NeighborNotifyEvent> swap = event -> { if (event.getLevel() == level && event.getPos().equals(c.trough) && c.stock().food().getCount() == 10) { cradle.takeSeed(); cradle.insert(replacement); } };
            NeoForge.EVENT_BUS.addListener(swap); try { work(h,c); } finally { NeoForge.EVENT_BUS.unregister(swap); }
            h.assertTrue(c.stock().food().getCount() == 11 && cradle.age() == 0 && ItemStack.matches(cradle.storedSeed(),replacement)
                    && dog.gardeningWait() == 0,"A litter replaced inside stock notifications must keep its stage and refund the reserved guava");
            cradle.takeSeed(); cradle.insert(seed); work(h,c);
            h.assertTrue(cradle.age() == 1 && cradle.growthWait() == NurseryCradleBlockEntity.STAGE_TICKS && ItemStack.matches(cradle.storedSeed(),seed)
                    && c.stock().food().getCount() == 10 && dog.gardeningWait() == Gardening.REST_TICKS,
                    "A successful nurse job spends one guava for exactly one stage while preserving the whole treated named litter");
            ready(dog); cradle.grow(true);
            h.assertTrue(cradle.age() == SeedCropBlock.MAX_AGE && !new GardenGoal(dog).canUse() && ItemStack.matches(cradle.storedSeed(),seed),
                    "Ripe Cradles await explicit player/redstone hatching and cannot consume more food");
            h.assertTrue(level.getEntitiesOfClass(Bunnidog.class,new AABB(c.bed).inflate(4),Bunnidog::isAlive).size() == 1,"Nurses must never hatch pups on their own");
        });
    }
}
