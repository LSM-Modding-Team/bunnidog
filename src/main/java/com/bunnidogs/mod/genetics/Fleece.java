package com.bunnidogs.mod.genetics;

/** Modest renewable loose fleece; full pelts and the death-drop tables keep their existing role. */
public final class Fleece {
    private Fleece() {}
    public static final int COUNT = 2;
    public static final int REGROW_TICKS = 10 * 60 * 20;
    public static boolean ready(Genome genome, boolean adult, float health, float maximum, boolean recovered) {
        return adult && recovered && genome.expressed(Gene.BUILD).name().equals("woolly") && Sampling.healthy(health,maximum);
    }
}
