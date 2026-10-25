package com.bunnidogs.mod.society;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

/** A pre-Apex support option; only accepted healing of an engaged ally earns a hunt contribution. */
public final class HearthPoulticeItem extends Item {
    public HearthPoulticeItem(Properties p) {super(p);}
    @Override public InteractionResult interactLivingEntity(ItemStack stack,Player p,LivingEntity ally,InteractionHand hand) {
        if(p.getItemInHand(hand)!=stack || p.level()!=ally.level() || !p.isAlive() || p.isSpectator() || !ally.isAlive() || p.distanceToSqr(ally)>16 || ally instanceof com.bunnidogs.mod.entity.Apex || ally instanceof com.bunnidogs.mod.entity.ApexMinion || ally.getHealth()>=ally.getMaxHealth() || p.getCooldowns().isOnCooldown(stack))return InteractionResult.PASS;
        if(p.level().isClientSide())return InteractionResult.SUCCESS;
        boolean creative=p.hasInfiniteMaterials();var paid=stack.copyWithCount(1);stack.consume(1,p);float before=ally.getHealth();ally.heal(6);float actual=Math.clamp(ally.getHealth()-before,0,6);
        if(actual<=0) {if(!creative && !p.getInventory().add(paid))p.drop(paid,false);return InteractionResult.FAIL;}
        p.getCooldowns().addCooldown(p.getCooldowns().getCooldownGroup(paid),100);if(p instanceof ServerPlayer server)HuntProgress.support(server,ally,actual);return InteractionResult.SUCCESS;
    }
}
