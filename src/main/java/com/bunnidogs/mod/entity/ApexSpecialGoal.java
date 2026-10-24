package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Specialists cast while their own ground/aerial controller maintains the role's position. */
final class ApexSpecialGoal extends Goal {
    private final Apex apex;
    private LivingEntity target;
    private int elapsed,duration;
    private Vec3 direction;
    ApexSpecialGoal(Apex apex) { this.apex=apex;setFlags(EnumSet.of(Flag.LOOK)); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        var victim=apex.getTarget();
        return apex.kind()!=ApexKind.BRUTE && apex.abilityWait()==0 && victim!=null && victim.isAlive()
            && apex.distanceToSqr(victim)<=(apex.kind()==ApexKind.BLAST ? apex.flying() ? 144 : 25 : apex.kind()==ApexKind.SUPPORT ? 400 : 1024)
            && (apex.kind()==ApexKind.PRISM || apex.hasLineOfSight(victim))
            && (apex.kind()!=ApexKind.BLAST || apex.getHealth()>apex.getMaxHealth()*.31F);
    }
    @Override public void start() {
        target=apex.getTarget();elapsed=0;duration=apex.kind()==ApexKind.PRISM ? 90 : 30;
        direction=target.getEyePosition().subtract(apex.skills().mouth()).normalize();
        apex.charging(true);apex.getNavigation().stop();
        apex.playSound(apex.kind()==ApexKind.BLAST ? SoundEvents.CREEPER_PRIMED : SoundEvents.RAVAGER_ROAR,1,apex.kind()==ApexKind.BROOD ? 1.5F : .75F);
    }
    @Override public boolean canContinueToUse() { return elapsed<duration && apex.isAlive() && target!=null && target.isAlive() && target.level()==apex.level(); }
    @Override public void tick() {
        var level=(ServerLevel)apex.level();elapsed++;
        if(!apex.flying())apex.getNavigation().stop();
        if(apex.kind()!=ApexKind.PRISM || elapsed<=30 || apex.enraged()) {
            Vec3 desired=target.getEyePosition().subtract(apex.skills().mouth()).normalize();
            direction=elapsed==1 ? desired : direction.lerp(desired,elapsed<=30 ? .2 : .12).normalize();
        }
        Vec3 look=apex.skills().mouth().add(direction.scale(20));
        apex.getLookControl().setLookAt(look.x,look.y,look.z,30,30);
        if(apex.kind()==ApexKind.PRISM) {
            if(elapsed<30 && elapsed%5==0)apex.skills().trace(level,direction,false);
            // Six damaging pulses over sixty ticks; the initial thirty-tick warning remains.
            if(elapsed>=30 && elapsed<90 && elapsed%10==0) {
                apex.skills().trace(level,direction,true);apex.playSound(SoundEvents.BEACON_ACTIVATE,.8F,1.5F);
            }
        } else if(apex.kind()==ApexKind.BLAST)apex.fuse(elapsed);
        else if(elapsed%5==0)level.sendParticles(apex.kind()==ApexKind.SUPPORT ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER,apex.getX(),apex.getY()+.7,apex.getZ(),5,.7,.4,.7,.01);
        if(elapsed!=duration)return;
        switch(apex.kind()) {
            case BROOD -> apex.skills().summon(level);
            case BLAST -> { apex.skills().blast(level);apex.retreatFor(80); }
            case SUPPORT -> apex.skills().hook(level,target);
            default -> {}
        }
        apex.beginAbility();apex.charging(false);apex.fuse(0);
    }
    @Override public void stop() { target=null;apex.charging(false);apex.fuse(0); }
}
