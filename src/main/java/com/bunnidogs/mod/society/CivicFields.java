package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Grain/material farming is a profession; the worker need not personally eat the crop. */
public final class CivicFields {
    private CivicFields() {}
    public static BlockState plant(ItemStack seed) {
        if(seed.is(Items.WHEAT_SEEDS))return Blocks.WHEAT.defaultBlockState();if(seed.is(Items.BEETROOT_SEEDS))return Blocks.BEETROOTS.defaultBlockState();
        if(seed.is(Items.CARROT))return Blocks.CARROTS.defaultBlockState();if(seed.is(Items.POTATO))return Blocks.POTATOES.defaultBlockState();return null;
    }
    public static Item seed(BlockState crop) {return crop.is(Blocks.WHEAT) ? Items.WHEAT_SEEDS : crop.is(Blocks.BEETROOTS) ? Items.BEETROOT_SEEDS : crop.is(Blocks.CARROTS) ? Items.CARROT : crop.is(Blocks.POTATOES) ? Items.POTATO : null;}
    public static boolean plot(HearthTreeBlockEntity home,BlockPos crop) {return home.attached() && home.sites().contains(crop.below()) && home.getLevel().hasChunkAt(crop) && home.getLevel().getBlockState(crop.below()).is(Blocks.FARMLAND) && home.getLevel().getWorldBorder().isWithinBounds(crop) && !home.getLevel().hasNeighborSignal(crop.below());}
    public static int seedSlot(HearthTreeBlockEntity home) {
        // Keep one renewable grain line ahead of optional meal crops; planted crops retain their own seed type.
        for(var wanted:List.of(Items.WHEAT_SEEDS,Items.BEETROOT_SEEDS,Items.CARROT,Items.POTATO))for(int slot=0;slot<home.getContainerSize();slot++)if(home.getItem(slot).is(wanted) && CivicRecipes.ordinary(home.getItem(slot)))return slot;return -1;
    }
    public static boolean harvest(Bunnidog dog,HearthTreeBlockEntity home,BlockPos at,BlockState expected) {
        if(!plot(home,at) || seed(expected)==null || !(expected.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(expected))return false;
        var level=(ServerLevel)dog.level();
        if(!EventHooks.canEntityGrief(level,dog) || !CommonHooks.canEntityDestroy(level,at,dog) || EventHooks.onBlockPlace(dog,BlockSnapshot.create(level.dimension(),level,at),Direction.UP)
                || !dog.society().ready() || dog.society().hearth()!=home || !plot(home,at) || !level.getBlockState(at).equals(expected))return false;
        var drops=new ArrayList<>(Block.getDrops(expected,level,at,null,dog,ItemStack.EMPTY));if(drops.size()>16)return false;
        var sow=drops.stream().filter(stack->stack.is(seed(expected)) && CivicRecipes.ordinary(stack)).findFirst().orElse(null);
        if(sow!=null)sow.shrink(1);if(!level.setBlock(at,sow==null ? Blocks.AIR.defaultBlockState() : crop.getStateForAge(0),3))return false;
        scatter(home,at,drops);return true;
    }
    public static void scatter(HearthTreeBlockEntity home,BlockPos at,List<ItemStack> drops) {
        if(!home.attached())return;var level=(ServerLevel)home.getLevel();if(!level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.BLOCK_DROPS) || level.restoringBlockSnapshots)return;
        for(var stack:drops)if(!stack.isEmpty()) {
            var marked=stack.copy();marked.set(ModItems.FARM_ORIGIN.get(),home.site().toString());var item=new ItemEntity(level,at.getX()+.5,at.getY()+.25,at.getZ()+.5,marked);if(home.owner()!=null)item.setTarget(home.owner());item.setDefaultPickUpDelay();level.addFreshEntity(item);
        }
    }
    private static boolean nearWorkPlot(HearthTreeBlockEntity home,BlockPos item) {
        var level=home.getLevel();
        if(home.sites().stream().anyMatch(pos->pos.distSqr(item)<=9 && level.hasChunkAt(pos) && level.getBlockState(pos).is(Blocks.FARMLAND) && !level.hasNeighborSignal(pos)))return true;
        if(home.wild().natural() && java.util.stream.Stream.concat(home.wild().timber().stream(),home.wild().fruit().stream()).anyMatch(pos->pos.distSqr(item)<=9 && level.hasChunksAt(pos.offset(-1,-1,-1),pos.offset(1,1,1)) && !level.hasNeighborSignal(pos)))return true;
        return home.authorized() && home.orchard().stream().anyMatch(pos->pos.distSqr(item)<=9 && level.hasChunkAt(pos) && level.getBlockState(pos).is(ModBlocks.GUAVA_COPPICE.get()) && !level.hasNeighborSignal(pos));
    }
    public static boolean available(HearthTreeBlockEntity home,ItemEntity item) {
        return home.attached() && item!=null && item.isAlive() && !item.hasPickUpDelay() && !item.isOnFire() && !item.isPassenger() && !item.isVehicle() && CivicRecipes.ordinary(item.getItem())
                && home.site().toString().equals(item.getItem().get(ModItems.FARM_ORIGIN.get())) && Objects.equals(item.getTarget(),home.owner())
                && home.getLevel().hasChunkAt(item.blockPosition()) && nearWorkPlot(home,item.blockPosition());
    }
    public static List<ItemEntity> drops(HearthTreeBlockEntity home) {return !home.attached() ? List.of() : home.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(home.getBlockPos()).inflate(24),item->available(home,item)).stream().limit(16).toList();}
    private static final class Ground extends ItemStackResourceHandler {
        private final ItemEntity item;Ground(ItemEntity item) {this.item=item;}
        @Override protected ItemStack getStack() {return item.getItem();}
        @Override protected void setStack(ItemStack value) {item.setItem(value);}
    }
    public static boolean visible(Bunnidog dog,ItemEntity item) {
        var from=dog.blockPosition();var to=item.blockPosition();if(dog.level()!=item.level() || !dog.level().hasChunksAt(new BlockPos(Math.min(from.getX(),to.getX()),Math.min(from.getY(),to.getY()),Math.min(from.getZ(),to.getZ())),new BlockPos(Math.max(from.getX(),to.getX()),Math.max(from.getY(),to.getY()),Math.max(from.getZ(),to.getZ()))))return false;
        return dog.level().clip(new net.minecraft.world.level.ClipContext(dog.getEyePosition(),item.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,dog)).getType()==net.minecraft.world.phys.HitResult.Type.MISS;
    }
    public static ItemStack collect(Bunnidog dog,HearthTreeBlockEntity home,ItemEntity item) {
        if(!dog.society().gathers() || !available(home,item) || dog.distanceToSqr(item)>9 || !visible(dog,item) || !EventHooks.canEntityGrief((ServerLevel)dog.level(),dog) || !available(home,item) || dog.distanceToSqr(item)>9 || !visible(dog,item) || !dog.society().ready())return ItemStack.EMPTY;
        var exact=item.getItem().copyWithCount(Math.min(dog.society().gatheringLimit(),item.getItem().getCount()));
        try(var tx=Transaction.openRoot()) {
            if(new Ground(item).extract(ItemResource.of(exact),exact.getCount(),tx)!=exact.getCount() || !home.attached() || dog.society().hearth()!=home)return ItemStack.EMPTY;tx.commit();
        }
        dog.take(item,exact.getCount());if(item.getItem().isEmpty())item.discard();exact.remove(ModItems.FARM_ORIGIN.get());return exact;
    }
}
