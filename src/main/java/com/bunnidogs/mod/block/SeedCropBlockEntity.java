package com.bunnidogs.mod.block;

import java.util.Optional;

import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Remembers the seed planted in a {@link SeedCropBlock}: the {@link Seed} in its canonical text form (the same string the
 * item stores, saved under {@code Seed}) plus an optional whole {@code SeedItem} for names/components. Server side only.
 */
public class SeedCropBlockEntity extends BlockEntity {
    private static final String TAG_SEED = "Seed";

    private String seedText = "";
    private ItemStack originalSeed = ItemStack.EMPTY;

    public SeedCropBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SEED_CROP_BE.get(), pos, state);
    }

    /** The planted seed, or empty if this crop holds none (a crop placed by {@code /setblock}) or the text is malformed. */
    public Optional<Seed> seed() {
        return Seed.parse(seedText);
    }

    public void setSeed(Seed seed) {
        this.seedText = seed.toString();
        if (!this.originalSeed.is(ModItems.SEED.get())) this.originalSeed = SeedItem.create(seed);
        else this.originalSeed.set(ModItems.SEED_DATA.get(),this.seedText);
        this.setChanged();
    }
    /** The existing Seed text remains authoritative; names and all other item components travel with it. */
    public ItemStack seedStack() {
        return seed().map(seed -> {
            var stack = this.originalSeed.is(ModItems.SEED.get()) ? this.originalSeed.copyWithCount(1) : SeedItem.create(seed);
            stack.set(ModItems.SEED_DATA.get(),seed.toString());
            return stack;
        }).orElse(ItemStack.EMPTY);
    }
    public boolean setSeedStack(ItemStack stack) {
        var seed = SeedItem.read(stack);
        if (seed.isEmpty()) return false;
        this.originalSeed = stack.copyWithCount(1);
        setSeed(seed.get());
        return true;
    }

    /** Reserve stock before native removal or spawning invokes external hooks. */
    public ItemStack takeSeed() {
        var out = seedStack();
        this.seedText = ""; this.originalSeed = ItemStack.EMPTY; setChanged();
        return out;
    }

    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos,state);
        if (this.level instanceof ServerLevel server) SeedCropBlock.dropSeed(server,pos,takeSeed());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString(TAG_SEED, seedText);
        if (!this.originalSeed.isEmpty()) output.store("SeedItem",ItemStack.CODEC,seedStack());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        seedText = input.getStringOr(TAG_SEED, "");
        this.originalSeed = input.read("SeedItem",ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (Seed.parse(seedText).isEmpty()) SeedItem.read(this.originalSeed).ifPresent(seed -> this.seedText = seed.toString());
    }
}
