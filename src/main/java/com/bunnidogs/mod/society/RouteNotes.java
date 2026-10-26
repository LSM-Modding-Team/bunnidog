package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.RegistryOps;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;

/** A finite household procedure in a real book. It records places, never stock or inherited skill. */
public record RouteNotes(UUID hearth,UUID author,DeliveryMemory.Route route) {
    private record Key(net.minecraft.core.HolderLookup.Provider registries,String data) {}
    private static final Map<Key,Optional<RouteNotes>> CACHE=new LinkedHashMap<>() {
        @Override protected boolean removeEldestEntry(Map.Entry<Key,Optional<RouteNotes>> entry) {return size()>64;}
    };
    public static final Codec<RouteNotes> CODEC=RecordCodecBuilder.<RouteNotes>create(i->i.group(
            Codec.intRange(1,1).optionalFieldOf("version",1).forGetter(n->1),UUIDUtil.STRING_CODEC.fieldOf("hearth").forGetter(RouteNotes::hearth),
            UUIDUtil.STRING_CODEC.fieldOf("author").forGetter(RouteNotes::author),DeliveryMemory.Route.CODEC.fieldOf("route").forGetter(RouteNotes::route))
            .apply(i,(version,hearth,author,route)->new RouteNotes(hearth,author,route)))
            .validate(n->recordable(n.route.filter()) ? DataResult.success(n) : DataResult.error(()->"Container and nested-record filters cannot be archived"));
    public static boolean recordable(ItemStack filter) {
        return !filter.isEmpty() && filter.getHoverName().getString().length()<=256 && !filter.has(ModItems.ROUTE_NOTES_DATA.get()) && !filter.has(DataComponents.CONTAINER)
                && !filter.has(DataComponents.BUNDLE_CONTENTS) && !filter.has(DataComponents.CHARGED_PROJECTILES)
                && !filter.has(DataComponents.WRITTEN_BOOK_CONTENT) && !filter.has(DataComponents.WRITABLE_BOOK_CONTENT) && !filter.has(DataComponents.CUSTOM_DATA);
    }
    public static boolean authorized(HearthTreeBlockEntity home,DeliveryMemory.Route route) {
        if(home==null || !home.attached() || home.owner()==null || route==null || !route.owner().equals(home.owner()))return false;
        var level=(net.minecraft.server.level.ServerLevel)home.getLevel();
        for(var stop:List.of(route.source(),route.target()))if(!stop.at().dimension().equals(level.dimension()) || stop.at().pos().distSqr(home.getBlockPos())>24*24
                || !DeliveryStores.allowed(level,DeliveryStores.resolve(level,stop),route.owner()))return false;
        return !route.source().overlaps(route.target()) && route.source().at().pos().distSqr(route.target().at().pos())<=32*32;
    }
    public boolean available(HearthTreeBlockEntity home) {return home!=null && hearth.equals(home.site()) && authorized(home,route);}
    public static boolean blank(ItemStack stack) {return stack.is(Items.WRITABLE_BOOK) && stack.getOrDefault(DataComponents.WRITABLE_BOOK_CONTENT,WritableBookContent.EMPTY).pages().isEmpty();}
    public static Optional<RouteNotes> read(ItemStack book,ServerLevel level) {
        if(!book.is(ModItems.ROUTE_NOTES.get()))return Optional.empty();var data=book.get(ModItems.ROUTE_NOTES_DATA.get());if(data==null || data.length()>32768)return Optional.empty();
        var key=new Key(level.registryAccess(),data);
        synchronized(CACHE) {
            var known=CACHE.get(key);if(known!=null)return known;
            Optional<RouteNotes> result;try {result=CODEC.parse(RegistryOps.create(JsonOps.INSTANCE,level.registryAccess()),JsonParser.parseString(data)).result();}catch(RuntimeException ignored){result=Optional.empty();}
            CACHE.put(key,result);return result;
        }
    }
    public ItemStack write(ItemStack blank,Bunnidog writer) {
        var encoded=CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE,writer.level().registryAccess()),this).result();if(encoded.isEmpty())return ItemStack.EMPTY;
        String data=encoded.get().toString();if(data.length()>32768)return ItemStack.EMPTY;
        var result=blank.transmuteCopy(ModItems.ROUTE_NOTES.get(),1);result.remove(DataComponents.WRITABLE_BOOK_CONTENT);
        result.set(ModItems.ROUTE_NOTES_DATA.get(),data);
        result.set(DataComponents.ITEM_NAME,Component.translatable("item.bunnidogs.route_notes.named",route.filter().getHoverName()));
        var from=route.source().at().pos();var to=route.target().at().pos();
        var page=Component.translatable("book.bunnidogs.route_notes.route",route.filter().getHoverName(),from.getX(),from.getY(),from.getZ(),to.getX(),to.getY(),to.getZ());
        result.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(Filterable.passThrough("Route"),writer.getName().getString(),0,List.of(Filterable.passThrough(page)),true));
        return result;
    }
}
