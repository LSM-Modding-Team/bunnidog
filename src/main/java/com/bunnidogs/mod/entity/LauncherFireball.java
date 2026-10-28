package com.bunnidogs.mod.entity;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Launcher ammunition keeps its original allegiance through native saves and deflection. */
public final class LauncherFireball extends LargeFireball {
    private UUID shooter;
    private int power=3;
    public LauncherFireball(EntityType<? extends LauncherFireball> type,Level level) {super(type,level);}
    public void prepare(Player player,int strength) {shooter=player.getUUID();setOwner(player);power=Math.clamp(strength,1,5)*3;}
    private boolean friendly(Entity entity) {
        if(shooter==null)return entity==getOwner();
        if(shooter.equals(entity.getUUID()) || entity instanceof Bunnidog dog && shooter.equals(dog.trail().keeper()))return true;
        var keeper=level() instanceof ServerLevel server ? server.getEntity(shooter) : null;
        return keeper!=null && keeper.isAlliedTo(entity);
    }
    @Override protected boolean canHitEntity(Entity entity) {return !friendly(entity) && super.canHitEntity(entity);}
    @Override protected void onHitEntity(EntityHitResult hit) {if(!friendly(hit.getEntity()))super.onHitEntity(hit);}
    @Override protected void onHit(HitResult hit) {
        if(!(level() instanceof ServerLevel server))return;
        if(hit instanceof EntityHitResult entityHit) {
            if(friendly(entityHit.getEntity()))return;
            onHitEntity(entityHit);
        } else if(hit instanceof BlockHitResult blockHit)onHitBlock(blockHit);
        var calculator=new EntityBasedExplosionDamageCalculator(this) {
            @Override public boolean shouldDamageEntity(Explosion explosion,Entity entity) {return !friendly(entity);}
            @Override public float getKnockbackMultiplier(Entity entity) {return friendly(entity) ? 0 : 1;}
        };
        // Preserve native explosion protection/terrain rules without leaving fires beneath allies.
        server.explode(this,null,calculator,position(),power,false,Level.ExplosionInteraction.MOB);
        discard();
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);out.putByte("ExplosionPower",(byte)power);
        if(shooter!=null)out.putString("LauncherShooter",shooter.toString());
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);power=Math.clamp(in.getByteOr("ExplosionPower",(byte)3),3,15);
        try {shooter=UUID.fromString(in.getStringOr("LauncherShooter",""));}catch(IllegalArgumentException ignored) {shooter=null;}
    }
}
