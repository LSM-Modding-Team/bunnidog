package com.bunnidogs.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Shared native permission/reach checks for direct gestures and workshop menu actions. */
public final class StationUse {
    private StationUse() {}
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
