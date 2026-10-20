package com.bunnidogs.mod.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Useful now, then concrete specialties; does not perform a path search or change animal intent. */
public final class RolePanel {
    private RolePanel() {}
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    public static CareView view(Player player,Bunnidog dog) {
        var rows=new ArrayList<Component>();
        rows.add(t("roles.packs",dog.bundleCapacity(),dog.carriedBundleCount()));
        rows.add(t(Trail.qualified(dog.genome()) ? "roles.partner" : dog.genome().traits().temperament()==Temperament.FERAL ? "roles.guard" : "roles.independent"));
        rows.add(Component.translatable("message.bunnidogs.care.gardening",Component.translatable("message.bunnidogs.gardening."+dog.gardeningStatus().name().toLowerCase(Locale.ROOT))));
        rows.add(t("roles.harvest"));
        rows.add(t("roles.cultivation",String.format(Locale.ROOT,"%+d",dog.stats().get(Stat.BLOOM)),Gardening.MIN_CULTIVATION_BLOOM,t(Gardening.cultivates(dog.stats()) ? "roles.available" : "roles.develop")));
        rows.add(t("roles.nursery",Gardening.MIN_NURSERY_BLOOM,t(Gardening.nurses(dog.stats()) ? "roles.available" : "roles.develop")));
        rows.add(t("roles.supply",String.format(Locale.ROOT,"%+d",dog.stats().get(Stat.SENSES)),Supplying.MIN_SENSES,t(Supplying.qualified(dog.stats()) ? "roles.available" : "roles.develop")));
        // Keep the short, already-authoritative expressed-trait benefit; exact rules remain in Calipers.
        for (var gene:List.of(Gene.COAT,Gene.LEGS,Gene.EARS)) {
            var allele=dog.genome().expressed(gene); String explanation=Adaptations.explanation(gene,allele);
            if (!explanation.isEmpty()) rows.add(t("roles.trait",Plain.label(allele),explanation.split("\\. ",2)[0]+(explanation.contains(". ") ? "." : "")));
        }
        rows.add(t("roles.breeding"));
        var controls=new ArrayList<CareView.Control>();
        controls.add(new CareView.Control(CarePanels.BACK,t("button.back"),true));
        controls.add(new CareView.Control(CarePanels.WORK,t(dog.workPaused() ? "button.work_on" : "button.work_off"),player.mayBuild()));
        if (dog.carriedBundleCount()>0) controls.add(new CareView.Control(CarePanels.CARGO,t("button.cargo"),true));
        return new CareView(t("roles.title",dog.getDisplayName()),t(dog.isBaby() ? "roles.next.pup" : dog.getHealth()<dog.getMaxHealth() ? "roles.next.heal" : "roles.next.adult"),rows,controls);
    }
}
