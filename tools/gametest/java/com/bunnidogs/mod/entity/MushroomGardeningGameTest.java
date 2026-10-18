package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;

/** Conserved fungal seed stock, including a companion disappearing inside native protection callbacks. */
public final class MushroomGardeningGameTest {
    private MushroomGardeningGameTest() {}
    private static void ready(Bunnidog dog) {
        var level = dog.level(); var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(output); var saved = output.buildResult(); saved.remove("GardenWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    private static void work(GameTestHelper h, Bunnidog dog, GardenGoal goal) {
        goal.start(); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,3))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) goal.tick(); goal.stop();
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.MYCELIUM);
            for (int y = 1; y <= 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(new BlockPos(3,1,4),ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true));
        var target = h.absolutePos(new BlockPos(5,1,3)); var spare = target.south(2); var brown = target.north(2);
        level.setBlock(target,Blocks.RED_MUSHROOM.defaultBlockState(),3); level.setBlock(brown,Blocks.BROWN_MUSHROOM.defaultBlockState(),3);
        var troughPos = h.absolutePos(new BlockPos(3,1,4)); var trough = (FeedingTroughBlockEntity)level.getBlockEntity(troughPos);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F);
        int eater = Gene.DIET.alleleNamed("mushroom_eater").id();
        var genome = Genome.DEFAULT.with(Gene.DIET,0,eater).with(Gene.DIET,1,eater);
        dog.setAge(0); dog.setGenome(genome); dog.setHealth(dog.getMaxHealth());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Mushroom workers need a claimed home");
                h.assertTrue(!new GardenGoal(dog).canUse(),"One red and one brown are both seed stock; colors cannot substitute");
                level.setBlock(spare,Blocks.RED_MUSHROOM.defaultBlockState(),3);
                dog.setGenome(Genome.DEFAULT); h.assertTrue(!new GardenGoal(dog).canUse(),"Plain diets must not thin fungal beds"); dog.setGenome(genome);
                trough.setFilter(new ItemStack(Items.BROWN_MUSHROOM)); h.assertTrue(!new GardenGoal(dog).canUse(),"Exact filters must reserve the other color"); trough.clearFilter();
                var interrupted = new GardenGoal(dog); h.assertTrue(interrupted.canUse(),"An extra red mushroom must form a sustainable harvest");
                Consumer<LivingDestroyBlockEvent> loseCompanion = event -> {
                    if (event.getEntity() == dog && event.getPos().equals(target)) level.removeBlock(spare,false);
                };
                NeoForge.EVENT_BUS.addListener(loseCompanion);
                try { work(h,dog,interrupted); } finally { NeoForge.EVENT_BUS.unregister(loseCompanion); }
                h.assertTrue(level.getBlockState(target).is(Blocks.RED_MUSHROOM) && level.getBlockState(spare).isAir()
                        && trough.food().isEmpty() && dog.gardeningWait() == 0,"A companion lost during protection callbacks must keep the final seed without rest or produce");
                level.setBlock(spare,Blocks.RED_MUSHROOM.defaultBlockState(),3);
                var harvest = new GardenGoal(dog); h.assertTrue(harvest.canUse(),"Restored seed stock must allow a new job");
                work(h,dog,harvest);
                h.assertTrue(level.getBlockState(target).isAir() && level.getBlockState(spare).is(Blocks.RED_MUSHROOM)
                        && level.getBlockState(brown).is(Blocks.BROWN_MUSHROOM) && trough.food().is(Items.RED_MUSHROOM)
                        && trough.food().getCount() == 1 && dog.gardeningWait() == Gardening.REST_TICKS,"Harvest must store one real mushroom, keep both colors and share work rest");
                h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(target).inflate(4),item -> item.getItem().is(Items.RED_MUSHROOM)).isEmpty(),"Thinning must not duplicate loose produce");
                ready(dog); trough.takeAll(); h.assertTrue(!new GardenGoal(dog).canUse(),"After recovery the final mushrooms must remain until native spread");
                level.removeBlock(spare,false); level.removeBlock(brown,false);
                level.setBlock(target,Blocks.BROWN_MUSHROOM.defaultBlockState(),3); level.setBlock(spare,Blocks.BROWN_MUSHROOM.defaultBlockState(),3);
                trough.setFilter(new ItemStack(Items.BROWN_MUSHROOM));
                var brownJob = new GardenGoal(dog); h.assertTrue(brownJob.canUse(),"Brown beds must support their own exact-color work"); work(h,dog,brownJob);
                h.assertTrue(trough.food().is(Items.BROWN_MUSHROOM) && trough.food().getCount() == 1 && level.getBlockState(spare).is(Blocks.BROWN_MUSHROOM),"Brown harvest must preserve its seed and exact filtered item");
                ready(dog); trough.takeAll(); level.setBlock(target,Blocks.BROWN_MUSHROOM.defaultBlockState(),3);
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
                h.assertTrue(!new GardenGoal(dog).canUse(),"Fungal work obeys native griefing"); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                level.setBlock(troughPos.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                h.assertTrue(!new GardenGoal(dog).canUse(),"Powered troughs must pause fungal work"); level.removeBlock(troughPos.west(),false);
                trough.insert(new ItemStack(Items.BROWN_MUSHROOM,16)); h.assertTrue(!new GardenGoal(dog).canUse(),"Full stock must leave the bed intact");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
