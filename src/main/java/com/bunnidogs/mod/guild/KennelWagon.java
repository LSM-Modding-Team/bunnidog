package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.entity.Bunnidog;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;

/** Real ground freight following a bonded draught animal, with four native pet passengers. */
public final class KennelWagon extends net.minecraft.world.entity.PathfinderMob implements Container {
    private NonNullList<ItemStack> cargo=NonNullList.withSize(27,ItemStack.EMPTY);
    private UUID owner,puller;
    private boolean dropped;
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> HITCHED=net.minecraft.network.syncher.SynchedEntityData.defineId(KennelWagon.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> HEAVY=net.minecraft.network.syncher.SynchedEntityData.defineId(KennelWagon.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(HITCHED,false);b.define(HEAVY,false);}

    public KennelWagon(EntityType<? extends KennelWagon> type,Level level) {super(type,level);setPersistenceRequired();getNavigation().setCanFloat(false);}
    public static AttributeSupplier.Builder attributes() {return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.MOVEMENT_SPEED,.22).add(Attributes.KNOCKBACK_RESISTANCE,1);}
    public void owner(UUID id) {owner=id;}
    @Override protected void registerGoals() {}
    @Override public boolean isPushable() {return false;}
    @Override protected boolean canAddPassenger(Entity passenger) {
        if(passenger instanceof Player p)return owner!=null && owner.equals(p.getUUID()) && getControllingPassenger()==null;
        return passenger instanceof Bunnidog dog && dog.acceptsCare() && owner!=null && owner.equals(dog.trail().keeper()) && getPassengers().stream().filter(e->e instanceof Bunnidog).count()<4;
    }
    @Override public LivingEntity getControllingPassenger(){return getPassengers().stream().filter(e->e instanceof Player).map(e->(LivingEntity)e).findFirst().orElse(null);}
    @Override protected Vec3 getRiddenInput(Player p,Vec3 input){return entityData.get(HITCHED) && !isInWater() ? new Vec3(p.xxa*.5,0,p.zza>0 ? p.zza : p.zza*.25) : Vec3.ZERO;}
    @Override protected float getRiddenSpeed(Player p){return entityData.get(HEAVY) ? .16F : .23F;}
    @Override protected void tickRidden(Player p,Vec3 input){setYRot(p.getYRot());setYHeadRot(getYRot());yBodyRot=getYRot();}
    @Override protected void positionRider(Entity p,Entity.MoveFunction move) {
        var pets=getPassengers().stream().filter(e->e instanceof Bunnidog).toList();int i=pets.indexOf(p);
        var offset=p instanceof Player ? new Vec3(0,.95,-1.65) : new Vec3(i%2==0 ? -.55 : .55,.65,i<2 ? -.6 : .6);
        offset=offset.yRot((float)(-getYRot()*Math.PI/180));move.accept(p,getX()+offset.x,getY()+offset.y,getZ()+offset.z);
    }
    @Override public InteractionResult mobInteract(Player p,InteractionHand hand) {
        if(level().isClientSide())return InteractionResult.SUCCESS;if(!p.isAlive() || p.isSpectator() || owner==null || !owner.equals(p.getUUID()))return InteractionResult.FAIL;
        if(p.getItemInHand(hand).is(Items.LEAD)) {
            if(puller!=null)return InteractionResult.FAIL;
            var dog=level().getEntitiesOfClass(Bunnidog.class,getBoundingBox().inflate(6),d->d.acceptsCare() && d.isAlive() && !d.isBaby() && owner.equals(d.trail().keeper()) && !d.isPassenger() && !d.combat().downed()
                && com.bunnidogs.mod.genetics.Carrying.capacity(d.genome())>=3).stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if(dog==null)return InteractionResult.FAIL;puller=dog.getUUID();p.getItemInHand(hand).consume(1,p);return InteractionResult.SUCCESS;
        }
        if(p.isShiftKeyDown()) {
            p.openMenu(new SimpleMenuProvider((id,inv,user)->ChestMenu.threeRows(id,inv,this),getDisplayName()));return InteractionResult.SUCCESS;
        }
        if(!p.getItemInHand(hand).is(ModItems.TRAIL_WHISTLE.get())) {p.startRiding(this,true,true);return InteractionResult.SUCCESS;}
        if(getPassengers().stream().anyMatch(e->e instanceof Bunnidog)) {for(var e:java.util.List.copyOf(getPassengers()))if(e instanceof Bunnidog)e.stopRiding();return InteractionResult.SUCCESS;}
        for(var dog:level().getEntitiesOfClass(Bunnidog.class,getBoundingBox().inflate(5),d->d.acceptsCare() && d.isAlive() && !d.isBaby() && owner.equals(d.trail().keeper()) && !d.isPassenger() && !d.combat().downed() && !d.getUUID().equals(puller))) {
            if(getPassengers().stream().filter(e->e instanceof Bunnidog).count()==4)break;dog.startRiding(this,true,true);
        }
        return InteractionResult.SUCCESS;
    }
    @Override public void tick() {
        super.tick();if(!(level() instanceof ServerLevel server) || !isAlive())return;if(puller==null) {entityData.set(HITCHED,false);return;}
        if(!(server.getEntity(puller) instanceof Bunnidog dog) || !dog.isAlive() || dog.isPassenger() || dog.combat().downed() || !owner.equals(dog.trail().keeper()) || distanceToSqr(dog)>32*32) {entityData.set(HITCHED,false);getNavigation().stop();return;}
        double distance=distanceToSqr(dog);entityData.set(HITCHED,distance<64 && !dog.isInWater());
        if(getControllingPassenger() instanceof Player driver) {
            getNavigation().stop();
            if(Math.abs(driver.zza)>.01 && entityData.get(HITCHED)) {
                var ahead=position().add(Vec3.directionFromRotation(0,driver.getYRot()).scale(driver.zza>0 ? 4 : -4));
                if(server.hasChunkAt(BlockPos.containing(ahead)))dog.getNavigation().moveTo(ahead.x,ahead.y,ahead.z,.95);
            }
            return;
        }
        if(distance>10 && distance<32*32 && !isInWater()) {getLookControl().setLookAt(dog,15,15);getNavigation().moveTo(dog,cargo.stream().filter(s->!s.isEmpty()).count()>18 ? .65 : .9);}
        else getNavigation().stop();
    }
    @Override public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.die(source);if(!dead || dropped || !(level() instanceof ServerLevel server))return;dropped=true;
        Containers.dropContents(server,this,this);spawnAtLocation(server,new ItemStack(GuildProjects.material("kennel_wagon")));if(puller!=null)spawnAtLocation(server,new ItemStack(Items.LEAD));
    }
    @Override public int getContainerSize() {return 27;}
    @Override public boolean isEmpty() {return cargo.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int i) {return cargo.get(i);}
    @Override public ItemStack removeItem(int i,int n) {return removed(i,n);}
    private ItemStack removed(int i,int n){var s=ContainerHelper.removeItem(cargo,i,n);setChanged();return s;}
    @Override public ItemStack removeItemNoUpdate(int i) {return ContainerHelper.takeItem(cargo,i);}
    @Override public void setItem(int i,ItemStack s) {cargo.set(i,s);setChanged();}
    @Override public void setChanged() {if(!level().isClientSide() && cargo!=null)entityData.set(HEAVY,cargo.stream().filter(s->!s.isEmpty()).count()>18);}
    @Override public boolean stillValid(Player p) {return isAlive() && owner!=null && owner.equals(p.getUUID()) && distanceToSqr(p)<64;}
    @Override public void clearContent() {cargo.clear();}
    @Override public void addAdditionalSaveData(ValueOutput out) {super.addAdditionalSaveData(out);ContainerHelper.saveAllItems(out,cargo);if(owner!=null)out.putString("WagonOwner",owner.toString());if(puller!=null)out.putString("WagonPuller",puller.toString());}
    @Override public void readAdditionalSaveData(ValueInput in) {super.readAdditionalSaveData(in);cargo=NonNullList.withSize(27,ItemStack.EMPTY);ContainerHelper.loadAllItems(in,cargo);setChanged();try {owner=UUID.fromString(in.getStringOr("WagonOwner",""));}catch(IllegalArgumentException ignored){}try{puller=UUID.fromString(in.getStringOr("WagonPuller",""));}catch(IllegalArgumentException ignored){}}
}
