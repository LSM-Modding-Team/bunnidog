package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import java.util.Optional;

import com.bunnidogs.mod.genetics.Home;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * R8 (H4, docs/HOME_SPECS.md): at dusk an ADULT with a claimed nest walks to it and stays within {@link Home#NIGHT_RADIUS} blocks until
 * dawn. The goal keeps running all night (so the stroll below it cannot take the dog away again) and ends at dawn, when it was scared
 * off (panic and avoid are above it) or when the nest is gone. Day and night come from the dimension's default clock, so rain and
 * thunder do not count, and a dimension without a clock (Nether, End) never has a night. The decisions are the pure
 * {@link Home#isNight} and {@link Home#nightFar}.
 */
public final class NightHomeGoal extends Goal {
    private static final double SPEED = 1.0D;

    private final Bunnidog dog;
    private int retry;

    public NightHomeGoal(Bunnidog dog) {
        this.dog = dog;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /** True when it is night in this dog's dimension (server side only; a client or a clockless dimension is never night). */
    private boolean night() {
        if (!(this.dog.level() instanceof ServerLevel level)) {
            return false;
        }
        Optional<Holder<WorldClock>> clock = level.dimensionType().defaultClock();
        return clock.isPresent() && Home.isNight(level.clockManager().getTotalTicks(clock.get()));
    }

    private double distSq() {
        BlockPos nest = this.dog.homeNest();
        return nest == null ? 0.0D : this.dog.blockPosition().distSqr(nest);
    }

    private boolean wanted() {
        return !this.dog.trail().ordered() && !this.dog.beingTransported() && !this.dog.isBaby() && this.dog.homeNest() != null && this.night();
    }

    @Override
    public boolean canUse() {
        return this.wanted();
    }

    @Override
    public boolean canContinueToUse() {
        return this.wanted();
    }

    @Override
    public void start() {
        this.retry = 0;
        this.walk();
    }

    @Override
    public void tick() {
        if (!this.wanted()) return;
        if (!Home.nightFar(this.distSq())) {
            // home: stand still until dawn
            if (!this.dog.getNavigation().isDone()) {
                this.dog.getNavigation().stop();
            }
            return;
        }
        if (this.dog.getNavigation().isDone()) {
            // a path can end early or not exist: ask again, but not every tick
            this.retry--;
            if (this.retry <= 0) {
                this.walk();
            }
        }
    }

    @Override
    public void stop() {
        this.dog.getNavigation().stop();
    }

    private void walk() {
        this.retry = Home.NIGHT_RETRY;
        BlockPos nest = this.dog.homeNest();
        if (nest != null && !this.dog.beingTransported()) {
            this.dog.getNavigation().moveTo(nest.getX() + 0.5D, nest.getY() + 1.0D, nest.getZ() + 0.5D, SPEED);
        }
    }
}
