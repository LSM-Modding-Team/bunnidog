package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** A new qualifying glade, never retroactively activated in an old warren or player build. */
public final class WildFamilyFeature extends Feature<NoneFeatureConfiguration> {
    public WildFamilyFeature() {super(NoneFeatureConfiguration.CODEC);}
    public static boolean fits(WorldGenLevel world,BlockPos origin) {
        for(int x=-7;x<=7;x++)for(int z=-8;z<=10;z++) {
            var floor=origin.offset(x,-1,z);if(!world.hasChunkAt(floor) || !world.getWorldBorder().isWithinBounds(floor) || !(world.getBlockState(floor).is(BlockTags.DIRT) || world.getBlockState(floor).is(Blocks.GRASS_BLOCK)) || !world.getFluidState(floor).isEmpty())return false;
            for(int y=0;y<4;y++) {var pos=origin.offset(x,y,z);var state=world.getBlockState(pos);if(!world.hasChunkAt(pos) || state.hasBlockEntity() || !state.getFluidState().isEmpty() || !state.isAir() && !state.is(BlockTags.REPLACEABLE_BY_TREES))return false;}
        }return true;
    }
    public static boolean generate(WorldGenLevel world,BlockPos at,long seed) {
        if(!fits(world,at))return false;
        for(int x=-7;x<=7;x++)for(int z=-8;z<=10;z++)for(int y=0;y<4;y++)if(!world.getBlockState(at.offset(x,y,z)).isAir())world.setBlock(at.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
        world.setBlock(at,ModBlocks.HEARTH_TREE.get().defaultBlockState(),2);
        if(!(world.getBlockEntity(at) instanceof HearthTreeBlockEntity home) || !home.wild().configure(seed))return false;
        for(var pos:home.wild().timber())world.setBlock(pos,ModBlocks.GUAVA_LOG.get().defaultBlockState(),2);
        for(var pos:home.wild().fruit()) {
            world.setBlock(pos.above(),ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),2);
            world.setBlock(pos,ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(com.bunnidogs.mod.block.GuavaFruitBlock.AGE,com.bunnidogs.mod.block.GuavaFruitBlock.MAX_AGE),2);
        }
        for(var pos:home.orchard())world.setBlock(pos,ModBlocks.GUAVA_COPPICE.get().defaultBlockState().setValue(CoppiceBlock.AGE,7),2);
        for(var pos:home.wild().beds())world.setBlock(pos,ModBlocks.NEST.get().defaultBlockState(),2);
        for(var pos:home.wild().fields()) {
            world.setBlock(pos.below(),Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE,7),2);
            world.setBlock(pos,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),2);
        }
        world.setBlock(at.offset(5,-1,-1),Blocks.WATER.defaultBlockState(),2);
        world.setBlock(home.wild().nursery().below(),Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE,7),2);
        world.setBlock(home.wild().nursery().below().east(2),Blocks.WATER.defaultBlockState(),2);
        return true;
    }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {return generate(context.level(),context.origin(),context.random().nextLong());}
}
