package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.guild.*;
import com.bunnidogs.mod.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** The station's original 27 saved slots, including actual workpiece and output. */
public final class ProjectMenu extends AbstractContainerMenu {
    public static final int WIDTH=212,HEIGHT=230;
    public static final String[] STATES={"workpiece","barding","repaired","equipment","book","incompatible","working","materials","ready","paused","fuel","active","shelter","wings","occupied"};
    private final Container stock;
    private final ContainerData data;
    private final Player owner;
    public final String kind;
    public ProjectMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer) {
        this(id,inventory,new SimpleContainer(27),buffer.readUtf(64),initialData(buffer));
    }
    private static ContainerData initialData(RegistryFriendlyByteBuf b) {var d=new SimpleContainerData(4);for(int i=0;i<4;i++)d.set(i,b.readVarInt());return d;}
    public ProjectMenu(int id,Inventory inventory,Container stock,String kind,ContainerData data) {
        super(ModMenus.PROJECT.get(),id);this.stock=stock;this.kind=kind;this.data=data;owner=inventory.player;checkContainerSize(stock,27);checkContainerDataCount(data,4);
        stock.startOpen(owner);
        for(int i=0;i<27;i++) {
            final int index=i;int x=26+i%9*18,y=80+i/9*18;
            if(workshop()) {
                if(i==0){x=34;y=24;}
                else if(i==output()){x=154;y=24;}
                else if(binding() && i==1){x=70;y=24;}
                else {int at=i-supplies();x=26+at%9*18;y=80+at/9*18;}
            }
            addSlot(new Slot(stock,i,x,y) {
                @Override public boolean mayPlace(ItemStack stack) {
                    if(workshop() && index==output())return false;
                    if(workshop() && index==0)return binding() ? !stack.is(Items.ENCHANTED_BOOK) && stack.has(net.minecraft.core.component.DataComponents.ENCHANTMENTS) : stack.getItem() instanceof com.bunnidogs.mod.equipment.BardingItem;
                    if(binding() && index==1)return stack.is(Items.ENCHANTED_BOOK);
                    return true;
                }
                @Override public int getMaxStackSize() {return workshop() && index<supplies() ? 1 : super.getMaxStackSize();}
            });
        }
        addStandardInventorySlots(inventory,26,148);addDataSlots(data);
    }
    public boolean binding(){return kind.equals("resonance_bench");}
    public boolean workshop(){return binding() || kind.equals("pack_armoury");}
    public int output(){return binding() ? 2 : 1;}
    public int supplies(){return workshop() ? binding() ? 3 : 2 : 0;}
    public WorkshopJob job(){return WorkshopJob.plan(kind,stock.getItem(0),binding() ? stock.getItem(1) : ItemStack.EMPTY);}
    public String state(){return STATES[Math.clamp(data.get(0),0,STATES.length-1)];}
    public int fuel(int index){return data.get(index);}
    public void writeOpeningData(RegistryFriendlyByteBuf b){b.writeUtf(kind,64);for(int i=0;i<4;i++)b.writeVarInt(data.get(i));}
    @Override public boolean stillValid(Player player){return player==owner && player.isAlive() && !player.isSpectator() && stock.stillValid(player);}
    private boolean usable(Player p){return p==owner && p.containerMenu==this && stillValid(p) && (!(stock instanceof ProjectStation s) || com.bunnidogs.mod.block.StationUse.editable(p,s));}
    @Override public boolean clickMenuButton(Player p,int id) {
        if(id!=0 || !kind.equals("launch_aerie") || !usable(p) || !(stock instanceof ProjectStation station) || !station.launch(p))return false;
        if(p.containerMenu==this)p.closeContainer();return true;
    }
    @Override public void clicked(int slot,int button,ContainerInput input,Player player){if(usable(player))super.clicked(slot,button,input,player);}
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(!usable(player) || index<0 || index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();
        if(index<27) {if(!moveItemStackTo(stack,27,slots.size(),true))return ItemStack.EMPTY;}
        else {
            boolean moved=false;
            if(workshop() && slots.get(0).mayPlace(stack))moved=moveItemStackTo(stack,0,1,false);
            else if(binding() && stack.is(Items.ENCHANTED_BOOK))moved=moveItemStackTo(stack,1,2,false);
            if(!moved && !moveItemStackTo(stack,supplies(),27,false))return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
    @Override public void removed(Player player){super.removed(player);stock.stopOpen(player);}
}
