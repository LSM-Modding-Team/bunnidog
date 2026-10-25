package com.bunnidogs.mod.society;

import com.bunnidogs.mod.block.StationUse;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** One actual reusable installed Keystone. Breaking the Crucible returns that exact item. */
public final class CrucibleHeartBlockEntity extends BlockEntity {
    private ItemStack key=ItemStack.EMPTY;
    public CrucibleHeartBlockEntity(BlockPos p,BlockState s) {super(ModBlocks.CRUCIBLE_HEART_BE.get(),p,s);}
    public ItemStack key() {return key.copy();}
    public boolean ready() {return key.is(ModItems.LIVING_KEYSTONE.get());}
    public boolean install(Player p,ItemStack actual) {
        if(!StationUse.editable(p,this) || !StationUse.owns(p,actual) || ready() || !actual.is(ModItems.LIVING_KEYSTONE.get()))return false;
        key=actual.copyWithCount(1);actual.consume(1,p);setChanged();level.setBlock(worldPosition,getBlockState().setValue(com.bunnidogs.mod.block.ApexCrucibleBlock.ROOTED,true),3);return true;
    }
    @Override protected void saveAdditional(ValueOutput o) {super.saveAdditional(o);if(!key.isEmpty())o.store("LivingKeystone",ItemStack.CODEC,key);}
    @Override protected void loadAdditional(ValueInput i) {super.loadAdditional(i);key=i.read("LivingKeystone",ItemStack.CODEC).filter(s->s.is(ModItems.LIVING_KEYSTONE.get())).orElse(ItemStack.EMPTY);if(!key.isEmpty())key.setCount(1);}
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s) {super.preRemoveSideEffects(p,s);if(level instanceof net.minecraft.server.level.ServerLevel server && !key.isEmpty()) {Block.popResource(server,p,key);key=ItemStack.EMPTY;}}
}
