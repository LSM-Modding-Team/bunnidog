package com.bunnidogs.mod.block;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.PedigreeSignal;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.registry.ModBlocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/** A copied reference is information, not an inventory. Poll only a loaded one-block front pad. */
public final class PedigreeComparatorBlockEntity extends BlockEntity {
    private Tissue reference;
    public record Reading(PedigreeSignal.Status status, int pairs) {
        int signal(boolean analog) { return status == PedigreeSignal.Status.READING ? PedigreeSignal.signal(pairs,analog) : 0; }
    }
    public PedigreeComparatorBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.PEDIGREE_COMPARATOR_BE.get(),pos,state); }
    public Optional<Tissue> reference() { return Optional.ofNullable(this.reference); }
    public boolean setReference(Tissue sample) {
        if (!live()) return false;
        this.reference = sample; setChanged(); refresh(); return true;
    }
    private boolean live() {
        return this.level instanceof ServerLevel && this.level.getBlockEntity(this.worldPosition) == this
                && this.level.getBlockState(this.worldPosition).is(ModBlocks.PEDIGREE_COMPARATOR.get());
    }
    public Reading read() {
        if (this.reference == null) return new Reading(PedigreeSignal.Status.EMPTY,-1);
        if (!live()) return new Reading(PedigreeSignal.Status.UNAVAILABLE,-1);
        var front = this.worldPosition.relative(getBlockState().getValue(PedigreeComparatorBlock.FACING));
        var pad = new AABB(front);
        if (!this.level.hasChunkAt(front) || !this.level.getWorldBorder().isWithinBounds(pad))
            return new Reading(PedigreeSignal.Status.UNLOADED,-1);
        if (!this.level.getBlockState(front).isAir() || !this.level.getBlockState(front.above()).isAir()
                || !this.level.getFluidState(front).isEmpty()) return new Reading(PedigreeSignal.Status.BLOCKED,-1);
        var animals = this.level.getEntitiesOfClass(Bunnidog.class,pad,dog -> dog.isAlive() && pad.contains(dog.position()));
        if (animals.isEmpty()) return new Reading(PedigreeSignal.Status.ABSENT,-1);
        if (animals.size() != 1) return new Reading(PedigreeSignal.Status.CROWDED,-1);
        var animal = animals.getFirst();
        return !animal.acceptsCare() ? new Reading(PedigreeSignal.Status.UNAVAILABLE,-1)
                : new Reading(PedigreeSignal.Status.READING,PedigreeSignal.matchingPairs(this.reference.genome(),animal.genome()));
    }
    public void refresh() {
        if (!live()) return;
        var state = getBlockState();
        int power = read().signal(state.getValue(PedigreeComparatorBlock.ANALOG));
        var next = state.setValue(PedigreeComparatorBlock.POWER,power).setValue(PedigreeComparatorBlock.REFERENCE,this.reference != null);
        if (!next.equals(state)) {
            this.level.setBlock(this.worldPosition,next,3);
            if (live()) this.level.updateNeighbourForOutputSignal(this.worldPosition,next.getBlock());
        }
    }
    @Override public void onLoad() {
        super.onLoad();
        if (live()) this.level.scheduleTick(this.worldPosition,getBlockState().getBlock(),1);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.reference != null) output.putString("Reference",this.reference.toString());
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.reference = Tissue.parse(input.getStringOr("Reference","")).orElse(null);
    }
}
