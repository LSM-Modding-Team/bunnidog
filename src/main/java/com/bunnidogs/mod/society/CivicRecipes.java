package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.registry.ModItems;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Finite practical recipe vocabulary, executed by the real recipe manager and common-store journal. */
public final class CivicRecipes {
    public enum RecipeId { PLANKS,STICKS,AXE,FRAME,NEST,INCENSE,DRESSING,COPPICE }
    public record Plan(RecipeId id,CraftingRecipe recipe,CraftingInput input,ItemStack result,List<ItemStack> remainders) {}
    private CivicRecipes() {}
    public static boolean ordinary(ItemStack stack) {if(stack.isEmpty())return false;var plain=stack.copyWithCount(1);plain.remove(ModItems.FARM_ORIGIN.get());return ItemStack.isSameItemSameComponents(plain,new ItemStack(stack.getItem()));}
    public static int count(HearthTreeBlockEntity home,Item item) {int total=0;for(int slot=0;slot<home.getContainerSize();slot++) {var stack=home.getItem(slot);if(stack.is(item) && ordinary(stack))total+=stack.getCount();}return total;}
    private static Predicate<ItemStack> item(Item item) {return stack->stack.is(item);}
    public static Optional<Plan> prepare(HearthTreeBlockEntity home,RecipeId id) {
        if(!home.attached())return Optional.empty();var level=(ServerLevel)home.getLevel();
        Predicate<ItemStack> planks=stack->stack.is(ItemTags.PLANKS);
        String key,pattern;int width;Map<Character,Predicate<ItemStack>> legend;
        switch(id) {
            case PLANKS -> {key="bunnidogs:guava_planks";pattern="L";width=1;legend=Map.of('L',item(ModItems.GUAVA_LOG.get()));}
            case STICKS -> {key="minecraft:stick";pattern="PP";width=1;legend=Map.of('P',planks);}
            case AXE -> {key="minecraft:wooden_axe";pattern="PPPS S";width=2;legend=Map.of('P',planks,'S',item(Items.STICK));}
            case FRAME -> {key="bunnidogs:grafting_frame";pattern="PBPPBP";width=3;legend=Map.of('P',planks,'B',item(Items.BONE_MEAL));}
            case NEST -> {key="bunnidogs:nest";pattern="WWWPPP";width=3;legend=Map.of('P',planks,'W',item(Items.WHEAT));}
            case INCENSE -> {key="bunnidogs:hearth_incense";pattern="GGGP";width=2;legend=Map.of('P',planks,'G',item(ModItems.GUAVA.get()));}
            case DRESSING -> {key="bunnidogs:hearth_poultice";pattern=" G BPH";width=3;legend=Map.of('G',item(ModItems.GUAVA.get()),'B',item(Items.BONE_MEAL),'P',item(Items.PAPER),'H',item(Items.HONEYCOMB));}
            case COPPICE -> {key="bunnidogs:guava_coppice";pattern="BS";width=2;legend=Map.of('B',item(Items.BONE_MEAL),'S',item(ModItems.GUAVA_SAPLING.get()));}
            default -> throw new IllegalStateException();
        }
        var reference=level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE,Identifier.parse(key))).orElse(null);
        if(reference==null || !(reference.value() instanceof CraftingRecipe recipe))return Optional.empty();
        int[] left=new int[home.getContainerSize()];for(int i=0;i<left.length;i++)left[i]=home.getItem(i).getCount();var grid=new ArrayList<ItemStack>();
        for(char part:pattern.toCharArray()) {
            if(part==' ') {grid.add(ItemStack.EMPTY);continue;}var accepts=legend.get(part);ItemStack chosen=ItemStack.EMPTY;
            for(int slot=0;slot<left.length;slot++)if(left[slot]>0 && ordinary(home.getItem(slot)) && accepts.test(home.getItem(slot))) {chosen=home.getItem(slot).copyWithCount(1);left[slot]--;break;}
            if(chosen.isEmpty())return Optional.empty();grid.add(chosen);
        }
        var input=CraftingInput.of(width,pattern.length()/width,List.copyOf(grid));if(!recipe.matches(input,level))return Optional.empty();var result=recipe.assemble(input);
        return result.isEmpty() ? Optional.empty() : Optional.of(new Plan(id,recipe,input,result,List.copyOf(recipe.getRemainingItems(input))));
    }
    public static boolean craft(Bunnidog dog,HearthTreeBlockEntity home,RecipeId id) {
        var plan=prepare(home,id).orElse(null);if(plan==null)return false;
        var level=(ServerLevel)dog.level();
        if(!EventHooks.canEntityGrief(level,dog) || !dog.society().ready() || dog.society().hearth()!=home || level.hasNeighborSignal(home.getBlockPos()))return false;
        // Callbacks may change the recipe or stock. Only the freshly validated exact inputs are authoritative.
        var current=prepare(home,id).orElse(null);if(current==null)return false;var items=VanillaContainerWrapper.of(home);
        try(var tx=Transaction.openRoot()) {
            for(var input:current.input.items())if(!input.isEmpty() && items.extract(ItemResource.of(input),1,tx)!=1)return false;
            if(items.insert(ItemResource.of(current.result),current.result.getCount(),tx)!=current.result.getCount())return false;
            for(var remaining:current.remainders)if(!remaining.isEmpty() && items.insert(ItemResource.of(remaining),remaining.getCount(),tx)!=remaining.getCount())return false;
            if(!home.attached() || dog.society().hearth()!=home || !dog.society().ready())return false;tx.commit();
        }
        if(id==RecipeId.FRAME)home.evidence("frame",1);return true;
    }
}
