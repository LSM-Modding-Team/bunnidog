package com.bunnidogs.mod.worldgen;

import java.util.List;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.PropShape;
import com.bunnidogs.mod.layout.RegionalFarms;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class RegionalFarmsGameTest {
    private RegionalFarmsGameTest() {}
    private static void clear(GameTestHelper h) {
        for (int x = 1; x < 8; x++) for (int z = 1; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.GRASS_BLOCK);
            for (int y = 1; y <= 8; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel(); var centre = h.absolutePos(new BlockPos(4,1,4));
        for (var style : List.of(Lineage.DESERT,Lineage.NETHER,Lineage.SWAMP,Lineage.CAVE,Lineage.END)) {
            clear(h); int turns = style.ordinal()%4;
            var prop = new Settlement.Prop(PropShape.Kind.FARM,0,0,turns);
            boolean surface = style != Lineage.CAVE && style != Lineage.NETHER;
            h.assertTrue(WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,surface,style,true,true),"Regional farm must fit in its native surface/cave mode: "+style);
            var cells = RegionalFarms.cells(style,turns,true);
            var troughCell = cells.stream().filter(c -> c.mat() == PropShape.Mat.TROUGH).findFirst().orElseThrow();
            var troughPos = centre.offset(troughCell.dx(),0,troughCell.dz());
            var trough = (FeedingTroughBlockEntity)level.getBlockEntity(troughPos);
            h.assertTrue(trough != null && trough.food().isEmpty() && trough.getBlockState().getValue(FeedingTroughBlock.TENDING),"Regional troughs start empty/enabled");
            for (var cell : cells) {
                var pos = centre.offset(cell.dx(),cell.dy(),cell.dz()); var state = level.getBlockState(pos);
                h.assertTrue(state.canSurvive(level,pos),"Native regional plant/support must survive: "+style+" / "+cell+" / "+state);
                h.assertTrue(level.getFluidState(pos).isEmpty(),"Regional farms must not introduce unsupported water");
            }
            if (style == Lineage.DESERT) h.assertTrue(level.getBlockState(centre).is(Blocks.CACTUS)
                    && level.getBlockState(centre.above()).is(Blocks.CACTUS) && level.getBlockState(centre.below()).is(Blocks.SAND),"Desert plots need a spare cactus above sand");
            if (style == Lineage.NETHER) h.assertTrue(cells.stream().filter(c -> c.mat() == PropShape.Mat.SOUL_SAND).count() == 8,"Wart must grow on actual soul sand");
            trough.insert(new ItemStack(Items.CARROT,3));
            h.assertTrue(!WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,surface,style,true,true)
                    && level.getBlockEntity(troughPos) == trough && trough.food().getCount() == 3,
                    "Repeated regional placement must not stack new plots or replace existing stock");
            if (style == Lineage.CAVE) h.assertTrue(!WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,style,true,true)
                    && level.getBlockEntity(troughPos) == trough && trough.food().getCount() == 3,"Surface-ground probing must also refuse the existing trellis roof without stacking plots");
        }
        clear(h);
        var prop = new Settlement.Prop(PropShape.Kind.FARM,0,0,0);
        h.assertTrue(WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.DESERT,false,false)
                && level.getBlockState(centre.offset(-1,0,-1)).is(Blocks.WHEAT),"Old desert starts retain the previous crop plan");
        clear(h); var obstruction = centre.above(2); level.setBlock(obstruction,Blocks.CHEST.defaultBlockState(),3);
        var chest = level.getBlockEntity(obstruction);
        h.assertTrue(!WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.CAVE,true,true)
                && level.getBlockEntity(obstruction) == chest && level.getBlockState(centre.below()).is(Blocks.GRASS_BLOCK),"A trellis obstruction must refuse before any ground/plant/container edits");
        var context = StructurePieceSerializationContext.fromLevel(level);
        var piece = new WarrenPiece(WarrenPiece.PROP,0,7,5,centre.getX(),centre.getZ(),centre.getY());
        var saved = piece.createTag(context);
        h.assertTrue(saved.getIntOr("WarrenVersion",0) == 5 && new WarrenPiece(saved).createTag(context).getIntOr("WarrenVersion",0) == 5,
                "New starts retain regional generation version five through native saves");
        saved.putInt("WarrenVersion",4);
        h.assertTrue(new WarrenPiece(saved).createTag(context).getIntOr("WarrenVersion",0) == 4,"Old starts must never be upgraded during load");
    }
}
