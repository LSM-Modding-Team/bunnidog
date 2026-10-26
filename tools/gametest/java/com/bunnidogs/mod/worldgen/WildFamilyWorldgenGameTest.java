package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.society.HearthTreeBlockEntity;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.*;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.*;

public final class WildFamilyWorldgenGameTest {
    public static void run(GameTestHelper h) {
        var level=h.getLevel();var center=new ChunkPos(1130,1130);var chunks=new java.util.HashMap<ChunkPos,ProtoChunk>();
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {var at=new ChunkPos(center.x()+dx,center.z()+dz);var chunk=new ProtoChunk(at,UpgradeData.EMPTY,level,PalettedContainerFactory.create(level.registryAccess()),null);chunk.setPersistedStatus(ChunkStatus.CARVERS);chunks.put(at,chunk);}
        var cache=StaticCache2D.<GenerationChunkHolder>create(center.x(),center.z(),1,(x,z)->new GenerationChunkHolder(new ChunkPos(x,z)) {
            @Override public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status) {return chunks.get(new ChunkPos(x,z));}
            @Override protected void addSaveDependency(CompletableFuture<?> sync) {}
            @Override public int getTicketLevel() {return 0;}
            @Override public int getQueueLevel() {return 0;}
        });
        var region=new WorldGenRegion(level,cache,ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES),chunks.get(center));var at=new BlockPos(center.getMinBlockX()+8,80,center.getMinBlockZ()+8);
        for(int x=-7;x<=7;x++)for(int z=-8;z<=10;z++)region.setBlock(at.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState(),2);
        region.setBlock(at.east(),Blocks.CHEST.defaultBlockState(),2);
        h.assertTrue(!WildFamilyFeature.generate(region,at,1) && region.getBlockState(at).isAir(),"Preflight refuses an occupied glade before clearing a single cell");region.setBlock(at.east(),Blocks.AIR.defaultBlockState(),2);
        h.assertTrue(WildFamilyFeature.generate(region,at,1),"The registered natural feature accepts actual grass terrain on generation-stage ProtoChunks");
        var home=(HearthTreeBlockEntity)region.getBlockEntity(at);h.assertTrue(home!=null && !home.hasLevel() && home.wild().natural() && home.isEmpty(),"A detached common cache initializes only saved data, with no level access or free supplies");
        var data=home.saveWithFullMetadata(level.registryAccess());var copy=(HearthTreeBlockEntity)BlockEntity.loadStatic(at,home.getBlockState(),data,level.registryAccess());
        h.assertTrue(copy!=null && copy.site().equals(home.site()) && copy.wild().founders().equals(home.wild().founders()) && copy.owner().equals(home.owner()),"Native detached saves preserve the exact site identity and three genomes");
        h.assertTrue(!WildFamilyFeature.generate(region,at,1) && region.getBlockEntity(at)==home && region.getBlockState(home.wild().beds().getFirst()).is(ModBlocks.NEST.get()),"Replaying the feature cannot replace existing homes, reset founders or duplicate infrastructure");h.succeed();
    }
}
