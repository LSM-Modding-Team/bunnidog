package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** Downed animals remain inspectable/rescuable; no wandering, work or immortal body blocking. */
final class DownedGoal extends Goal {
    private final Bunnidog dog;
    DownedGoal(Bunnidog dog) { this.dog=dog; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP)); }
    @Override public boolean canUse() { return dog.combat().downed(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { dog.getNavigation().stop(); }
    @Override public void tick() { dog.getNavigation().stop(); dog.setXxa(0); dog.setZza(0); dog.setSpeed(0); }
}
