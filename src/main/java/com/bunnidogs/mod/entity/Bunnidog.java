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
import com.bunnidogs.mod.genetics.Home;
import com.bunnidogs.mod.genetics.EarTag;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Growth;
import com.bunnidogs.mod.genetics.GuavaMemory;
import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Readout;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
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
public class Bunnidog extends Animal {
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

    // Cache of the decoded genome. NO initializers on purpose: fields with initializers would be reset after the
    // super constructor, which is harmless here but pointless.
    private Genome cachedGenome;
    private Traits cachedTraits;
    // True until this bunnidog got its real genome (NBT, breeding, command) or, failing that, a founder genome for the
    // biome it stands in. Not saved: entities loaded from disk always get a genome from NBT before their first tick.
    private boolean founderPending;
    // Bred stat points (Phase 4a). Server side only; null means all zero (see stats()). No initializer, like the fields above.
    private Stats stats;
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
    private long nextForageTick;
    private LivingEntity huntingTarget;

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
                .add(Attributes.ATTACK_DAMAGE, 2.0D);      // only used by feral bunnidogs biting back
    }

    // ------------------------------------------------------------------ genes

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GENES_LO, 0);
        builder.define(DATA_GENES_HI, 0);
    }

    /** The decoded genome. Cheap: only re-decodes when the synced value changed. */
    public Genome genome() {
        long packed = ((this.entityData.get(DATA_GENES_HI) & 0xFFFFFFFFL) << 24)
                | (this.entityData.get(DATA_GENES_LO) & 0xFFFFFFL);
        Genome cached = this.cachedGenome;
        if (cached == null || cached.packed() != packed) {
            cached = new Genome(packed);
            this.cachedGenome = cached;
            this.cachedTraits = cached.traits();
        }
        return cached;
    }

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
        if (!this.level().isClientSide()) {
            this.applyGenetics();
        }
    }

    /**
     * Server side: turn the expressed genes AND the bred stat points into attribute base values
     * (base x gene multiplier x stat multiplier). Keeps the health FRACTION, so a fresh
     * bunnidog stays at full health when its genome (and so its max health) is replaced, and a hurt one stays hurt.
     */
    private void applyGenetics() {
        Traits t = this.traits();
        float oldMax = this.getMaxHealth();
        float fraction = oldMax > 0.0F ? this.getHealth() / oldMax : 1.0F;
        Stats st = this.stats();
        setBase(Attributes.MAX_HEALTH, BASE_HEALTH * t.healthMultiplier() * st.multiplier(Stat.VITALITY));
        setBase(Attributes.MOVEMENT_SPEED, BASE_SPEED * t.speedMultiplier() * st.multiplier(Stat.SWIFTNESS));
        setBase(Attributes.JUMP_STRENGTH, BASE_JUMP * t.jumpMultiplier() * st.multiplier(Stat.LEAP));
        setBase(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE * t.hearingMultiplier() * st.multiplier(Stat.SENSES));
        this.setHealth(this.getMaxHealth() * fraction);
    }

    private void setBase(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
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
            if (this.level() instanceof ServerLevel serverLevel) {
                this.nestHome.tick(serverLevel, !this.isBaby());   // H2: a homeless adult looks for a free Nest, a dog with one checks it
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
        if (age >= 0) {
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
        BlockPos nest = this.homeNest();
        return !this.beingTransported() && this.level().getGameTime() >= this.nextForageTick
                && Foraging.canForage(!this.isBaby(), this.getHealth() < this.getMaxHealth(), nest != null,
                        this.homeNight(), this.getTarget() != null && this.getTarget() != this.huntingTarget)
                && Foraging.insideHome(nest == null ? Double.POSITIVE_INFINITY : this.blockPosition().distSqr(nest));
    }

    boolean foodWithinHome(BlockPos pos) {
        BlockPos nest = this.homeNest();
        return nest != null && Foraging.insideHome(pos.distSqr(nest));
    }

    void ateForage(float healing) {
        this.heal(healing);
        this.playEatingSound();
        this.nextForageTick = this.level().getGameTime() + Foraging.MEAL_COOLDOWN;
    }

    @Override protected void playEatingSound() { this.playSound(SoundEvents.GENERIC_EAT.value(), 0.5F, 1.1F); }

    void huntTarget(LivingEntity target) { this.huntingTarget = target; }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim, DamageSource source) {
        boolean result = super.killedEntity(level, victim, source);
        if (victim == this.huntingTarget && this.diet().hunts() && this.foodWithinHome(victim.blockPosition())) {
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
        if (held.getItem() instanceof CalipersItem) {
            // Phase 5: the Calipers measure instead of feeding or riding; they never reach vanilla's feeding code.
            // Phase 8 S6: a real player gets the sheet WINDOW (the server sends the animal, the client opens the screen); anything else keeps the chat readout.
            if (!this.level().isClientSide()) {
                if (player instanceof ServerPlayer serverPlayer) {
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
        if (!isBunnidogFood(held) && this.canEat(held)) {
            if (!Foraging.handFood(true, false, this.getHealth() < this.getMaxHealth(), this.canAgeUp())) {
                return InteractionResult.PASS;
            }
            if (!this.level().isClientSide()) {
                this.heal(Foraging.HEAL);
                if (this.canAgeUp()) this.ageUp(getSpeedUpSecondsWhenFeeding(-this.getAge()), true);
                held.consume(1, player);
                this.playEatingSound();
            }
            return InteractionResult.SUCCESS;
        }
        boolean readyForLove = !this.level().isClientSide() && isBunnidogFood(held) && !this.isBaby() && this.getAge() == 0
                && !this.guavaMemory.active();
        int tier = GuavaItem.tierOf(held);
        InteractionResult result = super.mobInteract(player, hand);
        if (readyForLove && result.consumesAction()) {
            this.guavaMemory.remember(tier);
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
        this.spawnAtLocation(level, SeedItem.create(seed), 0.5F);
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
     * empty means nothing was born because too many bunnidogs live nearby ({@link Breeding#CROWD_LIMIT}), so the caller
     * should keep the seed. This is the ONE place pups come from: the {@code /bunnidogs hatch} debug command uses it now
     * and the ripe seed crop does ({@link com.bunnidogs.mod.block.SeedCropHarvest}), passing its Fortune bonus as {@code extraQuality}.
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
        for (Seed.Pup roll : litter) {
            Bunnidog pup = ModEntities.BUNNIDOG.get().create(level, EntitySpawnReason.BREEDING);
            if (pup == null) {
                continue;
            }
            pup.setGenome(roll.genome());
            pup.setStats(roll.stats());
            pup.setParents(seed.mother(), seed.father());
            if (roll.mutated()) {
                BunnidogsMod.LOGGER.info("Mutation: a pup got a rare {} allele (genome {})",
                        roll.mutatedGene().displayName(), roll.genome());
            }
            pup.setBaby(true);
            pup.snapTo(pos.x, pos.y, pos.z, rng.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntityWithPassengers(pup);
            born.add(pup);
        }
        return born;
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
        super.die(source);
        if (this.level() instanceof ServerLevel serverLevel) {
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

    public NestHome.MoveResult moveHomeTo(ServerLevel level, BlockPos destination) {
        if (level != this.level()) return NestHome.MoveResult.INVALID;
        return this.nestHome.moveTo(level, destination);
    }

    // ------------------------------------------------------------------ saving

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_GENOME, this.genome().toString());
        output.putString(TAG_STATS, this.stats().toString());
        output.putInt("ForageWait", (int) Math.clamp(this.nextForageTick - this.level().getGameTime(), 0L, (long) Foraging.MEAL_COOLDOWN));
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
        Genome.parse(input.getStringOr(TAG_GENOME, "")).ifPresent(this::setGenome);
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
        this.nextForageTick = this.level().getGameTime() + Math.clamp(input.getIntOr("ForageWait", 0), 0, Foraging.MEAL_COOLDOWN);
        // Phase 4e: a save from before it, or a bunnidog not in love, has neither tag = no memory.
        this.guavaMemory.restore(input.getIntOr(TAG_LOVE_TIER, 0), input.getIntOr(TAG_LOVE_TIER_TICKS, 0));
        // H2: a save from before it, or a dog without a nest, has no Y = no nest.
        this.nestHome.restore(input.getIntOr(TAG_NEST_X, 0), input.getIntOr(TAG_NEST_Y, NestHome.NO_NEST), input.getIntOr(TAG_NEST_Z, 0));
    }

    // ------------------------------------------------------------------ behaviour

    // NOTE: registerGoals() runs INSIDE the super constructor, before the genome exists. So goals are registered for
    // every temperament and gate themselves in canUse() by reading the (later) genome.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Everyone flees when hurt, except feral ones, who fight back instead.
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D) {
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() != Temperament.FERAL && super.canUse();
            }
        });
        // Skittish ones keep their distance from players.
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Player>(this, Player.class, 8.0F, 1.0D, 1.6D) {
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() == Temperament.SKITTISH && super.canUse();
            }
        });
        // Feral ones bite whoever hurt them.
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3D, true) {
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() == Temperament.FERAL
                        && Bunnidog.this.getTarget() != Bunnidog.this.huntingTarget && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return Bunnidog.this.temperament() == Temperament.FERAL
                        && Bunnidog.this.getTarget() != Bunnidog.this.huntingTarget && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return Bunnidog.this.temperament() == Temperament.FERAL && super.canUse();
            }
        });
        // Breeding (Phase 2): mate with another bunnidog in love, follow a guava, pups follow an adult.
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.1D, this::canEat, false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1D));
        // H2: a dog with a home (a claimed Nest, see NestHome and docs/HOME_SPECS.md) walks back once something (a guava, a scare, a
        // long stroll) led it more than 16 blocks away. Below panic, avoid and tempt, above the stroll. Idle for a dog without a nest.
        // H4: at night an adult with a home walks to it and stays within 2 blocks until dawn (docs/HOME_SPECS.md R8). Registered BEFORE
        // GoHomeGoal (same priority: the first one in the list wins a tie) and above the stroll, which it keeps from running all night.
        this.goalSelector.addGoal(6, new NightHomeGoal(this));
        this.goalSelector.addGoal(6, new GoHomeGoal(this));
        this.goalSelector.addGoal(7, new HuntGoal(this));
        this.goalSelector.addGoal(7, new GrazeGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }
}
