package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.List;

/** Conservative loose materials from living adults; every product shares one grooming recovery. */
public final class Grooming {
    private Grooming() {}
    public static final int MIN_SENSES = 10;
    public static final int MIN_STARLIT_BLOOM = 20;
    public static final int MIN_MOSS_BLOOM = 10;
    public static final int MIN_FROST_BLOOM = 10;
    public static final int COUNT = 1;
    public static final int RECOVERY_TICKS = 10 * 60 * 20;
    public enum Yield {
        WHISKER("item.bunnidogs.whisker"), GLOWSTONE("item.minecraft.glowstone_dust"), STARLIT("item.bunnidogs.starlit_dust"), MOSS("block.minecraft.moss_carpet"), SNOWBALL("item.minecraft.snowball");
        private final String translationKey;
        Yield(String translationKey) { this.translationKey = translationKey; }
        public String translationKey() { return this.translationKey; }
    }
    public static boolean developed(Stats stats) { return stats.get(Stat.SENSES) >= MIN_SENSES; }
    public static List<Yield> yields(Genome genome, Stats stats) {
        return yields(genome,stats,false);
    }
    public static List<Yield> yields(Genome genome, Stats stats, boolean cold) {
        List<Yield> out = new ArrayList<>();
        if (developed(stats)) out.add(Yield.WHISKER);
        if (genome.expressed(Gene.COAT).name().equals("glow")) out.add(Yield.GLOWSTONE);
        if (genome.expressed(Gene.FERTILITY).name().equals("starborn") && stats.get(Stat.BLOOM) >= MIN_STARLIT_BLOOM) out.add(Yield.STARLIT);
        if (genome.expressed(Gene.COAT).name().equals("moss") && stats.get(Stat.BLOOM) >= MIN_MOSS_BLOOM) out.add(Yield.MOSS);
        if (cold && genome.expressed(Gene.COAT).name().equals("snow") && stats.get(Stat.BLOOM) >= MIN_FROST_BLOOM) out.add(Yield.SNOWBALL);
        return List.copyOf(out);
    }
}
