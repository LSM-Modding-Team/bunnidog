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

/** Flowering, green and ripe bunches. Living roots and mulch improve real yields;
 * legacy isolated fruit retains any-leaf support and a one-fruit crop. */
public class GuavaFruitBlock extends Block implements BonemealableBlock {
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final IntegerProperty BUNCH = IntegerProperty.create("bunch",1,3);
    /** On average 1 random tick in this many grows the fruit one age. */
    public static final int GROWTH_ODDS = 5;

    private static final VoxelShape SHAPE = Shapes.box(4.0D / 16.0D, 4.0D / 16.0D, 4.0D / 16.0D, 12.0D / 16.0D, 1.0D, 12.0D / 16.0D);

    public GuavaFruitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(BUNCH,1));
    }

    public static boolean isRipe(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE;
    }

    /** New grafts need dry loaded air and an actual rooted guava canopy. */
    public static boolean canPlant(Level level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && !level.isOutsideBuildHeight(pos.above())
                && level.hasChunkAt(pos) && level.hasChunkAt(pos.above()) && level.getWorldBorder().isWithinBounds(pos)
                && level.getBlockState(pos).isAir() && level.getFluidState(pos).isEmpty()
                && com.bunnidogs.mod.orchard.Orchard.root(level,pos)!=null;
    }

    public static boolean plant(ServerLevel level, BlockPos pos) {
        return canPlant(level,pos) && level.setBlock(pos,ModBlocks.GUAVA_FRUIT.get().defaultBlockState().setValue(BUNCH,com.bunnidogs.mod.orchard.Orchard.cropSize(level,pos)),3);
    }

    public static int fruitCount(BlockState state){return state.getValue(BUNCH);}
    /** A direct meal consumes one fruit rather than silently discarding the rest of its bunch. */
    public static BlockState mealReset(BlockState state){return fruitCount(state)>1 ? state.setValue(BUNCH,fruitCount(state)-1) : state.setValue(AGE,0);}
    public static BlockState grow(LevelReader level,BlockPos pos,BlockState state){return state.setValue(AGE,state.getValue(AGE)+1).setValue(BUNCH,state.getValue(AGE)==0 ? com.bunnidogs.mod.orchard.Orchard.cropSize(level,pos) : fruitCount(state));}
    public record Harvest(boolean picked,ItemStack produce){}
    public static Harvest take(ServerLevel level,BlockPos pos) {
        if(!level.hasChunkAt(pos) || !level.hasChunkAt(pos.above()) || !level.getWorldBorder().isWithinBounds(pos))return new Harvest(false,ItemStack.EMPTY);
        var state=level.getBlockState(pos);
        if(!state.is(ModBlocks.GUAVA_FRUIT.get()) || !isRipe(state) || !state.canSurvive(level,pos) || !level.setBlock(pos,state.setValue(AGE,0),2))return new Harvest(false,ItemStack.EMPTY);
        level.playSound(null,pos,net.minecraft.sounds.SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,net.minecraft.sounds.SoundSource.BLOCKS,.7F,1.1F);
        return new Harvest(true,level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.BLOCK_DROPS) ? new ItemStack(ModItems.GUAVA.get(),fruitCount(state)) : ItemStack.EMPTY);
    }

    /** Reset before paying the fruit; hand and dispenser harvesting share support and stale-state checks. */
    public static boolean pick(ServerLevel level, BlockPos pos) {
        var harvest=take(level,pos);if(!harvest.produce().isEmpty())Block.popResource(level,pos,harvest.produce());return harvest.picked();
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
        if (isValidBonemealTarget(level,pos,state)) level.setBlock(pos,grow(level,pos,state),2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE,BUNCH);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BUNCH)>1 ? Shapes.box(1D/16,0,1D/16,15D/16,1,15D/16) : SHAPE;
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
        if(age>=MAX_AGE)return;
        int crop=com.bunnidogs.mod.orchard.Orchard.cropSize(level,pos);int odds=crop==3 ? 2 : crop==2 ? 4 : GROWTH_ODDS;
        if (age < MAX_AGE && random.nextInt(odds) == 0 && level.getBlockState(pos).equals(state)) {
            level.setBlock(pos, grow(level,pos,state), 2);
        }
    }

    /** The tool catches a bunch; ordinary hand picking drops it below the branch. */
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.InteractionHand hand,BlockHitResult hit) {
        if(stack.getItem() instanceof com.bunnidogs.mod.orchard.FruitPickerItem picker)return picker.harvest(level,player,hand,pos,false);
        return super.useItemOn(stack,state,level,pos,player,hand,hit);
    }
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
