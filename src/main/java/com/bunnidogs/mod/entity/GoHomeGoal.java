package com.bunnidogs.mod.entity;

import java.util.EnumSet;

import com.bunnidogs.mod.genetics.Home;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * R5 (H2, docs/HOME_SPECS.md): a dog that has claimed a nest and is more than {@link Home#HOME_RADIUS} blocks from it walks back
 * until it is within {@link Home#RETURN_RADIUS}. Registered below panic, avoid, breeding and tempt (so a scare or a guava can still
 * lead the dog away for a while) and above the stroll. Idle for a dog without a nest. The decisions are the pure
 * {@link Home#tooFar} and {@link Home#arrived}.
 */
public final class GoHomeGoal extends Goal {
    private static final double SPEED = 1.0D;

    private final Bunnidog dog;

    public GoHomeGoal(Bunnidog dog) {
        this.dog = dog;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private double distSq() {
        BlockPos nest = this.dog.homeNest();
        return nest == null ? 0.0D : this.dog.blockPosition().distSqr(nest);
    }

    @Override
    public boolean canUse() {
        return !this.dog.beingTransported() && this.dog.homeNest() != null && Home.tooFar(this.distSq());
    }

    @Override
    public boolean canContinueToUse() {
        return !this.dog.beingTransported() && this.dog.homeNest() != null && !Home.arrived(this.distSq()) && !this.dog.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.walk();
    }

    @Override
    public void tick() {
        // a path can end early (a detour, a stuck step): ask again whenever the navigation has finished
        if (this.dog.getNavigation().isDone()) {
            this.walk();
        }
    }

    @Override
    public void stop() {
        this.dog.getNavigation().stop();
    }

    private void walk() {
        BlockPos nest = this.dog.homeNest();
        if (nest != null && !this.dog.beingTransported()) {
            this.dog.getNavigation().moveTo(nest.getX() + 0.5D, nest.getY() + 1.0D, nest.getZ() + 0.5D, SPEED);
        }
    }
}
