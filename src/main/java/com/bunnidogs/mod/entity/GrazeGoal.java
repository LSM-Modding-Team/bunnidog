package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.genetics.Foraging;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;

/** Bounded, path-tested daytime meals near a claimed Nest, below home/breeding/tempt goals. */
final class GrazeGoal extends Goal {
    private final Bunnidog dog;
    private BlockPos food;
    private Path path;
    private long nextSearch;
    private int eating;
    private int ticks;

    GrazeGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

    private Foraging.Meal meal(BlockState state) {
        if (state.is(Blocks.HAY_BLOCK)) return Foraging.Meal.HAY;
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS)) return Foraging.Meal.GRASS;
        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            if (state.is(Blocks.WHEAT)) return Foraging.Meal.WHEAT;
            if (state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS)) return Foraging.Meal.PRODUCE;
        }
        return Foraging.Meal.NONE;
    }

    private boolean edible(BlockPos pos) {
        if (!(this.dog.level() instanceof ServerLevel server) || !server.isLoaded(pos) || !this.dog.foodWithinHome(pos)) return false;
        return Foraging.canGraze(this.dog.diet(), this.meal(server.getBlockState(pos)), server.getGameRules().get(GameRules.MOB_GRIEFING));
    }

    @Override public boolean canUse() {
        if (!this.dog.diet().grazesCrops() || !this.dog.canForage() || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime() + Foraging.SEARCH_INTERVAL;
        ArrayList<BlockPos> candidates = new ArrayList<>();
        BlockPos centre = this.dog.blockPosition();
        int r = Foraging.SEARCH_RADIUS;
        for (BlockPos p : BlockPos.betweenClosed(centre.offset(-r,-2,-r), centre.offset(r,2,r))) if (this.edible(p)) candidates.add(p.immutable());
        candidates.sort(Comparator.comparingDouble(p -> p.distSqr(centre)));
        this.food = null;
        for (BlockPos p : candidates.stream().limit(8).toList()) {
            BlockPos stand = this.dog.level().getBlockState(p).isCollisionShapeFullBlock(this.dog.level(),p) ? p.above() : p;
            this.path = this.dog.getNavigation().createPath(stand, 1);
            if (this.path != null && this.path.canReach()) { this.food = p; return true; }
        }
        return false;
    }
    @Override public boolean canContinueToUse() { return this.food != null && this.dog.canForage() && this.edible(this.food) && this.ticks < 200; }
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
        if (this.dog.distanceToSqr(this.food.getX()+0.5,this.food.getY()+1,this.food.getZ()+0.5) > 4.0D) {
            this.eating = 0;
            if (this.ticks % 20 == 0) {
                BlockPos stand = this.dog.level().getBlockState(this.food).isCollisionShapeFullBlock(this.dog.level(),this.food) ? this.food.above() : this.food;
                this.path = this.dog.getNavigation().createPath(stand,1);
                if (this.path != null && this.path.canReach()) this.dog.getNavigation().moveTo(this.path,1.0D);
                else this.ticks = 200;
            }
            return;
        }
        this.dog.getNavigation().stop();
        var sight = this.dog.level().clip(new ClipContext(this.dog.getEyePosition(),Vec3.atCenterOf(this.food),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this.dog));
        if (sight.getType() != HitResult.Type.MISS && !sight.getBlockPos().equals(this.food)) { this.eating = 0; return; }
        if (++this.eating < Foraging.GRAZE_TICKS || !this.edible(this.food)) return;
        BlockState state = this.dog.level().getBlockState(this.food);
        if (!state.is(Blocks.HAY_BLOCK) && !CommonHooks.canEntityDestroy((ServerLevel)this.dog.level(),this.food,this.dog)) {
            this.nextSearch = this.dog.level().getGameTime() + Foraging.MEAL_COOLDOWN;
            this.food = null;
            return;
        }
        if (state.getBlock() instanceof CropBlock crop) this.dog.level().setBlock(this.food,crop.getStateForAge(0),3);
        else if (state.is(Blocks.GRASS_BLOCK)) this.dog.level().setBlock(this.food,Blocks.DIRT.defaultBlockState(),3);
        else if (state.is(Blocks.SHORT_GRASS)) this.dog.level().destroyBlock(this.food,false);
        this.dog.ateForage(Foraging.HEAL);
    }
    @Override public void stop() { this.dog.getNavigation().stop(); this.food = null; this.path = null; }
}
