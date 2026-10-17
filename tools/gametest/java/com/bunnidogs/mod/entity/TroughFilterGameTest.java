package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class TroughFilterGameTest {
    private TroughFilterGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        h.setBlock(3,1,3,ModBlocks.FEEDING_TROUGH.get());
        var pos = h.absolutePos(new BlockPos(3,1,3));
        var trough = (FeedingTroughBlockEntity)level.getBlockEntity(pos);
        var player = h.makeMockServerPlayerInLevel();
        player.setShiftKeyDown(true);
        var named = new ItemStack(Items.CARROT,8);
        named.set(DataComponents.CUSTOM_NAME,Component.literal("Nursery carrots"));
        player.setItemInHand(InteractionHand.MAIN_HAND,named);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        h.assertTrue(player.gameMode.useItemOn(player,level,named,InteractionHand.MAIN_HAND,hit).consumesAction(),
                "Actual crouched server interaction must reach filtering instead of bypassing the block");
        h.assertTrue(trough.food().isEmpty() && named.getCount() == 8 && trough.filter().getCount() == 1
                && ItemStack.isSameItemSameComponents(named,trough.filter()),"A virtual exact-component filter must spend no food or create stock");
        var handler = level.getCapability(Capabilities.Item.BLOCK,pos,Direction.UP);
        try (var tx = Transaction.openRoot()) {
            h.assertTrue(handler.insert(ItemResource.of(Items.CARROT),3,tx) == 0
                    && handler.insert(ItemResource.of(Items.POTATO),3,tx) == 0
                    && handler.insert(ItemResource.of(named),3,tx) == 3,"Empty filtered automation must reject different components/types and accept its exact template");
            tx.commit();
        }
        var potato = new ItemStack(Items.POTATO,8);
        player.setItemInHand(InteractionHand.MAIN_HAND,potato);
        player.gameMode.useItemOn(player,level,potato,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(trough.food().getCount() == 3 && ItemStack.isSameItemSameComponents(named,trough.filter())
                && potato.getCount() == 8,"A conflicting filter change must retain stock, filter and held food");
        var saved = trough.saveWithFullMetadata(level.registryAccess());
        var restored = (FeedingTroughBlockEntity)BlockEntity.loadStatic(pos,trough.getBlockState(),saved,level.registryAccess());
        h.assertTrue(ItemStack.matches(restored.food(),trough.food()) && ItemStack.matches(restored.filter(),trough.filter())
                && restored.insert(new ItemStack(Items.CARROT)) == 0,"Native saves must retain exact stock/filter components and enforce the filter");
        saved.remove("FoodFilter");
        var legacy = (FeedingTroughBlockEntity)BlockEntity.loadStatic(pos,trough.getBlockState(),saved,level.registryAccess());
        h.assertTrue(legacy.filter().isEmpty() && legacy.food().getCount() == 3,"Legacy saves must keep stock and remain unfiltered");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(trough.food().isEmpty() && !trough.filter().isEmpty(),"Withdrawing the final stock must preserve its reservation");
        int beforeClear = player.getInventory().countItem(Items.CARROT);
        // Native withdrawal may fill the selected empty slot; free the hand without discarding that food.
        player.getInventory().setItem(1,player.getMainHandItem());
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(trough.filter().isEmpty() && beforeClear == 3 && player.getInventory().countItem(Items.CARROT) == 3,
                "Clearing an empty reservation must never refund its virtual template");
        trough.setFilter(named); trough.insert(named.copyWithCount(2));
        level.destroyBlock(pos,true);
        int dropped = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),e -> ItemStack.isSameItemSameComponents(named,e.getItem()))
                .stream().mapToInt(e -> e.getItem().getCount()).sum();
        h.assertTrue(dropped == 2,"Breaking a reserved trough must drop only real stock, never its filter template");
        h.succeed();
    }
}
