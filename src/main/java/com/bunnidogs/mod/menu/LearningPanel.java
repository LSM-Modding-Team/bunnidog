package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Immediate teach/guide choices with real-world setup advice, separate from the genetic snapshot. */
public final class LearningPanel {
    private LearningPanel() {}
    public static final int OPEN = 220, TEACH = 221, STOP = 225;
    private static Component name(Learning.Routine routine) { return TextComponents.t(routine.task().displayName()); }
    public static Component status(Bunnidog dog, Learning.Routine routine) {
        var potential = CapabilityRules.potential(dog.mind(), routine.task());
        var knowledge = dog.learning().knowledge();
        if (potential == CapabilityRules.Potential.LIMITED) return TextComponents.t("too complex");
        if (knowledge.knows(routine)) return TextComponents.t(potential == CapabilityRules.Potential.SUPPORTED ? "learned; needs guidance" : "learned");
        return knowledge.progress(routine) == 0 ? TextComponents.t("needs a lesson")
                : TextComponents.t("practice %s/%s", knowledge.progress(routine), Learning.MASTERED);
    }
    public static CareView view(Player player, Bunnidog dog) {
        var rows = new ArrayList<Component>(); var controls = new ArrayList<CareView.Control>();
        rows.add(TextComponents.t("Genes set potential; successful guided work teaches the routine."));
        rows.add(TextComponents.t("Care, home, cargo and trusted recall stay useful at every capacity."));
        for (var routine : Learning.Routine.values()) {
            rows.add(TextComponents.t("%s: %s", name(routine), status(dog, routine)));
            boolean known = dog.learning().knowledge().knows(routine);
            controls.add(new CareView.Control(TEACH + routine.ordinal(), TextComponents.t(known ? "Guide %s" : "Teach %s", name(routine)),
                    dog.learning().canBegin(player, routine) && dog.learning().lesson() != routine));
        }
        rows.add(TextComponents.t("Garden tending needs Bloom +%s; nursery +%s; delivery needs Senses +%s.",
                Gardening.MIN_CULTIVATION_BLOOM, Gardening.MIN_NURSERY_BLOOM, Supplying.MIN_SENSES));
        rows.add(TextComponents.t("Practice needs a healthy adult at home, resident orders, daylight and enabled work. Normal work rest still applies."));
        var lesson = dog.learning().lesson();
        if (lesson != null) rows.add(TextComponents.t(dog.learning().guided() ? "Current lesson: %s; instructor nearby." : "Current lesson: %s; waiting for its instructor to return.", name(lesson)));
        controls.add(new CareView.Control(STOP, TextComponents.t("Stop lesson"), dog.learning().authorized(player) && lesson != null));
        controls.add(new CareView.Control(CarePanels.BACK, Component.translatable("care.bunnidogs.button.back"), true));
        Component next;
        if (dog.isBaby()) next = TextComponents.t("Let the pup grow. A newborn inherits potential, not its parents' lessons.");
        else if (dog.homeNest() == null) next = TextComponents.t("Claim a Nest first, then choose a routine here.");
        else if (dog.getHealth() < dog.getMaxHealth()) next = TextComponents.t("Heal and rest first; learned progress stays saved.");
        else if (dog.trail().ordered()) next = TextComponents.t("Use resident orders before teaching chores. Recall always interrupts safely.");
        else if (lesson == null) next = TextComponents.t("Choose a lesson, prepare its work nearby, and watch within %s blocks. Practice is saved between sessions.", com.bunnidogs.mod.entity.BunnidogLearning.GUIDE_RANGE);
        else next = switch (lesson) {
            case HARVEST -> TextComponents.t("Enable a tended trough beside ripe food it can eat. Stay nearby and watch it harvest into the trough.");
            case DELIVERY -> TextComponents.t("Use a Supply trough with more than 8 ordinary food and a receiving dish within 4 blocks with room below 8. Watch both dishes.");
            case GARDEN -> TextComponents.t("Use Garden or Compost on a tended trough. Leave suitable plants or bare planting beds and more than 8 compatible food for paid work.");
            case NURSERY -> TextComponents.t("Use Nursery on a tended trough with more than 8 plain guavas, beside an unfinished loaded Cradle. Each lesson spends a real guava.");
        };
        return new CareView(TextComponents.t("Learning: %s", dog.getDisplayName()), next, rows, controls);
    }
}
