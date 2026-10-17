package com.bunnidogs.mod.block;

import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Vanilla normally bypasses blocks when crouching with food; allow only this reserved action. */
public final class TroughInteraction {
    private TroughInteraction() {}
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        if (!event.isCanceled() && !event.getUseBlock().isFalse() && player.mayBuild() && player.isShiftKeyDown()
                && FeedingTroughBlockEntity.accepts(player.getItemInHand(event.getHand()))
                && event.getLevel().hasChunkAt(event.getPos())
                && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof FeedingTroughBlock)
            event.setUseBlock(TriState.TRUE);
    }
}
