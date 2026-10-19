package com.bunnidogs.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A visible larder; a hoe opts nearby residents into tending matching crops. */
public final class FeedingTroughBlock extends Block implements EntityBlock {
    public static final IntegerProperty FILL = IntegerProperty.create("fill",0,3);
    public static final BooleanProperty TENDING = BooleanProperty.create("tending");
    private static final VoxelShape SHAPE = Shapes.or(Shapes.box(.0625,0,.25,.9375,.125,.75),
            Shapes.box(.0625,.125,.25,.9375,.4375,.375),Shapes.box(.0625,.125,.625,.9375,.4375,.75),
            Shapes.box(.0625,.125,.375,.1875,.4375,.625),Shapes.box(.8125,.125,.375,.9375,.4375,.625));
    public FeedingTroughBlock(Properties props) { super(props); this.registerDefaultState(this.stateDefinition.any().setValue(FILL,0).setValue(TENDING,false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FILL,TENDING); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FeedingTroughBlockEntity(pos,state); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough ? trough.stockSignal() : 0;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (stack.is(ItemTags.HOES)) {
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
                    trough.setWorkMode(trough.workMode().next());
                    level.setBlock(pos,level.getBlockState(pos).setValue(TENDING,true),3);
                    player.sendOverlayMessage(Component.translatable("message.bunnidogs.trough.mode_set",
                            Component.translatable("work.bunnidogs.mode."+trough.workMode().key())));
                    return InteractionResult.SUCCESS;
                }
                boolean tending = !state.getValue(TENDING);
                level.setBlock(pos,state.setValue(TENDING,tending),3);
                player.sendOverlayMessage(Component.translatable("message.bunnidogs.trough.tending." + (tending ? "on" : "off")));
            }
            return InteractionResult.SUCCESS;
        }
        if (!FeedingTroughBlockEntity.accepts(stack)) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            if (player.isShiftKeyDown()) {
                player.sendOverlayMessage(trough.setFilter(stack)
                        ? Component.translatable("message.bunnidogs.trough.filter_set",stack.getHoverName())
                        : Component.translatable("message.bunnidogs.trough.filter_conflict"));
                return InteractionResult.SUCCESS;
            }
            int added = trough.insert(stack);
            if (!player.hasInfiniteMaterials()) stack.shrink(added);
            player.sendOverlayMessage(withFilter(trough,Component.translatable("message.bunnidogs.trough.added",added,trough.food().getCount())));
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
            if (player.isShiftKeyDown() && player.getOffhandItem().isEmpty() && player.mayBuild()) {
                if (trough.food().isEmpty() && !trough.filter().isEmpty()) {
                    trough.clearFilter();
                    player.sendOverlayMessage(Component.translatable("message.bunnidogs.trough.filter_cleared"));
                    return InteractionResult.SUCCESS;
                }
                ItemStack taken = trough.takeAll();
                if (!player.getInventory().add(taken)) player.drop(taken,false);
                if (!trough.filter().isEmpty()) player.sendOverlayMessage(Component.translatable("message.bunnidogs.trough.filtered",trough.filter().getHoverName()));
            } else player.sendOverlayMessage(withFilter(trough,trough.food().isEmpty() ? Component.translatable("message.bunnidogs.trough.empty")
                    : Component.translatable("message.bunnidogs.trough.stock",trough.food().getCount(),trough.food().getHoverName())));
        }
        return InteractionResult.SUCCESS;
    }
    private static Component withFilter(FeedingTroughBlockEntity trough, Component stock) {
        Component detail = trough.filter().isEmpty() ? stock
                : Component.translatable("message.bunnidogs.trough.stock_filter",stock,trough.filter().getHoverName());
        detail = Component.translatable("message.bunnidogs.trough.work",detail,
                Component.translatable("work.bunnidogs.mode."+trough.workMode().key()),
                Component.translatable(trough.getBlockState().getValue(TENDING) ? "work.bunnidogs.enabled" : "work.bunnidogs.disabled"));
        return trough.residentsPaused() ? Component.translatable("message.bunnidogs.trough.powered",detail) : detail;
    }
}
