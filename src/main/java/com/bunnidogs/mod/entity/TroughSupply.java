package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Supplying;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.item.GuavaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Native shared inventory journals make a one-item transfer atomic before stock notifications. */
final class TroughSupply {
    private TroughSupply() {}
    static FeedingTroughBlockEntity trough(Bunnidog dog, BlockPos pos) {
        if (!(dog.level() instanceof ServerLevel level) || !dog.foodWithinHome(pos)
                || !level.hasChunksAt(pos.offset(-2,-2,-2),pos.offset(2,2,2))
                || !(level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity stock)
                || stock.isRemoved() || stock.residentsPaused()) return null;
        return stock;
    }
    static boolean source(Bunnidog dog, FeedingTroughBlockEntity stock, ItemStack expected) {
        return stock != null && stock.workMode() == TroughWork.SUPPLY && stock.getBlockState().getValue(FeedingTroughBlock.TENDING)
                && stock.food().getCount() > Supplying.RESERVE && !expected.isEmpty() && GuavaItem.tierOf(expected) == 0
                && ItemStack.isSameItemSameComponents(stock.food(),expected) && dog.canEat(expected);
    }
    static boolean destination(FeedingTroughBlockEntity source, FeedingTroughBlockEntity target, ItemStack unit) {
        return target != null && source != target && target.workMode() != TroughWork.SUPPLY
                && source.getBlockPos().distSqr(target.getBlockPos()) <= Supplying.RANGE*Supplying.RANGE
                && target.food().getCount() < Supplying.RESERVE && target.canInsert(unit);
    }
    /** Caller checks current eligibility and commits only when both sides accept exactly one. */
    static boolean reserve(FeedingTroughBlockEntity source, FeedingTroughBlockEntity target, ItemStack unit, TransactionContext transaction) {
        var resource = ItemResource.of(unit);
        return source.automation().extract(resource,1,transaction) == 1 && target.automation().insert(resource,1,transaction) == 1;
    }
}
