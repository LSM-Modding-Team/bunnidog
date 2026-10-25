package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.realm.GreatRuinPiece;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** A real unfinished ProtoChunk must be able to create, fill and save its detached archive chest. */
public final class RealmRuinGameTest {
    public static void run(GameTestHelper h) {
        var level=h.getLevel();var center=new ChunkPos(900,900);
        var chunks=new java.util.HashMap<ChunkPos,ProtoChunk>();
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {
            var at=new ChunkPos(center.x()+dx,center.z()+dz);var chunk=new ProtoChunk(at,UpgradeData.EMPTY,level,PalettedContainerFactory.create(level.registryAccess()),null);
            chunk.setPersistedStatus(ChunkStatus.CARVERS);chunks.put(at,chunk);
        }
        var cache=StaticCache2D.<GenerationChunkHolder>create(center.x(),center.z(),1,(x,z)->new GenerationChunkHolder(new ChunkPos(x,z)) {
            @Override public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status) {return chunks.get(new ChunkPos(x,z));}
            @Override protected void addSaveDependency(CompletableFuture<?> sync) {}
            @Override public int getTicketLevel() {return 0;}
            @Override public int getQueueLevel() {return 0;}
        });
        var origin=new BlockPos(center.getMinBlockX()+8,80,center.getMinBlockZ()+8);var piece=new GreatRuinPiece(origin.getX(),origin.getY(),origin.getZ(),57120L);
        for(var chunk:chunks.values()) {
            var region=new WorldGenRegion(level,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),chunk);
            var at=chunk.getPos();var box=new BoundingBox(at.getMinBlockX(),level.getMinY(),at.getMinBlockZ(),at.getMaxBlockX(),level.getMaxY()-1,at.getMaxBlockZ());
            piece.postProcess(region,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(12),box,at,origin);
        }
        var preview=new com.google.gson.JsonArray();
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)for(int y=0;y<=12;y++) {
            var state=GreatRuinPiece.cell(x,y,z,57120L);if(state==null || state.isAir())continue;
            var cell=new com.google.gson.JsonArray();cell.add(x);cell.add(y);cell.add(z);cell.add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());preview.add(cell);
        }
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("realm_ruin_preview.json"),preview.toString());}
        catch(java.io.IOException error) {throw new RuntimeException(error);}
        var archivePos=origin.offset(-5,1,5);var treasurePos=origin.offset(5,1,5);
        var archive=(ChestBlockEntity)chunks.get(ChunkPos.containing(archivePos)).getBlockEntity(archivePos);
        var treasure=(ChestBlockEntity)chunks.get(ChunkPos.containing(treasurePos)).getBlockEntity(treasurePos);
        h.assertTrue(archive!=null && !archive.hasLevel() && treasure!=null && !treasure.hasLevel(),"Both ruin chests are created on actual detached generation-stage chunks");
        var seed=SeedItem.read(archive.getItem(11)).orElseThrow();var sample=TissueSampleItem.read(archive.getItem(13)).orElseThrow();
        h.assertTrue(seed.quality()>=20 && seed.quality()<=33 && sample.genome().equals(seed.mother()) && sample.stats().equals(seed.motherStats()),"Genetic archives freeze a real high-quality seed and matching reusable tissue");
        var saved=archive.saveWithFullMetadata(level.registryAccess());var copy=(ChestBlockEntity)BlockEntity.loadStatic(archivePos,archive.getBlockState(),saved,level.registryAccess());
        h.assertTrue(copy!=null && ItemStack.matches(copy.getItem(11),archive.getItem(11)) && ItemStack.matches(copy.getItem(13),archive.getItem(13)),"Native chest saves preserve exact frozen genetic components");
        h.assertTrue(treasure.getLootTable()!=null && treasure.getLootTable().identifier().getPath().equals("chests/great_ruin"),"Wealth uses the realm's actual native loot table");
        var tag=piece.createTag(StructurePieceSerializationContext.fromLevel(level));var replay=new GreatRuinPiece(tag);archive.setItem(11,ItemStack.EMPTY);
        for(var chunk:chunks.values()) {
            var region=new WorldGenRegion(level,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),chunk);var at=chunk.getPos();
            replay.postProcess(region,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(12),new BoundingBox(at.getMinBlockX(),level.getMinY(),at.getMinBlockZ(),at.getMaxBlockX(),level.getMaxY()-1,at.getMaxBlockZ()),at,origin);
        }
        h.assertTrue(archive.getItem(11).isEmpty(),"Saved chunk ownership prevents archive refills or rerolls on generation replay");
        // Exercise all three region features in the actual detached generation stage as well.
        String[] biomes={"crownroot_groves","glassbloom_flats","thunder_hollows"};
        for(int n=0;n<biomes.length;n++) {
            var biome=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs",biomes[n])));
            for(var chunk:chunks.values())chunk.fillBiomesFromNoise((x,y,z,sampler)->biome,level.getChunkSource().randomState().sampler());
            var region=new WorldGenRegion(level,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),chunks.get(center));var at=origin.above(40+n*15);
            for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)region.setBlock(at.offset(x,-1,z),(n==0 ? Blocks.MOSS_BLOCK : n==1 ? Blocks.CALCITE : Blocks.POLISHED_BASALT).defaultBlockState(),2);
            h.assertTrue(new com.bunnidogs.mod.realm.RealmLandmarkFeature().place(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE,region,level.getChunkSource().getGenerator(),RandomSource.create(0),at),"A real detached generation region must build its "+biomes[n]+" landmark");
            if(n==0) {
                int fruit=0,lamps=0;
                for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)for(int y=0;y<=30;y++) {
                    var cell=at.offset(x,y,z);var block=region.getBlockState(cell);
                    if(block.is(com.bunnidogs.mod.registry.ModBlocks.GUAVA_FRUIT.get())) {fruit++;h.assertTrue(region.getBlockState(cell.above()).is(com.bunnidogs.mod.registry.ModBlocks.GUAVA_LEAVES.get()),"Crownroot fruit hangs from actual leaves");}
                    if(block.is(Blocks.SHROOMLIGHT)) {lamps++;h.assertTrue(region.getBlockState(cell.above()).is(com.bunnidogs.mod.registry.ModBlocks.GUAVA_LEAVES.get()),"Crownroot lights attach to the canopy rather than float outside it");}
                }
                h.assertTrue(fruit>0 && lamps>0,"A giant Crownroot canopy supplies real ripe fruit and attached light");
            }
        }
        // Resolve the shipped wealth table on a live native chest, rather than assuming its JSON budget.
        var livePos=h.absolutePos(new BlockPos(2,2,2));level.setBlock(livePos,Blocks.CHEST.defaultBlockState(),3);
        var live=(ChestBlockEntity)level.getBlockEntity(livePos);live.setLootTable(treasure.getLootTable());live.setLootTableSeed(321);live.unpackLootTable(null);
        int diamonds=0,netherite=0,apples=0;
        for(int i=0;i<live.getContainerSize();i++) {var item=live.getItem(i);if(item.is(Items.DIAMOND))diamonds+=item.getCount();if(item.is(Items.NETHERITE_INGOT))netherite+=item.getCount();if(item.is(Items.ENCHANTED_GOLDEN_APPLE))apples+=item.getCount();}
        h.assertTrue(diamonds>=16 && netherite>=2 && apples>=2,"A real generated rich cache includes diamonds, netherite and enchanted apples together");
    }
}
