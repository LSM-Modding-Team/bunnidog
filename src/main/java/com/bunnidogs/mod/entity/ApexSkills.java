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
        Vec3 start=mouth(), end=start.add(direction.normalize().scale(32));
        if (!level.hasChunksAt(BlockPos.containing(new Vec3(Math.min(start.x,end.x),Math.min(start.y,end.y),Math.min(start.z,end.z))),
                BlockPos.containing(new Vec3(Math.max(start.x,end.x),Math.max(start.y,end.y),Math.max(start.z,end.z))))) return;
        end=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,apex)).getLocation();
        PacketDistributor.sendToPlayersTrackingEntity(apex,new ApexBeamPayload(start,end,damage ? 0 : 1));
        if (!damage) return;
        float amount=(float)(10*apex.getAttributeValue(Attributes.ATTACK_DAMAGE)/12.8);
        for (var entity:level.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(1),this::victim))
            if (entity.getBoundingBox().inflate(.6).clip(start,end).isPresent()) entity.hurtServer(level,apex.damageSources().mobAttack(apex),amount);
    }
    public int summon(ServerLevel level) {
        if (!apex.isAlive() || apex.kind()!=ApexKind.BROOD) return 0;
        var alive=level.getEntitiesOfClass(ApexMinion.class,apex.getBoundingBox().inflate(40),Entity::isAlive);
        int room=Math.min(12-alive.size(),6-(int)alive.stream().filter(m->m.belongsTo(apex)).count()), spawned=0;
        for (int attempt=0;attempt<20 && spawned<Math.min(4,room);attempt++) {
            var at=apex.blockPosition().offset(apex.getRandom().nextInt(13)-6,0,apex.getRandom().nextInt(13)-6);
            if (!level.hasChunkAt(at)) continue;
            for (int y=3;y>=-5;y--) {
                var pos=at.offset(0,y,0);
                if (!level.getBlockState(pos.below()).isSolidRender() || !level.getFluidState(pos).isEmpty()) continue;
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
    public boolean blast(ServerLevel level) {
        var pos=apex.blockPosition();
        if (apex.kind()!=ApexKind.BLAST || !apex.isAlive() || apex.getHealth()<=apex.getMaxHealth()*.16F || !level.hasChunksAt(pos.offset(-22,-22,-22),pos.offset(22,22,22))) return false;
        var calculator=new ExplosionDamageCalculator() {
            @Override public boolean shouldDamageEntity(Explosion explosion,Entity entity) { return !family(entity); }
            @Override public float getKnockbackMultiplier(Entity entity) { return family(entity) ? 0 : 1; }
            @Override public float getEntityDamageAmount(Explosion explosion,Entity entity,float exposure) {
                return 1.6F*super.getEntityDamageAmount(explosion,entity,exposure);
            }
        };
        // KEEP preserves player builds. The later dimension owns its separate terrain policy.
        var boom=new ServerExplosion(level,apex,null,calculator,apex.position(),10,false,Explosion.BlockInteraction.KEEP);
        if (net.neoforged.neoforge.event.EventHooks.onExplosionStart(level,boom)) return false;
        int blocks=boom.explode();
        for (var player:level.players()) if (player.distanceToSqr(apex)<4096)
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundExplodePacket(apex.position(),10,blocks,
                    java.util.Optional.ofNullable(boom.getHitPlayers().get(player)),ParticleTypes.EXPLOSION_EMITTER,SoundEvents.GENERIC_EXPLODE,
                    net.minecraft.util.random.WeightedList.of()));
        if (apex.isAlive()) apex.hurtServer(level,source("apex_backlash"),Math.min(apex.getMaxHealth()*.15F,apex.getHealth()-1));
        return true;
    }
    public void hook(ServerLevel level,LivingEntity target) {
        var hook=ModEntities.APEX_HOOK.get().create(level,EntitySpawnReason.TRIGGERED);
        if (hook==null) return;
        hook.setOwner(apex); hook.setPos(mouth());
        Vec3 aim=target.getEyePosition().subtract(hook.position()); hook.shoot(aim.x,aim.y,aim.z,1.5F,0);
        if (!level.addFreshEntity(hook)) hook.discard();
    }
    public boolean hookHit(LivingEntity target) {
        if (!(apex.level() instanceof ServerLevel level) || !apex.isAlive() || !victim(target) || apex.distanceToSqr(target)>400 || !apex.hasLineOfSight(target)) return false;
        long now=level.getGameTime(), until=target.getPersistentData().getLongOr("BunnidogsHookUntil",0);
        if (until>now && until<=now+160) return false;
        if (target.isUsingItem() && target.getUseItem().is(Items.SHIELD) && target.getLookAngle().dot(apex.position().subtract(target.position()).normalize())>.1) return false;
        if (!target.addEffect(new MobEffectInstance(ModEffects.STAGGER,10,0,false,true,true))) return false;
        target.getPersistentData().putLong("BunnidogsHookUntil",now+160);
        tether=target; tetherTicks=0; return true;
    }
    public void tick(ServerLevel level) {
        if (apex.kind()!=ApexKind.SUPPORT || !apex.isAlive()) { tether=null; return; }
        if (tether!=null) {
            if (!victim(tether) || tether.level()!=level || apex.distanceToSqr(tether)>(tetherTicks<8 ? 400 : 324) || !apex.hasLineOfSight(tether) || ++tetherTicks>40) tether=null;
            else {
                if (tetherTicks<=8 && apex.distanceToSqr(tether)>9) {
                    Vec3 pull=apex.position().subtract(tether.position()).normalize().scale(.35);
                    tether.setDeltaMovement(tether.getDeltaMovement().scale(.5).add(pull)); tether.hurtMarked=true;
                }
                if (tetherTicks%5==0) PacketDistributor.sendToPlayersTrackingEntity(apex,new ApexBeamPayload(mouth(),tether.getEyePosition(),2));
                if (tetherTicks%10==0) drain(level,tether);
            }
        }
        if (apex.tickCount%10==0 && vitality>0) {
            var ally=level.getEntitiesOfClass(Bunnidog.class,apex.getBoundingBox().inflate(12),e->e.isAlive() && family(e)
                    && e.getHealth()<e.getMaxHealth() && apex.hasLineOfSight(e)).stream()
                    .min(java.util.Comparator.comparingDouble(e->e.getHealth()/e.getMaxHealth())).orElse(null);
            if (ally!=null) {
                float before=ally.getHealth(), amount=Math.min(1,vitality); ally.heal(amount);
                vitality-=Math.clamp(ally.getHealth()-before,0,amount);
                PacketDistributor.sendToPlayersTrackingEntity(apex,new ApexBeamPayload(mouth(),ally.getEyePosition(),3));
            }
        }
    }
    public float drain(ServerLevel level,LivingEntity target) {
        if (!apex.isAlive() || !victim(target) || target.level()!=level || !apex.hasLineOfSight(target) || apex.distanceToSqr(target)>324) return 0;
        float before=target.getHealth()+target.getAbsorptionAmount(), amount=2.4F;
        boolean hit=target.hurtServer(level,source("apex_drain"),amount);
        float paid=hit ? Math.clamp(before-target.getHealth()-target.getAbsorptionAmount(),0,amount) : 0;
        vitality=Math.min(12,vitality+paid); return paid;
    }
}
