package com.bunnidogs.mod.block;

import net.minecraft.core.BlockPos;
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

/**
 * The Trophy Pedestal (drops D8a block, D8b memory): right-click it with an item in your main hand to stand ONE of that item on it
 * (creative mode keeps your stack); right-click it again (any hand state) to take the item back into your inventory. Breaking the
 * pedestal pops the item out ({@link TrophyPedestalBlockEntity#preRemoveSideEffects}). Sneak-click with a block in hand still
 * places the block (vanilla skips the block's own use while sneaking with an item). Since D8c the item is drawn on top of the
 * pedestal (the D8b chat lines are gone). The model (a base, a column and a top plate) is data/ JSON; the outline is ONE box a little
 * narrower than the block (1 to 15 sixteenths), the same {@code Shapes.box} the guava blocks use ({@code noOcclusion} in ModBlocks).
 */
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

    /** Empty pedestal + item in hand: put one on. Item on the pedestal: take it back. Empty pedestal + empty hand: nothing. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TrophyPedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        ItemStack inHand = player.getMainHandItem();
        if (pedestal.isEmpty()) {
            if (inHand.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                pedestal.setItem(inHand.copyWithCount(1));
                if (!player.hasInfiniteMaterials()) {
                    inHand.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            ItemStack taken = pedestal.takeItem();
            if (!player.getInventory().add(taken)) {
                player.drop(taken, false);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
