package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;

/** Bounded, path-tested daytime meals near a claimed Nest, below home/breeding/tempt goals. */
final class GrazeGoal extends Goal {
    private final Bunnidog dog;
    private BlockPos food, stand;
    private record Meal(BlockState before, BlockState after) {}
    private Meal planned;
    private Path path;
    private long nextSearch;
    private int eating;
    private int ticks;

    GrazeGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

    private Foraging.Meal fieldMeal(BlockState state) {
        if (state.is(Blocks.HAY_BLOCK)) return Foraging.Meal.HAY;
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS)) return Foraging.Meal.GRASS;
        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            if (state.is(Blocks.WHEAT)) return Foraging.Meal.WHEAT;
            if (state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS)) return Foraging.Meal.PRODUCE;
        }
        return Foraging.Meal.NONE;
    }

    private Meal meal(BlockPos pos) {
        if (!(this.dog.level() instanceof ServerLevel server) || !server.hasChunkAt(pos) || !this.dog.foodWithinHome(pos)) return null;
        var state = server.getBlockState(pos);
        if (!state.canSurvive(server,pos)) return null;
        boolean griefing = server.getGameRules().get(GameRules.MOB_GRIEFING);
        if (Foraging.canGraze(this.dog.diet(),fieldMeal(state),griefing)) {
            if (state.getBlock() instanceof CropBlock crop) return new Meal(state,crop.getStateForAge(0));
            return new Meal(state,state.is(Blocks.GRASS_BLOCK) ? Blocks.DIRT.defaultBlockState()
                    : state.is(Blocks.SHORT_GRASS) ? Blocks.AIR.defaultBlockState() : state);
        }
        if (!griefing) return null;
        if (state.is(ModBlocks.GUAVA_FRUIT.get()) && GuavaFruitBlock.isRipe(state) && this.dog.canEat(new ItemStack(ModItems.GUAVA.get())))
            return new Meal(state,state.setValue(GuavaFruitBlock.AGE,0));
        if (state.is(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) >= 2 && this.dog.canEat(new ItemStack(Items.SWEET_BERRIES)))
            return new Meal(state,state.setValue(SweetBerryBushBlock.AGE,1));
        if ((state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) && state.getValue(CaveVines.BERRIES)
                && this.dog.canEat(new ItemStack(Items.GLOW_BERRIES))) return new Meal(state,state.setValue(CaveVines.BERRIES,false));
        return null;
    }

    private List<BlockPos> standingSpots(BlockPos pos) {
        var spots = new ArrayList<BlockPos>();
        spots.add(pos.above());
        for (var direction : Direction.Plane.HORIZONTAL) for (int down = 0; down <= 1; down++) spots.add(pos.relative(direction).below(down));
        return spots.stream().filter(p -> HomeRoutes.standing(this.dog,p)
                && Vec3.atBottomCenterOf(p).distanceToSqr(Vec3.atCenterOf(pos)) <= 4).toList();
    }

    @Override public boolean canUse() {
        if (!this.dog.canForage() || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime() + Foraging.SEARCH_INTERVAL;
        ArrayList<BlockPos> candidates = new ArrayList<>();
        BlockPos centre = this.dog.blockPosition();
        int r = Foraging.SEARCH_RADIUS;
        for (BlockPos p : BlockPos.betweenClosed(centre.offset(-r,-2,-r), centre.offset(r,2,r))) if (this.meal(p) != null) candidates.add(p.immutable());
        candidates.sort(Comparator.comparingDouble(p -> p.distSqr(centre)));
        this.food = null; this.stand = null; this.planned = null; int attempts = 0;
        for (BlockPos p : candidates.stream().limit(8).toList()) {
            for (var spot : standingSpots(p)) {
                if (++attempts > 8) return false;
                this.path = this.dog.getNavigation().createPath(spot,0);
                if (HomeRoutes.safe(this.dog,this.path,null)) {
                    this.food = p; this.stand = spot; this.planned = this.meal(p); return true;
                }
            }
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        return this.food != null && this.planned != null && this.dog.canForage() && this.meal(this.food) != null
                && this.dog.level().getBlockState(this.food).equals(this.planned.before()) && this.ticks < 200
                && HomeRoutes.standing(this.dog,this.stand) && HomeRoutes.safe(this.dog,this.path,null);
    }
    @Override public void start() {
        this.eating = 0; this.ticks = 0;
        this.dog.getNavigation().moveTo(this.path, 1.0D);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void tick() {
        // GoalSelector may tick between continuation checks. Recheck before touching
        // the target or taking another meal after healing/cancellation.
        if (!this.canContinueToUse()) return;
        this.ticks++;
        this.dog.getLookControl().setLookAt(this.food.getX()+0.5, this.food.getY()+0.5, this.food.getZ()+0.5);
        if (this.dog.distanceToSqr(Vec3.atCenterOf(this.food)) > 4.0D) {
            this.eating = 0;
            if (this.ticks % 20 == 0) {
                this.path = this.dog.getNavigation().createPath(this.stand,0);
                if (HomeRoutes.safe(this.dog,this.path,null)) this.dog.getNavigation().moveTo(this.path,1.0D);
                else this.ticks = 200;
            }
            return;
        }
        this.dog.getNavigation().stop();
        var sight = this.dog.level().clip(new ClipContext(this.dog.getEyePosition(),Vec3.atCenterOf(this.food),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this.dog));
        if (sight.getType() != HitResult.Type.MISS && !sight.getBlockPos().equals(this.food)) { this.eating = 0; return; }
        if (++this.eating < Foraging.GRAZE_TICKS) return;
        var level = (ServerLevel)this.dog.level();
        if (!this.planned.before().equals(this.planned.after()) && (!EventHooks.canEntityGrief(level,this.dog)
                || !CommonHooks.canEntityDestroy(level,this.food,this.dog))) {
            this.nextSearch = this.dog.level().getGameTime() + Foraging.MEAL_COOLDOWN;
            this.food = null;
            return;
        }
        // Protection callbacks can replace the plant/container, heal the animal, change Diet or remove its safe route.
        if (!this.canContinueToUse() || !this.planned.after().canSurvive(level,this.food)) { this.food = null; return; }
        if (this.planned.before().equals(this.planned.after()) || level.setBlock(this.food,this.planned.after(),3)) {
            level.levelEvent(2001,this.food,Block.getId(this.planned.before()));
            this.dog.ateForage(Foraging.HEAL);
        }
        this.food = null;
    }
    @Override public void stop() { this.dog.getNavigation().stop(); this.food = null; this.stand = null; this.path = null; this.planned = null; }
}
