package com.bunnidogs.mod.society;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.Bunnidog;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class SocietyEvents {
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void staff(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if(!(event.getItemStack().getItem() instanceof StewardStaffItem))return;
        var result=event.getItemStack().useOn(new net.minecraft.world.item.context.UseOnContext(event.getEntity(),event.getHand(),event.getHitVec()));
        event.setCanceled(true);event.setCancellationResult(result);
    }
    @SubscribeEvent public static void drops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if(event.getEntity() instanceof Bunnidog dog && dog.level() instanceof net.minecraft.server.level.ServerLevel level) {var stack=dog.society().deathCargo();if(!stack.isEmpty())event.getDrops().add(new ItemEntity(level,dog.getX(),dog.getY(),dog.getZ(),stack));}
    }
    private SocietyEvents() {}
}
