package com.bunnidogs.mod.item;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Small native milestones record real handoffs; none is a prerequisite for care. */
public final class JourneyProgress {
    private JourneyProgress() {}
    public static boolean award(Player player,String step) {
        if (!(player instanceof ServerPlayer server)) return false;
        var advancement=server.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","journey/"+step));
        return advancement!=null && server.getAdvancements().award(advancement,step.equals("root") ? "met" : "done");
    }
    public static void met(Player player) { award(player,"root"); }
    public static void housed(Player player) { award(player,"home"); }
    public static void courtship(Player player) { award(player,"courtship"); }
    public static void planted(Player player) {
        if (award(player,"plant")) player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("care.bunnidogs.feedback.planted"));
    }
    public static void incubating(Player player) {
        if (award(player,"plant")) player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("care.bunnidogs.feedback.incubating"));
    }
    public static void hatched(Player player) { award(player,"litter"); }
}
