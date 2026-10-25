package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.village.poi.PoiManager;

/** Namespaced ecology and loaded-only outpost protection. No custom spawn loop or chunk tickets. */
public final class GreatWarren {
    public static final ResourceKey<Level> LEVEL=ResourceKey.create(Registries.DIMENSION,Identifier.fromNamespaceAndPath("bunnidogs","great_warren"));
    public static boolean isRealm(Level level) {return level.dimension().equals(LEVEL);}
    public static boolean warded(ServerLevel level,BlockPos pos) {
        return level.getPoiManager().findAll(type->type.is(ModPoi.WARREN_WARD.getKey()),
                at->level.hasChunkAt(at) && at.distSqr(pos)<=24*24 &&
                        (level.getBlockState(at).is(ModBlocks.WARREN_WARD.get()) || level.getBlockState(at).is(ModBlocks.WARREN_ANCHOR.get()) && level.getBlockState(at).getValue(WarrenAnchorBlock.ACTIVE)),
                pos,24,PoiManager.Occupancy.ANY).findAny().isPresent();
    }
    public static void spawn(net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck event) {
        var reason=event.getSpawnType();
        if((reason==net.minecraft.world.entity.EntitySpawnReason.NATURAL || reason==net.minecraft.world.entity.EntitySpawnReason.CHUNK_GENERATION)
                && event.getEntity().getType().getCategory()==net.minecraft.world.entity.MobCategory.MONSTER && isRealm(event.getLevel().getLevel())
                && warded(event.getLevel().getLevel(),event.getEntity().blockPosition()))event.setResult(net.neoforged.neoforge.event.entity.living.MobSpawnEvent.PositionCheck.Result.FAIL);
    }
    private GreatWarren() {}
}
