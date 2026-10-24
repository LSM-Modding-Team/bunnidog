package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.block.StationUse;
import com.bunnidogs.mod.genetics.Commissions;
import com.bunnidogs.mod.entity.ShowJudging;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.genetics.Show;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.item.TablePlanning;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Concise workshop controls; the existing sheets remain the detailed breeding forecast. */
public final class WorkshopPanels {
    private WorkshopPanels() {}
    public static final int READ_TABLE=110, ADD_SAMPLE=111, TAKE_A=112, TAKE_B=113, AWARD=114;
    public static final int GOALS=150, BACK_TABLE=151, GOAL=160, CLEAR_GOAL=169;
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    private static CareView.Control button(int id,String key,boolean enabled,boolean detail) {
        return new CareView.Control(id,t("button."+key),enabled,detail ? CareView.Control.DETAIL : CareView.Control.PRIMARY);
    }
    public static ItemStack nextSample(Player player) {
        for (int i=0;i<player.getInventory().getContainerSize();i++) {
            var item=player.getInventory().getItem(i); if (TissueSampleItem.read(item).isPresent()) return item;
        }
        return ItemStack.EMPTY;
    }
    public static CareView table(Player player,BlockPos pos,GeneticsTableBlockEntity table) {
        var rows=new ArrayList<Component>();
        var litter=LitterPanel.nextSeed(player);
        boolean a=TissueSampleItem.read(table.item(0)).isPresent(), b=TissueSampleItem.read(table.item(1)).isPresent();
        var stock=new ArrayList<CareView.Stock>();
        stock.add(new CareView.Stock(t("display.sample_a"),table.item(0)));
        stock.add(new CareView.Stock(t("display.sample_b"),table.item(1)));
        if (!table.goalKey().isEmpty()) rows.add(table.goal().map(goal->t("table.goal",Component.translatable("commission.bunnidogs."+goal.key()+".title")))
                .orElseGet(()->t("table.goal_unknown")));
        var controls=new ArrayList<CareView.Control>();
        if (a || b) controls.add(button(READ_TABLE,"read_table",true,false));
        controls.add(button(GOALS,"goals",true,false));
        if (FamilyPanels.hasCalipers(player)) controls.add(button(FamilyPanels.FAMILY,"family",a && b && table.goal().isPresent(),false));
        if (!litter.isEmpty()) controls.add(button(LitterPanel.LITTER,"litter",true,false));
        return new CareView(ModBlocks.GENETICS_TABLE.get().getName(),Component.empty(),rows,controls).withStock(stock.toArray(CareView.Stock[]::new));
    }
    public static CareView goals(Player player,BlockPos pos,GeneticsTableBlockEntity table) {
        var controls=new ArrayList<CareView.Control>(); boolean edit=StationUse.editable(player,table);
        for (var goal:Commissions.Request.values()) controls.add(new CareView.Control(GOAL+goal.ordinal(),
                Component.translatable("commission.bunnidogs."+goal.key()+".title"),edit && !table.goal().filter(goal::equals).isPresent()));
        controls.add(button(CLEAR_GOAL,"clear_goal",edit && !table.goalKey().isEmpty(),false)); controls.add(button(BACK_TABLE,"back_table",true,false));
        return new CareView(t("table.goals.title"),Component.empty(),List.of(),controls);
    }
    public static CareView show(Player player,BlockPos pos) {
        var reading=ShowJudging.inspect(player.level(),pos,player); var rows=new ArrayList<Component>();
        if (reading.candidate().isPresent()) {
            var dog=reading.candidate().orElseThrow();
            rows.add(t("show.entrant",dog.getDisplayName()));
            rows.add(t("show.tier",Component.translatable("show.bunnidogs.tier."+reading.tier().key())));
            rows.add(t("show.parents",t(dog.motherGenome().isPresent() ? "show.recorded" : "show.missing")));
            rows.add(t("show.fixed",Purebreds.fixedCount(dog.genome()),Gene.COUNT));
            if (Purebreds.fixedCount(dog.genome())<Gene.COUNT) rows.add(t("show.mixed",net.minecraft.network.chat.ComponentUtils.formatList(
                    java.util.Arrays.stream(Gene.values()).filter(gene->!dog.genome().isHomozygous(gene)).toList(),gene->TextComponents.gene(gene))));
            Show.Tier target=reading.tier()==Show.Tier.NONE ? Show.Tier.BRONZE : reading.status()==ShowJudging.Status.ALREADY && reading.tier()!=Show.Tier.GOLD
                    ? Show.Tier.values()[reading.tier().ordinal()+1] : reading.tier();
            rows.add(t("show.target",Component.translatable("show.bunnidogs.tier."+target.key())));
            for (Stat stat:Stat.values()) rows.add(t("show.stat",TextComponents.stat(stat),String.format(Locale.ROOT,"%+d",dog.stats().get(stat)),target.minimum())
                    .copy().withStyle(dog.stats().get(stat)>=target.minimum() ? ChatFormatting.GREEN : ChatFormatting.RED));

        } else { rows.add(t("show.lead")); }
        String next=reading.status()==ShowJudging.Status.ALREADY && reading.tier()==Show.Tier.GOLD ? "complete"
                : reading.status().name().toLowerCase(java.util.Locale.ROOT);
        return new CareView(ModBlocks.JUDGING_STAND.get().getName(),t("show.next."+next),rows,
                List.of(button(AWARD,"award",player.mayBuild() && player.level().mayInteract(player,pos) && reading.status()==ShowJudging.Status.READY,false)));
    }
    public static void act(Player player,BlockPos pos,GeneticsTableBlockEntity table,int id) {
        if (id==READ_TABLE && player instanceof ServerPlayer server) {
            var sheet=TablePlanning.snapshot(table,player);
            if (sheet.spreadCount()>0) { player.closeContainer(); ModNetwork.sendTable(server,sheet); }
        } else if (id==CLEAR_GOAL) table.setGoalFromPlayer(player,null);
        else if (id>=GOAL && id<GOAL+Commissions.Request.values().length) table.setGoalFromPlayer(player,Commissions.Request.values()[id-GOAL]);
    }
}
