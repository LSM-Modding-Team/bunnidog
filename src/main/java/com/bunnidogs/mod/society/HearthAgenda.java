package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.*;

/** A finite saved public instruction, made from actual local needs; no executable scripts or virtual resources. */
public final class HearthAgenda {
    public enum Need { FOOD,CARE,TOOL,HOUSING,FAMILY,ROOTS,RESERVE }
    private final HearthTreeBlockEntity home;
    private Need need;
    private UUID author;
    private List<CivicRecipes.RecipeId> steps=List.of();
    private long made;
    private long observedAt=Long.MIN_VALUE;private Need observed;
    private final Map<CivicRecipes.RecipeId,Boolean> affordable=new EnumMap<>(CivicRecipes.RecipeId.class);
    public void invalidate() {observedAt=Long.MIN_VALUE;affordable.clear();}
    public HearthAgenda(HearthTreeBlockEntity home) {this.home=home;}
    public Need need() {return need;}
    public UUID author() {return author;}
    public List<CivicRecipes.RecipeId> steps() {return steps;}
    private Need observedNeed() {
        long now=home.getLevel().getGameTime();if(observedAt==now)return observed;observedAt=now;affordable.clear();observed=scanNeed();return observed;
    }
    private Need scanNeed() {
        if(home.count(ModItems.GUAVA.get())<16)return Need.FOOD;
        if(home.residents().stream().anyMatch(d->(d.clinic().ready() || d.combat().canResidentAid()) && d.homeNest()!=null && d.homeNest().distSqr(d.blockPosition())<=9))return Need.CARE;
        if(home.count(Items.WOODEN_AXE)==0)return Need.TOOL;
        if(home.construction().cells().stream().anyMatch(home.construction()::canBuild))return Need.HOUSING;
        if(home.sites().stream().anyMatch(at->home.getLevel().hasChunkAt(at) && home.getLevel().getBlockEntity(at) instanceof com.bunnidogs.mod.block.NurseryCradleBlockEntity cradle && cradle.seed().isPresent()))return Need.FAMILY;
        return home.ripe() ? Need.ROOTS : Need.RESERVE;
    }
    private List<CivicRecipes.RecipeId> dependencies(Need purpose) {
        var result=new ArrayList<CivicRecipes.RecipeId>();
        boolean axe=home.count(Items.WOODEN_AXE)==0;
        boolean nest=purpose==Need.HOUSING && home.construction().cells().stream().anyMatch(c->c.item()==ModItems.NEST.get() && home.construction().canBuild(c)) && CivicRecipes.count(home,ModItems.NEST.get())==0;
        if(purpose==Need.CARE && home.count(ModItems.HEARTH_POULTICE.get())<4)result.add(CivicRecipes.RecipeId.DRESSING);
        if((purpose==Need.FOOD || purpose==Need.TOOL) && axe) {
            if(CivicRecipes.count(home,ModItems.GUAVA_PLANKS.get())<3)result.add(CivicRecipes.RecipeId.PLANKS);
            if(CivicRecipes.count(home,Items.STICK)<2) {if(CivicRecipes.count(home,ModItems.GUAVA_PLANKS.get())<2 && !result.contains(CivicRecipes.RecipeId.PLANKS))result.add(CivicRecipes.RecipeId.PLANKS);result.add(CivicRecipes.RecipeId.STICKS);}
            result.add(CivicRecipes.RecipeId.AXE);
        }
        if(purpose==Need.HOUSING) {
            if(CivicRecipes.count(home,ModItems.GUAVA_PLANKS.get())<8)result.add(CivicRecipes.RecipeId.PLANKS);
            if(nest)result.add(CivicRecipes.RecipeId.NEST);
        }
        if(purpose==Need.ROOTS && CivicRecipes.count(home,ModItems.GRAFTING_FRAME.get())==0) {
            if(CivicRecipes.count(home,ModItems.GUAVA_PLANKS.get())<4)result.add(CivicRecipes.RecipeId.PLANKS);result.add(CivicRecipes.RecipeId.FRAME);
        }
        return List.copyOf(result.stream().distinct().limit(8).toList());
    }
    public boolean wantsPlan() {return home.attached() && (need==null || made>home.getLevel().getGameTime() || home.getLevel().getGameTime()-made>=400) && (need!=observedNeed() || !steps.equals(dependencies(observedNeed())) || home.getLevel().getGameTime()-made>=2400);}
    public boolean write(Bunnidog planner) {
        if(!home.attached() || planner.society().hearth()!=home || !planner.society().ready() || planner.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(home.getBlockPos()))>16
                || !planner.learning().knowledge().independent(planner.mind(),Learning.Routine.GARDEN) || CapabilityRules.potential(planner.mind(),CapabilityRules.Task.PLANNING)!=CapabilityRules.Potential.INDEPENDENT || !wantsPlan())return false;
        need=observedNeed();steps=dependencies(need);author=planner.getUUID();made=home.getLevel().getGameTime();home.setChanged();return true;
    }
    private boolean missing(CivicRecipes.RecipeId step) {return switch(step) {
        case PLANKS -> CivicRecipes.count(home,ModItems.GUAVA_PLANKS.get())<(need==Need.HOUSING ? 8 : need==Need.ROOTS ? 4 : CivicRecipes.count(home,Items.STICK)<2 ? 5 : 3);
        case STICKS -> CivicRecipes.count(home,Items.STICK)<2;
        case AXE -> home.count(Items.WOODEN_AXE)==0;
        case NEST -> CivicRecipes.count(home,ModItems.NEST.get())==0;
        case FRAME -> CivicRecipes.count(home,ModItems.GRAFTING_FRAME.get())==0;
        case DRESSING -> home.count(ModItems.HEARTH_POULTICE.get())<4;
        default -> false;
    };}
    /** Follow a still-relevant instruction after the author leaves; completion never comes from the record. */
    public int boost(SocietyWork.Job job) {
        if(need==null || !home.attached() || observedNeed()!=need)return 0;
        if(job.kind()==SocietyWork.Kind.CRAFT && job.index()>=0 && job.index()<CivicRecipes.RecipeId.values().length) {
            var recipe=CivicRecipes.RecipeId.values()[job.index()];
            for(var step:steps)if(missing(step) && affordable.computeIfAbsent(step,id->CivicRecipes.prepare(home,id).isPresent()))return step==recipe ? 35 : 0;
        }
        return switch(need) {
            case FOOD -> switch(job.kind()) {case PRUNE,HAUL -> 30;default -> 0;};
            case CARE -> switch(job.kind()) {case CARE,MEDICINE,FETCH_AID -> 35;default -> 0;};
            case HOUSING -> switch(job.kind()) {case FETCH_BUILD,BUILD,HARVEST_FIELD,GATHER_FIELD,FETCH_SEED,SOW_FIELD -> 25;default -> 0;};
            case FAMILY -> switch(job.kind()) {case SEED,NURTURE,HATCH -> 25;default -> 0;};
            case ROOTS -> switch(job.kind()) {case TEND,COMPOST -> 25;default -> 0;};
            default -> 0;
        };
    }
    public void save(ValueOutput out) {if(need!=null)out.putString("HouseholdNeed",need.name());if(author!=null)out.putString("HouseholdPlanner",author.toString());out.putLong("HouseholdPlanTick",made);out.store("HouseholdSteps",com.mojang.serialization.Codec.STRING.listOf(),steps.stream().map(Enum::name).toList());}
    public void load(ValueInput in) {
        try {need=Need.valueOf(in.getStringOr("HouseholdNeed",""));}catch(IllegalArgumentException e){need=null;}
        try {author=UUID.fromString(in.getStringOr("HouseholdPlanner",""));}catch(IllegalArgumentException e){author=null;}
        steps=in.read("HouseholdSteps",com.mojang.serialization.Codec.STRING.listOf()).orElse(List.of()).stream().map(key->{try{return CivicRecipes.RecipeId.valueOf(key);}catch(IllegalArgumentException e){return null;}}).filter(Objects::nonNull).distinct().limit(8).toList();made=Math.max(0,in.getLongOr("HouseholdPlanTick",0));invalidate();
    }
}
