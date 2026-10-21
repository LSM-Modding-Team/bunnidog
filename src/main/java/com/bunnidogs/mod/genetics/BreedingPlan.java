package com.bunnidogs.mod.genetics;

import java.util.Optional;

/** Read-only table choices. Levels refer to the append-only guava ladder; no seed or item is saved/rolled. */
public record BreedingPlan(int guavaA, int guavaB, boolean mutagen, int fortune) {
    public static final BreedingPlan PLAIN = new BreedingPlan(0,0,false);
    public BreedingPlan(int guavaA, int guavaB, boolean mutagen) { this(guavaA,guavaB,mutagen,0); }
    public BreedingPlan {
        int count = GuavaTier.values().length;
        if (guavaA < 0 || guavaA >= count || guavaB < 0 || guavaB >= count)
            throw new IllegalArgumentException("Unknown planned guava tier");
        if (fortune < 0 || fortune > 255) throw new IllegalArgumentException("Unknown planned Fortune level");
    }
    public double guavaQuality() { return GuavaTier.average(this.guavaA,this.guavaB); }
    public double fortuneBonus() { return Seed.fortuneBonus(this.fortune); }
    public Seed seed(Tissue a, Tissue b) {
        var seed = Seed.of(a.genome(),a.stats(),b.genome(),b.stats(),guavaQuality());
        return this.mutagen ? seed.withMutagen() : seed;
    }
    public String guavas() { return Texts.t(GuavaTier.ofLevel(this.guavaA).displayName())+" + "+Texts.t(GuavaTier.ofLevel(this.guavaB).displayName()); }
    @Override public String toString() { return this.guavaA+","+this.guavaB+","+(this.mutagen ? "1" : "0")+(this.fortune == 0 ? "" : ","+this.fortune); }
    public static Optional<BreedingPlan> parse(String text) {
        if (text == null || text.length() > 16) return Optional.empty();
        String[] fields = text.split(",",-1);
        if (fields.length < 3 || fields.length > 4 || !(fields[2].equals("0") || fields[2].equals("1"))) return Optional.empty();
        try { return Optional.of(new BreedingPlan(Integer.parseInt(fields[0]),Integer.parseInt(fields[1]),fields[2].equals("1"),
                fields.length == 4 ? Integer.parseInt(fields[3]) : 0)); }
        catch (IllegalArgumentException e) { return Optional.empty(); }
    }
}
