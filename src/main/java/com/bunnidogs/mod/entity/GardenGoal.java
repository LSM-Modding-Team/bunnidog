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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
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
    private record Harvest(ItemStack produce, BlockState reset) {}
    GardenGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    private boolean wanted() {
        return this.dog.acceptsCare() && this.dog.level() instanceof ServerLevel server
                && server.getGameRules().get(GameRules.MOB_GRIEFING)
                && Gardening.eligible(!this.dog.isBaby(),this.dog.getHealth() >= this.dog.getMaxHealth(),this.dog.homeNest() != null,
                        this.dog.homeNight(),this.dog.beingTransported(),this.dog.getTarget() != null
                        || this.dog.getLastHurtByMob() != null && this.dog.tickCount-this.dog.getLastHurtByMobTimestamp() < 200,
                        this.dog.gardeningWait() == 0) && this.dog.foodWithinHome(this.dog.blockPosition());
    }
    private FeedingTroughBlockEntity trough(BlockPos pos) {
        var level = this.dog.level();
        return pos != null && level.hasChunkAt(pos) && this.dog.foodWithinHome(pos)
                && level.getBlockState(pos).is(ModBlocks.FEEDING_TROUGH.get())
                && level.getBlockState(pos).getValue(FeedingTroughBlock.TENDING)
                && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity entity ? entity : null;
    }
    private static Harvest harvest(BlockState state) {
        if (state.is(ModBlocks.GUAVA_FRUIT.get()) && GuavaFruitBlock.isRipe(state))
            return new Harvest(new ItemStack(ModItems.GUAVA.get()),state.setValue(GuavaFruitBlock.AGE,0));
        if (state.is(Blocks.NETHER_WART) && state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE)
            return new Harvest(new ItemStack(Items.NETHER_WART),state.setValue(NetherWartBlock.AGE,0));
        if ((state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) && state.getValue(CaveVines.BERRIES))
            return new Harvest(new ItemStack(Items.GLOW_BERRIES),state.setValue(CaveVines.BERRIES,false));
        if (!(state.getBlock() instanceof CropBlock plant) || !plant.isMaxAge(state)) return null;
        var item = state.is(Blocks.WHEAT) ? Items.WHEAT : state.is(Blocks.CARROTS) ? Items.CARROT
                : state.is(Blocks.POTATOES) ? Items.POTATO : state.is(Blocks.BEETROOTS) ? Items.BEETROOT : null;
        return item == null ? null : new Harvest(new ItemStack(item),plant.getStateForAge(0));
    }
    private boolean standing(BlockPos pos) {
        var level = this.dog.level();
        return level.hasChunkAt(pos) && this.dog.foodWithinHome(pos) && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir() && level.getFluidState(pos).isEmpty()
                && level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP);
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        var level = (ServerLevel)this.dog.level();
        this.nextSearch = level.getGameTime()+Gardening.SEARCH_INTERVAL;
        var troughs = level.getPoiManager().findAll(h -> h.is(ModPoi.FEEDING_TROUGH.getKey()),p -> trough(p) != null,
                this.dog.blockPosition(),8,PoiManager.Occupancy.ANY)
                .sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition()))).limit(2).toList();
        int attempts = 0;
        for (BlockPos target : troughs) {
            var stock = trough(target);
            var crops = new ArrayList<BlockPos>();
            int radius = Gardening.CROP_RADIUS;
            for (BlockPos p : BlockPos.betweenClosed(target.offset(-radius,-1,-radius),target.offset(radius,1,radius))) {
                if (!level.hasChunkAt(p) || !this.dog.foodWithinHome(p)) continue;
                var food = harvest(level.getBlockState(p));
                if (food != null && this.dog.canEat(food.produce()) && stock.canInsert(food.produce())) crops.add(p.immutable());
            }
            crops.sort(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition())));
            for (BlockPos p : crops.stream().limit(4).toList()) for (Direction d : Direction.Plane.HORIZONTAL) for (int down = 0; down <= 1; down++) {
                BlockPos spot = p.relative(d).below(down);
                if (!standing(spot) || Vec3.atBottomCenterOf(spot).distanceToSqr(Vec3.atCenterOf(target)) > 9) continue;
                if (++attempts > 8) return false;
                Path route = this.dog.getNavigation().createPath(spot,0);
                if (route == null || !route.canReach()) continue;
                this.trough = target.immutable(); this.crop = p; this.stand = spot;
                this.mature = level.getBlockState(p);
                var food = harvest(this.mature);
                this.produce = food.produce(); this.reset = food.reset(); this.path = route;
                return true;
            }
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        var stock = trough(this.trough);
        return wanted() && this.crop != null && this.dog.level().hasChunkAt(this.crop)
                && this.dog.canEat(this.produce)
                && this.dog.level().getBlockState(this.crop).equals(this.mature)
                && stock != null && stock.canInsert(this.produce) && this.ticks < Gardening.MAX_VISIT;
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
            if (this.ticks % 20 == 0 && !this.dog.getNavigation().moveTo(this.stand.getX()+.5,this.stand.getY(),this.stand.getZ()+.5,1)) this.ticks = Gardening.MAX_VISIT;
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
            if (stock == null || stock.insert(this.produce) != 1) Block.popResource(level,this.crop,this.produce.copy());
            level.levelEvent(2001,this.crop,Block.getId(this.mature));
            this.dog.finishedGardening();
        }
        this.ticks = Gardening.MAX_VISIT;
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.trough = null; this.crop = null; this.stand = null; this.path = null;
        this.produce = ItemStack.EMPTY; this.mature = null; this.reset = null;
    }
}
