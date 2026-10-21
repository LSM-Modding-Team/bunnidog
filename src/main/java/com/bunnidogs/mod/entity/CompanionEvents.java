package com.bunnidogs.mod.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** PvE crossfire and native, local participating-keeper credit; no synthetic loot/advancements. */
public final class CompanionEvents {
    private CompanionEvents() {}
    private static boolean whistle(Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.entity.Entity target) {
        if (player.level().isClientSide() || !player.mayBuild() || !player.getItemInHand(hand).is(com.bunnidogs.mod.registry.ModItems.TRAIL_WHISTLE.get())) return false;
        if (player.isShiftKeyDown() && target instanceof Bunnidog dog) dog.trail().use(player,hand);
        else TrailCompanion.call(player,hand);
        return true;
    }
    public static void block(PlayerInteractEvent.RightClickBlock event) {
        if (whistle(event.getEntity(),event.getHand(),null)) {
            event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }
    public static void entity(PlayerInteractEvent.EntityInteract event) {
        if (whistle(event.getEntity(),event.getHand(),event.getTarget())) {
            event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }
    public static void projectile(ProjectileImpactEvent event) {
        if (event.getProjectile().getOwner() instanceof Player && event.getRayTraceResult() instanceof EntityHitResult hit
                && hit.getEntity() instanceof Bunnidog dog && dog.combat().bonded()) {
            event.setCanceled(true);
        }
    }
    public static void credit(LivingDamageEvent.Pre event) {
        if (event.getNewDamage()<=0 || !(event.getSource().getEntity() instanceof Bunnidog dog)
                || !dog.combat().selected() || !dog.combat().fighting()) return;
        var keeper=dog.trail().keeperPlayer(); var enemy=event.getEntity();
        if (keeper!=null && dog.distanceToSqr(keeper)<=Math.pow(com.bunnidogs.mod.genetics.Trail.hearing(dog.genome(),dog.stats()),2)
                && (dog.combat().focusedOn(enemy) || keeper.getLastHurtMob()==enemy && keeper.tickCount-keeper.getLastHurtMobTimestamp()<100
                    || keeper.getLastHurtByMob()==enemy && keeper.tickCount-keeper.getLastHurtByMobTimestamp()<100))
            enemy.setLastHurtByPlayer(keeper,100);
    }
}
