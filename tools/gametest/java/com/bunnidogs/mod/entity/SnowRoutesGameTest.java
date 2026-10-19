package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** A real native route crosses a snow-surface-only corridor while changed hazards/footing still veto it. */
public final class SnowRoutesGameTest {
    private SnowRoutesGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 5; y++) h.setBlock(x,y,z,x == 0 || x == 7 || z == 0 || z == 7 ? Blocks.STONE : Blocks.AIR);
        }
        for (int x = 2; x <= 6; x++) for (int y = 1; y <= 3; y++) { h.setBlock(x,y,2,Blocks.STONE); h.setBlock(x,y,4,Blocks.STONE); }
        h.setBlock(1,1,1,ModBlocks.NEST.get()); h.setBlock(6,1,3,ModBlocks.FEEDING_TROUGH.get());
        h.setBlock(3,0,3,Blocks.POWDER_SNOW); h.setBlock(4,0,3,Blocks.POWDER_SNOW);
        var snowFloor = h.absolutePos(new BlockPos(3,0,3)); var snowStand = snowFloor.above(); var stock = (FeedingTroughBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(6,1,3)));
        stock.insert(new ItemStack(ModItems.GUAVA.get(),2));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),1.5F,1,3.5F); dog.setAge(0);
        int snow = Gene.COAT.alleleNamed("snow").id(); var genome = Genome.DEFAULT.with(Gene.COAT,0,snow).with(Gene.COAT,1,snow); dog.setGenome(genome); dog.setHealth(dog.getMaxHealth()-4);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            try {
                level.clockManager().setTotalTicks(clock,1000);
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Snow routes need a real accessible claimed Nest");
                h.assertTrue(PowderSnowBlock.canEntityWalkOnPowderSnow(dog) && HomeRoutes.standing(dog,snowStand) && HomeRoutes.dryCell(dog,snowStand),"Expressed snow gives native surface footing and a supported dry work place");
                var meal = new TroughGoal(dog); h.assertTrue(meal.canUse(),"A native complete route must reach food across the powder-only corridor"); meal.start(); var route = dog.getNavigation().getPath();
                boolean crossed = false; for (int i = 0; i < route.getNodeCount(); i++) if (route.getNode(i).type == PathType.ON_TOP_OF_POWDER_SNOW) crossed = true;
                h.assertTrue(crossed && HomeRoutes.safe(dog,route,null),"The native path must actually use top-of-powder nodes rather than a hidden detour");
                dog.setGenome(genome.with(Gene.COAT,1,Gene.COAT.alleleNamed("spotted").id()));
                h.assertTrue(!HomeRoutes.standing(dog,snowStand) && !HomeRoutes.safe(dog,route,null) && !meal.canContinueToUse(),"Losing expressed footing immediately cancels a cached snow route"); meal.stop();
                int woolly = Gene.BUILD.alleleNamed("woolly").id(); dog.setGenome(Genome.DEFAULT.with(Gene.BUILD,0,woolly).with(Gene.BUILD,1,woolly));
                h.assertTrue(!dog.canFreeze() && !PowderSnowBlock.canEntityWalkOnPowderSnow(dog) && !HomeRoutes.standing(dog,snowStand) && !new TroughGoal(dog).canUse(),"Cold protection alone never authorizes a sinking woolly route");
                dog.setGenome(genome); dog.setHealth(dog.getMaxHealth()-4);
                h.assertTrue(!HomeRoutes.dryCell(dog,snowFloor),"Inside powder stays unsafe even for snow coats");
                level.setBlock(snowFloor,Blocks.MAGMA_BLOCK.defaultBlockState(),3); h.assertTrue(!HomeRoutes.safe(dog,route,null),"A newly damaging floor still invalidates the cached surface route"); level.setBlock(snowFloor,Blocks.POWDER_SNOW.defaultBlockState(),3);
                var fire = snowStand.north(); level.setBlock(fire,Blocks.FIRE.defaultBlockState(),3);
                h.assertTrue(!HomeRoutes.dryCell(dog,snowStand) && !HomeRoutes.standing(dog,snowStand) && !HomeRoutes.safe(dog,route,null),"Native neighbor fire checks must also protect top-of-powder routes"); level.setBlock(fire,Blocks.STONE.defaultBlockState(),3);
                level.setBlock(snowFloor,Blocks.AIR.defaultBlockState(),3); h.assertTrue(!HomeRoutes.safe(dog,route,null),"Losing the supporting snow floor must invalidate a cached top node"); level.setBlock(snowFloor,Blocks.POWDER_SNOW.defaultBlockState(),3);
                dog.setShiftKeyDown(true); h.assertTrue(!HomeRoutes.standing(dog,snowStand) && !HomeRoutes.safe(dog,route,null),"Native descending deliberately gives up surface footing"); dog.setShiftKeyDown(false);
                var retry = new TroughGoal(dog); h.assertTrue(retry.canUse(),"Restoring the real footing and safe surface permits a fresh route"); retry.start(); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(5,1,3))));
                float health = dog.getHealth(); for (int i = 0; i < Foraging.GRAZE_TICKS; i++) retry.tick(); retry.stop();
                h.assertTrue(stock.food().getCount() == 1 && dog.getHealth() == health+Foraging.HEAL && dog.forageWait() == Foraging.MEAL_COOLDOWN,"The snow route must reach one real normal meal without changing cost/healing/rest"); h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); }
        });
    }
}
