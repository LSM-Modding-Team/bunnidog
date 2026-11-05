package com.bunnidogs.mod.society;

import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Natural marker or paid communal stores beneath a visibly developing grafted tree. */
public final class HearthTreeBlock extends Block implements EntityBlock {
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,4);
    public static final BooleanProperty CACHED=BooleanProperty.create("cached");
    public HearthTreeBlock(Properties p) {super(p);registerDefaultState(stateDefinition.any().setValue(STAGE,0).setValue(CACHED,true));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {b.add(STAGE,CACHED);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s) {return new HearthTreeBlockEntity(p,s);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {return state.getValue(CACHED) ? Shapes.block() : Block.box(4,0,4,12,16,12);}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack) {if(placer instanceof Player p && level.getBlockEntity(pos) instanceof HearthTreeBlockEntity h)h.found(p);}
    private InteractionResult open(Level level,BlockPos pos,Player p) {
        if(level.getBlockEntity(pos) instanceof HearthTreeBlockEntity h && h.cached() && h.permitted(p)) {if(!level.isClientSide()) {p.openMenu(h);p.sendOverlayMessage(h.need());}return InteractionResult.SUCCESS;}
        return InteractionResult.FAIL;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player p,BlockHitResult hit) {return open(level,pos,p);}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit) {return stack.getItem() instanceof StewardStaffItem || stack.getItem() instanceof RootCacheItem ? InteractionResult.PASS : open(level,pos,p);}
}
