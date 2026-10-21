package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.Random;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Breeding;
import com.bunnidogs.mod.genetics.Diet;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Adaptations;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Scouting;
import com.bunnidogs.mod.genetics.Fleece;
import com.bunnidogs.mod.genetics.Splicing;
import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Guarding;
import com.bunnidogs.mod.genetics.Social;
import com.bunnidogs.mod.genetics.Home;
import com.bunnidogs.mod.genetics.EarTag;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Growth;
import com.bunnidogs.mod.genetics.GuavaMemory;
import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Readout;
import com.bunnidogs.mod.genetics.RecoveryRest;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Temperament;
import com.bunnidogs.mod.genetics.Traits;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.item.EarTagItem;
import com.bunnidogs.mod.item.GuavaItem;
import com.bunnidogs.mod.item.PedigreeBookItem;
import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBiomeTags;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModDietTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The bunnidog. Phase 1: every bunnidog carries a {@link Genome} (6 genes x 2 alleles). Phase 2: it is a vanilla
 * {@link Animal}, so feeding two adults a guava puts them in love and the vanilla breed goal walks them together
 * (see {@link #spawnChildFromBreeding}).
 *
 * Phase 3: a bunnidog that nobody gave a genome to (natural spawn, plain {@code /summon}) becomes a wild founder of the
 * lineage of the biome it stands in, on its first server tick (see {@link #tick()}).
 *
 * Phase 4a: it also carries {@link Stats} (five bred stat points, server side only, saved as {@code Stats:"3,-1,0,2,12"}).
 * Pups inherit the parents' average plus a drift set by the parents' genes ({@link com.bunnidogs.mod.genetics.Drift}); the points multiply the
 * genetic attribute values ({@link #applyGenetics()}) and Bloom speeds up or slows down growing up ({@link #tick()}).
 *
 * <p>Phase 4c: a mating no longer makes pups. It drops ONE {@link Seed} item holding both parents' genes ({@link #spawnChildFromBreeding});
 * the litter is rolled when the seed hatches ({@link #hatch}: {@code /bunnidogs hatch} for now, the ripe seed crop in step 4d).
 *
 * <p>Phase 4e: a bunnidog fed a guava remembers the guava's tier while it is in love ({@link #mobInteract}); when two
 * of them mate, the seed's quality is the average of the two tiers' qualities ({@link GuavaTier#average}).
 *
 * <ul>
 *   <li>The genome is 56 bits, synced as one 24-bit low word and one 32-bit high word.</li>
 *   <li>Saved to NBT as its text form, e.g. {@code Genome:"00-01-20-00-03-11"}, so it is hand-editable and works in
 *       {@code /summon bunnidogs:bunnidog ~ ~ ~ {Genome:"22-00-00-00-00-00"}}.</li>
 *   <li>A new bunnidog rolls a random founder genome in its constructor (server side). NBT loading and future
 *       breeding overwrite it via {@link #setGenome(Genome)}.</li>
 *   <li>The expressed genes change attributes ({@link #applyGenetics()}) and the look (client renderer/model).</li>
 * </ul>
 */
public class Bunnidog extends Animal implements net.neoforged.neoforge.common.IShearable {
    private static final Identifier TRAIT_ARMOR = Identifier.fromNamespaceAndPath("bunnidogs","inherited_armor");
    private static final Identifier TRAIT_KNOCKBACK = Identifier.fromNamespaceAndPath("bunnidogs","inherited_knockback_resistance");
    private static final Identifier TRAIT_STEP = Identifier.fromNamespaceAndPath("bunnidogs","inherited_step_height");
    private static final Identifier TRAIT_TERRAIN = Identifier.fromNamespaceAndPath("bunnidogs","inherited_movement_efficiency");
    private static final Identifier TRAIT_SWIM = Identifier.fromNamespaceAndPath("bunnidogs","inherited_swim_speed");
    private static final Identifier TRAIT_OXYGEN = Identifier.fromNamespaceAndPath("bunnidogs","inherited_oxygen_bonus");
    private static final Identifier CARGO_LOAD = Identifier.fromNamespaceAndPath("bunnidogs","overloaded_bundle_packs");
    // Base stats of an "average" bunnidog; the genes scale these (see Traits multipliers).
    public static final double BASE_HEALTH = 8.0D;
    public static final double BASE_SPEED = 0.25D;
    public static final double BASE_JUMP = 0.42D;      // vanilla default jump strength
    public static final double BASE_FOLLOW_RANGE = 16.0D;

    private static final String TAG_GENOME = "Genome";
    private static final String TAG_STATS = "Stats";
    private static final String TAG_MOTHER = "Mother";
    private static final String TAG_FATHER = "Father";
    private static final String TAG_EAR_TAG = "EarTag";
    private static final String TAG_LOVE_TIER = "LoveTier";
    private static final String TAG_LOVE_TIER_TICKS = "LoveTierTicks";
    // H2: the claimed nest (docs/HOME_SPECS.md); written only while there is one
    private static final String TAG_NEST_X = "NestX";
    private static final String TAG_NEST_Y = "NestY";
    private static final String TAG_NEST_Z = "NestZ";

    // Preserve the existing split at bit 24. The high word now uses its sign bit too.
    private static final EntityDataAccessor<Integer> DATA_GENES_LO =
            SynchedEntityData.defineId(Bunnidog.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GENES_HI =
            SynchedEntityData.defineId(Bunnidog.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MIND =
            SynchedEntityData.defineId(Bunnidog.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FLEECE_READY =
            SynchedEntityData.defineId(Bunnidog.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BUNDLE_COUNT = SynchedEntityData.defineId(Bunnidog.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COMBAT_STATE = SynchedEntityData.defineId(Bunnidog.class,EntityDataSerializers.INT);

    // Cache of the decoded genome. NO initializers on purpose: fields with initializers would be reset after the
    // super constructor, which is harmless here but pointless.
    private Genome cachedGenome;
    private Traits cachedTraits;
    private com.bunnidogs.mod.genetics.MindProfile cachedMind;
    // True until this bunnidog got its real genome (NBT, breeding, command) or, failing that, a founder genome for the
    // biome it stands in. Not saved: entities loaded from disk always get a genome from NBT before their first tick.
    private boolean founderPending;
    // Bred stat points (Phase 4a). Server side only; null means all zero (see stats()). No initializer, like the fields above.
    private Stats stats;
    private int highestShowAward;
    private boolean workPaused;
    // Phase 5b: the genomes of the two parents (the seed's mother and father), set when a seed hatches. Null = not recorded
    // (wild founders, /summon). Saved as text like the genome. No initializer, like the fields above.
    private Genome motherGenome;
    private Genome fatherGenome;
    // Phase 5d: the registration code of the Ear Tag this bunnidog wears, or null for none. Saved as text. No initializer, like the fields above.
    private String earTag;
    // Fraction of an age tick carried over to the next tick by the Bloom growth speed-up (see growthTick()).
    private double growthCarry;
    // Phase 4e: the tier of the guava this bunnidog was fed to fall in love, for as long as the love lasts (see GuavaMemory).
    // Saved, like vanilla's own love timer. Nothing in the super constructor touches it, so an initializer is safe here.
    private final GuavaMemory guavaMemory = new GuavaMemory();
    /** H2: the Nest this dog has claimed (docs/HOME_SPECS.md); server side only. */
    private final NestHome nestHome = new NestHome(this);
    private final PhaseEscape phaseEscape = new PhaseEscape(this);
    private final BunnidogPacks packs = new BunnidogPacks(this);
    private final CaveGuiding caveGuiding = new CaveGuiding(this);
    private final TrailCompanion trailCompanion = new TrailCompanion(this);
    private final CompanionCombat companionCombat = new CompanionCombat(this);
    private final BunnidogLearning learning = new BunnidogLearning(this);
    private int biteWait;
    private boolean handCaring;
    private long nextForageTick;
    private long nextGardenTick;
    private long nextSampleTick;
    private long nextScoutTick;
    private long nextGroomTick;
    private long nextFleeceTick;
    private long nextSpliceTick;
    private LivingEntity huntingTarget;
    private boolean guardEnabled;
    private Mob guardingTarget;
    private Mob refugeThreat;
    private BlockPos calledBell;
    private long bellCallUntil, nextBellCallTick;

    public Bunnidog(EntityType<? extends Bunnidog> type, Level level) {
        super(type, level);
        if (!level.isClientSide()) {
            this.setGenome(Genome.randomFounder(new Random(this.getRandom().nextLong())));   // placeholder, see tick()
            this.setHealth(this.getMaxHealth());
            this.founderPending = true;     // AFTER setGenome, which clears it
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.JUMP_STRENGTH, BASE_JUMP)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE)
                .add(Attributes.ARMOR,0).add(Attributes.KNOCKBACK_RESISTANCE,0)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);      // only used by feral bunnidogs biting back
    }

    // ------------------------------------------------------------------ genes

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GENES_LO, 0);
        builder.define(DATA_GENES_HI, 0);
        builder.define(DATA_MIND, com.bunnidogs.mod.genetics.CognitiveGene.LEGACY_HELPER);
        builder.define(DATA_FLEECE_READY,true);
        builder.define(DATA_BUNDLE_COUNT,0);
        builder.define(DATA_COMBAT_STATE,0);
    }

    /** The decoded genome. Cheap: only re-decodes when the synced value changed. */
    public Genome genome() {
        long packed = ((this.entityData.get(DATA_GENES_HI) & 0xFFFFFFFFL) << 24)
                | (this.entityData.get(DATA_GENES_LO) & 0xFFFFFFL);
        Genome cached = this.cachedGenome;
        int mind = this.entityData.get(DATA_MIND);
        if (cached == null || cached.packed() != packed || cached.mindPacked() != mind) {
            cached = new Genome(packed, mind);
            this.cachedGenome = cached;
            this.cachedTraits = cached.traits();
            this.cachedMind = cached.mind();
        }
        return cached;
    }

    public com.bunnidogs.mod.genetics.MindProfile mind() { this.genome(); return this.cachedMind; }
    public BunnidogLearning learning() { return this.learning; }

    /** What the genome looks like / does after dominance. */
    public Traits traits() {
        this.genome();          // refreshes the cache if needed
        return this.cachedTraits;
    }

    public Temperament temperament() {
        return this.traits().temperament();
    }

    /** The bred stat points (all zero for a bunnidog nobody has bred). Only meaningful on the server. */
    /** The guava tier this bunnidog was fed to fall in love (0 when it is not in love or was fed a plain guava). */
    public int loveTier() {
        return this.guavaMemory.tier();
    }

    /** Ticks left of the guava-tier memory (0 = no memory; love mode itself lasts about as long). For the debug read-out. */
    public int loveTierTicks() {
        return this.guavaMemory.ticksLeft();
    }

    public Stats stats() {
        return this.stats == null ? Stats.ZERO : this.stats;
    }

    public int highestShowAward() { return this.highestShowAward; }

    /** Server-side, monotonic claim. Saved independently of later gene/stat edits. */
    public boolean recordShowAward(int tier) {
        if (tier <= this.highestShowAward || tier < 1 || tier > com.bunnidogs.mod.genetics.Show.Tier.GOLD.ordinal()) return false;
        this.highestShowAward = tier;
        return true;
    }

    /** Replaces the bred stat points (newborns, NBT, commands) and re-applies the attributes. Server side. */
    public void setStats(Stats stats) {
        this.stats = stats;
        if (!this.level().isClientSide()) {
            this.applyGenetics();
        }
    }

    /** Replaces the genome (use for newborns / commands). On the server this also updates the attributes. */
    public void setGenome(Genome genome) {
        this.founderPending = false;        // somebody decided this bunnidog's genes: no lineage roll needed
        this.entityData.set(DATA_GENES_LO, genome.syncLow());
        this.entityData.set(DATA_GENES_HI, genome.syncHigh());
        this.entityData.set(DATA_MIND, genome.mindPacked());
        if (!this.level().isClientSide()) {
            this.applyGenetics();
            if (Adaptations.poisonProof(genome)) this.removeEffect(MobEffects.POISON);
            if (Adaptations.levitationProof(genome)) this.removeEffect(MobEffects.LEVITATION);
        }
    }

    /**
     * Server side: turn the expressed genes AND the bred stat points into attribute base values
     * (base x gene multiplier x stat multiplier). Keeps the health FRACTION, so a fresh
     * bunnidog stays at full health when its genome (and so its max health) is replaced, and a hurt one stays hurt.
     */
    protected void applyGenetics() {
        Traits t = this.traits();
        float oldMax = this.getMaxHealth();
        float fraction = oldMax > 0.0F ? this.getHealth() / oldMax : 1.0F;
        Stats st = this.stats();
        setBase(Attributes.MAX_HEALTH, BASE_HEALTH * t.healthMultiplier() * st.multiplier(Stat.VITALITY));
        setBase(Attributes.MOVEMENT_SPEED, BASE_SPEED * t.speedMultiplier() * st.multiplier(Stat.SWIFTNESS));
        setBase(Attributes.JUMP_STRENGTH, BASE_JUMP * t.jumpMultiplier() * st.multiplier(Stat.LEAP));
        setBase(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE * t.hearingMultiplier() * st.multiplier(Stat.SENSES));
        if (this.acceptsCare()) setBase(Attributes.ATTACK_DAMAGE,com.bunnidogs.mod.genetics.Combat.profile(this.genome(),st).bite());
        this.applyAdaptations();
        this.setHealth(this.getMaxHealth() * fraction);
    }

    private void setBase(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    /** Transient named modifiers preserve other mods' base values/equipment and cannot accumulate across reloads. */
    protected final void applyAdaptations() {
        this.inheritedBonus(Attributes.ARMOR,TRAIT_ARMOR,Adaptations.armor(this.genome()));
        this.inheritedBonus(Attributes.KNOCKBACK_RESISTANCE,TRAIT_KNOCKBACK,Adaptations.knockbackResistance(this.genome()));
        this.inheritedBonus(Attributes.STEP_HEIGHT,TRAIT_STEP,Adaptations.stepBonus(this.genome()));
        this.inheritedBonus(Attributes.MOVEMENT_EFFICIENCY,TRAIT_TERRAIN,Adaptations.movementEfficiency(this.genome()));
        this.inheritedBonus(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED,TRAIT_SWIM,Adaptations.swimBonus(this.genome()));
        this.inheritedBonus(Attributes.OXYGEN_BONUS,TRAIT_OXYGEN,Adaptations.oxygenBonus(this.genome()));
        this.applyCargoLoad();
    }
    private void inheritedBonus(Holder<Attribute> attribute, Identifier id, double amount) {
        var value = this.getAttribute(attribute);
        if (value == null) return;
        if (amount == 0) value.removeModifier(id);
        else value.addOrUpdateTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));
    }

    // ------------------------------------------------------------------ wild lineages (Phase 3)

    /**
     * First server tick of a bunnidog nobody gave a genome to: roll a founder of the lineage of the biome it stands
     * in (tundra bunnidogs in snowy biomes, ...). Summoning with {@code {Genome:"..."}}, breeding and loading from
     * disk all set the genome earlier and skip this. Plain {@code /summon} therefore also follows the biome, which
     * is handy for testing lineages.
     */
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.founderPending) {
                Lineage lineage = ModBiomeTags.lineageAt(this.level(), this.blockPosition());
                this.setGenome(Genome.randomFounder(lineage, new Random(this.getRandom().nextLong())));
            }
            this.growthTick();
            this.guavaMemory.tick(this.getAge());      // Phase 4e: forgets the tier when love is over
            this.trailCompanion.tick();
            if (this.biteWait>0) this.biteWait--;
            this.companionCombat.tick();
            this.learning.tick();
            if (!this.entityData.get(DATA_FLEECE_READY) && this.level().getGameTime() >= this.nextFleeceTick) {
                this.entityData.set(DATA_FLEECE_READY,true);
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                if (this.acceptsCare()) this.nestHome.tick(serverLevel, !this.isBaby());
            }
        }
    }

    /**
     * Bloom (Phase 4a): vanilla ages a pup by one tick per tick (adult after 20 minutes). Bloom adds (or, when
     * negative, removes) extra age ticks on top: x2 at 20 points, never an adult in under 60 seconds, at worst
     * 0.4x speed. Age below zero = pup; the age of an adult is the breeding cooldown and is left alone.
     */
    private void growthTick() {
        int age = this.getAge();
        if (age >= 0 || this.isAgeLocked()) {
            this.growthCarry = 0.0D;
            return;
        }
        Growth.Step step = Growth.step(this.growthCarry, this.stats().growthMultiplier());
        this.growthCarry = step.carry();
        if (step.extraTicks() != 0) {
            this.setAge(Math.max(-Growth.BABY_TICKS, Math.min(0, age + step.extraTicks())));
        }
    }

    // ------------------------------------------------------------------ breeding (Phase 2)

    boolean readyForPen() {
        return this.isAlive() && this.acceptsCare() && !this.combat().recovering() && this.getAge() == 0 && !this.isInLove() && !this.guavaMemory.active()
                && this.getHealth() >= this.getMaxHealth() && !this.beingTransported() && !this.isVehicle()
                && this.getTarget() == null && !this.seekingRefuge() && !this.isInWater() && !this.isInLava() && !this.isOnFire();
    }
    /** Caller has reserved two actual guavas and rechecked the pair before any root-commit notifications. */
    public void beginPenCourtship(int tier) {
        beginPenCourtship(tier,null);
    }
    public void beginPenCourtship(int tier,Player keeper) {
        this.guavaMemory.remember(tier);
        this.setInLove(keeper);
    }
    /** Inventory-backed care buttons share the same meal/growth operation as deliberate hand care. */
    public boolean careFromStock(Player player,ItemStack actual) {
        if (this.combat().downed()) return this.combat().firstAid(player,actual);
        if (!(level() instanceof ServerLevel) || !player.mayBuild() || player.level()!=level() || distanceToSqr(player)>36
                || !acceptsCare() || !isAlive() || !actual.is(ModItems.GUAVA.get()) || !canEat(actual)
                || !this.handCareReady()
                || !Foraging.handFood(true,false,getHealth()<getMaxHealth(),canAgeUp())) return false;
        applyHandCare(player,actual); return true;
    }
    private void applyHandCare(Player player,ItemStack actual) {
        handCaring=true;
        try {
            actual.consume(1,player);
            this.heal(com.bunnidogs.mod.genetics.Combat.mealHealing(this.getMaxHealth()));
            if (this.canAgeUp()) this.ageUp(getSpeedUpSecondsWhenFeeding(-this.getAge()),true);
            this.playEatingSound();
        } finally { handCaring=false; }
    }
    public boolean handCareReady() {
        return !handCaring && !this.combat().receivingAid() && !this.combat().downed() && (this.isBaby()
                || !this.combat().danger() && this.getTarget()==null && !this.seekingRefuge());
    }
    public boolean readyForCareCourtship() {
        return readyForPen() && (getLastHurtByMob()==null || tickCount-getLastHurtByMobTimestamp()>=100);
    }
    public boolean courtshipFromStock(Player player,ItemStack actual) {
        if (!(level() instanceof ServerLevel) || !player.mayBuild() || player.level()!=level() || distanceToSqr(player)>36
                || !actual.is(ModItems.GUAVA.get()) || !isBunnidogFood(actual) || !readyForCareCourtship()) return false;
        actual.consume(1,player); beginPenCourtship(0,player); courtshipFeedback(player,0); return true;
    }
    public boolean groomFromStock(Player player,ItemStack brush) {
        if (!(level() instanceof ServerLevel server) || !(player instanceof ServerPlayer keeper) || !player.mayBuild()
                || player.level()!=level() || distanceToSqr(player)>36 || !brush.is(Items.BRUSH) || !BunnidogCare.canGroom(this)) return false;
        var rewards=BunnidogCare.groom(this);
        if (rewards.isEmpty()) return false;
        // Recovery and actual inventory-tool wear precede delivery callbacks. No equipped hand is substituted.
        brush.hurtAndBreak(1,server,keeper,item->this.playSound(SoundEvents.ITEM_BREAK.value(),1,1));
        for (var reward:rewards) if (!player.getInventory().add(reward)) player.drop(reward,false);
        return true;
    }
    private void courtshipFeedback(Player player,int tier) {
        this.guavaMemory.remember(tier);
        com.bunnidogs.mod.item.JourneyProgress.courtship(player);
        player.sendOverlayMessage(Component.translatable("care.bunnidogs.feedback.courtship"));
    }

    /**
     * Bunnidogs breed on guavas (Phase 4b; carrots no longer work). Which items count is the item tag
     * {@code bunnidogs:bunnidog_food}. Vanilla's love mode, breed goal, cooldown, baby ageing and food-feeds-pups do the rest.
     */
    public static boolean isBunnidogFood(ItemStack stack) {
        return stack.is(ModItems.BUNNIDOG_FOOD);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return isBunnidogFood(stack);
    }

    public Diet diet() { return Diet.of(this.genome()); }
    public boolean canEat(ItemStack stack) { return isBunnidogFood(stack) || ModDietTags.accepts(this.diet(), stack); }

    boolean homeNight() {
        return this.level() instanceof ServerLevel server && server.dimensionType().defaultClock()
                .map(clock -> Home.isNight(server.clockManager().getTotalTicks(clock))).orElse(false);
    }

    boolean canForage() {
        return this.foragingStatus() == Foraging.Status.READY;
    }
    public int forageWait() {
        return (int)Math.clamp(this.nextForageTick-this.level().getGameTime(),0L,(long)Foraging.MEAL_COOLDOWN);
    }
    /** Eligibility, not a food search. Shared with the Calipers resident snapshot. */
    public Foraging.Status foragingStatus() {
        return this.foragingStatus(false);
    }
    /** Night food is a short near-Nest exception; outdoor foraging keeps its daylight rule. */
    public Foraging.Status troughFeedingStatus() { return this.foragingStatus(true); }
    boolean canFeedFromTrough() {
        var status = this.troughFeedingStatus();
        return status == Foraging.Status.READY || status == Foraging.Status.NEST_MEAL;
    }
    private Foraging.Status foragingStatus(boolean trough) {
        if (!this.acceptsCare() || !this.isAlive() || !(this.level() instanceof ServerLevel)) return Foraging.Status.UNAVAILABLE;
        if (this.combat().downed() || !trough && this.combat().recovering()) return Foraging.Status.UNAVAILABLE;
        if (this.trail().ordered()) return Foraging.Status.TRAIL;
        if (this.seekingRefuge()) return Foraging.Status.DANGER;
        boolean night = this.homeNight();
        boolean nightMeal = trough && night && this.homeNest() != null
                && Foraging.insideNightHome(this.blockPosition().distSqr(this.homeNest()));
        if (trough && night && this.getLastHurtByMob() != null && this.tickCount-this.getLastHurtByMobTimestamp() < 200)
            return Foraging.Status.DANGER;
        var status = Foraging.status(!this.isBaby(),this.getHealth() < this.getMaxHealth(),this.homeNest() != null,
                night && !nightMeal,this.hasCombatTarget());
        if (status != Foraging.Status.READY) return status;
        if (this.beingTransported()) return Foraging.Status.TRANSPORTED;
        if (!this.foodWithinHome(this.blockPosition())) return Foraging.Status.OUTSIDE_HOME;
        return this.forageWait() > 0 ? Foraging.Status.RESTING : nightMeal ? Foraging.Status.NEST_MEAL : status;
    }

    boolean foodWithinHome(BlockPos pos) {
        BlockPos nest = this.homeNest();
        return nest != null && Foraging.insideHome(pos.distSqr(nest));
    }

    /** A cheap care snapshot; actual safe resting spots and routes are searched only by the idle goal. */
    public RecoveryRest.Status recoveryRestStatus() {
        if (!this.acceptsCare() || !this.isAlive() || !(this.level() instanceof ServerLevel)) return RecoveryRest.Status.UNAVAILABLE;
        if (this.isBaby()) return RecoveryRest.Status.ADULT;
        if (!RecoveryRest.needed(this.getHealth(),this.getMaxHealth())) return RecoveryRest.Status.HEALTH;
        if (this.homeNest() == null) return RecoveryRest.Status.HOME;
        if (this.beingTransported()) return RecoveryRest.Status.TRANSPORTED;
        if (this.getTarget() != null || this.seekingRefuge()
                || this.getLastHurtByMob() != null && this.tickCount-this.getLastHurtByMobTimestamp() < 200)
            return RecoveryRest.Status.DANGER;
        if (this.homeNight()) return RecoveryRest.Status.NIGHT;
        if (this.level().isThundering()) return RecoveryRest.Status.WEATHER;
        return this.foodWithinHome(this.blockPosition()) ? RecoveryRest.Status.READY
                : RecoveryRest.Status.OUTSIDE_HOME;
    }

    void ateForage(float healing) {
        this.nextForageTick = this.level().getGameTime() + Foraging.MEAL_COOLDOWN;
        this.heal(healing>0 ? Math.max(healing,com.bunnidogs.mod.genetics.Combat.mealHealing(this.getMaxHealth())) : 0);
        this.playEatingSound();
    }

    public int gardeningWait() {
        return (int)Math.clamp(this.nextGardenTick-this.level().getGameTime(),0L,
                (long)Gardening.REST_TICKS);
    }
    public boolean workPaused() { return this.workPaused; }
    public void setWorkPaused(boolean paused) { this.workPaused=paused; }
    /** The same cheap snapshot serves the AI and Calipers; it does not search for crops or paths. */
    public Gardening.Status gardeningStatus() {
        if (!this.acceptsCare() || !this.isAlive() || !(this.level() instanceof ServerLevel server)) return Gardening.Status.UNAVAILABLE;
        if (this.combat().recovering()) return Gardening.Status.UNAVAILABLE;
        if (this.trail().ordered()) return Gardening.Status.TRAIL;
        if (this.workPaused) return Gardening.Status.PAUSED;
        var status = Gardening.status(!this.isBaby(),this.getHealth() >= this.getMaxHealth(),
                this.homeNest() != null,this.homeNight(),this.beingTransported(),this.getTarget() != null || this.seekingRefuge()
                || this.getLastHurtByMob() != null && this.tickCount-this.getLastHurtByMobTimestamp() < 200,this.gardeningWait() == 0);
        if (status != Gardening.Status.READY) return status;
        if (!this.foodWithinHome(this.blockPosition())) return Gardening.Status.OUTSIDE_HOME;
        return server.getGameRules().get(GameRules.MOB_GRIEFING) ? status : Gardening.Status.GRIEFING_OFF;
    }
    void finishedGardening() {
        this.nextGardenTick = this.level().getGameTime()+Gardening.REST_TICKS;
    }

    @Override protected void playEatingSound() { this.playSound(SoundEvents.GENERIC_EAT.value(), 0.5F, 1.1F); }

    void huntTarget(LivingEntity target) { this.huntingTarget = target; }
    boolean hasCombatTarget() { return this.getTarget() != null && this.getTarget() != this.huntingTarget; }
    void guardTarget(Mob target) { this.guardingTarget = target; }
    public boolean guardEnabled() { return this.guardEnabled; }
    /** A cheap status snapshot; no intruder/path search and no claim or target changes. */
    public Guarding.Status guardingStatus() {
        if (!this.acceptsCare() || !this.isAlive() || !(this.level() instanceof ServerLevel)) return Guarding.Status.UNAVAILABLE;
        if (this.combat().recovering() || this.trail().ordered()) return Guarding.Status.UNAVAILABLE;
        if (!this.guardEnabled) return Guarding.Status.DISABLED;
        if (this.isBaby()) return Guarding.Status.ADULT;
        if (this.temperament() != Temperament.FERAL) return Guarding.Status.FERAL;
        if (this.homeNest() == null) return Guarding.Status.HOME;
        if (this.beingTransported()) return Guarding.Status.TRANSPORTED;
        if (!Sampling.healthy(this.getHealth(),this.getMaxHealth())) return Guarding.Status.HEALTH;
        if (this.getTarget() != null && this.getTarget() != this.guardingTarget) return Guarding.Status.COMBAT;
        if (!Guarding.within(this.blockPosition().distSqr(this.homeNest()))) return Guarding.Status.OUTSIDE_HOME;
        return this.guardingTarget != null && this.guardingTarget.isAlive() ? Guarding.Status.ACTIVE : Guarding.Status.READY;
    }
    void refugeFrom(Mob threat) { this.refugeThreat = threat; }
    public boolean seekingRefuge() { return this.refugeThreat != null && this.refugeThreat.isAlive(); }

    void hearBell(BlockPos bell) {
        long now = this.level().getGameTime();
        if (now < this.nextBellCallTick || !BellGatherGoal.willing(this)
                || bell.distSqr(this.homeNest()) > Social.BELL_RANGE*Social.BELL_RANGE) return;
        this.calledBell = bell.immutable(); this.bellCallUntil = now+Social.CALL_WINDOW;
        this.nextBellCallTick = now+Social.CALL_INTERVAL;
    }
    BlockPos calledBell() { return this.level().getGameTime() < this.bellCallUntil ? this.calledBell : null; }
    void forgetBellCall() { this.calledBell = null; }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim, DamageSource source) {
        boolean result = super.killedEntity(level, victim, source);
        if (victim == this.huntingTarget && this.canForage() && this.diet().seeksPrey()
                && HuntGoal.edible(this,victim) && this.foodWithinHome(victim.blockPosition())) {
            this.ateForage(Foraging.HUNT_HEAL);
        }
        return result;
    }

    /**
     * Phase 4e: feeding a guava to an adult that is ready puts it in love (vanilla), and here the tier of that guava is
     * remembered for the seed. The tier is read BEFORE {@code super}, because vanilla uses up the stack. It is only
     * stored when vanilla really accepts the food for love: an adult of age 0 (not a pup, whose feeding only grows it,
     * and not an adult in its cooldown) that is not in love yet (an animal in love refuses another guava, so its earlier
     * tier stays: the memory lasts exactly as long as the love) and an interaction that consumed the action.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.combat().downed() && held.is(ModItems.GUAVA.get())) {
            if (!this.level().isClientSide()) this.combat().firstAid(player,held);
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() instanceof net.minecraft.world.item.BundleItem) return this.packs.equip(player,held);
        if (held.isEmpty() && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty() && this.acceptsCare() && this.isAlive()) {
            if (player.isShiftKeyDown() && this.carriedBundleCount()>0) return this.packs.emptyHands(player);
            com.bunnidogs.mod.menu.CareMenu.open(player,this);
            return InteractionResult.SUCCESS;
        }
        if (held.is(net.minecraft.tags.ItemTags.HOES) && player.isShiftKeyDown()) {
            if (!player.mayBuild() || !this.acceptsCare() || !this.isAlive()) return InteractionResult.PASS;
            if (!this.level().isClientSide()) {
                this.workPaused = !this.workPaused;
                player.sendOverlayMessage(Component.translatable(this.workPaused ? "message.bunnidogs.work.paused" : "message.bunnidogs.work.resumed"));
            }
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.COMPASS)) return player.isShiftKeyDown() ? MineralScouting.use(this,player,hand)
                : ResidentWayfinding.markHome(this,player,hand);
        if (held.is(Items.GLOW_BERRIES) && player.isShiftKeyDown()) return this.caveGuiding.use(player,hand);
        if (held.is(Items.BOWL)) return this.trailCompanion.toggleProvisions(player,hand);
        if (held.is(Items.BONE) && player.isShiftKeyDown()) {
            if (!player.mayBuild()) return InteractionResult.PASS;
            if (!this.level().isClientSide()) {
                String message;
                if (!this.acceptsCare() || !this.isAlive()) message = "unavailable";
                else if (this.guardEnabled) { this.guardEnabled = false; message = "disabled"; }
                else if (this.isBaby() || this.temperament() != Temperament.FERAL || this.homeNest() == null) message = "requirement";
                else { this.guardEnabled = true; message = "enabled"; WorkingLineProgress.guard(player); }
                player.sendOverlayMessage(Component.translatable("message.bunnidogs.guard."+message));
            }
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() instanceof CalipersItem) {
            // Phase 5: the Calipers measure instead of feeding or riding; they never reach vanilla's feeding code.
            // Normal use opens the sheet; sneak-use reports a server-side care snapshot in chat.
            if (!this.level().isClientSide()) {
                if (player.isShiftKeyDown()) {
                    CalipersItem.showCare(player, this);
                } else if (player instanceof ServerPlayer serverPlayer) {
                    ModNetwork.sendSheet(serverPlayer, new Pedigree.Entry(this.genome(), this.motherGenome(), this.fatherGenome(), Optional.of(this.getUUID()), this.earTag(), Optional.of(this.stats()), this.isBaby()));
                } else {
                    CalipersItem.send(player, Readout.animal(this.genome(), this.stats(), this.isBaby(), this.motherGenome(), this.fatherGenome(), this.earTag()));
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() instanceof EarTagItem) {
            // Phase 5d: an Ear Tag is clipped on instead of feeding.
            if (!this.level().isClientSide()) {
                EarTagItem.apply(player, held, this);
            }
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() instanceof PedigreeBookItem) {
            // Phase 5c: the Pedigree Book logs this bunnidog instead of feeding it.
            if (!this.level().isClientSide()) {
                PedigreeBookItem.log(player, held, new Pedigree.Entry(this.genome(), this.motherGenome(), this.fatherGenome(), Optional.of(this.getUUID()), this.earTag(), Optional.of(this.stats()), this.isBaby()));
            }
            return InteractionResult.SUCCESS;
        }
        if (held.is(Items.BRUSH)) return BunnidogGrooming.use(this, player, held, hand);
        boolean guavaCare = player.isShiftKeyDown() && held.getItem() instanceof GuavaItem;
        if (guavaCare) {
            if (!this.acceptsCare() || !this.isAlive() || !this.canEat(held)) return InteractionResult.PASS;
            if (!held.is(ModItems.GUAVA.get())) {
                if (!this.level().isClientSide()) player.sendOverlayMessage(Component.translatable("message.bunnidogs.feeding.plain_care"));
                return InteractionResult.SUCCESS;
            }
        }
        if (guavaCare || !isBunnidogFood(held) && this.canEat(held)) {
            if (!this.handCareReady()) {
                if (!this.level().isClientSide()) player.sendOverlayMessage(Component.translatable("message.bunnidogs.combat.meal_rest"));
                return InteractionResult.SUCCESS;
            }
            if (!Foraging.handFood(true, false, this.getHealth() < this.getMaxHealth(), this.canAgeUp())) {
                if (!this.level().isClientSide()) player.sendOverlayMessage(Component.translatable("message.bunnidogs.feeding.no_care"));
                return InteractionResult.SUCCESS;
            }
            if (!this.level().isClientSide()) {
                // Reserve the real hand's food before native healing hooks can replace that hand.
                applyHandCare(player,held);
            }
            return InteractionResult.SUCCESS;
        }
        if (this.combat().recovering() && isBunnidogFood(held)) {
            if (!this.level().isClientSide() && held.is(ModItems.GUAVA.get())) this.careFromStock(player,held);
            return InteractionResult.SUCCESS;
        }
        boolean readyForLove = !this.level().isClientSide() && isBunnidogFood(held) && !this.isBaby() && this.getAge() == 0
                && !this.guavaMemory.active();
        if (isBunnidogFood(held) && !this.isBaby() && (this.getAge()>0 || this.isInLove())) {
            if (!this.level().isClientSide()) player.sendOverlayMessage(Component.translatable(this.isInLove()
                    ? "care.bunnidogs.feedback.courting" : "care.bunnidogs.feedback.rest",(this.getAge()+19)/20));
            return InteractionResult.SUCCESS;
        }
        int tier = GuavaItem.tierOf(held);
        InteractionResult result = super.mobInteract(player, hand);
        if (readyForLove && result.consumesAction()) {
            courtshipFeedback(player,tier);
        }
        return result;
    }

    /**
     * Vanilla calls this on the parent whose breed goal reaches the mating first: that parent is "the mother" (its
     * alleles go to slot 0 of the seed's genes, see {@link Genome}). Phase 4c: instead of a litter of pups the mating
     * drops ONE {@link Seed} holding both parents' genomes and stat points; nothing is rolled yet ({@link #hatch}
     * does that when the seed hatches). The quality is the average of the two parents' guava qualities (step 4e) plus their
     * starborn bonus. Afterwards each parent rests according to ITS OWN fertility, as before.
     *
     * <p>We do not call {@code super}: vanilla would ask {@link #getBreedOffspring} for a baby, post the
     * {@code BabyEntitySpawnEvent} (no baby exists now, so no event) and only then reset love, so we reset love and
     * set the cooldowns ourselves. The vanilla "animals bred" statistic and advancement are skipped for the same reason;
     * the hearts and a 1-7 XP orb are kept.
     */
    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal partner) {
        if (!(partner instanceof Bunnidog other)) {
            super.spawnChildFromBreeding(level, partner);       // cannot happen (mating needs the same class)
            return;
        }
        // Phase 4e: the guava part of the quality is the average of the tiers the two parents were fed.
        double guavaQuality = GuavaTier.average(this.guavaMemory.tier(), other.guavaMemory.tier());
        Seed seed = Seed.of(this.genome(), this.stats(), other.genome(), other.stats(), guavaQuality);
        var dropped=this.spawnAtLocation(level, SeedItem.create(seed), 0.5F);
        if (dropped!=null) {
            var first=this.getLoveCause(); var second=other.getLoveCause();
            if (com.bunnidogs.mod.item.JourneyProgress.award(first,"seed")) first.sendOverlayMessage(Component.translatable("care.bunnidogs.feedback.seed"));
            if (second!=first && com.bunnidogs.mod.item.JourneyProgress.award(second,"seed")) second.sendOverlayMessage(Component.translatable("care.bunnidogs.feedback.seed"));
        }
        BunnidogsMod.LOGGER.info("Seed dropped: {} (guava tiers {} and {})", seed, this.guavaMemory.tier(), other.guavaMemory.tier());
        level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.6D, this.getZ(), 7, 0.5D, 0.5D, 0.5D, 0.02D);
        level.addFreshEntity(new ExperienceOrb(level, this.position(), Vec3.ZERO, this.getRandom().nextInt(7) + 1));
        this.resetLove();
        other.resetLove();
        this.guavaMemory.forget();
        other.guavaMemory.forget();
        this.setAge(Breeding.cooldownTicks(this.traits().fertility()));
        other.setAge(Breeding.cooldownTicks(other.traits().fertility()));
    }

    /**
     * Nothing is born at the mating any more (see {@link #spawnChildFromBreeding}); pups come from seeds
     * ({@link #hatch}). Kept because {@link AgeableMob#getBreedOffspring} is abstract; null means "no offspring".
     */
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    /**
     * Rolls the litter of a seed ({@link Seed#hatch}) and adds the pups to the world at {@code pos}. Returns the pups;
     * empty means crowding or native spawn refusal, so the caller should keep the seed. The debug command and ripe
     * crop use this ordinary placement; Nursery Cradles additionally check and distribute their whole litter on a pad.
     * Fortune raises crop quality through {@code extraQuality}.
     *
     * <p>Every pup is a baby with its own genome and stat points; mutations are logged in {@code run/logs/latest.log}.
     */
    public static List<Bunnidog> hatch(ServerLevel level, Vec3 pos, Seed seed, double extraQuality, Random rng) {
        int nearby = level.getEntitiesOfClass(Bunnidog.class,
                new AABB(BlockPos.containing(pos)).inflate(Breeding.CROWD_RADIUS)).size();
        List<Seed.Pup> litter = seed.hatch(extraQuality, nearby, rng);
        List<Bunnidog> born = new ArrayList<>(litter.size());
        double quality = seed.effectiveQuality(extraQuality);
        if (!litter.isEmpty() && quality > 0.0D) {
            BunnidogsMod.LOGGER.info("Litter of {}: seed quality {}", litter.size(), String.format(Locale.ROOT, "%.2f", quality));
        }
        try {
            for (Seed.Pup roll : litter) {
                Bunnidog pup = createPup(level,seed,roll);
                if (pup == null) { born.forEach(Bunnidog::discard); return List.of(); }
                pup.snapTo(pos.x,pos.y,pos.z,rng.nextFloat()*360.0F,0.0F);
                born.add(pup);
            }
            for (var pup : born) if (!level.addFreshEntity(pup)) {
                born.forEach(Bunnidog::discard); return List.of();
            }
            return born;
        } catch (RuntimeException failure) {
            born.forEach(Bunnidog::discard);
            BunnidogsMod.LOGGER.warn("Crop/command hatch refused at {}",pos,failure);
            return List.of();
        }
    }
    /** Common native construction for crop/command and checked nursery hatching. */
    static Bunnidog createPup(ServerLevel level, Seed seed, Seed.Pup roll) {
        var pup = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.BREEDING);
        if (pup == null) return null;
        pup.setGenome(roll.genome()); pup.setStats(roll.stats()); pup.setParents(seed.mother(),seed.father()); pup.setBaby(true);
        if (roll.mutated()) BunnidogsMod.LOGGER.info("Mutation: a pup changed its {} gene (genome {})",roll.mutatedGene().displayName(),roll.genome());
        return pup;
    }

    // ------------------------------------------------------------------ parents (Phase 5b)

    /** The recorded mother's genome, if this bunnidog hatched from a seed. */
    public Optional<Genome> motherGenome() {
        return Optional.ofNullable(this.motherGenome);
    }

    /** The recorded father's genome, if this bunnidog hatched from a seed. */
    public Optional<Genome> fatherGenome() {
        return Optional.ofNullable(this.fatherGenome);
    }

    public void setParents(Genome mother, Genome father) {
        this.motherGenome = mother;
        this.fatherGenome = father;
    }

    // ------------------------------------------------------------------ ear tag (Phase 5d)

    /** The code of the Ear Tag this bunnidog wears, if any. */
    public Optional<String> earTag() {
        return Optional.ofNullable(this.earTag);
    }

    /**
     * Clips an Ear Tag on: the code comes from this bunnidog's UUID ({@link EarTag#code}). Returns the new code, or empty if it
     * already wears one (a tag is for good). The code is also shown as the animal's name, unless it already has a name of its own.
     */
    public Optional<String> applyEarTag() {
        if (this.earTag != null) {
            return Optional.empty();
        }
        this.earTag = EarTag.code(this.getUUID().getMostSignificantBits(), this.getUUID().getLeastSignificantBits());
        if (!this.hasCustomName()) {
            this.setCustomName(Component.literal(this.earTag));
        }
        return Optional.of(this.earTag);
    }

    /** H2, rule R7: a dead dog gives its nest ticket back, so the nest is free for the next dog. */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide() && this.combat().defeat(source)) return;
        super.die(source);
        if (this.isDeadOrDying() && this.level() instanceof ServerLevel serverLevel) {
            this.nestHome.release(serverLevel);
        }
    }

    /** The nest this dog has claimed, or null (H2). Read by {@link GoHomeGoal}. */
    public BlockPos homeNest() {
        return this.nestHome == null ? null : this.nestHome.nest();
    }

    /** Native leads and vehicle rides take precedence over voluntary home/food navigation. */
    public boolean beingTransported() {
        return this.getLeashData() != null || this.isPassenger();
    }

    /** Domestic routines and tools are inappropriate for the summoned Apex. */
    public boolean acceptsCare() { return true; }
    public TrailCompanion trail() { return this.trailCompanion; }
    public CompanionCombat combat() { return this.companionCombat; }
    public int combatState() { return this.entityData.get(DATA_COMBAT_STATE); }
    void syncCombatState(int state) { this.entityData.set(DATA_COMBAT_STATE,state); }
    @Override public boolean canBeSeenAsEnemy() { return this.combatState()!=3 && super.canBeSeenAsEnemy(); }
    @Override public boolean canBeHitByProjectile() { return this.combatState()!=3 && super.canBeHitByProjectile(); }
    @Override public boolean isPushable() { return this.combatState()!=3 && super.isPushable(); }
    @Override protected boolean considersEntityAsAlly(net.minecraft.world.entity.Entity other) {
        if (this.trailCompanion!=null && this.trail().keeper()!=null) {
            if (this.trail().keeper().equals(other.getUUID())) return true;
            if (other instanceof Bunnidog pet && this.trail().keeper().equals(pet.trail().keeper())) return true;
            var keeper=this.trail().keeperPlayer();
            if (keeper!=null && keeper.isAlliedTo(other)) return true;
        }
        return super.considersEntityAsAlly(other);
    }
    @Override public boolean doHurtTarget(ServerLevel level,net.minecraft.world.entity.Entity enemy) {
        if (this.acceptsCare()) {
            if (this.biteWait>0 || this.combat().recovering() || this.combat().bonded() && enemy instanceof Player) return false;
            this.biteWait=com.bunnidogs.mod.genetics.Combat.profile(this.genome(),this.stats()).interval();
        }
        return super.doHurtTarget(level,enemy);
    }
    ItemStack takeTrailProvision() { return this.packs.takeProvision(); }

    public int carriedBundleCount() { return this.entityData.get(DATA_BUNDLE_COUNT); }
    public int bundleCapacity() { return com.bunnidogs.mod.genetics.Carrying.capacity(this.genome()); }
    public List<ItemStack> carriedBundles() { return this.packs.snapshot(); }
    public void retrieveBundle(Player player) { this.packs.retrieve(player); }
    public com.bunnidogs.mod.genetics.Carrying.Status carryingStatus() { return this.packs.status(); }
    public int carryingSlowdown() { return com.bunnidogs.mod.genetics.Carrying.slowdown(this.carriedBundleCount(),this.bundleCapacity()); }
    void syncBundleCount(int count) { this.entityData.set(DATA_BUNDLE_COUNT,count); this.applyCargoLoad(); }
    private void applyCargoLoad() {
        if (this.level().isClientSide() || this.entityData == null) return;
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        int slowdown = this.carryingSlowdown();
        if (slowdown == 0) speed.removeModifier(CARGO_LOAD);
        else speed.addOrUpdateTransientModifier(new AttributeModifier(CARGO_LOAD,-slowdown/100D,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override protected void dropEquipment(ServerLevel level) {
        super.dropEquipment(level);
        this.packs.dropEquipment(level);
    }

    public com.bunnidogs.mod.genetics.Camouflage.Status camouflageStatus() {
        return CoatCamouflage.status(this);
    }

    @Override
    public double getVisibilityPercent(net.minecraft.world.entity.Entity observer) {
        double nativeVisibility = super.getVisibilityPercent(observer);
        return this.entityData != null && this.camouflageStatus() == com.bunnidogs.mod.genetics.Camouflage.Status.BLENDED
                ? nativeVisibility*com.bunnidogs.mod.genetics.Camouflage.VISIBILITY : nativeVisibility;
    }

    /** A carried animal leaves its old ticket available and chooses a new home after release. */
    public void releaseHomeForTravel(ServerLevel server) {
        if (server == this.level() && this.acceptsCare()) this.nestHome.release(server);
    }

    public NestHome.MoveResult moveHomeTo(ServerLevel level, BlockPos destination) {
        if (level != this.level() || !this.acceptsCare()) return NestHome.MoveResult.INVALID;
        return this.nestHome.moveTo(level, destination);
    }

    @Override
    public boolean fireImmune() {
        return super.fireImmune() || this.entityData != null && Adaptations.fireProof(this.genome());
    }
    @Override
    public boolean canFreeze() {
        return super.canFreeze() && (this.entityData == null || !Adaptations.coldProof(this.genome()));
    }
    /** Native snow collision and escape queries may vary per expressed animal, not just per entity type. */
    @Override
    public boolean is(TagKey<EntityType<?>> tag) {
        return super.is(tag) || tag.equals(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)
                && this.entityData != null && Adaptations.snowFooted(this.genome());
    }
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (!super.canBeAffected(effect)) return false;
        if (this.entityData == null) return true;
        return !(effect.is(MobEffects.POISON) && Adaptations.poisonProof(this.genome()))
                && !(effect.is(MobEffects.LEVITATION) && Adaptations.levitationProof(this.genome()));
    }
    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return super.isInvulnerableTo(level, source) || this.entityData != null
                && this.companionCombat!=null && this.combat().protects(source) || this.entityData != null
                && source.is(DamageTypes.CACTUS) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && Adaptations.cactusProof(this.genome());
    }
    @Override
    public boolean causeFallDamage(double distance, float modifier, DamageSource source) {
        float adaptation = this.entityData == null ? 1F : Adaptations.fallMultiplier(this.genome());
        return super.causeFallDamage(distance,modifier*adaptation,source);
    }

    public int samplingWait() {
        return (int)Math.clamp(this.nextSampleTick-this.level().getGameTime(),0L,
                (long)Sampling.RECOVERY_TICKS);
    }
    public int phaseWait() { return this.phaseEscape.waitTicks(); }
    public int caveSightWait() { return this.caveGuiding.waitTicks(); }
    public com.bunnidogs.mod.genetics.CaveSight.Status caveSightStatus(Player player) { return this.caveGuiding.status(player); }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        float before = this.getHealth()+this.getAbsorptionAmount();
        var origin=this.position();
        boolean hurt = super.hurtServer(level,source,amount);
        if (hurt && this.getHealth()+this.getAbsorptionAmount() < before) {
            this.combat().hurt();
            if (!this.combat().recovering() && source.getEntity() instanceof LivingEntity attacker) this.phaseEscape.afterAttack(level,attacker);
            if (!origin.equals(this.position()) && this.combat().selected() && this.trail().ordered()) this.combat().recall(this.trail().keeperPlayer());
        }
        return hurt;
    }
    public int groomingWait() {
        return (int)Math.clamp(this.nextGroomTick-this.level().getGameTime(),0L,(long)Grooming.RECOVERY_TICKS);
    }
    public void beginGroomingRecovery() { this.nextGroomTick = this.level().getGameTime()+Grooming.RECOVERY_TICKS; }
    public int scoutingWait() {
        return (int)Math.clamp(this.nextScoutTick-this.level().getGameTime(),0L,(long)Scouting.RECOVERY_TICKS);
    }
    public void beginScoutingRecovery() { this.nextScoutTick = this.level().getGameTime()+Scouting.RECOVERY_TICKS; }
    public Scouting.Status scoutingStatus() {
        if (!this.acceptsCare() || !this.isAlive() || !(this.level() instanceof ServerLevel)) return Scouting.Status.UNAVAILABLE;
        if (this.isBaby()) return Scouting.Status.ADULT;
        if (!Sampling.healthy(this.getHealth(),this.getMaxHealth())) return Scouting.Status.HEALTH;
        if (!Scouting.qualified(this.genome(),this.stats())) return Scouting.Status.TRAITS;
        if (this.getTarget() != null || this.seekingRefuge()) return Scouting.Status.COMBAT;
        return this.scoutingWait() > 0 ? Scouting.Status.RESTING : Scouting.Status.READY;
    }
    public void beginSamplingRecovery() {
        this.nextSampleTick = this.level().getGameTime()+Sampling.RECOVERY_TICKS;
    }
    public int splicingWait() {
        return (int)Math.clamp(this.nextSpliceTick-this.level().getGameTime(),0L,(long)Splicing.RECOVERY_TICKS);
    }
    public void beginSplicingRecovery() { this.nextSpliceTick = this.level().getGameTime()+Splicing.RECOVERY_TICKS; }

    public int fleeceWait() {
        return (int)Math.clamp(this.nextFleeceTick-this.level().getGameTime(),0L,
                (long)Fleece.REGROW_TICKS);
    }
    /** Exact prospective native products, shared by shearing and read-only Calipers. */
    public List<ItemStack> fleeceProducts() {
        return Fleece.yields(this.genome()).stream().map(product -> new ItemStack(
                net.minecraft.world.item.Items.WOOL.pick(net.minecraft.world.item.DyeColor.byName(product.color(),net.minecraft.world.item.DyeColor.WHITE)),
                product.count())).toList();
    }
    @Override
    public boolean isShearable(@org.jspecify.annotations.Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        return this.acceptsCare() && this.isAlive() && Fleece.ready(this.genome(),!this.isBaby(),
                this.getHealth(),this.getMaxHealth(),this.entityData.get(DATA_FLEECE_READY));
    }
    @Override
    public List<ItemStack> onSheared(@org.jspecify.annotations.Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        if (!this.isShearable(player,item,level,pos) || level.isClientSide()) return List.of();
        this.nextFleeceTick = level.getGameTime()+Fleece.REGROW_TICKS;
        this.entityData.set(DATA_FLEECE_READY,false);
        this.playSound(SoundEvents.SHEEP_SHEAR,.5F,1.2F);
        return this.fleeceProducts();
    }

    // ------------------------------------------------------------------ saving

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.packs.save(output);
        this.trailCompanion.save(output);
        this.companionCombat.save(output);
        this.learning.save(output);
        if (this.biteWait>0) output.putInt("CombatBiteWait",this.biteWait);
        output.putString(TAG_GENOME, this.genome().toString());
        output.putString(TAG_STATS, this.stats().toString());
        if (this.highestShowAward > 0) output.putInt("ShowAward", this.highestShowAward);
        if (this.guardEnabled) output.putBoolean("GuardNest",true);
        if (this.workPaused) output.putBoolean("WorkPaused",true);
        if (this.gardeningWait() > 0) output.putInt("GardenWait",this.gardeningWait());
        output.putInt("ForageWait",this.forageWait());
        if (this.samplingWait() > 0) output.putInt("SampleWait",this.samplingWait());
        if (this.scoutingWait() > 0) output.putInt("ScoutWait",this.scoutingWait());
        if (this.caveSightWait() > 0) output.putInt("CaveSightWait",this.caveSightWait());
        if (this.groomingWait() > 0) output.putInt("GroomWait",this.groomingWait());
        if (this.fleeceWait() > 0) output.putInt("FleeceWait",this.fleeceWait());
        if (this.splicingWait() > 0) output.putInt("SpliceWait",this.splicingWait());
        if (this.phaseWait() > 0) output.putInt("PhaseWait",this.phaseWait());
        if (this.motherGenome != null && this.fatherGenome != null) {
            output.putString(TAG_MOTHER, this.motherGenome.toString());
            output.putString(TAG_FATHER, this.fatherGenome.toString());
        }
        if (this.earTag != null) {
            output.putString(TAG_EAR_TAG, this.earTag);
        }
        if (this.guavaMemory.active()) {
            output.putInt(TAG_LOVE_TIER, this.guavaMemory.tier());
            output.putInt(TAG_LOVE_TIER_TICKS, this.guavaMemory.ticksLeft());
        }
        BlockPos nest = this.nestHome.nest();
        if (nest != null) {
            output.putInt(TAG_NEST_X, nest.getX());
            output.putInt(TAG_NEST_Y, nest.getY());
            output.putInt(TAG_NEST_Z, nest.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        // Genome FIRST: it sets max health etc., and super restores the saved Health value (which is clamped to
        // max health) afterwards. A missing or malformed tag keeps the genome rolled in the constructor.
        String savedGenome = input.getStringOr(TAG_GENOME, "");
        var decodedGenome = Genome.parse(savedGenome);
        decodedGenome.ifPresent(this::setGenome);
        // Stats after the genome (both change the attributes); a save from before Phase 4a has none = all zero.
        Stats.parse(input.getStringOr(TAG_STATS, "")).ifPresent(this::setStats);
        // Phase 5b: both parents or neither; a save from before it has none.
        Optional<Genome> mother = Genome.parse(input.getStringOr(TAG_MOTHER, ""));
        Optional<Genome> father = Genome.parse(input.getStringOr(TAG_FATHER, ""));
        if (mother.isPresent() && father.isPresent()) {
            this.setParents(mother.get(), father.get());
        }
        // Phase 5d: a save from before it, or an untagged bunnidog, has no tag. (The name shown above it is vanilla's own saved CustomName.)
        this.earTag = EarTag.parse(input.getStringOr(TAG_EAR_TAG, "")).orElse(null);
        super.readAdditionalSaveData(input);
        this.trailCompanion.restore(input);
        this.companionCombat.restore(input);
        this.biteWait=Math.clamp(input.getIntOr("CombatBiteWait",0),0,28);
        this.packs.load(input);
        this.learning.restore(input, decodedGenome.isPresent() && !Genome.isFullCode(savedGenome));
        if (!this.level().isClientSide()) this.applyAdaptations();
        this.highestShowAward = Math.max(0, input.getIntOr("ShowAward", 0));
        this.guardEnabled = input.getBooleanOr("GuardNest",false);
        this.workPaused = input.getBooleanOr("WorkPaused",false);
        this.guardingTarget = null;
        this.nextGardenTick = this.level().getGameTime()+Math.clamp(input.getIntOr("GardenWait",0),0,
                com.bunnidogs.mod.genetics.Gardening.REST_TICKS);
        this.nextGroomTick = this.level().getGameTime()+Math.clamp(input.getIntOr("GroomWait",0),0,Grooming.RECOVERY_TICKS);
        this.nextScoutTick = this.level().getGameTime()+Math.clamp(input.getIntOr("ScoutWait",0),0,Scouting.RECOVERY_TICKS);
        this.caveGuiding.restore(input.getIntOr("CaveSightWait",0));
        this.nextForageTick = this.level().getGameTime() + Math.clamp(input.getIntOr("ForageWait", 0), 0, Foraging.MEAL_COOLDOWN);
        this.nextSampleTick = this.level().getGameTime()+Math.clamp(input.getIntOr("SampleWait",0),0,
                Sampling.RECOVERY_TICKS);
        this.nextFleeceTick = this.level().getGameTime()+Math.clamp(input.getIntOr("FleeceWait",0),0,
                Fleece.REGROW_TICKS);
        this.entityData.set(DATA_FLEECE_READY,this.fleeceWait() == 0);
        this.nextSpliceTick = this.level().getGameTime()+Math.clamp(input.getIntOr("SpliceWait",0),0,Splicing.RECOVERY_TICKS);
        this.phaseEscape.restore(input.getIntOr("PhaseWait",0));
        // Phase 4e: a save from before it, or a bunnidog not in love, has neither tag = no memory.
        this.guavaMemory.restore(input.getIntOr(TAG_LOVE_TIER, 0), input.getIntOr(TAG_LOVE_TIER_TICKS, 0));
        // H2: a save from before it, or a dog without a nest, has no Y = no nest.
        this.nestHome.restore(input.getIntOr(TAG_NEST_X, 0), input.getIntOr(TAG_NEST_Y, NestHome.NO_NEST), input.getIntOr(TAG_NEST_Z, 0));
        this.refugeThreat = null;
        this.calledBell = null; this.bellCallUntil = 0; this.nextBellCallTick = 0;
    }

    // ------------------------------------------------------------------ behaviour

    // NOTE: registerGoals() runs INSIDE the super constructor, before the genome exists. So goals are registered for
    // every temperament and gate themselves in canUse() by reading the (later) genome.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new DownedGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this,this.level()));
        // Everyone flees when hurt, except feral ones, who fight back instead.
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D) {
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() != Temperament.FERAL && !Bunnidog.this.combat().managesMovement() && super.canUse();
            }
            @Override public boolean canContinueToUse() { return !Bunnidog.this.combat().managesMovement() && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(2, new RefugeGoal(this));
        this.goalSelector.addGoal(2, new GuardGoal(this));
        this.goalSelector.addGoal(2, new CompanionFightGoal(this));
        // Skittish ones keep their distance from players.
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Player>(this, Player.class, 8.0F, 1.0D, 1.6D) {
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() == Temperament.SKITTISH && !Bunnidog.this.combat().managesMovement()
                        && super.canUse() && !Bunnidog.this.trail().belongsTo(this.toAvoid)
                        && !Bunnidog.this.learning().recognizesInstructor(this.toAvoid);
            }
            @Override public boolean canContinueToUse() {
                return !Bunnidog.this.combat().managesMovement() && !Bunnidog.this.trail().belongsTo(this.toAvoid)
                        && !Bunnidog.this.learning().recognizesInstructor(this.toAvoid) && super.canContinueToUse();
            }
        });
        // Feral ones bite whoever hurt them.
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3D, true) {
            @Override protected int getAttackInterval() {
                return com.bunnidogs.mod.genetics.Combat.profile(Bunnidog.this.genome(),Bunnidog.this.stats()).interval();
            }
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() == Temperament.FERAL && !Bunnidog.this.combat().bonded()
                        && Bunnidog.this.getTarget() != Bunnidog.this.huntingTarget
                        && Bunnidog.this.getTarget() != Bunnidog.this.guardingTarget && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return Bunnidog.this.temperament() == Temperament.FERAL && !Bunnidog.this.combat().bonded()
                        && Bunnidog.this.getTarget() != Bunnidog.this.huntingTarget
                        && Bunnidog.this.getTarget() != Bunnidog.this.guardingTarget && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() == Temperament.FERAL && !Bunnidog.this.combat().bonded() && super.canUse();
            }
        });
        // Breeding (Phase 2): mate with another bunnidog in love, follow a guava, pups follow an adult.
        this.goalSelector.addGoal(4, new TrailGoal(this));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.1D, this::canEat, false) {
            @Override public boolean canUse() { return Bunnidog.this.learning().lesson() == null && super.canUse(); }
            @Override public boolean canContinueToUse() { return Bunnidog.this.learning().lesson() == null && super.canContinueToUse(); }
        });
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1D));
        // H2: a dog with a home (a claimed Nest, see NestHome and docs/HOME_SPECS.md) walks back once something (a guava, a scare, a
        // long stroll) led it more than 16 blocks away. Below panic, avoid and tempt, above the stroll. Idle for a dog without a nest.
        // H4: at night an adult with a home walks to it and stays within 2 blocks until dawn (docs/HOME_SPECS.md R8). Registered BEFORE
        // GoHomeGoal (same priority: the first one in the list wins a tie) and above the stroll, which it keeps from running all night.
        // A short healing meal near the Nest can interrupt night waiting; player control remains above it.
        this.goalSelector.addGoal(6, new TroughGoal(this));
        this.goalSelector.addGoal(7, new NightHomeGoal(this));
        this.goalSelector.addGoal(7, new GoHomeGoal(this));
        this.goalSelector.addGoal(7, new HuntGoal(this));
        this.goalSelector.addGoal(7, new GrazeGoal(this));
        this.goalSelector.addGoal(7, new LooseFoodGoal(this,false));
        this.goalSelector.addGoal(7, new GardenGoal(this));
        this.goalSelector.addGoal(7, new SupplyGoal(this));
        this.goalSelector.addGoal(7, new LooseFoodGoal(this,true));
        this.goalSelector.addGoal(7, new BellGatherGoal(this));
        this.goalSelector.addGoal(8, new RecoveryRestGoal(this));
        this.goalSelector.addGoal(8, new StormShelterGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }
}
