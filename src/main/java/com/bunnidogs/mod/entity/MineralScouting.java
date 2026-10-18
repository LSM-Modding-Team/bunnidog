package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Scouting;
import com.bunnidogs.mod.item.PlaceMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

/** Deliberate local survey; no terrain edits, random rolls, path search or chunk requests. */
public final class MineralScouting {
    private MineralScouting() {}
    public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath("bunnidogs","surveyable_ores"));
    static BlockPos nearest(ServerLevel level, BlockPos center) {
        BlockPos best = null;
        double distance = Double.POSITIVE_INFINITY;
        int r = Scouting.RADIUS;
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            double squared = dx*dx+dy*dy+dz*dz;
            if (squared > r*r || squared >= distance) continue;
            var pos = center.offset(dx,dy,dz);
            if (level.hasChunkAt(pos) && level.getWorldBorder().isWithinBounds(pos)
                    && level.getBlockState(pos).is(ORES)) { best = pos; distance = squared; }
        }
        return best;
    }
    public static InteractionResult use(Bunnidog dog, Player player, InteractionHand hand) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!(dog.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var status = dog.scoutingStatus();
        if (status != Scouting.Status.READY) {
            Component reason = status == Scouting.Status.RESTING
                    ? Component.translatable("message.bunnidogs.scouting.wait",(dog.scoutingWait()+19)/20)
                    : Component.translatable("message.bunnidogs.scouting."+status.name().toLowerCase(java.util.Locale.ROOT));
            player.sendOverlayMessage(reason);
            return InteractionResult.SUCCESS;
        }
        var pos = nearest(level,dog.blockPosition());
        // Empty surveys also rest, so repeated clicks/gene edits cannot turn this into a continuous scanner.
        dog.beginScoutingRecovery();
        if (pos == null) player.sendOverlayMessage(Component.translatable("message.bunnidogs.scouting.none",Scouting.RADIUS));
        else {
            PlaceMarkers.bind(level,pos,player,hand);
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.scouting.marked",level.getBlockState(pos).getBlock().getName(),
                    pos.getX(),pos.getY(),pos.getZ()));
        }
        dog.playSound(SoundEvents.AMETHYST_BLOCK_CHIME,.5F,1.2F);
        return InteractionResult.SUCCESS;
    }
}
