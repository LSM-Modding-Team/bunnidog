package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Combat;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Temperament;
import com.bunnidogs.mod.genetics.Trail;
import com.bunnidogs.mod.registry.ModItems;
import java.util.Comparator;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.pathfinder.Path;

/** Owns the cooperative target and voluntary withdrawal; native combat still applies each hit. */
public final class CompanionCombat {
    private final Bunnidog dog;
    private boolean enabled, downed, injured, regrouping, aiding;
    private int recovery, danger, quiet, pursuit;
    private Mob target, retreatFrom;
    private Mob focus;
    private BlockPos hold;
    private final CompanionWarnings warnings;
    private long nextSearch;
    CompanionCombat(Bunnidog dog) {this.tactics=new CombatTactics(dog); this.dog=dog; warnings=new CompanionWarnings(dog); }
    public boolean bonded() { return dog.acceptsCare() && dog.trail().keeper()!=null; }
    public boolean recovering() { return injured; }
    public boolean downed() { return downed; }
    public boolean receivingAid() { return aiding; }
    private final CombatTactics tactics;
    public CombatTactics tactics() {return tactics;}
    public boolean danger() { return danger>0 || target!=null; }
    public int recoveryLeft() { return injured ? Math.max(0,Combat.recoveryTicks(dog.stats())-recovery) : 0; }
    public boolean selected() {
        return bonded() && dog.level() instanceof ServerLevel level
                && CombatRoster.get(level).chosen(dog.trail().keeper(),dog.getUUID());
    }
    private long now() { return ((ServerLevel)dog.level()).getServer().overworld().getGameTime(); }
    public String intent() {
        return downed ? "downed" : injured ? "recovering" : regrouping ? "regrouping"
                : !selected() || !dog.trail().ordered() || !enabled && focus==null ? "safe"
                : dog.trail().keeperPlayer()==null ? "waiting"
                : target!=null ? "fighting" : warnings.alerting() ? "alert" : dog.trail().order()==Trail.Order.WAIT ? "holding" : "assist";
    }
    public boolean assisting() { return enabled; }
    public int partyCount() { return dog.level() instanceof ServerLevel level ? CombatRoster.get(level).count(dog.trail().keeper()) : 0; }
    public int partySlot() { return dog.level() instanceof ServerLevel level ? CombatRoster.get(level).slot(dog.trail().keeper(),dog.getUUID()) : -1; }
    public boolean commanded() {
        return dog.level() instanceof ServerLevel level && dog.getUUID().equals(CombatRoster.get(level).animal(dog.trail().keeper()));
    }
    public String partyState() {
        if (!(dog.level() instanceof ServerLevel level)) return "";
        var roster=CombatRoster.get(level); var keeper=dog.trail().keeper();
        return roster.members(keeper).toString()+roster.animal(keeper);
    }
    public boolean commandPartner(Player player) {
        if (!(dog.level() instanceof ServerLevel level) || !local(player) || !selected()) return false;
        CombatRoster.get(level).choose(player.getUUID(),dog.getUUID(),now()); say(player,"command_partner"); return true;
    }
    public boolean canStandDown(Player player) {
        return dog.level() instanceof ServerLevel level && local(player) && selected() && !danger()
                && !keeperBusy(player) && !CombatRoster.get(level).busy(player.getUUID(),now());
    }
    public boolean standDown(Player player) {
        if (!canStandDown(player) || !CombatRoster.get((ServerLevel)dog.level()).remove(player.getUUID(),dog.getUUID(),now())) return false;
        enabled=false; clearTarget(); regrouping=false; retreatFrom=null; hold=null; quiet=0;
        say(player,"stand_down"); return true;
    }
    void orderChanged(Trail.Order order) { hold=order==Trail.Order.WAIT && selected() ? dog.blockPosition().immutable() : null; }
    BlockPos hold() { return selected() && dog.trail().order()==Trail.Order.WAIT ? hold : null; }
    boolean focusedOn(LivingEntity enemy) { return focus==enemy; }
    private boolean local(Player player) {
        return player.mayBuild() && dog.isAlive() && dog.acceptsCare() && player.level()==dog.level()
                && dog.distanceToSqr(player)<=36 && dog.trail().belongsTo(player);
    }
    public boolean canSelect(Player player) {
        return local(player) && !dog.isBaby() && Trail.qualified(dog.genome()) && !injured && !dog.beingTransported()
                && !dog.isVehicle() && !dog.isInLove() && Sampling.healthy(dog.getHealth(),dog.getMaxHealth());
    }
    public boolean assist(Player player) {
        if (!(dog.level() instanceof ServerLevel level) || !canSelect(player)) return false;
        boolean joining=!selected();
        if (!selected() && CombatRoster.get(level).count(player.getUUID())>=CombatRoster.LIMIT) { say(player,"party_full"); return false; }
        if (!CombatRoster.get(level).chosen(player.getUUID(),dog.getUUID()) && keeperBusy(player)
                || !CombatRoster.get(level).choose(player.getUUID(),dog.getUUID(),now())) {
            say(player,"busy"); return false;
        }
        enabled=true; regrouping=false; quiet=0;
        if (dog.trail().order()!=Trail.Order.WAIT) dog.trail().combatFollow();
        else if (hold==null || joining) hold=dog.blockPosition().immutable();
        CombatRoster.get(level).saw(dog);
        say(player,"enabled"); return true;
    }
    public boolean staySafe(Player player) {
        if (!(dog.level() instanceof ServerLevel) || !local(player)) return false;
        enabled=false; regroup(); say(player,"safe"); return true;
    }
    /** Hearing is checked by the caller. Unlike ordinary orders, this never requires calm. */
    public boolean recall(Player player) {
        if (player==null || !player.mayBuild() || dog.level().isClientSide() || player.level()!=dog.level() || !dog.isAlive()
                || !dog.trail().belongsTo(player) || downed || dog.beingTransported() || dog.isVehicle()) return false;
        regroup(); dog.trail().combatFollow(); return true;
    }
    static boolean keeperBusy(Player player) {
        return player.getLastHurtByMob()!=null && player.tickCount-player.getLastHurtByMobTimestamp()<Combat.QUIET_TICKS
                || player.getLastHurtMob()!=null && player.tickCount-player.getLastHurtMobTimestamp()<Combat.QUIET_TICKS;
    }
    public boolean canRelease() {
        return !injured && !danger() && (!selected() || !regrouping && dog.level() instanceof ServerLevel level
                && !CombatRoster.get(level).busy(dog.trail().keeper(),now())
                && (dog.trail().keeperPlayer()==null || !keeperBusy(dog.trail().keeperPlayer())));
    }
    void released(Player player) {
        if (selected()) CombatRoster.get((ServerLevel)dog.level()).remove(player.getUUID(),dog.getUUID(),now());
        enabled=false; hold=null; regrouping=false; retreatFrom=null; clearTarget();
    }
    private void clearTarget() {
        if (target!=null && dog.getTarget()==target) dog.setTarget(null);
        target=null; focus=null; pursuit=0;
    }
    private void regroup() {
        if (target!=null) retreatFrom=target;
        clearTarget(); dog.setTarget(null); dog.refugeFrom(null);
        dog.getNavigation().stop(); regrouping=true; quiet=0;
        if (selected() && dog.trail().order()==Trail.Order.WAIT && dog.trail().keeperPlayer()!=null) dog.trail().combatFollow();
    }
    public boolean managesMovement() { return bonded() && (dog.trail().ordered() || injured); }
    boolean fighting() { return target!=null && eligible() && !regrouping; }
    Mob target() { return target; }
    private boolean eligible() {
        return selected() && (enabled || focus!=null) && dog.trail().ordered() && !dog.isBaby() && !injured && !dog.isInLove()
                && !dog.beingTransported() && !dog.isVehicle() && dog.trail().keeperPlayer()!=null;
    }
    private double leash() { return Math.max(5,Math.min(20,Trail.hearing(dog.genome(),dog.stats())-3)); }
    boolean legal(Mob mob) {
        var keeper=dog.trail().keeperPlayer();
        return keeper!=null && mob!=null && mob.isAlive() && mob.level()==dog.level() && mob instanceof Enemy
                && !(mob instanceof TamableAnimal) && !(mob instanceof Bunnidog) && !mob.hasCustomName()
                && !mob.isPassenger() && !mob.isVehicle() && mob.getLeashData()==null && !dog.isAlliedTo(mob)
                && mob.getType()!=EntityTypes.CREEPER && mob.getType()!=EntityTypes.WARDEN
                && mob.getType()!=EntityTypes.WITHER && mob.getType()!=EntityTypes.ENDER_DRAGON
                && mob.distanceToSqr(keeper)<=leash()*leash() && dog.distanceToSqr(keeper)<=leash()*leash()
                && (hold()==null || mob.blockPosition().distSqr(hold())<=16)
                && HomeRoutes.visible(dog,mob);
    }
    private Mob recent(LivingEntity entity,int age) { return age<100 && entity instanceof Mob mob && legal(mob) ? mob : null; }
    private Mob findTarget(ServerPlayer keeper) {
        var candidates=new LinkedHashMap<Mob,Integer>();
        Mob hit=recent(dog.getLastHurtByMob(),dog.tickCount-dog.getLastHurtByMobTimestamp());
        if (hit!=null) candidates.put(hit,0);
        if (dog.temperament()!=Temperament.SKITTISH) {
            hit=recent(keeper.getLastHurtMob(),keeper.tickCount-keeper.getLastHurtMobTimestamp());
            if (hit!=null) candidates.putIfAbsent(hit,1);
            if (dog.temperament()!=Temperament.FERAL) {
                hit=recent(keeper.getLastHurtByMob(),keeper.tickCount-keeper.getLastHurtByMobTimestamp());
                if (hit!=null) candidates.putIfAbsent(hit,2);
                for (var mob:dog.level().getEntitiesOfClass(Mob.class,dog.getBoundingBox().inflate(leash()),
                        enemy->legal(enemy) && (enemy.getTarget()==keeper || enemy.getTarget()==dog)
                                && (dog.temperament()!=Temperament.STOIC || enemy.distanceToSqr(keeper)<=36)))
                    candidates.putIfAbsent(mob,3);
            }
        }
        if(!tactics.coordinates())return candidates.keySet().stream().min(Comparator.comparingInt((Mob enemy)->candidates.get(enemy)).thenComparingDouble(dog::distanceToSqr)).orElse(null);
        var partners=dog.level().getEntitiesOfClass(Bunnidog.class,dog.getBoundingBox().inflate(leash()*2),
                other->other!=dog && other.combat().selected() && dog.trail().keeper().equals(other.trail().keeper()) && HomeRoutes.visible(dog,other)).stream().limit(2).toList();
        if(dog.temperament()!=Temperament.FERAL && dog.temperament()!=Temperament.SKITTISH)for(var partner:partners) {
            var threat=recent(partner.getLastHurtByMob(),partner.tickCount-partner.getLastHurtByMobTimestamp());
            if(threat!=null)candidates.putIfAbsent(threat,3);
        }
        return candidates.keySet().stream().filter(enemy->partners.stream().filter(other->other.getTarget()==enemy).count()<2)
                .min(Comparator.comparingInt((Mob enemy)->10*(int)partners.stream().filter(other->other.getTarget()==enemy).count()+candidates.get(enemy))
                        .thenComparingDouble(dog::distanceToSqr)).orElse(null);
    }
    void tick() {
        if (!(dog.level() instanceof ServerLevel level)) return;
        if (danger>0) danger--;
        warnings.tick();
        var keeper=dog.trail().keeperPlayer();
        if (selected() && keeper!=null && dog.tickCount%100==0) CombatRoster.get(level).saw(dog);
        if (selected() && dog.trail().order()==Trail.Order.WAIT && hold==null) hold=dog.blockPosition().immutable();
        if (downed) {
            clearTarget(); dog.getNavigation().stop(); dog.setDeltaMovement(0,dog.getDeltaMovement().y,0);
        } else if (injured && safeRest()) {
            if (recovery<Combat.recoveryTicks(dog.stats())) recovery++;
            if (recovery>=Combat.recoveryTicks(dog.stats()) && Sampling.healthy(dog.getHealth(),dog.getMaxHealth())) {
                injured=false; recovery=0;
            }
        }
        if (!eligible()) {
            clearTarget();
            if (selected() && keeper==null && dog.trail().ordered() && dog.trail().order()!=Trail.Order.WAIT) dog.trail().combatWait();
            if (regrouping && !injured && danger==0 && (keeper==null || !keeperBusy(keeper)) && ++quiet>=Combat.QUIET_TICKS) {
                regrouping=false; retreatFrom=null;
            }
        } else {
            if (dog.getHealth()/dog.getMaxHealth()<=Combat.profile(dog.genome(),dog.stats()).withdrawal()
                    || dog.isOnFire() || dog.isInWater() || dog.isInLava()) {
                if (!regrouping) regroup();
            }
            if (regrouping) {
                if (danger==0 && !keeperBusy(keeper) && dog.distanceToSqr(keeper)<=9
                        && dog.getHealth()/dog.getMaxHealth()>=Combat.profile(dog.genome(),dog.stats()).withdrawal()+.2
                        && safeRest()) quiet++; else quiet=0;
                if (quiet>=Combat.QUIET_TICKS) { regrouping=false; retreatFrom=null; }
            }
            if (!regrouping) {
                if (target!=null && (!legal(target) || ++pursuit>200)) { regroup(); }
                if (target==null && !regrouping && level.getGameTime()>=nextSearch) {
                    nextSearch=level.getGameTime()+20;
                    var candidate=findTarget(keeper);
                    if (candidate!=null) {
                        target=candidate; dog.setTarget(candidate); pursuit=0;
                        CombatRoster.get(level).engaged(dog.trail().keeper(),dog.getUUID(),now());
                    }
                }
            }
        }
        if (selected() && (danger>0 || target!=null || keeper!=null && keeperBusy(keeper)) && dog.tickCount%10==0)
            CombatRoster.get(level).engaged(dog.trail().keeper(),dog.getUUID(),now());
        dog.syncCombatState(downed ? 3 : injured ? 4 : regrouping ? 2 : target!=null ? 5 : warnings.alerting() ? 6 : eligible() ? 1 : 0);
    }
    void avoid(Mob threat) {
        if (!regrouping) regroup();
        retreatFrom=threat;
        // A visible fuse also frees an ordinary parked carrier to get out; it does not recruit it as a fighter.
        if (dog.trail().order()==Trail.Order.WAIT && dog.trail().keeperPlayer()!=null) dog.trail().combatFollow();
    }
    void skirmishDone() { if (dog.temperament()==Temperament.SKITTISH) regroup(); }
    public boolean focus(Player player,Mob enemy) {
        if (!(dog.level() instanceof ServerLevel level) || !player.mayBuild() || player.level()!=dog.level() || !dog.trail().belongsTo(player)) return false;
        if (!selected()) return refuse(player,"choose_first");
        if (injured) return refuse(player,"needs_recovery");
        if (!dog.trail().ordered()) return refuse(player,"follow_first");
        if (dog.beingTransported() || dog.isVehicle() || dog.isInWater() || dog.isInLava() || dog.isOnFire()) return refuse(player,"unsafe_footing");
        if (dog.distanceToSqr(player)>Math.pow(Trail.hearing(dog.genome(),dog.stats()),2)) return refuse(player,"not_heard");
        if (dog.getHealth()/dog.getMaxHealth()<Combat.profile(dog.genome(),dog.stats()).withdrawal()+.1) return refuse(player,"too_hurt");
        if (!legal(enemy) || enemy instanceof net.minecraft.world.entity.NeutralMob && enemy.getTarget()==null
                && player.getLastHurtMob()!=enemy) return refuse(player,"unsafe_target");
        focus=enemy; target=enemy; regrouping=false; retreatFrom=null; quiet=0; pursuit=0;
        dog.setTarget(enemy); dog.getNavigation().stop();
        CombatRoster.get(level).engaged(player.getUUID(),dog.getUUID(),now());
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.combat.focus",dog.getDisplayName(),enemy.getDisplayName()));
        return true;
    }
    private static boolean refuse(Player player,String reason) { say(player,reason); return false; }
    /** Server ray trace of the current view, bounded by hearing and already-loaded terrain. */
    public static boolean focusLook(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return false;
        var id=CombatRoster.get(level).animal(player.getUUID());
        if (id==null || !(level.getEntity(id) instanceof Bunnidog dog) || !dog.trail().belongsTo(player)) return false;
        double range=Trail.hearing(dog.genome(),dog.stats());
        var from=player.getEyePosition();
        while (range>1) {
            var to=from.add(player.getLookAngle().scale(range));
            var a=BlockPos.containing(from); var b=BlockPos.containing(to);
            if (level.hasChunksAt(new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ())),
                    new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ())))) break;
            range--;
        }
        var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnViewVector(player,net.minecraft.world.entity.Entity::isPickable,range);
        if (hit instanceof net.minecraft.world.phys.EntityHitResult entity && entity.getEntity() instanceof Mob enemy
                && enemy instanceof Enemy && !(enemy instanceof Bunnidog)) {
            dog.combat().focus(player,enemy); return true;
        }
        return false;
    }
    boolean safeRest() {
        return danger==0 && dog.isAlive() && dog.onGround() && !dog.beingTransported() && !dog.isVehicle()
                && !dog.isInWater() && !dog.isInLava() && !dog.isOnFire() && dog.getTarget()==null
                && !dog.seekingRefuge() && HomeRoutes.dryCell(dog,dog.blockPosition());
    }
    boolean rally(BlockPos pos) {
        return retreatFrom==null || !retreatFrom.isAlive() || pos.distSqr(retreatFrom.blockPosition())>=
                Math.min(16,dog.blockPosition().distSqr(retreatFrom.blockPosition())+1);
    }
    boolean rally(Path path) {
        if (!HomeRoutes.trailSafe(dog,path)) return false;
        if (retreatFrom==null || !retreatFrom.isAlive()) return true;
        double clearance=Math.min(4,dog.blockPosition().distSqr(retreatFrom.blockPosition()));
        for (int i=path.getNextNodeIndex();i<path.getNodeCount();i++)
            if (path.getNode(i).asBlockPos().distSqr(retreatFrom.blockPosition())<clearance) return false;
        return true;
    }
    private boolean terrain(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.DROWN) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.CRAMMING)
                || source.is(DamageTypes.OUTSIDE_BORDER);
    }
    boolean protects(DamageSource source) {
        return bonded() && !terrain(source) && (downed || source.getEntity() instanceof Player && !source.is(DamageTypeTags.IS_EXPLOSION));
    }
    boolean defeat(DamageSource source) {
        if (!bonded() || dog.isBaby() || terrain(source) || !(source.getEntity() instanceof LivingEntity
                || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.DRAGON_BREATH)
                || source.is(DamageTypes.ON_FIRE) && danger>0)) return false;
        regroup(); injured=true; downed=true; dog.setHealth(1); dog.resetLove();
        dog.clearFire(); dog.removeEffect(MobEffects.WITHER); dog.removeEffect(MobEffects.POISON);
        dog.syncCombatState(3); return true;
    }
    void hurt() {
        danger=100;
        if (selected() && dog.level() instanceof ServerLevel level)
            CombatRoster.get(level).engaged(dog.trail().keeper(),dog.getUUID(),now());
    }
    public boolean canFirstAid(Player player) {
        return downed && dog.isAlive() && dog.acceptsCare() && !aiding && player.mayBuild() && player.level()==dog.level() && dog.distanceToSqr(player)<=36
                && dog.onGround() && !dog.isInWater() && !dog.isInLava() && !dog.isOnFire()
                && HomeRoutes.dryCell(dog,dog.blockPosition());
    }
    public boolean firstAid(Player player,ItemStack guava) {
        if (dog.level().isClientSide() || !canFirstAid(player) || !guava.is(ModItems.GUAVA.get())) return false;
        aiding=true;
        try {
            guava.consume(1,player); downed=false; dog.syncCombatState(4);
            dog.ateForage(Combat.mealHealing(dog.getMaxHealth())); say(player,"first_aid"); return true;
        } finally { aiding=false; }
    }
    void save(ValueOutput out) {
        if (enabled) out.putBoolean("CombatAssist",true);
        if (injured) { out.putBoolean("CombatInjured",true); out.putInt("CombatRecovery",recovery); }
        if (downed) out.putBoolean("CombatDowned",true);
        if (danger>0) out.putInt("CombatDanger",danger);
        if (regrouping) out.putBoolean("CombatRegroup",true);
        if (hold!=null) out.store("CombatHold",BlockPos.CODEC,hold);
        warnings.save(out);
        tactics.save(out);
    }
    void restore(ValueInput in) {
        enabled=in.getBooleanOr("CombatAssist",false); downed=in.getBooleanOr("CombatDowned",false);
        injured=downed || in.getBooleanOr("CombatInjured",false);
        recovery=Math.clamp(in.getIntOr("CombatRecovery",0),0,Combat.RECOVERY_TICKS);
        danger=Math.clamp(in.getIntOr("CombatDanger",0),0,100);
        regrouping=injured || danger>0 || in.getBooleanOr("CombatRegroup",false);
        target=null; focus=null; retreatFrom=null; quiet=0; pursuit=0; aiding=false; nextSearch=0;
        hold=in.read("CombatHold",BlockPos.CODEC).orElse(null);
        warnings.restore(in);
        tactics.load(in,enabled);
        if (downed) { dog.setHealth(Math.max(1,dog.getHealth())); dog.syncCombatState(3); }
        else dog.syncCombatState(injured ? 4 : 0);
    }
    private static void say(Player player,String key) { player.sendOverlayMessage(Component.translatable("message.bunnidogs.combat."+key)); }
}
