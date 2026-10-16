package com.bunnidogs.mod.registry;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Diet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Additional diet foods are data, separate from the existing guava breeding-food tag. */
public final class ModDietTags {
    private ModDietTags() {}
    private static final Map<String, TagKey<Item>> FOODS = java.util.Arrays.stream(Diet.values())
            .flatMap(d -> d.tags().stream()).distinct().collect(Collectors.toUnmodifiableMap(Function.identity(),
                    name -> TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "diet/" + name))));
    public static final TagKey<EntityType<?>> PREY = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "prey"));
    public static boolean accepts(Diet diet, ItemStack stack) {
        return diet.tags().stream().anyMatch(name -> stack.is(FOODS.get(name)));
    }
}
