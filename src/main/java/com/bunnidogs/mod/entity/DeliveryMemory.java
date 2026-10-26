package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Learned places and owned cargo are personal experience, never genotype or virtual community stock. */
public final class DeliveryMemory {
    public record Route(DeliveryStores.Stop source,DeliveryStores.Stop target,ItemStack filter,int batch,UUID owner) {
        public static final Codec<Route> CODEC=RecordCodecBuilder.create(i->i.group(DeliveryStores.Stop.CODEC.fieldOf("source").forGetter(Route::source),DeliveryStores.Stop.CODEC.fieldOf("target").forGetter(Route::target),ItemStack.CODEC.fieldOf("filter").forGetter(Route::filter),Codec.intRange(1,8).fieldOf("batch").forGetter(Route::batch),net.minecraft.core.UUIDUtil.STRING_CODEC.fieldOf("owner").forGetter(Route::owner)).apply(i,Route::new));
        public Route {filter=filter.copyWithCount(1);batch=Math.min(batch,filter.getMaxStackSize());}
        @Override public ItemStack filter() {return filter.copy();}
    }
    public enum State { NONE, WATCHING, EMPTY_SOURCE, FULL_TARGET, CHANGED_STORE, PAUSED_STORE, GUIDANCE, PAUSED, UNAVAILABLE, UNLOADED, RESTING, WALKING, CARRYING, READY, BLOCKED }
    private final Bunnidog dog;
    private Route route;
    private ItemStack cargo=ItemStack.EMPTY;
    private boolean busy,paused;
    private boolean proven;
    public boolean proven() {return proven;}
    public static boolean sameProcedure(Route a,Route b) {return a!=null && b!=null && a.owner.equals(b.owner) && a.source.equals(b.source) && a.target.equals(b.target) && ItemStack.isSameItemSameComponents(a.filter,b.filter);}
    public static boolean sameRoute(Route a,Route b) {return sameProcedure(a,b) && a.batch==b.batch;}
    boolean learnHousehold(Route learned) {
        if(route!=null || busy || !cargo.isEmpty() || watching())return false;
        route=new Route(learned.source,learned.target,learned.filter,learned.batch,learned.owner);proven=false;paused=false;activity=State.READY;dog.setPersistenceRequired();return true;
    }
    private long rest;
    private State activity=State.NONE,reported=State.NONE;
    private UUID teacher;
    private long until;
    private BlockPos clicked;
    private long clickedAt;
    private Snapshot watching;
    private DeliveryStores.Stop pickup;
    private ItemStack shown=ItemStack.EMPTY;
    private int shownCount;
    private record Snapshot(AbstractContainerMenu menu,DeliveryStores.Live store,List<ItemStack> before,List<ItemStack> player) {}
    DeliveryMemory(Bunnidog dog) {this.dog=dog;}
    public Route route() {return route;}
    public ItemStack cargo() {return cargo.copy();}
    public boolean watching() {return teacher!=null && dog.level().getGameTime()<until;}
    public UUID teacher() {return teacher;}
    public boolean paused() {return paused;}
    public State state() {
        if(watching())return State.WATCHING;if(route==null)return State.NONE;if(paused)return State.PAUSED;if(!readyBody())return State.UNAVAILABLE;if(!capable())return State.GUIDANCE;
        if(!(dog.level() instanceof ServerLevel level) || cargo.isEmpty() && !route.source.at().dimension().equals(level.dimension()) || !route.target.at().dimension().equals(level.dimension()) || cargo.isEmpty() && !level.hasChunksAt(route.source.at().pos().offset(-1,0,-1),route.source.at().pos().offset(1,0,1)) || !level.hasChunksAt(route.target.at().pos().offset(-1,0,-1),route.target.at().pos().offset(1,0,1)))return State.UNLOADED;
        if(cargo.isEmpty() && endpoint(true)==null || endpoint(false)==null)return State.CHANGED_STORE;if(cargo.isEmpty() && !allowed(endpoint(true)) || !allowed(endpoint(false)))return State.PAUSED_STORE;
        if(dog.level().getGameTime()<rest)return State.RESTING;return activity;
    }
    void walking() {activity=cargo.isEmpty() ? State.WALKING : State.CARRYING;}
    private void say(Player p,String key) {p.sendOverlayMessage(Component.translatable("message.bunnidogs.delivery."+key,dog.getDisplayName()));}
    public boolean authorized(Player p) {return dog.learning().authorized(p) && (route==null || route.owner.equals(p.getUUID()) || dog.trail().belongsTo(p));}
    public boolean canObserve(Player p) {return authorized(p) && cargo.isEmpty() && dog.society().cargoEmpty() && !busy && readyBody() && p.distanceToSqr(dog)<=36 && p.hasLineOfSight(dog) && CapabilityRules.potential(dog.mind(),CapabilityRules.Task.DELIVERY)!=CapabilityRules.Potential.LIMITED && Supplying.qualified(dog.stats());}
    public boolean begin(Player p) {
        if(!canObserve(p) || dog.level().isClientSide())return false;
        dog.learning().cancel();dog.courierLore().cancel();teacher=p.getUUID();until=dog.level().getGameTime()+2400;clicked=null;watching=null;pickup=null;shown=ItemStack.EMPTY;shownCount=0;dog.setPersistenceRequired();say(p,"watching");return true;
    }
    public void stopWatching() {teacher=null;watching=null;clicked=null;pickup=null;shown=ItemStack.EMPTY;until=0;}
    public boolean setPaused(Player p,boolean value) {if(!authorized(p) || route==null || busy)return false;paused=value;return true;}
    public boolean clear(Player p) {
        if(!authorized(p) || busy || !cargo.isEmpty())return false;route=null;proven=false;paused=false;activity=State.NONE;stopWatching();return true;
    }
    boolean readyBody() {return dog.acceptsCare() && dog.isAlive() && !dog.isBaby() && !dog.homeNight() && !dog.workPaused() && !dog.trail().ordered() && !dog.beingTransported() && !dog.isVehicle() && !dog.combat().danger() && !dog.combat().downed() && dog.getTarget()==null && !dog.seekingRefuge() && !dog.isInLove() && !dog.isOnFire() && !dog.isInWater() && dog.getHealth()>=dog.getMaxHealth()*.8F;}
    public boolean observes(Player p) {return watching() && teacher.equals(p.getUUID()) && p.level()==dog.level() && p.isAlive() && authorized(p) && readyBody() && dog.distanceToSqr(p)<=144 && dog.getSensing().hasLineOfSight(p);}
    void click(Player p,BlockPos pos) {if(observes(p)) {clicked=pos.immutable();clickedAt=dog.level().getGameTime();}}
    void opened(ServerPlayer p,AbstractContainerMenu menu) {
        watching=null;if(clicked==null || clickedAt!=dog.level().getGameTime() || !observes(p))return;
        var live=DeliveryStores.observe(p.level(),clicked,p,menu);if(live!=null && sees(clicked))watching=new Snapshot(menu,live,items(live.items()),playerItems(p,menu));clicked=null;
    }
    void closed(ServerPlayer p,AbstractContainerMenu menu) {
        var seen=watching;watching=null;if(seen==null || seen.menu!=menu || !observes(p) || !seen.store.current(p.level()) || dog.distanceToSqr(Vec3.atCenterOf(seen.store.stop().at().pos()))>144 || !sees(seen.store.stop().at().pos()))return;
        var after=items(seen.store.items());var playerAfter=playerItems(p,menu);
        if(pickup==null) {
            var removed=singleChange(seen.before,after,false);if(removed.isEmpty() || removed.getCount()>64 || count(playerAfter,removed)-count(seen.player,removed)!=removed.getCount())return;
            pickup=seen.store.stop();shown=removed.copyWithCount(1);shownCount=Math.min(8,removed.getCount());say(p,"pickup_seen");return;
        }
        var added=singleChange(seen.before,after,true);
        if(added.isEmpty() || !ItemStack.isSameItemSameComponents(added,shown) || count(seen.player,shown)-count(playerAfter,shown)!=added.getCount() || pickup.overlaps(seen.store.stop()) || !pickup.at().dimension().equals(seen.store.stop().at().dimension()) || pickup.at().pos().distSqr(seen.store.stop().at().pos())>32*32)return;
        var source=DeliveryStores.resolve(p.level(),pickup);
        if(source==null || !DeliveryStores.allowed(p.level(),source,p.getUUID()) || !DeliveryStores.allowed(p.level(),seen.store,p.getUUID()))return;
        route=new Route(pickup,seen.store.stop(),shown,Math.min(shownCount,added.getCount()),p.getUUID());proven=false;paused=false;activity=State.READY;dog.learning().deliveryPractice();say(p,"learned");stopWatching();
    }
    private boolean sees(BlockPos at) {
        var from=dog.blockPosition();if(!dog.level().hasChunksAt(new BlockPos(Math.min(from.getX(),at.getX()),from.getY(),Math.min(from.getZ(),at.getZ())),new BlockPos(Math.max(from.getX(),at.getX()),from.getY(),Math.max(from.getZ(),at.getZ()))))return false;
        var hit=dog.level().clip(new net.minecraft.world.level.ClipContext(dog.getEyePosition(),Vec3.atCenterOf(at),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,dog));return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS || hit.getBlockPos().equals(at);
    }
    private static List<ItemStack> items(net.neoforged.neoforge.transfer.ResourceHandler<ItemResource> items) {var out=new ArrayList<ItemStack>();for(int i=0;i<items.size();i++)if(!items.getResource(i).isEmpty())out.add(items.getResource(i).toStack(items.getAmountAsInt(i)));return out;}
    private static List<ItemStack> playerItems(Player p,AbstractContainerMenu menu) {var out=new ArrayList<ItemStack>();for(int i=0;i<p.getInventory().getContainerSize();i++)if(!p.getInventory().getItem(i).isEmpty())out.add(p.getInventory().getItem(i).copy());if(!menu.getCarried().isEmpty())out.add(menu.getCarried().copy());return out;}
    private static int count(List<ItemStack> items,ItemStack key) {int n=0;for(var s:items)if(ItemStack.isSameItemSameComponents(s,key))n+=s.getCount();return n;}
    private static ItemStack singleChange(List<ItemStack> before,List<ItemStack> after,boolean added) {
        var keys=new ArrayList<ItemStack>();for(var s:before)if(keys.stream().noneMatch(k->ItemStack.isSameItemSameComponents(k,s)))keys.add(s);for(var s:after)if(keys.stream().noneMatch(k->ItemStack.isSameItemSameComponents(k,s)))keys.add(s);
        ItemStack result=ItemStack.EMPTY;for(var key:keys) {int delta=count(after,key)-count(before,key);if(delta==0)continue;if(!result.isEmpty() || added && delta<0 || !added && delta>0)return ItemStack.EMPTY;result=key.copyWithCount(Math.abs(delta));}return result;
    }
    boolean guided() {if(route==null || !(dog.level() instanceof ServerLevel l))return false;var p=l.getPlayerByUUID(route.owner);return p!=null && p.isAlive() && authorized(p) && dog.distanceToSqr(p)<=144 && dog.getSensing().hasLineOfSight(p);}
    boolean capable() {return CapabilityRules.potential(dog.mind(),CapabilityRules.Task.DELIVERY)!=CapabilityRules.Potential.LIMITED && Supplying.qualified(dog.stats()) && (dog.learning().knowledge().independent(dog.mind(),Learning.Routine.DELIVERY) || guided() || dog.courierLore().guided(route));}
    boolean canWork() {return route!=null && dog.society().active()==null && !dog.courierLore().occupied() && (dog.trail().keeper()==null || dog.trail().keeper().equals(route.owner)) && !paused && !watching() && readyBody() && capable() && dog.level().getGameTime()>=rest && dog.society().cargoEmpty() && dog.level() instanceof ServerLevel l && l.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_GRIEFING);}
    DeliveryStores.Live endpoint(boolean source) {return route==null || !(dog.level() instanceof ServerLevel l) ? null : DeliveryStores.resolve(l,source ? route.source : route.target);}
    boolean allowed(DeliveryStores.Live live) {return route!=null && dog.level() instanceof ServerLevel l && DeliveryStores.allowed(l,live,route.owner);}
    public boolean unload(Player p) {
        if(!authorized(p) || busy || cargo.isEmpty() || dog.distanceToSqr(p)>36)return false;var held=cargo;cargo=ItemStack.EMPTY;paused=true;if(!p.getInventory().add(held))p.drop(held,false);return true;
    }
    boolean hasShipment() {
        var src=endpoint(true);var dst=endpoint(false);if(src==null || dst==null)return false;
        int n=Math.min(route.batch,count(items(src.items()),route.filter)-(foodReserve(route.filter) ? 8 : 0));if(n<=0) {report(State.EMPTY_SOURCE);return false;}
        try(var tx=Transaction.openRoot()) {if(dst.items().insert(ItemResource.of(route.filter),n,tx)<=0) {report(State.FULL_TARGET);return false;}}return true;
    }
    boolean pickup() {
        if(busy || !cargo.isEmpty() || !canWork())return false;
        var l=(ServerLevel)dog.level();busy=true;
        try {
            if(!net.neoforged.neoforge.event.EventHooks.canEntityGrief(l,dog)) {report(State.PAUSED_STORE);return false;}
            if(!canWork())return false;
            var src=endpoint(true);var dst=endpoint(false);if(!allowed(src) || !allowed(dst))return false;
            var resource=ItemResource.of(route.filter);int available=count(items(src.items()),route.filter);
            int reserve=foodReserve(route.filter) ? 8 : 0;int amount=Math.min(route.batch,available-reserve);
            if(amount<=0) {report(State.EMPTY_SOURCE);return false;}
            try(var preview=Transaction.openRoot()) {amount=dst.items().insert(resource,amount,preview);}
            if(amount<=0) {report(State.FULL_TARGET);return false;}
            try(var tx=Transaction.openRoot()) {
                int n=src.items().extract(resource,amount,tx);
                if(n<=0 || !src.current(l) || !dst.current(l) || !allowed(src) || !allowed(dst) || !canWork())return false;
                var teaching=route;cargo=route.filter.copyWithCount(n);tx.commit();activity=State.CARRYING;CourierLore.pickedUp(dog,teaching,n);return true;
            }
        } finally {busy=false;}
    }
    boolean deposit() {
        if(busy || cargo.isEmpty() || !canWork())return false;
        var l=(ServerLevel)dog.level();busy=true;
        try {
            if(!net.neoforged.neoforge.event.EventHooks.canEntityGrief(l,dog)) {report(State.PAUSED_STORE);return false;}
            if(!canWork())return false;
            var dst=endpoint(false);if(!allowed(dst))return false;
            var original=cargo.copy();boolean independent=dog.learning().knowledge().independent(dog.mind(),Learning.Routine.DELIVERY);
            try(var tx=Transaction.openRoot()) {
                int n=dst.items().insert(ItemResource.of(original),original.getCount(),tx);
                if(n<=0) {report(State.FULL_TARGET);return false;}
                if(!dst.current(l) || !allowed(dst) || !ItemStack.matches(cargo,original) || !canWork())return false;
                var teaching=route;cargo.shrink(n);tx.commit();
                if(cargo.isEmpty()) {proven=true;dog.society().completed(Learning.Routine.DELIVERY,independent);dog.learning().deliveryPractice();rest=l.getGameTime()+100;activity=State.RESTING;reported=State.NONE;}
                CourierLore.delivered(dog,teaching,n);
                return true;
            }
        } finally {busy=false;}
    }
    private static boolean foodReserve(ItemStack s) {return com.bunnidogs.mod.block.FeedingTroughBlockEntity.accepts(s);}
    void report(State next) {activity=next;if(reported==next || route==null || !(dog.level() instanceof ServerLevel l))return;reported=next;var p=l.getPlayerByUUID(route.owner);if(p!=null && p.distanceToSqr(dog)<=256)say(p,"state."+next.name().toLowerCase(Locale.ROOT));}
    void tick() {if(teacher!=null && (!watching() || !readyBody()))stopWatching();}
    public boolean show(Player p) {if(!authorized(p) || route==null || !(dog.level() instanceof ServerLevel l))return false;for(var stop:List.of(route.source,route.target))if(stop.at().dimension().equals(l.dimension()) && l.hasChunkAt(stop.at().pos())) {var at=stop.at().pos();l.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,at.getX()+.5,at.getY()+1.2,at.getZ()+.5,18,.2,.2,.2,0);}return true;}
    public ItemStack deathCargo() {var out=cargo;cargo=ItemStack.EMPTY;return out;}
    void save(ValueOutput out) {if(route!=null)out.store("DeliveryRoute",Route.CODEC,route);if(!cargo.isEmpty())out.store("DeliveryCargo",ItemStack.CODEC,cargo);out.putBoolean("DeliveryProven",proven);out.putBoolean("DeliveryPaused",paused);out.putInt("DeliveryRest",(int)Math.clamp(rest-dog.level().getGameTime(),0L,100L));}
    void load(ValueInput in) {stopWatching();route=in.read("DeliveryRoute",Route.CODEC).orElse(null);cargo=in.read("DeliveryCargo",ItemStack.CODEC).orElse(ItemStack.EMPTY);proven=route!=null && in.getBooleanOr("DeliveryProven",false);paused=in.getBooleanOr("DeliveryPaused",false);rest=dog.level().getGameTime()+Math.clamp(in.getIntOr("DeliveryRest",0),0,100);busy=false;activity=route==null ? State.NONE : State.READY;reported=State.NONE;}
}
