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
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;

/** Renewable melon fruit, decorative-block conservation and native stem reset/growth. */
public final class MelonGardeningGameTest {
    private MelonGardeningGameTest() {}
    private static void ready(Bunnidog dog) {
        var level = dog.level(); var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(output); var saved = output.buildResult(); saved.remove("GardenWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    private static void work(GameTestHelper h, Bunnidog dog, GardenGoal goal) {
        goal.start(); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) goal.tick(); goal.stop();
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y <= 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(new BlockPos(3,1,4),ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true));
        var fruit = h.absolutePos(new BlockPos(5,1,4)); var stem = fruit.north();
        level.setBlock(stem.below(),Blocks.FARMLAND.defaultBlockState(),3);
        level.setBlock(fruit,Blocks.MELON.defaultBlockState(),3);
        var attached = Blocks.ATTACHED_MELON_STEM.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.SOUTH);
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(3,1,4)));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F);
        int forager = Gene.DIET.alleleNamed("forager").id();
        var genome = Genome.DEFAULT.with(Gene.DIET,0,forager).with(Gene.DIET,1,forager);
        dog.setAge(0); dog.setGenome(genome); WorkerFixture.familiar(dog); dog.setStats(Stats.ZERO); dog.setHealth(dog.getMaxHealth());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Melon work needs a real claimed Nest");
                h.assertTrue(!new GardenGoal(dog).canUse(),"Decorative melons without an attached stem must remain intact");
                level.setBlock(stem,attached.setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH),3);
                h.assertTrue(!new GardenGoal(dog).canUse(),"An attached stem facing away must not authorize this melon");
                level.setBlock(stem,attached,3);
                dog.setGenome(Genome.DEFAULT); h.assertTrue(!new GardenGoal(dog).canUse(),"Plain diets cannot harvest melon slices"); dog.setGenome(genome);
                trough.setFilter(new ItemStack(Items.POTATO)); h.assertTrue(!new GardenGoal(dog).canUse(),"Melon produce obeys exact trough filters");
                trough.clearFilter(); trough.setFilter(new ItemStack(Items.MELON_SLICE));
                var interrupted = new GardenGoal(dog); h.assertTrue(interrupted.canUse(),"A living linked melon must form a native garden job");
                Consumer<LivingDestroyBlockEvent> loseStem = event -> {
                    if (event.getEntity() == dog && event.getPos().equals(fruit))
                        level.setBlock(stem,Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE,7),3);
                };
                NeoForge.EVENT_BUS.addListener(loseStem);
                try { work(h,dog,interrupted); } finally { NeoForge.EVENT_BUS.unregister(loseStem); }
                h.assertTrue(level.getBlockState(fruit).is(Blocks.MELON) && trough.food().isEmpty() && dog.gardeningWait() == 0,
                        "Losing the linked stem in a protection callback must cancel without food or rest");
                level.setBlock(stem,attached,3);
                var harvest = new GardenGoal(dog); h.assertTrue(harvest.canUse(),"A restored stem must permit a new job"); work(h,dog,harvest);
                h.assertTrue(level.getBlockState(fruit).isAir() && level.getBlockState(stem).is(Blocks.MELON_STEM)
                        && level.getBlockState(stem).getValue(StemBlock.AGE) == 7 && trough.food().is(Items.MELON_SLICE)
                        && trough.food().getCount() == 1 && dog.gardeningWait() == Gardening.REST_TICKS,
                        "One fruit must yield one slice and native updates must restore its mature growing stem");
                h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(fruit).inflate(3),item -> item.getItem().is(Items.MELON_SLICE)).isEmpty(),
                        "Trough harvesting must not duplicate loose slices");
                ready(dog); trough.takeAll();
                h.assertTrue(!new GardenGoal(dog).canUse(),"A mature bare stem must await native fruit growth rather than be harvested");
                level.setBlock(stem,Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE,2),3);
                dog.setStats(Stats.of(0,0,0,0,9)); h.assertTrue(!new GardenGoal(dog).canUse(),"Bloom +9 must leave young stems alone");
                dog.setStats(Stats.of(0,0,0,0,10));
                var grow = new GardenGoal(dog); h.assertTrue(grow.canUse(),"Bloom +10 may tend a matching young melon stem"); work(h,dog,grow);
                h.assertTrue(level.getBlockState(stem).getValue(StemBlock.AGE) == 3 && level.getBlockState(fruit).isAir()
                        && trough.food().isEmpty() && dog.gardeningWait() == Gardening.REST_TICKS,
                        "Cultivation must advance exactly one stage, produce no food and share gardening rest");
                ready(dog); level.setBlock(stem,Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE,7),3);
                h.assertTrue(!new GardenGoal(dog).canUse(),"Bloom must not wrap mature stems or force another fruit");
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                var slices = new ItemStack(Items.MELON_SLICE,2); player.setItemInHand(InteractionHand.MAIN_HAND,slices);
                dog.setHealth(dog.getMaxHealth()-4); dog.setGenome(Genome.DEFAULT); float hurt = dog.getHealth();
                player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
                h.assertTrue(dog.getHealth() == hurt && slices.getCount() == 2,"Plain lines must refuse hand-fed melon");
                dog.setGenome(genome); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
                h.assertTrue(dog.getHealth() == hurt+2 && slices.getCount() == 1 && !Bunnidog.isBunnidogFood(slices),
                        "Forager slices heal for one real item while remaining outside breeding food");
                dog.setAge(-2400); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
                h.assertTrue(dog.getAge() > -2400 && slices.isEmpty(),"A compatible pup must grow using the final real slice");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
