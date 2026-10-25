package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** Actual paid work teaches; sessions remain local and transient while knowledge belongs to the individual. */
public final class LearningGameTest {
    private LearningGameTest() {}
    private record Context(GameTestHelper h, Bunnidog dog, ServerPlayer teacher, BlockPos source, BlockPos target) {
        FeedingTroughBlockEntity stock(BlockPos pos) { return (FeedingTroughBlockEntity)h.getLevel().getBlockEntity(pos); }
    }
    private static Context setup(GameTestHelper h, boolean delivery) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x, 0, z, Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x, y, z, Blocks.AIR);
        }
        h.setBlock(1, 1, 1, ModBlocks.NEST.get());
        var source = h.absolutePos(new BlockPos(delivery ? 2 : 4, 1, 4));
        var target = h.absolutePos(new BlockPos(delivery ? 6 : 5, 1, 4));
        h.getLevel().setBlock(source, ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING, true), 3);
        if (delivery) h.getLevel().setBlock(target, ModBlocks.FEEDING_TROUGH.get().defaultBlockState(), 3);
        else {
            h.getLevel().setBlock(target.below(), Blocks.FARMLAND.defaultBlockState(), 3);
            ripe(h, target);
        }
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 4.5F, 1, 3.5F);
        int forager = Gene.DIET.alleleNamed("forager").id();
        dog.setGenome(Genome.DEFAULT.with(Gene.DIET, 0, forager).with(Gene.DIET, 1, forager));
        dog.setAge(0); dog.setStats(Stats.of(0, 0, 0, 20, 20)); dog.setHealth(dog.getMaxHealth());
        var teacher = h.makeMockServerPlayerInLevel(); CareMenuGameTest.prepare(teacher);
        teacher.snapTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4, 1, 2))));
        var c = new Context(h, dog, teacher, source, target);
        c.stock(source).setWorkMode(delivery ? TroughWork.SUPPLY : TroughWork.GARDEN);
        if (delivery) {
            var carrots = new ItemStack(Items.CARROT, 11); carrots.set(DataComponents.CUSTOM_NAME, Component.literal("Lesson supplies"));
            c.stock(source).insert(carrots); c.stock(target).setFilter(carrots);
        }
        return c;
    }
    private static void ripe(GameTestHelper h, BlockPos target) {
        var plant = (CropBlock)Blocks.CARROTS;
        h.getLevel().setBlock(target, plant.getStateForAge(plant.getMaxAge()), 3);
    }
    private static CompoundTag save(Bunnidog dog) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, dog.level().registryAccess());
        dog.saveWithoutId(out); return out.buildResult();
    }
    private static void ready(Bunnidog dog) {
        var tag = save(dog); tag.remove("GardenWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, dog.level().registryAccess(), tag));
    }
    private static void day(Context c, Runnable work) {
        c.h.runAfterDelay(2, () -> {
            var level = c.h.getLevel(); var clock = level.dimensionType().defaultClock().orElseThrow();
            long previous = level.clockManager().getTotalTicks(clock); boolean grief = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock, 1000); level.getGameRules().set(GameRules.MOB_GRIEFING, true, level.getServer());
                c.h.assertTrue(c.dog.moveHomeTo(level, c.h.absolutePos(new BlockPos(1, 1, 1))) == NestHome.MoveResult.MOVED, "A learner needs a real claimed home");
                work.run(); c.h.succeed();
            } finally { level.clockManager().setTotalTicks(clock, previous); level.getGameRules().set(GameRules.MOB_GRIEFING, grief, level.getServer()); }
        });
    }
    private static void teachFromMenu(Context c, Learning.Routine routine) {
        CareMenu.open(c.teacher, c.dog);
        c.h.assertTrue(c.teacher.containerMenu instanceof CareMenu, "Learning must be reachable through ordinary animal care");
        var menu = (CareMenu)c.teacher.containerMenu;
        c.h.assertTrue(menu.clickMenuButton(c.teacher, menu.buttonId(CarePanels.ROLES)), "Skills and roles opens");
        c.h.assertTrue(menu.clickMenuButton(c.teacher, menu.buttonId(LearningPanel.OPEN)), "Lessons opens");
        c.h.assertTrue(menu.clickMenuButton(c.teacher, menu.buttonId(LearningPanel.TEACH + routine.ordinal())), "The authoritative lesson button starts practice");
        c.h.assertTrue(c.teacher.containerMenu!=menu,"Beginning a real lesson closes conversation so the animal can do the work");
    }
    private static void harvest(Context c) {
        var job = new GardenGoal(c.dog); c.h.assertTrue(job.canUse(), "A guided/educated compatible resident finds real ripe produce");
        job.start();
        if (c.dog.learning().lesson()!=null) c.h.assertTrue(c.dog.learning().activity()==BunnidogLearning.Activity.WALKING,"Lesson activity follows the actual started approach");
        c.dog.setPos(Vec3.atBottomCenterOf(c.h.absolutePos(new BlockPos(4, 1, 3))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) job.tick();
        job.stop();
    }
    private static void deliver(Context c) {
        var job = new SupplyGoal(c.dog); c.h.assertTrue(job.canUse(), "A guided/educated supplier finds both real dishes");
        job.start(); c.dog.setPos(Vec3.atBottomCenterOf(c.h.absolutePos(new BlockPos(4, 1, 4))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) job.tick();
        job.stop();
    }
    public static void feedback(GameTestHelper h) {
        var c=setup(h,false);
        day(c,()->{
            var knowledge=c.dog.learning().knowledge();
            c.dog.setWorkPaused(true); CareMenu.open(c.teacher,c.dog);
            var menu=(CareMenu)c.teacher.containerMenu;
            h.assertTrue(menu.clickMenuButton(c.teacher,menu.buttonId(CarePanels.ROLES)) && menu.clickMenuButton(c.teacher,menu.buttonId(LearningPanel.OPEN)),"A paused worker can still inspect its lesson actions");
            var teach=menu.view().controls().stream().filter(control->(control.id()&255)==LearningPanel.TEACH).findFirst().orElseThrow();
            h.assertTrue(!teach.enabled() && teach.hint().equals(AnimalPanels.text("work_paused")) && c.dog.learning().knowledge().equals(knowledge),"Disabled lessons identify the actual pause without reading genes or changing knowledge");
            CohesionGameTest.preview("Paused lessons",menu.view()); c.teacher.closeContainer();
            c.dog.setWorkPaused(false); teachFromMenu(c,Learning.Routine.HARVEST);
            h.assertTrue(c.dog.learning().activity()==BunnidogLearning.Activity.SEARCHING,"A chosen lesson begins by looking for real work");
            c.teacher.snapTo(c.teacher.position().add(15,0,0));
            h.assertTrue(c.dog.learning().activity()==BunnidogLearning.Activity.WAITING_FOR_GUIDE && c.dog.learning().knowledge().equals(knowledge),"Walking away pauses instruction without fabricated progress");
            c.teacher.snapTo(c.dog.position().add(1,0,0));
            h.assertTrue(c.dog.learning().activity()==BunnidogLearning.Activity.SEARCHING,"Returning the real guide resumes the same session");
            CareMenu.open(c.teacher,c.dog); menu=(CareMenu)c.teacher.containerMenu;
            h.assertTrue(menu.clickMenuButton(c.teacher,menu.buttonId(CarePanels.ROLES)),"Work exposes only the current real activity");
            CohesionGameTest.preview("Searching for practice",menu.view()); c.teacher.closeContainer();
            c.dog.setWorkPaused(true); c.dog.learning().tick();
            h.assertTrue(c.dog.learning().lesson()==null && c.dog.learning().activity()==BunnidogLearning.Activity.IDLE && c.dog.learning().knowledge().equals(knowledge),"A real work interruption ends transient instruction and retains personal knowledge");
            c.dog.setWorkPaused(false); h.assertTrue(c.dog.learning().begin(c.teacher,Learning.Routine.HARVEST),"Interrupted practice can be deliberately resumed");
            c.dog.learning().cancel();
        });
    }
    public static void harvest(GameTestHelper h) {
        var c = setup(h, false);
        day(c, () -> {
            h.assertTrue(!new GardenGoal(c.dog).canUse() && c.dog.learning().knowledge().equals(Learning.EMPTY), "A fresh compatible adult inherits capacity without uploaded garden lessons");
            var genome = c.dog.genome(); var stats = c.dog.stats();
            teachFromMenu(c, Learning.Routine.HARVEST); harvest(c);
            h.assertTrue(c.dog.learning().activity()==BunnidogLearning.Activity.RESTING,"Paid work reports actual shared recovery, not a generic active-lesson label");
            int first = c.dog.learning().knowledge().progress(Learning.Routine.HARVEST);
            h.assertTrue(first > 0 && first < Learning.MASTERED && c.stock(c.source).food().getCount() == 1, "A real harvest deposits one food and teaches bounded personal practice");
            ready(c.dog); ripe(h, c.target);
            h.assertTrue(c.dog.learning().lesson() == null && c.dog.learning().knowledge().progress(Learning.Routine.HARVEST) == first && !new GardenGoal(c.dog).canUse(),
                    "Reload saves progress, drops transient instruction and never grants free mastery");
            h.assertTrue(c.dog.learning().begin(c.teacher, Learning.Routine.HARVEST), "A second short session resumes practice");
            c.teacher.snapTo(c.teacher.position().add(15, 0, 0));
            h.assertTrue(c.dog.learning().activity()==BunnidogLearning.Activity.WAITING_FOR_GUIDE,"An absent guide has a distinct live status");
            h.assertTrue(!c.dog.learning().canWork(Learning.Routine.HARVEST) && !new GardenGoal(c.dog).canUse(), "An absent instructor cannot teach from afar");
            c.teacher.snapTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4, 1, 2))));
            harvest(c);
            h.assertTrue(c.dog.learning().knowledge().knows(Learning.Routine.HARVEST) && c.dog.learning().lesson() == null, "Two paid successful lessons teach the helper profile independent harvest");
            ready(c.dog); ripe(h, c.target); c.teacher.snapTo(c.teacher.position().add(15, 0, 0)); harvest(c);
            h.assertTrue(c.stock(c.source).food().getCount() == 3 && c.dog.genome().equals(genome) && c.dog.stats().equals(stats)
                    && c.dog.learning().knowledge().progress(Learning.Routine.GARDEN) == 0, "Mastered harvest works alone without raising DNA, stats or unrelated knowledge");
            var basket = new ItemStack(ModItems.TRAVEL_BASKET.get());
            h.assertTrue(TravelBasketItem.capture(h.getLevel(), basket, c.dog).success(), "A learner can travel normally");
            var carried = basket.get(DataComponents.BUCKET_ENTITY_DATA).copyTag();
            h.assertTrue(Learning.parse(carried.getStringOr("Learning", "")).orElseThrow().knows(Learning.Routine.HARVEST), "Travel stores the individual's learned routine");
        });
    }
    public static void delivery(GameTestHelper h) {
        var c = setup(h, true);
        day(c, () -> {
            h.assertTrue(!new SupplyGoal(c.dog).canUse(), "Senses alone does not upload a transfer routine");
            teachFromMenu(c, Learning.Routine.DELIVERY); deliver(c);
            h.assertTrue(c.stock(c.source).food().getCount() == 10 && c.stock(c.target).food().getCount() == 1
                    && c.dog.learning().knowledge().progress(Learning.Routine.DELIVERY) > 0, "First lesson spends and delivers the same real food");
            ready(c.dog); h.assertTrue(c.dog.learning().begin(c.teacher, Learning.Routine.DELIVERY), "Delivery resumes after rest/reload"); deliver(c);
            h.assertTrue(c.dog.learning().knowledge().knows(Learning.Routine.DELIVERY), "Paid practice teaches independent transfer");
            ready(c.dog); c.teacher.snapTo(c.teacher.position().add(15, 0, 0)); deliver(c);
            h.assertTrue(c.stock(c.source).food().getCount() == Supplying.RESERVE && c.stock(c.target).food().getCount() == 3
                    && c.stock(c.target).food().getHoverName().getString().equals("Lesson supplies"), "Independent work conserves components and the eight-food reserve");
            ready(c.dog); h.assertTrue(!new SupplyGoal(c.dog).canUse(), "Education cannot bypass source reserves");
        });
    }
    public static void limits(GameTestHelper h) {
        var c = setup(h, false);
        day(c, () -> {
            var body = c.dog.genome(); c.dog.setGenome(new Genome(body.packed(), 0));
            h.assertTrue(!c.dog.learning().canBegin(c.teacher, Learning.Routine.HARVEST), "The lowest combined capacities cannot be taught a complex chore");
            Genome supported = null;
            for (int code = 0; code < 256 && supported == null; code++) {
                int word = 0;
                for (int i = 0; i < 4; i++) word |= (((code >>> (i * 2)) & 3) * 17) << (i * 8);
                var candidate = new Genome(body.packed(), word);
                if (CapabilityRules.potential(candidate.mind(), CapabilityRules.Task.HARVEST) == CapabilityRules.Potential.SUPPORTED) supported = candidate;
            }
            h.assertTrue(supported != null, "Supported-only minds must exist in the real allele table");
            c.dog.setGenome(supported); WorkerFixture.familiar(c.dog);
            h.assertTrue(!c.dog.learning().canWork(Learning.Routine.HARVEST), "Knowing a routine does not bypass a supported-only capacity");
            teachFromMenu(c, Learning.Routine.HARVEST);
            h.assertTrue(c.dog.learning().canWork(Learning.Routine.HARVEST), "A supported learner remains useful with local guidance");
            // A solid wall between the instructor and work must stop the operation, even with a visible animal.
            h.setBlock(4, 2, 3, Blocks.STONE); h.setBlock(5, 2, 3, Blocks.STONE); h.setBlock(5, 1, 3, Blocks.STONE);
            h.assertTrue(!c.dog.learning().witnesses(c.target, c.source), "Instruction cannot see through walls");
            h.setBlock(4, 2, 3, Blocks.AIR); h.setBlock(5, 2, 3, Blocks.AIR); h.setBlock(5, 1, 3, Blocks.AIR);
            var knowledge = c.dog.learning().knowledge();
            c.dog.setWorkPaused(true); c.dog.learning().tick();
            h.assertTrue(c.dog.learning().lesson() == null && c.dog.learning().knowledge().equals(knowledge), "Pausing work interrupts instruction without losing knowledge");
            c.dog.setWorkPaused(false); c.dog.setGenome(new Genome(body.packed(), 0));
            c.teacher.getInventory().add(new ItemStack(ModItems.TRAIL_WHISTLE.get()));
            h.assertTrue(c.dog.trail().trainFromStock(c.teacher, new ItemStack(ModItems.GUAVA.get())) && c.dog.trail().belongsTo(c.teacher),
                    "Low cognitive capacity must not prevent attachment or trusted companion orders");
            c.teacher.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ModItems.TRAIL_WHISTLE.get()));
            h.assertTrue(c.dog.combat().assist(c.teacher), "A limited mind can still join the deliberately chosen combat party");
            h.assertTrue(c.dog.trail().command(c.teacher, Trail.Order.WAIT), "A limited partner can wait normally");
            TrailCompanion.call(c.teacher, net.minecraft.world.InteractionHand.MAIN_HAND);
            h.assertTrue(c.dog.trail().order() == Trail.Order.FOLLOW, "The actual normal Whistle must recall a limited partner reliably");
        });
    }
    public static void legacy(GameTestHelper h) {
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 1, 1, 1); dog.setAge(0); dog.setGenome(Genome.DEFAULT);
        var old = save(dog); old.putString("Genome", Genome.DEFAULT.bodyCode()); old.remove("Learning");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), old));
        h.assertTrue(dog.learning().knowledge().equals(Learning.LEGACY_FAMILIAR), "Existing adults keep familiar routines once");
        dog.setGenome(new Genome(0, 0));
        h.assertTrue(!dog.learning().canWork(Learning.Routine.HARVEST) && dog.learning().knowledge().equals(Learning.LEGACY_FAMILIAR), "A limiting splice leaves dormant knowledge intact");
        dog.setGenome(Genome.DEFAULT);
        h.assertTrue(dog.learning().canWork(Learning.Routine.HARVEST), "Restoring capacity restores access to learned routines");
        var rewritten = save(dog); rewritten.putString("Genome", Genome.DEFAULT.bodyCode()); rewritten.putString("Learning", Learning.EMPTY.toString());
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), rewritten));
        h.assertTrue(dog.learning().knowledge().equals(Learning.EMPTY), "A schema marker prevents repeat free migration lessons");
        old.putInt("Age", -24000);
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), old));
        h.assertTrue(dog.isBaby() && dog.learning().knowledge().equals(Learning.EMPTY), "Legacy pups do not receive adult work experience");
        old.putString("Learning","l1.1-2-3-4");dog.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),old));
        h.assertTrue(dog.learning().knowledge().progress(Learning.Routine.GARDEN)==3 && !dog.learning().knowledge().knows(Learning.Routine.SCOUT),"Old pups retain exact saved practice without adult surveying experience");
        old.putInt("Age",0);dog.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),old));
        h.assertTrue(dog.learning().knowledge().progress(Learning.Routine.NURSERY)==4 && dog.learning().knowledge().knows(Learning.Routine.SCOUT),"Old adults preserve both their exact work progress and previously familiar surveying");
        var child = ModEntities.BUNNIDOG.get().create(h.getLevel(), EntitySpawnReason.BREEDING);
        child.setGenome(Genome.DEFAULT); child.setParents(Genome.DEFAULT, Genome.DEFAULT); child.setBaby(true);
        h.assertTrue(child.learning().knowledge().equals(Learning.EMPTY), "Newborns inherit potential, never parental work lessons");
    }
    public static void autonomous(GameTestHelper h) {
        // An elevated fixture isolates normal skittish avoidance from other tests' players.
        final int y = 120;
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x, y, z, Blocks.STONE);
            for (int up = 1; up < 5; up++) h.setBlock(x, y + up, z, Blocks.AIR);
        }
        h.setBlock(1, y + 1, 1, ModBlocks.NEST.get());
        var trough = h.absolutePos(new BlockPos(4, y + 1, 4));
        var crop = h.absolutePos(new BlockPos(5, y + 1, 4));
        h.getLevel().setBlock(trough, ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING, true), 3);
        h.getLevel().setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), 3); ripe(h, crop);
        // withNoFreeWill removes the goal selectors; use a normal spawn with a temporary AI pause.
        var dog = h.spawn(ModEntities.BUNNIDOG.get(), 3.5F, y + 1, 3.5F); dog.setNoAi(true);
        var genome = Genome.DEFAULT.with(Gene.DIET, 0, 2).with(Gene.DIET, 1, 2).with(Gene.TEMPERAMENT, 0, 1).with(Gene.TEMPERAMENT, 1, 1);
        dog.setGenome(genome); dog.setAge(0); dog.setHealth(dog.getMaxHealth());
        var teacher = h.makeMockServerPlayerInLevel(); teacher.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        teacher.snapTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4, y + 1, 2))));
        var carrot = new ItemStack(Items.CARROT); teacher.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, carrot);
        h.runAfterDelay(2, () -> {
            dog.setNoAi(false); dog.setOnGround(true);
            var home = h.absolutePos(new BlockPos(1, y + 1, 1)); var moved = dog.moveHomeTo(h.getLevel(), home);
            h.assertTrue(home.equals(dog.homeNest()), "A real AI learner claims or retains its home: " + moved);
            h.assertTrue(dog.learning().begin(teacher, Learning.Routine.HARVEST), "A skittish animal can recognize its active instructor: status="+dog.gardeningStatus()+", distance="+dog.distanceToSqr(teacher)+", sight="+teacher.hasLineOfSight(dog)+", knowledge="+dog.learning().knowledge()+", health="+dog.getHealth()+"/"+dog.getMaxHealth());
            h.succeedWhen(() -> {
                var stock = (FeedingTroughBlockEntity)h.getLevel().getBlockEntity(trough);
                h.assertTrue(stock.food().getCount() == 1 && dog.learning().knowledge().progress(Learning.Routine.HARVEST) > 0,
                        "Normal AI must complete guided harvest: status=" + dog.gardeningStatus() + ", lesson=" + dog.learning().lesson()
                        + ", guided=" + dog.learning().guided() + ", progress=" + dog.learning().knowledge() + ", position=" + dog.position()
                        + ", crop=" + crop + ", trough=" + trough + ", witnesses=" + dog.learning().witnesses(crop, trough)
                        + ", distances=" + dog.distanceToSqr(Vec3.atCenterOf(crop)) + "/" + dog.distanceToSqr(Vec3.atCenterOf(trough))
                        + ", goals=" + dog.getGoalSelector().getAvailableGoals().stream().filter(g -> g.isRunning())
                        .map(g -> g.getGoal().getClass().getSimpleName()).toList());
                h.assertTrue(carrot.getCount() == 1 && dog.genome().equals(genome) && dog.trail().keeper() == null,
                        "A lesson neither spends the held lure nor silently changes genes or bonds the learner");
                dog.discard();
            });
        });
    }
}
