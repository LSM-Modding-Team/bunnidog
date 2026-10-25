package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.society.HearthTreeBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;

/** Explicitly observed storage only. Journals alias the original inventories, including both chest halves. */
public final class DeliveryStores {
    public record Stamp(BlockPos pos,String token,String block) {
        static final Codec<Stamp> CODEC=RecordCodecBuilder.create(i->i.group(BlockPos.CODEC.fieldOf("pos").forGetter(Stamp::pos),Codec.STRING.fieldOf("token").forGetter(Stamp::token),Codec.STRING.fieldOf("block").forGetter(Stamp::block)).apply(i,Stamp::new));
    }
    public record Stop(GlobalPos at,List<Stamp> stores) {
        public static final Codec<Stop> CODEC=RecordCodecBuilder.create(i->i.group(GlobalPos.CODEC.fieldOf("at").forGetter(Stop::at),Stamp.CODEC.listOf(1,2).fieldOf("stores").forGetter(Stop::stores)).apply(i,Stop::new));
        public Stop {stores=List.copyOf(stores);}
        public boolean overlaps(Stop other) {return at.dimension().equals(other.at.dimension()) && stores.stream().anyMatch(s->other.stores.stream().anyMatch(t->s.pos.equals(t.pos)));}
    }
    public record Live(Stop stop,ResourceHandler<ItemResource> items,List<BlockEntity> entities) {
        public boolean current(ServerLevel level) {var now=resolve(level,stop);return now!=null && now.entities.equals(entities);}
    }
    private static final String TOKEN="BunnidogDeliveryStore";
    private static List<BlockEntity> stores(ServerLevel level,BlockPos pos) {
        if(!level.hasChunksAt(pos.offset(-1,0,-1),pos.offset(1,0,1)))return List.of();
        var be=level.getBlockEntity(pos);if(!(be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof HearthTreeBlockEntity || be instanceof FeedingTroughBlockEntity))return List.of();
        var out=new ArrayList<BlockEntity>();out.add(be);var state=level.getBlockState(pos);
        if(be instanceof ChestBlockEntity && state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE)!=ChestType.SINGLE) {
            var other=level.getBlockEntity(pos.relative(ChestBlock.getConnectedDirection(state)));if(!(other instanceof ChestBlockEntity))return List.of();out.add(other);
        }
        out.sort(Comparator.comparingLong(e->e.getBlockPos().asLong()));return out;
    }
    private static boolean accessible(BlockEntity be,UUID owner) {
        if(be instanceof BaseContainerBlockEntity c && c.isLocked())return false;
        if(be instanceof HearthTreeBlockEntity h && h.owner()!=null && !h.owner().equals(owner))return false;
        return true;
    }
    public static Live observe(ServerLevel level,BlockPos pos,Player owner,AbstractContainerMenu menu) {
        var list=stores(level,pos);if(list.isEmpty() || list.stream().anyMatch(be->!accessible(be,owner.getUUID()) || !level.mayInteract(owner,be.getBlockPos())))return null;
        boolean actual=menu instanceof ChestMenu chest && list.stream().allMatch(be->chest.getContainer()==be || chest.getContainer() instanceof net.minecraft.world.CompoundContainer both && both.contains((Container)be))
                || menu instanceof com.bunnidogs.mod.menu.CareMenu care && care.deliveryStation()==list.getFirst();
        if(!actual)return null;
        var stamps=new ArrayList<Stamp>();for(var be:list) {
            var data=be.getPersistentData();String id=data.getStringOr(TOKEN,"");if(id.isEmpty()) {id=UUID.randomUUID().toString();data.putString(TOKEN,id);be.setChanged();}
            stamps.add(new Stamp(be.getBlockPos(),id,BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).toString()));
        }
        var stop=new Stop(GlobalPos.of(level.dimension(),pos),stamps);return live(stop,list);
    }
    public static Live resolve(ServerLevel level,Stop stop) {
        if(!stop.at.dimension().equals(level.dimension()))return null;var list=stores(level,stop.at.pos());if(list.size()!=stop.stores.size())return null;
        for(int i=0;i<list.size();i++) {var be=list.get(i);var stamp=stop.stores.get(i);if(!be.getBlockPos().equals(stamp.pos) || !stamp.token.equals(be.getPersistentData().getStringOr(TOKEN,"")) || !stamp.block.equals(BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).toString()))return null;}
        return live(stop,list);
    }
    private static Live live(Stop stop,List<BlockEntity> list) {
        var handlers=new ArrayList<ResourceHandler<ItemResource>>();for(var be:list)handlers.add(be instanceof FeedingTroughBlockEntity t ? t.automation() : VanillaContainerWrapper.of((Container)be));
        return new Live(stop,new CombinedResourceHandler<>(handlers),List.copyOf(list));
    }
    public static boolean allowed(ServerLevel level,Live live,UUID owner) {
        if(live==null)return false;for(var be:live.entities) {
            if(!level.getWorldBorder().isWithinBounds(be.getBlockPos()) || !accessible(be,owner) || level.hasNeighborSignal(be.getBlockPos()))return false;
            if(be instanceof ChestBlockEntity && be.getBlockState().getBlock() instanceof ChestBlock chest && ChestBlock.getContainer(chest,be.getBlockState(),level,be.getBlockPos(),false)==null)return false;
            if(be instanceof FeedingTroughBlockEntity t && (!t.getBlockState().getValue(FeedingTroughBlock.TENDING) || t.residentsPaused()))return false;
        }return true;
    }
    private DeliveryStores() {}
}
