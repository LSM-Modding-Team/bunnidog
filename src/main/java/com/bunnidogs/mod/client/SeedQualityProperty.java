package com.bunnidogs.mod.client;

import com.bunnidogs.mod.item.SeedItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/** Reads all existing seeds directly: no CustomModelData stamp, duplicate quality component or save migration. */
public record SeedQualityProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<SeedQualityProperty> CODEC = MapCodec.unit(new SeedQualityProperty());
    @Override public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        return SeedItem.read(stack).map(value -> (float)value.quality()).orElse(-1F);
    }
    @Override public MapCodec<SeedQualityProperty> type() { return CODEC; }
}
