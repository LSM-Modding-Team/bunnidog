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
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;

/** Real cactus segments: protected top pruning, safe grounded routes, exact food and shared work recovery. */
public final class CactusGardeningGameTest {
    private CactusGardeningGameTest() {}
    private static void ready(Bunnidog dog) {
        var level = dog.level(); var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(output); var saved = output.buildResult(); saved.remove("GardenWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y <= 5; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        h.setBlock(new BlockPos(3,1,4),ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true));
        h.setBlock(5,0,4,Blocks.SAND);
        var base = h.absolutePos(new BlockPos(5,1,4)); var top = base.above();
        var original = Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE,9);
        level.setBlock(base,original,3);
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(3,1,4)));
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,3.5F);
        int browser = Gene.DIET.alleleNamed("desert_browser").id();
        var genome = Genome.DEFAULT.with(Gene.DIET,0,browser).with(Gene.DIET,1,browser);
        dog.setAge(0); dog.setGenome(genome); WorkerFixture.familiar(dog); dog.setHealth(dog.getMaxHealth());
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"A cactus worker needs a real home");
                h.assertTrue(!new GardenGoal(dog).canUse(),"A lone cactus base must never be harvested");
                level.setBlock(top,Blocks.CACTUS.defaultBlockState(),3);
                dog.setGenome(Genome.DEFAULT);
                h.assertTrue(!new GardenGoal(dog).canUse(),"Other diets must leave cactus intact"); dog.setGenome(genome);
                trough.setFilter(new ItemStack(Items.POTATO));
                h.assertTrue(!new GardenGoal(dog).canUse(),"Trough filters must apply to cactus pruning"); trough.clearFilter();
                var interrupted = new GardenGoal(dog); h.assertTrue(interrupted.canUse(),"A browser must find a spare upper segment");
                interrupted.start(); dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
                Consumer<LivingDestroyBlockEvent> growAtCommit = event -> {
                    if (event.getEntity() == dog && event.getPos().equals(top)) level.setBlock(top.above(),Blocks.CACTUS.defaultBlockState(),3);
                };
                NeoForge.EVENT_BUS.addListener(growAtCommit);
                try { for (int i = 0; i < Gardening.WORK_TICKS; i++) interrupted.tick(); }
                finally { NeoForge.EVENT_BUS.unregister(growAtCommit); interrupted.stop(); }
                h.assertTrue(level.getBlockState(top).is(Blocks.CACTUS) && level.getBlockState(top.above()).is(Blocks.CACTUS)
                        && trough.food().isEmpty() && dog.gardeningWait() == 0,"Growth during protection callbacks must prevent a chain break without cost");
                var trim = new GardenGoal(dog); h.assertTrue(trim.canUse(),"The new three-high column must expose its actual top");
                trim.start(); var route = dog.getNavigation().getPath();
                h.assertTrue(route.canReach() && HomeRoutes.safe(dog,route,null),"Cactus jobs need complete hazard-avoiding grounded routes");
                for (int i = 0; i < Gardening.WORK_TICKS; i++) trim.tick(); trim.stop();
                h.assertTrue(level.getBlockState(top.above()).isAir() && level.getBlockState(top).is(Blocks.CACTUS)
                        && level.getBlockState(base).equals(original) && trough.food().is(Items.CACTUS) && trough.food().getCount() == 1
                        && dog.gardeningWait() == Gardening.REST_TICKS,"One job must trim exactly the top, preserve lower ages and stock one cactus with shared rest");
                h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(base).inflate(2),item -> item.getItem().is(Items.CACTUS)).isEmpty(),
                        "Pruning must not create a second loose drop");
                ready(dog); trough.takeAll(); level.setBlock(top.above(),Blocks.CACTUS_FLOWER.defaultBlockState(),3);
                h.assertTrue(!new GardenGoal(dog).canUse(),"Flowering tops must remain intact");
                level.setBlock(top.above(),Blocks.AIR.defaultBlockState(),3);
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
                h.assertTrue(!new GardenGoal(dog).canUse(),"Cactus jobs obey native mob griefing");
                level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                trough.insert(new ItemStack(Items.CACTUS,16));
                h.assertTrue(!new GardenGoal(dog).canUse() && level.getBlockState(top).is(Blocks.CACTUS),"Full stock must leave the spare segment growing");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
