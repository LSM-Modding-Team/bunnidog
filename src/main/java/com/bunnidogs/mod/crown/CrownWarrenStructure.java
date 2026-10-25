package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;

public final class CrownWarrenStructure extends Structure {
    public static final MapCodec<CrownWarrenStructure> CODEC=simpleCodec(CrownWarrenStructure::new);
    public CrownWarrenStructure(StructureSettings s) {super(s);}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ(),high=-64,low=320;
        for(int dx:new int[]{-70,0,70})for(int dz:new int[]{-70,0,70}) {
            int y=c.chunkGenerator().getFirstFreeHeight(x+dx,z+dz,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState());
            high=Math.max(high,y);low=Math.min(low,y);
        }
        if(low<43 || high>c.heightAccessor().getMaxY()-38 || high-low>34)return Optional.empty();
        int y=high;long seed=c.random().nextLong();
        return Optional.of(new GenerationStub(new BlockPos(x,y,z),b->b.addPiece(new CrownWarrenPiece(x,y,z,seed))));
    }
    @Override public StructureType<?> type() {return ModStructures.CROWN_WARREN.get();}
}
