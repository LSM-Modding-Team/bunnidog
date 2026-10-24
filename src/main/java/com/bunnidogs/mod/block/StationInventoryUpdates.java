package com.bunnidogs.mod.block;

import java.util.LinkedHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Publish station changes after a complete vanilla inventory gesture owns its cursor/remainders. */
public final class StationInventoryUpdates implements AutoCloseable {
    private static final ThreadLocal<StationInventoryUpdates> CURRENT=new ThreadLocal<>();
    private final StationInventoryUpdates parent;
    private final LinkedHashMap<BlockEntity,Runnable> pending=new LinkedHashMap<>();
    private boolean closed;
    private StationInventoryUpdates() { parent=CURRENT.get(); CURRENT.set(this); }
    public static StationInventoryUpdates begin() { return new StationInventoryUpdates(); }
    /** Authoritative stock changes immediately; only its dirty/visual/neighbor notification is deferred. */
    public static boolean defer(BlockEntity station,Runnable notification) {
        var batch=CURRENT.get();
        if (batch==null) return false;
        batch.pending.putIfAbsent(station,notification); return true;
    }
    @Override public void close() {
        if (closed) return;
        if (CURRENT.get()!=this) throw new IllegalStateException("Station update scopes closed out of order");
        closed=true;
        if (parent!=null) { CURRENT.set(parent); pending.forEach(parent.pending::putIfAbsent); }
        else { CURRENT.remove(); pending.values().forEach(Runnable::run); }
    }
}
