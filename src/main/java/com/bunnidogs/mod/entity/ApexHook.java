package com.bunnidogs.mod.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A visible fast hook that clears ordinary terrain through native grief/protection hooks. */
public final class ApexHook extends ThrowableItemProjectile {
    public ApexHook(EntityType<? extends ApexHook> type,Level level) { super(type,level); }
    @Override protected Item getDefaultItem() { return Items.LEAD; }
    @Override protected boolean canHitEntity(Entity entity) { return !(entity instanceof Apex || entity instanceof ApexMinion) && super.canHitEntity(entity); }
    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide() && getOwner() instanceof Apex apex && hit.getEntity() instanceof net.minecraft.world.entity.LivingEntity victim)
            apex.skills().hookHit(victim);
    }
    @Override protected void onHit(HitResult hit) { super.onHit(hit); if (!level().isClientSide()) discard(); }
    @Override public void tick() {
        if (!level().isClientSide() && (tickCount>30 || !(getOwner() instanceof Apex apex) || !apex.isAlive() || distanceToSqr(apex)>576
                || !level().hasChunksAt(blockPosition().offset(-2,-2,-2),blockPosition().offset(2,2,2)))) { discard(); return; }
        if (level() instanceof net.minecraft.server.level.ServerLevel level && getOwner() instanceof Apex apex) {
            if(!ApexTerrain.clear(level,apex,position(),position().add(getDeltaMovement()),1.2,1.2)) { discard();return; }
            var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(level,this,position(),position().add(getDeltaMovement()),getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.2),this::canHitEntity,1.2F);
            if(hit!=null && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this,hit)) {onHit(hit);return;}
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,getX(),getY(),getZ(),2,.2,.2,.2,0);
        }
        super.tick();
    }
}
