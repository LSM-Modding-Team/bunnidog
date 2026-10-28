package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Only actual self-planting foods (or one slice's vanilla seed) can restore a garden gap. */
final class ResidentPlanting {
    private ResidentPlanting() {}
    static BlockState plan(ServerLevel level, BlockPos pos, ItemStack food) {
        if (food.isEmpty() || !level.hasChunkAt(pos) || !level.hasChunkAt(pos.below()) || !level.hasChunkAt(pos.above())
                || level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) return null;
        if (food.is(Items.KELP)) {
            var below=level.getBlockState(pos.below());
            if (!level.getBlockState(pos).is(Blocks.WATER) || !level.getFluidState(pos).isSource()
                    || !level.getBlockState(pos.above()).is(Blocks.WATER) || !level.getFluidState(pos.above()).isSource()
                    || below.is(Blocks.KELP) || below.is(Blocks.KELP_PLANT)) return null;
            var kelp=Blocks.KELP.defaultBlockState(); return kelp.canSurvive(level,pos) ? kelp : null;
        }
        if (!level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()) return null;
        if(food.is(ModItems.GUAVA.get()))return com.bunnidogs.mod.block.GuavaFruitBlock.canPlant(level,pos)
                ? ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(com.bunnidogs.mod.block.GuavaFruitBlock.BUNCH,com.bunnidogs.mod.orchard.Orchard.cropSize(level,pos)) : null;
        var plant = plant(food);
        if (plant == null) return null;
        if (plant == Blocks.CACTUS && (level.getBlockState(pos.below()).is(Blocks.CACTUS)
                || !level.getEntitiesOfClass(LivingEntity.class,new AABB(pos).inflate(.1),LivingEntity::isAlive).isEmpty())) return null;
        var state = plant.defaultBlockState();
        return state.canSurvive(level,pos) ? state : null;
    }
    static boolean supports(ItemStack food) { return plant(food) != null; }
    private static net.minecraft.world.level.block.Block plant(ItemStack food) {
        return food.is(Items.CARROT) ? Blocks.CARROTS : food.is(Items.POTATO) ? Blocks.POTATOES
                : food.is(Items.MELON_SLICE) ? Blocks.MELON_STEM : food.is(Items.NETHER_WART) ? Blocks.NETHER_WART
                : food.is(Items.SWEET_BERRIES) ? Blocks.SWEET_BERRY_BUSH : food.is(Items.GLOW_BERRIES) ? Blocks.CAVE_VINES
                : food.is(Items.RED_MUSHROOM) ? Blocks.RED_MUSHROOM : food.is(Items.BROWN_MUSHROOM) ? Blocks.BROWN_MUSHROOM
                : food.is(Items.CRIMSON_FUNGUS) ? Blocks.CRIMSON_FUNGUS : food.is(Items.WARPED_FUNGUS) ? Blocks.WARPED_FUNGUS
                : food.is(Items.CACTUS) ? Blocks.CACTUS : food.is(Items.KELP) ? Blocks.KELP
                : food.is(ModItems.GUAVA.get()) ? ModBlocks.GUAVA_FRUIT.get() : null;
    }

}
