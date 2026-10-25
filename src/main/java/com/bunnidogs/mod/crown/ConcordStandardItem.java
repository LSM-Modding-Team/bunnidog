package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.expedition.PowerModes;
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
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,TooltipFlag flag) {super.appendHoverText(stack,context,display,lines,flag);PowerModes.tooltip(stack,lines);}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user) {return 30;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.BOW;}
    @Override public InteractionResult use(Level level,Player p,InteractionHand hand) {
        if(PowerModes.cycle(level,p,p.getItemInHand(hand)))return InteractionResult.SUCCESS;
        if(!p.isAlive() || p.isSpectator() || p.getCooldowns().isOnCooldown(p.getItemInHand(hand)) || !PowerModes.available(p,ModItems.HEART_SPOOL.get(),PowerModes.get(p.getItemInHand(hand))))return InteractionResult.FAIL;
        p.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user) {
        if(!(level instanceof ServerLevel server) || !(user instanceof Player p) || !p.isAlive() || p.getUseItem()!=stack || p.getItemInHand(p.getUsedItemHand())!=stack || p.getCooldowns().isOnCooldown(stack))return stack;
        int power=PowerModes.get(stack);if(!PowerModes.available(p,ModItems.HEART_SPOOL.get(),power))return stack;
        var encounters=server.getEntitiesOfClass(CrownedApex.class,p.getBoundingBox().inflate(70),LivingEntity::isAlive).stream().map(CrownEncounter::controller).filter(s->s!=null && s.active() && s.participant(p.getUUID())).distinct().toList();
        if(!PowerModes.pay(p,ModItems.HEART_SPOOL.get(),power))return stack;p.getCooldowns().addCooldown(stack,100);stack.hurtAndBreak(power,p,p.getUsedItemHand());
        int affected=0;
        for(var ally:server.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(10),e->e.isAlive() && !ApexSkills.family(e) && e.distanceToSqr(p)<=100)) {
            boolean friend=ally==p || p.isAlliedTo(ally) || ally instanceof Bunnidog dog && p.getUUID().equals(dog.trail().keeper())
                    || ally instanceof Player other && encounters.stream().anyMatch(s->s.participant(other.getUUID()));
            if(!friend || ally instanceof Bunnidog dog && dog.combatState()==3)continue;
            if(affected++>=16)break;
            float before=ally.getHealth();ally.heal(10*power);float paid=Math.clamp(ally.getHealth()-before,0,10*power);
            ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,80,power-1,false,true,true));
            server.sendParticles(ParticleTypes.HEART,ally.getX(),ally.getEyeY(),ally.getZ(),4,.25,.2,.25,0);
            if(p instanceof net.minecraft.server.level.ServerPlayer healer)com.bunnidogs.mod.society.HuntProgress.support(healer,ally,paid);
            if(ally!=p && paid>0)for(var seal:encounters)if(seal.contains(ally.position(),7))seal.credit(p.getUUID(),paid);
        }
        server.playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,net.minecraft.sounds.SoundSource.PLAYERS,1,1.25F);
        return stack;
    }
}
