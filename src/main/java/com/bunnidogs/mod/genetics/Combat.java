package com.bunnidogs.mod.genetics;

import java.util.Locale;

/** Inherited combat potential. Bonds, orders and injuries never enter the genome. */
public final class Combat {
    private Combat() {}
    public static final int QUIET_TICKS=100, RECOVERY_TICKS=6000, MIN_RECOVERY_TICKS=2400;
    public record Profile(double bite,int interval,double withdrawal) {}
    public static Profile profile(Genome genome,Stats stats) {
        String build=genome.expressed(Gene.BUILD).name();
        double weight=switch (build) {
            case "slim" -> .95; case "lean" -> .9;
            case "stocky","woolly" -> 1.1; case "chonker" -> 1.2; default -> 1;
        };
        int interval=switch (build) { case "stocky","woolly" -> 24; case "chonker" -> 28; default -> 20; };
        double agility=genome.traits().speedMultiplier()*Math.sqrt(Math.max(.3,stats.multiplier(Stat.SWIFTNESS)));
        interval=Math.clamp((int)Math.round(interval/Math.max(.5,agility)),10,36);
        var temperament=genome.traits().temperament();
        double force=temperament==Temperament.FERAL ? 1.2 : temperament==Temperament.SKITTISH ? .9 : 1;
        int vitality=stats.get(Stat.VITALITY);
        double fitness=vitality>=0 ? 1+.2*Math.sqrt(vitality/20.0) : Math.max(.65,1+.02*vitality);
        double withdrawal=switch (temperament) { case LOYAL -> .4; case STOIC -> .35; case FERAL -> .25; case SKITTISH -> .55; };
        // The target version's native ATTACK_DAMAGE range ends at 2048; readouts must match actual attributes.
        return new Profile(Math.min(2048,2*weight*force*fitness*Math.sqrt(Math.max(.3,stats.multiplier(Stat.LEAP)))),interval,withdrawal);
    }
    public static int recoveryTicks(Stats stats) {
        return Math.max(MIN_RECOVERY_TICKS,(int)(RECOVERY_TICKS/Math.max(1,stats.growthMultiplier())));
    }
    public static float mealHealing(float maximumHealth) { return Math.max(2,maximumHealth*.25F); }

    /** Actual adult potential, also valid for a recorded sample/parent; no learned or live state is inferred. */
    public static String describe(Genome genome,Stats stats) {
        var profile=profile(genome,stats);
        String habit=switch (genome.traits().temperament()) {
            case LOYAL -> Texts.t("Helps your attacks and watches for nearby attackers.");
            case STOIC -> Texts.t("Helps your attacks and defends against attackers close to you.");
            case FERAL -> Texts.t("Stronger directed strikes; less automatic keeper protection.");
            case SKITTISH -> Texts.t("Warns about visible threats; a directed bite is followed by withdrawal.");
        };
        return Texts.t("Adult bite: %.1f HP / %.1f s; withdraws at %.0f%% health. %s Whistle: %.0f blocks. Injury: %.1f safe loaded minutes and half health. Base potential before enemy defenses; bond and orders are learned individually.",
                profile.bite(),profile.interval()/20.0,profile.withdrawal()*100,habit,Trail.hearing(genome,stats),recoveryTicks(stats)/1200.0);
    }
}
