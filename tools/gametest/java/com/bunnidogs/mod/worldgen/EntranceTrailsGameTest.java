package com.bunnidogs.mod.worldgen;

import java.util.Random;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.EntranceTrail;
import com.bunnidogs.mod.registry.ModBiomeTags;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Actual new/legacy structure generation, whole-route fitting and preservation of unsafe columns. */
public final class EntranceTrailsGameTest {
    private EntranceTrailsGameTest() {}
    private static void terrain(GameTestHelper h, BlockPos centre) {
        var level = h.getLevel();
        for (int x = -11; x <= 11; x++) for (int z = -11; z <= 11; z++) {
            for (int y = -9; y < -1; y++) level.setBlock(centre.offset(x,y,z),Blocks.DIRT.defaultBlockState(),2);
            level.setBlock(centre.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState(),2);
            for (int y = 0; y <= 8; y++) level.setBlock(centre.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
        }
    }
    private static long paths(GameTestHelper h, BlockPos centre, Lineage style) {
        int count = 0;
        for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++)
            if (h.getLevel().getBlockState(centre.offset(x,-1,z)).is(WarrenPalette.get(style,WarrenPalette.Role.PATH).getBlock())) count++;
        return count;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel(); var centre = h.absolutePos(new BlockPos(4,24,4)); terrain(h,centre);
        var context = StructurePieceSerializationContext.fromLevel(level);
        var piece = new WarrenPiece(WarrenPiece.CLUSTER,0,23,1,centre.getX(),centre.getZ(),centre.getY());
        var saved = piece.createTag(context); var current = new WarrenPiece(saved);
        h.assertTrue(saved.getIntOr("WarrenVersion",0) == 7 && current.getBoundingBox().minX() == centre.getX()-EntranceTrail.REACH,
                "Fresh structure saves must retain version seven and contain the complete outer doorstep trail");
        var chunk = new ChunkPos(centre.getX() >> 4,centre.getZ() >> 4);
        var owner = new BoundingBox(chunk.getMinBlockX(),level.getMinY(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),level.getMaxY()-1,chunk.getMaxBlockZ());
        current.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(3),owner,chunk,centre);
        var style = ModBiomeTags.lineageAt(level,centre);
        h.assertTrue(paths(h,centre,style) > 0 && level.getBlockState(centre).is(ModBlocks.GUAVA_LOG.get()),
                "The actual level-one structure piece must add entrance trails and keep its tree");
        var den = DenShape.generate(new Random(23));
        for (var dir : den.dirs().subList(0,2)) {
            var route = EntranceTrail.route(dir,7,1);
            h.assertTrue(route.stream().allMatch(p -> level.getBlockState(centre.offset(p.dx(),-1,p.dz())).is(WarrenPalette.get(style,WarrenPalette.Role.PATH).getBlock())),
                    "Each actual fitted mouth must get a complete path round its mound");
            var door = DenShape.at(dir,7,0);
            h.assertTrue(level.getBlockState(centre.offset(door.dx(),0,door.dz())).isAir(),"The mouth opening must stay clear");
            for (var cell : DenShape.mound(dir,7,0)) h.assertTrue(level.getBlockState(centre.offset(cell.dx(),cell.dy(),cell.dz())).equals(WarrenTunnels.state(cell.kind(),style)),
                    "A trail must never overwrite the mouth wall, ridge or doorway frame");
        }
        h.assertTrue(den.furnishing().stream().filter(c -> c.kind() == DenShape.Kind.NEST)
                .allMatch(c -> level.getBlockState(centre.offset(c.dx(),c.dy(),c.dz())).is(ModBlocks.NEST.get())),"Floor Nests must remain intact");
        terrain(h,centre); saved.putInt("WarrenVersion",6); var legacy = new WarrenPiece(saved);
        legacy.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),RandomSource.create(3),owner,chunk,centre);
        h.assertTrue(paths(h,centre,style) == 0 && legacy.createTag(context).getIntOr("WarrenVersion",0) == 6,
                "An old saved start must keep its exact previous layout without added trails");

        terrain(h,centre);
        var obstacle = centre.offset(6,0,2); level.setBlock(obstacle,Blocks.CHEST.defaultBlockState(),2);
        var chest = (ChestBlockEntity)level.getBlockEntity(obstacle); chest.setItem(0,new ItemStack(Items.DIAMOND,3));
        h.assertTrue(WarrenTrails.place(level,centre,Lineage.MEADOW,DenShape.Dir.EAST,7,0)
                && level.getBlockEntity(obstacle) == chest && chest.getItem(0).getCount() == 3,
                "An obstructed preferred side must use the opposite whole route and preserve container stock");
        h.assertTrue(EntranceTrail.route(DenShape.Dir.EAST,7,-1).stream().allMatch(p -> level.getBlockState(centre.offset(p.dx(),-1,p.dz())).is(Blocks.DIRT_PATH))
                && level.getBlockState(centre.offset(7,-1,2)).is(Blocks.GRASS_BLOCK),"A rejected alternative must leave no partial trail");
        terrain(h,centre); var doorstep = centre.offset(8,0,0); level.setBlock(doorstep,Blocks.WATER.defaultBlockState(),2);
        h.assertTrue(!WarrenTrails.place(level,centre,Lineage.MEADOW,DenShape.Dir.EAST,7,0)
                && paths(h,centre,Lineage.MEADOW) == 0 && level.getBlockState(doorstep).is(Blocks.WATER),
                "A fluid blocking both alternatives must preserve every ground column and the fluid");
        terrain(h,centre); level.setBlock(centre.offset(8,-1,0),Blocks.DIAMOND_ORE.defaultBlockState(),2);
        h.assertTrue(!WarrenTrails.place(level,centre,Lineage.DESERT,DenShape.Dir.EAST,7,0)
                && paths(h,centre,Lineage.DESERT) == 0 && level.getBlockState(centre.offset(8,-1,0)).is(Blocks.DIAMOND_ORE),
                "Even solid non-meadow paths must preserve ore rather than pave it");
    }
}
