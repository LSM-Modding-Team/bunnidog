package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.*;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Real void terrain, with matching native column/height queries for structures and spawning. */
public final class RealmChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<RealmChunkGenerator> CODEC=RecordCodecBuilder.mapCodec(i->i.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(RealmChunkGenerator::getBiomeSource),NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(RealmChunkGenerator::generatorSettings)).apply(i,RealmChunkGenerator::new));
    private volatile RealmTerrain terrain=new RealmTerrain(0);
    public RealmChunkGenerator(BiomeSource source,Holder<NoiseGeneratorSettings> settings) {super(source,settings);}
    public RealmTerrain terrain() {return terrain;}
    @Override protected MapCodec<? extends ChunkGenerator> codec() {return CODEC;}
    @Override public ChunkGeneratorStructureState createState(HolderLookup<net.minecraft.world.level.levelgen.structure.StructureSet> sets,RandomState random,long seed) {terrain=new RealmTerrain(seed);return super.createState(sets,random,seed);}
    public BlockState state(int x,int y,int z,RealmTerrain.Column c) {
        boolean moon=y>=c.moonBottom() && y<=c.moonTop();boolean land=y>=c.bottom() && y<=c.top();
        if(!moon && !land)return Blocks.AIR.defaultBlockState();
        if(!moon && c.bridge() && y>=78)return (y==80 && Math.floorMod(x+z,16)==0 ? Blocks.SEA_LANTERN : Blocks.END_STONE_BRICKS).defaultBlockState();
        long chance=Math.floorMod(terrain.hash(x,y,z),10000);
        if(chance<(moon ? 240 : 16))return ModBlocks.ASTRALITE_ORE.get().defaultBlockState();
        if(chance<(moon ? 560 : 40))return ModBlocks.CINDERITE_ORE.get().defaultBlockState();
        if(moon)return ModBlocks.MOONSTONE.get().defaultBlockState();
        if(y==c.top())return Blocks.MOSS_BLOCK.defaultBlockState();
        return Blocks.END_STONE.defaultBlockState();
    }
    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender,RandomState random,StructureManager manager,ChunkAccess chunk) {
        var pos=new BlockPos.MutableBlockPos();var cp=chunk.getPos();
        for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++) {
            int x=cp.getMinBlockX()+dx,z=cp.getMinBlockZ()+dz;var c=terrain.column(x,z);
            for(int y=Math.max(chunk.getMinY(),Math.min(c.bottom(),c.moonBottom()));y<=Math.min(chunk.getMaxY()-1,Math.max(c.top(),c.moonTop()));y++) {
                var state=state(x,y,z,c);if(state.isAir())continue;pos.set(x,y,z);chunk.setBlockState(pos,state,0);
                for(var type:Heightmap.Types.values())if(type.keepAfterWorldgen() || type==Heightmap.Types.OCEAN_FLOOR_WG || type==Heightmap.Types.WORLD_SURFACE_WG)chunk.getOrCreateHeightmapUnprimed(type).update(dx,y,dz,state);
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }
    @Override public int getBaseHeight(int x,int z,Heightmap.Types type,LevelHeightAccessor level,RandomState random) {var c=terrain.column(x,z);return Math.max(level.getMinY(),Math.max(c.top(),c.moonTop())+1);}
    @Override public NoiseColumn getBaseColumn(int x,int z,LevelHeightAccessor level,RandomState random) {var c=terrain.column(x,z);var states=new BlockState[level.getHeight()];for(int i=0;i<states.length;i++)states[i]=state(x,i+level.getMinY(),z,c);return new NoiseColumn(level.getMinY(),states);}
    @Override public void buildSurface(WorldGenRegion region,StructureManager manager,RandomState random,ChunkAccess chunk) {}
    @Override public void applyCarvers(WorldGenRegion region,long seed,RandomState random,BiomeManager biomes,StructureManager manager,ChunkAccess chunk) {}
    @Override public void spawnOriginalMobs(WorldGenRegion region) {} // Native natural spawning uses the actual loaded surfaces, including the moons.
    @Override public net.minecraft.util.random.WeightedList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> biome,StructureManager manager,net.minecraft.world.entity.MobCategory category,BlockPos pos) {
        if(pos.getY()>170 && category==net.minecraft.world.entity.MobCategory.MONSTER)return net.minecraft.util.random.WeightedList.<MobSpawnSettings.SpawnerData>builder().add(new MobSpawnSettings.SpawnerData(com.bunnidogs.mod.registry.ModEntities.BLASTMAW.get(),1,2),3).add(new MobSpawnSettings.SpawnerData(com.bunnidogs.mod.registry.ModEntities.apex(com.bunnidogs.mod.entity.ApexKind.BLAST,true),1,2),2).build();
        return super.getMobsAt(biome,manager,category,pos);
    }
}
