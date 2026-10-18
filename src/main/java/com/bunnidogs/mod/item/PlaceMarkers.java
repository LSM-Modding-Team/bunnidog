package com.bunnidogs.mod.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

/** Persistent native place markers shared by Nests, residents and mineral scouts. Call on the server with a compass. */
public final class PlaceMarkers {
    private PlaceMarkers() {}
    public static void bind(Level level, BlockPos pos, Player player, InteractionHand hand) {
        var actual = player.getItemInHand(hand);
        var tracker = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(),pos)),false);
        if (actual.getCount() == 1) actual.set(DataComponents.LODESTONE_TRACKER,tracker);
        else {
            var marked = actual.copyWithCount(1);
            marked.set(DataComponents.LODESTONE_TRACKER,tracker);
            actual.consume(1,player);
            if (!player.getInventory().add(marked)) player.drop(marked,false);
        }
    }
}
