package com.bunnidogs.mod.genetics;

/** Saved append-only ID, untranslated data key, and one copy's contribution to all four capacities. */
public record CognitiveAllele(int id, String key, MindProfile contribution, int founderWeight) {}
