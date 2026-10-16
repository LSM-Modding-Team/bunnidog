package com.bunnidogs.mod.genetics;

import java.util.List;

/** Inherited food preferences. Guavas always breed; these extra foods only heal or grow pups. */
public enum Diet {
    PLAIN("plain", "Guavas only.", List.of()),
    GRAZER("grazer", "Hay, wheat and grass.", List.of("grazer")),
    FORAGER("forager", "Carrots, potatoes, beetroot, apples and berries.", List.of("forager")),
    HUNTER("hunter", "Raw meat; wounded adults hunt small prey near their Nest.", List.of("hunter")),
    OMNIVORE("omnivore", "Grazer, forager and hunter foods; can graze and hunt.", List.of("grazer", "forager", "hunter")),
    FISHER("fisher", "Raw fish; wounded adults hunt reachable fish near their Nest.", List.of("fisher")),
    DESERT_BROWSER("desert_browser", "Cactus and dried kelp.", List.of("desert")),
    MUSHROOM_EATER("mushroom_eater", "Red and brown mushrooms.", List.of("swamp")),
    CAVE_FORAGER("cave_forager", "Glow berries.", List.of("cave")),
    MOUNTAIN_GRAZER("mountain_grazer", "Grazer foods plus moss and azalea leaves.", List.of("grazer", "mountain")),
    NETHER_FORAGER("nether_forager", "Nether wart and crimson or warped fungi.", List.of("nether")),
    CHORUS_EATER("chorus_eater", "Chorus fruit.", List.of("end"));

    private final String key;
    private final String foods;
    private final List<String> tags;

    Diet(String key, String foods, List<String> tags) { this.key = key; this.foods = foods; this.tags = tags; }
    public String key() { return key; }
    public String foods() { return foods; }
    /** Paths below bunnidogs:diet/; data packs can extend these item tags. */
    public List<String> tags() { return tags; }
    public boolean grazesHay() { return tags.contains("grazer"); }
    public boolean grazesCrops() { return grazesHay() || tags.contains("forager"); }
    public boolean hunts() { return tags.contains("hunter"); }
    public boolean fishes() { return this == FISHER; }
    public boolean seeksPrey() { return hunts() || fishes(); }
    public static Diet of(Genome genome) { return named(genome.expressed(Gene.DIET).name()); }
    public static Diet named(String key) {
        for (Diet diet : values()) if (diet.key.equals(key)) return diet;
        throw new IllegalArgumentException("Unknown diet: " + key);
    }
}
