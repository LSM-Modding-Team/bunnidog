package com.bunnidogs.mod.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A physical thrown anvil. Cover can intercept it; successful impact starts the look-to-escape stun. */
public final class ApexAnvil extends ThrowableItemProjectile {
    public ApexAnvil(EntityType<? extends ApexAnvil> type,Level level) { super(type,level); }
    @Override protected double getDefaultGravity() {return .06;}
    public void aimAt(net.minecraft.world.phys.Vec3 target) {
        var delta=target.subtract(position().add(0,getBbHeight()/2,0));int ticks=Math.clamp((int)Math.ceil(delta.horizontalDistance()*1.1),16,32);
        // Native throwable order is gravity, air drag, then movement. Solve that same parabola.
        double weight=0,fall=0,vertical=0,power=1,drag=.99F;
        for(int i=0;i<ticks;i++) {power*=drag;weight+=power;vertical=(vertical-getDefaultGravity())*drag;fall+=vertical;}
        setDeltaMovement(delta.x/weight,(delta.y-fall)/weight,delta.z/weight);
    }
    @Override protected Item getDefaultItem() { return Items.ANVIL; }
    @Override protected boolean canHitEntity(Entity target) { return !(target instanceof Bunnidog) && super.canHitEntity(target); }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if(level() instanceof ServerLevel level && getOwner() instanceof Apex apex && hit.getEntity() instanceof ServerPlayer p
                && !p.isSpectator() && !p.hasInfiniteMaterials() && p.hurtServer(level,damageSources().thrown(this,apex),(float)(apex.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)*2)) && p.isAlive()) {ApexDaze.hit(apex,p);apex.skills().queueHook(p);}
    }
    @Override protected void onHit(HitResult hit) {super.onHit(hit);if(!level().isClientSide())discard();}
    @Override public void tick() {
        if(!level().isClientSide() && (tickCount>70 || !(getOwner() instanceof Apex a) || !a.isAlive()
                || !level().hasChunksAt(blockPosition().offset(-3,-3,-3),blockPosition().offset(3,3,3)))) {discard();return;}
        if(level() instanceof ServerLevel level) {
            var center=position().add(0,getBbHeight()/2,0);
            var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(level,this,center,center.add(getDeltaMovement()),getBoundingBox().expandTowards(getDeltaMovement()).inflate(1),this::canHitEntity,1F);
            if(hit!=null && level.clip(new net.minecraft.world.level.ClipContext(center,hit.getEntity().getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS
                    && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this,hit)) {onHit(hit);return;}
        }
        super.tick();
    }
}
