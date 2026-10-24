package com.bunnidogs.mod.block;

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
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * What stands on a {@link TrophyPedestalBlock} (drops D8b): ONE item stack of size 1, saved with the whole item (components too,
 * so a Tissue Sample keeps its genes) the way NeoForge's own test block entity does it ({@code output.store(key, ItemStack.CODEC, ...)} /
 * {@code input.read(key, ItemStack.CODEC)}). Since D8c the item is also sent to the client (the update tag / packet trio of that same
 * test block entity) so {@link com.bunnidogs.mod.client.renderer.TrophyPedestalRenderer} can draw it; a change calls
 * {@code sendBlockUpdated}. When the pedestal is broken or replaced the item pops out.
 */
public class TrophyPedestalBlockEntity extends BlockEntity {
    private static final String TAG_ITEM = "Item";

    private ItemStack held = ItemStack.EMPTY;
    /** Ritual-only journal; no new hopper capability or secondary saved inventory. */
    private final ResourceHandler<ItemResource> ritualStock=new ItemStackResourceHandler() {
        @Override protected ItemStack getStack() { return held; }
        @Override protected void setStack(ItemStack stack) { held=stack; }
        @Override protected int getCapacity(ItemResource resource) { return 1; }
        @Override protected void onRootCommit(ItemStack previous) { changed(); }
    };
    public ResourceHandler<ItemResource> ritualStock() { return ritualStock; }
    public boolean placeFromPlayer(Player player,ItemStack actual) {
        if (!StationUse.editable(player,this) || !StationUse.owns(player,actual) || !held.isEmpty()) return false;
        var stored=actual.copyWithCount(1); actual.consume(1,player); setItem(stored); return true;
    }
    public boolean returnToPlayer(Player player) {
        if (!StationUse.editable(player,this) || held.isEmpty()) return false;
        StationUse.give(player,takeItem()); return true;
    }

    public TrophyPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TROPHY_PEDESTAL_BE.get(), pos, state);
    }

    public boolean isEmpty() {
        return this.held.isEmpty();
    }

    /** The item on the pedestal (do not change it; use {@link #setItem} / {@link #takeItem}). */
    public ItemStack item() {
        return this.held;
    }

    public void setItem(ItemStack stack) {
        this.held = stack;
        this.changed();
    }

    /** Empties the pedestal and hands back what stood on it. */
    public ItemStack takeItem() {
        ItemStack out = this.held;
        this.held = ItemStack.EMPTY;
        this.changed();
        return out;
    }

    /** Saves the chunk and tells the clients near it to fetch the new update tag (they redraw the item). */
    private void changed() {
        if (StationInventoryUpdates.defer(this,this::changed)) return;
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
        if (!this.held.isEmpty()) {         // the codec refuses an empty stack
            output.store(TAG_ITEM, ItemStack.CODEC, this.held);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.held = input.read(TAG_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /** Called before the block entity goes away (the block broken or replaced): the item pops out above the pedestal's position. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.getLevel() instanceof ServerLevel serverLevel && !this.held.isEmpty()) {
            Block.popResource(serverLevel, pos, this.held);
            this.held = ItemStack.EMPTY;
        }
    }
}
