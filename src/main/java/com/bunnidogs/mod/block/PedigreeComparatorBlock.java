package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.PedigreeSignal;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.menu.CareMenu;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A sample-programmed live gene detector, with exact-match or seven-pair analog output. */
public final class PedigreeComparatorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty ANALOG = BooleanProperty.create("analog");
    public static final BooleanProperty REFERENCE = BooleanProperty.create("reference");
    private static final VoxelShape SHAPE = Shapes.box(.125,0,.125,.875,.875,.875);
    public PedigreeComparatorBlock(Properties props) {
        super(props); registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH)
                .setValue(POWER,0).setValue(ANALOG,false).setValue(REFERENCE,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,POWER,ANALOG,REFERENCE); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PedigreeComparatorBlockEntity(pos,state); }
    @Override protected boolean isSignalSource(BlockState state) { return true; }
    @Override protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) { return state.getValue(POWER); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction side) { return state.getValue(POWER); }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        if (!level.isClientSide() && !previous.is(this)) level.scheduleTick(pos,this,1);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof PedigreeComparatorBlockEntity detector) {
            detector.refresh();
            if (level.getBlockEntity(pos) == detector) level.scheduleTick(pos,this,PedigreeSignal.POLL_TICKS);
        }
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PedigreeComparatorBlockEntity detector)) return InteractionResult.PASS;
        var sample = TissueSampleItem.read(player.getItemInHand(hand));
        if (sample.isEmpty()) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!level.isClientSide()) {
            detector.copyFromPlayer(player,player.getItemInHand(hand));
            inspect(level,pos,player);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()
                || !(level.getBlockEntity(pos) instanceof PedigreeComparatorBlockEntity detector)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) { detector.clearFromPlayer(player); inspect(level,pos,player); }
            else CareMenu.open(player,pos);
        }
        return InteractionResult.SUCCESS;
    }
    public static void inspect(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel) || !(level.getBlockEntity(pos) instanceof PedigreeComparatorBlockEntity detector)) return;
        var reading = detector.read();
        var mode = Component.translatable(detector.getBlockState().getValue(ANALOG) ? "message.bunnidogs.detector.analog" : "message.bunnidogs.detector.exact");
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.detector.status",mode,
                Component.translatable("message.bunnidogs.detector."+reading.status().name().toLowerCase(java.util.Locale.ROOT),reading.pairs()),
                detector.getBlockState().getValue(POWER)));
        detector.reference().ifPresent(sample -> player.sendSystemMessage(Component.translatable("message.bunnidogs.detector.reference",sample.genome().toString())));
    }
}
