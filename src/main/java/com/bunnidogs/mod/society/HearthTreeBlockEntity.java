package com.bunnidogs.mod.society;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.AABB;

/** Owns real supplies and durable evidence of completed resident work, not a population/elapsed-time unlock. */
public final class HearthTreeBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
    private UUID site=UUID.randomUUID(),owner;
    private final List<BlockPos> sites=new ArrayList<>();
    private int harvested,hauled,meals,timber,frames,built,nurtured,born,continuity,tending,families;
    private boolean authorized,busy,stageUpdating;
    private final HouseholdJobs jobs=new HouseholdJobs(this);
    public HouseholdJobs jobs() {return jobs;}
    private final HearthConstruction construction=new HearthConstruction(this);
    public HearthConstruction construction() {return construction;}
    private final HearthAgenda agenda=new HearthAgenda(this);
    public HearthAgenda agenda() {return agenda;}
    private final WildHearth wild=new WildHearth(this);
    public WildHearth wild() {return wild;}
    void wildOwner() {owner=UUID.nameUUIDFromBytes((site+":community").getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    void authorize() {authorized=true;setChanged();}
    public HearthTreeBlockEntity(BlockPos p,BlockState s) {super(ModBlocks.HEARTH_TREE_BE.get(),p,s);}
    public UUID site() {return site;} public UUID owner() {return owner;}
    public void found(Player p) {if(owner==null) {owner=p.getUUID();setChanged();}}
    public boolean permitted(Player p) {return p.mayBuild() && !p.isSpectator() && p.level()==level && (owner==null || owner.equals(p.getUUID())) && level.mayInteract(p,worldPosition);}
    public boolean attached() {return level instanceof ServerLevel && !isRemoved() && level.hasChunkAt(worldPosition) && level.getBlockEntity(worldPosition)==this;}
    public boolean authorized() {return authorized;}
    public List<BlockPos> sites() {return List.copyOf(sites);}
    public List<Bunnidog> residents() {return !attached() ? List.of() : level.getEntitiesOfClass(Bunnidog.class,new AABB(worldPosition).inflate(24),d->d.isAlive() && d.society().belongs(this));}
    public boolean link(BlockPos pos) {
        if(!attached() || pos.distSqr(worldPosition)>24*24 || !level.hasChunkAt(pos))return false;
        if(level.getBlockState(pos).getBlock() instanceof CropBlock)pos=pos.below();
        if(pos.distSqr(worldPosition)>24*24)return false;
        var b=level.getBlockEntity(pos);if(!(b instanceof FeedingTroughBlockEntity || b instanceof NurseryCradleBlockEntity) && !level.getBlockState(pos).is(Blocks.COMPOSTER) && !level.getBlockState(pos).is(Blocks.FARMLAND))return false;
        if(!sites.contains(pos) && sites.size()>=12)return false;if(!sites.contains(pos))sites.add(pos.immutable());if(b instanceof FeedingTroughBlockEntity t && !t.getBlockState().getValue(FeedingTroughBlock.TENDING))level.setBlock(pos,t.getBlockState().setValue(FeedingTroughBlock.TENDING,true),3);setChanged();return true;
    }
    public int count(Item item) {int n=0;for(var s:items)if(s.is(item))n+=s.getCount();return n;}
    public boolean room(ItemStack add) {int free=0;for(var s:items)if(s.isEmpty())free+=add.getMaxStackSize();else if(ItemStack.isSameItemSameComponents(s,add))free+=s.getMaxStackSize()-s.getCount();return free>=add.getCount();}
    public boolean put(ItemStack add) {if(!attached() || !room(add))return false;ItemStack left;
        left=add.copy();for(var s:items)if(!s.isEmpty() && ItemStack.isSameItemSameComponents(s,left)) {int take=Math.min(left.getCount(),s.getMaxStackSize()-s.getCount());s.grow(take);left.shrink(take);}
        for(int i=0;i<items.size() && !left.isEmpty();i++)if(items.get(i).isEmpty()) {int take=Math.min(left.getCount(),left.getMaxStackSize());items.set(i,left.split(take));}setChanged();return true;
    }
    public ItemStack take(Item item,int n) {if(!attached() || count(item)<n)return ItemStack.EMPTY;var result=new ItemStack(item,n);for(var s:items)if(s.is(item)) {int take=Math.min(n,s.getCount());s.shrink(take);n-=take;if(n==0)break;}setChanged();return result;}
    public ItemStack takeExact(int index,int n) {if(!attached() || index<0 || index>=27 || items.get(index).getCount()<n)return ItemStack.EMPTY;var result=items.get(index).split(n);setChanged();return result;}
    public void evidence(String kind,int n) {
        if(!attached() || n<=0)return;
        switch(kind) {case "harvest"->harvested=Math.min(32,harvested+n);case "haul"->hauled=Math.min(16,hauled+n);case "meal"->meals=Math.min(8,meals+n);case "timber"->timber=Math.min(8,timber+n);case "frame"->frames=Math.min(4,frames+n);case "build"->built=Math.min(100,built+n);case "nurture"->nurtured=Math.min(7,nurtured+n);case "birth"->born=1;case "court"->families=1;case "continuity"->continuity=1;default->{return;}}setChanged();updateStage();
    }
    public int progress() {return timber>=2 && frames>=1 ? harvested>=16 && hauled>=8 && meals>=4 ? shelterComplete() && built>=8 && nurtured>=2 && born>0 && families>0 ? continuity>0 ? 4 : 3 : 2 : 1 : 0;}
    @Override public void setChanged() {super.setChanged();if(agenda!=null)agenda.invalidate();if(attached())HearthSites.register(this);if(!stageUpdating && attached()) {stageUpdating=true;try {updateStage();}finally {stageUpdating=false;}}}
    private void updateStage() {if(!attached())return;int stage=progress();if(count(ModItems.LIVING_KEYSTONE.get())==0 && stage==4)stage=3;if(getBlockState().getValue(HearthTreeBlock.STAGE)!=stage)level.setBlock(worldPosition,getBlockState().setValue(HearthTreeBlock.STAGE,stage),3);}
    public Component need() {String need= !authorized ? "plot" : timber<2 ? "orchard" : frames<1 ? "craft" : harvested<16 ? "harvest" : hauled<8 ? "haul" : meals<4 ? "meals" : !shelterComplete() || built<8 ? "shelter" : nurtured<2 || born==0 || families==0 ? "family" : continuity==0 ? "teaching" : "fruit";return Component.translatable("message.bunnidogs.hearth.need."+need);}
    public boolean ripe() {return progress()==4 && count(ModItems.LIVING_KEYSTONE.get())==0;}
    public boolean tend() {
        if(!attached() || !ripe() || busy || count(ModItems.GUAVA.get())<16 || count(Items.BONE_MEAL)<2 || count(ModItems.GRAFTING_FRAME.get())<1 || !room(new ItemStack(ModItems.LIVING_KEYSTONE.get())))return false;
        busy=true;try {take(ModItems.GUAVA.get(),8);take(Items.BONE_MEAL,2);take(ModItems.GRAFTING_FRAME.get(),1);if(++tending>=4) {var key=new ItemStack(ModItems.LIVING_KEYSTONE.get());key.set(ModItems.HEARTH_SITE.get(),site.toString());put(key);tending=0;updateStage();}setChanged();return true;}finally {busy=false;}
    }
    public record Cell(BlockPos pos,BlockState state,Item item) {}
    public List<Cell> shelter() {return construction.base();}
    public List<BlockPos> orchard() {return List.of(worldPosition.offset(-6,0,-4),worldPosition.offset(-3,0,-4),worldPosition.offset(3,0,-4),worldPosition.offset(6,0,-4));}
    public boolean plotSafe() {return construction.fits(0,2) && orchardSafe();}
    public boolean orchardSafe() {
        if(!attached())return false;
        for(var p:orchard())if(!level.hasChunksAt(p.below(),p.above()) || !level.getWorldBorder().isWithinBounds(p) || !level.getBlockState(p).isAir() && !level.getBlockState(p).is(ModBlocks.GUAVA_COPPICE.get()) || !ModBlocks.GUAVA_COPPICE.get().defaultBlockState().canSurvive(level,p))return false;
        return true;
    }
    public boolean shelterComplete() {if(!attached())return false;for(var cell:shelter())if(!level.hasChunkAt(cell.pos()) || !level.getBlockState(cell.pos()).equals(cell.state()))return false;return true;}
    public void survey(Player p) {
        if(!permitted(p) || !attached())return;
        if(authorized)construction.survey(p);
        else if(!construction.chooseBase(p))p.sendOverlayMessage(Component.translatable("message.bunnidogs.hearth.plot_blocked"));
    }
    @Override public void onLoad() {super.onLoad();if(attached())HearthSites.register(this);}
    @Override public int getContainerSize() {return 27;}
    @Override protected NonNullList<ItemStack> getItems() {return items;}
    @Override protected void setItems(NonNullList<ItemStack> value) {items=value;}
    @Override protected Component getDefaultName() {return Component.translatable("block.bunnidogs.hearth_tree");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inv) {return ChestMenu.threeRows(id,inv,this);}
    @Override public boolean stillValid(Player p) {return permitted(p) && super.stillValid(p);}
    @Override protected void saveAdditional(ValueOutput o) {super.saveAdditional(o);ContainerHelper.saveAllItems(o,items);o.putString("Site",site.toString());if(owner!=null)o.putString("Owner",owner.toString());o.store("Sites",BlockPos.CODEC.listOf(),sites);o.putBoolean("Authorized",authorized);o.putIntArray("Evidence",new int[]{harvested,hauled,meals,timber,frames,built,nurtured,born,continuity,tending,families});construction.save(o);agenda.save(o);wild.save(o);}
    @Override protected void loadAdditional(ValueInput i) {super.loadAdditional(i);items=NonNullList.withSize(27,ItemStack.EMPTY);ContainerHelper.loadAllItems(i,items);try {site=UUID.fromString(i.getStringOr("Site",site.toString()));String id=i.getStringOr("Owner","");owner=id.isEmpty() ? null : UUID.fromString(id);}catch(IllegalArgumentException ignored) {}sites.clear();sites.addAll(i.read("Sites",BlockPos.CODEC.listOf()).orElse(List.of()).stream().limit(12).toList());authorized=i.getBooleanOr("Authorized",false);int[] a=i.getIntArray("Evidence").orElse(new int[0]);if(a.length>=10) {harvested=Math.clamp(a[0],0,32);hauled=Math.clamp(a[1],0,16);meals=Math.clamp(a[2],0,8);timber=Math.clamp(a[3],0,8);frames=Math.clamp(a[4],0,4);built=Math.clamp(a[5],0,100);nurtured=Math.clamp(a[6],0,7);born=Math.clamp(a[7],0,1);continuity=Math.clamp(a[8],0,1);tending=Math.clamp(a[9],0,3);families=a.length>10 ? Math.clamp(a[10],0,1) : 0;}construction.load(i,authorized);agenda.load(i);wild.load(i);}
}
