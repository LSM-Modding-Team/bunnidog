package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.registry.ModStructures;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

/** Each saved piece owns one site's native chunk tiles. Detached generation never invokes live NPC/portal code. */
public final class LanternWarrenPiece extends StructurePiece {
    private record Tile(BlockPos offset,String name) {}
    private static final List<Tile> TILES=tiles();
    private final BlockPos center;
    private final Set<Long> done=java.util.concurrent.ConcurrentHashMap.newKeySet();
    public LanternWarrenPiece(BlockPos c) {super(ModStructures.LANTERN_WARREN_PIECE.get(),0,new BoundingBox(c.getX()-48,c.getY()-3,c.getZ()-64,c.getX()+47,c.getY()+51,c.getZ()+47));center=c;}
    public LanternWarrenPiece(CompoundTag t) {super(ModStructures.LANTERN_WARREN_PIECE.get(),t);center=BlockPos.of(t.getLongOr("TownCenter",0));for(long n:t.getLongArray("Done").orElse(new long[0]))done.add(n);}
    public BlockPos center(){return center;}
    private static List<Tile> tiles() {
        try(var in=LanternWarrenPiece.class.getResourceAsStream("/data/bunnidogs/structure/lantern_warren.json")) {
            var rows=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("tiles");var out=new ArrayList<Tile>();
            for(var row:rows) {var a=row.getAsJsonArray();out.add(new Tile(new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt()),a.get(3).getAsString()));}return List.copyOf(out);
        }catch(java.io.IOException e){throw new ExceptionInInitializerError(e);}
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag t) {t.putLong("TownCenter",center.asLong());t.putLongArray("Done",done.stream().mapToLong(Long::longValue).toArray());}
    @Override public synchronized void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox chunkBox,ChunkPos chunk,BlockPos origin) {
        if(done.contains(chunk.pack()) || !boundingBox.intersects(chunkBox))return;
        boolean oldWorld=world.getChunk(chunk.x(),chunk.z()).getPersistedStatus().isOrAfter(net.minecraft.world.level.chunk.status.ChunkStatus.FULL);
        var processor=new StructureProcessor() {
            @Override public StructureTemplate.StructureBlockInfo process(LevelReader reader,BlockPos target,BlockPos ref,StructureTemplate.StructureBlockInfo original,StructureTemplate.StructureBlockInfo info,StructurePlaceSettings settings,StructureTemplate template) {
                if(!chunkBox.isInside(info.pos()) || !world.ensureCanWrite(info.pos()))return null;var old=reader.getBlockState(info.pos());
                return old.hasBlockEntity() || oldWorld && !com.bunnidogs.mod.crown.CrownWarrenPiece.natural(old) || old.isAir() && info.state().isAir() ? null : info;
            }
            @Override public com.mojang.serialization.MapCodec<? extends StructureProcessor> codec(){return com.mojang.serialization.MapCodec.unit(this);}
        };
        var settings=new StructurePlaceSettings().setBoundingBox(chunkBox).setIgnoreEntities(true).setKnownShape(true).setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING).addProcessor(processor);
        for(var tile:TILES) {
            var at=center.offset(tile.offset());if(!new BoundingBox(at.getX(),at.getY(),at.getZ(),at.getX()+15,at.getY()+54,at.getZ()+15).intersects(chunkBox))continue;
            var template=world.getLevel().getStructureManager().get(Identifier.fromNamespaceAndPath("bunnidogs",tile.name())).orElseThrow();template.placeInWorld(world,at,at,settings,random,2);
        }
        var heart=center.offset(12,1,19);
        if(chunkBox.isInside(heart) && world.getBlockEntity(heart) instanceof ProjectStation station)station.generatedTown();
        // Join the entrance terrace to the original hill with an actual graded stair approach.
        if(generator instanceof com.bunnidogs.mod.realm.RealmChunkGenerator islands)for(int dx=-4;dx<=4;dx++)for(int dz=-64;dz<=-53;dz++) {
            int x=center.getX()+dx,z=center.getZ()+dz;if((x>>4)!=chunk.x() || (z>>4)!=chunk.z())continue;
            int outside=islands.terrain().column(x,center.getZ()-64).landTop();double t=(dz+64)/12.0;
            int y=(int)Math.round(outside*(1-t)+(center.getY()+32)*t);var at=new BlockPos(x,y,z);
            if(!world.ensureCanWrite(at) || world.getBlockState(at).hasBlockEntity())continue;
            for(int d=-2;d<=5;d++) {
                var p=at.above(d);if(!chunkBox.isInside(p) || !world.ensureCanWrite(p) || world.getBlockState(p).hasBlockEntity())continue;
                var state=d>0 ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : com.bunnidogs.mod.crown.LunarMasonry.BLOCKS.get("lunar_bricks").get().defaultBlockState();
                if(d==0 && outside!=center.getY()+32)state=com.bunnidogs.mod.crown.LunarMasonry.BLOCKS.get("lunar_bricks_stairs").get().defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING,outside<center.getY()+32 ? net.minecraft.core.Direction.SOUTH : net.minecraft.core.Direction.NORTH);
                if(!oldWorld || com.bunnidogs.mod.crown.CrownWarrenPiece.natural(world.getBlockState(p)))world.setBlock(p,state,2);
            }
        }
        done.add(chunk.pack());
    }
}
