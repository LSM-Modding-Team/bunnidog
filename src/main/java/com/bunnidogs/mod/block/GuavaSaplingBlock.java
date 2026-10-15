package com.bunnidogs.mod.block;

import java.util.List;
import java.util.Random;

import com.bunnidogs.mod.layout.TreeShape;
import com.bunnidogs.mod.worldgen.GuavaTree;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The guava sapling (Phase 4g), crafted from one guava: what makes guavas renewable (the rarity is in finding the first warren, not
 * in a hard cap). Needs dirt or farmland below. Grows into a small guava tree ({@link TreeShape}, built by {@link GuavaTree}) on random
 * ticks, slowly (1 in {@value #GROWTH_ODDS} random ticks) and only if the trunk has room; bone meal tries at once (45%). No light needed.
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
        BlockState soil = level.getBlockState(pos.below());
        return soil.is(Blocks.FARMLAND) || soil.is(BlockTags.DIRT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (random.nextInt(GROWTH_ODDS) == 0) {
            tryGrow(level, pos, random);
        }
    }

    /** Rolls a tree and plants it in place of the sapling if the trunk has room. Returns whether it grew. */
    public static boolean tryGrow(ServerLevel level, BlockPos pos, RandomSource random) {
        List<TreeShape.Cell> cells = TreeShape.generate(new Random(random.nextLong()));
        if (!GuavaTree.fits(level, pos, cells)) {
            return false;
        }
        GuavaTree.place(level, pos, cells);
        return true;
    }

    // ------------------------------------------------------------------ bone meal (BonemealableBlock)

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < BONEMEAL_CHANCE;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        tryGrow(level, pos, random);
    }
}
