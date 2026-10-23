package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Personally practiced routines and work controls; no inferred genes or bred-point measurements. */
public final class RolePanel {
    private RolePanel() {}
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    public static CareView view(Player player,Bunnidog dog) {
        var rows=new ArrayList<Component>();
        for (var routine:Learning.Routine.values())
            if (dog.learning().knowledge().progress(routine)>0)
                rows.add(TextComponents.t("%s: %s",TextComponents.t(routine.task().displayName()),LearningPanel.status(dog,routine)));
        var controls=new ArrayList<CareView.Control>();
        controls.add(new CareView.Control(CarePanels.BACK,t("button.back"),true));
        controls.add(new CareView.Control(LearningPanel.OPEN,TextComponents.t("Lessons"),true));
        controls.add(new CareView.Control(CarePanels.WORK,t(dog.workPaused() ? "button.work_on" : "button.work_off"),player.mayBuild()));
        if (FamilyPanels.hasCalipers(player)) controls.add(new CareView.Control(FamilyPanels.COMPARE,t("button.compare_goal"),true));
        if (dog.carriedBundleCount()>0) controls.add(new CareView.Control(CarePanels.CARGO,t("button.cargo"),true));
        return new CareView(AnimalPanels.text("work"),LearningPanel.activity(dog),rows,controls);
    }
}
