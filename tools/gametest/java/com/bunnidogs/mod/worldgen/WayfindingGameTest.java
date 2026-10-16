package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.layout.TunnelShape.Pos;
import com.bunnidogs.mod.layout.Wayfinding;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import java.util.List;

/** Saved worldgen labels must respect chunk ownership and preserve player edits on a repeated placement. */
public final class WayfindingGameTest {
    private WayfindingGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        BlockPos anchor = h.absolutePos(new BlockPos(0,2,0));
        BlockPos pos = new BlockPos((anchor.getX() >> 4)*16+8,anchor.getY(),(anchor.getZ() >> 4)*16+8);
        var ownChunk = new BoundingBox(pos.getX() & ~15,level.getMinY(),pos.getZ() & ~15,
                (pos.getX() & ~15)+15,level.getMaxY()-1,(pos.getZ() & ~15)+15);
        var marker = new Wayfinding.Sign(new Pos(0,0,0),Wayfinding.Facing.NORTH,Wayfinding.Label.PANTRY);
        var data = Wayfinding.encode(List.of(marker));
        level.setBlock(pos.south(),Blocks.STONE.defaultBlockState(),2);
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        var foreign = new BoundingBox(ownChunk.minX()+16,ownChunk.minY(),ownChunk.minZ(),ownChunk.maxX()+16,ownChunk.maxY(),ownChunk.maxZ());
        WarrenSigns.place(level,pos,data,foreign);
        h.assertTrue(level.getBlockState(pos).isAir(),"Another chunk must not place this label");
        WarrenSigns.place(level,pos,data,ownChunk);
        h.assertTrue(level.getBlockState(pos).is(ModBlocks.GUAVA_WALL_SIGN.get()),"Owning chunk must place its guava sign");
        var sign = (SignBlockEntity)level.getBlockEntity(pos);
        h.assertTrue(sign != null && sign.getFrontText().getMessage(0,false).getString().equals("Pantry")
                && sign.getFrontText().hasGlowingText(),"Generated sign must carry the saved readable room label");
        sign.setText(sign.getFrontText().setMessage(0,Component.literal("Player's pantry")),true);
        WarrenSigns.place(level,pos,data,ownChunk);
        h.assertTrue(sign.getFrontText().getMessage(0,false).getString().equals("Player's pantry"),"Repeated generation must preserve edited sign text");
        level.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);
        WarrenSigns.place(level,pos,data,ownChunk);
        h.assertTrue(level.getBlockState(pos).is(Blocks.CHEST),"Labels must not replace existing containers");
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        level.setBlock(pos.south(),Blocks.AIR.defaultBlockState(),2);
        WarrenSigns.place(level,pos,data,ownChunk);
        h.assertTrue(level.getBlockState(pos).isAir(),"A skipped/missing support must omit only the label");
        h.assertTrue(Wayfinding.decode(new int[0]).isEmpty(),"Old starts without sign data must remain empty");
    }
}
