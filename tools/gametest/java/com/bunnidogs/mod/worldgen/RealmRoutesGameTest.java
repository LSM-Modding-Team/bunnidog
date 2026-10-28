package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.realm.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Walk the actual planner's complete long routes, not just an isolated bridge sample. */
final class RealmRoutesGameTest {
    static BlockPos run(GameTestHelper h,ServerLevel realm,RealmChunkGenerator generator) {
        BlockPos bridgeSample=null;
        for(long seed:new long[]{realm.getSeed(),realm.getSeed()^819271,realm.getSeed()^-59213,realm.getSeed()^0x7ff445eee12L}) {
            var terrain=new RealmTerrain(seed);var cold=new RealmTerrain(seed);
            var links=terrain.crownLinks(-1,-1);var reached=new HashSet<RealmTerrain.Site>();
            reached.add(terrain.site(-2,-2));boolean changed;
            do {changed=false;for(var link:links)if(reached.contains(link.a()) || reached.contains(link.b())) {changed|=reached.add(link.a());changed|=reached.add(link.b());}}while(changed);
            for(int x=-2;x<=1;x++)for(int z=-2;z<=1;z++)h.assertTrue(reached.contains(terrain.site(x,z)),"Overlapping local trees connect every Crown in the 4x4 neighborhood, seed "+seed);
            var longest=Comparator.comparingDouble((RealmTerrain.Edge e)->Math.hypot(e.a().x()-e.b().x(),e.a().z()-e.b().z()));
            var crossing=links.stream().filter(e->(e.a().x()<0)!=(e.b().x()<0) || (e.a().z()<0)!=(e.b().z()<0)).max(longest).orElseThrow();
            var negative=links.stream().filter(e->e.a().x()<0 && e.b().x()<0 && e.a().z()<0 && e.b().z()<0).max(longest).orElseThrow();
            var nativeSeams=new LinkedHashMap<ChunkPos,BlockPos>();
            for(var link:List.of(crossing,negative)) {
                var path=terrain.islandPath(link);
                h.assertTrue(path.size()>=5 && path.getFirst().equals(link.a()) && path.getLast().equals(link.b()),"A long Crown connection reaches both real endpoints through multiple intermediate islands");
                Integer previous=null;BlockPos previousPos=null;
                for(int i=1;i<path.size();i++) {
                    var a=path.get(i-1);var b=path.get(i);int steps=Math.max(Math.abs(a.x()-b.x()),Math.abs(a.z()-b.z()));
                    h.assertTrue(steps<=RealmTerrain.GRID*2,"A route uses local island hops rather than a direct long causeway");
                    for(int j=0;j<=steps;j++) {
                        double t=steps==0 ? 0 : (double)j/steps;int x=(int)Math.round(a.x()+(b.x()-a.x())*t),z=(int)Math.round(a.z()+(b.z()-a.z())*t);
                        var column=terrain.column(x,z);var at=new BlockPos(x,column.top(),z);
                        if(bridgeSample==null && seed==realm.getSeed() && column.bridge() && column.bottom()==column.top()-2)bridgeSample=at;
                        h.assertTrue(column.top()>0,"A complete island route has no missing floor at "+at+", seed "+seed);
                        h.assertTrue(previous==null || Math.abs(column.top()-previous)<=1,"A complete island route remains walkable at "+at+", previous Y="+previous+", seed "+seed);
                        if(j%97==0)h.assertTrue(column.equals(cold.column(x,z)),"Routes are independent of cache warm-up/generation order at "+at);
                        if(previousPos!=null && (Math.floorDiv(previousPos.getX(),RealmTerrain.REGION)!=Math.floorDiv(x,RealmTerrain.REGION) || Math.floorDiv(previousPos.getZ(),RealmTerrain.REGION)!=Math.floorDiv(z,RealmTerrain.REGION)))nativeSeams.putIfAbsent(new ChunkPos(x>>4,z>>4),at);
                        previous=column.top();previousPos=at;
                    }
                }
            }
            if(seed==realm.getSeed()) {
                h.assertTrue(!nativeSeams.isEmpty(),"The full-route audit crosses actual positive/negative Crown-region boundaries");
                for(var entry:nativeSeams.entrySet()) {
                    var chunk=new ProtoChunk(entry.getKey(),UpgradeData.EMPTY,realm,PalettedContainerFactory.create(realm.registryAccess()),null);
                    generator.fillFromNoise(Blender.empty(),realm.getChunkSource().randomState(),realm.structureManager(),chunk).join();var at=entry.getValue();
                    h.assertTrue(!chunk.getBlockState(at).isAir() && chunk.getBlockState(at.above()).isAir() && chunk.getBlockState(at.above(2)).isAir(),"An actual unfinished native chunk has a usable road and headroom across the Crown-region seam");
                }
            }
        }
        h.assertTrue(bridgeSample!=null,"The complete native route contains real elevated bridge spans over void");
        return bridgeSample;
    }
    private RealmRoutesGameTest() {}
}
