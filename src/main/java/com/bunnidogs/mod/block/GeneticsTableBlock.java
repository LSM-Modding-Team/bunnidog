package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.TableUse;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.item.TablePlanning;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.menu.CareMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
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
 * The Genetics Table (T1 and T2, docs/TABLE_SPECS.md): a lab bench that keeps two Tissue Samples, A and B, and reads them. What a click does
 * is decided by the pure {@link TableUse#decide} (and {@link TableUse#slot}, which slot it works on): a readable sample in hand goes on the
 * first free slot (or swaps with B when full), empty hands open status and named controls, and a held planning cue reads the existing sheet.
 * Sneaking with both hands empty takes the last sample back.
 * Sneaking with an item in hand never reaches a block (vanilla), so a swap or a store while sneaking is not possible; that is fine.
 * Native item interactions use the actual hand; the empty-hand panel/withdrawal path requires both hands empty.
 */
public class GeneticsTableBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 13.0D / 16.0D, 1.0D);

    public GeneticsTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneticsTableBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof GeneticsTableBlockEntity table ? table.stockSignal() : 0;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return stack.isEmpty() ? super.useItemOn(stack,state,level,pos,player,hand,hit)
                : interact(level,pos,player,player.getItemInHand(hand));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Let a real off-hand item reach useItemOn rather than reading/taking prematurely from the empty main hand.
        if (player.getMainHandItem().isEmpty() && !player.getOffhandItem().isEmpty()) return InteractionResult.PASS;
        if (player.getMainHandItem().isEmpty() && !player.isShiftKeyDown()) {
            CareMenu.open(player,pos); return InteractionResult.SUCCESS;
        }
        return interact(level,pos,player,player.getMainHandItem());
    }

    private InteractionResult interact(Level level, BlockPos pos, Player player, ItemStack inHand) {
        if (!(level.getBlockEntity(pos) instanceof GeneticsTableBlockEntity table)) {
            return InteractionResult.PASS;
        }
        boolean readable = TissueSampleItem.read(inHand).isPresent();
        boolean aFull = !table.isEmpty(TableUse.SLOT_A);
        boolean bFull = !table.isEmpty(TableUse.SLOT_B);
        TableUse.Action action = TableUse.decide(readable, player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty(),
                player.isShiftKeyDown(), aFull, bFull);
        if (action == TableUse.Action.NOTHING) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;                       // the client only needs to know the click was used
        }
        if (action!=TableUse.Action.READ && (!player.mayBuild() || !level.mayInteract(player,pos))) return InteractionResult.PASS;
        int slot = TableUse.slot(action, aFull, bFull);
        switch (action) {
            case STORE, SWAP -> table.placeFromPlayer(player,inHand,true);
            case TAKE -> table.returnToPlayer(player,slot);
            case READ -> {
                TableSheet sheet = TablePlanning.snapshot(table,player);
                if (sheet.spreadCount() > 0 && player instanceof ServerPlayer serverPlayer) {
                    ModNetwork.sendTable(serverPlayer, sheet);
                }
            }
            default -> { }
        }
        level.playSound(null,pos,SoundEvents.WOODEN_BUTTON_CLICK_ON,SoundSource.BLOCKS,0.25F,1.2F);
        return InteractionResult.SUCCESS;
    }

}
