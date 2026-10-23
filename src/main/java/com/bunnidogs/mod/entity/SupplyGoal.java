package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Learning;
import com.bunnidogs.mod.genetics.Supplying;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Supply two nearby dishes from one dry work position; no item is ever carried between ticks. */
final class SupplyGoal extends Goal {
    private final Bunnidog dog;
    private FeedingTroughBlockEntity source, target;
    private BlockPos stand, home;
    private ItemStack unit = ItemStack.EMPTY;
    private Path path;
    private long nextSearch;
    private int ticks, working;
    SupplyGoal(Bunnidog dog) { this.dog = dog; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    private boolean wanted() { return this.dog.gardeningStatus() == Gardening.Status.READY && Supplying.qualified(this.dog.stats())
            && this.dog.learning().canWork(Learning.Routine.DELIVERY); }
    private boolean close(BlockPos pos) { return this.dog.distanceToSqr(Vec3.atCenterOf(pos)) <= Supplying.REACH_SQUARED; }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        var level = (ServerLevel)this.dog.level(); this.nextSearch = level.getGameTime()+Gardening.SEARCH_INTERVAL;
        var dishes = level.getPoiManager().findAll(h -> h.is(ModPoi.FEEDING_TROUGH.getKey()),p -> TroughSupply.trough(this.dog,p) != null,
                this.dog.blockPosition(),8,PoiManager.Occupancy.ANY).sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition())))
                .limit(12).map(p -> TroughSupply.trough(this.dog,p)).toList();
        int attempts = 0, sources = 0;
        for (var from : dishes) {
            var food = from.food().copyWithCount(1);
            if (!TroughSupply.source(this.dog,from,food)) continue;
            if (++sources > 2) break;
            for (var to : dishes) {
                if (!TroughSupply.destination(from,to,food)) continue;
                var midpoint = BlockPos.containing((from.getBlockPos().getX()+to.getBlockPos().getX())/2.0,
                        (from.getBlockPos().getY()+to.getBlockPos().getY())/2.0,(from.getBlockPos().getZ()+to.getBlockPos().getZ())/2.0);
                var stands = new ArrayList<BlockPos>();
                for (var pos : BlockPos.betweenClosed(midpoint.offset(-1,-1,-1),midpoint.offset(1,1,1))) {
                    var bottom = Vec3.atBottomCenterOf(pos);
                    if (bottom.distanceToSqr(Vec3.atCenterOf(from.getBlockPos())) <= Supplying.REACH_SQUARED
                            && bottom.distanceToSqr(Vec3.atCenterOf(to.getBlockPos())) <= Supplying.REACH_SQUARED && HomeRoutes.standing(this.dog,pos)) stands.add(pos.immutable());
                }
                stands.sort(Comparator.comparingDouble(p -> p.distSqr(this.dog.blockPosition())));
                for (var pos : stands) {
                    if (++attempts > 8) return false;
                    var route = this.dog.getNavigation().createPath(pos,0);
                    if (!HomeRoutes.safe(this.dog,route,null)) continue;
                    this.source = from; this.target = to; this.unit = food; this.stand = pos; this.path = route; this.home = this.dog.homeNest(); return true;
                }
            }
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        return this.source != null && this.target != null && wanted() && this.home.equals(this.dog.homeNest())
                && this.dog.learning().witnesses(this.source.getBlockPos(), this.target.getBlockPos())
                && TroughSupply.trough(this.dog,this.source.getBlockPos()) == this.source && TroughSupply.source(this.dog,this.source,this.unit)
                && TroughSupply.trough(this.dog,this.target.getBlockPos()) == this.target && TroughSupply.destination(this.source,this.target,this.unit)
                && this.ticks < Gardening.MAX_VISIT && HomeRoutes.standing(this.dog,this.stand) && HomeRoutes.safe(this.dog,this.path,null);
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { this.ticks = 0; this.working = 0; this.dog.learning().observe(Learning.Routine.DELIVERY,BunnidogLearning.Activity.WALKING); this.dog.getNavigation().moveTo(this.path,1); }
    private boolean sight(BlockPos pos) {
        var hit = this.dog.level().clip(new ClipContext(this.dog.getEyePosition(),Vec3.atCenterOf(pos),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this.dog));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++; this.dog.getLookControl().setLookAt(Vec3.atCenterOf(this.target.getBlockPos()));
        if (!close(this.source.getBlockPos()) || !close(this.target.getBlockPos())
                || !sight(this.source.getBlockPos()) || !sight(this.target.getBlockPos())) {
            this.working = 0; this.dog.learning().observe(Learning.Routine.DELIVERY,BunnidogLearning.Activity.WALKING);
            if (this.ticks % 20 == 0) {
                var route = this.dog.getNavigation().createPath(this.stand,0);
                if (!HomeRoutes.safe(this.dog,route,null) || !this.dog.getNavigation().moveTo(route,1)) this.ticks = Gardening.MAX_VISIT;
                else this.path = route;
            }
            return;
        }
        this.dog.getNavigation().stop();
        this.dog.learning().observe(Learning.Routine.DELIVERY,BunnidogLearning.Activity.WORKING);
        if (++this.working < Gardening.WORK_TICKS) return;
        if (EventHooks.canEntityGrief((ServerLevel)this.dog.level(),this.dog) && canContinueToUse()
                && close(this.source.getBlockPos()) && close(this.target.getBlockPos()) && sight(this.source.getBlockPos()) && sight(this.target.getBlockPos())) {
            try (var transaction = Transaction.openRoot()) {
                if (TroughSupply.reserve(this.source,this.target,this.unit,transaction)) {
                    transaction.commit(); this.dog.finishedGardening();
                    this.dog.learning().completed(Learning.Routine.DELIVERY);
                }
            }
        }
        this.ticks = Gardening.MAX_VISIT;
    }
    @Override public void stop() {
        this.dog.learning().observe(Learning.Routine.DELIVERY,BunnidogLearning.Activity.SEARCHING);
        this.dog.getNavigation().stop(); this.source = null; this.target = null; this.stand = null; this.home = null; this.path = null; this.unit = ItemStack.EMPTY;
    }
}
