package com.bunnidogs.mod.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A real colliding, blockable hook; no instant hit through cover. */
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
        super.tick();
    }
}
