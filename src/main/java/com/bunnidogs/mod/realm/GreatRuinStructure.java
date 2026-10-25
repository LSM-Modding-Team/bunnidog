package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Rich, open ruins are regional exploration landmarks, separate from the future Crown dungeon. */
public final class GreatRuinStructure extends Structure {
    public static final MapCodec<GreatRuinStructure> CODEC=simpleCodec(GreatRuinStructure::new);
    public GreatRuinStructure(StructureSettings settings) {super(settings);}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x=context.chunkPos().getMinBlockX()+8,z=context.chunkPos().getMinBlockZ()+8;
        int min=320,max=-64;
        for(int dx:new int[]{-10,0,10})for(int dz:new int[]{-10,0,10}) {
            int y=context.chunkGenerator().getFirstFreeHeight(x+dx,z+dz,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
            min=Math.min(min,y);max=Math.max(max,y);
        }
        if(min<43 || max-min>9 || max>context.heightAccessor().getMaxY()-16)return Optional.empty();
        int y=max;long seed=context.random().nextLong();
        return Optional.of(new GenerationStub(new BlockPos(x,y,z),builder->builder.addPiece(new GreatRuinPiece(x,y,z,seed))));
    }
    @Override public StructureType<?> type() {return ModStructures.GREAT_RUIN.get();}
}
