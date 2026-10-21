package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.layout.GoalSheet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Produced seeds keep their own quality and treatment; held feed cues belong only to prospective parent plans. */
public final class LitterPanel {
    private LitterPanel() {}
    public static final int LITTER=210, READ_SEED=211;
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    public static ItemStack nextSeed(Player player) {
        for (int i=0;i<player.getInventory().getContainerSize();i++) {
            var stack=player.getInventory().getItem(i); if (SeedItem.read(stack).isPresent()) return stack;
        }
        return ItemStack.EMPTY;
    }
    public static boolean matches(GeneticsTableBlockEntity table,Seed seed) {
        var a=TissueSampleItem.read(table.item(0)); var b=TissueSampleItem.read(table.item(1));
        var mother=new Tissue(seed.mother(),seed.motherStats()); var father=new Tissue(seed.father(),seed.fatherStats());
        return a.isPresent() && b.isPresent() && (a.orElseThrow().equals(mother) && b.orElseThrow().equals(father)
                || a.orElseThrow().equals(father) && b.orElseThrow().equals(mother));
    }
    public static CareView view(Player player,GeneticsTableBlockEntity table) {
        var item=nextSeed(player); var seed=SeedItem.read(item); var rows=new ArrayList<Component>();
        String next=seed.isEmpty() ? "none" : "ready";
        rows.add(seed.isPresent() ? t("litter.source",item.getHoverName()) : t("litter.none"));
        if (seed.isPresent()) {
            var actual=seed.orElseThrow();
            rows.add(t(matches(table,actual) ? "litter.matches" : "litter.different"));
            rows.add(t("litter.quality",String.format(Locale.ROOT,"%.2f",actual.quality()),GoalSheet.percent(actual.mutationChance())));
            if (actual.mutagen()) rows.add(t("litter.treated"));
            if (table.goal().isPresent()) {
                var goal=table.goal().orElseThrow(); var cross=new Cross(actual); var forecast=new GoalForecast(cross,goal);
                next=forecast.genes()==0 ? "genes" : forecast.points()==0 ? "points" : "ready";
                rows.add(t("board.request",Component.translatable("commission.bunnidogs."+goal.key()+".title")));
                rows.add(t("litter.odds",GoalSheet.percent(forecast.perPup()),GoalSheet.percent(forecast.inLitter())));
                var checks=goal.checks(Genome.DEFAULT,Stats.ZERO,0);
                for (var check:checks) {
                    if (check.stat().isPresent()) {
                        var stat=check.stat().orElseThrow(); double expected=cross.expectedPoints(stat);
                        rows.add(t("litter.points",TextComponents.stat(stat),String.format(Locale.ROOT,"%+.1f",expected),check.required())
                                .copy().withStyle(expected>=check.required() ? ChatFormatting.GREEN : ChatFormatting.RED));
                    } else if (check.failure()==Commissions.Status.GENES) {
                        rows.add(t("litter.genes",t("check."+check.key(),check.detail().replace('_',' ')),check.required()));
                    }
                }
            } else rows.add(t("family.no_goal"));
            rows.add(t("litter.before"));
        }
        return new CareView(t("litter.title"),t("litter.next."+next),rows,List.of(
                new CareView.Control(WorkshopPanels.BACK_TABLE,t("button.back_table"),true),
                new CareView.Control(FamilyPanels.SEED_FAMILY,t("button.seed_family"),seed.isPresent() && table.goal().isPresent() && FamilyPanels.hasCalipers(player)),
                new CareView.Control(READ_SEED,t("button.read_seed"),seed.isPresent())));
    }
}
