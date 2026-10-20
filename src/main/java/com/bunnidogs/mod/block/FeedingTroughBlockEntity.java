package com.bunnidogs.mod.block;

import java.util.Arrays;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Diet;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModDietTags;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;

/** One preserved food stack. Meals are server-thread transactions; residents share the stock. */
public final class FeedingTroughBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16;
    private ItemStack food = ItemStack.EMPTY;
    private ItemStack filter = ItemStack.EMPTY;
    private TroughWork workMode = TroughWork.MIXED;
    /** One journal for every side, retaining the existing Food save and exact components. */
    private final ItemStackResourceHandler automation = new ItemStackResourceHandler() {
        @Override protected ItemStack getStack() { return food; }
        @Override protected void setStack(ItemStack stack) { food = stack; }
        @Override protected boolean isValid(ItemResource resource) { return acceptsInput(resource.toStack()); }
        @Override protected int getCapacity(ItemResource resource) {
            return resource.isEmpty() ? CAPACITY : Math.min(CAPACITY,resource.getMaxStackSize());
        }
        @Override protected void onRootCommit(ItemStack previous) { changed(); }
    };
    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.FEEDING_TROUGH_BE.get(),pos,state); }
    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && !stack.has(DataComponents.USE_REMAINDER) && com.bunnidogs.mod.item.GuavaItem.tierOf(stack)==0
                && (stack.is(ModItems.BUNNIDOG_FOOD) || Arrays.stream(Diet.values()).anyMatch(d -> ModDietTags.accepts(d,stack)));
    }
    public ItemStack food() { return this.food; }
    public ItemStack filter() { return this.filter.copy(); }
    public TroughWork workMode() { return this.workMode; }
    public boolean allowsWork(TroughWork.Job job) { return this.workMode.allows(job); }
    public void setWorkMode(TroughWork mode) { this.workMode = mode; changed(); }
    private boolean acceptsInput(ItemStack stack) {
        return accepts(stack) && (this.filter.isEmpty() || ItemStack.isSameItemSameComponents(this.filter,stack));
    }
    /** A virtual one-item template, never taken from inventory or dropped as stock. */
    public boolean setFilter(ItemStack stack) {
        if (!accepts(stack) || !this.food.isEmpty() && !ItemStack.isSameItemSameComponents(this.food,stack)) return false;
        this.filter = stack.copyWithCount(1); changed(); return true;
    }
    public void clearFilter() { this.filter = ItemStack.EMPTY; changed(); }
    public ResourceHandler<ItemResource> automation() { return this.automation; }
    public int stockSignal() {
        return this.food.isEmpty() ? 0 : 1+14*Math.min(this.food.getCount(),Math.min(CAPACITY,this.food.getMaxStackSize()))
                / Math.min(CAPACITY,this.food.getMaxStackSize());
    }
    public boolean canInsert(ItemStack stack) {
        return acceptsInput(stack) && (this.food.isEmpty() || ItemStack.isSameItemSameComponents(this.food,stack))
                && this.food.getCount() < Math.min(CAPACITY,stack.getMaxStackSize());
    }
    /** Only new worldgen furniture uses this; no live-server block writes from a WorldGenRegion. */
    public void setInitialFood(ItemStack stack) {
        if (!this.food.isEmpty() || !acceptsInput(stack)) return;
        this.food = stack.copyWithCount(Math.min(stack.getCount(),Math.min(CAPACITY,stack.getMaxStackSize())));
        this.setChanged();
    }
    public int insert(ItemStack stack) {
        if (!acceptsInput(stack) || !this.food.isEmpty() && !ItemStack.isSameItemSameComponents(this.food,stack)) return 0;
        int amount = Math.max(0,Math.min(stack.getCount(),Math.min(CAPACITY,stack.getMaxStackSize())-this.food.getCount()));
        if (amount > 0) {
            if (this.food.isEmpty()) this.food = stack.copyWithCount(amount); else this.food.grow(amount);
            changed();
        }
        return amount;
    }
    /** Reserve actual player stock before committed inventory observers run. */
    public int insertFromPlayer(net.minecraft.world.entity.player.Player player,ItemStack actual,int maximum) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel server) || server.getBlockEntity(worldPosition)!=this) return 0;
        try (var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            int count=automation.insert(0,ItemResource.of(actual),Math.min(maximum,actual.getCount()),tx);
            if (count>0) { actual.consume(count,player); tx.commit(); }
            return count;
        }
    }
    public ItemStack takeAll() { ItemStack out = this.food; this.food = ItemStack.EMPTY; changed(); return out; }
    /** World power pauses resident work; manual and native inventory transfers remain independent. */
    public boolean residentsPaused() { return this.level != null && this.level.hasNeighborSignal(this.worldPosition); }
    public boolean canFeed(Bunnidog dog) { return !this.residentsPaused() && accepts(this.food) && dog.canEat(this.food); }
    public boolean takeMeal(Bunnidog dog) {
        if (!(this.level instanceof ServerLevel) || !canFeed(dog)) return false;
        this.food.shrink(1); changed(); return true;
    }
    /** A work job reserves one exact existing input while leaving its food buffer intact. */
    public ItemStack takeSurplus(ItemStack expected, int reserve) {
        if (!(this.level instanceof ServerLevel) || this.residentsPaused() || this.food.getCount() <= Math.max(0,reserve)
                || !ItemStack.isSameItemSameComponents(this.food,expected)) return ItemStack.EMPTY;
        var out = this.food.split(1); changed(); return out;
    }
    private void changed() {
        this.setChanged();
        if (this.level instanceof ServerLevel server && !this.isRemoved() && server.getBlockEntity(this.worldPosition) == this) {
            int fill = this.food.isEmpty() ? 0 : this.food.getCount() <= 5 ? 1 : this.food.getCount() <= 10 ? 2 : 3;
            if (this.getBlockState().getValue(FeedingTroughBlock.FILL) != fill)
                server.setBlock(this.worldPosition,this.getBlockState().setValue(FeedingTroughBlock.FILL,fill),3);
            server.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),Block.UPDATE_ALL);
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return this.saveWithoutMetadata(registries); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this,BlockEntity::getUpdateTag); }
    @Override public void onDataPacket(Connection connection, ValueInput input) { this.handleUpdateTag(input); }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.food.isEmpty()) output.store("Food",ItemStack.CODEC,this.food);
        if (!this.filter.isEmpty()) output.store("FoodFilter",ItemStack.CODEC,this.filter);
        if (this.workMode != TroughWork.MIXED) output.putString("WorkMode",this.workMode.key());
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); this.food = input.read("Food",ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.filter = input.read("FoodFilter",ItemStack.CODEC).map(stack -> stack.copyWithCount(1)).orElse(ItemStack.EMPTY);
        this.workMode = TroughWork.read(input.getStringOr("WorkMode","mixed"));
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos,state);
        if (this.level instanceof ServerLevel server && !this.food.isEmpty()) {
            Block.popResource(server,pos,this.food); this.food = ItemStack.EMPTY;
        }
    }
}
