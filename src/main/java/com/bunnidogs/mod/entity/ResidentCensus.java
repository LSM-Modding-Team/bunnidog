package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.bunnidogs.mod.genetics.Social;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Cheap read-only roll call. Cached homes and loaded entities only; no POI, route or food searches. */
public final class ResidentCensus {
    private static final int MAX_ROWS = 8;
    private ResidentCensus() {}
    static List<Bunnidog> nearby(ServerLevel level, BlockPos bell) {
        var residents = level.getEntitiesOfClass(Bunnidog.class,new AABB(bell).inflate(Social.CALL_RANGE),
                dog -> dog.isAlive() && dog.acceptsCare() && !dog.isBaby() && dog.homeNest() != null
                        && dog.homeNest().distSqr(bell) <= Social.BELL_RANGE*Social.BELL_RANGE
                        && bell.closerToCenterThan(dog.position(),Social.CALL_RANGE)
                        && level.getWorldBorder().isWithinBounds(dog.blockPosition()));
        residents.sort(Comparator.comparing((Bunnidog dog) -> dog.getHealth() >= dog.getMaxHealth())
                .thenComparingDouble(dog -> dog.homeNest().distSqr(bell)).thenComparing(Bunnidog::getUUID));
        return residents;
    }
    public static void show(ServerLevel level, BlockPos bell, Player player) {
        var residents = nearby(level,bell);
        long injured = residents.stream().filter(dog -> dog.getHealth() < dog.getMaxHealth()).count();
        player.sendSystemMessage(Component.translatable("message.bunnidogs.census.header",residents.size(),injured,
                Social.BELL_RANGE,Social.CALL_RANGE).withStyle(ChatFormatting.GOLD));
        for (var dog : residents.subList(0,Math.min(MAX_ROWS,residents.size()))) {
            var home = dog.homeNest();
            player.sendSystemMessage(Component.translatable("message.bunnidogs.census.row",dog.getDisplayName(),dog.earTag().orElse("—"),
                    String.format(Locale.ROOT,"%.1f / %.1f",dog.getHealth(),dog.getMaxHealth()),dog.diet().foods(),
                    home.getX(),home.getY(),home.getZ()).withStyle(dog.getHealth() < dog.getMaxHealth() ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
        }
        if (residents.size() > MAX_ROWS) player.sendSystemMessage(Component.translatable("message.bunnidogs.census.more",residents.size()-MAX_ROWS)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
