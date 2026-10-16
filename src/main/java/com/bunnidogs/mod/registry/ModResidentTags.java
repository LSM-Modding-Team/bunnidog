package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModResidentTags {
    private ModResidentTags() {}
    public static final TagKey<EntityType<?>> THREATS = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID,"resident_threats"));
}
