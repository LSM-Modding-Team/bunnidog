package com.bunnidogs.mod.crown;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The closed gate also accepts its keys from inside the arena, through the same authoritative seal. */
public class CrownGateBlock extends net.minecraft.world.level.block.Block {
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty KIND=net.minecraft.world.level.block.state.properties.IntegerProperty.create("gate_kind",0,2);
    public CrownGateBlock(Properties p) {super(p);registerDefaultState(stateDefinition.any().setValue(KIND,0));}
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b) {b.add(KIND);}
    @Override public boolean canEntityDestroy(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.entity.Entity entity) {return false;}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit) {return p.isShiftKeyDown() ? InteractionResult.PASS : use(level,pos,p,hand);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player p,BlockHitResult hit) {return use(level,pos,p,InteractionHand.MAIN_HAND);}
    private InteractionResult use(Level level,BlockPos pos,Player p,InteractionHand hand) {
        if(level.isClientSide())return InteractionResult.SUCCESS;
        int kind=level.getBlockState(pos).getValue(KIND);
        // Known authored offsets, followed by controller-owned contact validation. No nearest-site search.
        int reach=kind==0 ? 6 : 3;
        for(int x=-reach;x<=reach;x++)for(int y=-2;y<=8;y++) {
            var at=pos.offset(x+(kind==1 ? -31 : kind==2 ? 31 : 0),-y,kind==0 ? 4 : level.getBlockState(pos).is(com.bunnidogs.mod.registry.ModBlocks.CROWN_SOCKET.get()) ? -18 : -17);
            if(level.hasChunkAt(at) && level.getBlockEntity(at) instanceof CrownSealBlockEntity seal && seal.wing()==3 && seal.controls(pos)) {seal.useGate(p,hand,pos,kind);return InteractionResult.SUCCESS;}
        }
        return InteractionResult.PASS;
    }
}
