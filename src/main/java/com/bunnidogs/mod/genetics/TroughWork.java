package com.bunnidogs.mod.genetics;

import java.util.EnumSet;

/** Named work policies preserve the old mixed routine while making food-spending jobs explicit. */
public enum TroughWork {
    MIXED("mixed", Job.HARVEST,Job.CULTIVATE,Job.COMPOST,Job.GATHER),
    GARDEN("garden", Job.HARVEST,Job.CULTIVATE,Job.PLANT,Job.GATHER),
    GATHER("gather", Job.GATHER),
    COMPOST("compost", Job.COMPOST),
    NURSERY("nursery", Job.NURTURE),
    SUPPLY("supply", Job.SUPPLY);

    public enum Job { HARVEST, CULTIVATE, PLANT, COMPOST, GATHER, NURTURE, SUPPLY }
    private final String key;
    private final EnumSet<Job> jobs;
    TroughWork(String key, Job... jobs) {
        this.key = key; this.jobs = EnumSet.noneOf(Job.class);
        for (var job : jobs) this.jobs.add(job);
    }
    public String key() { return this.key; }
    public boolean allows(Job job) { return this.jobs.contains(job); }
    public TroughWork next() { return values()[(ordinal()+1)%values().length]; }
    public static TroughWork read(String key) {
        for (var mode : values()) if (mode.key.equals(key)) return mode;
        return MIXED;
    }
}
