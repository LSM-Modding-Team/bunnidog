package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Scavenging;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/** Injured meals or healthy opted-in collection; no chasing players, new inventories or item replacement. */
final class LooseFoodGoal extends Goal {
    private final Bunnidog dog;
    private final boolean gathering;
    private ItemEntity item;
    private ItemStack expected = ItemStack.EMPTY;
    private BlockPos troughPos, stand;
    private Path path;
    private long nextSearch;
    private int ticks, working;
    private record Candidate(ItemEntity item, BlockPos trough) {}
    LooseFoodGoal(Bunnidog dog, boolean gathering) {
        this.dog = dog; this.gathering = gathering; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }
    private boolean wanted() {
        return this.gathering ? this.dog.gardeningStatus() == Gardening.Status.READY && Scavenging.gathers(this.dog.stats()) : this.dog.canForage();
    }
    private FeedingTroughBlockEntity trough(BlockPos pos) {
        var level = this.dog.level();
        return pos != null && level.hasChunkAt(pos) && this.dog.foodWithinHome(pos)
                && level.getBlockState(pos).is(ModBlocks.FEEDING_TROUGH.get()) && level.getBlockState(pos).getValue(FeedingTroughBlock.TENDING)
                && level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity stock && !stock.residentsPaused() ? stock : null;
    }
    private boolean nearTrough(ItemEntity item, BlockPos pos) {
        return item.position().distanceToSqr(Vec3.atCenterOf(pos)) <= Scavenging.TROUGH_RADIUS*Scavenging.TROUGH_RADIUS;
    }
    private ArrayList<BlockPos> spots(ItemEntity food, BlockPos trough) {
        var out = new ArrayList<BlockPos>(); var pos = food.blockPosition(); out.add(pos); out.add(pos.above());
        for (var direction : Direction.Plane.HORIZONTAL) out.add(pos.relative(direction));
        out.removeIf(p -> !HomeRoutes.standing(this.dog,p) || Vec3.atBottomCenterOf(p).distanceToSqr(food.position()) > 2.25
                || trough != null && Vec3.atBottomCenterOf(p).distanceToSqr(Vec3.atCenterOf(trough)) > 9);
        return out;
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        var level = (ServerLevel)this.dog.level(); this.nextSearch = level.getGameTime()+Foraging.SEARCH_INTERVAL;
        var candidates = new ArrayList<Candidate>();
        if (this.gathering) {
            var troughs = level.getPoiManager().findAll(h -> h.is(ModPoi.FEEDING_TROUGH.getKey()),p -> trough(p) != null,
                    this.dog.blockPosition(),Foraging.SEARCH_RADIUS,PoiManager.Occupancy.ANY)
                    .sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition()))).limit(2).toList();
            for (var pos : troughs) {
                var stock = trough(pos);
                for (var food : level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(Scavenging.TROUGH_RADIUS),e -> LooseFood.available(this.dog,e)))
                    if (nearTrough(food,pos) && stock.canInsert(food.getItem())) candidates.add(new Candidate(food,pos.immutable()));
            }
        } else {
            for (var food : level.getEntitiesOfClass(ItemEntity.class,this.dog.getBoundingBox().inflate(Foraging.SEARCH_RADIUS),e -> LooseFood.available(this.dog,e)))
                if (this.dog.distanceToSqr(food) <= Foraging.SEARCH_RADIUS*Foraging.SEARCH_RADIUS) candidates.add(new Candidate(food,null));
        }
        candidates.sort(Comparator.comparingDouble(c -> this.dog.distanceToSqr(c.item())));
        int attempts = 0;
        for (var candidate : candidates.stream().limit(8).toList()) for (var spot : spots(candidate.item(),candidate.trough())) {
            if (++attempts > Scavenging.PATH_ATTEMPTS) return false;
            var route = this.dog.getNavigation().createPath(spot,0);
            if (!HomeRoutes.safe(this.dog,route,null)) continue;
            this.item = candidate.item(); this.expected = this.item.getItem().copyWithCount(1);
            this.troughPos = candidate.trough(); this.stand = spot; this.path = route; return true;
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        if (!wanted() || !LooseFood.available(this.dog,this.item) || !ItemStack.isSameItemSameComponents(this.item.getItem(),this.expected)
                || this.ticks >= Scavenging.MAX_VISIT || !HomeRoutes.standing(this.dog,this.stand) || !HomeRoutes.safe(this.dog,this.path,null)
                || Vec3.atBottomCenterOf(this.stand).distanceToSqr(this.item.position()) > 2.25) return false;
        var stock = this.gathering ? trough(this.troughPos) : null;
        return !this.gathering || stock != null && stock.canInsert(this.expected) && nearTrough(this.item,this.troughPos);
    }
    @Override public void start() { this.ticks = 0; this.working = 0; this.dog.getNavigation().moveTo(this.path,1); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    private boolean sightOfTrough() {
        var hit = this.dog.level().clip(new ClipContext(this.dog.getEyePosition(),Vec3.atCenterOf(this.troughPos),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this.dog));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(this.troughPos);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++; this.dog.getLookControl().setLookAt(this.item,30,30);
        if (this.dog.distanceToSqr(this.item) > 2.25 || this.gathering && this.dog.distanceToSqr(Vec3.atCenterOf(this.troughPos)) > 9) {
            this.working = 0;
            if (this.ticks % 20 == 0) {
                var route = this.dog.getNavigation().createPath(this.stand,0);
                if (!HomeRoutes.safe(this.dog,route,null) || !this.dog.getNavigation().moveTo(route,1)) this.ticks = Scavenging.MAX_VISIT;
                else this.path = route;
            }
            return;
        }
        this.dog.getNavigation().stop();
        if (!HomeRoutes.visible(this.dog,this.item) || this.gathering && !sightOfTrough()) { this.working = 0; return; }
        if (++this.working < (this.gathering ? Gardening.WORK_TICKS : Foraging.GRAZE_TICKS)) return;
        var level = (ServerLevel)this.dog.level();
        if (!EventHooks.canEntityGrief(level,this.dog) || !canContinueToUse()) { this.ticks = Scavenging.MAX_VISIT; return; }
        var stock = this.gathering ? trough(this.troughPos) : null;
        if (this.gathering && stock == null) { this.ticks = Scavenging.MAX_VISIT; return; }
        if (LooseFood.take(this.dog,this.item,stock,this.expected)) {
            if (this.gathering) this.dog.finishedGardening(); else this.dog.ateForage(Foraging.HEAL);
        }
        this.ticks = Scavenging.MAX_VISIT;
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.item = null; this.expected = ItemStack.EMPTY;
        this.troughPos = null; this.stand = null; this.path = null;
    }
}
