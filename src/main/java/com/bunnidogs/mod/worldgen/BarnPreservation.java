package com.bunnidogs.mod.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Never overwrite existing block entities, fluids or protected hard blocks, including a replayed supply chest. */
public final class BarnPreservation implements StructureProcessor {
    private static final BarnPreservation INSTANCE = new BarnPreservation();
    public static final MapCodec<BarnPreservation> CODEC = MapCodec.unit(INSTANCE);
    private static final TagKey<Block> KEEP = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath("bunnidogs","barn_preserve"));
    private BarnPreservation() {}
    @Override public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos position, BlockPos reference,
            StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo placed,
            StructurePlaceSettings settings, StructureTemplate template) {
        var pos = placed.pos();
        if (!level.hasChunkAt(pos)) return null;
        var existing = level.getBlockState(pos);
        return existing.hasBlockEntity() || !existing.getFluidState().isEmpty() || existing.is(KEEP) ? null : placed;
    }
    @Override public MapCodec<BarnPreservation> codec() { return CODEC; }
}
