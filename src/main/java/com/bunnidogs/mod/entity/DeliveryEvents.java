package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.BunnidogsMod;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.*;

/** Observes ordinary native container gestures, after protection has allowed the actual interaction. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class DeliveryEvents {
    private static List<Bunnidog> learners(Player p) {return p.level().getEntitiesOfClass(Bunnidog.class,p.getBoundingBox().inflate(12),d->d.deliveries().observes(p)).stream().limit(4).toList();}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void click(PlayerInteractEvent.RightClickBlock event) {if(event.getEntity() instanceof ServerPlayer p)for(var dog:learners(p))dog.deliveries().click(p,event.getPos());}
    @SubscribeEvent public static void open(PlayerContainerEvent.Open event) {if(event.getEntity() instanceof ServerPlayer p)for(var dog:learners(p))dog.deliveries().opened(p,event.getContainer());}
    @SubscribeEvent public static void close(PlayerContainerEvent.Close event) {if(event.getEntity() instanceof ServerPlayer p)for(var dog:learners(p))dog.deliveries().closed(p,event.getContainer());}
    @SubscribeEvent public static void drops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {if(event.getEntity() instanceof Bunnidog dog && dog.level() instanceof net.minecraft.server.level.ServerLevel level) {var cargo=dog.deliveries().deathCargo();if(!cargo.isEmpty())event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(level,dog.getX(),dog.getY(),dog.getZ(),cargo));}}
    private DeliveryEvents() {}
}
