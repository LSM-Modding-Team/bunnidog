package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.GuavaSaplingBlock;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;

/** Actual planting pulses, exact native stock costs, scheduled support recovery and bounded growth. */
public final class SaplingStationGameTest {
    private SaplingStationGameTest() {}
    private static void pulse(GameTestHelper h, BlockPos dispenser, Runnable after) {
        var back = dispenser.west(); h.getLevel().setBlock(back,Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> { h.getLevel().setBlock(back,Blocks.AIR.defaultBlockState(),3); after.run(); });
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 9; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var target = h.absolutePos(new BlockPos(5,1,3)); var source = target.west();
        level.setBlock(source,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST),3);
        var dispenser = (DispenserBlockEntity)level.getBlockEntity(source);
        var stock = new ItemStack(ModItems.GUAVA_SAPLING.get(),3); stock.set(DataComponents.CUSTOM_NAME,Component.literal("Orchard saplings")); dispenser.setItem(0,stock);
        pulse(h,source,() -> {
            h.assertTrue(stock.getCount() == 3 && level.getBlockState(target).isAir(),"Invalid soil must retain all saplings");
            level.setBlock(target.below(),Blocks.DIRT.defaultBlockState(),3); level.setBlock(target,Blocks.CHEST.defaultBlockState(),3); var chest = level.getBlockEntity(target);
            pulse(h,source,() -> {
                h.assertTrue(stock.getCount() == 3 && level.getBlockEntity(target) == chest,"Occupied targets must preserve the container and named stock");
                for (var side : Direction.Plane.HORIZONTAL) if (side != Direction.WEST) level.setBlock(target.relative(side),Blocks.STONE.defaultBlockState(),3);
                level.setBlock(target,Blocks.WATER.defaultBlockState(),3);
                pulse(h,source,() -> {
                    h.assertTrue(stock.getCount() == 3 && level.getBlockState(target).is(Blocks.WATER),"Sapling stations must preserve fluids");
                    level.setBlock(target,Blocks.AIR.defaultBlockState(),3);
                    for (var side : Direction.Plane.HORIZONTAL) if (side != Direction.WEST) level.setBlock(target.relative(side),Blocks.AIR.defaultBlockState(),3);
                    pulse(h,source,() -> {
                        h.assertTrue(stock.getCount() == 2 && stock.getHoverName().getString().equals("Orchard saplings")
                                && level.getBlockState(target).is(ModBlocks.GUAVA_SAPLING.get()),"One valid pulse must place one native sapling and keep remaining components");
                        pulse(h,source,() -> {
                            h.assertTrue(stock.getCount() == 2,"Repeated pulses must not replace the sapling or spend stock");
                            h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(target).inflate(3),e -> e.getItem().is(ModItems.GUAVA_SAPLING.get())).isEmpty(),"Refused saplings must never eject");
                            level.setBlock(target.below(),Blocks.AIR.defaultBlockState(),3);
                            var meal = new ItemStack(Items.BONE_MEAL,2);
                            h.assertTrue(!BoneMealItem.applyBonemeal(meal,level,target,null) && meal.getCount() == 2
                                    && !GuavaSaplingBlock.tryGrow(level,target,RandomSource.create(42)),"Unsupported saplings must refuse native bone meal and direct growth");
                            h.runAfterDelay(2,() -> {
                                h.assertTrue(level.getBlockState(target).isAir() && level.getEntitiesOfClass(ItemEntity.class,new AABB(target).inflate(3),e -> e.getItem().is(ModItems.GUAVA_SAPLING.get()))
                                        .stream().mapToInt(e -> e.getItem().getCount()).sum() == 1,"Scheduled support loss must recover exactly one sapling");
                                var remote = new BlockPos(25000000,64,25000000); var chunks = level.getChunkSource();
                                h.assertTrue(chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null,"Remote growth fixture must start unloaded");
                                h.assertTrue(!GuavaSaplingBlock.tryGrow(level,remote,RandomSource.create(42))
                                        && chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null,"A direct growth request must not load missing terrain");
                                h.assertTrue(!GuavaSaplingBlock.tryGrow(level,target,RandomSource.create(42)),"Stale growth at an empty cell must refuse");
                                for (var soil : new net.minecraft.world.level.block.Block[]{Blocks.PODZOL,Blocks.MYCELIUM,Blocks.ROOTED_DIRT,Blocks.FARMLAND,Blocks.GRASS_BLOCK}) {
                                    level.setBlock(target.below(),soil.defaultBlockState(),3);
                                    h.assertTrue(ModBlocks.GUAVA_SAPLING.get().defaultBlockState().canSurvive(level,target),"Native vegetation soils must support guava saplings: "+soil);
                                }
                                pulse(h,source,() -> {
                                    h.assertTrue(stock.getCount() == 1 && level.getBlockState(target).is(ModBlocks.GUAVA_SAPLING.get()),"A repaired supported station must plant again: stock="+stock.getCount()+", target="+level.getBlockState(target));
                                    var obstruction = target.above(2); level.setBlock(obstruction,Blocks.CHEST.defaultBlockState(),3); var obstacle = level.getBlockEntity(obstruction);
                                    h.assertTrue(!GuavaSaplingBlock.tryGrow(level,target,RandomSource.create(42)) && level.getBlockEntity(obstruction) == obstacle
                                            && level.getBlockState(target).is(ModBlocks.GUAVA_SAPLING.get()),"Blocked trunks must preserve the sapling and container without partial growth");
                                    level.setBlock(obstruction,Blocks.AIR.defaultBlockState(),3);
                                    h.assertTrue(GuavaSaplingBlock.tryGrow(level,target,RandomSource.create(42)) && level.getBlockState(target).is(ModBlocks.GUAVA_LOG.get()),"A supported clear sapling must still grow its actual guava tree");
                                    h.succeed();
                                });
                            });
                        });
                    });
                });
            });
        });
    }
}
