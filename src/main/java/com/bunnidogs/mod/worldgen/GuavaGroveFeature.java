package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.layout.TreeShape;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Small ordinary fruit groves, independent of a settlement or a creature spawn. */
public final class GuavaGroveFeature extends Feature<NoneFeatureConfiguration> {
    public GuavaGroveFeature(){super(NoneFeatureConfiguration.CODEC);}
    public static boolean grow(WorldGenLevel world,BlockPos base,long seed) {
        if(!world.ensureCanWrite(base) || !world.hasChunkAt(base.below()) || !world.getBlockState(base.below()).is(BlockTags.SUPPORTS_VEGETATION))return false;
        var rng=new java.util.Random(seed);int lift=2+rng.nextInt(3);var shape=new java.util.ArrayList<TreeShape.Cell>();
        for(int y=0;y<lift;y++)shape.add(new TreeShape.Cell(0,y,0,TreeShape.Kind.LOG));
        for(var cell:TreeShape.generate(rng,true))shape.add(new TreeShape.Cell(cell.dx(),cell.dy()+lift,cell.dz(),cell.kind()));
        for(var cell:shape) {var at=base.offset(cell.dx(),cell.dy(),cell.dz());if(!world.ensureCanWrite(at) || !world.hasChunkAt(at) || world.isOutsideBuildHeight(at) || !world.getWorldBorder().isWithinBounds(at))return false;}
        if(!GuavaTree.fits(world,base,shape))return false;
        GuavaTree.place(world,base,shape,true);return true;
    }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c) {
        int trees=0;int[][] offsets={{0,0},{5,3},{-5,3}};
        for(var offset:offsets) {
            var probe=c.origin().offset(offset[0],0,offset[1]);if(!c.level().ensureCanWrite(probe))continue;
            int y=c.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,probe.getX(),probe.getZ());
            if(grow(c.level(),new BlockPos(probe.getX(),y,probe.getZ()),c.random().nextLong()))trees++;
        }return trees>0;
    }
}
