package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Local apprenticeship and actual written procedures. Neither one creates goods or inherited education. */
public final class CourierLore {
    private final Bunnidog dog;
    private UUID teacher,guide;
    private DeliveryMemory.Route example;
    private int witnessed;
    private long search,guideSearch;
    private boolean atDesk,busy;
    CourierLore(Bunnidog dog) {this.dog=dog;}
    public UUID teacher() {return teacher;}
    public boolean apprenticing() {return teacher!=null;}
    public boolean occupied() {return atDesk || apprenticing();}
    void desk(boolean value) {atDesk=value;}
    public void cancel() {teacher=null;example=null;witnessed=0;}
    void reset() {cancel();guide=null;search=guideSearch=0;atDesk=busy=false;}
    private boolean receptive() {
        return dog.deliveries().readyBody() && dog.deliveries().cargo().isEmpty() && dog.society().cargoEmpty() && !dog.deliveries().watching()
                && dog.learning().lesson()==null && Supplying.qualified(dog.stats()) && CapabilityRules.potential(dog.mind(),CapabilityRules.Task.DELIVERY)!=CapabilityRules.Potential.LIMITED;
    }
    private static boolean expert(Bunnidog teacher,HearthTreeBlockEntity home) {
        return teacher!=null && teacher.isAlive() && teacher.society().belongs(home) && teacher.deliveries().readyBody() && !teacher.deliveries().paused()
                && !teacher.deliveries().watching() && teacher.learning().lesson()==null && teacher.learning().knowledge().independent(teacher.mind(),Learning.Routine.DELIVERY)
                && CapabilityRules.potential(teacher.mind(),CapabilityRules.Task.TEACHING)==CapabilityRules.Potential.INDEPENDENT && teacher.society().cargoEmpty()
                && RouteNotes.authorized(home,teacher.deliveries().route());
    }
    Bunnidog instructor() {
        if(teacher==null || !receptive() || !(dog.level() instanceof ServerLevel level) || !(level.getEntity(teacher) instanceof Bunnidog mentor))return null;
        var home=dog.society().hearth();return home!=null && expert(mentor,home) && dog.distanceToSqr(mentor)<=24*24 ? mentor : null;
    }
    void tick() {
        if(!(dog.level() instanceof ServerLevel level))return;
        if(atDesk)return;
        if(apprenticing()) {if(!receptive() || dog.deliveries().route()!=null || instructor()==null)cancel();return;}
        if(level.getGameTime()<search || !receptive() || dog.deliveries().route()!=null)return;search=level.getGameTime()+60;
        var home=dog.society().hearth();if(home==null)return;
        if(dog.learning().knowledge().knows(Learning.Routine.DELIVERY) && archiveJob(home)!=null)return;
        for(var mentor:home.residents())if(mentor!=dog && dog.distanceToSqr(mentor)<=64 && HomeRoutes.visible(dog,mentor) && expert(mentor,home)
                && home.residents().stream().noneMatch(other->mentor.getUUID().equals(other.courierLore().teacher))) {teacher=mentor.getUUID();break;}
    }
    boolean guided(DeliveryMemory.Route route) {
        if(!(dog.level() instanceof ServerLevel level) || !dog.learning().knowledge().knows(Learning.Routine.DELIVERY))return false;
        var home=dog.society().hearth();if(home==null)return false;
        if(guide!=null && level.getEntity(guide) instanceof Bunnidog known && validGuide(known,home,route))return true;
        if(level.getGameTime()<guideSearch)return false;guideSearch=level.getGameTime()+20;guide=null;
        for(var mentor:home.residents())if(mentor!=dog && validGuide(mentor,home,route)) {guide=mentor.getUUID();return true;}return false;
    }
    private boolean validGuide(Bunnidog mentor,HearthTreeBlockEntity home,DeliveryMemory.Route route) {
        return dog.distanceToSqr(mentor)<=144 && expert(mentor,home) && DeliveryMemory.sameProcedure(route,mentor.deliveries().route()) && HomeRoutes.visible(dog,mentor);
    }
    private boolean sees(Bunnidog mentor,BlockPos stop) {
        if(!receptive() || instructor()!=mentor || dog.distanceToSqr(mentor)>64 || dog.distanceToSqr(Vec3.atCenterOf(stop))>64 || !HomeRoutes.visible(dog,mentor))return false;
        var from=dog.blockPosition();if(!dog.level().hasChunksAt(new BlockPos(Math.min(from.getX(),stop.getX()),from.getY(),Math.min(from.getZ(),stop.getZ())),new BlockPos(Math.max(from.getX(),stop.getX()),from.getY(),Math.max(from.getZ(),stop.getZ()))))return false;
        var hit=dog.level().clip(new ClipContext(dog.getEyePosition(),Vec3.atCenterOf(stop),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,dog));return hit.getType()==HitResult.Type.MISS || hit.getBlockPos().equals(stop);
    }
    static void pickedUp(Bunnidog mentor,DeliveryMemory.Route route,int count) {
        var home=mentor.society().hearth();if(home==null || !expert(mentor,home))return;
        for(var pupil:home.residents())if(mentor.getUUID().equals(pupil.courierLore().teacher)) {
            var lesson=pupil.courierLore();lesson.example=null;lesson.witnessed=0;
            if(lesson.sees(mentor,route.source().at().pos())) {lesson.example=route;lesson.witnessed=count;}
        }
    }
    static void delivered(Bunnidog mentor,DeliveryMemory.Route route,int count) {
        var home=mentor.society().hearth();if(home==null || !expert(mentor,home))return;
        for(var pupil:home.residents())if(mentor.getUUID().equals(pupil.courierLore().teacher)) {
            var lesson=pupil.courierLore();var seen=lesson.example;int amount=lesson.witnessed;lesson.example=null;lesson.witnessed=0;
            if(amount<=0 || !DeliveryMemory.sameRoute(seen,route) || !lesson.sees(mentor,route.target().at().pos()))continue;
            pupil.learning().deliveryPractice();
            ((ServerLevel)pupil.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,pupil.getX(),pupil.getY()+.5,pupil.getZ(),4,.15,.15,.15,0);
            if(pupil.learning().knowledge().knows(Learning.Routine.DELIVERY) && pupil.deliveries().learnHousehold(new DeliveryMemory.Route(route.source(),route.target(),route.filter(),Math.min(route.batch(),Math.min(amount,count)),route.owner()))) {
                pupil.society().taught(Learning.Routine.DELIVERY);lesson.cancel();
                var owner=((ServerLevel)pupil.level()).getPlayerByUUID(route.owner());if(owner!=null && owner.distanceToSqr(pupil)<=256)owner.sendOverlayMessage(Component.translatable("message.bunnidogs.courier.taught",pupil.getDisplayName(),mentor.getDisplayName()));
            }
        }
    }
    private boolean canRead(RouteNotes notes,HearthTreeBlockEntity home) {
        return notes!=null && notes.available(home) && receptive() && dog.deliveries().route()==null && dog.learning().knowledge().knows(Learning.Routine.DELIVERY)
                && (CapabilityRules.potential(dog.mind(),CapabilityRules.Task.DELIVERY)==CapabilityRules.Potential.INDEPENDENT || guided(notes.route()));
    }
    public SocietyWork.Job archiveJob(HearthTreeBlockEntity home) {
        if(apprenticing() || !receptive())return null;
        if(dog.deliveries().route()==null)for(int slot=0;slot<home.getContainerSize();slot++)if(canRead(RouteNotes.read(home.getItem(slot),(ServerLevel)dog.level()).orElse(null),home))return new SocietyWork.Job(SocietyWork.Kind.STUDY_ROUTE,home.getBlockPos(),slot,home.getBlockState());
        if(!canWrite(home))return null;
        for(int slot=0;slot<home.getContainerSize();slot++)if(RouteNotes.blank(home.getItem(slot)))return new SocietyWork.Job(SocietyWork.Kind.RECORD_ROUTE,home.getBlockPos(),slot,home.getBlockState());return null;
    }
    private boolean canWrite(HearthTreeBlockEntity home) {
        var route=dog.deliveries().route();if(!expert(dog,home) || !dog.deliveries().proven() || !RouteNotes.recordable(route.filter()))return false;
        int records=0;for(int i=0;i<home.getContainerSize();i++){var book=home.getItem(i);if(book.is(ModItems.ROUTE_NOTES.get()))records++;var note=RouteNotes.read(book,(ServerLevel)dog.level()).orElse(null);if(note!=null && note.hearth().equals(home.site()) && DeliveryMemory.sameRoute(note.route(),route))return false;}return records<8;
    }
    public boolean record(HearthTreeBlockEntity home,int slot) {
        if(busy || !receptive() || !canWrite(home) || slot<0 || slot>=home.getContainerSize())return false;
        var original=home.getItem(slot).copy();if(!RouteNotes.blank(original))return false;var route=dog.deliveries().route();var notes=new RouteNotes(home.site(),dog.getUUID(),route);var result=notes.write(original,dog);if(result.isEmpty())return false;
        busy=true;
        try {
            if(!EventHooks.canEntityGrief((ServerLevel)dog.level(),dog) || !receptive() || dog.society().hearth()!=home || !canWrite(home) || !DeliveryMemory.sameRoute(route,dog.deliveries().route()) || !ItemStack.matches(original,home.getItem(slot)))return false;
            var items=VanillaContainerWrapper.of(home);
            try(var tx=Transaction.openRoot()) {
                if(items.extract(slot,ItemResource.of(original),1,tx)!=1 || items.insert(slot,ItemResource.of(result),1,tx)!=1 || !home.attached())return false;tx.commit();return true;
            }
        } finally {busy=false;}
    }
    public boolean study(HearthTreeBlockEntity home,int slot) {
        if(busy || slot<0 || slot>=home.getContainerSize())return false;var book=home.getItem(slot);var notes=RouteNotes.read(book,(ServerLevel)dog.level()).orElse(null);
        if(!canRead(notes,home) || dog.society().hearth()!=home)return false;
        if(dog.deliveries().learnHousehold(notes.route())) {dog.society().taught(Learning.Routine.DELIVERY);return true;}return false;
    }
}
