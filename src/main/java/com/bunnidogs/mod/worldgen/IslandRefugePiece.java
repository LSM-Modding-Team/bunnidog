package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.IslandShape;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModStructures;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Version-one, chunk-owned terrain and fixtures; saved chunk/founder masks prevent replay farming or restoration. */
public final class IslandRefugePiece extends StructurePiece {
    private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("c","ores"));
    private final int cx, sea, cz, bed;
    private final long seed;
    private final AtomicInteger completedChunks=new AtomicInteger(), attemptedFounders=new AtomicInteger();
    private final AtomicInteger workingChunks=new AtomicInteger();
    public IslandRefugePiece(int cx,int sea,int cz,int bed,long seed) {
        super(ModStructures.ISLAND_PIECE.get(),0,new BoundingBox(cx-IslandShape.RADIUS,bed,cz-IslandShape.RADIUS,
                cx+IslandShape.RADIUS,sea+IslandShape.ABOVE,cz+IslandShape.RADIUS));
        this.cx=cx; this.sea=sea; this.cz=cz; this.bed=bed; this.seed=seed;
    }
    public IslandRefugePiece(CompoundTag tag) {
        super(ModStructures.ISLAND_PIECE.get(),tag);
        this.cx=tag.getIntOr("CX",0); this.sea=tag.getIntOr("Sea",63); this.cz=tag.getIntOr("CZ",0);
        this.bed=tag.getIntOr("Bed",this.sea-16); this.seed=tag.getLongOr("Seed",0);
        this.completedChunks.set(tag.getIntOr("CompletedChunks",0)); this.attemptedFounders.set(tag.getIntOr("AttemptedFounders",0));
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("IslandVersion",1); tag.putInt("CX",cx); tag.putInt("Sea",sea); tag.putInt("CZ",cz);
        tag.putInt("Bed",bed); tag.putLong("Seed",seed);
        tag.putInt("CompletedChunks",completedChunks.get()); tag.putInt("AttemptedFounders",attemptedFounders.get());
    }
    private int chunkBit(ChunkPos chunk) {
        int x=chunk.x()-(this.boundingBox.minX()>>4), z=chunk.z()-(this.boundingBox.minZ()>>4);
        int width=(this.boundingBox.maxX()>>4)-(this.boundingBox.minX()>>4)+1;
        return 1<<(z*width+x);
    }
    private static boolean ore(BlockState state) { return state.is(ORES) || state.is(Blocks.ANCIENT_DEBRIS); }
    private static boolean natural(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(BlockTags.DIRT) || state.is(BlockTags.GRASS_BLOCKS)
                || state.is(BlockTags.SAND) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY) || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS) || ore(state);
    }
    @Override public void postProcess(WorldGenLevel world, StructureManager structures, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBox, ChunkPos chunk, BlockPos origin) {
        if (chunk.x()<(boundingBox.minX()>>4) || chunk.x()>(boundingBox.maxX()>>4)
                || chunk.z()<(boundingBox.minZ()>>4) || chunk.z()>(boundingBox.maxZ()>>4)) return;
        int bit=chunkBit(chunk);
        if (!world.hasChunkAt(new BlockPos(chunk.getMinBlockX(),sea,chunk.getMinBlockZ())) || (completedChunks.get()&bit)!=0) return;
        // Independent neighbor chunks can finish in parallel; reserve in-progress work without saving it as complete.
        if ((workingChunks.getAndUpdate(mask->mask|bit)&bit)!=0) return;
        try {
            if ((completedChunks.get()&bit)==0) {
                generateChunk(world,chunkBox);
                completedChunks.getAndUpdate(mask->mask|bit);
            }
        } finally { workingChunks.getAndUpdate(mask->mask&~bit); }
    }
    private void generateChunk(WorldGenLevel world,BoundingBox chunkBox) {
        Set<Long> blocked = new HashSet<>();
        for (int x=Math.max(chunkBox.minX(),boundingBox.minX());x<=Math.min(chunkBox.maxX(),boundingBox.maxX());x++)
            for (int z=Math.max(chunkBox.minZ(),boundingBox.minZ());z<=Math.min(chunkBox.maxZ(),boundingBox.maxZ());z++) {
                int dx=x-cx, dz=z-cz;
                if (!IslandShape.contains(dx,dz,seed) || !world.hasChunkAt(new BlockPos(x,sea,z))) continue;
                int low=sea+IslandShape.bottom(dx,dz,bed-sea), top=sea+IslandShape.top(dx,dz);
                boolean safe=true;
                for (int y=low;y<=sea+IslandShape.ABOVE;y++) {
                    var existing=world.getBlockState(new BlockPos(x,y,z));
                    if (existing.hasBlockEntity() || !natural(existing)) { safe=false; break; }
                }
                if (!safe) { blocked.add(new BlockPos(x,0,z).asLong()); continue; }
                for (int y=low;y<=top;y++) {
                    var pos=new BlockPos(x,y,z); var existing=world.getBlockState(pos);
                    if (ore(existing)) continue;
                    world.setBlock(pos,state(IslandShape.terrain(dx,y-sea,dz,seed),0),2);
                }
            }
        for (var cell : IslandShape.decorations(seed)) {
            var pos=new BlockPos(cx+cell.dx(),sea+cell.dy(),cz+cell.dz());
            if (!chunkBox.isInside(pos) || blocked.contains(new BlockPos(pos.getX(),0,pos.getZ()).asLong()) || !world.hasChunkAt(pos)) continue;
            var existing=world.getBlockState(pos);
            if (existing.hasBlockEntity() || ore(existing)) continue;
            world.setBlock(pos,state(cell),2);
            if (cell.material()==IslandShape.Mat.CHEST && world.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest) {
                chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("bunnidogs","chests/island_refuge")));
                chest.setLootTableSeed(seed^pos.asLong()); chest.setChanged();
            }
        }
        int index=0;
        for (var spot : IslandShape.FOUNDERS) {
            int founderBit=1<<index; int ordinal=index++;
            var pos=new BlockPos(cx+spot.dx(),sea+spot.dy(),cz+spot.dz());
            if (!chunkBox.isInside(pos) || (attemptedFounders.getAndUpdate(mask->mask|founderBit)&founderBit)!=0) continue;
            if (blocked.contains(new BlockPos(pos.getX(),0,pos.getZ()).asLong())
                    || !world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()
                    || !world.getBlockState(pos.below()).is(ModBlocks.NEST.get())) continue;
            Bunnidog dog=ModEntities.BUNNIDOG.get().create(world.getLevel(),EntitySpawnReason.STRUCTURE);
            if (dog==null) continue;
            dog.setUUID(UUID.nameUUIDFromBytes(("bunnidogs:island:"+seed+":"+cx+":"+cz+":"+ordinal).getBytes(StandardCharsets.UTF_8)));
            dog.setGenome(Genome.randomFounder(Lineage.ISLAND,new Random(seed+ordinal*731L)));
            dog.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5); dog.setPersistenceRequired(); dog.setHealth(dog.getMaxHealth());
            world.addFreshEntityWithPassengers(dog);
        }
    }
    private static BlockState state(IslandShape.Cell cell) {
        return state(cell.material(),cell.detail());
    }
    private static BlockState state(IslandShape.Mat material,int detail) {
        return switch(material) {
            case AIR -> Blocks.AIR.defaultBlockState(); case SAND -> Blocks.SAND.defaultBlockState();
            case STONE -> Blocks.STONE.defaultBlockState(); case DIRT -> Blocks.DIRT.defaultBlockState();
            case GRASS -> Blocks.GRASS_BLOCK.defaultBlockState(); case COARSE_DIRT -> Blocks.COARSE_DIRT.defaultBlockState();
            case SANDSTONE -> Blocks.SANDSTONE.defaultBlockState();
            case SLAB -> Blocks.SANDSTONE_SLAB.defaultBlockState(); case LOG -> ModBlocks.GUAVA_LOG.get().defaultBlockState();
            case STAIRS -> Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
            case LEAVES -> ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT,false).setValue(LeavesBlock.DISTANCE,detail);
            case FRUIT -> ModBlocks.GUAVA_FRUIT.get().defaultBlockState(); case NEST -> ModBlocks.NEST.get().defaultBlockState();
            case TROUGH -> ModBlocks.FEEDING_TROUGH.get().defaultBlockState(); case BELL -> Blocks.BELL.defaultBlockState();
            case LANTERN -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
            case CHEST -> Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST);
            case WATER -> Blocks.WATER.defaultBlockState(); case KELP_PLANT -> Blocks.KELP_PLANT.defaultBlockState();
            case KELP -> Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE,20); case SEAGRASS -> Blocks.SEAGRASS.defaultBlockState();
            case PATH -> Blocks.DIRT_PATH.defaultBlockState();
        };
    }
}
