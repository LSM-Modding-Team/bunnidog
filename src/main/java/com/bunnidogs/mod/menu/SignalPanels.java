package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import java.util.List;
import com.bunnidogs.mod.block.*;
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
    public static CareView jar(Player player,BlockPos pos,SpecimenJarBlockEntity jar) {
        var rows=new ArrayList<Component>(); var sample=jar.sample(); var tissue=TissueSampleItem.read(sample);
        tissue.ifPresent(value->rows.add(TextComponents.look(value.genome())));
        if (!sample.isEmpty()) rows.add(t("jar.signal",jar.fixedSignal()));
        var view=new CareView(ModBlocks.SPECIMEN_JAR.get().getName(),!sample.isEmpty() && tissue.isEmpty() ? t("jar.next.unreadable") : Component.empty(),rows,List.of(
                button(JAR_READ,"jar_read",tissue.isPresent())));
        return view.withStock(new CareView.Stock(t("display.sample"),sample));
    }
    public static CareView detector(Player player,BlockPos pos,PedigreeComparatorBlockEntity detector) {
        var rows=new ArrayList<Component>(); var next=WorkshopPanels.nextSample(player); var reference=detector.reference();
        var reading=detector.read(); boolean analog=detector.getBlockState().getValue(PedigreeComparatorBlock.ANALOG), edit=StationUse.editable(player,detector);
        reference.ifPresent(value->rows.add(t("detector.reference",TextComponents.look(value.genome()))));
        rows.add(Component.translatable("message.bunnidogs.detector."+reading.status().name().toLowerCase(java.util.Locale.ROOT),reading.pairs()));
        rows.add(t("detector.mode",t(analog ? "detector.analog" : "detector.exact"),detector.getBlockState().getValue(PedigreeComparatorBlock.POWER)));
        return new CareView(ModBlocks.PEDIGREE_COMPARATOR.get().getName(),Component.empty(),rows,List.of(
                new CareView.Control(COPY,t("button.copy_reference"),edit && !next.isEmpty(),CareView.Control.PRIMARY,t("station.copy_keeps_sample")),
                new CareView.Control(EXACT,t("button.exact"),edit && analog,CareView.Control.PRIMARY,t("station.exact")),
                new CareView.Control(ANALOG,t("button.analog"),edit && !analog,CareView.Control.PRIMARY,t("station.analog")),
                button(READ_REFERENCE,"read_reference",reference.isPresent()),button(CLEAR,"clear_reference",edit && reference.isPresent()),
                button(com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD,"show_front_pad",true)))
                .withStock(new CareView.Stock(t("display.next_sample"),next));
    }
    public static CareView trophy(Player player,BlockPos pos,TrophyPedestalBlockEntity pedestal) {
        var item=pedestal.item(); var rows=new ArrayList<Component>();
        var ribbon=ShowRibbonItem.read(item); var sample=TissueSampleItem.read(item);
        ribbon.ifPresent(value->rows.add(t("trophy.winner",value.tag().orElse(""),TextComponents.look(value.genome()))));
        sample.ifPresent(value->rows.add(TextComponents.look(value.genome())));
        return new CareView(ModBlocks.TROPHY_PEDESTAL.get().getName(),Component.empty(),rows,List.of(
                button(TROPHY_READ,"read_trophy",ribbon.isPresent() || sample.isPresent())))
                .withStock(new CareView.Stock(t("display.trophy"),item));
    }
    public static boolean readOnly(int action) { return action==READ_REFERENCE || action==JAR_READ || action==TROPHY_READ; }
    public static void act(Player player,BlockEntity target,int action) {
        if (target instanceof SpecimenJarBlockEntity jar) {
            if (action==JAR_READ && player instanceof ServerPlayer server) TissueSampleItem.read(jar.sample()).ifPresent(sample->{player.closeContainer(); ModNetwork.sendSample(server,sample);});
        } else if (target instanceof PedigreeComparatorBlockEntity detector) {
            if (action==COPY) detector.copyFromPlayer(player,WorkshopPanels.nextSample(player));
            else if (action==CLEAR) detector.clearFromPlayer(player);
            else if (action==EXACT || action==ANALOG) detector.modeFromPlayer(player,action==ANALOG);
            else if (action==READ_REFERENCE && player instanceof ServerPlayer server) detector.reference().ifPresent(sample->{player.closeContainer(); ModNetwork.sendSample(server,sample);});
        } else if (target instanceof TrophyPedestalBlockEntity pedestal) {
            if (action==TROPHY_READ && player instanceof ServerPlayer server) {
                var ribbon=ShowRibbonItem.read(pedestal.item()); var sample=TissueSampleItem.read(pedestal.item());
                if (ribbon.isPresent()) { player.closeContainer(); ModNetwork.sendSheet(server,ribbon.orElseThrow()); }
                else if (sample.isPresent()) { player.closeContainer(); ModNetwork.sendSample(server,sample.orElseThrow()); }
            }
        }
    }
}
