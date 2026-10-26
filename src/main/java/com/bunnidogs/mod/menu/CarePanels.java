package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.network.TextComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.NurseryHatching;
import com.bunnidogs.mod.entity.PenBreeding;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Short live advice and explicit controls for the existing animal-care loop. */
public final class CarePanels {
    private CarePanels() {}
    public static final int GENETICS=1, WORK=2, FOLLOW=3, WAIT=4, RESIDENT=5, PROVISIONS=6, TAKE_BUNDLE=7, CARE=8, COURTSHIP=9;
    public static final int TENDING=10, TAKE_FOOD=11, CLEAR_FILTER=12, MODE=20;
    public static final int ADD_FOOD=13, TRAIN=14, RELEASE=15, GROOM=16;
    public static final int CARGO=17, BACK=18;
    public static final int TAKE_SUPPLIES=243;
    public static final int ROLES=19;
    public static final int PAIR=30, TAKE_LEFT=31, TAKE_RIGHT=32, ADD_LEFT=33, ADD_RIGHT=34;
    public static final int HATCH=40, TAKE_SEED=41, AUTO_HATCH=42, ADD_SEED=43, GROW=44;
    public static final int TAKE_JUICE=50, TAKE_FRUIT=51, TAKE_CARRIER=52, ADD_FRUIT=53, ADD_BOTTLE=54;
    public static final int REHOME=60, SUBMIT=70, REQUEST=80;
    public static final int WORK_MODES=228, REQUESTS=229, CONFIRM_RELEASE=230;

