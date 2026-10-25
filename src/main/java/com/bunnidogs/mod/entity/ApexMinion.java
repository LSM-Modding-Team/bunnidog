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
    private UUID handler;
    private boolean held;
    private int controlAge,flightAge,flightDuration;
    private net.minecraft.world.phys.Vec3 flightFrom,flightTo;
    private net.minecraft.world.entity.LivingEntity thrownAt;
    public ApexMinion(EntityType<? extends ApexMinion> type,Level level) {
        super(type,level); setGenome(genome()); setHealth(getMaxHealth()); refreshDimensions();
        getNavigation().setCanFloat(true);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,0);
    }
    public static AttributeSupplier.Builder createAttributes() { return Bunnidog.createAttributes().add(Attributes.SCALE,1).add(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED,3); }
    @Override protected void applyGenetics() {
        float fraction=getMaxHealth()>0 ? getHealth()/getMaxHealth() : 1;
        var traits=genome().traits();var stats=stats();var combat=com.bunnidogs.mod.genetics.Combat.profile(genome(),stats);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(.9*Math.clamp(.85*8*traits.healthMultiplier()*stats.multiplier(com.bunnidogs.mod.genetics.Stat.VITALITY),16,40));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(.85*Math.clamp(combat.bite()*1.6,5.5,12));
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
    public boolean controlled() { return handler!=null || flightDuration>0; }
    public boolean held() { return held; }
    void returnTo(Apex apex) { handler=apex.getUUID();held=false;controlAge=0; }
    void hold(Apex apex) { handler=apex.getUUID();held=true;controlAge=0;setNoAi(false);setNoGravity(true); }
    void release() { handler=null;held=false;flightDuration=0;controlAge=0;setNoAi(false);setNoGravity(false);biteWait=10; }
    public void launchAt(net.minecraft.world.entity.LivingEntity target) {
        held=false;handler=null;flightFrom=position();flightTo=target.position().add(0,.4,0);thrownAt=target;flightAge=0;
        flightDuration=Math.clamp((int)Math.ceil(flightFrom.distanceTo(flightTo)/1.2),10,28);setNoAi(false);setNoGravity(true);
    }
    private void controlledTick(ServerLevel level) {
        if(flightDuration>0) {
            double t=Math.min(1,(double)++flightAge/flightDuration),height=Math.max(2,flightFrom.distanceTo(flightTo)*.12);
            var point=flightFrom.lerp(flightTo,t).add(0,4*height*t*(1-t),0);var step=point.subtract(position());
            if(step.lengthSqr()>16 || !level.hasChunksAt(blockPosition().offset(-3,-3,-3),net.minecraft.core.BlockPos.containing(point).offset(3,3,3))) {release();return;}
            var blocked=level.clip(new net.minecraft.world.level.ClipContext(position().add(0,.4,0),point.add(0,.4,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));
            if(blocked.getType()!=net.minecraft.world.phys.HitResult.Type.MISS) {release();return;}
            for(var enemy:level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,getBoundingBox().expandTowards(step).inflate(.2),e->e.isAlive() && !(e instanceof Bunnidog)
                    && !e.isSpectator() && !(e instanceof Player p && p.hasInfiniteMaterials()) && (e==thrownAt || e instanceof Player))) {
                if(enemy.getBoundingBox().inflate(getBbWidth()/2,.5,getBbWidth()/2).clip(position().add(0,.4,0),point.add(0,.4,0)).isEmpty())continue;
                Entity owner=caller==null ? this : level.getEntity(caller);var source=damageSources().mobAttack(owner instanceof net.minecraft.world.entity.LivingEntity living ? living : this);
                float damage=getHealth()*.4F;
                if(enemy.hurtServer(level,source,damage)) {
                    var away=enemy.position().subtract(owner==null ? position() : owner.position());
                    enemy.knockback(2, -away.x,-away.z,source,damage);enemy.setDeltaMovement(enemy.getDeltaMovement().add(0,.25,0));enemy.hurtMarked=true;
                }
                release();return;
            }
            setDeltaMovement(step);resetFallDistance();hurtMarked=true;
            if(flightAge>=flightDuration)release();return;
        }
        if(handler==null)return;
        if(!(level.getEntity(handler) instanceof Apex apex) || !apex.isAlive() || ++controlAge>100) {release();return;}
        if(held) {setPos(apex.skills().mouth().add(0,-.3,0));setDeltaMovement(0,0,0);return;}
        var toward=apex.skills().mouth().subtract(position());
        if(com.bunnidogs.mod.genetics.Wings.expressed(genome())) {setNoGravity(true);ApexFlightGoal.fly(this,apex.skills().mouth(),.75);}
        else {setNoGravity(false);var flat=toward.multiply(1,0,1).normalize().scale(.45);setDeltaMovement(flat.x,horizontalCollision && onGround() ? .5 : isInWater() ? .2 : getDeltaMovement().y,flat.z);}
    }
    public boolean belongsTo(Apex apex) { return apex.getUUID().equals(caller); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.Goal() {
            {setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
            @Override public boolean canUse() {return controlled();}
            @Override public boolean requiresUpdateEveryTick() {return true;}
            @Override public void tick() {getNavigation().stop();}
        });
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
        if(hasEffect(com.bunnidogs.mod.registry.ModEffects.RESONANT_STUN)) {super.tick();return;}
        if(biteWait>0)biteWait--;
        if(controlled()) {setNoAi(false);if(held || flightDuration>0)setNoGravity(true);}
        super.tick();
        if(level() instanceof ServerLevel server) {
            if(controlled())controlledTick(server);
            else if(!isNoAi())ApexMobility.swim(this,getTarget(),com.bunnidogs.mod.genetics.Wings.expressed(genome()),false,false);
        }
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
        setAge(0); applyGenetics();release();
    }
}
