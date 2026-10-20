package com.bunnidogs.mod.block;

import java.util.Locale;
import com.bunnidogs.mod.entity.NurseryHatching;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

/** A visible, one-litter incubator with explicit hatching and ordinary native redstone. */
public final class NurseryCradleBlock extends Block implements EntityBlock, BonemealableBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty FILL = IntegerProperty.create("fill",0,4);
    public NurseryCradleBlock(Properties props) { super(props); registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(POWERED,false).setValue(FILL,0)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,POWERED,FILL); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()).setValue(POWERED,context.getLevel().hasNeighborSignal(context.getClickedPos())); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new NurseryCradleBlockEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() || type != ModBlocks.NURSERY_CRADLE_BE.get() ? null
                : (world,pos,current,entity) -> { if (entity instanceof NurseryCradleBlockEntity cradle) cradle.serverTick((ServerLevel)world); };
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) { return level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle ? cradle.stockSignal() : 0; }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos,state.setValue(POWERED,powered),2);
            if (powered) level.scheduleTick(pos,this,1);
        }
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle && !cradle.manualHatchOnly()) cradle.hatch();
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!player.mayBuild() || !(level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle)) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && stack.is(ItemTags.HOES)) {
            if (!level.isClientSide()) {
                cradle.setManualHatchOnly(!cradle.manualHatchOnly());
                player.sendOverlayMessage(Component.translatable(cradle.manualHatchOnly() ? "message.bunnidogs.cradle.manual_only" : "message.bunnidogs.cradle.redstone_enabled"));
            }
            return InteractionResult.SUCCESS;
        }
        boolean seed = stack.is(ModItems.SEED.get()), dust = stack.is(ModItems.STARLIT_DUST.get()), meal = stack.is(Items.BONE_MEAL);
        if (!seed && !dust && !meal) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!level.isClientSide()) {
            var actual = player.getItemInHand(hand);
            boolean changed = seed ? cradle.insertFromPlayer(player,actual) : cradle.grow(dust);
            if (changed) {
                if (!seed) actual.consume(1,player);
                if (!seed) level.levelEvent(2005,pos.above(),15);
            }
            player.sendOverlayMessage(status(cradle));
        }
        return InteractionResult.SUCCESS;
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle && cradle.seed().isPresent() && cradle.age() < SeedCropBlock.MAX_AGE;
    }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle) cradle.grow(false);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty() || !(level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle)) return InteractionResult.PASS;
        if (!player.isShiftKeyDown() && player.getOffhandItem().isEmpty()) {
            com.bunnidogs.mod.menu.CareMenu.open(player,pos); return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            if (player.mayBuild() && player.isShiftKeyDown() && player.getOffhandItem().isEmpty()) {
                var taken = cradle.takeSeed(); if (!player.getInventory().add(taken)) player.drop(taken,false);
                player.sendOverlayMessage(Component.translatable("message.bunnidogs.cradle.withdrawn"));
            } else if (player.mayBuild() && cradle.age() == SeedCropBlock.MAX_AGE && cradle.seed().isPresent()) {
                var result = cradle.hatch();
                player.sendOverlayMessage(Component.translatable("message.bunnidogs.cradle."+result.status().name().toLowerCase(Locale.ROOT),result.count()));
            } else player.sendOverlayMessage(status(cradle));
        }
        return InteractionResult.SUCCESS;
    }
    private static Component status(NurseryCradleBlockEntity cradle) {
        return cradle.storedSeed().isEmpty() ? Component.translatable("message.bunnidogs.cradle.empty")
                : cradle.seed().isEmpty() ? Component.translatable("message.bunnidogs.cradle.invalid")
                : cradle.age() == SeedCropBlock.MAX_AGE ? Component.translatable(cradle.manualHatchOnly() ? "message.bunnidogs.cradle.ready_manual" : "message.bunnidogs.cradle.ready")
                : Component.translatable("message.bunnidogs.cradle.growing",cradle.age(),(cradle.growthWait()+19)/20);
    }
}
