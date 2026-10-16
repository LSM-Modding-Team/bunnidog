package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.genetics.Lineage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

/** Called only after a generator places a fresh barrel; opening uses native deferred loot. */
final class WarrenPantries {
    private WarrenPantries() {}
    static ResourceKey<LootTable> table(Lineage style) {
        return ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("bunnidogs","chests/warren_pantry_" + style.key()));
    }
    static void seedFresh(WorldGenLevel world, BlockPos pos, Lineage style) {
        if (!(world.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity barrel)
                || barrel.getLootTable() != null || !barrel.isEmpty()) return;
        barrel.setLootTable(table(style));
        barrel.setLootTableSeed(world.getSeed() ^ pos.asLong());
        barrel.setChanged();
    }
}
