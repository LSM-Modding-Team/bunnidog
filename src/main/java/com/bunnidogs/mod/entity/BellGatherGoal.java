package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import com.bunnidogs.mod.genetics.Social;
import net.minecraft.core.BlockPos;
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
    private BlockPos bell, stand, home;
    private Path path;
    private long nextSearch, restUntil;
    private int ticks;
    private boolean calledVisit;
    BellGatherGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    static boolean willing(Bunnidog dog) {
        return dog.acceptsCare() && dog.isAlive() && dog.level() instanceof ServerLevel && !dog.isBaby() && dog.homeNest() != null
                && !dog.beingTransported() && !dog.seekingRefuge() && dog.getHealth() >= dog.getMaxHealth() && dog.getTarget() == null
                && !(dog.getLastHurtByMob() != null && dog.tickCount-dog.getLastHurtByMobTimestamp() < 200)
                && dog.foodWithinHome(dog.blockPosition()) && !dog.homeNight();
    }
    private boolean scheduled() {
        var server = (ServerLevel)this.dog.level();
        return server.dimensionType().defaultClock().map(clock -> Social.gatheringTime(server.clockManager().getTotalTicks(clock))).orElse(false);
    }
    private boolean validBell(BlockPos pos) {
        return pos != null && this.dog.level() instanceof ServerLevel server && server.hasChunkAt(pos) && server.getBlockState(pos).is(Blocks.BELL);
    }
    @Override public boolean canUse() {
        if (!willing(this.dog) || this.dog.level().getGameTime() < this.restUntil) return false;
        var server = (ServerLevel)this.dog.level();
        BlockPos call = this.dog.calledBell();
        if (call == null && (!scheduled() || server.getGameTime() < this.nextSearch)) return false;
        this.calledVisit = call != null;
        if (this.calledVisit) {
            this.dog.forgetBellCall();
            if (!validBell(call) || call.distSqr(this.dog.homeNest()) > Social.BELL_RANGE*Social.BELL_RANGE) return false;
        }
        this.nextSearch = server.getGameTime()+Social.SEARCH_INTERVAL;
        this.bell = null;
        this.home = this.dog.homeNest();
        var bells = this.calledVisit ? List.of(call) : server.getPoiManager().findAll(h -> h.is(PoiTypes.MEETING),this::validBell,this.home,Social.BELL_RANGE,PoiManager.Occupancy.ANY)
                .sorted(Comparator.comparingDouble(p -> p.distSqr(this.dog.homeNest()))).limit(2).toList();
        int start = this.dog.getUUID().hashCode() & 7;
        for (BlockPos bell : bells) for (int n = 0; n < 4; n++) {
            int[] offset = SPOTS[(start+n*2)&7];
            BlockPos spot = bell.offset(offset[0],0,offset[1]);
            if (!HomeRoutes.standing(this.dog,spot)) continue;
            Path candidate = this.dog.getNavigation().createPath(spot,0);
            if (HomeRoutes.safe(this.dog,candidate,null)) { this.bell = bell.immutable(); this.stand = spot; this.path = candidate; return true; }
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        return willing(this.dog) && this.home != null && this.home.equals(this.dog.homeNest()) && validBell(this.bell)
                && HomeRoutes.standing(this.dog,this.stand) && this.ticks < Social.VISIT_TICKS && (this.calledVisit || scheduled());
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { this.ticks = 0; this.dog.getNavigation().moveTo(this.path,.8); }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++;
        this.dog.getLookControl().setLookAt(this.bell.getX()+.5,this.bell.getY()+.8,this.bell.getZ()+.5);
        if (this.dog.distanceToSqr(this.stand.getX()+.5,this.stand.getY(),this.stand.getZ()+.5) <= 1) this.dog.getNavigation().stop();
        else if (this.ticks % 40 == 0) {
            Path route = this.dog.getNavigation().createPath(this.stand,0);
            if (HomeRoutes.safe(this.dog,route,null)) this.dog.getNavigation().moveTo(route,.8);
            else this.ticks = Social.VISIT_TICKS;
        }
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.bell = null; this.stand = null; this.home = null; this.path = null;
        this.calledVisit = false;
        this.restUntil = this.dog.level().getGameTime()+Social.REST_TICKS;
        this.nextSearch = this.restUntil;
    }
}
