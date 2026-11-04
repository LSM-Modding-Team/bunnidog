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
    private boolean medicalBusy,operating;
    private boolean buildingCargo,fieldCargo,firstAidCargo;
    private boolean rescueCargo;
    private boolean miningCargo;
    private int furnaceSlot=-1;
    private String furnaceToken;
    private UUID parcelOwner;
    public enum Kind { MEAL, CRAFT, BUILD, PLANT, PRUNE, HAUL, SUPPLY, DELIVER, COMPOST, COURT, SEED, NURTURE, HATCH, TEND, TEACH, MEDICINE, CARE, RECORD_ROUTE, STUDY_ROUTE, FETCH_BUILD, FETCH_SEED, SOW_FIELD, HARVEST_FIELD, GATHER_FIELD, PLAN, FETCH_AID, CLIP_WOOD, PICK_FRUIT, HATCH_SOIL, FETCH_FURNACE, LOAD_FURNACE, EMPTY_FURNACE, FETCH_PICKAXE, MINE_STONE, FETCH_RESCUE, PICKUP_RESCUE, RELEASE_RESCUE, RECORD_CRAFT }
    public record Patient(UUID id,BlockPos bed) {}
    public record Job(Kind kind,BlockPos target,int index,BlockState expected,Patient patient,BlockPos destination) {
        public Job(Kind kind,BlockPos target,int index,BlockState expected) {this(kind,target,index,expected,null,null);}
        public Job(Kind kind,BlockPos target,int index,BlockState expected,Patient patient) {this(kind,target,index,expected,patient,null);}
    }
    SocietyWork(Bunnidog dog) {this.dog=dog;}
    public HearthTreeBlockEntity hearth() {
        var l=dog.level();return home!=null && site!=null && home.dimension().equals(l.dimension()) && l.hasChunkAt(home.pos()) && l.getBlockEntity(home.pos()) instanceof HearthTreeBlockEntity h && site.equals(h.site()) ? h : null;
    }
    public boolean belongs(HearthTreeBlockEntity h) {return home!=null && home.dimension().equals(dog.level().dimension()) && home.pos().equals(h.getBlockPos()) && h.site().equals(site);}
    public boolean authorized(Player p) {var h=hearth();return home==null || h!=null && (h.owner()==null || h.owner().equals(p.getUUID()));}
    public boolean cargoEmpty() {return cargo.isEmpty();}
    public ItemStack medicalSupplies() {return aidPatient==null ? ItemStack.EMPTY : cargo.copy();}
    public ItemStack workSupplies() {return cargo.copy();}
    public BlockPos buildTarget() {return buildingCargo && !cargo.isEmpty() ? destination : null;}
    public BlockPos fieldTarget() {return fieldCargo && !cargo.isEmpty() ? destination : null;}
    public BlockPos furnaceTarget() {return furnaceSlot>=0 && !cargo.isEmpty() ? destination : null;}
    public boolean miningTool() {return miningCargo && WildForaging.usablePickaxe(cargo);}
    public BlockPos miningTarget() {return miningTool() ? destination : null;}
    public static boolean furnaceJob(Kind kind) {return kind==Kind.FETCH_FURNACE || kind==Kind.LOAD_FURNACE || kind==Kind.EMPTY_FURNACE;}
    public UUID treatingPatient() {return !cargo.isEmpty() ? aidPatient : null;}
    public BlockPos rescueBed() {var h=hearth();return rescueCargo && rescueOccupant() && h!=null && Objects.equals(aidOwner,h.owner()) ? aidBed : null;}
    public boolean child() {return child;}
    public boolean invite(Player player,HearthTreeBlockEntity h,boolean leave) {
        if(operating || medicalBusy || leave && aidPatient!=null && !cargo.isEmpty() || !h.permitted(player) || !dog.deliveries().authorized(player) || !dog.isAlive() || !dog.acceptsCare() || dog.isBaby() || dog.level()!=h.getLevel() || dog.distanceToSqr(Vec3.atCenterOf(h.getBlockPos()))>24*24 || dog.trail().keeper()!=null && !dog.trail().belongsTo(player))return false;
        if(leave) {if(!belongs(h))return false;release();returnCargo();home=null;site=null;child=false;return true;}
        if(home!=null && !belongs(h) || !belongs(h) && h.residents().size()>=16)return false;
        if(dog.trail().ordered() && !dog.trail().command(player,Trail.Order.RESIDENT))return false;
        home=GlobalPos.of(dog.level().dimension(),h.getBlockPos());site=h.site();dog.setPersistenceRequired();return true;
    }
    public void settle(HearthTreeBlockEntity h) {if(home==null && h.wild().natural()) {home=GlobalPos.of(dog.level().dimension(),h.getBlockPos());site=h.site();child=false;dog.setPersistenceRequired();}}
    public void born(HearthTreeBlockEntity h) {home=GlobalPos.of(dog.level().dimension(),h.getBlockPos());site=h.site();child=true;dog.setPersistenceRequired();}
    private boolean calm() {return dog.acceptsCare() && !dog.homeNight() && dog.isAlive() && !dog.isBaby() && !dog.beingTransported() && !dog.isVehicle() && !dog.trail().ordered() && !dog.workPaused() && !dog.combat().danger() && !dog.combat().downed() && dog.getTarget()==null && !dog.seekingRefuge() && !dog.isInLove() && !dog.isOnFire() && !dog.isInWater() && dog.getHealth()>=dog.getMaxHealth()*.8F;}
    public boolean ready() {var h=hearth();return h!=null && calm() && dog.level().getGameTime()>=nextWork && dog.distanceToSqr(Vec3.atCenterOf(h.getBlockPos()))<24*24;}
    public int waitTicks() {return (int)Math.clamp(nextWork-dog.level().getGameTime(),0,80);}
    public boolean practiceReady() {return hearth()!=null && calm() && Gardening.cultivates(dog.stats()) && dog.distanceToSqr(Vec3.atCenterOf(hearth().getBlockPos()))<24*24;}
    public Bunnidog mentor(Learning.Routine routine) {
        var h=hearth();if(h==null || !calm())return null;
        return h.residents().stream().filter(d->d!=dog && d.distanceToSqr(dog)<=(routine==Learning.Routine.BUILD ? 144 : 36) && d.society().calm() && d.learning().lesson()==null && d.learning().knowledge().independent(d.mind(),routine) && (routine==Learning.Routine.BUILD || CapabilityRules.potential(d.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT) && HomeRoutes.visible(dog,d)).findFirst().orElse(null);
    }
    public boolean guided(Learning.Routine r) {return CapabilityRules.potential(dog.mind(),r.task())!=CapabilityRules.Potential.LIMITED && mentor(r)!=null;}
    public void completed(Learning.Routine routine,boolean independent) {var h=hearth();if(h!=null && child && independent && (residentLessons&(1<<routine.ordinal()))!=0)h.evidence("continuity",1);}
    public void taught(Learning.Routine routine) {residentLessons|=1<<routine.ordinal();}
    public void harvested(BlockPos trough) {var h=hearth();if(h!=null && h.sites().contains(trough))h.evidence("harvest",1);}
    private boolean knows(Learning.Routine r) {return dog.learning().canWork(r);}
    private Job at(Kind k,BlockPos p) {return new Job(k,p.immutable(),-1,dog.level().getBlockState(p));}
    /** Primitive discovery belongs only to this natural household, within independent inherited capacity. */
    public boolean pioneer(Learning.Routine routine) {var h=hearth();return h!=null && h.wild().natural() && dog.learning().canDiscover() && !dog.courierLore().occupied() && !dog.workshopLessons().apprenticing() && !dog.deliveries().watching() && dog.deliveries().cargo().isEmpty() && CapabilityRules.potential(dog.mind(),routine.task())==CapabilityRules.Potential.INDEPENDENT;}
    private boolean harvestReady() {return knows(Learning.Routine.HARVEST) || pioneer(Learning.Routine.HARVEST);}
    private boolean gardener() {return Gardening.cultivates(dog.stats()) && knows(Learning.Routine.GARDEN) || pioneer(Learning.Routine.GARDEN);}
    private boolean artisan() {return Gardening.cultivates(dog.stats()) && knows(Learning.Routine.CRAFT) || pioneer(Learning.Routine.CRAFT);}
    private boolean advancedWorkshop() {return Gardening.cultivates(dog.stats()) && knows(Learning.Routine.CRAFT);}
    public boolean gathers() {return knows(Learning.Routine.DELIVERY) && Supplying.qualified(dog.stats()) || pioneer(Learning.Routine.DELIVERY);}
    public int gatheringLimit() {return knows(Learning.Routine.DELIVERY) && Supplying.qualified(dog.stats()) ? 8 : 1;}
    private boolean familyNurse() {return nurse() || pioneer(Learning.Routine.NURSERY);}
    private boolean recipeAllowed(CivicRecipes.RecipeId recipe) {return advancedWorkshop() || pioneer(Learning.Routine.CRAFT) && switch(recipe) {case PLANKS,STICKS,AXE,NEST,COPPICE,PICKAXE,FURNACE,TORCH,WORKBENCH->true;default->false;};}
    private Job active;
    public Job active() {return active;}
    private CivicRecipes.RecipeId craftRecipe(Job job) {return job.kind()==Kind.CRAFT && job.index()>=0 && job.index()<CivicRecipes.RecipeId.values().length ? CivicRecipes.RecipeId.values()[job.index()] : null;}
    public BlockPos workTarget(Job job) {var recipe=craftRecipe(job);var h=hearth();return recipe!=null && h!=null && h.wild().works().requiresWorkbench(recipe) ? h.wild().works().workbench() : job.target();}
    public boolean workspaceAvailable(Job job) {var recipe=craftRecipe(job);var h=hearth();return recipe==null || h!=null && h.wild().works().craftingAvailable(recipe);}
    boolean claim(Job job) {var h=hearth();if(h==null || !h.jobs().claim(dog,job))return false;active=job;if(miningCargo && job.kind()==Kind.MINE_STONE)destination=job.target();return true;}
    boolean owns(Job job) {var h=hearth();return h!=null && h.jobs().owns(dog,job);}
    void release() {var h=hearth();if(h!=null)h.jobs().release(dog);active=null;}
    public Job plan() {var offered=offers();return offered.isEmpty() ? null : offered.getFirst();}
    private int mealSlot(HearthTreeBlockEntity h) {
        for(int slot=0;slot<h.getContainerSize();slot++) {var food=h.getItem(slot);if(CivicRecipes.ordinary(food) && com.bunnidogs.mod.item.GuavaItem.tierOf(food)==0 && dog.canEat(food))return slot;}return -1;
    }
    /** Read-only bounded offers. The movement goal acquires a short exclusive claim only when it starts. */
    public List<Job> offers() {
        if(!ready() || dog.courierLore().apprenticing() || dog.workshopLessons().apprenticing() || dog.deliveries().watching() || !dog.deliveries().cargo().isEmpty())return List.of();
        var h=hearth();var l=(ServerLevel)dog.level();var at=h.getBlockPos();var jobs=new ArrayList<Job>();
        if(!cargo.isEmpty() && rescueCargo) {
            if(!rescueReady(h))return List.of();
            var ref=new Patient(aidPatient,aidBed);
            if(nurse() && com.bunnidogs.mod.item.TravelBasketItem.occupied(cargo)) {
                var spot=CivicRescue.recoverySpot(dog,h,aidBed);
                if(rescueOccupant() && spot!=null && !NestBlock.claimed(l,aidBed))return List.of(new Job(Kind.RELEASE_RESCUE,spot,-1,l.getBlockState(spot),ref));
            } else if(nurse()) {
                var patient=CivicRescue.patient(dog,h,ref);
                if(patient!=null)return List.of(new Job(Kind.PICKUP_RESCUE,patient.blockPosition(),-1,l.getBlockState(patient.blockPosition()),ref));
            }
            return List.of(at(Kind.DELIVER,at));
        }
        if(!cargo.isEmpty() && miningCargo) {
            if(destination!=null && !l.hasChunkAt(destination))return List.of();
            if(!l.hasChunksAt(at.offset(-1,-1,-1),at.offset(1,1,1)))return List.of();
            if(h.isLocked() || l.hasNeighborSignal(at))return List.of();
            if(miningTool() && Objects.equals(parcelOwner,h.owner()) && harvestReady() && workSinceMeal<6 && h.wild().works().needsStone()) {
                for(var stone:h.wild().works().stones())if(h.wild().works().canMine(stone))jobs.add(at(Kind.MINE_STONE,stone));
                jobs.removeIf(job->!h.jobs().available(dog,job));
                if(!jobs.isEmpty())return List.copyOf(jobs);
            }
            return List.of(at(Kind.DELIVER,at));
        }
        if(!cargo.isEmpty() && furnaceSlot>=0) {
            if(destination!=null && !l.hasChunkAt(destination))return List.of();
            if(destination!=null && h.workshop().resolve(destination,furnaceToken)!=null && Objects.equals(parcelOwner,h.owner())) {
                if(h.workshop().available(destination)==null)return List.of();
                if(artisan() && h.workshop().canLoad(destination,furnaceToken,furnaceSlot,cargo,advancedWorkshop()))return List.of(at(Kind.LOAD_FURNACE,destination));
            }
            return List.of(at(Kind.DELIVER,at));
        }
        if(!cargo.isEmpty() && fieldCargo) {
            var planted=CivicFields.plant(cargo);
            return List.of(planted!=null && gardener() && destination!=null && CivicFields.plot(h,destination) && l.getBlockState(destination).isAir() && planted.canSurvive(l,destination) && Objects.equals(parcelOwner,h.owner()) ? at(Kind.SOW_FIELD,destination) : at(Kind.DELIVER,at));
        }
        if(!cargo.isEmpty() && buildingCargo) {
            var cell=destination==null ? null : h.construction().cell(destination);
            return List.of(cell!=null && buildingReady(cell) && h.construction().canBuild(cell) && cargo.is(cell.item()) && Objects.equals(parcelOwner,h.owner()) ? at(Kind.BUILD,destination) : at(Kind.DELIVER,at));
        }
        if(!cargo.isEmpty() && aidPatient!=null) {
            if(!Objects.equals(aidOwner,h.owner()))return List.of();var patient=firstAidCargo ? rescuePatient(new Patient(aidPatient,aidBed),false) : medicalPatient(new Patient(aidPatient,aidBed));
            jobs.add(nurse() && patient!=null ? new Job(Kind.CARE,patient.blockPosition().immutable(),-1,l.getBlockState(patient.blockPosition()),new Patient(patient.getUUID(),patient.homeNest())) : at(Kind.DELIVER,at));return jobs;
        }
        if(!cargo.isEmpty())return List.of(at(Kind.DELIVER,destination!=null && l.hasChunkAt(destination) && (destination.equals(at) || h.wild().natural() && destination.equals(h.wild().nursery()) || h.sites().contains(destination) && (l.getBlockEntity(destination) instanceof FeedingTroughBlockEntity || l.getBlockEntity(destination) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly())) ? destination : at));
        boolean hungry=workSinceMeal>=6;
        if(hungry && mealSlot(h)>=0)return List.of(at(Kind.MEAL,at));
        boolean grow=harvestReady(),garden=gardener(),nurse=nurse();
        boolean craft=artisan();
        if(!hungry) {
            for(var furnace:h.sites())if(h.workshop().available(furnace)!=null) {
                if(craft)for(int slot=0;slot<2;slot++)if(h.workshop().supply(furnace,slot,gatheringLimit(),advancedWorkshop())!=null)
                    jobs.add(new Job(Kind.FETCH_FURNACE,at,slot,l.getBlockState(at),null,furnace));
                if(gathers())for(int slot:new int[]{2,1}) {
                    var output=h.workshop().output(furnace,slot,gatheringLimit());
                    if(!output.isEmpty() && h.room(output))jobs.add(new Job(Kind.EMPTY_FURNACE,furnace,slot,l.getBlockState(furnace),null,at));
                }
            }
            if(dog.learning().knowledge().independent(dog.mind(),Learning.Routine.GARDEN) && CapabilityRules.potential(dog.mind(),CapabilityRules.Task.PLANNING)==CapabilityRules.Potential.INDEPENDENT && h.agenda().wantsPlan())jobs.add(at(Kind.PLAN,at));
            if(nurse && h.count(ModItems.GUAVA.get())>0 && !l.hasNeighborSignal(at))for(var patient:h.residents())
                if(patient!=dog && rescuePatient(new Patient(patient.getUUID(),patient.homeNest()),false)!=null && HomeRoutes.visible(dog,patient))jobs.add(new Job(Kind.FETCH_AID,at,-1,l.getBlockState(at),new Patient(patient.getUUID(),patient.homeNest())));
            if(nurse && CivicRecipes.count(h,ModItems.TRAVEL_BASKET.get())>0 && rescueReady(h))for(var patient:h.residents()) {
                var ref=new Patient(patient.getUUID(),patient.homeNest());var spot=CivicRescue.recoverySpot(dog,h,ref.bed());
                if(spot!=null && CivicRescue.patient(dog,h,ref)!=null && HomeRoutes.visible(dog,patient))jobs.add(new Job(Kind.FETCH_RESCUE,at,-1,l.getBlockState(at),ref,spot));
            }
            if(nurse && h.count(ModItems.HEARTH_POULTICE.get())>0 && !l.hasNeighborSignal(at))
                for(var patient:h.residents())if(patient!=dog && patient.clinic().ready() && ClinicCare.restingAtHome(h,patient) && HomeRoutes.visible(dog,patient))
                    jobs.add(new Job(Kind.MEDICINE,at,-1,l.getBlockState(at),new Patient(patient.getUUID(),patient.homeNest())));
            if(!l.hasNeighborSignal(at))for(var recipe:CivicRecipes.RecipeId.values())
                if((craft && recipeAllowed(recipe) || dog.workshopLore().guides(h,recipe)) && h.wild().works().craftingAvailable(recipe) && recipeNeeded(h,recipe) && CivicRecipes.prepare(h,recipe).isPresent())jobs.add(new Job(Kind.CRAFT,at,recipe.ordinal(),l.getBlockState(at)));
            var archive=dog.courierLore().archiveJob(h);if(archive!=null)jobs.add(archive);
            var craftArchive=dog.workshopLore().archiveJob(h);if(craftArchive!=null)jobs.add(craftArchive);
            if(CapabilityRules.potential(dog.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT)
                for(var pupil:h.residents())if(pupil!=dog && !pupil.isBaby() && pupil.society().calm())for(var r:Learning.Routine.values())
                    if(r!=Learning.Routine.SCOUT && !r.workshop() && dog.learning().knowledge().independent(dog.mind(),r) && pupil.learning().physicallyReady(r) && !pupil.learning().knowledge().knows(r) && CapabilityRules.potential(pupil.mind(),r.task())!=CapabilityRules.Potential.LIMITED) {
                        jobs.add(new Job(Kind.TEACH,pupil.blockPosition(),r.ordinal(),l.getBlockState(pupil.blockPosition()),new Patient(pupil.getUUID(),null)));break;
                    }
            if(nurse)for(var pos:h.sites())if(l.hasChunkAt(pos) && l.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle && !cradle.manualHatchOnly()) {
                if(cradle.seed().isPresent() && cradle.age()==SeedCropBlock.MAX_AGE)jobs.add(at(Kind.HATCH,pos));
                if(cradle.seed().isPresent() && cradle.age()<SeedCropBlock.MAX_AGE && h.count(ModItems.GUAVA.get())>8)jobs.add(at(Kind.NURTURE,pos));
                if(cradle.seed().isEmpty())for(int i=0;i<h.getContainerSize();i++)if(communitySeed(h.getItem(i),h)) {jobs.add(new Job(Kind.SEED,at,i,l.getBlockState(at)));break;}
            }
            if(familyNurse() && h.wild().natural()) {
                var soil=h.wild().nursery();
                if(h.wild().emptyNursery())for(int i=0;i<h.getContainerSize();i++)if(communitySeed(h.getItem(i),h)) {jobs.add(new Job(Kind.SEED,at,i,l.getBlockState(at)));break;}
                if(l.hasChunkAt(soil) && l.getBlockEntity(soil) instanceof SeedCropBlockEntity crop && communitySeed(crop.seedStack(),h) && SeedCropBlock.isRipe(l.getBlockState(soil)))jobs.add(at(Kind.HATCH_SOIL,soil));
            }
            if(familyNurse() && canStartFamily(h))jobs.add(at(Kind.COURT,at));
        }
        if(!hungry)for(var soil:h.sites()) {
            var pos=soil.above();if(!CivicFields.plot(h,pos))continue;var state=l.getBlockState(pos);
            if(grow && CivicFields.seed(state)!=null && state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state))jobs.add(at(Kind.HARVEST_FIELD,pos));
            if(garden && state.isAir()) {int slot=CivicFields.seedSlot(h);if(slot>=0 && CivicFields.plant(h.getItem(slot)).canSurvive(l,pos))jobs.add(new Job(Kind.FETCH_SEED,at,slot,l.getBlockState(at),null,pos));}
        }
        if(gathers())for(var item:CivicFields.drops(h))if(!hungry || dog.canEat(item.getItem()))jobs.add(new Job(Kind.GATHER_FIELD,item.blockPosition(),item.getId(),l.getBlockState(item.blockPosition())));
        if(hungry && craft && h.count(Items.WOODEN_AXE)==0 && h.orchard().stream().anyMatch(pos->l.hasChunkAt(pos) && l.getBlockState(pos).is(ModBlocks.GUAVA_COPPICE.get()) && l.getBlockState(pos).getValue(CoppiceBlock.AGE)==7))
            for(var recipe:List.of(CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.STICKS,CivicRecipes.RecipeId.WORKBENCH,CivicRecipes.RecipeId.AXE))if(recipeAllowed(recipe) && h.wild().works().craftingAvailable(recipe) && recipeNeeded(h,recipe) && CivicRecipes.prepare(h,recipe).isPresent())jobs.add(new Job(Kind.CRAFT,at,recipe.ordinal(),l.getBlockState(at)));
        if(h.wild().natural() && grow) {
            if(!hungry && h.wild().works().needsStone() && WildForaging.pickaxeSlot(h)>=0)
                for(var stone:h.wild().works().stones())if(h.wild().works().canMine(stone))jobs.add(new Job(Kind.FETCH_PICKAXE,at,-1,l.getBlockState(at),null,stone));
            for(var pos:h.wild().timber())if(!hungry && h.wild().timberAvailable(pos) && l.hasChunkAt(pos) && l.getBlockState(pos).is(ModBlocks.GUAVA_LOG.get()))jobs.add(at(Kind.CLIP_WOOD,pos));
            for(var pos:h.wild().fruit())if(l.hasChunkAt(pos) && l.getBlockState(pos).is(ModBlocks.GUAVA_FRUIT.get()) && l.getBlockState(pos).getValue(GuavaFruitBlock.AGE)==GuavaFruitBlock.MAX_AGE && (!hungry || dog.canEat(new ItemStack(ModItems.GUAVA.get()))))jobs.add(at(Kind.PICK_FRUIT,pos));
        }
        if(h.authorized()) {
            if(!hungry && Gardening.cultivates(dog.stats()) && knows(Learning.Routine.GARDEN) && h.ripe() && h.count(ModItems.GUAVA.get())>=16 && h.count(Items.BONE_MEAL)>=2 && h.count(ModItems.GRAFTING_FRAME.get())>0)jobs.add(at(Kind.TEND,at));
            if(!hungry) {
                var cells=buildingReady(null) ? h.construction().cells() : primitiveFurnitureReady() ? h.wild().works().cells() : List.<HearthTreeBlockEntity.Cell>of();
                for(var cell:cells)if(h.construction().canBuild(cell) && buildingMaterialAvailable(h,cell)) {
                    jobs.add(new Job(Kind.FETCH_BUILD,at,-1,l.getBlockState(at),null,cell.pos()));if(jobs.size()>=40)break;
                }
            }
            for(var pos:h.orchard())if(l.hasChunkAt(pos)) {
                var state=l.getBlockState(pos);
                if(state.is(ModBlocks.GUAVA_COPPICE.get()) && state.getValue(CoppiceBlock.AGE)==7 && grow && (!hungry || dog.canEat(new ItemStack(ModItems.GUAVA.get()))) && h.count(Items.WOODEN_AXE)>0 && h.room(new ItemStack(ModItems.GUAVA_LOG.get(),2)) && h.room(new ItemStack(ModItems.GUAVA.get(),4)))jobs.add(at(Kind.PRUNE,pos));
                if(!hungry && state.isAir() && garden && h.count(ModItems.GUAVA_COPPICE.get())>0 && ModBlocks.GUAVA_COPPICE.get().defaultBlockState().canSurvive(l,pos))jobs.add(at(Kind.PLANT,pos));
            }
        }
        if(knows(Learning.Routine.DELIVERY) && Supplying.qualified(dog.stats()))for(int index=0;index<h.sites().size();index++) {
            var pos=h.sites().get(index);if(!l.hasChunkAt(pos) || !(l.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) || !trough.getBlockState().getValue(FeedingTroughBlock.TENDING) || trough.residentsPaused() || !trough.food().isEmpty() && com.bunnidogs.mod.item.GuavaItem.tierOf(trough.food())!=0)continue;
            if((trough.food().getCount()>12 || trough.food().getCount()>8 && trough.food().is(ModItems.GUAVA.get()) && h.count(ModItems.GUAVA.get())<16) && (!hungry || dog.canEat(trough.food())) && h.count(trough.food().getItem())<(trough.food().is(ModItems.GUAVA.get()) ? 96 : 32) && h.room(trough.food().copyWithCount(8)))jobs.add(at(Kind.HAUL,pos));
            if(!hungry && (trough.food().isEmpty() || trough.food().is(ModItems.GUAVA.get())) && trough.food().getCount()<8 && h.count(ModItems.GUAVA.get())>24 && trough.canInsert(new ItemStack(ModItems.GUAVA.get())))jobs.add(new Job(Kind.SUPPLY,at,index,l.getBlockState(at)));
        }
        if(!hungry && garden)for(var pos:h.sites())if(l.hasChunkAt(pos) && l.getBlockState(pos).is(Blocks.COMPOSTER) && (l.getBlockState(pos).getValue(ComposterBlock.LEVEL)==8 && h.room(new ItemStack(Items.BONE_MEAL)) || l.getBlockState(pos).getValue(ComposterBlock.LEVEL)<7 && h.count(ModItems.GUAVA.get())>16 && h.count(Items.BONE_MEAL)<16))jobs.add(at(Kind.COMPOST,pos));
        jobs.removeIf(job->!lessonAllows(job) || !h.jobs().available(dog,job));jobs.sort(Comparator.comparingDouble((Job job)->priority(h,job)).reversed());return List.copyOf(jobs.stream().limit(16).toList());
    }
    static Learning.Routine routine(Kind kind) {return switch(kind) {
        case CRAFT,FETCH_FURNACE,LOAD_FURNACE -> Learning.Routine.CRAFT;
        case BUILD,FETCH_BUILD -> Learning.Routine.BUILD;
        case PLANT,COMPOST,FETCH_SEED,SOW_FIELD,TEND -> Learning.Routine.GARDEN;
        case PRUNE,CLIP_WOOD,PICK_FRUIT,FETCH_PICKAXE,MINE_STONE,HARVEST_FIELD -> Learning.Routine.HARVEST;
        case MEDICINE,CARE,FETCH_AID,FETCH_RESCUE,PICKUP_RESCUE,RELEASE_RESCUE,COURT,SEED,NURTURE,HATCH,HATCH_SOIL -> Learning.Routine.NURSERY;
        case HAUL,SUPPLY,GATHER_FIELD,EMPTY_FURNACE -> Learning.Routine.DELIVERY;
        default -> null;
    };}
    private boolean lessonAllows(Job job) {var lesson=dog.learning().lesson();return lesson==null || job.kind()==Kind.MEAL || job.kind()==Kind.DELIVER || lesson==routine(job.kind());}
    private static WorkSpecialty specialty(Kind kind) {return switch(kind) {
        case BUILD,FETCH_BUILD -> WorkSpecialty.BUILDER;
        case PLAN -> WorkSpecialty.PLANNER;
        case CRAFT,FETCH_FURNACE,LOAD_FURNACE -> WorkSpecialty.CRAFTSPERSON;
        case HAUL,SUPPLY,DELIVER,SEED,GATHER_FIELD,EMPTY_FURNACE -> WorkSpecialty.COURIER;
        case MEDICINE,CARE,FETCH_AID,COURT,NURTURE,HATCH,HATCH_SOIL,FETCH_RESCUE,PICKUP_RESCUE,RELEASE_RESCUE -> WorkSpecialty.CARER;
        case TEACH,RECORD_ROUTE,STUDY_ROUTE,RECORD_CRAFT -> WorkSpecialty.TEACHER;
        default -> WorkSpecialty.GROWER;
    };}
    private double priority(HearthTreeBlockEntity h,Job job) {
        boolean scarce=h.count(ModItems.GUAVA.get())<16;int urgency=switch(job.kind()) {
            case FETCH_AID,FETCH_RESCUE -> 175;case CARE,MEDICINE -> 150;
            case PLAN -> 105;case HATCH,HATCH_SOIL -> 120;case NURTURE,SEED -> 95;
            case FETCH_FURNACE -> 100;case LOAD_FURNACE -> 120;case EMPTY_FURNACE -> 110;
            case FETCH_PICKAXE,MINE_STONE -> 120;
            case HAUL -> scarce ? 135 : 70;case SUPPLY -> 100;
            case PRUNE -> scarce ? 130 : 75;case PICK_FRUIT -> scarce ? 130 : 75;case CLIP_WOOD -> h.count(Items.WOODEN_AXE)==0 ? 125 : 60;case GATHER_FIELD -> 105;case HARVEST_FIELD -> h.count(Items.WHEAT)<12 ? 110 : 75;case FETCH_SEED,SOW_FIELD -> 90;case PLANT -> 85;
            case BUILD,FETCH_BUILD -> (job.destination()!=null && h.construction().needsRepair(job.destination()) || h.construction().needsRepair(job.target())) ? 125 : h.wild().works().workCell(job.destination()!=null ? job.destination() : job.target()) ? 115 : 65;case TEACH -> 45;case RECORD_ROUTE,STUDY_ROUTE,RECORD_CRAFT -> 35;
            case COMPOST -> h.count(Items.BONE_MEAL)<2 ? 90 : 40;
            case COURT -> 55;case TEND -> 60;
            case CRAFT -> switch(CivicRecipes.RecipeId.values()[job.index()]) {
                case DRESSING -> 150;case BASKET -> 160;case AXE -> h.count(Items.WOODEN_AXE)==0 ? 145 : 40;case STICKS -> h.count(Items.WOODEN_AXE)==0 ? 142 : h.wild().works().needsPickaxe() || h.wild().works().needsTorches() ? 122 : 40;
                case PLANKS -> h.count(Items.WOODEN_AXE)==0 ? 140 : 80;
                case NEST -> 90;case COPPICE -> 90;case FRAME -> h.count(ModItems.GRAFTING_FRAME.get())==0 ? 80 : 50;case INCENSE -> 20;
                case PICKAXE -> 125;case FURNACE -> 125;case TORCH -> 100;
                case WORKBENCH -> 146;
            };
            default -> 200;
        };
        var role=specialty(job.kind());double affinity=role.affinity(dog.mind(),dog.stats());
        // Stable personal familiarity rewards real work, while urgency always dominates a preference.
        return urgency+h.agenda().boost(job)+affinity*2+(lastSpecialty==role ? 2 : 0);
    }
    private WorkSpecialty lastSpecialty;
    private boolean recipeNeeded(HearthTreeBlockEntity h,CivicRecipes.RecipeId recipe) {return switch(recipe) {
        case PLANKS -> CivicRecipes.count(h,ModItems.GUAVA_PLANKS.get())<(h.wild().works().pending() ? 12 : 64);
        case STICKS -> CivicRecipes.count(h,Items.STICK)<2 && (h.count(Items.WOODEN_AXE)==0 || h.wild().works().needsPickaxe() || h.wild().works().needsTorches());
        case AXE -> h.count(Items.WOODEN_AXE)==0;
        case FRAME -> CivicRecipes.count(h,ModItems.GRAFTING_FRAME.get())<2;
        case NEST -> CivicRecipes.count(h,ModItems.NEST.get())<4 && h.construction().cells().stream().anyMatch(c->c.item()==ModItems.NEST.get() && h.construction().canBuild(c));
        case INCENSE -> CivicRecipes.count(h,ModItems.HEARTH_INCENSE.get())<8 && h.count(ModItems.GUAVA.get())>24;
        case DRESSING -> dressingNeeded(h);
        case COPPICE -> h.authorized() && h.orchard().stream().anyMatch(p->dog.level().hasChunkAt(p) && dog.level().getBlockState(p).isAir()) && CivicRecipes.count(h,ModItems.GUAVA_COPPICE.get())<2;
        case PICKAXE -> h.wild().works().needsPickaxe();
        case FURNACE -> h.wild().works().needsFurnace();
        case TORCH -> h.wild().works().needsTorches();
        case BASKET -> CivicRescue.needsBasket(h);
        case WORKBENCH -> h.wild().works().needsWorkbench();
    };}
    private boolean canStartFamily(HearthTreeBlockEntity h) {
        var list=h.residents();if(list.size()>=8 || list.stream().anyMatch(Bunnidog::isBaby) || h.count(ModItems.GUAVA.get())<20)return false;
        for(int i=0;i<h.getContainerSize();i++)if(communitySeed(h.getItem(i),h))return false;
        if(list.stream().anyMatch(d->communitySeed(d.society().workSupplies(),h)))return false;
        boolean nursery=h.wild().emptyNursery() || h.sites().stream().anyMatch(p->dog.level().hasChunkAt(p) && dog.level().getBlockEntity(p) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly() && c.seed().isEmpty());
        return nursery && list.stream().filter(d->d.readyForPen() && d.homeNest()!=null).count()>=2;
    }
    public static boolean communitySeed(ItemStack stack,HearthTreeBlockEntity h) {return com.bunnidogs.mod.item.SeedItem.read(stack).isPresent() && h.site().toString().equals(stack.get(ModItems.HEARTH_SITE.get()));}
    private boolean grief(ServerLevel l,BlockPos p) {return EventHooks.canEntityGrief(l,dog) && CommonHooks.canEntityDestroy(l,p,dog);}
    private boolean place(ServerLevel l,BlockPos p,BlockState before,BlockState next) {return grief(l,p) && !EventHooks.onBlockPlace(dog,BlockSnapshot.create(l.dimension(),l,p),Direction.UP) && ready() && hearth()!=null && hearth().attached() && l.getBlockState(p).equals(before) && next.canSurvive(l,p) && l.setBlock(p,next,3);}
    private boolean paidPlace(HearthTreeBlockEntity h,ServerLevel l,BlockPos p,BlockState before,BlockState after,Item cost) {
        if(!after.getCollisionShape(l,p).isEmpty() && !l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(p),e->e.isAlive() && !e.isSpectator()).isEmpty())return false;
        ItemStack held=ItemStack.EMPTY;for(int slot=0;slot<h.getContainerSize();slot++)if(h.getItem(slot).is(cost) && CivicRecipes.ordinary(h.getItem(slot))) {held=h.takeExact(slot,1);break;}
        if(held.isEmpty())return false;boolean placed=false;try {placed=place(l,p,before,after);return placed;}finally {if(!placed && !h.put(held))Block.popResource(l,h.getBlockPos(),held);}
    }
    private boolean buildingReady(HearthTreeBlockEntity.Cell cell) {
        if(structuralBuildingReady())return true;
        return cell!=null && primitiveFurnitureReady() && hearth().wild().works().workCell(cell.pos());
    }
    private boolean structuralBuildingReady() {return Gardening.cultivates(dog.stats()) && knows(Learning.Routine.BUILD) || pioneer(Learning.Routine.BUILD);}
    private boolean primitiveFurnitureReady() {var h=hearth();return h!=null && h.wild().works().workbenchPlot() && dog.learning().knowledge().knows(Learning.Routine.CRAFT) && pioneer(Learning.Routine.CRAFT);}
    private boolean buildingMaterialAvailable(HearthTreeBlockEntity h,HearthTreeBlockEntity.Cell cell) {
        int reserve=cell.item()==ModItems.GUAVA_PLANKS.get() && h.wild().works().pending() && !h.wild().works().workCell(cell.pos()) ? 12 : 0;
        return CivicRecipes.count(h,cell.item())>reserve;
    }
    private boolean collectBuild(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var cell=job.destination()==null ? null : h.construction().cell(job.destination());
        if(!cargo.isEmpty() || cell==null || !job.target().equals(h.getBlockPos()) || !buildingReady(cell) || !h.construction().canBuild(cell) || !buildingMaterialAvailable(h,cell) || l.hasNeighborSignal(h.getBlockPos()))return false;
        medicalBusy=true;
        try {
            if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !h.construction().canBuild(cell) || !buildingMaterialAvailable(h,cell) || !buildingReady(cell))return false;
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                var resource=net.neoforged.neoforge.transfer.item.ItemResource.of(new ItemStack(cell.item()));
                if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).extract(resource,1,tx)!=1 || !h.attached())return false;
                cargo=new ItemStack(cell.item());destination=cell.pos();buildingCargo=true;parcelOwner=h.owner();grownCargo=false;tx.commit();return true;
            }
        } finally {medicalBusy=false;}
    }
    private boolean build(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var cell=h.construction().cell(job.target());
        if(!buildingCargo || cell==null || !job.target().equals(destination) || !cargo.is(cell.item()) || cargo.getCount()!=1 || !Objects.equals(parcelOwner,h.owner()) || !buildingReady(cell) || !h.construction().canBuild(cell))return false;
        if(!cell.state().getCollisionShape(l,cell.pos()).isEmpty() && !l.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(cell.pos()),e->e.isAlive() && !e.isSpectator()).isEmpty())return false;
        var exact=cargo.copy();medicalBusy=true;
        try {
            if(!grief(l,cell.pos()) || EventHooks.onBlockPlace(dog,BlockSnapshot.create(l.dimension(),l,cell.pos()),Direction.UP) || !ready() || hearth()!=h || !h.attached() || !buildingReady(cell) || !h.construction().canBuild(cell) || !ItemStack.matches(cargo,exact)) {h.construction().failed(cell.pos());return false;}
            // The paid material stays owned by this carrier through permission callbacks and failed edits.
            var material=cargo;cargo=ItemStack.EMPTY;buildingCargo=false;boolean placed=false;
            try {placed=l.setBlock(cell.pos(),cell.state(),3);if(!placed)return false;
                destination=null;parcelOwner=null;h.construction().placed(cell.pos());h.wild().works().placed(cell.pos());h.evidence("build",1);return true;
            } finally {if(!placed) {if(dog.isAlive() && !dog.isRemoved()) {cargo=material;buildingCargo=true;}else dog.spawnAtLocation(l,material);}}
        } finally {medicalBusy=false;}
    }
    private boolean collectFieldSeed(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();if(!cargo.isEmpty() || !gardener() || job.destination()==null || !job.target().equals(h.getBlockPos()) || !CivicFields.plot(h,job.destination()) || !l.getBlockState(job.destination()).isAir() || job.index()<0 || job.index()>=h.getContainerSize() || l.hasNeighborSignal(h.getBlockPos()))return false;
        var seed=h.getItem(job.index()).copyWithCount(1);var plant=CivicFields.plant(seed);if(plant==null || !CivicRecipes.ordinary(seed) || !plant.canSurvive(l,job.destination()))return false;
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !CivicFields.plot(h,job.destination()) || !l.getBlockState(job.destination()).isAir())return false;
        try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).extract(job.index(),net.neoforged.neoforge.transfer.item.ItemResource.of(seed),1,tx)!=1 || !h.attached())return false;
            cargo=seed;destination=job.destination();parcelOwner=h.owner();fieldCargo=true;grownCargo=false;tx.commit();return true;
        }
    }
    private boolean sowField(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var plant=CivicFields.plant(cargo);
        if(!fieldCargo || plant==null || !Objects.equals(parcelOwner,h.owner()) || !job.target().equals(destination) || !gardener() || !CivicFields.plot(h,destination) || !l.getBlockState(destination).isAir() || !plant.canSurvive(l,destination))return false;
        var exact=cargo.copy();
        if(!grief(l,destination) || EventHooks.onBlockPlace(dog,BlockSnapshot.create(l.dimension(),l,destination),Direction.UP) || !ready() || hearth()!=h || !gardener() || !CivicFields.plot(h,destination) || !l.getBlockState(destination).isAir() || !ItemStack.matches(cargo,exact))return false;
        var material=cargo;cargo=ItemStack.EMPTY;fieldCargo=false;boolean placed=false;
        try {placed=l.setBlock(destination,plant,3);if(placed) {destination=null;parcelOwner=null;}return placed;}
        finally {if(!placed) {if(dog.isAlive() && !dog.isRemoved()) {cargo=material;fieldCargo=true;}else dog.spawnAtLocation(l,material);}}
    }
    private boolean collectFurnaceSupply(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();
        if(!cargo.isEmpty() || !artisan() || !job.target().equals(h.getBlockPos()) || job.destination()==null)return false;
        var supply=h.workshop().supply(job.destination(),job.index(),gatheringLimit(),advancedWorkshop());
        if(supply==null || !EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !artisan())return false;
        // Stock and furnace callbacks may invalidate the original proposal; use a fresh exact source journal.
        supply=h.workshop().supply(job.destination(),job.index(),gatheringLimit(),advancedWorkshop());if(supply==null)return false;
        try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).extract(supply.sourceSlot(),net.neoforged.neoforge.transfer.item.ItemResource.of(supply.stack()),supply.stack().getCount(),tx)!=supply.stack().getCount()
                    || !h.attached() || !ready() || hearth()!=h || !cargo.isEmpty() || h.workshop().resolve(job.destination(),supply.token())==null)return false;
            cargo=supply.stack().copy();destination=job.destination();parcelOwner=h.owner();furnaceSlot=supply.furnaceSlot();furnaceToken=supply.token();grownCargo=false;tx.commit();return true;
        }
    }
    private boolean loadFurnace(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();
        if(furnaceSlot<0 || !job.target().equals(destination) || !Objects.equals(parcelOwner,h.owner()) || !artisan()
                || !h.workshop().canLoad(destination,furnaceToken,furnaceSlot,cargo,advancedWorkshop()))return false;
        var original=cargo.copy();
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !artisan() || !Objects.equals(parcelOwner,h.owner()) || !ItemStack.matches(cargo,original)
                || !h.workshop().canLoad(destination,furnaceToken,furnaceSlot,cargo,advancedWorkshop()))return false;
        var furnace=h.workshop().resolve(destination,furnaceToken);
        try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            int inserted=net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(furnace).insert(furnaceSlot,net.neoforged.neoforge.transfer.item.ItemResource.of(original),original.getCount(),tx);
            if(inserted!=original.getCount() || !ready() || hearth()!=h || h.workshop().resolve(destination,furnaceToken)!=furnace || !ItemStack.matches(cargo,original))return false;
            cargo=ItemStack.EMPTY;destination=null;parcelOwner=null;furnaceSlot=-1;furnaceToken=null;tx.commit();return true;
        }
    }
    private boolean emptyFurnace(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();
        if(!cargo.isEmpty() || !gathers())return false;var original=h.workshop().output(job.target(),job.index(),gatheringLimit());
        if(original.isEmpty() || !h.room(original))return false;var token=h.workshop().token(job.target());var furnace=h.workshop().resolve(job.target(),token);
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !gathers() || !cargo.isEmpty() || !h.room(original)
                || h.workshop().resolve(job.target(),token)!=furnace || !ItemStack.matches(original,h.workshop().output(job.target(),job.index(),gatheringLimit())))return false;
        try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(furnace).extract(job.index(),net.neoforged.neoforge.transfer.item.ItemResource.of(original),original.getCount(),tx)!=original.getCount()
                    || !h.attached() || !ready() || hearth()!=h || h.workshop().resolve(job.target(),token)!=furnace || !cargo.isEmpty())return false;
            cargo=original;destination=h.getBlockPos();parcelOwner=h.owner();grownCargo=false;tx.commit();return true;
        }
    }
    private boolean collectPickaxe(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();
        if(!cargo.isEmpty() || !harvestReady() || h.isLocked() || !l.hasChunksAt(h.getBlockPos().offset(-1,-1,-1),h.getBlockPos().offset(1,1,1)) || l.hasNeighborSignal(h.getBlockPos()) || !job.target().equals(h.getBlockPos())
                || job.destination()==null || !h.wild().works().needsStone() || !h.wild().works().canMine(job.destination()))return false;
        int slot=WildForaging.pickaxeSlot(h);if(slot<0)return false;var tool=h.getItem(slot).copy();
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !harvestReady() || !cargo.isEmpty() || h.isLocked()
                || !l.hasChunksAt(h.getBlockPos().offset(-1,-1,-1),h.getBlockPos().offset(1,1,1)) || l.hasNeighborSignal(h.getBlockPos())
                || !h.wild().works().needsStone() || !h.wild().works().canMine(job.destination()) || !ItemStack.matches(tool,h.getItem(slot)))return false;
        try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).extract(slot,net.neoforged.neoforge.transfer.item.ItemResource.of(tool),1,tx)!=1
                    || !h.attached() || !ready() || hearth()!=h || !cargo.isEmpty())return false;
            cargo=tool;miningCargo=true;destination=job.destination();parcelOwner=h.owner();grownCargo=false;tx.commit();return true;
        }
    }
    private boolean mineReady(HearthTreeBlockEntity h,Job job,ItemStack tool) {
        return ready() && hearth()==h && harvestReady() && miningTool() && Objects.equals(parcelOwner,h.owner()) && ItemStack.matches(cargo,tool)
                && !h.isLocked() && dog.level().hasChunksAt(h.getBlockPos().offset(-1,-1,-1),h.getBlockPos().offset(1,1,1))
                && !dog.level().hasNeighborSignal(h.getBlockPos()) && h.wild().works().needsStone() && h.wild().works().canMine(job.target())
                && cargo.isCorrectToolForDrops(job.expected());
    }
    private boolean mineStone(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var exact=cargo.copy();
        if(!mineReady(h,job,exact))return false;
        boolean allowed=grief(l,job.target());h.wild().works().observe();if(!allowed || !mineReady(h,job,exact))return false;
        boolean canceled=EventHooks.onBlockPlace(dog,BlockSnapshot.create(l.dimension(),l,job.target()),Direction.UP);h.wild().works().observe();if(canceled || !mineReady(h,job,exact))return false;
        var drops=Block.getDrops(job.expected(),l,job.target(),null,dog,exact);
        h.wild().works().observe();
        if(!mineReady(h,job,exact) || !h.wild().works().reserveStone(job.target()))return false;
        var tool=cargo;var owner=parcelOwner;var target=destination;cargo=ItemStack.EMPTY;miningCargo=false;boolean changed=false;
        try {
            changed=l.setBlock(job.target(),Blocks.AIR.defaultBlockState(),3);if(!changed)return false;
            tool.setDamageValue(tool.getDamageValue()+1);if(tool.getDamageValue()>=tool.getMaxDamage())tool.shrink(1);
            CivicFields.scatter(h,job.target(),drops);return true;
        } finally {
            if(!changed)h.wild().works().unreserveStone(job.target());
            if(!tool.isEmpty()) {
                if(dog.isAlive() && !dog.isRemoved() && cargo.isEmpty()) {cargo=tool;miningCargo=true;parcelOwner=owner;destination=target;}
                else dog.spawnAtLocation(l,tool);
            } else if(cargo.isEmpty()) {parcelOwner=null;destination=null;}
        }
    }
    /** Called by the goal only after walking to the target and completing a visible work period. */
    public boolean perform(Job job) {
        if(operating || medicalBusy || dog.workshopLessons().apprenticing() || !lessonAllows(job) || job.kind()!=Kind.DELIVER && job.kind()!=Kind.MEAL && !dog.learning().witnesses(job.target(),job.destination()==null ? job.target() : job.destination()) || !cargo.isEmpty() && job.kind()!=Kind.DELIVER && job.kind()!=Kind.BUILD && job.kind()!=Kind.CARE && job.kind()!=Kind.SOW_FIELD && job.kind()!=Kind.LOAD_FURNACE && job.kind()!=Kind.MINE_STONE && job.kind()!=Kind.PICKUP_RESCUE && job.kind()!=Kind.RELEASE_RESCUE || !ready() || !hearth().jobs().available(dog,job) || dog.distanceToSqr(Vec3.atCenterOf(job.target()))>16 || !dog.level().hasChunkAt(job.target()))return false;
        operating=true;
        try {
        var h=hearth();var l=(ServerLevel)dog.level();var spent=job.kind()==Kind.BUILD || job.kind()==Kind.LOAD_FURNACE ? cargo.copy() : ItemStack.EMPTY;boolean ok=false,replanted=false;Learning.Routine learned=null;
        if(job.kind()!=Kind.DELIVER && job.kind()!=Kind.GATHER_FIELD && job.kind()!=Kind.LOAD_FURNACE && job.kind()!=Kind.EMPTY_FURNACE && !l.getBlockState(job.target()).equals(job.expected()))return false;
        switch(job.kind()) {
            case FETCH_FURNACE -> {ok=collectFurnaceSupply(job);}
            case LOAD_FURNACE -> {ok=loadFurnace(job);learned=Learning.Routine.CRAFT;}
            case EMPTY_FURNACE -> {ok=emptyFurnace(job);}
            case FETCH_PICKAXE -> {ok=collectPickaxe(job);}
            case MINE_STONE -> {ok=mineStone(job);learned=Learning.Routine.HARVEST;}
            case RECORD_ROUTE -> {ok=dog.courierLore().record(h,job.index());}
            case STUDY_ROUTE -> {ok=dog.courierLore().study(h,job.index());}
            case RECORD_CRAFT -> {ok=dog.workshopLore().record(h,job.index());}
            case FETCH_AID -> {ok=collectAid(job);}
            case FETCH_RESCUE -> {ok=collectRescue(job);}
            case PICKUP_RESCUE -> {ok=pickupRescue(job);learned=Learning.Routine.NURSERY;}
            case RELEASE_RESCUE -> {ok=releaseRescue(job);learned=Learning.Routine.NURSERY;}
            case MEDICINE -> {ok=collectMedicine(job);}
            case CARE -> {ok=firstAidCargo ? aidResident(job) : treatResident(job);learned=Learning.Routine.NURSERY;}
            case TEACH -> {ok=job.index()>=0 && job.index()<Learning.Routine.values().length && !Learning.Routine.values()[job.index()].workshop() && CapabilityRules.potential(dog.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT;}
            case MEAL -> {int slot=mealSlot(h);if(slot>=0) {h.takeExact(slot,1);dog.heal(2);workSinceMeal=0;h.evidence("meal",1);ok=true;}}
            case CRAFT -> {ok=craft(h,job.index());learned=Learning.Routine.CRAFT;}
            case PLAN -> {ok=h.agenda().write(dog);}
            case FETCH_SEED -> {ok=collectFieldSeed(job);}
            case SOW_FIELD -> {ok=sowField(job);learned=Learning.Routine.GARDEN;}
            case CLIP_WOOD,PICK_FRUIT -> {ok=harvestReady() && WildForaging.harvest(dog,h,job);learned=Learning.Routine.HARVEST;}
            case HATCH_SOIL -> {ok=familyNurse() && h.wild().hatch(dog,job.target());learned=Learning.Routine.NURSERY;}
            case HARVEST_FIELD -> {if(harvestReady()) {var result=CivicFields.harvest(dog,h,job.target(),job.expected());ok=result!=CivicFields.Harvest.NONE;replanted=result==CivicFields.Harvest.REPLANTED;}learned=Learning.Routine.HARVEST;}
            case GATHER_FIELD -> {if(gathers() && l.getEntity(job.index()) instanceof net.minecraft.world.entity.item.ItemEntity item) {var taken=CivicFields.collect(dog,h,item);if(!taken.isEmpty()) {cargo=taken;destination=h.getBlockPos();parcelOwner=h.owner();grownCargo=cargo.is(ModItems.GUAVA.get());ok=true;}}}
            case FETCH_BUILD -> {ok=collectBuild(job);}
            case BUILD -> {ok=build(job);if(ok && structuralBuildingReady())learned=Learning.Routine.BUILD;}
            case PLANT -> {if(gardener() && h.authorized() && h.orchard().contains(job.target()) && h.count(ModItems.GUAVA_COPPICE.get())>0 && job.expected().isAir() && paidPlace(h,l,job.target(),job.expected(),ModBlocks.GUAVA_COPPICE.get().defaultBlockState(),ModItems.GUAVA_COPPICE.get())) {ok=true;}learned=Learning.Routine.GARDEN;}
            case PRUNE -> {if(harvestReady() && h.orchard().contains(job.target()) && h.count(Items.WOODEN_AXE)>0 && job.expected().is(ModBlocks.GUAVA_COPPICE.get()) && job.expected().getValue(CoppiceBlock.AGE)==7 && h.room(new ItemStack(ModItems.GUAVA_LOG.get(),2)) && h.room(new ItemStack(ModItems.GUAVA.get(),4)) && place(l,job.target(),job.expected(),job.expected().setValue(CoppiceBlock.AGE,0))) {CivicFields.scatter(h,job.target(),List.of(new ItemStack(ModItems.GUAVA_LOG.get(),2),new ItemStack(ModItems.GUAVA.get(),4)));for(int i=0;i<27;i++)if(h.getItem(i).is(Items.WOODEN_AXE)) {var axe=h.getItem(i);axe.setDamageValue(axe.getDamageValue()+1);if(axe.getDamageValue()>=axe.getMaxDamage())axe.shrink(1);h.setChanged();break;}h.evidence("timber",1);h.evidence("harvest",4);ok=true;}learned=Learning.Routine.HARVEST;}
            case HAUL -> {if(knows(Learning.Routine.DELIVERY) && Supplying.qualified(dog.stats()) && h.sites().contains(job.target()) && l.getBlockEntity(job.target()) instanceof FeedingTroughBlockEntity t && t.getBlockState().getValue(FeedingTroughBlock.TENDING) && !t.residentsPaused() && !t.food().isEmpty() && com.bunnidogs.mod.item.GuavaItem.tierOf(t.food())==0 && t.food().getCount()>8 && h.room(t.food().copyWithCount(Math.min(8,t.food().getCount()-8)))) {cargo=t.takeSurplus(t.food().copy(),8);destination=h.getBlockPos();parcelOwner=h.owner();grownCargo=cargo.is(ModItems.GUAVA.get());ok=!cargo.isEmpty();}}
            case SUPPLY -> {if(!knows(Learning.Routine.DELIVERY) || !Supplying.qualified(dog.stats()) || h.count(ModItems.GUAVA.get())<=24)break;for(var p:h.sites())if((job.index()<0 || job.index()<h.sites().size() && p.equals(h.sites().get(job.index()))) && l.hasChunkAt(p) && l.getBlockEntity(p) instanceof FeedingTroughBlockEntity t && t.getBlockState().getValue(FeedingTroughBlock.TENDING) && !t.residentsPaused() && t.food().getCount()<8 && t.canInsert(new ItemStack(ModItems.GUAVA.get()))) {cargo=h.take(ModItems.GUAVA.get(),4);destination=p;parcelOwner=h.owner();grownCargo=true;ok=true;break;}}
            case DELIVER -> {if(cargo.isEmpty())break;
                if(aidPatient!=null) {ok=returnMedicine();break;}
                if(job.target().equals(h.getBlockPos()))ok=returnSupplies();
                else if(l.getBlockEntity(job.target()) instanceof FeedingTroughBlockEntity t && h.sites().contains(job.target()) && t.getBlockState().getValue(FeedingTroughBlock.TENDING) && !t.residentsPaused()) {int inserted=t.insert(cargo);if(grownCargo && inserted>0)h.evidence("haul",inserted);cargo.shrink(inserted);ok=inserted>0;}
                else if(h.wild().natural() && job.target().equals(h.wild().nursery()))ok=plantSoil(h,job.target());
                else if(h.sites().contains(job.target()) && l.getBlockEntity(job.target()) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly() && c.seed().isEmpty() && communitySeed(cargo,h) && c.insert(cargo)) {cargo.shrink(1);ok=true;}
                if(ok) {if(cargo.isEmpty()) {destination=null;grownCargo=false;buildingCargo=fieldCargo=miningCargo=false;parcelOwner=null;furnaceSlot=-1;furnaceToken=null;}learned=Learning.Routine.DELIVERY;}
            }
            case COMPOST -> {if(!gardener() || !h.sites().contains(job.target()) || !job.expected().is(Blocks.COMPOSTER) || !grief(l,job.target()))break;
                int n=job.expected().getValue(ComposterBlock.LEVEL);
                if(n==8 && h.room(new ItemStack(Items.BONE_MEAL)) && place(l,job.target(),job.expected(),job.expected().setValue(ComposterBlock.LEVEL,0))) {h.put(new ItemStack(Items.BONE_MEAL));ok=true;}
                else if(n<7 && h.count(ModItems.GUAVA.get())>16) {var food=h.take(ModItems.GUAVA.get(),1);ComposterBlock.insertItem(dog,job.expected(),l,food,job.target());if(!food.isEmpty())h.put(food);ok=food.isEmpty();}learned=Learning.Routine.GARDEN;
            }
            case COURT -> {if(!familyNurse() || !canStartFamily(h))break;var pair=h.residents().stream().filter(d->d.readyForPen() && d.homeNest()!=null && d.distanceToSqr(dog)<=64).limit(2).toList();if(pair.size()==2) {h.take(ModItems.GUAVA.get(),2);pair.forEach(d->d.beginPenCourtship(0));h.evidence("court",1);ok=true;}learned=Learning.Routine.NURSERY;}
            case SEED -> {if(!familyNurse() || job.index()<0 || job.index()>=h.getContainerSize() || !communitySeed(h.getItem(job.index()),h))break;
                if(h.wild().emptyNursery()) {cargo=h.takeExact(job.index(),1);destination=h.wild().nursery();parcelOwner=h.owner();ok=!cargo.isEmpty();break;}for(var p:h.sites())if(l.hasChunkAt(p) && l.getBlockEntity(p) instanceof NurseryCradleBlockEntity c && c.seed().isEmpty() && !c.manualHatchOnly()) {cargo=h.takeExact(job.index(),1);destination=p;parcelOwner=h.owner();ok=true;break;}}
            case NURTURE -> {if(Gardening.nurses(dog.stats()) && knows(Learning.Routine.NURSERY) && h.count(ModItems.GUAVA.get())>8 && h.sites().contains(job.target()) && l.getBlockEntity(job.target()) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly() && communitySeed(c.storedSeed(),h) && c.nurture(c.storedSeed(),c.age())) {h.take(ModItems.GUAVA.get(),1);h.evidence("nurture",1);ok=true;}learned=Learning.Routine.NURSERY;}
            case HATCH -> {if(Gardening.nurses(dog.stats()) && knows(Learning.Routine.NURSERY) && h.sites().contains(job.target()) && l.getBlockEntity(job.target()) instanceof NurseryCradleBlockEntity c && !c.manualHatchOnly() && communitySeed(c.storedSeed(),h))ok=c.hatch().status()==NurseryHatching.Status.HATCHED;learned=Learning.Routine.NURSERY;}
            case TEND -> {if(Gardening.cultivates(dog.stats()) && knows(Learning.Routine.GARDEN))ok=h.tend();learned=Learning.Routine.GARDEN;}
        }
        if(ok) {WorkshopApprenticeship.completed(dog,h,job,spent);lastSpecialty=specialty(job.kind());if(replanted && pioneer(Learning.Routine.GARDEN))dog.learning().pioneerPractice(Learning.Routine.GARDEN);if(learned!=null) {if(pioneer(learned))dog.learning().pioneerPractice(learned);else dog.learning().completed(learned);}if(job.kind()!=Kind.MEAL)workSinceMeal++;nextWork=l.getGameTime()+80;l.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,dog.getX(),dog.getY()+.4,dog.getZ(),2,.15,.1,.15,0);}
        return ok;
        } finally {operating=false;}
    }
    private boolean plantSoil(HearthTreeBlockEntity h,BlockPos pos) {
        var l=(ServerLevel)dog.level();if(!familyNurse() || !h.wild().emptyNursery() || !Objects.equals(parcelOwner,h.owner()) || !communitySeed(cargo,h))return false;
        var exact=cargo.copy();
        if(!grief(l,pos) || EventHooks.onBlockPlace(dog,BlockSnapshot.create(l.dimension(),l,pos),Direction.UP) || !ready() || hearth()!=h || !familyNurse() || !h.wild().emptyNursery() || !ItemStack.matches(exact,cargo))return false;
        cargo=ItemStack.EMPTY;boolean planted=false;
        try {planted=SeedCropBlock.plant(l,pos,exact);return planted;}
        finally {if(!planted) {if(dog.isAlive() && !dog.isRemoved())cargo=exact;else dog.spawnAtLocation(l,exact);}}
    }
    private boolean nurse() {return Gardening.nurses(dog.stats()) && knows(Learning.Routine.NURSERY);}
    Bunnidog medicalPatient(Patient ref) {
        var h=hearth();if(ref==null || ref.bed()==null || h==null || !(dog.level() instanceof ServerLevel l) || !(l.getEntity(ref.id()) instanceof Bunnidog patient))return null;
        return patient!=dog && ref.bed().equals(patient.homeNest()) && ClinicCare.restingAtHome(h,patient) ? patient : null;
    }
    Bunnidog carePatient(Job job) {var h=hearth();return job.kind()==Kind.FETCH_RESCUE ? h==null ? null : CivicRescue.patient(dog,h,job.patient()) : job.kind()==Kind.FETCH_AID ? rescuePatient(job.patient(),false) : medicalPatient(job.patient());}
    private Bunnidog rescuePatient(Patient ref,boolean receiving) {
        var h=hearth();if(h==null || ref==null || ref.bed()==null || !(dog.level() instanceof ServerLevel l) || !(l.getEntity(ref.id()) instanceof Bunnidog patient) || patient==dog || !patient.society().belongs(h))return null;
        if(patient.trail().keeper()!=null && !Objects.equals(patient.trail().keeper(),h.owner()) || !ref.bed().equals(patient.homeNest()) || !l.hasChunkAt(ref.bed()) || !l.getBlockState(ref.bed()).is(ModBlocks.NEST.get()) || !NestBlock.claimed(l,ref.bed()) || ref.bed().distSqr(h.getBlockPos())>24*24 || patient.distanceToSqr(Vec3.atCenterOf(ref.bed()))>9)return null;
        return (receiving ? patient.combat().downed() && patient.combat().receivingAid() && !patient.combat().danger() && patient.getTarget()==null && !patient.seekingRefuge() && patient.onGround() && !patient.beingTransported() && !patient.isVehicle() && !patient.isOnFire() && !patient.isInWater() && !patient.isInLava() && HomeRoutes.dryCell(patient,patient.blockPosition()) : patient.combat().canResidentAid()) ? patient : null;
    }
    private boolean rescueReady(HearthTreeBlockEntity h) {
        return h.attached() && !h.isLocked() && dog.level().hasChunksAt(h.getBlockPos().offset(-1,-1,-1),h.getBlockPos().offset(1,1,1))
                && !dog.level().hasNeighborSignal(h.getBlockPos()) && (!rescueCargo || Objects.equals(aidOwner,h.owner()) && Objects.equals(parcelOwner,h.owner()));
    }
    private boolean rescueOccupant() {
        return cargo.is(ModItems.TRAVEL_BASKET.get()) && cargo.getCount()==1 && com.bunnidogs.mod.item.TravelBasketItem.readEntry(cargo)
                .flatMap(Pedigree.Entry::id).filter(id->id.equals(aidPatient)).isPresent();
    }
    private boolean collectRescue(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var ref=job.patient();
        if(!cargo.isEmpty() || !nurse() || !job.target().equals(h.getBlockPos()) || !rescueReady(h))return false;
        var patient=CivicRescue.patient(dog,h,ref);var spot=CivicRescue.recoverySpot(dog,h,ref==null ? null : ref.bed());
        if(patient==null || spot==null || !spot.equals(job.destination()) || !HomeRoutes.visible(dog,patient))return false;
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !nurse() || !rescueReady(h)
                || CivicRescue.patient(dog,h,ref)!=patient || !spot.equals(CivicRescue.recoverySpot(dog,h,ref.bed())) || !HomeRoutes.visible(dog,patient))return false;
        for(int slot=0;slot<h.getContainerSize();slot++)if(h.getItem(slot).is(ModItems.TRAVEL_BASKET.get()) && CivicRecipes.ordinary(h.getItem(slot))) {
            var basket=h.getItem(slot).copyWithCount(1);
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).extract(slot,net.neoforged.neoforge.transfer.item.ItemResource.of(basket),1,tx)!=1
                        || !h.attached() || !ready() || hearth()!=h || !cargo.isEmpty() || !rescueReady(h) || CivicRescue.patient(dog,h,ref)!=patient)return false;
                cargo=basket;rescueCargo=true;aidPatient=ref.id();aidBed=ref.bed();aidOwner=parcelOwner=h.owner();destination=aidBed;grownCargo=false;tx.commit();return true;
            }
        }return false;
    }
    private boolean pickupRescue(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var ref=new Patient(aidPatient,aidBed);var patient=CivicRescue.patient(dog,h,ref);
        if(!rescueCargo || !ref.equals(job.patient()) || patient==null || !job.target().equals(patient.blockPosition()) || !nurse() || !rescueReady(h)
                || !cargo.is(ModItems.TRAVEL_BASKET.get()) || cargo.getCount()!=1 || com.bunnidogs.mod.item.TravelBasketItem.occupied(cargo)
                || dog.distanceToSqr(patient)>6.25 || !HomeRoutes.visible(dog,patient))return false;
        var exact=cargo.copy();
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !nurse() || !rescueReady(h) || CivicRescue.patient(dog,h,ref)!=patient
                || !job.target().equals(patient.blockPosition()) || dog.distanceToSqr(patient)>6.25 || !HomeRoutes.visible(dog,patient) || !ItemStack.matches(exact,cargo))return false;
        // The actual basket is outside recoverable cargo during native capture/removal callbacks.
        var basket=cargo;var owner=aidOwner;cargo=ItemStack.EMPTY;
        try {return com.bunnidogs.mod.item.TravelBasketItem.capture(l,basket,patient)==com.bunnidogs.mod.item.TravelBasketItem.Result.CAPTURED;}
        finally {holdRescueBasket(basket,owner,ref,true);}
    }
    private boolean releaseRescue(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();var ref=new Patient(aidPatient,aidBed);var spot=job.target();
        if(!rescueCargo || !ref.equals(job.patient()) || !rescueOccupant() || !nurse() || !rescueReady(h) || !CivicRescue.bed(h,aidBed)
                || NestBlock.claimed(l,aidBed) || !spot.equals(CivicRescue.recoverySpot(dog,h,aidBed)))return false;
        var exact=cargo.copy();
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h || !nurse() || !rescueReady(h) || !CivicRescue.bed(h,aidBed)
                || NestBlock.claimed(l,aidBed) || !spot.equals(CivicRescue.recoverySpot(dog,h,aidBed)) || !ItemStack.matches(exact,cargo))return false;
        var basket=cargo;var owner=aidOwner;cargo=ItemStack.EMPTY;boolean released=false;
        try {
            released=com.bunnidogs.mod.item.TravelBasketItem.release(l,spot,basket)==com.bunnidogs.mod.item.TravelBasketItem.Result.RELEASED;
            if(released && l.getEntity(ref.id()) instanceof Bunnidog patient && patient.society().belongs(h)
                    && CivicRescue.bed(h,ref.bed()) && !NestBlock.claimed(l,ref.bed()))patient.moveHomeTo(l,ref.bed());
            return released;
        } finally {holdRescueBasket(basket,owner,ref,!released);}
    }
    private void holdRescueBasket(ItemStack basket,UUID owner,Patient ref,boolean ongoing) {
        if(!dog.isAlive() || dog.isRemoved() || !cargo.isEmpty()) {dog.spawnAtLocation((ServerLevel)dog.level(),basket);return;}
        cargo=basket;clearMedicine();parcelOwner=owner;
        if(ongoing) {rescueCargo=true;aidPatient=ref.id();aidBed=ref.bed();aidOwner=owner;destination=aidBed;}
        else {var h=hearth();destination=h==null ? null : h.getBlockPos();}
    }
    private boolean collectAid(Job job) {
        var h=hearth();var l=(ServerLevel)dog.level();if(!cargo.isEmpty() || !nurse() || !job.target().equals(h.getBlockPos()) || l.hasNeighborSignal(h.getBlockPos()))return false;
        if(!EventHooks.canEntityGrief(l,dog) || !ready() || hearth()!=h)return false;var patient=rescuePatient(job.patient(),false);if(patient==null || !HomeRoutes.visible(dog,patient))return false;
        for(int slot=0;slot<h.getContainerSize();slot++)if(h.getItem(slot).is(ModItems.GUAVA.get()) && CivicRecipes.ordinary(h.getItem(slot))) {
            var exact=h.getItem(slot).copyWithCount(1);
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).extract(slot,net.neoforged.neoforge.transfer.item.ItemResource.of(exact),1,tx)!=1 || !h.attached() || rescuePatient(job.patient(),false)!=patient)return false;
                cargo=exact;aidPatient=patient.getUUID();aidBed=job.patient().bed();aidOwner=parcelOwner=h.owner();destination=aidBed;firstAidCargo=true;grownCargo=false;tx.commit();return true;
            }
        }return false;
    }
    private boolean aidResident(Job job) {
        var h=hearth();var patient=rescuePatient(job.patient(),false);if(patient==null || !firstAidCargo || !patient.getUUID().equals(aidPatient) || !cargo.is(ModItems.GUAVA.get()) || cargo.getCount()!=1 || !Objects.equals(aidOwner,h.owner()) || !nurse() || dog.distanceToSqr(patient)>6.25 || !HomeRoutes.visible(dog,patient) || dog.level().hasNeighborSignal(h.getBlockPos()))return false;
        var original=cargo.copy();
        if(!EventHooks.canEntityGrief((ServerLevel)dog.level(),dog) || !ready() || hearth()!=h)return false;
        boolean healed=patient.combat().residentAid(()->dog.isAlive() && !dog.isRemoved() && ready() && nurse() && hearth()==h && h.attached() && Objects.equals(aidOwner,h.owner()) && !dog.level().hasNeighborSignal(h.getBlockPos()) && rescuePatient(job.patient(),patient.combat().receivingAid())==patient && dog.distanceToSqr(patient)<=6.25 && HomeRoutes.visible(dog,patient) && ItemStack.matches(original,cargo),()->cargo.shrink(1));
        if(healed && cargo.isEmpty())clearMedicine();return healed;
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
                    cargo=unit;parcelOwner=h.owner();aidPatient=patient.getUUID();aidBed=job.patient().bed();aidOwner=h.owner();destination=aidBed;grownCargo=false;tx.commit();return true;
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
    private void clearMedicine() {buildingCargo=fieldCargo=firstAidCargo=miningCargo=rescueCargo=false;furnaceSlot=-1;furnaceToken=null;parcelOwner=null;aidPatient=null;aidBed=null;aidOwner=null;destination=null;grownCargo=false;}
    private boolean returnSupplies() {
        var h=hearth();if(h==null || parcelOwner!=null && !parcelOwner.equals(h.owner()))return false;var original=cargo.copy();boolean grown=grownCargo;
        if(!EventHooks.canEntityGrief((ServerLevel)dog.level(),dog) || !ready() || hearth()!=h || !ItemStack.matches(cargo,original))return false;
        try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).insert(net.neoforged.neoforge.transfer.item.ItemResource.of(original),original.getCount(),tx)!=original.getCount() || !h.attached() || !ready() || !ItemStack.matches(cargo,original))return false;
            cargo=ItemStack.EMPTY;tx.commit();
        }
        if(grown)h.evidence("haul",original.getCount());return true;
    }
    private boolean returnMedicine() {
        var h=hearth();if(h==null || !Objects.equals(aidOwner,h.owner()) || rescueCargo && !rescueReady(h))return false;
        var exact=cargo.copy();var owner=aidOwner;
        medicalBusy=true;
        try {
            if(!EventHooks.canEntityGrief((ServerLevel)dog.level(),dog) || !ready() || hearth()!=h || !Objects.equals(owner,h.owner())
                    || !ItemStack.matches(exact,cargo) || rescueCargo && !rescueReady(h))return false;
            try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                if(net.neoforged.neoforge.transfer.item.VanillaContainerWrapper.of(h).insert(net.neoforged.neoforge.transfer.item.ItemResource.of(exact),exact.getCount(),tx)!=exact.getCount()
                        || !h.attached() || !ready() || hearth()!=h || !Objects.equals(owner,h.owner()) || !ItemStack.matches(exact,cargo) || rescueCargo && !rescueReady(h))return false;
                cargo=ItemStack.EMPTY;clearMedicine();tx.commit();return true;
            }
        } finally {medicalBusy=false;}
    }
    public boolean mayTakeSupplies(Player p) {return !cargo.isEmpty() && !medicalBusy && !operating && p.mayBuild() && !p.isSpectator() && p.level()==dog.level() && dog.distanceToSqr(p)<=36 && (parcelOwner!=null ? parcelOwner.equals(p.getUUID()) : aidOwner!=null ? aidOwner.equals(p.getUUID()) : hearth()!=null && authorized(p));}
    public boolean takeSupplies(Player p) {
        if(!mayTakeSupplies(p))return false;medicalBusy=true;var held=cargo;cargo=ItemStack.EMPTY;
        try {boolean given=p.getInventory().add(held);if(held.isEmpty())clearMedicine();else cargo=held;return given;}finally {medicalBusy=false;}
    }
    public ItemStack deathCargo() {release();var out=cargo;cargo=ItemStack.EMPTY;clearMedicine();return out;}
    private boolean craft(HearthTreeBlockEntity h,int index) {
        if(index>=0)return index<CivicRecipes.RecipeId.values().length && canCraft(CivicRecipes.RecipeId.values()[index]) && recipeNeeded(h,CivicRecipes.RecipeId.values()[index]) && CivicRecipes.craft(dog,h,CivicRecipes.RecipeId.values()[index]);
        // Compatibility for an already offered generic craft: re-evaluate concrete current needs.
        return Arrays.stream(CivicRecipes.RecipeId.values()).filter(recipe->canCraft(recipe) && recipeNeeded(h,recipe) && CivicRecipes.prepare(h,recipe).isPresent())
                .map(recipe->new Job(Kind.CRAFT,h.getBlockPos(),recipe.ordinal(),h.getBlockState())).sorted(Comparator.comparingDouble((Job job)->priority(h,job)).reversed())
                .findFirst().map(job->CivicRecipes.craft(dog,h,CivicRecipes.RecipeId.values()[job.index()])).orElse(false);
    }
    private boolean atWorkbench(CivicRecipes.RecipeId recipe) {
        var h=hearth();if(!h.wild().works().requiresWorkbench(recipe))return true;
        var at=h.wild().works().workbench();var from=dog.blockPosition();
        if(!h.wild().works().craftingAvailable(recipe) || dog.distanceToSqr(Vec3.atCenterOf(at))>16
                || !dog.level().hasChunksAt(new BlockPos(Math.min(from.getX(),at.getX()),Math.min(from.getY(),at.getY()),Math.min(from.getZ(),at.getZ())),new BlockPos(Math.max(from.getX(),at.getX()),Math.max(from.getY(),at.getY()),Math.max(from.getZ(),at.getZ()))))return false;
        var hit=dog.level().clip(new net.minecraft.world.level.ClipContext(dog.getEyePosition(),Vec3.atCenterOf(at),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,dog));
        return (hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS || hit.getBlockPos().equals(at)) && dog.learning().witnesses(h.getBlockPos(),at);
    }
    public boolean canCraft(CivicRecipes.RecipeId recipe) {return ready() && (artisan() && recipeAllowed(recipe) || dog.workshopLore().guides(hearth(),recipe)) && atWorkbench(recipe) && dog.learning().witnesses(hearth().getBlockPos(),hearth().getBlockPos());}
    private boolean dressingNeeded(HearthTreeBlockEntity h) {
        return h.count(ModItems.HEARTH_POULTICE.get())<4 && h.count(ModItems.GUAVA.get())>8 && h.count(Items.BONE_MEAL)>0 && h.count(Items.PAPER)>0 && h.count(Items.HONEYCOMB)>0
                && h.residents().stream().anyMatch(p->p!=dog && p.clinic().ready() && ClinicCare.restingAtHome(h,p) && HomeRoutes.visible(dog,p));
    }
    public static boolean litter(Bunnidog a,Bunnidog b,ItemStack seed) {
        var h=a.society().hearth();if(h==null || !b.society().belongs(h))return false;
        seed.set(ModItems.HEARTH_SITE.get(),h.site().toString());seed.set(ModItems.HEARTH_LINK.get(),GlobalPos.of(a.level().dimension(),h.getBlockPos()));return h.put(seed);
    }
    public static HearthTreeBlockEntity seedHome(ServerLevel l,ItemStack seed) {var p=seed.get(ModItems.HEARTH_LINK.get());return p!=null && p.dimension().equals(l.dimension()) && l.hasChunkAt(p.pos()) && l.getBlockEntity(p.pos()) instanceof HearthTreeBlockEntity h && communitySeed(seed,h) ? h : null;}
    public void returnCargo() {
        if(cargo.isEmpty())return;var h=hearth();
        if(h==null || parcelOwner!=null && !parcelOwner.equals(h.owner()) || !h.put(cargo))dog.spawnAtLocation((ServerLevel)dog.level(),cargo);
        cargo=ItemStack.EMPTY;clearMedicine();
    }
    public void save(ValueOutput o) {
        o.putBoolean("SocietyRescueParcel",rescueCargo);o.putBoolean("SocietyMiningParcel",miningCargo);
        if(furnaceSlot>=0 && furnaceToken!=null && !cargo.isEmpty()) {o.putInt("SocietyFurnaceSlot",furnaceSlot);o.putString("SocietyFurnaceToken",furnaceToken);}
        o.putBoolean("SocietyFirstAidParcel",firstAidCargo);o.putBoolean("SocietyFieldParcel",fieldCargo);o.putBoolean("SocietyBuildParcel",buildingCargo);
        if(parcelOwner!=null)o.putString("SocietyParcelOwner",parcelOwner.toString());
        if(lastSpecialty!=null)o.putString("SocietySpecialty",lastSpecialty.name());
        if(aidPatient!=null) {
            o.putString("SocietyAidPatient",aidPatient.toString());
            if(aidBed!=null)o.store("SocietyAidBed",BlockPos.CODEC,aidBed);
            if(aidOwner!=null)o.putString("SocietyAidOwner",aidOwner.toString());
        }
        if(home!=null && site!=null) {
            o.store("SocietyHome",GlobalPos.CODEC,home);o.putString("SocietySite",site.toString());o.putBoolean("SocietyBorn",child);
            o.putInt("SocietyTeaching",residentLessons);o.putInt("SocietyMeals",workSinceMeal);
            o.putInt("SocietyWait",(int)Math.clamp(nextWork-dog.level().getGameTime(),0L,80L));
        }
        if(!cargo.isEmpty()) {
            o.store("SocietyCargo",ItemStack.CODEC,cargo);
            if(destination!=null)o.store("SocietyDestination",BlockPos.CODEC,destination);
            o.putBoolean("SocietyGrownCargo",grownCargo);
        }
    }
    public void load(ValueInput i) {
        release();
        try {lastSpecialty=WorkSpecialty.valueOf(i.getStringOr("SocietySpecialty",""));}catch(IllegalArgumentException e){lastSpecialty=null;}
        buildingCargo=i.getBooleanOr("SocietyBuildParcel",false);fieldCargo=!buildingCargo && i.getBooleanOr("SocietyFieldParcel",false);
        firstAidCargo=!buildingCargo && !fieldCargo && i.getBooleanOr("SocietyFirstAidParcel",false);
        try {parcelOwner=UUID.fromString(i.getStringOr("SocietyParcelOwner",""));}catch(IllegalArgumentException e){parcelOwner=null;}
        medicalBusy=false;aidBed=i.read("SocietyAidBed",BlockPos.CODEC).orElse(null);
        try {aidPatient=UUID.fromString(i.getStringOr("SocietyAidPatient",""));}catch(IllegalArgumentException e){aidPatient=null;}
        try {aidOwner=UUID.fromString(i.getStringOr("SocietyAidOwner",""));}catch(IllegalArgumentException e){aidOwner=null;}
        home=i.read("SocietyHome",GlobalPos.CODEC).orElse(null);
        try {site=UUID.fromString(i.getStringOr("SocietySite",""));}catch(IllegalArgumentException e) {site=null;home=null;}
        child=i.getBooleanOr("SocietyBorn",false);residentLessons=i.getIntOr("SocietyTeaching",0)&((1<<Learning.Routine.values().length)-1);
        workSinceMeal=Math.clamp(i.getIntOr("SocietyMeals",0),0,6);cargo=i.read("SocietyCargo",ItemStack.CODEC).orElse(ItemStack.EMPTY);
        destination=i.read("SocietyDestination",BlockPos.CODEC).orElse(null);grownCargo=i.getBooleanOr("SocietyGrownCargo",false);
        nextWork=dog.level().getGameTime()+Math.clamp(i.getIntOr("SocietyWait",0),0,80);
        rescueCargo=i.getBooleanOr("SocietyRescueParcel",false) && !buildingCargo && !fieldCargo && !firstAidCargo
                && aidPatient!=null && aidBed!=null && aidOwner!=null && cargo.is(ModItems.TRAVEL_BASKET.get()) && cargo.getCount()==1;
        miningCargo=i.getBooleanOr("SocietyMiningParcel",false) && !buildingCargo && !fieldCargo && aidPatient==null && WildForaging.usablePickaxe(cargo);
        furnaceSlot=i.getIntOr("SocietyFurnaceSlot",-1);furnaceToken=i.getStringOr("SocietyFurnaceToken","");
        if(furnaceSlot<0 || furnaceSlot>1 || furnaceToken.isEmpty() || cargo.isEmpty() || buildingCargo || fieldCargo || miningCargo || aidPatient!=null) {furnaceSlot=-1;furnaceToken=null;}
    }
}
