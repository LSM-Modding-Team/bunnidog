package com.bunnidogs.mod.block;

import java.util.List;
import java.util.Random;

import com.bunnidogs.mod.layout.TreeShape;
import com.bunnidogs.mod.worldgen.GuavaTree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The guava sapling (Phase 4g), crafted from one guava: what makes guavas renewable (the rarity is in finding the first warren, not
 * in a hard cap). Needs dirt or farmland below. Grows into a small guava tree ({@link TreeShape}, built by {@link GuavaTree}) on random
 * ticks, slowly (1 in {@value #GROWTH_ODDS} random ticks) and only if the trunk has room; bone meal tries at once (45%). No light needed.
 * Support loss recovers one sapling on the next scheduled tick. Growth requires a supported sapling and a loaded, bounded tree plan.
 */
public class GuavaSaplingBlock extends Block implements BonemealableBlock {
    /** On average 1 random tick in this many tries to grow the tree. */
    public static final int GROWTH_ODDS = 20;
    /** Chance that one bone meal makes the tree grow (a vanilla sapling: 45%). */
    public static final float BONEMEAL_CHANCE = 0.45F;

    private static final VoxelShape SHAPE = Shapes.box(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 12.0D / 16.0D, 14.0D / 16.0D);

    public GuavaSaplingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var below = pos.below(); var soil = level.getBlockState(below);
        var decision = soil.canSustainPlant(level,below,Direction.UP,state);
        return !decision.isDefault() ? decision.isTrue() : soil.is(BlockTags.SUPPORTS_VEGETATION);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !canSurvive(state,level,pos)) ticks.scheduleTick(pos,this,1);
        return super.updateShape(state,level,ticks,pos,direction,neighborPos,neighborState,random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state,level,pos)) level.destroyBlock(pos,true);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos,true);
            return;
        }
        if (random.nextInt(GROWTH_ODDS) == 0) {
            tryGrow(level, pos, random);
        }
    }

    /** Rolls a tree and plants it in place of the sapling if the trunk has room. Returns whether it grew. */
    public static boolean tryGrow(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.hasChunkAt(pos)) return false;
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof GuavaSaplingBlock) || !state.canSurvive(level,pos)) return false;
        List<TreeShape.Cell> cells = TreeShape.generate(new Random(random.nextLong()));
        for (var cell : cells) {
            var target = pos.offset(cell.dx(),cell.dy(),cell.dz());
            if (!level.hasChunkAt(target) || level.isOutsideBuildHeight(target) || !level.getWorldBorder().isWithinBounds(target)) return false;
        }
        if (!GuavaTree.fits(level, pos, cells)) {
            return false;
        }
        GuavaTree.place(level, pos, cells,false);
        return true;
    }

    // ------------------------------------------------------------------ bone meal (BonemealableBlock)

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.is(this) && state.equals(level.getBlockState(pos)) && canSurvive(state,level,pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return isValidBonemealTarget(level,pos,state) && random.nextFloat() < BONEMEAL_CHANCE;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        tryGrow(level, pos, random);
    }
}
