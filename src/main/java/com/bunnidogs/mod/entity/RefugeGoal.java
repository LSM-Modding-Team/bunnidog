package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.genetics.Refuge;
import com.bunnidogs.mod.genetics.Temperament;
import com.bunnidogs.mod.registry.ModResidentTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Loaded, bounded refuge near the current home; no attack target, teleport or ticket change. */
final class RefugeGoal extends Goal {
    private static final int[][] NEAR_HOME = {{0,0},{-1,0},{1,0},{0,-1},{0,1},{-2,0},{2,0},{0,-2},{0,2}};
    private static final int[][] AWAY = {{-4,0},{4,0},{0,-4},{0,4},{-3,-3},{3,-3},{-3,3},{3,3}};
    private final Bunnidog dog;
    private Mob threat;
    private BlockPos home, destination;
    private Path path;
    private long nextSearch;
    private int ticks, quiet;
    RefugeGoal(Bunnidog dog) { this.dog = dog; this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    private boolean wanted() {
        return this.dog.acceptsCare() && this.dog.isAlive() && this.dog.level() instanceof ServerLevel
                && Refuge.eligible(!this.dog.isBaby(),this.dog.homeNest() != null,this.dog.temperament() == Temperament.FERAL,
                        this.dog.beingTransported(),this.dog.hasCombatTarget(),this.dog.foodWithinHome(this.dog.blockPosition()));
    }
    private boolean threat(Mob mob) {
        return mob != null && mob.isAlive() && mob.is(ModResidentTags.THREATS) && !this.dog.isAlliedTo(mob)
                && !(mob instanceof Bunnidog other && other.acceptsCare())
                && !(mob instanceof TamableAnimal tame && tame.isTame());
    }
    private boolean standing(BlockPos pos) {
        var level = this.dog.level();
        return pos != null && level.hasChunkAt(pos) && this.dog.foodWithinHome(pos)
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()
                && level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP);
    }
    private boolean visible(Mob mob) {
        BlockPos from = this.dog.blockPosition(), to = mob.blockPosition();
        var low = new BlockPos(Math.min(from.getX(),to.getX()),Math.min(from.getY(),to.getY()),Math.min(from.getZ(),to.getZ()));
        var high = new BlockPos(Math.max(from.getX(),to.getX()),Math.max(from.getY(),to.getY()),Math.max(from.getZ(),to.getZ()));
        return this.dog.level().hasChunksAt(low,high)
                && this.dog.getSensing().hasLineOfSight(mob);
    }
    private boolean safe(Path route) {
        if (route == null || !route.canReach()) return false;
        double clearance = Math.min(4,this.dog.blockPosition().distSqr(this.threat.blockPosition()));
        for (int i = route.getNextNodeIndex(); i < route.getNodeCount(); i++) {
            var node = route.getNode(i);
            if (!this.dog.level().hasChunkAt(node.asBlockPos()) || !this.dog.foodWithinHome(node.asBlockPos())
                    || node.asBlockPos().distSqr(this.threat.blockPosition()) < clearance) return false;
            switch (node.type) {
                case WATER, LAVA, FIRE, FIRE_IN_NEIGHBOR, DAMAGING, DAMAGE_CAUTIOUS, POWDER_SNOW, ON_TOP_OF_POWDER_SNOW -> { return false; }
                default -> {}
            }
        }
        return true;
    }
    @Override public boolean canUse() {
        if (!wanted() || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime()+Refuge.SEARCH_INTERVAL+(this.dog.getUUID().hashCode()&7);
        this.threat = this.dog.level().getEntitiesOfClass(Mob.class,this.dog.getBoundingBox().inflate(Refuge.RANGE),this::threat)
                .stream().filter(m -> this.dog.distanceToSqr(m) <= Refuge.RANGE*Refuge.RANGE)
                .filter(this::visible).min(Comparator.comparingDouble(this.dog::distanceToSqr)).orElse(null);
        if (this.threat == null) return false;
        this.home = this.dog.homeNest();
        var candidates = new ArrayList<BlockPos>();
        for (int[] offset : NEAR_HOME) for (int up = 0; up <= 1; up++) candidates.add(this.home.offset(offset[0],up,offset[1]));
        for (int[] offset : AWAY) for (int dy = -1; dy <= 1; dy++) candidates.add(this.dog.blockPosition().offset(offset[0],dy,offset[1]));
        double before = this.dog.blockPosition().distSqr(this.threat.blockPosition());
        candidates.removeIf(p -> !standing(p) || !Refuge.safer(before,p.distSqr(this.threat.blockPosition())));
        candidates.sort(Comparator.comparingDouble(p -> p.distSqr(this.home)));
        int attempts = 0;
        for (BlockPos pos : candidates) {
            if (++attempts > Refuge.PATH_ATTEMPTS) break;
            Path route = this.dog.getNavigation().createPath(pos,0);
            if (!safe(route)) continue;
            this.destination = pos; this.path = route; return true;
        }
        this.threat = null; return false;
    }
    @Override public boolean canContinueToUse() {
        return wanted() && this.home != null && this.home.equals(this.dog.homeNest()) && threat(this.threat)
                && standing(this.destination) && this.destination.distSqr(this.threat.blockPosition()) >= Refuge.SAFE_DISTANCE*Refuge.SAFE_DISTANCE
                && this.ticks < Refuge.MAX_VISIT && this.quiet < Refuge.QUIET_TICKS;
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() {
        this.ticks = 0; this.quiet = 0; this.dog.refugeFrom(this.threat);
        this.dog.getNavigation().moveTo(this.path,1.3);
    }
    @Override public void tick() {
        if (!canContinueToUse()) return;
        this.ticks++;
        if (this.dog.distanceToSqr(this.threat) > (Refuge.RANGE+4)*(Refuge.RANGE+4)
                || !visible(this.threat)) this.quiet++;
        else this.quiet = 0;
        this.dog.getLookControl().setLookAt(this.threat,30,30);
        if (this.dog.distanceToSqr(Vec3.atBottomCenterOf(this.destination)) <= 1) this.dog.getNavigation().stop();
        else if (this.dog.getNavigation().isDone() || !safe(this.dog.getNavigation().getPath())) {
            this.dog.getNavigation().stop(); this.ticks = Refuge.MAX_VISIT;
        }
    }
    @Override public void stop() {
        this.dog.getNavigation().stop(); this.dog.refugeFrom(null);
        this.threat = null; this.home = null; this.destination = null; this.path = null;
        this.nextSearch = this.dog.level().getGameTime()+Refuge.SEARCH_INTERVAL;
    }
}
