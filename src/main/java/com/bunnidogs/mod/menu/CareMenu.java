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
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
    private enum Mode { MAIN, CARGO, ROLES, GOALS, CONFIRM_RITUAL, FAMILY, SEED_FAMILY, FIT, FIT_CHOICES, LITTER, COMBAT, PARTY }
    private Mode mode=Mode.MAIN;
    private Commissions.Request fitGoal;
    private int familyPage;
    private final ItemStack preferredSample;
    private Component actionNotice;
    private long noticeUntil;
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
    private CareMenu(int id,Inventory inventory,Bunnidog dog,BlockPos pos) {
        this(id,inventory,dog,pos,null);
    }
    private CareMenu(int id,Inventory inventory,Bunnidog dog,BlockPos pos,ItemStack preferredSample) {
        super(ModMenus.CARE.get(),id); this.owner=inventory.player; this.level=owner.level();
        this.preferredSample=preferredSample;
        this.dog=dog; this.pos=pos==null ? null : pos.immutable();
        this.block=pos==null ? null : level.getBlockState(pos).getBlock();
        this.station=pos==null ? null : level.getBlockEntity(pos);
    }
    public static void open(Player player,Bunnidog dog) {
        openAnimal(player,dog,null);
    }
    private static void openAnimal(Player player,Bunnidog dog,Commissions.Request goal) {
        openAnimal(player,dog,goal,false);
    }
    public static void openCombat(Player player,Bunnidog dog) { openAnimal(player,dog,null,true); }
    public static void openParty(Player player) {
        if (player instanceof ServerPlayer server && !player.isSpectator()) server.openMenu(new SimpleMenuProvider((id,inv,p)->{
            var menu=new CareMenu(id,inv,null,null); menu.mode=Mode.PARTY; return menu;
        },Component.translatable("care.bunnidogs.party.title")));
    }
    private static void openAnimal(Player player,Bunnidog dog,Commissions.Request goal,boolean combat) {
        if (player instanceof ServerPlayer server && !player.isSpectator() && dog.isAlive() && dog.acceptsCare() && dog.level()==player.level() && dog.distanceToSqr(player)<=36) {
            com.bunnidogs.mod.item.JourneyProgress.met(player);
            server.openMenu(new SimpleMenuProvider((id,inv,p)->{
                var menu=new CareMenu(id,inv,dog,null);
                if (goal!=null) { menu.fitGoal=goal; menu.mode=Mode.FIT; }
                if (combat) menu.mode=Mode.COMBAT;
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
            server.openMenu(new SimpleMenuProvider((id,inv,p)->new CareMenu(id,inv,null,pos,preferredSample),Component.translatable("screen.bunnidogs.care.title")));
        }
    }
    public CareView view() { return view; }
    public int buttonId(int action) {
        return view==null ? -1 : view.controls().stream().filter(c->(c.id()&255)==action).mapToInt(CareView.Control::id).findFirst().orElse(-1);
    }
    public void receive(CareView value) { if (level.isClientSide()) view=value; }
    @Override public boolean stillValid(Player player) {
        if (level.isClientSide()) return true;
        if (player!=owner || player.level()!=level || !player.isAlive() || player.isSpectator()) return false;
        if (mode==Mode.PARTY) return true;
        if (dog!=null) return dog.isAlive() && !dog.isRemoved() && dog.acceptsCare() && dog.level()==level && dog.distanceToSqr(player)<=36;
        return pos!=null && level.hasChunkAt(pos) && level.getBlockState(pos).is(block) && level.getBlockEntity(pos)==station
                && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36;
    }
    private CareView snapshot() {
        if (mode==Mode.PARTY) return PartyPanel.view(owner);
        if (dog!=null) return switch (mode) {
            case CARGO -> CarePanels.cargo(owner,dog);
            case ROLES -> RolePanel.view(owner,dog);
            case COMBAT -> CombatPanel.view(owner,dog);
            case FIT -> FamilyPanels.fit(owner,dog,fitGoal);
            case FIT_CHOICES -> FamilyPanels.choices(owner,dog);
            default -> CarePanels.animal(owner,dog);
        };
        if (block instanceof ApexCrucibleBlock) return ApexPanel.view(owner,pos,ApexPanel.sample(owner,preferredSample),mode==Mode.CONFIRM_RITUAL);
        if (station instanceof GeneticsTableBlockEntity table && mode==Mode.GOALS) return WorkshopPanels.goals(owner,pos,table);
        if (station instanceof GeneticsTableBlockEntity table && (mode==Mode.FAMILY || mode==Mode.SEED_FAMILY)) return FamilyPanels.family(owner,pos,table,familyPage,mode==Mode.SEED_FAMILY);
        if (station instanceof GeneticsTableBlockEntity table && mode==Mode.LITTER) return LitterPanel.view(owner,table);
        return CarePanels.station(owner,pos,station);
    }
    private static boolean supported(Block block) {
        return block instanceof NestBlock || block instanceof FeedingTroughBlock || block instanceof BreedingPenBlock
                || block instanceof NurseryCradleBlock || block instanceof JuicerBlock || block instanceof CommissionBoardBlock
                || block instanceof GeneticsTableBlock || block instanceof JudgingStandBlock || block instanceof SpecimenJarBlock
                || block instanceof PedigreeComparatorBlock || block instanceof TrophyPedestalBlock || block instanceof ApexCrucibleBlock;
    }
    /** Bind an action to the actual shared stock/selection the player saw, not to a timer tick. */
    private Context context() {
        var facts=new ArrayList<Object>(); var stock=new ArrayList<ItemStack>();
        if (mode==Mode.PARTY) facts.add(com.bunnidogs.mod.entity.CompanionParty.context(owner));
        else if (dog!=null) {
            facts.add(dog.workPaused()); facts.add(Optional.ofNullable(dog.trail().keeper())); facts.add(dog.trail().order()); facts.add(dog.trail().provisions());
            facts.add(dog.combat().selected()); facts.add(dog.combat().downed()); facts.add(dog.combat().recovering());
            facts.add(dog.combat().partyState());
            facts.add(Optional.ofNullable(dog.homeNest()));
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
                if (table.isEmpty(0) || table.isEmpty(1)) stock.add(WorkshopPanels.nextSample(owner));
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
                stock.add(jar.sample()); if (jar.sample().isEmpty()) stock.add(WorkshopPanels.nextSample(owner));
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
    private void publish(boolean force) {
        if (!(owner instanceof ServerPlayer server) || !stillValid(owner)) return;
        var currentContext=context();
        if (!currentContext.same(published)) {
            generation=generation==0x7FFFFF ? 1 : generation+1; published=currentContext;
            if (mode==Mode.CONFIRM_RITUAL) mode=Mode.MAIN;
        }
        var base=snapshot();
        var current=new CareView(base.title(),level.getGameTime()<changedUntil ? Component.translatable("care.bunnidogs.feedback.changed")
                : actionNotice!=null && level.getGameTime()<noticeUntil ? actionNotice : base.next(),
                base.rows(),base.controls().stream().map(c->new CareView.Control((generation<<8)|c.id(),c.label(),c.enabled(),c.section())).toList());
        if (force || !current.equals(view)) { view=current; PacketDistributor.sendToPlayer(server,new CareViewPayload(containerId,current)); }
    }
    @Override public void broadcastChanges() {
        super.broadcastChanges();
        if (!(owner instanceof ServerPlayer server) || !stillValid(owner) || refresh++%10!=0) return;
        publish(false);
    }
    @Override public boolean clickMenuButton(Player player,int buttonId) {
        int id=buttonId&255;
        if (acting || player.containerMenu!=this || !stillValid(player) || !CarePanels.readOnly(id) && !player.mayBuild()
                || pos!=null && !level.mayInteract(player,pos)) return false;
        if (buttonId<0 || (buttonId>>>8)!=generation || !context().same(published)) {
            changedUntil=level.getGameTime()+60; publish(true); return false;
        }
        if (!snapshot().permits(id)) { publish(true); return false; }
        acting=true;
        actionNotice=null;
        try {
            if (id==CarePanels.CARGO) mode=Mode.CARGO;
            else if (id==CarePanels.ROLES) mode=Mode.ROLES;
            else if (id==CombatPanel.OPEN) mode=Mode.COMBAT;
            else if (id==PartyPanel.OPEN) { openParty(player); }
            else if (mode==Mode.PARTY) PartyPanel.act(player,id);
            else if (id==CarePanels.BACK) mode=Mode.MAIN;
            else if (id==FamilyPanels.COMPARE && dog!=null) mode=fitGoal==null ? Mode.FIT_CHOICES : Mode.FIT;
            else if (id==FamilyPanels.CHOOSE_GOAL && dog!=null) mode=Mode.FIT_CHOICES;
            else if (id==FamilyPanels.BACK_FIT && dog!=null) mode=fitGoal==null ? Mode.ROLES : Mode.FIT;
            else if (id>=FamilyPanels.SELECT_GOAL && id<FamilyPanels.SELECT_GOAL+Commissions.Request.values().length && dog!=null) {
                fitGoal=Commissions.Request.values()[id-FamilyPanels.SELECT_GOAL]; mode=Mode.FIT;
            }
            else if (id==FamilyPanels.FAMILY && station instanceof GeneticsTableBlockEntity) { mode=Mode.FAMILY; familyPage=0; }
            else if (id==FamilyPanels.SEED_FAMILY && station instanceof GeneticsTableBlockEntity) { mode=Mode.SEED_FAMILY; familyPage=0; }
            else if (id==LitterPanel.LITTER && station instanceof GeneticsTableBlockEntity) mode=Mode.LITTER;
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
            else if (id==WorkshopPanels.GOALS && station instanceof GeneticsTableBlockEntity) mode=Mode.GOALS;
            else if (id==WorkshopPanels.BACK_TABLE && station instanceof GeneticsTableBlockEntity) mode=Mode.MAIN;
            else if (id==com.bunnidogs.mod.entity.StationMarkers.SHOW_PAD) { player.closeContainer(); com.bunnidogs.mod.entity.StationMarkers.show(player,pos); }
            else if (block instanceof ApexCrucibleBlock) {
                if (id==ApexPanel.PREPARE) mode=Mode.CONFIRM_RITUAL;
                else if (id==ApexPanel.CANCEL) mode=Mode.MAIN;
                else if (id==ApexPanel.SHOW_RING) {
                    player.closeContainer();
                    ((ApexCrucibleBlock)block).showRing(player,pos);
                } else if (id==ApexPanel.SUMMON && mode==Mode.CONFIRM_RITUAL) {
                    mode=Mode.MAIN;
                    var result=com.bunnidogs.mod.entity.ApexRitual.summon(player,pos,ApexPanel.sample(owner,preferredSample));
                    if (result==com.bunnidogs.mod.entity.ApexRitual.Status.SUMMONED) {
                        player.closeContainer(); player.sendOverlayMessage(Component.translatable("message.bunnidogs.apex.summoned"));
                    } else { actionNotice=Component.translatable("care.bunnidogs.apex.next."+result.name().toLowerCase(java.util.Locale.ROOT)); noticeUntil=level.getGameTime()+60; }
                }
            }
            else {
                CarePanels.act(player,dog,pos,station,id);
                if (station instanceof GeneticsTableBlockEntity && (id==WorkshopPanels.CLEAR_GOAL || id>=WorkshopPanels.GOAL && id<WorkshopPanels.GOAL+com.bunnidogs.mod.genetics.Commissions.Request.values().length)) mode=Mode.MAIN;
            }
        }
        finally { acting=false; refresh=0; changedUntil=0; }
        if (player.containerMenu==this) publish(true);
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int slot) { return ItemStack.EMPTY; }
}
