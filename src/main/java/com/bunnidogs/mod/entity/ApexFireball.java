package com.bunnidogs.mod.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Native fireball flight with power four and complete bunnidog-family damage immunity. */
public final class ApexFireball extends LargeFireball {
    public ApexFireball(EntityType<? extends ApexFireball> type,Level level) { super(type,level); }
    @Override protected boolean canHitEntity(Entity target) { return !(target instanceof Bunnidog) && super.canHitEntity(target); }
    @Override protected void onHit(HitResult hit) {
        if(level() instanceof ServerLevel level) {
            if(getOwner() instanceof Apex apex)ApexSkills.explosion(level,apex,position(),4,true);
            discard();
        }
    }
    @Override public void tick() {
        if(!level().isClientSide() && (tickCount>100 || !(getOwner() instanceof Apex a) || !a.isAlive()
                || !level().hasChunksAt(blockPosition().offset(-12,-12,-12),blockPosition().offset(12,12,12)))) {discard();return;}
        super.tick();
    }
}
