package com.bunnidogs.mod.menu;

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
        var reading=ApexRitual.inspect(player,pos,sample); var rows=new ArrayList<Component>();
        TissueSampleItem.read(sample).ifPresent(source->{
            rows.add(t("apex.source",sample.getHoverName())); rows.add(Component.literal(Plain.look(source.genome())));
            var plan=ApexRules.forecast(source);
            rows.add(Component.translatable("message.bunnidogs.apex.forecast_combat",number(plan.normal().health()),number(plan.normal().damage()),number(plan.enraged().damage())));
            rows.add(Component.translatable("message.bunnidogs.apex.forecast_defense",number(plan.armor()),Math.round(plan.knockbackResistance()*100),number(plan.normal().awareness())));
            rows.add(Component.translatable("message.bunnidogs.apex.forecast_motion",Math.round((plan.enraged().speed()/plan.normal().speed()-1)*100),number(Units.jumpHeight(plan.normal().jump()))));
        });
        for (var legend:ApexRitual.legends()) rows.add(t("apex.offering",new ItemStack(legend).getHoverName(),t(reading.missing().contains(legend) ? "apex.missing" : "apex.ready"))
                .copy().withStyle(reading.missing().contains(legend) ? ChatFormatting.RED : ChatFormatting.GREEN));
        rows.add(t("apex.ring")); rows.add(t("apex.cost"));
        boolean ready=StationUse.editable(player,pos) && reading.status()==ApexRitual.Status.READY;
        var controls=new ArrayList<CareView.Control>();
        if (armed) { controls.add(button(SUMMON,"summon",ready)); controls.add(button(CANCEL,"cancel_ritual",true)); }
        else controls.add(button(PREPARE,"prepare_ritual",ready));
        controls.add(new CareView.Control(SHOW_RING,t("button.show_ring"),true,CareView.Control.DETAIL));
        return new CareView(ModBlocks.APEX_CRUCIBLE.get().getName(),t(armed ? "apex.confirm" : "apex.next."+reading.status().name().toLowerCase(Locale.ROOT)),rows,controls);
    }
}
