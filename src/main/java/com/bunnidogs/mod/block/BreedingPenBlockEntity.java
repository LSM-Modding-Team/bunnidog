package com.bunnidogs.mod.block;

import com.bunnidogs.mod.entity.PenBreeding;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Pen;
import com.bunnidogs.mod.item.GuavaItem;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** The planned Breeding Pen's two mangers. Feeding is paid; normal animals still do the mating. */
public final class BreedingPenBlockEntity extends BlockEntity {
    private final ItemStack[] food = {ItemStack.EMPTY,ItemStack.EMPTY};
    private boolean feeding;
    private final ResourceHandler<ItemResource> inventory = new CombinedResourceHandler<>(slot(0),slot(1));
    private final ResourceHandler<ItemResource> top = input(0), side = input(1), closed = new CombinedResourceHandler<>();
    private final ResourceHandler<ItemResource> unsided = new CombinedResourceHandler<>(this.top,this.side);
    public BreedingPenBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.BREEDING_PEN_BE.get(),pos,state); }
    public ItemStack food(int slot) { return this.food[slot].copy(); }
    public ResourceHandler<ItemResource> automation(Direction side) { return side == null ? this.unsided : side == Direction.DOWN ? this.closed : side == Direction.UP ? this.top : this.side; }
    private boolean live() { return this.level != null && !this.isRemoved() && this.level.getBlockEntity(this.worldPosition) == this; }
    private static boolean accepts(ItemStack stack) { return stack.getItem() instanceof GuavaItem && Bunnidog.isBunnidogFood(stack); }
    private ItemStackResourceHandler slot(int slot) {
        return new ItemStackResourceHandler() {
            @Override protected ItemStack getStack() { return food[slot]; }
            @Override protected void setStack(ItemStack stack) { food[slot] = stack; }
            @Override protected boolean isValid(ItemResource resource) { return live() && accepts(resource.toStack()); }
            @Override protected int getCapacity(ItemResource resource) { return resource.isEmpty() ? 16 : Math.min(16,resource.getMaxStackSize()); }
            @Override protected void onRootCommit(ItemStack previous) { changed(); }
        };
    }
    private ResourceHandler<ItemResource> input(int slot) {
        return new ResourceHandler<>() {
            private void index(int index) { java.util.Objects.checkIndex(index,1); }
            @Override public int size() { return 1; }
            @Override public ItemResource getResource(int index) { index(index); return inventory.getResource(slot); }
            @Override public long getAmountAsLong(int index) { index(index); return inventory.getAmountAsLong(slot); }
            @Override public long getCapacityAsLong(int index, ItemResource resource) { index(index); return inventory.getCapacityAsLong(slot,resource); }
            @Override public boolean isValid(int index, ItemResource resource) { index(index); return inventory.isValid(slot,resource); }
            @Override public int insert(int index, ItemResource resource, int amount, TransactionContext tx) { index(index); return live() ? inventory.insert(slot,resource,amount,tx) : 0; }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext tx) { index(index); return 0; }
        };
    }
    public int insertFromHand(Player player, ItemStack actual, int slot) {
        if (!live() || !accepts(actual)) return 0;
        try (var tx = Transaction.openRoot()) {
            int count = this.inventory.insert(slot,ItemResource.of(actual),actual.getCount(),tx);
            if (count > 0) { actual.consume(count,player); tx.commit(); }
            return count;
        }
    }
    public ItemStack take(int slot) {
        if (!live() || this.food[slot].isEmpty()) return ItemStack.EMPTY;
        var out = this.food[slot].copy();
        try (var tx = Transaction.openRoot()) { this.inventory.extract(slot,ItemResource.of(out),out.getCount(),tx); tx.commit(); }
        return out;
    }
    public Pen.Status status() {
        if (!(this.level instanceof ServerLevel server) || !live()) return Pen.Status.CHANGED;
        if (!accepts(this.food[0]) || !accepts(this.food[1])) return Pen.Status.EMPTY;
        return PenBreeding.inspect(server,this.worldPosition,this.getBlockState().getValue(BreedingPenBlock.FACING)).status();
    }
    public Pen.Status activate(boolean automated) {
        if (this.feeding) return Pen.Status.BUSY;
        if (!(this.level instanceof ServerLevel server) || !live()) return Pen.Status.CHANGED;
        if (!accepts(this.food[0]) || !accepts(this.food[1])) return Pen.Status.EMPTY;
        this.feeding = true;
        try {
            var facing = this.getBlockState().getValue(BreedingPenBlock.FACING);
            var pair = PenBreeding.inspect(server,this.worldPosition,facing);
            if (pair.status() != Pen.Status.READY) return pair.status();
            var a = ItemResource.of(this.food[0]); var b = ItemResource.of(this.food[1]);
            if (!(a.toStack().getItem() instanceof GuavaItem first) || !(b.toStack().getItem() instanceof GuavaItem second)) return Pen.Status.EMPTY;
            if (automated) for (var parent : pair.parents()) {
                if (!EventHooks.canEntityGrief(server,parent)) return Pen.Status.PROTECTED;
                if (!live()) return Pen.Status.CHANGED;
            }
            if (!live() || this.getBlockState().getValue(BreedingPenBlock.FACING) != facing
                    || !accepts(this.food[0]) || !accepts(this.food[1])) return Pen.Status.CHANGED;
            var current = PenBreeding.inspect(server,this.worldPosition,facing);
            if (current.status() != Pen.Status.READY || !current.parents().equals(pair.parents())) return Pen.Status.CHANGED;
            try (var tx = Transaction.openRoot()) {
                if (this.inventory.extract(0,a,1,tx) != 1 || this.inventory.extract(1,b,1,tx) != 1) return Pen.Status.CHANGED;
                // Native setInLove has no server-side event callbacks. Both costs and love states precede commit observers.
                pair.parents().get(0).beginPenCourtship(first.tier().level());
                pair.parents().get(1).beginPenCourtship(second.tier().level());
                tx.commit();
            }
            if (live()) server.playSound(null,this.worldPosition,SoundEvents.FOX_EAT,SoundSource.BLOCKS,.4F,1.1F);
            return Pen.Status.FED;
        } finally { this.feeding = false; }
    }
    public int stockSignal() {
        if (this.food[0].isEmpty() && this.food[1].isEmpty()) return 0;
        return 1+14*Math.min(this.food[0].getCount(),this.food[1].getCount())/16;
    }
    private void changed() {
        this.setChanged();
        if (live() && !this.level.isClientSide()) {
            int fill = (this.food[0].isEmpty() ? 0 : 1)+(this.food[1].isEmpty() ? 0 : 2);
            if (this.getBlockState().getValue(BreedingPenBlock.FILL) != fill)
                this.level.setBlock(this.worldPosition,this.getBlockState().setValue(BreedingPenBlock.FILL,fill),3);
            this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),Block.UPDATE_ALL);
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this,BlockEntity::getUpdateTag); }
    @Override public void onDataPacket(Connection connection, ValueInput input) { handleUpdateTag(input); }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); for (int i = 0; i < 2; i++) if (!this.food[i].isEmpty()) output.store("Food"+i,ItemStack.CODEC,this.food[i]);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); for (int i = 0; i < 2; i++) this.food[i] = input.read("Food"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos,state);
        if (this.level instanceof ServerLevel server) for (int i = 0; i < 2; i++) { var out = this.food[i]; this.food[i] = ItemStack.EMPTY; Block.popResource(server,pos,out); }
    }
}
