package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.network.TextComponents;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A local, transient instructor session around the existing paid world actions. No new competing AI goals. */
public final class BunnidogLearning {
    public static final int GUIDE_RANGE = 8;
    private static final int SESSION_TICKS = 10 * 60 * 20;
    private final Bunnidog dog;
    private Learning knowledge = Learning.EMPTY;
    private Learning.Routine lesson;
    private UUID teacher;
    private long until;
    private String opaqueKnowledge;
    private final WorkObservation observation=new WorkObservation(this);
    public enum Activity { IDLE, SEARCHING, WALKING, WORKING, WATCHING, WAITING_FOR_GUIDE, RESTING }
    private Activity observed=Activity.SEARCHING;
    BunnidogLearning(Bunnidog dog) { this.dog = dog; }
    public Learning knowledge() { return knowledge; }
    public Learning.Routine lesson() { return lesson; }
    public UUID teacher() { return teacher; }
    Bunnidog animal() {return dog;}
    WorkObservation observation() {return observation;}
    /** Reports actual session/goal activity, without inferring ability or searching the world. */
    public Activity activity() {
        if (lesson==null) return Activity.IDLE;
        if (!guided()) return Activity.WAITING_FOR_GUIDE;
        return (lesson==Learning.Routine.SCOUT ? dog.scoutingWait() : dog.gardeningWait())>0 ? Activity.RESTING : observed;
    }
    void observe(Learning.Routine routine,Activity activity) {
        if (lesson==routine) observed=activity;
    }
    /** Brief occlusion pauses instruction; it does not erase recognition and trigger skittish flight. */
    public boolean recognizesInstructor(Player player) {
        return lesson != null && player.getUUID().equals(teacher) && authorized(player)
                && dog.level().getGameTime() < until && dog.distanceToSqr(player) <= GUIDE_RANGE * GUIDE_RANGE;
    }
    public boolean authorized(Player player) {
        return player.mayBuild() && dog.society().authorized(player) && player.level() == dog.level() && !player.isSpectator()
                && (dog.trail().keeper() == null || dog.trail().belongsTo(player));
    }
    public boolean physicallyReady(Learning.Routine routine) {
        if(routine==Learning.Routine.SCOUT) {var status=dog.scoutingBodyStatus();return !dog.isInLove() && (status==Scouting.Status.READY || status==Scouting.Status.RESTING);}
        var status = dog.gardeningStatus();
        if (status != Gardening.Status.READY && status != Gardening.Status.RESTING) return false;
        if (dog.isInLove()) return false;
        return switch (routine) {
            case HARVEST -> true; case DELIVERY -> Supplying.qualified(dog.stats());
            case GARDEN -> Gardening.cultivates(dog.stats()); case NURSERY -> Gardening.nurses(dog.stats());
            case SCOUT -> false; // Handled above: field lessons do not require a garden/home/daylight.
        };
    }
    public boolean canBegin(Player player, Learning.Routine routine) {
        return opaqueKnowledge == null && !dog.deliveries().watching() && dog.deliveries().cargo().isEmpty() && dog.society().cargoEmpty() && dog.deliveries().authorized(player) && dog.distanceToSqr(player) <= GUIDE_RANGE * GUIDE_RANGE
                && player.hasLineOfSight(dog) && physicallyReady(routine)
                && CapabilityRules.potential(dog.mind(), routine.task()) != CapabilityRules.Potential.LIMITED
                && !knowledge.independent(dog.mind(), routine);
    }
    public boolean begin(Player player, Learning.Routine routine) {
        if (!canBegin(player, routine) || dog.level().isClientSide()) return false;
        dog.courierLore().cancel();
        observation.clear();lesson = routine; teacher = player.getUUID(); until = dog.level().getGameTime() + SESSION_TICKS; observed=routine==Learning.Routine.SCOUT ? Activity.WATCHING : Activity.SEARCHING;
        dog.setPersistenceRequired();
        player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.bunnidogs.learning.started",
                dog.getDisplayName(),TextComponents.t(routine.task().displayName())));
        return true;
    }
    public boolean guided() {
        if (lesson == null || !(dog.level() instanceof ServerLevel level) || level.getGameTime() >= until) return false;
        var player = level.getPlayerByUUID(teacher);
        return player != null && player.isAlive() && authorized(player) && dog.distanceToSqr(player) <= GUIDE_RANGE * GUIDE_RANGE
                && player.hasLineOfSight(dog) && !dog.trail().ordered() && !dog.workPaused();
    }
    public boolean canWork(Learning.Routine routine) {
        if (opaqueKnowledge != null || dog.courierLore().occupied() || dog.deliveries().watching() || !dog.deliveries().cargo().isEmpty()) return false;
        // A lesson focuses one routine, so other chores cannot consume its shared work/rest window.
        if (lesson != null) return lesson == routine && guided()
                && CapabilityRules.potential(dog.mind(), routine.task()) != CapabilityRules.Potential.LIMITED;
        return knowledge.independent(dog.mind(), routine) || dog.society().guided(routine);
    }
    public boolean witnesses(BlockPos first, BlockPos second) {
        if (lesson == null) return true;
        if (!guided() || !(dog.level() instanceof ServerLevel level)) return false;
        var player = level.getPlayerByUUID(teacher);
        for (var pos : new BlockPos[]{first, second}) {
            if (!level.hasChunkAt(pos)) return false;
            var hit = level.clip(new ClipContext(player.getEyePosition(), Vec3.atCenterOf(pos),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.MISS && !hit.getBlockPos().equals(pos)) return false;
        }
        return true;
    }
    /** Called only after the native crop edit/resource transaction actually succeeded. */
    void completed(Learning.Routine routine) {
        if (!(dog.level() instanceof ServerLevel level) || !canWork(routine)) return;
        boolean independent=knowledge.independent(dog.mind(),routine);
        dog.society().completed(routine,independent);
        if(lesson==null) {
            if(!independent && dog.society().guided(routine)) {knowledge=knowledge.practice(dog.mind(),routine);if(knowledge.knows(routine))dog.society().taught(routine);level.sendParticles(ParticleTypes.HAPPY_VILLAGER,dog.getX(),dog.getY()+.6,dog.getZ(),4,.2,.2,.2,0); }
            return;
        }
        if(lesson!=routine || !physicallyReady(routine))return;
        practice(routine);
    }
    private void practice(Learning.Routine routine) {
        var level=(ServerLevel)dog.level();
        var player = level.getPlayerByUUID(teacher);
        boolean knew = knowledge.knows(routine);
        knowledge = knowledge.practice(dog.mind(), routine);
        if (player != null && !knew) {
            if (knowledge.knows(routine)) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(knowledge.independent(dog.mind(), routine)
                        ? "message.bunnidogs.learning.learned" : "message.bunnidogs.learning.guided",
                        dog.getDisplayName(),TextComponents.t(routine.task().displayName())));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, dog.getX(), dog.getY() + .6, dog.getZ(), 5, .2, .2, .2, 0);
            } else player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.bunnidogs.learning.practicing",
                    dog.getDisplayName(),TextComponents.t(routine.task().displayName())));
        }
        if (knowledge.independent(dog.mind(), routine)) cancel();
    }
    boolean acceptsExample(Player player,Learning.Routine routine,BlockPos pos) {
        if(lesson!=routine || !player.getUUID().equals(teacher) || !guided() || !physicallyReady(routine)
                || dog.distanceToSqr(Vec3.atCenterOf(pos))>GUIDE_RANGE*GUIDE_RANGE || player.distanceToSqr(Vec3.atCenterOf(pos))>36
                || routine!=Learning.Routine.SCOUT && !dog.foodWithinHome(pos))return false;
        var from=dog.blockPosition();
        if(!dog.level().hasChunksAt(new BlockPos(Math.min(from.getX(),pos.getX()),from.getY(),Math.min(from.getZ(),pos.getZ())),new BlockPos(Math.max(from.getX(),pos.getX()),from.getY(),Math.max(from.getZ(),pos.getZ()))))return false;
        var hit=dog.level().clip(new ClipContext(dog.getEyePosition(),Vec3.atCenterOf(pos),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dog));
        return hit.getType()==HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }
    void demonstrated(Player player,Learning.Routine routine,BlockPos pos) {
        if(opaqueKnowledge==null && acceptsExample(player,routine,pos))practice(routine);
    }
    void deliveryPractice() {
        if(opaqueKnowledge==null && !dog.level().isClientSide() && CapabilityRules.potential(dog.mind(),Learning.Routine.DELIVERY.task())!=CapabilityRules.Potential.LIMITED)knowledge=knowledge.practice(dog.mind(),Learning.Routine.DELIVERY);
    }
    public void cancel() { lesson = null; teacher = null; until = 0; observed=Activity.SEARCHING; observation.clear(); }
    void tick() {
        observation.tick();
        if (lesson == null) return;
        var status = dog.gardeningStatus();
        if (dog.level().getGameTime() >= until || !physicallyReady(lesson)
                || dog.isInLove() || CapabilityRules.potential(dog.mind(), lesson.task()) == CapabilityRules.Potential.LIMITED) {
            var player=dog.level() instanceof ServerLevel level ? level.getPlayerByUUID(teacher) : null;
            if (player!=null && player.isAlive() && dog.distanceToSqr(player)<=GUIDE_RANGE*GUIDE_RANGE) {
                String reason=dog.level().getGameTime()>=until ? "finished" : lesson==Learning.Routine.SCOUT ? "stopped" : switch (status) {
                    case TRAIL -> "order"; case PAUSED -> "paused"; case FULL_HEALTH, DANGER, UNAVAILABLE -> "unsafe";
                    case DAYTIME -> "night"; default -> "stopped";
                };
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.bunnidogs.learning."+reason,dog.getDisplayName()));
            }
            cancel();
        }
    }
    public void save(ValueOutput output) { output.putString("Learning", opaqueKnowledge == null ? knowledge.toString() : opaqueKnowledge); }
    public void restore(ValueInput input, boolean legacyGenome) {
        cancel();
        String saved = input.getStringOr("Learning", "");
        opaqueKnowledge = !saved.isEmpty() && Learning.parse(saved).isEmpty() ? saved : null;
        knowledge = Learning.parse(saved).orElse(legacyGenome && !dog.isBaby() && saved.isEmpty() ? Learning.LEGACY_FAMILIAR : Learning.EMPTY);
        if(dog.isBaby() && saved.startsWith("l1."))knowledge=new Learning(knowledge.packed()&0xFFFF);
    }
}
