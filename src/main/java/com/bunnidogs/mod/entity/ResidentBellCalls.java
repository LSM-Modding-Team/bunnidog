package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Social;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.VanillaGameEvent;

/** Relay actual native rings once; no polling, ticket changes or remote block searches. */
public final class ResidentBellCalls {
    private ResidentBellCalls() {}
    public static void onGameEvent(VanillaGameEvent event) {
        if (event.isCanceled() || !event.getVanillaEvent().equals(GameEvent.BLOCK_CHANGE)
                || !(event.getLevel() instanceof ServerLevel server)) return;
        BlockPos bell = BlockPos.containing(event.getEventPosition());
        if (!server.hasChunkAt(bell) || !server.getBlockState(bell).is(Blocks.BELL)
                || !(server.getBlockEntity(bell) instanceof BellBlockEntity ringing) || !ringing.shaking || ringing.ticks != 0) return;
        for (Bunnidog dog : server.getEntitiesOfClass(Bunnidog.class,new AABB(bell).inflate(Social.CALL_RANGE)))
            if (bell.closerToCenterThan(dog.position(),Social.CALL_RANGE)) dog.hearBell(bell);
    }
}
