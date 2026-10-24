package com.bunnidogs.mod.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Commissions;
import com.bunnidogs.mod.network.CareViewPayload;
import com.bunnidogs.mod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/** Vanilla container lifetime and button packets; the client never supplies a target or an item. */
public final class CareMenu extends AbstractContainerMenu {
    private final Player owner;
    private final Level level;
    private final Bunnidog dog;
    private final BlockPos pos;
    private final Block block;
    private final BlockEntity station;
    private CareView view;
    private int refresh;
    private int generation;
    private Context published;
    private long changedUntil;
    private boolean acting;
    private enum Mode { MAIN, CARE, GUAVA, ORDERS, CARGO, ROLES, LEARNING, GOALS, CONFIRM_RITUAL, FAMILY, SEED_FAMILY, FIT, FIT_CHOICES, LITTER, COMBAT, PARTY, TROUGH_WORK, REQUESTS, RELEASE_BOND, MEASURE }
    private Mode mode=Mode.MAIN;
    private Mode publishedMode;
    private final java.util.ArrayDeque<Mode> history=new java.util.ArrayDeque<>();
    private void navigate(Mode next) { if (mode!=next) { history.push(mode); mode=next; } }
    private void back() { mode=history.isEmpty() ? Mode.MAIN : history.pop(); }
    private Commissions.Request fitGoal;
    private int familyPage;
    private int readingPage=-1;
    private final ItemStack preferredSample;
    private Component actionNotice;
    private long noticeUntil;
    private InteractionHand offeringHand;
    private StationInventory.Kind inventoryKind=StationInventory.Kind.NONE;
    private record Context(List<Object> facts,List<ItemStack> stock) {
        Context { facts=List.copyOf(facts); stock=stock.stream().map(ItemStack::copy).toList(); }
        boolean same(Context other) {
            if (other==null || !facts.equals(other.facts) || stock.size()!=other.stock.size()) return false;
            for (int i=0;i<stock.size();i++) if (!ItemStack.matches(stock.get(i),other.stock.get(i))) return false;
            return true;
        }
    }

