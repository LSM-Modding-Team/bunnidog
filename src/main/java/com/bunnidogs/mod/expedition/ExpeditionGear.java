package com.bunnidogs.mod.expedition;

import com.bunnidogs.mod.registry.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.*;

/** Equipped benefits and successful-totem retaliation. No changes to the accepted enemy profiles. */
public final class ExpeditionGear {
    private static final Identifier HEARTS=Identifier.fromNamespaceAndPath("bunnidogs","heartbound_armor");
    public static int pieces(LivingEntity player) {
        int count=0;for(var slot:EquipmentSlot.values())if(slot.isArmor()) {
            var item=player.getItemBySlot(slot).getItem();if(ModItems.HEARTBOUND_ARMOR.values().stream().anyMatch(entry->entry.get()==item))count++;
        }return count;
    }
    public static void armor(ServerPlayer player) {
        var attr=player.getAttribute(Attributes.MAX_HEALTH);int count=pieces(player);var old=attr.getModifier(HEARTS);
        if(old!=null && old.amount()==count*3)return;
        attr.removeModifier(HEARTS);if(count>0)attr.addPermanentModifier(new AttributeModifier(HEARTS,count*3,AttributeModifier.Operation.ADD_VALUE));
        if(player.getHealth()>player.getMaxHealth())player.setHealth(player.getMaxHealth());
    }
    public static void detonate(ServerPlayer player) {
        var level=player.level();var center=player.position();
        var calc=new ExplosionDamageCalculator() {
            @Override public boolean shouldDamageEntity(Explosion explosion,Entity entity) {return entity!=player;}
            @Override public float getKnockbackMultiplier(Entity entity) {return entity==player ? 0 : 1;}
        };
        level.explode(player,null,calc,center,20,false,Level.ExplosionInteraction.TNT);
        for(var enemy:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(40),e->e!=player && e.isAlive() && !e.isSpectator() && e.distanceToSqr(center)<=1600))
            enemy.addEffect(new MobEffectInstance(ModEffects.RESONANT_STUN,100,0,false,true,true));
        player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING,100,0,false,true,true));
    }
    public static void stun(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
        var entity=event.getEntity();if(entity.level().isClientSide() || !(entity instanceof Mob mob))return;
        boolean leased=mob.getPersistentData().getBooleanOr("ExpeditionStunAi",false);
        if(mob.hasEffect(ModEffects.RESONANT_STUN)) {
            if(!mob.isNoAi()) {mob.getPersistentData().putBoolean("ExpeditionStunAi",true);mob.setNoAi(true);mob.getNavigation().stop();}
        } else if(leased) {mob.getPersistentData().remove("ExpeditionStunAi");mob.setNoAi(false);}
    }
    public static void attack(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if(event.getSource().getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.RESONANT_STUN))event.setCanceled(true);
    }
    private ExpeditionGear() {}
}
