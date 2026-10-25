package com.bunnidogs.mod.realm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Expedition masonry withstands the Apex family's deliberate terrain attacks, while remaining player-mineable. */
public final class RootstoneBlock extends Block {
    public RootstoneBlock(Properties properties) {super(properties);}
    @Override public boolean canEntityDestroy(BlockState state,BlockGetter level,BlockPos pos,Entity entity) {return false;}
}
