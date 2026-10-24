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
    private int life=600,biteWait;
    public ApexMinion(EntityType<? extends ApexMinion> type,Level level) {
        super(type,level); setGenome(genome()); setHealth(getMaxHealth()); refreshDimensions();
    }
    public static AttributeSupplier.Builder createAttributes() { return Bunnidog.createAttributes().add(Attributes.SCALE,1); }
    @Override protected void applyGenetics() {
        float fraction=getMaxHealth()>0 ? getHealth()/getMaxHealth() : 1;
        var traits=genome().traits();var stats=stats();var combat=com.bunnidogs.mod.genetics.Combat.profile(genome(),stats);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.clamp(8*traits.healthMultiplier()*stats.multiplier(com.bunnidogs.mod.genetics.Stat.VITALITY),16,48));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.clamp(combat.bite()*1.6,5.5,12));
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Math.clamp(.25*traits.speedMultiplier()*stats.multiplier(com.bunnidogs.mod.genetics.Stat.SWIFTNESS),.4,.65));
        getAttribute(Attributes.ATTACK_SPEED).setBaseValue(20.0/combat.interval());
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40);
        applyAdaptations();setHealth(getMaxHealth()*fraction);
    }
    public void caller(Apex apex) {
        caller=apex.getUUID();var rng=apex.getRandom();var genes=apex.genome();
        var builds=new String[]{"average","stocky","woolly","chonker"};var legs=new String[]{"long","springy","fleet"};
        for(int slot=0;slot<2;slot++) {
            genes=genes.with(com.bunnidogs.mod.genetics.Gene.BUILD,slot,com.bunnidogs.mod.genetics.Gene.BUILD.alleleNamed(builds[rng.nextInt(builds.length)]).id());
            genes=genes.with(com.bunnidogs.mod.genetics.Gene.LEGS,slot,com.bunnidogs.mod.genetics.Gene.LEGS.alleleNamed(legs[rng.nextInt(legs.length)]).id());
            genes=genes.with(com.bunnidogs.mod.genetics.Gene.TEMPERAMENT,slot,2);
        }
        if(apex.flying())genes=com.bunnidogs.mod.genetics.Wings.winged(genes);
        setGenome(genes);setStats(com.bunnidogs.mod.genetics.Stats.of(25+rng.nextInt(26),35+rng.nextInt(31),30+rng.nextInt(31),15+rng.nextInt(21),0));
        setHealth(getMaxHealth());setAge(0);setTarget(apex.getTarget());
    }
    @Override public boolean doHurtTarget(ServerLevel level,Entity enemy) {
        if(biteWait>0)return false;
        biteWait=com.bunnidogs.mod.genetics.Combat.profile(genome(),stats()).interval();
        return super.doHurtTarget(level,enemy);
    }
    public boolean belongsTo(Apex apex) { return apex.getUUID().equals(caller); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this)); goalSelector.addGoal(1,new WingFlightGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1,true) {
            @Override protected void checkAndPerformAttack(net.minecraft.world.entity.LivingEntity target) {
                if(biteWait==0 && isWithinMeleeAttackRange(target) && hasLineOfSight(target)) {
                    swing(net.minecraft.world.InteractionHand.MAIN_HAND);doHurtTarget((ServerLevel)level(),target);
                }
            }
            @Override public boolean canUse() { return !com.bunnidogs.mod.genetics.Wings.expressed(genome()) && super.canUse(); }
            @Override public boolean canContinueToUse() { return !com.bunnidogs.mod.genetics.Wings.expressed(genome()) && super.canContinueToUse(); }
        });
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public void tick() {
        if(biteWait>0)biteWait--;
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
