package com.bunnidogs.mod.block;

import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A guava hanging under the leaves of a guava tree (Phase 4g). Three ages: green, yellowing, ripe. It grows on random ticks,
 * slowly (1 in {@value #GROWTH_ODDS} random ticks per age, about 10 minutes from green to ripe at the default random tick speed).
 * Right-clicking a ripe one picks it (one plain guava) and sends it back to green, so a tree keeps giving; breaking a ripe one drops
 * a guava too (the loot table {@code data/bunnidogs/loot_table/blocks/guava_fruit.json}), an unripe one drops nothing. It needs leaves
 * above it: when they are gone it falls off at once (a scheduled tick after the shape update, F1); a ripe one drops its guava, a green one nothing. A random tick is the safety net.
 * Plain guavas can plant green fruit beneath any leaves; native bone meal advances one age per application.
 */
public class GuavaFruitBlock extends Block implements BonemealableBlock {
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    /** On average 1 random tick in this many grows the fruit one age. */
    public static final int GROWTH_ODDS = 5;

    private static final VoxelShape SHAPE = Shapes.box(4.0D / 16.0D, 4.0D / 16.0D, 4.0D / 16.0D, 12.0D / 16.0D, 1.0D, 12.0D / 16.0D);

    public GuavaFruitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public static boolean isRipe(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    /** A plain guava can establish a green fruit only in dry, loaded air beneath leaves. */
    public static boolean canPlant(Level level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && !level.isOutsideBuildHeight(pos.above())
                && level.hasChunkAt(pos) && level.hasChunkAt(pos.above()) && level.getWorldBorder().isWithinBounds(pos)
                && level.getBlockState(pos).isAir() && level.getFluidState(pos).isEmpty()
                && ModBlocks.GUAVA_FRUIT.get().defaultBlockState().canSurvive(level,pos);
    }

    public static boolean plant(ServerLevel level, BlockPos pos) {
        return canPlant(level,pos) && level.setBlock(pos,ModBlocks.GUAVA_FRUIT.get().defaultBlockState(),3);
    }

    /** Reset before paying the fruit; hand and dispenser harvesting share support and stale-state checks. */
    public static boolean pick(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || !level.hasChunkAt(pos.above()) || !level.getWorldBorder().isWithinBounds(pos)) return false;
        var state = level.getBlockState(pos);
        if (!state.is(ModBlocks.GUAVA_FRUIT.get()) || !isRipe(state) || !state.canSurvive(level,pos)
                || !level.setBlock(pos,state.setValue(AGE,0),2)) return false;
        Block.popResource(level,pos,new ItemStack(ModItems.GUAVA.get()));
        return true;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.is(this) && !isRipe(state) && state.equals(level.getBlockState(pos)) && canSurvive(state,level,pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return isValidBonemealTarget(level,pos,state);
    }

    /** One successful native hand/dispenser application advances exactly one age. */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isValidBonemealTarget(level,pos,state)) level.setBlock(pos,state.setValue(AGE,state.getValue(AGE)+1),2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Hangs from leaves. */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof LeavesBlock;
    }

    /**
     * F1 (docs/FIXES_PLAN.md): the leaf above went (broken, decayed, pushed, blown up) -> check on the next tick, the vanilla way
     * (bamboo, cactus): a shape update cannot drop items, a scheduled tick can.
     */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !this.canSurvive(state, level, pos)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    /** The scheduled check of {@link #updateShape}: a fruit with no leaf above falls off; the loot table gives a guava only when it was ripe. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        int age = state.getValue(AGE);
        if (age < MAX_AGE && random.nextInt(GROWTH_ODDS) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    /** Picks a ripe guava (one plain guava, back to green); an unripe one ignores the click. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!isRipe(state)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            pick((ServerLevel)level,pos);
        }
        return InteractionResult.SUCCESS;
    }
}
