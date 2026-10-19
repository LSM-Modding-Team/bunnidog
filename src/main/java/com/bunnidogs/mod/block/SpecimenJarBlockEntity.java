package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A single preserved sample displayed in its expressed coat; the actual snapshot remains the authority. */
public final class SpecimenJarBlockEntity extends BlockEntity {
    private ItemStack sample = ItemStack.EMPTY;
    public SpecimenJarBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.SPECIMEN_JAR_BE.get(),pos,state); }
    public ItemStack sample() { return this.sample.copy(); }
    public boolean insertFromHand(Player player, ItemStack actual) {
        if (!this.sample.isEmpty() || TissueSampleItem.read(actual).isEmpty() || !live()) return false;
        this.sample = actual.copyWithCount(1);
        actual.consume(1,player);
        changed(); return true;
    }
    public ItemStack take() {
        if (!live()) return ItemStack.EMPTY;
        var out = this.sample; this.sample = ItemStack.EMPTY; changed(); return out;
    }
    public int fixedSignal() { return TissueSampleItem.read(this.sample).map(t -> 1+2*Purebreds.fixedCount(t.genome())).orElse(0); }
    private boolean live() { return this.level != null && !this.isRemoved() && this.level.getBlockEntity(this.worldPosition) == this; }
    private void changed() {
        this.setChanged();
        if (live() && !this.level.isClientSide()) {
            int coat = TissueSampleItem.read(this.sample).map(t -> t.genome().expressed(Gene.COAT).id()+1).orElse(0);
            if (this.getBlockState().getValue(SpecimenJarBlock.SPECIMEN) != coat)
                this.level.setBlock(this.worldPosition,this.getBlockState().setValue(SpecimenJarBlock.SPECIMEN,coat),3);
            this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),Block.UPDATE_ALL);
        }
    }
    @Override public void onLoad() { super.onLoad(); changed(); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this,BlockEntity::getUpdateTag); }
    @Override public void onDataPacket(Connection connection, ValueInput input) { handleUpdateTag(input); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); if (!this.sample.isEmpty()) output.store("Sample",ItemStack.CODEC,this.sample); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); this.sample = input.read("Sample",ItemStack.CODEC).orElse(ItemStack.EMPTY); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos,state);
        if (this.level instanceof ServerLevel server) { var out = this.sample; this.sample = ItemStack.EMPTY; Block.popResource(server,pos,out); }
    }
}
