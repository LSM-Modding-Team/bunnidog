package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.crown.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.network.ApexBeamPayload;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;

/** The finale uses one native health pool and one attack director; no damage caps, repeated health refills, or attack pileups. */
public final class CrownedApex extends Apex {
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(CrownedApex.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WARNING=SynchedEntityData.defineId(CrownedApex.class,EntityDataSerializers.INT);
    private final ServerBossEvent crownBar=new ServerBossEvent(getUUID(),Component.translatable("entity.bunnidogs.crowned_apex"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_6);
    private BlockPos arena=BlockPos.ZERO;
    private int roster=1,cast,elapsed,recovery=60,index,biteWait,addWait=80;
    private boolean livingPack;
    private float linkBudget=120;
    private Vec3 aim=Vec3.ZERO,origin=Vec3.ZERO,addAim,addOrigin;
    private UUID aimedAdd;
    public CrownedApex(EntityType<? extends CrownedApex> type,Level level) {super(type,level);}
    public static AttributeSupplier.Builder createAttributes() {return Apex.createAttributes().add(ModAttributes.CROWN_HEALTH,3000).add(Attributes.SCALE,4.6);}
    // Only this creature redirects native max-health lookups. Vanilla/global attribute bounds stay intact.
    private Holder<Attribute> nativeAttribute(Holder<Attribute> a) {return a.equals(Attributes.MAX_HEALTH) ? ModAttributes.CROWN_HEALTH : a;}
    @Override public AttributeInstance getAttribute(Holder<Attribute> a) {return super.getAttribute(nativeAttribute(a));}
    @Override public double getAttributeValue(Holder<Attribute> a) {return super.getAttributeValue(nativeAttribute(a));}
    @Override public double getAttributeBaseValue(Holder<Attribute> a) {return super.getAttributeBaseValue(nativeAttribute(a));}
    @Override protected void applyGenetics() {
        float fraction=getMaxHealth()>0 ? getHealth()/getMaxHealth() : 1;
        super.applyGenetics();
        getAttribute(ModAttributes.CROWN_HEALTH).setBaseValue(3000*(1+.55*(Math.clamp(roster,1,4)-1)));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(phase()==3 ? 44 : 30);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.36);
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(72);
        getAttribute(Attributes.ARMOR).setBaseValue(16);
        setHealth(getMaxHealth()*fraction);
    }
    public void prepare(BlockPos center,int players) {arena=center.immutable();roster=Math.clamp(players,1,4);applyGenetics();setHealth(getMaxHealth());}
    @Override protected void defineSynchedData(SynchedEntityData.Builder data) {super.defineSynchedData(data);data.define(PHASE,1);data.define(WARNING,0);}
    public int phase() {return entityData.get(PHASE);}
    public int warning() {return entityData.get(WARNING);}
    public int roster() {return roster;}
    public float linkBudget() {return linkBudget;}
    @Override public boolean flying() {return phase()==3;}
    @Override public boolean causeFallDamage(double distance,float modifier,DamageSource source) {return false;}
    @Override public boolean wild() {return false;}
    @Override public void makeWild() {} // This encounter is never part of natural ecology.
    @Override protected void registerGoals() {
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,64));
        targetSelector.addGoal(1,new HurtByTargetGoal(this,Apex.class));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public void startSeenByPlayer(ServerPlayer p) {crownBar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p) {crownBar.removePlayer(p);}
    @Override protected int getBaseExperienceReward(ServerLevel level) {var seal=CrownEncounter.controller(this);return seal!=null && !seal.spawning() ? 10000 : 0;}
    private boolean victim(LivingEntity e) {return e!=null && e.isAlive() && !ApexSkills.family(e) && !e.isSpectator() && !(e instanceof Player p && p.hasInfiniteMaterials());}
    @Override public void tick() {
        super.tick();if(!(level() instanceof ServerLevel server) || !isAlive())return;
        if(flying()) {setNoGravity(true);resetFallDistance();getNavigation().stop();}
        crownBar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));
        if(!CrownEncounter.member(this) && arena.equals(BlockPos.ZERO))arena=blockPosition().below();
        int next=getHealth()<=getMaxHealth()/3 ? 3 : getHealth()<=getMaxHealth()*2/3 ? 2 : 1;
        if(next>phase())changePhase(server,next);
        if(isNoAi() || hasEffect(ModEffects.RESONANT_STUN))return;
        if(biteWait>0)biteWait--;
        var target=getTarget();
        if(!victim(target) || !inside(target.position(),7)) {
            target=server.getNearestPlayer(getX(),getY(),getZ(),70,p->p instanceof LivingEntity living && victim(living) && inside(p.position(),7));setTarget(target);
        }
        if(!victim(target)) {getNavigation().stop();return;}
        getLookControl().setLookAt(target,35,35);
        if(cast!=0) {resolve(server,target);return;}
        if(!mobility().leaping()) {
            if(phase()==3) {
                setNoGravity(true);resetFallDistance();double angle=tickCount*.025;
                Vec3 hover=Vec3.atBottomCenterOf(arena).add(Math.cos(angle)*17,17,Math.sin(angle)*17);
                ApexFlightGoal.fly(this,hover,.65);
            } else {
                setNoGravity(false);
                if(distanceToSqr(target)>16)getNavigation().moveTo(target,1);
                else {getNavigation().stop();if(biteWait==0 && isWithinMeleeAttackRange(target) && hasLineOfSight(target)) {doHurtTarget(server,target);biteWait=24;}}
            }
        }
        if(phase()==2) {if(!livingPack) {livingPack=true;pack(server);}links(server);}
        if(--recovery>0 || mobility().leaping())return;
        int choice=phase()==1 ? (++index%2==0 ? 2 : 1) : phase()==2 ? 2 : (++index%2==0 ? 4 : 1);
        beginCast(choice,target);
    }
    private boolean inside(Vec3 p,int margin) {return Math.abs(p.x-arena.getX()-.5)<=40+margin && Math.abs(p.z-arena.getZ()-.5)<=40+margin && p.y>=arena.getY()-3 && p.y<=arena.getY()+33;}
    private void changePhase(ServerLevel server,int value) {
        cancel();cleanup(server,CrownEncounter.controller(this)==null ? null : CrownEncounter.controller(this).run(),Vec3.atCenterOf(arena));
        entityData.set(PHASE,value);crownBar.setColor(value==3 ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.GREEN);
        crownBar.setName(Component.translatable("entity.bunnidogs.crowned_apex.phase_"+value));recovery=70;applyGenetics();
        server.sendParticles(ParticleTypes.END_ROD,getX(),getEyeY(),getZ(),4,1,1,1,0);
        playSound(SoundEvents.WITHER_SPAWN,1,.65F);
    }
    public void beginCast(int choice,LivingEntity target) {
        if(choice!=1 && choice!=2 && choice!=4 || !victim(target))return;
        cast=choice;elapsed=0;origin=skills().mouth();aim=target.getEyePosition();entityData.set(WARNING,choice);getNavigation().stop();setDeltaMovement(Vec3.ZERO);
        playSound(choice==2 ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.RAVAGER_ROAR,.9F,choice==4 ? .6F : 1.2F);
    }
    public void cancel() {cast=0;elapsed=0;addAim=addOrigin=null;aimedAdd=null;entityData.set(WARNING,0);mobility().stopLeap();setNoGravity(flying());}
    private void resolve(ServerLevel server,LivingEntity target) {
        elapsed++;getNavigation().stop();if(!mobility().leaping())setDeltaMovement(getDeltaMovement().multiply(.4,0,.4));
        if(cast==1) {
            // A readable orange landing marker precedes a real colliding, terrain-clearing arc.
            if(elapsed<28) {if(elapsed%4==0)ring(server,aim.add(0,-target.getEyeHeight(),0),3,ParticleTypes.FLAME);}
            else if(elapsed==28) {mobility().leap(target,aim.add(0,-target.getEyeHeight(),0));entityData.set(WARNING,0);}
            if(elapsed>28 && !mobility().leaping())finish(45);
        } else if(cast==2) {
            if(elapsed<=34) {if(elapsed%4==0)beam(server,origin,aim.subtract(origin),false);}
            else if(elapsed<=94) {if((elapsed-34)%10==0)beam(server,origin,aim.subtract(origin),true);}
            else finish(55);
        } else if(cast==4) {
            var c=Vec3.atBottomCenterOf(arena).add(0,1,0);
            var left=c.add(index%4==0 ? 22 : 0,0,index%4==0 ? 0 : 22);var right=c.add(index%4==0 ? -22 : 0,0,index%4==0 ? 0 : -22);
            if(elapsed<=60 && elapsed%4==0) {ring(server,left,19,ParticleTypes.FLAME);ring(server,right,19,ParticleTypes.FLAME);}
            if(elapsed==64) {ApexSkills.explosion(server,this,left,9,false);ApexSkills.explosion(server,this,right,9,false);}
            if(elapsed>=68)finish(65);
        }
    }
    private void finish(int ticks) {cast=0;entityData.set(WARNING,0);recovery=ticks;}
    private void beam(ServerLevel level,Vec3 start,Vec3 direction,boolean damage) {
        if(direction.lengthSqr()<.001)return;
        var unit=direction.normalize();Vec3 end=start;
        for(int i=1;i<=64;i++) {var next=start.add(unit.scale(i));if(!inside(next,9))break;end=next;}
        var box=new AABB(start,end).inflate(1.25);if(!level.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))return;
        PacketDistributor.sendToPlayersTrackingEntity(this,new ApexBeamPayload(start,end,damage ? 0 : 1));
        if(!damage)return;
        var source=new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,Identifier.fromNamespaceAndPath("bunnidogs","apex_beam"))),this);
        for(var enemy:level.getEntitiesOfClass(LivingEntity.class,box,this::victim))if(enemy.getBoundingBox().inflate(1.25).clip(start,end).isPresent())enemy.hurtServer(level,source,24);
    }
    private static void ring(ServerLevel server,Vec3 at,int radius,net.minecraft.core.particles.ParticleOptions particle) {
        for(int i=0;i<24;i++) {double a=i*Math.PI/12;server.sendParticles(particle,at.x+Math.cos(a)*radius,at.y+.25,at.z+Math.sin(a)*radius,1,0,.05,0,0);}
    }
    private void pack(ServerLevel server) {
        for(int i=0;i<5;i++) {
            Apex ally=i<2 ? ModEntities.apex(i==0 ? ApexKind.SUPPORT : ApexKind.PRISM,false).create(server,EntitySpawnReason.MOB_SUMMONED) : null;
            Bunnidog mob=ally!=null ? ally : ModEntities.APEX_MINION.get().create(server,EntitySpawnReason.MOB_SUMMONED);if(mob==null)continue;
            var seal=CrownEncounter.controller(this);if(seal==null) {mob.discard();return;}
            CrownEncounter.bind(mob,seal);
            if(mob instanceof ApexMinion pup)pup.caller(this);else mob.setNoAi(true);
            double angle=i*Math.PI*2/5;mob.snapTo(Vec3.atBottomCenterOf(arena).add(Math.cos(angle)*12,1,Math.sin(angle)*12));
            var bounds=mob.getBoundingBox();
            if(!server.hasChunksAt(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ))) {mob.discard();continue;}
            if(!server.noCollision(mob) || !server.addFreshEntity(mob))mob.discard();
        }
    }
    private void links(ServerLevel server) {
        var seal=CrownEncounter.controller(this);if(seal==null)return;
        var adds=server.getEntitiesOfClass(Apex.class,getBoundingBox().inflate(60),e->e!=this && e.isAlive() && CrownEncounter.controller(e)==seal);
        if(tickCount%20==0 && linkBudget>0) {
            var support=adds.stream().filter(e->e.kind()==ApexKind.SUPPORT).findFirst().orElse(null);
            if(support!=null) {
                float before=getHealth(),paid=Math.min(8,linkBudget);heal(paid);linkBudget-=Math.clamp(getHealth()-before,0,paid);
                PacketDistributor.sendToPlayersTrackingEntity(this,new ApexBeamPayload(support.skills().mouth(),getEyePosition(),3));
            }
        }
        // Only one add major, and only while the boss is recovering. These directed casters never start native autonomous casts.
        if(--addWait<=0 && cast==0 && recovery>=25 && getTarget()!=null) {
            adds.stream().filter(e->e.kind()==ApexKind.PRISM).findFirst().ifPresent(prism->{
                aimedAdd=prism.getUUID();addOrigin=prism.skills().mouth();addAim=getTarget().getEyePosition().subtract(addOrigin);prism.skills().trace(server,addOrigin,addAim,false);
            });
            // The aimed line is a warning; resolution follows 20 ticks later, unless a boss cast interrupts it.
            addWait=100;
        }
        if(addWait==80 && cast==0 && recovery>=5 && aimedAdd!=null && addAim!=null) {
            adds.stream().filter(e->e.getUUID().equals(aimedAdd)).findFirst().ifPresent(prism->prism.skills().trace(server,addOrigin,addAim,true));aimedAdd=null;addAim=addOrigin=null;
        }
    }
    public static void cleanup(ServerLevel level,UUID run,Vec3 center) {
        if(run==null)return;
        for(var entity:level.getEntitiesOfClass(Entity.class,new AABB(center,center).inflate(100),e->ApexSkills.family(e) && !(e instanceof CrownedApex) && run.toString().equals(e.getPersistentData().getStringOr("CrownRun",""))))entity.discard();
    }
    @Override public void die(DamageSource source) {super.die(source);if(dead) {crownBar.removeAllPlayers();cancel();}}
    @Override public void addAdditionalSaveData(ValueOutput out) {super.addAdditionalSaveData(out);out.store("CrownArena",BlockPos.CODEC,arena);out.putInt("CrownRoster",roster);out.putInt("CrownPhase",phase());out.putBoolean("CrownPack",livingPack);out.putFloat("CrownLinks",linkBudget);}
    @Override public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);arena=in.read("CrownArena",BlockPos.CODEC).orElse(blockPosition().below());roster=Math.clamp(in.getIntOr("CrownRoster",1),1,4);
        entityData.set(PHASE,Math.clamp(in.getIntOr("CrownPhase",1),1,3));livingPack=in.getBooleanOr("CrownPack",false);linkBudget=Math.clamp(in.getFloatOr("CrownLinks",0),0,120);cancel();recovery=60;applyGenetics();
        crownBar.setColor(phase()==3 ? BossEvent.BossBarColor.RED : phase()==2 ? BossEvent.BossBarColor.GREEN : BossEvent.BossBarColor.PURPLE);
        crownBar.setName(Component.translatable("entity.bunnidogs.crowned_apex.phase_"+phase()));
    }
}
