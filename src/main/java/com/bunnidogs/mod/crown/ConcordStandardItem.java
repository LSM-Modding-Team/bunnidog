package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Paid, deliberate party healing. Only real healing of another engaged ally earns support credit. */
public final class ConcordStandardItem extends Item {
    public ConcordStandardItem(Properties properties) {super(properties);}
    private static ItemStack spool(Player p) {for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(ModItems.HEART_SPOOL.get()))return p.getInventory().getItem(i);return ItemStack.EMPTY;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user) {return 30;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.BOW;}
    @Override public InteractionResult use(Level level,Player p,InteractionHand hand) {
        if(!p.isAlive() || p.isSpectator() || p.getCooldowns().isOnCooldown(p.getItemInHand(hand)) || !p.hasInfiniteMaterials() && spool(p).isEmpty())return InteractionResult.FAIL;
        p.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user) {
        if(!(level instanceof ServerLevel server) || !(user instanceof Player p) || !p.isAlive() || p.getUseItem()!=stack || p.getItemInHand(p.getUsedItemHand())!=stack || p.getCooldowns().isOnCooldown(stack))return stack;
        var ammo=spool(p);if(!p.hasInfiniteMaterials() && ammo.isEmpty())return stack;
        var encounters=server.getEntitiesOfClass(CrownedApex.class,p.getBoundingBox().inflate(70),LivingEntity::isAlive).stream().map(CrownEncounter::controller).filter(s->s!=null && s.active() && s.participant(p.getUUID())).distinct().toList();
        ammo.consume(1,p);p.getCooldowns().addCooldown(stack,100);stack.hurtAndBreak(1,p,p.getUsedItemHand());
        int affected=0;
        for(var ally:server.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(10),e->e.isAlive() && !ApexSkills.family(e) && e.distanceToSqr(p)<=100)) {
            boolean friend=ally==p || p.isAlliedTo(ally) || ally instanceof Bunnidog dog && p.getUUID().equals(dog.trail().keeper())
                    || ally instanceof Player other && encounters.stream().anyMatch(s->s.participant(other.getUUID()));
            if(!friend || ally instanceof Bunnidog dog && dog.combatState()==3)continue;
            if(affected++>=16)break;
            float before=ally.getHealth();ally.heal(6);float paid=Math.clamp(ally.getHealth()-before,0,6);
            ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,80,0,false,true,true));
            server.sendParticles(ParticleTypes.HEART,ally.getX(),ally.getEyeY(),ally.getZ(),4,.25,.2,.25,0);
            if(ally!=p && paid>0)for(var seal:encounters)if(seal.contains(ally.position(),7))seal.credit(p.getUUID(),paid);
        }
        server.playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,net.minecraft.sounds.SoundSource.PLAYERS,1,1.25F);
        return stack;
    }
}
