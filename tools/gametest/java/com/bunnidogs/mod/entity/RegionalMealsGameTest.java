package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;

/** Habitat meals conserve growing stock even when a native protection callback changes it. */
public final class RegionalMealsGameTest {
    private RegionalMealsGameTest() {}
    private static void ready(Bunnidog dog, String diet) {
        var level = dog.level(); var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(out); var saved = out.buildResult(); saved.remove("ForageWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        int allele = Gene.DIET.alleleNamed(diet).id(); dog.setGenome(Genome.DEFAULT.with(Gene.DIET,0,allele).with(Gene.DIET,1,allele));
        dog.setHealth(dog.getMaxHealth()-4);
    }
    private static void eat(GameTestHelper h, Bunnidog dog, GrazeGoal goal) {
        goal.start(); h.assertTrue(HomeRoutes.safe(dog,dog.getNavigation().getPath(),null),"Regional meals need a complete safe native path");
        dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));
        for (int i = 0; i < Foraging.GRAZE_TICKS; i++) goal.tick(); goal.stop();
    }
    private static void cancel(GameTestHelper h, Bunnidog dog, BlockPos food, Runnable change) {
        var goal = new GrazeGoal(dog); h.assertTrue(goal.canUse(),"Expected a renewable meal before its protection callback");
        Consumer<LivingDestroyBlockEvent> callback = event -> { if (event.getEntity() == dog && event.getPos().equals(food)) change.run(); };
        float health = dog.getHealth(); NeoForge.EVENT_BUS.addListener(callback);
        try { eat(h,dog,goal); } finally { NeoForge.EVENT_BUS.unregister(callback); }
        h.assertTrue(dog.getHealth() == health && dog.forageWait() == 0,"A callback invalidating growing stock must cancel without healing or recovery");
    }
    private static void meal(GameTestHelper h, Bunnidog dog) {
        var goal = new GrazeGoal(dog); h.assertTrue(goal.canUse(),"An injured matching resident must find renewable food");
        float health = dog.getHealth(); eat(h,dog,goal);
        h.assertTrue(dog.getHealth() == health+Foraging.HEAL && dog.forageWait() == Foraging.MEAL_COOLDOWN,
                "One committed regional meal must heal two HP and share food recovery");
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 7; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        h.setBlock(1,1,1,ModBlocks.NEST.get());
        var field = h.absolutePos(new BlockPos(5,1,4)); var top = field.above(2); var companion = field.south(2);
        level.setBlock(field.below(),Blocks.SAND.defaultBlockState(),3);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,1,4.5F); dog.setAge(0); ready(dog,"desert_browser");
        h.runAfterDelay(2,() -> {
            var clock = level.dimensionType().defaultClock().orElseThrow(); long time = level.clockManager().getTotalTicks(clock);
            boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
            try {
                level.clockManager().setTotalTicks(clock,1000); level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer());
                h.assertTrue(dog.moveHomeTo(level,h.absolutePos(new BlockPos(1,1,1))) == NestHome.MoveResult.MOVED,"Habitat meals require a claimed home");
                level.setBlock(field,Blocks.CACTUS.defaultBlockState(),3);
                h.assertTrue(!new GrazeGoal(dog).canUse(),"A lone cactus is growing stock rather than food");
                level.setBlock(field.above(),Blocks.CACTUS.defaultBlockState(),3); level.setBlock(top,Blocks.CACTUS.defaultBlockState(),3);
                cancel(h,dog,top,() -> level.setBlock(top.above(),Blocks.CACTUS_FLOWER.defaultBlockState(),3));
                h.assertTrue(level.getBlockState(top).is(Blocks.CACTUS),"An attached cactus flower must preserve its supporting top");
                level.removeBlock(top.above(),false);
                level.getGameRules().set(GameRules.MOB_GRIEFING,false,level.getServer()); h.assertTrue(!new GrazeGoal(dog).canUse(),"Regional meals must obey griefing");
                level.getGameRules().set(GameRules.MOB_GRIEFING,true,level.getServer()); meal(h,dog);
                h.assertTrue(level.getBlockState(top).isAir() && level.getBlockState(field).is(Blocks.CACTUS) && level.getBlockState(field.above()).is(Blocks.CACTUS),
                        "A ground-level resident must prune only the reachable top of a three-block cactus");
                level.removeBlock(field.above(),false); level.removeBlock(field,false);
                level.setBlock(field.below(),Blocks.MYCELIUM.defaultBlockState(),3); level.setBlock(companion.below(),Blocks.MYCELIUM.defaultBlockState(),3);
                ready(dog,"mushroom_eater"); level.setBlock(field,Blocks.RED_MUSHROOM.defaultBlockState(),3);
                level.setBlock(companion,Blocks.BROWN_MUSHROOM.defaultBlockState(),3);
                h.assertTrue(!new GrazeGoal(dog).canUse(),"The final red and brown mushroom are separate seed stocks");
                level.setBlock(companion,Blocks.RED_MUSHROOM.defaultBlockState(),3);
                cancel(h,dog,field,() -> level.removeBlock(companion,false));
                h.assertTrue(level.getBlockState(field).is(Blocks.RED_MUSHROOM),"Losing a companion during protection must preserve the final mushroom");
                level.setBlock(companion,Blocks.RED_MUSHROOM.defaultBlockState(),3); meal(h,dog);
                h.assertTrue(level.getBlockState(field).isAir() && level.getBlockState(companion).is(Blocks.RED_MUSHROOM),"A fungal meal must leave living same-color seed stock");
                ready(dog,"mushroom_eater"); h.assertTrue(!new GrazeGoal(dog).canUse(),"The remaining mushroom must stay after food recovery"); level.removeBlock(companion,false);
                level.setBlock(field.below(),Blocks.SOUL_SAND.defaultBlockState(),3);
                level.setBlock(field,Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE,2),3); ready(dog,"nether_forager");
                h.assertTrue(!new GrazeGoal(dog).canUse(),"Unripe wart is not food");
                level.setBlock(field,Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE,3),3); ready(dog,"plain");
                h.assertTrue(!new GrazeGoal(dog).canUse(),"Habitat meals must retain inherited Diet"); ready(dog,"nether_forager"); meal(h,dog);
                h.assertTrue(level.getBlockState(field).getValue(NetherWartBlock.AGE) == 0,"Ripe wart meals must leave a young plant on soul sand");
                level.removeBlock(field,false); level.setBlock(field.below(),Blocks.STONE.defaultBlockState(),3);
                level.setBlock(field,Blocks.MELON.defaultBlockState(),3); ready(dog,"forager");
                h.assertTrue(!new GrazeGoal(dog).canUse(),"Decorative melons must not become direct meals");
                var stem = field.north(); level.setBlock(stem.below(),Blocks.FARMLAND.defaultBlockState(),3);
                var attached = Blocks.ATTACHED_MELON_STEM.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.SOUTH);
                level.setBlock(stem,attached,3);
                cancel(h,dog,field,() -> level.setBlock(stem,Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE,7),3));
                h.assertTrue(level.getBlockState(field).is(Blocks.MELON),"Detached fruit must survive a canceled direct meal");
                level.setBlock(stem,attached,3); meal(h,dog);
                h.assertTrue(level.getBlockState(field).isAir() && level.getBlockState(stem).is(Blocks.MELON_STEM)
                        && level.getBlockState(stem).getValue(StemBlock.AGE) == 7,"Melon meals must restore the native mature growing stem");
                h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(field).inflate(5),e -> true).isEmpty(),"Direct regional meals must not duplicate loose food");
                h.succeed();
            } finally { level.clockManager().setTotalTicks(clock,time); level.getGameRules().set(GameRules.MOB_GRIEFING,griefing,level.getServer()); }
        });
    }
}
