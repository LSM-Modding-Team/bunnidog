package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.BunnidogsMod;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Native per-player milestones, awarded only after the corresponding animal interaction succeeds. */
final class WorkingLineProgress {
    private WorkingLineProgress() {}
    static void guard(Player player) { award(player,"standing_watch","enabled"); }
    static void survey(Player player, boolean focused) {
        award(player,"quiet_listener","surveyed");
        if (focused) award(player,"discerning_ear","focused");
    }
    private static void award(Player player, String path, String criterion) {
        if (player instanceof ServerPlayer server) {
            var advancement = server.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(BunnidogsMod.MODID,path));
            if (advancement != null) server.getAdvancements().award(advancement,criterion);
        }
    }
}
