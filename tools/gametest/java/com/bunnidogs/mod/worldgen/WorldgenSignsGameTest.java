package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.layout.TunnelShape.Pos;
import com.bunnidogs.mod.layout.Wayfinding;
import com.bunnidogs.mod.network.TextComponents;
import com.bunnidogs.mod.registry.ModBlocks;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Real generation-stage sign creation: a ProtoChunk's block entities have no live Level yet. */
public final class WorldgenSignsGameTest {
    private WorldgenSignsGameTest() {}

    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        // Match the reported chunk without writing to the test world's loaded chunks or player saves.
        var chunkPos = new ChunkPos(-1, -24);
        var chunk = new ProtoChunk(chunkPos, UpgradeData.EMPTY, level,
                PalettedContainerFactory.create(level.registryAccess()), null);
        chunk.setPersistedStatus(ChunkStatus.CARVERS);
        var holder = new GenerationChunkHolder(chunkPos) {
            @Override public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus status) { return chunk; }
            @Override protected void addSaveDependency(CompletableFuture<?> sync) {}
            @Override public int getTicketLevel() { return 0; }
            @Override public int getQueueLevel() { return 0; }
        };
        var cache = StaticCache2D.<GenerationChunkHolder>create(chunkPos.x(), chunkPos.z(), 0, (x, z) -> holder);
        var region = new WorldGenRegion(level, cache, ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FEATURES), chunk);
        BlockPos origin = new BlockPos(chunkPos.getMinBlockX() + 3, 40, chunkPos.getMinBlockZ() + 3);
        var box = new BoundingBox(chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), level.getMaxY() - 1, chunkPos.getMaxBlockZ());

        for (var label : Wayfinding.Label.values()) {
            BlockPos pos = origin.offset((label.ordinal() % 4) * 3, 0, (label.ordinal() / 4) * 4);
            region.setBlock(pos.south(), Blocks.STONE.defaultBlockState(), 2);
            var data = Wayfinding.encode(List.of(new Wayfinding.Sign(new Pos(0, 0, 0), Wayfinding.Facing.NORTH, label)));
            // Cave features pass no box; saved structure pieces pass their owning chunk's box.
            WarrenSigns.place(region, pos, data, label.ordinal() % 2 == 0 ? null : box);
            h.assertTrue(region.getBlockState(pos).is(ModBlocks.GUAVA_WALL_SIGN.get()),
                    "Generation must place every supported room label");
            var sign = (SignBlockEntity) region.getBlockEntity(pos);
            h.assertTrue(sign != null && !sign.hasLevel(), "Generation-stage signs must remain unattached to a live Level");
            assertLabel(h, sign, label);
            var saved = sign.saveWithFullMetadata(level.registryAccess());
            var restored = (SignBlockEntity) BlockEntity.loadStatic(pos, sign.getBlockState(), saved, level.registryAccess());
            h.assertTrue(restored != null, "Generated signs must survive native block-entity serialization");
            assertLabel(h, restored, label);
        }
    }

    private static void assertLabel(GameTestHelper h, SignBlockEntity sign, Wayfinding.Label label) {
        for (boolean front : new boolean[]{true, false}) {
            var text = sign.getText(front);
            h.assertTrue(text.getMessage(0, false).equals(TextComponents.t(label.title()))
                            && text.getMessage(2, false).equals(TextComponents.t(label.detail()))
                            && text.getMessage(0, true).equals(TextComponents.t(label.title()))
                            && text.getColor() == DyeColor.GREEN && text.hasGlowingText(),
                    "Both sign faces must preserve translatable title/detail, filtered text, dye and glow");
        }
    }
}
