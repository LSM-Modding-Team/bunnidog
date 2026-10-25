package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Wings;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModStructures;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Chunk-owned ruined halls, with frozen genetic archives and replay-proof native loot chests. */
public final class GreatRuinPiece extends StructurePiece {
    private final int cx,cy,cz;
    private final long seed;
    private final AtomicInteger done=new AtomicInteger(),working=new AtomicInteger();
    public GreatRuinPiece(int x,int y,int z,long seed) {
        super(ModStructures.GREAT_RUIN_PIECE.get(),0,new BoundingBox(x-12,y-10,z-12,x+12,y+12,z+12));
        cx=x;cy=y;cz=z;this.seed=seed;
    }
    public GreatRuinPiece(CompoundTag tag) {
        super(ModStructures.GREAT_RUIN_PIECE.get(),tag);cx=tag.getIntOr("CX",0);cy=tag.getIntOr("CY",80);cz=tag.getIntOr("CZ",0);seed=tag.getLongOr("Seed",0);done.set(tag.getIntOr("Done",0));
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag tag) {
        tag.putInt("CX",cx);tag.putInt("CY",cy);tag.putInt("CZ",cz);tag.putLong("Seed",seed);tag.putInt("Done",done.get());
    }
    private int bit(ChunkPos chunk) {
        int width=(boundingBox.maxX()>>4)-(boundingBox.minX()>>4)+1;
        return 1<<((chunk.z()-(boundingBox.minZ()>>4))*width+chunk.x()-(boundingBox.minX()>>4));
    }
    @Override public void postProcess(WorldGenLevel world,StructureManager structures,ChunkGenerator generator,RandomSource random,BoundingBox chunkBox,ChunkPos chunk,BlockPos origin) {
        if(chunk.x()<(boundingBox.minX()>>4) || chunk.x()>(boundingBox.maxX()>>4) || chunk.z()<(boundingBox.minZ()>>4) || chunk.z()>(boundingBox.maxZ()>>4))return;
        int bit=bit(chunk);if((done.get()&bit)!=0 || (working.getAndUpdate(mask->mask|bit)&bit)!=0)return;
        try {
            for(int x=Math.max(cx-12,chunkBox.minX());x<=Math.min(cx+12,chunkBox.maxX());x++)for(int z=Math.max(cz-12,chunkBox.minZ());z<=Math.min(cz+12,chunkBox.maxZ());z++) {
                int dx=x-cx,dz=z-cz;
                // Do not edit any protected/container column, even during an interrupted or old generation pass.
                boolean blocked=false;
                for(int y=cy-10;y<=cy+12;y++) {
                    var existing=world.getBlockState(new BlockPos(x,y,z));
                    if(existing.hasBlockEntity() || !natural(existing)) {blocked=true;break;}
                }
                if(blocked)continue;
                for(int y=cy-10;y<=cy+12;y++) {
                    var pos=new BlockPos(x,y,z);var state=cell(dx,y-cy,dz,seed);if(state==null || !chunkBox.isInside(pos) || !world.ensureCanWrite(pos))continue;
                    if(state.isAir() && world.getBlockState(pos).isAir())continue;
                    world.setBlock(pos,state,2);
                    if(state.is(Blocks.CHEST) && world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                        if(dx<0)archive(chest,seed^pos.asLong());
                        else {
                            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("bunnidogs",dx==0 ? "chests/warren_supplies" : "chests/great_ruin")));
                            chest.setLootTableSeed(seed^pos.asLong());
                        }
                        chest.setChanged();
                    }
                }
            }
            done.getAndUpdate(mask->mask|bit);
        } finally {working.getAndUpdate(mask->mask&~bit);}
    }
    private static boolean natural(BlockState s) {
        return s.isAir() || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.DIRT) || s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES)
                || s.is(Blocks.MOSS_BLOCK) || s.is(Blocks.CALCITE) || s.is(Blocks.POLISHED_BASALT) || s.is(Blocks.BASALT) || s.is(Blocks.AMETHYST_BLOCK) || s.is(Blocks.AMETHYST_CLUSTER)
                || s.is(Blocks.SHROOMLIGHT) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS);
    }
    /** Exposed for art: the same room cells produce the headless preview and generated structure. */
    public static BlockState cell(int x,int y,int z,long seed) {
        int edge=Math.max(Math.abs(x),Math.abs(z));
        if(edge>11)return null;
        boolean pillar=(Math.abs(x)==8 && Math.abs(z)==8) || (Math.abs(x)==8 && z==0);
        if(y<0 && pillar)return Blocks.TUFF_BRICKS.defaultBlockState();
        if(y<0)return null;
        if(y==0)return (edge==11 || x%4==0 || z%4==0 ? Blocks.POLISHED_TUFF : Blocks.TUFF_BRICKS).defaultBlockState();
        if(pillar && y<=8)return (y==8 ? Blocks.CHISELED_TUFF_BRICKS : Blocks.TUFF_BRICKS).defaultBlockState();
        if(y==9 && (x==0 && Math.abs(z)<=8 || z==0 && Math.abs(x)<=8 || Math.abs(x)==8 && Math.abs(z)<=8 || Math.abs(z)==8 && Math.abs(x)<=8))return Blocks.POLISHED_TUFF.defaultBlockState();
        if(y==10 && (x*x+z*z<25))return (x*x+z*z<8 ? Blocks.SEA_LANTERN : Blocks.TINTED_GLASS).defaultBlockState();
        if(y==1 && (Math.abs(x)==5 && z==5 || x==0 && z==-5))return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.NORTH);
        if(edge==11 && y<=2 && !(Math.abs(x)<3 || Math.abs(z)<3) && Math.floorMod(x*31+z*19+seed,7)!=0)return Blocks.TUFF_BRICK_WALL.defaultBlockState();
        if(y<9 && edge<11)return Blocks.AIR.defaultBlockState();
        return null;
    }
    private static void archive(ChestBlockEntity chest,long seed) {
        var random=new Random(seed);var a=Genome.randomFounder(Lineage.END,random);var b=Genome.randomFounder(Lineage.CAVE,random);
        if(random.nextBoolean())a=Wings.winged(a);
        var stats=Stats.of(40+random.nextInt(31),40+random.nextInt(31),40+random.nextInt(31),40+random.nextInt(31),40+random.nextInt(31));
        chest.setItem(11,SeedItem.create(new Seed(a,stats,b,stats,20+random.nextInt(14))));
        chest.setItem(13,TissueSampleItem.create(a,stats));
        chest.setItem(15,new ItemStack(ModItems.MUTAGEN.get(),8+random.nextInt(9)));
        chest.setItem(20,new ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD,64));
        chest.setItem(24,new ItemStack(ModItems.STARLIT_DUST.get(),16));
    }
}
