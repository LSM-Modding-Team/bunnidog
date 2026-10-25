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
    public static final int WATCH=236,STOP_WATCH=237,SHOW_ROUTE=238,PAUSE_ROUTE=239,CLEAR_ROUTE=240,UNLOAD_ROUTE=241;
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
        var delivery=dog.deliveries();
        if(delivery.watching())controls.add(new CareView.Control(STOP_WATCH,Component.translatable("care.bunnidogs.delivery.stop"),delivery.authorized(player)));
        else controls.add(new CareView.Control(WATCH,Component.translatable("care.bunnidogs.delivery.watch"),delivery.canObserve(player),CareView.Control.PRIMARY,deliveryReason(player,dog)));
        if(delivery.route()!=null) {
            rows.add(Component.translatable("care.bunnidogs.delivery.parcel",delivery.route().filter().getHoverName()));
            rows.add(Component.translatable("care.bunnidogs.delivery.state."+delivery.state().name().toLowerCase(java.util.Locale.ROOT)));
            controls.add(new CareView.Control(SHOW_ROUTE,Component.translatable("care.bunnidogs.delivery.show"),delivery.authorized(player),CareView.Control.DETAIL));
            controls.add(new CareView.Control(PAUSE_ROUTE,Component.translatable(delivery.paused() ? "care.bunnidogs.delivery.resume" : "care.bunnidogs.delivery.pause"),delivery.authorized(player),CareView.Control.DETAIL));
            controls.add(new CareView.Control(CLEAR_ROUTE,Component.translatable("care.bunnidogs.delivery.clear"),delivery.authorized(player) && delivery.cargo().isEmpty(),CareView.Control.DETAIL));
        }
        if(!delivery.cargo().isEmpty())controls.add(new CareView.Control(UNLOAD_ROUTE,Component.translatable("care.bunnidogs.delivery.unload"),delivery.authorized(player)));
        var lesson = dog.learning().lesson();
        if (lesson!=null) controls.add(new CareView.Control(STOP, TextComponents.t("Stop lesson"), dog.learning().authorized(player)));
        controls.add(new CareView.Control(CarePanels.BACK, Component.translatable("care.bunnidogs.button.back"), true));
        Component next=delivery.watching() ? Component.translatable("care.bunnidogs.delivery.watching") : lesson==null ? blocker(dog) : activity(dog);
        return new CareView(TextComponents.t("Learning: %s", dog.getDisplayName()), next, rows, controls);
    }
    private static Component deliveryReason(Player p,Bunnidog dog) {
        if(dog.deliveries().canObserve(p))return Component.empty();
        if(!dog.deliveries().authorized(p))return AnimalPanels.text("keeper_only");
        String key=dog.isBaby() ? "young" : !dog.deliveries().cargo().isEmpty() ? "parcel" : !Supplying.qualified(dog.stats()) ? "senses" : CapabilityRules.potential(dog.mind(),CapabilityRules.Task.DELIVERY)==CapabilityRules.Potential.LIMITED ? "complex" : "ready";
        return Component.translatable("care.bunnidogs.delivery.reason."+key);
    }
    public static Component activity(Bunnidog dog) {
        var lesson=dog.learning().lesson();
        if (lesson==null) return blocker(dog);
        var status=dog.gardeningStatus();
        if (status!=Gardening.Status.READY && status!=Gardening.Status.RESTING) return blocker(dog);
        return AnimalPanels.text("practice."+dog.learning().activity().name().toLowerCase(java.util.Locale.ROOT),name(lesson));
    }
    /** Immediate work constraints, never inferred genes, numerical thresholds or a setup tutorial. */
    public static Component blocker(Bunnidog dog) {
        return switch (dog.gardeningStatus()) {
            case READY -> Component.empty();
            case RESTING -> AnimalPanels.text("resting");
            case ADULT -> AnimalPanels.text("still_growing");
            case HOME -> AnimalPanels.text("need_home");
            case FULL_HEALTH -> AnimalPanels.text("injured");
            case PAUSED -> AnimalPanels.text("work_paused");
            case TRAIL -> AnimalPanels.text("following_order");
            case DAYTIME -> AnimalPanels.text("night_rest");
            case OUTSIDE_HOME -> AnimalPanels.text("away_home");
            case GRIEFING_OFF -> AnimalPanels.text("world_work_off");
            case TRANSPORTED -> AnimalPanels.text("transported");
            case DANGER -> AnimalPanels.text("alarmed");
            case UNAVAILABLE -> AnimalPanels.condition(dog);
        };
    }
    private static Component lessonReason(Player player,Bunnidog dog,Learning.Routine routine) {
        if (dog.learning().canBegin(player,routine) && dog.learning().lesson()!=routine) return Component.empty();
        if (!player.mayBuild()) return AnimalPanels.text("read_only");
        if (!dog.learning().authorized(player)) return AnimalPanels.text("keeper_only");
        if (dog.learning().lesson()==routine) return activity(dog);
        if (dog.learning().knowledge().independent(dog.mind(),routine)) return AnimalPanels.text("already_learned");
        if (!player.hasLineOfSight(dog)) return AnimalPanels.text("guide_out_of_sight");
        if (dog.gardeningStatus()==Gardening.Status.RESTING) return AnimalPanels.text("lesson_unavailable");
        var blocker=blocker(dog);
        return blocker.getString().isEmpty() ? AnimalPanels.text("lesson_unavailable") : blocker;
    }
}
