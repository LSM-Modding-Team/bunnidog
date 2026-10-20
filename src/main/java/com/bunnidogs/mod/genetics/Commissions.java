package com.bunnidogs.mod.genetics;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;

/** Concrete functional breeding targets, distinct from balanced-stat Show Ring judging. */
public final class Commissions {
    private Commissions() {}
    public static final int BREEDER_RANGE=8, POLL_TICKS=20;
    public enum Status { READY, UNAVAILABLE, BLOCKED, ABSENT, CROWDED, BREEDER, ADULT, PARENTS, HEALTH, TRANSPORTED, DANGER, GENES, STATS, CARGO, BUSY, ALREADY }
    public record Check(String key,String detail,int current,int required,Status failure,java.util.Optional<Gene> gene,java.util.Optional<Stat> stat) {
        public Check(String key,String detail,int current,int required,Status failure) {
            this(key,detail,current,required,failure,java.util.Optional.empty(),java.util.Optional.empty());
        }
        public boolean met() { return current>=required; }
    }
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
            for (var check:checks(genome,stats,bundles)) if (!check.met()) return check.failure();
            return Status.READY;
        }
        /** The actual predicate also supplies the player's checklist; the two cannot drift apart. */
        public List<Check> checks(Genome genome,Stats stats,int bundles) {
            var out=new ArrayList<Check>();
            switch (this) {
                case NIGHT_GUIDE -> { gene(out,genome,Gene.COAT,"pale"); gene(out,genome,Gene.EARS,"bat"); }
                case COASTAL_SCOUT -> { gene(out,genome,Gene.COAT,"seal"); gene(out,genome,Gene.LEGS,"paddle"); gene(out,genome,Gene.DIET,"shore_forager"); }
                case MOUNTAIN_COURIER -> {
                    gene(out,genome,Gene.LEGS,"hoofed");
                    out.add(new Check("build","",genome.isHomozygous(Gene.BUILD) ? Carrying.capacity(genome) : 0,3,Status.GENES,java.util.Optional.of(Gene.BUILD),java.util.Optional.empty()));
                }
                case EMBER_GARDENER -> { gene(out,genome,Gene.COAT,"ember"); gene(out,genome,Gene.DIET,"nether_forager"); }
                case FROST_STEWARD -> { gene(out,genome,Gene.COAT,"snow"); gene(out,genome,Gene.EARS,"tiny"); gene(out,genome,Gene.DIET,"fisher"); }
                case DESERT_MESSENGER -> { gene(out,genome,Gene.EARS,"fennec"); gene(out,genome,Gene.LEGS,"fleet"); gene(out,genome,Gene.BUILD,"lean"); }
                case SWAMP_HERBALIST -> { gene(out,genome,Gene.COAT,"moss"); gene(out,genome,Gene.LEGS,"webbed"); gene(out,genome,Gene.DIET,"mushroom_eater"); }
                case END_NURSERY -> { gene(out,genome,Gene.COAT,"void"); gene(out,genome,Gene.LEGS,"phase"); gene(out,genome,Gene.FERTILITY,"starborn"); gene(out,genome,Gene.DIET,"chorus_eater"); }
                case CROSSING_BRANCHES -> {
                    out.add(new Check("fixed","",Purebreds.fixedCount(genome),Gene.COUNT,Status.GENES));
                    out.add(new Check("origins","",wildOrigins(genome),4,Status.GENES));
                }
            }
            switch (this) {
                case NIGHT_GUIDE, CROSSING_BRANCHES -> stat(out,stats,Stat.SENSES,20);
                case COASTAL_SCOUT -> { stat(out,stats,Stat.SENSES,10); stat(out,stats,Stat.SWIFTNESS,10); }
                case MOUNTAIN_COURIER -> { stat(out,stats,Stat.LEAP,20); stat(out,stats,Stat.SWIFTNESS,10); }
                case EMBER_GARDENER -> stat(out,stats,Stat.BLOOM,10);
                case FROST_STEWARD -> { stat(out,stats,Stat.BLOOM,10); stat(out,stats,Stat.VITALITY,20); }
                case DESERT_MESSENGER -> { stat(out,stats,Stat.SWIFTNESS,20); stat(out,stats,Stat.SENSES,10); }
                case SWAMP_HERBALIST -> { stat(out,stats,Stat.BLOOM,10); stat(out,stats,Stat.SENSES,10); }
                case END_NURSERY -> stat(out,stats,Stat.BLOOM,20);
            }
            if (this==MOUNTAIN_COURIER) out.add(new Check("cargo","",bundles,1,Status.CARGO));
            return List.copyOf(out);
        }
    }
    private static void gene(List<Check> out,Genome genome,Gene gene,String name) {
        int copies=(genome.allele(gene,0).name().equals(name) ? 1 : 0)+(genome.allele(gene,1).name().equals(name) ? 1 : 0);
        out.add(new Check("gene."+gene.name().toLowerCase(java.util.Locale.ROOT),name,copies,2,Status.GENES,java.util.Optional.of(gene),java.util.Optional.empty()));
    }
    private static void stat(List<Check> out,Stats stats,Stat stat,int target) {
        out.add(new Check("stat",stat.displayName(),stats.get(stat),target,Status.STATS,java.util.Optional.empty(),java.util.Optional.of(stat)));
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
