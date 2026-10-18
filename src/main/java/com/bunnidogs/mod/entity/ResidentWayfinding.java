package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.item.PlaceMarkers;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/** Marks the animal's recorded home, including while its terrain is unloaded. Never moves or claims it. */
public final class ResidentWayfinding {
    private ResidentWayfinding() {}
    public static InteractionResult markHome(Bunnidog dog, Player player, InteractionHand hand) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!(dog.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var home = dog.homeNest();
        if (!dog.isAlive() || !dog.acceptsCare() || home == null) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.home_marker.none"));
        } else if (!level.getWorldBorder().isWithinBounds(home) || level.isOutsideBuildHeight(home)
                || level.hasChunkAt(home) && !level.getBlockState(home).is(ModBlocks.NEST.get())) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.home_marker.missing"));
        } else {
            PlaceMarkers.bind(level,home,player,hand);
            player.sendSystemMessage(Component.translatable("message.bunnidogs.home_marker.marked",dog.getDisplayName(),
                    home.getX(),home.getY(),home.getZ()));
            level.playSound(null,dog.blockPosition(),SoundEvents.LODESTONE_COMPASS_LOCK,SoundSource.PLAYERS,.6F,1);
        }
        return InteractionResult.SUCCESS;
    }
}
