package com.bunnidogs.mod.society;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** A renewable, visibly growing guava coppice; harvesting regrows the same rooted plant. */
public final class CoppiceBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE=IntegerProperty.create("age",0,7);
    public CoppiceBlock(Properties p) {super(p);registerDefaultState(stateDefinition.any().setValue(AGE,0));}
    @Override public com.mojang.serialization.MapCodec<BushBlock> codec() {return simpleCodec(CoppiceBlock::new);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) {b.add(AGE);}
    @Override protected boolean isRandomlyTicking(BlockState s) {return s.getValue(AGE)<7;}
    @Override public void randomTick(BlockState s,ServerLevel level,BlockPos pos,RandomSource r) {if(level.getRawBrightness(pos,0)>=9 && r.nextInt(5)==0)level.setBlock(pos,s.setValue(AGE,Math.min(7,s.getValue(AGE)+1)),2);}
    @Override public boolean isValidBonemealTarget(LevelReader l,BlockPos p,BlockState s) {return s.getValue(AGE)<7;}
    @Override public boolean isBonemealSuccess(Level l,RandomSource r,BlockPos p,BlockState s) {return true;}
    @Override public void performBonemeal(ServerLevel l,RandomSource r,BlockPos p,BlockState s) {l.setBlock(p,s.setValue(AGE,Math.min(7,s.getValue(AGE)+2)),2);}
}
