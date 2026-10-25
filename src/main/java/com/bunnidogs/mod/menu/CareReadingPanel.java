package com.bunnidogs.mod.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.BunnidogCare;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.network.TextComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Requested Calipers readings, grouped by purpose instead of flooding chat. The actual bound animal remains unchanged. */
public final class CareReadingPanel {
    private CareReadingPanel() {}
    public static final int FIRST=231, LAST=235;
    private static final String[] GROUPS={"health","home","work","field","combat"};
    private record Reading(int group,Component line) {}
    private static Component t(String key,Object... args) { return Component.translatable("measurement.bunnidogs."+key,args); }
    public static boolean action(int id) { return id>=FIRST && id<=LAST; }
    public static CareView view(Player player,Bunnidog dog,int page) {
        if (!FamilyPanels.hasCalipers(player)) return AnimalPanels.home(player,dog);
        var controls=new ArrayList<CareView.Control>();
        if (page<0) {
            for (int i=0;i<GROUPS.length;i++) controls.add(new CareView.Control(FIRST+i,t(GROUPS[i]),true));
            controls.add(new CareView.Control(CarePanels.GENETICS,t("genes"),true));
            controls.add(AnimalPanels.back());
            return new CareView(t("title",dog.getDisplayName()),Component.empty(),List.of(),controls);
        }
        var rows=new ArrayList<Component>();
        if (page==0) {
            rows.add(t("health_value",String.format(Locale.ROOT,"%.1f",dog.getHealth()),String.format(Locale.ROOT,"%.1f",dog.getMaxHealth())));
            if (dog.isBaby()) rows.add(AnimalPanels.text("still_growing"));
        }
        if (page==4 && dog.combat().recovering()) rows.add(Component.translatable("care.bunnidogs.combat.recovery",(dog.combat().recoveryLeft()+19)/20));
        if (page==4 && dog.combat().bonded())rows.add(dog.combat().tactics().status());
        if (page==2) workRows(dog,rows);
        else for (var reading:readings(player,dog)) if (reading.group()==page) rows.add(reading.line());
        controls.add(AnimalPanels.back());
        return new CareView(t("page_title",t(GROUPS[page]),dog.getDisplayName()),Component.empty(),rows,controls);
    }
    private static List<Reading> readings(Player player,Bunnidog dog) {
        var rows=new ArrayList<Reading>();
        var combat=com.bunnidogs.mod.genetics.Combat.profile(dog.genome(),dog.stats());
        rows.add(new Reading(4,Component.translatable("care.bunnidogs.combat.bite",String.format(Locale.ROOT,"%.1f",combat.bite()),String.format(Locale.ROOT,"%.1f",combat.interval()/20.0))));
        var nest = dog.homeNest();
        careRow(rows,"combat",Component.translatable("care.bunnidogs.combat.intent."+dog.combat().intent()));
        if (dog.trail().keeper()!=null) careRow(rows,"trail",Component.translatable("message.bunnidogs.trail.inspect",
                Component.translatable("message.bunnidogs.trail.order."+dog.trail().order().name().toLowerCase(Locale.ROOT)),
                Component.translatable(dog.trail().belongsTo(player) ? "message.bunnidogs.trail.yours" : "message.bunnidogs.trail.other")));
        if (dog.trail().keeper()!=null) careRow(rows,"provisions",Component.translatable(dog.trail().provisions()
                ? "message.bunnidogs.trail.provisions_enabled" : "message.bunnidogs.trail.provisions_disabled"));
        careRow(rows,"home",nest == null ? Component.translatable("message.bunnidogs.care.home_none")
                : Component.translatable("message.bunnidogs.care.home_at",nest.getX(),nest.getY(),nest.getZ(),
                        String.format(Locale.ROOT,"%.1f",Math.sqrt(dog.blockPosition().distSqr(nest)))));
        careRow(rows,"food",dog.diet() == Diet.PLAIN ? Component.translatable("message.bunnidogs.care.food_guavas")
                : Component.translatable("message.bunnidogs.care.food_other",TextComponents.diet(dog.diet())));
        careRow(rows,"camouflage",Component.translatable("message.bunnidogs.camouflage."+dog.camouflageStatus().name().toLowerCase(Locale.ROOT),
                TextComponents.t(com.bunnidogs.mod.genetics.Camouflage.cover(dog.genome()).ground())));
        careRow(rows,"packs",Component.translatable(dog.carryingSlowdown() > 0 ? "message.bunnidogs.packs.overloaded" : "message.bunnidogs.packs.stock",
                dog.carriedBundleCount(),dog.bundleCapacity(),dog.carryingSlowdown()));
        var meals = dog.troughFeedingStatus();
        Component foodStatus = meals == Foraging.Status.RESTING ? recovery(dog.forageWait())
                : Component.translatable("message.bunnidogs.foraging." + meals.name().toLowerCase(Locale.ROOT));
        if (dog.forageWait() > 0 && meals != Foraging.Status.RESTING)
            foodStatus = Component.translatable("message.bunnidogs.care.status_recovery",foodStatus,recovery(dog.forageWait()));
        careRow(rows,"foraging",foodStatus);
        boolean healthy = Sampling.healthy(dog.getHealth(), dog.getMaxHealth());
        Component health = Component.translatable("message.bunnidogs.care.health");
        Component adult = Component.translatable("message.bunnidogs.care.adult");
        careRow(rows, "sampling", !healthy ? health : recovery(dog.samplingWait()));
        careRow(rows, "fleece", dog.isBaby() ? adult
                : !dog.genome().expressed(Gene.BUILD).name().equals("woolly") ? Component.translatable("message.bunnidogs.care.woolly")
                : !healthy ? health : dog.fleeceWait() > 0 ? recovery(dog.fleeceWait())
                : Component.translatable("message.bunnidogs.care.fleece_ready",ComponentUtils.formatList(dog.fleeceProducts(),
                        product -> Component.translatable("message.bunnidogs.care.fleece_product",product.getCount(),product.getHoverName()))));
        var products = BunnidogCare.groomingProducts(dog);
        careRow(rows, "grooming", dog.isBaby() ? adult
                : products.isEmpty() ? Component.translatable("message.bunnidogs.groom.traits",Grooming.MIN_SENSES,Grooming.MIN_STARLIT_BLOOM,Grooming.MIN_MOSS_BLOOM,Grooming.MIN_FROST_BLOOM)
                : !healthy ? health : dog.groomingWait() > 0 ? recovery(dog.groomingWait())
                : Component.translatable("message.bunnidogs.care.groom_ready",
                        ComponentUtils.formatList(products,p -> Component.translatable(p.translationKey()))));
        careRow(rows, "splicing", dog.isBaby() ? adult : !healthy ? health
                : dog.splicingWait() > 0 ? recovery(dog.splicingWait()) : Component.translatable("message.bunnidogs.care.ready"));
        careRow(rows,"guarding",Component.translatable("message.bunnidogs.guarding."+dog.guardingStatus().name().toLowerCase(Locale.ROOT)));
        var scouting = dog.scoutingStatus();
        careRow(rows,"scouting",scouting == com.bunnidogs.mod.genetics.Scouting.Status.RESTING ? recovery(dog.scoutingWait())
                : Component.translatable("message.bunnidogs.scouting."+scouting.name().toLowerCase(Locale.ROOT)));
        if (dog.genome().expressed(com.bunnidogs.mod.genetics.Gene.COAT).name().equals("pale")) {
            var sight=dog.caveSightStatus(player);
            careRow(rows,"cave_sight",sight==com.bunnidogs.mod.genetics.CaveSight.Status.RESTING ? recovery(dog.caveSightWait())
                    : Component.translatable("message.bunnidogs.cave_sight."+sight.name().toLowerCase(Locale.ROOT)));
        }
        var rest=dog.recoveryRestStatus();
        careRow(rows,"recovery_rest",rest==RecoveryRest.Status.HEALTH ? t("not_needed") : Component.translatable("message.bunnidogs.recovery_rest."+rest.name().toLowerCase(Locale.ROOT)));
        if (com.bunnidogs.mod.genetics.Adaptations.phasing(dog.genome())) careRow(rows,"phasing",dog.isBaby() ? adult
                : dog.beingTransported() || dog.isVehicle() ? Component.translatable("message.bunnidogs.phasing.transported")
                : dog.phaseWait() > 0 ? recovery(dog.phaseWait()) : Component.translatable("message.bunnidogs.phasing.ready"));
        return rows;
    }
    private static void workRows(Bunnidog dog,List<Component> rows) {
        var work=dog.gardeningStatus();
        careRowLine(rows,"gardening",work==Gardening.Status.RESTING ? recovery(dog.gardeningWait())
                : Component.translatable("message.bunnidogs.gardening."+work.name().toLowerCase(Locale.ROOT)));
        for (var routine:Learning.Routine.values()) rows.add(t("practice",TextComponents.t(routine.task().displayName()),LearningPanel.status(dog,routine)));
        requirement(rows,dog,Component.translatable("work.bunnidogs.mode.garden"),Stat.BLOOM,Gardening.MIN_CULTIVATION_BLOOM);
        requirement(rows,dog,Component.translatable("work.bunnidogs.mode.nursery"),Stat.BLOOM,Gardening.MIN_NURSERY_BLOOM);
        requirement(rows,dog,Component.translatable("work.bunnidogs.mode.gather"),Stat.SENSES,Scavenging.MIN_GATHERING_SENSES);
        requirement(rows,dog,Component.translatable("work.bunnidogs.mode.supply"),Stat.SENSES,Supplying.MIN_SENSES);
        if (dog.canEat(new ItemStack(Items.CHORUS_FRUIT))) requirement(rows,dog,Component.translatable("message.bunnidogs.care.chorus"),Stat.BLOOM,Gardening.MIN_CHORUS_BLOOM);
        rows.add(t("reserve",Gardening.COMPOST_RESERVE));
    }
    private static void requirement(List<Component> rows,Bunnidog dog,Component job,Stat stat,int minimum) {
        rows.add(t("requirement",job,TextComponents.stat(stat),String.format(Locale.ROOT,"%+d",dog.stats().get(stat)),String.format(Locale.ROOT,"%+d",minimum)));
    }
    private static void careRowLine(List<Component> rows,String activity,Component status) {
        rows.add(Component.translatable("message.bunnidogs.care."+activity,status));
    }
    private static Component recovery(int ticks) {
        if (ticks == 0) return Component.translatable("message.bunnidogs.care.ready");
        int seconds = (ticks + 19) / 20;
        return Component.translatable("message.bunnidogs.care.wait", String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60));
    }

    private static void careRow(List<Reading> rows,String activity,Component status) {
        int group=switch (activity) {
            case "sampling","splicing","recovery_rest" -> 0;
            case "home","provisions","packs","food","foraging" -> 1;
            case "combat","trail" -> 4;
            default -> 3;
        };
        rows.add(new Reading(group,Component.translatable("message.bunnidogs.care."+activity,status)));
    }
}
