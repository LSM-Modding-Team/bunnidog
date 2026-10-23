package com.bunnidogs.mod.block;

import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A small visible screw press; powered stations pause, while hands and hoppers can still move stock. */
public final class JuicerBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WORKING = BooleanProperty.create("working");
    private static final VoxelShape SHAPE = Shapes.box(.0625,0,.0625,.9375,1,.9375);
    public JuicerBlock(Properties props) { super(props); registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WORKING,false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,WORKING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new JuicerBlockEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() || type != ModBlocks.JUICER_BE.get() ? null
                : (world,pos,current,entity) -> { if (entity instanceof JuicerBlockEntity juicer) juicer.serverTick((ServerLevel)world); };
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof JuicerBlockEntity juicer ? juicer.stockSignal() : 0;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!(level.getBlockEntity(pos) instanceof JuicerBlockEntity juicer)) return InteractionResult.PASS;
        var inspection=StationUse.openHeld(player,pos,hand,entry->entry.is(net.minecraft.world.item.Items.GLASS_BOTTLE));
        if (inspection.consumesAction()) return inspection;
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            int inserted = juicer.insertFromHand(player,player.getItemInHand(hand));
            player.sendOverlayMessage(Component.translatable(inserted > 0 ? "message.bunnidogs.juicer.inserted" : "message.bunnidogs.juicer.inputs",inserted));
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof JuicerBlockEntity juicer)) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) { com.bunnidogs.mod.menu.CareMenu.open(player,pos); return InteractionResult.SUCCESS; }
        if (!level.isClientSide()) {
            if (player.mayBuild()) {
                if (player.isShiftKeyDown()) { give(player,juicer.take(1)); give(player,juicer.take(0)); }
                else give(player,juicer.take(2));
            }
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.juicer.status",juicer.item(0).getCount(),juicer.item(1).getCount(),juicer.item(2).getCount(),juicer.progress()*100/JuicerBlockEntity.PROCESS_TICKS,
                    level.hasNeighborSignal(pos) ? "powered: paused" : juicer.outputBlocked() ? "output blocked" : "ready"));
        }
        return InteractionResult.SUCCESS;
    }
    private static void give(Player player, ItemStack stack) { if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack,false); }
}
