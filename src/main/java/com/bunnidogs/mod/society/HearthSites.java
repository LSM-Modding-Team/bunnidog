package com.bunnidogs.mod.society;

import com.bunnidogs.mod.BunnidogsMod;
import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Loaded-only index and a bounded post-explosion observation. Never obtains a missing chunk. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class HearthSites {
    private record Damage(WeakReference<HearthTreeBlockEntity> home,UUID site,BlockPos at,long after) {}
    private static final Map<ServerLevel,LinkedHashMap<BlockPos,WeakReference<HearthTreeBlockEntity>>> LOADED=new WeakHashMap<>();
    private static final Map<ServerLevel,ArrayDeque<Damage>> DAMAGE=new WeakHashMap<>();
    private HearthSites() {}
    public static void register(HearthTreeBlockEntity home) {
        if(!home.attached() || !(home.getLevel() instanceof ServerLevel level))return;
        var sites=LOADED.computeIfAbsent(level,l->new LinkedHashMap<>());
        if(sites.containsKey(home.getBlockPos())) {if(sites.get(home.getBlockPos()).get()!=home)sites.put(home.getBlockPos(),new WeakReference<>(home));return;}
        if(sites.size()>=4096)sites.entrySet().removeIf(e->e.getValue().get()==null || !e.getValue().get().attached());
        if(sites.size()<4096)sites.put(home.getBlockPos(),new WeakReference<>(home));
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void explosion(ExplosionEvent.Detonate event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;var sites=LOADED.get(level);if(sites==null)return;
        var queue=DAMAGE.computeIfAbsent(level,l->new ArrayDeque<>());
        var affected=new HashSet<>(event.getAffectedBlocks().stream().limit(8192).toList());int visited=0;
        for(var reference:sites.values()) {
            var home=reference.get();if(home==null || !home.attached() || net.minecraft.world.phys.Vec3.atCenterOf(home.getBlockPos()).distanceToSqr(event.getExplosion().center())>Math.pow(event.getExplosion().radius()*2+16,2))continue;if(++visited>32 || queue.size()>=512)break;
            for(var cell:home.construction().cells())if(queue.size()<512 && affected.contains(cell.pos()) && home.construction().owned(cell.pos()))queue.add(new Damage(reference,home.site(),cell.pos(),level.getGameTime()+1));
        }
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;var queue=DAMAGE.get(level);
        if(queue!=null)for(int n=0;n<512 && !queue.isEmpty() && queue.peek().after<=level.getGameTime();n++) {
            var damage=queue.remove();var home=damage.home.get();if(home!=null && home.attached() && damage.site.equals(home.site()))home.construction().explosionDamage(damage.at);
        }
        if(level.getGameTime()%100!=0)return;var sites=LOADED.get(level);if(sites==null)return;
        // Rotate a bounded share: more loaded Hearths do not multiply this tick's world work.
        var keys=sites.keySet().stream().limit(16).toList();
        for(var key:keys) {var ref=sites.remove(key);var home=ref.get();if(home!=null && home.attached()) {home.wild().tick();home.construction().observe();sites.put(key,ref);}}
    }
}
