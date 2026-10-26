package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.worldgen.WarrenFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The world generation feature types of the mod (Phase 4g). Only the TYPE is code; where and how often it is placed is data
 * (data/bunnidogs/worldgen/configured_feature, placed_feature and neoforge/biome_modifier).
 */
public final class ModFeatures {
    private ModFeatures() {}
    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator>> GENERATORS=DeferredRegister.create(Registries.CHUNK_GENERATOR,BunnidogsMod.MODID);
    public static final DeferredHolder<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator>,com.mojang.serialization.MapCodec<com.bunnidogs.mod.realm.RealmChunkGenerator>> REALM_GENERATOR=GENERATORS.register("realm_islands",()->com.bunnidogs.mod.realm.RealmChunkGenerator.CODEC);

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, BunnidogsMod.MODID);

    public static final DeferredHolder<Feature<?>,com.bunnidogs.mod.realm.RealmLandmarkFeature> REALM_LANDMARKS=
            FEATURES.register("realm_landmarks",()->new com.bunnidogs.mod.realm.RealmLandmarkFeature());

    public static final DeferredHolder<Feature<?>,com.bunnidogs.mod.worldgen.WildFamilyFeature> WILD_FAMILY=FEATURES.register("wild_family",com.bunnidogs.mod.worldgen.WildFamilyFeature::new);

    /** A warren: a guava tree, burrows and wild bunnidogs. No configuration. This kind searches down for a floor (caves, the nether). */
    public static final DeferredHolder<Feature<?>, WarrenFeature> WARREN =
            FEATURES.register("warren", () -> new WarrenFeature(NoneFeatureConfiguration.CODEC, false));

    /** The same warren for the terrain (surface, end islands): it builds exactly where it is told or not at all, never in a cave below. */
    public static final DeferredHolder<Feature<?>, WarrenFeature> SURFACE_WARREN =
            FEATURES.register("surface_warren", () -> new WarrenFeature(NoneFeatureConfiguration.CODEC, true));
}
