package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;

/** Complete safe paths, actual protected replacement, plant-preserving meals and shared recovery. */
public final class FruitForagingGameTest {
    private FruitForagingGameTest() {}
    private static Genome diet(String name) {
        int allele = Gene.DIET.alleleNamed(name).id(); return Genome.DEFAULT.with(Gene.DIET,0,allele).with(Gene.DIET,1,allele);
    }
    private static void ready(Bunnidog dog) {
        var level = dog.level(); var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(out); var saved = out.buildResult(); saved.remove("ForageWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved)); dog.setHealth(dog.getMaxHealth()-4);
    }
    private static void work(GameTestHelper h, Bunnidog dog, GrazeGoal goal) {
        goal.start(); h.assertTrue(HomeRoutes.safe(dog,dog.getNavigation().getPath(),null),"Foraging must use a complete loaded, home-bounded hazard-safe path");
        dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
        for (int i = 0; i < Foraging.GRAZE_TICKS; i++) goal.tick(); goal.stop();
    }
    private static void replaceDuringProtection(GameTestHelper h, Bunnidog dog, BlockPos food) {
        var level = h.getLevel(); var goal = new GrazeGoal(dog);
        h.assertTrue(goal.canUse(),"A ripe meal must be planned before testing the native callback");
        Consumer<LivingDestroyBlockEvent> replace = event -> {
            if (event.getEntity() == dog && event.getPos().equals(food)) level.setBlock(food,Blocks.CHEST.defaultBlockState(),3);
        };
        float health = dog.getHealth(); NeoForge.EVENT_BUS.addListener(replace);
        try { work(h,dog,goal); } finally { NeoForge.EVENT_BUS.unregister(replace); }
        h.assertTrue(level.getBlockState(food).is(Blocks.CHEST) && level.getBlockEntity(food) != null
                && dog.getHealth() == health && dog.forageWait() == 0,"Protection callbacks replacing a meal with a container must preserve it without healing/rest");
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        var fruit = h.absolutePos(new BlockPos(5,2,4)); var field = fruit.below();
        var ripe = ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(GuavaFruitBlock.AGE,GuavaFruitBlock.MAX_AGE);
        level.setBlock(fruit.above(),ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);
        level.setBlock(fruit,ripe,3);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,4.5F);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth()-4);
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(!new GrazeGoal(dog).canUse(),"Fruit foraging still requires a real home");
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Fruit foragers need a claimed Nest");
                replaceDuringProtection(h,dog,fruit); level.setBlock(fruit,ripe,3);
                var guava = new GrazeGoal(dog); h.assertTrue(guava.canUse(),"Even a plain Diet must find ripe hanging guava"); float health = dog.getHealth(); work(h,dog,guava);
                h.assertTrue(level.getBlockState(fruit).is(ModBlocks.GUAVA_FRUIT.get()) && level.getBlockState(fruit).getValue(GuavaFruitBlock.AGE) == 0
                        && dog.getHealth() == health+Foraging.HEAL && dog.forageWait() == Foraging.MEAL_COOLDOWN,"One guava meal must preserve the fruit, heal two HP and start shared recovery");
                h.assertTrue(!new GrazeGoal(dog).canUse() && level.getEntitiesOfClass(ItemEntity.class,new AABB(fruit).inflate(3),e -> true).isEmpty(),"A direct meal must not duplicate food or bypass recovery");
                ready(dog); h.assertTrue(!new GrazeGoal(dog).canUse(),"Unripe fruit is not a meal");
                level.setBlock(field.below(),Blocks.GRASS_BLOCK.defaultBlockState(),3);
                level.setBlock(field,Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE,3),3);
                h.assertTrue(!new GrazeGoal(dog).canUse(),"Plain Diet must refuse sweet berries"); dog.setGenome(diet("forager"));
                var berry = new GrazeGoal(dog); h.assertTrue(berry.canUse(),"Foragers must find ripe sweet berries from safe neighboring ground"); work(h,dog,berry);
                h.assertTrue(level.getBlockState(field).getValue(SweetBerryBushBlock.AGE) == 1 && dog.forageWait() == Foraging.MEAL_COOLDOWN,"A berry meal must keep the bush and use shared recovery");
                level.setBlock(field,Blocks.AIR.defaultBlockState(),3); level.setBlock(fruit.above(),Blocks.STONE.defaultBlockState(),3);
                for (var vine : new net.minecraft.world.level.block.Block[]{Blocks.CAVE_VINES,Blocks.CAVE_VINES_PLANT}) {
                    ready(dog); dog.setGenome(diet("cave_forager")); level.setBlock(fruit,vine.defaultBlockState().setValue(CaveVines.BERRIES,true),3);
                    var glow = new GrazeGoal(dog); h.assertTrue(glow.canUse(),"Cave foragers must find native fruiting vine heads and bodies"); work(h,dog,glow);
                    h.assertTrue(level.getBlockState(fruit).is(vine) && !level.getBlockState(fruit).getValue(CaveVines.BERRIES)
                            && dog.forageWait() == Foraging.MEAL_COOLDOWN,"Glow meals must preserve the exact vine kind");
                }
                ready(dog); dog.setGenome(diet("grazer")); level.setBlock(field.below(),Blocks.FARMLAND.defaultBlockState(),3);
                var wheat = (CropBlock)Blocks.WHEAT; level.setBlock(field,wheat.getStateForAge(wheat.getMaxAge()),3);
                replaceDuringProtection(h,dog,field); level.setBlock(field,wheat.getStateForAge(wheat.getMaxAge()),3);
                var crop = new GrazeGoal(dog); h.assertTrue(crop.canUse(),"Legacy ripe crop meals must remain available"); work(h,dog,crop);
                h.assertTrue(level.getBlockState(field).equals(wheat.getStateForAge(0)) && dog.forageWait() == Foraging.MEAL_COOLDOWN,"Legacy crop grazing must regrow and heal only after successful commit");
                ready(dog); dog.setGenome(Genome.DEFAULT); level.setBlock(fruit.above(),ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3); level.setBlock(fruit,ripe,3);
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer()); h.assertTrue(!new GrazeGoal(dog).canUse(),"Fruit-changing meals must obey native griefing");
                level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer()); level.clockManager().setTotalTicks(clock,13000);
                h.assertTrue(!new GrazeGoal(dog).canUse(),"Outdoor fruit meals remain daytime only"); level.clockManager().setTotalTicks(clock,1000);
                dog.setHealth(dog.getMaxHealth()); h.assertTrue(!new GrazeGoal(dog).canUse(),"Healthy adults must leave fruit for food and gardening");
                ready(dog); dog.setGenome(diet("grazer")); level.setBlock(fruit,Blocks.AIR.defaultBlockState(),3); level.setBlock(field,Blocks.HAY_BLOCK.defaultBlockState(),3);
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer());
                var hay = new GrazeGoal(dog); h.assertTrue(hay.canUse(),"Existing non-destructive hay meals still work with griefing disabled"); work(h,dog,hay);
                h.assertTrue(level.getBlockState(field).is(Blocks.HAY_BLOCK) && dog.forageWait() == Foraging.MEAL_COOLDOWN,"Hay must remain intact after its ordinary meal");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
