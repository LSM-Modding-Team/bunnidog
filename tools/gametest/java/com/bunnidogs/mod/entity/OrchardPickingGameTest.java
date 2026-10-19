package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;

/** Real redstone picking, ordinary hopper collection and delegated native beehive shearing. */
public final class OrchardPickingGameTest {
    private OrchardPickingGameTest() {}
    private static void pulse(GameTestHelper h, BlockPos source, Runnable after) {
        h.getLevel().setBlock(source.west(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> { h.getLevel().setBlock(source.west(),Blocks.AIR.defaultBlockState(),3); after.run(); });
    }
    private static int products(GameTestHelper h, BlockPos fruit, HopperBlockEntity hopper, Item item) {
        int count = h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(fruit).inflate(4),e -> e.getItem().is(item))
                .stream().mapToInt(e -> e.getItem().getCount()).sum();
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) if (hopper.getItem(slot).is(item)) count += hopper.getItem(slot).getCount();
        return count;
    }
    private static void planting(GameTestHelper h, BlockPos fruit, BlockPos source, DispenserBlockEntity dispenser, HopperBlockEntity hopper) {
        var level = h.getLevel();
        level.setBlock(fruit.above(),Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);
        var stock = new ItemStack(ModItems.GUAVA.get(),3); stock.set(DataComponents.CUSTOM_NAME,Component.literal("Canopy repair")); dispenser.setItem(0,stock);
        pulse(h,source,() -> {
            h.assertTrue(level.getBlockState(fruit).is(ModBlocks.GUAVA_FRUIT.get()) && level.getBlockState(fruit).getValue(GuavaFruitBlock.AGE) == 0
                    && stock.getCount() == 2 && stock.getHoverName().getString().equals("Canopy repair")
                    && products(h,fruit,hopper,ModItems.GUAVA.get()) == 2,"Canopy repair must plant one real plain guava and preserve its named remainder without ejecting a copy");
            level.setBlock(fruit,Blocks.CHEST.defaultBlockState(),3); var chest = level.getBlockEntity(fruit);
            pulse(h,source,() -> {
                h.assertTrue(level.getBlockEntity(fruit) == chest && stock.getCount() == 1 && products(h,fruit,hopper,ModItems.GUAVA.get()) == 3,
                        "Blocked planting must preserve the container and retain ordinary one-item food ejection");
                level.removeBlock(fruit,false); var upgraded = new ItemStack(ModItems.GOLDEN_GUAVA.get()); dispenser.setItem(0,upgraded);
                pulse(h,source,() -> {
                    h.assertTrue(upgraded.isEmpty() && level.getBlockState(fruit).isAir() && products(h,fruit,hopper,ModItems.GOLDEN_GUAVA.get()) == 1,
                            "Upgraded guavas must keep ordinary dispenser ejection without planting fruit");
                    h.succeed();
                });
            });
        });
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 7; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var fruit = h.absolutePos(new BlockPos(5,2,3)); var source = fruit.west();
        level.setBlock(fruit.below(),Blocks.HOPPER.defaultBlockState(),3);
        var hopper = (HopperBlockEntity)level.getBlockEntity(fruit.below());
        level.setBlock(fruit.above(),Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);
        var ripe = ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(GuavaFruitBlock.AGE,2);
        level.setBlock(fruit,ripe,3);
        level.setBlock(source,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST),3);
        var dispenser = (DispenserBlockEntity)level.getBlockEntity(source); var shears = new ItemStack(Items.SHEARS); dispenser.setItem(0,shears);
        pulse(h,source,() -> {
            h.assertTrue(level.getBlockState(fruit).is(ModBlocks.GUAVA_FRUIT.get()) && level.getBlockState(fruit).getValue(GuavaFruitBlock.AGE) == 0
                    && shears.getDamageValue() == 1 && products(h,fruit,hopper,ModItems.GUAVA.get()) == 1,
                    "One shears pulse must reset ripe fruit, pay one guava and wear exactly once");
            pulse(h,source,() -> {
                h.assertTrue(shears.getDamageValue() == 1 && products(h,fruit,hopper,ModItems.GUAVA.get()) == 1,"Unripe pulses must retain shears without duplicate food or wear");
                level.setBlock(fruit,ripe,3);
                h.assertTrue(GuavaFruitBlock.pick(level,fruit) && !GuavaFruitBlock.pick(level,fruit)
                        && products(h,fruit,hopper,ModItems.GUAVA.get()) == 2,"Hand/shared picking must pay once and reject a stale repeat");
                level.setBlock(fruit,ripe,3); level.removeBlock(fruit.above(),false);
                h.assertTrue(!GuavaFruitBlock.pick(level,fruit),"Unsupported ripe fruit must not be picked before its scheduled fall");
                level.removeBlock(fruit,false);
                var remote = new BlockPos(25000000,64,25000000); var chunks = level.getChunkSource();
                h.assertTrue(chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null && !GuavaFruitBlock.pick(level,remote)
                        && chunks.getChunkNow(remote.getX()>>4,remote.getZ()>>4) == null,"Picking must never load missing terrain");
                level.setBlock(fruit,Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL,5),3);
                pulse(h,source,() -> {
                    h.assertTrue(level.getBlockState(fruit).getValue(BeehiveBlock.HONEY_LEVEL) == 0 && shears.getDamageValue() == 2
                            && products(h,fruit,hopper,Items.HONEYCOMB) == 3,"Non-guava targets must retain native beehive output and tool wear");
                    level.removeBlock(fruit,false);
                    h.runAfterDelay(25,() -> {
                        int collected = 0;
                        for (int slot = 0; slot < hopper.getContainerSize(); slot++) if (hopper.getItem(slot).is(ModItems.GUAVA.get())) collected += hopper.getItem(slot).getCount();
                        h.assertTrue(collected == 2 && products(h,fruit,hopper,ModItems.GUAVA.get()) == 2,"An ordinary hopper must collect both paid fruit with no extra copy");
                        planting(h,fruit,source,dispenser,hopper);
                    });
                });
            });
        });
    }
}
