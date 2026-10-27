package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.registry.ModStructures;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Chunk-owned placement; interrupted passes preserve containers and never refill encounter/reward data. */
public final class CrownWarrenPiece extends StructurePiece {
    private final BlockPos center;
    private final UUID site;
    private final int layoutVersion;
    private final Set<Long> done=ConcurrentHashMap.newKeySet(),working=ConcurrentHashMap.newKeySet();
    public CrownWarrenPiece(int x,int y,int z,long seed) {
        this(x,y,z,seed,1);
    }
    public static CrownWarrenPiece citadel(int x,int y,int z,long seed) {return new CrownWarrenPiece(x,y,z,seed,2);}
    private CrownWarrenPiece(int x,int y,int z,long seed,int revision) {
        super(ModStructures.CROWN_WARREN_PIECE.get(),0,revision==1 ? new BoundingBox(x-90,y-12,z-90,x+90,y+64,z+90) : new BoundingBox(x-120,y-36,z-120,x+119,y+70,z+119));
        layoutVersion=revision;
        center=new BlockPos(x,y,z);site=new UUID(seed,center.asLong());
    }
    public CrownWarrenPiece(CompoundTag t) {
        super(ModStructures.CROWN_WARREN_PIECE.get(),t);layoutVersion=t.getIntOr("LayoutVersion",1);center=new BlockPos(t.getIntOr("CX",0),t.getIntOr("CY",80),t.getIntOr("CZ",0));
        site=new UUID(t.getLongOr("SiteHigh",0),t.getLongOr("SiteLow",center.asLong()));
        for(long n:t.getLongArray("Done").orElse(new long[0]))done.add(n);
    }
    public int layoutVersion() {return layoutVersion;}
    public BlockPos center() {return center;}
    public UUID site() {return site;}
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext c,CompoundTag t) {
        t.putInt("LayoutVersion",layoutVersion);t.putInt("CX",center.getX());t.putInt("CY",center.getY());t.putInt("CZ",center.getZ());t.putLong("SiteHigh",site.getMostSignificantBits());t.putLong("SiteLow",site.getLeastSignificantBits());t.putLongArray("Done",done.stream().mapToLong(Long::longValue).toArray());
    }
    @Override public void postProcess(WorldGenLevel world,StructureManager manager,ChunkGenerator generator,RandomSource random,BoundingBox box,ChunkPos chunk,BlockPos origin) {
        if(!boundingBox.intersects(box) || done.contains(chunk.pack()) || !working.add(chunk.pack()))return;
        try {
            boolean existingWorld=world.getChunk(chunk.x(),chunk.z()).getPersistedStatus().isOrAfter(net.minecraft.world.level.chunk.status.ChunkStatus.FULL);
            if(layoutVersion==2) {CrownCitadelPlacement.place(world,center,site,box,existingWorld);done.add(chunk.pack());return;}
            for(int x=Math.max(boundingBox.minX(),box.minX());x<=Math.min(boundingBox.maxX(),box.maxX());x++)for(int z=Math.max(boundingBox.minZ(),box.minZ());z<=Math.min(boundingBox.maxZ(),box.maxZ());z++) {
                int dx=x-center.getX(),dz=z-center.getZ();boolean floor=CrownLayout.cell(dx,0,dz)!=null;
                boolean protectedColumn=false;
                for(int y=center.getY()-12;y<=center.getY()+64;y++) {var state=world.getBlockState(new BlockPos(x,y,z));if(state.hasBlockEntity() || existingWorld && y>=center.getY() && !natural(state)) {protectedColumn=true;break;}}
                if(protectedColumn)continue;
                for(int y=center.getY()-12;y<=center.getY()+64;y++) {
                    var pos=new BlockPos(x,y,z);if(!box.isInside(pos) || !world.ensureCanWrite(pos))continue;
                    var state=CrownLayout.cell(dx,y-center.getY(),dz);
                    if(state==null && y>center.getY())state=Blocks.AIR.defaultBlockState();
                    // Short supporting roots ground the elevated site without digging into terrain.
                    if(floor && state==null && y<center.getY()-2 && dx%12==0 && dz%12==0 && world.getBlockState(pos).isAir())state=Blocks.TUFF_BRICKS.defaultBlockState();
                    if(state==null || state.isAir() && world.getBlockState(pos).isAir())continue;
                    world.setBlock(pos,state,2);
                    if(state.is(com.bunnidogs.mod.registry.ModBlocks.CROWN_SEAL.get()) && world.getBlockEntity(pos) instanceof CrownSealBlockEntity seal)seal.configure(site,center);
                }
            }
            done.add(chunk.pack());
        } finally {working.remove(chunk.pack());}
    }
    public static boolean natural(BlockState s) {
        return s.isAir() || s.is(Blocks.END_STONE) || s.is(com.bunnidogs.mod.registry.ModBlocks.MOONSTONE.get()) || s.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock || s.is(Blocks.SMOOTH_BASALT) || s.is(Blocks.BUDDING_AMETHYST) || s.is(Blocks.GILDED_BLACKSTONE) || s.is(Blocks.GRAVEL) || s.is(Blocks.SAND) || s.is(Blocks.CLAY) || net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath().endsWith("_ore") || s.is(com.bunnidogs.mod.registry.ModBlocks.GUAVA_FRUIT.get()) || s.is(BlockTags.FLOWERS) || s.is(BlockTags.REPLACEABLE_BY_TREES) || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.DIRT) || s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES) || !s.getFluidState().isEmpty()
                || s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.CALCITE) || s.is(Blocks.BASALT) || s.is(Blocks.POLISHED_BASALT) || s.is(Blocks.AMETHYST_BLOCK) || s.is(Blocks.AMETHYST_CLUSTER)
                || s.is(Blocks.SHROOMLIGHT) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS);
    }
}
