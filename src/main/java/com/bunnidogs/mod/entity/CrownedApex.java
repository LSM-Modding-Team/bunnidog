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

/** One native health pool and a mobile, phase-aware attack director with a finite active escort pack. */
public final class CrownedApex extends Apex {
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(CrownedApex.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WARNING=SynchedEntityData.defineId(CrownedApex.class,EntityDataSerializers.INT);
    private final ServerBossEvent crownBar=new ServerBossEvent(getUUID(),Component.translatable("entity.bunnidogs.crowned_apex"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_6);
    private BlockPos arena=BlockPos.ZERO;
    private int roster=1,cast,elapsed,recovery=60,index,biteWait;
    private boolean livingPack,aerialPack,bardingEra,activePack=true;
    private float linkBudget=360;
    private Vec3 aim=Vec3.ZERO,origin=Vec3.ZERO,beamDirection=Vec3.ZERO,blastA=Vec3.ZERO,blastB=Vec3.ZERO;
    private static final int[][] ROTATION={{1,2,3,1,4},{2,1,2,1},{2,1,3,4,1,2,4}};
    public CrownedApex(EntityType<? extends CrownedApex> type,Level level) {super(type,level);}
    public static AttributeSupplier.Builder createAttributes() {return Apex.createAttributes().add(ModAttributes.CROWN_HEALTH,6000).add(Attributes.SCALE,4.6);}
    // Only this creature redirects native max-health lookups. Vanilla/global attribute bounds stay intact.
    private Holder<Attribute> nativeAttribute(Holder<Attribute> a) {return a.equals(Attributes.MAX_HEALTH) ? ModAttributes.CROWN_HEALTH : a;}
    @Override public AttributeInstance getAttribute(Holder<Attribute> a) {return super.getAttribute(nativeAttribute(a));}
    @Override public double getAttributeValue(Holder<Attribute> a) {return super.getAttributeValue(nativeAttribute(a));}
    @Override public double getAttributeBaseValue(Holder<Attribute> a) {return super.getAttributeBaseValue(nativeAttribute(a));}
    @Override protected void applyGenetics() {
        float fraction=getMaxHealth()>0 ? getHealth()/getMaxHealth() : 1;
        super.applyGenetics();
        getAttribute(ModAttributes.CROWN_HEALTH).setBaseValue((bardingEra ? 3000 : 6000)*(1+.55*(Math.clamp(roster,1,4)-1)));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(bardingEra ? phase()==3 ? 35 : 25 : phase()==3 ? 56 : 40);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(bardingEra ? .46/1.08 : .46);
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(72);
        getAttribute(Attributes.ARMOR).setBaseValue(bardingEra ? 4 : 18);
        setHealth(getMaxHealth()*fraction);
    }
    public void prepare(BlockPos center,int players) {bardingEra=true;com.bunnidogs.mod.equipment.Barding.astralite(this);setDropChance(EquipmentSlot.BODY,2);arena=center.immutable();roster=Math.clamp(players,1,4);applyGenetics();setHealth(getMaxHealth());}
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
    private boolean victim(LivingEntity e) {return e!=null && CrownEncounter.allowedTarget(this,e) && e.isAlive() && !ApexSkills.family(e) && !e.isSpectator() && !(e instanceof Player p && p.hasInfiniteMaterials());}
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
                setNoGravity(true);resetFallDistance();double angle=tickCount*.035;
                Vec3 hover=target.position().add(Math.cos(angle)*12,12,Math.sin(angle)*12);
                ApexFlightGoal.fly(this,hover,.9);
            } else {
                setNoGravity(false);
                if(isWithinMeleeAttackRange(target) && hasLineOfSight(target)) {
                    getNavigation().stop();if(biteWait==0) {doHurtTarget(server,target);biteWait=20;}
                } else getNavigation().moveTo(target,1.15);
            }
        }
        if(phase()==3 && !aerialPack) {aerialPack=true;aerialEscort(server);}
        if(phase()==2) {if(!livingPack) {livingPack=true;pack(server);}resumePack(server);links(server);}
        if(--recovery>0 || mobility().leaping())return;
        var rotation=ROTATION[phase()-1];
        int choice=rotation[Math.floorMod(index++,rotation.length)];
        // Bite nearby players during recovery; save the leap for an actual closing gap.
        if(choice==1 && isWithinMeleeAttackRange(target) && hasLineOfSight(target))choice=2;
        beginCast(choice,target);
    }
    private boolean inside(Vec3 p,int margin) {return Math.abs(p.x-arena.getX()-.5)<=40+margin && Math.abs(p.z-arena.getZ()-.5)<=40+margin && p.y>=arena.getY()-3 && p.y<=arena.getY()+33;}
    private void changePhase(ServerLevel server,int value) {
        for(var shot:server.getEntitiesOfClass(ApexFireball.class,getBoundingBox().inflate(90),e->e.getOwner()==this))shot.discard();
        cancel();cleanup(server,CrownEncounter.controller(this)==null ? null : CrownEncounter.controller(this).run(),Vec3.atCenterOf(arena));
        entityData.set(PHASE,value);index=0;crownBar.setColor(value==3 ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.GREEN);
        crownBar.setName(Component.translatable("entity.bunnidogs.crowned_apex.phase_"+value));recovery=70;applyGenetics();
        server.sendParticles(ParticleTypes.END_ROD,getX(),getEyeY(),getZ(),4,1,1,1,0);
        playSound(SoundEvents.WITHER_SPAWN,1,.65F);
    }
    public void beginCast(int choice,LivingEntity target) {
        if(choice!=1 && choice!=2 && choice!=3 && choice!=4 || !victim(target))return;
        cast=choice;elapsed=0;origin=skills().mouth();aim=target.getEyePosition();beamDirection=aim.subtract(origin).normalize();entityData.set(WARNING,choice);getNavigation().stop();setDeltaMovement(Vec3.ZERO);
        if(choice==4) {
            var middle=Vec3.atBottomCenterOf(arena).add(0,1,0);
            blastA=middle.add(Math.clamp(aim.x-middle.x,-23,23),0,Math.clamp(aim.z-middle.z,-23,23));
            var away=blastA.subtract(middle).multiply(1,0,1);if(away.lengthSqr()<16)away=new Vec3(index%2==0 ? 1 : -1,0,0);
            blastB=middle.subtract(away.normalize().scale(27));
        }
        playSound(choice==2 ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.RAVAGER_ROAR,.9F,choice==4 ? .6F : 1.2F);
    }
    public void cancel() {cast=0;elapsed=0;beamDirection=Vec3.ZERO;entityData.set(WARNING,0);mobility().stopLeap();setNoGravity(flying());}
    private void resolve(ServerLevel server,LivingEntity target) {
        elapsed++;getNavigation().stop();if(!mobility().leaping())setDeltaMovement(getDeltaMovement().multiply(.4,0,.4));
        if(cast==1) {
            // A readable orange landing marker precedes a real colliding, terrain-clearing arc.
            if(elapsed<28) {
                if(elapsed<=20)aim=target.getEyePosition();
                if(elapsed%4==0)ring(server,aim.add(0,-target.getEyeHeight(),0),4,ParticleTypes.FLAME);
            } else if(elapsed==28) {mobility().leap(target,aim.add(0,-target.getEyeHeight(),0),1.25);entityData.set(WARNING,0);}
            if(elapsed>28 && !mobility().leaping()) {
                var source=damageSources().mobAttack(this);float impact=(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*.65F;
                for(var enemy:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(4,1,4),this::victim))
                    if(distanceToSqr(enemy)<=16 && Math.abs(enemy.getY()-getY())<3 && hasLineOfSight(enemy) && enemy.hurtServer(server,source,impact))
                        enemy.knockback(.8,getX()-enemy.getX(),getZ()-enemy.getZ(),source,impact);
                finish(22);
            }
        } else if(cast==2) {
            origin=skills().mouth();
            beamDirection=turnBeam(beamDirection,target.getEyePosition().subtract(origin),elapsed<=24 ? .055 : .012+(phase()-1)*.003);
            var look=origin.add(beamDirection.scale(20));getLookControl().setLookAt(look.x,look.y,look.z,30,30);
            if(elapsed<=34) {if(elapsed%4==0)beam(server,origin,beamDirection,false);}
            else if(elapsed<=94) {
                boolean pulse=(elapsed-34)%10==0;
                if(pulse || elapsed%4==0)beam(server,origin,beamDirection,pulse);
            } else finish(28);
        } else if(cast==3) {
            // Three separate aimed volleys, each locked eight ticks before release.
            if(elapsed==16 || elapsed==32 || elapsed==48) {
                aim=target.getEyePosition().add(target.getDeltaMovement().scale(5));
                playSound(SoundEvents.BEACON_POWER_SELECT,.65F,1.4F);
            }
            if((elapsed<=24 || elapsed>=32 && elapsed<=40 || elapsed>=48 && elapsed<=56) && elapsed%4==0)
                ring(server,aim.add(0,-target.getEyeHeight(),0),2,ParticleTypes.SOUL_FIRE_FLAME);
            if(elapsed==24 || elapsed==40 || elapsed==56) {
                for(int side:new int[]{-1,1}) {
                    var shot=ModEntities.APEX_FIREBALL.get().create(server,EntitySpawnReason.TRIGGERED);if(shot==null)continue;
                    var mouth=skills().mouth();var direction=aim.subtract(mouth).normalize().yRot(side*.09F);
                    shot.setOwner(this);shot.setPos(mouth.add(direction.scale(1.4)));shot.setDeltaMovement(direction.scale(1.1));
                    if(!server.addFreshEntity(shot))shot.discard();
                }
                playSound(SoundEvents.GHAST_SHOOT,.85F,.65F);
            }
            if(elapsed>=64)finish(24);
        } else if(cast==4) {
            if(elapsed<=60 && elapsed%4==0) {ring(server,blastA,19,ParticleTypes.FLAME);ring(server,blastB,19,ParticleTypes.FLAME);}
            if(elapsed==64) {ApexSkills.explosion(server,this,blastA,9,false);ApexSkills.explosion(server,this,blastB,9,false);}
            if(elapsed>=68)finish(32);
        }
    }
    /** A hard angular bound, including an exact reversal, prevents instant aim snaps. */
    static Vec3 turnBeam(Vec3 current,Vec3 target,double radians) {
        var desired=target.normalize();if(desired.lengthSqr()<.001)return current;
        if(current.lengthSqr()<.001)return desired;
        var unit=current.normalize();double dot=Math.clamp(unit.dot(desired),-1,1),angle=Math.acos(dot);
        if(angle<=radians)return desired;
        var tangent=desired.subtract(unit.scale(dot));
        if(tangent.lengthSqr()<1e-10) {
            tangent=unit.cross(Math.abs(unit.y)<.9 ? new Vec3(0,1,0) : new Vec3(1,0,0));
        }
        return unit.scale(Math.cos(radians)).add(tangent.normalize().scale(Math.sin(radians))).normalize();
    }
    private void finish(int ticks) {cast=0;entityData.set(WARNING,0);recovery=ticks;}
    private void beam(ServerLevel level,Vec3 start,Vec3 direction,boolean damage) {
        if(direction.lengthSqr()<.001)return;
        var unit=direction.normalize();Vec3 end=start;
        for(int i=1;i<=64;i++) {var next=start.add(unit.scale(i));if(!inside(next,9))break;end=next;}
        var box=new AABB(start,end).inflate(1.25);if(!level.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))return;
        PacketDistributor.sendToPlayersTrackingEntity(this,new ApexBeamPayload(start,end,damage || elapsed>34 ? 0 : 1));
        if(!damage)return;
        var source=new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,Identifier.fromNamespaceAndPath("bunnidogs","apex_beam"))),this);
        for(var enemy:level.getEntitiesOfClass(LivingEntity.class,box,this::victim))if(enemy.getBoundingBox().inflate(1.25).clip(start,end).isPresent())enemy.hurtServer(level,source,(float)(bardingEra ? 20*com.bunnidogs.mod.equipment.Barding.damage(this) : 32));
    }
    private static void ring(ServerLevel server,Vec3 at,int radius,net.minecraft.core.particles.ParticleOptions particle) {
        for(int i=0;i<24;i++) {double a=i*Math.PI/12;server.sendParticles(particle,at.x+Math.cos(a)*radius,at.y+.25,at.z+Math.sin(a)*radius,1,0,.05,0,0);}
    }
    private void pack(ServerLevel server) {
        for(int i=0;i<7;i++) {
            Apex ally=i<4 ? ModEntities.apex(i==0 ? ApexKind.SUPPORT : i==3 ? ApexKind.BRUTE : ApexKind.PRISM,i==2).create(server,EntitySpawnReason.MOB_SUMMONED) : null;
            Bunnidog mob=ally!=null ? ally : ModEntities.APEX_MINION.get().create(server,EntitySpawnReason.MOB_SUMMONED);if(mob==null)continue;
            var seal=CrownEncounter.controller(this);if(seal==null) {mob.discard();return;}
            if(ally!=null)CrownBalance.train(ally,getUUID().getLeastSignificantBits()^i,roster,true);
            CrownEncounter.bind(mob,seal);
            if(mob instanceof ApexMinion pup)pup.caller(this);
            mob.setNoAi(false);mob.setTarget(getTarget());
            double angle=i*Math.PI*2/7;mob.snapTo(Vec3.atBottomCenterOf(arena).add(Math.cos(angle)*18,i==2 ? 8 : 1,Math.sin(angle)*18));
            var bounds=mob.getBoundingBox();
            if(!server.hasChunksAt(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ))) {mob.discard();continue;}
            if(!server.noCollision(mob) || !server.addFreshEntity(mob))mob.discard();
        }
    }
    private void aerialEscort(ServerLevel server) {
        var seal=CrownEncounter.controller(this);if(seal==null)return;
        for(int i=0;i<2;i++) {
            var mob=ModEntities.apex(ApexKind.PRISM,true).create(server,EntitySpawnReason.MOB_SUMMONED);if(mob==null)continue;
            CrownBalance.train(mob,getUUID().getMostSignificantBits()^i,roster,true);CrownEncounter.bind(mob,seal);mob.setTarget(getTarget());
            mob.snapTo(Vec3.atBottomCenterOf(arena).add(i==0 ? -18 : 18,12,0));
            var bounds=mob.getBoundingBox();
            if(!server.hasChunksAt(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ)) || !server.noCollision(mob) || !server.addFreshEntity(mob))mob.discard();
        }
    }
    private void links(ServerLevel server) {
        var seal=CrownEncounter.controller(this);if(seal==null)return;
        var adds=server.getEntitiesOfClass(Apex.class,getBoundingBox().inflate(60),e->e!=this && e.isAlive() && CrownEncounter.controller(e)==seal);
        if(tickCount%20==0 && linkBudget>0) {
            var support=adds.stream().filter(e->e.kind()==ApexKind.SUPPORT).findFirst().orElse(null);
            if(support!=null) {
                float before=getHealth(),paid=Math.min(12,linkBudget);heal(paid);linkBudget-=Math.clamp(getHealth()-before,0,paid);
                PacketDistributor.sendToPlayersTrackingEntity(this,new ApexBeamPayload(support.skills().mouth(),getEyePosition(),3));
            }
        }
    }
    /** Old middle-phase saves contain deliberately disabled support/Prism AI. Repair that pack once. */
    private void resumePack(ServerLevel server) {
        if(activePack)return;
        var seal=CrownEncounter.controller(this);if(seal==null)return;
        boolean waiting=false;
        for(var ally:server.getEntitiesOfClass(Apex.class,getBoundingBox().inflate(60),e->e!=this && e.isAlive() && CrownEncounter.controller(e)==seal)) {
            if(ally.kind()!=ApexKind.SUPPORT && ally.kind()!=ApexKind.PRISM)continue;
            if(ally.hasEffect(ModEffects.RESONANT_STUN)) {waiting=true;continue;}
            ally.setNoAi(false);if(ally.getTarget()==null)ally.setTarget(getTarget());
        }
        if(!waiting)activePack=true;
    }
    public static void cleanup(ServerLevel level,UUID run,Vec3 center) {
        if(run==null)return;
        for(var entity:level.getEntitiesOfClass(Entity.class,new AABB(center,center).inflate(100),e->ApexSkills.family(e) && !(e instanceof CrownedApex) && run.toString().equals(e.getPersistentData().getStringOr("CrownRun","")))) {
            if(entity instanceof Apex support && support.kind()==ApexKind.SUPPORT)LifebinderMarks.get(level).dismissed(support);
            entity.discard();
        }
    }
    @Override public void die(DamageSource source) {super.die(source);if(dead) {crownBar.removeAllPlayers();cancel();}}
    @Override public void addAdditionalSaveData(ValueOutput out) {super.addAdditionalSaveData(out);out.store("CrownArena",BlockPos.CODEC,arena);out.putInt("CrownRoster",roster);out.putInt("CrownAttackIndex",index);out.putInt("CrownPhase",phase());out.putBoolean("CrownBardingEra",bardingEra);out.putBoolean("CrownPack",livingPack);out.putBoolean("CrownAerialPack",aerialPack);out.putBoolean("CrownActivePack",activePack);out.putFloat("CrownLinks",linkBudget);}
    @Override public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);bardingEra=in.getBooleanOr("CrownBardingEra",false);arena=in.read("CrownArena",BlockPos.CODEC).orElse(blockPosition().below());roster=Math.clamp(in.getIntOr("CrownRoster",1),1,4);
        entityData.set(PHASE,Math.clamp(in.getIntOr("CrownPhase",1),1,3));index=Math.max(0,in.getIntOr("CrownAttackIndex",0));livingPack=in.getBooleanOr("CrownPack",false);aerialPack=in.getBooleanOr("CrownAerialPack",false);activePack=in.getBooleanOr("CrownActivePack",false);linkBudget=Math.clamp(in.getFloatOr("CrownLinks",0),0,360);cancel();recovery=60;applyGenetics();
        crownBar.setColor(phase()==3 ? BossEvent.BossBarColor.RED : phase()==2 ? BossEvent.BossBarColor.GREEN : BossEvent.BossBarColor.PURPLE);
        crownBar.setName(Component.translatable("entity.bunnidogs.crowned_apex.phase_"+phase()));
    }
}
