package com.bunnidogs.mod.genetics;

/** Voluntary companion rules. Training/order are care state, never inherited genome data. */
public final class Trail {
    private Trail() {}
    public static final int CALL_COOLDOWN=40, LOST_KEEPER_TICKS=200, MAX_CALLS=16;
    public static final int MAX_HEARING=32, MAX_FOLLOW_DISTANCE=64, REPATH_TICKS=20;
    public enum Order {
        RESIDENT, FOLLOW, WAIT;
        public static Order parse(String value) {
            for (var order:values()) if (order.name().equalsIgnoreCase(value)) return order;
            return RESIDENT;
        }
    }
    public enum Status { READY, UNAVAILABLE, ADULT, HEALTH, TEMPERAMENT, DANGER, FOOD, OTHER_KEEPER, BUSY }
    public static boolean qualified(Genome genome) {
        var temperament=genome.traits().temperament();
        return switch (temperament) { case LOYAL, STOIC, FERAL, SKITTISH -> true; };
    }
    public static double hearing(Genome genome,Stats stats) {
        return Math.clamp(16*genome.traits().earScale()+Math.max(0,stats.get(Stat.SENSES))*.2,8,MAX_HEARING);
    }
}
