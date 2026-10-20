package com.bunnidogs.mod.block;

import java.util.Random;
import com.bunnidogs.mod.genetics.Infusion;
import com.bunnidogs.mod.item.GuavaItem;
import com.bunnidogs.mod.item.JuiceItem;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Two paid ingredients and one output, committed together before any native inventory callbacks. */
public final class JuicerBlockEntity extends BlockEntity {
    public static final int PROCESS_TICKS = 100;
    private final ItemStack[] stock = {ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};
    private ItemStack workingA = ItemStack.EMPTY, workingB = ItemStack.EMPTY;
    private int progress;
    private boolean processing;
    private final ResourceHandler<ItemResource> inventory = new CombinedResourceHandler<>(slot(0),slot(1),slot(2));
    private final ResourceHandler<ItemResource> top = port(0), side = port(1), bottom = port(2);

    public JuicerBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.JUICER_BE.get(),pos,state); }
    public ItemStack item(int slot) { return this.stock[slot].copy(); }
    public int progress() { return this.progress; }
    public ResourceHandler<ItemResource> automation(Direction direction) {
        return direction == null ? new CombinedResourceHandler<>(this.top,this.side,this.bottom)
                : direction == Direction.DOWN ? this.bottom : direction == Direction.UP ? this.top : this.side;
    }
    private boolean live() { return this.level != null && !isRemoved() && this.level.getBlockEntity(this.worldPosition) == this; }
    private static boolean accepts(int slot, ItemStack stack) {
        return switch (slot) {
            case 0 -> stack.getItem() instanceof GuavaItem || SeedItem.read(stack).isPresent();
            case 1 -> stack.is(Items.GLASS_BOTTLE) || JuiceItem.carrier(stack).isPresent();
            default -> stack.getItem() instanceof JuiceItem;
        };
    }
    private ItemStackResourceHandler slot(int slot) {
        return new ItemStackResourceHandler() {
            @Override protected ItemStack getStack() { return stock[slot]; }
            @Override protected void setStack(ItemStack stack) { stock[slot] = stack; }
            @Override protected boolean isValid(ItemResource resource) { return live() && accepts(slot,resource.toStack()); }
            @Override protected int getCapacity(ItemResource resource) { return resource.isEmpty() ? 16 : Math.min(16,resource.getMaxStackSize()); }
            @Override protected void onRootCommit(ItemStack previous) { changed(); }
        };
    }
    private ResourceHandler<ItemResource> port(int slot) {
        // Implement the interface directly: its whole-handler defaults must obey the same directional restrictions.
        return new ResourceHandler<>() {
            private void index(int index) { java.util.Objects.checkIndex(index,1); }
            @Override public int size() { return 1; }
            @Override public ItemResource getResource(int index) { index(index); return inventory.getResource(slot); }
            @Override public long getAmountAsLong(int index) { index(index); return inventory.getAmountAsLong(slot); }
            @Override public long getCapacityAsLong(int index, ItemResource resource) { index(index); return slot == 2 ? 0 : inventory.getCapacityAsLong(slot,resource); }
            @Override public boolean isValid(int index, ItemResource resource) { index(index); return slot != 2 && inventory.isValid(slot,resource); }
            @Override public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
                index(index); return slot == 2 || !live() ? 0 : inventory.insert(slot,resource,amount,tx);
            }
            @Override public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
                index(index); return slot != 2 || !live() ? 0 : inventory.extract(slot,resource,amount,tx);
            }
        };
    }
    /** Native hand input pays before root-commit observers can change the hand or remove the station. */
    public int insertFromHand(Player player, ItemStack actual) {
        return insertFromHand(player,actual,actual.getCount());
    }
    public int insertFromHand(Player player,ItemStack actual,int maximum) {
        int slot = accepts(0,actual) ? 0 : accepts(1,actual) ? 1 : -1;
        if (slot < 0 || !live()) return 0;
        try (var tx = Transaction.openRoot()) {
            int count = this.inventory.insert(slot,ItemResource.of(actual),Math.min(maximum,actual.getCount()),tx);
            if (count > 0) { actual.consume(count,player); tx.commit(); }
            return count;
        }
    }
    public boolean canInsert(ItemStack stack) {
        int slot=accepts(0,stack) ? 0 : accepts(1,stack) ? 1 : -1;
        return live() && slot>=0 && (stock[slot].isEmpty() || ItemStack.isSameItemSameComponents(stock[slot],stack))
                && stock[slot].getCount()<inventory.getCapacityAsLong(slot,ItemResource.of(stack));
    }
    public ItemStack take(int slot) {
        if (!live() || this.stock[slot].isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = this.stock[slot].copy();
        try (var tx = Transaction.openRoot()) {
            if (this.inventory.extract(slot,ItemResource.of(taken),taken.getCount(),tx) != taken.getCount()) return ItemStack.EMPTY;
            tx.commit();
        }
        return taken;
    }
    private boolean pressing() { return this.stock[0].getItem() instanceof GuavaItem && this.stock[1].is(Items.GLASS_BOTTLE); }
    private boolean infusing() { return SeedItem.read(this.stock[0]).isPresent() && JuiceItem.carrier(this.stock[1]).isPresent(); }
    private ItemStack pressedJuice() {
        var guava = (GuavaItem)this.stock[0].getItem();
        return new ItemStack(ModItems.JUICES.get(guava.tier().level()).get());
    }
    public boolean outputBlocked() {
        if (pressing()) return !this.stock[2].isEmpty()
                && (!ItemStack.isSameItemSameComponents(this.stock[2],pressedJuice()) || this.stock[2].getCount() >= 16);
        return infusing() && !this.stock[2].isEmpty();
    }
    public void serverTick(ServerLevel level) {
        if (this.processing || !live()) return;
        if (!sameInput(this.workingA,this.stock[0]) || !sameInput(this.workingB,this.stock[1])) {
            this.progress = 0;
            this.workingA = this.stock[0].copyWithCount(1); this.workingB = this.stock[1].copyWithCount(1);
        }
        boolean active = (pressing() || infusing()) && !outputBlocked() && !level.hasNeighborSignal(this.worldPosition);
        if (this.getBlockState().getValue(JuicerBlock.WORKING) != active)
            level.setBlock(this.worldPosition,this.getBlockState().setValue(JuicerBlock.WORKING,active),2);
        if (!active || !live()) return;
        this.progress++; this.setChanged();
        if (this.progress < PROCESS_TICKS) return;
        this.processing = true;
        boolean completed = false;
        try (var tx = Transaction.openRoot()) {
            ItemStack result = pressing() ? pressedJuice() : JuiceItem.create(Infusion.roll(SeedItem.read(this.stock[0]).orElseThrow(),
                    JuiceItem.carrier(this.stock[1]).orElseThrow(),new Random(level.getRandom().nextLong())));
            var a = ItemResource.of(this.stock[0]); var b = ItemResource.of(this.stock[1]);
            if (this.inventory.extract(0,a,1,tx) == 1 && this.inventory.extract(1,b,1,tx) == 1
                    && this.inventory.insert(2,ItemResource.of(result),1,tx) == 1) {
                this.progress = 0;
                tx.commit(); completed = true;
            }
        } finally { this.processing = false; }
        if (completed && live()) {
            level.playSound(null,this.worldPosition,SoundEvents.BREWING_STAND_BREW,SoundSource.BLOCKS,.35F,1.25F);
            changed();
        }
    }
    private static boolean sameInput(ItemStack a, ItemStack b) {
        return a.isEmpty() && b.isEmpty() || !a.isEmpty() && !b.isEmpty() && ItemStack.isSameItemSameComponents(a,b);
    }
    public int stockSignal() { return this.stock[2].isEmpty() ? 0 : 1+14*this.stock[2].getCount()/Math.min(16,this.stock[2].getMaxStackSize()); }
    private void changed() {
        this.setChanged();
        if (live() && !this.level.isClientSide()) this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),Block.UPDATE_ALL);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this,BlockEntity::getUpdateTag); }
    @Override public void onDataPacket(Connection connection, ValueInput input) { handleUpdateTag(input); }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < 3; i++) if (!this.stock[i].isEmpty()) output.store("Slot"+i,ItemStack.CODEC,this.stock[i]);
        output.putInt("Progress",this.progress);
        if (!this.workingA.isEmpty()) output.store("WorkingA",ItemStack.CODEC,this.workingA);
        if (!this.workingB.isEmpty()) output.store("WorkingB",ItemStack.CODEC,this.workingB);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < 3; i++) this.stock[i] = input.read("Slot"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.progress = Math.clamp(input.getIntOr("Progress",0),0,PROCESS_TICKS-1);
        this.workingA = input.read("WorkingA",ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.workingB = input.read("WorkingB",ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos,state);
        if (this.level instanceof ServerLevel server) {
            for (int i = 0; i < 3; i++) { var out = this.stock[i]; this.stock[i] = ItemStack.EMPTY; Block.popResource(server,pos,out); }
        }
    }
}
