package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Scavenging;
import com.bunnidogs.mod.item.GuavaItem;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** One real ground item, with a shared native journal for its destination; never a carried-item save. */
final class LooseFood {
    private LooseFood() {}
    static boolean available(Bunnidog dog, ItemEntity item) {
        if (!(dog.level() instanceof ServerLevel level) || item == null || item.level() != level || !item.isAlive()
                || item.hasPickUpDelay() || item.getTarget() != null || !Scavenging.oldEnough(item.getAge())
                || item.isPassenger() || item.isVehicle() || item.isOnFire() || !level.hasChunkAt(item.blockPosition())
                || !dog.foodWithinHome(item.blockPosition()) || !level.getFluidState(item.blockPosition()).isEmpty()
                || !level.getGameRules().get(GameRules.MOB_GRIEFING)) return false;
        var stack = item.getItem();
        return FeedingTroughBlockEntity.accepts(stack) && GuavaItem.tierOf(stack) == 0 && dog.canEat(stack);
    }
    private static final class GroundStack extends ItemStackResourceHandler {
        private final ItemEntity item;
        GroundStack(ItemEntity item) { this.item = item; }
        @Override protected ItemStack getStack() { return this.item.getItem(); }
        @Override protected void setStack(ItemStack stack) { this.item.setItem(stack); }
    }
    /** Caller commits only success; abort restores both the entity and the trough, including complete components. */
    static boolean reserve(ItemEntity item, FeedingTroughBlockEntity trough, ItemStack unit, TransactionContext transaction) {
        var resource = ItemResource.of(unit);
        return new GroundStack(item).extract(resource,1,transaction) == 1
                && (trough == null || trough.automation().insert(resource,1,transaction) == 1);
    }
    static boolean take(Bunnidog dog, ItemEntity item, FeedingTroughBlockEntity trough, ItemStack expected) {
        if (!available(dog,item) || !ItemStack.isSameItemSameComponents(item.getItem(),expected)
                || trough != null && (trough.isRemoved() || trough.residentsPaused() || !trough.canInsert(expected))) return false;
        var unit = expected.copyWithCount(1);
        try (var transaction = Transaction.openRoot()) {
            if (!reserve(item,trough,unit,transaction)) return false;
            transaction.commit();
        }
        // The live stack may now be empty. Native thrown-item criteria receive the committed one-item snapshot.
        if (item.getOwner() instanceof ServerPlayer player) CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger(player,unit,dog);
        dog.take(item,1);
        if (item.getItem().isEmpty()) item.discard();
        return true;
    }
}
