package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.layout.BarnSite;
import com.bunnidogs.mod.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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

/** Dry-site preflight around ordinary saved native pool pieces; no ticker, restocking or entity injection. */
public final class BreedingBarnStructure extends Structure {
    public static final MapCodec<BreedingBarnStructure> CODEC = simpleCodec(BreedingBarnStructure::new);
    private static final ResourceKey<StructureTemplatePool> START = ResourceKey.create(Registries.TEMPLATE_POOL,
            Identifier.fromNamespaceAndPath("bunnidogs","breeding_barn/start"));
    private static final Identifier ANCHOR = Identifier.fromNamespaceAndPath("bunnidogs","barn_center");
    public BreedingBarnStructure(StructureSettings settings) { super(settings); }
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMinBlockX()+8, z = context.chunkPos().getMinBlockZ()+8;
        var height = BarnSite.height((dx,dz) -> {
            int sx = x+dx, sz = z+dz;
            int y = context.chunkGenerator().getFirstFreeHeight(sx,sz,Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(),context.randomState());
            var floor = context.chunkGenerator().getBaseColumn(sx,sz,context.heightAccessor(),context.randomState()).getBlock(y-1);
            boolean ground = floor.is(BlockTags.DIRT) || floor.is(BlockTags.GRASS_BLOCKS) || floor.is(BlockTags.SAND)
                    || floor.is(BlockTags.BASE_STONE_OVERWORLD) || floor.is(Blocks.SNOW_BLOCK);
            return new BarnSite.Surface(y,ground && floor.getFluidState().isEmpty());
        });
        if (height.isEmpty()) return Optional.empty();
        return JigsawPlacement.addPieces(context,context.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL).getOrThrow(START),
                Optional.of(ANCHOR),1,new BlockPos(x,height.getAsInt(),z),false,Optional.empty(),
                new JigsawStructure.MaxDistance(32),PoolAliasLookup.EMPTY,DimensionPadding.ZERO,LiquidSettings.IGNORE_WATERLOGGING);
    }
    @Override public StructureType<?> type() { return ModStructures.BREEDING_BARN.get(); }
}
