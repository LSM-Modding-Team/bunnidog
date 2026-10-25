package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.realm.GreatWarren;
import net.minecraft.server.level.ServerLevel;

/** GameTestServer deliberately discards all datapack dimensions. Mount the shipped LevelStem for this test only. */
final class RealmFixture implements AutoCloseable {
    final ServerLevel realm;
    private final net.minecraft.server.MinecraftServer server;
    private final net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess storage;
    RealmFixture(ServerLevel source) throws Exception {
        server=source.getServer();
        var resource=server.getResourceManager().getResourceOrThrow(net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","dimension/great_warren.json"));
        net.minecraft.world.level.dimension.LevelStem stem;
        try(var reader=resource.openAsReader()) {
            stem=net.minecraft.world.level.dimension.LevelStem.CODEC.parse(server.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE),com.google.gson.JsonParser.parseReader(reader)).getOrThrow();
        }
        var path=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("realm-fixture");
        storage=net.minecraft.world.level.storage.LevelStorageSource.createDefault(path).validateAndCreateAccess("great-warren");
        realm=new ServerLevel(server,net.minecraft.util.Util.backgroundExecutor(),storage,new net.minecraft.world.level.storage.DerivedLevelData(server.getWorldData(),(net.minecraft.world.level.storage.ServerLevelData)source.getLevelData()),GreatWarren.LEVEL,stem,false,net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(source.getSeed()),java.util.List.of(),false);
        server.forgeGetWorldMap().put(GreatWarren.LEVEL,realm);server.markWorldsDirty();
    }
    @Override public void close() throws Exception {
        server.forgeGetWorldMap().remove(GreatWarren.LEVEL);server.markWorldsDirty();realm.close();storage.close();
    }
}
