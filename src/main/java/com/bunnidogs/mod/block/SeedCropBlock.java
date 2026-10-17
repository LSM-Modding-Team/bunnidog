package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A planted bunnidog seed (Phase 4d). It grows through the ages 0-7 on random ticks; at age 7 it is ripe. Its
 * {@link SeedCropBlockEntity} holds the {@link Seed}; what breaking it gives is decided in {@link SeedCropHarvest}
 * (unripe: the seed comes back untouched; ripe: the litter is born, Fortune raises its quality).
 *
 * <p>Not a vanilla {@code CropBlock}, so it does its own upkeep: it survives on farmland or any dirt-like block (a trampled
 * field does not lose the seed), and when the soil is gone it pops off, giving the seed back, on its next random tick.
 * Bone meal (session 6) works through vanilla's {@link BonemealableBlock}, so dispensers can use it too: it grows the
 * crop 2-5 ages at once, like wheat. Starlit Dust (D7b) ripens it at once ({@link #ripen}). Not done yet: a light requirement, right-click harvesting.
 *
 * <p>Looks (session 6): the crop is a developing embryo, one distinct picture per age (see {@code art/gen_seed_crop.py}).
 */
public class SeedCropBlock extends Block implements EntityBlock, BonemealableBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    /** On average 1 random tick in this many grows the crop one age (a wheat-like crop but about twice as fast). */
    public static final int GROWTH_ODDS = 3;

    /** One bone meal grows the crop by this many ages at least / at most. */
    public static final int BONEMEAL_MIN_AGES = 2;
    public static final int BONEMEAL_MAX_AGES = 5;

    private static final VoxelShape[] SHAPES = new VoxelShape[MAX_AGE + 1];

    static {
        for (int age = 0; age <= MAX_AGE; age++) {
            SHAPES[age] = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (age + 1) * 2.0D / 16.0D, 1.0D);
        }
    }

    public SeedCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public static boolean isRipe(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        return soil.is(Blocks.FARMLAND) || soil.is(BlockTags.DIRT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            if (level.getBlockEntity(pos) instanceof SeedCropBlockEntity crop) {
                var stack = crop.seedStack();
                if (!stack.isEmpty()) dropSeed(level,pos,stack);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        int age = state.getValue(AGE);
        if (age < MAX_AGE && random.nextInt(GROWTH_ODDS) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    // ------------------------------------------------------------------ bone meal (BonemealableBlock)

    /** Bone meal works until the crop is ripe. */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !isRipe(state);
    }

    /** Always succeeds (vanilla's crops do too). */
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    /** Grows {@link #boneMealAges} ages at once, never past ripe. The block entity (the seed) stays: only the state changes. */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = Math.min(MAX_AGE, state.getValue(AGE) + boneMealAges(random));
        level.setBlock(pos, state.setValue(AGE, age), 2);
    }

    /** Shared hand/dispenser placement; consume the source only after the crop remembers the complete litter. */
    public static boolean plant(ServerLevel level, BlockPos pos, Seed seed) {
        return plant(level,pos,SeedItem.create(seed));
    }
    public static boolean plant(ServerLevel level, BlockPos pos, net.minecraft.world.item.ItemStack stack) {
        if (SeedItem.read(stack).isEmpty()) return false;
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || !level.hasChunkAt(pos) || !level.hasChunkAt(pos.below()) || !level.getBlockState(pos).isAir()
                || !level.getBlockState(pos.below()).is(Blocks.FARMLAND)) return false;
        var planted = ModBlocks.SEED_CROP.get().defaultBlockState();
        if (!level.setBlock(pos,planted,3)) return false;
        if (!(level.getBlockEntity(pos) instanceof SeedCropBlockEntity crop)) {
            if (level.getBlockState(pos).equals(planted)) level.removeBlock(pos,false);
            return false;
        }
        crop.setSeedStack(stack);
        return true;
    }

    /** Starlit Dust changes only a valid saved crop's age; refusal retains the consumable. */
    public static boolean ripen(Level level, BlockPos pos, BlockState state) {
        return level instanceof ServerLevel && level.hasChunkAt(pos) && level.getWorldBorder().isWithinBounds(pos)
                && state.getBlock() instanceof SeedCropBlock && !isRipe(state) && state.equals(level.getBlockState(pos))
                && state.canSurvive(level,pos) && level.getBlockEntity(pos) instanceof SeedCropBlockEntity crop && crop.seed().isPresent()
                && level.setBlock(pos,state.setValue(AGE,MAX_AGE),2);
    }

    /** How many ages one bone meal adds: 2 to 5, the same spread as a vanilla wheat-style crop with 8 ages. */
    public static int boneMealAges(RandomSource random) {
        return BONEMEAL_MIN_AGES + random.nextInt(BONEMEAL_MAX_AGES - BONEMEAL_MIN_AGES + 1);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeedCropBlockEntity(pos, state);
    }

    /** Drops a seed item holding this payload in the middle of the block. */
    public static void dropSeed(ServerLevel level, BlockPos pos, Seed seed) {
        dropSeed(level,pos,SeedItem.create(seed));
    }
    public static void dropSeed(ServerLevel level, BlockPos pos, net.minecraft.world.item.ItemStack stack) {
        if (!stack.isEmpty()) level.addFreshEntity(new ItemEntity(level,pos.getX()+.5,pos.getY()+.3,pos.getZ()+.5,stack.copyWithCount(1)));
    }
}
