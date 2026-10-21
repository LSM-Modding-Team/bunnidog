package com.bunnidogs.mod.genetics;

/** Genetic potential only: no learned routines, loyalty, health or world knowledge. */
public record MindProfile(double memory, double reasoning, double handling, double communication) {
    public static final MindProfile ZERO = new MindProfile(0, 0, 0, 0);

    public MindProfile {
        memory = bounded(memory); reasoning = bounded(reasoning);
        handling = bounded(handling); communication = bounded(communication);
    }

    private static double bounded(double value) { return Double.isFinite(value) ? Math.clamp(value, 0, 4) : 0; }

    public double get(CognitiveGene axis) {
        return switch (axis) {
            case MEMORY -> memory; case REASONING -> reasoning;
            case HANDLING -> handling; case COMMUNICATION -> communication;
        };
    }

    public boolean meets(MindProfile required) {
        return memory >= required.memory && reasoning >= required.reasoning
                && handling >= required.handling && communication >= required.communication;
    }
}
