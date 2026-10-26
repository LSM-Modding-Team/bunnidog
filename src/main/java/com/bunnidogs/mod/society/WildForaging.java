package com.bunnidogs.mod.society;

import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.registry.*;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

/** Finite known fallen wood, renewable fruit, physical output; never general tree/player-build demolition. */
public final class WildForaging {
    private WildForaging() {}
    public static boolean harvest(Bunnidog dog,HearthTreeBlockEntity home,SocietyWork.Job job) {
        if(!home.wild().natural() || !(dog.level() instanceof ServerLevel level))return false;
        boolean wood=job.kind()==SocietyWork.Kind.CLIP_WOOD;
        if(wood ? !home.wild().timberAvailable(job.target()) || !job.expected().is(ModBlocks.GUAVA_LOG.get()) : !home.wild().fruit().contains(job.target()) || !job.expected().is(ModBlocks.GUAVA_FRUIT.get()) || job.expected().getValue(GuavaFruitBlock.AGE)!=GuavaFruitBlock.MAX_AGE)return false;
        if(!level.hasChunksAt(job.target().offset(-1,-1,-1),job.target().offset(1,1,1)) || !job.expected().canSurvive(level,job.target()) || level.hasNeighborSignal(job.target()) || !EventHooks.canEntityGrief(level,dog) || !CommonHooks.canEntityDestroy(level,job.target(),dog) || EventHooks.onBlockPlace(dog,BlockSnapshot.create(level.dimension(),level,job.target()),Direction.UP)
                || !home.attached() || dog.society().hearth()!=home || !dog.society().ready() || !level.getBlockState(job.target()).equals(job.expected()) || level.hasNeighborSignal(job.target()))return false;
        var drops=wood ? Block.getDrops(job.expected(),level,job.target(),null,dog,ItemStack.EMPTY) : List.of(new ItemStack(ModItems.GUAVA.get()));
        if(!level.setBlock(job.target(),wood ? Blocks.AIR.defaultBlockState() : job.expected().setValue(GuavaFruitBlock.AGE,0),3))return false;
        if(wood)home.wild().timberUsed(job.target());CivicFields.scatter(home,job.target(),drops);return true;
    }
}
