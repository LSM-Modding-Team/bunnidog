package com.bunnidogs.mod.entity;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** Owner-scoped party inspection. Locations are remembered sightings, never remote entity/chunk searches. */
public final class CompanionParty {
    private CompanionParty() {}
    public record Member(UUID id,Component name,Component detail,boolean commanded) {}
    public static List<Member> members(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return List.of();
        var roster=CombatRoster.get(level); var keeper=player.getUUID();
        return roster.members(keeper).stream().map(id->{
            var seen=roster.sighting(keeper,id); var found=level.getEntity(id);
            Component name=Component.literal(seen==null ? id.toString().substring(0,8) : seen.name());
            Component detail=Component.translatable("care.bunnidogs.party.away");
            if (found instanceof Bunnidog dog && dog.trail().belongsTo(player) && dog.isAlive() && dog.distanceToSqr(player)<=4096) {
                name=dog.getDisplayName(); detail=Component.translatable("care.bunnidogs.combat.intent."+dog.combat().intent());
            } else if (seen!=null) detail=Component.translatable("care.bunnidogs.party.seen",seen.dimension(),seen.pos().getX(),seen.pos().getY(),seen.pos().getZ());
            return new Member(id,name,detail,id.equals(roster.animal(keeper)));
        }).toList();
    }
    public static boolean canChange(Player player) {
        return player.mayBuild() && player.level() instanceof ServerLevel level && !CompanionCombat.keeperBusy(player)
                && !CombatRoster.get(level).busy(player.getUUID(),level.getServer().overworld().getGameTime());
    }
    public static boolean remove(Player player,UUID id) {
        return canChange(player) && CombatRoster.get((ServerLevel)player.level()).remove(player.getUUID(),id,
                ((ServerLevel)player.level()).getServer().overworld().getGameTime());
    }
    public static boolean command(Player player,UUID id) {
        if (!player.mayBuild() || !(player.level() instanceof ServerLevel level)) return false;
        var roster=CombatRoster.get(level);
        return roster.chosen(player.getUUID(),id) && roster.choose(player.getUUID(),id,level.getServer().overworld().getGameTime());
    }
    public static String context(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return "";
        var roster=CombatRoster.get(level);
        return roster.members(player.getUUID()).toString()+roster.animal(player.getUUID())+canChange(player);
    }
}
