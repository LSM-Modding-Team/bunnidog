package com.bunnidogs.mod.worldgen;

import java.util.Arrays;
import java.util.List;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.NurseryCradleBlock;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.PropShape;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.layout.SettlementFixtures;
import com.bunnidogs.mod.layout.TunnelShape.Pos;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
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
        var nursery = pos.east(2);
        for (var p : List.of(pos,bell,nursery)) {
            level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),2);
            level.setBlock(p,Blocks.AIR.defaultBlockState(),2);
            level.setBlock(p.above(),Blocks.AIR.defaultBlockState(),2);
        }
        var fixtures = List.of(new SettlementFixtures.Fixture(new Pos(0,0,0),SettlementFixtures.Kind.TROUGH),
                new SettlementFixtures.Fixture(new Pos(0,0,2),SettlementFixtures.Kind.BELL),
                new SettlementFixtures.Fixture(new Pos(2,0,0),SettlementFixtures.Kind.NURSERY_CRADLE));
        var data = SettlementFixtures.encode(fixtures);
        var owner = chunk(pos,level.getMinY(),level.getMaxY()-1);
        var foreign = new BoundingBox(owner.minX()+16,owner.minY(),owner.minZ(),owner.maxX()+16,owner.maxY(),owner.maxZ());
        WarrenFixtures.place(level,pos,data,foreign);
        h.assertTrue(level.getBlockState(pos).isAir(),"Another chunk must not write a trough");
        WarrenFixtures.place(level,pos,data,owner);
        WarrenFixtures.place(level,pos,data,chunk(bell,level.getMinY(),level.getMaxY()-1));
        WarrenFixtures.place(level,pos,data,chunk(nursery,level.getMinY(),level.getMaxY()-1));
        h.assertTrue(level.getBlockState(pos).is(ModBlocks.FEEDING_TROUGH.get()) && level.getBlockState(bell).is(Blocks.BELL),
                "Owning columns must place their trough and native bell");
        var cradle = (NurseryCradleBlockEntity)level.getBlockEntity(nursery);
        h.assertTrue(cradle != null && cradle.storedSeed().isEmpty() && cradle.getBlockState().getValue(NurseryCradleBlock.FACING) == Direction.EAST,
                "The owning column must place an empty east-facing nursery Cradle");
        var litter = SeedItem.create(new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,2.5)); cradle.insert(litter); cradle.grow(false);
        int stage = cradle.age(), wait = cradle.growthWait();
        var nurseryTunnel = new int[]{2,-1,0,DenShape.Kind.FILL.ordinal(),2,0,0,DenShape.Kind.AIR.ordinal(),2,1,0,DenShape.Kind.AIR.ordinal()};
        WarrenTunnels.place(level,pos,nurseryTunnel,chunk(nursery,level.getMinY(),level.getMaxY()-1),Lineage.MEADOW);
        WarrenFixtures.place(level,pos,data,chunk(nursery,level.getMinY(),level.getMaxY()-1));
        h.assertTrue(level.getBlockEntity(nursery) == cradle && ItemStack.matches(cradle.storedSeed(),litter) && cradle.age() == stage && cradle.growthWait() == wait,
                "Repeated tunnel/furniture placement must preserve the Cradle's exact seed and incubation");
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
        farm(h);
    }
    private static void farm(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 1; x < 8; x++) for (int z = 1; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.GRASS_BLOCK);
            // Surface fitting scans above the template; remove its native barrier roof too.
            for (int y = 1; y <= 8; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var centre = h.absolutePos(new BlockPos(4,1,4));
        var prop = new Settlement.Prop(PropShape.Kind.FARM,0,0,0);
        var corner = centre.offset(-1,0,-1);
        // Legacy starts use exactly their old crop layout.
        h.assertTrue(WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.MEADOW),
                "Legacy farms must still fit");
        h.assertTrue(level.getBlockState(corner).is(Blocks.WHEAT) && level.getBlockState(corner.below()).is(Blocks.FARMLAND),
                "Old farm generation must keep its corner crop and soil: " + level.getBlockState(corner) + " / " + level.getBlockState(corner.below()));
        for (int x = 3; x <= 5; x++) for (int z = 3; z <= 5; z++) {
            h.setBlock(x,0,z,Blocks.GRASS_BLOCK); h.setBlock(x,1,z,Blocks.AIR);
        }
        h.setBlock(2,1,3,Blocks.CHEST);
        h.assertTrue(WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.MEADOW,true)
                && level.getBlockState(corner).is(Blocks.WHEAT) && level.getBlockState(corner.west()).is(Blocks.CHEST),
                "Blocked walking space must omit only the trough and preserve the obstacle");
        for (int x = 3; x <= 5; x++) for (int z = 3; z <= 5; z++) {
            h.setBlock(x,0,z,Blocks.GRASS_BLOCK); h.setBlock(x,1,z,Blocks.AIR);
        }
        h.setBlock(2,1,3,Blocks.AIR);
        h.assertTrue(WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.MEADOW,true),
                "A safely fitted new farm must build");
        var stock = (FeedingTroughBlockEntity)level.getBlockEntity(corner);
        h.assertTrue(stock != null && stock.food().isEmpty() && stock.getBlockState().getValue(FeedingTroughBlock.TENDING)
                && stock.getBlockState().getValue(FeedingTroughBlock.FILL) == 0
                && level.getBlockState(corner.below()).isFaceSturdy(level,corner.below(),net.minecraft.core.Direction.UP),
                "A new farm needs an empty enabled trough on solid support, leaving stock type open");
        stock.insert(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CARROT,3));
        WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.MEADOW,true);
        h.assertTrue(level.getBlockEntity(corner) == stock && stock.food().getCount() == 3,
                "Repeat farm placement must preserve the existing trough and stock");
    }
}
