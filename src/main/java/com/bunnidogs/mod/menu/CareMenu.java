package com.bunnidogs.mod.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.entity.Bunnidog;
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
    private boolean cargo;
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
        super(ModMenus.CARE.get(),id); this.owner=inventory.player; this.level=owner.level();
        this.dog=dog; this.pos=pos==null ? null : pos.immutable();
        this.block=pos==null ? null : level.getBlockState(pos).getBlock();
        this.station=pos==null ? null : level.getBlockEntity(pos);
    }
    public static void open(Player player,Bunnidog dog) {
        if (player instanceof ServerPlayer server && !player.isSpectator() && dog.isAlive() && dog.acceptsCare() && dog.level()==player.level() && dog.distanceToSqr(player)<=36) {
            com.bunnidogs.mod.item.JourneyProgress.met(player);
            server.openMenu(new SimpleMenuProvider((id,inv,p)->new CareMenu(id,inv,dog,null),dog.getDisplayName()));
        }
    }
    public static void open(Player player,BlockPos pos) {
        if (player instanceof ServerPlayer server && !player.isSpectator() && server.level().hasChunkAt(pos) && supported(server.level().getBlockState(pos).getBlock())
                && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36) {
            com.bunnidogs.mod.item.JourneyProgress.met(player);
            server.openMenu(new SimpleMenuProvider((id,inv,p)->new CareMenu(id,inv,null,pos),Component.translatable("screen.bunnidogs.care.title")));
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
        if (dog!=null) return dog.isAlive() && !dog.isRemoved() && dog.acceptsCare() && dog.level()==level && dog.distanceToSqr(player)<=36;
        return pos!=null && level.hasChunkAt(pos) && level.getBlockState(pos).is(block) && level.getBlockEntity(pos)==station
                && player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36;
    }
    private CareView snapshot() { return dog!=null ? cargo ? CarePanels.cargo(owner,dog) : CarePanels.animal(owner,dog) : CarePanels.station(owner,pos,station); }
    private static boolean supported(Block block) {
        return block instanceof NestBlock || block instanceof FeedingTroughBlock || block instanceof BreedingPenBlock
                || block instanceof NurseryCradleBlock || block instanceof JuicerBlock || block instanceof CommissionBoardBlock;
    }
    /** Bind an action to the actual shared stock/selection the player saw, not to a timer tick. */
    private Context context() {
        var facts=new ArrayList<Object>(); var stock=new ArrayList<ItemStack>();
        if (dog!=null) {
            facts.add(dog.workPaused()); facts.add(Optional.ofNullable(dog.trail().keeper())); facts.add(dog.trail().order()); facts.add(dog.trail().provisions());
            facts.add(Optional.ofNullable(dog.homeNest()));
            stock.addAll(dog.carriedBundles());
        } else {
            facts.add(level.getBlockState(pos));
            if (station instanceof FeedingTroughBlockEntity trough) { facts.add(trough.workMode()); stock.add(trough.food()); stock.add(trough.filter()); }
            else if (station instanceof BreedingPenBlockEntity pen) {
                stock.add(pen.food(0)); stock.add(pen.food(1));
                for (var parent:com.bunnidogs.mod.entity.PenBreeding.inspect((net.minecraft.server.level.ServerLevel)level,pos,
                        pen.getBlockState().getValue(BreedingPenBlock.FACING)).parents()) facts.add(parent.getUUID());
            } else if (station instanceof NurseryCradleBlockEntity cradle) { stock.add(cradle.storedSeed()); facts.add(cradle.manualHatchOnly()); }
            else if (station instanceof JuicerBlockEntity juicer) for (int i=0;i<3;i++) stock.add(juicer.item(i));
            else if (station instanceof CommissionBoardBlockEntity board) {
                facts.add(board.request()); facts.add(Optional.ofNullable(board.read().candidate()).map(Bunnidog::getUUID));
            }
        }
        return new Context(facts,stock);
    }
    private void publish(boolean force) {
        if (!(owner instanceof ServerPlayer server) || !stillValid(owner)) return;
        var currentContext=context();
        if (!currentContext.same(published)) { generation=generation==0x7FFFFF ? 1 : generation+1; published=currentContext; }
        var base=snapshot();
        var current=new CareView(base.title(),level.getGameTime()<changedUntil ? Component.translatable("care.bunnidogs.feedback.changed") : base.next(),
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
        try {
            if (id==CarePanels.CARGO) cargo=true;
            else if (id==CarePanels.BACK) cargo=false;
            else CarePanels.act(player,dog,pos,station,id);
        }
        finally { acting=false; refresh=0; changedUntil=0; }
        if (player.containerMenu==this) publish(true);
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int slot) { return ItemStack.EMPTY; }
}
