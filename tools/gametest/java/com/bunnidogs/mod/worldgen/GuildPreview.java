package com.bunnidogs.mod.worldgen;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
/** Exact native cell export, including orientation properties, for the shared Blender inspection. */
public final class GuildPreview {
    public static void bridge(net.minecraft.server.level.ServerLevel realm,com.bunnidogs.mod.realm.RealmChunkGenerator g,BlockPos center) {
        var chunks=new java.util.HashMap<net.minecraft.world.level.ChunkPos,net.minecraft.world.level.chunk.ProtoChunk>();var cells=new java.util.LinkedHashMap<BlockPos,BlockState>();
        for(int dx=-36;dx<=36;dx++)for(int dz=-36;dz<=36;dz++) {
            int x=center.getX()+dx,z=center.getZ()+dz;var column=g.terrain().column(x,z);if(!column.bridge())continue;
            var cp=new net.minecraft.world.level.ChunkPos(x>>4,z>>4);var chunk=chunks.computeIfAbsent(cp,key->{
                var c=new net.minecraft.world.level.chunk.ProtoChunk(key,net.minecraft.world.level.chunk.UpgradeData.EMPTY,realm,net.minecraft.world.level.chunk.PalettedContainerFactory.create(realm.registryAccess()),null);
                g.fillFromNoise(net.minecraft.world.level.levelgen.blending.Blender.empty(),realm.getChunkSource().randomState(),realm.structureManager(),c).join();return c;
            });
            for(int y=column.roadY()-17;y<=column.roadY()+8;y++) {var state=chunk.getBlockState(new BlockPos(x,y,z));if(!state.isAir())cells.put(new BlockPos(dx,y-center.getY(),dz),state);}
        }
        write(cells);
    }
    public static void write(Map<BlockPos,BlockState> cells) {
        var array=new com.google.gson.JsonArray();
        for(var e:cells.entrySet()) {
            var row=new com.google.gson.JsonObject();var p=e.getKey();var a=new com.google.gson.JsonArray();a.add(p.getX());a.add(p.getY());a.add(p.getZ());row.add("pos",a);
            var state=new com.google.gson.JsonObject();state.addProperty("Name",net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getValue().getBlock()).toString());var props=new com.google.gson.JsonObject();
            for(var property:e.getValue().getProperties())props.addProperty(property.getName(),name(e.getValue(),property));state.add("Properties",props);row.add("state",state);array.add(row);
        }
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("guild_bridge_preview.json"),array.toString());}catch(java.io.IOException ex){throw new RuntimeException(ex);}
    }
    private static <T extends Comparable<T>> String name(BlockState s,net.minecraft.world.level.block.state.properties.Property<T> p){return p.getName(s.getValue(p));}
}
