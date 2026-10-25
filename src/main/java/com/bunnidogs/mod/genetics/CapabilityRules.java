package com.bunnidogs.mod.genetics;

/** Potential for a particular task, never a promise of learned skill or available world resources. */
public final class CapabilityRules {
    private CapabilityRules() {}
    public enum Potential { INDEPENDENT, SUPPORTED, LIMITED }
    public enum Task {
        HARVEST("Harvest", new MindProfile(.75, .25, .5, .5), new MindProfile(1.5, .75, 1, .75)),
        DELIVERY("Delivery", new MindProfile(1, .5, .75, .75), new MindProfile(2, 1.5, 1.25, 1)),
        PLANTING("Planting", new MindProfile(1.5, 1, 1.25, 1), new MindProfile(2, 1.75, 1.75, 1.25)),
        NURSERY("Family care", new MindProfile(1.5, 1.25, 1.25, 1.5), new MindProfile(2, 1.75, 1.5, 2)),
        BUILDING("Building", new MindProfile(2, 1.75, 2, 1.5), new MindProfile(2.75, 2.75, 2.75, 2)),
        TEACHING("Teaching", new MindProfile(2, 1.5, 1, 2), new MindProfile(2.75, 2.5, 1.5, 2.75)),
        PLANNING("Planning", new MindProfile(2.5, 2.75, 1.5, 2.25), new MindProfile(3, 3.25, 1.75, 2.75)),
        SCOUTING("Scouting", new MindProfile(1, .75, .5, .75), new MindProfile(1.75, 1.5, .75, 1.25)),
        TACTICS("Group tactics", new MindProfile(1.25, 1, .75, 1), new MindProfile(2, 1.75, 1.25, 1.75));
        private final String label;
        private final MindProfile supported, independent;
        Task(String label, MindProfile supported, MindProfile independent) {
            this.label = label; this.supported = supported; this.independent = independent;
        }
        public String label() { return Texts.t(label); }
        public String displayName() { return label; }
        public MindProfile required(boolean withSupport) { return withSupport ? supported : independent; }
    }
    public static Potential potential(MindProfile mind, Task task) {
        return mind.meets(task.independent) ? Potential.INDEPENDENT
                : mind.meets(task.supported) ? Potential.SUPPORTED : Potential.LIMITED;
    }
    public static String describe(Potential potential) {
        return switch (potential) {
            case INDEPENDENT -> Texts.t("can learn"); case SUPPORTED -> Texts.t("with help"); case LIMITED -> Texts.t("too complex");
        };
    }
    public static String requirements(Task task, boolean support) {
        var required = task.required(support);
        return Texts.t("Requires memory %.2f, reasoning %.2f, handling %.2f, communication %.2f.",
                required.memory(), required.reasoning(), required.handling(), required.communication());
    }
}
