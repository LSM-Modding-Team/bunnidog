package com.bunnidogs.mod.genetics;

/** Comparative practical strengths; these scores never grant capacity, education or materials. */
public enum WorkSpecialty {
    GROWER,COURIER,CRAFTSPERSON,BUILDER,CARER,TEACHER,PLANNER;
    public double affinity(MindProfile mind,Stats stats) {
        return switch(this) {
            case GROWER -> Math.min(mind.memory(),mind.handling())+.25*mind.reasoning()+Math.min(40,stats.get(Stat.BLOOM))/80.0;
            case COURIER -> Math.min(mind.memory(),mind.communication())+.25*mind.reasoning()+Math.min(40,stats.get(Stat.SENSES))/80.0;
            case CRAFTSPERSON -> Math.min(mind.handling(),mind.reasoning())+.25*mind.memory();
            case BUILDER -> Math.min(mind.handling(),mind.reasoning())+.25*mind.communication();
            case CARER -> Math.min(mind.communication(),mind.handling())+.25*mind.memory()+Math.min(40,stats.get(Stat.BLOOM))/80.0;
            case PLANNER -> Math.min(mind.reasoning(),mind.memory())+.25*mind.communication();
            case TEACHER -> Math.min(mind.communication(),mind.memory())+.25*mind.reasoning();
        };
    }
}
