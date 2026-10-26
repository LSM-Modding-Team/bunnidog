package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Short, local work claims. Observing an offer never reserves goods or starts work. */
public final class HouseholdJobs {
    private record Key(SocietyWork.Kind kind,BlockPos at,int index,UUID personal) {}
    private record Claim(UUID actor,long until) {}
    private record Rejected(UUID actor,Key key) {}
    private record Backoff(long until,int failures) {}
    private final HearthTreeBlockEntity home;
    private final Map<Key,Claim> claims=new HashMap<>();
    private final Map<Rejected,Backoff> unreachable=new HashMap<>();
    public HouseholdJobs(HearthTreeBlockEntity home) {this.home=home;}
    private Key key(Bunnidog dog,SocietyWork.Job job) {
        var kind=job.kind();
        UUID personal=switch(kind) {
            case MEAL,DELIVER,CARE,STUDY_ROUTE -> dog.getUUID();
            case MEDICINE,FETCH_AID,TEACH -> job.patient()==null ? null : job.patient().id();
            default -> null;
        };
        return new Key(kind,kind==SocietyWork.Kind.GATHER_FIELD ? BlockPos.ZERO : job.destination()!=null ? job.destination() : job.target(),kind==SocietyWork.Kind.CRAFT || kind==SocietyWork.Kind.TEACH ? -1 : job.index(),personal);
    }
    private boolean live(Claim claim) {
        if(claim==null || !home.attached() || !(home.getLevel() instanceof ServerLevel level) || level.getGameTime()>=claim.until)return false;
        return level.getEntity(claim.actor) instanceof Bunnidog dog && dog.isAlive() && dog.society().belongs(home) && !dog.workPaused() && !dog.trail().ordered() && !dog.combat().downed();
    }
    public boolean available(Bunnidog dog,SocietyWork.Job job) {
        if((job.kind()==SocietyWork.Kind.MEDICINE || job.kind()==SocietyWork.Kind.FETCH_AID) && job.patient()!=null && home.residents().stream().anyMatch(other->other!=dog && job.patient().id().equals(other.society().treatingPatient())))return false;
        var failed=unreachable.get(new Rejected(dog.getUUID(),key(dog,job)));if(failed!=null && failed.until>home.getLevel().getGameTime())return false;
        if(job.kind()==SocietyWork.Kind.FETCH_SEED || job.kind()==SocietyWork.Kind.SOW_FIELD) {var target=job.destination()!=null ? job.destination() : job.target();if(home.residents().stream().anyMatch(other->other!=dog && target.equals(other.society().fieldTarget())))return false;}
        if(job.kind()==SocietyWork.Kind.FETCH_BUILD || job.kind()==SocietyWork.Kind.BUILD) {var target=job.destination()!=null ? job.destination() : job.target();if(home.residents().stream().anyMatch(other->other!=dog && target.equals(other.society().buildTarget())))return false;}
        var claim=claims.get(key(dog,job));return !live(claim) || claim.actor.equals(dog.getUUID());}
    public boolean claim(Bunnidog dog,SocietyWork.Job job) {
        if(!home.attached() || !dog.society().belongs(home) || !available(dog,job))return false;
        claims.entrySet().removeIf(e->!live(e.getValue()));
        if(claims.size()>=32)return false;
        claims.put(key(dog,job),new Claim(dog.getUUID(),home.getLevel().getGameTime()+260));unreachable.remove(new Rejected(dog.getUUID(),key(dog,job)));return true;
    }
    public boolean owns(Bunnidog dog,SocietyWork.Job job) {var claim=claims.get(key(dog,job));return live(claim) && claim.actor.equals(dog.getUUID());}
    public void unreachable(Bunnidog dog,SocietyWork.Job job) {
        if(!home.attached())return;long now=home.getLevel().getGameTime();var key=new Rejected(dog.getUUID(),key(dog,job));var previous=unreachable.get(key);int failures=previous==null ? 1 : Math.min(4,previous.failures+1);
        if(unreachable.size()>=256)unreachable.entrySet().removeIf(e->e.getValue().until<=now);if(unreachable.size()<256)unreachable.put(key,new Backoff(now+Math.min(200,40<<Math.min(3,failures-1)),failures));
    }
    public void release(Bunnidog dog) {claims.entrySet().removeIf(e->e.getValue().actor.equals(dog.getUUID()));}
}
