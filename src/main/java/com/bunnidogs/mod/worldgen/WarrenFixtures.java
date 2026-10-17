package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.NurseryCradleBlock;
import com.bunnidogs.mod.layout.SettlementFixtures;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** One owning column writes each fixture and support; occupied blocks and existing stock are preserved. */
final class WarrenFixtures {
    private WarrenFixtures() {}
    static void place(WorldGenLevel world, BlockPos origin, int[] data, BoundingBox chunkBox) {
        for (var fixture : SettlementFixtures.decode(data)) {
            BlockPos pos = origin.offset(fixture.pos().x(),fixture.pos().y(),fixture.pos().z());
            if (chunkBox != null && (!chunkBox.isInside(pos) || !chunkBox.isInside(pos.below()))) continue;
            if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.above()).isAir()
                    || !world.getFluidState(pos).isEmpty()
                    || !world.getBlockState(pos.below()).isFaceSturdy(world,pos.below(),Direction.UP)) continue;
            var state = switch (fixture.kind()) {
                case TROUGH -> ModBlocks.FEEDING_TROUGH.get().defaultBlockState().setValue(FeedingTroughBlock.TENDING,true);
                case BELL -> Blocks.BELL.defaultBlockState();
                case NURSERY_CRADLE -> ModBlocks.NURSERY_CRADLE.get().defaultBlockState().setValue(NurseryCradleBlock.FACING,Direction.EAST);
            };
            if (!world.setBlock(pos,state,2)) continue;
            if (fixture.kind() == SettlementFixtures.Kind.TROUGH
                    && world.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
                trough.setInitialFood(new ItemStack(ModItems.GUAVA.get(),2));
                // WorldGenRegion does not use the live-server block-update path in the entity.
                world.setBlock(pos,state.setValue(FeedingTroughBlock.FILL,1),2);
            }
        }
    }
}
