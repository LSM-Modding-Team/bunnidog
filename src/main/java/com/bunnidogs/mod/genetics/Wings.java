package com.bunnidogs.mod.genetics;

/** Wings occupy the appended body byte; old lines migrate to grounded/grounded. */
public final class Wings {
    private Wings() {}
    public static boolean expressed(Genome genome) { return genome.expressed(Gene.WINGS).value()>0; }
    public static Genome winged(Genome genome) { return genome.with(Gene.WINGS,0,1).with(Gene.WINGS,1,1); }
}
