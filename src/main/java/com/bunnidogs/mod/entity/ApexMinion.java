package com.bunnidogs.mod.entity;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Temporary hostile offspring are never a source of permanent animals, materials or experience. */
public final class ApexMinion extends Bunnidog implements Enemy {
    private UUID caller;
    private int life=600;
    public ApexMinion(EntityType<? extends ApexMinion> type,Level level) {
        super(type,level); setGenome(genome()); setHealth(getMaxHealth()); refreshDimensions();
    }
    public static AttributeSupplier.Builder createAttributes() { return Bunnidog.createAttributes().add(Attributes.SCALE,.7); }
    @Override protected void applyGenetics() {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(8);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.8);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.34);
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(32);
    }
    public void caller(Apex apex) { caller=apex.getUUID(); setGenome(apex.genome()); setTarget(apex.getTarget()); }
    public boolean belongsTo(Apex apex) { return apex.getUUID().equals(caller); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this)); goalSelector.addGoal(1,new MeleeAttackGoal(this,1,true));
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && (--life<=0 || tickCount%10==0 && (caller==null
                || !(server.getEntity(caller) instanceof Apex apex) || !apex.isAlive() || distanceToSqr(apex)>1600))) discard();
    }
    @Override public boolean acceptsCare() { return false; }
    @Override public boolean canEat(ItemStack stack) { return false; }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override protected boolean canRide(Entity entity) { return false; }
    @Override protected int getBaseExperienceReward(ServerLevel level) { return 0; }
    @Override public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output); output.putInt("ApexMinionLife",life);
        if (caller!=null) output.putString("ApexCaller",caller.toString());
    }
    @Override public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input); life=Math.clamp(input.getIntOr("ApexMinionLife",0),0,600);
        try { caller=UUID.fromString(input.getStringOr("ApexCaller","")); } catch (IllegalArgumentException ignored) { life=0; }
        setAge(0); applyGenetics();
    }
}
