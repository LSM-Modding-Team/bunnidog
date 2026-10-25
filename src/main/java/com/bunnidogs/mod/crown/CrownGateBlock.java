package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.realm.RootstoneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The closed gate also accepts its keys from inside the arena, through the same authoritative seal. */
public final class CrownGateBlock extends net.minecraft.world.level.block.Block {
    public CrownGateBlock(Properties p) {super(p);}
    @Override public boolean canEntityDestroy(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.entity.Entity entity) {return false;}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit) {return p.isShiftKeyDown() ? InteractionResult.PASS : use(level,pos,p,hand);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player p,BlockHitResult hit) {return use(level,pos,p,InteractionHand.MAIN_HAND);}
    private InteractionResult use(Level level,BlockPos pos,Player p,InteractionHand hand) {
        if(level.isClientSide())return InteractionResult.SUCCESS;
        for(int x=-3;x<=3;x++)for(int y=0;y<=5;y++) {
            var at=pos.offset(x,-y,4);
            if(level.hasChunkAt(at) && level.getBlockEntity(at) instanceof CrownSealBlockEntity seal && seal.wing()==3 && seal.center().offset(0,1,46).equals(at)) {seal.use(p,hand);return InteractionResult.SUCCESS;}
        }
        return InteractionResult.PASS;
    }
}
