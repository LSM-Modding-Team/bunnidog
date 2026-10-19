package com.bunnidogs.mod.block;

import java.util.Optional;
import java.util.Objects;
import com.bunnidogs.mod.entity.NurseryHatching;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;

/** One saved litter; incubation belongs to the block and transfers only commit their inventory changes. */
public final class NurseryCradleBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    public static final int STAGE_TICKS = 60*20;
    private ItemStack stored = ItemStack.EMPTY;
    private int age, growthWait = STAGE_TICKS;
    private boolean hatching;
    private boolean manualHatchOnly;
    private boolean seedCached;
    private String cachedData;
    private Optional<Seed> cachedSeed = Optional.empty();
    private final ItemStackResourceHandler automation = new ItemStackResourceHandler() {
        @Override protected ItemStack getStack() { return hatching ? ItemStack.EMPTY : stored; }
        @Override protected void setStack(ItemStack stack) { stored = stack; }
        @Override protected boolean isValid(ItemResource resource) { return !hatching && SeedItem.read(resource.toStack()).isPresent(); }
        @Override protected int getCapacity(ItemResource resource) { return 1; }
        @Override protected void onRootCommit(ItemStack previous) { resetGrowth(); changed(); }
    };
    public NurseryCradleBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.NURSERY_CRADLE_BE.get(),pos,state); }
    public ItemStack storedSeed() { return this.stored.copy(); }
    public Optional<Seed> seed() {
        if (this.stored.getCount() != 1 || !this.stored.is(ModItems.SEED.get())) return Optional.empty();
        String data = this.stored.get(ModItems.SEED_DATA.get());
        if (!this.seedCached || !Objects.equals(data,this.cachedData)) {
            this.seedCached = true; this.cachedData = data; this.cachedSeed = SeedItem.read(this.stored);
        }
        return this.cachedSeed;
    }
    public int age() { return this.age; }
    public int growthWait() { return this.growthWait; }
    public boolean manualHatchOnly() { return this.manualHatchOnly; }
    public boolean setManualHatchOnly(boolean enabled) {
        if (this.hatching || !(this.level instanceof ServerLevel server) || !attached(server) || this.manualHatchOnly == enabled) return false;
        this.manualHatchOnly = enabled; changed(); return true;
    }
    public ResourceHandler<ItemResource> automation() { return this.automation; }
    public int stockSignal() { return this.stored.isEmpty() ? 0 : 1+2*this.age; }
    private void resetGrowth() { this.age = 0; this.growthWait = STAGE_TICKS; }
    public boolean insert(ItemStack stack) {
        if (this.hatching || !this.stored.isEmpty() || SeedItem.read(stack).isEmpty()) return false;
        this.stored = stack.copyWithCount(1); resetGrowth(); changed(); return true;
    }
    public ItemStack takeSeed() {
        if (this.hatching) return ItemStack.EMPTY;
        var out = this.stored; this.stored = ItemStack.EMPTY; resetGrowth(); changed(); return out;
    }
    public boolean grow(boolean instant) {
        if (this.hatching || !(this.level instanceof ServerLevel server) || seed().isEmpty() || this.age >= SeedCropBlock.MAX_AGE) return false;
        this.age = instant ? SeedCropBlock.MAX_AGE : Math.min(SeedCropBlock.MAX_AGE,this.age+SeedCropBlock.boneMealAges(server.getRandom()));
        this.growthWait = this.age == SeedCropBlock.MAX_AGE ? 0 : STAGE_TICKS;
        changed(); return true;
    }
    /** A resident pays for one exact stage, never rerolling or replacing the saved litter. */
    public boolean nurture(ItemStack expected, int expectedAge) {
        if (this.hatching || !(this.level instanceof ServerLevel server) || !attached(server) || seed().isEmpty() || this.age != expectedAge
                || this.age >= SeedCropBlock.MAX_AGE || !ItemStack.matches(this.stored,expected)) return false;
        this.age++;
        this.growthWait = this.age == SeedCropBlock.MAX_AGE ? 0 : STAGE_TICKS;
        changed(); return true;
    }
    public boolean applyMutagen() {
        if (this.hatching || !(this.level instanceof ServerLevel)) return false;
        var seed = seed();
        if (seed.isEmpty() || seed.get().mutagen()) return false;
        this.stored.set(ModItems.SEED_DATA.get(),seed.get().withMutagen().toString());
        changed(); return true;
    }
    public void serverTick(ServerLevel level) {
        if (this.hatching || this.age >= SeedCropBlock.MAX_AGE || seed().isEmpty()) return;
        if (--this.growthWait <= 0) {
            this.age++; this.growthWait = this.age == SeedCropBlock.MAX_AGE ? 0 : STAGE_TICKS; changed();
        } else level.blockEntityChanged(this.worldPosition);
    }
    private boolean attached(ServerLevel server) { return !isRemoved() && server.getBlockEntity(this.worldPosition) == this; }
    public NurseryHatching.Outcome hatch() {
        if (!(this.level instanceof ServerLevel server) || this.hatching || seed().isEmpty()) return NurseryHatching.refused(NurseryHatching.Status.EMPTY);
        if (this.age < SeedCropBlock.MAX_AGE) return NurseryHatching.refused(NurseryHatching.Status.NOT_RIPE);
        var seed = seed().orElseThrow(); var reserved = this.stored;
        this.hatching = true; this.stored = ItemStack.EMPTY;
        NurseryHatching.Outcome outcome;
        try { outcome = NurseryHatching.hatch(server,this.worldPosition,getBlockState().getValue(NurseryCradleBlock.FACING),seed); }
        finally { this.hatching = false; }
        if (outcome.status() == NurseryHatching.Status.HATCHED) resetGrowth();
        else if (attached(server)) this.stored = reserved;
        else Block.popResource(server,this.worldPosition,reserved);
        if (attached(server)) changed();
        return outcome;
    }
    private void changed() {
        setChanged();
        if (this.level instanceof ServerLevel server && attached(server)) {
            int fill = this.stored.isEmpty() ? 0 : 1+this.age*3/SeedCropBlock.MAX_AGE;
            if (getBlockState().getValue(NurseryCradleBlock.FILL) != fill)
                server.setBlock(this.worldPosition,getBlockState().setValue(NurseryCradleBlock.FILL,fill),3);
            server.sendBlockUpdated(this.worldPosition,getBlockState(),getBlockState(),Block.UPDATE_ALL);
        }
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.stored.isEmpty()) output.store("SeedItem",ItemStack.CODEC,this.stored);
        output.putInt("Age",this.age); output.putInt("GrowthWait",this.growthWait);
        if (this.manualHatchOnly) output.putBoolean("ManualHatchOnly",true);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.stored = input.read("SeedItem",ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.age = this.stored.isEmpty() ? 0 : Math.clamp(input.getIntOr("Age",0),0,SeedCropBlock.MAX_AGE);
        this.growthWait = this.age == SeedCropBlock.MAX_AGE ? 0 : Math.clamp(input.getIntOr("GrowthWait",STAGE_TICKS),1,STAGE_TICKS);
        this.hatching = false;
        this.manualHatchOnly = input.getBooleanOr("ManualHatchOnly",false);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos,state);
        if (this.level instanceof ServerLevel server && !this.stored.isEmpty()) {
            Block.popResource(server,pos,this.stored); this.stored = ItemStack.EMPTY;
        }
    }
}
