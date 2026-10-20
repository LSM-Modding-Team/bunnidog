package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import com.bunnidogs.mod.genetics.Adaptations;
import com.bunnidogs.mod.genetics.Home;
import com.bunnidogs.mod.genetics.Phasing;
import com.bunnidogs.mod.genetics.Trail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/** One bounded landing search per actual attack. No path requests, terrain loads or home-ticket changes. */
final class PhaseEscape {
    private final Bunnidog dog;
    private long nextTick;
    private boolean busy;

    PhaseEscape(Bunnidog dog) { this.dog = dog; }
    int waitTicks() { return (int)Math.clamp(this.nextTick-this.dog.level().getGameTime(),0L,(long)Phasing.RECOVERY_TICKS); }
    void restore(int ticks) { this.nextTick = this.dog.level().getGameTime()+Math.clamp(ticks,0,Phasing.RECOVERY_TICKS); }
    private BlockPos boundaryHome() {
        return dog.trail().ordered() && Trail.qualified(dog.genome()) && dog.trail().keeperPlayer()!=null ? null : dog.homeNest();
    }

    private boolean ready(ServerLevel level, LivingEntity attacker) {
        return this.dog.level() == level && this.dog.acceptsCare() && this.dog.isAlive() && !this.dog.isBaby()
                && Adaptations.phasing(this.dog.genome()) && waitTicks() == 0
                && !this.dog.beingTransported() && !this.dog.isVehicle() && this.dog.onGround()
                && !this.dog.isInWater() && !this.dog.isInLava()
                && attacker != this.dog && attacker.isAlive() && attacker.level() == level;
    }
    private boolean landing(ServerLevel level, LivingEntity attacker, Vec3 origin, BlockPos home, Vec3 target) {
        if (!Phasing.inRange(target.subtract(origin).horizontalDistanceSqr(),target.y-origin.y)
                || !Phasing.farther(origin.distanceToSqr(attacker.position()),target.distanceToSqr(attacker.position()))) return false;
        var pos = BlockPos.containing(target);
        if (home != null && Home.tooFar(pos.distSqr(home))) return false;
        var box = this.dog.getBoundingBox().move(target.subtract(this.dog.position()));
        if (!level.getWorldBorder().isWithinBounds(box)
                || !level.hasChunksAt(BlockPos.containing(box.minX-1,box.minY-1,box.minZ-1),
                        BlockPos.containing(box.maxX+1,box.maxY+1,box.maxZ+1))
                || pos.getY() < level.getMinY()+1 || box.maxY >= level.getMaxY()) return false;
        // Exact full-block landings avoid accepting redirected positions inside a floor or over a cliff.
        return target.y == pos.getY() && level.getFluidState(pos.below()).isEmpty()
                && level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP)
                && HomeRoutes.dryCell(this.dog,pos) && level.noCollision(this.dog,box) && !level.containsAnyLiquid(box);
    }
    void afterAttack(ServerLevel level, LivingEntity attacker) {
        if (this.busy || !ready(level,attacker)) return;
        this.busy = true;
        try {
            var origin = this.dog.position(); var home = this.dog.homeNest(); var boundary = boundaryHome();
            var candidates = new ArrayList<Vec3>(48);
            for (int radius : new int[]{4,6}) for (int i = 0; i < 8; i++) {
                double angle = i*Math.PI/4;
                int dx = (int)Math.round(Math.cos(angle)*radius), dz = (int)Math.round(Math.sin(angle)*radius);
                for (int dy : new int[]{0,1,-1}) candidates.add(Vec3.atBottomCenterOf(this.dog.blockPosition().offset(dx,dy,dz)));
            }
            candidates.sort(Comparator.comparingDouble((Vec3 p) -> p.distanceToSqr(attacker.position())).reversed());
            var target = candidates.stream().filter(p -> landing(level,attacker,origin,boundary,p)).findFirst().orElse(null);
            if (target == null) return;
            var event = EventHooks.onEnderTeleport(this.dog,target.x,target.y,target.z);
            if (event.isCanceled() || !ready(level,attacker) || !origin.equals(this.dog.position())
                    || !Objects.equals(home,this.dog.homeNest()) || !Objects.equals(boundary,boundaryHome())
                    || !landing(level,attacker,origin,boundary,event.getTarget())) return;
            target = event.getTarget();
            // Reserve recovery before native movement callbacks can save or pack the animal.
            this.nextTick = level.getGameTime()+Phasing.RECOVERY_TICKS;
            this.dog.getNavigation().stop();
            this.dog.teleportTo(target.x,target.y,target.z);
            this.dog.setDeltaMovement(Vec3.ZERO);
            level.sendParticles(ParticleTypes.PORTAL,origin.x,origin.y+.4,origin.z,16,.25,.3,.25,.1);
            level.sendParticles(ParticleTypes.PORTAL,target.x,target.y+.4,target.z,16,.25,.3,.25,.1);
            level.gameEvent(GameEvent.TELEPORT,origin,GameEvent.Context.of(this.dog));
            if (!this.dog.isSilent()) {
                level.playSound(null,origin.x,origin.y,origin.z,SoundEvents.ENDERMAN_TELEPORT,this.dog.getSoundSource(),.6F,1.3F);
                this.dog.playSound(SoundEvents.ENDERMAN_TELEPORT,.6F,1.3F);
            }
        } finally { this.busy = false; }
    }
}
