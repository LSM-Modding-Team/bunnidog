package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.TableUse;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.network.ModNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
 * The Genetics Table (T1 and T2, docs/TABLE_SPECS.md): a lab bench that keeps two Tissue Samples, A and B, and reads them. What a click does
 * is decided by the pure {@link TableUse#decide} (and {@link TableUse#slot}, which slot it works on): a readable sample in hand goes on the
 * first free slot (or swaps with the one on B when both are full), any other click reads what is on the table (the table window opens on
 * that player's screen: sample A's sheet, then sample B's, turned with Previous / Next), and sneaking with both hands empty takes the last one back.
 * Sneaking with an item in hand never reaches a block (vanilla), so a swap or a store while sneaking is not possible; that is fine.
 * Same shape as {@link TrophyPedestalBlock}: {@code useWithoutItem} runs whatever is in the hand because the block has no item use of its own.
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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof GeneticsTableBlockEntity table)) {
            return InteractionResult.PASS;
        }
        ItemStack inHand = player.getMainHandItem();
        boolean readable = TissueSampleItem.read(inHand).isPresent();
        boolean aFull = !table.isEmpty(TableUse.SLOT_A);
        boolean bFull = !table.isEmpty(TableUse.SLOT_B);
        TableUse.Action action = TableUse.decide(readable, inHand.isEmpty(), player.isShiftKeyDown(), aFull, bFull);
        if (action == TableUse.Action.NOTHING) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;                       // the client only needs to know the click was used
        }
        int slot = TableUse.slot(action, aFull, bFull);
        switch (action) {
            case STORE -> store(player, inHand, table, slot);
            case SWAP -> {
                giveBack(player, table.takeItem(slot));
                store(player, inHand, table, slot);
            }
            case TAKE -> giveBack(player, table.takeItem(slot));
            case READ -> {
                TableSheet sheet = new TableSheet(TissueSampleItem.read(table.item(TableUse.SLOT_A)), TissueSampleItem.read(table.item(TableUse.SLOT_B)));
                if (sheet.spreadCount() > 0 && player instanceof ServerPlayer serverPlayer) {
                    ModNetwork.sendTable(serverPlayer, sheet);
                }
            }
            default -> { }
        }
        return InteractionResult.SUCCESS;
    }

    /** Stands ONE of the sample in hand on a slot of the table (creative mode keeps the stack). */
    private static void store(Player player, ItemStack inHand, GeneticsTableBlockEntity table, int slot) {
        table.setItem(slot, inHand.copyWithCount(1));
        if (!player.hasInfiniteMaterials()) {
            inHand.shrink(1);
        }
    }

    /** Hands a stack to the player, dropped at their feet if the inventory is full. */
    private static void giveBack(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
