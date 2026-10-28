package com.bunnidogs.mod.entity;

/** GameTest hard-codes generate_structures=false. Enable the genuine pipeline only while a
 * synchronous realm fixture runs, then restore that exact option. Never used by the mod JAR. */
final class NativeStructureGeneration implements AutoCloseable {
    private final net.minecraft.world.level.levelgen.WorldOptions options;
    private final java.lang.reflect.Field enabled;
    private final boolean original;
    NativeStructureGeneration(net.minecraft.server.MinecraftServer server) throws ReflectiveOperationException {
        options=server.getWorldGenSettings().options();original=options.generateStructures();
        enabled=net.minecraft.world.level.levelgen.WorldOptions.class.getDeclaredField("generateStructures");enabled.setAccessible(true);enabled.setBoolean(options,true);
        if(!options.generateStructures())throw new IllegalStateException("Native structure fixture did not enable generation");
    }
    @Override public void close() throws IllegalAccessException {enabled.setBoolean(options,original);}
}
