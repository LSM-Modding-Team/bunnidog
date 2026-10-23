package com.bunnidogs.mod.block;

import java.util.Locale;
import com.bunnidogs.mod.entity.PenBreeding;
import com.bunnidogs.mod.item.GuavaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The pen's two-place manger: deliberate manual feeding or one attempt per rising redstone edge. */
public final class BreedingPenBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty FILL = IntegerProperty.create("fill",0,3);
    private static final VoxelShape SHAPE = Shapes.box(.0625,0,.125,.9375,1,.875);
    public BreedingPenBlock(Properties props) { super(props); registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(POWERED,false).setValue(FILL,0)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,POWERED,FILL); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()).setValue(POWERED,context.getLevel().hasNeighborSignal(context.getClickedPos())); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BreedingPenBlockEntity(pos,state); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction side) { return level.getBlockEntity(pos) instanceof BreedingPenBlockEntity pen ? pen.stockSignal() : 0; }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean moved) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) { level.setBlock(pos,state.setValue(POWERED,powered),2); if (powered) level.scheduleTick(pos,this,1); }
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof BreedingPenBlockEntity pen) pen.activate(true);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!(level.getBlockEntity(pos) instanceof BreedingPenBlockEntity pen)) return InteractionResult.PASS;
        var inspection=StationUse.openHeld(player,pos,hand);
        if (inspection.consumesAction()) return inspection;
        if (!level.isClientSide()) {
            if (player.mayBuild() && stack.getItem() instanceof GuavaItem) {
                var right = state.getValue(FACING).getCounterClockWise();
                double side = (hit.getLocation().x-pos.getX()-.5)*right.getStepX()+(hit.getLocation().z-pos.getZ()-.5)*right.getStepZ();
                pen.insertFromHand(player,player.getItemInHand(hand),side <= 0 ? 0 : 1);
            }
            inspect(level,pos,player);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty() || !(level.getBlockEntity(pos) instanceof BreedingPenBlockEntity pen)) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) { com.bunnidogs.mod.menu.CareMenu.open(player,pos); return InteractionResult.SUCCESS; }
        if (!level.isClientSide()) {
            if (player.mayBuild() && player.isShiftKeyDown()) { give(player,pen.take(0)); give(player,pen.take(1)); }
            else if (player.mayBuild()) player.sendOverlayMessage(status(pen.activate(false)));
            else inspect(level,pos,player);
        }
        return InteractionResult.SUCCESS;
    }
    public static void inspect(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel server) || !(level.getBlockEntity(pos) instanceof BreedingPenBlockEntity pen)) return;
        player.sendOverlayMessage(status(pen.status()));
        var check = PenBreeding.inspect(server,pos,pen.getBlockState().getValue(FACING));
        if (check.parents().size() == 2) {
            var a = check.parents().get(0); var b = check.parents().get(1);
            player.sendSystemMessage(Component.translatable("message.bunnidogs.pen.pair",a.earTag().orElse(a.getUUID().toString().substring(0,8)),
                    pen.food(0).getHoverName(),pen.food(0).getCount(),b.earTag().orElse(b.getUUID().toString().substring(0,8)),pen.food(1).getHoverName(),pen.food(1).getCount()));
        }
    }
    private static Component status(com.bunnidogs.mod.genetics.Pen.Status status) { return Component.translatable("message.bunnidogs.pen."+(status == com.bunnidogs.mod.genetics.Pen.Status.PAIR ? "need_pair" : status.name().toLowerCase(Locale.ROOT))); }
    private static void give(Player player, ItemStack stack) { if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack,false); }
}
