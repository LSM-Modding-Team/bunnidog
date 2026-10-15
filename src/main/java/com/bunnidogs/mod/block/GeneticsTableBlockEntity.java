package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.TableUse;
import com.bunnidogs.mod.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Tissue Samples on a {@link GeneticsTableBlock} (Genetics Table T1 and T2, docs/TABLE_SPECS.md): TWO slots, A and B
 * ({@link TableUse#SLOT_A}, {@link TableUse#SLOT_B}), each ONE item stack of size 1, saved with the whole item (components too, so the
 * sample keeps its genes) and sent to the client, exactly like the Trophy Pedestal's {@link TrophyPedestalBlockEntity} (the same pattern:
 * the client needs to know what is on the table to answer a click the way the server does). A table saved by T1 (one sample under the
 * key {@code Item}) loads that sample into slot A. When the table is broken or replaced both samples pop out.
 */
public class GeneticsTableBlockEntity extends BlockEntity {
    private static final String TAG_A = "SampleA";
    private static final String TAG_B = "SampleB";
    /** The key T1 saved its single sample under; read as slot A so an old table keeps its sample. */
    private static final String TAG_T1 = "Item";

    private ItemStack sampleA = ItemStack.EMPTY;
    private ItemStack sampleB = ItemStack.EMPTY;

    public GeneticsTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.GENETICS_TABLE_BE.get(), pos, state);
    }

    /** True if nothing stands on this slot ({@code TableUse.SLOT_A} or {@code SLOT_B}). */
    public boolean isEmpty(int slot) {
        return item(slot).isEmpty();
    }

    /** The sample on a slot (do not change it; use {@link #setItem} / {@link #takeItem}). */
    public ItemStack item(int slot) {
        return slot == TableUse.SLOT_B ? this.sampleB : this.sampleA;
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot == TableUse.SLOT_B) {
            this.sampleB = stack;
        } else {
            this.sampleA = stack;
        }
        this.changed();
    }

    /** Empties a slot and hands back what stood on it. */
    public ItemStack takeItem(int slot) {
        ItemStack out = item(slot);
        if (slot == TableUse.SLOT_B) {
            this.sampleB = ItemStack.EMPTY;
        } else {
            this.sampleA = ItemStack.EMPTY;
        }
        this.changed();
        return out;
    }

    /** Saves the chunk and tells the clients near it to fetch the new update tag. */
    private void changed() {
        this.setChanged();
        if (this.getLevel() != null && !this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        this.handleUpdateTag(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.sampleA.isEmpty()) {          // the codec refuses an empty stack
            output.store(TAG_A, ItemStack.CODEC, this.sampleA);
        }
        if (!this.sampleB.isEmpty()) {
            output.store(TAG_B, ItemStack.CODEC, this.sampleB);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.sampleA = input.read(TAG_A, ItemStack.CODEC).or(() -> input.read(TAG_T1, ItemStack.CODEC)).orElse(ItemStack.EMPTY);
        this.sampleB = input.read(TAG_B, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /** Called before the block entity goes away (the block broken or replaced): the samples pop out at the table's position. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            if (!this.sampleA.isEmpty()) {
                Block.popResource(serverLevel, pos, this.sampleA);
                this.sampleA = ItemStack.EMPTY;
            }
            if (!this.sampleB.isEmpty()) {
                Block.popResource(serverLevel, pos, this.sampleB);
                this.sampleB = ItemStack.EMPTY;
            }
        }
    }
}
