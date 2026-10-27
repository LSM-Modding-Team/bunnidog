package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.realm.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

public final class RealmTerrainGameTest {
    public static void run(GameTestHelper h,ServerLevel realm) {
        h.assertTrue(realm.getChunkSource().getGenerator() instanceof RealmChunkGenerator,"The actual shipped LevelStem uses the island generator");
        var g=(RealmChunkGenerator)realm.getChunkSource().getGenerator();var terrain=g.terrain();var random=realm.getChunkSource().randomState();
        var site=terrain.site(0,0);var tile=terrain.tile(site.x(),site.z());var moon=tile.moons().stream().min(java.util.Comparator.comparingDouble(m->Math.hypot(m.x()-site.x(),m.z()-site.z()))).orElseThrow();
        var land=generate(realm,g,new ChunkPos(site.x()>>4,site.z()>>4));var sky=generate(realm,g,new ChunkPos(moon.x()>>4,moon.z()>>4));
        int ores=0,stone=0;for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=180;y<300;y++) {var s=sky.getBlockState(new BlockPos(sky.getPos().getMinBlockX()+x,y,sky.getPos().getMinBlockZ()+z));if(s.is(ModBlocks.ASTRALITE_ORE.get()) || s.is(ModBlocks.CINDERITE_ORE.get()))ores++;if(s.is(ModBlocks.MOONSTONE.get()))stone++;}
        h.assertTrue(stone>5000 && ores>200,"An actual generated high moon contains a substantial volume of rich unique ores");
        h.assertTrue(land.getBlockState(new BlockPos(site.x(),10,site.z())).isAir() && land.getBlockState(new BlockPos(site.x(),81,site.z())).isAir() && land.getBlockState(new BlockPos(site.x(),80,site.z())).is(Blocks.MOSS_BLOCK),"Actual island has open sky, a stable dungeon plateau, a tapered bottom and void beneath");
        var pos=new BlockPos(moon.x(),moon.y(),moon.z());
        var mobs=g.getMobsAt(realm.getBiome(pos),realm.structureManager(),net.minecraft.world.entity.MobCategory.MONSTER,pos);
        for(var entry:mobs.unwrap())h.assertTrue(entry.value().type()==ModEntities.BLASTMAW.get() || entry.value().type()==ModEntities.apex(com.bunnidogs.mod.entity.ApexKind.BLAST,true),"Native moon spawn selection contains only Blastmaws and flying Blastmaws");
        var actualPlacement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)realm.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE_SET,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","crown_warrens"))).value().placement();
        h.assertTrue(actualPlacement.spacing()==192 && actualPlacement.separation()==96 && actualPlacement.getPotentialStructureChunk(realm.getSeed(),0,0).getMiddleBlockX()==site.x(),"The actual rarer dungeon placement and generated foundation share the same native placement rule");
        boolean bridge=false,voidFound=false;int low=320,high=-64;int natural=0,empty=0;
        for(int x=site.x()-1600;x<=site.x()+1600;x+=64)for(int z=site.z()-1600;z<=site.z()+1600;z+=64) {
            var c=terrain.column(x,z);if(c.top()<0)empty++;else if(Math.hypot(x-site.x(),z-site.z())>400 && !c.bridge()) {natural++;low=Math.min(low,c.top());high=Math.max(high,c.top());}
            if(!bridge && c.bridge() && c.bottom()==c.top()-2) {var chunk=generate(realm,g,new ChunkPos(x>>4,z>>4));h.assertTrue(!chunk.getBlockState(new BlockPos(x,c.top(),z)).isAir() && chunk.getBlockState(new BlockPos(x,c.top()-3,z)).isAir(),"An actual narrow, height-varying causeway has open void below");bridge=true;}
            if(!voidFound && c.top()<0 && c.moonTop()<0) {var chunk=generate(realm,g,new ChunkPos(x>>4,z>>4));h.assertTrue(chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x&15,z&15)==realm.getMinY()-1,"Open gaps are genuine void");voidFound=true;}
        }
        h.assertTrue(bridge && voidFound && natural>100 && empty>400 && high-low>40,"The real seed has narrow bridges, substantial open void and varied non-dungeon mountains/islands: "+low+".."+high+", land="+natural+", void="+empty);
        var arrival=terrain.arrival(site.x()+500,site.z()+500);h.assertTrue(arrival!=null && Math.hypot(arrival.x()-terrain.nearest(arrival.x(),arrival.z()).x(),arrival.z()-terrain.nearest(arrival.x(),arrival.z()).z())>=700,"A fresh expedition lands on ordinary land a real journey away from the nearest dungeon");
        int surface=g.getBaseHeight(moon.x(),moon.z(),Heightmap.Types.WORLD_SURFACE_WG,realm,random);
        h.assertTrue(sky.getHeight(Heightmap.Types.WORLD_SURFACE_WG,moon.x()&15,moon.z()&15)+1==surface && !g.getBaseColumn(moon.x(),moon.z(),realm,random).getBlock(surface-1).isAir(),"Native height queries, generated heightmaps and complete column queries agree");
        var alternate=new RealmTerrain(realm.getSeed()+1);h.assertTrue(!alternate.site(0,0).equals(site),"World seeds change terrain and matching dungeon positions");
        var snapshot=new com.google.gson.JsonObject();snapshot.addProperty("seed",realm.getSeed());snapshot.addProperty("step",24);var cells=new com.google.gson.JsonArray();var crownSites=new com.google.gson.JsonArray();
        for(int rx=-2;rx<=2;rx++)for(int rz=-2;rz<=2;rz++) {var point=terrain.site(rx,rz);var p=new com.google.gson.JsonArray();p.add(point.x()-site.x());p.add(point.z()-site.z());crownSites.add(p);}snapshot.add("crowns",crownSites);
        for(int x=site.x()-3600;x<site.x()+3600;x+=24)for(int z=site.z()-3600;z<site.z()+3600;z+=24) {var c=terrain.column(x,z);var row=new com.google.gson.JsonArray();row.add(x-site.x());row.add(z-site.z());row.add(c.top());row.add(c.moonTop());row.add(c.bridge());cells.add(row);}snapshot.add("cells",cells);
        var spans=new java.util.LinkedHashSet<RealmTerrain.Edge>();for(int x=site.x()-3600;x<site.x()+3600;x+=256)for(int z=site.z()-3600;z<site.z()+3600;z+=256)spans.addAll(terrain.tile(x,z).edges());var routes=new com.google.gson.JsonArray();
        for(var e:spans) {var row=new com.google.gson.JsonArray();row.add(e.a().x()-site.x());row.add(e.a().z()-site.z());row.add(e.b().x()-site.x());row.add(e.b().z()-site.z());routes.add(row);}snapshot.add("routes",routes);
        var close=new com.google.gson.JsonArray();for(int x=-400;x<400;x+=5)for(int z=-400;z<400;z+=5) {var c=terrain.column(site.x()+x,site.z()+z);var row=new com.google.gson.JsonArray();row.add(x);row.add(z);row.add(c.top());row.add(c.bridge());close.add(row);}snapshot.add("detail",close);
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("realm_islands_preview.json"),snapshot.toString());}catch(java.io.IOException e) {throw new RuntimeException(e);}
    }
    private static ProtoChunk generate(ServerLevel realm,RealmChunkGenerator g,ChunkPos pos) {
        var c=new ProtoChunk(pos,UpgradeData.EMPTY,realm,PalettedContainerFactory.create(realm.registryAccess()),null);g.fillFromNoise(Blender.empty(),realm.getChunkSource().randomState(),realm.structureManager(),c).join();return c;
    }
}
