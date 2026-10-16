package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.Apex;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All entity types for the mod. Add new mobs here. */
public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BunnidogsMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Bunnidog>> BUNNIDOG =
            ENTITIES.register("bunnidog", () -> EntityType.Builder
                    .of(Bunnidog::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.9F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "bunnidog"))));

    public static final DeferredHolder<EntityType<?>, EntityType<Apex>> APEX =
            ENTITIES.register("apex", () -> EntityType.Builder.of(Apex::new, MobCategory.MONSTER)
                    .sized(.6F, .9F).clientTrackingRange(12).notInPeaceful().noLootTable()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "apex"))));

    /** Native boats keep vehicle physics, passenger rules, inventory saves and drops. Suppliers avoid item-registry cycles. */
    public static final DeferredHolder<EntityType<?>, EntityType<Boat>> GUAVA_BOAT =
            ENTITIES.register("guava_boat", () -> EntityType.Builder.<Boat>of(
                    (type, level) -> new Boat(type, level, () -> ModItems.GUAVA_BOAT.get()), MobCategory.MISC)
                    .noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "guava_boat"))));
    public static final DeferredHolder<EntityType<?>, EntityType<ChestBoat>> GUAVA_CHEST_BOAT =
            ENTITIES.register("guava_chest_boat", () -> EntityType.Builder.<ChestBoat>of(
                    (type, level) -> new ChestBoat(type, level, () -> ModItems.GUAVA_CHEST_BOAT.get()), MobCategory.MISC)
                    .noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "guava_chest_boat"))));

    /** Registered on the mod event bus from {@link BunnidogsMod}. */
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BUNNIDOG.get(), Bunnidog.createAttributes().build());
        event.put(APEX.get(), Apex.createAttributes().build());
    }

    /**
     * Where bunnidogs may appear naturally (Phase 3): on solid ground at the top of the world, with the extra rule in
     * {@link ModBiomeTags#canSpawnNaturally}. Since Phase 4g nothing spawns them naturally: they live in warrens
     * ({@link com.bunnidogs.mod.worldgen.WarrenFeature}); this rule only matters if a data pack adds a spawner. Registered on the mod event bus from {@link BunnidogsMod}.
     */
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(BUNNIDOG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> ModBiomeTags.canSpawnNaturally(level, pos),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
