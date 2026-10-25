package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.bunnidogs.mod.block.StationUse;
import com.bunnidogs.mod.entity.ApexRitual;
import com.bunnidogs.mod.genetics.ApexRules;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Units;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A visible forecast and cost review before deliberately starting the optional encounter. */
public final class ApexPanel {
    private ApexPanel() {}
    public static final int PREPARE=140, SUMMON=141, CANCEL=142, SHOW_RING=143;
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    private static String number(double value) { return String.format(Locale.ROOT,"%.1f",value); }
    private static CareView.Control button(int id,String label,boolean enabled) { return new CareView.Control(id,t("button."+label),enabled); }
    public static ItemStack sample(Player player,ItemStack preferred) {
        return StationUse.owns(player,preferred) && TissueSampleItem.read(preferred).isPresent() ? preferred : WorkshopPanels.nextSample(player);
    }
    public static CareView view(Player player,BlockPos pos,ItemStack sample,boolean armed) {
        return view(player,pos,sample,ItemStack.EMPTY,armed);
    }
    public static CareView view(Player player,BlockPos pos,ItemStack sample,ItemStack sigil,boolean armed) {
        var focus=sigil.getItem() instanceof com.bunnidogs.mod.item.ApexSigilItem item ? item : null;
        var reading=ApexRitual.inspect(player,pos,sample); var rows=new ArrayList<Component>();
        TissueSampleItem.read(sample).ifPresent(source->{
            rows.add(t("apex.source",sample.getHoverName())); rows.add(t("apex.kind",Component.translatable("entity.bunnidogs."+(focus==null ? "apex" : (focus.flying ? "flying_" : "")+focus.kind.id))));
            var plan=ApexRules.forecast(source);
            rows.add(Component.translatable("message.bunnidogs.apex.forecast_combat",number(plan.normal().health()*(focus!=null && focus.kind==com.bunnidogs.mod.entity.ApexKind.BLAST ? 1.2 : 1)),number(plan.normal().damage()*(focus==null || focus.kind==com.bunnidogs.mod.entity.ApexKind.BRUTE ? focus!=null && focus.flying ? 2 : 1.3 : 1)),number(plan.enraged().damage()*(focus==null || focus.kind==com.bunnidogs.mod.entity.ApexKind.BRUTE ? focus!=null && focus.flying ? 2 : 1.3 : 1))));
            rows.add(Component.translatable("message.bunnidogs.apex.forecast_defense",number(plan.armor()),Math.round(plan.knockbackResistance()*100),number(plan.normal().awareness())));
            rows.add(Component.translatable("message.bunnidogs.apex.forecast_motion",Math.round((plan.enraged().speed()/plan.normal().speed()-1)*100),number(Units.jumpHeight(plan.normal().jump()))));
        });
        for (var legend:ApexRitual.legends()) rows.add(t("apex.offering",new ItemStack(legend).getHoverName(),t(reading.missing().contains(legend) ? "apex.missing" : "apex.ready"))
                .copy().withStyle(reading.missing().contains(legend) ? ChatFormatting.RED : ChatFormatting.GREEN));
        rows.add(t(focus==null ? "apex.cost" : "apex.focus_cost"));
        boolean ready=(sigil.isEmpty() || StationUse.owns(player,sigil)) && StationUse.editable(player,pos) && reading.status()==ApexRitual.Status.READY;
        var controls=new ArrayList<CareView.Control>();
        if (armed) { controls.add(button(SUMMON,"summon",ready)); controls.add(button(CANCEL,"cancel_ritual",true)); }
        else controls.add(button(PREPARE,"prepare_ritual",ready));
        controls.add(new CareView.Control(SHOW_RING,t("button.show_ring"),true,CareView.Control.DETAIL));
        return new CareView(ModBlocks.APEX_CRUCIBLE.get().getName(),armed ? t("apex.confirm") : reading.status()==ApexRitual.Status.READY ? Component.empty() : t("apex.next."+reading.status().name().toLowerCase(Locale.ROOT)),rows,controls);
    }
}
