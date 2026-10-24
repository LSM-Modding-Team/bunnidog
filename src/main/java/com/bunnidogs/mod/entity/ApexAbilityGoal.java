package com.bunnidogs.mod.entity;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Short readable charge; the target can dodge before either move resolves. */
final class ApexAbilityGoal extends Goal {
    private final Apex apex;
    private LivingEntity target;
    private boolean stomp;
    private int charge;
    ApexAbilityGoal(Apex apex) {
        this.apex = apex;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public boolean canUse() {
        var victim = this.apex.getTarget();
        return apex.kind()==ApexKind.BRUTE && !apex.flying() && this.apex.abilityWait() == 0 && this.apex.onGround() && victim != null && victim.isAlive()
                && this.apex.distanceToSqr(victim) <= 100 && this.apex.hasLineOfSight(victim);
    }
    @Override public void start() {
        this.target = this.apex.getTarget();
        this.stomp = this.apex.distanceToSqr(this.target) <= 9;
        this.charge = this.stomp ? 30 : 20;
        if (this.apex.enraged()) this.charge = this.charge * 2 / 3;
        this.apex.beginAbility();
        this.apex.getNavigation().stop();
        this.apex.charging(true);
        this.apex.playSound(SoundEvents.RAVAGER_ROAR, .8F, this.stomp ? .65F : 1.1F);
    }
    @Override public boolean canContinueToUse() {
        return this.charge > 0 && this.target != null && this.target.isAlive();
    }
    @Override public void tick() {
        this.apex.getNavigation().stop();
        this.apex.getLookControl().setLookAt(this.target, 30, 30);
        var level = (ServerLevel) this.apex.level();
        if (this.charge % 4 == 0) level.sendParticles(ParticleTypes.WITCH,
                this.apex.getX(), this.apex.getY() + .5, this.apex.getZ(), 5, .6, .3, .6, .01);
        if (--this.charge > 0) return;
        if (this.stomp) stomp(level);
        else {
            Vec3 towards = this.target.position().subtract(this.apex.position()).multiply(1, 0, 1).normalize();
            this.apex.setDeltaMovement(towards.scale(this.apex.enraged() ? 1.05 : .9)
                    .add(0, this.apex.getAttributeValue(Attributes.JUMP_STRENGTH), 0));
            this.apex.hurtMarked = true;
        }
    }
    private void stomp(ServerLevel level) {
        var source = this.apex.damageSources().mobAttack(this.apex);
        float damage = (float) this.apex.getAttributeValue(Attributes.ATTACK_DAMAGE) * .75F;
        for (Player player : level.getEntitiesOfClass(Player.class, this.apex.getBoundingBox().inflate(4, 2, 4),
                p -> p.isAlive() && !p.isSpectator() && !p.hasInfiniteMaterials())) {
            if (this.apex.distanceToSqr(player) > 16 || !this.apex.hasLineOfSight(player)) continue;
            if (player.hurtServer(level, source, damage)) player.knockback(1,
                    this.apex.getX() - player.getX(), this.apex.getZ() - player.getZ(), source, damage);
        }
        level.sendParticles(ParticleTypes.POOF, this.apex.getX(), this.apex.getY() + .2,
                this.apex.getZ(), 40, 2, .1, 2, .15);
        this.apex.playSound(SoundEvents.RAVAGER_STEP, 1.2F, .6F);
    }
    @Override public void stop() {
        this.charge = 0;
        this.target = null;
        this.apex.charging(false);
    }
}
