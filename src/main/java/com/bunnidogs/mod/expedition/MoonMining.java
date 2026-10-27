package com.bunnidogs.mod.expedition;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.core.component.DataComponents;

public final class MoonMining {
    public static final TagKey<net.minecraft.world.level.block.Block> STONE=TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("bunnidogs","needs_astralite_tool"));
    public static final TagKey<DamageType> HAZARDS=TagKey.create(Registries.DAMAGE_TYPE,Identifier.fromNamespaceAndPath("bunnidogs","moon_material_hazards"));
    public static Item.Properties material(Item.Properties p) {return p.delayedComponent(DataComponents.DAMAGE_RESISTANT,context->new DamageResistant(context.getOrThrow(HAZARDS)));}
    private MoonMining() {}
}
