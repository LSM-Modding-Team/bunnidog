package com.bunnidogs.mod.block;

import java.util.Arrays;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Diet;
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

/** One preserved food stack. Meals are server-thread transactions; residents share the stock. */
public final class FeedingTroughBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16;
    private ItemStack food = ItemStack.EMPTY;
    public FeedingTroughBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.FEEDING_TROUGH_BE.get(),pos,state); }
    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && !stack.has(DataComponents.USE_REMAINDER)
                && (stack.is(ModItems.BUNNIDOG_FOOD) || Arrays.stream(Diet.values()).anyMatch(d -> ModDietTags.accepts(d,stack)));
    }
    public ItemStack food() { return this.food; }
    public int insert(ItemStack stack) {
        if (!accepts(stack) || !this.food.isEmpty() && !ItemStack.isSameItemSameComponents(this.food,stack)) return 0;
        int amount = Math.max(0,Math.min(stack.getCount(),Math.min(CAPACITY,stack.getMaxStackSize())-this.food.getCount()));
        if (amount > 0) {
            if (this.food.isEmpty()) this.food = stack.copyWithCount(amount); else this.food.grow(amount);
            changed();
        }
        return amount;
    }
    public ItemStack takeAll() { ItemStack out = this.food; this.food = ItemStack.EMPTY; changed(); return out; }
    public boolean canFeed(Bunnidog dog) { return accepts(this.food) && dog.canEat(this.food); }
    public boolean takeMeal(Bunnidog dog) {
        if (!(this.level instanceof ServerLevel) || !canFeed(dog)) return false;
        this.food.shrink(1); changed(); return true;
    }
    private void changed() {
        this.setChanged();
        if (this.level instanceof ServerLevel server) {
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
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); this.food = input.read("Food",ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos,state);
        if (this.level instanceof ServerLevel server && !this.food.isEmpty()) {
            Block.popResource(server,pos,this.food); this.food = ItemStack.EMPTY;
        }
    }
}