    /** Native client factory. Target resolution exists only on the server. */
    public CareMenu(int id,Inventory inventory) { this(id,inventory,null,null); }
    public CareMenu(int id,Inventory inventory,RegistryFriendlyByteBuf data) {
        this(id,inventory,null,null);
        if (data!=null) { this.view=CareViewPayload.VIEW.decode(data); addStorageSlots(inventory,null); }
    }
    /** The initial view travels with the native opening packet, before client screen construction. */
    private static MenuProvider provider(MenuConstructor factory,Component title) {
        return new MenuProvider() {
            @Override public Component getDisplayName() { return title; }
            @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player) { return factory.createMenu(id,inventory,player); }
            @Override public void writeClientSideData(AbstractContainerMenu menu,RegistryFriendlyByteBuf buffer) {
                ((CareMenu)menu).writeOpeningData(buffer);
            }
        };
    }
    public void writeOpeningData(RegistryFriendlyByteBuf buffer) {
        view=currentView();
        CareViewPayload.VIEW.encode(buffer,view);
    }
    private CareMenu(int id,Inventory inventory,Bunnidog dog,BlockPos pos) {
        this(id,inventory,dog,pos,null);
    }
    private CareMenu(int id,Inventory inventory,Bunnidog dog,BlockPos pos,ItemStack preferredSample) {
        super(ModMenus.CARE.get(),id); this.owner=inventory.player; this.level=owner.level();
        this.preferredSample=preferredSample;
        this.dog=dog; this.pos=pos==null ? null : pos.immutable();
        this.block=pos==null ? null : level.getBlockState(pos).getBlock();
        this.station=pos==null ? null : level.getBlockEntity(pos);
        if (station!=null) addStorageSlots(inventory,station);
    }
    public StationInventory.Kind inventoryKind() { return inventoryKind; }
    public boolean inventoryVisible() {
        return inventoryKind!=StationInventory.Kind.NONE && (station!=null ? mode==Mode.MAIN : view!=null && (view.page()&255)==0);
    }
    private boolean editStorage() { return station!=null && inventoryVisible() && stillValid(owner) && StationUse.editable(owner,station); }
    private void addStorageSlots(Inventory inventory,BlockEntity target) {
        inventoryKind=target==null ? view.station().kind() : StationInventory.state(target).kind();
        if (inventoryKind==StationInventory.Kind.NONE) return;
        var kind=inventoryKind;
        if (target!=null) {
            var handler=StationInventory.handler(target);
            for (int i=0;i<kind.slots;i++) {
                final int index=i;
                addSlot(new ResourceHandlerSlot(handler,(slot,resource,amount)->{
                    if (editStorage()) {
                        var next=resource.toStack(amount);
                        boolean incubating=kind==StationInventory.Kind.CRADLE && com.bunnidogs.mod.item.SeedItem.read(next).isPresent()
                                && !handler.getResource(slot).equals(resource);
                        StationInventory.replace(handler,slot,next);
                        if (incubating) com.bunnidogs.mod.item.JourneyProgress.incubating(owner);
                    }
                },i,kind.x(i),44) {
                    @Override public boolean isActive() { return inventoryVisible(); }
                    @Override public boolean mayPlace(ItemStack stack) { return editStorage() && !(kind==StationInventory.Kind.JUICER && index==2) && super.mayPlace(stack); }
                    @Override public boolean mayPickup(Player player) { return player==owner && editStorage() && super.mayPickup(player); }
                });
            }
        } else {
            var shadow=new SimpleContainer(kind.slots);
            for (int i=0;i<kind.slots;i++) {
                if (i<view.stock().size()) shadow.setItem(i,view.stock().get(i).item().copy());
                final int index=i;
                addSlot(new Slot(shadow,i,kind.x(i),44) {
                    @Override public boolean isActive() { return inventoryVisible(); }
                    @Override public int getMaxStackSize() { return kind.capacity; }
                    @Override public int getMaxStackSize(ItemStack stack) { return Math.min(kind.capacity,stack.getMaxStackSize()); }
                    @Override public boolean mayPlace(ItemStack stack) {
                        var filter=kind==StationInventory.Kind.TROUGH && view.stock().size()>1 ? view.stock().get(1).item() : ItemStack.EMPTY;
                        return inventoryVisible() && owner.mayBuild() && kind.accepts(index,stack,filter);
                    }
                    @Override public boolean mayPickup(Player player) { return inventoryVisible() && player.mayBuild(); }
                });
            }
        }
        for (int row=0;row<3;row++) for (int column=0;column<9;column++) addPlayerSlot(inventory,column+9+row*9,8+column*18,105+row*18);
        for (int column=0;column<9;column++) addPlayerSlot(inventory,column,8+column*18,163);
    }
    private void addPlayerSlot(Inventory inventory,int index,int x,int y) {
        addSlot(new Slot(inventory,index,x,y) {
            @Override public boolean isActive() { return inventoryVisible(); }
            @Override public boolean mayPlace(ItemStack stack) { return inventoryVisible(); }
            @Override public boolean mayPickup(Player player) { return player==owner && inventoryVisible(); }
        });
    }
    @Override public void clicked(int slot,int button,ContainerInput input,Player player) {
        if (acting || player!=owner || player.containerMenu!=this || !stillValid(player) || !inventoryVisible()) return;
        acting=true;
        try {
            // A neighbor notification may close/replace the menu; publish it after vanilla owns the carried item.
            try (var updates=StationInventoryUpdates.begin()) { super.clicked(slot,button,input,player); }
            if (player.containerMenu==this) { refresh=0; broadcastChanges(); }
        }
        finally { acting=false; }
    }
    public static void open(Player player,Bunnidog dog) {
        openAnimal(player,dog,null);
    }
    private static void openAnimal(Player player,Bunnidog dog,Commissions.Request goal) {
        openAnimal(player,dog,goal,false);
    }
    public static void openCombat(Player player,Bunnidog dog) { openAnimal(player,dog,null,true); }
    public static void openCareReading(Player player,Bunnidog dog) {
        if (FamilyPanels.hasCalipers(player)) openAnimal(player,dog,null,false,null,true);
    }
    public static void openGuava(Player player,Bunnidog dog,InteractionHand hand) { openAnimal(player,dog,null,false,hand); }
    public static void openParty(Player player) {
        if (player instanceof ServerPlayer server && !player.isSpectator()) server.openMenu(provider((id,inv,p)->{
            var menu=new CareMenu(id,inv,null,null); menu.mode=Mode.PARTY; return menu;
        },Component.translatable("care.bunnidogs.party.title")));
    }
    private static void openAnimal(Player player,Bunnidog dog,Commissions.Request goal,boolean combat) {
        openAnimal(player,dog,goal,combat,null);
    }
    private static void openAnimal(Player player,Bunnidog dog,Commissions.Request goal,boolean combat,InteractionHand offeringHand) {
        openAnimal(player,dog,goal,combat,offeringHand,false);
    }
    private static void openAnimal(Player player,Bunnidog dog,Commissions.Request goal,boolean combat,InteractionHand offeringHand,boolean measured) {
        if (player instanceof ServerPlayer server && !player.isSpectator() && dog.isAlive() && dog.acceptsCare() && dog.level()==player.level() && dog.distanceToSqr(player)<=36) {
            com.bunnidogs.mod.item.JourneyProgress.met(player);
            server.openMenu(provider((id,inv,p)->{
                var menu=new CareMenu(id,inv,dog,null);
                if (goal!=null) { menu.fitGoal=goal; menu.mode=Mode.FIT; }
                if (measured) menu.navigate(Mode.MEASURE);
                if (combat) { menu.navigate(Mode.ORDERS); menu.navigate(Mode.COMBAT); }
                if (offeringHand!=null) { menu.mode=Mode.GUAVA; menu.offeringHand=offeringHand; }
                dog.conversation().begin(server,menu);
                return menu;
            },dog.getDisplayName()));
        }
    }
    public static void open(Player player,BlockPos pos) {
        open(player,pos,null);
    }
    public static void open(Player player,BlockPos pos,ItemStack preferredSample) {
        if (player instanceof ServerPlayer server && !player.isSpectator() && server.level().hasChunkAt(pos) && supported(server.level().getBlockState(pos).getBlock())
                && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36) {
            com.bunnidogs.mod.item.JourneyProgress.met(player);
            server.openMenu(provider((id,inv,p)->new CareMenu(id,inv,null,pos,preferredSample),Component.translatable("screen.bunnidogs.care.title")));
        }
    }
    public CareView view() { return view; }
    public boolean talksTo(Bunnidog target) { return dog==target && owner.containerMenu==this && stillValid(owner); }
    @Override public void removed(Player player) {
        super.removed(player);
        if (dog!=null) dog.conversation().end(player,this);
    }
    public int buttonId(int action) {
        return view==null ? -1 : view.controls().stream().filter(c->(c.id()&255)==action).mapToInt(CareView.Control::id).findFirst().orElse(-1);
    }
    public void receive(CareView value) { if (level.isClientSide()) view=value; }
    @Override public boolean stillValid(Player player) {
        if (level.isClientSide()) return true;
        if (player!=owner || player.level()!=level || !player.isAlive() || player.isSpectator()) return false;
        if (mode==Mode.PARTY && dog==null) return true;
        if (dog!=null) return dog.isAlive() && !dog.isRemoved() && dog.acceptsCare() && dog.level()==level && dog.distanceToSqr(player)<=36;
        return pos!=null && level.hasChunkAt(pos) && level.getBlockState(pos).is(block) && level.getBlockEntity(pos)==station
                && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36;
    }
    private CareView snapshot() {
        if (mode==Mode.PARTY) {
            var party=PartyPanel.view(owner);
            if (history.isEmpty()) return party;
            var controls=new ArrayList<>(party.controls());
            controls.add(new CareView.Control(CarePanels.BACK,AnimalPanels.text("back"),true));
            return new CareView(party.title(),party.next(),party.rows(),controls);
        }
        if (dog!=null) return switch (mode) {
            case MEASURE -> CareReadingPanel.view(owner,dog,readingPage);
            case CARE -> AnimalPanels.care(owner,dog);
            case GUAVA -> AnimalPanels.guava(owner,dog,owner.getItemInHand(offeringHand));
            case ORDERS -> AnimalPanels.orders(owner,dog);
            case RELEASE_BOND -> AnimalPanels.release(owner,dog);
            case CARGO -> CarePanels.cargo(owner,dog);
            case ROLES -> RolePanel.view(owner,dog);
            case LEARNING -> LearningPanel.view(owner,dog);
            case COMBAT -> CombatPanel.view(owner,dog);
            case FIT -> FamilyPanels.fit(owner,dog,fitGoal);
            case FIT_CHOICES -> FamilyPanels.choices(owner,dog);
            default -> CarePanels.animal(owner,dog);
        };
        if (block instanceof ApexCrucibleBlock) return ApexPanel.view(owner,pos,ApexPanel.sample(owner,preferredSample),mode==Mode.CONFIRM_RITUAL);
        if (station instanceof FeedingTroughBlockEntity trough && mode==Mode.TROUGH_WORK) return CarePanels.troughWork(owner,pos,trough);
        if (station instanceof CommissionBoardBlockEntity board && mode==Mode.REQUESTS) return CarePanels.requests(owner,pos,board);
        if (station instanceof GeneticsTableBlockEntity table && mode==Mode.GOALS) return WorkshopPanels.goals(owner,pos,table);
        if (station instanceof GeneticsTableBlockEntity table && (mode==Mode.FAMILY || mode==Mode.SEED_FAMILY)) return FamilyPanels.family(owner,pos,table,familyPage,mode==Mode.SEED_FAMILY);
        if (station instanceof GeneticsTableBlockEntity table && mode==Mode.LITTER) return LitterPanel.view(owner,table);
        return CarePanels.station(owner,pos,station);
    }
    private static boolean supported(Block block) {
        return block instanceof FeedingTroughBlock || block instanceof BreedingPenBlock
                || block instanceof NurseryCradleBlock || block instanceof JuicerBlock || block instanceof CommissionBoardBlock
                || block instanceof GeneticsTableBlock || block instanceof JudgingStandBlock || block instanceof SpecimenJarBlock
                || block instanceof PedigreeComparatorBlock || block instanceof TrophyPedestalBlock || block instanceof ApexCrucibleBlock;
    }
    /** Bind an action to the actual shared stock/selection the player saw, not to a timer tick. */
    private Context context() {
        var facts=new ArrayList<Object>(); var stock=new ArrayList<ItemStack>();
        if (mode==Mode.PARTY) facts.add(com.bunnidogs.mod.entity.CompanionParty.context(owner));
        else if (dog!=null) {
            if (mode==Mode.MEASURE) facts.add(readingPage);
            if (mode==Mode.GUAVA) stock.add(owner.getItemInHand(offeringHand));
            facts.add(dog.workPaused()); facts.add(Optional.ofNullable(dog.trail().keeper())); facts.add(dog.trail().order()); facts.add(dog.trail().provisions());
            facts.add(dog.combat().selected()); facts.add(dog.combat().downed()); facts.add(dog.combat().recovering());
            facts.add(dog.combat().partyState());
            facts.add(Optional.ofNullable(dog.homeNest()));
            facts.add(dog.genome()); facts.add(dog.stats()); facts.add(dog.learning().knowledge());
            facts.add(Optional.ofNullable(dog.learning().lesson())); facts.add(Optional.ofNullable(dog.learning().teacher()));
            facts.add(FamilyPanels.hasCalipers(owner));
            stock.addAll(dog.carriedBundles());
            if (mode==Mode.FIT || mode==Mode.FIT_CHOICES || mode==Mode.ROLES) facts.add(FamilyPanels.hasCalipers(owner));
        } else {
            var state=level.getBlockState(pos);
            facts.add(block instanceof PedigreeComparatorBlock ? state.setValue(PedigreeComparatorBlock.POWER,0) : state);
            if (station instanceof FeedingTroughBlockEntity trough) { facts.add(trough.workMode()); stock.add(trough.food()); stock.add(trough.filter()); }
            else if (station instanceof BreedingPenBlockEntity pen) {
                stock.add(pen.food(0)); stock.add(pen.food(1));
                for (var parent:com.bunnidogs.mod.entity.PenBreeding.inspect((net.minecraft.server.level.ServerLevel)level,pos,
                        pen.getBlockState().getValue(BreedingPenBlock.FACING)).parents()) facts.add(parent.getUUID());
            } else if (station instanceof NurseryCradleBlockEntity cradle) { stock.add(cradle.storedSeed()); facts.add(cradle.manualHatchOnly()); }
            else if (station instanceof JuicerBlockEntity juicer) for (int i=0;i<3;i++) stock.add(juicer.item(i));
            else if (station instanceof CommissionBoardBlockEntity board) {
                facts.add(board.request()); facts.add(Optional.ofNullable(board.read().candidate()).map(Bunnidog::getUUID));
            } else if (station instanceof GeneticsTableBlockEntity table) {
                facts.add(table.goalKey());
                stock.add(table.item(0)); stock.add(table.item(1));
                stock.add(LitterPanel.nextSeed(owner));
                if (mode==Mode.FAMILY || mode==Mode.SEED_FAMILY) {
                    facts.add(FamilyPanels.hasCalipers(owner));
                    var family=FamilyPanels.relatives(owner,pos,table,mode==Mode.SEED_FAMILY); facts.add(mode); facts.add(familyPage); facts.add(family.size());
                    for (var relative:FamilyPanels.visible(family,familyPage)) facts.add(relative.getUUID());
                }
            } else if (block instanceof JudgingStandBlock) {
                var reading=com.bunnidogs.mod.entity.ShowJudging.inspect(level,pos,owner);
                facts.add(reading.entry());
                facts.add(reading.candidate().map(Bunnidog::highestShowAward));
            } else if (station instanceof SpecimenJarBlockEntity jar) {
                stock.add(jar.sample());
            } else if (station instanceof PedigreeComparatorBlockEntity detector) {
                facts.add(detector.reference()); stock.add(WorkshopPanels.nextSample(owner));
            } else if (station instanceof TrophyPedestalBlockEntity pedestal) {
                stock.add(pedestal.item());
            } else if (block instanceof ApexCrucibleBlock) {
                var source=ApexPanel.sample(owner,preferredSample); stock.add(source);
                var reading=com.bunnidogs.mod.entity.ApexRitual.inspect(owner,pos,source);
                facts.add(level.getDifficulty()); facts.add(reading.status());
                for (var slot:reading.slots()) { facts.add(slot.pos()); facts.add(Optional.ofNullable(slot.pedestal())); stock.add(slot.stock()); }
            }
        }
        return new Context(facts,stock);
    }
    private CareView currentView() {
        if (mode==Mode.MEASURE && !FamilyPanels.hasCalipers(owner)) { mode=Mode.MAIN; readingPage=-1; history.clear(); }
        var currentContext=context();
        boolean changed=!currentContext.same(published);
        if (changed || mode!=publishedMode) {
            generation=generation==0x7FFFFF ? 1 : generation+1;
            if (changed && mode==Mode.CONFIRM_RITUAL && publishedMode==Mode.CONFIRM_RITUAL) { back(); }
            published=currentContext; publishedMode=mode;
        }
        var base=snapshot();
        return new CareView(base.title(),level.getGameTime()<changedUntil ? Component.translatable("care.bunnidogs.feedback.changed")
                : actionNotice!=null && level.getGameTime()<noticeUntil ? actionNotice : base.next(),
                base.rows(),base.controls().stream().map(this::stamp).toList(),dog==null || mode==Mode.MEASURE ? -1 : dog.getId(),
                mode.ordinal() | (mode==Mode.MEASURE ? (readingPage+1)<<8 : (mode==Mode.FAMILY || mode==Mode.SEED_FAMILY) ? familyPage<<8 : 0),base.stock(),StationInventory.state(station));
    }
    private CareView.Control stamp(CareView.Control control) {
        var hint=pos!=null && !control.enabled() && !CarePanels.readOnly(control.id()) && (!owner.mayBuild() || !level.mayInteract(owner,pos))
                ? Component.translatable("care.bunnidogs.station.read_only") : control.hint();
        return new CareView.Control((generation<<8)|control.id(),control.label(),control.enabled(),control.section(),hint);
    }
    private void publish(boolean force) {
        if (!(owner instanceof ServerPlayer server) || !stillValid(owner)) return;
        var current=currentView();
        if (force || !current.equals(view)) { view=current; PacketDistributor.sendToPlayer(server,new CareViewPayload(containerId,current)); }
    }
    @Override public void broadcastChanges() {
        super.broadcastChanges();
        if (!(owner instanceof ServerPlayer server) || !stillValid(owner) || refresh++%10!=0) return;
        publish(false);
    }
    @Override public boolean clickMenuButton(Player player,int buttonId) {
        int id=buttonId&255;
        if (acting || StationInventory.transferButton(id) || player.containerMenu!=this || !stillValid(player) || !CarePanels.readOnly(id) && !player.mayBuild()
                || pos!=null && !level.mayInteract(player,pos)) return false;
        if (buttonId<0 || (buttonId>>>8)!=generation || !context().same(published)) {
            changedUntil=level.getGameTime()+60; publish(true); return false;
        }
        if (!snapshot().permits(id)) { publish(true); return false; }
        acting=true;
        actionNotice=null;
        try {
            if (mode==Mode.GUAVA && dog!=null && (id==CarePanels.CARE || id==CarePanels.COURTSHIP || id==CarePanels.TRAIN)) {
                var actual=player.getItemInHand(offeringHand);
                boolean used=switch (id) {
                    case CarePanels.CARE -> dog.careFromStock(player,actual);
                    case CarePanels.COURTSHIP -> dog.courtshipFromStock(player,actual);
                    default -> dog.trail().trainFromStock(player,actual);
                };
                if (used && player.containerMenu==this) player.closeContainer();
            }
            else if (id==AnimalPanels.CARE_MENU && dog!=null) navigate(Mode.CARE);
            else if (id==AnimalPanels.ORDERS && dog!=null) navigate(Mode.ORDERS);
            else if (id==CarePanels.RELEASE && dog!=null) navigate(Mode.RELEASE_BOND);
            else if (id==CarePanels.CARGO) navigate(Mode.CARGO);
            else if (id==CarePanels.ROLES) navigate(Mode.ROLES);
            else if (id==LearningPanel.OPEN && dog!=null) navigate(Mode.LEARNING);
            else if (id>=LearningPanel.TEACH && id<LearningPanel.TEACH+com.bunnidogs.mod.genetics.Learning.Routine.values().length && dog!=null) {
                if (dog.learning().begin(player,com.bunnidogs.mod.genetics.Learning.Routine.values()[id-LearningPanel.TEACH]) && player.containerMenu==this)
                    player.closeContainer();
            }
            else if (id==LearningPanel.STOP && dog!=null && dog.learning().authorized(player)) dog.learning().cancel();
            else if (id==CombatPanel.OPEN) navigate(Mode.COMBAT);
            else if (id==PartyPanel.OPEN) navigate(Mode.PARTY);
            else if (CarePanels.backAction(id)) {
                if (mode==Mode.MEASURE && readingPage>=0) readingPage=-1; else back();
            }
            else if (mode==Mode.MEASURE && CareReadingPanel.action(id)) readingPage=id-CareReadingPanel.FIRST;
            else if (mode==Mode.PARTY) PartyPanel.act(player,id);
            else if (id==CarePanels.WORK_MODES) navigate(Mode.TROUGH_WORK);
            else if (id==CarePanels.REQUESTS) navigate(Mode.REQUESTS);
            else if (id==FamilyPanels.COMPARE && dog!=null) navigate(fitGoal==null ? Mode.FIT_CHOICES : Mode.FIT);
            else if (id==FamilyPanels.CHOOSE_GOAL && dog!=null) navigate(Mode.FIT_CHOICES);
            else if (id>=FamilyPanels.SELECT_GOAL && id<FamilyPanels.SELECT_GOAL+Commissions.Request.values().length && dog!=null) {
                fitGoal=Commissions.Request.values()[id-FamilyPanels.SELECT_GOAL]; mode=Mode.FIT;
                if (history.peek()==Mode.FIT) history.pop();
            }
            else if (id==FamilyPanels.FAMILY && station instanceof GeneticsTableBlockEntity) { navigate(Mode.FAMILY); familyPage=0; }
            else if (id==FamilyPanels.SEED_FAMILY && station instanceof GeneticsTableBlockEntity) { navigate(Mode.SEED_FAMILY); familyPage=0; }
            else if (id==LitterPanel.LITTER && station instanceof GeneticsTableBlockEntity) navigate(Mode.LITTER);
            else if (id==LitterPanel.READ_SEED && player instanceof ServerPlayer server) {
                var seed=station instanceof NurseryCradleBlockEntity cradle ? cradle.seed()
                        : station instanceof GeneticsTableBlockEntity ? com.bunnidogs.mod.item.SeedItem.read(LitterPanel.nextSeed(owner)) : Optional.<com.bunnidogs.mod.genetics.Seed>empty();
                if (seed.isPresent()) { player.closeContainer(); com.bunnidogs.mod.network.ModNetwork.sendSeed(server,seed.orElseThrow()); }
            }
            else if ((id==FamilyPanels.PREV || id==FamilyPanels.NEXT) && station instanceof GeneticsTableBlockEntity table) {
                familyPage=FamilyPanels.page(FamilyPanels.relatives(owner,pos,table,mode==Mode.SEED_FAMILY),familyPage)+(id==FamilyPanels.NEXT ? 1 : -1);
            }
            else if (id==FamilyPanels.SHOW_FAMILY && station instanceof GeneticsTableBlockEntity table) {
                var shown=FamilyPanels.visible(FamilyPanels.relatives(owner,pos,table,mode==Mode.SEED_FAMILY),familyPage);
                player.closeContainer(); FamilyPanels.show(player,shown);
            }
            else if (id>=FamilyPanels.SELECT && id<FamilyPanels.SELECT+FamilyPanels.PAGE_SIZE && station instanceof GeneticsTableBlockEntity table) {
                var shown=FamilyPanels.visible(FamilyPanels.relatives(owner,pos,table,mode==Mode.SEED_FAMILY),familyPage);
                if (id-FamilyPanels.SELECT<shown.size() && table.goal().isPresent() && FamilyPanels.hasCalipers(player))
                    openAnimal(player,shown.get(id-FamilyPanels.SELECT),table.goal().orElseThrow());
            }
            else if (id==WorkshopPanels.GOALS && station instanceof GeneticsTableBlockEntity) navigate(Mode.GOALS);
            else if (id==com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD) { player.closeContainer(); com.bunnidogs.mod.entity.StationMarkers.show(player,pos); }
            else if (block instanceof ApexCrucibleBlock) {
                if (id==ApexPanel.PREPARE) navigate(Mode.CONFIRM_RITUAL);
                else if (id==ApexPanel.SHOW_RING) {
                    player.closeContainer();
                    ((ApexCrucibleBlock)block).showRing(player,pos);
                } else if (id==ApexPanel.SUMMON && mode==Mode.CONFIRM_RITUAL) {
                    back();
                    var result=com.bunnidogs.mod.entity.ApexRitual.summon(player,pos,ApexPanel.sample(owner,preferredSample));
                    if (result==com.bunnidogs.mod.entity.ApexRitual.Status.SUMMONED) {
                        player.closeContainer(); player.sendOverlayMessage(Component.translatable("message.bunnidogs.apex.summoned"));
                    } else { actionNotice=Component.translatable("care.bunnidogs.apex.next."+result.name().toLowerCase(java.util.Locale.ROOT)); noticeUntil=level.getGameTime()+60; }
                }
            }
            else if (dog!=null) {
                boolean completed=CarePanels.animalAction(player,dog,id);
                if (completed && CarePanels.endsConversation(id) && player.containerMenu==this) player.closeContainer();
            }
            else {
                CarePanels.act(player,null,pos,station,id);
                if (station instanceof GeneticsTableBlockEntity && (id==WorkshopPanels.CLEAR_GOAL || id>=WorkshopPanels.GOAL && id<WorkshopPanels.GOAL+com.bunnidogs.mod.genetics.Commissions.Request.values().length)) back();
                else if (mode==Mode.TROUGH_WORK && id>=CarePanels.MODE && id<CarePanels.MODE+com.bunnidogs.mod.genetics.TroughWork.values().length
                        || mode==Mode.REQUESTS && id>=CarePanels.REQUEST && id<CarePanels.REQUEST+Commissions.Request.values().length) back();
            }
        }
        finally { acting=false; refresh=0; changedUntil=0; }
        if (player.containerMenu==this) publish(true);
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if (player!=owner || !stillValid(player) || !inventoryVisible() || index<0 || index>=slots.size()) return ItemStack.EMPTY;
        var slot=slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        var stack=slot.getItem(); var original=stack.copy();
        boolean moved=index<inventoryKind.slots ? moveItemStackTo(stack,inventoryKind.slots,slots.size(),true)
                : moveItemStackTo(stack,0,inventoryKind.slots,false);
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player,stack); return original;
    }
}
