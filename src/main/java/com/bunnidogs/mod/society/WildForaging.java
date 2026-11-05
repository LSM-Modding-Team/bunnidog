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
    public static boolean usablePickaxe(ItemStack stack) {
        return CivicTools.usablePickaxe(stack);
    }
    public static int pickaxeSlot(HearthTreeBlockEntity home) {
        return CivicTools.pickaxeSlot(home);
    }
    private static boolean ready(Bunnidog dog,HearthTreeBlockEntity home,SocietyWork.Job job,ServerLevel level) {
        var pos=job.target();
        return home.wild().natural() && home.attached() && dog.society().hearth()==home && dog.society().harvestReadyAt(pos)
                && level.hasChunksAt(pos.offset(-1,-1,-1),pos.offset(1,1,1)) && level.getWorldBorder().isWithinBounds(pos)
                && job.expected().canSurvive(level,pos) && level.getBlockState(pos).equals(job.expected()) && !level.hasNeighborSignal(pos);
    }
    public static boolean harvest(Bunnidog dog,HearthTreeBlockEntity home,SocietyWork.Job job) {
        if(!(dog.level() instanceof ServerLevel level) || !ready(dog,home,job,level))return false;
        boolean wood=job.kind()==SocietyWork.Kind.CLIP_WOOD;
        if(wood ? !home.wild().timberAvailable(job.target()) || !job.expected().is(ModBlocks.GUAVA_LOG.get()) : !home.wild().fruit().contains(job.target()) || !job.expected().is(ModBlocks.GUAVA_FRUIT.get()) || job.expected().getValue(GuavaFruitBlock.AGE)!=GuavaFruitBlock.MAX_AGE)return false;
        if(!EventHooks.canEntityGrief(level,dog) || !CommonHooks.canEntityDestroy(level,job.target(),dog) || EventHooks.onBlockPlace(dog,BlockSnapshot.create(level.dimension(),level,job.target()),Direction.UP)
                || !ready(dog,home,job,level))return false;
        var drops=wood ? Block.getDrops(job.expected(),level,job.target(),null,dog,ItemStack.EMPTY) : List.of(new ItemStack(ModItems.GUAVA.get(),GuavaFruitBlock.fruitCount(job.expected())));
        if(!ready(dog,home,job,level) || wood && !home.wild().reserveTimber(job.target()))return false;
        boolean changed=false;
        try {
            if(!ready(dog,home,job,level))return false;
            changed=level.setBlock(job.target(),wood ? Blocks.AIR.defaultBlockState() : job.expected().setValue(GuavaFruitBlock.AGE,0),3);
            if(!changed)return false;CivicFields.scatter(home,job.target(),drops);return true;
        } finally {if(wood && !changed)home.wild().unreserveTimber(job.target());}
    }
}
