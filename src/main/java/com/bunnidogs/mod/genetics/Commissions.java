package com.bunnidogs.mod.genetics;

import java.util.EnumSet;

/** Concrete functional breeding targets, distinct from balanced-stat Show Ring judging. */
public final class Commissions {
    private Commissions() {}
    public static final int BREEDER_RANGE=8, POLL_TICKS=20;
    public enum Status { READY, UNAVAILABLE, BLOCKED, ABSENT, CROWDED, BREEDER, ADULT, PARENTS, HEALTH, TRANSPORTED, DANGER, GENES, STATS, CARGO, BUSY, ALREADY }
    public enum Request {
        NIGHT_GUIDE("night_guide"), COASTAL_SCOUT("coastal_scout"), MOUNTAIN_COURIER("mountain_courier"),
        EMBER_GARDENER("ember_gardener"), FROST_STEWARD("frost_steward"), DESERT_MESSENGER("desert_messenger"),
        SWAMP_HERBALIST("swamp_herbalist"), END_NURSERY("end_nursery"), CROSSING_BRANCHES("crossing_branches");
        private final String key;
        Request(String key) { this.key=key; }
        public String key() { return key; }
        public Request next() { return values()[(ordinal()+1)%values().length]; }
        public static Request parse(String key) {
            for (var request:values()) if (request.key.equals(key)) return request;
            return NIGHT_GUIDE;
        }
        public Status match(Genome genome,Stats stats,int bundles) {
            boolean genes=switch(this) {
                case NIGHT_GUIDE -> fixed(genome,Gene.COAT,"pale") && fixed(genome,Gene.EARS,"bat");
                case COASTAL_SCOUT -> fixed(genome,Gene.COAT,"seal") && fixed(genome,Gene.LEGS,"paddle") && fixed(genome,Gene.DIET,"shore_forager");
                case MOUNTAIN_COURIER -> fixed(genome,Gene.LEGS,"hoofed") && genome.isHomozygous(Gene.BUILD) && Carrying.capacity(genome)>=3;
                case EMBER_GARDENER -> fixed(genome,Gene.COAT,"ember") && fixed(genome,Gene.DIET,"nether_forager");
                case FROST_STEWARD -> fixed(genome,Gene.COAT,"snow") && fixed(genome,Gene.EARS,"tiny") && fixed(genome,Gene.DIET,"fisher");
                case DESERT_MESSENGER -> fixed(genome,Gene.EARS,"fennec") && fixed(genome,Gene.LEGS,"fleet") && fixed(genome,Gene.BUILD,"lean");
                case SWAMP_HERBALIST -> fixed(genome,Gene.COAT,"moss") && fixed(genome,Gene.LEGS,"webbed") && fixed(genome,Gene.DIET,"mushroom_eater");
                case END_NURSERY -> fixed(genome,Gene.COAT,"void") && fixed(genome,Gene.LEGS,"phase") && fixed(genome,Gene.FERTILITY,"starborn") && fixed(genome,Gene.DIET,"chorus_eater");
                case CROSSING_BRANCHES -> Purebreds.fixedCount(genome)==Gene.COUNT && wildOrigins(genome)>=4;
            };
            if (!genes) return Status.GENES;
            boolean trained=switch(this) {
                case NIGHT_GUIDE, CROSSING_BRANCHES -> stats.get(Stat.SENSES)>=20;
                case COASTAL_SCOUT -> stats.get(Stat.SENSES)>=10 && stats.get(Stat.SWIFTNESS)>=10;
                case MOUNTAIN_COURIER -> stats.get(Stat.LEAP)>=20 && stats.get(Stat.SWIFTNESS)>=10;
                case EMBER_GARDENER -> stats.get(Stat.BLOOM)>=10;
                case FROST_STEWARD -> stats.get(Stat.BLOOM)>=10 && stats.get(Stat.VITALITY)>=20;
                case DESERT_MESSENGER -> stats.get(Stat.SWIFTNESS)>=20 && stats.get(Stat.SENSES)>=10;
                case SWAMP_HERBALIST -> stats.get(Stat.BLOOM)>=10 && stats.get(Stat.SENSES)>=10;
                case END_NURSERY -> stats.get(Stat.BLOOM)>=20;
            };
            if (!trained) return Status.STATS;
            return this==MOUNTAIN_COURIER && bundles<1 ? Status.CARGO : Status.READY;
        }
    }
    private static boolean fixed(Genome genome,Gene gene,String name) {
        return genome.isHomozygous(gene) && genome.expressed(gene).name().equals(name);
    }
    public static int wildOrigins(Genome genome) {
        var origins=EnumSet.noneOf(Lineage.class);
        for (var gene:Gene.values()) {
            var lineage=genome.expressed(gene).lineage();
            if (lineage!=null) origins.add(lineage);
        }
        return origins.size();
    }
}
