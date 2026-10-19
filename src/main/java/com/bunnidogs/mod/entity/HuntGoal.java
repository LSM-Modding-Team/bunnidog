package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.registry.ModDietTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Wounded hunters/fishers use safe dry routes; a shore bite never becomes a swim after moving fish. */
final class HuntGoal extends Goal {
    private final Bunnidog dog;
    private LivingEntity prey;
    private BlockPos home, stand;
    private Path path;
    private long nextSearch;
    private int ticks, biteWait;
    HuntGoal(Bunnidog dog) { this.dog = dog; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }

    static boolean edible(Bunnidog dog, LivingEntity candidate) {
        return !candidate.hasCustomName() && !candidate.isPassenger() && !candidate.isVehicle()
                && !(candidate instanceof Mob mob && mob.isLeashed())
                && Foraging.prey(candidate.is(dog.diet().fishes() ? ModDietTags.FISH_PREY : ModDietTags.PREY),candidate.isBaby(),
                        candidate instanceof Player,candidate instanceof Bunnidog,
                        candidate instanceof TamableAnimal tame && tame.isTame(),dog.isAlliedTo(candidate));
    }
    private boolean valid(LivingEntity candidate) {
        return candidate != null && candidate.isAlive() && candidate.level() == this.dog.level()
                && this.dog.level().hasChunkAt(candidate.blockPosition()) && this.dog.foodWithinHome(candidate.blockPosition())
                && edible(this.dog,candidate) && HomeRoutes.visible(this.dog,candidate);
    }
    private boolean plan(LivingEntity target, int[] attempts) {
        if (this.dog.isWithinMeleeAttackRange(target) && HomeRoutes.standing(this.dog,this.dog.blockPosition())) {
            this.stand = this.dog.blockPosition(); this.path = null; return true;
        }
        var spots = new ArrayList<BlockPos>(); var pos = target.blockPosition(); spots.add(pos);
        for (var direction : Direction.Plane.HORIZONTAL) for (int up = -1; up <= 1; up++) spots.add(pos.relative(direction).above(up));
        spots.removeIf(p -> !HomeRoutes.standing(this.dog,p) || Vec3.atBottomCenterOf(p).distanceToSqr(target.position()) > 4);
        spots.sort(Comparator.comparingDouble(p -> Vec3.atBottomCenterOf(p).distanceToSqr(target.position())));
        for (var spot : spots) {
            if (++attempts[0] > Foraging.HUNT_PATH_ATTEMPTS) return false;
            var route = this.dog.getNavigation().createPath(spot,0);
            if (!HomeRoutes.safe(this.dog,route,null)) continue;
            this.stand = spot; this.path = route; return true;
        }
        return false;
    }
    @Override public boolean canUse() {
        if (!this.dog.diet().seeksPrey() || !this.dog.canForage() || this.dog.getTarget() != null
                || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime()+Foraging.SEARCH_INTERVAL;
        this.prey = null; this.path = null; this.stand = null; this.home = this.dog.homeNest();
        var candidates = this.dog.level().getEntitiesOfClass(LivingEntity.class,this.dog.getBoundingBox().inflate(Foraging.SEARCH_RADIUS),this::valid)
                .stream().filter(p -> this.dog.distanceToSqr(p) <= Foraging.SEARCH_RADIUS*Foraging.SEARCH_RADIUS)
                .sorted(Comparator.comparingDouble(this.dog::distanceToSqr)).limit(4).toList();
        int[] attempts = {0};
        for (var candidate : candidates) if (plan(candidate,attempts)) {
            this.prey = candidate; this.dog.huntTarget(candidate); this.dog.setTarget(candidate); return true;
        }
        return false;
    }
    @Override public boolean canContinueToUse() {
        if (this.prey == null || this.dog.getTarget() != this.prey || !this.dog.canForage() || !this.dog.diet().seeksPrey()
                || this.home == null || !this.home.equals(this.dog.homeNest()) || !valid(this.prey) || this.ticks >= Foraging.HUNT_TICKS
                || !HomeRoutes.dryCell(this.dog,this.dog.blockPosition())) return false;
        if (this.dog.isWithinMeleeAttackRange(this.prey) && HomeRoutes.standing(this.dog,this.dog.blockPosition())) return true;
        var active = this.dog.getNavigation().getPath();
        return this.path != null && HomeRoutes.standing(this.dog,this.stand) && HomeRoutes.safe(this.dog,this.path,null)
                && (active == null || HomeRoutes.safe(this.dog,active,null));
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() {
        this.ticks = 0; this.biteWait = 0;
        if (this.path == null) this.dog.getNavigation().stop(); else this.dog.getNavigation().moveTo(this.path,1.2);
    }
    @Override public void tick() {
        if (!canContinueToUse()) { this.dog.getNavigation().stop(); this.ticks = Foraging.HUNT_TICKS; return; }
        this.ticks++; this.biteWait--; this.dog.getLookControl().setLookAt(this.prey,30,30);
        if (this.dog.isWithinMeleeAttackRange(this.prey) && HomeRoutes.standing(this.dog,this.dog.blockPosition())) {
            this.dog.getNavigation().stop();
            if (this.biteWait <= 0 && this.dog.level() instanceof ServerLevel server) {
                this.biteWait = Foraging.HUNT_BITE_INTERVAL; this.dog.swing(InteractionHand.MAIN_HAND); this.dog.doHurtTarget(server,this.prey);
            }
        } else if (this.ticks % Foraging.HUNT_REPATH_INTERVAL == 0) {
            if (!plan(this.prey,new int[]{0}) || this.path == null || !this.dog.getNavigation().moveTo(this.path,1.2)) {
                this.ticks = Foraging.HUNT_TICKS; this.dog.getNavigation().stop();
            }
        }
    }
    @Override public void stop() {
        this.dog.getNavigation().stop();
        if (this.dog.getTarget() == this.prey) this.dog.setTarget(null);
        this.dog.huntTarget(null); this.prey = null; this.home = null; this.stand = null; this.path = null;
        this.nextSearch = this.dog.level().getGameTime()+Foraging.SEARCH_INTERVAL;
    }
}
