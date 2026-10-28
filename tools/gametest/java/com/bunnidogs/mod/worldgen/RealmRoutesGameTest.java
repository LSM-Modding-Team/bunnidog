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
        BlockPos bridgeSample=null,tunnelSample=null,elevatedSample=null;RealmTerrain.Edge tunnelSpan=null;
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
                        var column=terrain.column(x,z);var at=new BlockPos(x,column.pathY(),z);
                        if(elevatedSample==null && seed==realm.getSeed() && column.bridge() && column.landTop()>=0 && column.landTop()<column.roadY()-3)elevatedSample=at;
                        if(bridgeSample==null && seed==realm.getSeed() && column.bridge() && column.top()==column.roadY() && column.bottom()==column.roadY()-2)bridgeSample=at;
                        if(tunnelSample==null && seed==realm.getSeed() && column.tunnel() && column.top()>=column.roadY()+RealmTerrain.TUNNEL_HEIGHT+3) {var crown=terrain.nearest(x,z);if(Math.hypot(x-crown.x(),z-crown.z())>=600) {tunnelSample=at;tunnelSpan=new RealmTerrain.Edge(a,b);}}
                        h.assertTrue(column.pathY()>0,"A complete island route has no missing floor at "+at+", seed "+seed);
                        h.assertTrue(previous==null || Math.abs(column.pathY()-previous)<=1,"A complete island route remains walkable at "+at+", previous Y="+previous+", seed "+seed);
                        if(j%97==0)h.assertTrue(column.equals(cold.column(x,z)),"Routes are independent of cache warm-up/generation order at "+at);
                        if(previousPos!=null && (Math.floorDiv(previousPos.getX(),RealmTerrain.REGION)!=Math.floorDiv(x,RealmTerrain.REGION) || Math.floorDiv(previousPos.getZ(),RealmTerrain.REGION)!=Math.floorDiv(z,RealmTerrain.REGION)))nativeSeams.putIfAbsent(new ChunkPos(x>>4,z>>4),at);
                        previous=column.pathY();previousPos=at;
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
        h.assertTrue(tunnelSample!=null,"A real route enters an island with preserved rock above its corridor");
        h.assertTrue(elevatedSample!=null,"A real elevated route crosses lower natural land");
        var elevated=chunk(realm,generator,new ChunkPos(elevatedSample.getX()>>4,elevatedSample.getZ()>>4));
        h.assertTrue(elevated.getBlockState(elevatedSample.below(3)).isAir(),"An elevated deck does not extrude a solid wall down to lower island terrain");
        tunnel(h,realm,generator,tunnelSample,tunnelSpan);
        return bridgeSample;
    }
    private static ProtoChunk chunk(ServerLevel realm,RealmChunkGenerator generator,ChunkPos pos) {
        var chunk=new ProtoChunk(pos,UpgradeData.EMPTY,realm,PalettedContainerFactory.create(realm.registryAccess()),null);
        generator.fillFromNoise(Blender.empty(),realm.getChunkSource().randomState(),realm.structureManager(),chunk).join();return chunk;
    }
    private static void tunnel(GameTestHelper h,ServerLevel realm,RealmChunkGenerator generator,BlockPos at,RealmTerrain.Edge span) {
        var chunks=new HashMap<ChunkPos,ProtoChunk>();var here=new ChunkPos(at.getX()>>4,at.getZ()>>4);var actual=chunk(realm,generator,here);chunks.put(here,actual);
        var column=generator.terrain().column(at.getX(),at.getZ());
        h.assertTrue(!actual.getBlockState(at).isAir(),"The real tunnel retains its route deck");
        for(int y=1;y<=RealmTerrain.TUNNEL_HEIGHT;y++)h.assertTrue(actual.getBlockState(at.above(y)).isAir(),"The real tunnel has five clear blocks above its deck");
        h.assertTrue(!actual.getBlockState(at.above(RealmTerrain.TUNNEL_HEIGHT+1)).isAir() && !actual.getBlockState(at.above(RealmTerrain.TUNNEL_HEIGHT+2)).isAir(),"Natural island rock remains as a solid tunnel roof rather than an open trench");
        h.assertTrue(actual.getBlockState(new BlockPos(at.getX(),column.top(),at.getZ())).is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK),"The original higher island surface survives above the tunnel");
        var random=realm.getChunkSource().randomState();int height=generator.getBaseHeight(at.getX(),at.getZ(),net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,realm,random);
        h.assertTrue(actual.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,at.getX()&15,at.getZ()&15)+1==height && !generator.getBaseColumn(at.getX(),at.getZ(),realm,random).getBlock(height-1).isAir(),"Native height queries describe the island roof, not the buried road");
        // Inspect an actual generated longitudinal section; no synthetic replacement terrain in the preview.
        var snapshot=new com.google.gson.JsonObject();snapshot.addProperty("seed",realm.getSeed());snapshot.addProperty("headroom",RealmTerrain.TUNNEL_HEIGHT);snapshot.addProperty("floor",at.getY());
        var cells=new com.google.gson.JsonArray();var profile=new com.google.gson.JsonArray();
        double dx=span.b().x()-span.a().x(),dz=span.b().z()-span.a().z(),length=Math.hypot(dx,dz);
        for(int step=-96;step<=96;step++) {
            int x=at.getX()+(int)Math.round(dx/length*step),z=at.getZ()+(int)Math.round(dz/length*step);var c=generator.terrain().column(x,z);
            var p=new ChunkPos(x>>4,z>>4);var generated=chunks.computeIfAbsent(p,key->chunk(realm,generator,key));
            var row=new com.google.gson.JsonArray();row.add(step);row.add(c.pathY());row.add(c.top());row.add(c.tunnel());profile.add(row);
            for(int y=Math.max(0,at.getY()-18);y<=Math.min(170,at.getY()+65);y++) {
                var cell=new com.google.gson.JsonArray();cell.add(step);cell.add(y);cell.add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(generated.getBlockState(new BlockPos(x,y,z)).getBlock()).toString());cells.add(cell);
            }
        }
        snapshot.add("cells",cells);snapshot.add("profile",profile);
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("realm_tunnel_preview.json"),snapshot.toString());}catch(java.io.IOException error){throw new RuntimeException(error);}
    }
    private RealmRoutesGameTest() {}
}
