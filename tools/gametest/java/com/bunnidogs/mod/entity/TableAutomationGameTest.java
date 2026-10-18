package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Real hopper/comparator transactions and actual held-stack placement share the saved A/B inputs. */
public final class TableAutomationGameTest {
    private TableAutomationGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        h.setBlock(3,0,3,Blocks.REDSTONE_BLOCK); h.setBlock(4,1,3,Blocks.STONE);
        h.setBlock(new BlockPos(3,1,3),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.ENABLED,false));
        h.setBlock(3,2,3,ModBlocks.GENETICS_TABLE.get()); h.setBlock(3,3,3,Blocks.HOPPER);
        h.setBlock(new BlockPos(4,2,3),Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING,Direction.WEST));
        var pos = h.absolutePos(new BlockPos(3,2,3));
        var table = (GeneticsTableBlockEntity)level.getBlockEntity(pos);
        var input = (HopperBlockEntity)level.getBlockEntity(pos.above());
        var output = (HopperBlockEntity)level.getBlockEntity(pos.below());
        var comparator = (ComparatorBlockEntity)level.getBlockEntity(pos.east());
        var a = TissueSampleItem.create(Genome.DEFAULT,Stats.of(1,2,3,4,5));
        var b = TissueSampleItem.create(Genome.DEFAULT,Stats.of(5,4,3,2,1));
        a.set(DataComponents.CUSTOM_NAME,Component.literal("Mother's original sample"));
        b.set(DataComponents.CUSTOM_NAME,Component.literal("Father's original sample"));
        h.runAfterDelay(2,() -> {
            var handler = level.getCapability(Capabilities.Item.BLOCK,pos,Direction.UP);
            h.assertTrue(handler != null && handler.size() == 2
                    && handler == level.getCapability(Capabilities.Item.BLOCK,pos,Direction.DOWN),"Every side shares the two actual sample slots");
            input.setItem(0,a.copy()); input.setItem(1,b.copy()); input.setItem(2,a.copyWithCount(3));
            for (int tick = 0; tick < 80; tick++) HopperBlockEntity.pushItemsTick(level,pos.above(),input.getBlockState(),input);
            h.assertTrue(ItemStack.matches(table.item(0),a) && ItemStack.matches(table.item(1),b)
                    && input.getItem(2).getCount() == 3,"Real hopper input fills A then B exactly once and cannot swap or stack further samples");
            input.clearContent();
            try (var tx = Transaction.openRoot()) {
                h.assertTrue(handler.extract(0,ItemResource.of(a),1,tx) == 1 && handler.extract(1,ItemResource.of(b),1,tx) == 1,
                        "Both slots can participate in one tentative transfer");
            }
            h.assertTrue(ItemStack.matches(table.item(0),a) && ItemStack.matches(table.item(1),b) && table.stockSignal() == 15,
                    "Aborting a multi-slot transfer restores both full-component samples and the signal");
            var saved = table.saveWithFullMetadata(level.registryAccess());
            var restored = (GeneticsTableBlockEntity)BlockEntity.loadStatic(pos,table.getBlockState(),saved,level.registryAccess());
            h.assertTrue(ItemStack.matches(restored.item(0),a) && ItemStack.matches(restored.item(1),b),"Automation retains the native A/B save format");
            var legacy = saved.copy(); legacy.put("Item",legacy.get("SampleA")); legacy.remove("SampleA"); legacy.remove("SampleB");
            var old = (GeneticsTableBlockEntity)BlockEntity.loadStatic(pos,table.getBlockState(),legacy,level.registryAccess());
            h.assertTrue(ItemStack.matches(old.item(0),a) && old.item(1).isEmpty() && old.automation().getResource(0).matches(a),
                    "The old one-sample Item key must still feed the same automation view");
            h.runAfterDelay(3,() -> {
                h.assertTrue(comparator.getOutputSignal() == 15,"Native comparators must see two occupied samples");
                table.takeItem(1);
                try (var tx = Transaction.openRoot()) {
                    h.assertTrue(handler.insert(ItemResource.of(Items.STONE),1,tx) == 0
                            && handler.insert(ItemResource.of(ModItems.TISSUE_SAMPLE.get()),1,tx) == 0,
                            "Non-samples and blank unreadable samples must refuse automation");
                    tx.commit();
                }
                h.runAfterDelay(3,() -> {
                    h.assertTrue(comparator.getOutputSignal() == 8,"One occupied sample gives the native half-full signal");
                    level.setBlock(pos.below(2),Blocks.STONE.defaultBlockState(),3);
                    HopperBlockEntity.pushItemsTick(level,pos.below(),output.getBlockState(),output);
                    level.setBlock(pos.below(2),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                    h.assertTrue(table.item(0).isEmpty() && ItemStack.matches(output.getItem(0),a),"A real output hopper extracts the intact first sample");
                    h.runAfterDelay(3,() -> {
                        h.assertTrue(comparator.getOutputSignal() == 0,"Empty automation clears the actual comparator");
                        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                        var held = b.copyWithCount(3); player.setItemInHand(InteractionHand.OFF_HAND,held);
                        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
                        h.assertTrue(!table.getBlockState().useWithoutItem(level,player,hit).consumesAction(),
                                "An empty main hand must let an off-hand sample reach native item use");
                        player.gameMode.useItemOn(player,level,held,InteractionHand.OFF_HAND,hit);
                        h.assertTrue(held.getCount() == 2 && ItemStack.matches(table.item(0),b) && player.getMainHandItem().isEmpty(),
                                "Survival off-hand placement spends one actual sample and retains all its details");
                        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
                        player.gameMode.useItemOn(player,level,held,InteractionHand.OFF_HAND,hit);
                        h.assertTrue(held.getCount() == 2 && ItemStack.matches(table.item(1),b),"Creative off-hand placement changes the table while preserving its real stack");
                        level.destroyBlock(pos,true);
                        int dropped = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),
                                item -> ItemStack.isSameItemSameComponents(item.getItem(),b)).stream().mapToInt(item -> item.getItem().getCount()).sum();
                        h.assertTrue(dropped == 2,"Breaking an automated table drops exactly its two remaining named samples");
                        h.succeed();
                    });
                });
            });
        });
    }
}
