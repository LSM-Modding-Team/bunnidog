package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.menu.CareMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A museum jar for sampled lines, with native full-sheet reading and a two-copy comparator signal. */
public final class SpecimenJarBlock extends Block implements EntityBlock {
    public static final IntegerProperty SPECIMEN = IntegerProperty.create("specimen",0,Gene.COAT.alleles().size());
    private static final VoxelShape SHAPE = Shapes.box(.1875,0,.1875,.8125,.875,.8125);
    public SpecimenJarBlock(Properties props) { super(props); registerDefaultState(this.stateDefinition.any().setValue(SPECIMEN,0)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(SPECIMEN); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SpecimenJarBlockEntity(pos,state); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar ? jar.fixedSignal() : 0;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!(level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (StationUse.editable(player,jar) && TissueSampleItem.read(stack).isPresent() && jar.sample().isEmpty())
                jar.insertFromHand(player,player.getItemInHand(hand));
            else read(jar,player);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof SpecimenJarBlockEntity jar)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) jar.returnToPlayer(player);
            else CareMenu.open(player,pos);
        }
        return InteractionResult.SUCCESS;
    }
    private static void read(SpecimenJarBlockEntity jar, Player player) {
        var sample = TissueSampleItem.read(jar.sample());
        if (sample.isPresent() && player instanceof ServerPlayer serverPlayer) ModNetwork.sendSample(serverPlayer,sample.get());
        else player.sendOverlayMessage(Component.translatable("message.bunnidogs.jar.empty"));
    }
}
