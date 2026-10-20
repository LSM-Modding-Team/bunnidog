package com.bunnidogs.mod.block;

import net.minecraft.util.TriState;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Native crouching bypasses blocks; allow only the deliberate food/hoe station controls. */
public final class TroughInteraction {
    private TroughInteraction() {}
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        if (!event.isCanceled() && !event.getUseBlock().isFalse() && player.mayBuild() && player.isShiftKeyDown()
                && (FeedingTroughBlockEntity.accepts(player.getItemInHand(event.getHand()))
                    || player.getItemInHand(event.getHand()).getItem() instanceof com.bunnidogs.mod.item.GuavaItem
                    || player.getItemInHand(event.getHand()).is(ItemTags.HOES))
                && event.getLevel().hasChunkAt(event.getPos())
                && (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof FeedingTroughBlock
                    || player.getItemInHand(event.getHand()).is(ItemTags.HOES)
                        && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof NurseryCradleBlock))
            event.setUseBlock(TriState.TRUE);
    }
}
