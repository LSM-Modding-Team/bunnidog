package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.Commissions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** An opt-in inspection station. Native career/rewards remain separate from the Pen job site. */
public final class CommissionBoardBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty READY=BooleanProperty.create("ready");
    private static final VoxelShape NORTH=Shapes.or(Shapes.box(.125,0,.375,.25,.5,.625),Shapes.box(.75,0,.375,.875,.5,.625),
            Shapes.box(.0625,.375,.3125,.9375,1,.6875));
    private static final VoxelShape EAST=Shapes.or(Shapes.box(.375,0,.125,.625,.5,.25),Shapes.box(.375,0,.75,.625,.5,.875),
            Shapes.box(.3125,.375,.0625,.6875,1,.9375));
    public CommissionBoardBlock(Properties properties) {
        super(properties); registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(READY,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,READY); }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return state.getValue(FACING).getAxis()==Direction.Axis.Z ? NORTH : EAST;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state,Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state,Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new CommissionBoardBlockEntity(pos,state); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state,Level level,BlockPos pos,Direction side) { return state.getValue(READY) ? 15 : 0; }
    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean moved) {
        if (!level.isClientSide() && !previous.is(this)) level.scheduleTick(pos,this,1);
    }
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CommissionBoardBlockEntity board) {
            board.refresh();
            if (level.getBlockEntity(pos)==board) level.scheduleTick(pos,this,Commissions.POLL_TICKS);
        }
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if (stack.isEmpty()) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!(level.getBlockEntity(pos) instanceof CommissionBoardBlockEntity board)) return InteractionResult.PASS;
        if (!stack.is(Items.PAPER)) return StationUse.openHeld(player,pos,hand,entry->entry.is(Items.PAPER));
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!level.isClientSide()) board.submit(player,hand);
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()
                || !(level.getBlockEntity(pos) instanceof CommissionBoardBlockEntity board)) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) { com.bunnidogs.mod.menu.CareMenu.open(player,pos); return InteractionResult.SUCCESS; }
        if (!level.isClientSide()) {
            if (player.mayBuild() && !player.isShiftKeyDown()) board.cycle();
            board.inspect(player);
        }
        return InteractionResult.SUCCESS;
    }
}
