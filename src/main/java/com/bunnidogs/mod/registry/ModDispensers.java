package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.BunnidogCare;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.item.MutagenItem;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
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
            DispenserBlock.registerBehavior(ModItems.SEED.get(),new NurseryBehavior(NurseryAction.PLANT));
            DispenserBlock.registerBehavior(ModItems.STARLIT_DUST.get(),new NurseryBehavior(NurseryAction.RIPEN));
            DispenserBlock.registerBehavior(ModItems.MUTAGEN.get(),new NurseryBehavior(NurseryAction.TREAT));
            DispenserBlock.registerBehavior(ModItems.GUAVA_SAPLING.get(),new SaplingBehavior());
            var previousGuava = DispenserBlock.DISPENSER_REGISTRY.get(ModItems.GUAVA.get());
            var droppingGuava = previousGuava == null ? new DefaultDispenseItemBehavior() : previousGuava;
            var plantingFruit = new FruitPlantingBehavior();
            DispenserBlock.registerBehavior(ModItems.GUAVA.get(),(source,stack) -> {
                var front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                return GuavaFruitBlock.canPlant(source.level(),front) ? plantingFruit.dispense(source,stack) : droppingGuava.dispense(source,stack);
            });
            var previousShears = DispenserBlock.DISPENSER_REGISTRY.get(Items.SHEARS);
            var picking = new FruitPickingBehavior();
            DispenserBlock.registerBehavior(Items.SHEARS,(source,tool) -> {
                var front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                return source.level().hasChunkAt(front) && source.level().getBlockState(front).is(ModBlocks.GUAVA_FRUIT.get()) || previousShears == null
                        ? picking.dispense(source,tool) : previousShears.dispense(source,tool);
            });
        });
    }
    /** A valid canopy gap uses one plain guava; other targets keep the original food-ejection behavior. */
    private static final class FruitPlantingBehavior extends OptionalDispenseItemBehavior {
        @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
            var front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            boolean planted = GuavaFruitBlock.plant(source.level(),front);
            if (planted) stack.shrink(1);
            this.setSuccess(planted);
            return stack;
        }
    }
    /** Ordinary shears still delegate to their original behavior away from guava fruit. */
    private static final class FruitPickingBehavior extends OptionalDispenseItemBehavior {
        @Override protected ItemStack execute(BlockSource source, ItemStack tool) {
            var front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            boolean picked = GuavaFruitBlock.pick(source.level(),front);
            if (picked) tool.hurtAndBreak(1,source.level(),null,item -> {});
            this.setSuccess(picked);
            return tool;
        }
    }
    private enum NurseryAction { PLANT, RIPEN, TREAT }
    /** Native block-item placement consumes exactly one; invalid targets retain stock without ejection. */
    private static final class SaplingBehavior extends OptionalDispenseItemBehavior {
        @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
            this.setSuccess(false);
            var level = source.level(); var direction = source.state().getValue(DispenserBlock.FACING);
            var front = source.pos().relative(direction);
            if (!level.hasChunkAt(front) || !level.hasChunkAt(front.below()) || level.isOutsideBuildHeight(front)
                    || !level.getWorldBorder().isWithinBounds(front) || !level.getBlockState(front).isAir()
                    || !level.getFluidState(front).isEmpty()) return stack;
            if (stack.getItem() instanceof BlockItem sapling)
                this.setSuccess(sapling.place(new DirectionalPlaceContext(level,front,direction,stack,Direction.UP)).consumesAction());
            return stack;
        }
    }
    private static final class NurseryBehavior extends OptionalDispenseItemBehavior {
        private final NurseryAction action;
        NurseryBehavior(NurseryAction action) { this.action = action; }
        @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
            this.setSuccess(false);
            var level = source.level();
            var front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            if (!level.hasChunkAt(front)) return stack;
            var cradle = level.getBlockEntity(front) instanceof NurseryCradleBlockEntity held ? held : null;
            boolean changed = switch (this.action) {
                case PLANT -> cradle != null ? cradle.insert(stack) : SeedCropBlock.plant(level,front,stack);
                case RIPEN -> cradle != null ? cradle.grow(true) : SeedCropBlock.ripen(level,front,level.getBlockState(front));
                case TREAT -> MutagenItem.apply(level,front);
            };
            if (changed) {
                stack.shrink(1);
                if (this.action != NurseryAction.PLANT) level.levelEvent(2005,front,15);
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
