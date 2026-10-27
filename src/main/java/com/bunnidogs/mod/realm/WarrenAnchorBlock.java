package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.item.ApexCrestItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;

/** A visible eye socket, never a tutorial menu. Attunement preserves the presented source Crest. */
public final class WarrenAnchorBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty MARKS=net.minecraft.world.level.block.state.properties.IntegerProperty.create("marks",0,63);
    public static final BooleanProperty ACTIVE=BooleanProperty.create("active"),RETURN=BooleanProperty.create("return");
    public static final MapCodec<WarrenAnchorBlock> CODEC=simpleCodec(WarrenAnchorBlock::new);
    public WarrenAnchorBlock(Properties properties) {super(properties);registerDefaultState(stateDefinition.any().setValue(ACTIVE,false).setValue(RETURN,false).setValue(MARKS,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec() {return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder) {builder.add(ACTIVE,RETURN,MARKS);}
    @Override protected RenderShape getRenderShape(BlockState state) {return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) {return new WarrenAnchorBlockEntity(pos,state);}
    @Override public boolean canEntityDestroy(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.entity.Entity entity) {return false;}
    @Override protected float getDestroyProgress(BlockState state,Player player,net.minecraft.world.level.BlockGetter level,BlockPos pos) {
        return state.getValue(RETURN) ? 0 : super.getDestroyProgress(state,player,level,pos);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(level.isClientSide())return InteractionResult.SUCCESS;
        if(level.getBlockEntity(pos) instanceof WarrenAnchorBlockEntity anchor) {
            if(anchor.linked())anchor.cross(player);
            else player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.bunnidogs.realm.socket"));
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(state.getValue(ACTIVE))return useWithoutItem(state,level,pos,player,hit);
        if(stack.is(ModItems.APEX_CREST.get()) && player.getItemInHand(hand==InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).is(ModItems.RESONANT_EYE.get())) {
            if(!level.isClientSide() && level.getBlockEntity(pos) instanceof WarrenAnchorBlockEntity anchor)anchor.attune(player,hand==InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            return InteractionResult.SUCCESS;
        }
        if(!stack.is(ModItems.RESONANT_EYE.get()))return InteractionResult.PASS;
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof WarrenAnchorBlockEntity anchor)anchor.attune(player,hand);
        return InteractionResult.SUCCESS;
    }
}
