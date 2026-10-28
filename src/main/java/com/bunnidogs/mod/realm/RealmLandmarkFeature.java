package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Large, bounded region silhouettes. Never constructs live entities or updates detached station block entities. */
public final class RealmLandmarkFeature extends Feature<NoneFeatureConfiguration> {
    public RealmLandmarkFeature() {super(NoneFeatureConfiguration.CODEC);}
    private static void put(WorldGenLevel world,BlockPos pos,BlockState state) {
        if(world.ensureCanWrite(pos) && world.getBlockState(pos).isAir())world.setBlock(pos,state,2);
    }
    /** Structures precede vegetation: reserve their entire clearing, including neighboring canopy reach. */
    public static boolean blockedByDungeon(WorldGenLevel world,BlockPos pos) {
        var manager=world.getLevel().structureManager();
        if(world instanceof net.minecraft.server.level.WorldGenRegion region)manager=manager.forWorldGenRegion(region);
        var chunk=net.minecraft.world.level.ChunkPos.containing(pos);
        var growth=new net.minecraft.world.level.levelgen.structure.BoundingBox(pos.getX()-9,world.getMinY(),pos.getZ()-9,pos.getX()+9,world.getMaxY()-1,pos.getZ()+9);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)for(var start:manager.startsForStructure(new net.minecraft.world.level.ChunkPos(chunk.x()+x,chunk.z()+z),s->s instanceof com.bunnidogs.mod.crown.CrownWarrenStructure || s instanceof com.bunnidogs.mod.guild.LanternWarrenStructure))
            if(start.isValid() && start.getBoundingBox().intersects(growth))return true;
        return false;
    }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        var world=ctx.level();var pos=ctx.origin();var rng=ctx.random();
        if(blockedByDungeon(world,pos))return false;
        if(ctx.chunkGenerator() instanceof RealmChunkGenerator roads && roads.terrain().column(pos.getX(),pos.getZ()).bridge())return false;
        if(world.getBlockState(pos.below()).is(Blocks.END_STONE_BRICKS) || world.getBlockState(pos.below()).is(Blocks.SEA_LANTERN) || pos.getY()>170 || pos.getY()<42 || !world.getFluidState(pos.below()).isEmpty() || !world.getBlockState(pos.below()).isSolidRender())return false;
        String biome=world.getBiome(pos).unwrapKey().map(k->k.identifier().getPath()).orElse("");
        if(biome.equals("crownroot_groves")) {
            if(rng.nextInt(3)!=0)return false;
            int height=18+rng.nextInt(10);
            for(int y=0;y<height;y++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(Math.abs(x)+Math.abs(z)<2 || y<height/2)put(world,pos.offset(x,y,z),ModBlocks.GUAVA_LOG.get().defaultBlockState());
            for(int arm=0;arm<4;arm++)for(int t=1;t<=8;t++) {
                int x=arm==0 ? t : arm==1 ? -t : 0,z=arm==2 ? t : arm==3 ? -t : 0;
                int y=(int)(Math.sin(t/8.0*Math.PI)*4);
                put(world,pos.offset(x,y,z),ModBlocks.GUAVA_WOOD.get().defaultBlockState());
                put(world,pos.offset(x,y+1,z),ModBlocks.GUAVA_WOOD.get().defaultBlockState());
            }
            for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)for(int y=height-3;y<=height+2;y++)
                if(x*x+z*z+(y-height)*(y-height)*3<50)put(world,pos.offset(x,y,z),ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
            for(int x:new int[]{-3,3})for(int z:new int[]{-3,3}) {
                var lamp=pos.offset(x,height-4,z);
                if(world.ensureCanWrite(lamp) && world.getBlockState(lamp.above()).is(ModBlocks.GUAVA_LEAVES.get()))put(world,lamp,Blocks.SHROOMLIGHT.defaultBlockState());
            }
            for(int x=-6;x<=6;x+=3)for(int z=-6;z<=6;z+=3)for(int y=height-4;y<=height+1;y++) {
                var fruit=pos.offset(x,y,z);
                if(world.ensureCanWrite(fruit) && world.getBlockState(fruit).isAir() && world.getBlockState(fruit.above()).is(ModBlocks.GUAVA_LEAVES.get())) {
                    put(world,fruit,ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(com.bunnidogs.mod.block.GuavaFruitBlock.AGE,2));break;
                }
            }
        } else if(biome.equals("glassbloom_flats")) {
            for(int arm=0;arm<5;arm++) {
                int x=rng.nextInt(11)-5,z=rng.nextInt(11)-5,height=4+rng.nextInt(7);
                int groundY=world.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,pos.getX()+x,pos.getZ()+z);
                var stem=new BlockPos(pos.getX()+x,groundY,pos.getZ()+z);
                if(!world.ensureCanWrite(stem) || !world.getFluidState(stem.below()).isEmpty() || !world.getBlockState(stem.below()).isSolidRender())continue;
                for(int y=0;y<height;y++) {
                    put(world,stem.offset(0,y,0),y>height-3 ? Blocks.AMETHYST_BLOCK.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
                    if(y<height/2) {put(world,stem.offset(1,y,0),Blocks.CALCITE.defaultBlockState());put(world,stem.offset(0,y,1),Blocks.CALCITE.defaultBlockState());}
                }
                put(world,stem.above(height),Blocks.AMETHYST_CLUSTER.defaultBlockState());
            }
        } else if(biome.equals("thunder_hollows")) {
            int height=4+rng.nextInt(9);
            for(int y=0;y<height;y++)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(x*x+z*z<7-y*.35)put(world,pos.offset(x,y,z),y>height-3 ? Blocks.GILDED_BLACKSTONE.defaultBlockState() : Blocks.BASALT.defaultBlockState());
        } else return false;
        return true;
    }
}
