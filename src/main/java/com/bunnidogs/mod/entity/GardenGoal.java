package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;

/** Healthy residents tend a tiny opted-in garden, with no inventory raiding or carried-item state. */
final class GardenGoal extends Goal {
    private final Bunnidog dog;
    private BlockPos trough, crop, stand;
    private BlockState mature, reset;
    private ItemStack produce = ItemStack.EMPTY;
    private Path path;
    private long nextSearch;
    private int ticks, working;
    private boolean harvesting;
    private record Work(ItemStack produce, BlockState reset, boolean harvest) {}
    private record Candidate(BlockPos trough, BlockPos crop, Work work) {}
    GardenGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    private boolean wanted() {
        return this.dog.gardeningStatus() == Gardening.Status.READY;
    }
    private FeedingTroughBlockEntity trough(BlockPos pos) {
        var level = this.dog.level();
        return pos != null && level.hasChunkAt(pos) && this.dog.foodWithinHome(pos)
                && level.getBlockState(pos).is(ModBlocks.FEEDING_TROUGH.get())
                && level.getBlockState(pos).getValue(FeedingTroughBlock.TENDING)
                && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity entity ? entity : null;
    }
    private static Work staged(BlockState state, net.minecraft.world.level.block.state.properties.IntegerProperty age,
            int ripe, int reset, net.minecraft.world.item.Item item, boolean cultivate) {
        int current = state.getValue(age);
        boolean harvest = current >= ripe;
        return harvest || cultivate ? new Work(new ItemStack(item),state.setValue(age,harvest ? reset : current+1),harvest) : null;
    }
    private static Work work(BlockState state, boolean cultivate) {
        if (state.is(ModBlocks.GUAVA_FRUIT.get()))
            return staged(state,GuavaFruitBlock.AGE,GuavaFruitBlock.MAX_AGE,0,ModItems.GUAVA.get(),cultivate);
        if (state.is(Blocks.NETHER_WART))
            return staged(state,NetherWartBlock.AGE,NetherWartBlock.MAX_AGE,0,Items.NETHER_WART,cultivate);
        if (state.is(Blocks.SWEET_BERRY_BUSH))
            return staged(state,SweetBerryBushBlock.AGE,2,1,Items.SWEET_BERRIES,cultivate);
        if (state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) {
            boolean berries = state.getValue(CaveVines.BERRIES);
            return berries || cultivate ? new Work(new ItemStack(Items.GLOW_BERRIES),state.setValue(CaveVines.BERRIES,!berries),berries) : null;
        }
        if (!(state.getBlock() instanceof CropBlock plant)) return null;
        var item = state.is(Blocks.WHEAT) ? Items.WHEAT : state.is(Blocks.CARROTS) ? Items.CARROT
                : state.is(Blocks.POTATOES) ? Items.POTATO : state.is(Blocks.BEETROOTS) ? Items.BEETROOT : null;
        boolean ripe = plant.isMaxAge(state);
        return item != null && (ripe || cultivate)
                ? new Work(new ItemStack(item),plant.getStateForAge(ripe ? 0 : plant.getAge(state)+1),ripe) : null;
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        var level = (ServerLevel)this.dog.level();
        this.nextSearch = level.getGameTime()+Gardening.SEARCH_INTERVAL;
        var troughs = level.getPoiManager().findAll(h -> h.is(ModPoi.FEEDING_TROUGH.getKey()),p -> trough(p) != null,
                this.dog.blockPosition(),8,PoiManager.Occupancy.ANY)
                .sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition()))).limit(2).toList();
        var candidates = new ArrayList<Candidate>();
        for (BlockPos target : troughs) {
            var stock = trough(target);
            int radius = Gardening.CROP_RADIUS;
            for (BlockPos p : BlockPos.betweenClosed(target.offset(-radius,-1,-radius),target.offset(radius,1,radius))) {
                if (!level.hasChunkAt(p) || !this.dog.foodWithinHome(p)) continue;
                var food = work(level.getBlockState(p),Gardening.cultivates(this.dog.stats()));
                if (food != null && this.dog.canEat(food.produce()) && stock.canInsert(food.produce()))
                    candidates.add(new Candidate(target.immutable(),p.immutable(),food));
            }
        }
        candidates.sort(Comparator.<Candidate,Boolean>comparing(c -> !c.work().harvest())
                .thenComparingDouble(c -> c.crop().distSqr(this.dog.blockPosition())));
        int attempts = 0;
        for (Candidate candidate : candidates.stream().limit(8).toList()) {
            BlockPos p = candidate.crop(), target = candidate.trough();
            for (Direction d : Direction.Plane.HORIZONTAL) for (int down = 0; down <= 1; down++) {
                BlockPos spot = p.relative(d).below(down);
                if (!HomeRoutes.standing(this.dog,spot) || Vec3.atBottomCenterOf(spot).distanceToSqr(Vec3.atCenterOf(target)) > 9) continue;
                if (++attempts > 8) return false;
                Path route = this.dog.getNavigation().createPath(spot,0);
                if (!HomeRoutes.safe(this.dog,route,null)) continue;
                this.trough = target.immutable(); this.crop = p; this.stand = spot;
                this.mature = level.getBlockState(p);
                var food = candidate.work();
                this.produce = food.produce(); this.reset = food.reset(); this.harvesting = food.harvest(); this.path = route;
                return true;
            }
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        var stock = trough(this.trough);
        return wanted() && this.crop != null && this.dog.level().hasChunkAt(this.crop)
                && this.dog.canEat(this.produce)
                && (this.harvesting || Gardening.cultivates(this.dog.stats()))
                && this.dog.level().getBlockState(this.crop).equals(this.mature)
                && stock != null && stock.canInsert(this.produce) && this.ticks < Gardening.MAX_VISIT
                && HomeRoutes.standing(this.dog,this.stand) && HomeRoutes.safe(this.dog,this.path,null);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { this.ticks = 0; this.working = 0; this.dog.getNavigation().moveTo(this.path,1); }
    private boolean sight(BlockPos pos) {
        var hit = this.dog.level().clip(new ClipContext(this.dog.getEyePosition(),Vec3.atCenterOf(pos),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this.dog));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++;
        this.dog.getLookControl().setLookAt(Vec3.atCenterOf(this.crop));
        if (this.dog.distanceToSqr(Vec3.atCenterOf(this.crop)) > 4 || this.dog.distanceToSqr(Vec3.atCenterOf(this.trough)) > 9) {
            this.working = 0;
            if (this.ticks % 20 == 0) {
                Path route = this.dog.getNavigation().createPath(this.stand,0);
                if (!HomeRoutes.safe(this.dog,route,null) || !this.dog.getNavigation().moveTo(route,1)) this.ticks = Gardening.MAX_VISIT;
                else this.path = route;
            }
            return;
        }
        this.dog.getNavigation().stop();
        if (!sight(this.crop) || !sight(this.trough)) { this.working = 0; return; }
        if (++this.working < Gardening.WORK_TICKS) return;
        var level = (ServerLevel)this.dog.level();
        if (!EventHooks.canEntityGrief(level,this.dog) || !CommonHooks.canEntityDestroy(level,this.crop,this.dog)
                || !this.reset.canSurvive(level,this.crop)) {
            this.dog.finishedGardening(); this.ticks = Gardening.MAX_VISIT; return;
        }
        if (level.setBlock(this.crop,this.reset,3)) {
            var stock = trough(this.trough);
            if (this.harvesting && (stock == null || stock.insert(this.produce) != 1)) Block.popResource(level,this.crop,this.produce.copy());
            level.levelEvent(this.harvesting ? 2001 : 2005,this.crop,this.harvesting ? Block.getId(this.mature) : 15);
            this.dog.finishedGardening();
        }
        this.ticks = Gardening.MAX_VISIT;
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.trough = null; this.crop = null; this.stand = null; this.path = null;
        this.produce = ItemStack.EMPTY; this.mature = null; this.reset = null;
    }
}
