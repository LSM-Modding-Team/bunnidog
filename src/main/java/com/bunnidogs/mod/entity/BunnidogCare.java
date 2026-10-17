package com.bunnidogs.mod.entity;

import java.util.List;
import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** One server-side animal transaction for hand tools and dispensers; delivery/wear belongs to the caller. */
public final class BunnidogCare {
    private BunnidogCare() {}
    private static boolean healthy(Bunnidog dog) {
        return dog.level() instanceof ServerLevel && dog.acceptsCare() && dog.isAlive()
                && Sampling.healthy(dog.getHealth(),dog.getMaxHealth());
    }
    public static boolean canSample(Bunnidog dog) { return healthy(dog) && dog.samplingWait() == 0; }
    public static ItemStack sample(Bunnidog dog) {
        if (!canSample(dog)) return ItemStack.EMPTY;
        var sample = TissueSampleItem.create(dog.genome(),dog.stats());
        dog.beginSamplingRecovery();
        dog.playSound(SoundEvents.BOTTLE_FILL,.25F,1.4F);
        return sample;
    }
    public static boolean canGroom(Bunnidog dog) {
        return healthy(dog) && !dog.isBaby() && dog.groomingWait() == 0 && !Grooming.yields(dog.genome(),dog.stats()).isEmpty();
    }
    public static List<ItemStack> groom(Bunnidog dog) {
        if (!canGroom(dog)) return List.of();
        var products = Grooming.yields(dog.genome(),dog.stats()).stream().map(product -> new ItemStack(switch (product) {
            case WHISKER -> ModItems.WHISKER.get();
            case GLOWSTONE -> Items.GLOWSTONE_DUST;
            case STARLIT -> ModItems.STARLIT_DUST.get();
            case MOSS -> Items.MOSS_CARPET;
        },Grooming.COUNT)).toList();
        dog.beginGroomingRecovery();
        dog.playSound(SoundEvents.BRUSH_GENERIC,.4F,1.1F);
        return products;
    }
}
