package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.society.HearthTreeBlockEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** One local pupil observes actual started, paid work. Sessions/parcel witnesses never survive reload. */
public final class WorkshopApprenticeship {
    private final Bunnidog dog;
    private UUID teacher,site,owner;
    private Learning.Routine routine;
    private long search,until;
    private ItemStack material=ItemStack.EMPTY;
    private BlockPos destination;
    private BunnidogLearning.Activity activity=BunnidogLearning.Activity.WATCHING;
    WorkshopApprenticeship(Bunnidog dog) {this.dog=dog;}
    public UUID teacher() {return teacher;}
    public Learning.Routine routine() {return routine;}
    public boolean apprenticing() {return teacher!=null;}
    public BunnidogLearning.Activity activity() {return activity;}
    void activity(BunnidogLearning.Activity value) {activity=value;}
    public void cancel() {teacher=site=owner=null;routine=null;material=ItemStack.EMPTY;destination=null;until=0;search=dog.level().getGameTime()+80;}
    void reset() {cancel();search=0;}
    private boolean receptive() {
        return dog.learning().canDiscover() && dog.society().ready() && dog.society().cargoEmpty()
                && !dog.courierLore().occupied() && !dog.deliveries().watching() && dog.deliveries().cargo().isEmpty() && dog.deliveries().route()==null;
    }
    private boolean canLearn(Learning.Routine topic,HearthTreeBlockEntity home) {
        if(topic==null || !topic.workshop() || dog.learning().knowledge().knows(topic))return false;
        var potential=CapabilityRules.potential(dog.mind(),topic.task());
        return potential!=CapabilityRules.Potential.LIMITED && (Gardening.cultivates(dog.stats()) || home.wild().natural() && potential==CapabilityRules.Potential.INDEPENDENT);
    }
    private boolean expert(Bunnidog mentor,HearthTreeBlockEntity home,Learning.Routine topic) {
        return mentor!=null && mentor!=dog && mentor.society().belongs(home) && mentor.deliveries().readyBody()
                && mentor.learning().lesson()==null && !mentor.courierLore().occupied() && !mentor.workshopLessons().apprenticing()
                && !mentor.deliveries().watching() && mentor.deliveries().cargo().isEmpty()
                && mentor.learning().knowledge().independent(mentor.mind(),topic)
                && (topic==Learning.Routine.BUILD || CapabilityRules.potential(mentor.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT);
    }
    Bunnidog instructor() {
        var home=dog.society().hearth();
        if(!apprenticing() || !(dog.level() instanceof ServerLevel level) || home==null || !home.site().equals(site)
                || !Objects.equals(home.owner(),owner) || !receptive() || !canLearn(routine,home) || level.getGameTime()>=until
                || !(level.getEntity(teacher) instanceof Bunnidog mentor) || dog.distanceToSqr(mentor)>24*24 || !expert(mentor,home,routine))return null;
        return mentor;
    }
    void tick() {
        if(!(dog.level() instanceof ServerLevel level))return;
        if(apprenticing()) {if(instructor()==null)cancel();return;}
        if(level.getGameTime()<search || dog.society().active()!=null || !receptive())return;search=level.getGameTime()+40;
        var home=dog.society().hearth();if(home==null)return;var residents=home.residents();
        for(var mentor:residents) {
            var job=mentor.society().active();var topic=job==null ? null : SocietyWork.routine(job.kind());
            if(!canLearn(topic,home) || !expert(mentor,home,topic) || !mentor.society().owns(job) || dog.distanceToSqr(mentor)>64 || !HomeRoutes.visible(dog,mentor))continue;
            if(residents.stream().anyMatch(other->other!=dog && (mentor.getUUID().equals(other.workshopLessons().teacher()) || mentor.getUUID().equals(other.courierLore().teacher()))))continue;
            teacher=mentor.getUUID();routine=topic;site=home.site();owner=home.owner();until=level.getGameTime()+1200;activity=BunnidogLearning.Activity.WATCHING;return;
        }
    }
    private boolean sees(Bunnidog mentor,BlockPos target) {
        if(instructor()!=mentor || target==null || dog.distanceToSqr(mentor)>64 || dog.distanceToSqr(Vec3.atCenterOf(target))>64 || !HomeRoutes.visible(dog,mentor))return false;
        var from=dog.blockPosition();if(!dog.level().hasChunksAt(new BlockPos(Math.min(from.getX(),target.getX()),Math.min(from.getY(),target.getY()),Math.min(from.getZ(),target.getZ())),new BlockPos(Math.max(from.getX(),target.getX()),Math.max(from.getY(),target.getY()),Math.max(from.getZ(),target.getZ()))))return false;
        var hit=dog.level().clip(new ClipContext(dog.getEyePosition(),Vec3.atCenterOf(target),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dog));
        return hit.getType()==HitResult.Type.MISS || hit.getBlockPos().equals(target);
    }
    /** Called only after the original native operation commits, with the exact pre-operation parcel. */
    static void completed(Bunnidog mentor,HearthTreeBlockEntity home,SocietyWork.Job job,ItemStack spent) {
        var topic=SocietyWork.routine(job.kind());if(topic==null || !topic.workshop() || mentor.society().active()!=job || !mentor.society().owns(job) || mentor.society().hearth()!=home)return;
        for(var pupil:home.residents()) {
            var lesson=pupil.workshopLessons();if(!mentor.getUUID().equals(lesson.teacher) || lesson.routine!=topic)continue;
            if(job.kind()==SocietyWork.Kind.FETCH_BUILD || job.kind()==SocietyWork.Kind.FETCH_FURNACE) {
                lesson.material=ItemStack.EMPTY;lesson.destination=null;
                if(lesson.sees(mentor,home.getBlockPos()) && job.destination()!=null) {lesson.material=mentor.society().workSupplies();lesson.destination=job.destination().immutable();}
                continue;
            }
            boolean paidParcel=job.kind()==SocietyWork.Kind.BUILD || job.kind()==SocietyWork.Kind.LOAD_FURNACE;
            boolean complete=job.kind()==SocietyWork.Kind.CRAFT || paidParcel && job.target().equals(lesson.destination) && !spent.isEmpty() && ItemStack.matches(spent,lesson.material);
            if(paidParcel) {lesson.material=ItemStack.EMPTY;lesson.destination=null;}
            if(!complete || !lesson.sees(mentor,job.target()))continue;
            if(job.kind()==SocietyWork.Kind.BUILD && !home.construction().owned(job.target()))continue;
            pupil.learning().workshopPractice(topic);if(pupil.learning().knowledge().knows(topic))lesson.cancel();
        }
    }
}
