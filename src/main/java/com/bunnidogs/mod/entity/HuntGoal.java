package com.bunnidogs.mod.entity;

import java.util.Comparator;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.registry.ModDietTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;

/** A wounded hunter/fisher adult takes matching reachable prey near its Nest; never overrides retaliation. */
final class HuntGoal extends MeleeAttackGoal {
    private final Bunnidog dog;
    private LivingEntity prey;
    private long nextSearch;
    private int ticks;

    HuntGoal(Bunnidog dog) { super(dog, 1.2D, true); this.dog = dog; }

    private boolean valid(LivingEntity candidate) {
        return candidate.isAlive() && this.dog.foodWithinHome(candidate.blockPosition())
                && Foraging.prey(candidate.is(this.dog.diet().fishes() ? ModDietTags.FISH_PREY : ModDietTags.PREY), candidate.isBaby(),
                        candidate instanceof Player, candidate instanceof Bunnidog,
                        candidate instanceof TamableAnimal tame && tame.isTame(), this.dog.isAlliedTo(candidate));
    }

    @Override public boolean canUse() {
        if (!this.dog.diet().seeksPrey() || !this.dog.canForage() || this.dog.getTarget() != null
                || this.dog.level().getGameTime() < this.nextSearch) return false;
        this.nextSearch = this.dog.level().getGameTime() + Foraging.SEARCH_INTERVAL;
        this.prey = this.dog.level().getEntitiesOfClass(LivingEntity.class, this.dog.getBoundingBox().inflate(Foraging.SEARCH_RADIUS), this::valid)
                .stream().filter(p -> this.dog.getSensing().hasLineOfSight(p))
                .sorted(Comparator.comparingDouble(p -> this.dog.distanceToSqr(p))).limit(4)
                .filter(p -> {
                    if (this.dog.isWithinMeleeAttackRange(p)) return true;
                    var path = this.dog.getNavigation().createPath(p,0);
                    return path != null && path.canReach();
                })
                .findFirst().orElse(null);
        if (this.prey == null) return false;
        // Super's actual path test decides whether the selected prey can be pursued.
        this.dog.setTarget(this.prey);
        if (super.canUse()) { this.dog.huntTarget(this.prey); return true; }
        this.dog.setTarget(null); this.prey = null;
        return false;
    }

    @Override public boolean canContinueToUse() {
        return this.prey != null && this.dog.getTarget() == this.prey && this.dog.canForage() && this.dog.diet().seeksPrey()
                && this.valid(this.prey) && this.ticks < Foraging.HUNT_TICKS && super.canContinueToUse();
    }
    @Override public void start() { this.ticks = 0; super.start(); }
    @Override public void tick() { this.ticks++; if (this.canContinueToUse()) super.tick(); }
    @Override public void stop() {
        super.stop();
        if (this.dog.getTarget() == this.prey) this.dog.setTarget(null);
        this.dog.huntTarget(null); this.prey = null;
    }
}
