package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.BunnidogCare;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Native care and nursery stations: one successful pulse treats one animal or one saved litter. */
public final class ModDispensers {
    private ModDispensers() {}
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var previousBrush = DispenserBlock.DISPENSER_REGISTRY.get(Items.BRUSH);
            var brushing = new CareBehavior(false);
            DispenserBlock.registerBehavior(Items.BRUSH,(source,tool) -> brushing.findAnimal(source) != null || previousBrush == null
                    ? brushing.dispense(source,tool) : previousBrush.dispense(source,tool));
            DispenserBlock.registerBehavior(ModItems.SYRINGE.get(),new CareBehavior(true));
            DispenserBlock.registerBehavior(ModItems.SEED.get(),new NurseryBehavior(false));
            DispenserBlock.registerBehavior(ModItems.STARLIT_DUST.get(),new NurseryBehavior(true));
        });
    }
    private static final class NurseryBehavior extends OptionalDispenseItemBehavior {
        private final boolean ripening;
        NurseryBehavior(boolean ripening) { this.ripening = ripening; }
        @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
            this.setSuccess(false);
            var level = source.level();
            var front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            if (!level.hasChunkAt(front)) return stack;
            boolean changed = level.getBlockEntity(front) instanceof NurseryCradleBlockEntity cradle
                    ? this.ripening ? cradle.grow(true) : cradle.insert(stack)
                    : this.ripening ? SeedCropBlock.ripen(level,front,level.getBlockState(front)) : SeedCropBlock.plant(level,front,stack);
            if (changed) {
                stack.shrink(1);
                if (this.ripening) level.levelEvent(2005,front,15);
                this.setSuccess(true);
            }
            return stack;
        }
    }
    private static final class CareBehavior extends OptionalDispenseItemBehavior {
        private final boolean sampling;
        CareBehavior(boolean sampling) { this.sampling = sampling; }
        private Bunnidog findAnimal(BlockSource source) {
            var front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            if (!source.level().hasChunkAt(front)) return null;
            return source.level().getEntitiesOfClass(Bunnidog.class,new AABB(front),
                    candidate -> this.sampling ? BunnidogCare.canSample(candidate) : BunnidogCare.canGroom(candidate)).stream()
                    .min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(Vec3.atCenterOf(front)))).orElse(null);
        }
        @Override protected ItemStack execute(BlockSource source, ItemStack tool) {
            this.setSuccess(false);
            var level = source.level();
            var direction = source.state().getValue(DispenserBlock.FACING);
            var front = source.pos().relative(direction);
            if (!level.hasChunkAt(front)) return tool;
            var dog = findAnimal(source);
            if (dog == null) return tool;
            List<ItemStack> products = this.sampling ? List.of(BunnidogCare.sample(dog)) : BunnidogCare.groom(dog);
            if (products.isEmpty() || products.getFirst().isEmpty()) return tool;
            products.forEach(product -> DefaultDispenseItemBehavior.spawnItem(level,product,6,direction,DispenserBlock.getDispensePosition(source)));
            tool.hurtAndBreak(1,level,null,item -> {});
            this.setSuccess(true);
            return tool;
        }
    }
}
