package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Teach/guide actions report personal learning; inherited capacity stays in the measuring tools. */
public final class LearningPanel {
    private LearningPanel() {}
    public static final int OPEN = 220, TEACH = 221, STOP = 225;
    private static Component name(Learning.Routine routine) { return TextComponents.t(routine.task().displayName()); }
    public static Component status(Bunnidog dog, Learning.Routine routine) {
        var potential = CapabilityRules.potential(dog.mind(), routine.task());
        var knowledge = dog.learning().knowledge();
        if (potential == CapabilityRules.Potential.LIMITED) return TextComponents.t("too complex");
        if (knowledge.knows(routine)) return TextComponents.t(potential == CapabilityRules.Potential.SUPPORTED ? "learned; needs guidance" : "learned");
        return knowledge.progress(routine) == 0 ? TextComponents.t("needs a lesson") : AnimalPanels.text("learning");
    }
    public static CareView view(Player player, Bunnidog dog) {
        var rows = new ArrayList<Component>(); var controls = new ArrayList<CareView.Control>();
        for (var routine : Learning.Routine.values()) {
            boolean known = dog.learning().knowledge().knows(routine);
            boolean learned=known && CapabilityRules.potential(dog.mind(),routine.task())==CapabilityRules.Potential.INDEPENDENT;
            controls.add(new CareView.Control(TEACH + routine.ordinal(), learned ? AnimalPanels.text("learned_label",name(routine)) : TextComponents.t(known ? "Guide %s" : "Teach %s", name(routine)),
                    dog.learning().canBegin(player, routine) && dog.learning().lesson() != routine,CareView.Control.PRIMARY,lessonReason(player,dog,routine)));
        }
        var lesson = dog.learning().lesson();
        if (lesson!=null) controls.add(new CareView.Control(STOP, TextComponents.t("Stop lesson"), dog.learning().authorized(player)));
        controls.add(new CareView.Control(CarePanels.BACK, Component.translatable("care.bunnidogs.button.back"), true));
        Component next=dog.isBaby() ? AnimalPanels.text("still_growing")
                : dog.homeNest()==null ? AnimalPanels.text("need_home")
                : dog.getHealth()<dog.getMaxHealth() ? AnimalPanels.text("injured")
                : dog.trail().ordered() ? AnimalPanels.text("busy")
                : lesson==null ? Component.empty() : AnimalPanels.text("lesson_active",name(lesson));
        return new CareView(TextComponents.t("Learning: %s", dog.getDisplayName()), next, rows, controls);
    }
    private static Component lessonReason(Player player,Bunnidog dog,Learning.Routine routine) {
        if (dog.learning().canBegin(player,routine) && dog.learning().lesson()!=routine) return Component.empty();
        return !dog.learning().authorized(player) ? AnimalPanels.text("keeper_only")
                : dog.isBaby() ? AnimalPanels.text("still_growing") : dog.homeNest()==null ? AnimalPanels.text("need_home")
                : dog.getHealth()<dog.getMaxHealth() ? AnimalPanels.text("injured")
                : dog.learning().lesson()==routine ? AnimalPanels.text("lesson_active",name(routine))
                : dog.learning().knowledge().knows(routine) && CapabilityRules.potential(dog.mind(),routine.task())==CapabilityRules.Potential.INDEPENDENT
                ? AnimalPanels.text("already_learned") : AnimalPanels.text("lesson_unavailable");
    }

}
