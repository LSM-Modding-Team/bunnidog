package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

/** Personal membership, actual carried stock and concrete jobs for the first sustainable household. */
public final class SocietyWork {
    private final Bunnidog dog;
    private GlobalPos home;
    private UUID site;
    private boolean child;
    private int workSinceMeal,residentLessons;
    private long nextWork;
    private ItemStack cargo=ItemStack.EMPTY;
    private BlockPos destination;
    private boolean grownCargo;
    private UUID aidPatient,aidOwner;
    private BlockPos aidBed;
    private boolean medicalBusy;
    public enum Kind { MEAL, CRAFT, BUILD, PLANT, PRUNE, HAUL, SUPPLY, DELIVER, COMPOST, COURT, SEED, NURTURE, HATCH, TEND, TEACH, MEDICINE, CARE, RECORD_ROUTE, STUDY_ROUTE }
    public record Patient(UUID id,BlockPos bed) {}
    public record Job(Kind kind,BlockPos target,int index,BlockState expected,Patient patient) {
        public Job(Kind kind,BlockPos target,int index,BlockState expected) {this(kind,target,index,expected,null);}
    }
    SocietyWork(Bunnidog dog) {this.dog=dog;}
    public HearthTreeBlockEntity hearth() {
        var l=dog.level();return home!=null && site!=null && home.dimension().equals(l.dimension()) && l.hasChunkAt(home.pos()) && l.getBlockEntity(home.pos()) instanceof HearthTreeBlockEntity h && site.equals(h.site()) ? h : null;
    }
    public boolean belongs(HearthTreeBlockEntity h) {return home!=null && home.dimension().equals(dog.level().dimension()) && home.pos().equals(h.getBlockPos()) && h.site().equals(site);}
    public boolean authorized(Player p) {var h=hearth();return home==null || h!=null && (h.owner()==null || h.owner().equals(p.getUUID()));}
    public boolean cargoEmpty() {return cargo.isEmpty();}
    public ItemStack medicalSupplies() {return aidPatient==null ? ItemStack.EMPTY : cargo.copy();}
    public boolean child() {return child;}
    public boolean invite(Player player,HearthTreeBlockEntity h,boolean leave) {
        if(medicalBusy || leave && aidPatient!=null && !cargo.isEmpty() || !h.permitted(player) || !dog.deliveries().authorized(player) || !dog.isAlive() || !dog.acceptsCare() || dog.isBaby() || dog.level()!=h.getLevel() || dog.distanceToSqr(Vec3.atCenterOf(h.getBlockPos()))>24*24 || dog.trail().keeper()!=null && !dog.trail().belongsTo(player))return false;
        if(leave) {if(!belongs(h))return false;returnCargo();home=null;site=null;child=false;return true;}
        if(home!=null && !belongs(h) || !belongs(h) && h.residents().size()>=16)return false;
        if(dog.trail().ordered() && !dog.trail().command(player,Trail.Order.RESIDENT))return false;
        home=GlobalPos.of(dog.level().dimension(),h.getBlockPos());site=h.site();dog.setPersistenceRequired();return true;
    }
    public void born(HearthTreeBlockEntity h) {home=GlobalPos.of(dog.level().dimension(),h.getBlockPos());site=h.site();child=true;dog.setPersistenceRequired();}
    private boolean calm() {return dog.acceptsCare() && !dog.homeNight() && dog.isAlive() && !dog.isBaby() && !dog.beingTransported() && !dog.isVehicle() && !dog.trail().ordered() && !dog.workPaused() && !dog.combat().danger() && !dog.combat().downed() && dog.getTarget()==null && !dog.seekingRefuge() && !dog.isInLove() && !dog.isOnFire() && !dog.isInWater() && dog.getHealth()>=dog.getMaxHealth()*.8F;}
    public boolean ready() {var h=hearth();return h!=null && calm() && dog.level().getGameTime()>=nextWork && dog.distanceToSqr(Vec3.atCenterOf(h.getBlockPos()))<24*24;}
    public Bunnidog mentor(Learning.Routine routine) {
        var h=hearth();if(h==null || !calm())return null;
        return h.residents().stream().filter(d->d!=dog && d.distanceToSqr(dog)<=36 && d.society().calm() && d.learning().lesson()==null && d.learning().knowledge().independent(d.mind(),routine) && CapabilityRules.potential(d.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT && d.getSensing().hasLineOfSight(dog)).findFirst().orElse(null);
    }
    public boolean guided(Learning.Routine r) {return CapabilityRules.potential(dog.mind(),r.task())!=CapabilityRules.Potential.LIMITED && mentor(r)!=null;}
    public void completed(Learning.Routine routine,boolean independent) {var h=hearth();if(h!=null && child && independent && (residentLessons&(1<<routine.ordinal()))!=0)h.evidence("continuity",1);}
    public void taught(Learning.Routine routine) {residentLessons|=1<<routine.ordinal();}
    public void harvested(BlockPos trough) {var h=hearth();if(h!=null && h.sites().contains(trough))h.evidence("harvest",1);}
    private boolean knows(Learning.Routine r) {return dog.learning().canWork(r);}
    private Job at(Kind k,BlockPos p) {return new Job(k,p.immutable(),-1,dog.level().getBlockState(p));}
    private boolean gardener() {return Gardening.cultivates(dog.stats()) && knows(Learning.Routine.GARDEN);}
    public Job plan() {
        if(!ready() || dog.courierLore().apprenticing() || dog.deliveries().watching() || !dog.deliveries().cargo().isEmpty())return null;var h=hearth();var l=(ServerLevel)dog.level();var at=h.getBlockPos();
        if(!cargo.isEmpty() && aidPatient!=null) {
            if(!Objects.equals(aidOwner,h.owner()))return null;
            var patient=medicalPatient(new Patient(aidPatient,aidBed));
            if(nurse() && patient!=null)return new Job(Kind.CARE,patient.blockPosition().immutable(),-1,l.getBlockState(patient.blockPosition()),new Patient(aidPatient,aidBed));
            return at(Kind.DELIVER,at);
        }
        if(!cargo.isEmpty())return at(Kind.DELIVER,destination!=null && l.hasChunkAt(destination) && (destination.equals(at) || l.getBlockEntity(destination) instanceof FeedingTroughBlockEntity || l.getBlockEntity(destination) instanceof NurseryCradleBlockEntity) ? destination : at);
        if(workSinceMeal>=6)return h.count(ModItems.GUAVA.get())>0 && dog.canEat(new ItemStack(ModItems.GUAVA.get())) ? at(Kind.MEAL,at) : null;
        boolean grow=knows(Learning.Routine.HARVEST),garden=gardener(),nurse=Gardening.nurses(dog.stats()) && knows(Learning.Routine.NURSERY);
        if(garden && dog.mind().handling()>=1.5 && dog.mind().reasoning()>=1.5 && dressingNeeded(h))return at(Kind.CRAFT,at);
        if(nurse && h.count(ModItems.HEARTH_POULTICE.get())>0 && !l.hasNeighborSignal(at))
            for(var patient:h.residents())if(patient!=dog && patient.clinic().ready() && ClinicCare.restingAtHome(h,patient) && HomeRoutes.visible(dog,patient))
                return new Job(Kind.MEDICINE,at,-1,l.getBlockState(at),new Patient(patient.getUUID(),patient.homeNest()));
        var archive=dog.courierLore().archiveJob(h);if(archive!=null)return archive;
        if(CapabilityRules.potential(dog.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT && workSinceMeal<6)
            for(var pupil:h.residents())if(pupil!=dog && !pupil.isBaby() && pupil.society().calm())for(var r:Learning.Routine.values())
                if(r!=Learning.Routine.SCOUT && dog.learning().knowledge().independent(dog.mind(),r) && pupil.learning().physicallyReady(r) && !pupil.learning().knowledge().knows(r) && CapabilityRules.potential(pupil.mind(),r.task())!=CapabilityRules.Potential.LIMITED)return at(Kind.TEACH,pupil.blockPosition());
        if(nurse)for(var p:h.sites())if(l.hasChunkAt(p) && l.getBlockEntity(p) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly()) {
            if(c.seed().isPresent() && c.age()==SeedCropBlock.MAX_AGE)return at(Kind.HATCH,p);
            if(c.seed().isPresent() && c.age()<SeedCropBlock.MAX_AGE && h.count(ModItems.GUAVA.get())>8)return at(Kind.NURTURE,p);
            if(c.seed().isEmpty())for(int i=0;i<h.getContainerSize();i++)if(communitySeed(h.getItem(i),h))return new Job(Kind.SEED,at,i,l.getBlockState(at));
        }
        if(nurse && canStartFamily(h))return at(Kind.COURT,at);
        boolean craft=garden && dog.mind().handling()>=1.5 && dog.mind().reasoning()>=1.5;
        if(craft && craftNeeded(h))return at(Kind.CRAFT,at);
        if(h.authorized()) {
            if(garden && h.ripe() && h.count(ModItems.GUAVA.get())>=16 && h.count(Items.BONE_MEAL)>=2 && h.count(ModItems.GRAFTING_FRAME.get())>0)return at(Kind.TEND,at);
            if(garden && CapabilityRules.potential(dog.mind(),CapabilityRules.Task.BUILDING)==CapabilityRules.Potential.INDEPENDENT)
                for(var c:h.shelter())if(l.hasChunkAt(c.pos()) && l.getBlockState(c.pos()).isAir() && h.count(c.item())>0)return at(Kind.BUILD,c.pos());
            for(var p:h.orchard())if(l.hasChunkAt(p)) {
                var s=l.getBlockState(p);
                if(s.is(ModBlocks.GUAVA_COPPICE.get()) && s.getValue(CoppiceBlock.AGE)==7 && grow && h.count(Items.WOODEN_AXE)>0 && h.room(new ItemStack(ModItems.GUAVA_LOG.get(),2)) && h.room(new ItemStack(ModItems.GUAVA.get(),4)))return at(Kind.PRUNE,p);
                if(s.isAir() && garden && h.count(ModItems.GUAVA_COPPICE.get())>0 && ModBlocks.GUAVA_COPPICE.get().defaultBlockState().canSurvive(l,p))return at(Kind.PLANT,p);
            }
        }
        if(knows(Learning.Routine.DELIVERY) && Supplying.qualified(dog.stats()))for(var p:h.sites())if(l.hasChunkAt(p) && l.getBlockEntity(p) instanceof FeedingTroughBlockEntity t && t.getBlockState().getValue(FeedingTroughBlock.TENDING) && !t.residentsPaused() && (t.food().isEmpty() || com.bunnidogs.mod.item.GuavaItem.tierOf(t.food())==0)) {
            if((t.food().getCount()>12 || t.food().getCount()>8 && t.food().is(ModItems.GUAVA.get()) && h.count(ModItems.GUAVA.get())<16) && h.count(t.food().getItem())<(t.food().is(ModItems.GUAVA.get()) ? 96 : 32) && h.room(t.food().copyWithCount(8)))return at(Kind.HAUL,p);
            if((t.food().isEmpty() || t.food().is(ModItems.GUAVA.get())) && t.food().getCount()<8 && h.count(ModItems.GUAVA.get())>24 && t.canInsert(new ItemStack(ModItems.GUAVA.get())))return at(Kind.SUPPLY,at);
        }
        if(garden)for(var p:h.sites())if(l.hasChunkAt(p) && l.getBlockState(p).is(Blocks.COMPOSTER) && (l.getBlockState(p).getValue(ComposterBlock.LEVEL)==8 && h.room(new ItemStack(Items.BONE_MEAL)) || l.getBlockState(p).getValue(ComposterBlock.LEVEL)<7 && h.count(ModItems.GUAVA.get())>16 && h.count(Items.BONE_MEAL)<16))return at(Kind.COMPOST,p);
        return null;
    }
    private boolean craftNeeded(HearthTreeBlockEntity h) {return h.count(ModItems.GUAVA_LOG.get())>0 && h.count(ModItems.GUAVA_PLANKS.get())<64 || h.count(Items.WOODEN_AXE)==0 && h.count(ModItems.GUAVA_PLANKS.get())>=4 || h.count(ModItems.GRAFTING_FRAME.get())<2 && h.count(ModItems.GUAVA_PLANKS.get())>=4 && h.count(Items.BONE_MEAL)>=2 || h.count(ModItems.NEST.get())<4 && h.count(Items.WHEAT)>=3 && h.count(ModItems.GUAVA_PLANKS.get())>=3 || h.count(ModItems.HEARTH_INCENSE.get())<8 && h.count(ModItems.GUAVA.get())>24 && h.count(ModItems.GUAVA_PLANKS.get())>0;}
    private boolean canStartFamily(HearthTreeBlockEntity h) {
        var list=h.residents();if(list.size()>=8 || list.stream().anyMatch(Bunnidog::isBaby) || h.count(ModItems.GUAVA.get())<20)return false;
        for(var p:h.sites())if(dog.level().hasChunkAt(p) && dog.level().getBlockEntity(p) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly() && c.seed().isEmpty()) {
            for(int i=0;i<h.getContainerSize();i++)if(communitySeed(h.getItem(i),h))return false;
            return list.stream().filter(d->d.readyForPen() && d.homeNest()!=null).count()>=2;
        }return false;
    }
    public static boolean communitySeed(ItemStack stack,HearthTreeBlockEntity h) {return com.bunnidogs.mod.item.SeedItem.read(stack).isPresent() && h.site().toString().equals(stack.get(ModItems.HEARTH_SITE.get()));}
    private boolean grief(ServerLevel l,BlockPos p) {return EventHooks.canEntityGrief(l,dog) && CommonHooks.canEntityDestroy(l,p,dog);}
    private boolean place(ServerLevel l,BlockPos p,BlockState before,BlockState next) {return grief(l,p) && !EventHooks.onBlockPlace(dog,BlockSnapshot.create(l.dimension(),l,p),Direction.UP) && ready() && hearth()!=null && hearth().attached() && l.getBlockState(p).equals(before) && next.canSurvive(l,p) && l.setBlock(p,next,3);}
    private boolean paidPlace(HearthTreeBlockEntity h,ServerLevel l,BlockPos p,BlockState before,BlockState after,Item cost) {
        if(h.count(cost)<1 || !after.getCollisionShape(l,p).isEmpty() && !l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(p),e->e.isAlive() && !e.isSpectator()).isEmpty())return false;var held=h.take(cost,1);
        boolean placed=false;try {placed=place(l,p,before,after);return placed;}finally {if(!placed && !h.put(held))Block.popResource(l,h.getBlockPos(),held);}
    }
    /** Called by the goal only after walking to the target and completing a visible work period. */
    public boolean perform(Job job) {
        if(medicalBusy || !ready() || dog.distanceToSqr(Vec3.atCenterOf(job.target()))>16 || !dog.level().hasChunkAt(job.target()))return false;
        var h=hearth();var l=(ServerLevel)dog.level();boolean ok=false;Learning.Routine learned=null;
        if(job.kind()!=Kind.DELIVER && !l.getBlockState(job.target()).equals(job.expected()))return false;
        switch(job.kind()) {
            case RECORD_ROUTE -> {ok=dog.courierLore().record(h,job.index());}
            case STUDY_ROUTE -> {ok=dog.courierLore().study(h,job.index());}
            case MEDICINE -> {ok=collectMedicine(job);}
            case CARE -> {ok=treatResident(job);learned=Learning.Routine.NURSERY;}
            case TEACH -> {ok=CapabilityRules.potential(dog.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT;}
            case MEAL -> {if(h.count(ModItems.GUAVA.get())>0 && dog.canEat(new ItemStack(ModItems.GUAVA.get()))) {h.take(ModItems.GUAVA.get(),1);dog.heal(2);workSinceMeal=0;h.evidence("meal",1);ok=true;}}
            case CRAFT -> {ok=craft(h);learned=Learning.Routine.GARDEN;}
            case BUILD -> {for(var c:h.shelter())if(c.pos().equals(job.target()) && h.authorized() && l.getBlockState(c.pos()).isAir() && h.count(c.item())>0 && CapabilityRules.potential(dog.mind(),CapabilityRules.Task.BUILDING)==CapabilityRules.Potential.INDEPENDENT && gardener()) {if(paidPlace(h,l,c.pos(),job.expected(),c.state(),c.item())) {h.evidence("build",1);ok=true;}break;}learned=Learning.Routine.GARDEN;}
            case PLANT -> {if(gardener() && h.authorized() && h.orchard().contains(job.target()) && h.count(ModItems.GUAVA_COPPICE.get())>0 && job.expected().isAir() && paidPlace(h,l,job.target(),job.expected(),ModBlocks.GUAVA_COPPICE.get().defaultBlockState(),ModItems.GUAVA_COPPICE.get())) {ok=true;}learned=Learning.Routine.GARDEN;}
            case PRUNE -> {if(knows(Learning.Routine.HARVEST) && h.orchard().contains(job.target()) && h.count(Items.WOODEN_AXE)>0 && job.expected().is(ModBlocks.GUAVA_COPPICE.get()) && job.expected().getValue(CoppiceBlock.AGE)==7 && h.room(new ItemStack(ModItems.GUAVA_LOG.get(),2)) && h.room(new ItemStack(ModItems.GUAVA.get(),4)) && place(l,job.target(),job.expected(),job.expected().setValue(CoppiceBlock.AGE,0))) {h.put(new ItemStack(ModItems.GUAVA_LOG.get(),2));h.put(new ItemStack(ModItems.GUAVA.get(),4));for(int i=0;i<27;i++)if(h.getItem(i).is(Items.WOODEN_AXE)) {var axe=h.getItem(i);axe.setDamageValue(axe.getDamageValue()+1);if(axe.getDamageValue()>=axe.getMaxDamage())axe.shrink(1);h.setChanged();break;}h.evidence("timber",1);h.evidence("harvest",4);ok=true;}learned=Learning.Routine.HARVEST;}
            case HAUL -> {if(knows(Learning.Routine.DELIVERY) && Supplying.qualified(dog.stats()) && h.sites().contains(job.target()) && l.getBlockEntity(job.target()) instanceof FeedingTroughBlockEntity t && t.getBlockState().getValue(FeedingTroughBlock.TENDING) && !t.residentsPaused() && !t.food().isEmpty() && com.bunnidogs.mod.item.GuavaItem.tierOf(t.food())==0 && t.food().getCount()>8 && h.room(t.food().copyWithCount(Math.min(8,t.food().getCount()-8)))) {cargo=t.takeSurplus(t.food().copy(),8);destination=h.getBlockPos();grownCargo=cargo.is(ModItems.GUAVA.get());ok=!cargo.isEmpty();}}
            case SUPPLY -> {if(!knows(Learning.Routine.DELIVERY) || !Supplying.qualified(dog.stats()) || h.count(ModItems.GUAVA.get())<=24)break;for(var p:h.sites())if(l.hasChunkAt(p) && l.getBlockEntity(p) instanceof FeedingTroughBlockEntity t && t.getBlockState().getValue(FeedingTroughBlock.TENDING) && !t.residentsPaused() && t.food().getCount()<8 && t.canInsert(new ItemStack(ModItems.GUAVA.get()))) {cargo=h.take(ModItems.GUAVA.get(),4);destination=p;grownCargo=true;ok=true;break;}}
            case DELIVER -> {if(cargo.isEmpty())break;
                if(aidPatient!=null) {ok=returnMedicine();break;}
                if(job.target().equals(h.getBlockPos()) && h.put(cargo)) {if(grownCargo)h.evidence("haul",cargo.getCount());cargo=ItemStack.EMPTY;ok=true;}
                else if(l.getBlockEntity(job.target()) instanceof FeedingTroughBlockEntity t && h.sites().contains(job.target()) && t.getBlockState().getValue(FeedingTroughBlock.TENDING) && !t.residentsPaused()) {int inserted=t.insert(cargo);if(grownCargo && inserted>0)h.evidence("haul",inserted);cargo.shrink(inserted);ok=inserted>0;}
                else if(l.getBlockEntity(job.target()) instanceof NurseryCradleBlockEntity c && c.seed().isEmpty() && communitySeed(cargo,h) && c.insert(cargo)) {cargo.shrink(1);ok=true;}
                if(ok) {if(cargo.isEmpty()) {destination=null;grownCargo=false;}learned=Learning.Routine.DELIVERY;}
            }
            case COMPOST -> {if(!gardener() || !h.sites().contains(job.target()) || !job.expected().is(Blocks.COMPOSTER) || !grief(l,job.target()))break;
                int n=job.expected().getValue(ComposterBlock.LEVEL);
                if(n==8 && h.room(new ItemStack(Items.BONE_MEAL)) && place(l,job.target(),job.expected(),job.expected().setValue(ComposterBlock.LEVEL,0))) {h.put(new ItemStack(Items.BONE_MEAL));ok=true;}
                else if(n<7 && h.count(ModItems.GUAVA.get())>16) {var food=h.take(ModItems.GUAVA.get(),1);ComposterBlock.insertItem(dog,job.expected(),l,food,job.target());if(!food.isEmpty())h.put(food);ok=food.isEmpty();}learned=Learning.Routine.GARDEN;
            }
            case COURT -> {if(!Gardening.nurses(dog.stats()) || !knows(Learning.Routine.NURSERY) || !canStartFamily(h))break;var pair=h.residents().stream().filter(d->d.readyForPen() && d.homeNest()!=null && d.distanceToSqr(dog)<=64).limit(2).toList();if(pair.size()==2) {h.take(ModItems.GUAVA.get(),2);pair.forEach(d->d.beginPenCourtship(0));h.evidence("court",1);ok=true;}learned=Learning.Routine.NURSERY;}
            case SEED -> {if(!Gardening.nurses(dog.stats()) || !knows(Learning.Routine.NURSERY) || !communitySeed(h.getItem(job.index()),h))break;for(var p:h.sites())if(l.hasChunkAt(p) && l.getBlockEntity(p) instanceof NurseryCradleBlockEntity c && c.seed().isEmpty() && !c.manualHatchOnly()) {cargo=h.takeExact(job.index(),1);destination=p;ok=true;break;}}
            case NURTURE -> {if(Gardening.nurses(dog.stats()) && knows(Learning.Routine.NURSERY) && h.count(ModItems.GUAVA.get())>8 && h.sites().contains(job.target()) && l.getBlockEntity(job.target()) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly() && communitySeed(c.storedSeed(),h) && c.nurture(c.storedSeed(),c.age())) {h.take(ModItems.GUAVA.get(),1);h.evidence("nurture",1);ok=true;}learned=Learning.Routine.NURSERY;}
            case HATCH -> {if(Gardening.nurses(dog.stats()) && knows(Learning.Routine.NURSERY) && h.sites().contains(job.target()) && l.getBlockEntity(job.target()) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly() && communitySeed(c.storedSeed(),h))ok=c.hatch().status()==NurseryHatching.Status.HATCHED;learned=Learning.Routine.NURSERY;}
            case TEND -> {if(gardener())ok=h.tend();learned=Learning.Routine.GARDEN;}
        }
        if(ok) {if(learned!=null)dog.learning().completed(learned);if(job.kind()!=Kind.MEAL)workSinceMeal++;nextWork=l.getGameTime()+80;l.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,dog.getX(),dog.getY()+.4,dog.getZ(),2,.15,.1,.15,0);}
        return ok;
    }
    private boolean nurse() {return Gardening.nurses(dog.stats()) && knows(Learning.Routine.NURSERY);}
    Bunnidog medicalPatient(Patient ref) {
        var h=hearth();if(ref==null || ref.bed()==null || h==null || !(dog.level() instanceof ServerLevel l) || !(l.getEntity(ref.id()) instanceof Bunnidog patient))return null;
        return patient!=dog && ref.bed().equals(patient.homeNest()) && ClinicCare.restingAtHome(h,patient) ? patient : null;
    }
    private boolean collectMedicine(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();if(!cargo.isEmpty() || !job.target().equals(h.getBlockPos()) || !nurse() || l.hasNeighborSignal(h.getBlockPos()))return false;
        medicalBusy=true;
        try {
            if(!EventHooks.canEntityGrief(l,dog) || !ready())return false;
            var patient=medicalPatient(job.patient());if(patient==null || !patient.clinic().ready() || !HomeRoutes.visible(dog,patient))return false;
            for(int slot=0;slot<h.getContainerSize();slot++)if(h.getItem(slot).is(ModItems.HEARTH_POULTICE.get())) {
                var unit=h.getItem(slot).copyWithCount(1);
                try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                    if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).extract(slot,net.neoforged.neoforge.transfer.item.ItemResource.of(unit),1,tx)!=1)return false;
                    if(!h.attached() || hearth()!=h || medicalPatient(job.patient())!=patient || !ready())return false;
                    cargo=unit;aidPatient=patient.getUUID();aidBed=job.patient().bed();aidOwner=h.owner();destination=aidBed;grownCargo=false;tx.commit();return true;
                }
            }return false;
        } finally {medicalBusy=false;}
    }
    private boolean treatResident(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var patient=medicalPatient(job.patient());
        if(patient==null || aidPatient==null || !aidPatient.equals(patient.getUUID()) || !nurse() || !cargo.is(ModItems.HEARTH_POULTICE.get())
                || !Objects.equals(aidOwner,h.owner()) || l.hasNeighborSignal(h.getBlockPos()) || dog.distanceToSqr(patient)>6.25 || !HomeRoutes.visible(dog,patient))return false;
        var exact=cargo.copy();medicalBusy=true;
        try {
            if(!EventHooks.canEntityGrief(l,dog))return false;
            boolean ok=patient.clinic().treat(()->dog.isAlive() && !dog.isRemoved() && ready() && nurse() && hearth()==h && h.attached() && Objects.equals(aidOwner,h.owner())
                    && !l.hasNeighborSignal(h.getBlockPos()) && medicalPatient(job.patient())==patient && dog.distanceToSqr(patient)<=6.25 && HomeRoutes.visible(dog,patient) && ItemStack.matches(cargo,exact),()->cargo.shrink(1));
            if(ok && cargo.isEmpty())clearMedicine();return ok;
        } finally {medicalBusy=false;}
    }
    private void clearMedicine() {aidPatient=null;aidBed=null;aidOwner=null;destination=null;grownCargo=false;}
    private boolean returnMedicine() {
        var h=hearth();if(h==null || !Objects.equals(aidOwner,h.owner()))return false;
        medicalBusy=true;
        try {
            if(!EventHooks.canEntityGrief((ServerLevel)dog.level(),dog) || !ready() || hearth()!=h || !Objects.equals(aidOwner,h.owner()))return false;
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).insert(net.neoforged.neoforge.transfer.item.ItemResource.of(cargo),cargo.getCount(),tx)!=cargo.getCount())return false;
                cargo=ItemStack.EMPTY;clearMedicine();tx.commit();return true;
            }
        } finally {medicalBusy=false;}
    }
    public boolean mayTakeSupplies(Player p) {return aidPatient!=null && !cargo.isEmpty() && !medicalBusy && p.mayBuild() && !p.isSpectator() && p.level()==dog.level() && dog.distanceToSqr(p)<=36 && (aidOwner==null ? authorized(p) : aidOwner.equals(p.getUUID()));}
    public boolean takeSupplies(Player p) {
        if(!mayTakeSupplies(p))return false;medicalBusy=true;var held=cargo;cargo=ItemStack.EMPTY;
        try {boolean given=p.getInventory().add(held);if(held.isEmpty())clearMedicine();else cargo=held;return given;}finally {medicalBusy=false;}
    }
    public ItemStack deathCargo() {var out=cargo;cargo=ItemStack.EMPTY;clearMedicine();return out;}
    private boolean craft(HearthTreeBlockEntity h) {
        if(!gardener() || dog.mind().handling()<1.5 || dog.mind().reasoning()<1.5)return false;
        Item output=null;int number=1;var costs=new LinkedHashMap<Item,Integer>();
        if(dressingNeeded(h)) {costs.put(ModItems.GUAVA.get(),1);costs.put(Items.BONE_MEAL,1);costs.put(Items.PAPER,1);costs.put(Items.HONEYCOMB,1);output=ModItems.HEARTH_POULTICE.get();number=4;}
        else if(h.count(ModItems.GUAVA_LOG.get())>0 && h.count(ModItems.GUAVA_PLANKS.get())<64) {costs.put(ModItems.GUAVA_LOG.get(),1);output=ModItems.GUAVA_PLANKS.get();number=4;}
        else if(h.count(Items.WOODEN_AXE)==0 && h.count(ModItems.GUAVA_PLANKS.get())>=4) {costs.put(ModItems.GUAVA_PLANKS.get(),4);output=Items.WOODEN_AXE;}
        else if(h.count(ModItems.GRAFTING_FRAME.get())<2 && h.count(ModItems.GUAVA_PLANKS.get())>=4 && h.count(Items.BONE_MEAL)>=2) {costs.put(ModItems.GUAVA_PLANKS.get(),4);costs.put(Items.BONE_MEAL,2);output=ModItems.GRAFTING_FRAME.get();}
        else if(h.count(ModItems.NEST.get())<4 && h.count(Items.WHEAT)>=3 && h.count(ModItems.GUAVA_PLANKS.get())>=3) {costs.put(Items.WHEAT,3);costs.put(ModItems.GUAVA_PLANKS.get(),3);output=ModItems.NEST.get();}
        else if(h.count(ModItems.HEARTH_INCENSE.get())<8 && h.count(ModItems.GUAVA.get())>24 && h.count(ModItems.GUAVA_PLANKS.get())>0) {costs.put(ModItems.GUAVA.get(),4);costs.put(ModItems.GUAVA_PLANKS.get(),1);output=ModItems.HEARTH_INCENSE.get();number=4;}
        if(output==null || !h.room(new ItemStack(output,number)))return false;for(var c:costs.entrySet())h.take(c.getKey(),c.getValue());h.put(new ItemStack(output,number));if(output==ModItems.GRAFTING_FRAME.get())h.evidence("frame",1);return true;
    }
    private boolean dressingNeeded(HearthTreeBlockEntity h) {
        return h.count(ModItems.HEARTH_POULTICE.get())<4 && h.count(ModItems.GUAVA.get())>8 && h.count(Items.BONE_MEAL)>0 && h.count(Items.PAPER)>0 && h.count(Items.HONEYCOMB)>0
                && h.residents().stream().anyMatch(p->p!=dog && p.clinic().ready() && ClinicCare.restingAtHome(h,p) && HomeRoutes.visible(dog,p));
    }
    public static boolean litter(Bunnidog a,Bunnidog b,ItemStack seed) {
        var h=a.society().hearth();if(h==null || !b.society().belongs(h))return false;
        seed.set(ModItems.HEARTH_SITE.get(),h.site().toString());seed.set(ModItems.HEARTH_LINK.get(),GlobalPos.of(a.level().dimension(),h.getBlockPos()));return h.put(seed);
    }
    public static HearthTreeBlockEntity seedHome(ServerLevel l,ItemStack seed) {var p=seed.get(ModItems.HEARTH_LINK.get());return p!=null && p.dimension().equals(l.dimension()) && l.hasChunkAt(p.pos()) && l.getBlockEntity(p.pos()) instanceof HearthTreeBlockEntity h && communitySeed(seed,h) ? h : null;}
    public void returnCargo() {if(cargo.isEmpty())return;var h=hearth();if(h==null || !h.put(cargo))dog.spawnAtLocation((ServerLevel)dog.level(),cargo);cargo=ItemStack.EMPTY;destination=null;grownCargo=false;}
    public void save(ValueOutput o) {if(aidPatient!=null) {o.putString("SocietyAidPatient",aidPatient.toString());if(aidBed!=null)o.store("SocietyAidBed",BlockPos.CODEC,aidBed);if(aidOwner!=null)o.putString("SocietyAidOwner",aidOwner.toString());}if(home!=null && site!=null) {o.store("SocietyHome",GlobalPos.CODEC,home);o.putString("SocietySite",site.toString());o.putBoolean("SocietyBorn",child);o.putInt("SocietyTeaching",residentLessons);o.putInt("SocietyMeals",workSinceMeal);o.putInt("SocietyWait",(int)Math.clamp(nextWork-dog.level().getGameTime(),0L,80L));}if(!cargo.isEmpty()) {o.store("SocietyCargo",ItemStack.CODEC,cargo);if(destination!=null)o.store("SocietyDestination",BlockPos.CODEC,destination);o.putBoolean("SocietyGrownCargo",grownCargo);}}
    public void load(ValueInput i) {medicalBusy=false;aidBed=i.read("SocietyAidBed",BlockPos.CODEC).orElse(null);try {aidPatient=UUID.fromString(i.getStringOr("SocietyAidPatient",""));}catch(IllegalArgumentException e){aidPatient=null;}try {aidOwner=UUID.fromString(i.getStringOr("SocietyAidOwner",""));}catch(IllegalArgumentException e){aidOwner=null;}home=i.read("SocietyHome",GlobalPos.CODEC).orElse(null);try {site=UUID.fromString(i.getStringOr("SocietySite",""));}catch(IllegalArgumentException e) {site=null;home=null;}child=i.getBooleanOr("SocietyBorn",false);residentLessons=i.getIntOr("SocietyTeaching",0)&15;workSinceMeal=Math.clamp(i.getIntOr("SocietyMeals",0),0,6);cargo=i.read("SocietyCargo",ItemStack.CODEC).orElse(ItemStack.EMPTY);destination=i.read("SocietyDestination",BlockPos.CODEC).orElse(null);grownCargo=i.getBooleanOr("SocietyGrownCargo",false);nextWork=dog.level().getGameTime()+Math.clamp(i.getIntOr("SocietyWait",0),0,80);}
}
