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
        boolean bridge=false,voidFound=false;
        for(var e:tile.edges())for(int n=2;n<9;n++) {int x=e.a().x()+(e.b().x()-e.a().x())*n/10,z=e.a().z()+(e.b().z()-e.a().z())*n/10;var c=terrain.column(x,z);if(!c.bridge() || c.bottom()!=78)continue;
            var chunk=generate(realm,g,new ChunkPos(x>>4,z>>4));h.assertTrue(!chunk.getBlockState(new BlockPos(x,80,z)).isAir() && chunk.getBlockState(new BlockPos(x,77,z)).isAir(),"A real causeway crosses void at walkable island height");bridge=true;break;}
        for(int x=0;x<1024 && !voidFound;x+=32)for(int z=0;z<1024;z+=32) {var c=terrain.column(x,z);if(c.top()>=0 || c.moonTop()>=0)continue;var chunk=generate(realm,g,new ChunkPos(x>>4,z>>4));h.assertTrue(chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x&15,z&15)==realm.getMinY()-1,"Empty generated columns are genuine void, not underground Overworld terrain");voidFound=true;break;}
        h.assertTrue(bridge && voidFound,"The actual seed contains both island connections and open void");
        int surface=g.getBaseHeight(moon.x(),moon.z(),Heightmap.Types.WORLD_SURFACE_WG,realm,random);
        h.assertTrue(sky.getHeight(Heightmap.Types.WORLD_SURFACE_WG,moon.x()&15,moon.z()&15)+1==surface && !g.getBaseColumn(moon.x(),moon.z(),realm,random).getBlock(surface-1).isAir(),"Native height queries, generated heightmaps and complete column queries agree");
        var alternate=new RealmTerrain(realm.getSeed()+1);h.assertTrue(!alternate.site(0,0).equals(site),"World seeds change terrain and matching dungeon positions");
        var snapshot=new com.google.gson.JsonObject();snapshot.addProperty("seed",realm.getSeed());var cells=new com.google.gson.JsonArray();
        for(int x=site.x()-1000;x<site.x()+1600;x+=12)for(int z=site.z()-1000;z<site.z()+1600;z+=12) {var c=terrain.column(x,z);var row=new com.google.gson.JsonArray();row.add(x-site.x());row.add(z-site.z());row.add(c.top());row.add(c.moonTop());row.add(c.bridge());cells.add(row);}snapshot.add("cells",cells);
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("realm_islands_preview.json"),snapshot.toString());}catch(java.io.IOException e) {throw new RuntimeException(e);}
    }
    private static ProtoChunk generate(ServerLevel realm,RealmChunkGenerator g,ChunkPos pos) {
        var c=new ProtoChunk(pos,UpgradeData.EMPTY,realm,PalettedContainerFactory.create(realm.registryAccess()),null);g.fillFromNoise(Blender.empty(),realm.getChunkSource().randomState(),realm.structureManager(),c).join();return c;
    }
}
