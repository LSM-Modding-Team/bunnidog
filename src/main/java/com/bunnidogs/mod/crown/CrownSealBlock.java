package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Physical encounter stones. No tutorial screen, quest UI, or automatic encounter on approach. */
public final class CrownSealBlock extends BaseEntityBlock {
    public static final IntegerProperty WING=IntegerProperty.create("wing",0,3),STATE=IntegerProperty.create("state",0,2),SOCKETS=IntegerProperty.create("sockets",0,7);
    public static final MapCodec<CrownSealBlock> CODEC=simpleCodec(CrownSealBlock::new);
    public CrownSealBlock(Properties p) {super(p);registerDefaultState(stateDefinition.any().setValue(WING,0).setValue(STATE,0).setValue(SOCKETS,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec() {return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {b.add(WING,STATE,SOCKETS);}
    @Override protected RenderShape getRenderShape(BlockState s) {return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s) {return new CrownSealBlockEntity(p,s);}
    @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.entity.Entity e) {return false;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type) {
        return l.isClientSide() ? null : createTickerHelper(type,ModBlocks.CROWN_SEAL_BE.get(),(world,pos,state,be)->be.tick());
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit) {return use(l,p,player,InteractionHand.MAIN_HAND);}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit) {return player.isShiftKeyDown() ? InteractionResult.PASS : use(l,p,player,hand);}
    private InteractionResult use(Level l,BlockPos p,Player player,InteractionHand hand) {
        if(!l.isClientSide() && l.getBlockEntity(p) instanceof CrownSealBlockEntity seal)seal.use(player,hand);
        return InteractionResult.SUCCESS;
    }
}
