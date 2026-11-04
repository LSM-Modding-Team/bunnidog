package com.bunnidogs.mod.genetics;

import java.util.Optional;

/** Bounded personal practice, never part of a Genome, sample, parent snapshot or culture. */
public record Learning(int packed) {
    public static final int MASTERED = 6;
    public static final Learning EMPTY = new Learning(0);
    public static final Learning LEGACY_FAMILIAR = new Learning(0x6666666);
    public enum Routine {
        HARVEST("harvest", CapabilityRules.Task.HARVEST), DELIVERY("delivery", CapabilityRules.Task.DELIVERY),
        GARDEN("garden", CapabilityRules.Task.PLANTING), NURSERY("nursery", CapabilityRules.Task.NURSERY),
        SCOUT("scout", CapabilityRules.Task.SCOUTING), CRAFT("craft", CapabilityRules.Task.CRAFTING),
        BUILD("build", CapabilityRules.Task.BUILDING);
        private final String key;
        private final CapabilityRules.Task task;
        Routine(String key, CapabilityRules.Task task) { this.key = key; this.task = task; }
        public String key() { return key; }
        public CapabilityRules.Task task() { return task; }
        public boolean workshop() { return this == CRAFT || this == BUILD; }
    }
    public Learning {
        int clean = 0;
        for (var routine : Routine.values()) clean |= Math.min(MASTERED, (packed >>> (4 * routine.ordinal())) & 15) << (4 * routine.ordinal());
        packed = clean;
    }
    public int progress(Routine routine) { return (packed >>> (4 * routine.ordinal())) & 15; }
    public boolean knows(Routine routine) { return progress(routine) == MASTERED; }
    public boolean independent(MindProfile mind, Routine routine) {
        return knows(routine) && CapabilityRules.potential(mind, routine.task()) == CapabilityRules.Potential.INDEPENDENT;
    }
    /** One actual successful operation earns practice; better retention/communication need fewer repetitions. */
    public Learning practice(MindProfile mind, Routine routine) {
        if (CapabilityRules.potential(mind, routine.task()) == CapabilityRules.Potential.LIMITED) return this;
        int gain = Math.clamp(1 + (int)Math.floor(mind.memory() + mind.communication()), 1, MASTERED);
        int shift = 4 * routine.ordinal();
        return new Learning((packed & ~(15 << shift)) | (Math.min(MASTERED, progress(routine) + gain) << shift));
    }
    @Override public String toString() {
        return "l3." + java.util.Arrays.stream(Routine.values()).map(r -> Integer.toString(progress(r))).collect(java.util.stream.Collectors.joining("-"));
    }
    public static Optional<Learning> parse(String text) {
        if (text == null) return Optional.empty();
        boolean legacy=text.matches("l1\\.[0-6]-[0-6]-[0-6]-[0-6]");
        boolean old=legacy || text.matches("l2\\.[0-6]-[0-6]-[0-6]-[0-6]-[0-6]");
        if (!old && !text.matches("l3\\.[0-6]-[0-6]-[0-6]-[0-6]-[0-6]-[0-6]-[0-6]")) return Optional.empty();
        int packed = 0;
        for (var routine : Routine.values()) {
            // Established Garden masters already performed these jobs. Preserve that individual ability once;
            // explicit new-schema beginners and offspring receive no cross-profession experience.
            int value=old && routine.workshop() ? text.charAt(7)=='6' ? MASTERED : 0
                    : legacy && routine==Routine.SCOUT ? MASTERED : text.charAt(3 + routine.ordinal() * 2) - '0';
            packed |= value << (routine.ordinal() * 4);
        }
        return Optional.of(new Learning(packed));
    }
}
