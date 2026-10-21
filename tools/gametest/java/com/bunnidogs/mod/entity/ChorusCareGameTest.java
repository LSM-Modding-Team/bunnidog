package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;

/** Spent flowers are renewable care targets; branch ages are never treated as crop ripeness. */
public final class ChorusCareGameTest {
    private ChorusCareGameTest() {}
    private static void bloom(Bunnidog dog, int value) { dog.setStats(Stats.of(0,0,0,0,value)); }
    private static void work(GameTestHelper h, Bunnidog dog, GardenGoal goal) {
        goal.start(); h.assertTrue(HomeRoutes.safe(dog,dog.getNavigation().getPath(),null),"Chorus care requires a complete safe route");
        dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
        for (int i = 0; i < Gardening.WORK_TICKS; i++) goal.tick(); goal.stop();
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE); for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        var trough = h.absolutePos(new BlockPos(3,1,4)); var root = h.absolutePos(new BlockPos(5,1,4)); var flower = root.above();
        level.setBlock(trough,ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true),3);
        var stock = (FeedingTroughBlockEntity)level.getBlockEntity(trough);
        level.setBlock(root.below(),Blocks.END_STONE.defaultBlockState(),3); level.setBlock(root,Blocks.CHORUS_PLANT.defaultBlockState(),3);
        level.setBlock(flower,Blocks.CHORUS_FLOWER.defaultBlockState().setValue(ChorusFlowerBlock.AGE,4),3);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F); dog.setAge(0);
        int diet = Gene.DIET.alleleNamed("chorus_eater").id(); var line = Genome.DEFAULT.with(Gene.DIET,0,diet).with(Gene.DIET,1,diet);
        dog.setGenome(line); WorkerFixture.familiar(dog); bloom(dog,20); dog.setHealth(dog.getMaxHealth());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"End gardeners need a claimed home");
                h.assertTrue(!new GardenGoal(dog).canUse(),"A living age-four flower must keep its native branching age");
                var spent = Blocks.CHORUS_FLOWER.defaultBlockState().setValue(ChorusFlowerBlock.AGE,5); level.setBlock(flower,spent,3);
                bloom(dog,19); h.assertTrue(!new GardenGoal(dog).canUse(),"Chorus care needs Bloom +20");
                bloom(dog,20); dog.setGenome(Genome.DEFAULT); h.assertTrue(!new GardenGoal(dog).canUse(),"Chorus work must obey inherited Diet");
                dog.setGenome(line); stock.setWorkMode(TroughWork.GATHER); h.assertTrue(!new GardenGoal(dog).canUse(),"Gather mode must not revive chorus");
                stock.setWorkMode(TroughWork.GARDEN); level.setBlock(trough.north(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                h.assertTrue(!new GardenGoal(dog).canUse(),"Power pauses End care"); level.removeBlock(trough.north(),false);
                var interrupted = new GardenGoal(dog); h.assertTrue(interrupted.canUse(),"Spent chorus should be a valid high-Bloom job");
                Consumer<LivingDestroyBlockEvent> callback = event -> { if (event.getEntity() == dog && event.getPos().equals(flower)) bloom(dog,19); };
                NeoForge.EVENT_BUS.addListener(callback);
                try { work(h,dog,interrupted); } finally { NeoForge.EVENT_BUS.unregister(callback); }
                h.assertTrue(level.getBlockState(flower).equals(spent) && stock.food().isEmpty() && dog.gardeningWait() == 0,
                        "A callback-lost Bloom bonus must preserve the flower without payment or work rest");
                bloom(dog,20); var goal = new GardenGoal(dog); h.assertTrue(goal.canUse(),"Restored high-Bloom worker must retry"); work(h,dog,goal);
                h.assertTrue(level.getBlockState(flower).getValue(ChorusFlowerBlock.AGE) == 0 && level.getBlockState(root).is(Blocks.CHORUS_PLANT)
                        && stock.food().is(Items.CHORUS_FRUIT) && stock.food().getCount() == 1 && dog.gardeningWait() == Gardening.REST_TICKS,
                        "One job must revive only the spent flower, preserve its branch and bank one real fruit");
                stock.takeAll(); level.setBlock(flower,spent,3); dog.setHealth(dog.getMaxHealth()-4);
                var meal = new GrazeGoal(dog); h.assertTrue(meal.canUse(),"An injured End gardener may eat a spent flower's fruit directly");
                float health = dog.getHealth(); meal.start(); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
                for (int i = 0; i < Foraging.GRAZE_TICKS; i++) meal.tick(); meal.stop();
                h.assertTrue(dog.getHealth() == health+Foraging.HEAL && dog.forageWait() == Foraging.MEAL_COOLDOWN
                        && level.getBlockState(flower).getValue(ChorusFlowerBlock.AGE) == 0 && level.getBlockState(root).is(Blocks.CHORUS_PLANT)
                        && level.getEntitiesOfClass(ItemEntity.class,new AABB(root).inflate(4)).isEmpty(),
                        "Direct chorus meals heal normally, retain growing stock and create no loose duplicate or teleport");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
