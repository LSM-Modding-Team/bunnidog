package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.menu.CareMenu;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** Temporary attention while a real nearby player has this animal's interaction menu open. Never saved. */
public final class BunnidogConversation {
    private final Bunnidog dog;
    private final Map<UUID,CareMenu> viewers=new LinkedHashMap<>();
    BunnidogConversation(Bunnidog dog) { this.dog=dog; }
    public void begin(ServerPlayer player,CareMenu menu) {
        viewers.put(player.getUUID(),menu);
        if (calm(player)) hold();
    }
    public void end(Player player,CareMenu menu) { viewers.remove(player.getUUID(),menu); }
    private boolean calm(Player player) {
        return dog.isAlive() && dog.acceptsCare() && !dog.combat().downed() && !dog.combat().danger()
                && dog.getTarget()==null && !dog.isOnFire() && !dog.isInWater() && !dog.isInLava()
                && !dog.beingTransported() && !dog.isVehicle()
                && !(dog.getLastHurtByMob()==player && dog.tickCount-dog.getLastHurtByMobTimestamp()<100);
    }
    public Player viewer() {
        if (viewers.isEmpty() || !(dog.level() instanceof ServerLevel level)) return null;
        var entries=viewers.entrySet().iterator();
        while (entries.hasNext()) {
            var entry=entries.next(); var player=level.getPlayerByUUID(entry.getKey());
            if (player==null || !player.isAlive() || player.isSpectator() || !entry.getValue().talksTo(dog)) {
                entries.remove(); continue;
            }
            if (calm(player)) return player;
        }
        return null;
    }
    public boolean recognizes(Player player) {
        var menu=player==null ? null : viewers.get(player.getUUID());
        return menu!=null && menu.talksTo(dog) && calm(player);
    }
    private void hold() {
        dog.getNavigation().stop(); dog.setXxa(0); dog.setZza(0); dog.setSpeed(0);
    }
    /** registerGoals runs before the animal's fields initialize; read conversation state only at runtime. */
    public static Goal goal(Bunnidog dog) {
        return new Goal() {
            { setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
            @Override public boolean canUse() { return dog.conversation().viewer()!=null; }
            @Override public boolean canContinueToUse() { return canUse(); }
            @Override public boolean requiresUpdateEveryTick() { return true; }
            @Override public void tick() {
                var player=dog.conversation().viewer();
                if (player!=null) { dog.conversation().hold(); dog.getLookControl().setLookAt(player,30,30); }
            }
        };
    }
}
