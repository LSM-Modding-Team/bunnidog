package com.bunnidogs.mod.genetics;

/** Amplify the source line without letting unbounded bred points break a boss fight. */
public final class ApexRules {
    private ApexRules() {}
    public static final int ABILITY_RECOVERY = 120;
    public static final double SCALE = 1.8;
    public record Combat(double health, double speed, double jump, double damage, double awareness) {}

    public static Combat combat(Genome genome, Stats source, boolean enraged) {
        Traits t = genome.traits();
        double anger = enraged ? 1.25 : 1;
        return new Combat(
                Math.clamp(240 * t.healthMultiplier() * boost(source, Stat.VITALITY, .005), 180, 720),
                Math.clamp(.28 * t.speedMultiplier() * boost(source, Stat.SWIFTNESS, .001)
                        * (enraged ? 1.15 : 1), .25, .42),
                Math.clamp(.5 * t.jumpMultiplier() * boost(source, Stat.LEAP, .001), .4, .8),
                Math.clamp(8 * boost(source, Stat.LEAP, .005) * anger, 8, 15),
                Math.clamp(32 * t.hearingMultiplier() * boost(source, Stat.SENSES, .002), 24, 64));
    }
    private static double boost(Stats source, Stat stat, double perPoint) {
        return 1 + Math.clamp(source.get(stat), 0, 100) * perPoint;
    }
}
