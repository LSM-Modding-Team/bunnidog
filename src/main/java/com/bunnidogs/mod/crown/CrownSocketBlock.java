package com.bunnidogs.mod.crown;

import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Two seats flank the throne gate; the optional third seat belongs to the separate archive. */
public final class CrownSocketBlock extends CrownGateBlock {
    public static final IntegerProperty KEY=IntegerProperty.create("key",0,3);
    public CrownSocketBlock(Properties p) {super(p);registerDefaultState(defaultBlockState().setValue(KEY,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b) {super.createBlockStateDefinition(b);b.add(KEY);}
}
