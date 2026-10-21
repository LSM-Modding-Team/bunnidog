package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Learning;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

/** Established workers for existing world-action tests; new learners are tested separately. */
final class WorkerFixture {
    private WorkerFixture() {}
    static void familiar(Bunnidog dog) {
        var tag = new CompoundTag(); tag.putString("Learning", Learning.LEGACY_FAMILIAR.toString());
        dog.learning().restore(TagValueInput.create(ProblemReporter.DISCARDING, dog.level().registryAccess(), tag), false);
    }
}
