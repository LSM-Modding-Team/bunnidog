package com.bunnidogs.mod.guild;
import com.bunnidogs.mod.realm.*;
import com.bunnidogs.mod.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.*;
/** Fit the whole inhabited volume into rock, without requiring an exact island-center lottery. */
public final class LanternWarrenStructure extends Structure {
    public static final MapCodec<LanternWarrenStructure> CODEC=simpleCodec(LanternWarrenStructure::new);
    public LanternWarrenStructure(StructureSettings s) {super(s);}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        if(!(c.chunkGenerator() instanceof RealmChunkGenerator g))return Optional.empty();
        int x=c.chunkPos().getMiddleBlockX(),z=c.chunkPos().getMiddleBlockZ();var origins=new LinkedHashSet<RealmTerrain.Site>();
        for(var island:g.terrain().tile(x,z).islands().stream().filter(i->!i.crown() && i.rx()>=90 && i.rz()>=80).sorted(Comparator.comparingDouble(i->Math.hypot(i.at().x()-x,i.at().z()-z))).toList()) {
            if(Math.max(Math.abs(island.at().x()-x),Math.abs(island.at().z()-z))>112)continue;
            origins.add(new RealmTerrain.Site(x,z));origins.add(new RealmTerrain.Site(Math.clamp(island.at().x(),x-48,x+48),Math.clamp(island.at().z(),z-48,z+48)));
            for(int dx:new int[]{0,-24,24})for(int dz:new int[]{0,-24,24})origins.add(new RealmTerrain.Site(island.at().x()+dx,island.at().z()+dz));
        }
        for(var at:origins) {
            int cx=at.x(),cz=at.z();
            // Native structure references search eight chunks: the ENTIRE town must remain in that reach.
            if(Math.max(Math.abs(cx-x),Math.abs(cz-z))>64)continue;
            int floor=g.terrain().column(cx,cz-45).landTop()-32;boolean valid=floor>=12;
            for(int dx:new int[]{-44,0,44})for(int dz:new int[]{-16,12,44}) {
                var column=g.terrain().column(cx+dx,cz+dz);
                if(column.landTop()<floor+22 || column.landBottom()>floor-6 || column.bridge())valid=false;
            }
            if(!valid)continue;var center=new BlockPos(cx,floor,cz);
            return Optional.of(new GenerationStub(center,b->b.addPiece(new LanternWarrenPiece(center))));
        }
        return Optional.empty();
    }
    @Override public StructureType<?> type(){return ModStructures.LANTERN_WARREN.get();}
}
