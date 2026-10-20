package com.bunnidogs.mod.block;

import com.bunnidogs.mod.menu.CareMenu;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One actual displayed item. Held items fill an empty display; menus inspect/read/retrieve, and crouched empty use takes it back. */
public class TrophyPedestalBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 1.0D, 15.0D / 16.0D);

    public TrophyPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrophyPedestalBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if (stack.isEmpty()) return super.useItemOn(stack,state,level,pos,player,hand,hit);
        if (!(level.getBlockEntity(pos) instanceof TrophyPedestalBlockEntity pedestal)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (pedestal.isEmpty()) pedestal.placeFromPlayer(player,player.getItemInHand(hand));
            else CareMenu.open(player,pos);
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()
                || !(level.getBlockEntity(pos) instanceof TrophyPedestalBlockEntity pedestal)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) pedestal.returnToPlayer(player);
            else CareMenu.open(player,pos);
        }
        return InteractionResult.SUCCESS;
    }
}
