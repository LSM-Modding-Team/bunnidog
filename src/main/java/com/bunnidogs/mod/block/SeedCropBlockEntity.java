package com.bunnidogs.mod.block;

import java.util.Optional;

import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Remembers the seed planted in a {@link SeedCropBlock}: the {@link Seed} in its canonical text form (the same string the
 * item stores, so a plain string is all that is saved, under the tag {@code Seed}). Server side only; nothing is synced.
 */
public class SeedCropBlockEntity extends BlockEntity {
    private static final String TAG_SEED = "Seed";

    private String seedText = "";

    public SeedCropBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SEED_CROP_BE.get(), pos, state);
    }

    /** The planted seed, or empty if this crop holds none (a crop placed by {@code /setblock}) or the text is malformed. */
    public Optional<Seed> seed() {
        return Seed.parse(seedText);
    }

    public void setSeed(Seed seed) {
        this.seedText = seed.toString();
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString(TAG_SEED, seedText);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        seedText = input.getStringOr(TAG_SEED, "");
    }
}