    private static net.minecraft.network.chat.MutableComponent t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    private static Component message(String key,Object... args) { return Component.translatable("message.bunnidogs."+key,args); }
    private static CareView.Control control(int id,String key,boolean enabled) {
        boolean detail=id==GENETICS || id==WORK || id==PROVISIONS || id==RELEASE || id==TAKE_FOOD || id==CLEAR_FILTER
                || id==TAKE_LEFT || id==TAKE_RIGHT || id==TAKE_SEED || id==AUTO_HATCH || id==TAKE_FRUIT || id==TAKE_CARRIER;
        return new CareView.Control(id,t("button."+key),enabled,detail ? CareView.Control.DETAIL : CareView.Control.PRIMARY);
    }
    private static Component stock(ItemStack stack) { return stack.isEmpty() ? t("empty") : t("stock",stack.getCount(),stack.getHoverName()); }
    private static boolean has(Player player,Item item) { return !find(player,item).isEmpty(); }
    private static ItemStack find(Player player,Item item) {
        return find(player,stack->stack.is(item));
    }
    private static ItemStack find(Player player,java.util.function.Predicate<ItemStack> match) {
        for (int i=0;i<player.getInventory().getContainerSize();i++) {
            var stack=player.getInventory().getItem(i); if (match.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }
    private static boolean editable(Player player,BlockPos pos) { return player.mayBuild() && (pos==null || player.level().mayInteract(player,pos)); }
    public static boolean backAction(int id) {
        id&=255;
        return id==BACK || id==FamilyPanels.BACK_FIT || id==WorkshopPanels.BACK_TABLE || id==ApexPanel.CANCEL;
    }
    public static boolean readOnly(int id) { return CareReadingPanel.action(id) || id==WORK_MODES || id==REQUESTS || id==AnimalPanels.CARE_MENU || id==AnimalPanels.ORDERS || id==GENETICS || id==CARGO || id==BACK || id==ROLES || id==LearningPanel.OPEN || id==CombatPanel.OPEN || id==PartyPanel.OPEN || id==WorkshopPanels.READ_TABLE || SignalPanels.readOnly(id)
            || id==ApexPanel.PREPARE || id==ApexPanel.CANCEL || id==ApexPanel.SHOW_RING || id==WorkshopPanels.GOALS || id==WorkshopPanels.BACK_TABLE
            || id==com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD || FamilyPanels.readOnly(id) || id==LitterPanel.LITTER || id==LitterPanel.READ_SEED; }
    public static CareView animal(Player player,Bunnidog dog) {
        return AnimalPanels.home(player,dog);
    }
    public static CareView cargo(Player player,Bunnidog dog) {
        var bags=dog.carriedBundles(); var rows=new ArrayList<Component>();
        if(!bags.isEmpty())rows.add(t("animal.cargo",bags.size(),dog.bundleCapacity()));
        if (bags.size()>6) rows.add(t("cargo.more_bags",bags.size()-6));
        var controls=new ArrayList<CareView.Control>();controls.add(control(BACK,"back",true));
        if(!bags.isEmpty())controls.add(control(TAKE_BUNDLE,"take_bundle",editable(player,null)));
        if(!dog.society().workSupplies().isEmpty())controls.add(new CareView.Control(TAKE_SUPPLIES,Component.translatable("care.bunnidogs.commons.take"),dog.society().mayTakeSupplies(player)));
        var view=new CareView(t("cargo.title",dog.getDisplayName()),Component.empty(),rows,
                controls);
        // Show the newest six, including the actual next bag returned by Retrieve.
        var stock=new ArrayList<CareView.Stock>();
        if(!dog.society().workSupplies().isEmpty())stock.add(new CareView.Stock(Component.translatable("care.bunnidogs.commons.supplies"),dog.society().workSupplies()));
        for (int i=Math.max(0,bags.size()-6);i<bags.size();i++) stock.add(new CareView.Stock(t("display.bundle",i+1),bags.get(i)));
        return view.withStock(stock.toArray(CareView.Stock[]::new));
    }
    public static CareView station(Player player,BlockPos pos,BlockEntity be) {
        if (be instanceof FeedingTroughBlockEntity trough) return trough(player,pos,trough);
        if (be instanceof BreedingPenBlockEntity pen) return pen(player,pos,pen);
        if (be instanceof NurseryCradleBlockEntity cradle) return cradle(player,pos,cradle);
        if (be instanceof JuicerBlockEntity juicer) return juicer(player,pos,juicer);
        if (be instanceof CommissionBoardBlockEntity board) return board(player,pos,board);
        if (be instanceof GeneticsTableBlockEntity table) return WorkshopPanels.table(player,pos,table);
        if (be instanceof SpecimenJarBlockEntity jar) return SignalPanels.jar(player,pos,jar);
        if (be instanceof PedigreeComparatorBlockEntity detector) return SignalPanels.detector(player,pos,detector);
        if (be instanceof TrophyPedestalBlockEntity pedestal) return SignalPanels.trophy(player,pos,pedestal);
        if (player.level().getBlockState(pos).getBlock() instanceof JudgingStandBlock) return WorkshopPanels.show(player,pos);
        throw new IllegalArgumentException("Unsupported care station");
    }
    private static CareView.Control reason(int id,String key,boolean enabled,String hint) {
        var c=control(id,key,enabled);
        return new CareView.Control(c.id(),c.label(),enabled,c.section(),enabled ? Component.empty() : t(hint));
    }
    private static CareView.Stock display(String key,ItemStack item) { return new CareView.Stock(t("display."+key),item); }
    private static CareView trough(Player player,BlockPos pos,FeedingTroughBlockEntity trough) {
        var controls=new ArrayList<CareView.Control>(); boolean edit=editable(player,pos);
        boolean tending=trough.getBlockState().getValue(FeedingTroughBlock.TENDING);
        controls.add(control(TENDING,tending ? "tending_off" : "tending_on",edit));
        controls.add(control(WORK_MODES,"work_modes",true));
        if (!trough.filter().isEmpty()) controls.add(control(CLEAR_FILTER,"clear_filter",edit));
        var view=new CareView(ModBlocks.FEEDING_TROUGH.get().getName(),trough.residentsPaused() ? t("station.paused")
                : com.bunnidogs.mod.item.GuavaItem.tierOf(trough.food())>0 ? t("trough.rich") : Component.empty(),
                List.of(t("trough.work",Component.translatable("work.bunnidogs.mode."+trough.workMode().key()),t(tending ? "on" : "off"))),controls);
        return trough.filter().isEmpty() ? view.withStock(display("food",trough.food()))
                : view.withStock(display("food",trough.food()),display("filter",trough.filter()));
    }
    public static CareView troughWork(Player player,BlockPos pos,FeedingTroughBlockEntity trough) {
        var controls=new ArrayList<CareView.Control>(); boolean edit=editable(player,pos);
        for (var mode:TroughWork.values()) controls.add(new CareView.Control(MODE+mode.ordinal(),
                Component.translatable("work.bunnidogs.mode."+mode.key()),edit && mode!=trough.workMode(),CareView.Control.PRIMARY,t("trough.mode."+mode.key())));
        controls.add(control(BACK,"back",true));
        return new CareView(t("station.work_title"),Component.empty(),List.of(),controls);
    }
    private static CareView pen(Player player,BlockPos pos,BreedingPenBlockEntity pen) {
        var status=pen.status(); boolean edit=editable(player,pos);
        var rows=new ArrayList<Component>();
        var pair=PenBreeding.inspect((ServerLevel)player.level(),pos,pen.getBlockState().getValue(BreedingPenBlock.FACING));
        for (var parent:pair.parents().stream().limit(2).toList()) rows.add(parent.getDisplayName());
        return new CareView(ModBlocks.BREEDING_PEN.get().getName(),status==Pen.Status.READY ? Component.empty() : t("pen.next."+status.name().toLowerCase(Locale.ROOT)),rows,
                List.of(new CareView.Control(PAIR,t("button.pair"),edit && status==Pen.Status.READY,CareView.Control.PRIMARY,
                        Component.translatable("storage.bunnidogs.pair_cost"))))
                .withStock(display("manger_a",pen.food(0)),display("manger_b",pen.food(1)));
    }
    private static CareView cradle(Player player,BlockPos pos,NurseryCradleBlockEntity cradle) {
        boolean edit=editable(player,pos), ripe=cradle.age()==SeedCropBlock.MAX_AGE && cradle.seed().isPresent();
        var pad=NurseryHatching.inspect((ServerLevel)player.level(),pos,cradle.getBlockState().getValue(NurseryCradleBlock.FACING));
        var rows=new ArrayList<Component>();
        if (ripe) rows.add(t("station.ripe"));
        var controls=new ArrayList<CareView.Control>();
        if (ripe) controls.add(reason(HATCH,"hatch",edit && pad.status()==NurseryHatching.Status.READY,"station.pad_blocked"));
        else if (cradle.seed().isPresent()) controls.add(reason(GROW,"grow",edit && has(player,Items.BONE_MEAL),"station.need_bone_meal"));
        if (cradle.seed().isPresent()) controls.add(control(LitterPanel.READ_SEED,"read_seed",true));
        controls.add(control(com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD,"show_hatch_pad",true));
        controls.add(control(AUTO_HATCH,cradle.manualHatchOnly() ? "auto_on" : "auto_off",edit));
        return new CareView(ModBlocks.NURSERY_CRADLE.get().getName(),ripe && pad.status()!=NurseryHatching.Status.READY ? t("station.pad_blocked") : Component.empty(),rows,controls)
                .withStock(display("seed",cradle.storedSeed()));
    }
    private static CareView juicer(Player player,BlockPos pos,JuicerBlockEntity juicer) {
        boolean powered=player.level().hasNeighborSignal(pos);
        return new CareView(ModBlocks.JUICER.get().getName(),powered ? t("station.paused") : juicer.outputBlocked() ? t("station.output_full") : Component.empty(),
                List.of(),List.of())
                .withStock(display("input",juicer.item(0)),display("carrier",juicer.item(1)),display("output",juicer.item(2)));
    }
    private static CareView board(Player player,BlockPos pos,CommissionBoardBlockEntity board) {
        var reading=board.read(); var request=board.request(); boolean edit=editable(player,pos), done=board.completed(player,request);
        var rows=new ArrayList<Component>();
        rows.add(t("board.request",Component.translatable("commission.bunnidogs."+request.key()+".title")));
        rows.add(Component.translatable("commission.bunnidogs."+request.key()+".reward"));
        if (reading.candidate()!=null) {
            var dog=reading.candidate(); rows.add(t("board.candidate",dog.getDisplayName()));
            for (var check:request.checks(dog.genome(),dog.stats(),dog.carriedBundleCount())) rows.add(t("board.check",
                    t("check."+check.key(),TextComponents.t(check.detail().replace('_',' '))),check.current(),check.required())
                    .withStyle(check.met() ? ChatFormatting.GREEN : ChatFormatting.RED));
        } else rows.add(Component.translatable("commission.bunnidogs."+request.key()+".requirements"));
        return new CareView(ModBlocks.COMMISSION_BOARD.get().getName(),done ? t("station.completed") : reading.status()==Commissions.Status.READY ? Component.empty()
                : message("commission."+reading.status().name().toLowerCase(Locale.ROOT)),rows,List.of(
                reason(SUBMIT,"submit",edit && !done && has(player,Items.PAPER) && reading.status()==Commissions.Status.READY,"station.need_submission"),
                control(REQUESTS,"requests",true),control(com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD,"show_front_pad",true)));
    }
    public static CareView requests(Player player,BlockPos pos,CommissionBoardBlockEntity board) {
        var controls=new ArrayList<CareView.Control>();
        for (var choice:Commissions.Request.values()) controls.add(new CareView.Control(REQUEST+choice.ordinal(),
                t(board.completed(player,choice) ? "board.done" : "board.choice",Component.translatable("commission.bunnidogs."+choice.key()+".title")),editable(player,pos) && choice!=board.request()));
        controls.add(control(BACK,"back",true));
        return new CareView(t("station.requests_title"),Component.empty(),List.of(t("board.progress",board.completedCount(player),Commissions.Request.values().length)),controls);
    }
    /** True only after the real animal operation commits; callers can then release conversation. */
    public static boolean animalAction(Player player,Bunnidog dog,int id) {
        if (id>=CombatPanel.ASSIST && id<=CombatPanel.COMMAND) return CombatPanel.act(player,dog,id);
        return switch (id) {
            case GENETICS -> {
                boolean measured=player instanceof ServerPlayer && has(player,ModItems.CALIPERS.get());
                if (measured) {
                    player.closeContainer();
                    ModNetwork.sendSheet((ServerPlayer)player,new Pedigree.Entry(dog.genome(),dog.motherGenome(),dog.fatherGenome(),Optional.of(dog.getUUID()),dog.earTag(),Optional.of(dog.stats()),dog.isBaby()));
                }
                yield measured;
            }
            case WORK -> { dog.setWorkPaused(!dog.workPaused()); yield true; }
            case CARE -> dog.careFromStock(player,find(player,ModItems.GUAVA.get()));
            case COURTSHIP -> dog.courtshipFromStock(player,find(player,ModItems.GUAVA.get()));
            case TRAIN -> dog.trail().trainFromStock(player,find(player,ModItems.GUAVA.get()));
            case GROOM -> dog.groomFromStock(player,find(player,Items.BRUSH));
            case CONFIRM_RELEASE -> dog.trail().release(player);
            case FOLLOW -> dog.trail().command(player,Trail.Order.FOLLOW);
            case WAIT -> dog.trail().command(player,Trail.Order.WAIT);
            case RESIDENT -> dog.trail().command(player,Trail.Order.RESIDENT);
            case PROVISIONS -> dog.trail().setProvisions(player,!dog.trail().provisions());
            case TAKE_BUNDLE -> { int before=dog.carriedBundleCount(); dog.retrieveBundle(player); yield dog.carriedBundleCount()<before; }
            default -> false;
        };
    }
    public static boolean endsConversation(int id) {
        return id==CARE || id==COURTSHIP || id==TRAIN || id==GROOM || id==CONFIRM_RELEASE || id==FOLLOW || id==WAIT || id==RESIDENT
                || id>=CombatPanel.ASSIST && id<=CombatPanel.COMMAND;
    }
    public static void act(Player player,Bunnidog dog,BlockPos pos,BlockEntity be,int id) {
        if (dog!=null) { animalAction(player,dog,id); return; }
        if (be instanceof FeedingTroughBlockEntity trough) {
            if (id==TENDING) player.level().setBlock(pos,trough.getBlockState().cycle(FeedingTroughBlock.TENDING),3);
            else if (id==CLEAR_FILTER) trough.clearFilter();
            else if (id>=MODE && id<MODE+TroughWork.values().length) trough.setWorkMode(TroughWork.values()[id-MODE]);
        } else if (be instanceof BreedingPenBlockEntity pen) {
            if (id==PAIR) player.sendOverlayMessage(message("pen."+pen.activate(player).name().toLowerCase(Locale.ROOT)));
        } else if (be instanceof NurseryCradleBlockEntity cradle) {
            if (id==HATCH) { var result=cradle.hatch();
                if (result.status()==NurseryHatching.Status.HATCHED) com.bunnidogs.mod.item.JourneyProgress.hatched(player);
                player.sendOverlayMessage(message("cradle."+result.status().name().toLowerCase(Locale.ROOT),result.count())); }
            else if (id==AUTO_HATCH) cradle.setManualHatchOnly(!cradle.manualHatchOnly());
            else if (id==GROW) cradle.growFromPlayer(player,find(player,Items.BONE_MEAL));
        } else if (be instanceof CommissionBoardBlockEntity board) {
            if (id==SUBMIT) board.submitFromInventory(player);
            else if (id>=REQUEST && id<REQUEST+Commissions.Request.values().length) board.select(Commissions.Request.values()[id-REQUEST]);
        } else if (be instanceof GeneticsTableBlockEntity table) WorkshopPanels.act(player,pos,table,id);
        else if (be instanceof SpecimenJarBlockEntity || be instanceof PedigreeComparatorBlockEntity || be instanceof TrophyPedestalBlockEntity) SignalPanels.act(player,be,id);
        else if (id==WorkshopPanels.AWARD && player.level().getBlockState(pos).getBlock() instanceof JudgingStandBlock)
            com.bunnidogs.mod.entity.ShowJudging.award(player.level(),pos,player);
        else if (id==REHOME && player.level().getBlockState(pos).getBlock() instanceof NestBlock nest) nest.assignLed(player.level(),pos,player);
    }
}
