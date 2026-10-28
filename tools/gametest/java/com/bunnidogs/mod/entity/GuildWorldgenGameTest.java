package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.guild.*;
import com.bunnidogs.mod.realm.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.*;
import net.minecraft.util.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

final class GuildWorldgenGameTest {
    static void run(GameTestHelper h) throws Exception {
        try(var structures=new NativeStructureGeneration(h.getLevel().getServer());var fixture=new RealmFixture(h.getLevel(),"lantern-natural-v3")) {
            var realm=fixture.realm;var g=(RealmChunkGenerator)realm.getChunkSource().getGenerator();var r=realm.registryAccess();var random=realm.getChunkSource().randomState();g.createState(r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET),random,realm.getSeed());
            var key=net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","lantern_warren");var structure=(LanternWarrenStructure)r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,key)).value();
            var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE_SET,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","lantern_warrens"))).value().placement();
            LanternWarrenPiece piece=null;
            outer:for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++) {
                var cp=placement.getPotentialStructureChunk(realm.getSeed(),x*placement.spacing(),z*placement.spacing());var context=new Structure.GenerationContext(r,g,g.getBiomeSource(),random,realm.getServer().getStructureManager(),realm.getSeed(),cp,realm,b->true);var stub=structure.findGenerationPoint(context);
                if(stub.isPresent()) {piece=(LanternWarrenPiece)stub.get().getPiecesBuilder().build().pieces().getFirst();break outer;}
            }
            h.assertTrue(piece!=null,"Actual native island terrain admits a buried Lantern Warren at its shipped rarity");
            var box=piece.getBoundingBox();var center=new BlockPos(box.minX()+48,box.minY()+3,box.minZ()+64);var mid=ChunkPos.containing(center);var chunks=new HashMap<ChunkPos,ProtoChunk>();
            for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++) {var cp=new ChunkPos(mid.x()+dx,mid.z()+dz);var c=new ProtoChunk(cp,UpgradeData.EMPTY,realm,PalettedContainerFactory.create(r),null);g.fillFromNoise(net.minecraft.world.level.levelgen.blending.Blender.empty(),random,realm.structureManager(),c).join();c.setPersistedStatus(ChunkStatus.CARVERS);chunks.put(cp,c);}
            var cache=StaticCache2D.<GenerationChunkHolder>create(mid.x(),mid.z(),5,(x,z)->new GenerationChunkHolder(new ChunkPos(x,z)) {
                @Override public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status){return chunks.get(new ChunkPos(x,z));}
                @Override protected void addSaveDependency(CompletableFuture<?> p){}
                @Override public int getTicketLevel(){return 0;}
                @Override public int getQueueLevel(){return 0;}
            });
            for(var c:chunks.values())if(box.intersects(new BoundingBox(c.getPos().getMinBlockX(),box.minY(),c.getPos().getMinBlockZ(),c.getPos().getMaxBlockX(),box.maxY(),c.getPos().getMaxBlockZ())))place(realm,cache,piece,c,center);
            var neighbor=LanternTowns.discover(realm,center);
            h.assertTrue(neighbor!=null && realm.getBlockEntity(neighbor.pos()) instanceof ProjectStation actual && actual.isTown(),"An explicit first portal journey generates another actual native town rather than landing at a guessed coordinate");
            var heart=center.offset(12,1,19);var owner=chunks.get(ChunkPos.containing(heart));var station=(ProjectStation)owner.getBlockEntity(heart);
            h.assertTrue(station!=null && !station.hasLevel() && station.isTown(),"A detached ProtoChunk saves the actual town marker without ticking NPCs/portals");
            var saved=piece.createTag(StructurePieceSerializationContext.fromLevel(realm));var restored=new LanternWarrenPiece(saved);owner.setBlockState(heart.east(),Blocks.GOLD_BLOCK.defaultBlockState(),0);place(realm,cache,restored,owner,center);
            h.assertTrue(owner.getBlockState(heart.east()).is(Blocks.GOLD_BLOCK),"Saved generation completion preserves stock and later edits on replay");
            java.util.function.Function<BlockPos,net.minecraft.world.level.block.state.BlockState> cell=p->{var c=chunks.get(ChunkPos.containing(p));return c==null ? Blocks.AIR.defaultBlockState() : c.getBlockState(p);};
            var seen=new HashSet<BlockPos>();var q=new ArrayDeque<BlockPos>();q.add(center.offset(-3,33,-49));
            while(!q.isEmpty()) {var p=q.remove();if(!seen.add(p))continue;for(var dir:Direction.Plane.HORIZONTAL)for(int dy:new int[]{0,1,-1}) {
                var next=p.relative(dir).above(dy);if(Math.abs(next.getX()-center.getX())>46 || Math.abs(next.getZ()-center.getZ())>60 || next.getY()<center.getY() || next.getY()>center.getY()+40 || seen.contains(next))continue;
                if(!cell.apply(next.below()).getCollisionShape(EmptyBlockGetter.INSTANCE,next.below()).isEmpty() && cell.apply(next).getCollisionShape(EmptyBlockGetter.INSTANCE,next).isEmpty() && cell.apply(next.above()).getCollisionShape(EmptyBlockGetter.INSTANCE,next.above()).isEmpty())q.add(next);
            }}
            for(int[] dest:new int[][]{{0,1,-5},{25,1,33},{0,1,31},{-30,10,4},{30,10,4}})h.assertTrue(seen.contains(center.offset(dest[0],dest[1],dest[2])),"A real player can walk from the surface down to each civic district/home: "+Arrays.toString(dest));

        }
    }
    private static void place(ServerLevel l,StaticCache2D<GenerationChunkHolder> cache,LanternWarrenPiece p,ProtoChunk c,BlockPos origin) {
        var region=new WorldGenRegion(l,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),c);var cp=c.getPos();p.postProcess(region,l.structureManager(),l.getChunkSource().getGenerator(),RandomSource.create(1),new BoundingBox(cp.getMinBlockX(),l.getMinY(),cp.getMinBlockZ(),cp.getMaxBlockX(),l.getMaxY()-1,cp.getMaxBlockZ()),cp,origin);
    }
}
