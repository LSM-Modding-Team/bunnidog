package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.layout.IslandSite;
import com.bunnidogs.mod.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** A small new island rooted in mild ocean terrain, rather than an ocean-wide founder biome. */
public final class IslandRefugeStructure extends Structure {
    public static final MapCodec<IslandRefugeStructure> CODEC = simpleCodec(IslandRefugeStructure::new);
    public IslandRefugeStructure(StructureSettings settings) { super(settings); }
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMinBlockX()+8, z = context.chunkPos().getMinBlockZ()+8;
        var generator = context.chunkGenerator(); int sea = IslandSite.SEA_LEVEL;
        var bed = IslandSite.bed((dx,dz) -> {
            int sx=x+dx, sz=z+dz;
            var column = generator.getBaseColumn(sx,sz,context.heightAccessor(),context.randomState());
            var biome = generator.getBiomeSource().getNoiseBiome(sx>>2,sea>>2,sz>>2,context.randomState().sampler());
            int floor = generator.getFirstFreeHeight(sx,sz,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
            return new IslandSite.Column(floor,biome.is(BiomeTags.IS_OCEAN),
                    column.getBlock(sea-1).getFluidState().is(FluidTags.WATER) && column.getBlock(sea).isAir());
        });
        if (bed.isEmpty()) return Optional.empty();
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(new BlockPos(x,sea+4,z),
                builder -> builder.addPiece(new IslandRefugePiece(x,sea,z,bed.getAsInt(),seed))));
    }
    @Override public StructureType<?> type() { return ModStructures.ISLAND_REFUGE.get(); }
}
