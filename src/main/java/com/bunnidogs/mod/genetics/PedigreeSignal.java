package com.bunnidogs.mod.genetics;

/** Compare seven diploid pairs without changing the saved maternal/paternal copy order. */
public final class PedigreeSignal {
    private PedigreeSignal() {}
    public static final int POLL_TICKS = 10;
    public enum Status { EMPTY, UNLOADED, BLOCKED, ABSENT, CROWDED, UNAVAILABLE, READING }
    public static int matchingPairs(Genome reference, Genome animal) {
        int matches = 0;
        for (Gene gene : Gene.values()) {
            int a = reference.alleleId(gene,0), b = reference.alleleId(gene,1);
            int c = animal.alleleId(gene,0), d = animal.alleleId(gene,1);
            if (a == c && b == d || a == d && b == c) matches++;
        }
        return matches;
    }
    public static int signal(int pairs, boolean analog) {
        return pairs < 0 ? 0 : analog ? 1+2*Math.clamp(pairs,0,Gene.COUNT) : pairs == Gene.COUNT ? 15 : 0;
    }
}
