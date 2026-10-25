package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Trail;
import com.bunnidogs.mod.registry.ModItems;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Saved voluntary training. Existing care/transport permissions and Nest ownership remain independent. */
public final class TrailCompanion {
    private final Bunnidog dog;
    private UUID keeper;
    private Trail.Order order=Trail.Order.RESIDENT;
    private int missingTicks;
    private boolean training;
    private boolean provisions, eating;
    private long nextMealSearch;
    TrailCompanion(Bunnidog dog) { this.dog=dog; }
    public UUID keeper() { return keeper; }
    public Trail.Order order() { return order; }
    public boolean ordered() { return keeper!=null && order!=Trail.Order.RESIDENT; }
    public boolean provisions() { return provisions; }
    public boolean belongsTo(Player player) { return player.getUUID().equals(keeper); }
    private boolean calm() {
        return !dog.combat().danger() && !dog.combat().downed() && dog.getTarget()==null && !dog.seekingRefuge() && !dog.isInLove()
                && (dog.getLastHurtByMob()==null || dog.tickCount-dog.getLastHurtByMobTimestamp()>=100);
    }
    public ServerPlayer keeperPlayer() {
        if (!(dog.level() instanceof ServerLevel level) || keeper==null) return null;
        var player=level.getPlayerByUUID(keeper);
        return player instanceof ServerPlayer server && server.isAlive() && !server.isSpectator()
                && dog.distanceToSqr(server)<=Trail.MAX_FOLLOW_DISTANCE*Trail.MAX_FOLLOW_DISTANCE ? server : null;
    }
    boolean canWalk() {
        return ordered() && dog.acceptsCare() && dog.isAlive() && !dog.isBaby() && Trail.qualified(dog.genome())
                && !dog.combat().downed() && !dog.beingTransported() && !dog.isVehicle() && dog.getTarget()==null && !dog.seekingRefuge() && !dog.isInLove();
    }
    void tick() {
        if (!ordered()) return;
        if (!dog.acceptsCare() || dog.isBaby() || !Trail.qualified(dog.genome()) || dog.isInLove()) {
            setOrder(Trail.Order.RESIDENT); return;
        }
        if (order==Trail.Order.WAIT || keeperPlayer()!=null) missingTicks=0;
        else if (++missingTicks>=Trail.LOST_KEEPER_TICKS) setOrder(dog.combat().selected() ? Trail.Order.WAIT : Trail.Order.RESIDENT);
        if (ordered()) meal();
    }
    private void meal() {
        if (!provisions || eating || dog.combat().downed() || !dog.isAlive() || dog.getHealth()>=dog.getMaxHealth() || dog.forageWait()>0
                || dog.beingTransported() || dog.isVehicle() || !dog.onGround() || dog.isInWater() || dog.isInLava() || dog.isOnFire()
                || !calm() || !HomeRoutes.dryCell(dog,dog.blockPosition())) return;
        long now=dog.level().getGameTime();
        if (now<nextMealSearch) return;
        nextMealSearch=now+com.bunnidogs.mod.genetics.Foraging.SEARCH_INTERVAL;
        eating=true;
        try {
            if (!dog.takeTrailProvision().isEmpty()) dog.ateForage(com.bunnidogs.mod.genetics.Foraging.HEAL);
        } finally { eating=false; }
    }
    public InteractionResult toggleProvisions(Player player,InteractionHand hand) {
        if (!player.mayBuild() || !dog.acceptsCare() || !dog.isAlive() || dog.level()!=player.level() || dog.distanceToSqr(player)>36)
            return InteractionResult.PASS;
        if (dog.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!player.getItemInHand(hand).is(Items.BOWL)) return InteractionResult.PASS;
        if (!belongsTo(player)) { say(player,keeper==null ? "train_first" : "other_keeper"); return InteractionResult.SUCCESS; }
        provisions=!provisions; nextMealSearch=0; say(player,provisions ? "provisions_on" : "provisions_off");
        return InteractionResult.SUCCESS;
    }
    private void setOrder(Trail.Order next) {
        order=next; missingTicks=0;
        dog.combat().orderChanged(next);
        if (calm()) dog.getNavigation().stop();
    }
    void combatFollow() { setOrder(Trail.Order.FOLLOW); }
    void combatWait() { setOrder(Trail.Order.WAIT); }
    /** The trained keeper can give the same local orders through the care panel. */
    public boolean command(Player player,Trail.Order next) {
        if (dog.level().isClientSide() || !belongsTo(player) || readiness(player)!=Trail.Status.READY) return false;
        setOrder(next); return true;
    }
    public boolean setProvisions(Player player,boolean enabled) {
        if (dog.level().isClientSide() || !player.mayBuild() || !belongsTo(player) || !dog.acceptsCare() || !dog.isAlive()
                || dog.level()!=player.level() || dog.distanceToSqr(player)>36) return false;
        provisions=enabled; nextMealSearch=0; return true;
    }
    public boolean release(Player player) {
        if (dog.level().isClientSide() || !player.mayBuild() || !belongsTo(player) || training || !dog.isAlive()
                || !dog.acceptsCare() || player.level()!=dog.level() || dog.distanceToSqr(player)>36
                || !calm() || !dog.combat().canRelease()) return false;
        dog.combat().released(player);
        keeper=null; provisions=false; setOrder(Trail.Order.RESIDENT); say(player,"released"); return true;
    }
    private Trail.Status readiness(Player player) {
        return readiness(player,true);
    }
    public boolean canTrainFromStock(Player player) {
        if (keeper!=null || dog.level().isClientSide() || readiness(player)!=Trail.Status.READY) return false;
        for (int i=0;i<player.getInventory().getContainerSize();i++) if (player.getInventory().getItem(i).is(ModItems.TRAIL_WHISTLE.get())) return true;
        return false;
    }
    public boolean trainFromStock(Player player,ItemStack food) {
        return canTrainFromStock(player) && train(player,food);
    }
    private boolean train(Player player,ItemStack food) {
        if (keeper!=null || !food.is(ModItems.GUAVA.get()) || readiness(player)!=Trail.Status.READY) return false;
        training=true;
        try {
            food.consume(1,player);
            keeper=player.getUUID(); setOrder(Trail.Order.FOLLOW); dog.setPersistenceRequired(); say(player,"trained");
            if (player instanceof ServerPlayer server) {
                var advancement=server.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","trail_partner"));
                if (advancement!=null) server.getAdvancements().award(advancement,"trained");
            }
            return true;
        } finally { training=false; }
    }
    private Trail.Status readiness(Player player,boolean local) {
        if (!player.mayBuild() || !dog.acceptsCare() || !dog.isAlive() || dog.level()!=player.level()
                || local && dog.distanceToSqr(player)>36) return Trail.Status.UNAVAILABLE;
        if (training) return Trail.Status.BUSY;
        if(!dog.society().authorized(player) || !dog.deliveries().authorized(player))return Trail.Status.OTHER_KEEPER;
        if (keeper!=null && !belongsTo(player)) return Trail.Status.OTHER_KEEPER;
        if (dog.isBaby()) return Trail.Status.ADULT;
        if (!Trail.qualified(dog.genome())) return Trail.Status.TEMPERAMENT;
        if (!calm()) return Trail.Status.DANGER;
        if (keeper==null && !Sampling.healthy(dog.getHealth(),dog.getMaxHealth())) return Trail.Status.HEALTH;
        return Trail.Status.READY;
    }
    public InteractionResult use(Player player,InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (keeper==null) com.bunnidogs.mod.menu.CareMenu.open(player,dog);
            else com.bunnidogs.mod.menu.CareMenu.openCombat(player,dog);
            return InteractionResult.SUCCESS;
        }
        return call(player,hand);
    }
    private static void say(Player player,String key) { player.sendOverlayMessage(Component.translatable("message.bunnidogs.trail."+key)); }
    private static final java.util.Map<UUID,Long> LAST_SOUND=new java.util.WeakHashMap<>();
    public static InteractionResult call(Player player,InteractionHand hand) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!(player.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!player.getItemInHand(hand).is(ModItems.TRAIL_WHISTLE.get())) return InteractionResult.PASS;
        var dogs=level.getEntitiesOfClass(Bunnidog.class,player.getBoundingBox().inflate(Trail.MAX_HEARING),dog->dog.trail().belongsTo(player)
                && dog.acceptsCare() && dog.isAlive() && dog.distanceToSqr(player)<=Math.pow(Trail.hearing(dog.genome(),dog.stats()),2))
                .stream().sorted(Comparator.comparingInt((Bunnidog dog)->dog.combat().selected() ? 0 : 1)
                        .thenComparingDouble(dog->dog.distanceToSqr(player))).limit(Trail.MAX_CALLS).toList();
        if (player.isShiftKeyDown()) {
            if (CompanionCombat.focusLook(player)) return InteractionResult.SUCCESS;
            var near=dogs.stream().filter(d->d.distanceToSqr(player)<=36)
                    .min(Comparator.comparingInt(d->d.combat().selected() ? 0 : 1));
            if (near.isPresent()) com.bunnidogs.mod.menu.CareMenu.openCombat(player,near.get());
            else com.bunnidogs.mod.menu.CareMenu.openParty(player);
            return InteractionResult.SUCCESS;
        }
        int changed=0;
        for (var dog:dogs) {
            if (dog.trail().order()==Trail.Order.RESIDENT || dog.trail().order()==Trail.Order.WAIT && !dog.combat().selected()) continue;
            if (dog.combat().recall(player)) changed++;
        }
        long now=level.getGameTime();
        if (now>=LAST_SOUND.getOrDefault(player.getUUID(),now-Trail.CALL_COOLDOWN)+Trail.CALL_COOLDOWN) {
            LAST_SOUND.put(player.getUUID(),now);
            level.playSound(null,player.blockPosition(),SoundEvents.NOTE_BLOCK_FLUTE.value(),SoundSource.PLAYERS,2,1.5F);
        }
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.trail.called",changed));
        return InteractionResult.SUCCESS;
    }
    void save(ValueOutput output) {
        if (keeper!=null) {
            output.putString("TrailKeeper",keeper.toString()); output.putString("TrailOrder",order.name().toLowerCase(Locale.ROOT));
            if (provisions) output.putBoolean("TrailProvisions",true);
        }
    }
    void restore(ValueInput input) {
        keeper=null; order=Trail.Order.RESIDENT; missingTicks=0; training=false; provisions=false; nextMealSearch=0;
        try { keeper=UUID.fromString(input.getStringOr("TrailKeeper","")); } catch (IllegalArgumentException ignored) { }
        if (keeper!=null) {
            order=Trail.Order.parse(input.getStringOr("TrailOrder","resident")); provisions=input.getBooleanOr("TrailProvisions",false);
        }
    }
}
