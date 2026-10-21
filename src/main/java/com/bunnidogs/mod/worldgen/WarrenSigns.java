package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.network.TextComponents;
import com.bunnidogs.mod.layout.Wayfinding;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Places saved labels after their chunk's tunnel columns, without overwriting existing blocks or edited signs. */
final class WarrenSigns {
    private WarrenSigns() {}
    static void place(WorldGenLevel world, BlockPos origin, int[] data, BoundingBox chunkBox) {
        for (var marker : Wayfinding.decode(data)) {
            BlockPos pos = origin.offset(marker.pos().x(),marker.pos().y(),marker.pos().z());
            BlockPos support = origin.offset(marker.support().x(),marker.support().y(),marker.support().z());
            if (chunkBox != null && (!chunkBox.isInside(pos) || !chunkBox.isInside(support))) continue;
            if ((pos.getX() >> 4) != (support.getX() >> 4) || (pos.getZ() >> 4) != (support.getZ() >> 4)) continue;
            Direction facing = switch (marker.facing()) { case NORTH -> Direction.NORTH; case EAST -> Direction.EAST;
                case SOUTH -> Direction.SOUTH; case WEST -> Direction.WEST; };
            var wall = world.getBlockState(support);
            if (!world.getBlockState(pos).isAir() || wall.hasBlockEntity() || !wall.getFluidState().isEmpty()
                    || !wall.isFaceSturdy(world,support,facing)) continue;
            if (!world.setBlock(pos,ModBlocks.GUAVA_WALL_SIGN.get().defaultBlockState().setValue(WallSignBlock.FACING,facing),2)) continue;
            if (world.getBlockEntity(pos) instanceof SignBlockEntity sign) {
                var label = marker.label();
                var text = new SignText().setMessage(0,TextComponents.t(label.title()))
                        .setMessage(2,TextComponents.t(label.detail())).setColor(DyeColor.GREEN).setHasGlowingText(true);
                // ProtoChunk signs have no Level. Native text setters send live block updates
                // and crash during decoration; initialize through the native saved-data codec.
                try (var reporter = new ProblemReporter.ScopedCollector(sign.problemPath(), BunnidogsMod.LOGGER)) {
                    var output = TagValueOutput.createWithContext(reporter,world.registryAccess());
                    output.store("front_text",SignText.DIRECT_CODEC,text);
                    output.store("back_text",SignText.DIRECT_CODEC,text);
                    sign.loadCustomOnly(TagValueInput.create(reporter,world.registryAccess(),output.buildResult()));
                }
                sign.setChanged();
                // Debug-command placement also uses this helper, in an already loaded world.
                if (world instanceof ServerLevel level)
                    level.sendBlockUpdated(pos,sign.getBlockState(),sign.getBlockState(),2);
            }
        }
    }
}
