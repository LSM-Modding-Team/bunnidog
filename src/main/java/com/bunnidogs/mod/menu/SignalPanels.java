package com.bunnidogs.mod.menu;

import java.util.ArrayList;
import java.util.List;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.item.ApexOfferingItem;
import com.bunnidogs.mod.item.ShowRibbonItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Physical samples, copied redstone references and trophy displays keep their distinct jobs. */
public final class SignalPanels {
    private SignalPanels() {}
    public static final int COPY=120, EXACT=121, ANALOG=122, CLEAR=123, READ_REFERENCE=124;
    public static final int JAR_ADD=130, JAR_TAKE=131, JAR_READ=132, TROPHY_TAKE=133, TROPHY_READ=134;
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    private static CareView.Control button(int id,String key,boolean enabled) { return new CareView.Control(id,t("button."+key),enabled); }
    private static Component stock(ItemStack item) { return item.isEmpty() ? t("empty") : t("stock",item.getCount(),item.getHoverName()); }
    private static void nextSample(List<Component> rows,ItemStack next) {
        rows.add(t("table.next_sample",next.getHoverName()));
        rows.add(Component.literal(Plain.look(TissueSampleItem.read(next).orElseThrow().genome())));
    }
    public static CareView jar(Player player,BlockPos pos,SpecimenJarBlockEntity jar) {
        var rows=new ArrayList<Component>(); var sample=jar.sample(); var tissue=TissueSampleItem.read(sample);
        var next=WorkshopPanels.nextSample(player); boolean edit=StationUse.editable(player,jar);
        if (sample.isEmpty() && !next.isEmpty()) nextSample(rows,next);
        rows.add(t("jar.sample",stock(sample)));
        tissue.ifPresent(value->rows.add(Component.literal(Plain.look(value.genome()))));
        rows.add(t("jar.signal",jar.fixedSignal())); rows.add(t("jar.use"));
        return new CareView(ModBlocks.SPECIMEN_JAR.get().getName(),t(!sample.isEmpty() && tissue.isEmpty() ? "jar.next.unreadable"
                : sample.isEmpty() ? "jar.next.sample" : "jar.next.ready"),rows,List.of(
                button(JAR_ADD,"jar_add",edit && sample.isEmpty() && !next.isEmpty()),
                button(JAR_READ,"jar_read",tissue.isPresent()),button(JAR_TAKE,"jar_take",edit && !sample.isEmpty())));
    }
    public static CareView detector(Player player,BlockPos pos,PedigreeComparatorBlockEntity detector) {
        var rows=new ArrayList<Component>(); var next=WorkshopPanels.nextSample(player); var reference=detector.reference();
        var reading=detector.read(); boolean analog=detector.getBlockState().getValue(PedigreeComparatorBlock.ANALOG), edit=StationUse.editable(player,detector);
        if (!next.isEmpty()) nextSample(rows,next);
        rows.add(t("detector.reference",reference.map(r->Component.literal(Plain.look(r.genome()))).orElseGet(()->Component.literal("—"))));
        rows.add(Component.translatable("message.bunnidogs.detector."+reading.status().name().toLowerCase(java.util.Locale.ROOT),reading.pairs()));
        rows.add(t("detector.mode",t(analog ? "detector.analog" : "detector.exact"),detector.getBlockState().getValue(PedigreeComparatorBlock.POWER)));
        var front=pos.relative(detector.getBlockState().getValue(PedigreeComparatorBlock.FACING));
        rows.add(t("detector.pad",front.getX(),front.getY(),front.getZ()));
        rows.add(t("detector.copies")); reference.ifPresent(r->rows.add(t("show.fixed",Purebreds.fixedCount(r.genome()),com.bunnidogs.mod.genetics.Gene.COUNT)));
        return new CareView(ModBlocks.PEDIGREE_COMPARATOR.get().getName(),t("detector.next."+reading.status().name().toLowerCase(java.util.Locale.ROOT)),rows,List.of(
                button(COPY,"copy_reference",edit && !next.isEmpty()),button(EXACT,"exact",edit && analog),button(ANALOG,"analog",edit && !analog),
                button(READ_REFERENCE,"read_reference",reference.isPresent()),button(CLEAR,"clear_reference",edit && reference.isPresent()),
                button(com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD,"show_front_pad",true)));
    }
    public static CareView trophy(Player player,BlockPos pos,TrophyPedestalBlockEntity pedestal) {
        var item=pedestal.item(); var rows=new ArrayList<Component>(); rows.add(t("trophy.stock",stock(item)));
        var ribbon=ShowRibbonItem.read(item); var sample=TissueSampleItem.read(item);
        ribbon.ifPresent(value->rows.add(t("trophy.winner",value.tag().orElse(""),Plain.look(value.genome()))));
        sample.ifPresent(value->rows.add(Component.literal(Plain.look(value.genome()))));
        if (item.getItem() instanceof ApexOfferingItem) rows.add(t("trophy.offering"));
        rows.add(t("trophy.use"));
        return new CareView(ModBlocks.TROPHY_PEDESTAL.get().getName(),t(item.isEmpty() ? "trophy.next.place" : "trophy.next.ready"),rows,List.of(
                button(TROPHY_TAKE,"take_trophy",StationUse.editable(player,pedestal) && !item.isEmpty()),
                button(TROPHY_READ,"read_trophy",ribbon.isPresent() || sample.isPresent())));
    }
    public static boolean readOnly(int action) { return action==READ_REFERENCE || action==JAR_READ || action==TROPHY_READ; }
    public static void act(Player player,BlockEntity target,int action) {
        if (target instanceof SpecimenJarBlockEntity jar) {
            if (action==JAR_ADD) jar.insertFromHand(player,WorkshopPanels.nextSample(player));
            else if (action==JAR_TAKE) jar.returnToPlayer(player);
            else if (action==JAR_READ && player instanceof ServerPlayer server) TissueSampleItem.read(jar.sample()).ifPresent(sample->{player.closeContainer(); ModNetwork.sendSample(server,sample);});
        } else if (target instanceof PedigreeComparatorBlockEntity detector) {
            if (action==COPY) detector.copyFromPlayer(player,WorkshopPanels.nextSample(player));
            else if (action==CLEAR) detector.clearFromPlayer(player);
            else if (action==EXACT || action==ANALOG) detector.modeFromPlayer(player,action==ANALOG);
            else if (action==READ_REFERENCE && player instanceof ServerPlayer server) detector.reference().ifPresent(sample->{player.closeContainer(); ModNetwork.sendSample(server,sample);});
        } else if (target instanceof TrophyPedestalBlockEntity pedestal) {
            if (action==TROPHY_TAKE) pedestal.returnToPlayer(player);
            else if (action==TROPHY_READ && player instanceof ServerPlayer server) {
                var ribbon=ShowRibbonItem.read(pedestal.item()); var sample=TissueSampleItem.read(pedestal.item());
                if (ribbon.isPresent()) { player.closeContainer(); ModNetwork.sendSheet(server,ribbon.orElseThrow()); }
                else if (sample.isPresent()) { player.closeContainer(); ModNetwork.sendSample(server,sample.orElseThrow()); }
            }
        }
    }
}
