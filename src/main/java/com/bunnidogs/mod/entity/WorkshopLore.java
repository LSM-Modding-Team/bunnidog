package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.*;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Real recipe experience can be preserved in a paid book for another resident's later practical work. */
public final class WorkshopLore {
    private final Bunnidog dog;
    private final List<String> proven=new ArrayList<>();
    private boolean busy;
    WorkshopLore(Bunnidog dog) {this.dog=dog;}
    private boolean receptive(HearthTreeBlockEntity home) {
        return home!=null && home.attached() && dog.level() instanceof ServerLevel && home.owner()!=null && !home.isLocked() && !dog.level().hasNeighborSignal(home.getBlockPos()) && dog.society().hearth()==home && dog.society().ready() && dog.society().cargoEmpty() && dog.learning().canDiscover()
                && !dog.workshopLessons().apprenticing() && !dog.courierLore().occupied() && !dog.deliveries().watching() && dog.deliveries().cargo().isEmpty() && dog.deliveries().route()==null;
    }
    public boolean guides(HearthTreeBlockEntity home,CivicRecipes.RecipeId recipe) {
        if(!receptive(home) || !dog.society().practiceReady() || dog.learning().knowledge().knows(Learning.Routine.CRAFT)
                || CapabilityRules.potential(dog.mind(),CapabilityRules.Task.CRAFTING)!=CapabilityRules.Potential.INDEPENDENT)return false;
        for(int slot=0;slot<home.getContainerSize();slot++) {var notes=CraftNotes.read(home.getItem(slot),(ServerLevel)dog.level()).orElse(null);if(notes!=null && notes.recipe()==recipe && notes.available(home))return true;}return false;
    }
    /** Called only after the native recipe transaction commits. */
    public void completed(HearthTreeBlockEntity home,CivicRecipes.Plan plan) {
        if(home==null || home.owner()==null || dog.society().hearth()!=home || dog.level().isClientSide())return;
        var level=(ServerLevel)dog.level();var encoded=CraftNotes.example(home,dog,plan).data(level);
        if(encoded.isPresent()) {proven.removeIf(raw->CraftNotes.readData(raw,level).map(n->n.recipe()==plan.id()).orElse(false));if(proven.size()<32)proven.add(encoded.get());}
        if(guides(home,plan.id()))dog.learning().workshopPractice(Learning.Routine.CRAFT);
    }
    private String nextRecipe(HearthTreeBlockEntity home) {
        if(!receptive(home) || !dog.learning().knowledge().independent(dog.mind(),Learning.Routine.CRAFT)
                || CapabilityRules.potential(dog.mind(),CapabilityRules.Task.TEACHING)!=CapabilityRules.Potential.INDEPENDENT)return null;
        var existing=EnumSet.noneOf(CivicRecipes.RecipeId.class);int books=0;
        for(int slot=0;slot<home.getContainerSize();slot++) {var item=home.getItem(slot);if(item.is(ModItems.CRAFT_NOTES.get()))books++;var notes=CraftNotes.read(item,(ServerLevel)dog.level()).orElse(null);if(notes!=null && notes.available(home))existing.add(notes.recipe());}
        if(books>=8)return null;
        for(var raw:proven) {var notes=CraftNotes.readData(raw,(ServerLevel)dog.level()).orElse(null);if(notes!=null && notes.available(home) && !existing.contains(notes.recipe()))return raw;}return null;
    }
    public SocietyWork.Job archiveJob(HearthTreeBlockEntity home) {
        if(busy || nextRecipe(home)==null)return null;
        for(int slot=0;slot<home.getContainerSize();slot++)if(RouteNotes.blank(home.getItem(slot)))return new SocietyWork.Job(SocietyWork.Kind.RECORD_CRAFT,home.getBlockPos(),slot,home.getBlockState());return null;
    }
    public boolean record(HearthTreeBlockEntity home,int slot) {
        if(busy || home==null || slot<0 || slot>=home.getContainerSize())return false;var example=nextRecipe(home);if(example==null)return false;var notes=CraftNotes.readData(example,(ServerLevel)dog.level()).orElseThrow();
        var original=home.getItem(slot).copy();if(!RouteNotes.blank(original))return false;var result=notes.write(original,dog);if(result.isEmpty())return false;
        busy=true;
        try {
            if(!EventHooks.canEntityGrief((ServerLevel)dog.level(),dog) || !Objects.equals(nextRecipe(home),example) || !notes.available(home) || !ItemStack.matches(original,home.getItem(slot)))return false;
            var items=VanillaContainerWrapper.of(home);
            try(var tx=Transaction.openRoot()) {
                if(items.extract(slot,ItemResource.of(original),1,tx)!=1 || items.insert(slot,ItemResource.of(result),1,tx)!=1 || !home.attached()
                        || !receptive(home) || !notes.available(home))return false;tx.commit();return true;
            }
        } finally {busy=false;}
    }
    public void save(ValueOutput out) {out.store("CraftProven",Codec.STRING.listOf(),List.copyOf(proven));}
    void load(ValueInput in) {proven.clear();for(var data:in.read("CraftProven",Codec.STRING.listOf()).orElse(List.of()))if(data.length()<=4096 && proven.size()<32)proven.add(data);busy=false;}
}
