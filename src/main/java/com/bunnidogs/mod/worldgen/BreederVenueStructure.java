package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.layout.VenueSite;
import com.bunnidogs.mod.registry.ModStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

/** Data-selected native venue pieces, fitted before placement; saved pool pieces need no custom format. */
public final class BreederVenueStructure extends Structure {
    public static final MapCodec<BreederVenueStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
            Identifier.CODEC.fieldOf("start_jigsaw_name").forGetter(s -> s.anchor),
            Codec.intRange(4,24).fieldOf("site_radius").forGetter(s -> s.radius)
    ).apply(i,BreederVenueStructure::new));
    private final Holder<StructureTemplatePool> startPool;
    private final Identifier anchor;
    private final int radius;
    public BreederVenueStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool,
            Identifier anchor, int radius) {
        super(settings); this.startPool = startPool; this.anchor = anchor; this.radius = radius;
    }
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMinBlockX()+8, z = context.chunkPos().getMinBlockZ()+8;
        var height = VenueSite.height(radius,(dx,dz) -> {
            int sx = x+dx, sz = z+dz;
            int y = context.chunkGenerator().getFirstFreeHeight(sx,sz,Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(),context.randomState());
            var floor = context.chunkGenerator().getBaseColumn(sx,sz,context.heightAccessor(),context.randomState()).getBlock(y-1);
            boolean ground = floor.is(BlockTags.DIRT) || floor.is(BlockTags.GRASS_BLOCKS) || floor.is(BlockTags.SAND)
                    || floor.is(BlockTags.BASE_STONE_OVERWORLD) || floor.is(Blocks.SNOW_BLOCK);
            return new VenueSite.Surface(y,ground && floor.getFluidState().isEmpty());
        });
        if (height.isEmpty()) return Optional.empty();
        // Native placement subtracts the local y=1 anchor and the single-pool ground delta (1).
        // Supply first-free + 1 so template y=0 replaces the highest supporting ground block.
        return JigsawPlacement.addPieces(context,startPool,Optional.of(anchor),1,
                new BlockPos(x,height.getAsInt()+1,z),false,Optional.empty(),
                new JigsawStructure.MaxDistance(48),PoolAliasLookup.EMPTY,DimensionPadding.ZERO,LiquidSettings.IGNORE_WATERLOGGING);
    }
    @Override public StructureType<?> type() { return ModStructures.BREEDER_VENUE.get(); }
}
