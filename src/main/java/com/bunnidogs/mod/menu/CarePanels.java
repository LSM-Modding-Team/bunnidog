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
    public static final int ROLES=19;
    public static final int PAIR=30, TAKE_LEFT=31, TAKE_RIGHT=32, ADD_LEFT=33, ADD_RIGHT=34;
    public static final int HATCH=40, TAKE_SEED=41, AUTO_HATCH=42, ADD_SEED=43, GROW=44;
    public static final int TAKE_JUICE=50, TAKE_FRUIT=51, TAKE_CARRIER=52, ADD_FRUIT=53, ADD_BOTTLE=54;
    public static final int REHOME=60, SUBMIT=70, REQUEST=80;

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
    private static Component home(Bunnidog dog) {
        var pos=dog.homeNest(); return pos==null ? t("animal.no_home") : t("animal.home",pos.getX(),pos.getY(),pos.getZ());
    }
    public static boolean readOnly(int id) { return id==AnimalPanels.CARE_MENU || id==AnimalPanels.ORDERS || id==GENETICS || id==CARGO || id==BACK || id==ROLES || id==LearningPanel.OPEN || id==CombatPanel.OPEN || id==PartyPanel.OPEN || id==WorkshopPanels.READ_TABLE || SignalPanels.readOnly(id)
            || id==ApexPanel.PREPARE || id==ApexPanel.CANCEL || id==ApexPanel.SHOW_RING || id==WorkshopPanels.GOALS || id==WorkshopPanels.BACK_TABLE
            || id==com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD || FamilyPanels.readOnly(id) || id==LitterPanel.LITTER || id==LitterPanel.READ_SEED; }
    public static CareView animal(Player player,Bunnidog dog) {
        return AnimalPanels.home(player,dog);
    }
    public static CareView cargo(Player player,Bunnidog dog) {
        var bags=dog.carriedBundles(); var rows=new ArrayList<Component>();
        rows.add(t("animal.cargo",bags.size(),dog.bundleCapacity()));
        for (int i=0;i<Math.min(4,bags.size());i++) {
            var bag=bags.get(i); rows.add(t("cargo.bag",i+1,bag.getHoverName()));
            var contents=bag.getOrDefault(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS,net.minecraft.world.item.component.BundleContents.EMPTY);
            if (contents.weight().isError()) rows.add(t("cargo.unsupported"));
            else if (contents.items().isEmpty()) rows.add(t("empty"));
            else {
                var summary=net.minecraft.network.chat.ComponentUtils.formatList(contents.items().stream().limit(8).toList(),entry->stock(entry.create())).copy();
                if (contents.items().size()>8) summary.append("; ").append(t("cargo.more",contents.items().size()-8));
                rows.add(summary);
            }
        }
        if (bags.size()>4) rows.add(t("cargo.more_bags",bags.size()-4));
        // Two rows per shown bag keep the snapshot bounded even for legacy or command-added cargo.
        return new CareView(t("cargo.title",dog.getDisplayName()),Component.empty(),rows,
                List.of(control(BACK,"back",true),control(TAKE_BUNDLE,"take_bundle",editable(player,null) && !bags.isEmpty())));
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
        return nest(player,pos);
    }
    private static CareView trough(Player player,BlockPos pos,FeedingTroughBlockEntity trough) {
        var controls=new ArrayList<CareView.Control>(); boolean edit=editable(player,pos);
        boolean tending=trough.getBlockState().getValue(FeedingTroughBlock.TENDING);
        controls.add(control(TENDING,tending ? "tending_off" : "tending_on",edit));
        for (var mode:TroughWork.values()) controls.add(new CareView.Control(MODE+mode.ordinal(),
                Component.translatable("work.bunnidogs.mode."+mode.key()),edit && mode!=trough.workMode(),CareView.Control.DETAIL));
        controls.add(control(TAKE_FOOD,"take_food",edit && !trough.food().isEmpty()));
        controls.add(control(CLEAR_FILTER,"clear_filter",edit && !trough.filter().isEmpty()));
        controls.add(control(ADD_FOOD,"add_food",edit && !find(player,stack->stack.is(ModItems.GUAVA.get()) && trough.canInsert(stack)).isEmpty()));
        return new CareView(ModBlocks.FEEDING_TROUGH.get().getName(),t(trough.residentsPaused() ? "trough.next.power" : com.bunnidogs.mod.item.GuavaItem.tierOf(trough.food())>0
                ? "trough.next.rich" : trough.food().isEmpty() ? "trough.next.food" : "trough.next.ready"),
                List.of(t("trough.stock",stock(trough.food())),t("trough.filter",stock(trough.filter())),
                        t("trough.work",Component.translatable("work.bunnidogs.mode."+trough.workMode().key()),t(tending ? "on" : "off")),
                        t("trough.mode."+trough.workMode().key()),t("trough.meals")),controls);
    }
    private static CareView pen(Player player,BlockPos pos,BreedingPenBlockEntity pen) {
        var status=pen.status(); boolean edit=editable(player,pos);
        var rows=new ArrayList<Component>(); rows.add(t("pen.left",stock(pen.food(0)))); rows.add(t("pen.right",stock(pen.food(1))));
        rows.add(message("pen."+(status==Pen.Status.PAIR ? "need_pair" : status.name().toLowerCase(Locale.ROOT))));
        var pair=PenBreeding.inspect((ServerLevel)player.level(),pos,pen.getBlockState().getValue(BreedingPenBlock.FACING));
        for (int i=0;i<Math.min(2,pair.parents().size());i++) rows.add(t("pen.parent",i+1,pair.parents().get(i).getDisplayName(),home(pair.parents().get(i))));
        rows.add(t("pen.seed"));
        return new CareView(ModBlocks.BREEDING_PEN.get().getName(),t("pen.next."+status.name().toLowerCase(Locale.ROOT)),rows,
                List.of(control(ADD_LEFT,"add_left",edit && !find(player,stack->stack.is(ModItems.GUAVA.get()) && pen.canInsert(stack,0)).isEmpty()),
                        control(ADD_RIGHT,"add_right",edit && !find(player,stack->stack.is(ModItems.GUAVA.get()) && pen.canInsert(stack,1)).isEmpty()),
                        control(PAIR,"pair",edit && status==Pen.Status.READY),control(TAKE_LEFT,"take_left",edit && !pen.food(0).isEmpty()),
                        control(TAKE_RIGHT,"take_right",edit && !pen.food(1).isEmpty())));
    }
    private static CareView cradle(Player player,BlockPos pos,NurseryCradleBlockEntity cradle) {
        boolean edit=editable(player,pos), ripe=cradle.age()==SeedCropBlock.MAX_AGE && cradle.seed().isPresent();
        var pad=NurseryHatching.inspect((ServerLevel)player.level(),pos,cradle.getBlockState().getValue(NurseryCradleBlock.FACING));
        var rows=new ArrayList<Component>(); rows.add(t("cradle.seed",stock(cradle.storedSeed())));
        rows.add(t("cradle.growth",cradle.age(),SeedCropBlock.MAX_AGE,(cradle.growthWait()+19)/20));
        rows.add(t("cradle.pad",message("cradle."+pad.status().name().toLowerCase(Locale.ROOT))));
        if (pad.problem()!=null) rows.add(t("cradle.blocked_at",pad.problem().getX(),pad.problem().getY(),pad.problem().getZ()));
        rows.add(t("cradle.auto",t(cradle.manualHatchOnly() ? "off" : "on")));
        rows.add(t("cradle.grow"));
        return new CareView(ModBlocks.NURSERY_CRADLE.get().getName(),t(cradle.storedSeed().isEmpty() ? "cradle.next.seed" : !ripe ? "cradle.next.grow"
                : pad.status()==NurseryHatching.Status.READY ? "cradle.next.hatch" : "cradle.next.space"),rows,
                List.of(control(HATCH,"hatch",edit && ripe && pad.status()==NurseryHatching.Status.READY),
                        control(TAKE_SEED,"take_seed",edit && !cradle.storedSeed().isEmpty()),
                        control(AUTO_HATCH,cradle.manualHatchOnly() ? "auto_on" : "auto_off",edit),
                        control(ADD_SEED,"add_seed",edit && cradle.storedSeed().isEmpty() && !find(player,stack->com.bunnidogs.mod.item.SeedItem.read(stack).isPresent()).isEmpty()),
                        control(GROW,"grow",edit && cradle.seed().isPresent() && !ripe && has(player,Items.BONE_MEAL)),
                        control(LitterPanel.READ_SEED,"read_seed",cradle.seed().isPresent()),
                        control(com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD,"show_hatch_pad",true)));
    }
    private static CareView juicer(Player player,BlockPos pos,JuicerBlockEntity juicer) {
        boolean edit=editable(player,pos), powered=player.level().hasNeighborSignal(pos);
        return new CareView(ModBlocks.JUICER.get().getName(),t(powered ? "juicer.next.power" : juicer.outputBlocked() ? "juicer.next.output" : "juicer.next.inputs"),
                List.of(t("juicer.fruit",stock(juicer.item(0))),t("juicer.carrier",stock(juicer.item(1))),t("juicer.output",stock(juicer.item(2))),
                        t("juicer.progress",juicer.progress()*100/JuicerBlockEntity.PROCESS_TICKS),t("juicer.infuse")),
                List.of(control(ADD_FRUIT,"add_fruit",edit && juicer.canInsert(find(player,ModItems.GUAVA.get()))),
                        control(ADD_BOTTLE,"add_bottle",edit && juicer.canInsert(find(player,Items.GLASS_BOTTLE))),
                        control(TAKE_JUICE,"take_juice",edit && !juicer.item(2).isEmpty()),
                        control(TAKE_FRUIT,"take_fruit",edit && !juicer.item(0).isEmpty()),control(TAKE_CARRIER,"take_carrier",edit && !juicer.item(1).isEmpty())));
    }
    private static CareView board(Player player,BlockPos pos,CommissionBoardBlockEntity board) {
        var reading=board.read(); var request=board.request(); boolean edit=editable(player,pos), done=board.completed(player,request);
        var rows=new ArrayList<Component>(); var controls=new ArrayList<CareView.Control>();
        rows.add(t("board.request",Component.translatable("commission.bunnidogs."+request.key()+".title")));
        rows.add(t("board.progress",board.completedCount(player),Commissions.Request.values().length));
        rows.add(Component.translatable("commission.bunnidogs."+request.key()+".reward"));
        if (reading.candidate()!=null) {
            var dog=reading.candidate(); rows.add(t("board.candidate",dog.getDisplayName()));
            for (var check:request.checks(dog.genome(),dog.stats(),dog.carriedBundleCount())) rows.add(t("board.check",
                    t("check."+check.key(),TextComponents.t(check.detail().replace('_',' '))),check.current(),check.required())
                    .withStyle(check.met() ? ChatFormatting.GREEN : ChatFormatting.RED));
        } else rows.add(Component.translatable("commission.bunnidogs."+request.key()+".requirements"));
        rows.add(t("board.fixed"));
        rows.add(t("board.setup"));
        for (var choice:Commissions.Request.values()) controls.add(new CareView.Control(REQUEST+choice.ordinal(),
                t(board.completed(player,choice) ? "board.done" : "board.choice",Component.translatable("commission.bunnidogs."+choice.key()+".title")),edit && choice!=request,CareView.Control.CHOICE));
        controls.add(control(SUBMIT,"submit",edit && !done && has(player,Items.PAPER) && reading.status()==Commissions.Status.READY));
        controls.add(control(com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD,"show_front_pad",true));
        return new CareView(ModBlocks.COMMISSION_BOARD.get().getName(),done ? t("board.next.done") : reading.status()==Commissions.Status.READY
                ? t(has(player,Items.PAPER) ? "board.next.submit" : "board.next.paper")
                : message("commission."+reading.status().name().toLowerCase(Locale.ROOT)),rows,controls);
    }
    private static CareView nest(Player player,BlockPos pos) {
        var level=(ServerLevel)player.level(); boolean claimed=NestBlock.claimed(level,pos);
        var residents=level.getEntitiesOfClass(Bunnidog.class,new net.minecraft.world.phys.AABB(pos).inflate(24),
                dog->dog.isAlive() && dog.acceptsCare() && pos.equals(dog.homeNest()));
        var rows=new ArrayList<Component>(); rows.add(t(claimed ? "nest.claimed" : "nest.free"));
        for (var dog:residents.stream().limit(1).toList()) rows.add(t("nest.resident",dog.getDisplayName()));
        rows.add(t("nest.one")); rows.add(t("nest.range")); rows.add(t("nest.compass"));
        return new CareView(ModBlocks.NEST.get().getName(),t(claimed ? "nest.next.claimed" : "nest.next.lead"),rows,
                List.of(control(REHOME,"rehome",editable(player,pos) && NestBlock.hasLedAdult(level,pos,player)
                        && (!claimed || residents.stream().anyMatch(dog->dog.isLeashed() && dog.getLeashHolder()==player)))));
    }
    public static void act(Player player,Bunnidog dog,BlockPos pos,BlockEntity be,int id) {
        if (dog!=null) {
            if (id>=CombatPanel.ASSIST && id<=CombatPanel.COMMAND) { CombatPanel.act(player,dog,id); return; }
            switch (id) {
                case GENETICS -> {
                    if (player instanceof ServerPlayer server && has(player,ModItems.CALIPERS.get())) {
                        player.closeContainer();
                        ModNetwork.sendSheet(server,new Pedigree.Entry(dog.genome(),dog.motherGenome(),dog.fatherGenome(),Optional.of(dog.getUUID()),dog.earTag(),Optional.of(dog.stats()),dog.isBaby()));
                    }
                }
                case WORK -> dog.setWorkPaused(!dog.workPaused());
                case CARE -> dog.careFromStock(player,find(player,ModItems.GUAVA.get()));
                case COURTSHIP -> dog.courtshipFromStock(player,find(player,ModItems.GUAVA.get()));
                case TRAIN -> dog.trail().trainFromStock(player,find(player,ModItems.GUAVA.get()));
                case GROOM -> dog.groomFromStock(player,find(player,Items.BRUSH));
                case RELEASE -> dog.trail().release(player);
                case FOLLOW -> dog.trail().command(player,Trail.Order.FOLLOW);
                case WAIT -> dog.trail().command(player,Trail.Order.WAIT);
                case RESIDENT -> dog.trail().command(player,Trail.Order.RESIDENT);
                case PROVISIONS -> dog.trail().setProvisions(player,!dog.trail().provisions());
                case TAKE_BUNDLE -> dog.retrieveBundle(player);
                default -> { }
            }
        } else if (be instanceof FeedingTroughBlockEntity trough) {
            if (id==TENDING) player.level().setBlock(pos,trough.getBlockState().cycle(FeedingTroughBlock.TENDING),3);
            else if (id==TAKE_FOOD) give(player,trough.takeAll());
            else if (id==CLEAR_FILTER) trough.clearFilter();
            else if (id==ADD_FOOD) trough.insertFromPlayer(player,find(player,stack->stack.is(ModItems.GUAVA.get()) && trough.canInsert(stack)),1);
            else if (id>=MODE && id<MODE+TroughWork.values().length) trough.setWorkMode(TroughWork.values()[id-MODE]);
        } else if (be instanceof BreedingPenBlockEntity pen) {
            if (id==PAIR) player.sendOverlayMessage(message("pen."+pen.activate(player).name().toLowerCase(Locale.ROOT)));
            else if (id==TAKE_LEFT || id==TAKE_RIGHT) give(player,pen.take(id==TAKE_LEFT ? 0 : 1));
            else if (id==ADD_LEFT || id==ADD_RIGHT) { int slot=id==ADD_LEFT ? 0 : 1;
                pen.insertFromHand(player,find(player,stack->stack.is(ModItems.GUAVA.get()) && pen.canInsert(stack,slot)),slot,1); }
        } else if (be instanceof NurseryCradleBlockEntity cradle) {
            if (id==HATCH) { var result=cradle.hatch();
                if (result.status()==NurseryHatching.Status.HATCHED) com.bunnidogs.mod.item.JourneyProgress.hatched(player);
                player.sendOverlayMessage(message("cradle."+result.status().name().toLowerCase(Locale.ROOT),result.count())); }
            else if (id==TAKE_SEED) give(player,cradle.takeSeed());
            else if (id==AUTO_HATCH) cradle.setManualHatchOnly(!cradle.manualHatchOnly());
            else if (id==ADD_SEED) cradle.insertFromPlayer(player,find(player,stack->com.bunnidogs.mod.item.SeedItem.read(stack).isPresent()));
            else if (id==GROW) cradle.growFromPlayer(player,find(player,Items.BONE_MEAL));
        } else if (be instanceof JuicerBlockEntity juicer) {
            if (id==TAKE_JUICE || id==TAKE_FRUIT || id==TAKE_CARRIER) give(player,juicer.take(id==TAKE_JUICE ? 2 : id==TAKE_FRUIT ? 0 : 1));
            else if (id==ADD_FRUIT || id==ADD_BOTTLE) juicer.insertFromHand(player,find(player,id==ADD_FRUIT ? ModItems.GUAVA.get() : Items.GLASS_BOTTLE),1);
        } else if (be instanceof CommissionBoardBlockEntity board) {
            if (id==SUBMIT) board.submitFromInventory(player);
            else if (id>=REQUEST && id<REQUEST+Commissions.Request.values().length) board.select(Commissions.Request.values()[id-REQUEST]);
        } else if (be instanceof GeneticsTableBlockEntity table) WorkshopPanels.act(player,pos,table,id);
        else if (be instanceof SpecimenJarBlockEntity || be instanceof PedigreeComparatorBlockEntity || be instanceof TrophyPedestalBlockEntity) SignalPanels.act(player,be,id);
        else if (id==WorkshopPanels.AWARD && player.level().getBlockState(pos).getBlock() instanceof JudgingStandBlock)
            com.bunnidogs.mod.entity.ShowJudging.award(player.level(),pos,player);
        else if (id==REHOME && player.level().getBlockState(pos).getBlock() instanceof NestBlock nest) nest.assignLed(player.level(),pos,player);
    }
    private static void give(Player player,ItemStack stack) { if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack,false); }
}
