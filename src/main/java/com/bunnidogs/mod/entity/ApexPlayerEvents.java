package com.bunnidogs.mod.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Successful native totems/deaths, rather than item possession, repay heart loss. */
public final class ApexPlayerEvents {
    private ApexPlayerEvents() {}
    private record Totem(Item item,int before) {}
    private static final Map<UUID,Totem> TOTEMS=new HashMap<>();
    public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer p)LifebinderMarks.get(p.level()).sync(p);
    }
    public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.getEntity() instanceof ServerPlayer p)LifebinderMarks.get(p.level()).sync(p);
    }
    public static void totem(LivingUseTotemEvent event) {
        if(event.getEntity() instanceof ServerPlayer p)TOTEMS.putIfAbsent(p.getUUID(),new Totem(event.getTotem().getItem(),p.getStats().getValue(Stats.ITEM_USED.get(event.getTotem().getItem()))));
    }
    public static void death(LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer p) {
            LifebinderMarks.get(p.level()).clear(p);ApexDaze.clear(p);TOTEMS.remove(p.getUUID());
        } else if(event.getEntity() instanceof Apex a && a.kind()==ApexKind.SUPPORT)LifebinderMarks.get((net.minecraft.server.level.ServerLevel)a.level()).casterDied(a);
    }
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var pending=TOTEMS.remove(p.getUUID());
        if(pending!=null) {
            int used=p.getStats().getValue(Stats.ITEM_USED.get(pending.item))-pending.before;
            if(used>0)LifebinderMarks.get(p.level()).restore(p,3*used);
        }
        if(p.tickCount%20==0)LifebinderMarks.get(p.level()).sync(p);
        ApexDaze.tick(p);
    }
}
