package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Personal combat experience above reliable instinct/recall; cognition never changes physical bite stats. */
public final class CombatTactics {
    private final Bunnidog dog;
    private int experience;
    CombatTactics(Bunnidog dog) {this.dog=dog;}
    public int experience() {return experience;}
    private CapabilityRules.Potential potential() {return CapabilityRules.potential(dog.mind(),CapabilityRules.Task.TACTICS);}
    private boolean instructed() {
        var keeper=dog.trail().keeperPlayer();
        return keeper!=null && keeper.isAlive() && dog.distanceToSqr(keeper)<=Math.pow(Trail.hearing(dog.genome(),dog.stats()),2) && dog.getSensing().hasLineOfSight(keeper)
                && (keeper.getLastHurtMob()!=null && keeper.tickCount-keeper.getLastHurtMobTimestamp()<100
                    || keeper.getLastHurtByMob()!=null && keeper.tickCount-keeper.getLastHurtByMobTimestamp()<100);
    }
    public boolean coordinates() {return experience==Learning.MASTERED && potential()!=CapabilityRules.Potential.LIMITED && (potential()==CapabilityRules.Potential.INDEPENDENT || instructed());}
    /** Caller confirms native damage. Only a real exchange involving this keeper teaches. */
    void practiced(Mob enemy) {
        var keeper=dog.trail().keeperPlayer();
        if(experience==Learning.MASTERED || potential()==CapabilityRules.Potential.LIMITED || !dog.combat().selected() || !instructed() || keeper==null
                || !(dog.combat().focusedOn(enemy) || keeper.getLastHurtMob()==enemy || keeper.getLastHurtByMob()==enemy || enemy.getTarget()==keeper))return;
        int gain=Math.clamp(1+(int)Math.floor(dog.mind().memory()+dog.mind().communication()),1,Learning.MASTERED);experience=Math.min(Learning.MASTERED,experience+gain);
        if(experience==Learning.MASTERED)keeper.sendOverlayMessage(Component.translatable("message.bunnidogs.tactics.learned",dog.getDisplayName()));
    }
    public Component status() {
        String key=potential()==CapabilityRules.Potential.LIMITED ? "instinct" : experience<Learning.MASTERED ? "learning" : potential()==CapabilityRules.Potential.SUPPORTED ? "guided" : "coordinated";
        return Component.translatable("care.bunnidogs.tactics."+key);
    }
    void save(ValueOutput out) {out.putInt("CombatTactics",experience);}
    void load(ValueInput in,boolean established) {experience=Math.clamp(in.read("CombatTactics",com.mojang.serialization.Codec.INT).orElse(established ? Learning.MASTERED : 0),0,Learning.MASTERED);}
}
