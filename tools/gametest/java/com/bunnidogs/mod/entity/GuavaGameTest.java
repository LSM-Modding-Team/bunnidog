package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Runs only from the development source set; neither tests nor fixture enter the mod JAR. */
@EventBusSubscriber(modid = BunnidogsMod.MODID)
public final class GuavaGameTest extends GameTestInstance {
    private static final Identifier TYPE = id("guava_integration");
    public static final MapCodec<GuavaGameTest> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("scenario").forGetter(t -> t.scenario),
            TestData.CODEC.forGetter(GuavaGameTest::info)).apply(i, GuavaGameTest::new));
    private final String scenario;

    private GuavaGameTest(String scenario, TestData<Holder<TestEnvironmentDefinition<?>>> data) {
        super(data);
        this.scenario = scenario;
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, path); }
    @SubscribeEvent
    public static void registerCodec(RegisterEvent event) {
        event.register(Registries.TEST_INSTANCE_TYPE, helper -> helper.register(TYPE, CODEC));
    }
    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("guava_integration"), new TestEnvironmentDefinition.AllOf());
        for (String name : new String[]{"apex_variants", "apex_attacks", "apex_blast", "apex_support", "apex_focus", "boats", "signs", "transport", "rehoming", "wayfinding", "worldgen_signs", "interaction_discovery", "interaction_conversation", "interaction_commands", "interaction_stations", "station_hands", "station_inventory", "station_inventory_machines", "calipers_readings", "entrance_trails", "sampling", "adaptations", "camouflage", "camouflage_sculk", "breeder", "breeding_barn", "breeder_venues", "island_refuge", "island_noise", "coastal_traits", "kelp_gardening", "shore_meals", "cave_sight", "genetic_discoveries", "trail_training", "trail_walking", "pedigree_comparator", "bundle_packs", "bundle_travel", "phasing", "swimming", "snow_routes", "fleece", "splicing", "registry", "lineage_names", "table_planning", "table_automation", "juicer", "breeding_pen", "pen_safety", "pen_callbacks", "pelt_rugs", "specimen_jar", "show", "grooming", "cold_care", "settlement", "apex", "fixtures", "regional_farms", "orchard", "orchard_picking", "sapling_station", "gardening", "cactus_gardening", "mushroom_gardening", "melon_gardening", "fruit_foraging", "regional_meals", "loose_food", "food_gathering", "pantry", "fishing", "hunt_safety", "chorus_care", "refuge", "guarding", "scouting", "resident_finder", "storm_shelter", "recovery_rest", "trough_automation", "bell_calls", "trough_filters", "care_dispensers", "basket", "basket_dispenser", "basket_callbacks", "cultivation", "nest_markers", "nest_signals", "night_meals", "nursery", "seed_recovery", "cradle_storage", "cradle_hatching", "cradle_spawn_refusal", "crop_harvest", "composting", "work_modes", "work_pause", "hand_care", "cradle_controls", "work_census", "trough_supply", "supply_callbacks", "garden_restoration", "nursery_workers", "nursery_treatment"}) {
            event.registerTest(id(name), new GuavaGameTest(name, new TestData<>(environment, id(name.startsWith("apex_") ? "apex_arena" : name.equals("trail_walking") ? "trail_corridor" : "empty"), name.equals("nest_signals") || name.equals("juicer") || name.equals("breeding_pen") || name.equals("trail_walking") ? 180 : 100, 0, true)));
        }
        for (String name:new String[]{"learning_ai","learning_feedback","learning_harvest","learning_delivery","learning_limits","learning_legacy","cognitive_genetics","commissions","commission_callbacks","trail_provisions","care_menus","care_stations","first_litter","workshop_controls","signal_controls","apex_preparation","family_goals","companion_fight","companion_rescue","companion_roster","companion_crossfire","companion_genetics","companion_focus","companion_lookout","companion_hold","companion_party"})
            event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id(name.equals("family_goals") ? "family_workshop" : "empty"),name.equals("learning_ai") ? com.bunnidogs.mod.genetics.Gardening.SEARCH_INTERVAL + com.bunnidogs.mod.genetics.Gardening.MAX_VISIT + 40 : name.equals("companion_roster") ? 260 : name.equals("first_litter") ? 180 : name.equals("companion_party") ? 120 : 100,0,true)));
    }
    @Override public MapCodec<GuavaGameTest> codec() { return CODEC; }
    @Override protected MutableComponent typeDescription() { return Component.literal(this.scenario); }
    @Override public void run(GameTestHelper h) {
        switch (this.scenario) {
            case "apex_variants" -> ApexTypesGameTest.variants(h);
            case "apex_attacks" -> ApexTypesGameTest.attacks(h);
            case "apex_blast" -> ApexTypesGameTest.blast(h);
            case "apex_support" -> ApexTypesGameTest.support(h);
            case "apex_focus" -> ApexTypesGameTest.focus(h);
            case "boats" -> boats(h);
            case "signs" -> signs(h);
            case "wayfinding" -> com.bunnidogs.mod.worldgen.WayfindingGameTest.run(h);
            case "interaction_discovery" -> InteractionGameTest.run(h);
            case "interaction_stations" -> StationInteractionGameTest.run(h);
            case "calipers_readings" -> CareReadingsGameTest.run(h);
            case "station_inventory" -> StationInventoryGameTest.selection(h);
            case "station_inventory_machines" -> StationInventoryGameTest.machines(h);
            case "station_hands" -> StationHandsGameTest.run(h);
            case "interaction_commands" -> { InteractionCommandsGameTest.run(h); return; }
            case "interaction_conversation" -> { InteractionGameTest.conversation(h); return; }
            case "worldgen_signs" -> com.bunnidogs.mod.worldgen.WorldgenSignsGameTest.run(h);
            case "entrance_trails" -> com.bunnidogs.mod.worldgen.EntranceTrailsGameTest.run(h);
            case "sampling" -> SamplingGameTest.run(h);
            case "adaptations" -> AdaptationsGameTest.run(h);
            case "camouflage" -> CamouflageGameTest.rules(h);
            case "camouflage_sculk" -> { CamouflageGameTest.vibrations(h); return; }
            case "breeder" -> { BreederGameTest.run(h); return; }
            case "breeding_barn" -> com.bunnidogs.mod.worldgen.BreedingBarnGameTest.run(h);
            case "breeder_venues" -> com.bunnidogs.mod.worldgen.BreederVenuesGameTest.run(h);
            case "island_refuge" -> com.bunnidogs.mod.worldgen.IslandRefugeGameTest.run(h);
            case "island_noise" -> com.bunnidogs.mod.worldgen.NaturalIslandGameTest.run(h);
            case "coastal_traits" -> { CoastalTraitsGameTest.run(h); return; }
            case "kelp_gardening" -> ShoreCareGameTest.garden(h);
            case "shore_meals" -> ShoreCareGameTest.meals(h);
            case "cave_sight" -> { CaveSightGameTest.run(h); return; }
            case "genetic_discoveries" -> DiscoveryGameTest.run(h);
            case "trail_training" -> TrailGameTest.training(h);
            case "trail_walking" -> { TrailGameTest.walking(h); return; }
            case "companion_fight" -> { CompanionCombatGameTest.fight(h); return; }
            case "companion_rescue" -> { CompanionCombatGameTest.rescue(h); return; }
            case "companion_roster" -> { CompanionCombatGameTest.roster(h); return; }
            case "companion_crossfire" -> { CompanionCombatGameTest.crossfire(h); return; }
            case "companion_genetics" -> CompanionCombatGameTest.genetics(h);
            case "companion_focus" -> { CompanionCombatGameTest.focus(h); return; }
            case "companion_lookout" -> { CompanionCombatGameTest.lookout(h); return; }
            case "companion_hold" -> { CompanionCombatGameTest.hold(h); return; }
            case "companion_party" -> { CompanionCombatGameTest.party(h); return; }
            case "commissions" -> CommissionGameTest.rules(h);
            case "commission_callbacks" -> CommissionGameTest.callbacks(h);
            case "trail_provisions" -> TrailProvisionsGameTest.run(h);
            case "care_menus" -> CohesionGameTest.animals(h);
            case "care_stations" -> CohesionGameTest.stations(h);
            case "workshop_controls" -> WorkshopControlsGameTest.run(h);
            case "signal_controls" -> SignalControlsGameTest.run(h);
            case "apex_preparation" -> ApexPreparationGameTest.run(h);
            case "family_goals" -> { FamilyGoalsGameTest.run(h); return; }
            case "first_litter" -> { CohesionGameTest.journey(h); return; }
            case "pedigree_comparator" -> { PedigreeComparatorGameTest.run(h); return; }
            case "bundle_packs" -> BundlePacksGameTest.rules(h);
            case "bundle_travel" -> { BundlePacksGameTest.travel(h); return; }
            case "phasing" -> { PhaseGameTest.run(h); return; }
            case "swimming" -> { SwimmingGameTest.run(h); return; }
            case "snow_routes" -> { SnowRoutesGameTest.run(h); return; }
            case "fleece" -> { FleeceGameTest.run(h); return; }
            case "splicing" -> SplicingGameTest.run(h);
            case "cognitive_genetics" -> CognitionGameTest.run(h);
            case "learning_feedback" -> { LearningGameTest.feedback(h); return; }
            case "learning_harvest" -> { LearningGameTest.harvest(h); return; }
            case "learning_delivery" -> { LearningGameTest.delivery(h); return; }
            case "learning_limits" -> { LearningGameTest.limits(h); return; }
            case "learning_legacy" -> LearningGameTest.legacy(h);
            case "learning_ai" -> { LearningGameTest.autonomous(h); return; }
            case "lineage_names" -> LineageEditingGameTest.run(h);
            case "registry" -> RegistryGameTest.run(h);
            case "table_planning" -> TablePlanningGameTest.run(h);
            case "pelt_rugs" -> { PeltRugsGameTest.run(h); return; }
            case "specimen_jar" -> { SpecimenJarGameTest.run(h); return; }
            case "breeding_pen" -> { BreedingPenGameTest.courtship(h); return; }
            case "pen_safety" -> { BreedingPenGameTest.safeguards(h); return; }
            case "pen_callbacks" -> { BreedingPenGameTest.callbacks(h); return; }
            case "juicer" -> { JuicerGameTest.run(h); return; }
            case "table_automation" -> { TableAutomationGameTest.run(h); return; }
            case "show" -> ShowGameTest.run(h);
            case "apex" -> ApexGameTest.run(h);
            case "fixtures" -> com.bunnidogs.mod.worldgen.FixturesGameTest.run(h);
            case "regional_farms" -> com.bunnidogs.mod.worldgen.RegionalFarmsGameTest.run(h);
            case "orchard" -> { OrchardGameTest.run(h); return; }
            case "orchard_picking" -> { OrchardPickingGameTest.run(h); return; }
            case "sapling_station" -> { SaplingStationGameTest.run(h); return; }
            case "pantry" -> com.bunnidogs.mod.worldgen.PantryGameTest.run(h);
            case "gardening" -> { GardeningGameTest.run(h); return; }
            case "cactus_gardening" -> { CactusGardeningGameTest.run(h); return; }
            case "mushroom_gardening" -> { MushroomGardeningGameTest.run(h); return; }
            case "melon_gardening" -> { MelonGardeningGameTest.run(h); return; }
            case "fruit_foraging" -> { FruitForagingGameTest.run(h); return; }
            case "regional_meals" -> { RegionalMealsGameTest.run(h); return; }
            case "loose_food" -> { LooseFoodGameTest.run(h); return; }
            case "food_gathering" -> { FoodGatheringGameTest.run(h); return; }
            case "cultivation" -> { CultivationGameTest.run(h); return; }
            case "nest_markers" -> { NestMarkerGameTest.run(h); return; }
            case "nest_signals" -> { NestSignalGameTest.run(h); return; }
            case "night_meals" -> { NightMealGameTest.run(h); return; }
            case "nursery" -> { NurseryGameTest.run(h); return; }
            case "seed_recovery" -> SeedRecoveryGameTest.run(h);
            case "cradle_storage" -> { com.bunnidogs.mod.block.CradleStorageGameTest.run(h); return; }
            case "cradle_hatching" -> { CradleHatchingGameTest.run(h); return; }
            case "cradle_spawn_refusal" -> CradleSpawnRefusalGameTest.run(h);
            case "crop_harvest" -> CropHarvestGameTest.run(h);
            case "composting" -> { CompostingGameTest.run(h); return; }
            case "work_modes" -> ResidentWorkGameTest.modes(h);
            case "work_pause" -> { WorkPauseGameTest.run(h); return; }
            case "hand_care" -> HandCareGameTest.run(h);
            case "cradle_controls" -> { CradleControlsGameTest.run(h); return; }
            case "work_census" -> { WorkCensusGameTest.run(h); return; }
            case "trough_supply" -> { SupplyGameTest.run(h); return; }
            case "supply_callbacks" -> { SupplyGameTest.callbacks(h); return; }
            case "garden_restoration" -> { ResidentWorkGameTest.restoration(h); return; }
            case "nursery_workers" -> { ResidentWorkGameTest.nursery(h); return; }
            case "nursery_treatment" -> { com.bunnidogs.mod.block.NurseryTreatmentGameTest.run(h); return; }
            case "fishing" -> { FishingGameTest.run(h); return; }
            case "hunt_safety" -> { HuntSafetyGameTest.run(h); return; }
            case "refuge" -> { RefugeGameTest.run(h); return; }
            case "guarding" -> { GuardGameTest.run(h); return; }
            case "scouting" -> ScoutingGameTest.run(h);
            case "resident_finder" -> ResidentFinderGameTest.run(h);
            case "storm_shelter" -> { StormShelterGameTest.run(h); return; }
            case "recovery_rest" -> { RecoveryRestGameTest.run(h); return; }
            case "trough_automation" -> { TroughAutomationGameTest.run(h); return; }
            case "bell_calls" -> { BellCallGameTest.run(h); return; }
            case "trough_filters" -> { TroughFilterGameTest.run(h); return; }
            case "care_dispensers" -> { CareDispenserGameTest.run(h); return; }
            case "basket" -> { BasketGameTest.run(h); return; }
            case "basket_dispenser" -> { BasketDispenserGameTest.run(h); return; }
            case "basket_callbacks" -> { BasketDispenserGameTest.callbacks(h); return; }
            case "chorus_care" -> { ChorusCareGameTest.run(h); return; }
            case "grooming" -> GroomingGameTest.run(h);
            case "cold_care" -> { ColdCareGameTest.run(h); return; }
            case "settlement" -> { SettlementGameTest.run(h); return; }
            case "transport" -> transport(h);
            case "rehoming" -> { rehoming(h); return; }
            default -> h.fail("Unknown integration scenario: " + this.scenario);
        }
        h.succeed();
    }
    private static void boats(GameTestHelper h) {
        var level = h.getLevel();
        var boat = ModEntities.GUAVA_BOAT.get().create(level, EntitySpawnReason.COMMAND);
        var chest = ModEntities.GUAVA_CHEST_BOAT.get().create(level, EntitySpawnReason.COMMAND);
        h.assertTrue(boat != null && chest != null, "Vehicle factories must create both guava boat types");
        var boatPos = h.absolutePos(new BlockPos(1, 2, 1));
        var chestPos = h.absolutePos(new BlockPos(4, 2, 4));
        boat.setInitialPos(boatPos.getX() + .5, boatPos.getY(), boatPos.getZ() + .5);
        chest.setInitialPos(chestPos.getX() + .5, chestPos.getY(), chestPos.getZ() + .5);
        level.addFreshEntity(boat);
        level.addFreshEntity(chest);
        h.assertTrue(boat.getPickResult().is(ModItems.GUAVA_BOAT.get()), "Normal boat pick/drop identity");
        h.assertTrue(chest.getPickResult().is(ModItems.GUAVA_CHEST_BOAT.get()), "Chest boat pick/drop identity");
        var dog1 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 1, 2, 1);
        var dog2 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 1, 2, 2);
        var dog3 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 2, 2, 1);
        var dog4 = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 2, 2, 2);
        h.assertTrue(dog1.startRiding(boat) && dog2.startRiding(boat) && !dog3.startRiding(boat), "Normal boat must have exactly two seats");
        dog1.stopRiding(); dog2.stopRiding();
        // Fresh passengers avoid vanilla's cooldown after dismounting the other boat.
        h.assertTrue(dog3.startRiding(chest) && !dog4.startRiding(chest), "Chest boat must have exactly one seat");
        dog3.stopRiding();
        chest.setCustomName(Component.literal("Guava expedition"));
        chest.setItem(0, new ItemStack(ModItems.GUAVA.get(), 3));
        var sample = new ItemStack(ModItems.SEED.get());
        sample.set(ModItems.SEED_DATA.get(), Genome.DEFAULT + "|" + Genome.DEFAULT + "|1.5");
        chest.setItem(26, sample.copy());
        var reporter = new ProblemReporter.Collector();
        var output = TagValueOutput.createWithContext(reporter, level.registryAccess());
        chest.saveWithoutId(output);
        var restored = ModEntities.GUAVA_CHEST_BOAT.get().create(level, EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(reporter, level.registryAccess(), output.buildResult()));
        h.assertTrue(reporter.isEmpty(), "Boat save/load must not lose codec data");
        h.assertTrue(restored.getContainerSize() == 27 && restored.getItem(0).getCount() == 3
                && ItemStack.isSameItemSameComponents(restored.getItem(26), sample), "All 27 chest slots and seed components must survive load");
        h.assertTrue(restored.getCustomName() != null && restored.getCustomName().getString().equals("Guava expedition"), "Boat name must survive load");
        chest.destroy(level, level.damageSources().generic());
        var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(chest.blockPosition()).inflate(3));
        h.assertTrue(drops.stream().filter(e -> e.getItem().is(ModItems.GUAVA_CHEST_BOAT.get())).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                "Destroying a chest boat must drop exactly its own boat item");
        h.assertTrue(drops.stream().filter(e -> e.getItem().is(ModItems.GUAVA.get())).mapToInt(e -> e.getItem().getCount()).sum() == 3,
                "Chest contents must drop exactly once");
        h.assertTrue(drops.stream().anyMatch(e -> ItemStack.isSameItemSameComponents(e.getItem(), sample)), "Dropped seeds must preserve their genetic data");
    }
    private static void signs(GameTestHelper h) {
        var blocks = new SignBlock[]{ModBlocks.GUAVA_SIGN.get(), ModBlocks.GUAVA_WALL_SIGN.get(),
                ModBlocks.GUAVA_HANGING_SIGN.get(), ModBlocks.GUAVA_WALL_HANGING_SIGN.get()};
        for (int i = 0; i < blocks.length; i++) {
            var pos = new BlockPos(i + 1, 2, 2);
            h.setBlock(pos.below(), Blocks.STONE);
            h.setBlock(pos.above(), Blocks.STONE);
            h.setBlock(pos.south(), Blocks.STONE);
            h.setBlock(pos, blocks[i]);
            var sign = h.getBlockEntity(pos, SignBlockEntity.class);
            var state = h.getBlockState(pos);
            h.assertTrue(sign.getType().isValid(state), "Native block-entity type must accept every guava sign form");
            h.assertTrue(blocks[i].getTicker(h.getLevel(), state, sign.getType()) != null, "Native sign editing-lock ticker must exist");
            sign.setText(new SignText().setMessage(0, Component.literal("Nursery")).setColor(DyeColor.GREEN).setHasGlowingText(true), true);
            sign.setText(new SignText().setMessage(0, Component.literal("Pantry")).setColor(DyeColor.RED), false);
            sign.setWaxed(true);
            var loaded = BlockEntity.loadStatic(h.absolutePos(pos), state, sign.saveWithFullMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
            h.assertTrue(loaded instanceof SignBlockEntity, "Sign must load through the registered native type");
            var copy = (SignBlockEntity) loaded;
            h.assertTrue(copy.isWaxed() && copy.getFrontText().hasGlowingText() && copy.getFrontText().getColor() == DyeColor.GREEN
                    && copy.getBackText().getColor() == DyeColor.RED, "Wax, glow and both colors must survive save/load");
            h.assertTrue(copy.getFrontText().getMessage(0, false).getString().equals("Nursery")
                    && copy.getBackText().getMessage(0, false).getString().equals("Pantry"), "Both text faces must survive save/load");
            h.assertTrue(copy.getMaxTextLineWidth() == (i < 2 ? 90 : 60), "Native text width must match sign geometry");
        }
    }
    private static void transport(GameTestHelper h) {
        var clock = h.getLevel().dimensionType().defaultClock().orElseThrow();
        long previous = h.getLevel().clockManager().getTotalTicks(clock);
        h.getLevel().clockManager().setTotalTicks(clock, 1000);
        try { transportAtDay(h); }
        finally { h.getLevel().clockManager().setTotalTicks(clock, previous); }
    }
    private static void transportAtDay(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 3, 2, 3);
        dog.setAge(0);
        dog.setGenome(Genome.DEFAULT.with(Gene.DIET, 0, 1).with(Gene.DIET, 1, 1));
        dog.setHealth(dog.getMaxHealth() - 2);
        var home = h.absolutePos(new BlockPos(1, 1, 1));
        h.setBlock(1, 1, 1, ModBlocks.NEST.get());
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        dog.saveWithoutId(output);
        var saved = output.buildResult();
        saved.putInt("NestX", home.getX()); saved.putInt("NestY", home.getY()); saved.putInt("NestZ", home.getZ());
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        h.assertTrue(dog.canForage(), "An injured grazer at its home must be eligible before transport: baby=" + dog.isBaby()
                + ", health=" + dog.getHealth() + "/" + dog.getMaxHealth() + ", night=" + dog.homeNight()
                + ", transport=" + dog.beingTransported() + ", home=" + dog.homeNest());
        var holder = h.makeMockServerPlayerInLevel();
        dog.setLeashedTo(holder, false);
        h.assertTrue(dog.beingTransported() && !dog.canForage(), "A lead must suspend food navigation");
        var clock = level.dimensionType().defaultClock().orElseThrow();
        level.clockManager().setTotalTicks(clock, 13000);
        h.assertTrue(dog.homeNight() && !new NightHomeGoal(dog).canUse() && !new NightHomeGoal(dog).canContinueToUse(), "A lead must suspend home goals at night");
        var before = dog.position();
        dog.setPos(home.getX() + 20, home.getY() + 1, home.getZ());
        h.assertTrue(!new GoHomeGoal(dog).canUse(), "A lead must prevent returning from outside the home radius");
        dog.setPos(before);
        var leashOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        dog.saveWithoutId(leashOutput);
        var restored = ModEntities.BUNNIDOG.get().create(level, EntitySpawnReason.LOAD);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), leashOutput.buildResult()));
        h.assertTrue(restored.beingTransported() && home.equals(restored.homeNest()), "Pending leash restoration must retain and suspend its saved Nest");
        dog.removeLeash();
        h.assertTrue(new NightHomeGoal(dog).canUse(), "At night, unleashing must resume the home goal");
        level.clockManager().setTotalTicks(clock, 1000);
        h.assertTrue(!dog.beingTransported() && home.equals(dog.homeNest()) && dog.canForage(), "Unleashing must restore normal behavior without forgetting home");
        var boat = ModEntities.GUAVA_BOAT.get().create(level, EntitySpawnReason.COMMAND);
        boat.setInitialPos(dog.getX(), dog.getY(), dog.getZ()); level.addFreshEntity(boat);
        h.assertTrue(dog.startRiding(boat), "Bunnidog must fit a guava boat");
        h.assertTrue(dog.beingTransported() && !dog.canForage() && !new NightHomeGoal(dog).canUse(), "Riding must suspend voluntary home/food navigation");
        dog.stopRiding();
        h.assertTrue(!dog.beingTransported() && home.equals(dog.homeNest()), "Disembarking must retain the Nest");
        dog.setPos(home.getX() + 20, home.getY() + 1, home.getZ());
        h.assertTrue(new GoHomeGoal(dog).canUse(), "After transport, a dog outside its home radius must resume returning");
    }
    private static void rehoming(GameTestHelper h) {
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x, 0, z, Blocks.STONE);
        var oldPos = new BlockPos(1, 0, 1);
        var newPos = new BlockPos(5, 0, 5);
        var blockedPos = new BlockPos(1, 0, 6);
        h.setBlock(oldPos, ModBlocks.NEST.get());
        h.setBlock(newPos, ModBlocks.NEST.get());
        h.setBlock(blockedPos, ModBlocks.NEST.get());
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            h.setBlock(blockedPos.offset(dx, 1, dz), Blocks.STONE);
            h.setBlock(blockedPos.offset(dx, 2, dz), Blocks.STONE);
        }
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 2, 1, 2);
        var other = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 4, 1, 4);
        var pup = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 5, 1, 4);
        pup.setAge(-24000);
        var player = h.makeMockServerPlayerInLevel();
        CareMenuGameTest.prepare(player);
        var playerPos = h.absolutePos(new BlockPos(4, 1, 3));
        player.setPos(playerPos.getX() + .5, playerPos.getY(), playerPos.getZ() + .5);
        dog.setLeashedTo(player, false);
        other.setLeashedTo(player, false);
        var level = h.getLevel();
        var oldHome = h.absolutePos(oldPos);
        var newHome = h.absolutePos(newPos);
        // Block-to-POI updates can be queued; test after the server has applied them.
        h.runAfterDelay(2, () -> {
            h.assertTrue(dog.moveHomeTo(level, oldHome) == NestHome.MoveResult.MOVED, "First home must acquire a free POI ticket");
            h.assertTrue(dog.moveHomeTo(level, oldHome) == NestHome.MoveResult.ALREADY_HOME, "Same-home requests must not consume another ticket");
            h.assertTrue(other.moveHomeTo(level, oldHome) == NestHome.MoveResult.OCCUPIED, "A resident must not be displaced");
            h.assertTrue(dog.moveHomeTo(level, h.absolutePos(blockedPos)) == NestHome.MoveResult.UNREACHABLE
                    && oldHome.equals(dog.homeNest()), "A failed route must retain the old home");
            h.assertTrue(pup.moveHomeTo(level, newHome) == NestHome.MoveResult.INVALID, "Pups must not claim Nests");
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(net.minecraft.world.item.Items.LEAD));
            h.useBlock(newPos, player);
            h.assertTrue(newHome.equals(other.homeNest()) && oldHome.equals(dog.homeNest()), "One Nest click must rehome only the nearest led adult");
            h.assertTrue(dog.moveHomeTo(level, newHome) == NestHome.MoveResult.OCCUPIED && oldHome.equals(dog.homeNest()), "Occupied destination must keep the source claim");
            other.removeLeash();
            // Empty-hand interaction also works after the last carried lead was attached.
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            h.useBlock(newPos, player);
            h.assertTrue(oldHome.equals(dog.homeNest()), "An occupied Nest must still refuse an empty-hand move");
            h.assertTrue(other.moveHomeTo(level, h.absolutePos(new BlockPos(7, 0, 7))) == NestHome.MoveResult.INVALID, "Ordinary blocks cannot become a home");
            h.setBlock(6, 0, 2, ModBlocks.NEST.get());
            h.runAfterDelay(2, () -> {
                var thirdHome = h.absolutePos(new BlockPos(6, 0, 2));
                CareMenuGameTest.assignHome(h,player,thirdHome);
                h.assertTrue(thirdHome.equals(dog.homeNest()) && dog.isLeashed(), "The explicit assignment must retain the lead and set the new Nest");
                h.assertTrue(other.moveHomeTo(level, oldHome) == NestHome.MoveResult.MOVED, "Successful relocation must free the old Nest ticket");
                h.succeed();
            });
        });
    }
}
