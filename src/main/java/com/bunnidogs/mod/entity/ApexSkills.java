package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.network.ApexBeamPayload;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Authoritative attack effects. Geometry, recipients and resources are revalidated at resolution. */
public final class ApexSkills {
    private final Apex apex;
    private LivingEntity tether;
    private int tetherTicks;
    private float vitality=8;
    private int fireWait=50,anvilWait=70,broodWait=20,grabTicks;
    private ApexMinion grabbed;
    private LivingEntity queuedHook;
    private int hookDelay;
    private Vec3 queuedAim;
    private boolean lastMeal;
    ApexSkills(Apex apex) { this.apex=apex; }
    public float vitality() { return vitality; }
    void restore(float value) { vitality=Float.isFinite(value) ? Math.clamp(value,0,12) : 0; tether=null; }
    public static boolean family(Entity entity) { return entity instanceof Apex || entity instanceof ApexMinion; }
    private boolean victim(LivingEntity entity) {
        return entity.isAlive() && !family(entity) && !entity.isSpectator() && !(entity instanceof Player p && p.hasInfiniteMaterials())
                && (entity instanceof Player || entity==apex.getTarget() || entity instanceof Bunnidog dog && dog.trail().keeper()!=null);
    }
    public Vec3 mouth() { return apex.position().add(0,apex.getBbHeight()*.72,0).add(Vec3.directionFromRotation(apex.getXRot(),apex.getYHeadRot()).scale(apex.getBbWidth()*.45)); }
    private DamageSource source(String id) {
        return new DamageSource(apex.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,
                Identifier.fromNamespaceAndPath("bunnidogs",id))),apex);
    }
    public void trace(ServerLevel level,Vec3 direction,boolean damage) {
        trace(level,mouth(),direction,damage);
    }
    public void trace(ServerLevel level,Vec3 start,Vec3 direction,boolean damage) {
        Vec3 end=start.add(direction.normalize().scale(32));
        if (!level.hasChunksAt(BlockPos.containing(new Vec3(Math.min(start.x,end.x),Math.min(start.y,end.y),Math.min(start.z,end.z))),
                BlockPos.containing(new Vec3(Math.max(start.x,end.x),Math.max(start.y,end.y),Math.max(start.z,end.z))))) return;
        PacketDistributor.sendToPlayersTrackingEntity(apex,new ApexBeamPayload(start,end,damage ? 0 : 1));
        if (!damage) return;
        float amount=(float)(12.75*apex.getAttributeValue(Attributes.ATTACK_DAMAGE)/12.8);
        for (var entity:level.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(1.25),this::victim))
            if (entity.getBoundingBox().inflate(1.25).clip(start,end).isPresent()) entity.hurtServer(level,source("apex_beam"),amount);
    }
    public int summon(ServerLevel level) {
        if (!apex.isAlive() || apex.kind()!=ApexKind.BROOD) return 0;
        var alive=level.getEntitiesOfClass(ApexMinion.class,apex.getBoundingBox().inflate(40),Entity::isAlive);
        int room=Math.min(20-alive.size(),10-(int)alive.stream().filter(m->m.belongsTo(apex)).count()), spawned=0;
        for (int attempt=0;attempt<40 && spawned<Math.min(5,room);attempt++) {
            var at=apex.blockPosition().offset(apex.getRandom().nextInt(13)-6,0,apex.getRandom().nextInt(13)-6);
            if (!level.hasChunkAt(at)) continue;
            for (int y=apex.flying() ? 1 : 3;y>= (apex.flying() ? -1 : -5);y--) {
                var pos=at.offset(0,y,0);
                if ((!apex.flying() && !level.getBlockState(pos.below()).isSolidRender()) || !level.getFluidState(pos).isEmpty()) continue;
                var minion=ModEntities.APEX_MINION.get().create(level,EntitySpawnReason.MOB_SUMMONED);
                if (minion==null) break;
                minion.caller(apex); minion.snapTo(Vec3.atBottomCenterOf(pos));
                if (level.noCollision(minion) && !level.containsAnyLiquid(minion.getBoundingBox()) && level.addFreshEntity(minion) && minion.isAlive()) {
                    spawned++; level.sendParticles(ParticleTypes.POOF,minion.getX(),minion.getY()+.3,minion.getZ(),8,.3,.3,.3,.02);
                } else minion.discard();
                break;
            }
        }
        return spawned;
    }
    public boolean blast(ServerLevel level) { return blast(level,apex.getHealth()<=apex.blastCost()); }
    boolean blast(ServerLevel level,boolean lastStand) {
        if(apex.kind()!=ApexKind.BLAST || !apex.isAlive())return false;
        if(!explosion(level,apex,apex.position(),lastStand ? 20 : 10,false))return false;
        fireWait=75;
        if(apex.isAlive())apex.hurtServer(level,source("apex_backlash"),lastStand ? apex.getHealth()+apex.getAbsorptionAmount() : apex.blastCost());
        return true;
    }
    static boolean explosion(ServerLevel level,Apex caster,Vec3 center,int power,boolean allBunnidogs) {
        var pos=BlockPos.containing(center);int bound=power*2+2;
        if(!level.hasChunksAt(pos.offset(-bound,-bound,-bound),pos.offset(bound,bound,bound)))return false;
        var calculator=new ExplosionDamageCalculator() {
            private boolean immune(Entity entity) { return allBunnidogs ? entity instanceof Bunnidog : family(entity); }
            @Override public boolean shouldDamageEntity(Explosion explosion,Entity entity) { return !immune(entity); }
            @Override public float getKnockbackMultiplier(Entity entity) { return immune(entity) ? 0 : 1; }
            @Override public float getEntityDamageAmount(Explosion explosion,Entity entity,float exposure) {
                return (allBunnidogs ? .6F : .96F)*super.getEntityDamageAmount(explosion,entity,exposure);
            }
        };
        boolean grief=net.neoforged.neoforge.event.EventHooks.canEntityGrief(level,caster);
        var interaction=!grief ? Explosion.BlockInteraction.KEEP : level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_EXPLOSION_DROP_DECAY)
                ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
        var boom=new ServerExplosion(level,caster,null,calculator,center,power,grief,interaction);
        if(net.neoforged.neoforge.event.EventHooks.onExplosionStart(level,boom))return false;
        int blocks=boom.explode();
        for(var player:level.players())if(player.position().distanceToSqr(center)<16384)
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundExplodePacket(center,power,blocks,
                java.util.Optional.ofNullable(boom.getHitPlayers().get(player)),ParticleTypes.EXPLOSION_EMITTER,SoundEvents.GENERIC_EXPLODE,net.minecraft.util.random.WeightedList.of()));
        return true;
    }
    public void fireball(ServerLevel level,LivingEntity target) {
        var fire=ModEntities.APEX_FIREBALL.get().create(level,EntitySpawnReason.TRIGGERED);if(fire==null)return;
        fire.setOwner(apex);fire.setPos(mouth());fire.setDeltaMovement(target.getEyePosition().subtract(fire.position()).normalize().scale(1.1));
        if(!level.addFreshEntity(fire))fire.discard();else apex.playSound(SoundEvents.GHAST_SHOOT,1,.8F);
    }
    void queueHook(LivingEntity target) {queuedHook=target;hookDelay=18;queuedAim=null;}
    public void anvil(ServerLevel level,LivingEntity target) {
        var anvil=ModEntities.APEX_ANVIL.get().create(level,EntitySpawnReason.TRIGGERED);if(anvil==null)return;
        anvil.setOwner(apex);anvil.setPos(mouth());anvil.aimAt(target.getEyePosition());
        if(!level.addFreshEntity(anvil))anvil.discard();else apex.playSound(SoundEvents.ANVIL_PLACE,.8F,.7F);
    }
    public void hook(ServerLevel level,LivingEntity target) {
        hook(level,target.getEyePosition().subtract(mouth()).normalize());
    }
    public void hook(ServerLevel level,Vec3 direction) {
        var hook=ModEntities.APEX_HOOK.get().create(level,EntitySpawnReason.TRIGGERED);
        if (hook==null) return;
        hook.setOwner(apex); hook.setPos(mouth());
        hook.shoot(direction.x,direction.y,direction.z,4F,0);
        if (!level.addFreshEntity(hook)) hook.discard();
    }
    public boolean hookHit(LivingEntity target) {
        if (!(apex.level() instanceof ServerLevel level) || !apex.isAlive() || !victim(target) || apex.distanceToSqr(target)>400 || !clearHookPath(level,target)) return false;
        long now=level.getGameTime(), until=target.getPersistentData().getLongOr("BunnidogsHookUntil",0);
        if (until>now && until<=now+160) return false;
        if (target.isUsingItem() && target.getUseItem().is(Items.SHIELD) && target.getLookAngle().dot(apex.position().subtract(target.position()).normalize())>.1) return false;
        if (!target.addEffect(new MobEffectInstance(ModEffects.STAGGER,10,0,false,true,true))) return false;
        target.getPersistentData().putLong("BunnidogsHookUntil",now+160);
        if(target instanceof net.minecraft.server.level.ServerPlayer p)LifebinderMarks.get(level).mark(apex,p);
        tether=target; tetherTicks=0; return true;
    }
    public void tick(ServerLevel level) {
        if(!apex.isAlive())return;
        var target=apex.getTarget();
        if(!apex.isNoAi() && apex.kind()==ApexKind.BLAST && --fireWait<=0 && target!=null && target.isAlive() && (apex.abilityWait()>0 || !apex.inBlastRange(target)) && !apex.casting() && !apex.mobility().leaping() && apex.distanceToSqr(target)<=1024 && apex.hasLineOfSight(target)) {fireball(level,target);fireWait=75;}
        if(!apex.isNoAi() && apex.kind()==ApexKind.BROOD)brood(level);
        if(apex.kind()!=ApexKind.SUPPORT) {tether=null;return;}
        if(queuedHook!=null && hookDelay==10)queuedAim=queuedHook.getEyePosition().subtract(mouth()).normalize();
        if(queuedHook!=null && --hookDelay<=0) {
            if(victim(queuedHook) && queuedHook.level()==level && apex.distanceToSqr(queuedHook)<=625)hook(level,queuedAim==null ? queuedHook.getEyePosition().subtract(mouth()).normalize() : queuedAim);
            queuedHook=null;
        }
        if(!apex.isNoAi() && --anvilWait<=0 && target!=null && target.isAlive() && !apex.casting() && apex.distanceToSqr(target)<=625 && apex.hasLineOfSight(target)) {anvil(level,target);anvilWait=280;}

        if (tether!=null) {
            if (!victim(tether) || tether.level()!=level || apex.distanceToSqr(tether)>(tetherTicks<8 ? 400 : 324) || !clearHookPath(level,tether) || ++tetherTicks>40) tether=null;
            else {
                if (tetherTicks<=8 && apex.distanceToSqr(tether)>9) {
                    Vec3 pull=apex.position().subtract(tether.position()).normalize().scale(.9);
                    ApexTerrain.clear(level,apex,tether.position(),tether.position().add(pull.scale(2)),tether.getBbWidth()/2+.2,tether.getBbHeight());
                    tether.setDeltaMovement(tether.getDeltaMovement().scale(.5).add(pull)); tether.hurtMarked=true;
                }
                if (tetherTicks%5==0) PacketDistributor.sendToPlayersTrackingEntity(apex,new ApexBeamPayload(mouth(),tether.getEyePosition(),2));
                if (tetherTicks%10==0) drain(level,tether);
            }
        }
        if (apex.tickCount%10==0 && vitality>0) {
            var ally=level.getEntitiesOfClass(Bunnidog.class,apex.getBoundingBox().inflate(16),e->e!=apex && e.isAlive() && family(e)
                    && e.getHealth()<e.getMaxHealth() && apex.hasLineOfSight(e)).stream()
                    .min(java.util.Comparator.comparingInt((Bunnidog e)->e instanceof Apex ? 0 : 1).thenComparingDouble(e->e.getHealth()/e.getMaxHealth())).orElse(null);
            if (ally!=null) {
                float before=ally.getHealth(), amount=Math.min(3,vitality); ally.heal(amount);
                vitality-=Math.clamp(ally.getHealth()-before,0,amount);
                PacketDistributor.sendToPlayersTrackingEntity(apex,new ApexBeamPayload(mouth(),ally.getEyePosition(),3));
            }
        }
    }
    private void brood(ServerLevel level) {
        if(grabbed!=null) {
            if(!grabbed.isAlive() || !grabbed.belongsTo(apex) || ++grabTicks>90) {if(grabbed.isAlive())grabbed.release();grabbed=null;broodWait=40;return;}
            if(!grabbed.held()) {
                if(apex.distanceToSqr(grabbed)<=12.25) {grabbed.hold(apex);grabTicks=0;}return;
            }
            boolean critical=apex.getHealth()<=apex.getMaxHealth()*.25F;
            boolean eating=critical || apex.getHealth()<apex.getMaxHealth()*.5F && !lastMeal;
            if(grabTicks<(eating ? critical ? 2 : 5 : 15))return;
            if(eating) {
                lastMeal=consume(level,grabbed);
                broodWait=apex.getHealth()<=apex.getMaxHealth()*.25F ? 0 : 6;
            } else {
                if(apex.getTarget()!=null)grabbed.launchAt(apex.getTarget());else grabbed.release();lastMeal=false;broodWait=40;
            }
            grabbed=null;return;
        }
        if(apex.getHealth()<=apex.getMaxHealth()*.25F)broodWait=0;
        if(--broodWait>0 || apex.casting() || apex.getTarget()==null)return;
        grabbed=level.getEntitiesOfClass(ApexMinion.class,apex.getBoundingBox().inflate(24),m->m.isAlive() && m.belongsTo(apex) && !m.controlled())
                .stream().min(java.util.Comparator.comparingDouble(apex::distanceToSqr)).orElse(null);
        if(grabbed!=null) {grabbed.returnTo(apex);grabTicks=0;}else broodWait=20;
    }
    public boolean consume(ServerLevel level,ApexMinion minion) {
        if(!apex.isAlive() || !minion.isAlive() || !minion.belongsTo(apex) || apex.distanceToSqr(minion)>16 || apex.getHealth()>=apex.getMaxHealth()*.5F)return false;
        float before=apex.getHealth();apex.heal(minion.getHealth());
        if(apex.getHealth()<=before) {minion.release();return false;}
        level.sendParticles(ParticleTypes.HEART,apex.getX(),apex.getEyeY(),apex.getZ(),8,.4,.4,.4,0);
        apex.playSound(SoundEvents.GENERIC_EAT.value(),1,.7F);minion.discard();return true;
    }
    private boolean clearHookPath(ServerLevel level,LivingEntity target) {
        return ApexTerrain.clear(level,apex,mouth(),target.getEyePosition(),1.2,1.2);
    }
    public float drain(ServerLevel level,LivingEntity target) {
        if (!apex.isAlive() || !victim(target) || target.level()!=level || !apex.hasLineOfSight(target) || apex.distanceToSqr(target)>324) return 0;
        float before=target.getHealth()+target.getAbsorptionAmount(), amount=3F;
        boolean hit=target.hurtServer(level,source("apex_drain"),amount);
        float paid=hit ? Math.clamp(before-target.getHealth()-target.getAbsorptionAmount(),0,amount) : 0;
        // The explicit seven-times conversion applies once to actual accepted damage, then pays self/allies.
        float income=paid*7,ownBefore=apex.getHealth();if(income>0)apex.heal(income);
        float used=Math.clamp(apex.getHealth()-ownBefore,0,income);
        vitality=Math.min(12,vitality+income-used);return paid;
    }
}
