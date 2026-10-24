package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Role-specific windups and finite channels share movement ownership with the original warned Brute. */
final class ApexSpecialGoal extends Goal {
    private final Apex apex;
    private LivingEntity target;
    private int elapsed, duration;
    private Vec3 direction;
    ApexSpecialGoal(Apex apex) { this.apex=apex; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        var victim=apex.getTarget();
        return (apex.kind()!=ApexKind.BRUTE || apex.flying()) && apex.abilityWait()==0 && victim!=null && victim.isAlive()
                && apex.distanceToSqr(victim)<=(apex.kind()==ApexKind.BLAST ? 144 : apex.kind()==ApexKind.SUPPORT ? 400 : 1024)
                && apex.hasLineOfSight(victim) && (apex.kind()!=ApexKind.BLAST || apex.getHealth()>apex.getMaxHealth()*.16F);
    }
    @Override public void start() {
        target=apex.getTarget(); elapsed=0; duration=apex.kind()==ApexKind.PRISM ? 60 : apex.kind()==ApexKind.BLAST ? 60 : 30;
        direction=target.getEyePosition().subtract(apex.skills().mouth()).normalize();
        apex.beginAbility(); apex.charging(true); apex.getNavigation().stop();
        apex.playSound(apex.kind()==ApexKind.BLAST ? SoundEvents.CREEPER_PRIMED : SoundEvents.RAVAGER_ROAR,1,apex.kind()==ApexKind.BROOD ? 1.5F : .75F);
    }
    @Override public boolean canContinueToUse() { return elapsed<duration && apex.isAlive() && target!=null && target.isAlive() && target.level()==apex.level(); }
    @Override public void tick() {
        var level=(ServerLevel)apex.level(); elapsed++; apex.getNavigation().stop();
        apex.getLookControl().setLookAt(target,apex.kind()==ApexKind.PRISM && elapsed>30 ? 2 : 12,12);
        Vec3 desired=target.getEyePosition().subtract(apex.skills().mouth()).normalize();
        direction=direction.lerp(desired,elapsed>30 ? .025 : .15).normalize();
        if (apex.kind()==ApexKind.PRISM) {
            if (elapsed<30 && elapsed%5==0) apex.skills().trace(level,direction,false);
            if (elapsed>=30 && elapsed%10==0 && elapsed<60) {
                apex.skills().trace(level,direction,true); apex.playSound(SoundEvents.BEACON_ACTIVATE,.8F,1.5F);
            }
        } else if (elapsed%5==0) {
            var particle=apex.kind()==ApexKind.BLAST ? ParticleTypes.FLAME : apex.kind()==ApexKind.SUPPORT ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER;
            level.sendParticles(particle,apex.getX(),apex.getY()+.7,apex.getZ(),5,.7,.4,.7,.01);
            if (apex.kind()==ApexKind.BLAST) {
                double radius=20.0*elapsed/duration;
                for (int i=0;i<24;i++) { double a=i*Math.PI/12; level.sendParticles(ParticleTypes.FLAME,apex.getX()+Math.cos(a)*radius,apex.getY()+.1,apex.getZ()+Math.sin(a)*radius,1,0,0,0,0); }
            }
        }
        if (elapsed!=duration) return;
        switch (apex.kind()) {
            case BROOD -> apex.skills().summon(level);
            case BLAST -> apex.skills().blast(level);
            case SUPPORT -> apex.skills().hook(level,target);
            case BRUTE -> { apex.setDeltaMovement(target.position().subtract(apex.position()).normalize().scale(1.1)); apex.hurtMarked=true; }
            default -> {}
        }
        apex.landFor(60); apex.charging(false);
    }
    @Override public void stop() { target=null; apex.charging(false); }
}
