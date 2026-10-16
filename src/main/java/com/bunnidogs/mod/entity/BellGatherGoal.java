package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.genetics.Social;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;

/** Reads the shared bell POI index; never takes villager tickets or replaces a Nest. */
final class BellGatherGoal extends Goal {
    private static final int[][] SPOTS = {{2,0},{2,2},{0,2},{-2,2},{-2,0},{-2,-2},{0,-2},{2,-2}};
    private final Bunnidog dog;
    private BlockPos bell, stand;
    private Path path;
    private long nextSearch;
    private int ticks;
    BellGatherGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    private boolean wanted() {
        if (!(this.dog.level() instanceof ServerLevel server) || this.dog.isBaby() || this.dog.homeNest() == null
                || this.dog.beingTransported() || this.dog.seekingRefuge() || this.dog.getHealth() < this.dog.getMaxHealth() || this.dog.getTarget() != null
                || this.dog.getLastHurtByMob() != null && this.dog.tickCount-this.dog.getLastHurtByMobTimestamp() < 200
                || !this.dog.foodWithinHome(this.dog.blockPosition())) return false;
        return server.dimensionType().defaultClock().map(clock -> Social.gatheringTime(server.clockManager().getTotalTicks(clock))).orElse(false);
    }
    private boolean validBell(BlockPos pos) {
        return pos != null && this.dog.level() instanceof ServerLevel server && server.hasChunkAt(pos) && server.getBlockState(pos).is(Blocks.BELL);
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        var server = (ServerLevel)this.dog.level();
        this.nextSearch = server.getGameTime()+Social.SEARCH_INTERVAL;
        this.bell = null;
        var bells = server.getPoiManager().findAll(h -> h.is(PoiTypes.MEETING),this::validBell,this.dog.homeNest(),Social.BELL_RANGE,PoiManager.Occupancy.ANY)
                .sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.homeNest()))).limit(2).toList();
        int start = this.dog.getUUID().hashCode() & 7;
        for (BlockPos bell : bells) for (int n = 0; n < 4; n++) {
            int[] offset = SPOTS[(start+n*2)&7];
            BlockPos spot = bell.offset(offset[0],0,offset[1]);
            if (!server.hasChunkAt(spot) || !this.dog.foodWithinHome(spot) || !server.getBlockState(spot).isAir()
                    || !server.getBlockState(spot.above()).isAir() || !server.getFluidState(spot).isEmpty()
                    || !server.getBlockState(spot.below()).isFaceSturdy(server,spot.below(),Direction.UP)) continue;
            Path candidate = this.dog.getNavigation().createPath(spot,0);
            if (candidate != null && candidate.canReach()) { this.bell = bell.immutable(); this.stand = spot; this.path = candidate; return true; }
        }
        return false;
    }
    @Override public boolean canContinueToUse() { return wanted() && validBell(this.bell) && this.ticks < Social.VISIT_TICKS; }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { this.ticks = 0; this.dog.getNavigation().moveTo(this.path,.8); }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++;
        this.dog.getLookControl().setLookAt(this.bell.getX()+.5,this.bell.getY()+.8,this.bell.getZ()+.5);
        if (this.dog.distanceToSqr(this.stand.getX()+.5,this.stand.getY(),this.stand.getZ()+.5) <= 1) this.dog.getNavigation().stop();
        else if (this.ticks % 40 == 0 && !this.dog.getNavigation().moveTo(this.stand.getX()+.5,this.stand.getY(),this.stand.getZ()+.5,.8)) this.ticks = Social.VISIT_TICKS;
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.bell = null; this.stand = null; this.path = null;
        this.nextSearch = this.dog.level().getGameTime()+Social.REST_TICKS;
    }
}
