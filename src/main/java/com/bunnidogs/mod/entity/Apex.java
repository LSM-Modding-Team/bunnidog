package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.ApexRules;
import com.bunnidogs.mod.item.CalipersItem;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A summoned combat copy of a line, using the ordinary genome renderer and saves. */
public final class Apex extends Bunnidog implements Enemy {
    private final ServerBossEvent boss = new ServerBossEvent(this.getUUID(), this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private ApexKind kind=ApexKind.BRUTE;
    private boolean flying;
    private boolean wild;
    private int retreat;
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> FUSE = net.minecraft.network.syncher.SynchedEntityData.defineId(Apex.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
    private final ApexSkills skills=new ApexSkills(this);
    private final ApexMobility mobility=new ApexMobility(this);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> LAST_STAND=net.minecraft.network.syncher.SynchedEntityData.defineId(Apex.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private boolean casting;
    private boolean enraged;
    private int abilityWait = 60;

    public Apex(EntityType<? extends Apex> type, Level level) {
        this(type,level,ApexKind.BRUTE,false);
    }
    public Apex(EntityType<? extends Apex> type,Level level,ApexKind kind,boolean flying) {
        super(type,level); this.kind=kind; this.flying=flying;
        // Prevent the ordinary first-tick biome founder roll even for a plain /summon.
        if (!level.isClientSide()) {
            this.setGenome(this.genome());
            this.setHealth(this.getMaxHealth());
            this.setPersistenceRequired();
        }
        // Default attribute values do not fire a Scale-change callback during construction.
        this.refreshDimensions();
        getNavigation().setCanFloat(true);
        setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,0);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Bunnidog.createAttributes().add(Attributes.ARMOR,ApexRules.BASE_ARMOR)
                .add(Attributes.KNOCKBACK_RESISTANCE,ApexRules.BASE_KNOCKBACK_RESISTANCE).add(Attributes.SCALE,ApexRules.SCALE).add(Attributes.FLYING_SPEED,.3).add(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED,3);
    }
    @Override
    protected void applyGenetics() {
        float oldMax = this.getMaxHealth();
        float fraction = oldMax > 0 ? this.getHealth() / oldMax : 1;
        var combat = ApexRules.combat(this.genome(), this.stats(), this.enraged);
        base(Attributes.MAX_HEALTH, combat.health()*(kind==ApexKind.BLAST ? 1.2 : 1));
        base(Attributes.MOVEMENT_SPEED, combat.speed()*(kind==ApexKind.BRUTE ? 1.1 : 1));
        base(Attributes.JUMP_STRENGTH, combat.jump());
        base(Attributes.ATTACK_DAMAGE, combat.damage()*(kind==ApexKind.BRUTE ? flying ? 2 : 1.3 : 1));
        base(Attributes.FOLLOW_RANGE, combat.awareness());
        this.applyAdaptations();
        this.setHealth(this.getMaxHealth() * fraction);
    }
    private void base(Holder<Attribute> attribute, double value) {
        var instance = this.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }
    public boolean wild() {return wild;}
    public void makeWild() {
        wild=true;boss.removeAllPlayers();
        var rng=new java.util.Random(getRandom().nextLong());
        var line=switch(kind) {case PRISM->com.bunnidogs.mod.genetics.Lineage.CAVE;case BLAST->com.bunnidogs.mod.genetics.Lineage.NETHER;case SUPPORT->com.bunnidogs.mod.genetics.Lineage.SWAMP;default->com.bunnidogs.mod.genetics.Lineage.MEADOW;};
        var genes=com.bunnidogs.mod.genetics.Genome.randomFounder(line,rng);
        if(flying)genes=com.bunnidogs.mod.genetics.Wings.winged(genes);
        setGenome(genes);setStats(com.bunnidogs.mod.genetics.Stats.of(25+rng.nextInt(31),25+rng.nextInt(31),25+rng.nextInt(31),25+rng.nextInt(31),10+rng.nextInt(21)));
        setAge(0);setHealth(getMaxHealth());
    }
    @Override public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,net.minecraft.world.DifficultyInstance difficulty,net.minecraft.world.entity.EntitySpawnReason reason,net.minecraft.world.entity.SpawnGroupData group) {
        var result=super.finalizeSpawn(level,difficulty,reason,group);
        if((reason==net.minecraft.world.entity.EntitySpawnReason.NATURAL || reason==net.minecraft.world.entity.EntitySpawnReason.CHUNK_GENERATION) && com.bunnidogs.mod.realm.GreatWarren.isRealm(level.getLevel()))makeWild();
        return result;
    }
    @Override public boolean isPersistenceRequired() {return !wild || hasCustomName();}
    @Override public boolean removeWhenFarAway(double distance) {return wild;}
    public ApexKind kind() { return kind; }
    public boolean flying() { return flying; }
    public ApexSkills skills() { return skills; }
    ApexMobility mobility() { return mobility; }
    public boolean trackingBeam() { return getHealth()<=getMaxHealth()*.25F; }
    public boolean lastStand() { return entityData.get(LAST_STAND); }
    void lastStand(boolean value) { entityData.set(LAST_STAND,value); }
    boolean inBlastRange(net.minecraft.world.entity.LivingEntity target) {return target!=null && distanceToSqr(target)<=(flying ? 144 : 25);}
    float blastCost() { return getMaxHealth()*.225F; }
    int fuseLength() { return 40+(flying ? 30 : 0)+(lastStand() ? 20 : 0); }
    boolean flightActive() { return flying && isAlive(); }
    int retreatTicks() { return retreat; }
    void retreatFor(int ticks) { retreat=ticks; }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder data) {
        super.defineSynchedData(data); data.define(FUSE,0);data.define(LAST_STAND,false);
    }
    void fuse(int ticks) { entityData.set(FUSE,Math.clamp(ticks*30/fuseLength(),0,30)); }
    public float swelling(float partial) { int fuse=entityData.get(FUSE); return fuse==0 ? 0 : Math.min(1,(fuse+partial)/30F); }
    boolean casting() { return casting; }
    public boolean enraged() { return this.enraged; }
    int abilityWait() { return this.abilityWait; }
    void beginAbility() { this.abilityWait = kind.recovery; }
    void charging(boolean value) {
        casting=value;
        this.boss.setName(value ? Component.translatable("entity.bunnidogs.apex.charging_kind",getType().getDescription()) : this.getDisplayName());
    }
    @Override
    public void tick() {
        if (flying) { setNoGravity(true); resetFallDistance(); }
        super.tick();
        if (this.level().isClientSide()) return;
        if (this.abilityWait > 0) this.abilityWait--;
        if (retreat>0) retreat--;
        setNoGravity(flying);
        skills.tick((ServerLevel)level());
        mobility.tick((ServerLevel)level());
        if (flying && flightActive() && tickCount%12==0) ((ServerLevel)level()).sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                getX(),getY(),getZ(),2,.4,.1,.4,.01);
        boolean angry = this.getHealth() <= this.getMaxHealth() * .5F;
        if (angry != this.enraged) {
            this.enraged = angry;
            this.applyGenetics();
        }
        this.boss.setColor(this.enraged ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);
        this.boss.setProgress(Math.clamp(this.getHealth() / this.getMaxHealth(), 0F, 1F));
    }
    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(1, new ApexAbilityGoal(this));
        this.goalSelector.addGoal(1,new ApexSpecialGoal(this));
        this.goalSelector.addGoal(2,new ApexFlightGoal(this));
        this.goalSelector.addGoal(2,new ApexPositionGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true) {
            @Override public boolean canUse() { return kind()==ApexKind.BRUTE && !flying() && super.canUse(); }
            @Override public boolean canContinueToUse() { return kind()==ApexKind.BRUTE && !flying() && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, .7));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Apex.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public boolean doHurtTarget(ServerLevel level,Entity target) {
        return kind==ApexKind.BRUTE && super.doHurtTarget(level,target);
    }
    @Override public boolean acceptsCare() { return false; }
    @Override public boolean canEat(ItemStack stack) { return false; }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override protected boolean canRide(Entity vehicle) { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override protected int getBaseExperienceReward(ServerLevel level) { return 500; }
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(player.getItemInHand(hand).getItem() instanceof CalipersItem)) return InteractionResult.PASS;
        if (!this.level().isClientSide()) player.sendSystemMessage(Component.translatable(
                "message.bunnidogs.apex.measure", Math.ceil(this.getHealth()), Math.ceil(this.getMaxHealth())));
        return InteractionResult.SUCCESS;
    }
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if(!wild)this.boss.addPlayer(player);
    }
    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.boss.removePlayer(player);
    }
    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("ApexWild",wild);
        output.putInt("ApexAbilityWait", this.abilityWait);
        output.putInt("ApexRetreat",retreat);
        output.putFloat("ApexVitality",skills.vitality());
    }
    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        wild=input.getBooleanOr("ApexWild",false);
        this.abilityWait = Math.clamp(input.getIntOr("ApexAbilityWait", 60), 0, kind.recovery);
        retreat=Math.clamp(input.getIntOr("ApexRetreat",0),0,100);
        fuse(0);lastStand(false);mobility.stopLeap();
        skills.restore(input.getFloatOr("ApexVitality",8));
        this.enraged = this.getHealth() <= this.getMaxHealth() * .5F;
        this.applyGenetics();
        this.setAge(0);
        this.setPersistenceRequired();
        this.boss.setName(this.getDisplayName());
    }
}
