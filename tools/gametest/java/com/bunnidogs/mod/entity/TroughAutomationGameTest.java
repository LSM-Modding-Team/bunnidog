package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class TroughAutomationGameTest {
    private TroughAutomationGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        h.setBlock(3,0,3,Blocks.REDSTONE_BLOCK); h.setBlock(4,1,3,Blocks.STONE);
        h.setBlock(new BlockPos(3,1,3),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.ENABLED,false));
        h.setBlock(3,2,3,ModBlocks.FEEDING_TROUGH.get());
        h.setBlock(3,3,3,Blocks.HOPPER);
        h.setBlock(new BlockPos(4,2,3),Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING,Direction.WEST));
        var pos = h.absolutePos(new BlockPos(3,2,3));
        var comparator = (ComparatorBlockEntity)level.getBlockEntity(pos.east());
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(pos);
        var input = (HopperBlockEntity)level.getBlockEntity(pos.above());
        var output = (HopperBlockEntity)level.getBlockEntity(pos.below());
        var named = new ItemStack(Items.CARROT,64);
        named.set(DataComponents.CUSTOM_NAME,Component.literal("Delivered carrots"));
        h.runAfterDelay(2,() -> {
            var handler = level.getCapability(Capabilities.Item.BLOCK,pos,Direction.UP);
            h.assertTrue(handler != null && handler == level.getCapability(Capabilities.Item.BLOCK,pos,Direction.DOWN),
                    "All sides must share one transactional view of the real stock");
            input.setItem(0,named.copy());
            for (int tick = 0; tick < 160; tick++)
                HopperBlockEntity.pushItemsTick(level,pos.above(),input.getBlockState(),input);
            h.assertTrue(trough.food().getCount() == 16 && input.getItem(0).getCount() == 48
                    && ItemStack.isSameItemSameComponents(named,trough.food()),
                    "Real hopper pushes must stop at 16, retaining every remaining item and its components");
            h.assertTrue(level.getBlockState(pos).getValue(FeedingTroughBlock.FILL) == 3,"Committed automation must update visible stock");
            input.clearContent();
            try (var transaction = Transaction.openRoot()) {
                h.assertTrue(handler.extract(ItemResource.of(named),5,transaction) == 5,"A transfer may tentatively extract matching stock");
            }
            h.assertTrue(trough.food().getCount() == 16 && trough.stockSignal() == 15,
                    "An uncommitted transfer must restore exact stock and leave visible/redstone state alone");
            h.runAfterDelay(3,() -> {
                h.assertTrue(comparator.getOutputSignal() == 15,"A real neighbouring comparator must notice a full trough");
                level.setBlock(pos.below(2),Blocks.STONE.defaultBlockState(),3);
                HopperBlockEntity.pushItemsTick(level,pos.below(),output.getBlockState(),output);
                level.setBlock(pos.below(2),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                h.assertTrue(trough.food().getCount() == 15 && output.getItem(0).getCount() == 1
                        && ItemStack.isSameItemSameComponents(named,output.getItem(0)),
                        "A real hopper below must extract exactly one preserved item");
                try (var transaction = Transaction.openRoot()) {
                    h.assertTrue(handler.insert(ItemResource.of(Items.CARROT),1,transaction) == 0
                            && handler.insert(ItemResource.of(Items.STONE),1,transaction) == 0
                            && handler.insert(ItemResource.of(Items.MUSHROOM_STEW),1,transaction) == 0,
                            "Automation must reject mismatched components, non-food and remainder-bearing food");
                    transaction.commit();
                }
                h.runAfterDelay(3,() -> {
                    h.assertTrue(comparator.getOutputSignal() == 14,"Extraction must update the comparator even within the same visual fill level");
                    var savedStock = trough.takeAll();
                    var small = new ItemStack(ModItems.GUAVA.get());
                    small.set(DataComponents.MAX_STACK_SIZE,1);
                    try (var transaction = Transaction.openRoot()) {
                        h.assertTrue(handler.insert(ItemResource.of(small),8,transaction) == 1,
                                "A smaller component-defined stack limit must cap even a bulk transfer");
                        transaction.commit();
                    }
                    h.assertTrue(trough.food().getCount() == 1 && trough.stockSignal() == 15,
                            "Comparator fullness must use the effective item capacity");
                    try (var transaction = Transaction.openRoot()) {
                        h.assertTrue(handler.extract(ItemResource.of(small),8,transaction) == 1,"Extraction must respect available stock");
                        transaction.commit();
                    }
                    h.runAfterDelay(3,() -> {
                        h.assertTrue(comparator.getOutputSignal() == 0 && trough.food().isEmpty()
                                && level.getBlockState(pos).getValue(FeedingTroughBlock.FILL) == 0,"Empty committed stock must clear both outputs");
                        trough.insert(savedStock);
                        level.destroyBlock(pos,true);
                        int dropped = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),
                                item -> ItemStack.isSameItemSameComponents(item.getItem(),savedStock))
                                .stream().mapToInt(item -> item.getItem().getCount()).sum();
                        h.assertTrue(dropped == 15,"Breaking an automated trough must drop its preserved stock exactly once");
                        h.succeed();
                    });
                });
            });
        });
    }
}
