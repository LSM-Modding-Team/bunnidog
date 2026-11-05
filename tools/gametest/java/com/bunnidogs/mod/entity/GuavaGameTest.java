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
        // Work scenarios require daytime even when the development world is reused between runs.
        var environment = event.registerEnvironment(id("guava_integration"), new TestEnvironmentDefinition.Functions(java.util.Optional.of(id("test_setup")),java.util.Optional.empty()));
        // Long native growth waits run alone, rather than keeping forty-nine unrelated arenas ticking.
        var continuity = event.registerEnvironment(id("wild_continuity"), new TestEnvironmentDefinition.Functions(java.util.Optional.of(id("test_setup")),java.util.Optional.empty()));
        var furniture = event.registerEnvironment(id("wild_furniture"), new TestEnvironmentDefinition.Functions(java.util.Optional.of(id("test_setup")),java.util.Optional.empty()));
        var industry = event.registerEnvironment(id("wild_industry"), new TestEnvironmentDefinition.Functions(java.util.Optional.of(id("test_setup")),java.util.Optional.empty()));
        // These assert exact machine/hand transactions; natural growth belongs to the separate wild references.
        var orchardRules=event.registerEnvironment(id("orchard_rules"),new TestEnvironmentDefinition.SetGameRules(new net.minecraft.world.level.gamerules.GameRuleMap.Builder().set(net.minecraft.world.level.gamerules.GameRules.RANDOM_TICK_SPEED,0).build()));
        var orchard=event.registerEnvironment(id("orchard_transactions"),new TestEnvironmentDefinition.AllOf(java.util.List.of(environment,orchardRules)));
        event.registerTest(id("crown_citadel"),new GuavaGameTest("crown_citadel",new TestData<>(environment,id("crown_citadel"),480,0,true)));
        for(String name:new String[]{"wild_genesis","wild_founder_veto","wild_materials","wild_nursery","wild_nursery_veto","wild_limits","wild_worldgen","wild_ai"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("wild_ai") ? 3600 : 180,0,true)));
        for(String name:new String[]{"wild_bench_safety","wild_bench_legacy","wild_bench_handoffs"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        for(String name:new String[]{"primitive_furniture","primitive_limits","primitive_ai"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(name.equals("primitive_ai") ? furniture : environment,id("crown_arena"),name.equals("primitive_ai") ? 120000 : 240,0,true)));
        event.registerTest(id("primitive_placement_collision"),new GuavaGameTest("primitive_placement_collision",new TestData<>(environment,id("crown_arena"),300,0,true)));
        event.registerTest(id("primitive_family"),new GuavaGameTest("primitive_family",new TestData<>(continuity,id("crown_arena"),300000,0,true)));
        event.registerTest(id("primitive_family_reserve"),new GuavaGameTest("primitive_family_reserve",new TestData<>(environment,id("crown_arena"),300,0,true)));
        event.registerTest(id("primitive_family_carer"),new GuavaGameTest("primitive_family_carer",new TestData<>(environment,id("crown_arena"),300,0,true)));
        for(String name:new String[]{"primitive_cache_payment","primitive_cache_safety","primitive_cache_callbacks","primitive_cache_meals","primitive_cache_player_legacy","primitive_cache_shared_learning","primitive_cache_collision","primitive_cache_clearance"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        event.registerTest(id("primitive_food_tool"),new GuavaGameTest("primitive_food_tool",new TestData<>(environment,id("crown_arena"),300,0,true)));
        for(String name:new String[]{"civic_tool_custody","civic_tool_callbacks"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        for(String name:new String[]{"civic_haul_capacity","civic_haul_authority"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        for(String name:new String[]{"civic_recovery_stock","civic_recovery_authority","civic_recovery_tools","civic_recovery_axe"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        event.registerTest(id("civic_recovery_ai"),new GuavaGameTest("civic_recovery_ai",new TestData<>(environment,id("crown_arena"),600,0,true)));
        for(String name:new String[]{"hearth_pause_power","hearth_pause_lock","hearth_pause_meals"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        event.registerTest(id("primitive_material_demand"),new GuavaGameTest("primitive_material_demand",new TestData<>(environment,id("crown_arena"),300,0,true)));
        event.registerTest(id("primitive_diet_meals"),new GuavaGameTest("primitive_diet_meals",new TestData<>(environment,id("crown_arena"),300,0,true)));
        for(String name:new String[]{"wild_harvest_capacity","wild_harvest_receipt"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        // Its two paid homes can defer optional quarry work until the same actual family reserve as continuity.
        for(String name:new String[]{"wild_works_chain","wild_works_safety","wild_works_legacy","wild_works_ai"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(name.equals("wild_works_ai") ? industry : environment,id("crown_arena"),name.equals("wild_works_ai") ? 300000 : 240,0,true)));
        for(String name:new String[]{"wild_rest_safety","wild_rest_legacy"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        for(String name:new String[]{"fields_coppice","fields_journey","fields_protection","fields_ownership","fields_ai"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("fields_ai") ? 760 : 160,0,true)));
        for(String name:new String[]{"commons_planning","commons_planning_limits","commons_claims","commons_recipes","commons_scarcity","construction_parcel","construction_fitting","construction_repair","construction_helper","construction_ai"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("construction_ai") ? 760 : 160,0,true)));
        for(String name:new String[]{"culture_ai","culture_archive","culture_books","culture_interruptions","culture_limits","culture_stale"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("culture_ai") ? 760 : 160,0,true)));
        for(String name:new String[]{"clinic_first_aid","clinic_first_aid_callbacks","clinic_ai","clinic_stock","clinic_callbacks","clinic_lesson","clinic_limits","clinic_craft","tactics_learning","tactics_coordination"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("clinic_first_aid") || name.equals("clinic_ai") ? 600 : 320,0,true)));
        event.registerTest(id("clinic_first_aid_reach"),new GuavaGameTest("clinic_first_aid_reach",new TestData<>(environment,id("crown_arena"),600,0,true)));
        for(String name:new String[]{"field_harvest","field_garden","field_nursery","field_safety","field_scout","field_gathering"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),160,0,true)));
        for(var kind:ApexKind.values())for(boolean flying:new boolean[]{false,true}) {
            String name="apex_pillar_"+(flying ? "flying_" : "")+kind.id;
            event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("apex_arena"),260,0,true)));
        }
        for(String name:new String[]{"guild_ammo","guild_workshop","guild_outpost","guild_towns","guild_wagon","guild_worldgen","guild_habitation"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),300,0,true)));
        event.registerTest(id("review_crown_director"),new GuavaGameTest("review_crown_director",new TestData<>(environment,id("crown_arena"),1640,0,true)));
        for(String name:new String[]{"orchard_shake","orchard_picker","orchard_grove"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),120,0,true)));
        for(String name:new String[]{"scale_night","scale_deaths","scale_audit","scale_acquisition"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),160,0,true)));
        for(String name:new String[]{"infusion_native","infusion_sources"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),160,0,true)));
        for(String name:new String[]{"rebalance_supply","rebalance_pressure","rebalance_defence"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("rebalance_pressure") ? 950 : 240,0,true)));
        for(String name:new String[]{"workshop_teaching","workshop_building","workshop_replacement","workshop_migration","workshop_walking"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("workshop_walking") ? 900 : 360,0,true)));
        for(String name:new String[]{"apprentice_recipe","apprentice_rules","apprentice_parcel","apprentice_walking"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("apprentice_walking") ? 1200 : 600,0,true)));
        for(String name:new String[]{"notes_recipe","notes_safety","notes_boundaries","notes_walking"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("notes_walking") ? 1200 : 240,0,true)));
        for(String name:new String[]{"furnace_processing","furnace_safety","furnace_claims","furnace_ai"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("furnace_ai") ? 1400 : 180,0,true)));
        for(String name:new String[]{"worksite_capacity","worksite_parcels","worksite_furnace"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),180,0,true)));
        for(String name:new String[]{"rescue_policy","rescue_chain","rescue_callbacks","rescue_blocked","rescue_recovery","rescue_authority","rescue_ai"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("rescue_ai") ? 2000 : 240,0,true)));
        for(String name:new String[]{"review_launcher","review_shield","review_crown_beam","review_crown_pack","lunar_forge","barding","crown_boundary","lunar_masonry","route_watch_safety","route_food","route_double","route_pause","route_observation","route_transfer","route_changes","route_limits","route_ai","hearth_conservation","hearth_gestures","hearth_ai","hearth_household","hearth_family","hunt_progress","entry_materials","kit_supplies","moon_mining","astralite_smith","equipment_modes","equipment_support","kit_loadouts","crown_wings","crown_balance","crown_gate","crown_boss","crown_rematch","crown_gear","crown_attacks","crown_support","crown_worldgen","crown_citadel_proto","crown_abandon"})event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id("crown_arena"),name.equals("route_ai") ? 480 : 240,0,true)));
        for(String name:new String[]{"exp_armor","exp_smith","exp_vacuum","exp_flask","exp_launcher","exp_totem","realm_travel","realm_harness","realm_ecology","realm_ward","realm_lance","realm_ruin","apex_followup_blast","apex_anvil_arc","apex_critical_meals","apex_multilayer_dive","apex_client_death","apex_swimming","apex_final_blast","apex_debts","apex_brood_tools"})
            event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id(name.equals("apex_final_blast") || name.equals("exp_totem") ? "apex_blast_arena" : "apex_arena"),name.equals("apex_multilayer_dive") ? 260 : name.equals("exp_totem") ? 200 : 100,0,true)));
        for (String name : new String[]{"apex_tactics", "apex_variants", "apex_attacks", "apex_blast", "apex_support", "apex_focus", "boats", "signs", "transport", "rehoming", "wayfinding", "worldgen_signs", "interaction_discovery", "interaction_conversation", "interaction_commands", "interaction_stations", "station_hands", "station_inventory", "station_inventory_machines", "calipers_readings", "entrance_trails", "sampling", "adaptations", "camouflage", "camouflage_sculk", "breeder", "breeding_barn", "breeder_venues", "island_refuge", "island_noise", "coastal_traits", "kelp_gardening", "shore_meals", "cave_sight", "genetic_discoveries", "trail_training", "trail_walking", "pedigree_comparator", "bundle_packs", "bundle_travel", "phasing", "swimming", "snow_routes", "fleece", "splicing", "registry", "lineage_names", "table_planning", "table_automation", "juicer", "breeding_pen", "pen_safety", "pen_callbacks", "pelt_rugs", "specimen_jar", "show", "grooming", "cold_care", "settlement", "apex", "fixtures", "regional_farms", "orchard", "orchard_picking", "sapling_station", "gardening", "cactus_gardening", "mushroom_gardening", "melon_gardening", "fruit_foraging", "regional_meals", "loose_food", "food_gathering", "pantry", "fishing", "hunt_safety", "chorus_care", "refuge", "guarding", "scouting", "resident_finder", "storm_shelter", "recovery_rest", "trough_automation", "bell_calls", "trough_filters", "care_dispensers", "basket", "basket_dispenser", "basket_callbacks", "cultivation", "nest_markers", "nest_signals", "night_meals", "nursery", "seed_recovery", "cradle_storage", "cradle_hatching", "cradle_spawn_refusal", "crop_harvest", "composting", "work_modes", "work_pause", "hand_care", "cradle_controls", "work_census", "trough_supply", "supply_callbacks", "garden_restoration", "nursery_workers", "nursery_treatment"}) {
            event.registerTest(id(name), new GuavaGameTest(name, new TestData<>(name.equals("orchard") || name.equals("orchard_picking") ? orchard : environment, id(name.startsWith("apex_") ? "apex_arena" : name.equals("trail_walking") ? "trail_corridor" : "empty"), name.equals("apex_tactics") ? 260 : name.equals("cave_sight") || name.equals("storm_shelter") ? 1200 : name.equals("nest_signals") || name.equals("juicer") || name.equals("breeding_pen") || name.equals("trail_walking") ? 180 : 100, 0, true)));
        }
        for (String name:new String[]{"learning_ai","learning_feedback","learning_harvest","learning_delivery","learning_limits","learning_legacy","cognitive_genetics","commissions","commission_callbacks","trail_provisions","care_menus","care_stations","first_litter","workshop_controls","signal_controls","apex_preparation","family_goals","companion_fight","companion_recall_detour","companion_rescue","companion_roster","companion_crossfire","companion_genetics","companion_focus","companion_lookout","companion_hold","companion_party"})
            event.registerTest(id(name),new GuavaGameTest(name,new TestData<>(environment,id(name.equals("family_goals") ? "family_workshop" : "empty"),name.equals("learning_ai") ? com.bunnidogs.mod.genetics.Gardening.SEARCH_INTERVAL + com.bunnidogs.mod.genetics.Gardening.MAX_VISIT + 40 : name.equals("companion_roster") ? 260 : name.equals("first_litter") ? 180 : name.equals("companion_party") ? 120 : 100,0,true)));
    }
    @Override public MapCodec<GuavaGameTest> codec() { return CODEC; }
    @Override protected MutableComponent typeDescription() { return Component.literal(this.scenario); }
    @Override public void run(GameTestHelper h) {
        if(scenario.startsWith("apex_pillar_")) {
            String type=scenario.substring("apex_pillar_".length());boolean flying=type.startsWith("flying_");if(flying)type=type.substring(7);
            for(var kind:ApexKind.values())if(kind.id.equals(type)) {ApexReviewGameTest.pillar(h,kind,flying);return;}
        }
        switch (this.scenario) {
            case "scale_night" -> {try{ProgressionScaleGameTest.night(h);}catch(Exception e){throw new RuntimeException(e);}return;}
            case "scale_deaths" -> {ProgressionScaleGameTest.deaths(h);return;}
            case "scale_audit" -> {ProgressionScaleGameTest.audit(h);return;}
            case "infusion_native" -> {InfusionGameTest.drink(h);return;}
            case "infusion_sources" -> {InfusionGameTest.sources(h);return;}
            case "rebalance_supply" -> {RebalanceGameTest.supply(h);return;}
            case "rebalance_pressure" -> {RebalanceGameTest.pressure(h);return;}
            case "rebalance_defence" -> {RebalanceGameTest.defence(h);return;}
            case "workshop_teaching" -> {WorkshopLearningGameTest.teaching(h);return;}
            case "workshop_building" -> {WorkshopLearningGameTest.building(h);return;}
            case "workshop_replacement" -> {WorkshopLearningGameTest.replacement(h);return;}
            case "workshop_migration" -> {WorkshopLearningGameTest.migration(h);return;}
            case "workshop_walking" -> {WorkshopLearningGameTest.walking(h);return;}
            case "apprentice_recipe" -> {WorkshopApprenticeGameTest.recipe(h);return;}
            case "apprentice_rules" -> {WorkshopApprenticeGameTest.rules(h);return;}
            case "apprentice_parcel" -> {WorkshopApprenticeGameTest.parcel(h);return;}
            case "apprentice_walking" -> {WorkshopApprenticeGameTest.walking(h);return;}
            case "notes_recipe" -> {WorkshopNotesGameTest.recipe(h);return;}
            case "notes_safety" -> {WorkshopNotesGameTest.safety(h);return;}
            case "notes_boundaries" -> {WorkshopNotesGameTest.boundaries(h);return;}
            case "notes_walking" -> {WorkshopNotesGameTest.walking(h);return;}
            case "furnace_processing" -> {FurnaceWorkGameTest.processing(h);return;}
            case "furnace_safety" -> {FurnaceWorkGameTest.safety(h);return;}
            case "furnace_claims" -> {FurnaceWorkGameTest.claims(h);return;}
            case "furnace_ai" -> {FurnaceWorkGameTest.ai(h);return;}
            case "worksite_capacity" -> {WorksiteLinksGameTest.capacity(h);return;}
            case "worksite_parcels" -> {WorksiteLinksGameTest.parcels(h);return;}
            case "worksite_furnace" -> {WorksiteLinksGameTest.furnace(h);return;}
            case "rescue_policy" -> {CivicRescueGameTest.policy(h);return;}
            case "rescue_chain" -> {CivicRescueGameTest.chain(h);return;}
            case "rescue_callbacks" -> {CivicRescueGameTest.callbacks(h);return;}
            case "rescue_blocked" -> {CivicRescueGameTest.blocked(h);return;}
            case "rescue_recovery" -> {CivicRescueGameTest.recovery(h);return;}
            case "rescue_authority" -> {CivicRescueGameTest.authority(h);return;}
            case "rescue_ai" -> {CivicRescueGameTest.ai(h);return;}
            case "scale_acquisition" -> {try{AcquisitionAuditGameTest.run(h);}catch(Exception e){throw new RuntimeException(e);}return;}
            case "fields_coppice" -> {CivicFieldsGameTest.coppice(h);return;}
            case "fields_journey" -> {CivicFieldsGameTest.journey(h);return;}
            case "fields_protection" -> {CivicFieldsGameTest.protection(h);return;}
            case "fields_ownership" -> {CivicFieldsGameTest.ownership(h);return;}
            case "fields_ai" -> {CivicFieldsGameTest.ai(h);return;}
            case "commons_planning" -> {CommonsGameTest.planning(h);return;}
            case "commons_planning_limits" -> {CommonsGameTest.planningLimits(h);return;}
            case "commons_claims" -> {CommonsGameTest.claims(h);return;}
            case "commons_recipes" -> {CommonsGameTest.recipes(h);return;}
            case "commons_scarcity" -> {CommonsGameTest.scarcity(h);return;}
            case "construction_parcel" -> {CommonsGameTest.parcel(h);return;}
            case "construction_fitting" -> {CommonsGameTest.fitting(h);return;}
            case "construction_repair" -> {CommonsGameTest.repair(h);return;}
            case "construction_helper" -> {CommonsGameTest.helper(h);return;}
            case "construction_ai" -> {CommonsGameTest.ai(h);return;}
            case "culture_ai" -> {CourierCultureGameTest.ai(h);return;}
            case "culture_archive" -> {CourierCultureGameTest.archive(h);return;}
            case "culture_books" -> {CourierCultureGameTest.books(h);return;}
            case "culture_interruptions" -> {CourierCultureGameTest.interruptions(h);return;}
            case "culture_limits" -> {CourierCultureGameTest.limits(h);return;}
            case "culture_stale" -> {CourierCultureGameTest.stale(h);return;}
            case "clinic_first_aid" -> {ClinicGameTest.firstAid(h);return;}
            case "clinic_first_aid_callbacks" -> {ClinicGameTest.firstAidCallbacks(h);return;}
            case "clinic_ai" -> {ClinicGameTest.ai(h);return;}
            case "clinic_stock" -> {ClinicGameTest.stock(h);return;}
            case "clinic_callbacks" -> {ClinicGameTest.callbacks(h);return;}
            case "clinic_lesson" -> {ClinicGameTest.lesson(h);return;}
            case "clinic_limits" -> {ClinicGameTest.limits(h);return;}
            case "clinic_craft" -> {ClinicGameTest.craft(h);return;}
            case "tactics_learning" -> {TacticsGameTest.learning(h);return;}
            case "tactics_coordination" -> {TacticsGameTest.coordination(h);return;}
            case "field_harvest" -> {FieldLearningGameTest.harvest(h);return;}
            case "field_garden" -> {FieldLearningGameTest.garden(h);return;}
            case "field_nursery" -> {FieldLearningGameTest.nursery(h);return;}
            case "field_safety" -> {FieldLearningGameTest.safety(h);return;}
            case "field_scout" -> {FieldLearningGameTest.scout(h);return;}
            case "field_gathering" -> {FieldLearningGameTest.gathering(h);return;}
            case "guild_habitation" -> {GuildProjectsGameTest.habitation(h);return;}
            case "guild_ammo" -> GuildProjectsGameTest.ammo(h);
            case "guild_workshop" -> GuildProjectsGameTest.workshop(h);
            case "guild_outpost" -> GuildProjectsGameTest.outpost(h);
            case "guild_towns" -> GuildProjectsGameTest.towns(h);
            case "guild_wagon" -> GuildProjectsGameTest.wagon(h);
            case "guild_worldgen" -> {try{GuildWorldgenGameTest.run(h);}catch(Exception e){throw new RuntimeException(e);}}
            case "review_crown_director" -> {CombatReviewGameTest.director(h);return;}
            case "review_launcher" -> CombatReviewGameTest.launcher(h);
            case "review_shield" -> CombatReviewGameTest.shield(h);
            case "review_crown_beam" -> CombatReviewGameTest.beam(h);
            case "review_crown_pack" -> {CombatReviewGameTest.pack(h);return;}
            case "equipment_modes" -> EquipmentModesGameTest.weapons(h);
            case "equipment_support" -> EquipmentModesGameTest.support(h);
            case "route_watch_safety" -> DeliveryGameTest.watchSafety(h);
            case "route_food" -> DeliveryGameTest.food(h);
            case "route_double" -> DeliveryGameTest.doubleChest(h);
            case "route_pause" -> DeliveryGameTest.pause(h);
            case "route_observation" -> DeliveryGameTest.observation(h);
            case "route_transfer" -> DeliveryGameTest.transfer(h);
            case "route_changes" -> DeliveryGameTest.changes(h);
            case "route_limits" -> DeliveryGameTest.limits(h);
            case "route_ai" -> {DeliveryGameTest.ai(h);return;}
            case "wild_founder_veto" -> {WildFamilyGameTest.founderVeto(h);return;}
            case "wild_genesis" -> {WildFamilyGameTest.genesis(h);return;}
            case "wild_works_chain" -> {WildWorksGameTest.chain(h);return;}
            case "wild_works_safety" -> {WildWorksGameTest.safety(h);return;}
            case "wild_works_legacy" -> {WildWorksGameTest.legacy(h);return;}
            case "wild_works_ai" -> {WildWorksGameTest.ai(h);return;}
            case "wild_rest_safety" -> {WildRestGameTest.safety(h);return;}
            case "wild_rest_legacy" -> {WildRestGameTest.legacy(h);return;}
            case "wild_bench_safety" -> {WildWorkbenchGameTest.safety(h);return;}
            case "wild_bench_legacy" -> {WildWorkbenchGameTest.legacy(h);return;}
            case "wild_bench_handoffs" -> {WildWorkbenchGameTest.handoffs(h);return;}
            case "primitive_furniture" -> {PrimitiveFurnitureGameTest.furniture(h);return;}
            case "primitive_limits" -> {PrimitiveFurnitureGameTest.limits(h);return;}
            case "primitive_placement_collision" -> {PrimitiveFurnitureGameTest.collision(h);return;}
            case "primitive_ai" -> {PrimitiveFurnitureGameTest.ai(h);return;}
            case "primitive_family" -> {PrimitiveFurnitureGameTest.family(h);return;}
            case "primitive_family_reserve" -> {PrimitiveFurnitureGameTest.familyReserve(h);return;}
            case "primitive_family_carer" -> {PrimitiveFurnitureGameTest.familyCarer(h);return;}
            case "primitive_cache_payment" -> {PrimitiveCacheGameTest.payment(h);return;}
            case "primitive_cache_safety" -> {PrimitiveCacheGameTest.safety(h);return;}
            case "primitive_cache_callbacks" -> {PrimitiveCacheGameTest.callbacks(h);return;}
            case "primitive_cache_meals" -> {PrimitiveCacheGameTest.meals(h);return;}
            case "primitive_cache_shared_learning" -> {PrimitiveCacheGameTest.sharedLearning(h);return;}
            case "primitive_cache_collision" -> {PrimitiveCacheGameTest.collision(h);return;}
            case "primitive_cache_clearance" -> {PrimitiveCacheGameTest.clearance(h);return;}
            case "civic_haul_capacity" -> {CivicHaulGameTest.capacity(h);return;}
            case "civic_haul_authority" -> {CivicHaulGameTest.authority(h);return;}
            case "civic_recovery_stock" -> {CivicRecoveryGameTest.stock(h);return;}
            case "civic_recovery_ai" -> {CivicRecoveryGameTest.ai(h);return;}
            case "civic_recovery_authority" -> {CivicRecoveryGameTest.authority(h);return;}
            case "civic_recovery_tools" -> {CivicRecoveryGameTest.tools(h);return;}
            case "civic_recovery_axe" -> {CivicRecoveryGameTest.axe(h);return;}
            case "hearth_pause_power" -> {HearthPauseGameTest.pause(h,false);return;}
            case "hearth_pause_lock" -> {HearthPauseGameTest.pause(h,true);return;}
            case "hearth_pause_meals" -> {HearthPauseGameTest.meals(h);return;}
            case "clinic_first_aid_reach" -> {ClinicGameTest.firstAidReach(h);return;}
            case "primitive_cache_player_legacy" -> {PrimitiveCacheGameTest.playerLegacy(h);return;}
            case "primitive_food_tool" -> {PrimitiveFurnitureGameTest.foodTool(h);return;}
            case "civic_tool_custody" -> {CivicToolGameTest.custody(h);return;}
            case "civic_tool_callbacks" -> {CivicToolGameTest.callbacks(h);return;}
            case "primitive_material_demand" -> {PrimitiveFurnitureGameTest.materialDemand(h);return;}
            case "primitive_diet_meals" -> {PrimitiveFurnitureGameTest.dietMeals(h);return;}
            case "wild_harvest_capacity" -> {WildHarvestGameTest.capacity(h);return;}
            case "wild_harvest_receipt" -> {WildHarvestGameTest.receipt(h);return;}
            case "wild_materials" -> {WildFamilyGameTest.materials(h);return;}
            case "wild_nursery" -> {WildFamilyGameTest.nursery(h,false);return;}
            case "wild_nursery_veto" -> {WildFamilyGameTest.nursery(h,true);return;}
            case "wild_limits" -> {WildFamilyGameTest.limits(h);return;}
            case "wild_ai" -> {WildFamilyGameTest.ai(h);return;}
            case "wild_worldgen" -> {com.bunnidogs.mod.worldgen.WildFamilyWorldgenGameTest.run(h);return;}
            case "hearth_conservation" -> SocietyGameTest.conservation(h);
            case "hearth_gestures" -> SocietyGameTest.gestures(h);
            case "hearth_ai" -> {SocietyGameTest.ai(h);return;}
            case "hearth_household" -> SocietyGameTest.household(h);
            case "hearth_family" -> SocietyGameTest.family(h);
            case "hunt_progress" -> SocietyGameTest.hunts(h);
            case "entry_materials" -> RealmProgressionGameTest.recipe(h);
            case "lunar_forge" -> LunarUpdateGameTest.forge(h);
            case "barding" -> LunarUpdateGameTest.armour(h);
            case "crown_boundary" -> LunarUpdateGameTest.boundary(h);
            case "lunar_masonry" -> LunarUpdateGameTest.masonry(h);
            case "kit_supplies" -> RealmProgressionGameTest.kits(h);
            case "moon_mining" -> RealmProgressionGameTest.mining(h);
            case "astralite_smith" -> AstraliteGameTest.run(h);
            case "kit_loadouts" -> {
                var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));
                com.bunnidogs.mod.command.KitCommands.apply(p,"vanilla","endgame");
                h.assertTrue(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE),"Vanilla kit equips native armor");
                var enchantments=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);var chest=p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
                h.assertTrue(chest.getEnchantmentLevel(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION))==4 && chest.getEnchantmentLevel(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_PROTECTION))==0 && chest.getEnchantmentLevel(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.THORNS))==0,"Benchmark armor has real Protection IV, without conflicting protection enchantments or Thorns");
                com.bunnidogs.mod.command.KitCommands.apply(p,"bunnidog","endgame");
                h.assertTrue(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getEnchantmentLevel(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING))==1,"Sovereign kit armor supports and carries native Mending");
                boolean flask=false;for(int i=0;i<36;i++) {var stack=p.getInventory().getItem(i);if(stack.is(ModItems.ABYSSAL_FLASK.get())) {
                    flask=stack.get(ModItems.FLASK_COUNT.get())==96;
                    var storage=com.bunnidogs.mod.expedition.FlaskStorage.get(h.getLevel());var id=stack.get(ModItems.FLASK_ID.get());
                    var combat=com.bunnidogs.mod.item.JuiceItem.read(storage.pop(id)).orElseThrow();var recovery=com.bunnidogs.mod.item.JuiceItem.read(storage.top(id)).orElseThrow();
                    var strength=com.bunnidogs.mod.genetics.Juice.effects(combat.genome(),combat.stats(),combat.quality()).stream().filter(e->e.id().equals("minecraft:strength")).findFirst().orElseThrow();
                    var regen=com.bunnidogs.mod.genetics.Juice.effects(recovery.genome(),recovery.stats(),recovery.quality()).stream().filter(e->e.id().equals("minecraft:regeneration")).findFirst().orElseThrow();
                    h.assertTrue(strength.amplifier()==29 && strength.ticks()==1980 && regen.ticks()==1760,"The actual combat/recovery pair has the ninety-nine/eighty-eight-second alternating refresh window");
                    h.assertTrue(combat.quality()==1 && recovery.quality()==1 && combat.stats().get(com.bunnidogs.mod.genetics.Stat.VITALITY)==116
                            && recovery.stats().get(com.bunnidogs.mod.genetics.Stat.SENSES)==120 && regen.amplifier()==4,
                            "Kits use distinct reproducible plain-seed donor lines and golden carriers instead of injected quality");
                    storage.pop(id);
                    for(int drink=2;drink<96;drink++)h.assertTrue(com.bunnidogs.mod.item.JuiceItem.read(storage.pop(id)).orElseThrow().equals(com.bunnidogs.mod.genetics.ExpeditionInfusions.bottle(true,drink%2==0)),"Every remaining endgame entry alternates the exact original combat/recovery references: "+drink);
                    h.assertTrue(storage.size(id)==0,"Endgame has exactly forty-eight of each paid reference, with no surplus entries");
                }}
                h.assertTrue(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getEnchantmentLevel(enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING))==3,"Kit armor carries Unbreaking III");
                h.assertTrue(flask && p.getMaxHealth()==44,"Upgraded kit creates a real stocked world-backed flask and equipped Sovereign hearts");
                com.bunnidogs.mod.command.KitCommands.apply(p,"bunnidog","late");boolean lateFlask=false;
                for(int i=0;i<36;i++) {var stack=p.getInventory().getItem(i);if(!stack.is(ModItems.ABYSSAL_FLASK.get()))continue;
                    var storage=com.bunnidogs.mod.expedition.FlaskStorage.get(h.getLevel());var id=stack.get(ModItems.FLASK_ID.get());lateFlask=stack.get(ModItems.FLASK_COUNT.get())==32 && storage.size(id)==32;
                    for(int drink=0;drink<32;drink++)h.assertTrue(com.bunnidogs.mod.item.JuiceItem.read(storage.pop(id)).orElseThrow().equals(com.bunnidogs.mod.genetics.ExpeditionInfusions.bottle(false,drink%2==0)),"Every late entry alternates its exact original combat/recovery references: "+drink);
                    h.assertTrue(storage.size(id)==0,"Late has exactly sixteen of each reference, with no surplus entries");
                }h.assertTrue(lateFlask,"Late keeps its actual thirty-two-entry world-backed flask");p.discard();
            }
            case "crown_balance" -> CrownDungeonGameTest.balance(h);
            case "crown_wings" -> CrownDungeonGameTest.wings(h);
            case "crown_gate" -> CrownDungeonGameTest.gate(h);
            case "crown_boss" -> CrownDungeonGameTest.boss(h);
            case "crown_rematch" -> CrownDungeonGameTest.rematch(h);
            case "crown_gear" -> CrownDungeonGameTest.gear(h);
            case "crown_attacks" -> {CrownDungeonGameTest.attacks(h);return;}
            case "crown_abandon" -> {CrownDungeonGameTest.abandon(h);return;}
            case "crown_support" -> CrownDungeonGameTest.support(h);
            case "crown_citadel_proto" -> com.bunnidogs.mod.worldgen.CrownWorldgenGameTest.citadel(h);
            case "crown_citadel" -> {CrownCitadelGameTest.run(h);return;}
            case "crown_worldgen" -> com.bunnidogs.mod.worldgen.CrownWorldgenGameTest.run(h);
            case "exp_armor" -> {ExpeditionRewardsGameTest.armor(h);return;}
            case "exp_smith" -> ExpeditionRewardsGameTest.smith(h);
            case "exp_vacuum" -> ExpeditionRewardsGameTest.vacuum(h);
            case "exp_flask" -> ExpeditionRewardsGameTest.flask(h);
            case "exp_launcher" -> ExpeditionRewardsGameTest.launcher(h);
            case "exp_totem" -> {ExpeditionRewardsGameTest.totem(h);return;}
            case "realm_travel" -> {RealmGameTest.travel(h);return;}
            case "realm_ecology" -> RealmGameTest.ecology(h);
            case "realm_ward" -> {RealmGameTest.ward(h);return;}
            case "realm_harness" -> RealmGameTest.harness(h);
            case "realm_lance" -> RealmGameTest.lance(h);
            case "realm_ruin" -> com.bunnidogs.mod.worldgen.RealmRuinGameTest.run(h);
            case "apex_followup_blast" -> ApexFollowupGameTest.blast(h);
            case "apex_anvil_arc" -> ApexFollowupGameTest.anvil(h);
            case "apex_critical_meals" -> {ApexFollowupGameTest.meals(h);return;}
            case "apex_multilayer_dive" -> {ApexFollowupGameTest.dive(h);return;}
            case "apex_client_death" -> ApexReviewGameTest.clientDeath(h);
            case "apex_swimming" -> {ApexReviewGameTest.swimming(h);return;}
            case "apex_final_blast" -> ApexReviewGameTest.blast(h);
            case "apex_debts" -> {ApexReviewGameTest.support(h);return;}
            case "apex_brood_tools" -> {ApexReviewGameTest.brood(h);return;}
            case "apex_tactics" -> { ApexTypesGameTest.tactics(h); return; }
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
            case "companion_recall_detour" -> { CompanionCombatGameTest.recallDetour(h); return; }
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
            case "care_stations" -> {CohesionGameTest.stations(h);return;}
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
            case "orchard_shake" -> {OrchardProgressionGameTest.shake(h);return;}
            case "orchard_picker" -> {OrchardProgressionGameTest.picker(h);return;}
            case "orchard_grove" -> {OrchardProgressionGameTest.grove(h);return;}
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
