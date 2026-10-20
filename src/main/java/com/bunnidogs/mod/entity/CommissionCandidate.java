package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Commissions;
import com.bunnidogs.mod.genetics.Sampling;

/** The same living-animal conditions at a Board and while measuring a prospective entrant. */
public final class CommissionCandidate {
    private CommissionCandidate() {}
    public static Commissions.Status condition(Bunnidog dog) {
        if (!dog.isAlive() || !dog.acceptsCare()) return Commissions.Status.UNAVAILABLE;
        if (dog.isBaby()) return Commissions.Status.ADULT;
        if (dog.motherGenome().isEmpty() || dog.fatherGenome().isEmpty()) return Commissions.Status.PARENTS;
        if (!Sampling.healthy(dog.getHealth(),dog.getMaxHealth())) return Commissions.Status.HEALTH;
        if (dog.isPassenger() || dog.isVehicle()) return Commissions.Status.TRANSPORTED;
        if (dog.getTarget()!=null || dog.seekingRefuge() || dog.isInLove()
                || dog.getLastHurtByMob()!=null && dog.tickCount-dog.getLastHurtByMobTimestamp()<100)
            return Commissions.Status.DANGER;
        return Commissions.Status.READY;
    }
}
