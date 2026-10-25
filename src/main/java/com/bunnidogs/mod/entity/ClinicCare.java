package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.NestBlock;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.society.HearthTreeBlockEntity;
import java.util.function.BooleanSupplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/** Quiet wound care. Native healing hooks run before committing the exact owned dressing. */
public final class ClinicCare {
    public static final int REST=200;
    public static final float HEAL=4;
    private final Bunnidog patient;
    private long until;
    private boolean treating;
    ClinicCare(Bunnidog patient) {this.patient=patient;}
    public boolean busy() {return treating;}
    public int waitTicks() {return (int)Math.clamp(until-patient.level().getGameTime(),0L,REST);}
    private boolean condition() {
        return patient.level() instanceof ServerLevel && patient.isAlive() && !patient.isRemoved() && patient.acceptsCare()
                && patient.getHealth()<patient.getMaxHealth() && waitTicks()==0 && !patient.combat().downed() && !patient.combat().danger()
                && patient.getTarget()==null && !patient.seekingRefuge() && !patient.beingTransported() && !patient.isVehicle()
                && !patient.trail().ordered() && !patient.isInLove() && !patient.isOnFire() && !patient.isInWater() && !patient.isInLava()
                && (patient.getLastHurtByMob()==null || patient.tickCount-patient.getLastHurtByMobTimestamp()>=100)
                && patient.onGround() && HomeRoutes.dryCell(patient,patient.blockPosition());
    }
    public boolean ready() {return !treating && condition();}
    static boolean restingAtHome(HearthTreeBlockEntity home,Bunnidog patient) {
        if(!home.attached() || patient==null || !patient.society().belongs(home) || !patient.clinic().condition()
                || patient.trail().keeper()!=null && !patient.trail().keeper().equals(home.owner()))return false;
        var bed=patient.homeNest();var level=(ServerLevel)home.getLevel();
        return bed!=null && bed.distSqr(home.getBlockPos())<=24*24 && level.hasChunkAt(bed) && level.getBlockState(bed).is(ModBlocks.NEST.get())
                && NestBlock.claimed(level,bed) && patient.distanceToSqr(Vec3.atCenterOf(bed))<=9;
    }
    /** Revalidate after callbacks, then pay before changing native health. A refusal keeps the dressing. */
    boolean treat(BooleanSupplier stillValid,Runnable spend) {
        if(!ready() || !stillValid.getAsBoolean())return false;
        treating=true;
        try {
            float before=patient.getHealth();float amount=EventHooks.onLivingHeal(patient,HEAL);
            if(!Float.isFinite(amount) || amount<=0 || !condition() || patient.getHealth()!=before || !stillValid.getAsBoolean())return false;
            spend.run();until=patient.level().getGameTime()+REST;patient.setHealth(before+amount);
            ((ServerLevel)patient.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,patient.getX(),patient.getY()+.5,patient.getZ(),5,.2,.2,.2,0);
            return true;
        } finally {treating=false;}
    }
    void save(ValueOutput out) {if(waitTicks()>0)out.putInt("ClinicRest",waitTicks());}
    void load(ValueInput in) {until=patient.level().getGameTime()+Math.clamp(in.getIntOr("ClinicRest",0),0,REST);treating=false;}
}
