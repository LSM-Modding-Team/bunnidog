package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.entity.Bunnidog;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Short, live companion orders. This is a deliberate opt-in, separate from training. */
public final class CombatPanel {
    private CombatPanel() {}
    public static final int OPEN=91, ASSIST=92, SAFE=93, RECALL=94, STAND_DOWN=95, COMMAND=96;
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs.combat."+key,args); }
    private static CareView.Control button(int id,Component text,boolean enabled) { return new CareView.Control(id,text,enabled); }
    public static CareView view(Player player,Bunnidog dog) {
        var rows=new ArrayList<Component>(); var buttons=new ArrayList<CareView.Control>();
        rows.add(t("state",t("intent."+dog.combat().intent())));
        if (dog.combat().recovering()) rows.add(t("recovery",(dog.combat().recoveryLeft()+19)/20));
        rows.add(Component.translatable("care.bunnidogs.party.count",dog.combat().partyCount()));
        if (dog.combat().selected()) rows.add(t(dog.combat().commanded() ? "commanded" : "other_command"));
        boolean own=player.mayBuild() && dog.trail().belongsTo(player);
        if (dog.combat().downed()) {
            boolean guava=false;
            for (int i=0;i<player.getInventory().getContainerSize();i++) if (player.getInventory().getItem(i).is(com.bunnidogs.mod.registry.ModItems.GUAVA.get())) { guava=true; break; }
            buttons.add(button(CarePanels.CARE,Component.translatable("care.bunnidogs.button.first_aid"),guava && dog.combat().canFirstAid(player)));
        } else buttons.add(button(ASSIST,t(dog.combat().selected() ? "assist" : "choose"),own && dog.combat().canSelect(player)
                && (dog.combat().selected() || dog.combat().partyCount()<3)));
        if (dog.combat().selected()) buttons.add(button(SAFE,t("safe"),own));
        buttons.add(button(RECALL,t("recall"),own && !dog.combat().downed() && !dog.beingTransported() && !dog.isVehicle()));
        if (dog.combat().selected()) buttons.add(button(STAND_DOWN,t("stand_down"),own && dog.combat().canStandDown(player)));
        if (dog.combat().selected() && !dog.combat().commanded()) buttons.add(button(COMMAND,t("command"),own));
        if (dog.combat().partyCount()>0) buttons.add(button(PartyPanel.OPEN,Component.translatable("care.bunnidogs.party.title"),true));
        buttons.add(button(CarePanels.BACK,Component.translatable("care.bunnidogs.button.back"),true));
        return new CareView(t("title",dog.getDisplayName()),AnimalPanels.condition(dog),rows,buttons);
    }
    public static boolean act(Player player,Bunnidog dog,int id) {
        return switch (id) {
            case ASSIST -> dog.combat().assist(player);
            case SAFE -> dog.combat().staySafe(player);
            case RECALL -> dog.combat().recall(player);
            case STAND_DOWN -> dog.combat().standDown(player);
            case COMMAND -> dog.combat().commandPartner(player);
            default -> false;
        };
    }
}
