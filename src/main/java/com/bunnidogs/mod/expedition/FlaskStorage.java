package com.bunnidogs.mod.expedition;

import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.*;

/** World-owned unbounded drink stacks. Items sync a small count/top snapshot, never a growing network payload. */
public final class FlaskStorage extends SavedData {
    private final Map<String,List<ItemStack>> drinks=new HashMap<>();
    public static final Codec<FlaskStorage> CODEC=Codec.unboundedMap(Codec.STRING,ItemStack.CODEC.listOf()).xmap(FlaskStorage::new,s->s.drinks);
    private static final SavedDataType<FlaskStorage> TYPE=new SavedDataType<>(net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","abyssal_flasks"),FlaskStorage::new,CODEC);
    private FlaskStorage() {}
    private FlaskStorage(Map<String,List<ItemStack>> data) {data.forEach((id,list)->drinks.put(id,new ArrayList<>(list)));}
    public static FlaskStorage get(ServerLevel level) {return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);}
    List<ItemStack> contents(String id) {return drinks.computeIfAbsent(id,k->new ArrayList<>());}
    public ItemStack top(String id) {var list=contents(id);return list.isEmpty() ? ItemStack.EMPTY : list.getLast().copy();}
    public int size(String id) {return contents(id).size();}
    public void push(String id,ItemStack item) {contents(id).add(item.copyWithCount(1));setDirty();}
    public ItemStack pop(String id) {var list=contents(id);if(list.isEmpty())return ItemStack.EMPTY;setDirty();return list.removeLast();}
}
