package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Temperament;
import com.bunnidogs.mod.genetics.Trail;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Local, visible warnings. A fuse always matters; a selected skittish lookout gives earlier notice. */
final class CompanionWarnings {
    private record Notice(long tick,boolean urgent) {}
    private static final Map<UUID,Notice> NOTICES=new WeakHashMap<>();
    private final Bunnidog dog;
    private int cooldown, alert;
    private long nextScan;
    private Mob watched;
    CompanionWarnings(Bunnidog dog) { this.dog=dog; }
    boolean alerting() { return alert>0; }
    void tick() {
        if (cooldown>0) cooldown--;
        if (alert>0) alert--;
        if (!(dog.level() instanceof ServerLevel level) || !dog.trail().ordered() || dog.isBaby()
                || dog.combat().recovering() || dog.beingTransported() || dog.isVehicle()) return;
        var keeper=dog.trail().keeperPlayer();
        if (keeper==null) return;
        if (alert>0 && watched!=null && watched.isAlive() && dog.getTarget()==null) dog.getLookControl().setLookAt(watched,30,30);
        long now=level.getGameTime(); if (now<nextScan) return; nextScan=now+10;
        boolean lookout=dog.combat().selected() && dog.combat().assisting() && dog.temperament()==Temperament.SKITTISH;
        double radius=lookout ? Math.min(20,Trail.hearing(dog.genome(),dog.stats())) : 6;
        var threat=level.getEntitiesOfClass(Mob.class,dog.getBoundingBox().inflate(radius),mob->mob instanceof Enemy
                && !(mob instanceof Bunnidog) && !mob.hasCustomName() && !mob.isPassenger() && !mob.isVehicle()
                && mob.isAlive() && !dog.isAlliedTo(mob) && dog.distanceToSqr(mob)<=radius*radius
                && (lookout || mob instanceof Creeper creeper && creeper.getSwellDir()>0) && HomeRoutes.visible(dog,mob))
                .stream().min(Comparator.comparingInt((Mob mob)->mob instanceof Creeper ? 0 : 1)
                        .thenComparingDouble(dog::distanceToSqr)).orElse(null);
        if (threat==null) return;
        boolean urgent=threat instanceof Creeper creeper && creeper.getSwellDir()>0;
        if (urgent && dog.distanceToSqr(threat)<=36) dog.combat().avoid(threat);
        if (cooldown>0 && !urgent) return;
        var last=NOTICES.get(keeper.getUUID());
        if (last!=null && now-last.tick<100 && (!urgent || last.urgent)) return;
        NOTICES.put(keeper.getUUID(),new Notice(now,urgent)); cooldown=urgent ? 100 : 400; alert=40; watched=threat;
        keeper.sendOverlayMessage(Component.translatable("message.bunnidogs.combat.warning",dog.getDisplayName(),threat.getDisplayName()));
        level.playSound(null,dog.blockPosition(),SoundEvents.NOTE_BLOCK_BELL.value(),SoundSource.NEUTRAL,.45F,urgent ? 1.6F : 1.2F);
    }
    void save(ValueOutput out) { if (cooldown>0) out.putInt("CombatWarningRest",cooldown); }
    void restore(ValueInput in) { cooldown=Math.clamp(in.getIntOr("CombatWarningRest",0),0,400); alert=0; watched=null; nextScan=0; }
}
