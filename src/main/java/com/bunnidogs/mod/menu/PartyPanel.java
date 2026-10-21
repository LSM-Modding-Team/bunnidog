package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.entity.CompanionParty;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Three members, a single directed-order recipient and explicit quiet-time removal. */
public final class PartyPanel {
    private PartyPanel() {}
    public static final int OPEN=98, COMMAND=100, REMOVE=103;
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs.party."+key,args); }
    public static CareView view(Player player) {
        var rows=new ArrayList<Component>(); var controls=new ArrayList<CareView.Control>();
        var members=CompanionParty.members(player);
        rows.add(t("count",members.size())); rows.add(t("focus_hint"));
        for (int i=0;i<members.size();i++) {
            var member=members.get(i);
            rows.add(t(member.commanded() ? "chosen_row" : "row",i+1,member.name())); rows.add(member.detail());
            controls.add(new CareView.Control(COMMAND+i,t("command",i+1),player.mayBuild() && !member.commanded()));
            controls.add(new CareView.Control(REMOVE+i,t("remove",i+1),CompanionParty.canChange(player)));
        }
        if (members.isEmpty()) rows.add(t("empty"));
        return new CareView(t("title"),t(CompanionParty.canChange(player) ? "next" : "busy"),rows,controls);
    }
    public static void act(Player player,int action) {
        int index=action>=REMOVE ? action-REMOVE : action-COMMAND;
        var members=CompanionParty.members(player);
        if (index<0 || index>=members.size()) return;
        if (action>=REMOVE && action<REMOVE+3) CompanionParty.remove(player,members.get(index).id());
        else if (action>=COMMAND && action<COMMAND+3) CompanionParty.command(player,members.get(index).id());
    }
}
