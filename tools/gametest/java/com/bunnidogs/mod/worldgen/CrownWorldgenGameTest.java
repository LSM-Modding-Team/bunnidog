package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.crown.*;
import com.bunnidogs.mod.registry.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.*;
import net.minecraft.util.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class CrownWorldgenGameTest {
    public static void run(GameTestHelper h) {
        var level=h.getLevel();var chunkCenter=new ChunkPos(1040,1040);var chunks=new java.util.HashMap<ChunkPos,ProtoChunk>();
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {var pos=new ChunkPos(chunkCenter.x()+dx,chunkCenter.z()+dz);var c=new ProtoChunk(pos,UpgradeData.EMPTY,level,PalettedContainerFactory.create(level.registryAccess()),null);c.setPersistedStatus(ChunkStatus.CARVERS);chunks.put(pos,c);}
        var cache=StaticCache2D.<GenerationChunkHolder>create(chunkCenter.x(),chunkCenter.z(),1,(x,z)->new GenerationChunkHolder(new ChunkPos(x,z)) {
            @Override public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status) {return chunks.get(new ChunkPos(x,z));}
            @Override protected void addSaveDependency(CompletableFuture<?> sync) {}
            @Override public int getTicketLevel() {return 0;}
            @Override public int getQueueLevel() {return 0;}
        });
        var center=new BlockPos(chunkCenter.getMinBlockX()+8,90,chunkCenter.getMinBlockZ()+8-46);var piece=new CrownWarrenPiece(center.getX(),center.getY(),center.getZ(),981223);
        var threatened=center.offset(0,1,46);var threatenedChunk=chunks.get(ChunkPos.containing(threatened));
        threatenedChunk.setBlockState(threatened.above(13),Blocks.BUDDING_AMETHYST.defaultBlockState(),0);
        threatenedChunk.setBlockState(threatened.above(12),ModBlocks.GUAVA_FRUIT.get().defaultBlockState(),0);
        threatenedChunk.setBlockState(threatened.below(6),Blocks.DIAMOND_ORE.defaultBlockState(),0);
        chunks.get(ChunkPos.containing(threatened.offset(10,15,0))).setBlockState(threatened.offset(10,15,0),ModBlocks.GUAVA_LOG.get().defaultBlockState(),0);
        for(var c:chunks.values())place(level,cache,piece,c,center);
        h.assertTrue(threatenedChunk.getBlockState(threatened.above(12)).isAir() && chunks.get(ChunkPos.containing(threatened.offset(10,15,0))).getBlockState(threatened.offset(10,15,0)).isAir(),"Natural fruit and trees are cleared even outside room floors; buried ore cannot suppress the seal column");
        var definition=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","crown_warren"))).value();
        var start=new net.minecraft.world.level.levelgen.structure.StructureStart(definition,chunkCenter,0,new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(piece)));
        chunks.get(chunkCenter).setStartForStructure(definition,start);for(var c:chunks.values())c.addReferenceForStructure(definition,chunkCenter.pack());
        var reserved=new WorldGenRegion(level,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),chunks.get(chunkCenter));
        h.assertTrue(com.bunnidogs.mod.realm.RealmLandmarkFeature.blockedByDungeon(reserved,threatened.above(9)),"Actual structure references reserve the roof against later vegetation generation");
        var nodePos=center.offset(0,1,46);var node=(CrownSealBlockEntity)chunks.get(ChunkPos.containing(nodePos)).getBlockEntity(nodePos);
        h.assertTrue(node!=null && !node.hasLevel() && node.site()!=null && node.center().equals(center),"The central seal configures and saves safely on a real detached ProtoChunk");
        var copy=(CrownSealBlockEntity)BlockEntity.loadStatic(nodePos,node.getBlockState(),node.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
        h.assertTrue(copy!=null && copy.site().equals(node.site()) && copy.center().equals(center) && !copy.active(),"Native worldgen controller metadata retains its exact site without a live Level");
        var data=piece.createTag(StructurePieceSerializationContext.fromLevel(level));var replay=new CrownWarrenPiece(data);
        var edited=nodePos.east(3);var owner=chunks.get(ChunkPos.containing(edited));owner.setBlockState(edited,Blocks.GOLD_BLOCK.defaultBlockState(),0);
        for(var c:chunks.values())place(level,cache,replay,c,center);
        h.assertTrue(owner.getBlockState(edited).is(Blocks.GOLD_BLOCK),"Saved per-chunk completion never overwrites a later player edit on generation replay");
        // Reachability uses the actual shipped floor/air cells; the protected central gate is deliberately closed.
        var seen=new java.util.HashSet<BlockPos>();var queue=new java.util.ArrayDeque<BlockPos>();queue.add(new BlockPos(0,0,84));
        while(!queue.isEmpty()) {var p=queue.remove();if(!seen.add(p))continue;for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL) {var q=p.relative(d);if(Math.abs(q.getX())>90 || Math.abs(q.getZ())>90 || seen.contains(q))continue;var floor=CrownLayout.cell(q.getX(),0,q.getZ());var air=CrownLayout.cell(q.getX(),2,q.getZ());if(floor!=null && !floor.isAir() && air!=null && air.isAir())queue.add(q);}}
        for(int z=-68;z<42;z++)h.assertTrue(CrownLayout.cell(0,2,z).isAir(),"The northern colored route passes through the cover openings to the gate");
        for(int i=0;i<4;i++) {var p=CrownLayout.wing(i);h.assertTrue(seen.contains(p),"Every physical wing/central seal is reachable from staging without mining: "+i);}
        var snapshot=new com.google.gson.JsonArray();
        for(int x=-90;x<=90;x++)for(int z=-90;z<=90;z++) {
            var floor=CrownLayout.cell(x,0,z);if(floor==null)continue;var row=new com.google.gson.JsonArray();row.add(x);row.add(z);row.add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(floor.getBlock()).toString());
            var wall=CrownLayout.cell(x,2,z);row.add(wall!=null && !wall.isAir());snapshot.add(row);
        }
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("crown_layout_preview.json"),snapshot.toString());}catch(java.io.IOException e) {throw new RuntimeException(e);}
        var repairPos=center.offset(0,1,46);level.getChunk(ChunkPos.containing(repairPos).x(),ChunkPos.containing(repairPos).z());
        level.setBlock(repairPos,ModBlocks.GUAVA_LOG.get().defaultBlockState(),2);var stockPos=repairPos.east(3);level.setBlock(stockPos,Blocks.CHEST.defaultBlockState(),2);
        var stock=(ChestBlockEntity)level.getBlockEntity(stockPos);stock.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,7));
        CrownRepair.repair(level,piece);var restored=(CrownSealBlockEntity)level.getBlockEntity(repairPos);
        h.assertTrue(restored!=null && restored.site().equals(node.site()) && stock.getItem(0).getCount()==7,"Explicit repair restores a missing seal with the original site identity and preserves real container contents");
        var pos=h.absolutePos(new BlockPos(3,2,3));level.setBlock(pos,Blocks.CHEST.defaultBlockState(),3);var chest=(ChestBlockEntity)level.getBlockEntity(pos);
        chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","chests/crown_vault")));chest.setLootTableSeed(1040);chest.unpackLootTable(null);
        int netherite=0,diamonds=0;for(int i=0;i<27;i++) {var s=chest.getItem(i);if(s.is(net.minecraft.world.item.Items.NETHERITE_BLOCK))netherite+=s.getCount();if(s.is(net.minecraft.world.item.Items.DIAMOND_BLOCK))diamonds+=s.getCount();}
        h.assertTrue(netherite>=8 && diamonds>=24,"The actual final vault resolves generous generic wealth, not a token stack");
    }
    public static void natural(GameTestHelper h,ServerLevel realm) {
        var r=realm.registryAccess();var generator=realm.getChunkSource().getGenerator();var random=realm.getChunkSource().randomState();
        var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE_SET,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","crown_warrens"))).value().placement();
        var structure=(CrownWarrenStructure)r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","crown_warren"))).value();
        var state=generator.createState(r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET),random,realm.getSeed());
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++) {
            var chunk=placement.getPotentialStructureChunk(realm.getSeed(),x*64,z*64);if(!placement.isStructureChunk(state,chunk.x(),chunk.z()))continue;
            var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(r,generator,generator.getBiomeSource(),random,realm.getServer().getStructureManager(),realm.getSeed(),chunk,realm,b->true);
            var point=structure.findGenerationPoint(context);if(point.isPresent()) {h.assertTrue(point.get().getPiecesBuilder().build().pieces().size()==1,"Actual shipped realm terrain admits a naturally fitted Crown Warren");return;}
        }
        h.assertTrue(false,"A bounded set of actual Crown random-spread candidates must contain a fitted dungeon");
    }
    private static void place(ServerLevel l,StaticCache2D<GenerationChunkHolder> cache,CrownWarrenPiece p,ProtoChunk c,BlockPos origin) {
        var region=new WorldGenRegion(l,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),c);var at=c.getPos();p.postProcess(region,l.structureManager(),l.getChunkSource().getGenerator(),RandomSource.create(1),new BoundingBox(at.getMinBlockX(),l.getMinY(),at.getMinBlockZ(),at.getMaxBlockX(),l.getMaxY()-1,at.getMaxBlockZ()),at,origin);
    }
}
