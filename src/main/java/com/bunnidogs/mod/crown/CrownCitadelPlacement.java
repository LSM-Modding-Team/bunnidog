package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

/** Chunk-local native templates: authored air carves occupied rooms only. No whole-island clear,
 * block entities are configured without an attached level, and retries never refill a container. */
public final class CrownCitadelPlacement {
    static void place(WorldGenLevel world,BlockPos center,UUID site,BoundingBox chunk,boolean existingWorld) {
        Set<Long> protectedColumns=new HashSet<>();
        for(int x=chunk.minX();x<=chunk.maxX();x++)for(int z=chunk.minZ();z<=chunk.maxZ();z++) {
            for(int y=center.getY()-12;y<=center.getY()+70;y++) {
                var state=world.getBlockState(new BlockPos(x,y,z));
                if(state.hasBlockEntity() || existingWorld && y>=center.getY()-12 && !CrownWarrenPiece.natural(state)) {protectedColumns.add(BlockPos.asLong(x,0,z));break;}
            }
        }
        var processor=new StructureProcessor() {
            @Override public StructureTemplate.StructureBlockInfo process(LevelReader level,BlockPos target,BlockPos reference,StructureTemplate.StructureBlockInfo original,StructureTemplate.StructureBlockInfo info,StructurePlaceSettings settings,StructureTemplate template) {
                var p=info.pos();
                // Native processing precedes its bounding-box filter: guard BEFORE any world lookup.
                if(!chunk.isInside(p) || protectedColumns.contains(BlockPos.asLong(p.getX(),0,p.getZ())) || !world.ensureCanWrite(p))return null;
                var old=level.getBlockState(p);
                return old.hasBlockEntity() || info.state().isAir() && old.isAir() ? null : info;
            }
            @Override public MapCodec<? extends StructureProcessor> codec() {return MapCodec.unit(this);}
        };
        var settings=new StructurePlaceSettings().setBoundingBox(chunk).setIgnoreEntities(true).setKnownShape(true).setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING).addProcessor(processor);
        var manager=world.getLevel().getStructureManager();
        for(var tile:CrownSitePlan.tiles()) {
            var origin=center.offset(tile.offset());
            if(!new BoundingBox(origin.getX(),origin.getY(),origin.getZ(),origin.getX()+15,origin.getY()+82,origin.getZ()+15).intersects(chunk))continue;
            var template=manager.get(Identifier.fromNamespaceAndPath("bunnidogs",tile.name())).orElseThrow(()->new IllegalStateException("Missing Crown tile "+tile.name()));
            template.placeInWorld(world,origin,origin,settings,RandomSource.create(site.getMostSignificantBits()^site.getLeastSignificantBits()^origin.asLong()),2);
            for(var info:template.filterBlocks(origin,settings,Blocks.CHEST)) {
                if(!protectedColumns.contains(BlockPos.asLong(info.pos().getX(),0,info.pos().getZ())) && world.getBlockEntity(info.pos()) instanceof RandomizableContainerBlockEntity chest) {
                    chest.setLootTableSeed(site.getMostSignificantBits()^site.getLeastSignificantBits()^info.pos().asLong());chest.setChanged();
                }
            }
            for(var info:template.filterBlocks(origin,settings,Blocks.BARREL)) {
                if(!protectedColumns.contains(BlockPos.asLong(info.pos().getX(),0,info.pos().getZ())) && world.getBlockEntity(info.pos()) instanceof RandomizableContainerBlockEntity chest) {
                    chest.setLootTableSeed(site.getMostSignificantBits()^site.getLeastSignificantBits()^info.pos().asLong());chest.setChanged();
                }
            }
        }
        // Only authored root feet grow down to the local island, capped at 24 blocks. Terrain
        // between foundations retains its hills, hollows and vegetation; never grade a square island.
        for(int x=chunk.minX();x<=chunk.maxX();x++)for(int z=chunk.minZ();z<=chunk.maxZ();z++) {
            var foot=new BlockPos(x,center.getY()-12,z);
            if(protectedColumns.contains(BlockPos.asLong(x,0,z)) || !world.getBlockState(foot).is(ModBlocks.ROOTSTONE.get()))continue;
            for(int d=1;d<=24;d++) {
                var p=foot.below(d);if(!chunk.isInside(p) || !world.ensureCanWrite(p))break;
                var old=world.getBlockState(p);if(!old.isAir() && !old.canBeReplaced())break;
                world.setBlock(p,ModBlocks.ROOTSTONE.get().defaultBlockState(),2);
            }
        }
        for(int wing=0;wing<4;wing++) {
            var p=CrownSitePlan.seal(center,wing);
            if(chunk.isInside(p) && world.getBlockEntity(p) instanceof CrownSealBlockEntity seal && seal.site()==null)seal.configureCitadel(site,center,CrownSitePlan.geometry(center,wing));
        }
    }
    /** A V2 repair clears only authored air currently obstructed by natural terrain, and restores
     * missing controls and only still-closed saved gates. It never repastes roofs, furniture or container NBT. */
    static int repair(net.minecraft.server.level.ServerLevel level,CrownWarrenPiece piece) {
        int count=0;var center=piece.center();
        for(var tile:CrownSitePlan.tiles()) {
            var origin=center.offset(tile.offset());
            if(!level.hasChunkAt(origin))continue;
            var template=level.getStructureManager().get(Identifier.fromNamespaceAndPath("bunnidogs",tile.name())).orElseThrow();
            var settings=new StructurePlaceSettings();
            for(var info:template.filterBlocks(origin,settings,Blocks.AIR)) {
                var p=info.pos();if(!level.hasChunkAt(p))continue;
                var old=level.getBlockState(p);
                if(!old.isAir() && !old.hasBlockEntity() && CrownWarrenPiece.natural(old)) {level.setBlock(p,Blocks.AIR.defaultBlockState(),2);count++;}
            }
        }
        for(int wing=0;wing<4;wing++) {
            var p=CrownSitePlan.seal(center,wing);if(!level.hasChunkAt(p))continue;
            var old=level.getBlockState(p);
            if(!old.hasBlockEntity() && (old.isAir() || CrownWarrenPiece.natural(old))) {level.setBlock(p,ModBlocks.CROWN_SEAL.get().defaultBlockState().setValue(CrownSealBlock.WING,wing),2);count++;}
            if(level.getBlockEntity(p) instanceof CrownSealBlockEntity seal && seal.site()==null)seal.configureCitadel(piece.site(),center,CrownSitePlan.geometry(center,wing));
        }
        var mainPos=CrownSitePlan.seal(center,3);
        if(level.hasChunkAt(mainPos) && level.getBlockEntity(mainPos) instanceof CrownSealBlockEntity main && piece.site().equals(main.site()) && main.citadel()) {
            var plan=main.plan();
            for(int i=0;i<plan.sockets().size();i++) {
                var p=plan.sockets().get(i);if(!level.hasChunkAt(p))continue;var old=level.getBlockState(p);
                if(!old.hasBlockEntity() && (old.isAir() || CrownWarrenPiece.natural(old))) {level.setBlock(p,ModBlocks.CROWN_SOCKET.get().defaultBlockState().setValue(CrownGateBlock.KIND,i==2 ? 1 : 0),2);count++;}
            }
            for(var gate:plan.gates()) {
                boolean open=gate.kind()==0 ? Integer.bitCount(main.sockets())>=2 : gate.kind()==1 ? main.sockets()==7 : main.cleared();
                if(open)continue;var b=gate.region().box();
                for(var p:BlockPos.betweenClosed(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ())) {
                    if(!level.hasChunkAt(p))continue;var old=level.getBlockState(p);
                    if(!old.hasBlockEntity() && (old.isAir() || CrownWarrenPiece.natural(old))) {level.setBlock(p,ModBlocks.CROWN_GATE.get().defaultBlockState().setValue(CrownGateBlock.KIND,gate.kind()),2);count++;}
                }
            }
        }
        return count;
    }
    public static int fortify(net.minecraft.server.level.ServerLevel level,CrownWarrenPiece piece) {
        int count=0;
        for(var tile:CrownSitePlan.tiles()) {
            var origin=piece.center().offset(tile.offset());if(!level.hasChunkAt(origin))continue;
            var template=level.getStructureManager().get(Identifier.fromNamespaceAndPath("bunnidogs",tile.name())).orElseThrow();
            for(var entry:LunarMasonry.BLOCKS.values())for(var info:template.filterBlocks(origin,new StructurePlaceSettings(),entry.get())) {
                var pos=info.pos();if(!level.hasChunkAt(pos))continue;var old=level.getBlockState(pos);
                if(old.hasBlockEntity())continue;var upgraded=LunarMasonry.upgrade(old);
                if(upgraded!=old && upgraded.getBlock()==info.state().getBlock()) {level.setBlock(pos,upgraded,2);count++;}
            }
        }
        return count;
    }
    private CrownCitadelPlacement() {}
}
