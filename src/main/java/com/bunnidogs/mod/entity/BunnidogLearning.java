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
    public enum Activity { IDLE, SEARCHING, WALKING, WORKING, WAITING_FOR_GUIDE, RESTING }
    private Activity observed=Activity.SEARCHING;
    BunnidogLearning(Bunnidog dog) { this.dog = dog; }
    public Learning knowledge() { return knowledge; }
    public Learning.Routine lesson() { return lesson; }
    public UUID teacher() { return teacher; }
    /** Reports actual session/goal activity, without inferring ability or searching the world. */
    public Activity activity() {
        if (lesson==null) return Activity.IDLE;
        if (!guided()) return Activity.WAITING_FOR_GUIDE;
        return dog.gardeningWait()>0 ? Activity.RESTING : observed;
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
        return player.mayBuild() && player.level() == dog.level() && !player.isSpectator()
                && (dog.trail().keeper() == null || dog.trail().belongsTo(player));
    }
    public boolean physicallyReady(Learning.Routine routine) {
        var status = dog.gardeningStatus();
        if (status != Gardening.Status.READY && status != Gardening.Status.RESTING) return false;
        if (dog.isInLove()) return false;
        return switch (routine) {
            case HARVEST -> true; case DELIVERY -> Supplying.qualified(dog.stats());
            case GARDEN -> Gardening.cultivates(dog.stats()); case NURSERY -> Gardening.nurses(dog.stats());
        };
    }
    public boolean canBegin(Player player, Learning.Routine routine) {
        return opaqueKnowledge == null && authorized(player) && dog.distanceToSqr(player) <= GUIDE_RANGE * GUIDE_RANGE
                && player.hasLineOfSight(dog) && physicallyReady(routine)
                && CapabilityRules.potential(dog.mind(), routine.task()) != CapabilityRules.Potential.LIMITED
                && !knowledge.independent(dog.mind(), routine);
    }
    public boolean begin(Player player, Learning.Routine routine) {
        if (!canBegin(player, routine) || dog.level().isClientSide()) return false;
        lesson = routine; teacher = player.getUUID(); until = dog.level().getGameTime() + SESSION_TICKS; observed=Activity.SEARCHING;
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
        if (opaqueKnowledge != null) return false;
        // A lesson focuses one routine, so other chores cannot consume its shared work/rest window.
        if (lesson != null) return lesson == routine && guided()
                && CapabilityRules.potential(dog.mind(), routine.task()) != CapabilityRules.Potential.LIMITED;
        return knowledge.independent(dog.mind(), routine);
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
        if (lesson != routine || !canWork(routine) || !physicallyReady(routine) || !(dog.level() instanceof ServerLevel level)) return;
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
    public void cancel() { lesson = null; teacher = null; until = 0; observed=Activity.SEARCHING; }
    void tick() {
        if (lesson == null) return;
        var status = dog.gardeningStatus();
        if (dog.level().getGameTime() >= until || status != Gardening.Status.READY && status != Gardening.Status.RESTING
                || dog.isInLove() || CapabilityRules.potential(dog.mind(), lesson.task()) == CapabilityRules.Potential.LIMITED) {
            var player=dog.level() instanceof ServerLevel level ? level.getPlayerByUUID(teacher) : null;
            if (player!=null && player.isAlive() && dog.distanceToSqr(player)<=GUIDE_RANGE*GUIDE_RANGE) {
                String reason=dog.level().getGameTime()>=until ? "finished" : switch (status) {
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
    }
}
