package com.bunnidogs.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import java.util.function.Predicate;
import com.bunnidogs.mod.menu.CareMenu;

/** Shared native permission/reach checks for direct gestures and workshop menu actions. */
public final class StationUse {
    private StationUse() {}
    /** Plain materials can inspect a station; item-owned tools and enchanted preview cues keep native use. */
    private static boolean ordinary(ItemStack stack) {
        return !stack.isEmpty() && !stack.isEnchanted() && (stack.getItem().getClass()==Item.class || stack.getItem() instanceof BlockItem);
    }
    public static boolean inspectHeld(Player player,InteractionHand hand,Predicate<ItemStack> direct) {
        if (player.isShiftKeyDown() || !ordinary(player.getItemInHand(hand)) || direct.test(player.getItemInHand(hand))) return false;
        var off=player.getOffhandItem();
        return hand!=InteractionHand.MAIN_HAND || off.isEmpty() || ordinary(off) && !direct.test(off);
    }
    public static InteractionResult openHeld(Player player,BlockPos pos,InteractionHand hand) {
        return openHeld(player,pos,hand,stack->false);
    }
    public static InteractionResult openHeld(Player player,BlockPos pos,InteractionHand hand,Predicate<ItemStack> direct) {
        if (!inspectHeld(player,hand,direct)) return InteractionResult.PASS;
        CareMenu.open(player,pos); return InteractionResult.SUCCESS;
    }
    public static boolean editable(Player player,BlockPos pos) {
        var level=player.level();
        return !level.isClientSide() && player.isAlive() && !player.isSpectator() && player.mayBuild()
                && level.hasChunkAt(pos) && level.mayInteract(player,pos)
                && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36;
    }
    public static boolean editable(Player player,BlockEntity station) {
        return player.level()==station.getLevel() && !station.isRemoved() && editable(player,station.getBlockPos())
                && player.level().getBlockEntity(station.getBlockPos())==station;
    }
    public static boolean owns(Player player,ItemStack actual) {
        if (actual==null || actual.isEmpty()) return false;
        for (int i=0;i<player.getInventory().getContainerSize();i++) if (player.getInventory().getItem(i)==actual) return true;
        return false;
    }
    public static void give(Player player,ItemStack actual) {
        if (!actual.isEmpty() && !player.getInventory().add(actual)) player.drop(actual,false);
    }
}
