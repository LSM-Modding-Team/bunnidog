package com.bunnidogs.mod.worldgen;

import java.util.Arrays;
import java.util.List;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.SettlementFixtures;
import com.bunnidogs.mod.layout.TunnelShape.Pos;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class FixturesGameTest {
    private FixturesGameTest() {}
    private static BoundingBox chunk(BlockPos pos, int low, int high) {
        int x = pos.getX() & ~15, z = pos.getZ() & ~15;
        return new BoundingBox(x,low,z,x+15,high,z+15);
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(3,1,3));
        var bell = pos.south(2);
        for (var p : List.of(pos,bell)) {
            level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),2);
            level.setBlock(p,Blocks.AIR.defaultBlockState(),2);
            level.setBlock(p.above(),Blocks.AIR.defaultBlockState(),2);
        }
        var fixtures = List.of(new SettlementFixtures.Fixture(new Pos(0,0,0),SettlementFixtures.Kind.TROUGH),
                new SettlementFixtures.Fixture(new Pos(0,0,2),SettlementFixtures.Kind.BELL));
        var data = SettlementFixtures.encode(fixtures);
        var owner = chunk(pos,level.getMinY(),level.getMaxY()-1);
        var foreign = new BoundingBox(owner.minX()+16,owner.minY(),owner.minZ(),owner.maxX()+16,owner.maxY(),owner.maxZ());
        WarrenFixtures.place(level,pos,data,foreign);
        h.assertTrue(level.getBlockState(pos).isAir(),"Another chunk must not write a trough");
        WarrenFixtures.place(level,pos,data,owner);
        WarrenFixtures.place(level,pos,data,chunk(bell,level.getMinY(),level.getMaxY()-1));
        h.assertTrue(level.getBlockState(pos).is(ModBlocks.FEEDING_TROUGH.get()) && level.getBlockState(bell).is(Blocks.BELL),
                "Owning columns must place their trough and native bell");
        var stock = (FeedingTroughBlockEntity)level.getBlockEntity(pos);
        h.assertTrue(stock.food().is(ModItems.GUAVA.get()) && stock.food().getCount() == 2
                && stock.getBlockState().getValue(FeedingTroughBlock.FILL) == 1
                && stock.getBlockState().getValue(FeedingTroughBlock.TENDING),"New troughs need starter food, visible fill and enabled gardening");
        stock.takeAll();
        var tunnel = new int[]{0,-1,0,DenShape.Kind.FILL.ordinal(),0,0,0,DenShape.Kind.AIR.ordinal(),0,1,0,DenShape.Kind.AIR.ordinal()};
        WarrenTunnels.place(level,pos,tunnel,owner,Lineage.MEADOW);
        WarrenFixtures.place(level,pos,data,owner);
        h.assertTrue(level.getBlockEntity(pos) == stock && stock.food().isEmpty(),"Repeat tunnel/furniture placement must preserve the trough without refilling it");
        level.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);
        WarrenFixtures.place(level,pos,data,owner);
        h.assertTrue(level.getBlockState(pos).is(Blocks.CHEST),"Furniture must preserve existing player containers");
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        level.setBlock(pos.below(),Blocks.AIR.defaultBlockState(),2);
        WarrenFixtures.place(level,pos,data,owner);
        h.assertTrue(level.getBlockState(pos).isAir(),"Missing support must omit furniture");
        var context = StructurePieceSerializationContext.fromLevel(level);
        var piece = new WarrenPiece(WarrenPiece.TUNNEL,0,7,3,pos.getX(),pos.getZ(),pos.getY(),false,
                tunnel,new int[0],new int[0],data);
        var saved = piece.createTag(context);
        var restored = new WarrenPiece(saved).createTag(context);
        h.assertTrue(Arrays.equals(restored.getIntArray("LifeFixtures").orElseThrow(),data),"Native structure saves must preserve optional furniture");
        saved.remove("LifeFixtures"); saved.putInt("WarrenVersion",2);
        var legacy = new WarrenPiece(saved).createTag(context);
        h.assertTrue(!legacy.contains("LifeFixtures") && legacy.getIntOr("WarrenVersion",0) == 2,
                "Old saved starts must retain their version and empty furniture plan");
    }
}
