package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.TroughWork;
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
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
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
    private NurseryCradleBlockEntity nursery;
    private ItemStack litter = ItemStack.EMPTY;
    private int nurseryAge;
    private enum Task {
        HARVEST(TroughWork.Job.HARVEST), CULTIVATE(TroughWork.Job.CULTIVATE), PLANT(TroughWork.Job.PLANT),
        NURTURE(TroughWork.Job.NURTURE), COMPOST(TroughWork.Job.COMPOST);
        final TroughWork.Job job;
        Task(TroughWork.Job job) { this.job = job; }
        boolean spendsFood() { return this == PLANT || this == NURTURE || this == COMPOST; }
    }
    private Task task;
    private record Work(ItemStack produce, BlockState reset, Task task) {}
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
                && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity entity && !entity.residentsPaused() ? entity : null;
    }
    private static Work staged(BlockState state, net.minecraft.world.level.block.state.properties.IntegerProperty age,
            int ripe, int reset, net.minecraft.world.item.Item item, boolean cultivate) {
        int current = state.getValue(age);
        boolean harvest = current >= ripe;
        return harvest || cultivate ? new Work(new ItemStack(item),state.setValue(age,harvest ? reset : current+1),harvest ? Task.HARVEST : Task.CULTIVATE) : null;
    }
    private Work work(ServerLevel level, BlockPos pos, BlockState state, boolean cultivate, ItemStack stock, TroughWork mode) {
        if (state.isAir() && mode.allows(TroughWork.Job.PLANT) && cultivate && stock.getCount() > Gardening.COMPOST_RESERVE) {
            var planted = ResidentPlanting.plan(level,pos,stock);
            return planted == null ? null : new Work(stock.copyWithCount(1),planted,Task.PLANT);
        }
        if (state.is(ModBlocks.NURSERY_CRADLE.get())) return mode.allows(TroughWork.Job.NURTURE) && Gardening.nurses(this.dog.stats()) && stock.is(ModItems.GUAVA.get())
                && stock.getCount() > Gardening.COMPOST_RESERVE && level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle
                && cradle.seed().isPresent() && cradle.age() < SeedCropBlock.MAX_AGE
                ? new Work(stock.copyWithCount(1),state,Task.NURTURE) : null;
        if (state.is(Blocks.CHORUS_FLOWER)) return ResidentPlants.spentChorus(this.dog,pos)
                ? new Work(new ItemStack(Items.CHORUS_FRUIT),state.setValue(ChorusFlowerBlock.AGE,0),Task.HARVEST) : null;
        if (state.is(Blocks.MELON)) return ResidentPlants.linkedMelon(this.dog,pos)
                ? new Work(new ItemStack(Items.MELON_SLICE),Blocks.AIR.defaultBlockState(),Task.HARVEST) : null;
        if (state.is(Blocks.MELON_STEM)) return cultivate && state.getValue(StemBlock.AGE) < StemBlock.MAX_AGE && state.canSurvive(level,pos)
                ? new Work(new ItemStack(Items.MELON_SLICE),state.cycle(StemBlock.AGE),Task.CULTIVATE) : null;
        if (state.is(Blocks.CACTUS)) return ResidentPlants.spareCactus(this.dog,pos)
                ? new Work(new ItemStack(Items.CACTUS),Blocks.AIR.defaultBlockState(),Task.HARVEST) : null;
        if (ResidentPlants.mushroom(state)) return ResidentPlants.spareMushroom(this.dog,pos)
                ? new Work(new ItemStack(state.is(Blocks.RED_MUSHROOM) ? Items.RED_MUSHROOM : Items.BROWN_MUSHROOM),
                    Blocks.AIR.defaultBlockState(),Task.HARVEST) : null;
        if (state.is(Blocks.COMPOSTER)) return cultivate && state.getValue(ComposterBlock.LEVEL) < ComposterBlock.MAX_LEVEL
                && stock.getCount() > Gardening.COMPOST_RESERVE && ComposterBlock.getValue(stock) > 0
                ? new Work(stock.copyWithCount(1),state,Task.COMPOST) : null;
        if (state.is(ModBlocks.GUAVA_FRUIT.get()))
            return staged(state,GuavaFruitBlock.AGE,GuavaFruitBlock.MAX_AGE,0,ModItems.GUAVA.get(),cultivate);
        if (state.is(Blocks.NETHER_WART))
            return staged(state,NetherWartBlock.AGE,NetherWartBlock.MAX_AGE,0,Items.NETHER_WART,cultivate);
        if (state.is(Blocks.SWEET_BERRY_BUSH))
            return staged(state,SweetBerryBushBlock.AGE,2,1,Items.SWEET_BERRIES,cultivate);
        if (state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) {
            boolean berries = state.getValue(CaveVines.BERRIES);
            return berries || cultivate ? new Work(new ItemStack(Items.GLOW_BERRIES),state.setValue(CaveVines.BERRIES,!berries),berries ? Task.HARVEST : Task.CULTIVATE) : null;
        }
        if (!(state.getBlock() instanceof CropBlock plant)) return null;
        var item = state.is(Blocks.WHEAT) ? Items.WHEAT : state.is(Blocks.CARROTS) ? Items.CARROT
                : state.is(Blocks.POTATOES) ? Items.POTATO : state.is(Blocks.BEETROOTS) ? Items.BEETROOT : null;
        boolean ripe = plant.isMaxAge(state);
        return item != null && (ripe || cultivate)
                ? new Work(new ItemStack(item),plant.getStateForAge(ripe ? 0 : plant.getAge(state)+1),ripe ? Task.HARVEST : Task.CULTIVATE) : null;
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
            for (BlockPos p : BlockPos.betweenClosed(target.offset(-radius,-1,-radius),target.offset(radius,2,radius))) {
                if (!level.hasChunkAt(p) || !this.dog.foodWithinHome(p)) continue;
                var food = work(level,p,level.getBlockState(p),Gardening.cultivates(this.dog.stats()),stock.food(),stock.workMode());
                if (food != null && stock.allowsWork(food.task().job) && this.dog.canEat(food.produce())
                        && (food.task().spendsFood() || stock.canInsert(food.produce())))
                    candidates.add(new Candidate(target.immutable(),p.immutable(),food));
            }
        }
        candidates.sort(Comparator.<Candidate,Task>comparing(c -> c.work().task())
                .thenComparingDouble(c -> c.crop().distSqr(this.dog.blockPosition())));
        int attempts = 0;
        for (Candidate candidate : candidates.stream().limit(8).toList()) {
            BlockPos p = candidate.crop(), target = candidate.trough();
            for (Direction d : Direction.Plane.HORIZONTAL) for (int down = 0; down <= 2; down++) {
                BlockPos spot = p.relative(d).below(down);
                if (!HomeRoutes.standing(this.dog,spot) || Vec3.atBottomCenterOf(spot).distanceToSqr(Vec3.atCenterOf(target)) > 9) continue;
                if (++attempts > 8) return false;
                Path route = this.dog.getNavigation().createPath(spot,0);
                if (!HomeRoutes.safe(this.dog,route,null)) continue;
                this.trough = target.immutable(); this.crop = p; this.stand = spot;
                this.mature = level.getBlockState(p);
                var food = candidate.work();
                this.produce = food.produce(); this.reset = food.reset(); this.task = food.task(); this.path = route;
                this.nursery = this.task == Task.NURTURE && level.getBlockEntity(p) instanceof NurseryCradleBlockEntity cradle ? cradle : null;
                this.litter = this.nursery == null ? ItemStack.EMPTY : this.nursery.storedSeed();
                this.nurseryAge = this.nursery == null ? 0 : this.nursery.age();
                return true;
            }
        }
        return false;
    }
    @Override public boolean canContinueToUse() { return continuing(false); }
    private boolean continuing(boolean reserved) {
        var stock = trough(this.trough);
        return wanted() && this.crop != null && this.dog.level().hasChunkAt(this.crop) && this.dog.foodWithinHome(this.crop)
                && this.dog.canEat(this.produce)
                && (this.task == Task.HARVEST || (this.task == Task.NURTURE ? Gardening.nurses(this.dog.stats()) : Gardening.cultivates(this.dog.stats())))
                && this.dog.level().getBlockState(this.crop).equals(this.mature)
                && (!this.mature.is(Blocks.MELON) || ResidentPlants.linkedMelon(this.dog,this.crop))
                && (!this.mature.is(Blocks.CACTUS) || ResidentPlants.spareCactus(this.dog,this.crop))
                && (!ResidentPlants.mushroom(this.mature) || ResidentPlants.spareMushroom(this.dog,this.crop))
                && (!this.mature.is(Blocks.CHORUS_FLOWER) || ResidentPlants.spentChorus(this.dog,this.crop))
                && (this.task != Task.PLANT || this.reset.equals(ResidentPlanting.plan((ServerLevel)this.dog.level(),this.crop,this.produce)))
                && (this.task != Task.NURTURE || this.nursery != null && this.dog.level().getBlockEntity(this.crop) == this.nursery && this.nursery.seed().isPresent()
                        && this.nursery.age() == this.nurseryAge && ItemStack.matches(this.nursery.storedSeed(),this.litter))
                && stock != null && stock.allowsWork(this.task.job) && (this.task.spendsFood()
                    ? stock.food().getCount() > Gardening.COMPOST_RESERVE-(reserved ? 1 : 0) && ItemStack.isSameItemSameComponents(stock.food(),this.produce)
                        && (this.task != Task.COMPOST || ComposterBlock.getValue(this.produce) > 0) : stock.canInsert(this.produce))
                && this.ticks < Gardening.MAX_VISIT
                && HomeRoutes.standing(this.dog,this.stand) && HomeRoutes.safe(this.dog,this.path,null);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { this.ticks = 0; this.working = 0; this.dog.getNavigation().moveTo(this.path,1); }
    private boolean sight(BlockPos pos) {
        var eye = this.dog.getEyePosition(); var target = ResidentPlants.sightPoint(this.dog,pos);
        var hit = this.dog.level().clip(new ClipContext(eye,target,
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this.dog));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++;
        this.dog.getLookControl().setLookAt(Vec3.atCenterOf(this.crop));
        double reach = this.mature.is(Blocks.CACTUS) ? 9 : 4;
        if (this.dog.distanceToSqr(Vec3.atCenterOf(this.crop)) > reach || this.dog.distanceToSqr(Vec3.atCenterOf(this.trough)) > 9) {
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
        // Protection callbacks may change stock, power, crop state or the animal's eligibility.
        if (!canContinueToUse()) { this.ticks = Gardening.MAX_VISIT; return; }
        if (this.task == Task.COMPOST) {
            compost(level); this.ticks = Gardening.MAX_VISIT; return;
        }
        if (this.task == Task.PLANT || this.task == Task.NURTURE) {
            spendFood(level); this.ticks = Gardening.MAX_VISIT; return;
        }
        if (level.setBlock(this.crop,this.reset,3)) {
            var stock = trough(this.trough);
            boolean harvest = this.task == Task.HARVEST;
            if (harvest && (stock == null || stock.insert(this.produce) != 1)) Block.popResource(level,this.crop,this.produce.copy());
            level.levelEvent(harvest ? 2001 : 2005,this.crop,harvest ? Block.getId(this.mature) : 15);
            this.dog.finishedGardening();
        }
        this.ticks = Gardening.MAX_VISIT;
    }
    private void spendFood(ServerLevel level) {
        var stock = trough(this.trough);
        if (stock == null) return;
        var input = stock.takeSurplus(this.produce,Gardening.COMPOST_RESERVE);
        if (input.isEmpty()) return;
        boolean acted = false;
        // Stock and native placement callbacks can remove either block or change the worker/mode.
        boolean permitted = this.task != Task.PLANT || !EventHooks.onBlockPlace(this.dog,BlockSnapshot.create(level.dimension(),level,this.crop),Direction.UP);
        if (permitted && trough(this.trough) == stock && continuing(true) && this.reset.canSurvive(level,this.crop)) {
            acted = this.task == Task.PLANT ? level.setBlock(this.crop,this.reset,3) : this.nursery.nurture(this.litter,this.nurseryAge);
            if (acted) {
                input.shrink(1); level.levelEvent(2005,this.crop,15); this.dog.finishedGardening();
            }
        }
        if (!input.isEmpty()) {
            if (level.getBlockEntity(this.trough) == stock && !stock.isRemoved()) input.shrink(stock.insert(input));
            if (!input.isEmpty()) Block.popResource(level,this.trough,input);
        }
    }
    private void compost(ServerLevel level) {
        var stock = trough(this.trough);
        if (stock == null) return;
        var input = stock.takeSurplus(this.produce,Gardening.COMPOST_RESERVE);
        if (input.isEmpty()) return;
        // Stock notifications may invoke other blocks. Recheck before calling the native composter.
        if (level.getBlockEntity(this.trough) == stock && !stock.isRemoved() && !stock.residentsPaused()
                && level.getBlockState(this.crop).equals(this.mature)) {
            var updated = ComposterBlock.insertItem(this.dog,this.mature,level,input,this.crop);
            level.levelEvent(1500,this.crop,updated.equals(this.mature) ? 0 : 1);
        }
        if (!input.isEmpty()) {
            if (level.getBlockEntity(this.trough) == stock && !stock.isRemoved()) input.shrink(stock.insert(input));
            if (!input.isEmpty()) Block.popResource(level,this.trough,input);
        }
        this.dog.finishedGardening();
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.trough = null; this.crop = null; this.stand = null; this.path = null;
        this.produce = ItemStack.EMPTY; this.mature = null; this.reset = null; this.task = null;
        this.nursery = null; this.litter = ItemStack.EMPTY; this.nurseryAge = 0;
    }
}
